/*
 *
 *
 */

package be.ibridge.kettle.job.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.repository.Repository;


/**
 * Takes care of displaying a dialog that will handle the wait while saving a job...
 * 
 * @author Matt
 * @since  13-mrt-2005
 */
public class JobSaveProgressDialog
{
	private Props props;
	private Shell shell;
	private Repository rep;
	private JobMeta jobInfo;
	
	/**
	 * Creates a new dialog that will handle the wait while saving a job...
	 */
	public JobSaveProgressDialog(LogWriter log, Props props, Shell shell, Repository rep, JobMeta jobInfo)
	{
		this.props = props;
		this.shell = shell;
		this.rep = rep;
		this.jobInfo = jobInfo;
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
					jobInfo.saveRep(rep, monitor);
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
			pmd.run(false, false, op);
		}
		catch (InvocationTargetException e)
		{
			new ErrorDialog(shell, props, "Error saving job", "An error occured saving the job!", e);
			retval=false;
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, props, "Error saving job", "An error occured saving the job!", e);
			retval=false;
		}

		return retval;
	}
}
