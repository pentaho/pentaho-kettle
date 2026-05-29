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
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class OAuthHttpClientTest {

  private static final String TOKEN_URI = "http://localhost/token";
  private static final String HEADER_VALUE = "value";
  private static final String STRING_KEY = "string";

  @Test
  public void postFormAppliesHeadersWritesBodyAndDisconnects() throws IOException {
    HttpURLConnection connection = mock( HttpURLConnection.class );
    ByteArrayOutputStream requestBody = new ByteArrayOutputStream();
    OAuthHttpClient client = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( connection ).when( client ).openConnection( anyString() );

    when( connection.getOutputStream() ).thenReturn( requestBody );
    when( connection.getResponseCode() ).thenReturn( HttpURLConnection.HTTP_OK );
    when( connection.getInputStream() )
      .thenReturn( new ByteArrayInputStream( "success".getBytes( StandardCharsets.UTF_8 ) ) );

    Map<String, String> params = new LinkedHashMap<>();
    params.put( "scope", "openid profile" );
    params.put( "client_id", null );

    String result = client.postForm( TOKEN_URI, params, Map.of( "X-Test", HEADER_VALUE ) );

    assertEquals( "success", result );
    verify( client ).openConnection( TOKEN_URI );
    verify( connection ).setRequestMethod( "POST" );
    verify( connection ).setDoOutput( true );
    verify( connection ).setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
    verify( connection ).setRequestProperty( "Accept", "application/json" );
    verify( connection ).setRequestProperty( "X-Test", HEADER_VALUE );
    assertEquals( "scope=openid%20profile&client_id=", requestBody.toString( StandardCharsets.UTF_8 ) );
    verify( connection ).disconnect();
  }

  @Test
  public void getJsonReturnsNullForNonSuccessResponse() throws IOException {
    HttpURLConnection connection = mock( HttpURLConnection.class );
    OAuthHttpClient client = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( connection ).when( client ).openConnection( anyString() );

    when( connection.getResponseCode() ).thenReturn( HttpURLConnection.HTTP_BAD_REQUEST );
    when( connection.getErrorStream() )
      .thenReturn( new ByteArrayInputStream( "bad-request".getBytes( StandardCharsets.UTF_8 ) ) );

    String result = client.getJson( "http://localhost/discovery" );

    assertNull( result );
    verify( connection ).setRequestMethod( "GET" );
    verify( connection ).setRequestProperty( "Accept", "application/json" );
    verify( connection ).disconnect();
  }

  @Test
  public void postFormRawReturnsStatusAndErrorBody() throws IOException {
    HttpURLConnection connection = mock( HttpURLConnection.class );
    ByteArrayOutputStream requestBody = new ByteArrayOutputStream();
    OAuthHttpClient client = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( connection ).when( client ).openConnection( anyString() );

    when( connection.getOutputStream() ).thenReturn( requestBody );
    when( connection.getResponseCode() ).thenReturn( HttpURLConnection.HTTP_BAD_REQUEST );
    when( connection.getErrorStream() )
      .thenReturn( new ByteArrayInputStream( "invalid".getBytes( StandardCharsets.UTF_8 ) ) );

    OAuthHttpClient.PostResult result = client.postFormRaw( TOKEN_URI, Map.of( "grant_type", "x" ) );

    assertEquals( HttpURLConnection.HTTP_BAD_REQUEST, result.status() );
    assertEquals( "invalid", result.body() );
    assertEquals( "grant_type=x", requestBody.toString( StandardCharsets.UTF_8 ) );
    verify( connection ).disconnect();
  }

  @Test
  public void postEmptyRawSetsContentLengthAndHeaders() throws IOException {
    HttpURLConnection connection = mock( HttpURLConnection.class );
    OAuthHttpClient client = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( connection ).when( client ).openConnection( anyString() );

    when( connection.getResponseCode() ).thenReturn( HttpURLConnection.HTTP_OK );
    when( connection.getInputStream() ).thenReturn( new ByteArrayInputStream( "{}".getBytes( StandardCharsets.UTF_8 ) ) );

    OAuthHttpClient.PostResult result =
      client.postEmptyRaw( "http://localhost/status", Map.of( "X-Auth-Handle", "h" ) );

    assertEquals( HttpURLConnection.HTTP_OK, result.status() );
    assertEquals( "{}", result.body() );
    verify( connection ).setRequestMethod( "POST" );
    verify( connection ).setRequestProperty( "Content-Length", "0" );
    verify( connection ).setRequestProperty( "X-Auth-Handle", "h" );
    verify( connection ).disconnect();
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
  public void readErrorBodyReturnsEmptyWhenErrorStreamMissingOrUnreadable() {
    HttpURLConnection nullStreamConnection = mock( HttpURLConnection.class );
    HttpURLConnection failingStreamConnection = mock( HttpURLConnection.class );
    InputStream failingStream = new InputStream() {
      @Override
      public int read() throws IOException {
        throw new IOException( "boom" );
      }
    };
    OAuthHttpClient client = new OAuthHttpClient( 1000, 1000 );

    when( nullStreamConnection.getErrorStream() ).thenReturn( null );
    when( failingStreamConnection.getErrorStream() ).thenReturn( failingStream );

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
}
