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

package org.pentaho.di.ui.core.database.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.ProgressMonitorAdapter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.DatabaseMetaInformation;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.Spoon;


/**
 * Takes care of displaying a dialog that will handle the wait while 
 * we're finding out what tables, views etc we can reach in the database.
 * 
 * @author Matt
 * @since  07-apr-2005
 */
public class GetDatabaseInfoProgressDialog
{
	private static Class<?> PKG = GetDatabaseInfoProgressDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Shell shell;
	private DatabaseMeta dbInfo;

	/**
	 * Creates a new dialog that will handle the wait while we're 
	 * finding out what tables, views etc we can reach in the database.
	 */
	public GetDatabaseInfoProgressDialog(Shell shell, DatabaseMeta dbInfo)
	{
		this.shell = shell;
		this.dbInfo = dbInfo;
	}
	
	public DatabaseMetaInformation open()
	{
		final DatabaseMetaInformation dmi = new DatabaseMetaInformation(dbInfo);
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
				try
				{
					dmi.getData(Spoon.loggingObject, new ProgressMonitorAdapter(monitor));
				}
				catch(Exception e)
				{
					throw new InvocationTargetException(e, BaseMessages.getString(PKG, "GetDatabaseInfoProgressDialog.Error.GettingInfoTable",e.toString()));
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
		    showErrorDialog(e);
			return null;
		}
		catch (InterruptedException e)
		{
		    showErrorDialog(e);
			return null;
		}
		
		return dmi;
	}

    /**
     * Showing an error dialog
     * 
     * @param e
    */
    private void showErrorDialog(Exception e)
    {
        new ErrorDialog(shell, BaseMessages.getString(PKG, "GetDatabaseInfoProgressDialog.Error.Title"),
            BaseMessages.getString(PKG, "GetDatabaseInfoProgressDialog.Error.Message"), e);
    }
}
