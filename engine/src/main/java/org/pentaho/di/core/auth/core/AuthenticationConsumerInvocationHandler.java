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
