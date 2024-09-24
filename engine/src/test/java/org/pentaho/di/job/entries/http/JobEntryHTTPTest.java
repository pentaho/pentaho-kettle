/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.pentaho.di.job.entries.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

/**
 * @author Tatsiana_Kasiankova
 *
 */
public class JobEntryHTTPTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  // Test HTTP Server properties
  public static final String HTTP_HOST = "localhost";
  public static final int HTTP_PORT = 9998;
  public static final String HTTP_SERVER_BASEURL = "http://localhost:9998";
  private static HttpServer httpServer;
  private static final String PASSWORD_TEST_URI = "/password";
  private static final String PASSWORD_TEST_USER = "admin";
  private static final String PASSWORD_TEST_PASSWORD = "password";

  // Test Proxy Server properties
  public static final String PROXY_HOST = "localhost";
  public static final int PROXY_PORT = 9995;
  public static final String PROXY_SERVER_BASEURL = "http://localhost:9995";
  private static HttpServer proxyServer;
  private static final String PROXY_TEST_URI = "/proxy";
  private static final String PROXY_REQUEST_HEADER = "REQUEST_FROM_PROXY";

  private JobEntryHTTP jobEntryHttp = new JobEntryHTTP();
  private KettleDatabaseRepository ktlDbRepMock = mock( KettleDatabaseRepository.class );
  private ObjectId objIdMock = mock( ObjectId.class );

  @BeforeClass
  public static void beforeClass() throws KettleException, IOException {
    KettleClientEnvironment.init();
    JobEntryHTTPTest.startHTTPServer();
    JobEntryHTTPTest.startProxyServer();
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    String passwordEncoderPluginID =
        Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );
  }

  @AfterClass
  public static void tearDown() {
    JobEntryHTTPTest.stopHTTPServer();
    JobEntryHTTPTest.stopProxyServer();
  }

  @Test
  public void testDateTimeAddedFieldIsSetInTrue_WhenRepoReturnsTrue() throws KettleException {
    when( ktlDbRepMock.getJobEntryAttributeBoolean( objIdMock, "date_time_added" ) ).thenReturn( true );

    jobEntryHttp.loadRep( ktlDbRepMock, ktlDbRepMock.getRepositoryMetaStore(), objIdMock, null, null );
    verify( ktlDbRepMock, never() ).getJobEntryAttributeString( objIdMock, "date_time_added" );
    verify( ktlDbRepMock ).getJobEntryAttributeBoolean( objIdMock, "date_time_added" );
    assertTrue( "DateTimeAdded field should be TRUE.", jobEntryHttp.isDateTimeAdded() );

  }

  @SuppressWarnings( "deprecation" )
  @Test
  public void testDeprecatedTargetFilenameExtension() {
    jobEntryHttp.setTargetFilenameExtention( "txt" );
    assertTrue( "txt".equals( jobEntryHttp.getTargetFilenameExtension() ) );
    jobEntryHttp.setTargetFilenameExtension( "zip" );
    assertTrue( "zip".equals( jobEntryHttp.getTargetFilenameExtention() ) );
  }

  @Test
  public void testExecute_simpleConfiguration() throws IOException {
    String validURL = HTTP_SERVER_BASEURL;
    String invalidURL = "http://www.www.www.www";
    File tempTargetFile = File.createTempFile( "targetFile", ".tmp" );
    tempTargetFile.deleteOnExit();
    Result result = new Result();
    JobEntryHTTP basicHttpJobEntry = new JobEntryHTTP();
    basicHttpJobEntry.setTargetFilename( tempTargetFile.getAbsolutePath() );
    basicHttpJobEntry.setAddFilenameToResult( false );

    // Test valid URL
    basicHttpJobEntry.setUrl( validURL );
    basicHttpJobEntry.execute( result, 0 );
    assertEquals( 0L, result.getNrErrors() );
    assertEquals( HttpStatus.SC_OK, basicHttpJobEntry.getResponseStatusCode() );
    assertTrue( FileUtils.sizeOf( tempTargetFile ) > 0 );
    tempTargetFile.delete();

    // Test invalid URL
    result = new Result();
    basicHttpJobEntry.setUrl( invalidURL );
    basicHttpJobEntry.execute( result, 0 );
    assertEquals( 1L, result.getNrErrors() );
    assertEquals( 0, basicHttpJobEntry.getResponseStatusCode() );
    assertTrue( FileUtils.sizeOf( tempTargetFile ) == 0 );
    tempTargetFile.delete();
  }

  @Test
  public void testExecute_simpleAuthentication() throws IOException {
    String passwordURL = HTTP_SERVER_BASEURL + PASSWORD_TEST_URI;
    File tempTargetFile = File.createTempFile( "targetFile", ".tmp" );
    tempTargetFile.deleteOnExit();
    Result result = new Result();
    JobEntryHTTP basicHttpJobEntry = new JobEntryHTTP();
    basicHttpJobEntry.setTargetFilename( tempTargetFile.getAbsolutePath() );
    basicHttpJobEntry.setAddFilenameToResult( false );

    // Test no user or password when required
    basicHttpJobEntry.setUrl( passwordURL );
    basicHttpJobEntry.execute( result, 0 );
    assertEquals( 1L, result.getNrErrors() );
    assertEquals( HttpStatus.SC_UNAUTHORIZED, basicHttpJobEntry.getResponseStatusCode() );
    assertTrue( FileUtils.sizeOf( tempTargetFile ) == 0 );

    // Test wrong user or password when required
    result = new Result();
    basicHttpJobEntry.setUrl( passwordURL );
    basicHttpJobEntry.setUsername( "BAD" );
    basicHttpJobEntry.setPassword( "BAD" );
    basicHttpJobEntry.execute( result, 0 );
    assertEquals( 1L, result.getNrErrors() );
    assertEquals( HttpStatus.SC_UNAUTHORIZED, basicHttpJobEntry.getResponseStatusCode() );
    assertTrue( FileUtils.sizeOf( tempTargetFile ) == 0 );

    // Test correct user or password when required
    result = new Result();
    basicHttpJobEntry.setUrl( passwordURL );
    basicHttpJobEntry.setUsername( PASSWORD_TEST_USER );
    basicHttpJobEntry.setPassword( PASSWORD_TEST_PASSWORD );
    basicHttpJobEntry.execute( result, 0 );
    assertEquals( 0L, result.getNrErrors() );
    assertEquals( HttpStatus.SC_OK, basicHttpJobEntry.getResponseStatusCode() );
    assertTrue( FileUtils.sizeOf( tempTargetFile ) > 0 );
  }

  @Test
  public void testExecute_simpleConfiguration_withProxy() throws IOException {
    String proxyTestURL = HTTP_SERVER_BASEURL + PROXY_TEST_URI;
    File tempTargetFile = File.createTempFile( "targetFile", ".tmp" );
    tempTargetFile.deleteOnExit();
    Result result = new Result();
    JobEntryHTTP basicHttpJobEntry = new JobEntryHTTP();
    basicHttpJobEntry.setTargetFilename( tempTargetFile.getAbsolutePath() );
    basicHttpJobEntry.setAddFilenameToResult( false );

    // Test URL without proxy
    basicHttpJobEntry.setUrl( proxyTestURL );
    basicHttpJobEntry.execute( result, 0 );
    assertEquals( 1L, result.getNrErrors() );
    assertEquals( HttpStatus.SC_USE_PROXY, basicHttpJobEntry.getResponseStatusCode() );
    assertTrue( FileUtils.sizeOf( tempTargetFile ) == 0 );

    // Test URL with proxy
    result = new Result();
    basicHttpJobEntry.setUrl( proxyTestURL );
    basicHttpJobEntry.setProxyHostname( PROXY_HOST );
    basicHttpJobEntry.setProxyPort( PROXY_PORT + "" );
    basicHttpJobEntry.execute( result, 0 );
    assertEquals( 0L, result.getNrErrors() );
    assertEquals( HttpStatus.SC_OK, basicHttpJobEntry.getResponseStatusCode() );
    assertTrue( FileUtils.sizeOf( tempTargetFile ) > 0 );

  }

  private static void startHTTPServer() throws IOException {
    HttpHandler handler = new SimpleHTTPHandler();
    HttpHandler proxyTestHandler = new SimpleProxyTestHandler();
    httpServer = HttpServer.create( new InetSocketAddress( HTTP_HOST, HTTP_PORT ), 10 );
    httpServer.createContext( "/", handler );
    HttpContext passwordContext = httpServer.createContext( PASSWORD_TEST_URI, handler );
    passwordContext.setAuthenticator( new BasicAuthenticator( "get" ) {
      @Override
      public boolean checkCredentials( String user, String pwd ) {
        return user.equals( PASSWORD_TEST_USER ) && pwd.equals( PASSWORD_TEST_PASSWORD );
      }
    } );
    httpServer.createContext( PROXY_TEST_URI, proxyTestHandler );
    httpServer.start();
  }

  private static void stopHTTPServer() {
    httpServer.stop( 2 );
  }

  private static void startProxyServer() throws IOException {
    HttpHandler handler = new SimpleProxyHandler();
    proxyServer = HttpServer.create( new InetSocketAddress( PROXY_HOST, PROXY_PORT ), 10 );
    proxyServer.createContext( "/", handler );
    proxyServer.start();
  }

  private static void stopProxyServer() {
    proxyServer.stop( 2 );
  }

  private static class SimpleHTTPHandler implements HttpHandler {

    private static final String SIMPLE_HTML = "<html><body>Hello</body></html>";

    @Override
    public void handle( HttpExchange httpExchange ) throws IOException {
      Headers h = httpExchange.getResponseHeaders();
      h.add( "Content-Type", "text/html" );
      httpExchange.sendResponseHeaders( 200, 0 );
      OutputStream os = httpExchange.getResponseBody();
      os.write( SIMPLE_HTML.getBytes() );
      os.flush();
      os.close();
      httpExchange.close();
    }
  }

  private static class SimpleProxyTestHandler implements HttpHandler {

    private static final String SIMPLE_HTML = "<html><body>Proxy Tests</body></html>";

    @Override
    public void handle( HttpExchange httpExchange ) throws IOException {
      Headers h = httpExchange.getResponseHeaders();
      h.add( "Content-Type", "text/html" );

      if ( httpExchange.getRequestHeaders().containsKey( PROXY_REQUEST_HEADER ) ) {
        httpExchange.sendResponseHeaders( 200,  0 );
        OutputStream os = httpExchange.getResponseBody();
        os.write( SIMPLE_HTML.getBytes() );
        os.flush();
        os.close();
      } else {
        httpExchange.sendResponseHeaders( HttpStatus.SC_USE_PROXY,  0 );
      }
      httpExchange.close();
    }
  }

  private static class SimpleProxyHandler implements HttpHandler {

    @Override
    public void handle( HttpExchange httpExchange ) throws IOException {

      // Forward call to destination URL
      URI uri = httpExchange.getRequestURI();
      HttpClientBuilder clientBuilder = HttpClientBuilder.create();
      HttpClient client = clientBuilder.build();
      HttpRequestBase httpRequestBase = new HttpGet( uri );
      httpRequestBase.addHeader( PROXY_REQUEST_HEADER, "TRUE" );
      HttpResponse response = client.execute( httpRequestBase );
      InputStream is = response.getEntity().getContent();

      // Reply to caller
      Headers h = httpExchange.getResponseHeaders();
      h.add( "Content-Type", "text/html" );
      httpExchange.sendResponseHeaders( response.getStatusLine().getStatusCode(), 0 );
      OutputStream os = httpExchange.getResponseBody();
      int inputChar = -1;
      while ( ( inputChar = is.read() ) >= 0 ) {
        os.write( inputChar );
      }
      os.flush();
      os.close();
      httpExchange.close();

    }
  }
}
