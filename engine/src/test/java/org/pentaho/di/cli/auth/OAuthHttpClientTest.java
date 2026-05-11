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

package org.pentaho.di.cli.auth;


import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class OAuthHttpClientTest {

  private static final String TOKEN_URI = "http://localhost/token";
  private static final String HEADER_VALUE = "value";
  private static final String STRING_KEY = "string";

  @Test
  public void postFormAppliesHeadersWritesBodyAndDisconnects() throws IOException {
    FakeHttpURLConnection connection = new FakeHttpURLConnection();
    connection.configuredResponseCode = HttpURLConnection.HTTP_OK;
    connection.inputStream = new ByteArrayInputStream( "success".getBytes( StandardCharsets.UTF_8 ) );
    TestableOAuthHttpClient client = new TestableOAuthHttpClient( connection );

    Map<String, String> params = new LinkedHashMap<>();
    params.put( "scope", "openid profile" );
    params.put( "client_id", null );

    String result = client.postForm( TOKEN_URI, params, Map.of( "X-Test", HEADER_VALUE ) );

    assertEquals( "success", result );
    assertEquals( TOKEN_URI, client.openedUri );
    assertEquals( "POST", connection.requestMethod );
    assertTrue( connection.outputEnabled );
    assertEquals( "application/x-www-form-urlencoded", connection.requestProperties.get( "Content-Type" ) );
    assertEquals( "application/json", connection.requestProperties.get( "Accept" ) );
    assertEquals( HEADER_VALUE, connection.requestProperties.get( "X-Test" ) );
    assertEquals( "scope=openid%20profile&client_id=", connection.requestBodyAsString() );
    assertTrue( connection.disconnected );
  }

  @Test
  public void getJsonReturnsNullForNonSuccessResponse() throws IOException {
    FakeHttpURLConnection connection = new FakeHttpURLConnection();
    connection.configuredResponseCode = HttpURLConnection.HTTP_BAD_REQUEST;
    connection.errorStream = new ByteArrayInputStream( "bad-request".getBytes( StandardCharsets.UTF_8 ) );
    TestableOAuthHttpClient client = new TestableOAuthHttpClient( connection );

    String result = client.getJson( "http://localhost/discovery" );

    assertNull( result );
    assertEquals( "GET", connection.requestMethod );
    assertEquals( "application/json", connection.requestProperties.get( "Accept" ) );
    assertTrue( connection.disconnected );
  }

  @Test
  public void postFormRawReturnsStatusAndErrorBody() throws IOException {
    FakeHttpURLConnection connection = new FakeHttpURLConnection();
    connection.configuredResponseCode = HttpURLConnection.HTTP_BAD_REQUEST;
    connection.errorStream = new ByteArrayInputStream( "invalid".getBytes( StandardCharsets.UTF_8 ) );
    TestableOAuthHttpClient client = new TestableOAuthHttpClient( connection );

    OAuthHttpClient.PostResult result = client.postFormRaw( TOKEN_URI, Map.of( "grant_type", "x" ) );

    assertEquals( HttpURLConnection.HTTP_BAD_REQUEST, result.status() );
    assertEquals( "invalid", result.body() );
    assertTrue( connection.disconnected );
  }

  @Test
  public void postEmptyRawSetsContentLengthAndHeaders() throws IOException {
    FakeHttpURLConnection connection = new FakeHttpURLConnection();
    connection.configuredResponseCode = HttpURLConnection.HTTP_OK;
    connection.inputStream = new ByteArrayInputStream( "{}".getBytes( StandardCharsets.UTF_8 ) );
    TestableOAuthHttpClient client = new TestableOAuthHttpClient( connection );

    OAuthHttpClient.PostResult result =
      client.postEmptyRaw( "http://localhost/status", Map.of( "X-Auth-Handle", "h" ) );

    assertEquals( HttpURLConnection.HTTP_OK, result.status() );
    assertEquals( "{}", result.body() );
    assertEquals( "POST", connection.requestMethod );
    assertEquals( "0", connection.requestProperties.get( "Content-Length" ) );
    assertEquals( "h", connection.requestProperties.get( "X-Auth-Handle" ) );
    assertTrue( connection.disconnected );
  }

  @Test
  public void openConnectionAppliesConfiguredTimeouts() throws IOException {
    OAuthHttpClient client = new OAuthHttpClient( 123, 456 );

    HttpURLConnection connection = client.openConnection( "http://example.test" );

    assertNotNull( connection );
    assertEquals( 123, connection.getConnectTimeout() );
    assertEquals( 456, connection.getReadTimeout() );
    connection.disconnect();
  }

  @Test
  public void readErrorBodyReturnsEmptyWhenErrorStreamMissingOrUnreadable() throws IOException {
    FakeHttpURLConnection nullStreamConnection = new FakeHttpURLConnection();
    FakeHttpURLConnection failingStreamConnection = new FakeHttpURLConnection();
    failingStreamConnection.errorStream = new InputStream() {
      @Override
      public int read() throws IOException {
        throw new IOException( "boom" );
      }
    };
    OAuthHttpClient client = new OAuthHttpClient( 1000, 1000 );

    assertEquals( "", client.readErrorBody( nullStreamConnection ) );
    assertEquals( "", client.readErrorBody( failingStreamConnection ) );
  }

  @Test
  public void extractJsonHelpersCoverSuccessAndEdgeCases() {
    OAuthHttpClient client = new OAuthHttpClient( 1000, 1000 );
    String json = "{"
      + "\"string\":\"value\","
      + "\"number\":42,"
      + "\"flag\":true,"
      + "\"items\":[\"a\",1,{},null],"
      + "\"object\":{\"nested\":true}"
      + "}";

    assertEquals( HEADER_VALUE, client.extractJsonString( json, STRING_KEY ) );
    assertNull( client.extractJsonString( json, "object" ) );
    assertEquals( Long.valueOf( 42L ), client.extractJsonNumber( json, "number" ) );
    assertNull( client.extractJsonNumber( json, STRING_KEY ) );
    assertEquals( Optional.of( true ), client.extractJsonBoolean( json, "flag" ) );
    assertEquals( Optional.empty(), client.extractJsonBoolean( json, STRING_KEY ) );

    List<String> items = client.extractJsonArray( json, "items" );
    assertEquals( List.of( "a", "1", "null" ), items );
    try {
      items.add( "extra" );
      fail( "Expected the extracted array to be immutable" );
    } catch ( UnsupportedOperationException expected ) {
      // expected
    }

    assertNull( client.extractJsonElement( null, "key" ) );
    assertNull( client.extractJsonElement( json, null ) );
    assertNull( client.extractJsonElement( "{not-json", "key" ) );
  }

  private static final class TestableOAuthHttpClient extends OAuthHttpClient {
    private final HttpURLConnection connection;
    private String openedUri;

    private TestableOAuthHttpClient( HttpURLConnection connection ) {
      super( 1000, 1000 );
      this.connection = connection;
    }

    @Override
    public HttpURLConnection openConnection( String uri ) {
      openedUri = uri;
      return connection;
    }
  }

  private static final class FakeHttpURLConnection extends HttpURLConnection {
    private final Map<String, String> requestProperties = new LinkedHashMap<>();
    private final ByteArrayOutputStream requestBody = new ByteArrayOutputStream();
    private InputStream inputStream = new ByteArrayInputStream( new byte[ 0 ] );
    private InputStream errorStream;
    private int configuredResponseCode = HttpURLConnection.HTTP_OK;
    private boolean disconnected;
    private String requestMethod;
    private boolean outputEnabled;

    private FakeHttpURLConnection() throws IOException {
      super( URI.create( "http://example.test" ).toURL() );
    }

    @Override
    public void disconnect() {
      disconnected = true;
    }

    @Override
    public boolean usingProxy() {
      return false;
    }

    @Override
    public void connect() {
      // no-op
    }

    @Override
    public void setRequestMethod( String method ) {
      requestMethod = method;
    }

    @Override
    public void setDoOutput( boolean doOutput ) {
      this.outputEnabled = doOutput;
    }

    @Override
    public void setRequestProperty( String key, String value ) {
      requestProperties.put( key, value );
    }

    @Override
    public ByteArrayOutputStream getOutputStream() {
      return requestBody;
    }

    @Override
    public int getResponseCode() {
      return configuredResponseCode;
    }

    @Override
    public InputStream getInputStream() {
      return inputStream;
    }

    @Override
    public InputStream getErrorStream() {
      return errorStream;
    }

    private String requestBodyAsString() {
      return requestBody.toString( StandardCharsets.UTF_8 );
    }
  }
}
