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

import java.net.URI;
import java.util.Map;

/**
 * Strategy interface for different authentication mechanisms.
 * Implementations can provide session-based, OAuth, SAML, or other authentication methods.
 * <p>
 * This follows the Strategy design pattern, allowing authentication behavior
 * to be selected at runtime without modifying client code.
 */
public interface AuthenticationStrategy {

  /**
   * Get the authentication type identifier
   *
   * @return Authentication type
   */
  String getAuthType();

  /**
   * Store authentication credentials/tokens for a server
   *
   * @param serverUri   The server URI
   * @param credentials Map containing authentication data (tokens, sessions, etc.)
   */
  void storeCredentials( URI serverUri, Map<String, Object> credentials );

  /**
   * Retrieve authentication credentials/tokens for a server
   *
   * @param serverUri The server URI
   * @return Map containing authentication data, or null if not found
   */
  Map<String, Object> getCredentials( URI serverUri );

  /**
   * Check if valid authentication exists for a server
   *
   * @param serverUri The server URI
   * @return True if authenticated, false otherwise
   */
  boolean isAuthenticated( URI serverUri );

  /**
   * Remove authentication credentials for a server
   *
   * @param serverUri The server URI
   */
  void clearCredentials( URI serverUri );

  /**
   * Clear all stored authentication credentials
   */
  void clearAll();

  /**
   * Get a specific authentication token/value for a server
   *
   * @param serverUri The server URI
   * @param key       The credential key (e.g., "jsessionid", "access_token")
   * @return The credential value, or null if not found
   */
  String getCredentialValue( URI serverUri, String key );

  /**
   * Store a specific authentication token/value for a server
   *
   * @param serverUri The server URI
   * @param key       The credential key
   * @param value     The credential value
   */
  void storeCredentialValue( URI serverUri, String key, String value );

  /**
   * Remove a specific authentication token/value for a server.
   * This allows selective cleanup while preserving other credentials/metadata.
   *
   * @param serverUri The server URI
   * @param key       The credential key to remove
   */
  void clearCredentialValue( URI serverUri, String key );
}
