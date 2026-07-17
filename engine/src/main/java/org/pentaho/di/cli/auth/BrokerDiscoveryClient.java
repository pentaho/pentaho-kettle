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

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thin client that fetches broker discovery from the Pentaho server.
 */
public class BrokerDiscoveryClient {

  private static final LogChannelInterface LOG = new LogChannel( "BrokerDiscovery" );

  @SuppressWarnings( "java:S1075" ) // hardcoded URI
  private static final String DEFAULT_DISCOVERY_PATH = "/api/pentaho-oauth/discovery";
  private static final String FIELD_VERSION = "version";
  private static final String FIELD_DPOP_ENABLED = "dpopEnabled";
  private static final String FIELD_AVAILABLE_IDPS = "availableIdps";
  private static final String FIELD_SUPPORTS_CLIENT_CREDENTIALS = "supportsClientCredentials";
  private static final String FIELD_REGISTRATION_ID = "registrationId";
  private static final String FIELD_AUTHORIZATION_URI = "authorizationUri";
  private static final String FIELD_TOKEN_URI = "tokenUri";
  private static final String FIELD_DEVICE_CODE_URI = "deviceCodeUri";
  private static final String FIELD_CLIENT_ID = "clientId";
  private static final String FIELD_DEVICE_CODE_CLIENT_ID = "deviceCodeClientId";
  private static final String FIELD_SERVICE_CLIENT_ID = "serviceClientId";
  private static final String FIELD_SCOPE = "scope";
  private static final String FIELD_DEVICE_CODE_SCOPE = "deviceCodeScope";
  private static final String FIELD_CLIENT_CREDENTIALS_SCOPE = "clientCredentialsScope";
  private static final String FIELD_EFFECTIVE_CLIENT_AUTH_METHOD = "effectiveClientAuthMethod";
  private static final String FIELD_REQUIRES_CLIENT_SECRET = "requiresClientSecret";
  private static final String FIELD_SERVICE_CLIENT_REQUIRES_SECRET = "serviceClientRequiresSecret";
  private static final String FIELD_AUTHORIZATION_GRANT_TYPE = "authorizationGrantType";
  private static final String FIELD_CLIENT_AUTHENTICATION_METHOD = "clientAuthenticationMethod";
  private static final String FIELD_LOCAL_REDIRECT_PATH = "localRedirectPath";
  private static final int CONNECT_TIMEOUT_MS = 10_000;
  private static final int READ_TIMEOUT_MS = 10_000;
  static final long DEFAULT_CACHE_TTL_MS = 3_600_000L;

  private final OAuthHttpClient httpClient;
  private final AtomicReference<CachedDiscovery> cached = new AtomicReference<>();
  private final long cacheTtlMs;
  private final String discoveryPath;

  private static String message( String key, String... tokens ) {
    return BaseMessages.getString( BrokerDiscoveryClient.class, key, tokens );
  }

  public BrokerDiscoveryClient() {
    this( new OAuthHttpClient( CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS ), DEFAULT_CACHE_TTL_MS, DEFAULT_DISCOVERY_PATH );
  }

  BrokerDiscoveryClient( OAuthHttpClient httpClient ) {
    this( httpClient, DEFAULT_CACHE_TTL_MS, DEFAULT_DISCOVERY_PATH );
  }

  BrokerDiscoveryClient( OAuthHttpClient httpClient, long cacheTtlMs ) {
    this( httpClient, cacheTtlMs, DEFAULT_DISCOVERY_PATH );
  }

  BrokerDiscoveryClient( OAuthHttpClient httpClient, long cacheTtlMs, String discoveryPath ) {
    this.httpClient = httpClient;
    this.cacheTtlMs = cacheTtlMs;
    this.discoveryPath = discoveryPath;
  }

  /**
   * Fetches broker discovery from the server. Caches the result with a TTL.
   *
   * @param serverUrl the base Pentaho server URL (e.g.,
   *                  {@code http://localhost:8080/pentaho})
   * @return an unmodifiable map of discovery fields, or empty map on failure
   */
  public Map<String, Object> fetchDiscovery( String serverUrl ) {
    return fetchDiscovery( serverUrl, null );
  }

