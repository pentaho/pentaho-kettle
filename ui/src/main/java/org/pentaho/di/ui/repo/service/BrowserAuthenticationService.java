/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.ui.repo.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * HTTP callback authentication service for Pentaho Repository.
 * Opens system browser, starts local HTTP callback server, captures JSESSIONID.
 */
public class BrowserAuthenticationService {

  private static final LogChannelInterface log = new LogChannel( "BrowserAuthenticationService" );
  static final int CALLBACK_PORT = 8282;
  private static final int TIMEOUT_SECONDS = 300; // 5 minutes
  static final String PARAM_AUTHORIZATION_URI = "authorizationUri";

  private HttpServer callbackServer;
  private CompletableFuture<SessionInfo> sessionFuture;

  /**
   * Initiates HTTP callback authentication flow.
   *
   * @param serverUrl The Pentaho server URL
   * @return CompletableFuture that completes when session info is received
   */
  public CompletableFuture<SessionInfo> authenticate( String serverUrl ) {
    return authenticate( serverUrl, null );
  }

  public CompletableFuture<SessionInfo> authenticate( String serverUrl, String authorizationUri ) {
    sessionFuture = new CompletableFuture<>();

    try {
      startCallbackServer();
      String authUrl = buildAuthenticationUrl( serverUrl, authorizationUri );
      openSystemBrowser( authUrl );

      log.logBasic( "HTTP callback authentication initiated. Waiting for callback..." );

      return sessionFuture.orTimeout( TIMEOUT_SECONDS, TimeUnit.SECONDS )
        .whenComplete( ( result, error ) -> {
          stopCallbackServer();
          if ( error != null ) {
            log.logError( "HTTP callback authentication failed or timed out", error );
          }
        } );

    } catch ( Exception e ) {
      log.logError( "Failed to initiate HTTP callback authentication", e );
      sessionFuture.completeExceptionally( e );
      stopCallbackServer();
      return sessionFuture;
    }
  }

  /**
   * Starts the local HTTP callback server.
   */
  void startCallbackServer() throws IOException {
    String callbackPath = System.getProperty("pentaho.repository.browser.auth.callback.path", "/pentaho/auth/callback");
    callbackServer = createHttpServer( CALLBACK_PORT );
    callbackServer.createContext( callbackPath, new CallbackHandler() );
    callbackServer.setExecutor( null );
    callbackServer.start();
  }

  /**
   * Creates an HttpServer bound to the given port.
   * Package-visible to allow overriding in tests.
   */
  HttpServer createHttpServer( int port ) throws IOException {
    return HttpServer.create( new InetSocketAddress( port ), 0 );
  }

  /**
   * Stops the HTTP callback server.
   */
  void stopCallbackServer() {
    if ( callbackServer != null ) {
      callbackServer.stop( 0 );
      log.logBasic( "HTTP callback server stopped" );
      callbackServer = null;
    }
  }

  /**
   * Builds the authentication URL that will be opened in the browser.
   * The callback URL uses the machine's reachable IP address so the Pentaho
   * server can reach the local callback server from any network location.
   */
  String buildAuthenticationUrl( String serverUrl ) {
    return buildAuthenticationUrl( serverUrl, null );
  }

  String buildAuthenticationUrl( String serverUrl, String authorizationUri ) {
    String callbackPath = System.getProperty("pentaho.repository.browser.auth.callback.path", "/pentaho/auth/callback");
    String baseUrl = serverUrl.endsWith( "/" ) ? serverUrl.substring( 0, serverUrl.length() - 1 ) : serverUrl;
    String callbackHost = resolveCallbackHost( baseUrl );
    String callbackUrl = "http://" + callbackHost + ":" + CALLBACK_PORT + callbackPath;

    StringBuilder authUrl = new StringBuilder( baseUrl )
      .append( "/plugin/browser-auth/api/login?callback=" )
      .append( encodeURIComponent( callbackUrl ) );

    if ( authorizationUri != null && !authorizationUri.trim().isEmpty() ) {
      authUrl.append( '&' )
        .append( PARAM_AUTHORIZATION_URI )
        .append( '=' )
        .append( encodeURIComponent( authorizationUri.trim() ) );
      log.logDebug( "Building auth URL with provider - authorizationUri: " + authorizationUri );
    } else {
      log.logDebug( "Building auth URL WITHOUT provider - authorizationUri is null or empty" );
    }

    String finalUrl = authUrl.toString();
    log.logDebug( "Final authentication URL: " + finalUrl );
    return finalUrl;
  }
/**
  * Resolves the host to use for the local HTTP callback server.
   * <p>
   * Always returns a locally reachable address (loopback or a selected
    * local interface) rather than deriving the host from the Pentaho
   * server URL.
    */
  String resolveCallbackHost( String baseUrl ) {
    try {
      URI uri = URI.create( baseUrl );
      if ( uri.getHost() != null && !uri.getHost().isEmpty() ) {
        return uri.getHost();
      }
    } catch ( Exception e ) {
      log.logDebug( "Could not parse server URL host, falling back to local callback host", e );
    }
    return getLocalCallbackHost();
  }

