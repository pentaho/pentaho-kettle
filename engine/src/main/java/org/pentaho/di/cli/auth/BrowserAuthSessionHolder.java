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

import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.cli.auth.store.StoredCredential;
import org.pentaho.di.cli.auth.store.TokenStore;
import org.pentaho.di.cli.auth.store.TokenStoreFactory;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Holds and manages browser-based authentication session tokens.
 */
public class BrowserAuthSessionHolder implements CredentialProvider {

  private static final LogChannelInterface log = new LogChannel( "BrowserAuthSessionHolder" );
  private static final String TOKEN_TYPE_BEARER = "Bearer";
  private static final long UNKNOWN_TOKEN_EXPIRY = -1L;

  private static String message( String key, String... tokens ) {
    return BaseMessages.getString( BrowserAuthSessionHolder.class, key, tokens );
  }

  private static final BrowserAuthSessionHolder INSTANCE = new BrowserAuthSessionHolder( TokenStoreFactory.create() );

  final TokenStore tokenStore;
  // Current session data
  private final AtomicReference<SessionData> currentSession = new AtomicReference<>();

  BrowserAuthSessionHolder( TokenStore tokenStore ) {
    this.tokenStore = tokenStore;
    loadPersistedSession();
  }

  public static BrowserAuthSessionHolder getInstance() {
    return INSTANCE;
  }

  private static StoredCredential toStoredCredential( SessionData session ) {
    return StoredCredential.builder()
      .serverUrl( session.serverUrl )
      .sessionToken( session.sessionToken )
      .sessionCookie( session.sessionCookie )
      .username( session.username )
      .sessionExpiry( session.expiryTime )
      .oauthAccessToken( session.oauthAccessToken )
      .oauthRefreshToken( session.oauthRefreshToken )
      .oauthTokenType( session.oauthTokenType )
      .oauthIdpRegistrationId( session.oauthIdpRegistrationId )
      .oauthTokenExpiry( session.oauthTokenExpiry )
      .oauthRefreshHandle( session.oauthRefreshHandle )
      .oauthBrokerAuthHandle( session.oauthBrokerAuthHandle )
      .build();
  }

  private static SessionData fromStoredCredential( StoredCredential credential ) {
    SessionData session = new SessionData();
    session.serverUrl = credential.serverUrl();
    session.sessionToken = credential.sessionToken();
    session.sessionCookie = credential.sessionCookie();
    session.username = credential.username();
    session.expiryTime = credential.sessionExpiry();
    session.oauthAccessToken = credential.oauthAccessToken();
    session.oauthRefreshToken = credential.oauthRefreshToken();
    session.oauthTokenType = credential.oauthTokenType();
    session.oauthIdpRegistrationId = credential.oauthIdpRegistrationId();
    session.oauthTokenExpiry = credential.oauthTokenExpiry();
    session.oauthRefreshHandle = credential.oauthRefreshHandle();
    session.oauthBrokerAuthHandle = credential.oauthBrokerAuthHandle();
    return session;
  }

  public record OAuthTokenData(String serverUrl, String accessToken, String refreshToken,
                               String tokenType, String idpRegistrationId,
                               long expiresInSeconds, long expEpochSeconds, String username) {
  }

  /**
   * Store a new session token obtained from browser authentication.
   *
   * @param serverUrl     The server URL this session is for
   * @param sessionToken  The session token (e.g., JSESSIONID value)
   * @param sessionCookie The full session cookie string
   * @param username      The authenticated username
   */
  public void storeSession( String serverUrl, String sessionToken, String sessionCookie, String username ) {
    storeSession( serverUrl, sessionToken, sessionCookie, username, null );
  }

