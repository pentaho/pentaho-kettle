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

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thin client that initiates broker-owned auth flows on the Pentaho server.
 */
public class BrokerAuthClient {

  private static final LogChannelInterface LOG = new LogChannel( "BrokerAuthClient" );

  @SuppressWarnings( "java:S1075" ) // hardcoded URI
  private static final String OAUTH_API_PATH = "/api/pentaho-oauth/broker";
  private static final String AUTH_START_PATH = OAUTH_API_PATH + "/auth-start";
  private static final String AUTH_STATUS_PATH = OAUTH_API_PATH + "/auth-status";
  private static final String AUTH_CANCEL_PATH = OAUTH_API_PATH + "/auth-cancel";
  private static final String CLIENT_CREDENTIALS_PATH = OAUTH_API_PATH + "/client-credentials";
  private static final String TOKEN_REFRESH_PATH = OAUTH_API_PATH + "/token-refresh";
  private static final String SESSION_EXCHANGE_PATH = OAUTH_API_PATH + "/session-exchange";
  private static final String HEADER_REGISTRATION_ID = "X-Registration-Id";
  private static final String HEADER_AUTH_HANDLE = "X-Auth-Handle";
  private static final String HEADER_REFRESH_HANDLE = "X-Refresh-Handle";

  private static final String ACCESS_TOKEN = "access_token";
  private static final String FIELD_ACCESS_TOKEN_EXPIRY_SECONDS = "access_token_expiry_seconds";
  private static final String FIELD_AUTH_HANDLE = "authHandle";
  private static final String FIELD_REFRESH_HANDLE = "refresh_handle";
  private static final String FIELD_SESSION_ID = "session_id";
  private static final String FIELD_USERNAME = "username";

  private static final String PARAM_REGISTRATION_ID = "registration_id";
  private static final SecureRandom JITTER_RANDOM = new SecureRandom();

  private static final int CONNECT_TIMEOUT_MS = 10_000;
  private static final int READ_TIMEOUT_MS = 30_000;

  /**
   * Default poll interval for device-code / PKCE flows. Visible for logging in
   * callers.
   */
  public static final long DEFAULT_POLL_INTERVAL_MS = 6_000;

  /**
   * Maximum poll duration before giving up. Visible for logging in callers.
   */
  public static final long MAX_POLL_DURATION_MS = 300_000; // 5 minutes
  /**
   * OAuth grant type for PKCE / authorisation code flow (RFC 6749 §4.1).
   */
  public static final String GRANT_AUTHORIZATION_CODE = "authorization_code";

  /**
   * OAuth grant type for device authorisation flow (RFC 8628).
   */
  public static final String GRANT_DEVICE_CODE = "urn:ietf:params:oauth:grant-type:device_code";

  private static final String STATUS_COMPLETED = "COMPLETED";
  private static final String STATUS_FAILED = "FAILED";
  private static final String STATUS_EXPIRED = "EXPIRED";
  private static final String STATUS_CANCELLED = "CANCELLED";
  private static final String STATUS_PENDING = "PENDING";

  private final OAuthHttpClient httpClient;
  private final DPoPProofBuilder dpopProofBuilder;

  private static String message( String key, String... tokens ) {
    return BaseMessages.getString( BrokerAuthClient.class, key, tokens );
  }

  public BrokerAuthClient() {
    this( new OAuthHttpClient( CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS ), null );
  }

  /**
   * Creates a broker client with a custom read timeout. Use when debugging
   * server-side code — set via
   * {@code Pan.bat -config:set auth.broker.read.timeout.seconds 300}.
   */
  public BrokerAuthClient( int readTimeoutMs ) {
    this( new OAuthHttpClient( CONNECT_TIMEOUT_MS, readTimeoutMs ), null );
  }

  /**
   * Creates a broker client with DPoP proof-of-possession enabled.
   * When a {@link DPoPProofBuilder} is provided, every auth-start and
   * auth-status request includes a {@code DPoP} header signed by the
   * builder's key.
   */
  public BrokerAuthClient( DPoPProofBuilder dpopProofBuilder ) {
    this( new OAuthHttpClient( CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS ), dpopProofBuilder );
  }

