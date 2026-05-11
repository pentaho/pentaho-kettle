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

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class BrokerAuthClientTest {

  @ClassRule public static final RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final String SERVER_URL = "http://localhost:8080/pentaho";
  private static final String AUTH_START_URL = SERVER_URL + "/api/pentaho-oauth/broker/auth-start";
  private static final String AUTH_STATUS_URL = SERVER_URL + "/api/pentaho-oauth/broker/auth-status";
  private static final String AUTH_CANCEL_URL = SERVER_URL + "/api/pentaho-oauth/broker/auth-cancel";
  private static final String CLIENT_CREDENTIALS_URL = SERVER_URL + "/api/pentaho-oauth/broker/client-credentials";
  private static final String SESSION_EXCHANGE_URL = SERVER_URL + "/api/pentaho-oauth/broker/session-exchange";
  private static final String TOKEN_REFRESH_URL = SERVER_URL + "/api/pentaho-oauth/broker/token-refresh";
  private static final String REGISTRATION_ID = "azure";
  private static final String HEADER_REGISTRATION_ID = "X-Registration-Id";
  private static final String HEADER_AUTH_HANDLE = "X-Auth-Handle";
  private static final String USERNAME = "alice";
  private static final String ACCESS_TOKEN = "access-token";
  private static final String REFRESH_HANDLE = "refresh-handle";
  private static final String AUTH_HANDLE = "auth-handle-123";
  private static final String DPOP_PROOF = "dpop-proof";

  @Test
  public void startPkceFlowParsesBrokerFieldsAndAddsDpopHeader() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    DPoPProofBuilder dpopProofBuilder = mock( DPoPProofBuilder.class );
    doReturn( DPOP_PROOF ).when( dpopProofBuilder ).buildProof( "POST", AUTH_START_URL );
    doReturn( new OAuthHttpClient.PostResult( 200, authStartJson() ) )
      .when( httpClient ).postEmptyRaw( AUTH_START_URL,
        Map.of( "DPoP", DPOP_PROOF, "X-Grant-Type", "authorization_code", "X-Registration-Id",
          REGISTRATION_ID ) );

    BrokerAuthClient client = new BrokerAuthClient( httpClient, dpopProofBuilder );

    BrokerAuthClient.BrokerFlowResult result = client.startPkceFlow( SERVER_URL + "/", REGISTRATION_ID );

    assertTrue( result.isCompleted() );
    assertEquals( AUTH_HANDLE, result.authHandle() );
    assertEquals( BrokerAuthClient.GRANT_AUTHORIZATION_CODE, result.grantType() );
    assertEquals( USERNAME, result.username() );
    assertEquals( "https://idp.example/authorize", result.authorizeUrl() );
    assertEquals( "https://server.example/pentaho/api/pentaho-oauth/broker/callback?launch=nonce", result.sessionCaptureUrl() );
    assertEquals( ACCESS_TOKEN, result.accessToken() );
    assertEquals( "1717171717", result.sessionExpiry() );
    assertEquals( 1893456000L, result.accessTokenExpirySeconds() );
    assertEquals( REFRESH_HANDLE, result.refreshHandle() );

    verify( dpopProofBuilder ).buildProof( "POST", AUTH_START_URL );
    verify( httpClient ).postEmptyRaw( AUTH_START_URL,
      Map.of( "DPoP", DPOP_PROOF, "X-Grant-Type", "authorization_code", "X-Registration-Id", REGISTRATION_ID ) );
  }

  @Test
  public void startPkceFlowOmitsRegistrationHeaderWhenRegistrationIdIsMissing() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( new OAuthHttpClient.PostResult( 200, "{\"authHandle\":\"" + AUTH_HANDLE
      + "\",\"status\":\"PENDING\"}" ) )
      .when( httpClient ).postEmptyRaw( AUTH_START_URL,
        Map.of( "X-Grant-Type", BrokerAuthClient.GRANT_AUTHORIZATION_CODE ) );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.startPkceFlow( SERVER_URL );

    assertEquals( AUTH_HANDLE, result.authHandle() );
    assertFalse( result.isTerminal() );
    verify( httpClient ).postEmptyRaw( AUTH_START_URL,
      Map.of( "X-Grant-Type", BrokerAuthClient.GRANT_AUTHORIZATION_CODE ) );
  }

  @Test
  public void clientCredentialsReturnsCompletedTokenResultOnHttp200() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( new OAuthHttpClient.PostResult( 200,
      "{\"username\":\"alice\",\"access_token\":\"access-token\",\"access_token_expiry_seconds\":1893456000}" ) )
      .when( httpClient ).postEmptyRaw( CLIENT_CREDENTIALS_URL, Map.of() );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.clientCredentials( SERVER_URL, null );

    assertTrue( result.isCompleted() );
    assertEquals( "client_credentials", result.grantType() );
    assertEquals( USERNAME, result.username() );
    assertEquals( ACCESS_TOKEN, result.accessToken() );
    assertEquals( 1893456000L, result.accessTokenExpirySeconds() );
    assertNull( result.refreshHandle() );
  }

  @Test
  public void clientCredentialsIncludesRegistrationIdWhenProvided() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    Map<String, String> headers = Map.of( HEADER_REGISTRATION_ID, REGISTRATION_ID );
    doReturn( new OAuthHttpClient.PostResult( 200,
      "{\"username\":\"alice\",\"access_token\":\"access-token\",\"access_token_expiry_seconds\":1893456000}" ) )
      .when( httpClient ).postEmptyRaw( CLIENT_CREDENTIALS_URL, headers );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.clientCredentials( SERVER_URL, REGISTRATION_ID );

    assertTrue( result.isCompleted() );
    verify( httpClient ).postEmptyRaw( CLIENT_CREDENTIALS_URL, headers );
  }

  @Test
  public void clientCredentialsReturnsErrorForNonSuccessStatus() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( new OAuthHttpClient.PostResult( 401, "{\"error\":\"unauthorized\"}" ) )
      .when( httpClient ).postEmptyRaw( CLIENT_CREDENTIALS_URL, Map.of() );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.clientCredentials( SERVER_URL, null );

    assertTrue( result.isFailed() );
    assertEquals( "Client credentials proxy failed (HTTP 401)", result.error() );
  }

  @Test
  public void clientCredentialsReturnsConnectionErrorWhenRequestFails() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doThrow( new IOException( "connection reset" ) )
      .when( httpClient ).postEmptyRaw( CLIENT_CREDENTIALS_URL, Map.of() );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.clientCredentials( SERVER_URL, null );

    assertTrue( result.isFailed() );
    assertEquals( "Connection failed: connection reset", result.error() );
  }

  @Test
  public void startDeviceCodeFlowParsesVerificationFields() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( new OAuthHttpClient.PostResult( 200, "{"
      + "\"authHandle\":\"" + AUTH_HANDLE + "\","
      + "\"status\":\"PENDING\","
      + "\"grantType\":\"" + BrokerAuthClient.GRANT_DEVICE_CODE + "\","
      + "\"user_code\":\"ABCD-EFGH\","
      + "\"verification_uri\":\"https://idp.example/device\","
      + "\"verification_uri_complete\":\"https://idp.example/device?user_code=ABCD-EFGH\""
      + "}" ) )
      .when( httpClient ).postEmptyRaw( AUTH_START_URL,
        Map.of( "X-Grant-Type", BrokerAuthClient.GRANT_DEVICE_CODE, "X-Registration-Id",
          REGISTRATION_ID ) );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.startDeviceCodeFlow( SERVER_URL, REGISTRATION_ID );

    assertFalse( result.isTerminal() );
    assertEquals( AUTH_HANDLE, result.authHandle() );
    assertEquals( BrokerAuthClient.GRANT_DEVICE_CODE, result.grantType() );
    assertEquals( "ABCD-EFGH", result.userCode() );
    assertEquals( "https://idp.example/device", result.verificationUri() );
    assertEquals( "https://idp.example/device?user_code=ABCD-EFGH", result.verificationUriComplete() );
  }

  @Test
  public void startFlowReturnsErrorWhenBrokerReturnsNonSuccess() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( new OAuthHttpClient.PostResult( 400, "{\"error\":\"bad request\"}" ) )
      .when( httpClient ).postEmptyRaw( AUTH_START_URL,
        Map.of( "X-Grant-Type", BrokerAuthClient.GRANT_DEVICE_CODE, "X-Registration-Id",
          REGISTRATION_ID ) );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.startDeviceCodeFlow( SERVER_URL, REGISTRATION_ID );

    assertTrue( result.isFailed() );
    assertEquals( "Broker auth-start returned non-200 response", result.error() );
  }

  @Test
  public void startDeviceCodeFlowReturnsConnectionErrorWhenRequestFails() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doThrow( new IOException( "connection reset" ) )
      .when( httpClient ).postEmptyRaw( AUTH_START_URL,
        Map.of( "X-Grant-Type", BrokerAuthClient.GRANT_DEVICE_CODE ) );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.startDeviceCodeFlow( SERVER_URL );

    assertTrue( result.isFailed() );
    assertEquals( "Connection failed: connection reset", result.error() );
  }

  @Test
  public void pollOnceReturnsPendingWhenServerIsUnreachable() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doThrow( new IOException( "connection refused" ) )
      .when( httpClient ).postEmptyRaw( AUTH_STATUS_URL, Map.of( HEADER_AUTH_HANDLE, AUTH_HANDLE ) );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.pollOnce( SERVER_URL, AUTH_HANDLE );

    assertFalse( result.isTerminal() );
    assertNull( result.authHandle() );
    assertEquals( "PENDING", result.status() );
  }

  @Test
  public void pollOnceReturnsErrorForNon200Response() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( new OAuthHttpClient.PostResult( 500, "server error" ) )
      .when( httpClient ).postEmptyRaw( AUTH_STATUS_URL, Map.of( HEADER_AUTH_HANDLE, AUTH_HANDLE ) );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.pollOnce( SERVER_URL, AUTH_HANDLE );

    assertTrue( result.isFailed() );
    assertEquals( "Broker auth-status returned HTTP 500", result.error() );
  }

  @Test
  public void pollOnceAddsDpopHeaderWhenConfigured() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    DPoPProofBuilder dpopProofBuilder = mock( DPoPProofBuilder.class );
    doReturn( DPOP_PROOF ).when( dpopProofBuilder ).buildProof( "POST", AUTH_STATUS_URL );
    doReturn( new OAuthHttpClient.PostResult( 200, "{\"authHandle\":\"" + AUTH_HANDLE
      + "\",\"status\":\"PENDING\"}" ) )
      .when( httpClient ).postEmptyRaw( AUTH_STATUS_URL,
        Map.of( "DPoP", DPOP_PROOF, HEADER_AUTH_HANDLE, AUTH_HANDLE ) );

    BrokerAuthClient client = new BrokerAuthClient( httpClient, dpopProofBuilder );

    BrokerAuthClient.BrokerFlowResult result = client.pollOnce( SERVER_URL, AUTH_HANDLE );

    assertEquals( AUTH_HANDLE, result.authHandle() );
    assertEquals( "PENDING", result.status() );
    verify( dpopProofBuilder ).buildProof( "POST", AUTH_STATUS_URL );
  }

  @Test
  public void sessionExchangeParsesSessionIdAndMaxInactiveInterval() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( "{\"session_id\":\"JSESSIONID\",\"username\":\"alice\",\"max_inactive_interval\":\"900\"}" )
      .when( httpClient ).postForm( SESSION_EXCHANGE_URL, Map.of( "access_token", ACCESS_TOKEN ) );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.sessionExchange( SERVER_URL, ACCESS_TOKEN );

    assertTrue( result.isCompleted() );
    assertEquals( "session-exchange", result.grantType() );
    assertEquals( USERNAME, result.username() );
    assertEquals( "JSESSIONID", result.sessionId() );
    assertEquals( 900, result.maxInactiveInterval() );
  }

  @Test
  public void sessionExchangeReturnsErrorWhenResponseBodyIsMissing() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( null ).when( httpClient ).postForm( SESSION_EXCHANGE_URL, Map.of( "access_token", ACCESS_TOKEN ) );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.sessionExchange( SERVER_URL, ACCESS_TOKEN );

    assertTrue( result.isFailed() );
    assertEquals( "Session-exchange returned non-200 response", result.error() );
  }

  @Test
  public void sessionExchangeFallsBackToZeroWhenMaxInactiveIntervalIsInvalid() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( "{\"session_id\":\"JSESSIONID\",\"username\":\"alice\",\"max_inactive_interval\":\"oops\"}" )
      .when( httpClient ).postForm( SESSION_EXCHANGE_URL, Map.of( "access_token", ACCESS_TOKEN ) );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.sessionExchange( SERVER_URL, ACCESS_TOKEN );

    assertTrue( result.isCompleted() );
    assertEquals( 0, result.maxInactiveInterval() );
  }

  @Test
  public void sessionExchangeFallsBackToZeroWhenMaxInactiveIntervalIsMissing() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( "{\"session_id\":\"JSESSIONID\",\"username\":\"alice\"}" )
      .when( httpClient ).postForm( SESSION_EXCHANGE_URL, Map.of( "access_token", ACCESS_TOKEN ) );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.sessionExchange( SERVER_URL, ACCESS_TOKEN );

    assertTrue( result.isCompleted() );
    assertEquals( 0, result.maxInactiveInterval() );
  }

  @Test
  public void sessionExchangeReturnsErrorWhenRequestFails() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doThrow( new IOException( "session exchange down" ) )
      .when( httpClient ).postForm( SESSION_EXCHANGE_URL, Map.of( "access_token", ACCESS_TOKEN ) );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.sessionExchange( SERVER_URL, ACCESS_TOKEN );

    assertTrue( result.isFailed() );
    assertEquals( BaseMessages.getString( BrokerAuthClient.class, "BrokerAuthClient.SessionExchangeError", "session exchange down" ),
      result.error() );
  }

  @Test
  public void startPkceFlowDefaultsTokenExpiryWhenFieldIsMissing() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( new OAuthHttpClient.PostResult( 200,
      "{\"authHandle\":\"" + AUTH_HANDLE + "\",\"status\":\"COMPLETED\"}" ) )
      .when( httpClient ).postEmptyRaw( AUTH_START_URL,
        Map.of( "X-Grant-Type", BrokerAuthClient.GRANT_AUTHORIZATION_CODE ) );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.startPkceFlow( SERVER_URL );

    assertEquals( 0L, result.accessTokenExpirySeconds() );
  }

  @Test
  public void startPkceFlowDefaultsTokenExpiryWhenFieldIsInvalid() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( new OAuthHttpClient.PostResult( 200,
      "{\"authHandle\":\"" + AUTH_HANDLE + "\",\"status\":\"COMPLETED\","
        + "\"access_token_expiry_seconds\":\"oops\"}" ) )
      .when( httpClient ).postEmptyRaw( AUTH_START_URL,
        Map.of( "X-Grant-Type", BrokerAuthClient.GRANT_AUTHORIZATION_CODE ) );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.startPkceFlow( SERVER_URL );

    assertEquals( 0L, result.accessTokenExpirySeconds() );
  }

  @Test
  public void refreshAccessTokenParsesRotatedHandleAndExpiry() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( new OAuthHttpClient.PostResult( 200,
      "{\"access_token\":\"new-access-token\",\"refresh_handle\":\"new-refresh-handle\","
        + "\"access_token_expiry_seconds\":1893456001}" ) )
      .when( httpClient ).postEmptyRaw( TOKEN_REFRESH_URL, expectedRefreshHeaders() );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.refreshAccessToken( SERVER_URL, REFRESH_HANDLE, AUTH_HANDLE );

    assertTrue( result.isCompleted() );
    assertEquals( "refresh_token", result.grantType() );
    assertEquals( "new-access-token", result.accessToken() );
    assertEquals( "new-refresh-handle", result.refreshHandle() );
    assertEquals( 1893456001L, result.accessTokenExpirySeconds() );
  }

  @Test
  public void refreshAccessTokenReturnsReauthenticationErrorOnUnauthorized() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( new OAuthHttpClient.PostResult( 401, "{\"error\":\"invalid_grant\"}" ) )
      .when( httpClient ).postEmptyRaw( TOKEN_REFRESH_URL, expectedRefreshHeaders() );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.refreshAccessToken( SERVER_URL, REFRESH_HANDLE, AUTH_HANDLE );

    assertTrue( result.isFailed() );
    assertEquals( BaseMessages.getString( BrokerAuthClient.class, "BrokerAuthClient.RefreshRejected" ), result.error() );
  }

  @Test
  public void refreshAccessTokenOmitsBlankAuthHandle() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( new OAuthHttpClient.PostResult( 200,
      "{\"access_token\":\"new-access-token\",\"refresh_handle\":\"new-refresh-handle\","
        + "\"access_token_expiry_seconds\":1893456001}" ) )
      .when( httpClient ).postEmptyRaw( TOKEN_REFRESH_URL, Map.of( "X-Refresh-Handle", REFRESH_HANDLE ) );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.refreshAccessToken( SERVER_URL, REFRESH_HANDLE, "   " );

    assertTrue( result.isCompleted() );
    assertEquals( "new-refresh-handle", result.refreshHandle() );
  }

  @Test
  public void refreshAccessTokenReturnsHttpErrorForUnexpectedStatus() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doReturn( new OAuthHttpClient.PostResult( 503, "temporarily unavailable" ) )
      .when( httpClient ).postEmptyRaw( TOKEN_REFRESH_URL, expectedRefreshHeaders() );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.refreshAccessToken( SERVER_URL, REFRESH_HANDLE, AUTH_HANDLE );

    assertTrue( result.isFailed() );
    assertEquals( BaseMessages.getString( BrokerAuthClient.class, "BrokerAuthClient.RefreshFailedHttp", "503" ), result.error() );
  }

  @Test
  public void refreshAccessTokenReturnsConnectionErrorWhenRequestFails() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doThrow( new IOException( "connection lost" ) )
      .when( httpClient ).postEmptyRaw( TOKEN_REFRESH_URL, expectedRefreshHeaders() );

    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    BrokerAuthClient.BrokerFlowResult result = client.refreshAccessToken( SERVER_URL, REFRESH_HANDLE, AUTH_HANDLE );

    assertTrue( result.isFailed() );
    assertEquals( BaseMessages.getString( BrokerAuthClient.class, "BrokerAuthClient.RefreshConnectionFailed", "connection lost" ),
      result.error() );
  }

  @Test
  public void cancelAuthIgnoresBlankHandle() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    client.cancelAuth( SERVER_URL, "   " );

    verify( httpClient, never() ).postEmptyRaw( AUTH_CANCEL_URL, Map.of( HEADER_AUTH_HANDLE, "   " ) );
  }

  @Test
  public void cancelAuthSwallowsIoExceptions() throws IOException {
    OAuthHttpClient httpClient = spy( new OAuthHttpClient( 1000, 1000 ) );
    doThrow( new IOException( "connection refused" ) )
      .when( httpClient ).postEmptyRaw( AUTH_CANCEL_URL, Map.of( HEADER_AUTH_HANDLE, AUTH_HANDLE ) );
    BrokerAuthClient client = new BrokerAuthClient( httpClient );

    client.cancelAuth( SERVER_URL, AUTH_HANDLE );

    verify( httpClient ).postEmptyRaw( AUTH_CANCEL_URL, Map.of( HEADER_AUTH_HANDLE, AUTH_HANDLE ) );
  }

  @Test
  public void pollUntilCompleteReturnsCompletedResultOnFirstSuccessfulPoll() {
    BrokerAuthClient client = spy( new BrokerAuthClient( mock( OAuthHttpClient.class ) ) );
    BrokerAuthClient.BrokerFlowResult completed = new BrokerAuthClient.BrokerFlowResult(
      AUTH_HANDLE, "COMPLETED", BrokerAuthClient.GRANT_DEVICE_CODE, USERNAME, null, null, null, null,
      null, null, null, null, ACCESS_TOKEN, 0, null, 1893456000L, REFRESH_HANDLE );
    doReturn( completed ).when( client ).pollOnce( AUTH_STATUS_URL, AUTH_HANDLE );

    BrokerAuthClient.BrokerFlowResult result = client.pollUntilComplete( SERVER_URL, AUTH_HANDLE );

    assertTrue( result.isCompleted() );
    assertEquals( ACCESS_TOKEN, result.accessToken() );
  }

  @Test
  public void pollUntilCompleteReturnsInterruptedErrorWhenAwaitIsInterrupted() {
    BrokerAuthClient client = spy( new BrokerAuthClient( mock( OAuthHttpClient.class ) ) );
    doReturn( BrokerAuthClient.BrokerFlowResult.pending( AUTH_HANDLE ) )
      .when( client ).pollOnce( AUTH_STATUS_URL, AUTH_HANDLE );

    Thread.currentThread().interrupt();
    try {
      BrokerAuthClient.BrokerFlowResult result = client.pollUntilComplete( SERVER_URL, AUTH_HANDLE );

      assertTrue( result.isFailed() );
      assertEquals( BaseMessages.getString( BrokerAuthClient.class, "BrokerAuthClient.PollInterrupted" ), result.error() );
    } finally {
      Thread.interrupted();
    }
  }

  @Test
  public void pollForCompletionReturnsTimeoutWhenDeadlineHasPassed() {
    BrokerAuthClient client = spy( new BrokerAuthClient( mock( OAuthHttpClient.class ) ) );
    AtomicReference<BrokerAuthClient.BrokerFlowResult> resultRef = new AtomicReference<>();
    CountDownLatch doneLatch = new CountDownLatch( 1 );

    client.pollForCompletion( AUTH_STATUS_URL, AUTH_HANDLE, Instant.now().minusMillis( 1 ), resultRef, doneLatch,
      new AtomicInteger(), new AtomicInteger() );

    assertEquals( 0L, doneLatch.getCount() );
    assertNotNull( resultRef.get() );
    assertEquals( BaseMessages.getString( BrokerAuthClient.class, "BrokerAuthClient.PollTimeout",
      String.valueOf( BrokerAuthClient.MAX_POLL_DURATION_MS / 60_000 ) ), resultRef.get().error() );
  }

  @Test
  public void pollForCompletionFailsAfterThreeConsecutiveConnectionFailures() {
    BrokerAuthClient client = spy( new BrokerAuthClient( mock( OAuthHttpClient.class ) ) );
    doReturn( BrokerAuthClient.BrokerFlowResult.pending( null ) ).when( client ).pollOnce( AUTH_STATUS_URL, AUTH_HANDLE );
    AtomicReference<BrokerAuthClient.BrokerFlowResult> resultRef = new AtomicReference<>();
    CountDownLatch doneLatch = new CountDownLatch( 1 );
    AtomicInteger failures = new AtomicInteger( 2 );

    client.pollForCompletion( AUTH_STATUS_URL, AUTH_HANDLE, Instant.now().plusSeconds( 60 ), resultRef, doneLatch,
      new AtomicInteger(), failures );

    assertEquals( 3, failures.get() );
    assertEquals( 0L, doneLatch.getCount() );
    assertEquals( BaseMessages.getString( BrokerAuthClient.class, "BrokerAuthClient.ServerUnreachable", "3" ), resultRef.get().error() );
  }

  @Test
  public void pollForCompletionResetsConsecutiveFailuresWhenPendingHandleIsReturned() {
    BrokerAuthClient client = spy( new BrokerAuthClient( mock( OAuthHttpClient.class ) ) );
    doReturn( BrokerAuthClient.BrokerFlowResult.pending( AUTH_HANDLE ) ).when( client ).pollOnce( AUTH_STATUS_URL, AUTH_HANDLE );
    AtomicReference<BrokerAuthClient.BrokerFlowResult> resultRef = new AtomicReference<>();
    CountDownLatch doneLatch = new CountDownLatch( 1 );
    AtomicInteger failures = new AtomicInteger( 2 );
    AtomicInteger pollCount = new AtomicInteger( 9 );

    client.pollForCompletion( AUTH_STATUS_URL, AUTH_HANDLE, Instant.now().plusSeconds( 60 ), resultRef, doneLatch,
      pollCount, failures );

    assertEquals( 10, pollCount.get() );
    assertEquals( 0, failures.get() );
    assertEquals( 1L, doneLatch.getCount() );
    assertNull( resultRef.get() );
  }

  @Test
  public void pollForCompletionReturnsExpiredTerminalResult() {
    BrokerAuthClient client = spy( new BrokerAuthClient( mock( OAuthHttpClient.class ) ) );
    BrokerAuthClient.BrokerFlowResult expired = new BrokerAuthClient.BrokerFlowResult(
      AUTH_HANDLE, "EXPIRED", null, null, null, null, null, null, null, null, null, null, null,
      0, null, 0L, null );
    doReturn( expired ).when( client ).pollOnce( AUTH_STATUS_URL, AUTH_HANDLE );
    AtomicReference<BrokerAuthClient.BrokerFlowResult> resultRef = new AtomicReference<>();
    CountDownLatch doneLatch = new CountDownLatch( 1 );

    client.pollForCompletion( AUTH_STATUS_URL, AUTH_HANDLE, Instant.now().plusSeconds( 60 ), resultRef, doneLatch,
      new AtomicInteger(), new AtomicInteger() );

    assertEquals( 0L, doneLatch.getCount() );
    assertEquals( expired, resultRef.get() );
  }

  @Test
  public void refreshAccessTokenReturnsInterruptedErrorWhenJitterSleepIsInterrupted() {
    BrokerAuthClient client = spy( new BrokerAuthClient( mock( OAuthHttpClient.class ) ) );
    doReturn( 1L ).when( client ).nextRefreshJitterMs();

    Thread.currentThread().interrupt();
    try {
      BrokerAuthClient.BrokerFlowResult result = client.refreshAccessToken( SERVER_URL, REFRESH_HANDLE, AUTH_HANDLE );

      assertTrue( result.isFailed() );
      assertEquals( BaseMessages.getString( BrokerAuthClient.class, "BrokerAuthClient.RefreshInterrupted" ), result.error() );
    } finally {
      Thread.interrupted();
    }
  }

  @Test
  public void brokerFlowResultTerminalHelpersRecognizeExpiredAndCancelledStates() {
    BrokerAuthClient.BrokerFlowResult expired = new BrokerAuthClient.BrokerFlowResult(
      null, "EXPIRED", null, null, null, null, null, null, null, null, null, null, null, 0, null, 0L, null );
    BrokerAuthClient.BrokerFlowResult cancelled = new BrokerAuthClient.BrokerFlowResult(
      null, "CANCELLED", null, null, null, null, null, null, null, null, null, null, null, 0, null, 0L, null );

    assertTrue( expired.isExpired() );
    assertTrue( expired.isTerminal() );
    assertTrue( cancelled.isCancelled() );
    assertTrue( cancelled.isTerminal() );
  }

  private Map<String, String> expectedRefreshHeaders() {
    Map<String, String> headers = new LinkedHashMap<>();
    headers.put( "X-Refresh-Handle", REFRESH_HANDLE );
    headers.put( HEADER_AUTH_HANDLE, AUTH_HANDLE );
    return headers;
  }

  private String authStartJson() {
    return "{"
      + "\"authHandle\":\"" + AUTH_HANDLE + "\","
      + "\"status\":\"COMPLETED\","
      + "\"grantType\":\"" + BrokerAuthClient.GRANT_AUTHORIZATION_CODE + "\","
      + "\"username\":\"" + USERNAME + "\","
      + "\"authorizeUrl\":\"https://idp.example/authorize\","
      + "\"sessionCaptureUrl\":\"https://server.example/pentaho/api/pentaho-oauth/broker/callback?launch=nonce\","
      + "\"access_token\":\"" + ACCESS_TOKEN + "\","
      + "\"session_expiry\":\"1717171717\","
      + "\"access_token_expiry_seconds\":\"1893456000\","
      + "\"refresh_handle\":\"" + REFRESH_HANDLE + "\""
      + "}";
  }

}
