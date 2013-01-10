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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ProgressMonitorAdapter;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
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
	private static Class<?> PKG = RepositoryDialogInterface.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private Shell shell;
    private KettleDatabaseRepository rep;
    private boolean upgrade;
    
    private List<String> generatedStatements;
    
    private boolean dryRun;

	private LogChannelInterface	log;

    /**
     * Creates a new dialog that will handle the wait while upgrading or creating a repository...
     */
    public UpgradeRepositoryProgressDialog(Shell shell, KettleDatabaseRepository rep, boolean upgrade)
    {
        this.shell = shell;
        this.rep = rep;
        this.upgrade = upgrade;
        this.generatedStatements = new ArrayList<String>();
        this.dryRun = false;
        this.log = rep.getLog();
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
            	
            	// Ask if you want to do a dry run first?
            	//
            	MessageBox box = new MessageBox(shell, SWT.YES | SWT.NO);
            	box.setMessage(BaseMessages.getString(PKG, "UpgradeRepositoryDialog.DryRunQuestion.Message"));
            	box.setText(BaseMessages.getString(PKG, "UpgradeRepositoryDialog.DryRunQuestion.Title"));
            	int answer = box.open();
            	
                try
                {
                	if (answer==SWT.YES) {
                    	// First do a dry-run
                		//
                		dryRun=true;
                		rep.createRepositorySchema(new ProgressMonitorAdapter(monitor), upgrade, generatedStatements, true);
                	} else {
                		rep.createRepositorySchema(new ProgressMonitorAdapter(monitor), upgrade, generatedStatements, false);
                	}
                    
                }
                catch (KettleException e)
                {
                    log.logError(toString(), Const.getStackTracker(e));
                    throw new InvocationTargetException(e, BaseMessages.getString(PKG, "UpgradeRepositoryDialog.Error.CreateUpdate", e.getMessage()));
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
            log.logError(UpgradeRepositoryProgressDialog.class.toString(), "Error creating/updating repository: " + e.toString());
            log.logError(toString(), Const.getStackTracker(e));
            showErrorDialog(e);

            retval = false;
        }
        catch (InterruptedException e)
        {
            log.logError(UpgradeRepositoryProgressDialog.class.toString(), "Error creating/updating repository: " + e.toString());
            log.logError(toString(), Const.getStackTracker(e));
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
            sTitle = BaseMessages.getString(PKG, "UpgradeRepositoryDialog.ErrorUpgrade.Title");
            sMessage = BaseMessages.getString(PKG, "UpgradeRepositoryDialog.ErrorUpgrade.Message");
        }
        else
        {
            sTitle = BaseMessages.getString(PKG, "UpgradeRepositoryDialog.ErrorCreate.Title");
            sMessage = BaseMessages.getString(PKG, "UpgradeRepositoryDialog.ErrorCreate.Message");
        }

        new ErrorDialog(shell, sTitle, sMessage, e);
    }

	/**
	 * @return the dryRun
	 */
	public boolean isDryRun() {
		return dryRun;
	}

	/**
	 * @return the generatedStatements
	 */
	public List<String> getGeneratedStatements() {
		return generatedStatements;
	}
}
