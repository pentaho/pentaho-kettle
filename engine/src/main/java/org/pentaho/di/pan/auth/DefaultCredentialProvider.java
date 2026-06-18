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

import com.pentaho.oauth.client.BrowserAuthSessionHolder;

import java.util.Objects;
import java.util.Optional;

/**
 * Default OAuth-backed credential provider used by PUR and other engine-side
 * consumers that need to reuse browser-auth state.
 */
public class DefaultCredentialProvider implements CredentialProvider {

  private final BrowserAuthSessionHolder sessionHolder;

  public DefaultCredentialProvider() {
    this( SharedBrowserAuthSessionHolder.get() );
  }

  public DefaultCredentialProvider( BrowserAuthSessionHolder sessionHolder ) {
    this.sessionHolder = Objects.requireNonNull( sessionHolder, "sessionHolder" );
  }

  @Override
  public Optional<String> findAccessToken( String serverUrl ) {
    return Optional.ofNullable( sessionHolder.getOAuthAccessToken( serverUrl ) );
  }

  @Override
  public Optional<String> findSessionCookie( String serverUrl ) {
    return Optional.ofNullable( sessionHolder.getSessionCookie( serverUrl ) );
  }
}