  /**
   * Creates a broker client with DPoP enabled and a custom read timeout.
   */
  public BrokerAuthClient( int readTimeoutMs, DPoPProofBuilder dpopProofBuilder ) {
    this( new OAuthHttpClient( CONNECT_TIMEOUT_MS, readTimeoutMs ), dpopProofBuilder );
  }

  BrokerAuthClient( OAuthHttpClient httpClient ) {
    this( httpClient, null );
  }

  BrokerAuthClient( OAuthHttpClient httpClient, DPoPProofBuilder dpopProofBuilder ) {
    this.httpClient = httpClient;
    this.dpopProofBuilder = dpopProofBuilder;
  }

  /**
   * Start a broker-owned PKCE (authorization_code) flow, requesting a
   * specific IdP by its registration ID.
   *
   * @param serverUrl      the base Pentaho server URL
   * @param registrationId the IdP config key to target, or {@code null}
   *                       to use the server's default
   * @return result containing auth handle, status, and broker-produced data
   */
  public BrokerFlowResult startPkceFlow( String serverUrl, String registrationId ) {
    return startFlow( serverUrl, GRANT_AUTHORIZATION_CODE,
      registrationId != null ? Map.of( PARAM_REGISTRATION_ID, registrationId ) : Collections.emptyMap() );
  }

  /**
   * Start a broker-owned PKCE (authorization_code) flow.
   *
   * @param serverUrl the base Pentaho server URL
   * @return result containing auth handle, status, and broker-produced data
   */
  public BrokerFlowResult startPkceFlow( String serverUrl ) {
    return startPkceFlow( serverUrl, null );
  }

  /**
   * Start a broker-owned device-code flow, requesting a specific IdP.
   *
   * @param serverUrl      the base Pentaho server URL
   * @param registrationId the IdP config key to target, or {@code null}
   *                       to use the server's default
   * @return result containing auth handle, user_code, verification_uri, etc.
   */
  public BrokerFlowResult startDeviceCodeFlow( String serverUrl, String registrationId ) {
    return startFlow( serverUrl, GRANT_DEVICE_CODE,
      registrationId != null ? Map.of( PARAM_REGISTRATION_ID, registrationId ) : Collections.emptyMap() );
  }

  /**
   * Start a broker-owned device-code flow.
   *
   * @param serverUrl the base Pentaho server URL
   * @return result containing auth handle, user_code, verification_uri, etc.
   */
  public BrokerFlowResult startDeviceCodeFlow( String serverUrl ) {
    return startDeviceCodeFlow( serverUrl, null );
  }

  /**
   * Start a broker-owned flow by grant type.
   *
   * @param serverUrl   the base Pentaho server URL
   * @param grantType   the OAuth grant type
   * @param extraParams additional form parameters (nullable)
   * @return the flow result
   */
  public BrokerFlowResult startFlow( String serverUrl, String grantType,
                                     Map<String, String> extraParams ) {
    String url = normalizeUrl( serverUrl ) + AUTH_START_PATH;
    Map<String, String> headers = new LinkedHashMap<>( buildDPoPHeaders( "POST", url ) );
    headers.put( "X-Grant-Type", grantType );
    if ( extraParams != null ) {
      String regId = extraParams.get( PARAM_REGISTRATION_ID );
      if ( regId != null ) {
        headers.put( HEADER_REGISTRATION_ID, regId );
      }
    }

    try {
      OAuthHttpClient.PostResult postResult = httpClient.postEmptyRaw( url, headers );
      if ( postResult.status() != HttpURLConnection.HTTP_OK ) {
        return BrokerFlowResult.error( message( "BrokerAuthClient.AuthStartNon200" ) );
      }
      return parseFlowResult( postResult.body() );
    } catch ( IOException e ) {
      LOG.logBasic( message( "BrokerAuthClient.AuthStartFailed", e.getMessage() ) );
      LOG.logDebug( message( "BrokerAuthClient.AuthStartException" ), e );
      return BrokerFlowResult.error( message( "BrokerAuthClient.ConnectionFailed", e.getMessage() ) );
    }
  }

