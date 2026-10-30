package org.pentaho.s3.vfs.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

/**
 * An HTTPS server that answers everything with the same body, presenting a certificate a client is
 * only meant to accept if it was told to trust it.
 * <p>
 * Comes from the JDK rather than from a server library so the tests need nothing on the classpath
 * that production does not already have.
 */
public class HttpsTestServer implements AutoCloseable {

  private final SSLContext sslContext;
  private final String responseContent;

  private HttpsServer server;

  public HttpsTestServer( SSLContext sslContext, String responseContent ) {
    this.sslContext = sslContext;
    this.responseContent = responseContent;
  }

  /** @return the address the server is listening on, with the port the OS handed out */
  public URI start() throws Exception {
    server = HttpsServer.create( new InetSocketAddress( "localhost", 0 ), 0 );
    server.setHttpsConfigurator( new HttpsConfigurator( sslContext ) );
    server.createContext( "/", exchange -> {
      byte[] body = responseContent.getBytes( StandardCharsets.UTF_8 );
      exchange.sendResponseHeaders( 200, body.length );
      try ( OutputStream out = exchange.getResponseBody() ) {
        out.write( body );
      }
    } );
    server.start();
    return URI.create( "https://localhost:" + server.getAddress().getPort() + "/" );
  }

  @Override
  public void close() {
    if ( server != null ) {
      server.stop( 0 );
    }
  }

  /** An {@link SSLContext} that serves the certificate held in the given store. */
  public static SSLContext serverContext( Path keyStorePath, String password ) throws Exception {
    KeyStore keyStore = KeyStore.getInstance( "PKCS12" );
    try ( InputStream in = Files.newInputStream( keyStorePath ) ) {
      keyStore.load( in, password.toCharArray() );
    }

    KeyManagerFactory keyManagers = KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );
    keyManagers.init( keyStore, password.toCharArray() );

    SSLContext sslContext = SSLContext.getInstance( "TLS" );
    sslContext.init( keyManagers.getKeyManagers(), null, null );
    return sslContext;
  }
}
