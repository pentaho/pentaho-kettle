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

import org.pentaho.di.cli.auth.CredentialProvider;

import java.util.Optional;

/**
 * PUR-local provider used when the caller explicitly chose username/password
 * authentication.
 * <p>
 * Returning empty values forces transport callers to fall back to basic auth
 * instead of accidentally preferring stale browser OAuth tokens or session
 * cookies left behind by a previous CLI/browser auth attempt.
 * </p>
 * <p>
 * This class is intentionally implemented as a singleton because it is a
 * stateless policy object, not a credential store. Every instance would behave
 * identically, so allocating one per connect call adds churn without adding
 * isolation or correctness. Reusing one shared instance also makes the
 * explicit-basic-auth path easy to identify in tests and debugging because the
 * selected provider represents a fixed strategy: "never supply bearer or
 * session credentials for this flow".
 * </p>
 */
@SuppressWarnings( "java:S6548" ) // singleton
final class ExplicitBasicAuthCredentialProvider implements CredentialProvider {

  private static final ExplicitBasicAuthCredentialProvider INSTANCE =
    new ExplicitBasicAuthCredentialProvider();

  private ExplicitBasicAuthCredentialProvider() {
  }

  /**
   * Returns the shared stateless instance.
   * <p>
   * Singleton scope is appropriate here because the provider carries no
   * mutable connection state and only answers with empty optionals. If this
   * type ever needs per-connection data, it should stop being a singleton.
   * </p>
   */
  static ExplicitBasicAuthCredentialProvider getInstance() {
    return INSTANCE;
  }

  @Override
  public Optional<String> findAccessToken( String serverUrl ) {
    return Optional.empty();
  }

  @Override
  public Optional<String> findSessionCookie( String serverUrl ) {
    return Optional.empty();
  }
}