  /**
   * Poll the broker auth-status endpoint until the flow completes, fails,
   * or times out.
   * <p>
   * Uses a {@link CountDownLatch} to block the calling thread. A daemon
   * {@link ScheduledExecutorService} fires each poll attempt on a fixed
   * delay — no {@code Thread.sleep}. The latch is counted down as soon as a
   * terminal status is detected, so the caller returns immediately rather
   * than waiting for the full timeout.
   *
   * @param serverUrl  the base Pentaho server URL
   * @param authHandle the auth handle from auth-start
   * @return the terminal flow result
   */
  public BrokerFlowResult pollUntilComplete( String serverUrl, String authHandle ) {
    String url = normalizeUrl( serverUrl ) + AUTH_STATUS_PATH;
    Instant deadline = Instant.now().plusMillis( MAX_POLL_DURATION_MS );

    AtomicReference<BrokerFlowResult> resultRef = new AtomicReference<>();
    CountDownLatch doneLatch = new CountDownLatch( 1 );
    AtomicInteger pollCount = new AtomicInteger( 0 );
    AtomicInteger consecutiveFailures = new AtomicInteger( 0 );

    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor( r -> {
      Thread t = new Thread( r, "broker-poll-" + authHandle.substring( 0, Math.min( 8, authHandle.length() ) ) );
      t.setDaemon( true );
      return t;
    } );

    // Schedule first poll immediately; subsequent polls re-schedule themselves.
    ScheduledFuture<?>[] futureHolder = new ScheduledFuture<?>[ 1 ];
    futureHolder[ 0 ] = scheduler.scheduleWithFixedDelay( () -> pollForCompletion( url, authHandle, deadline, resultRef,
      doneLatch, pollCount, consecutiveFailures ), 0, DEFAULT_POLL_INTERVAL_MS, TimeUnit.MILLISECONDS );

    try {
      // Wait up to max duration + a small buffer for the scheduler
      boolean completed = doneLatch.await( MAX_POLL_DURATION_MS + 10_000, TimeUnit.MILLISECONDS );
      if ( !completed ) {
        resultRef.set( BrokerFlowResult.error( message( "BrokerAuthClient.PollLatchTimedOut" ) ) );
      }
    } catch ( InterruptedException e ) {
      Thread.currentThread().interrupt();
      resultRef.set( BrokerFlowResult.error( message( "BrokerAuthClient.PollInterrupted" ) ) );
    } finally {
      futureHolder[ 0 ].cancel( false );
      scheduler.shutdownNow();
    }

    BrokerFlowResult finalResult = resultRef.get();
    return finalResult != null ? finalResult : BrokerFlowResult.error( message( "BrokerAuthClient.NoPollResult" ) );
  }

  /**
   * Single poll call to the auth-status endpoint.
   * <p>
   * Sends {@code auth_handle} in the {@code X-Auth-Handle} request header.
   * Handle-only broker operations use empty-body POSTs so no servlet or JAX-RS
   * layer needs to parse form parameters before the runtime service sees the
   * request.
   */
  public BrokerFlowResult pollOnce( String serverUrl, String authHandle ) {
    String url = serverUrl.contains( AUTH_STATUS_PATH ) ? serverUrl : normalizeUrl( serverUrl ) + AUTH_STATUS_PATH;
    try {
      Map<String, String> headers = new LinkedHashMap<>( buildDPoPHeaders( "POST", url ) );
      headers.put( HEADER_AUTH_HANDLE, authHandle );
      OAuthHttpClient.PostResult postResult = httpClient.postEmptyRaw( url, headers );
      int responseCode = postResult.status();
      if ( responseCode != HttpURLConnection.HTTP_OK ) {
        LOG.logDebug( message( "BrokerAuthClient.AuthStatusHttp", String.valueOf( responseCode ), postResult.body() ) );
        return BrokerFlowResult.error( message( "BrokerAuthClient.AuthStatusNon200", String.valueOf( responseCode ) ) );
      }
      return parseFlowResult( postResult.body() );
    } catch ( IOException e ) {
      LOG.logDebug( message( "BrokerAuthClient.AuthStatusPollFailed", e.getMessage() ) );
      // Connection failure — return a clean pending so the caller retries.
      // The caller tracks consecutive connection failures via a separate flag.
      return BrokerFlowResult.pending( null );
    }
  }

