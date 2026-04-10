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
    callbackServer = createHttpServer( CALLBACK_PORT );
    callbackServer.createContext( "/pentaho/auth/callback", new CallbackHandler() );
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
    String baseUrl = serverUrl.endsWith( "/" ) ? serverUrl.substring( 0, serverUrl.length() - 1 ) : serverUrl;
    String callbackHost = resolveCallbackHost( serverUrl );
    String callbackUrl = "http://" + callbackHost + ":" + CALLBACK_PORT + "/pentaho/auth/callback";

    StringBuilder authUrl = new StringBuilder( baseUrl )
      .append( "/plugin/login/api/v0/browser-auth?callback=" )
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
   * Resolves the host to use in the callback URL by extracting it from the
   * server URL. This ensures the callback uses the same host the user
   * configured — if the server URL is {@code localhost}, the callback uses
   * {@code localhost}; if it is an IP like {@code 192.168.1.50}, the
   * callback uses that same IP. Falls back to {@code localhost} when the
   * host cannot be extracted.
   *
   * @param serverUrl the Pentaho server URL configured by the user
   */
  String resolveCallbackHost( String serverUrl ) {
    String host = extractHostFromUrl( serverUrl );
    return ( host != null && !host.isEmpty() ) ? host : getLocalCallbackHost();
  }

  /**
   * Extracts the host component from a URL string.
   * Package-visible to allow overriding in tests.
   */
  String extractHostFromUrl( String url ) {
    if ( url == null || url.isEmpty() ) {
      return null;
    }
    try {
      URI uri = URI.create( url );
      return uri.getHost();
    } catch ( Exception e ) {
      log.logDebug( "Could not extract host from URL: " + url, e );
      return null;
    }
  }

  /**
   * Returns the default fallback host for the callback URL.
   * Package-visible to allow overriding in tests.
   */
  String getLocalCallbackHost() {
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

    // OS-specific fallback using ProcessBuilder with separated arguments
    // to avoid command-injection risks and handle URLs with spaces correctly.
    String os = System.getProperty( "os.name" ).toLowerCase();
    ProcessBuilder pb;

    if ( os.contains( "win" ) ) {
      pb = createProcessBuilder( "rundll32", "url.dll,FileProtocolHandler", url );
    } else if ( os.contains( "mac" ) ) {
      pb = createProcessBuilder( "open", url );
    } else if ( os.contains( "nix" ) || os.contains( "nux" ) ) {
      pb = createProcessBuilder( "xdg-open", url );
    } else {
      throw new IOException( "Cannot open browser on OS: " + os );
    }

    pb.redirectErrorStream( true );
    pb.start();
  }

  /**
   * Creates a {@link ProcessBuilder} with the given command arguments.
   * Package-visible to allow overriding in tests.
   */
  ProcessBuilder createProcessBuilder( String... command ) {
    return new ProcessBuilder( command );
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

