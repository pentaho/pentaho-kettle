/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

/*
 * Created on 19-jun-2003
 *
 */

package org.pentaho.di.ui.job.entries.tableexists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.tableexists.JobEntryTableExists;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog allows you to edit the Table Exists job entry settings. (select the connection and
 * the table to be checked) This entry type evaluates!
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class JobEntryTableExistsDialog extends JobEntryDialog implements JobEntryDialogInterface
{	
	private static Class<?> PKG = JobEntryTableExists.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private Label wlName;

    private Text wName;

    private FormData fdlName, fdName;


    private CCombo wConnection;

    private Label wlTablename;

    private TextVar wTablename;

    private FormData fdlTablename, fdTablename;
    
    private Label wlSchemaname;

    private TextVar wSchemaname;

    private FormData fdlSchemaname, fdSchemaname;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntryTableExists jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;

    private boolean changed;

    public JobEntryTableExistsDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryTableExists) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(BaseMessages.getString(PKG, "JobTableExists.Name.Default"));
    }

    public JobEntryInterface open()
    {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, props.getJobsDialogStyle());
        props.setLook(shell);
        JobDialog.setShellImage(shell, jobEntry);

        ModifyListener lsMod = new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                jobEntry.setChanged();
            }
        };
        changed = jobEntry.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "JobTableExists.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Filename line
        wlName = new Label(shell, SWT.RIGHT);
        wlName.setText(BaseMessages.getString(PKG, "JobTableExists.Name.Label"));
        props.setLook(wlName);
        fdlName = new FormData();
        fdlName.left = new FormAttachment(0, 0);
        fdlName.right = new FormAttachment(middle, -margin);
        fdlName.top = new FormAttachment(0, margin);
        wlName.setLayoutData(fdlName);
        wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wName);
        wName.addModifyListener(lsMod);
        fdName = new FormData();
        fdName.left = new FormAttachment(middle, 0);
        fdName.top = new FormAttachment(0, margin);
        fdName.right = new FormAttachment(100, 0);
        wName.setLayoutData(fdName);

		// Connection line
		wConnection = addConnectionLine(shell, wName, middle, margin);
		if (jobEntry.getDatabase()==null && jobMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);
        
       
        // Schema name line
        wlSchemaname = new Label(shell, SWT.RIGHT);
        wlSchemaname.setText(BaseMessages.getString(PKG, "JobTableExists.Schemaname.Label"));
        props.setLook(wlSchemaname);
        fdlSchemaname = new FormData();
        fdlSchemaname.left = new FormAttachment(0, 0);
        fdlSchemaname.right = new FormAttachment(middle, -margin);
        fdlSchemaname.top = new FormAttachment(wConnection, margin);
        wlSchemaname.setLayoutData(fdlSchemaname);

        wSchemaname = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wSchemaname);
        wSchemaname.addModifyListener(lsMod);
        fdSchemaname = new FormData();
        fdSchemaname.left = new FormAttachment(middle, 0);
        fdSchemaname.top = new FormAttachment(wConnection, margin);
        fdSchemaname.right = new FormAttachment(100, 0);
        wSchemaname.setLayoutData(fdSchemaname);
		
        // Table name line
        wlTablename = new Label(shell, SWT.RIGHT);
        wlTablename.setText(BaseMessages.getString(PKG, "JobTableExists.Tablename.Label"));
        props.setLook(wlTablename);
        fdlTablename = new FormData();
        fdlTablename.left = new FormAttachment(0, 0);
        fdlTablename.right = new FormAttachment(middle, -margin);
        fdlTablename.top = new FormAttachment(wSchemaname, margin);
        wlTablename.setLayoutData(fdlTablename);

        wTablename = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wTablename);
        wTablename.addModifyListener(lsMod);
        fdTablename = new FormData();
        fdTablename.left = new FormAttachment(middle, 0);
        fdTablename.top = new FormAttachment(wSchemaname, margin);
        fdTablename.right = new FormAttachment(100, 0);
        wTablename.setLayoutData(fdTablename);

        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        FormData fd = new FormData();
        fd.right = new FormAttachment(50, -10);
        fd.bottom = new FormAttachment(100, 0);
        fd.width = 100;
        wOK.setLayoutData(fd);

        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
        fd = new FormData();
        fd.left = new FormAttachment(50, 10);
        fd.bottom = new FormAttachment(100, 0);
        fd.width = 100;
        wCancel.setLayoutData(fd);

        // Add listeners
        lsCancel = new Listener()
        {
            public void handleEvent(Event e)
            {
                cancel();
            }
        };
        lsOK = new Listener()
        {
            public void handleEvent(Event e)
            {
                ok();
            }
        };

        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);
		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wTablename);
        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };

        wName.addSelectionListener(lsDef);
        wTablename.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

        getData();

        BaseStepDialog.setSize(shell);

        shell.open();
        props.setDialogSize(shell, "JobTableExistsDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
    }

    public void dispose()
    {
        WindowProperty winprop = new WindowProperty(shell);
        props.setScreen(winprop);
        shell.dispose();
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    public void getData()
    {
        // System.out.println("evaluates: "+jobentry.evaluates());

        if (jobEntry.getName() != null)
            wName.setText(jobEntry.getName());
        if (jobEntry.getTablename() != null)
            wTablename.setText(jobEntry.getTablename());
        if (jobEntry.getSchemaname() != null)
            wSchemaname.setText(jobEntry.getSchemaname());
        if (jobEntry.getDatabase() != null)
        {
            wConnection.setText(jobEntry.getDatabase().getName());
        }
        wName.selectAll();
    }

    private void cancel()
    {
        jobEntry.setChanged(changed);
        jobEntry = null;
        dispose();
    }

    private void ok()
    {
 	   if(Const.isEmpty(wName.getText())) 
       {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setText(BaseMessages.getString(PKG, "System.StepJobEntryNameMissing.Title"));
			mb.setMessage(BaseMessages.getString(PKG, "System.JobEntryNameMissing.Msg"));
			mb.open(); 
			return;
       }
        jobEntry.setName(wName.getText());
        jobEntry.setDatabase(jobMeta.findDatabase(wConnection.getText()));
        jobEntry.setTablename(wTablename.getText());
        jobEntry.setSchemaname(wSchemaname.getText());
        
        dispose();
    }
}