  /**
   * Cancel a pending broker auth handle. Called from a JVM shutdown hook when
   * the user presses Ctrl+C during a PKCE or device-code flow.
   * <p>
   * The call is best-effort: any IOException is swallowed because the JVM may
   * be in a degraded state during shutdown. The server-side handle will expire
   * after its TTL regardless of whether this call succeeds.
   *
   * @param serverUrl  the base Pentaho server URL
   * @param authHandle the auth handle to cancel; ignored if blank
   */
  public void cancelAuth( String serverUrl, String authHandle ) {
    if ( authHandle == null || authHandle.isBlank() ) {
      return;
    }
    String url = normalizeUrl( serverUrl ) + AUTH_CANCEL_PATH;
    try {
      OAuthHttpClient.PostResult postResult = httpClient.postEmptyRaw( url,
        Map.of( HEADER_AUTH_HANDLE, authHandle ) );
      int status = postResult.status();
      LOG.logBasic( message( "BrokerAuthClient.AuthCancelStatus",
        String.valueOf( status ), authHandle.substring( 0, Math.min( 8, authHandle.length() ) ) ) );
    } catch ( IOException e ) {
      LOG.logBasic( message( "BrokerAuthClient.AuthCancelFailed", e.getMessage() ) );
    }
  }

  /**
   * Request a broker session via the server-side client-credentials proxy. *
   * <p>
   * The server holds the client secret and resolves selected-IdP details after
   * receiving the optional {@code X-Registration-Id} header.
   *
   * @param serverUrl      the base Pentaho server URL
   * @param registrationId the IdP to target, or {@code null} when the broker
   *                       should resolve the single enabled IdP
   * @return the CC proxy result containing access_token and username
   */
  public BrokerFlowResult clientCredentials( String serverUrl, String registrationId ) {
    String url = normalizeUrl( serverUrl ) + CLIENT_CREDENTIALS_PATH;
    Map<String, String> headers = new LinkedHashMap<>();
    if ( registrationId != null && !registrationId.isBlank() ) {
      headers.put( HEADER_REGISTRATION_ID, registrationId );
    }

    try {
      OAuthHttpClient.PostResult postResult = httpClient.postEmptyRaw( url, headers );
      int status = postResult.status();
      if ( status == HttpURLConnection.HTTP_OK ) {
        return parseCcResult( postResult.body() );
      }
      LOG.logBasic( message( "BrokerAuthClient.CcProxyHttp", String.valueOf( status ), postResult.body() ) );
      return BrokerFlowResult.error( message( "BrokerAuthClient.CcProxyFailed", String.valueOf( status ) ) );
    } catch ( IOException e ) {
      LOG.logBasic( message( "BrokerAuthClient.CcProxyRequestFailed", e.getMessage() ) );
      LOG.logDebug( message( "BrokerAuthClient.CcProxyException" ), e );
      return BrokerFlowResult.error( message( "BrokerAuthClient.ConnectionFailed", e.getMessage() ) );
    }
  }

