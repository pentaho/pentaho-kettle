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

package org.pentaho.di.ui.repository.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ProgressMonitorAdapter;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.imp.ImportRules;
import org.pentaho.di.repository.ExportFeedback;
import org.pentaho.di.repository.IRepositoryExporter;
import org.pentaho.di.repository.IRepositoryExporterFeedback;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;

/**
 * Takes care of displaying a dialog that will handle the wait while we are exporting the complete repository to XML...
 * 
 * @author Matt
 * @since 02-jun-2005
 */
public class RepositoryExportProgressDialog {
  private static Class<?> PKG = RepositoryDialogInterface.class; // for i18n purposes, needed by Translator2!!

  private Shell shell;
  private Repository rep;
  private RepositoryDirectoryInterface dir;
  private String filename;
  private ImportRules importRules;

  private LogChannelInterface log;

  public RepositoryExportProgressDialog( Shell shell, Repository rep, RepositoryDirectoryInterface dir,
      String filename ) {
    this( shell, rep, dir, filename, new ImportRules() );
  }

  public RepositoryExportProgressDialog( Shell shell, Repository rep, RepositoryDirectoryInterface dir,
      String filename, ImportRules importRules ) {
    this.shell = shell;
    this.rep = rep;
    this.dir = dir;
    this.filename = filename;
    this.importRules = importRules;
    this.log = rep.getLog();
  }

  public boolean open() {
    boolean retval = true;

    final List<ExportFeedback> list = new ArrayList<ExportFeedback>();
    IRepositoryExporter tmpExporter = null;
    try {
      tmpExporter = rep.getExporter();
    } catch ( KettleException e ) {
      log.logError( RepositoryExportProgressDialog.class.toString(), "Error creating repository: " + e.toString() );
      log.logError( Const.getStackTracker( e ) );
      new ErrorDialog( shell, BaseMessages.getString( PKG, "RepositoryExportDialog.ErrorExport.Title" ), BaseMessages
          .getString( PKG, "RepositoryExportDialog.ErrorExport.Message" ), e );
      return false;
    }
    final IRepositoryExporter exporter = tmpExporter;
    // this hack is only to support dog-nail build process for <...>
    // and keep base interfaces without changes - getExporter should 
    // directly return IRepositoryExporterFeedback.
    final boolean isFeedback = ( exporter instanceof IRepositoryExporterFeedback ) ? true : false;
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run( IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException {
        try {
          exporter.setImportRulesToValidate( importRules );
          ProgressMonitorAdapter pMonitor = new ProgressMonitorAdapter( monitor );
          if ( isFeedback ) {
            IRepositoryExporterFeedback fExporter = IRepositoryExporterFeedback.class.cast( exporter );
            List<ExportFeedback> ret =
                fExporter.exportAllObjectsWithFeedback( pMonitor, filename, dir, "all" );
            list.addAll( ret );
          } else {
            exporter.exportAllObjects( pMonitor, filename, dir, "all" );
          }
        } catch ( KettleException e ) {
          throw new InvocationTargetException( e, BaseMessages.getString( PKG,
              "RepositoryExportDialog.Error.CreateUpdate", Const.getStackTracker( e ) ) );
        }
      }
    };

    try {
      ProgressMonitorDialog pmd = new ProgressMonitorDialog( shell );
      pmd.run( true, true, op );

      if ( !pmd.getProgressMonitor().isCanceled() && isFeedback ) {
        // show some results here.
        IRepositoryExporterFeedback fExporter = IRepositoryExporterFeedback.class.cast( exporter );
        showExportResultStatus( list, fExporter.isRulesViolation() );
      }
    } catch ( InvocationTargetException e ) {
      log.logError( RepositoryExportProgressDialog.class.toString(), "Error creating repository: " + e.toString() );
      log.logError( Const.getStackTracker( e ) );
      new ErrorDialog( shell, BaseMessages.getString( PKG, "RepositoryExportDialog.ErrorExport.Title" ), BaseMessages
          .getString( PKG, "RepositoryExportDialog.ErrorExport.Message" ), e );
      retval = false;
    } catch ( InterruptedException e ) {
      log.logError( RepositoryExportProgressDialog.class.toString(), "Error creating repository: " + e.toString() );
      log.logError( Const.getStackTracker( e ) );
      new ErrorDialog( shell, BaseMessages.getString( PKG, "RepositoryExportDialog.ErrorExport.Title" ), BaseMessages
          .getString( PKG, "RepositoryExportDialog.ErrorExport.Message" ), e );
      retval = false;
    }

    return retval;
  }

  void showExportResultStatus( List<ExportFeedback> list, boolean fail ) {
    String desc =
        fail ? BaseMessages.getString( PKG, "RepositoryExportProgressDialog.ExportResultDialog.Fail" ) : BaseMessages
            .getString( PKG, "RepositoryExportProgressDialog.ExportResultDialog.Succes" );
    EnterTextDialog dialog =
        new EnterTextDialog( shell, BaseMessages.getString( PKG,
            "RepositoryExportProgressDialog.ExportResultDialog.Title" ), desc, getExportResultDetails( list, fail ) );
    dialog.setReadOnly();
    dialog.setModal();

    dialog.open();
  }

  /**
   * 
   * @param list
   * @param fail
   * @return
   */
  private String getExportResultDetails( List<ExportFeedback> list, boolean fail ) {
    StringBuilder sb = new StringBuilder();

    for ( ExportFeedback feedback : list ) {
      if ( fail && ( feedback.getResult() == null || feedback.getResult().isEmpty() ) ) {
        if ( feedback.isSimpleString() ) {
          sb.append( feedback.toString() );
        }
        // we do write only not success results in this case.
        continue;
      }
      sb.append( feedback.toString() );
    }
    return sb.toString();
  }

  /**
   * Check if file is empty, writable, and return message dialogue box if file not empty, null otherwise.
   * 
   * @param shell
   * @param log
   * @param filename
   * @return
   */
  public static MessageBox checkIsFileIsAcceptable( Shell shell, LogChannelInterface log, String filename ) {
    MessageBox box = null;

    // check if file is exists
    try {
      // check if file is not empty
      FileObject output = KettleVFS.getFileObject( filename );
      if ( output.exists() ) {
        if ( !output.isWriteable() ) {
          box = new MessageBox( shell, SWT.ICON_QUESTION | SWT.APPLICATION_MODAL | SWT.SHEET | SWT.OK | SWT.CANCEL );
          box.setText( BaseMessages.getString( PKG, "RepositoryExportProgressDialog.ExportFileDialog.AreadyExists" ) );
          box.setMessage( BaseMessages.getString( PKG,
              "RepositoryExportProgressDialog.ExportFileDialog.NoWritePermissions" ) );
          return box;
        }

        box = new MessageBox( shell, SWT.ICON_QUESTION | SWT.APPLICATION_MODAL | SWT.SHEET | SWT.OK | SWT.CANCEL );
        box.setText( BaseMessages.getString( PKG, "RepositoryExportProgressDialog.ExportFileDialog.AreadyExists" ) );
        box.setMessage( BaseMessages.getString( PKG, "RepositoryExportProgressDialog.ExportFileDialog.Overwrite" ) );
      }
      // in case of exception - anyway we will not be able to write into this file.
    } catch ( KettleFileException e ) {
      log.logError( "Can't access file: " + filename );
    } catch ( FileSystemException e ) {
      log.logError( "Can't check if file exists/file permissions: " + filename );
    }
    return box;
  }
}
