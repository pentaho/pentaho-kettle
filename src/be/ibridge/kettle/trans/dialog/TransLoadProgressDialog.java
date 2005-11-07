/*
 *
 *
 */

package be.ibridge.kettle.trans.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.trans.TransMeta;


/**
 * Takes care of displaying a dialog that will handle the wait while loading a transformation...
 * 
 * @author Matt
 * @since  13-mrt-2005
 */
public class TransLoadProgressDialog
{
	private Shell shell;
	private Repository rep;
	private String transname;
	private RepositoryDirectory repdir;
	private TransMeta transInfo;
	
	/**
	 * Creates a new dialog that will handle the wait while loading a transformation...
	 */
	public TransLoadProgressDialog(Shell shell, Repository rep, String transname, RepositoryDirectory repdir)
	{
		this.shell = shell;
		this.rep = rep;
		this.transname = transname;
		this.repdir = repdir;
		
		this.transInfo = null;
	}
	
	public TransMeta open()
	{
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
				try
				{
					transInfo = new TransMeta(rep, transname, repdir, monitor);
				}
				catch(KettleException e)
				{
					throw new InvocationTargetException(e, "Error loading transformation");
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
			new ErrorDialog(shell, Props.getInstance(), "Error loading transformation", "An error occured loading the transformation!", e);
			transInfo = null;
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, Props.getInstance(), "Error loading transformation", "An error occured loading the transformation!", e);
			transInfo = null;
		}

		return transInfo;
	}
}
