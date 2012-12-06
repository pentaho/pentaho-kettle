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
import org.pentaho.di.imp.ImportRules;
import org.pentaho.di.repository.IRepositoryExporter;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
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
    private RepositoryDirectoryInterface dir;
    private String filename;
    private ImportRules importRules;

	private LogChannelInterface	log;

  public RepositoryExportProgressDialog(Shell shell, Repository rep, RepositoryDirectoryInterface dir, String filename)
  {
    this(shell, rep, dir, filename, new ImportRules());
  }

    public RepositoryExportProgressDialog(Shell shell, Repository rep, RepositoryDirectoryInterface dir, String filename, ImportRules importRules)
    {
        this.shell = shell;
        this.rep = rep;
        this.dir = dir;        
        this.filename = filename;
        this.importRules = importRules;
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
                    IRepositoryExporter exporter = rep.getExporter();
                    exporter.setImportRulesToValidate(importRules);
                    
                    exporter.exportAllObjects(new ProgressMonitorAdapter(monitor), filename, dir, "all");
                }
                catch (KettleException e)
                {
                    throw new InvocationTargetException(e, BaseMessages.getString(PKG, "RepositoryExportDialog.Error.CreateUpdate", Const.getStackTracker(e)));
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
