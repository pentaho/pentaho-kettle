/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.repository.repositoryexplorer.controllers;

import java.util.List;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.KettleRepositoryLostException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.repository.dialog.RepositoryExplorerDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorerCallback;
import org.pentaho.di.ui.spoon.SharedObjectSyncUtil;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.swt.SwtBindingFactory;
import org.pentaho.ui.xul.swt.tags.SwtDialog;
import org.pentaho.ui.xul.util.DialogController;

/**
 *
 * This is the main XulEventHandler for the dialog. It sets up the main bindings for the user interface and responds to
 * some of the main UI events such as closing and accepting the dialog.
 *
 */
public class MainController extends AbstractXulEventHandler implements DialogController<Object> {

  private static Class<?> PKG = RepositoryExplorerDialog.class; // for i18n purposes, needed by Translator2!!

  private RepositoryExplorerCallback callback;

  public static final int CANCELLED = 0;
  public static final int OK = 1;

  private int lastClicked = CANCELLED;

  // private XulButton acceptButton;

  private XulDialog dialog;
  private List<DialogListener<Object>> listeners = new ArrayList<DialogListener<Object>>();

  private Shell shell;

  private Repository repository = null;

  BindingFactory bf;

  private boolean aborting = false;

  private SharedObjectSyncUtil sharedObjectSyncUtil;

  public MainController() {
  }

  public boolean getOkClicked() {
    return lastClicked == OK;
  }

  public void init() {
    bf = new SwtBindingFactory();
    bf.setDocument( this.getXulDomContainer().getDocumentRoot() );
    createBindings();

    if ( dialog != null && repository != null ) {
      dialog.setTitle( BaseMessages.getString( PKG, "RepositoryExplorerDialog.DevTitle", repository.getName() ) );
    }
  }

  public void showDialog() {
    dialog.show();
  }

  private void createBindings() {

    dialog = (XulDialog) document.getElementById( "repository-explorer-dialog" );
    shell = ( (SwtDialog) document.getElementById( "repository-explorer-dialog" ) ).getShell();
    // acceptButton = (XulButton) document.getElementById("repository-explorer-dialog_accept");
  }

  public RepositoryExplorerCallback getCallback() {
    return callback;
  }

  public void setCallback( RepositoryExplorerCallback callback ) {
    this.callback = callback;
  }

  public void setRepository( Repository rep ) {
    this.repository = rep;
  }

  public String getName() {
    return "mainController";
  }

  @Bindable
  public void closeDialog() {
    lastClicked = CANCELLED;
    this.dialog.hide();
    Spoon.getInstance().refreshTree();

    // listeners may remove themselves, old-style iteration
    for ( int i = 0; i < listeners.size(); i++ ) {
      listeners.get( i ).onDialogCancel();
    }
  }

  public void addDialogListener( DialogListener<Object> listener ) {
    if ( listeners.contains( listener ) == false ) {
      listeners.add( listener );
    }
  }

  public void removeDialogListener( DialogListener<Object> listener ) {
    if ( listeners.contains( listener ) ) {
      listeners.remove( listener );
    }
  }

  public void hideDialog() {
    closeDialog();

  }

  private synchronized boolean isAborting() {
    if ( !aborting ) {
      aborting = true;
      return false;
    } else {
      return true;
    }
  }

  public boolean handleLostRepository( Throwable e ) {
    KettleRepositoryLostException repLost = KettleRepositoryLostException.lookupStackStrace( e );
    try {
      if ( repLost != null ) {
        if ( !isAborting() ) {
          new ErrorDialog(
                shell,
                BaseMessages.getString( PKG, "RepositoryExplorer.Dialog.Error.Title" ),
                repLost.getPrefaceMessage(),
                repLost );
          if ( callback != null && callback.error( null ) ) {
            closeDialog();
          }
        }

        return true;
      }
    } catch ( Exception ex ) {
      return true;
    }

    return false;
  }

  public SharedObjectSyncUtil getSharedObjectSyncUtil() {
    return sharedObjectSyncUtil;
  }

  public void setSharedObjectSyncUtil( SharedObjectSyncUtil sharedObjectSyncUtil ) {
    this.sharedObjectSyncUtil = sharedObjectSyncUtil;
  }

}
