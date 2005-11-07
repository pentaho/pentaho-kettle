/*
 *
 *
 */

package be.ibridge.kettle.repository.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.repository.Repository;


/**
 * Takes care of displaying a dialog that will handle the wait while creating or upgrading a transformation...
 * 
 * @author Matt
 * @since  13-mrt-2005
 */
public class UpgradeRepositoryProgressDialog
{
	private Props props;
	private Shell shell;
	private Repository rep;
	private boolean upgrade;
	
	/**
	 * Creates a new dialog that will handle the wait while upgrading or creating a repository...
	 */
	public UpgradeRepositoryProgressDialog(LogWriter log, Props props, Shell shell, Repository rep, boolean upgrade)
	{
		this.props = props;
		this.shell = shell;
		this.rep = rep;
		this.upgrade = upgrade;
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
					rep.createRepositorySchema(monitor, upgrade);
				}
				catch(KettleException e)
				{
					e.printStackTrace();
					throw new InvocationTargetException(e, "Error creating or upgrading repository:"+Const.CR+e.getMessage()+Const.CR);
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
		    System.out.println("Error creating repository: "+e.toString());
		    e.printStackTrace();
			new ErrorDialog(shell, props, "Error "+(upgrade?"upgrading":"creating")+" repository", "An error occured "+(upgrade?"upgrading":"creating")+" the repository!", e);
			retval=false;
		}
		catch (InterruptedException e)
		{
		    System.out.println("Error creating repository: "+e.toString());
		    e.printStackTrace();
			new ErrorDialog(shell, props, "Error "+(upgrade?"upgrading":"creating")+" repository", "An error occured "+(upgrade?"upgrading":"creating")+" the repository!", e);
			retval=false;
		}

		return retval;
	}
}
