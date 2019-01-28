/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.dialog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.debug.BreakPointListener;
import org.pentaho.di.trans.debug.StepDebugMeta;
import org.pentaho.di.trans.debug.TransDebugMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Takes care of displaying a dialog that will handle the wait while previewing a transformation...
 *
 * @author Matt
 * @since 13-jan-2006
 */
public class TransPreviewProgressDialog {
  private static Class<?> PKG = TransDialog.class; // for i18n purposes, needed by Translator2!!

  private Shell shell;
  private TransMeta transMeta;
  private String[] previewStepNames;
  private int[] previewSize;
  private Trans trans;

  private boolean cancelled;
  private String loggingText;
  private TransDebugMeta transDebugMeta;

  /**
   * Creates a new dialog that will handle the wait while previewing a transformation...
   */
  public TransPreviewProgressDialog( Shell shell, TransMeta transMeta, String[] previewStepNames, int[] previewSize ) {
    this.shell = shell;
    this.transMeta = transMeta;
    this.previewStepNames = previewStepNames;
    this.previewSize = previewSize;

    cancelled = false;
  }

  public TransMeta open() {
    return open( true );
  }

  /**
   * Opens the progress dialog
   * @param showErrorDialogs dictates whether error dialogs should be shown when errors occur - can be set to false
   *                         to let the caller control error dialogs instead.
   * @return a {@link TransMeta}
   */
  public TransMeta open( final boolean showErrorDialogs ) {
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run( IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException {
        doPreview( monitor, showErrorDialogs );
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
              trans.stopAll();
            } catch ( Exception e ) { /* Ignore */
            }
          }
        }
      };

      // Start the cancel tracker in the background!
      new Thread( run ).start();

      pmd.run( true, true, op );
    } catch ( InvocationTargetException e ) {
      if ( showErrorDialogs ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "TransPreviewProgressDialog.ErrorLoadingTransformation.DialogTitle" ),
          BaseMessages.getString( PKG, "TransPreviewProgressDialog.ErrorLoadingTransformation.DialogMessage" ), e );
      }
      transMeta = null;
    } catch ( InterruptedException e ) {
      if ( showErrorDialogs ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "TransPreviewProgressDialog.ErrorLoadingTransformation.DialogTitle" ),
          BaseMessages.getString( PKG, "TransPreviewProgressDialog.ErrorLoadingTransformation.DialogMessage" ), e );
      }
      transMeta = null;
    }

    return transMeta;
  }

  private void doPreview( final IProgressMonitor progressMonitor, final boolean showErrorDialogs  ) {
    progressMonitor.beginTask(
      BaseMessages.getString( PKG, "TransPreviewProgressDialog.Monitor.BeginTask.Title" ), 100 );

    // This transformation is ready to run in preview!
    trans = new Trans( transMeta );
    trans.setPreview( true );

    // Prepare the execution...
    //
    try {
      trans.prepareExecution( null );
    } catch ( final KettleException e ) {
      if ( showErrorDialogs ) {
        shell.getDisplay().asyncExec( new Runnable() {
          public void run() {
            new ErrorDialog( shell,
              BaseMessages.getString( PKG, "System.Dialog.Error.Title" ),
              BaseMessages.getString( PKG, "TransPreviewProgressDialog.Exception.ErrorPreparingTransformation" ), e );
          }
        } );
      }

      // It makes no sense to continue, so just stop running...
      //
      return;
    }

    // Add the preview / debugging information...
    //
    transDebugMeta = new TransDebugMeta( transMeta );
    for ( int i = 0; i < previewStepNames.length; i++ ) {
      StepMeta stepMeta = transMeta.findStep( previewStepNames[i] );
      StepDebugMeta stepDebugMeta = new StepDebugMeta( stepMeta );
      stepDebugMeta.setReadingFirstRows( true );
      stepDebugMeta.setRowCount( previewSize[i] );
      transDebugMeta.getStepDebugMetaMap().put( stepMeta, stepDebugMeta );
    }

    int previousPct = 0;
    final List<String> previewComplete = new ArrayList<String>();
    // We add a break-point that is called every time we have a step with a full preview row buffer
    // That makes it easy and fast to see if we have all the rows we need
    //
    transDebugMeta.addBreakPointListers( new BreakPointListener() {
      public void breakPointHit( TransDebugMeta transDebugMeta, StepDebugMeta stepDebugMeta,
                                 RowMetaInterface rowBufferMeta, List<Object[]> rowBuffer ) {
        String stepName = stepDebugMeta.getStepMeta().getName();
        previewComplete.add( stepName );
        progressMonitor.subTask( BaseMessages.getString(
          PKG, "TransPreviewProgressDialog.SubTask.StepPreviewFinished", stepName ) );
      }
    } );
    // set the appropriate listeners on the transformation...
    //
    transDebugMeta.addRowListenersToTransformation( trans );

    // Fire off the step threads... start running!
    //
    try {
      trans.startThreads();
    } catch ( final KettleException e ) {
      shell.getDisplay().asyncExec( new Runnable() {
        public void run() {
          new ErrorDialog( shell, BaseMessages.getString( PKG, "System.Dialog.Error.Title" ), BaseMessages
            .getString( PKG, "TransPreviewProgressDialog.Exception.ErrorPreparingTransformation" ), e );
        }
      } );

      // It makes no sense to continue, so just stop running...
      //
      return;
    }

    while ( previewComplete.size() < previewStepNames.length
      && !trans.isFinished() && !progressMonitor.isCanceled() ) {

      // How many rows are done?
      int nrDone = 0;
      int nrTotal = 0;
      for ( StepDebugMeta stepDebugMeta : transDebugMeta.getStepDebugMetaMap().values() ) {
        nrDone += stepDebugMeta.getRowBuffer().size();
        nrTotal += stepDebugMeta.getRowCount();
      }

      int pct = 100 * nrDone / nrTotal;

      int worked = pct - previousPct;

      if ( worked > 0 ) {
        progressMonitor.worked( worked );
      }
      previousPct = pct;

      // Change the percentage...
      try {
        Thread.sleep( 500 );
      } catch ( InterruptedException e ) {
        // Ignore errors
      }

      if ( progressMonitor.isCanceled() ) {
        cancelled = true;
        trans.stopAll();
      }
    }

    trans.stopAll();

    // Capture preview activity to a String:
    loggingText =
      KettleLogStore.getAppender().getBuffer( trans.getLogChannel().getLogChannelId(), true ).toString();

    progressMonitor.done();
  }

  /**
   * @param stepname
   *          the name of the step to get the preview rows for
   * @return A list of rows as the result of the preview run.
   */
  public List<Object[]> getPreviewRows( String stepname ) {
    if ( transDebugMeta == null ) {
      return null;
    }

    for ( StepMeta stepMeta : transDebugMeta.getStepDebugMetaMap().keySet() ) {
      if ( stepMeta.getName().equals( stepname ) ) {
        StepDebugMeta stepDebugMeta = transDebugMeta.getStepDebugMetaMap().get( stepMeta );
        return stepDebugMeta.getRowBuffer();
      }
    }
    return null;
  }

  /**
   * @param stepname
   *          the name of the step to get the preview rows for
   * @return A description of the row (metadata)
   */
  public RowMetaInterface getPreviewRowsMeta( String stepname ) {
    if ( transDebugMeta == null ) {
      return null;
    }

    for ( StepMeta stepMeta : transDebugMeta.getStepDebugMetaMap().keySet() ) {
      if ( stepMeta.getName().equals( stepname ) ) {
        StepDebugMeta stepDebugMeta = transDebugMeta.getStepDebugMetaMap().get( stepMeta );
        return stepDebugMeta.getRowBufferMeta();
      }
    }
    return null;
  }

  /**
   * @return true is the preview was canceled by the user
   */
  public boolean isCancelled() {
    return cancelled;
  }

  /**
   * @return The logging text from the latest preview run
   */
  public String getLoggingText() {
    return loggingText;
  }

  /**
   *
   * @return The transformation object that executed the preview TransMeta
   */
  public Trans getTrans() {
    return trans;
  }

  /**
   * @return the transDebugMeta
   */
  public TransDebugMeta getTransDebugMeta() {
    return transDebugMeta;
  }
}
