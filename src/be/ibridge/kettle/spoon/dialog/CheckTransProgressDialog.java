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

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.trans.TransMeta;


/**
 * Takes care of displaying a dialog that will handle the wait while checking a transformation...
 * 
 * @author Matt
 * @since  16-mrt-2005
 */
public class CheckTransProgressDialog
{
	private Props props;
	private Shell shell;
	private TransMeta transMeta;
	private ArrayList remarks;
	private boolean onlySelected;
	/**
	 * Creates a new dialog that will handle the wait while checking a transformation...
	 */
	public CheckTransProgressDialog(LogWriter log, Props props, Shell shell, TransMeta transMeta, ArrayList remarks, boolean onlySelected)
	{
		this.props = props;
		this.shell = shell;
		this.transMeta = transMeta;
		this.onlySelected = onlySelected;
		this.remarks = remarks;
	}
	
	public void open()
	{
        final ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
        
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
				try
				{
					transMeta.checkSteps(remarks, onlySelected, monitor);
				}
				catch(Exception e)
				{
					throw new InvocationTargetException(e, "Problem encountered checking transformation: "+e.toString());
				}
			}
		};
		
		try
		{
            // Run something in the background to cancel active database queries, forecably if needed!
            Runnable run = new Runnable()
            {
                public void run()
                {
                    IProgressMonitor monitor = pmd.getProgressMonitor();
                    while (pmd.getShell()==null || ( !pmd.getShell().isDisposed() && !monitor.isCanceled() ))
                    {
                        try { Thread.sleep(250); } catch(InterruptedException e) { };
                    }
                    
                    if (monitor.isCanceled()) // Disconnect and see what happens!
                    {
                        try { transMeta.cancelQueries(); } catch(Exception e) {};
                    }
                }
            };
            // Dump the cancel looker in the background!
            new Thread(run).start();

			pmd.run(true, true, op);
		}
		catch (InvocationTargetException e)
		{
			new ErrorDialog(shell, props, "Error checking transformation", "An error occured checking this transformation!", e);
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, props, "Error checking transformation", "An error occured checking this transformation!", e);
		}
	}
}
