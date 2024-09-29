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


package org.pentaho.di.ui.job.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.ProgressMonitorAdapter;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.dialog.ErrorDialog;

/**
 * Takes care of displaying a dialog that will handle the wait while saving a job...
 *
 * @author Matt
 * @since 13-mrt-2005
 */
public class JobSaveProgressDialog {
  private Shell shell;
  private Repository rep;
  private JobMeta jobMeta;
  private String versionComment;

  /**
   * Creates a new dialog that will handle the wait while saving a job...
   */
  public JobSaveProgressDialog( Shell shell, Repository rep, JobMeta jobInfo, String versionComment ) {
    this.shell = shell;
    this.rep = rep;
    this.jobMeta = jobInfo;
    this.versionComment = versionComment;
  }

  public boolean open() {
    boolean retval = true;

    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run( IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException {
        try {
          rep.save( jobMeta, versionComment, new ProgressMonitorAdapter( monitor ) );
        } catch ( KettleException e ) {
          throw new InvocationTargetException( e, "Error saving job" );
        }
      }
    };

    try {
      ProgressMonitorDialog pmd = new ProgressMonitorDialog( shell );
      pmd.run( true, true, op );
    } catch ( InvocationTargetException e ) {
      new ErrorDialog( shell, "Error saving job", "An error occured saving the job!", e );
      retval = false;
    } catch ( InterruptedException e ) {
      new ErrorDialog( shell, "Error saving job", "An error occured saving the job!", e );
      retval = false;
    }

    return retval;
  }
}
