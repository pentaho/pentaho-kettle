/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.repo.timeout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.di.ui.repo.dialog.RepositoryDialog;
import org.pentaho.di.ui.spoon.Spoon;

public class SessionTimeoutHandler {

  private static Class<?> PKG = SessionTimeoutHandler.class;

  private static final int STACK_ELEMENTS_TO_SKIP = 3;

  private static final String EXCEPTION_CLASS_NAME = "ClientTransportException";

  private final RepositoryConnectController repositoryConnectController;

  private final AtomicBoolean needToLogin = new AtomicBoolean( false );

  private final AtomicBoolean reinvoke = new AtomicBoolean( false );

  public SessionTimeoutHandler( RepositoryConnectController repositoryConnectController ) {
    this.repositoryConnectController = repositoryConnectController;
  }

  public Object handle( Object objectToHandle, Throwable exception, Method method, Object[] args ) throws Throwable {
    if ( lookupForConnectTimeoutError( exception ) && !calledFromThisHandler() ) {
      try {
        return method.invoke( objectToHandle, args );
      } catch ( InvocationTargetException ex2 ) {
        if ( !lookupForConnectTimeoutError( ex2 ) ) {
          throw ex2.getCause();
        }
      }
      needToLogin.set( true );
      synchronized ( this ) {
        if ( needToLogin.get() ) {
          boolean result = showLoginScreen( repositoryConnectController );
          needToLogin.set( false );
          if ( result ) {
            reinvoke.set( true );
            return method.invoke( objectToHandle, args );
          }
          reinvoke.set( false );
        }
      }
      if ( reinvoke.get() ) {
        return method.invoke( objectToHandle, args );
      }
    }
    throw exception;
  }

  boolean showLoginScreen( RepositoryConnectController repositoryConnectController ) {
    String message = BaseMessages.getString( PKG, "Repository.Reconnection.Message" );
    RepositoryDialog loginDialog = new RepositoryDialog( getSpoon().getShell(), repositoryConnectController );
    RepositoryMeta repositoryMeta = repositoryConnectController.getConnectedRepository();
    return loginDialog.openRelogin( repositoryMeta, message );
  }

  boolean lookupForConnectTimeoutError( Throwable root ) {
    while ( root != null ) {
      if ( EXCEPTION_CLASS_NAME.equals( root.getClass().getSimpleName() ) ) {
        String errorMessage = root.getMessage();
        if ( errorMessage.contains( RepositoryConnectController.ERROR_401 ) ) {
          return true;
        } else {
          return false;
        }
      } else {
        root = root.getCause();
      }
    }
    return false;
  }

  boolean calledFromThisHandler() {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    for ( int i = STACK_ELEMENTS_TO_SKIP; i < stackTrace.length; i++ ) {
      if ( stackTrace[i].getClassName().equals( SessionTimeoutHandler.class.getCanonicalName() ) ) {
        return true;
      }
    }
    return false;
  }

  private Spoon getSpoon() {
    return Spoon.getInstance();
  }

}
