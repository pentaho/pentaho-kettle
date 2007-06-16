/*
 *
 *
 */

package org.pentaho.di.spoon.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;

import org.pentaho.di.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;




/**
 * Takes care of displaying a dialog that will handle the wait while getting the SQL for a job...
 * 
 * @author Matt
 * @since  29-mrt-2006
 */
public class GetJobSQLProgressDialog
{
	private Shell shell;
	private JobMeta jobMeta;
	private ArrayList stats;
    private Repository repository;

	
	/**
	 * Creates a new dialog that will handle the wait while getting the SQL for a job...
	 */
	public GetJobSQLProgressDialog(Shell shell, JobMeta jobMeta, Repository repository)
	{
		this.shell = shell;
		this.jobMeta = jobMeta;
        this.repository = repository;

	}
	
	public ArrayList open()
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
					stats = jobMeta.getSQLStatements(repository, monitor);
				}
				catch(KettleException e)
				{
					throw new InvocationTargetException(e, Messages.getString("GetJobSQLProgressDialog.RuntimeError.UnableToGenerateSQL.Exception", e.getMessage())); //Error generating SQL for job: \n{0}
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
			new ErrorDialog(shell, Messages.getString("GetJobSQLProgressDialog.Dialog.UnableToGenerateSQL.Title"), Messages.getString("GetJobSQLProgressDialog.Dialog.UnableToGenerateSQL.Message"), e); //"Error generating SQL for job","An error occured generating the SQL for this job\!"
			stats = null;
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, Messages.getString("GetJobSQLProgressDialog.Dialog.UnableToGenerateSQL.Title"), Messages.getString("GetJobSQLProgressDialog.Dialog.UnableToGenerateSQL.Message"), e); //"Error generating SQL for job","An error occured generating the SQL for this job\!"
			stats = null;
		}

		return stats;
	}
}
