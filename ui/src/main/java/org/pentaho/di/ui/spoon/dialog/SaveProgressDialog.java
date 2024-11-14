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


package org.pentaho.di.ui.spoon.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.ProgressMonitorAdapter;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.trans.dialog.TransDialog;

/**
 * Takes care of displaying a dialog that will handle the wait while saving a transformation...
 *
 * @author Matt
 * @since 13-mrt-2005
 */
public class SaveProgressDialog {
  private static Class<?> PKG = TransDialog.class; // for i18n purposes, needed by Translator2!!

  private Shell shell;
  private Repository rep;
  private EngineMetaInterface meta;

  private String versionComment;

  /**
   * Creates a new dialog that will handle the wait while saving a transformation...
   */
  public SaveProgressDialog( Shell shell, Repository rep, EngineMetaInterface meta, String versionComment ) {
    this.shell = shell;
    this.rep = rep;
    this.meta = meta;
    this.versionComment = versionComment;
  }

  public boolean open() {
    boolean retval = true;

    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run( IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException {
        try {
          rep.save( meta, versionComment, new ProgressMonitorAdapter( monitor ) );
        } catch ( KettleException e ) {
          throw new InvocationTargetException( e, BaseMessages.getString(
            PKG, "TransSaveProgressDialog.Exception.ErrorSavingTransformation" )
            + e.toString() );
        }
      }
    };

    try {
      ProgressMonitorDialog pmd = new ProgressMonitorDialog( shell );
      pmd.run( true, true, op );
    } catch ( InvocationTargetException e ) {
      MessageDialog errorDialog =
        new MessageDialog( shell, BaseMessages.getString( PKG, "TransSaveProgressDialog.UnableToSave.DialogTitle" ), null,
          BaseMessages.getString( PKG, "TransSaveProgressDialog.UnableToSave.DialogMessage" ), MessageDialog.ERROR,
          new String[] { BaseMessages.getString( PKG, "TransSaveProgressDialog.UnableToSave.Close" ) }, 0 );
      errorDialog.open();
      retval = false;
    } catch ( InterruptedException e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "TransSaveProgressDialog.ErrorSavingTransformation.DialogTitle" ),
        BaseMessages.getString( PKG, "TransSaveProgressDialog.ErrorSavingTransformation.DialogMessage" ), e );
      retval = false;
    }

    return retval;
  }
}
