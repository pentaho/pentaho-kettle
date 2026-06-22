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

package org.pentaho.di.core.database;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.HttpClientManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * JVM-wide Keycloak bearer token provider for the connection-management service.
 *
 * <h3>How it works</h3>
 * Uses the OAuth 2.0 <em>client credentials</em> grant to obtain a short-lived access token
 * from the Keycloak token endpoint configured via {@link Const#getCmsTokenUrl()}.
 * The token is cached and reused until it is within {@link #EXPIRY_BUFFER_MS} of expiry,
 * at which point a new token is fetched transparently.
 *
 * <h3>Configuration</h3>
 * Three environment variables (or JVM system properties) must all be set for authentication
 * to be active:
 * <ul>
 *   <li>{@code CMS_TOKEN_URL} — full Keycloak token endpoint, e.g.
 *       {@code https://keycloak.example.com/realms/pdi/protocol/openid-connect/token}</li>
 *   <li>{@code CMS_CLIENT_ID} — Keycloak client ID for the PDI service account</li>
 *   <li>{@code CMS_CLIENT_SECRET} — Keycloak client secret for the PDI service account</li>
 * </ul>
 * If any of these is absent, {@link #getToken()} returns {@code null} and callers should
 * proceed without an {@code Authorization} header (useful for local dev / test environments).
 *
 * <h3>Thread safety</h3>
 * The cached {@link TokenEntry} is held in an {@link AtomicReference}. The fast path (valid
 * cached token) is lock-free. The slow path (token fetch) is guarded by {@code synchronized}
 * with a double-check to prevent concurrent callers from each issuing a separate token request.
 */
public class CmsTokenProvider {

  private static final CmsTokenProvider INSTANCE = new CmsTokenProvider();
  private static final LogChannelInterface log = LogChannel.GENERAL;
  /**
   * Seconds before the token's stated expiry time at which we proactively refresh.
   * Prevents using a token that expires mid-request due to clock skew or network latency.
   */
  private static final long EXPIRY_BUFFER_MS = 30_000;
  private final AtomicReference<TokenEntry> cached = new AtomicReference<>();

  private CmsTokenProvider() {
    // Prevents instantiation.
  }

  /**
   * Returns the singleton instance.
   */
  public static CmsTokenProvider getInstance() {
    return INSTANCE;
  }

  /**
   * Returns a valid bearer token for the connection-management service.
   *
   * <p>If Keycloak is not configured (any of {@code CMS_TOKEN_URL}, {@code CMS_CLIENT_ID},
   * {@code CMS_CLIENT_SECRET} is absent), returns {@code null} — callers should skip the
   * {@code Authorization} header in that case.
   *
   * <p>Otherwise returns a cached token if still valid, or fetches a fresh one.
   *
   * @return a bearer token string, or {@code null} if authentication is not configured
   * @throws KettleDatabaseException if the token endpoint returns an error
   */
  public String getToken() throws KettleDatabaseException {
    if ( !isConfigured() ) {
      return null;
    }

    // Fast path — lock-free read of the cached entry.
    TokenEntry entry = cached.get();
    if ( entry != null && System.currentTimeMillis() < entry.validUntilMs ) {
      return entry.accessToken;
    }

    return fetchAndCache();
  }

  private boolean isConfigured() {
    return Const.getCmsTokenUrl() != null
      && Const.getCmsClientId() != null
      && Const.getCmsClientSecret() != null;
  }

  // ---------------------------------------------------------------------------
  // Internal helpers
  // ---------------------------------------------------------------------------

  /**
   * Fetches a fresh token from Keycloak. Synchronized to ensure only one thread issues
   * the request when the cache is stale; all other threads wait and then reuse the result.
   */
  private synchronized String fetchAndCache() throws KettleDatabaseException {
    // Double-check: another thread may have refreshed the token while we waited for the lock.
    TokenEntry entry = cached.get();
    if ( entry != null && System.currentTimeMillis() < entry.validUntilMs ) {
      return entry.accessToken;
    }

    String tokenUrl = Const.getCmsTokenUrl();
    String clientId = Const.getCmsClientId();
    String clientSecret = Const.getCmsClientSecret();

    log.logDebug( "CmsTokenProvider: fetching bearer token from " + tokenUrl );

    String body = "grant_type=client_credentials"
      + "&client_id=" + clientId
      + "&client_secret=" + clientSecret;

    HttpClientManager manager = HttpClientManager.getInstance();
    try ( var client = manager.createDefaultClient() ) {
      var request = new HttpPost( tokenUrl );

      request.addHeader( "Content-Type", "application/x-www-form-urlencoded" );
      request.addHeader( "Accept", "application/json" );
      request.setEntity( new StringEntity( body, StandardCharsets.UTF_8 ) );

      var response = client.execute( request );

      int status = response.getStatusLine().getStatusCode();
      if ( status != HttpURLConnection.HTTP_OK ) {
        throw new KettleDatabaseException(
          "CmsTokenProvider: Keycloak token request failed — HTTP " + status
            + " from " + tokenUrl );
      }

      Map<?, ?> responseBody;
      try ( java.io.InputStream is = response.getEntity().getContent() ) {
        responseBody = new ObjectMapper().readValue( is, Map.class );
      }

      Object tokenObj = responseBody.get( "access_token" );
      if ( tokenObj == null ) {
        throw new KettleDatabaseException(
          "CmsTokenProvider: Keycloak response did not contain 'access_token'" );
      }
      String accessToken = tokenObj.toString();

      long expiresInMs = 300_000L; // default 5 min if field is absent
      Object expiresInObj = responseBody.get( "expires_in" );
      if ( expiresInObj instanceof Number ) {
        expiresInMs = ( (Number) expiresInObj ).longValue() * 1000L;
      }
      long validUntilMs = System.currentTimeMillis() + expiresInMs - EXPIRY_BUFFER_MS;

      cached.set( new TokenEntry( accessToken, validUntilMs ) );
      log.logDebug( "CmsTokenProvider: token acquired, valid for ~" + ( expiresInMs / 1000 ) + "s" );
      return accessToken;

    } catch ( KettleDatabaseException e ) {
      throw e;
    } catch ( Exception e ) {
      throw new KettleDatabaseException(
        "CmsTokenProvider: failed to fetch token from '" + tokenUrl + "': " + e.getMessage(), e );
    }
  }

  /**
   * Holds the cached access token and the absolute time (epoch ms) at which it should
   * be considered expired for our purposes ({@code issued_at + expires_in_ms - buffer}).
   */
  private static final class TokenEntry {
    final String accessToken;
    final long validUntilMs;

    TokenEntry( String accessToken, long validUntilMs ) {
      this.accessToken = accessToken;
      this.validUntilMs = validUntilMs;
    }
  }
}
