/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.auth.core.impl;

import java.lang.reflect.Proxy;

import org.pentaho.di.core.auth.core.AuthenticationConsumer;
import org.pentaho.di.core.auth.core.AuthenticationConsumerFactory;
import org.pentaho.di.core.auth.core.AuthenticationConsumerInvocationHandler;
import org.pentaho.di.core.auth.core.AuthenticationConsumptionException;
import org.pentaho.di.core.auth.core.AuthenticationPerformer;
import org.pentaho.di.core.auth.core.AuthenticationProvider;

public class ClassloaderBridgingAuthenticationPerformer<ReturnType, CreateArgType, ConsumedType> implements
    AuthenticationPerformer<ReturnType, CreateArgType> {
  private final AuthenticationProvider provider;
  private final AuthenticationConsumerFactory<ReturnType, CreateArgType, ConsumedType> authenticationConsumerFactory;

  public ClassloaderBridgingAuthenticationPerformer( AuthenticationProvider provider,
      AuthenticationConsumerFactory<ReturnType, CreateArgType, ConsumedType> authenticationConsumerFactory ) {
    this.provider = provider;
    this.authenticationConsumerFactory = authenticationConsumerFactory;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public ReturnType perform( CreateArgType consumerCreateArg ) throws AuthenticationConsumptionException {
    AuthenticationConsumer<ReturnType, ConsumedType> consumer =
        authenticationConsumerFactory.create( consumerCreateArg );
    ConsumedType providerProxy =
        (ConsumedType) Proxy.newProxyInstance( consumer.getClass().getClassLoader(),
            new Class[] { authenticationConsumerFactory.getConsumedType() },
            new AuthenticationConsumerInvocationHandler( provider ) );
    return consumer.consume( providerProxy );
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
