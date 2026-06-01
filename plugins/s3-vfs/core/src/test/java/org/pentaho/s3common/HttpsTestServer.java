package org.pentaho.s3common;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/** Simple HTTPS single-response server */
public class HttpsTestServer implements AutoCloseable {

  private final String responseContent;
  private final SslContextFactory.Server sslContextFactory;

  private Server server;

  public HttpsTestServer( SslContextFactory.Server sslContextFactory, String response ) {
    this.sslContextFactory = sslContextFactory;
    this.responseContent = response;
  }

  /** @return server port */
  public int start() throws Exception {
    server = new Server();

    // put the s in https
    HttpConfiguration httpsConfig = new HttpConfiguration();
    httpsConfig.addCustomizer( new SecureRequestCustomizer() );

    var connector = new ServerConnector(
      server,
      sslContextFactory,
      new HttpConnectionFactory( httpsConfig )
    );
    connector.setHost( "localhost" );
    connector.setPort( 0 ); // supplied by OS
    server.addConnector( connector );

    server.setHandler( new Handler.Abstract() {

      @Override
      public boolean handle( Request request, Response response, Callback callback ) throws Exception {
        response.setStatus( 200 );
        response.write( true, ByteBuffer.wrap( responseContent.getBytes( StandardCharsets.UTF_8 ) ), callback );
        return true;
      }
    }
    );

    server.start();
    return connector.getLocalPort();
  }

  @Override
  public void close() throws Exception {
    if ( server != null && server.isRunning() ) {
      server.stop();
    }
  }
}
