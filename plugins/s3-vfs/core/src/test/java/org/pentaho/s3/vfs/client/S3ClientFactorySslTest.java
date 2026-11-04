package org.pentaho.s3.vfs.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.net.ssl.SSLException;

import org.apache.commons.vfs2.FileSystemOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.s3.vfs.S3FileSystemConfigBuilder;

import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.utils.SdkAutoCloseable;

/**
 * What the trust store options amount to, against a server presenting a certificate no JVM trusts
 * on its own: connecting to it is only possible because of what the options say.
 * <p>
 * Both HTTP clients are exercised, because the synchronous one is configured with an
 * {@link javax.net.ssl.SSLContext} and the asynchronous one with trust managers, so neither says
 * anything about the other.
 */
class S3ClientFactorySslTest {

  private static final String SERVER_STORE_PASSWORD = "pass";
  private static final String TRUST_STORE_PASSWORD = "lapasse";
  private static final String RESPONSE = "reached the server";

  private final List<SdkAutoCloseable> clients = new ArrayList<>();
  private final S3ClientFactory factory = new S3ClientFactory( new Behavior(), DefaultBowl.getInstance() );

  private HttpsTestServer server;
  private URI serverUri;

  /** Connection properties are read through Encr, which needs an encoder. */
  @BeforeAll
  static void initKettle() throws KettleException {
    KettleClientEnvironment.init();
  }

  @BeforeEach
  void startServer() throws Exception {
    server = new HttpsTestServer( HttpsTestServer.serverContext( getCertificatePath( "server/server_store.p12" ),
      SERVER_STORE_PASSWORD ), RESPONSE );
    serverUri = server.start();
  }

  @AfterEach
  void stopServer() {
    clients.forEach( SdkAutoCloseable::close );
    server.close();
  }

  @Test
  void reachesAServerHoldingACertificateTheTrustStoreVouchesFor() throws Exception {
    assertEquals( RESPONSE, SdkHttpProbe.get( syncClient( withTrustStore() ), serverUri ) );
  }

  @Test
  void reachesAServerHoldingACertificateTheTrustStoreVouchesForAsynchronously() throws Exception {
    assertEquals( RESPONSE, SdkHttpProbe.get( asyncClient( withTrustStore() ), serverUri ) );
  }

  @Test
  void reachesAnyServerWhenAskedToTrustThemAll() throws Exception {
    assertEquals( RESPONSE, SdkHttpProbe.get( syncClient( withTrustAll() ), serverUri ) );
  }

  @Test
  void reachesAnyServerWhenAskedToTrustThemAllAsynchronously() throws Exception {
    assertEquals( RESPONSE, SdkHttpProbe.get( asyncClient( withTrustAll() ), serverUri ) );
  }

  /**
   * Trusting everything is an answer on its own, so a trust store that could not be read is beside
   * the point.
   * <p>
   * Waiting on the client failure handling: the store is opened before trusting everything is even
   * considered, and a path that does not resolve leaves the factory with no client to hand back.
   */
  @Test
  void ignoresAnUnusableTrustStoreWhenAskedToTrustThemAll() throws Exception {
    S3Options options = options( cfg -> {
      cfg.setTrustStoreFilePath( "/no/such/store.jks" );
      cfg.setTrustStorePassword( "not the password" );
      cfg.setTrustAll( "true" );
    } );

    assertEquals( RESPONSE, SdkHttpProbe.get( syncClient( options ), serverUri ) );
    assertEquals( RESPONSE, SdkHttpProbe.get( asyncClient( options ), serverUri ) );
  }

  @Test
  void turnsDownTheServerWhenNothingSaysToTrustIt() {
    S3Options options = options( cfg -> {
      // nothing: the JVM's own trust store is all there is, and the certificate is not in it
    } );

    assertHandshakeFails( () -> SdkHttpProbe.get( syncClient( options ), serverUri ) );
    assertHandshakeFails( () -> SdkHttpProbe.get( asyncClient( options ), serverUri ) );
  }

  /**
   * The wrong password makes the store unreadable, and rather than build a client that would trust
   * nothing the factory builds none at all, leaving the caller on the SDK's defaults.
   */
  @Test
  void buildsNoClientWhenTheTrustStorePasswordIsWrong() {
    S3Options options = options( cfg -> {
      cfg.setTrustStoreFilePath( getCertificatePath( "truststore.jks" ).toString() );
      cfg.setTrustStorePassword( "not the password" );
    } );

    assertFalse( factory.createSslHttpClientBuilder( options ).isPresent() );
    assertFalse( factory.createSslAsyncHttpClientBuilder( options ).isPresent() );
  }

  @Test
  void buildsNoClientWhenTheTrustStoreIsNotThere() {
    S3Options options = options( cfg -> {
      cfg.setTrustStoreFilePath( "/no/such/store.jks" );
      cfg.setTrustStorePassword( TRUST_STORE_PASSWORD );
    } );

    assertFalse( factory.createSslHttpClientBuilder( options ).isPresent() );
    assertFalse( factory.createSslAsyncHttpClientBuilder( options ).isPresent() );
  }

  private S3Options withTrustStore() {
    return options( cfg -> {
      cfg.setTrustStoreFilePath( getCertificatePath( "truststore.jks" ).toString() );
      cfg.setTrustStorePassword( TRUST_STORE_PASSWORD );
    } );
  }

  private S3Options withTrustAll() {
    return options( cfg -> cfg.setTrustAll( "true" ) );
  }

  private static S3Options options( Consumer<S3FileSystemConfigBuilder> settings ) {
    S3FileSystemConfigBuilder configBuilder = new S3FileSystemConfigBuilder( new FileSystemOptions() );
    settings.accept( configBuilder );
    return S3Options.from( configBuilder );
  }

  private SdkHttpClient syncClient( S3Options options ) {
    return register( factory.createSslHttpClientBuilder( options ).orElseThrow().build() );
  }

  private SdkAsyncHttpClient asyncClient( S3Options options ) {
    return register( factory.createSslAsyncHttpClientBuilder( options ).orElseThrow().build() );
  }

  private <T extends SdkAutoCloseable> T register( T client ) {
    clients.add( client );
    return client;
  }

  private static Path getCertificatePath( String name ) {
    try {
      return Paths.get( S3ClientFactorySslTest.class.getResource( "/certs/" + name ).toURI() );
    } catch ( Exception e ) {
      throw new IllegalStateException( "missing test certificate: " + name, e );
    }
  }

  private static void assertHandshakeFails( Executable request ) {
    Throwable thrown = assertThrows( Exception.class, request );
    assertTrue( isSslFailure( thrown ), "expected the handshake to fail, but got " + thrown );
  }

  private static boolean isSslFailure( Throwable thrown ) {
    for ( Throwable cause = thrown; cause != null && cause != cause.getCause(); cause = cause.getCause() ) {
      if ( cause instanceof SSLException ) {
        return true;
      }
    }
    return false;
  }
}
