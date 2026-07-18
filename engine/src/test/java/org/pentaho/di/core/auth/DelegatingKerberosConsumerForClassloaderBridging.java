/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.core.auth;

import org.pentaho.di.core.auth.core.AuthenticationConsumer;
import org.pentaho.di.core.auth.core.AuthenticationConsumptionException;

public class DelegatingKerberosConsumerForClassloaderBridging implements
    AuthenticationConsumer<Object, KerberosAuthenticationProviderProxyInterface> {

  private AuthenticationConsumer<Object, KerberosAuthenticationProviderProxyInterface> delegate;

  public DelegatingKerberosConsumerForClassloaderBridging(
      AuthenticationConsumer<Object, KerberosAuthenticationProviderProxyInterface> delegate ) {
    this.delegate = delegate;
  }

  @Override
  public Object consume( KerberosAuthenticationProviderProxyInterface authenticationProvider ) throws AuthenticationConsumptionException {
    return delegate.consume( authenticationProvider );
  }
}
