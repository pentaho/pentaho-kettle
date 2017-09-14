/*!
* Copyright 2010 - 2017 Pentaho Corporation.  All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.pentaho.googledrive.vfs.util;

import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.util.Throwables;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

public class CustomLocalServerReceiver implements VerificationCodeReceiver {

  private Server server;
  String code;
  String error;
  private int port;
  private final String host;
  private String url;

  public CustomLocalServerReceiver() {
    this( "localhost", -1 );
  }

  CustomLocalServerReceiver( String host, int port ) {
    this.host = host;
    this.port = port;
  }

  public void setUrl( String url ) {
    this.url = url;
  }

  public String getRedirectUri() throws IOException {
    if ( this.port == -1 ) {
      this.port = getUnusedPort();
    }

    this.server = new Server( this.port );
    Connector[] arr$ = this.server.getConnectors();
    int len$ = arr$.length;

    for ( int i$ = 0; i$ < len$; ++i$ ) {
      Connector c = arr$[i$];
      c.setHost( this.host );
    }

    this.server.addHandler( new CustomLocalServerReceiver.CallbackHandler() );

    try {
      this.server.start();
    } catch ( Exception var5 ) {
      Throwables.propagateIfPossible( var5 );
      throw new IOException( var5 );
    }

    return "http://" + this.host + ":" + this.port + "/Callback/success.html";
  }

  public String waitForCode() throws IOException {
    return this.code;
  }

  public void stop() throws IOException {
    if ( this.server != null ) {
      try {
        this.server.stop();
      } catch ( Exception var2 ) {
        Throwables.propagateIfPossible( var2 );
        throw new IOException( var2 );
      }
      this.server = null;
    }
  }

  public String getHost() {
    return this.host;
  }

  public int getPort() {
    return this.port;
  }

  private static int getUnusedPort() throws IOException {
    Socket s = new Socket();
    s.bind( (SocketAddress) null );

    int var1;
    try {
      var1 = s.getLocalPort();
    } finally {
      s.close();
    }
    return var1;
  }

  class CallbackHandler extends WebAppContext {

    CallbackHandler() {
      URL warUrl = this.getClass().getClassLoader().getResource( "success_page" );
      String warUrlString = warUrl.toExternalForm();
      setResourceBase( warUrlString );
      setContextPath( "/Callback" );
    }

    public void handle( String target, HttpServletRequest request, HttpServletResponse response, int dispatch )
        throws IOException, ServletException {
      if ( target.contains( "/Callback" ) ) {

        CustomLocalServerReceiver.this.error = request.getParameter( "error" );
        if ( CustomLocalServerReceiver.this.code == null ) {
          CustomLocalServerReceiver.this.code = request.getParameter( "code" );
        }
        if ( CustomLocalServerReceiver.this.url != null && CustomLocalServerReceiver.this.error != null
            && CustomLocalServerReceiver.this.error.equals( "access_denied" ) ) {
          response.sendRedirect( CustomLocalServerReceiver.this.url );
        } else {
          super.handle( target, request, response, dispatch );
        }
        ( (Request) request ).setHandled( true );
      }
    }
  }

  public static final class Builder {
    private String host = "localhost";
    private int port = -1;

    public Builder() {
    }

    public CustomLocalServerReceiver build() {
      return new CustomLocalServerReceiver( this.host, this.port );
    }

    public String getHost() {
      return this.host;
    }

    public CustomLocalServerReceiver.Builder setHost( String host ) {
      this.host = host;
      return this;
    }

    public int getPort() {
      return this.port;
    }

    public CustomLocalServerReceiver.Builder setPort( int port ) {
      this.port = port;
      return this;
    }
  }
}