  public Map<String, Object> fetchDiscovery( String serverUrl, String preferredIdp ) {
    String normalizedServerUrl = normalizeUrl( serverUrl );
    String normalizedPreferredIdp = normalizePreferredIdp( preferredIdp );
    CachedDiscovery existing = cached.get();
    if ( existing != null
      && existing.serverUrl.equals( normalizedServerUrl )
      && Objects.equals( existing.preferredIdp, normalizedPreferredIdp )
      && !existing.isExpired( cacheTtlMs ) ) {
      return existing.discovery;
    }

    Map<String, Object> discovery = doFetch( serverUrl, normalizedPreferredIdp );
    cached.set(
      new CachedDiscovery( normalizedServerUrl, normalizedPreferredIdp, discovery, System.nanoTime() ) );
    return discovery;
  }

  /**
   * Checks whether the broker advertises OAuth support.
   */
  public boolean isBrokerOAuthAvailable( String serverUrl ) {
    return !getAvailableIdps( serverUrl ).isEmpty();
  }

  /**
   * Returns true if the server advertises client-credentials support
   * (i.e., a {@code serviceClientId} and {@code clientCredentialsScope}
   * are both present in the discovery response).
   */
  public boolean isClientCredentialsAvailable( String serverUrl ) {
    return booleanValue( fetchDiscovery( serverUrl ).get( FIELD_SUPPORTS_CLIENT_CREDENTIALS ) );
  }

  public boolean isDpopEnabled( String serverUrl ) {
    Object value = fetchDiscovery( serverUrl ).get( FIELD_DPOP_ENABLED );
    if ( value instanceof Boolean bool ) {
      return bool;
    }
    return value != null && Boolean.parseBoolean( value.toString() );
  }

  /**
   * Returns the list of IdP config key names advertised by the server.
   * <p>
   * Each value in the returned list is a valid {@code registration_id}
   * argument for the broker auth-start endpoint. Returns an empty list when
   * the server does not publish this field (older server versions).
   *
   * @param serverUrl the base Pentaho server URL
   * @return unmodifiable list of IdP names, never null
   */
  @SuppressWarnings( "unchecked" )
  public List<String> getAvailableIdps( String serverUrl ) {
    Object value = fetchDiscovery( serverUrl ).get( FIELD_AVAILABLE_IDPS );
    if ( value instanceof List<?> list ) {
      return Collections.unmodifiableList( (List<String>) list );
    }
    return Collections.emptyList();
  }

  /**
   * Invalidates the cached discovery, forcing a fresh fetch on next call.
   */
  public void invalidateCache() {
    cached.set( null );
  }

  private Map<String, Object> doFetch( String serverUrl, String preferredIdp ) {
    String url = buildDiscoveryUrl( serverUrl, preferredIdp );
    LOG.logBasic( message( "BrokerDiscovery.Fetching", url ) );

    try {
      String body = httpClient.getJson( url );
      if ( body == null ) {
        LOG.logBasic( message( "BrokerDiscovery.Non200" ) );
        return Collections.emptyMap();
      }
      return parseJson( body );
    } catch ( IOException e ) {
      LOG.logBasic( message( "BrokerDiscovery.FetchFailed", url, e.getMessage() ) );
      LOG.logDebug( message( "BrokerDiscovery.FetchException" ), e );
      return Collections.emptyMap();
    }
  }