  private BrokerFlowResult parseFlowResult( String json ) {
    String authHandle = httpClient.extractJsonString( json, FIELD_AUTH_HANDLE );
    String status = httpClient.extractJsonString( json, "status" );
    String grantType = httpClient.extractJsonString( json, "grantType" );
    String username = httpClient.extractJsonString( json, FIELD_USERNAME );
    String error = httpClient.extractJsonString( json, "error" );
    String userCode = httpClient.extractJsonString( json, "user_code" );
    String verificationUri = httpClient.extractJsonString( json, "verification_uri" );
    String verificationUriComplete = httpClient.extractJsonString( json, "verification_uri_complete" );
    String sessionId = httpClient.extractJsonString( json, FIELD_SESSION_ID );
    String expiresAt = httpClient.extractJsonString( json, "expiresAt" );
    String authorizeUrl = httpClient.extractJsonString( json, "authorizeUrl" );

    // Session-capture URL: Pentaho-login path that captures an existing PUC
    // JSESSIONID.
    // Returned alongside authorizeUrl in auth-start responses for PKCE flows.
    String sessionCaptureUrl = httpClient.extractJsonString( json, "sessionCaptureUrl" );
    String accessToken = httpClient.extractJsonString( json, ACCESS_TOKEN );

    // session_expiry: epoch-millisecond string from server (Scenario A — JSESSIONID
    // session).
    String sessionExpiry = httpClient.extractJsonString( json, "session_expiry" );
    // access_token_expiry_seconds: epoch-second value from server (Scenario B — IdP
    // Bearer token).
    long accessTokenExpirySeconds;
    String tokenExpiryRaw = httpClient.extractJsonString( json, FIELD_ACCESS_TOKEN_EXPIRY_SECONDS );
    accessTokenExpirySeconds = parseLongField( tokenExpiryRaw, FIELD_ACCESS_TOKEN_EXPIRY_SECONDS );
    // refresh_handle: opaque token for POST /api/oauth/token-refresh.
    // Present only for IdP SSO flows (PKCE, device-code) that obtained a refresh
    // token.
    String refreshHandle = httpClient.extractJsonString( json, FIELD_REFRESH_HANDLE );

    return new BrokerFlowResult( authHandle, status, grantType, username, error,
      userCode, verificationUri, verificationUriComplete, sessionId, expiresAt,
      authorizeUrl, sessionCaptureUrl, accessToken, 0, sessionExpiry, accessTokenExpirySeconds,
      refreshHandle );
  }

  private BrokerFlowResult parseCcResult( String json ) {
    String username = httpClient.extractJsonString( json, FIELD_USERNAME );
    String accessToken = httpClient.extractJsonString( json, ACCESS_TOKEN );
    Long expiryLong = httpClient.extractJsonNumber( json, FIELD_ACCESS_TOKEN_EXPIRY_SECONDS );
    long accessTokenExpirySeconds = ( expiryLong != null ) ? expiryLong : 0L;
    return createTokenResult( "client_credentials", username, accessToken, null, accessTokenExpirySeconds );
  }

  /**
   * Exchange an IdP access token for a Pentaho broker session (JSESSIONID) by
   * calling the server-side {@code /api/oauth/session-exchange} endpoint.
   *
   * @param serverUrl   the base Pentaho server URL
   * @param accessToken the IdP access token returned in a completed auth-status
   * @return a completed result carrying {@code sessionId} and {@code username},
   * or a failed result on any error
   */
  public BrokerFlowResult sessionExchange( String serverUrl, String accessToken ) {
    String url = normalizeUrl( serverUrl ) + SESSION_EXCHANGE_PATH;
    Map<String, String> params = Map.of( ACCESS_TOKEN, accessToken );

    LOG.logBasic( message( "BrokerAuthClient.SessionExchangeStart", url ) );
    try {
      String body = httpClient.postForm( url, params );
      if ( body == null ) {
        return BrokerFlowResult.error( message( "BrokerAuthClient.SessionExchangeNon200" ) );
      }
      String sessionId = httpClient.extractJsonString( body, FIELD_SESSION_ID );
      String username = httpClient.extractJsonString( body, FIELD_USERNAME );
      String maxInactiveRaw = httpClient.extractJsonString( body, "max_inactive_interval" );
      int maxInactiveInterval = parseIntField( maxInactiveRaw, "max_inactive_interval", "Session-exchange" );
      LOG.logBasic( message( "BrokerAuthClient.SessionExchangeCreated",
        username,
        maxInactiveInterval > 0 ? message( "BrokerAuthClient.SessionExchangeExpirySuffix",
          String.valueOf( maxInactiveInterval ) ) : "" ) );
      return new BrokerFlowResult( null, STATUS_COMPLETED, "session-exchange",
        username, null, null, null, null, sessionId, null, null, null, null, maxInactiveInterval,
        null, 0L, null );
    } catch ( IOException e ) {
      LOG.logBasic( message( "BrokerAuthClient.SessionExchangeFailed", e.getMessage() ) );
      LOG.logDebug( message( "BrokerAuthClient.SessionExchangeException" ), e );
      return BrokerFlowResult.error( message( "BrokerAuthClient.SessionExchangeError", e.getMessage() ) );
    }
  }

