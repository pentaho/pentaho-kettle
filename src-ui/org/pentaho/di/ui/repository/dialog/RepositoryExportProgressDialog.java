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

package org.pentaho.di.ui.repository.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ProgressMonitorAdapter;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryExporter;
import org.pentaho.di.ui.core.dialog.ErrorDialog;

/**
 * Takes care of displaying a dialog that will handle the wait while we are exporting the complete
 * repository to XML...
 * 
 * @author Matt
 * @since 02-jun-2005
 */
public class RepositoryExportProgressDialog
{
	private static Class<?> PKG = RepositoryDialogInterface.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private Shell shell;
    private Repository rep;
    private RepositoryDirectory dir;
    private String filename;

	private LogChannelInterface	log;

    public RepositoryExportProgressDialog(Shell shell, Repository rep, RepositoryDirectory dir, String filename)
    {
        this.shell = shell;
        this.rep = rep;
        this.dir = dir;        
        this.filename = filename;
        this.log = rep.getLog();
    }

    public boolean open()
    {
        boolean retval = true;

        IRunnableWithProgress op = new IRunnableWithProgress()
        {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
                try
                {
                    new RepositoryExporter(rep).exportAllObjects(new ProgressMonitorAdapter(monitor), filename, dir,"all");
                }
                catch (KettleException e)
                {
                    log.logError(Const.getStackTracker(e));
                    throw new InvocationTargetException(e, BaseMessages.getString(PKG, "RepositoryExportDialog.Error.CreateUpdate", e.getMessage()));
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
            log.logError(RepositoryExportProgressDialog.class.toString(), "Error creating repository: " + e.toString());
            log.logError(Const.getStackTracker(e));
            new ErrorDialog(shell, BaseMessages.getString(PKG, "RepositoryExportDialog.ErrorExport.Title"), BaseMessages.getString(PKG, "RepositoryExportDialog.ErrorExport.Message"), e);
            retval = false;
        }
        catch (InterruptedException e)
        {
            log.logError(RepositoryExportProgressDialog.class.toString(), "Error creating repository: " + e.toString());
            log.logError(Const.getStackTracker(e));
            new ErrorDialog(shell, BaseMessages.getString(PKG, "RepositoryExportDialog.ErrorExport.Title"), BaseMessages.getString(PKG, "RepositoryExportDialog.ErrorExport.Message"), e);
            retval = false;
        }

        return retval;
    }
}
