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


package org.pentaho.di.core.auth.core.impl;

import org.pentaho.di.core.auth.core.AuthenticationConsumer;
import org.pentaho.di.core.auth.core.AuthenticationConsumerFactory;
import org.pentaho.di.core.auth.core.AuthenticationConsumptionException;
import org.pentaho.di.core.auth.core.AuthenticationPerformer;
import org.pentaho.di.core.auth.core.AuthenticationProvider;

public class DefaultAuthenticationPerformer<ReturnType, CreateArgType, T extends AuthenticationProvider> implements
    AuthenticationPerformer<ReturnType, CreateArgType> {
  private final T provider;
  private final AuthenticationConsumerFactory<ReturnType, CreateArgType, T> authenticationConsumerFactory;

  public DefaultAuthenticationPerformer( T provider,
      AuthenticationConsumerFactory<ReturnType, CreateArgType, T> authenticationConsumerFactory ) {
    this.provider = provider;
    this.authenticationConsumerFactory = authenticationConsumerFactory;
  }

  @Override
  public ReturnType perform( CreateArgType consumerCreateArg ) throws AuthenticationConsumptionException {
    AuthenticationConsumer<ReturnType, T> consumer = authenticationConsumerFactory.create( consumerCreateArg );
    return consumer.consume( provider );
  }

  @Override
  public String getDisplayName() {
    return provider.getDisplayName();
  }

  @Override
  public AuthenticationProvider getAuthenticationProvider() {
    return provider;
  }
}