  /**
   * Store a new session token obtained from browser authentication with explicit
   * expiry.
   *
   * @param serverUrl     The server URL this session is for
   * @param sessionToken  The session token (e.g., JSESSIONID value)
   * @param sessionCookie The full session cookie string
   * @param username      The authenticated username
   * @param sessionExpiry The session expiry timestamp (epoch millis as string),
   *                      or null to use default TTL
   */
  public void storeSession( String serverUrl, String sessionToken, String sessionCookie, String username,
                            String sessionExpiry ) {
    SessionData session = new SessionData();
    session.serverUrl = serverUrl;
    session.sessionToken = sessionToken;
    session.sessionCookie = sessionCookie;
    session.username = username;

    // Use server-provided expiry if available, otherwise use default TTL
    if ( sessionExpiry != null && !sessionExpiry.isEmpty() ) {
      try {
        session.expiryTime = Long.parseLong( sessionExpiry );
        if ( log.isDebug() ) {
          log.logDebug( message( "BrowserAuthSessionHolder.ServerExpiry" ) );
        }
      } catch ( NumberFormatException e ) {
        session.expiryTime = -1;
        if ( log.isDebug() ) {
          log.logDebug( message( "BrowserAuthSessionHolder.InvalidExpiryFormat" ) );
        }
      }
    } else {
      session.expiryTime = -1;
    }

    currentSession.set( session );
    persistSession( session );

    if ( log.isBasic() ) {
      log.logBasic( message( "BrowserAuthSessionHolder.StoredBrowserSession" ) );
    }
  }

  /**
   * Get the current session token for the specified server.
   *
   * @param serverUrl The server URL to get session for
   * @return The session token, or null if no valid session exists
   */
  public String getSessionToken( String serverUrl ) {
    SessionData session = currentSession.get();
    if ( isSessionValid( session, serverUrl ) ) {
      return session.sessionToken;
    }
    return null;
  }

  /**
   * Get the current session cookie for the specified server.
   *
   * @param serverUrl The server URL to get session for
   * @return The session cookie string, or null if no valid session exists
   */
  public String getSessionCookie( String serverUrl ) {
    SessionData session = currentSession.get();
    if ( isSessionValid( session, serverUrl ) ) {
      return session.sessionCookie;
    }
    return null;
  }

  /**
   * Get the username associated with the current session.
   *
   * @param serverUrl The server URL
   * @return The username, or null if no valid session exists
   */
  public String getSessionUsername( String serverUrl ) {
    SessionData session = currentSession.get();
    if ( isSessionValid( session, serverUrl ) ) {
      return session.username;
    }
    return null;
  }

  /**
   * Check if a valid session exists for the specified server.
   *
   * @param serverUrl The server URL to check
   * @return true if a valid session exists
   */
  public boolean hasValidSession( String serverUrl ) {
    SessionData session = currentSession.get();
    return isSessionValid( session, serverUrl );
  }

  /**
   * Clear the current session.
   */
  public void clearSession() {
    currentSession.set( null );
    deletePersistedSession();
    if ( log.isBasic() ) {
      log.logBasic( message( "BrowserAuthSessionHolder.ClearedBrowserSession" ) );
    }
  }

  /**
   * Clear session for a specific server.
   *
   * @param serverUrl The server URL to clear session for
   */
  public void clearSession( String serverUrl ) {
    SessionData session = currentSession.get();
    if ( session != null && normalizeUrl( serverUrl ).equals( normalizeUrl( session.serverUrl ) ) ) {
      clearSession();
    }
  }

  /**
   * Store an OAuth access token obtained from Device Code or Auth Code flow.
   * Also sets the system property so that WebServiceManager and RestAuthHelper
   * can pick it up for Bearer token authentication.
   * <p>
   * Token expiry is determined from the JWT {@code exp} claim when available
   * (authoritative, absolute, immune to clock drift). Falls back to
   * {@code expiresInSeconds} (relative, from the token response) for opaque
   * tokens only.
   * <p>
   * {@code expiryTime} (session-based expiry) is NOT set here — it is
   * exclusively for JSESSIONID-based sessions. OAuth token lifecycle is
   * governed solely by {@code oauthTokenExpiry}.
   * <ul>
   * <li>serverUrl - The server URL this token is for</li>
   * <li>accessToken - The OAuth access token</li>
   * <li>refreshToken - The OAuth refresh token (maybe null)</li>
   * <li>tokenType - The token type (typically "Bearer")</li>
   * <li>idpRegistrationId - The IdP registration ID (e.g., "azure")</li>
   * <li>expiresInSeconds - Access token lifetime in seconds (fallback for
   * opaque tokens)</li>
   * <li>expEpochSeconds - Absolute expiry from the JWT {@code exp} claim
   * (epoch seconds); {@code 0} if opaque / unreadable</li>
   * <li>username - The authenticated username (from ID token or
   * userinfo)</li>
   * </ul>
   *
   * @param tokenData The OAuth token data to store
   */
  public void storeOAuthToken( OAuthTokenData tokenData ) {
    if ( tokenData.accessToken() == null || tokenData.accessToken().isEmpty() ) {
      log.logError( message( "BrowserAuthSessionHolder.EmptyAccessToken" ) );
      return;
    }

    SessionData session = currentSession.get();
    if ( session == null ) {
      session = new SessionData();
    }

    session.serverUrl = tokenData.serverUrl();
    session.username = tokenData.username();
    session.oauthAccessToken = tokenData.accessToken();
    session.oauthRefreshToken = tokenData.refreshToken();
    session.oauthTokenType = tokenData.tokenType() != null ? tokenData.tokenType() : TOKEN_TYPE_BEARER;
    session.oauthIdpRegistrationId = tokenData.idpRegistrationId();

    session.oauthTokenExpiry = resolveOAuthTokenExpiry( tokenData.expiresInSeconds(), tokenData.expEpochSeconds() );

    currentSession.set( session );
    persistSession( session );

    if ( log.isBasic() ) {
      log.logBasic( message( "BrowserAuthSessionHolder.StoredOAuthToken",
        describeTokenExpirySource( tokenData.expiresInSeconds(), tokenData.expEpochSeconds() ) ) );
    }
  }

