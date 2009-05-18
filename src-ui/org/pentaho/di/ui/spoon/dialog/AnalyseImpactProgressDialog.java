/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
/*
 *
 *
 */

package org.pentaho.di.ui.spoon.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.ProgressMonitorAdapter;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;


/**
 * Takes care of displaying a dialog that will handle the wait while 
 * where determining the impact of a transformation on the used databases.
 * 
 * @author Matt
 * @since  04-apr-2005
 */
public class AnalyseImpactProgressDialog
{
	private static Class<?> PKG = AnalyseImpactProgressDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Shell shell;
	private TransMeta transMeta;
	private List<DatabaseImpact> impact;
	private boolean impactHasRun;

    
	/**
	 * Creates a new dialog that will handle the wait while determining the impact of the transformation on the databases used...
	 */
	public AnalyseImpactProgressDialog(Shell shell, TransMeta transMeta, List<DatabaseImpact> impact)
	{
		this.shell = shell;
		this.transMeta = transMeta;
		this.impact = impact;        
    }
	
	public boolean open()
	{
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
				try
				{
					impact.clear(); // Start with a clean slate!!
					transMeta.analyseImpact(impact, new ProgressMonitorAdapter(monitor));
					impactHasRun = true;
				}
				catch(Exception e)
				{
					impact.clear();
					impactHasRun=false;
					throw new InvocationTargetException(e, BaseMessages.getString(PKG, "AnalyseImpactProgressDialog.RuntimeError.UnableToAnalyzeImpact.Exception", e.toString())); //Problem encountered generating impact list: {0}
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
			new ErrorDialog(shell, BaseMessages.getString(PKG, "AnalyseImpactProgressDialog.Dialog.UnableToAnalyzeImpact.Title"), BaseMessages.getString(PKG, "AnalyseImpactProgressDialog.Dialog.UnableToAnalyzeImpact.Messages"), e); //"Error checking transformation","An error occured checking this transformation\!"
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "AnalyseImpactProgressDialog.Dialog.UnableToAnalyzeImpact.Title"), BaseMessages.getString(PKG, "AnalyseImpactProgressDialog.Dialog.UnableToAnalyzeImpact.Messages"), e); //"Error checking transformation","An error occured checking this transformation\!"
		}
		
		return impactHasRun;
	}
}