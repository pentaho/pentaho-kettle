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


package org.pentaho.di.core.auth.core.impl;

import org.pentaho.di.core.auth.core.AuthenticationConsumerFactory;
import org.pentaho.di.core.auth.core.AuthenticationConsumerInvocationHandler;
import org.pentaho.di.core.auth.core.AuthenticationPerformer;
import org.pentaho.di.core.auth.core.AuthenticationPerformerFactory;
import org.pentaho.di.core.auth.core.AuthenticationProvider;

public class DefaultAuthenticationPerformerFactory implements AuthenticationPerformerFactory {

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public <ReturnType, CreateArgType, ConsumedType> AuthenticationPerformer<ReturnType, CreateArgType> create(
      AuthenticationProvider authenticationProvider,
      AuthenticationConsumerFactory<ReturnType, CreateArgType, ConsumedType> authenticationConsumerFactory ) {
    if ( authenticationConsumerFactory.getConsumedType().isInstance( authenticationProvider ) ) {
      return new DefaultAuthenticationPerformer( authenticationProvider, authenticationConsumerFactory );
    } else if ( AuthenticationConsumerInvocationHandler.isCompatible( authenticationConsumerFactory.getConsumedType(),
            authenticationProvider ) ) {
      return new ClassloaderBridgingAuthenticationPerformer<ReturnType, CreateArgType, ConsumedType>(
          authenticationProvider, authenticationConsumerFactory );
    }
    return null;
  }
}
