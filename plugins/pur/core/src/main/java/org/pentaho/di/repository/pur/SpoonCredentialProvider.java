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
import org.pentaho.di.ui.spoon.session.AuthenticationContext;
import org.pentaho.di.ui.spoon.session.SpoonSessionManager;

import java.util.Optional;

/**
 * {@link CredentialProvider} implementation for the Spoon UI browser-auth path
 * ({@link AuthenticationContext#SESSION_AUTH_TOKEN} sentinel).
 *
 * <p>Reads the JSESSIONID from {@link SpoonSessionManager} /
 * {@link AuthenticationContext} on every request so that mid-session renewals
 * are picked up automatically.</p>
 *
 * <p>allows {@link WebServiceManager} /
 * {@link CredentialHeaderFactory} to serve the Spoon UI session-auth path
 * without requiring any changes to those classes.</p>
 */
class SpoonCredentialProvider implements CredentialProvider {

  private final String serverUrl;

  SpoonCredentialProvider( String serverUrl ) {
    this.serverUrl = serverUrl;
  }

  /**
   * Spoon UI browser-auth uses a JSESSIONID cookie, not an OAuth access token.
   *
   * @return always empty
   */
  @Override
  public Optional<String> findAccessToken( String serverUrl ) {
    return Optional.empty();
  }

  /**
   * Returns {@code "JSESSIONID=<id>"} when a valid session exists in
   * {@link SpoonSessionManager} for {@link #serverUrl}, otherwise empty.
   * Failures are swallowed so that {@link CredentialHeaderFactory} can fall
   * back to basic auth gracefully.
   */
  @Override
  public Optional<String> findSessionCookie( String serverUrl ) {
    try {
      AuthenticationContext ctx =
        SpoonSessionManager.getInstance().getAuthenticationContext( this.serverUrl );
      if ( ctx != null && ctx.isAuthenticated() ) {
        String jsessionId = ctx.getJSessionId();
        if ( jsessionId != null && !jsessionId.trim().isEmpty() ) {
          return Optional.of( "JSESSIONID=" + jsessionId );
        }
      }
    } catch ( Exception ignored ) {
      // Graceful degradation — CredentialHeaderFactory will fall back to basic auth.
    }
    return Optional.empty();
  }
}
