/*!
 * Copyright 2010 - 2019 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
