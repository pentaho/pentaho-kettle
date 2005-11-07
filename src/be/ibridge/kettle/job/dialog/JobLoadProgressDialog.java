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
import be.ibridge.kettle.repository.RepositoryDirectory;


/**
* 
* 
* @author Matt
* @since  13-mrt-2005
*/
public class JobLoadProgressDialog
{
	private LogWriter log;
	private Props props;
	private Shell shell;
	private Repository rep;
	private String jobname;
	private RepositoryDirectory repdir;
	private JobMeta jobInfo;
	
	/**
	 * Creates a new dialog that will handle the wait while loading a job...
	 */
	public JobLoadProgressDialog(LogWriter log, Props props, Shell shell, Repository rep, String jobname, RepositoryDirectory repdir)
	{
		this.log = log;
		this.props = props;
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
				try
				{
					jobInfo = new JobMeta(log, rep, jobname, repdir, monitor);
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
			new ErrorDialog(shell, props, "Error loading job", "An error occured loading the job!", e);
			jobInfo = null;
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, props, "Error loading job", "An error occured loading the job!", e);
			jobInfo = null;
		}

		return jobInfo;
	}
}
