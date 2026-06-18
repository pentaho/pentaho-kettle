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

package org.pentaho.di.pan.auth;

import java.util.Optional;

/**
 * Abstraction over credential retrieval for PUR transport consumers
 * ({@code org.pentaho.di.repository.pur.WebServiceManager},
 * {@code org.pentaho.di.repository.pur.RestAuthHelper},
 * {@code org.pentaho.di.repository.pur.UserRoleDelegate}).
 * <p>
 * Callers accept a {@code CredentialProvider} via constructor injection instead
 * of reaching for a shared credential singleton directly.
 * Runtime owners may publish an OAuth-backed implementation, while the default
 * engine fallback intentionally returns no credentials.
 * <p>
 * Phase J (Section 9, Phase 3 — explicit credential passing).
 */
public interface CredentialProvider {

  /**
   * Returns the current OAuth access token for the given server URL, or empty
   * if no valid (non-expired) token exists.
   *
   * @param serverUrl the Pentaho server base URL
   * @return the access token, or empty
   */
  Optional<String> findAccessToken( String serverUrl );

  /**
   * Returns the current session cookie for the given server URL, or empty if no
   * valid session exists.
   *
   * @param serverUrl the Pentaho server base URL
   * @return the session cookie string, or empty
   */
  Optional<String> findSessionCookie( String serverUrl );
}
