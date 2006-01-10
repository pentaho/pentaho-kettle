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
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.repository.Repository;


/**
 * Takes care of displaying a dialog that will handle the wait while we are exporting the complete repository to XML...
 * 
 * @author Matt
 * @since  02-jun-2005
 */
public class RepositoryExportProgressDialog
{
	private Props props;
	private Shell shell;
	private Repository rep;
	private String filename;
	
	public RepositoryExportProgressDialog(Shell shell, Repository rep, String filename)
	{
		this.props = Props.getInstance();
		this.shell = shell;
		this.rep = rep;
		this.filename = filename;
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
                    rep.exportAllObjects(monitor, filename);
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
			pmd.run(true, true, op);
		}
		catch (InvocationTargetException e)
		{
		    System.out.println("Error creating repository: "+e.toString());
		    e.printStackTrace();
			new ErrorDialog(shell, props, "Error exporting the repository", "An error occured exporting the repository to XML!", e);
			retval=false;
		}
		catch (InterruptedException e)
		{
		    System.out.println("Error creating repository: "+e.toString());
		    e.printStackTrace();
			new ErrorDialog(shell, props, "Error exporting the repository", "An error occured exporting the repository to XML!", e);
			retval=false;
		}

		return retval;
	}
}
