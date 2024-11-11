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


package org.pentaho.di.ui.repository.repositoryexplorer.controllers;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public abstract class LazilyInitializedController extends AbstractXulEventHandler {

  private static Class<?> PKG = RepositoryExplorer.class; // for i18n purposes, needed by Translator2!!

  protected Repository repository;

  protected boolean initialized;

  public void init( Repository repository ) throws ControllerInitializationException {
    this.repository = repository;
  }

  protected synchronized void lazyInit() {
    if ( !initialized ) {
      try {
        boolean succeeded = doLazyInit();
        if ( succeeded ) {
          initialized = true;
        } else {
          showErrorDialog( null );
        }
      } catch ( Exception e ) {
        e.printStackTrace();
        showErrorDialog( e );
      }
    }
  }

  private void showErrorDialog( final Exception e ) {
    XulMessageBox messageBox = null;
    try {
      messageBox = (XulMessageBox) document.createElement( "messagebox" );
    } catch ( XulException xe ) {
      throw new RuntimeException( xe );
    }
    messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) );
    messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) );
    if ( e != null ) {
      messageBox.setMessage( BaseMessages.getString(
        PKG, "LazilyInitializedController.Message.UnableToInitWithParam", e.getLocalizedMessage() ) );
    } else {
      messageBox.setMessage( BaseMessages.getString( PKG, "LazilyInitializedController.Message.UnableToInit" ) );
    }
    messageBox.open();
  }

  protected abstract boolean doLazyInit();

  protected void doWithBusyIndicator( final Runnable r ) {
    BusyIndicator.showWhile( Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault(), r );
  }

  protected void doInEventThread( final Runnable r ) {
    if ( Display.getCurrent() != null ) {
      r.run();
    } else {
      Display.getDefault().syncExec( r );
    }

  }

}
