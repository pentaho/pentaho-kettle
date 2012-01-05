/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.job.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.ProgressMonitorAdapter;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;


/**
* 
* 
* @author Matt
* @since  13-mrt-2005
*/
public class JobLoadProgressDialog
{
	private Shell shell;
	private Repository rep;
	private String jobname;
	private RepositoryDirectoryInterface repdir;
	private JobMeta jobInfo;
	private String	versionLabel;
	private ObjectId objectId;

	/**
	 * Creates a new dialog that will handle the wait while loading a job...
	 */
	public JobLoadProgressDialog(Shell shell, Repository rep, String jobname, RepositoryDirectoryInterface repdir, String versionLabel)
	{
		this.shell = shell;
		this.rep = rep;
		this.jobname = jobname;
		this.repdir = repdir;
		this.versionLabel = versionLabel;
		
		this.jobInfo = null;
	}

	 /**
   * Creates a new dialog that will handle the wait while loading a job...
   */
  public JobLoadProgressDialog(Shell shell, Repository rep, ObjectId objectId, String versionLabel)
  {
    this.shell = shell;
    this.rep = rep;
    this.objectId = objectId;
    this.versionLabel = versionLabel;
    
    this.jobInfo = null;
  }
	
	public JobMeta open()
	{
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
				try
				{
				  if (objectId != null) {
				    jobInfo = rep.loadJob(objectId, versionLabel);
				  } else {
					  jobInfo = rep.loadJob(jobname, repdir, new ProgressMonitorAdapter(monitor), versionLabel);
				  }
				}
				catch(KettleException e)
				{
					throw new InvocationTargetException(e, "Error loading job");
				}
			}
		};
		
		try
		{
			ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
			pmd.run(false, false, op);
		}
		catch (InvocationTargetException e)
		{
			new ErrorDialog(shell, "Error loading job", "An error occured loading the job!", e);
			jobInfo = null;
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, "Error loading job", "An error occured loading the job!", e);
			jobInfo = null;
		}

		return jobInfo;
	}
}
