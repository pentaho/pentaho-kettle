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
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.trans.TransMeta;


/**
 * Takes care of displaying a dialog that will handle the wait while getting the SQL for a transformation...
 * 
 * @author Matt
 * @since  15-mrt-2005
 */
public class GetSQLProgressDialog
{
	private Props props;
	private Shell shell;
	private TransMeta transMeta;
	private ArrayList stats;
	
	/**
	 * Creates a new dialog that will handle the wait while getting the SQL for a transformation...
	 */
	public GetSQLProgressDialog(LogWriter log, Props props, Shell shell, TransMeta transMeta)
	{
		this.props = props;
		this.shell = shell;
		this.transMeta = transMeta;
	}
	
	public ArrayList open()
	{
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
				try
				{
					stats = transMeta.getSQLStatements(monitor);
				}
				catch(KettleException e)
				{
					throw new InvocationTargetException(e, "Error generating SQL for transformation: "+Const.CR+e.getMessage());
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
			new ErrorDialog(shell, props, "Error generating SQL for transformation", "An error occured generating the SQL for this transformation!", e);
			stats = null;
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, props, "Error generating SQL for transformation", "An error occured generating the SQL for this transformation!", e);
			stats = null;
		}

		return stats;
	}
}
