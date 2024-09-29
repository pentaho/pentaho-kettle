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


package org.pentaho.di.ui.core.database.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.ProgressMonitorAdapter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.DatabaseMetaInformation;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.Spoon;

/**
 * Takes care of displaying a dialog that will handle the wait while we're finding out what tables, views etc we can
 * reach in the database.
 *
 * @author Matt
 * @since 07-apr-2005
 */
public class GetDatabaseInfoProgressDialog {
  private static Class<?> PKG = GetDatabaseInfoProgressDialog.class; // for i18n purposes, needed by Translator2!!

  private Shell shell;
  private DatabaseMeta dbInfo;

  private List<DatabaseInfoProgressListener> listeners;

  /**
   * Creates a new dialog that will handle the wait while we're finding out what tables, views etc we can reach in the
   * database.
   */
  public GetDatabaseInfoProgressDialog( Shell shell, DatabaseMeta dbInfo ) {
    this.shell = shell;
    this.dbInfo = dbInfo;
    this.listeners = new ArrayList<>();
  }

  /**
   * Adds a Database progress listener to be notified when the operation finishes
   * @param listener the listener to be notified
   */
  public void addDatabaseProgressListener( DatabaseInfoProgressListener listener ) {
    listeners.add( listener );
  }

  /**
   * Removes a Database progress listener
   * @param listener the listener to be removed
   * @return true if the removal is successful. false otherwise.
   */
  public boolean removeDatabaseProgressListener( DatabaseInfoProgressListener listener ) {
    return listeners.remove( listener );
  }

  private void notifyDatabaseProgress( IProgressMonitor progressMonitor ) {
    listeners.forEach( listener -> listener.databaseInfoProgressFinished( progressMonitor ) );
  }

  @VisibleForTesting
  protected ProgressMonitorDialog newProgressMonitorDialog() {
    return new ProgressMonitorDialog( shell );
  }

  public DatabaseMetaInformation open() {
    final DatabaseMetaInformation dmi = new DatabaseMetaInformation( dbInfo );
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run( IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException {
        try {
          dmi.getData( Spoon.loggingObject, new ProgressMonitorAdapter( monitor ) );
        } catch ( Exception e ) {
          throw new InvocationTargetException( e, BaseMessages.getString(
            PKG, "GetDatabaseInfoProgressDialog.Error.GettingInfoTable", e.toString() ) );
        }
      }
    };

    try {
      ProgressMonitorDialog pmd = newProgressMonitorDialog();
      pmd.run( true, true, op );
      notifyDatabaseProgress( pmd.getProgressMonitor() );
    } catch ( InvocationTargetException e ) {
      showErrorDialog( e );
      return null;
    } catch ( InterruptedException e ) {
      showErrorDialog( e );
      return null;
    }

    return dmi;
  }

  /**
   * Showing an error dialog
   *
   * @param e
   */
  private void showErrorDialog( Exception e ) {
    new ErrorDialog(
      shell, BaseMessages.getString( PKG, "GetDatabaseInfoProgressDialog.Error.Title" ), BaseMessages.getString(
        PKG, "GetDatabaseInfoProgressDialog.Error.Message" ), e );
  }
}
