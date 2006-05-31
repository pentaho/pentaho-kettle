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

import be.ibridge.kettle.core.LocalVariables;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.TransMeta;


/**
 * Takes care of displaying a dialog that will handle the wait while saving a transformation...
 * 
 * @author Matt
 * @since  13-mrt-2005
 */
public class TransSaveProgressDialog
{
	private Props props;
	private Shell shell;
	private Repository rep;
	private TransMeta transInfo;
    private Thread parentThread;
	
	/**
	 * Creates a new dialog that will handle the wait while saving a transformation...
	 */
	public TransSaveProgressDialog(LogWriter log, Props props, Shell shell, Repository rep, TransMeta transInfo)
	{
		this.props = props;
		this.shell = shell;
		this.rep = rep;
		this.transInfo = transInfo;
        
        this.parentThread = Thread.currentThread();
	}
	
	public boolean open()
	{
		boolean retval=true;
		
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
                // This is running in a new process: copy some KettleVariables info
                LocalVariables.getInstance().createKettleVariables(Thread.currentThread().toString(), parentThread.toString(), true);

				try
				{
					transInfo.saveRep(rep, monitor);
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
			new ErrorDialog(shell, props, Messages.getString("TransSaveProgressDialog.ErrorSavingTransformation.DialogTitle"), Messages.getString("TransSaveProgressDialog.ErrorSavingTransformation.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
			retval=false;
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, props, Messages.getString("TransSaveProgressDialog.ErrorSavingTransformation.DialogTitle"), Messages.getString("TransSaveProgressDialog.ErrorSavingTransformation.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
			retval=false;
		}

		return retval;
	}
}