  // Prefer the absolute exp claim from the JWT (authoritative, set by IdP).
  // Fall back to expires_in-based relative calculation for opaque tokens only.
  // When neither is known, store -1. The guard in getOAuthAccessToken() is
  // (oauthTokenExpiry > 0 && expired), so both 0 and -1 cause the guard to
  // be skipped — the token is returned unconditionally and the server is
  // authoritative for validity via introspection/userinfo. -1 is used as
  // the sentinel here to distinguish "expiry genuinely unknown" from "expiry
  // is epoch zero" (which is an invalid timestamp anyway).
  private long resolveOAuthTokenExpiry( long expiresInSeconds, long expEpochSeconds ) {
    if ( expEpochSeconds > 0 ) {
      return expEpochSeconds * 1000L;
    }
    if ( expiresInSeconds > 0 ) {
      return System.currentTimeMillis() + ( expiresInSeconds * 1000L );
    }
    return UNKNOWN_TOKEN_EXPIRY;
  }

  private String describeTokenExpirySource( long expiresInSeconds, long expEpochSeconds ) {
    if ( expEpochSeconds > 0 ) {
      return "from JWT exp claim";
    }
    if ( expiresInSeconds > 0 ) {
      return "from expires_in";
    }
    return "unknown";
  }

  /**
   * Get the OAuth access token for the specified server.
   * Returns null if no token exists or the token has expired.
   *
   * @param serverUrl The server URL to get token for
   * @return The access token, or null
   */
  public String getOAuthAccessToken( String serverUrl ) {
    SessionData session = currentSession.get();
    if ( session == null || session.oauthAccessToken == null ) {
      return null;
    }
    if ( !normalizeUrl( serverUrl ).equals( normalizeUrl( session.serverUrl ) ) ) {
      return null;
    }
    // Check if OAuth token specifically has expired
    if ( session.oauthTokenExpiry > 0 && System.currentTimeMillis() > session.oauthTokenExpiry ) {
      if ( log.isDebug() ) {
        log.logDebug( message( "BrowserAuthSessionHolder.TokenExpired" ) );
      }
      return null;
    }
    return session.oauthAccessToken;
  }

  /**
   * Get the OAuth refresh token for the specified server.
   *
   * @param serverUrl The server URL
   * @return The refresh token, or null
   */
  public String getOAuthRefreshToken( String serverUrl ) {
    SessionData session = currentSession.get();
    if ( session == null || !normalizeUrl( serverUrl ).equals( normalizeUrl( session.serverUrl ) ) ) {
      return null;
    }
    return session.oauthRefreshToken;
  }

  /**
   * Get the OAuth token type for the specified server.
   *
   * @param serverUrl The server URL
   * @return The token type (e.g., "Bearer"), or null
   */
  public String getOAuthTokenType( String serverUrl ) {
    SessionData session = currentSession.get();
    if ( session == null || !normalizeUrl( serverUrl ).equals( normalizeUrl( session.serverUrl ) ) ) {
      return null;
    }
    return session.oauthTokenType;
  }

  /**
   * Get the IdP registration ID associated with the current OAuth session.
   *
   * @param serverUrl The server URL
   * @return The IdP registration ID, or null
   */
  public String getOAuthIdpRegistrationId( String serverUrl ) {
    SessionData session = currentSession.get();
    if ( session == null || !normalizeUrl( serverUrl ).equals( normalizeUrl( session.serverUrl ) ) ) {
      return null;
    }
    return session.oauthIdpRegistrationId;
  }

