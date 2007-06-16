/*
 *
 *
 */

package org.pentaho.di.core.database.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.DatabaseMetaInformation;

import org.pentaho.di.core.variables.LocalVariables;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.dialog.ErrorDialog;


/**
 * Takes care of displaying a dialog that will handle the wait while 
 * we're finding out what tables, views etc we can reach in the database.
 * 
 * @author Matt
 * @since  07-apr-2005
 */
public class GetDatabaseInfoProgressDialog
{
	private Shell shell;
	private DatabaseMeta dbInfo;
    private Thread parentThread;

    /**
     * @deprecated Use the constructor version without <i>log</i> and <i>props</i> parameter
     */
    public GetDatabaseInfoProgressDialog(LogWriter log, Props props, Shell shell, DatabaseMeta dbInfo)
    {
        this(shell, dbInfo);
    }

	/**
	 * Creates a new dialog that will handle the wait while we're 
	 * finding out what tables, views etc we can reach in the database.
	 */
	public GetDatabaseInfoProgressDialog(Shell shell, DatabaseMeta dbInfo)
	{
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
                    LocalVariables.getInstance().createKettleVariables(Thread.currentThread().getName(), parentThread.getName(), true);

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
		    showErrorDialog(e);
			return null;
		}
		catch (InterruptedException e)
		{
		    showErrorDialog(e);
			return null;
		}
		
		return dmi;
	}

    /**
     * Showing an error dialog
     * 
     * @param e
    */
    private void showErrorDialog(Exception e)
    {
        new ErrorDialog(shell, Messages.getString("GetDatabaseInfoProgressDialog.Error.Title"),
            Messages.getString("GetDatabaseInfoProgressDialog.Error.Message"), e);
    }
}
