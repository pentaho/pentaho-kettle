/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
/*
*
*
*/

package org.pentaho.di.ui.job.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
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
	private RepositoryDirectory repdir;
	private JobMeta jobInfo;

    /**
     * Creates a new dialog that will handle the wait while loading a job...
     * @deprecated please use the constructor version without log or props
     */
    public JobLoadProgressDialog(LogWriter log, Props props, Shell shell, Repository rep, String jobname, RepositoryDirectory repdir)
    {
        this(shell, rep, jobname, repdir);
    }
    
	/**
	 * Creates a new dialog that will handle the wait while loading a job...
	 */
	public JobLoadProgressDialog(Shell shell, Repository rep, String jobname, RepositoryDirectory repdir)
	{
		this.shell = shell;
		this.rep = rep;
		this.jobname = jobname;
		this.repdir = repdir;
		
		this.jobInfo = null;
	}
	
	public JobMeta open()
	{
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
                // This is running in a new process: copy some KettleVariables info
                // LocalVariables.getInstance().createKettleVariables(Thread.currentThread(), kettleVariables.getLocalThread(), true);
                // --> don't set variables if not running in different thread --> pmd.run(true,true, op);

				try
				{
					jobInfo = new JobMeta(LogWriter.getInstance(), rep, jobname, repdir, monitor);
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