  private static String normalizeUrl( String url ) {
    return url.endsWith( "/" ) ? url.substring( 0, url.length() - 1 ) : url;
  }

  private Map<String, String> buildDPoPHeaders( String method, String targetUri ) {
    if ( dpopProofBuilder == null ) {
      return Collections.emptyMap();
    }
    return Map.of( "DPoP", dpopProofBuilder.buildProof( method, targetUri ) );
  }

  /**
   * Silently refreshes an IdP access token at the server-side broker.
   *
   * <p>
   * Adds a random jitter of 0–300 ms before making the request to spread load
   * when multiple Pan clients all receive a 401 simultaneously (thundering herd
   * from n clients). The server applies its own per-session single-flight and
   * cross-node CAS to handle m PUC nodes.
   *
   * @param serverUrl     the base Pentaho server URL
   * @param refreshHandle the opaque handle issued during the original auth flow
   * @return a completed result carrying the new {@code accessToken} and
   * {@code accessTokenExpirySeconds}, or a failed result on any error
   */
  public BrokerFlowResult refreshAccessToken( String serverUrl, String refreshHandle, String authHandle ) {
    // Client-side jitter: 0–300 ms randomised delay to spread thundering herd
    // across n concurrent Pan clients all receiving a 401 at the same instant.
    long jitterMs = nextRefreshJitterMs();
    if ( jitterMs > 0 ) {
      try {
        Thread.sleep( jitterMs );
      } catch ( InterruptedException e ) {
        Thread.currentThread().interrupt();
        return BrokerFlowResult.error( message( "BrokerAuthClient.RefreshInterrupted" ) );
      }
    }

    String url = normalizeUrl( serverUrl ) + TOKEN_REFRESH_PATH;
    Map<String, String> headers = new LinkedHashMap<>();
    headers.put( HEADER_REFRESH_HANDLE, refreshHandle );
    if ( authHandle != null && !authHandle.isBlank() ) {
      headers.put( HEADER_AUTH_HANDLE, authHandle );
    }

    try {
      OAuthHttpClient.PostResult postResult = httpClient.postEmptyRaw( url, headers );
      int status = postResult.status();
      if ( status == HttpURLConnection.HTTP_OK ) {
        return parseRefreshResult( postResult.body() );
      }
      if ( status == HttpURLConnection.HTTP_UNAUTHORIZED ) {
        // Refresh token revoked or expired — caller must re-authenticate fully.
        LOG.logBasic( message( "BrokerAuthClient.RefreshRejected401", refreshHandle ) );
        return BrokerFlowResult.error( message( "BrokerAuthClient.RefreshRejected" ) );
      }
      LOG.logBasic( message( "BrokerAuthClient.RefreshHttp", String.valueOf( status ), postResult.body() ) );
      return BrokerFlowResult.error( message( "BrokerAuthClient.RefreshFailedHttp", String.valueOf( status ) ) );
    } catch ( IOException e ) {
      LOG.logBasic( message( "BrokerAuthClient.RefreshRequestFailed", e.getMessage() ) );
      return BrokerFlowResult.error( message( "BrokerAuthClient.RefreshConnectionFailed", e.getMessage() ) );
    }
  }

