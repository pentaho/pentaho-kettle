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


package org.pentaho.di.core.auth;

import org.pentaho.di.core.auth.core.AuthenticationConsumer;
import org.pentaho.di.core.auth.core.AuthenticationConsumptionException;

public class DelegatingNoAuthConsumer implements AuthenticationConsumer<Object, NoAuthenticationAuthenticationProvider> {
  private AuthenticationConsumer<Object, NoAuthenticationAuthenticationProvider> delegate;

  public DelegatingNoAuthConsumer( AuthenticationConsumer<Object, NoAuthenticationAuthenticationProvider> delegate ) {
    this.delegate = delegate;
  }

  @Override
  public Object consume( NoAuthenticationAuthenticationProvider authenticationProvider ) throws AuthenticationConsumptionException {
    return delegate.consume( authenticationProvider );
  }
}