  /**
   * Check if a valid OAuth access token exists for the specified server.
   *
   * @param serverUrl The server URL
   * @return true if a non-expired OAuth token exists
   */
  public boolean hasValidOAuthToken( String serverUrl ) {
    return getOAuthAccessToken( serverUrl ) != null;
  }

  /**
   * Return true when the OAuth access token for {@code serverUrl} is expired (or
   * absent) but a broker refresh handle is available, making a silent token
   * refresh possible.
   *
   * @param serverUrl the server URL
   * @return true if a refresh call to the broker should be attempted
   */
  public boolean isOAuthTokenExpiredButRefreshable( String serverUrl ) {
    SessionData session = currentSession.get();
    if ( session == null || !normalizeUrl( serverUrl ).equals( normalizeUrl( session.serverUrl ) ) ) {
      return false;
    }
    if ( session.oauthRefreshHandle == null || session.oauthRefreshHandle.isBlank() ) {
      return false;
    }
    // Token is either absent or its expiry has passed.
    return session.oauthAccessToken == null
      || ( session.oauthTokenExpiry > 0 && System.currentTimeMillis() > session.oauthTokenExpiry );
  }

  /**
   * Get the stored broker refresh handle for {@code serverUrl}.
   *
   * @param serverUrl the server URL
   * @return the refresh handle, or {@code null} if none is stored
   */
  public String getRefreshHandle( String serverUrl ) {
    SessionData session = currentSession.get();
    if ( session == null || !normalizeUrl( serverUrl ).equals( normalizeUrl( session.serverUrl ) ) ) {
      return null;
    }
    return session.oauthRefreshHandle;
  }

  /**
   * Get the broker auth handle (originating auth-flow handle) stored for
   * {@code serverUrl}.
   *
   * @param serverUrl the server URL
   * @return the broker auth handle, or {@code null} if none is stored
   */
  public String getBrokerAuthHandle( String serverUrl ) {
    SessionData session = currentSession.get();
    if ( session == null || !normalizeUrl( serverUrl ).equals( normalizeUrl( session.serverUrl ) ) ) {
      return null;
    }
    return session.oauthBrokerAuthHandle;
  }

  /**
   * Store the broker auth handle (originating auth-flow handle) for
   * {@code serverUrl}. This value is intentionally process-local only: the
   * server-side auth state expires within minutes, so persisting it across Pan
   * invocations would only create stale auth-handle mismatches during refresh.
   *
   * @param serverUrl        the server URL
   * @param brokerAuthHandle the handle to store (null to clear)
   */
  public void setBrokerAuthHandle( String serverUrl, String brokerAuthHandle ) {
    SessionData session = currentSession.get();
    if ( session == null || !normalizeUrl( serverUrl ).equals( normalizeUrl( session.serverUrl ) ) ) {
      return;
    }
    session.oauthBrokerAuthHandle = brokerAuthHandle;
    currentSession.set( session );
    persistSession( session );
  }

  /**
   * Store an opaque refresh handle returned by the server during initial auth
   * or after a successful token refresh. The handle is an immutable UUID-like
   * token that can be presented to {@code POST /api/oauth/token-refresh}.
   *
   * @param serverUrl     the server URL
   * @param refreshHandle the handle to store (null to clear)
   */
  public void setRefreshHandle( String serverUrl, String refreshHandle ) {
    SessionData session = currentSession.get();
    if ( session == null || !normalizeUrl( serverUrl ).equals( normalizeUrl( session.serverUrl ) ) ) {
      return;
    }
    session.oauthRefreshHandle = refreshHandle;
    currentSession.set( session );
    persistSession( session );
    if ( log.isDebug() ) {
      log.logDebug( message( "BrowserAuthSessionHolder.StoredRefreshHandle" ) );
    }
  }

  @Override
  public Optional<String> findAccessToken( String serverUrl ) {
    return Optional.ofNullable( getOAuthAccessToken( serverUrl ) );
  }

  @Override
  public Optional<String> findSessionCookie( String serverUrl ) {
    SessionData session = currentSession.get();
    if ( isSessionValid( session, serverUrl ) ) {
      return Optional.ofNullable( session.sessionCookie );
    }
    return Optional.empty();
  }

