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
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.repository.dialog.Messages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;



/**
 * Takes care of displaying a dialog that will handle the wait while creating or upgrading a
 * transformation...
 * 
 * @author Matt
 * @since 13-mrt-2005
 */
public class UpgradeRepositoryProgressDialog
{
    private Shell shell;
    private Repository rep;
    private boolean upgrade;

    /**
     * @deprecated Use the constructor version without <i>log</i> or <i>props</i>
     */
    public UpgradeRepositoryProgressDialog(LogWriter log, Props props, Shell shell, Repository rep, boolean upgrade)
    {
        this(shell, rep, upgrade);
    }
    
    /**
     * Creates a new dialog that will handle the wait while upgrading or creating a repository...
     */
    public UpgradeRepositoryProgressDialog(Shell shell, Repository rep, boolean upgrade)
    {
        this.shell = shell;
        this.rep = rep;
        this.upgrade = upgrade;
    }

    public boolean open()
    {
        boolean retval = true;

        IRunnableWithProgress op = new IRunnableWithProgress()
        {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
                // This is running in a new process: copy some KettleVariables info
                // LocalVariables.getInstance().createKettleVariables(Thread.currentThread(),
                // kettleVariables.getLocalThread(), true);
                // --> don't set variables if not running in different thread --> pmd.run(true,true,
                // op);

                try
                {
                    rep.createRepositorySchema(monitor, upgrade);
                }
                catch (KettleException e)
                {
                    LogWriter.getInstance().logError(toString(), Const.getStackTracker(e));
                    throw new InvocationTargetException(e, Messages.getString("UpgradeRepositoryDialog.Error.CreateUpdate", e.getMessage()));
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
            LogWriter.getInstance().logError(UpgradeRepositoryProgressDialog.class.toString(), "Error creating/updating repository: " + e.toString());
            LogWriter.getInstance().logError(toString(), Const.getStackTracker(e));
            showErrorDialog(e);

            retval = false;
        }
        catch (InterruptedException e)
        {
            LogWriter.getInstance().logError(UpgradeRepositoryProgressDialog.class.toString(), "Error creating/updating repository: " + e.toString());
            LogWriter.getInstance().logError(toString(), Const.getStackTracker(e));
            showErrorDialog(e);

            retval = false;
        }

        return retval;
    }

    private void showErrorDialog(Exception e)
    {
        String sTitle, sMessage;
        if (upgrade)
        {
            sTitle = Messages.getString("UpgradeRepositoryDialog.ErrorUpgrade.Title");
            sMessage = Messages.getString("UpgradeRepositoryDialog.ErrorUpgrade.Message");
        }
        else
        {
            sTitle = Messages.getString("UpgradeRepositoryDialog.ErrorCreate.Title");
            sMessage = Messages.getString("UpgradeRepositoryDialog.ErrorCreate.Message");
        }

        new ErrorDialog(shell, sTitle, sMessage, e);
    }
}
