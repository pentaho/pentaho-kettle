package org.pentaho.s3common;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.pentaho.amazon.s3.S3Details;
import org.pentaho.amazon.s3.provider.S3Provider;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.amazonaws.ClientConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class S3CommonFileSystemSslTest {

  private static Path certsBasePath;

  private static HttpsTestServer server;
  private static String serverUrl;

  private static final String SERVER_RESP = "Hellooo";

  @BeforeClass
  public static void setUpAll() throws Exception {
    certsBasePath = Paths.get( S3CommonFileSystemSslTest.class.getClassLoader().getResource( "certs" ).toURI() );

    var serverSsl = new SslContextFactory.Server();
    serverSsl.setKeyStoreType( "PKCS12" );
    serverSsl.setKeyStorePath( getCertPath( "server/server_store.p12" ) );
    serverSsl.setKeyStorePassword( "pass" );
    server = new HttpsTestServer( serverSsl, SERVER_RESP );
    int serverPort = server.start();
    serverUrl = "https://localhost:" + serverPort + "/";
  }

  @AfterClass
  public static void tearDownAll() {
    try {
      server.close();
    } catch ( Exception e ) {
      // who cares
    }
  }

  @Test
  public void testTrustStoreSslContext() throws Exception {
    var details = new S3Details();
    details.setTrustStoreFilePath( getCertPath( "truststore.jks" ) );
    details.setTrustStorePassword( "lapasse" );
    var s3Opts = getS3Opts( details );

    var clientConfig = new ClientConfiguration();
    S3CommonFileSystem.setSslContext( clientConfig, s3Opts.trustStore().get() );
    var sslFactory = clientConfig.getApacheHttpClientConfig().getSslSocketFactory();
    testSslConnection( sslFactory );
  }

  @Test
  public void testTrustStoreSslContextTrustAll() throws Exception {
    var details = new S3Details();
    details.setTrustAll( "true" );
    var s3Opts = getS3Opts( details );

    var clientConfig = new ClientConfiguration();
    S3CommonFileSystem.setSslContext( clientConfig, s3Opts.trustStore().get() );
    var sslFactory = clientConfig.getApacheHttpClientConfig().getSslSocketFactory();
    testSslConnection( sslFactory );
  }

  @Test( expected = javax.net.ssl.SSLHandshakeException.class )
  public void testTrustStoreSslContextNoTrust() throws Exception {
    var s3Opts = S3Options.from( new S3CommonFileSystemConfigBuilder( new S3Provider().getOpts( new S3Details() ) ) );
    assertTrue( "Must not have trust store options by default", s3Opts.trustStore().isEmpty() );

    // more of a sanity test for the setup at this point
    var clientConfig = new ClientConfiguration();
    var sslFactory = clientConfig.getApacheHttpClientConfig().getSslSocketFactory();
    testSslConnection( sslFactory );
  }

  @Test( expected = javax.net.ssl.SSLHandshakeException.class )
  public void testTrustStoreSslContextBadPass() throws Exception {
    var details = new S3Details();
    details.setTrustStoreFilePath( getCertPath( "truststore.jks" ) );
    details.setTrustStorePassword( "wrong" );
    var s3Opts = getS3Opts( details );

    var clientConfig = new ClientConfiguration();
    S3CommonFileSystem.setSslContext( clientConfig, s3Opts.trustStore().get() );
    var sslFactory = clientConfig.getApacheHttpClientConfig().getSslSocketFactory();
    testSslConnection( sslFactory );
  }

  @Test( expected = javax.net.ssl.SSLException.class )
  public void testTrustStoreSslContextNullPass() throws Exception {
    var details = new S3Details();
    details.setTrustStoreFilePath( getCertPath( "truststore.jks" ) );
    details.setTrustStorePassword( null );
    var s3Opts = getS3Opts( details );

    var clientConfig = new ClientConfiguration();
    S3CommonFileSystem.setSslContext( clientConfig, s3Opts.trustStore().get() );
    var sslFactory = clientConfig.getApacheHttpClientConfig().getSslSocketFactory();
    testSslConnection( sslFactory );
  }

  @Test( expected = javax.net.ssl.SSLException.class )
  public void testTrustStoreSslContextEmptyPass() throws Exception {
    var details = new S3Details();
    details.setTrustStoreFilePath( getCertPath( "truststore.jks" ) );
    details.setTrustStorePassword( "" );
    var s3Opts = getS3Opts( details );

    var clientConfig = new ClientConfiguration();
    S3CommonFileSystem.setSslContext( clientConfig, s3Opts.trustStore().get() );
    var sslFactory = clientConfig.getApacheHttpClientConfig().getSslSocketFactory();
    testSslConnection( sslFactory );
  }

  @Test
  public void testTrustStoreSslContextPassAndTrustAll() throws Exception {
    // ignore lone password and trust all
    var details = new S3Details();
    details.setTrustStorePassword( "something" );
    details.setTrustAll( "true" );
    var s3Opts = getS3Opts( details );

    var clientConfig = new ClientConfiguration();
    S3CommonFileSystem.setSslContext( clientConfig, s3Opts.trustStore().get() );
    var sslFactory = clientConfig.getApacheHttpClientConfig().getSslSocketFactory();
    testSslConnection( sslFactory );
  }

  @Test( expected = javax.net.ssl.SSLHandshakeException.class )
  public void testTrustStoreSslContextCertPathAndTrustAll() throws Exception {
    // expect trust store to win
    var details = new S3Details();
    details.setTrustStoreFilePath( getCertPath( "truststore.jks" ) );
    details.setTrustStorePassword( "wrong" );
    details.setTrustAll( "true" );
    var s3Opts = getS3Opts( details );

    var clientConfig = new ClientConfiguration();
    S3CommonFileSystem.setSslContext( clientConfig, s3Opts.trustStore().get() );
    var sslFactory = clientConfig.getApacheHttpClientConfig().getSslSocketFactory();
    testSslConnection( sslFactory );
  }

  private void testSslConnection( ConnectionSocketFactory sslFactory ) throws IOException {
    try ( var httpClient = HttpClients.custom().setSSLSocketFactory( (SSLConnectionSocketFactory) sslFactory ).build();
          var response = httpClient.execute( new HttpGet( serverUrl ) ) ) {
      assertEquals( 200, response.getStatusLine().getStatusCode() );
      assertEquals( SERVER_RESP, EntityUtils.toString( response.getEntity() ) );
    }
  }

  private S3Options getS3Opts( S3Details details ) {
    return S3Options.from( new S3CommonFileSystemConfigBuilder( new S3Provider().getOpts( details ) ) );
  }

  private static String getCertPath( String relativePath ) {
    return certsBasePath.resolve( relativePath ).toAbsolutePath().toString();
  }
}
