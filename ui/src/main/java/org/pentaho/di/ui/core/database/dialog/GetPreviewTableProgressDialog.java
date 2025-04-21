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


package org.pentaho.di.ui.core.database.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.ProgressMonitorAdapter;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.Spoon;

/**
 * Takes care of displaying a dialog that will handle the wait while we're getting rows for a certain SQL query on a
 * database.
 *
 * @author Matt
 * @since 12-may-2005
 */
public class GetPreviewTableProgressDialog {
  private static Class<?> PKG = GetPreviewTableProgressDialog.class; // for i18n purposes, needed by Translator2!!

  private Shell shell;
  private DatabaseMeta dbMeta;
  private String tableName;
  private int limit;
  private List<Object[]> rows;
  private RowMetaInterface rowMeta;

  private Database db;

  /**
   * Creates a new dialog that will handle the wait while we're doing the hard work.
   */
  public GetPreviewTableProgressDialog( Shell shell, DatabaseMeta dbInfo, String schemaName, String tableName,
    int limit ) {
    this.shell = shell;
    this.dbMeta = dbInfo;
    this.tableName = dbInfo.getQuotedSchemaTableCombination( schemaName, tableName );
    this.limit = limit;
  }

  public List<Object[]> open() {
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run( IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException {
        db = new Database( Spoon.loggingObject, dbMeta );
        try {
          db.connect();

          if ( limit > 0 ) {
            db.setQueryLimit( limit );
          }

          rows = db.getFirstRows( tableName, limit, new ProgressMonitorAdapter( monitor ) );
          rowMeta = db.getReturnRowMeta();
        } catch ( KettleException e ) {
          throw new InvocationTargetException( e, "Couldn't find any rows because of an error :" + e.toString() );
        } finally {
          db.close();
        }
      }
    };

    try {
      final ProgressMonitorDialog pmd = new ProgressMonitorDialog( shell );
      // Run something in the background to cancel active database queries, forecably if needed!
      Runnable run = new Runnable() {
        public void run() {
          IProgressMonitor monitor = pmd.getProgressMonitor();
          while ( pmd.getShell() == null || ( !pmd.getShell().isDisposed() && !monitor.isCanceled() ) ) {
            try {
              Thread.sleep( 100 );
            } catch ( InterruptedException e ) {
              // Ignore
            }
          }

          if ( monitor.isCanceled() ) { // Disconnect and see what happens!

            try {
              db.cancelQuery();
            } catch ( Exception e ) {
              // Ignore
            }
          }
        }
      };
      // Start the cancel tracker in the background!
      new Thread( run ).start();

      pmd.run( true, true, op );
    } catch ( InvocationTargetException e ) {
      showErrorDialog( e );
      return null;
    } catch ( InterruptedException e ) {
      showErrorDialog( e );
      return null;
    }

    return rows;
  }

  /**
   * Showing an error dialog
   *
   * @param e
   */
  private void showErrorDialog( Exception e ) {
    new ErrorDialog(
      shell, BaseMessages.getString( PKG, "GetPreviewTableProgressDialog.Error.Title" ), BaseMessages.getString(
        PKG, "GetPreviewTableProgressDialog.Error.Message" ), e );
  }

  /**
   * @return the rowMeta
   */
  public RowMetaInterface getRowMeta() {
    return rowMeta;
  }
}
