package org.pentaho.di.ui.core;

import static org.junit.Assert.assertEquals;

import java.io.OutputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.HttpServer;
import org.eclipse.jface.dialogs.DialogSettings;
import org.junit.Test;

public class JFaceXmlSecurityTest {

  @Test
  public void ordinaryDialogSettingsLoad() {
    DialogSettings settings = new DialogSettings( "initial" );
    settings.load( new StringReader( "<section name=\"test\"><item key=\"key\" value=\"value\"/></section>" ) );
    assertEquals( "value", settings.get( "key" ) );
  }

  @Test
  public void resourceLocatorWorksWithEquinox() throws Exception {
    Class<?> locator = Class.forName( "org.eclipse.jface.resource.ResourceLocator" );
    assertEquals( Optional.empty(), locator.getMethod( "locate", String.class, String.class )
      .invoke( null, "missing", "missing" ) );
  }

  @Test
  public void externalDtdCannotBeFetched() throws Exception {
    AtomicInteger requests = new AtomicInteger();
    HttpServer server = HttpServer.create( new InetSocketAddress( "127.0.0.1", 0 ), 0 );
    server.createContext( "/external.dtd", exchange -> {
      requests.incrementAndGet();
      byte[] body = "<!ENTITY marker \"controlled\">".getBytes( StandardCharsets.UTF_8 );
      exchange.sendResponseHeaders( 200, body.length );
      try ( OutputStream out = exchange.getResponseBody() ) {
        out.write( body );
      }
    } );
    server.start();
    try {
      String xml = "<!DOCTYPE section SYSTEM \"http://127.0.0.1:" + server.getAddress().getPort()
        + "/external.dtd\"><section name=\"test\">&marker;</section>";
      try {
        new DialogSettings( "initial" ).load( new StringReader( xml ) );
      } catch ( ClassCastException e ) {
        if ( requests.get() == 0 ) {
          throw e;
        }
      }
      assertEquals( "DialogSettings must not resolve external DTDs", 0, requests.get() );
    } finally {
      server.stop( 0 );
    }
  }
}