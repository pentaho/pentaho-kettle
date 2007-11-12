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

package org.pentaho.di.ui.job.entries.sql;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog; 
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.job.entries.sql.JobEntrySQL;
import org.pentaho.di.job.entries.sql.Messages;


/**
 * This dialog allows you to edit the SQL job entry settings. (select the connection and the sql
 * script to be executed)
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class JobEntrySQLDialog extends JobEntryDialog implements JobEntryDialogInterface
{
    private Label wlName;

    private Text wName;

    private FormData fdlName, fdName;

    private Label wlConnection;

    private CCombo wConnection;

    private Button wbConnection;

    private FormData fdlConnection, fdbConnection, fdConnection;

    private Label wlUseSubs;

    private Button wUseSubs;

    private FormData fdlUseSubs, fdUseSubs;

    private Label wlSQL;

    private Text wSQL;

    private FormData fdlSQL, fdSQL;

    private Label wlPosition;

    private FormData fdlPosition;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntrySQL jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;

    private boolean changed;

    public JobEntrySQLDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntrySQL) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(Messages.getString("JobSQL.Name.Default"));
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
        shell.setText(Messages.getString("JobSQL.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);

        // Filename line
        wlName = new Label(shell, SWT.RIGHT);
        wlName.setText(Messages.getString("JobSQL.Name.Label"));
        props.setLook(wlName);
        fdlName = new FormData();
        fdlName.left = new FormAttachment(0, 0);
        fdlName.right = new FormAttachment(middle, 0);
        fdlName.top = new FormAttachment(0, margin);
        wlName.setLayoutData(fdlName);
        wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wName);
        wName.addModifyListener(lsMod);
        fdName = new FormData();
        fdName.left = new FormAttachment(middle, margin);
        fdName.top = new FormAttachment(0, margin);
        fdName.right = new FormAttachment(100, 0);
        wName.setLayoutData(fdName);

        // Connection line
        wlConnection = new Label(shell, SWT.RIGHT);
        wlConnection.setText(Messages.getString("JobSQL.Connection.Label"));
        props.setLook(wlConnection);
        fdlConnection = new FormData();
        fdlConnection.left = new FormAttachment(0, 0);
        fdlConnection.right = new FormAttachment(middle, 0);
        fdlConnection.top = new FormAttachment(wName, margin);
        wlConnection.setLayoutData(fdlConnection);

        wbConnection = new Button(shell, SWT.PUSH);
        wbConnection.setText(Messages.getString("System.Button.New") + "...");
        wbConnection.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                DatabaseMeta databaseMeta = new DatabaseMeta();
                databaseMeta.shareVariablesWith(jobMeta);
                DatabaseDialog cid = new DatabaseDialog(shell, databaseMeta);
                cid.setModalDialog(true);
                if (cid.open() != null)
                {
                    jobMeta.addDatabase(databaseMeta);

                    // SB: Maybe do the same her as in BaseStepDialog: remove
                    // all db connections and add them again.
                    wConnection.add(databaseMeta.getName());
                    wConnection.select(wConnection.getItemCount() - 1);
                }
            }
        });
        fdbConnection = new FormData();
        fdbConnection.right = new FormAttachment(100, 0);
        fdbConnection.top = new FormAttachment(wName, margin);
        fdbConnection.height = 20;
        wbConnection.setLayoutData(fdbConnection);

        wConnection = new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
        props.setLook(wConnection);
        for (int i = 0; i < jobMeta.nrDatabases(); i++)
        {
            DatabaseMeta ci = jobMeta.getDatabase(i);
            wConnection.add(ci.getName());
        }
        wConnection.select(0);
        wConnection.addModifyListener(lsMod);
        fdConnection = new FormData();
        fdConnection.left = new FormAttachment(middle, margin);
        fdConnection.top = new FormAttachment(wName, margin);
        fdConnection.right = new FormAttachment(wbConnection, -margin);
        wConnection.setLayoutData(fdConnection);

        // Include Files?
        wlUseSubs = new Label(shell, SWT.RIGHT);
        wlUseSubs.setText(Messages.getString("JobSQL.UseVariableSubst.Label"));
        props.setLook(wlUseSubs);
        fdlUseSubs = new FormData();
        fdlUseSubs.left = new FormAttachment(0, 0);
        fdlUseSubs.top = new FormAttachment(wConnection, margin);
        fdlUseSubs.right = new FormAttachment(middle, -margin);
        wlUseSubs.setLayoutData(fdlUseSubs);
        wUseSubs = new Button(shell, SWT.CHECK);
        props.setLook(wUseSubs);
        wUseSubs.setToolTipText(Messages.getString("JobSQL.UseVariableSubst.Tooltip"));
        fdUseSubs = new FormData();
        fdUseSubs.left = new FormAttachment(middle, margin);
        fdUseSubs.top = new FormAttachment(wConnection, margin);
        fdUseSubs.right = new FormAttachment(100, 0);
        wUseSubs.setLayoutData(fdUseSubs);
        wUseSubs.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setUseVariableSubstitution(!jobEntry.getUseVariableSubstitution());
                jobEntry.setChanged();
            }
        });

        wlPosition = new Label(shell, SWT.NONE);
        wlPosition.setText(Messages.getString("JobSQL.LineNr.Label", "0"));
        props.setLook(wlPosition);
        fdlPosition = new FormData();
        fdlPosition.left = new FormAttachment(0, 0);
        fdlPosition.bottom = new FormAttachment(wOK, -margin);
        wlPosition.setLayoutData(fdlPosition);

        // Script line
        wlSQL = new Label(shell, SWT.NONE);
        wlSQL.setText(Messages.getString("JobSQL.Script.Label"));
        props.setLook(wlSQL);
        fdlSQL = new FormData();
        fdlSQL.left = new FormAttachment(0, 0);
        fdlSQL.top = new FormAttachment(wConnection, margin);
        wlSQL.setLayoutData(fdlSQL);
        wSQL = new Text(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        props.setLook(wSQL, Props.WIDGET_STYLE_FIXED);
        wSQL.addModifyListener(lsMod);
        fdSQL = new FormData();
        fdSQL.left = new FormAttachment(0, 0);
        fdSQL.top = new FormAttachment(wlSQL, margin);
        fdSQL.right = new FormAttachment(100, -5);
        fdSQL.bottom = new FormAttachment(wlPosition, -margin);
        wSQL.setLayoutData(fdSQL);

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

        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };

        wName.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

        wSQL.addKeyListener(new KeyAdapter()
        {
            public void keyReleased(KeyEvent e)
            {
                int linenr = wSQL.getCaretLineNumber() + 1;
                wlPosition.setText(Messages.getString("JobSQL.LineNr.Label", Integer
                    .toString(linenr)));
            }
        });

        getData();

        BaseStepDialog.setSize(shell);

        shell.open();
        props.setDialogSize(shell, "JobSQLDialogSize");
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
        if (jobEntry.getName() != null)
            wName.setText(jobEntry.getName());
        if (jobEntry.getSQL() != null)
            wSQL.setText(jobEntry.getSQL());
        DatabaseMeta dbinfo = jobEntry.getDatabase();
        if (dbinfo != null && dbinfo.getName() != null)
            wConnection.setText(dbinfo.getName());
        else
            wConnection.setText("");

        wUseSubs.setSelection(jobEntry.getUseVariableSubstitution());
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
        jobEntry.setName(wName.getText());
        jobEntry.setSQL(wSQL.getText());
        jobEntry.setDatabase(jobMeta.findDatabase(wConnection.getText()));
        dispose();
    }

    public String toString()
    {
        return this.getClass().getName();
    }
}
