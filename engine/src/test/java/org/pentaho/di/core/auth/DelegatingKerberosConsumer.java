/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core.auth;

import org.pentaho.di.core.auth.core.AuthenticationConsumer;
import org.pentaho.di.core.auth.core.AuthenticationConsumptionException;

public class DelegatingKerberosConsumer implements
    AuthenticationConsumer<Object, KerberosAuthenticationProvider> {
  private AuthenticationConsumer<Object, KerberosAuthenticationProvider> delegate;

  public DelegatingKerberosConsumer( AuthenticationConsumer<Object, KerberosAuthenticationProvider> delegate ) {
    this.delegate = delegate;
  }

  @Override
  public Object consume( KerberosAuthenticationProvider authenticationProvider ) throws AuthenticationConsumptionException {
    return delegate.consume( authenticationProvider );
  }
}
