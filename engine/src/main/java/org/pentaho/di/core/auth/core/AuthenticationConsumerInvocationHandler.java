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

package org.pentaho.di.core.auth.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class AuthenticationConsumerInvocationHandler implements InvocationHandler {
  private final Object target;

  public AuthenticationConsumerInvocationHandler( Object target ) {
    this.target = target;
  }

  @Override
  public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
    return target.getClass().getMethod( method.getName(), method.getParameterTypes() ).invoke( target, args );
  }

  public static boolean isCompatible( Class<?> proxyInterface, Object targetObject ) {
    for ( Method method : proxyInterface.getMethods() ) {
      try {
        targetObject.getClass().getMethod( method.getName(), method.getParameterTypes() );
      } catch ( Exception e ) {
        return false;
      }
    }
    for ( Method method : targetObject.getClass().getMethods() ) {
      // We don't care about proxying Object methods
      if ( method.getDeclaringClass() != Object.class ) {
        try {
          proxyInterface.getMethod( method.getName(), method.getParameterTypes() );
        } catch ( Exception e ) {
          return false;
        }
      }
    }
    return true;
  }
}