  /**
   * Update the OAuth access token after a refresh operation.
   * Preserves the existing refresh token and IdP info.
   * <p>
   * Token expiry is determined from the JWT {@code exp} claim when available;
   * falls back to {@code expiresInSeconds} for opaque tokens.
   * {@code expiryTime} is NOT touched — it is for JSESSIONID sessions only.
   *
   * @param serverUrl        The server URL
   * @param newAccessToken   The new access token
   * @param newRefreshToken  The new refresh token (maybe null to keep existing)
   * @param expiresInSeconds New access token lifetime in seconds (fallback)
   * @param expEpochSeconds  Absolute expiry from the JWT {@code exp} claim
   *                         (epoch seconds); {@code 0} if opaque / unreadable
   */
  public void updateOAuthToken( String serverUrl, String newAccessToken,
                                String newRefreshToken, long expiresInSeconds,
                                long expEpochSeconds ) {
    SessionData session = currentSession.get();
    if ( session == null || !normalizeUrl( serverUrl ).equals( normalizeUrl( session.serverUrl ) ) ) {
      log.logError( message( "BrowserAuthSessionHolder.UpdateWithoutSession" ) );
      return;
    }

    session.oauthAccessToken = newAccessToken;
    if ( newRefreshToken != null ) {
      session.oauthRefreshToken = newRefreshToken;
    }

    // Prefer the absolute exp claim from the JWT (authoritative, set by IdP).
    if ( expEpochSeconds > 0 ) {
      session.oauthTokenExpiry = expEpochSeconds * 1000L;
    } else {
      session.oauthTokenExpiry = System.currentTimeMillis() + ( expiresInSeconds * 1000L );
    }

    currentSession.set( session );
    persistSession( session );

    if ( log.isDebug() ) {
      log.logDebug( message( "BrowserAuthSessionHolder.UpdatedAfterRefresh" ) );
    }
  }

  /**
   * Clears only the JSESSIONID-related fields ({@code sessionToken},
   * {@code sessionCookie}, {@code expiryTime}), leaving OAuth token fields
   * intact. Called before storing a fresh OAuth token from a PKCE or
   * device-code flow to prevent stale JSESSIONID fields from a prior
   * session-capture auth surviving in the new credential file.
   * <p>
   * Does NOT persist — the caller is expected to call {@link #storeOAuthToken}
   * immediately afterward, which persists the cleaned session.
   *
   * @param serverUrl the server URL
   */
  public void clearSessionFields( String serverUrl ) {
    SessionData session = currentSession.get();
    if ( session == null || !normalizeUrl( serverUrl ).equals( normalizeUrl( session.serverUrl ) ) ) {
      return;
    }
    session.sessionToken = null;
    session.sessionCookie = null;
    session.expiryTime = 0;
    // currentSession still points to the same object — no set() needed.
  }

  /**
   * Clear only the OAuth portion of the session, leaving any browser-based
   * session intact.
   *
   * @param serverUrl The server URL
   */
  public void clearOAuthToken( String serverUrl ) {
    SessionData session = currentSession.get();
    if ( session != null && normalizeUrl( serverUrl ).equals( normalizeUrl( session.serverUrl ) ) ) {
      session.oauthAccessToken = null;
      session.oauthRefreshToken = null;
      session.oauthRefreshHandle = null;
      session.oauthBrokerAuthHandle = null;
      session.oauthTokenType = null;
      session.oauthIdpRegistrationId = null;
      session.oauthTokenExpiry = 0;
      if ( hasBrowserSessionFields( session ) ) {
        currentSession.set( session );
        persistSession( session );
      } else {
        currentSession.set( null );
        deletePersistedSession();
      }
      if ( log.isDebug() ) {
        log.logDebug( message( "BrowserAuthSessionHolder.ClearedOAuthTokens" ) );
      }
    }
  }

  private boolean isSessionValid( SessionData session, String serverUrl ) {
    if ( session == null ) {
      return false;
    }
    if ( !normalizeUrl( serverUrl ).equals( normalizeUrl( session.serverUrl ) ) ) {
      return false;
    }
    if ( isOAuthSession( session ) ) {
      return isValidOAuthSession( session );
    } else {
      return isValidJSession( session );
    }
  }

  private boolean isOAuthSession( SessionData session ) {
    return session.oauthAccessToken != null;
  }

