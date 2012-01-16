/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.spoon.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.ProgressMonitorAdapter;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;


/**
 * Takes care of displaying a dialog that will handle the wait while checking a transformation...
 * 
 * @author Matt
 * @since  16-mrt-2005
 */
public class CheckTransProgressDialog
{
	private static Class<?> PKG = CheckTransProgressDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Shell shell;
	private TransMeta transMeta;
	private List<CheckResultInterface> remarks;
	private boolean onlySelected;

	/**
	 * Creates a new dialog that will handle the wait while checking a transformation...
	 */
	public CheckTransProgressDialog(Shell shell, TransMeta transMeta, List<CheckResultInterface> remarks, boolean onlySelected)
	{
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
					transMeta.checkSteps(remarks, onlySelected, new ProgressMonitorAdapter(monitor));
				}
				catch(Exception e)
				{
					throw new InvocationTargetException(e, BaseMessages.getString(PKG, "AnalyseImpactProgressDialog.RuntimeError.ErrorCheckingTransformation.Exception", e.toString())); //Problem encountered checking transformation: {0}
				}
			}
		};
		
		try
		{
            // Run something in the background to cancel active database queries, force this if needed!
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
			new ErrorDialog(shell, BaseMessages.getString(PKG, "CheckTransProgressDialog.Dialog.ErrorCheckingTransformation.Title"), BaseMessages.getString(PKG, "CheckTransProgressDialog.Dialog.ErrorCheckingTransformation.Message"), e); // "Error checking transformation","An error occured checking this transformation\!"
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "CheckTransProgressDialog.Dialog.ErrorCheckingTransformation.Title"), BaseMessages.getString(PKG, "CheckTransProgressDialog.Dialog.ErrorCheckingTransformation.Message"), e); // "Error checking transformation","An error occured checking this transformation\!"
		}
	}
}