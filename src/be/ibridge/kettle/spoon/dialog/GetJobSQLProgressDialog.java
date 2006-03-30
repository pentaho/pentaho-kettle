/*
 *
 *
 */

package be.ibridge.kettle.spoon.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.repository.Repository;


/**
 * Takes care of displaying a dialog that will handle the wait while getting the SQL for a job...
 * 
 * @author Matt
 * @since  29-mrt-2006
 */
public class GetJobSQLProgressDialog
{
	private Props props;
	private Shell shell;
	private JobMeta jobMeta;
	private ArrayList stats;
    private Repository repository;
	
	/**
	 * Creates a new dialog that will handle the wait while getting the SQL for a job...
	 */
	public GetJobSQLProgressDialog(Shell shell, JobMeta jobMeta, Repository repository)
	{
		this.props = Props.getInstance();
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
				try
				{
					stats = jobMeta.getSQLStatements(repository, monitor);
				}
				catch(KettleException e)
				{
					throw new InvocationTargetException(e, "Error generating SQL for job: "+Const.CR+e.getMessage());
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
			new ErrorDialog(shell, props, "Error generating SQL for job", "An error occured generating the SQL for this job!", e);
			stats = null;
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, props, "Error generating SQL for job", "An error occured generating the SQL for this job!", e);
			stats = null;
		}

		return stats;
	}
}