  private boolean isValidOAuthSession( SessionData session ) {
    boolean accessTokenValid = session.oauthTokenExpiry <= 0
      || System.currentTimeMillis() < session.oauthTokenExpiry;
    if ( accessTokenValid || hasRefreshCapability( session ) ) {
      return true;
    } else {
      if ( log.isDebug() ) {
        log.logDebug( message( "BrowserAuthSessionHolder.SessionExpiredNoRefresh" ) );
      }
      return false;
    }
  }

  private boolean hasRefreshCapability( SessionData session ) {
    return ( session.oauthRefreshToken != null && !session.oauthRefreshToken.isEmpty() )
      || ( session.oauthRefreshHandle != null && !session.oauthRefreshHandle.isBlank() );
  }

  private boolean isValidJSession( SessionData session ) {
    if ( !hasBrowserSessionFields( session ) ) {
      return false;
    }
    // expiryTime <= 0 means "no expiry set" — treat as valid until explicitly
    // cleared.
    // A value of -1 is the sentinel for "unknown expiry"; comparing currentTimeMs >
    // -1
    // was previously always true, making every cookie-based session immediately
    // invalid.
    if ( session.expiryTime > 0 && System.currentTimeMillis() > session.expiryTime ) {
      if ( log.isDebug() ) {
        log.logDebug( message( "BrowserAuthSessionHolder.SessionExpired" ) );
      }
      return false;
    }
    return true;
  }

  private boolean hasBrowserSessionFields( SessionData session ) {
    return session != null
      && ( ( session.sessionToken != null && !session.sessionToken.isBlank() )
      || ( session.sessionCookie != null && !session.sessionCookie.isBlank() ) );
  }

  private String normalizeUrl( String url ) {
    if ( url == null ) {
      return "";
    }
    return stripTrailingSlashes( url.toLowerCase() );
  }

  private String stripTrailingSlashes( String url ) {
    int end = url.length();
    while ( end > 0 && url.charAt( end - 1 ) == '/' ) {
      end--;
    }
    return url.substring( 0, end );
  }

  private void persistSession( SessionData session ) {
    tokenStore.save( toStoredCredential( session ) );
  }

  // -------------------------------------------------------------------------
  // Persistence adapters — translate between in-memory SessionData and the
  // immutable StoredCredential record used by the TokenStore contract.
  // -------------------------------------------------------------------------

  private void loadPersistedSession() {
    tokenStore.load().ifPresent( credential -> {
      SessionData session = fromStoredCredential( credential );

      // Determine if this persisted session is still usable.
      // For JSESSIONID sessions: expiryTime is authoritative.
      // For OAuth sessions: oauthTokenExpiry governs access token validity,
      // and a refresh token may allow silent renewal even after expiry.
      // Never discard an OAuth session that still has a refresh token.
      boolean hasOAuthTokens = session.oauthAccessToken != null;
      boolean sessionStillValid;
      if ( hasOAuthTokens ) {
        // OAuth: keep if access token is still valid OR broker refresh capability is
        // available.
        // Broker-owned flows persist oauthRefreshHandle, not a raw refresh token.
        boolean accessTokenValid = session.oauthTokenExpiry <= 0
          || System.currentTimeMillis() < session.oauthTokenExpiry;
        sessionStillValid = accessTokenValid || hasRefreshCapability( session );
      } else {
        // JSESSIONID: keep only real browser-session credentials. The server's
        // 401 remains authoritative for expiry, but a shell session that has no
        // session token/cookie left is stale metadata and must not be restored.
        sessionStillValid = hasBrowserSessionFields( session );
      }

      if ( sessionStillValid ) {
        currentSession.set( session );
        if ( log.isDebug() ) {
          log.logDebug( message( "BrowserAuthSessionHolder.RestoredSession", session.serverUrl ) );
        }
      } else {
        tokenStore.delete();
      }
    } );
  }

  private void deletePersistedSession() {
    tokenStore.delete();
  }

  /**
   * Internal class to hold session data.
   */
  private static class SessionData {
    String serverUrl;
    String sessionToken;
    String sessionCookie;
    String username;
    long expiryTime;
    String oauthAccessToken;
    String oauthRefreshToken;
    String oauthTokenType;
    String oauthIdpRegistrationId;
    long oauthTokenExpiry;
    /**
     * Opaque broker handle for POST /api/oauth/token-refresh. Never sent as auth.
     */
    String oauthRefreshHandle;
    /**
     * Broker auth handle that originated this credential (bound at auth-complete
     * time).
     */
    String oauthBrokerAuthHandle;
  }
}
