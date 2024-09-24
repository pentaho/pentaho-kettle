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

package org.pentaho.di.ui.repo.timeout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.di.ui.repo.dialog.RepositoryConnectionDialog;
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
    RepositoryConnectionDialog loginDialog = new RepositoryConnectionDialog( getSpoon().getShell());
    RepositoryMeta repositoryMeta = repositoryConnectController.getConnectedRepository();
    repositoryConnectController.setRelogin( true );
    return loginDialog.createDialog( repositoryMeta.getName() );
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
