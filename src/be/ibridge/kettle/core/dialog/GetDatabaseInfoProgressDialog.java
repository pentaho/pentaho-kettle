/*
 *
 *
 */

package be.ibridge.kettle.core.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.core.LocalVariables;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.database.DatabaseMetaInformation;


/**
 * Takes care of displaying a dialog that will handle the wait while 
 * we're finding out what tables, views etc we can reach in the database.
 * 
 * @author Matt
 * @since  07-apr-2005
 */
public class GetDatabaseInfoProgressDialog
{
	private Props props;
	private Shell shell;
	private DatabaseMeta dbInfo;
    private Thread parentThread;
    
	/**
	 * Creates a new dialog that will handle the wait while we're 
	 * finding out what tables, views etc we can reach in the database.
	 */
	public GetDatabaseInfoProgressDialog(LogWriter log, Props props, Shell shell, DatabaseMeta dbInfo)
	{
		this.props = props;
		this.shell = shell;
		this.dbInfo = dbInfo;
        
        this.parentThread = Thread.currentThread();
	}
	
	public DatabaseMetaInformation open()
	{
		final DatabaseMetaInformation dmi = new DatabaseMetaInformation(dbInfo);
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
				try
				{
                    // This is running in a new process: copy some KettleVariables info
                    LocalVariables.getInstance().createKettleVariables(Thread.currentThread().toString(), parentThread.toString(), true);

					dmi.getData(monitor);
				}
				catch(Exception e)
				{
					throw new InvocationTargetException(e, "Problem encountered getting information from the database: "+e.toString());
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
			new ErrorDialog(shell, props, "Error getting information", "An error occured getting information from the database!", e);
			return null;
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, props, "Error getting information", "An error occured getting information from the database!", e);
			return null;
		}
		
		return dmi;
	}
}
