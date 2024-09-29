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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.pentaho.di.core.auth.core.AuthenticationConsumer;
import org.pentaho.di.core.auth.core.AuthenticationConsumerFactory;
import org.pentaho.di.core.auth.core.AuthenticationFactoryException;
import org.pentaho.di.i18n.BaseMessages;

public class DefaultAuthenticationConsumerFactory implements AuthenticationConsumerFactory<Object, Object, Object> {
  private static final Class<?> PKG = DefaultAuthenticationConsumerFactory.class;
  private final Constructor<?> constructor;
  private final Class<Object> consumedType;
  private final Class<Object> returnType;
  private final Class<Object> createArgType;

  @SuppressWarnings( "unchecked" )
  public DefaultAuthenticationConsumerFactory( Class<?> consumerClass ) throws AuthenticationFactoryException {
    Constructor<?>[] constructors = consumerClass.getConstructors();
    if ( constructors.length != 1 ) {
      throw new AuthenticationFactoryException( BaseMessages.getString( PKG,
          "DefaultAuthenticationConsumerFactory.Constructor", getClass().getName(),
          consumerClass.getCanonicalName() ) );
    }

    constructor = constructors[0];
    Class<?>[] parameterTypes = constructor.getParameterTypes();
    if ( parameterTypes.length != 1 ) {
      throw new AuthenticationFactoryException( BaseMessages.getString( PKG,
          "DefaultAuthenticationConsumerFactory.Constructor.Arg", getClass().getName(), consumerClass
              .getCanonicalName() ) );
    }

    Method consumeMethod = null;
    Class<?> consumedType = Object.class;
    for ( Method method : consumerClass.getMethods() ) {
      if ( "consume".equals( method.getName() ) ) {
        Class<?>[] methodParameterTypes = method.getParameterTypes();
        if ( methodParameterTypes.length == 1 && consumedType.isAssignableFrom( methodParameterTypes[0] ) ) {
          consumeMethod = method;
          consumedType = methodParameterTypes[0];
        }
      }
    }

    if ( consumeMethod == null ) {
      throw new AuthenticationFactoryException( BaseMessages.getString( PKG,
          "DefaultAuthenticationConsumerFactory.Consume", consumerClass.getCanonicalName() ) );
    }
    this.consumedType = (Class<Object>) consumeMethod.getParameterTypes()[0];
    this.returnType = (Class<Object>) consumeMethod.getReturnType();
    this.createArgType = (Class<Object>) parameterTypes[0];
  }

  @Override
  public Class<Object> getConsumedType() {
    return consumedType;
  }

  @Override
  public Class<Object> getReturnType() {
    return returnType;
  }

  @Override
  public Class<Object> getCreateArgType() {
    return createArgType;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public AuthenticationConsumer<Object, Object> create( Object createArg ) {
    try {
      return (AuthenticationConsumer<Object, Object>) constructor.newInstance( createArg );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }
}
