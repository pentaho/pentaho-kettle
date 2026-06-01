package org.pentaho.s3common;

import static org.junit.Assert.assertEquals;

import org.pentaho.amazon.s3.S3Details;
import org.pentaho.amazon.s3.provider.S3Provider;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.net.ssl.SSLHandshakeException;

import com.amazonaws.ClientConfiguration;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class S3CommonFileSystemMutTlsTest {

  private static Path certsBasePath;

  private static HttpsTestServer server;
  private static String serverUrl;

  private static final String SERVER_RESP = "Hellooo";

  @BeforeClass
  public static void setUpAll() throws Exception {
    certsBasePath = Paths.get( S3CommonFileSystemSslTest.class.getClassLoader().getResource( "certs" ).toURI() );

    var mtlsSsl = new SslContextFactory.Server();
    mtlsSsl.setKeyStoreType( "PKCS12" );
    mtlsSsl.setKeyStorePath( getCertPath( "server/server_store.p12" ) );
    mtlsSsl.setKeyStorePassword( "pass" );

    // mTLS stuff
    mtlsSsl.setNeedClientAuth( true );
    mtlsSsl.setTrustStorePath( getCertPath( "mtls/truststore_clients.jks" ) );
    mtlsSsl.setTrustStorePassword( "cepass" );

    server = new HttpsTestServer( mtlsSsl, SERVER_RESP );
    int mtlsPort = server.start();
    serverUrl = "https://localhost:" + mtlsPort + "/";
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
  public void testKeyStore() throws Exception {
    var details = new S3Details();
    details.setTrustAll( "true" );
    // same in client and server bc this keystore has both public cert and private key
    details.setKeyStoreFilePath( getCertPath( "mtls/truststore_clients.jks" ) );
    details.setKeyStorePassword( "cepass" );
    var s3Opts = getS3Opts( details );

    var clientConfig = new ClientConfiguration();
    S3CommonFileSystem.setSslContext( clientConfig, s3Opts );
    var sslFactory = clientConfig.getApacheHttpClientConfig().getSslSocketFactory();
    testSslConnection( sslFactory );
  }

  @Test( expected = SSLHandshakeException.class )
  public void testNoKeyStoreFail() throws Exception {
    var details = new S3Details();
    details.setTrustAll( "true" );
    var s3Opts = getS3Opts( details );

    var clientConfig = new ClientConfiguration();
    S3CommonFileSystem.setSslContext( clientConfig, s3Opts );
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
