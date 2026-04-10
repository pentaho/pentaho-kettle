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

package org.pentaho.di.ui.spoon.session;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;

import java.net.URI;
import java.util.Map;

/**
 * Context class for managing authentication using strategy pattern.
 * <p>
 * This class provides a high-level API for authentication operations
 * while delegating the actual authentication logic to a specific strategy.
 * <p>
 * Usage:
 * <pre>
 * // Create context with default (SESSION) strategy
 * AuthenticationContext context = new AuthenticationContext( serverUri );
 *
 * // Store authentication credentials
 * context.storeJSessionId( "ABC123..." );
 *
 * // Check if authenticated
 * if ( context.isAuthenticated() ) {
 *   String sessionId = context.getJSessionId();
 * }
 * </pre>
 */
public class AuthenticationContext {

  private static final LogChannelInterface log = new LogChannel( "AuthenticationContext" );
  private static final String JSESSIONID_KEY = "jsessionid";
  private static final String VALIDATION_ENDPOINT = "api/system/authentication-provider";

  private static final int CONNECT_TIMEOUT_MS = 10_000;
  private static final int READ_TIMEOUT_MS = 10_000;

  /**
   * Marker token indicating browser-based session authentication is in use.
   * When this value is used as the authentication token, the system should use
   * the stored JSESSIONID instead of a real password.
   */
  public static final String SESSION_AUTH_TOKEN = "_SESSION_AUTH_";

  private final URI serverUri;
  private final AuthenticationStrategy strategy;

  /**
   * Create an authentication context with the default strategy
   *
   * @param serverUri The server URI
   */
  public AuthenticationContext( URI serverUri ) {
    this( serverUri, AuthenticationStrategyFactory.getDefaultStrategy() );
  }

  /**
   * Create an authentication context with a specific strategy
   *
   * @param serverUri The server URI
   * @param strategy  The authentication strategy to use
   */
  public AuthenticationContext( URI serverUri, AuthenticationStrategy strategy ) {
    if ( serverUri == null ) {
      throw new IllegalArgumentException( "Server URI cannot be null" );
    }
    if ( strategy == null ) {
      throw new IllegalArgumentException( "Authentication strategy cannot be null" );
    }

    this.serverUri = serverUri;
    this.strategy = strategy;

    log.logDetailed( "Created authentication context for " + serverUri + " with strategy: " + strategy.getAuthType() );
  }


  /**
   * Get the current authentication type
   *
   * @return The authentication type identifier
   */
  public String getAuthType() {
    return strategy.getAuthType();
  }

  /**
   * Get all authentication credentials
   *
   * @return Map of credentials, or null if not found
   */
  public Map<String, Object> getCredentials() {
    return strategy.getCredentials( serverUri );
  }

  /**
   * Check if authenticated
   *
   * @return True if valid authentication exists, false otherwise
   */
  public boolean isAuthenticated() {
    return strategy.isAuthenticated( serverUri );
  }

  /**
   * Clear authentication credentials
   */
  public void clearCredentials() {
    strategy.clearCredentials( serverUri );
  }

  /**
   * Get a specific credential value
   *
   * @param key The credential key
   * @return The credential value, or null if not found
   */
  public String getCredentialValue( String key ) {
    return strategy.getCredentialValue( serverUri, key );
  }

  /**
   * Store a specific credential value
   *
   * @param key   The credential key
   * @param value The credential value
   */
  public void storeCredentialValue( String key, String value ) {
    strategy.storeCredentialValue( serverUri, key, value );
  }

  /**
   * Remove a specific credential value from storage while preserving
   * other credentials and metadata (e.g., the browser-auth marker).
   *
   * @param key The credential key to remove
   */
  public void clearCredentialValue( String key ) {
    strategy.clearCredentialValue( serverUri, key );
  }

  /**
   * Convenience method: Get JSESSIONID (for session-based auth)
   *
   * @return The JSESSIONID value, or null if not found
   */
  public String getJSessionId() {
    if ( strategy instanceof SessionBasedAuthStrategy ) {
      return ( (SessionBasedAuthStrategy) strategy ).getJSessionId( serverUri );
    }
    return getCredentialValue( JSESSIONID_KEY );
  }