  /**
   * Returns the best reachable IP address of this machine for use in the
   * callback URL. Prefers a non-loopback address so the Pentaho server can
   * call back even when it is running on a different host. Falls back to
   * {@code localhost} if no suitable address can be determined.
   * <p>
   * Package-visible to allow overriding in tests.
   */
  String getLocalCallbackHost() {
    try {
      InetAddress address = InetAddress.getLocalHost();
      if ( !address.isLoopbackAddress() ) {
        return address.getHostAddress();
      }
    } catch ( Exception e ) {
      log.logDebug( "Could not resolve local host address, falling back to localhost", e );
    }
    return "localhost";
  }

  /**
   * Opens the system default browser.
   * Package-visible to allow overriding in tests.
   */
  void openSystemBrowser( String url ) throws IOException {
    if ( Desktop.isDesktopSupported() ) {
      Desktop desktop = Desktop.getDesktop();
      if ( desktop.isSupported( Desktop.Action.BROWSE ) ) {
        desktop.browse( URI.create( url ) );
        log.logBasic( "Opened system browser: " + url );
        return;
      }
    }

    // OS-specific fallback
    String os = System.getProperty( "os.name" ).toLowerCase();
    Runtime runtime = Runtime.getRuntime();

    if ( os.contains( "win" ) ) {
      runtime.exec( "rundll32 url.dll,FileProtocolHandler " + url );
    } else if ( os.contains( "mac" ) ) {
      runtime.exec( "open " + url );
    } else if ( os.contains( "nix" ) || os.contains( "nux" ) ) {
      runtime.exec( "xdg-open " + url );
    } else {
      throw new IOException( "Cannot open browser on OS: " + os );
    }
  }

  /**
   * URL encoding utility.
   */
  static String encodeURIComponent( String value ) {
    try {
      return URLEncoder.encode( value, StandardCharsets.UTF_8 )
        .replace( "+", "%20" )
        .replace( "%21", "!" )
        .replace( "%27", "'" )
        .replace( "%28", "(" )
        .replace( "%29", ")" )
        .replace( "%7E", "~" );
    } catch ( Exception e ) {
      return value;
    }
  }

  /**
   * HTML escapes untrusted user input to prevent XSS/HTML injection.
   * Escapes: &, <, >, ", '
   */
  static String escapeHtml( String value ) {
    if ( value == null ) {
      return "";
    }
    return value
      .replace( "&", "&amp;" )
      .replace( "<", "&lt;" )
      .replace( ">", "&gt;" )
      .replace( "\"", "&quot;" )
      .replace( "'", "&#39;" );
  }

  /**
   * HTTP handler for the callback endpoint.
   */
  class CallbackHandler implements HttpHandler {
    @Override
    public void handle( HttpExchange exchange ) throws IOException {
      try {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQueryParams( query );

        String jsessionId = params.get( "jsessionid" );
        String username = params.get( "username" );
        String error = params.get( "error" );

        if ( error != null && !error.isEmpty() ) {
          log.logError( "Authentication error received: " + error );
          sendResponse( exchange, 400,
            "<html><body><h2>Authentication Failed</h2>"
              + "<p>Error: " + escapeHtml( error ) + "</p></body></html>" );
          sessionFuture.completeExceptionally( new Exception( "Authentication failed: " + error ) );
          return;
        }

        if ( jsessionId == null || jsessionId.isEmpty() ) {
          log.logError( "No JSESSIONID received in callback" );
          sendResponse( exchange, 400,
            "<html><body><h2>Authentication Failed</h2>"
              + "<p>No session ID received</p></body></html>" );
          sessionFuture.completeExceptionally( new Exception( "No JSESSIONID received" ) );
          return;
        }

        SessionInfo sessionInfo = new SessionInfo( jsessionId, username );
        log.logBasic( "Successfully received session info for user: " + username );
        sessionFuture.complete( sessionInfo );

      } catch ( Exception e ) {
        log.logError( "Error handling callback", e );
        try {
          sendResponse( exchange, 500,
            "<html><body><h2>Authentication Failed</h2>"
              + "<p>Error: " + escapeHtml( e.getMessage() ) + "</p></body></html>" );
        } catch ( IOException ioe ) {
          log.logError( "Failed to send error response", ioe );
        }
        sessionFuture.completeExceptionally( e );
      }
    }

    void sendResponse( HttpExchange exchange, int statusCode, String response ) throws IOException {
      byte[] bytes = response.getBytes( StandardCharsets.UTF_8 );
      exchange.getResponseHeaders().set( "Content-Type", "text/html; charset=UTF-8" );
      exchange.sendResponseHeaders( statusCode, bytes.length );
      try ( OutputStream os = exchange.getResponseBody() ) {
        os.write( bytes );
      }
    }

    Map<String, String> parseQueryParams( String query ) {
      Map<String, String> params = new HashMap<>();
      if ( query == null || query.isEmpty() ) {
        return params;
      }

      String[] pairs = query.split( "&" );
      for ( String pair : pairs ) {
        int idx = pair.indexOf( "=" );
        if ( idx > 0 ) {
          String key = URLDecoder.decode( pair.substring( 0, idx ), StandardCharsets.UTF_8 );
          String value = URLDecoder.decode( pair.substring( idx + 1 ), StandardCharsets.UTF_8 );
          params.put( key, value );
        }
      }
      return params;
    }
  }

  /**
   * Container for session information from HTTP callback.
   */
  public static class SessionInfo {
    private final String jsessionId;
    private final String username;

    public SessionInfo( String jsessionId, String username ) {
      this.jsessionId = jsessionId;
      this.username = username;
    }

    public String getJsessionId() {
      return jsessionId;
    }

    public String getUsername() {
      return username;
    }
  }

}

