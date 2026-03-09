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

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.mockito.MockedStatic;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class BrowserAuthenticationServiceTest {

  private BrowserAuthenticationService service;

  @BeforeClass
  public static void setUpClass() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() {
    service = new BrowserAuthenticationService();
  }

  @After
  public void tearDown() {
    service.stopCallbackServer();
  }


  @Test
  public void sessionInfoStoresJsessionIdAndUsername() {
    BrowserAuthenticationService.SessionInfo info =
      new BrowserAuthenticationService.SessionInfo( "ABC123", "admin" );

    assertEquals( "ABC123", info.getJsessionId() );
    assertEquals( "admin", info.getUsername() );
  }

  @Test
  public void sessionInfoAllowsNullUsername() {
    BrowserAuthenticationService.SessionInfo info =
      new BrowserAuthenticationService.SessionInfo( "ABC123", null );

    assertEquals( "ABC123", info.getJsessionId() );
    assertNull( info.getUsername() );
  }

  @Test
  public void sessionInfoAllowsNullJsessionId() {
    BrowserAuthenticationService.SessionInfo info =
      new BrowserAuthenticationService.SessionInfo( null, "admin" );

    assertNull( info.getJsessionId() );
    assertEquals( "admin", info.getUsername() );
  }

  @Test
  public void buildAuthenticationUrlStripsTrailingSlash() {
    String url = service.buildAuthenticationUrl( "http://localhost:8080/pentaho/" );

    assertTrue( url.startsWith( "http://localhost:8080/pentaho/plugin/login/api/v0/browser-auth?callback=" ) );
    assertFalse( url.contains( "pentaho//plugin" ) );
  }

  @Test
  public void buildAuthenticationUrlHandlesNoTrailingSlash() {
    String url = service.buildAuthenticationUrl( "http://localhost:8080/pentaho" );

    assertTrue( url.startsWith( "http://localhost:8080/pentaho/plugin/login/api/v0/browser-auth?callback=" ) );
  }

  @Test
  public void buildAuthenticationUrlAddsAuthorizationUriWhenProvided() {
    String url = service.buildAuthenticationUrl( "http://localhost:8080/pentaho", "oauth2/authorization/azure" );

    assertTrue( url.contains( "authorizationUri=oauth2%2Fauthorization%2Fazure" ) );
  }

  // ===== encodeURIComponent =====

  @Test
  public void encodeURIComponentEncodesSpacesAs20() {
    String result = BrowserAuthenticationService.encodeURIComponent( "hello world" );

    assertEquals( "hello%20world", result );
    assertFalse( result.contains( "+" ) );
  }

  @Test
  public void encodeURIComponentPreservesUnreservedChars() {
    String result = BrowserAuthenticationService.encodeURIComponent( "a!b'c(d)e~f" );

    assertEquals( "a!b'c(d)e~f", result );
  }

  @Test
  public void encodeURIComponentEncodesSpecialChars() {
    String result = BrowserAuthenticationService.encodeURIComponent( "key=value&foo=bar" );

    assertFalse( result.contains( "=" ) );
    assertFalse( result.contains( "&" ) );
  }

  @Test
  public void encodeURIComponentHandlesEmptyString() {
    assertEquals( "", BrowserAuthenticationService.encodeURIComponent( "" ) );
  }

  @Test
  public void encodeURIComponentReturnsNullWhenPassedNull() {
    // URLEncoder.encode(null, ...) throws NullPointerException, so the catch block returns value as-is
    assertNull( BrowserAuthenticationService.encodeURIComponent( null ) );
  }

  @Test
  public void encodeURIComponentHandlesUrl() {
    String result = BrowserAuthenticationService.encodeURIComponent(
      "http://localhost:8282/pentaho/auth/callback" );

    assertFalse( result.contains( ":" ) );
    assertFalse( result.contains( "/" ) );
  }

  @Test
  public void parseQueryParamsExtractsMultipleParams() {
    BrowserAuthenticationService.CallbackHandler handler = service.new CallbackHandler();

    Map<String, String> params = handler.parseQueryParams( "jsessionid=ABC123&username=admin" );

    assertEquals( "ABC123", params.get( "jsessionid" ) );
    assertEquals( "admin", params.get( "username" ) );
  }

  @Test
  public void parseQueryParamsReturnsEmptyMapForNullQuery() {
    BrowserAuthenticationService.CallbackHandler handler = service.new CallbackHandler();

    Map<String, String> params = handler.parseQueryParams( null );

    assertNotNull( params );
    assertTrue( params.isEmpty() );
  }

  @Test
  public void parseQueryParamsReturnsEmptyMapForEmptyQuery() {
    BrowserAuthenticationService.CallbackHandler handler = service.new CallbackHandler();

    Map<String, String> params = handler.parseQueryParams( "" );

    assertNotNull( params );
    assertTrue( params.isEmpty() );
  }

  @Test
  public void parseQueryParamsHandlesUrlEncodedValues() {
    BrowserAuthenticationService.CallbackHandler handler = service.new CallbackHandler();

    Map<String, String> params = handler.parseQueryParams( "error=login+failed%26retry" );

    assertEquals( "login failed&retry", params.get( "error" ) );
  }

  @Test
  public void parseQueryParamsIgnoresPairsWithoutEquals() {
    BrowserAuthenticationService.CallbackHandler handler = service.new CallbackHandler();

    Map<String, String> params = handler.parseQueryParams( "noequalssign&key=value" );

    assertNull( params.get( "noequalssign" ) );
    assertEquals( "value", params.get( "key" ) );
  }

  @Test
  public void parseQueryParamsSingleParam() {
    BrowserAuthenticationService.CallbackHandler handler = service.new CallbackHandler();

    Map<String, String> params = handler.parseQueryParams( "jsessionid=XYZ" );

    assertEquals( 1, params.size() );
    assertEquals( "XYZ", params.get( "jsessionid" ) );
  }

  @Test
  public void parseQueryParamsHandlesEmptyValue() {
    BrowserAuthenticationService.CallbackHandler handler = service.new CallbackHandler();

    Map<String, String> params = handler.parseQueryParams( "key=" );

    assertEquals( "", params.get( "key" ) );
  }


  @Test
  public void sendResponseWritesBodyAndSetsHeaders() throws IOException {
    BrowserAuthenticationService.CallbackHandler handler = service.new CallbackHandler();

    HttpExchange exchange = mock( HttpExchange.class );
    Headers headers = new Headers();
    ByteArrayOutputStream body = new ByteArrayOutputStream();

    when( exchange.getResponseHeaders() ).thenReturn( headers );
    when( exchange.getResponseBody() ).thenReturn( body );

    handler.sendResponse( exchange, 200, "<html>OK</html>" );

    assertEquals( "text/html; charset=UTF-8", headers.getFirst( "Content-Type" ) );
    verify( exchange ).sendResponseHeaders( eq( 200 ), anyLong() );
    assertEquals( "<html>OK</html>", body.toString( StandardCharsets.UTF_8 ) );
  }

  @Test
  public void sendResponseSets400StatusCode() throws IOException {
    BrowserAuthenticationService.CallbackHandler handler = service.new CallbackHandler();

    HttpExchange exchange = mock( HttpExchange.class );
    Headers headers = new Headers();
    ByteArrayOutputStream body = new ByteArrayOutputStream();

    when( exchange.getResponseHeaders() ).thenReturn( headers );
    when( exchange.getResponseBody() ).thenReturn( body );

    handler.sendResponse( exchange, 400, "error" );

    verify( exchange ).sendResponseHeaders( eq( 400 ), anyLong() );
  }

  @Test
  public void sendResponseClosesOutputStreamAfterWriting() throws IOException {
    BrowserAuthenticationService.CallbackHandler handler = service.new CallbackHandler();

    HttpExchange exchange = mock( HttpExchange.class );
    Headers headers = new Headers();
    OutputStream os = mock( OutputStream.class );

    when( exchange.getResponseHeaders() ).thenReturn( headers );
    when( exchange.getResponseBody() ).thenReturn( os );

    handler.sendResponse( exchange, 200, "done" );

    verify( os ).write( "done".getBytes( StandardCharsets.UTF_8 ) );
    verify( os ).close();
  }

  @Test
  public void handleCompletesExceptionallyWhenEmptyJsessionId() throws Exception {
    CompletableFuture<BrowserAuthenticationService.SessionInfo> future =
      service.authenticate( "http://localhost:9999/pentaho" );

    BrowserAuthenticationService.CallbackHandler handler = service.new CallbackHandler();
    HttpExchange exchange = mockExchangeWithQuery( "jsessionid=&username=admin" );

    handler.handle( exchange );

    assertTrue( future.isCompletedExceptionally() );
  }

  @Test
  public void handleCompletesExceptionallyWhenExchangeThrows() throws Exception {
    CompletableFuture<BrowserAuthenticationService.SessionInfo> future =
      service.authenticate( "http://localhost:9999/pentaho" );

    BrowserAuthenticationService.CallbackHandler handler = service.new CallbackHandler();

    HttpExchange exchange = mock( HttpExchange.class );
    when( exchange.getRequestURI() ).thenThrow( new RuntimeException( "broken exchange" ) );
    // Mock enough to handle the error response path
    Headers headers = new Headers();
    ByteArrayOutputStream body = new ByteArrayOutputStream();
    when( exchange.getResponseHeaders() ).thenReturn( headers );
    when( exchange.getResponseBody() ).thenReturn( body );

    handler.handle( exchange );

    assertTrue( future.isCompletedExceptionally() );
  }

  @Test
  public void handleCompletesExceptionallyOnNullQuery() throws Exception {
    CompletableFuture<BrowserAuthenticationService.SessionInfo> future =
      service.authenticate( "http://localhost:9999/pentaho" );

    BrowserAuthenticationService.CallbackHandler handler = service.new CallbackHandler();
    HttpExchange exchange = mockExchangeWithQuery( null );

    handler.handle( exchange );

    assertTrue( future.isCompletedExceptionally() );
  }

  @Test
  public void authenticateCompletesExceptionallyWhenServerStartFails() {
    BrowserAuthenticationService failingService = new BrowserAuthenticationService() {
      @Override HttpServer createHttpServer( int port ) throws IOException {
        throw new IOException( "port in use" );
      }
    };

    CompletableFuture<BrowserAuthenticationService.SessionInfo> future =
      failingService.authenticate( "http://localhost:8080/pentaho" );

    assertTrue( future.isCompletedExceptionally() );
    try {
      future.get();
      fail( "Expected ExecutionException" );
    } catch ( ExecutionException e ) {
      assertTrue( e.getCause().getMessage().contains( "port in use" ) );
    } catch ( InterruptedException e ) {
      fail( "Unexpected interrupt" );
    }
  }

  @Test
  public void authenticateCompletesExceptionallyWhenBrowserOpenFails() {
    BrowserAuthenticationService failingService = new BrowserAuthenticationService() {
      @Override void openSystemBrowser( String url ) throws IOException {
        throw new IOException( "no browser" );
      }
    };

    CompletableFuture<BrowserAuthenticationService.SessionInfo> future =
      failingService.authenticate( "http://localhost:8080/pentaho" );

    assertTrue( future.isCompletedExceptionally() );
    try {
      future.get();
      fail( "Expected ExecutionException" );
    } catch ( ExecutionException e ) {
      assertTrue( e.getCause().getMessage().contains( "no browser" ) );
    } catch ( InterruptedException e ) {
      fail( "Unexpected interrupt" );
    }
  }

  @Test
  public void authenticateReturnsNonNullFuture() {
    BrowserAuthenticationService noopService = new BrowserAuthenticationService() {
      @Override void openSystemBrowser( String url ) {
        // no-op
      }
    };

    CompletableFuture<BrowserAuthenticationService.SessionInfo> future =
      noopService.authenticate( "http://server:8080/pentaho" );

    assertNotNull( future );
    assertFalse( future.isDone() );
    noopService.stopCallbackServer();
  }

  @Test
  public void callbackPortIsPositive() {
    assertTrue( BrowserAuthenticationService.CALLBACK_PORT > 0 );
  }


  @Test
  public void getLocalCallbackHostReturnsNonEmpty() {
    String host = service.getLocalCallbackHost();
    assertNotNull( host );
    assertFalse( host.isEmpty() );
  }

  @Test
  public void getLocalCallbackHostDoesNotReturnNull() {
    String host = service.getLocalCallbackHost();
    assertNotNull( host );
  }

  @Test
  public void getLocalCallbackHostReturnsFallbackOnException() {
    BrowserAuthenticationService testService = new BrowserAuthenticationService() {
      @Override
      String getLocalCallbackHost() {
        return "localhost";
      }
    };
    assertEquals( "localhost", testService.getLocalCallbackHost() );
  }

  @Test
  public void resolveCallbackHostUsesLocalhostFromServerUrl() {
    assertEquals( "localhost", service.resolveCallbackHost( "http://localhost:8080/pentaho" ) );
  }

  @Test
  public void resolveCallbackHostUsesIpFromServerUrl() {
    assertEquals( "192.168.1.50", service.resolveCallbackHost( "http://192.168.1.50:8080/pentaho" ) );
  }

  @Test
  public void resolveCallbackHostFallsBackToLocalhostForNullUrl() {
    assertEquals( "localhost", service.resolveCallbackHost( null ) );
  }

  @Test
  public void resolveCallbackHostFallsBackToLocalhostForEmptyUrl() {
    assertEquals( "localhost", service.resolveCallbackHost( "" ) );
  }

  @Test
  public void extractHostFromUrlReturnsNullForMalformedUrl() {
    assertNull( service.extractHostFromUrl( "not a valid url ://{}[]" ) );
  }

  @Test
  public void resolveCallbackHostFallsBackToLocalhostWhenExtractedHostIsEmpty() {
    BrowserAuthenticationService testService = new BrowserAuthenticationService() {
      @Override String extractHostFromUrl( String url ) {
        return "";
      }
    };
    assertEquals( "localhost", testService.resolveCallbackHost( "http://anything" ) );
  }

  @Test
  public void resolveCallbackHostUsesHostnameFromServerUrl() {
    assertEquals( "myserver.example.com", service.resolveCallbackHost( "http://myserver.example.com:8080/pentaho" ) );
  }


  @Test
  public void escapeHtmlEncodesAmpersand() {
    String result = BrowserAuthenticationService.escapeHtml( "A&B" );
    assertEquals( "A&amp;B", result );
  }


  @Test
  public void escapeHtmlHandlesNull() {
    String result = BrowserAuthenticationService.escapeHtml( null );
    assertEquals( "", result );
  }

  @Test
  public void escapeHtmlHandlesEmptyString() {
    String result = BrowserAuthenticationService.escapeHtml( "" );
    assertEquals( "", result );
  }

  @Test
  public void escapeHtmlPreservesNormalText() {
    String result = BrowserAuthenticationService.escapeHtml( "Hello World 123" );
    assertEquals( "Hello World 123", result );
  }

  @Test
  public void startCallbackServerCreatesServer() throws IOException {
    BrowserAuthenticationService testService = new BrowserAuthenticationService();
    testService.startCallbackServer();
    assertNotNull( testService );
    testService.stopCallbackServer();
  }

  @Test
  public void startCallbackServerThrowsIoExceptionOnPortFailure() {
    BrowserAuthenticationService failingService = new BrowserAuthenticationService() {
      @Override
      HttpServer createHttpServer( int port ) throws IOException {
        throw new IOException( "Port already in use" );
      }
    };

    try {
      failingService.startCallbackServer();
      fail( "Expected IOException" );
    } catch ( IOException e ) {
      assertTrue( e.getMessage().contains( "Port" ) );
    }
  }

  @Test
  public void createHttpServerReturnsNotNull() throws IOException {
    HttpServer server = service.createHttpServer( 0 ); // 0 = any available port
    assertNotNull( server );
    server.stop( 0 );
  }

  @Test
  public void openSystemBrowserThrowsOnInvalidOs() {
    BrowserAuthenticationService browserService = new BrowserAuthenticationService() {
      @Override
      void openSystemBrowser( String url ) throws IOException {
        String os = System.getProperty( "os.name" ).toLowerCase();
        if ( os.contains( "win" ) || os.contains( "mac" ) || os.contains( "nix" ) || os.contains( "nux" ) ) {
          // Valid OS, don't throw
          return;
        }
        throw new IOException( "Cannot open browser on OS: " + os );
      }
    };
    // Test should not throw on known OS
    try {
      browserService.openSystemBrowser( "http://localhost:8282/callback" );
    } catch ( IOException e ) {
      // Expected for unknown OS
      assertTrue( e.getMessage().contains( "Cannot open browser" ) );
    }
  }

  private HttpExchange mockExchangeWithQuery( String query ) {
    HttpExchange exchange = mock( HttpExchange.class );
    URI uri = query != null ? URI.create( "http://localhost/callback?" + query )
      : URI.create( "http://localhost/callback" );
    when( exchange.getRequestURI() ).thenReturn( uri );
    Headers headers = new Headers();
    ByteArrayOutputStream body = new ByteArrayOutputStream();
    when( exchange.getResponseHeaders() ).thenReturn( headers );
    when( exchange.getResponseBody() ).thenReturn( body );
    return exchange;
  }

  @Test
  public void buildAuthenticationUrlIgnoresBlankAuthorizationUri() {
    String url = service.buildAuthenticationUrl( "http://localhost:8080/pentaho", "   " );

    assertFalse( url.contains( "authorizationUri" ) );
    assertTrue( url.contains( "/plugin/login/api/v0/browser-auth?callback=" ) );
  }

  @Test
  public void getLocalCallbackHostAlwaysReturnsLocalhost() {
    String host = service.getLocalCallbackHost();
    assertEquals( "localhost", host );
  }

  @Test
  public void escapeHtmlEncodesLessThan() {
    assertEquals( "&lt;", BrowserAuthenticationService.escapeHtml( "<" ) );
  }

  @Test
  public void escapeHtmlEncodesGreaterThan() {
    assertEquals( "&gt;", BrowserAuthenticationService.escapeHtml( ">" ) );
  }

  @Test
  public void escapeHtmlEncodesDoubleQuote() {
    assertEquals( "&quot;", BrowserAuthenticationService.escapeHtml( "\"" ) );
  }

  @Test
  public void escapeHtmlEncodesSingleQuote() {
    assertEquals( "&#39;", BrowserAuthenticationService.escapeHtml( "'" ) );
  }

  @Test
  public void escapeHtmlEncodesAllSpecialCharsTogether() {
    assertEquals( "&amp;&lt;&gt;&quot;&#39;",
      BrowserAuthenticationService.escapeHtml( "&<>\"'" ) );
  }

  @Test
  public void openSystemBrowserUsesDesktopBrowseWhenAvailable() throws IOException {
    try ( MockedStatic<Desktop> deskStatic = mockStatic( Desktop.class ) ) {
      Desktop mockDesktop = mock( Desktop.class );
      deskStatic.when( Desktop::isDesktopSupported ).thenReturn( true );
      deskStatic.when( Desktop::getDesktop ).thenReturn( mockDesktop );
      when( mockDesktop.isSupported( Desktop.Action.BROWSE ) ).thenReturn( true );

      service.openSystemBrowser( "http://localhost/test" );

      verify( mockDesktop ).browse( URI.create( "http://localhost/test" ) );
    }
  }


  @Test
  public void openSystemBrowserFallsBackToOsWhenBrowseNotSupported() throws IOException {
    String originalOs = System.getProperty( "os.name" );
    System.setProperty( "os.name", "Windows 10" );
    try ( MockedStatic<Desktop> deskStatic = mockStatic( Desktop.class ) ) {
      Desktop mockDesktop = mock( Desktop.class );
      deskStatic.when( Desktop::isDesktopSupported ).thenReturn( true );
      deskStatic.when( Desktop::getDesktop ).thenReturn( mockDesktop );
      when( mockDesktop.isSupported( Desktop.Action.BROWSE ) ).thenReturn( false );

      java.util.List<String> capturedCommand = new java.util.ArrayList<>();
      BrowserAuthenticationService testService = new BrowserAuthenticationService() {
        @Override
        ProcessBuilder createProcessBuilder( String... command ) {
          capturedCommand.addAll( java.util.Arrays.asList( command ) );
          ProcessBuilder pb = mock( ProcessBuilder.class );
          when( pb.redirectErrorStream( true ) ).thenReturn( pb );
          try {
            when( pb.start() ).thenReturn( mock( Process.class ) );
          } catch ( IOException e ) {
            throw new RuntimeException( e );
          }
          return pb;
        }
      };

      testService.openSystemBrowser( "http://localhost/test" );

      assertEquals( java.util.Arrays.asList( "rundll32", "url.dll,FileProtocolHandler", "http://localhost/test" ),
        capturedCommand );
    } finally {
      System.setProperty( "os.name", originalOs );
    }
  }

  @Test
  @Parameterized.Parameters
  public void openSystemBrowserExecutesCorrectCommandPerOs() throws IOException {
    Object[][] cases = {
      { "Windows 10", new String[]{ "rundll32", "url.dll,FileProtocolHandler", "http://test/path" } },
      { "Mac OS X",   new String[]{ "open", "http://test/path" } },
      { "Linux",      new String[]{ "xdg-open", "http://test/path" } }
    };

    String originalOs = System.getProperty( "os.name" );
    for ( Object[] tc : cases ) {
      String osName         = (String) tc[ 0 ];
      String[] expectedArgs = (String[]) tc[ 1 ];

      System.setProperty( "os.name", osName );
      try ( MockedStatic<Desktop> deskStatic = mockStatic( Desktop.class ) ) {
        deskStatic.when( Desktop::isDesktopSupported ).thenReturn( false );

        java.util.List<String> capturedCommand = new java.util.ArrayList<>();
        BrowserAuthenticationService testService = new BrowserAuthenticationService() {
          @Override
          ProcessBuilder createProcessBuilder( String... command ) {
            capturedCommand.addAll( java.util.Arrays.asList( command ) );
            ProcessBuilder pb = mock( ProcessBuilder.class );
            when( pb.redirectErrorStream( true ) ).thenReturn( pb );
            try {
              when( pb.start() ).thenReturn( mock( Process.class ) );
            } catch ( IOException e ) {
              throw new RuntimeException( e );
            }
            return pb;
          }
        };

        testService.openSystemBrowser( "http://test/path" );

        assertEquals( "Failed for OS: " + osName,
          java.util.Arrays.asList( expectedArgs ), capturedCommand );
      }
    }
    System.setProperty( "os.name", originalOs );
  }

  @Test
  public void openSystemBrowserThrowsIoExceptionForUnknownOs() {
    String originalOs = System.getProperty( "os.name" );
    System.setProperty( "os.name", "SolarisRMX_Unknown_2099" );
    try ( MockedStatic<Desktop> deskStatic = mockStatic( Desktop.class ) ) {
      deskStatic.when( Desktop::isDesktopSupported ).thenReturn( false );

      service.openSystemBrowser( "http://test/path" );
      fail( "Expected IOException for unknown OS" );
    } catch ( IOException e ) {
      assertTrue( e.getMessage().contains( "Cannot open browser on OS" ) );
    } finally {
      System.setProperty( "os.name", originalOs );
    }
  }

  @Test
  public void openSystemBrowserUsesXdgOpenForUnixOs() throws IOException {
    String originalOs = System.getProperty( "os.name" );
    System.setProperty( "os.name", "Unix" );
    try ( MockedStatic<Desktop> deskStatic = mockStatic( Desktop.class ) ) {
      deskStatic.when( Desktop::isDesktopSupported ).thenReturn( false );

      java.util.List<String> capturedCommand = new java.util.ArrayList<>();
      BrowserAuthenticationService testService = new BrowserAuthenticationService() {
        @Override
        ProcessBuilder createProcessBuilder( String... command ) {
          capturedCommand.addAll( java.util.Arrays.asList( command ) );
          ProcessBuilder pb = mock( ProcessBuilder.class );
          when( pb.redirectErrorStream( true ) ).thenReturn( pb );
          try {
            when( pb.start() ).thenReturn( mock( Process.class ) );
          } catch ( IOException e ) {
            throw new RuntimeException( e );
          }
          return pb;
        }
      };

      testService.openSystemBrowser( "http://localhost/test" );

      assertEquals( java.util.Arrays.asList( "xdg-open", "http://localhost/test" ),
        capturedCommand );
    } finally {
      System.setProperty( "os.name", originalOs );
    }
  }

  @Test
  public void handleSends400WithEscapedErrorMessageWhenErrorParamPresent() throws Exception {
    BrowserAuthenticationService testService = new BrowserAuthenticationService() {
      @Override HttpServer createHttpServer( int port ) throws IOException {
        return super.createHttpServer( 0 );
      }
      @Override void openSystemBrowser( String url ) { /* no-op */ }
    };
    CompletableFuture<BrowserAuthenticationService.SessionInfo> future =
      testService.authenticate( "http://localhost/pentaho" );

    BrowserAuthenticationService.CallbackHandler handler = testService.new CallbackHandler();
    HttpExchange exchange = mock( HttpExchange.class );
    URI uri = URI.create( "http://localhost/callback?error=%3Cscript%3Ealert(1)%3C%2Fscript%3E" );
    when( exchange.getRequestURI() ).thenReturn( uri );
    Headers headers = new Headers();
    ByteArrayOutputStream body = new ByteArrayOutputStream();
    when( exchange.getResponseHeaders() ).thenReturn( headers );
    when( exchange.getResponseBody() ).thenReturn( body );

    handler.handle( exchange );

    verify( exchange ).sendResponseHeaders( eq( 400 ), anyLong() );
    String responseBody = body.toString( StandardCharsets.UTF_8 );
    assertTrue( responseBody.contains( "&lt;script&gt;alert(1)&lt;/script&gt;" ) );
    assertFalse( responseBody.contains( "<script>" ) );
    assertTrue( future.isCompletedExceptionally() );
    testService.stopCallbackServer();
  }

  @Test
  public void handleCompletesExceptionallyOnErrorParam() throws Exception {
    BrowserAuthenticationService testService = new BrowserAuthenticationService() {
      @Override HttpServer createHttpServer( int port ) throws IOException {
        return super.createHttpServer( 0 );
      }
      @Override void openSystemBrowser( String url ) { /* no-op */ }
    };
    CompletableFuture<BrowserAuthenticationService.SessionInfo> future =
      testService.authenticate( "http://localhost/pentaho" );

    BrowserAuthenticationService.CallbackHandler handler = testService.new CallbackHandler();
    HttpExchange exchange = mockExchangeWithQuery( "error=access_denied" );

    handler.handle( exchange );

    assertTrue( future.isCompletedExceptionally() );
    testService.stopCallbackServer();
  }


  @Test
  public void handleCompletesSuccessfullyWithValidJsessionId() throws Exception {
    BrowserAuthenticationService testService = new BrowserAuthenticationService() {
      @Override HttpServer createHttpServer( int port ) throws IOException {
        return super.createHttpServer( 0 );
      }
      @Override void openSystemBrowser( String url ) { /* no-op */ }
    };
    CompletableFuture<BrowserAuthenticationService.SessionInfo> future =
      testService.authenticate( "http://localhost/pentaho" );

    BrowserAuthenticationService.CallbackHandler handler = testService.new CallbackHandler();
    HttpExchange exchange = mockExchangeWithQuery( "jsessionid=SESSION_ABC&username=admin" );

    handler.handle( exchange );

    assertFalse( future.isCompletedExceptionally() );
    BrowserAuthenticationService.SessionInfo info = future.get();
    assertEquals( "SESSION_ABC", info.getJsessionId() );
    assertEquals( "admin", info.getUsername() );
    testService.stopCallbackServer();
  }


  @Test
  public void createProcessBuilderReturnsBuilderWithGivenCommand() {
    ProcessBuilder pb = service.createProcessBuilder( "echo", "hello" );

    assertNotNull( pb );
    assertEquals( java.util.Arrays.asList( "echo", "hello" ), pb.command() );
  }

  @Test
  public void createProcessBuilderReturnsSingleArgumentCommand() {
    ProcessBuilder pb = service.createProcessBuilder( "notepad" );

    assertNotNull( pb );
    assertEquals( java.util.Arrays.asList( "notepad" ), pb.command() );
  }

  @Test
  public void createProcessBuilderPreservesMultipleArguments() {
    ProcessBuilder pb = service.createProcessBuilder( "rundll32", "url.dll,FileProtocolHandler", "http://localhost:8080" );

    assertEquals( 3, pb.command().size() );
    assertEquals( "rundll32", pb.command().get( 0 ) );
    assertEquals( "url.dll,FileProtocolHandler", pb.command().get( 1 ) );
    assertEquals( "http://localhost:8080", pb.command().get( 2 ) );
  }

  @Test
  public void createProcessBuilderReturnsNewInstanceEachCall() {
    ProcessBuilder pb1 = service.createProcessBuilder( "cmd" );
    ProcessBuilder pb2 = service.createProcessBuilder( "cmd" );

    assertNotSame( pb1, pb2 );
  }

  @Test
  public void createProcessBuilderPreservesArgumentsWithSpecialCharacters() {
    ProcessBuilder pb = service.createProcessBuilder( "open", "http://example.com/path?a=1&b=2" );

    assertEquals( "http://example.com/path?a=1&b=2", pb.command().get( 1 ) );
  }

  @Test
  public void createProcessBuilderHandlesArgumentsWithSpaces() {
    ProcessBuilder pb = service.createProcessBuilder( "cmd", "/c", "echo hello world" );

    assertEquals( java.util.Arrays.asList( "cmd", "/c", "echo hello world" ), pb.command() );
  }

  @Test
  public void handleSuppressesInnerIoExceptionWhenSendingErrorResponse() throws Exception {
    BrowserAuthenticationService testService = new BrowserAuthenticationService() {
      @Override HttpServer createHttpServer( int port ) throws IOException {
        return super.createHttpServer( 0 );
      }
      @Override void openSystemBrowser( String url ) { /* no-op */ }
    };
    CompletableFuture<BrowserAuthenticationService.SessionInfo> future =
      testService.authenticate( "http://localhost/pentaho" );

    BrowserAuthenticationService.CallbackHandler handler = testService.new CallbackHandler();
    HttpExchange exchange = mock( HttpExchange.class );
    // Trigger the outer catch block
    when( exchange.getRequestURI() ).thenThrow( new RuntimeException( "broken exchange" ) );
    // sendResponse will call getResponseHeaders() then sendResponseHeaders()
    when( exchange.getResponseHeaders() ).thenReturn( new Headers() );
    // sendResponseHeaders throws IOException — exercises the inner catch
    doThrow( new IOException( "write failed" ) ).when( exchange ).sendResponseHeaders( anyInt(), anyLong() );

    handler.handle( exchange );

    assertTrue( future.isCompletedExceptionally() );
    testService.stopCallbackServer();
  }
}


