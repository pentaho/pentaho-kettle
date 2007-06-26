/*
 *
 *
 */

package org.pentaho.di.spoon.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.dialog.ErrorDialog;
import org.pentaho.di.core.variables.LocalVariables;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.TransMeta;




/**
 * Takes care of displaying a dialog that will handle the wait while 
 * where determining the impact of a transformation on the used databases.
 * 
 * @author Matt
 * @since  04-apr-2005
 */
public class AnalyseImpactProgressDialog
{
	private Shell shell;
	private TransMeta transMeta;
	private List<DatabaseImpact> impact;
	private boolean impactHasRun;
    private Thread parentThread;

    
	/**
	 * Creates a new dialog that will handle the wait while determining the impact of the transformation on the databases used...
	 */
	public AnalyseImpactProgressDialog(Shell shell, TransMeta transMeta, List<DatabaseImpact> impact)
	{
		this.shell = shell;
		this.transMeta = transMeta;
		this.impact = impact;
        
        this.parentThread = Thread.currentThread();
    }
	
	public boolean open()
	{
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
                // This is running in a new process: copy some KettleVariables info
                LocalVariables.getInstance().createKettleVariables(Thread.currentThread().getName(), parentThread.getName(), true);

				try
				{
					impact.clear(); // Start with a clean slate!!
					transMeta.analyseImpact(impact, monitor);
					impactHasRun = true;
				}
				catch(Exception e)
				{
					impact.clear();
					impactHasRun=false;
					throw new InvocationTargetException(e, Messages.getString("AnalyseImpactProgressDialog.RuntimeError.UnableToAnalyzeImpact.Exception", e.toString())); //Problem encountered generating impact list: {0}
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
			new ErrorDialog(shell, Messages.getString("AnalyseImpactProgressDialog.Dialog.UnableToAnalyzeImpact.Title"), Messages.getString("AnalyseImpactProgressDialog.Dialog.UnableToAnalyzeImpact.Messages"), e); //"Error checking transformation","An error occured checking this transformation\!"
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, Messages.getString("AnalyseImpactProgressDialog.Dialog.UnableToAnalyzeImpact.Title"), Messages.getString("AnalyseImpactProgressDialog.Dialog.UnableToAnalyzeImpact.Messages"), e); //"Error checking transformation","An error occured checking this transformation\!"
		}
		
		return impactHasRun;
	}
}
