/*
 *
 *
 */

package org.pentaho.di.spoon.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.dialog.Messages;

import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.dialog.ErrorDialog;
import org.pentaho.di.core.exception.KettleException;


/**
 * Takes care of displaying a dialog that will handle the wait while saving a transformation...
 * 
 * @author Matt
 * @since  13-mrt-2005
 */
public class SaveProgressDialog
{
	private Shell shell;
	private Repository rep;
	private EngineMetaInterface meta;
	

	/**
	 * Creates a new dialog that will handle the wait while saving a transformation...
	 */
	public SaveProgressDialog(Shell shell, Repository rep, EngineMetaInterface meta)
	{
		this.shell = shell;
		this.rep = rep;
		this.meta = meta;
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
					meta.saveRep(rep, monitor);
				}
				catch(KettleException e)
				{
					throw new InvocationTargetException(e, Messages.getString("TransSaveProgressDialog.Exception.ErrorSavingTransformation")+e.toString()); //$NON-NLS-1$
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
			new ErrorDialog(shell, Messages.getString("TransSaveProgressDialog.ErrorSavingTransformation.DialogTitle"), Messages.getString("TransSaveProgressDialog.ErrorSavingTransformation.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
			retval=false;
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, Messages.getString("TransSaveProgressDialog.ErrorSavingTransformation.DialogTitle"), Messages.getString("TransSaveProgressDialog.ErrorSavingTransformation.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
			retval=false;
		}

		return retval;
	}
}
