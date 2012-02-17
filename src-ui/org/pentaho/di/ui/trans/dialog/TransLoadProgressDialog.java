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

package org.pentaho.di.ui.trans.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.ProgressMonitorAdapter;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.Spoon;






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
	private ObjectId objectId;

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
	
	 /**
   * Creates a new dialog that will handle the wait while loading a transformation...
   */
  public TransLoadProgressDialog(Shell shell, Repository rep, ObjectId objectId, String versionLabel)
  {
    this.shell = shell;
    this.rep = rep;
    this.objectId = objectId;
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
				  if (objectId != null) {
				    transInfo = rep.loadTransformation(objectId, versionLabel);
				  } else {
					  transInfo = rep.loadTransformation(transname, repdir, new ProgressMonitorAdapter(monitor), true, versionLabel);
				  }
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
		   Spoon.getInstance().hideSplash();
			new ErrorDialog(shell, BaseMessages.getString(PKG, "TransLoadProgressDialog.ErrorLoadingTransformation.DialogTitle"), BaseMessages.getString(PKG, "TransLoadProgressDialog.ErrorLoadingTransformation.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
			transInfo = null;
		}
		catch (InterruptedException e)
		{
		   Spoon.getInstance().hideSplash();
			new ErrorDialog(shell, BaseMessages.getString(PKG, "TransLoadProgressDialog.ErrorLoadingTransformation.DialogTitle"), BaseMessages.getString(PKG, "TransLoadProgressDialog.ErrorLoadingTransformation.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
			transInfo = null;
		}

		return transInfo;
	}
}
