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

package org.pentaho.di.repository.pur;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.After;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.cli.auth.CredentialProvider;

import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RestAuthHelperTest {

  private static final String SERVER_URL = "http://localhost:8080/pentaho";
  private static final String REQUEST_URL = SERVER_URL + "/api/test";
  private static final String USERNAME = "alice";
  private static final String BASIC_AUTH_CREDENTIAL = "test-basic-auth";
  private static final String TRUST_HEADER = "_trust_user_";
  private static final String TRUST_PROPERTY = "pentaho.repository.client.attemptTrust";
  private static final String ACCESS_TOKEN = "access-token";
  private static final String SESSION_COOKIE = "JSESSIONID=session-123";
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String COOKIE_HEADER = "Cookie";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String UNAUTHORIZED_BODY = "unauthorized";
  private static final String FORBIDDEN_BODY = "forbidden";

  @After
  public void tearDown() {
    System.clearProperty( TRUST_PROPERTY );
  }

  @Test
  public void executeWithAuthFallbackReturnsBearerResponseWithoutFallback() throws Exception {
    CredentialProvider provider = provider( Optional.of( ACCESS_TOKEN ), Optional.of( SESSION_COOKIE ) );
    CloseableHttpClient oauthClient = mock( CloseableHttpClient.class );
    stubExecute( oauthClient, 200, "oauth-ok", Map.of( AUTHORIZATION_HEADER, BEARER_PREFIX + ACCESS_TOKEN ) );

    try ( MockedStatic<HttpClients> httpClients = mockStatic( HttpClients.class ) ) {
      httpClients.when( HttpClients::createDefault ).thenReturn( oauthClient );

      String body = RestAuthHelper.executeWithAuthFallback(
        new HttpGet( REQUEST_URL ), SERVER_URL, USERNAME, BASIC_AUTH_CREDENTIAL, TRUST_HEADER, provider );

      assertEquals( "oauth-ok", body );
    }
  }

  @Test
  public void executeWithAuthFallbackFallsBackFromBearerToSessionCookieOn401() throws Exception {
    CredentialProvider provider = provider( Optional.of( ACCESS_TOKEN ), Optional.of( SESSION_COOKIE ) );
    CloseableHttpClient oauthClient = mock( CloseableHttpClient.class );
    CloseableHttpClient sessionClient = mock( CloseableHttpClient.class );
    stubExecute( oauthClient, 401, UNAUTHORIZED_BODY, Map.of( AUTHORIZATION_HEADER, BEARER_PREFIX + ACCESS_TOKEN ) );
    stubExecute( sessionClient, 200, "session-ok", Map.of( COOKIE_HEADER, SESSION_COOKIE ), AUTHORIZATION_HEADER );

    try ( MockedStatic<HttpClients> httpClients = mockStatic( HttpClients.class ) ) {
      httpClients.when( HttpClients::createDefault ).thenReturn( oauthClient, sessionClient );

      String body = RestAuthHelper.executeWithAuthFallback(
        new HttpGet( REQUEST_URL ), SERVER_URL, USERNAME, BASIC_AUTH_CREDENTIAL, TRUST_HEADER, provider );

      assertEquals( "session-ok", body );
    }
  }

  @Test
  public void executeWithAuthFallbackFallsBackFromBearerToSessionCookieOn403() throws Exception {
    CredentialProvider provider = provider( Optional.of( ACCESS_TOKEN ), Optional.of( SESSION_COOKIE ) );
    CloseableHttpClient oauthClient = mock( CloseableHttpClient.class );
    CloseableHttpClient sessionClient = mock( CloseableHttpClient.class );
    stubExecute( oauthClient, 403, FORBIDDEN_BODY, Map.of( AUTHORIZATION_HEADER, BEARER_PREFIX + ACCESS_TOKEN ) );
    stubExecute( sessionClient, 200, "session-ok", Map.of( COOKIE_HEADER, SESSION_COOKIE ), AUTHORIZATION_HEADER );

    try ( MockedStatic<HttpClients> httpClients = mockStatic( HttpClients.class ) ) {
      httpClients.when( HttpClients::createDefault ).thenReturn( oauthClient, sessionClient );

      String body = RestAuthHelper.executeWithAuthFallback(
        new HttpGet( REQUEST_URL ), SERVER_URL, USERNAME, BASIC_AUTH_CREDENTIAL, TRUST_HEADER, provider );

      assertEquals( "session-ok", body );
    }
  }

  @Test
  public void executeWithAuthFallbackFallsBackToTrustHeaderAfterBearerAndCookieReject() throws Exception {
    System.setProperty( TRUST_PROPERTY, "true" );
    CredentialProvider provider = provider( Optional.of( ACCESS_TOKEN ), Optional.of( SESSION_COOKIE ) );
    CloseableHttpClient oauthClient = mock( CloseableHttpClient.class );
    CloseableHttpClient sessionClient = mock( CloseableHttpClient.class );
    CloseableHttpClient trustClient = mock( CloseableHttpClient.class );
    stubExecute( oauthClient, 401, UNAUTHORIZED_BODY, Map.of( AUTHORIZATION_HEADER, BEARER_PREFIX + ACCESS_TOKEN ) );
    stubExecute( sessionClient, 401, UNAUTHORIZED_BODY, Map.of( COOKIE_HEADER, SESSION_COOKIE ), AUTHORIZATION_HEADER );
    stubExecute( trustClient, 200, "trust-ok", Map.of( TRUST_HEADER, USERNAME ), AUTHORIZATION_HEADER, COOKIE_HEADER );

    try ( MockedStatic<HttpClients> httpClients = mockStatic( HttpClients.class ) ) {
      httpClients.when( HttpClients::createDefault ).thenReturn( oauthClient, sessionClient, trustClient );

      String body = RestAuthHelper.executeWithAuthFallback(
        new HttpGet( REQUEST_URL ), SERVER_URL, USERNAME, BASIC_AUTH_CREDENTIAL, TRUST_HEADER, provider );

      assertEquals( "trust-ok", body );
    }
  }

  @Test
  public void executeWithAuthFallbackFallsBackToBasicAfterAlternativeAuthRejects() throws Exception {
    System.setProperty( TRUST_PROPERTY, "true" );
    CredentialProvider provider = provider( Optional.of( ACCESS_TOKEN ), Optional.of( SESSION_COOKIE ) );
    CloseableHttpClient oauthClient = mock( CloseableHttpClient.class );
    CloseableHttpClient sessionClient = mock( CloseableHttpClient.class );
    CloseableHttpClient trustClient = mock( CloseableHttpClient.class );
    CloseableHttpClient basicClient = mock( CloseableHttpClient.class );
    HttpClientBuilder builder = mock( HttpClientBuilder.class );
    stubExecute( oauthClient, 401, UNAUTHORIZED_BODY, Map.of( AUTHORIZATION_HEADER, BEARER_PREFIX + ACCESS_TOKEN ) );
    stubExecute( sessionClient, 401, UNAUTHORIZED_BODY, Map.of( COOKIE_HEADER, SESSION_COOKIE ), AUTHORIZATION_HEADER );
    stubExecute( trustClient, 401, UNAUTHORIZED_BODY, Map.of( TRUST_HEADER, USERNAME ), AUTHORIZATION_HEADER, COOKIE_HEADER );
    stubExecute( basicClient, 200, "basic-ok", Map.of(), AUTHORIZATION_HEADER, COOKIE_HEADER, TRUST_HEADER );
    when( builder.setDefaultCredentialsProvider( any( BasicCredentialsProvider.class ) ) ).thenReturn( builder );
    when( builder.build() ).thenReturn( basicClient );

    try ( MockedStatic<HttpClients> httpClients = mockStatic( HttpClients.class );
          MockedStatic<HttpClientBuilder> builderStatic = mockStatic( HttpClientBuilder.class ) ) {
      httpClients.when( HttpClients::createDefault ).thenReturn( oauthClient, sessionClient, trustClient );
      builderStatic.when( HttpClientBuilder::create ).thenReturn( builder );

      String body = RestAuthHelper.executeWithAuthFallback(
        new HttpGet( REQUEST_URL ), SERVER_URL, USERNAME, BASIC_AUTH_CREDENTIAL, TRUST_HEADER, provider );

      assertEquals( "basic-ok", body );
      verify( builder ).setDefaultCredentialsProvider( any( BasicCredentialsProvider.class ) );
    }
  }

  @Test
  public void executeWithAuthFallbackThrowsWhenBasicAuthReturns403() throws Exception {
    CredentialProvider provider = provider( Optional.empty(), Optional.empty() );
    CloseableHttpClient basicClient = mock( CloseableHttpClient.class );
    HttpClientBuilder builder = mock( HttpClientBuilder.class );
    stubExecute( basicClient, 403, FORBIDDEN_BODY, Map.of(), AUTHORIZATION_HEADER, COOKIE_HEADER, TRUST_HEADER );
    when( builder.setDefaultCredentialsProvider( any( BasicCredentialsProvider.class ) ) ).thenReturn( builder );
    when( builder.build() ).thenReturn( basicClient );

    try ( MockedStatic<HttpClientBuilder> builderStatic = mockStatic( HttpClientBuilder.class ) ) {
      builderStatic.when( HttpClientBuilder::create ).thenReturn( builder );

      try {
        RestAuthHelper.executeWithAuthFallback(
          new HttpGet( REQUEST_URL ), SERVER_URL, USERNAME, BASIC_AUTH_CREDENTIAL, TRUST_HEADER, provider );
        fail( "Expected RestAuthException" );
      } catch ( RestAuthHelper.RestAuthException e ) {
        assertEquals(
          "All authentication methods failed for " + SERVER_URL + " (last attempt: basic auth, HTTP 403)",
          e.getMessage() );
      }
    }
  }

  private CredentialProvider provider( Optional<String> accessToken, Optional<String> sessionCookie ) {
    CredentialProvider provider = mock( CredentialProvider.class );
    when( provider.findAccessToken( SERVER_URL ) ).thenReturn( accessToken );
    when( provider.findSessionCookie( SERVER_URL ) ).thenReturn( sessionCookie );
    return provider;
  }

  @SuppressWarnings( { "rawtypes" } )
  private void stubExecute( CloseableHttpClient client, int status, String body,
                            Map<String, String> expectedHeaders,
                            String... absentHeaders ) throws Exception {
    ClassicHttpResponse response = mock( ClassicHttpResponse.class );
    when( response.getCode() ).thenReturn( status );
    when( response.getEntity() ).thenReturn( new StringEntity( body ) );

    doAnswer( invocation -> {
      ClassicHttpRequest request = invocation.getArgument( 0 );
      for ( Map.Entry<String, String> entry : expectedHeaders.entrySet() ) {
        Header header = request.getFirstHeader( entry.getKey() );
        assertNotNull( "Expected header missing: " + entry.getKey(), header );
        assertEquals( entry.getValue(), header.getValue() );
      }
      for ( String absentHeader : absentHeaders ) {
        assertNull( "Header should have been cleared: " + absentHeader, request.getFirstHeader( absentHeader ) );
      }
      HttpClientResponseHandler handler = invocation.getArgument( 1 );
      return handler.handleResponse( response );
    } ).when( client ).execute( any( ClassicHttpRequest.class ), any( HttpClientResponseHandler.class ) );
  }
}
