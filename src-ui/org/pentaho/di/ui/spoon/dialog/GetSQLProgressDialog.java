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
import org.pentaho.di.core.Props;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;




/**
 * Takes care of displaying a dialog that will handle the wait while getting the SQL for a transformation...
 * 
 * @author Matt
 * @since  15-mrt-2005
 */
public class GetSQLProgressDialog
{
	private static Class<?> PKG = GetSQLProgressDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Shell shell;
	private TransMeta transMeta;
	private List<SQLStatement> stats;

    /**
     * Creates a new dialog that will handle the wait while getting the SQL for a transformation...
     * @deprecated please use the constructor version without log or props
     */
    public GetSQLProgressDialog(LogWriter log, Props props, Shell shell, TransMeta transMeta)
    {
        this(shell, transMeta);
    }
    
	/**
	 * Creates a new dialog that will handle the wait while getting the SQL for a transformation...
	 */
	public GetSQLProgressDialog(Shell shell, TransMeta transMeta)
	{
		this.shell = shell;
		this.transMeta = transMeta;
	}
	
	public List<SQLStatement> open()
	{
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
                // This is running in a new process: copy some KettleVariables info
                // LocalVariables.getInstance().createKettleVariables(Thread.currentThread(), parentThread, true);
                // --> don't set variables if not running in different thread --> pmd.run(true,true, op);

				try
				{
					stats = transMeta.getSQLStatements(new ProgressMonitorAdapter(monitor));
				}
				catch(KettleException e)
				{
					throw new InvocationTargetException(e, BaseMessages.getString(PKG, "GetSQLProgressDialog.RuntimeError.UnableToGenerateSQL.Exception", e.getMessage())); //Error generating SQL for transformation: \n{0}
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
			new ErrorDialog(shell, BaseMessages.getString(PKG, "GetSQLProgressDialog.Dialog.UnableToGenerateSQL.Title"), BaseMessages.getString(PKG, "GetSQLProgressDialog.Dialog.UnableToGenerateSQL.Message"), e); //"Error generating SQL for transformation","An error occured generating the SQL for this transformation\!"
			stats = null;
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "GetSQLProgressDialog.Dialog.UnableToGenerateSQL.Title"), BaseMessages.getString(PKG, "GetSQLProgressDialog.Dialog.UnableToGenerateSQL.Message"), e); //"Error generating SQL for transformation","An error occured generating the SQL for this transformation\!"
			stats = null;
		}

		return stats;
	}
}
