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

package org.pentaho.di.core.auth.kerberos;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.apache.commons.lang.ClassUtils;

public class LoginContextInvocationHandler<T> implements InvocationHandler {
  private final T delegate;
  private final LoginContext loginContext;
  private final Set<Class<?>> interfacesToDelegate;

  public LoginContextInvocationHandler( T delegate, LoginContext loginContext ) {
    this( delegate, loginContext, new HashSet<Class<?>>() );
  }

  public LoginContextInvocationHandler( T delegate, LoginContext loginContext, Set<Class<?>> interfacesToDelegate ) {
    this.delegate = delegate;
    this.loginContext = loginContext;
    this.interfacesToDelegate = interfacesToDelegate;
  }

  @SuppressWarnings( "unchecked" )
  public static <T> T forObject( T delegate, LoginContext loginContext, Set<Class<?>> interfacesToDelegate ) {
    return (T) Proxy.newProxyInstance( delegate.getClass().getClassLoader(), ( (List<Class<?>>) ClassUtils
        .getAllInterfaces( delegate.getClass() ) ).toArray( new Class<?>[] {} ),
        new LoginContextInvocationHandler<Object>( delegate, loginContext, interfacesToDelegate ) );
  }

  @Override
  public Object invoke( Object proxy, final Method method, final Object[] args ) throws Throwable {
    try {
      return Subject.doAs( loginContext.getSubject(), new PrivilegedExceptionAction<Object>() {

        @Override
        public Object run() throws Exception {
          Object result = method.invoke( delegate, args );
          if ( result != null ) {
            for ( Class<?> iface : result.getClass().getInterfaces() ) {
              if ( interfacesToDelegate.contains( iface ) ) {
                result = forObject( result, loginContext, interfacesToDelegate );
                break;
              }
            }
          }
          return result;
        }
      } );
    } catch ( PrivilegedActionException e ) {
      if ( e.getCause() instanceof InvocationTargetException ) {
        throw ( (InvocationTargetException) e.getCause() ).getCause();
      }
      throw e;
    }
  }
}
