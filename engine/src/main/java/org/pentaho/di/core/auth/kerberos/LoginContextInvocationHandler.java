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
