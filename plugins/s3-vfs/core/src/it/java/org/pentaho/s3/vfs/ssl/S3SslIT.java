package org.pentaho.s3.vfs.ssl;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.Selectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.pentaho.amazon.s3.S3Details;
import org.pentaho.s3.vfs.support.KettleS3Environment;

import javax.net.ssl.SSLException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The server here speaks HTTPS and presents a certificate no JVM trusts on its own, so reaching it
 * at all is what the connection's trust store options are worth.
 * <p>
 * Both clients are covered in one round trip: the content is written with the synchronous one and
 * copied inside the bucket with the asynchronous one, which are configured separately and so say
 * nothing about each other.
 */
public class S3SslIT {

  private static final String KEY_STORE_PASSWORD = "pass";
  private static final String KEY_ALIAS = "server";
  private static final String TRUST_STORE_PASSWORD = "lapasse";

  @RegisterExtension
  static final S3MockExtension S3_MOCK = S3MockExtension.builder()
    .withSecureConnection( true )
    .withSslParameters( certificate( "server/server_store.p12" ), KEY_STORE_PASSWORD, KEY_ALIAS,
      KEY_STORE_PASSWORD )
    .withInitialBuckets( KettleS3Environment.BUCKET )
    .silent()
    .build();

  private static KettleS3Environment environment;

  @BeforeAll
  static void startEnvironment() throws Exception {
    environment = new KettleS3Environment( S3_MOCK.getServiceEndpoint() );
  }

  @Test
  void reachesAServerHoldingACertificateTheTrustStoreVouchesFor() throws Exception {
    String bucketUri = connect( "trust-store", details -> {
      details.setTrustStoreFilePath( certificate( "truststore.jks" ) );
      details.setTrustStorePassword( TRUST_STORE_PASSWORD );
    } );

    assertRoundTrip( bucketUri, "trust-store" );
  }

  @Test
  void reachesAnyServerWhenAskedToTrustThemAll() throws Exception {
    String bucketUri = connect( "trust-all", details -> details.setTrustAll( "true" ) );

    assertRoundTrip( bucketUri, "trust-all" );
  }

  /**
   * Trusting everything is an answer on its own, so the trust store is never opened and its password
   * does not have to be right. A path that does not resolve is another matter: that one is read
   * before the question of trusting everything comes up, and fails the connection.
   */
  @Test
  void ignoresTheTrustStorePasswordWhenAskedToTrustThemAll() throws Exception {
    String bucketUri = connect( "trust-all-anyway", details -> {
      details.setTrustStoreFilePath( certificate( "truststore.jks" ) );
      details.setTrustStorePassword( "not the password" );
      details.setTrustAll( "true" );
    } );

    assertRoundTrip( bucketUri, "trust-all-anyway" );
  }

  @Test
  void turnsDownTheServerWhenNothingSaysToTrustIt() throws Exception {
    String bucketUri = connect( "no-trust", details -> {
      // nothing: the JVM's own trust store is all there is, and the certificate is not in it
    } );

    Exception thrown = assertThrows( Exception.class,
      () -> write( bucketUri + "/no-trust/refused.txt", "never gets there" ) );
    assertTrue( isSslFailure( thrown ), "expected the handshake to fail, but got " + thrown );
  }

  /** Writes with the synchronous client, then copies within S3, which goes through the asynchronous one. */
  private void assertRoundTrip( String bucketUri, String folder ) throws Exception {
    byte[] content = "carried over TLS".getBytes( StandardCharsets.UTF_8 );

    write( bucketUri + "/" + folder + "/source.txt", content );
    FileObject copy = resolve( bucketUri + "/" + folder + "/copy.txt" );
    copy.copyFrom( resolve( bucketUri + "/" + folder + "/source.txt" ), Selectors.SELECT_SELF );

    assertArrayEquals( content, read( resolve( bucketUri + "/" + folder + "/copy.txt" ) ) );
  }

  private String connect( String name, Consumer<S3Details> trust ) throws Exception {
    S3Details details = environment.details( name );
    trust.accept( details );
    return environment.createConnection( details, KettleS3Environment.BUCKET );
  }

  private void write( String uri, String content ) throws Exception {
    write( uri, content.getBytes( StandardCharsets.UTF_8 ) );
  }

  private void write( String uri, byte[] content ) throws Exception {
    try ( OutputStream out = resolve( uri ).getContent().getOutputStream() ) {
      out.write( content );
    }
  }

  private byte[] read( FileObject file ) throws Exception {
    try ( InputStream in = file.getContent().getInputStream() ) {
      return in.readAllBytes();
    }
  }

  private FileObject resolve( String uri ) throws Exception {
    return environment.vfs().getFileObject( uri );
  }

  private static boolean isSslFailure( Throwable thrown ) {
    for ( Throwable cause = thrown; cause != null && cause != cause.getCause(); cause = cause.getCause() ) {
      if ( cause instanceof SSLException ) {
        return true;
      }
    }
    return false;
  }

  /** S3Mock takes a file path, and the connection reads the store through VFS, so both want an absolute one. */
  private static String certificate( String name ) {
    try {
      Path path = Paths.get( S3SslIT.class.getResource( "/certs/" + name ).toURI() );
      return path.toString();
    } catch ( Exception e ) {
      throw new IllegalStateException( "missing test certificate: " + name, e );
    }
  }
}
