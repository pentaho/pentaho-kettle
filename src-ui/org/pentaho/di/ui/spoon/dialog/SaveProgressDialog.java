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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.trans.dialog.Messages;

import org.pentaho.di.core.EngineMetaInterface;
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
