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
