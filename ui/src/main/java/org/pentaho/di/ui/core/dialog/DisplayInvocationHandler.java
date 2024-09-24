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

package org.pentaho.di.ui.core.dialog;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.lang.ClassUtils;
import org.eclipse.swt.widgets.Display;
import org.pentaho.di.core.logging.LogChannelInterface;

public class DisplayInvocationHandler<T> implements InvocationHandler {
  private final Display display;
  private final T delegate;
  private final LogChannelInterface log;
  private final boolean asyncForVoid;

  private class ResultHolder {
    private volatile Throwable throwable = null;
    private volatile Object result = null;
  }

  @SuppressWarnings( "unchecked" )
  public static <T> T forObject( Class<T> iface, T delegate, Display display, LogChannelInterface log,
      boolean asyncForVoid ) {
    return (T) Proxy.newProxyInstance( delegate.getClass().getClassLoader(), (Class<?>[]) ClassUtils.getAllInterfaces(
        delegate.getClass() ).toArray( new Class<?>[] {} ), new DisplayInvocationHandler<T>( display, delegate, log,
          asyncForVoid ) );
  }

  public DisplayInvocationHandler( Display display, T delegate, LogChannelInterface log, boolean asyncForVoid ) {
    this.display = display;
    this.delegate = delegate;
    this.log = log;
    this.asyncForVoid = asyncForVoid;
  }

  @Override
  public Object invoke( Object proxy, final Method method, final Object[] args ) throws Throwable {
    if ( display.getThread() == Thread.currentThread() ) {
      try {
        return method.invoke( delegate, args );
      } catch ( InvocationTargetException e ) {
        throw e.getCause();
      }
    }
    if ( asyncForVoid && method.getReturnType().equals( Void.TYPE ) ) {
      display.asyncExec( new Runnable() {

        @Override
        public void run() {
          try {
            method.invoke( delegate, args );
          } catch ( Throwable e ) {
            if ( e instanceof InvocationTargetException ) {
              e = e.getCause();
            }
            log.logError( e.getMessage(), e );
          }
        }
      } );
      return null;
    }
    final ResultHolder resultHolder = new ResultHolder();
    display.syncExec( new Runnable() {

      @Override
      public void run() {
        try {
          resultHolder.result = method.invoke( delegate, args );
        } catch ( InvocationTargetException e ) {
          resultHolder.throwable = e.getCause();
        } catch ( Exception e ) {
          resultHolder.throwable = e;
        }
      }
    } );
    if ( resultHolder.result != null ) {
      return resultHolder.result;
    } else {
      throw resultHolder.throwable;
    }
  }

}
