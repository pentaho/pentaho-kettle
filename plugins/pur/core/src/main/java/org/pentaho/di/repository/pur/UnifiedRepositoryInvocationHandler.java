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

package org.pentaho.di.repository.pur;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ConnectException;

import com.sun.xml.ws.client.ClientTransportException;
import org.pentaho.di.repository.KettleAuthenticationException;
import org.pentaho.di.repository.KettleRepositoryLostException;

class UnifiedRepositoryInvocationHandler<T> implements InvocationHandler {
  private T rep;

  // private Repository owner;

  UnifiedRepositoryInvocationHandler( T rep ) {
    this.rep = rep;
  }

  @Override
  public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
    try {
      return method.invoke( rep, args );
    } catch ( InvocationTargetException ex ) {
      if ( lookupConnectException( ex ) ) {
        throw new KettleRepositoryLostException( ex.getCause() );
      }

      if ( lookupAuthenticationException( ex ) ) {
        throw new KettleAuthenticationException( ex.getCause() );
      }

      throw ex.getCause();
    }
  }

  private boolean lookupConnectException( Throwable root ) {
    while ( root != null ) {
      if ( root instanceof ConnectException ) {
        return true;
      } else {
        root = root.getCause();
      }
    }

    return false;
  }

  private boolean lookupAuthenticationException( Throwable root ) {
    while ( root != null ) {
      if ( root instanceof ClientTransportException ) {
        return true;
      } else {
        root = root.getCause();
      }
    }

    return false;
  }

  @SuppressWarnings( "unchecked" )
  public static <T> T forObject( T o, Class<T> clazz ) {
    return (T) Proxy.newProxyInstance( o.getClass().getClassLoader(), new Class<?>[] { clazz },
        new UnifiedRepositoryInvocationHandler<T>( o ) );
  }

}