  /**
   * Lightweight JSON parsing using OAuthHttpClient's Gson-backed helpers.
   */
  private Map<String, Object> parseJson( String json ) {
    Map<String, Object> result = new LinkedHashMap<>();
    if ( json == null || json.isBlank() ) {
      return result;
    }
    String version = httpClient.extractJsonString( json, FIELD_VERSION );
    if ( version != null ) {
      result.put( FIELD_VERSION, version );
    }
    httpClient.extractJsonBoolean( json, FIELD_DPOP_ENABLED )
      .ifPresent( dpopEnabled -> result.put( FIELD_DPOP_ENABLED, dpopEnabled ) );
    httpClient.extractJsonBoolean( json, FIELD_SUPPORTS_CLIENT_CREDENTIALS )
      .ifPresent( supported -> result.put( FIELD_SUPPORTS_CLIENT_CREDENTIALS, supported ) );
    putStringIfPresent( result, json, FIELD_REGISTRATION_ID );
    putStringIfPresent( result, json, FIELD_AUTHORIZATION_URI );
    putStringIfPresent( result, json, FIELD_TOKEN_URI );
    putStringIfPresent( result, json, FIELD_DEVICE_CODE_URI );
    putStringIfPresent( result, json, FIELD_CLIENT_ID );
    putStringIfPresent( result, json, FIELD_DEVICE_CODE_CLIENT_ID );
    putStringIfPresent( result, json, FIELD_SERVICE_CLIENT_ID );
    putStringIfPresent( result, json, FIELD_SCOPE );
    putStringIfPresent( result, json, FIELD_DEVICE_CODE_SCOPE );
    putStringIfPresent( result, json, FIELD_CLIENT_CREDENTIALS_SCOPE );
    putStringIfPresent( result, json, FIELD_EFFECTIVE_CLIENT_AUTH_METHOD );
    httpClient.extractJsonBoolean( json, FIELD_REQUIRES_CLIENT_SECRET )
      .ifPresent( value -> result.put( FIELD_REQUIRES_CLIENT_SECRET, value ) );
    httpClient.extractJsonBoolean( json, FIELD_SERVICE_CLIENT_REQUIRES_SECRET )
      .ifPresent( value -> result.put( FIELD_SERVICE_CLIENT_REQUIRES_SECRET, value ) );
    putStringIfPresent( result, json, FIELD_AUTHORIZATION_GRANT_TYPE );
    putStringIfPresent( result, json, FIELD_CLIENT_AUTHENTICATION_METHOD );
    putStringIfPresent( result, json, FIELD_LOCAL_REDIRECT_PATH );
    // Parse the availableIdps string array
    List<String> availableIdps = httpClient.extractJsonArray( json, FIELD_AVAILABLE_IDPS );
    if ( !availableIdps.isEmpty() ) {
      result.put( FIELD_AVAILABLE_IDPS, availableIdps );
    }
    return Collections.unmodifiableMap( result );
  }

  private void putStringIfPresent( Map<String, Object> result, String json, String field ) {
    String value = httpClient.extractJsonString( json, field );
    if ( value != null ) {
      result.put( field, value );
    }
  }

  private String buildDiscoveryUrl( String serverUrl, String preferredIdp ) {
    String url = normalizeUrl( serverUrl ) + discoveryPath;
    if ( preferredIdp == null ) {
      return url;
    }
    return url + "?registration_id=" + URLEncoder.encode( preferredIdp, StandardCharsets.UTF_8 );
  }

  private String normalizePreferredIdp( String preferredIdp ) {
    if ( preferredIdp == null ) {
      return null;
    }
    String trimmed = preferredIdp.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private static String normalizeUrl( String url ) {
    return url.endsWith( "/" ) ? url.substring( 0, url.length() - 1 ) : url;
  }

  private static boolean booleanValue( Object value ) {
    if ( value instanceof Boolean bool ) {
      return bool;
    }
    return value != null && Boolean.parseBoolean( value.toString() );
  }

  private record CachedDiscovery(String serverUrl, String preferredIdp, Map<String, Object> discovery,
                                 long fetchedAtNanos) {
    boolean isExpired( long cacheTtlMs ) {
      if ( cacheTtlMs <= 0 ) {
        return true;
      }
      long cacheTtlNanos = TimeUnit.MILLISECONDS.toNanos( cacheTtlMs );
      return System.nanoTime() - fetchedAtNanos >= cacheTtlNanos;
    }
  }
}
