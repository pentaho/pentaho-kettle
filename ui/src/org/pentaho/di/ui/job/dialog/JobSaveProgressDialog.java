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
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.dialog.ErrorDialog;


/**
 * Takes care of displaying a dialog that will handle the wait while saving a job...
 * 
 * @author Matt
 * @since  13-mrt-2005
 */
public class JobSaveProgressDialog
{
	private Shell shell;
	private Repository rep;
	private JobMeta jobMeta;
	private String	versionComment;
    
	/**
	 * Creates a new dialog that will handle the wait while saving a job...
	 */
	public JobSaveProgressDialog(Shell shell, Repository rep, JobMeta jobInfo, String versionComment)
	{
		this.shell = shell;
		this.rep = rep;
		this.jobMeta = jobInfo;
		this.versionComment = versionComment;
   	}
	
	public boolean open()
	{
		boolean retval=true;
		
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
				try
				{
					rep.save(jobMeta, versionComment, new ProgressMonitorAdapter(monitor));
				}
				catch(KettleException e)
				{
					throw new InvocationTargetException(e, "Error saving job");
				}
			}
		};
		
		try
		{
			ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
			pmd.run(true, true, op);
		}
		catch (InvocationTargetException e)
		{
			new ErrorDialog(shell, "Error saving job", "An error occured saving the job!", e);
			retval=false;
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, "Error saving job", "An error occured saving the job!", e);
			retval=false;
		}

		return retval;
	}
}