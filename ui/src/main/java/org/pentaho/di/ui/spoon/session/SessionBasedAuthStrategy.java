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

import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session-based authentication strategy using JSESSIONID cookies.
 * This implementation stores and manages HTTP session IDs for server authentication.
 */
public class SessionBasedAuthStrategy implements AuthenticationStrategy {

  private static final LogChannelInterface log = new LogChannel( "SessionBasedAuthStrategy" );
  private static final String AUTH_TYPE = "SESSION";
  private static final String JSESSIONID_KEY = "jsessionid";
  private static final String AUTH_METHOD_MARKER_KEY = "_auth_method_marker";
  
  // Map of server key to authentication credentials
  private final Map<String, Map<String, Object>> credentialsStore = new ConcurrentHashMap<>();

  @Override
  public String getAuthType() {
    return AUTH_TYPE;
  }

  @Override
  public void storeCredentials( URI serverUri, Map<String, Object> credentials ) {
    if ( serverUri == null ) {
      throw new IllegalArgumentException( "serverUri cannot be null" );
    }
    if ( credentials == null ) {
      throw new IllegalArgumentException( "credentials cannot be null" );
    }
    String key = getServerKey( serverUri );
    credentialsStore.put( key, new ConcurrentHashMap<>( credentials ) );
    log.logBasic( "Stored session credentials for " + key );
  }

  @Override
  public Map<String, Object> getCredentials( URI serverUri ) {
    if ( serverUri == null ) {
      throw new IllegalArgumentException( "serverUri cannot be null" );
    }
    String key = getServerKey( serverUri );
    Map<String, Object> credentials = credentialsStore.get( key );
    if ( credentials != null ) {
      log.logDetailed( "Retrieved session credentials for " + key );
      return new HashMap<>( credentials );
    }
    return new HashMap<>();
  }

  @Override
  public boolean isAuthenticated( URI serverUri ) {
    if ( serverUri == null ) {
      throw new IllegalArgumentException( "serverUri cannot be null" );
    }
    String key = getServerKey( serverUri );
    Map<String, Object> credentials = credentialsStore.get( key );
    if ( credentials == null ) {
      return false;
    }
    Object jsessionIdObj = credentials.get( JSESSIONID_KEY );
    if ( jsessionIdObj == null ) {
      return false;
    }
    String jsessionId = jsessionIdObj.toString();
    return !jsessionId.trim().isEmpty();
  }

  @Override
  public void clearCredentials( URI serverUri ) {
    if ( serverUri == null ) {
      throw new IllegalArgumentException( "serverUri cannot be null" );
    }
    String key = getServerKey( serverUri );
    credentialsStore.remove( key );
    log.logBasic( "Cleared session credentials for " + key );
  }

  @Override
  public void clearAll() {
    credentialsStore.clear();
    log.logBasic( "Cleared all session credentials" );
  }

  @Override
  public String getCredentialValue( URI serverUri, String credentialKey ) {
    if ( credentialKey == null || credentialKey.trim().isEmpty() ) {
      throw new IllegalArgumentException( "credentialKey cannot be null or empty" );
    }
    Map<String, Object> credentials = getCredentials( serverUri );
    if ( credentials != null && credentials.containsKey( credentialKey ) ) {
      Object value = credentials.get( credentialKey );
      return value != null ? value.toString() : null;
    }
    return null;
  }

  @Override
  public void storeCredentialValue( URI serverUri, String credentialKey, String value ) {
    if ( serverUri == null ) {
      throw new IllegalArgumentException( "serverUri cannot be null" );
    }
    if ( credentialKey == null || credentialKey.trim().isEmpty() ) {
      throw new IllegalArgumentException( "credentialKey cannot be null or empty" );
    }
    String key = getServerKey( serverUri );
    // Use compute to atomically create/update the nested map and add the credential
    // This prevents race conditions where multiple threads could interfere
    credentialsStore.compute( key, ( k, existing ) -> {
      Map<String, Object> credentials = existing != null ? existing : new ConcurrentHashMap<>();
      credentials.put( credentialKey, value );
      return credentials;
    } );
    log.logBasic( "Stored " + credentialKey + " for " + key );
  }

  /**
   * Remove a specific credential value from storage.
   * This allows selective cleanup (e.g., removing expired JSESSIONID) while preserving other metadata.
   * Uses atomic compute operation to prevent race conditions.
   *
   * @param serverUri The server URI
   * @param credentialKey The credential key to remove
   */
  public void clearCredentialValue( URI serverUri, String credentialKey ) {
    if ( serverUri == null ) {
      throw new IllegalArgumentException( "serverUri cannot be null" );
    }
    if ( credentialKey == null || credentialKey.trim().isEmpty() ) {
      throw new IllegalArgumentException( "credentialKey cannot be null or empty" );
    }
    String key = getServerKey( serverUri );
    // Use compute to atomically remove the credential and optionally remove the map if empty
    credentialsStore.compute( key, ( k, credentials ) -> {
      if ( credentials != null ) {
        credentials.remove( credentialKey );
        log.logBasic( "Cleared credential " + credentialKey + " for " + k );
        // Return null if map is now empty (removes the entry), otherwise return the map
        return credentials.isEmpty() ? null : credentials;
      }
      return null;
    } );
  }

  /**
   * Get JSESSIONID for a server (convenience method for session-based auth)
   * 
   * @param serverUri The server URI
   * @return The JSESSIONID value, or null if not found
   */
  public String getJSessionId( URI serverUri ) {
    return getCredentialValue( serverUri, JSESSIONID_KEY );
  }

  /**
   * Store JSESSIONID for a server (convenience method for session-based auth)
   * 
   * @param serverUri The server URI
   * @param jsessionId The JSESSIONID value
   */
  public void storeJSessionId( URI serverUri, String jsessionId ) {
    storeCredentialValue( serverUri, JSESSIONID_KEY, jsessionId );
    // Mark that this server has used browser authentication
    storeCredentialValue( serverUri, AUTH_METHOD_MARKER_KEY, "browser" );
  }

  /**
   * Check if browser authentication was ever used for this server,
   * even if the session has expired.
   * 
   * @param serverUri The server URI
   * @return true if browser auth was used previously
   */
  public boolean hasBrowserAuthMarker( URI serverUri ) {
    String marker = getCredentialValue( serverUri, AUTH_METHOD_MARKER_KEY );
    return "browser".equals( marker );
  }

  /**
   * Get cache key for a server URI
   */
  private String getServerKey( URI serverUri ) {
    int port = serverUri.getPort();
    if ( port == -1 ) {
      port = "https".equals( serverUri.getScheme() ) ? 443 : 80;
    }
    return serverUri.getHost() + ":" + port;
  }
}