  private BrokerFlowResult parseRefreshResult( String json ) {
    String newAccessToken = httpClient.extractJsonString( json, ACCESS_TOKEN );
    String newRefreshHandle = httpClient.extractJsonString( json, FIELD_REFRESH_HANDLE );
    // access_token_expiry_seconds is serialised as a JSON number by the server
    // (Jackson long → unquoted). extractJsonString only matches quoted values so
    // it always returned null here, leaving expiry as 0 → "unknown" in logs and
    // oauthTokenExpiry = -1 in the persisted session, causing the stored token to
    // be treated as expired on the very next process start.
    Long expiryLong = httpClient.extractJsonNumber( json, FIELD_ACCESS_TOKEN_EXPIRY_SECONDS );
    long newExpiry = expiryLong != null ? expiryLong : 0L;
    return createTokenResult( "refresh_token", null, newAccessToken, newRefreshHandle, newExpiry );
  }

  /**
   * Immutable result from a broker auth operation.
   */
  public record BrokerFlowResult(
    String authHandle,
    String status,
    String grantType,
    String username,
    String error,
    String userCode,
    String verificationUri,
    String verificationUriComplete,
    String sessionId,
    String expiresAt,
    /* PKCE: IdP authorize URL via Pentaho's Spring Security OAuth2 entry point. */
    String authorizeUrl,
    /*
     * PKCE: Pentaho-login session-capture URL
     * Used as a fallback if the IdP PKCE flow fails: opens a Pentaho login page
     * after the user authenticates via Pentaho form login (Scenario A) the
     * {@code Callback} captures the same JSESSIONID that
     * PUC uses, with the same session expiry.
     */
    String sessionCaptureUrl,
    /*
     * Scenario B (IdP SSO): IdP access token for direct Bearer auth on PUR calls.
     * {@code PentahoOAuthTokenLoginFilter} validates it on every request so no
     * session-exchange is needed.
     */
    String accessToken,
    /*
     * Session-exchange result: max inactive interval in seconds.
     * Used to compute a best-effort client-side session expiry when the server
     * does not return {@code session_expiry} directly.
     */
    int maxInactiveInterval,
    /*
     * Scenario A (Pentaho form login): epoch-millisecond string from auth-status
     * {@code session_expiry} field. Passed directly to
     * {@link BrowserAuthSessionHolder#storeSession}
     * so the client-side session validity check honours the server-reported expiry.
     */
    String sessionExpiry,
    /*
     * Scenario B (IdP SSO): epoch-second value from auth-status
     * {@code access_token_expiry_seconds} field. Passed to
     * {@link BrowserAuthSessionHolder#storeOAuthToken} so the client-side expiry
     * check correctly expires the Bearer token at the right time.
     */
    long accessTokenExpirySeconds,
    /*
     * Opaque refresh handle issued by the server during a PKCE or device-code
     * auth flow. Present only when the IdP issued a refresh token. Stored by
     * Pan and presented in the {@code X-Refresh-Handle} header to
     * {@code POST /api/pentaho-oauth/broker/token-refresh} when the
     * access token is near expiry. The handle is stable for the lifetime of the
     * refresh token; the server rotates it if the IdP rotates the underlying
     * refresh token.
     */
    String refreshHandle) {

    public boolean isCompleted() {
      return STATUS_COMPLETED.equals( status );
    }

    public boolean isFailed() {
      return STATUS_FAILED.equals( status );
    }

    public boolean isExpired() {
      return STATUS_EXPIRED.equals( status );
    }

    public boolean isCancelled() {
      return STATUS_CANCELLED.equals( status );
    }

    public boolean isTerminal() {
      return isCompleted() || isFailed() || isExpired() || isCancelled() || error != null;
    }

    static BrokerFlowResult error( String message ) {
      return new BrokerFlowResult( null, STATUS_FAILED, null, null, message,
        null, null, null, null, null, null, null, null, 0, null, 0L, null );
    }

    static BrokerFlowResult pending( String authHandle ) {
      return new BrokerFlowResult( authHandle, STATUS_PENDING, null, null, null,
        null, null, null, null, null, null, null, null, 0, null, 0L, null );
    }
  }