  /**
   * Convenience method: Store JSESSIONID (for session-based auth)
   *
   * @param jsessionId The JSESSIONID value
   */
  public void storeJSessionId( String jsessionId ) {
    if ( strategy instanceof SessionBasedAuthStrategy ) {
      ( (SessionBasedAuthStrategy) strategy ).storeJSessionId( serverUri, jsessionId );
    } else {
      storeCredentialValue( JSESSIONID_KEY, jsessionId );
    }
  }

  /**
   * Validate if the current session is still active on the server.
   * This makes a lightweight API call to check if the session is valid.
   *
   * @return true if session is valid, false if expired or invalid
   */
  public boolean isSessionValid() {
    if ( !isAuthenticated() ) {
      return false;
    }

    String jsessionId = getJSessionId();
    if ( jsessionId == null || jsessionId.trim().isEmpty() ) {
      return false;
    }

    try ( Client client = createClient() ) {
      String validationUrl = serverUri.toString();
      if ( !validationUrl.endsWith( "/" ) ) {
        validationUrl += "/";
      }
      validationUrl += VALIDATION_ENDPOINT;

      WebTarget target = client.target( validationUrl );
      try ( Response response = target.request()
        .header( "Cookie", "JSESSIONID=" + jsessionId )
        .get() ) {

        int status = response.getStatus();

        if ( status >= 200 && status < 300 ) {
          log.logDetailed( "Session validation successful, status: " + status );
          return true;
        }

        // 3xx redirects indicate the session is valid but the request was redirected
        if ( status >= 300 && status < 400 ) {
          log.logDetailed( "Session validation returned redirect status " + status + ", treating as valid session" );
          return true;
        }

        if ( status == 401 || status == 403 ) {
          log.logBasic( "Session validation failed with status " + status + ", session is expired" );
          return false;
        }

        log.logBasic( "Session validation returned unexpected status " + status + ", treating as invalid session" );
        return false;

      }

    } catch ( Exception e ) {
      log.logError( "Session validation failed due to exception: " + e.getMessage(), e );
      return false;
    }
  }

  /**
   * Creates a JAX-RS Client for session validation.
   * Package-visible to allow overriding in tests.
   */
  Client createClient() {
    ClientConfig config = new ClientConfig();
    config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT_MS );
    config.property( ClientProperties.READ_TIMEOUT, READ_TIMEOUT_MS );
    return ClientBuilder.newClient( config );
  }

  /**
   * Validate session and clear credentials if expired.
   * This is a convenience method that combines validation and cleanup.
   * Note: This removes the JSESSIONID but keeps the auth method marker
   * so we can detect browser auth was previously used.
   *
   * @return true if session is valid, false if expired (and cleared)
   */
  public boolean validateAndClearIfExpired() {
    if ( !isAuthenticated() ) {
      return false;
    }

    if ( !isSessionValid() ) {
      log.logBasic( "Session expired, clearing JSESSIONID for " + serverUri );
      // Remove the expired JSESSIONID entry from storage while preserving the
      // browser-auth marker so wasPreviouslyAuthenticated() still returns true.
      if ( strategy instanceof SessionBasedAuthStrategy ) {
        clearCredentialValue( JSESSIONID_KEY );
      } else {
        // For other strategies, clear all credentials
        clearCredentials();
      }
      return false;
    }

    return true;
  }

  /**
   * Check if browser authentication was previously used for this server.
   * This returns true even if the session has expired, as it indicates
   * the authentication method that should be used for reconnection.
   *
   * @return true if browser auth was ever used for this server, false otherwise
   */
  public boolean wasPreviouslyAuthenticated() {
    // Only return true if this is session-based auth with browser auth marker
    // This ensures we don't show session expiry warnings for password-based authentication
    if ( strategy instanceof SessionBasedAuthStrategy ) {
      return ( (SessionBasedAuthStrategy) strategy ).hasBrowserAuthMarker( serverUri );
    }
    // For non-session strategies (e.g., password-based), this should always return false
    return false;
  }

  @Override
  public String toString() {
    return "AuthenticationContext{" +
      "serverUri=" + serverUri +
      ", strategyType=" + strategy.getAuthType() +
      ", authenticated=" + isAuthenticated() +
      '}';
  }
}
