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

package org.pentaho.di.ui.trans.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.ProgressMonitorAdapter;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;






/**
 * Takes care of displaying a dialog that will handle the wait while loading a transformation...
 * 
 * @author Matt
 * @since  13-mrt-2005
 */
public class TransLoadProgressDialog
{
    private static Class<?> PKG = TransDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Shell shell;
	private Repository rep;
	private String transname;
	private RepositoryDirectoryInterface repdir;
	private TransMeta transInfo;

	private String	versionLabel;
	
	/**
	 * Creates a new dialog that will handle the wait while loading a transformation...
	 */
	public TransLoadProgressDialog(Shell shell, Repository rep, String transname, RepositoryDirectoryInterface repdir, String versionLabel)
	{
		this.shell = shell;
		this.rep = rep;
		this.transname = transname;
		this.repdir = repdir;
		this.versionLabel = versionLabel;
		
		this.transInfo = null;
	}
	
	public TransMeta open()
	{
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
				try
				{
					transInfo = rep.loadTransformation(transname, repdir, new ProgressMonitorAdapter(monitor), true, versionLabel);
				}
				catch(KettleException e)
				{
					throw new InvocationTargetException(e, BaseMessages.getString(PKG, "TransLoadProgressDialog.Exception.ErrorLoadingTransformation")); //$NON-NLS-1$
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
			new ErrorDialog(shell, BaseMessages.getString(PKG, "TransLoadProgressDialog.ErrorLoadingTransformation.DialogTitle"), BaseMessages.getString(PKG, "TransLoadProgressDialog.ErrorLoadingTransformation.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
			transInfo = null;
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "TransLoadProgressDialog.ErrorLoadingTransformation.DialogTitle"), BaseMessages.getString(PKG, "TransLoadProgressDialog.ErrorLoadingTransformation.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
			transInfo = null;
		}

		return transInfo;
	}
}