  private BrokerFlowResult createTokenResult( String grantType, String username, String accessToken,
                                              String refreshHandle, long accessTokenExpirySeconds ) {
    return new BrokerFlowResult( null, STATUS_COMPLETED, grantType,
      username, null, null, null, null, null, null, null, null,
      accessToken, 0, null, accessTokenExpirySeconds, refreshHandle );
  }

  @VisibleForTesting
  void pollForCompletion( String url, String authHandle, Instant deadline,
                          AtomicReference<BrokerFlowResult> resultRef, CountDownLatch doneLatch,
                          AtomicInteger pollCount, AtomicInteger consecutiveFailures ) {
    if ( Instant.now().isAfter( deadline ) ) {
      String timeoutMessage = buildPollTimeoutMessage();
      LOG.logMinimal( timeoutMessage );
      resultRef.set( BrokerFlowResult.error( timeoutMessage ) );
      doneLatch.countDown();
      return;
    }

    int attempt = pollCount.incrementAndGet();
    logPollProgress( authHandle, deadline, attempt );

    BrokerFlowResult status = pollOnce( url, authHandle );
    if ( status.isTerminal() ) {
      if ( status.isExpired() ) {
        LOG.logMinimal( message( "BrokerAuthClient.AuthHandleExpired" ) );
      }
      resultRef.set( status );
      doneLatch.countDown();
      return;
    }

    if ( status.authHandle() == null ) {
      int failures = consecutiveFailures.incrementAndGet();
      // pending(null) from an IOException — server didn't respond.
      // Track consecutive connection failures; after 3 trials give up.
      if ( failures >= 3 ) {
        String message = message( "BrokerAuthClient.ServerUnreachable", String.valueOf( failures ) );
        LOG.logMinimal( message );
        resultRef.set( BrokerFlowResult.error( message ) );
        doneLatch.countDown();
      }
      return;
    }

    consecutiveFailures.set( 0 ); // successful pending response — server is alive
  }

  private long parseLongField( String rawValue, String fieldName ) {
    if ( rawValue == null || rawValue.isBlank() ) {
      return 0L;
    }
    try {
      return Long.parseLong( rawValue );
    } catch ( NumberFormatException ignored ) {
      LOG.logDebug( message( "BrokerAuthClient.ParseLongFailed", fieldName, rawValue ) );
      return 0L;
    }
  }

  private int parseIntField( String rawValue, String fieldName, String context ) {
    if ( rawValue == null || rawValue.isBlank() ) {
      return 0;
    }
    try {
      return Integer.parseInt( rawValue );
    } catch ( NumberFormatException ignored ) {
      LOG.logDebug( message( "BrokerAuthClient.ParseIntFailed", context, fieldName, rawValue ) );
      return 0;
    }
  }

  private String buildPollTimeoutMessage() {
    long timeoutMinutes = MAX_POLL_DURATION_MS / 60_000;
    return message( "BrokerAuthClient.PollTimeout", String.valueOf( timeoutMinutes ) );
  }

  private void logPollProgress( String authHandle, Instant deadline, int attempt ) {
    if ( attempt % 10 == 0 ) {
      long elapsed = System.currentTimeMillis() - ( deadline.toEpochMilli() - MAX_POLL_DURATION_MS );
      LOG.logBasic( message( "BrokerAuthClient.PollProgress",
        String.valueOf( attempt ), String.valueOf( elapsed / 1000 ) ) );
      return;
    }
    LOG.logDebug( message( "BrokerAuthClient.PollDebug",
      String.valueOf( attempt ), authHandle.substring( 0, 8 ) ) );
  }

  @VisibleForTesting
  long nextRefreshJitterMs() {
    return JITTER_RANDOM.nextInt( 301 );
  }
}
