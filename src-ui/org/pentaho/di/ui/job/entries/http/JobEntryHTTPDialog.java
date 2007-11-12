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

package org.pentaho.di.ui.job.entries.http;

import org.eclipse.swt.SWT;
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
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog; 
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.job.entries.http.JobEntryHTTP;
import org.pentaho.di.job.entries.http.Messages;

/**
 * This dialog allows you to edit the SQL job entry settings. (select the connection and the sql
 * script to be executed)
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class JobEntryHTTPDialog extends JobEntryDialog implements JobEntryDialogInterface
{
    private Label wlName;

    private Text wName;

    private FormData fdlName, fdName;

    private Label wlURL;

    private TextVar wURL;

    private FormData fdlURL, fdURL;

    private Label wlRunEveryRow;

    private Button wRunEveryRow;

    private FormData fdlRunEveryRow, fdRunEveryRow;

    private Label wlFieldURL;

    private TextVar wFieldURL;

    private FormData fdlFieldURL, fdFieldURL;

    private Label wlTargetFile;

    private TextVar wTargetFile;

    private FormData fdlTargetFile, fdTargetFile;

    private Label wlAppend;

    private Button wAppend;

    private FormData fdlAppend, fdAppend;

    private Label wlDateTimeAdded;

    private Button wDateTimeAdded;

    private FormData fdlDateTimeAdded, fdDateTimeAdded;

    private Label wlTargetExt;

    private TextVar wTargetExt;

    private FormData fdlTargetExt, fdTargetExt;

    private Label wlUploadFile;

    private TextVar wUploadFile;

    private FormData fdlUploadFile, fdUploadFile;

    private Label wlUserName;

    private TextVar wUserName;

    private FormData fdlUserName, fdUserName;

    private Label wlPassword;

    private TextVar wPassword;

    private FormData fdlPassword, fdPassword;

    private Label wlProxyServer;

    private TextVar wProxyServer;

    private FormData fdlProxyServer, fdProxyServer;

    private Label wlProxyPort;

    private TextVar wProxyPort;

    private FormData fdlProxyPort, fdProxyPort;

    private Label wlNonProxyHosts;

    private TextVar wNonProxyHosts;

    private FormData fdlNonProxyHosts, fdNonProxyHosts;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntryHTTP jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;

    private boolean changed;

    public JobEntryHTTPDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryHTTP) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(Messages.getString("JobHTTP.Name.Default"));
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
        shell.setText(Messages.getString("JobHTTP.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Job entry name line
        wlName = new Label(shell, SWT.RIGHT);
        wlName.setText(Messages.getString("JobHTTP.Name.Label"));
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

        // URL line
        wlURL = new Label(shell, SWT.RIGHT);
        wlURL.setText(Messages.getString("JobHTTP.URL.Label"));
        props.setLook(wlURL);
        fdlURL = new FormData();
        fdlURL.left = new FormAttachment(0, 0);
        fdlURL.top = new FormAttachment(wName, margin);
        fdlURL.right = new FormAttachment(middle, -margin);
        wlURL.setLayoutData(fdlURL);
        wURL = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages.getString("JobHTTP.URL.Tooltip"));
        props.setLook(wURL);
        wURL.addModifyListener(lsMod);
        fdURL = new FormData();
        fdURL.left = new FormAttachment(middle, 0);
        fdURL.top = new FormAttachment(wName, margin);
        fdURL.right = new FormAttachment(100, 0);
        wURL.setLayoutData(fdURL);

        // RunEveryRow line
        wlRunEveryRow = new Label(shell, SWT.RIGHT);
        wlRunEveryRow.setText(Messages.getString("JobHTTP.RunForEveryRow.Label"));
        props.setLook(wlRunEveryRow);
        fdlRunEveryRow = new FormData();
        fdlRunEveryRow.left = new FormAttachment(0, 0);
        fdlRunEveryRow.top = new FormAttachment(wURL, margin);
        fdlRunEveryRow.right = new FormAttachment(middle, -margin);
        wlRunEveryRow.setLayoutData(fdlRunEveryRow);
        wRunEveryRow = new Button(shell, SWT.CHECK);
        wRunEveryRow.setToolTipText(Messages.getString("JobHTTP.RunForEveryRow.Tooltip"));
        props.setLook(wRunEveryRow);
        fdRunEveryRow = new FormData();
        fdRunEveryRow.left = new FormAttachment(middle, 0);
        fdRunEveryRow.top = new FormAttachment(wURL, margin);
        fdRunEveryRow.right = new FormAttachment(100, 0);
        wRunEveryRow.setLayoutData(fdRunEveryRow);
        wRunEveryRow.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                setFlags();
            }
        });

        // FieldURL line
        wlFieldURL = new Label(shell, SWT.RIGHT);
        wlFieldURL.setText(Messages.getString("JobHTTP.InputField.Label"));
        props.setLook(wlFieldURL);
        fdlFieldURL = new FormData();
        fdlFieldURL.left = new FormAttachment(0, 0);
        fdlFieldURL.top = new FormAttachment(wRunEveryRow, margin);
        fdlFieldURL.right = new FormAttachment(middle, -margin);
        wlFieldURL.setLayoutData(fdlFieldURL);
        wFieldURL = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wFieldURL);
        wFieldURL.setToolTipText(Messages.getString("JobHTTP.InputField.Tooltip"));
        wFieldURL.addModifyListener(lsMod);
        fdFieldURL = new FormData();
        fdFieldURL.left = new FormAttachment(middle, 0);
        fdFieldURL.top = new FormAttachment(wRunEveryRow, margin);
        fdFieldURL.right = new FormAttachment(100, 0);
        wFieldURL.setLayoutData(fdFieldURL);

        // TargetFile line
        wlTargetFile = new Label(shell, SWT.RIGHT);
        wlTargetFile.setText(Messages.getString("JobHTTP.TargetFile.Label"));
        props.setLook(wlTargetFile);
        fdlTargetFile = new FormData();
        fdlTargetFile.left = new FormAttachment(0, 0);
        fdlTargetFile.top = new FormAttachment(wFieldURL, margin);
        fdlTargetFile.right = new FormAttachment(middle, -margin);
        wlTargetFile.setLayoutData(fdlTargetFile);
        wTargetFile = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wTargetFile);
        wTargetFile.setToolTipText(Messages.getString("JobHTTP.TargetFile.Tooltip"));
        wTargetFile.addModifyListener(lsMod);
        fdTargetFile = new FormData();
        fdTargetFile.left = new FormAttachment(middle, 0);
        fdTargetFile.top = new FormAttachment(wFieldURL, margin);
        fdTargetFile.right = new FormAttachment(100, 0);
        wTargetFile.setLayoutData(fdTargetFile);

        // Append line
        wlAppend = new Label(shell, SWT.RIGHT);
        wlAppend.setText(Messages.getString("JobHTTP.TargetFileAppend.Label"));
        props.setLook(wlAppend);
        fdlAppend = new FormData();
        fdlAppend.left = new FormAttachment(0, 0);
        fdlAppend.top = new FormAttachment(wTargetFile, margin);
        fdlAppend.right = new FormAttachment(middle, -margin);
        wlAppend.setLayoutData(fdlAppend);
        wAppend = new Button(shell, SWT.CHECK);
        props.setLook(wAppend);
        wAppend.setToolTipText(Messages.getString("JobHTTP.TargetFileAppend.Tooltip"));
        fdAppend = new FormData();
        fdAppend.left = new FormAttachment(middle, 0);
        fdAppend.top = new FormAttachment(wTargetFile, margin);
        fdAppend.right = new FormAttachment(100, 0);
        wAppend.setLayoutData(fdAppend);

        // DateTimeAdded line
        wlDateTimeAdded = new Label(shell, SWT.RIGHT);
        wlDateTimeAdded.setText(Messages.getString("JobHTTP.TargetFilenameAddDate.Label"));
        props.setLook(wlDateTimeAdded);
        fdlDateTimeAdded = new FormData();
        fdlDateTimeAdded.left = new FormAttachment(0, 0);
        fdlDateTimeAdded.top = new FormAttachment(wAppend, margin);
        fdlDateTimeAdded.right = new FormAttachment(middle, -margin);
        wlDateTimeAdded.setLayoutData(fdlDateTimeAdded);
        wDateTimeAdded = new Button(shell, SWT.CHECK);
        props.setLook(wDateTimeAdded);
        wDateTimeAdded.setToolTipText(Messages.getString("JobHTTP.TargetFilenameAddDate.Tooltip"));
        fdDateTimeAdded = new FormData();
        fdDateTimeAdded.left = new FormAttachment(middle, 0);
        fdDateTimeAdded.top = new FormAttachment(wAppend, margin);
        fdDateTimeAdded.right = new FormAttachment(100, 0);
        wDateTimeAdded.setLayoutData(fdDateTimeAdded);
        wDateTimeAdded.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                setFlags();
            }
        });

        // TargetExt line
        wlTargetExt = new Label(shell, SWT.RIGHT);
        wlTargetExt.setText(Messages.getString("JobHTTP.TargetFileExt.Label"));
        props.setLook(wlTargetExt);
        fdlTargetExt = new FormData();
        fdlTargetExt.left = new FormAttachment(0, 0);
        fdlTargetExt.top = new FormAttachment(wDateTimeAdded, margin);
        fdlTargetExt.right = new FormAttachment(middle, -margin);
        wlTargetExt.setLayoutData(fdlTargetExt);
        wTargetExt = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wTargetExt);
        wTargetExt.setToolTipText(Messages.getString("JobHTTP.TargetFileExt.Tooltip"));
        wTargetExt.addModifyListener(lsMod);
        fdTargetExt = new FormData();
        fdTargetExt.left = new FormAttachment(middle, 0);
        fdTargetExt.top = new FormAttachment(wDateTimeAdded, margin);
        fdTargetExt.right = new FormAttachment(100, 0);
        wTargetExt.setLayoutData(fdTargetExt);

        Label lSeparator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_SOLID);
        FormData fdSep = new FormData();
        fdSep.left = new FormAttachment(0, 0);
        fdSep.right = new FormAttachment(100, 0);
        fdSep.top = new FormAttachment(wTargetExt, 15);
        lSeparator.setLayoutData(fdSep);

        // UploadFile line
        wlUploadFile = new Label(shell, SWT.RIGHT);
        wlUploadFile.setText(Messages.getString("JobHTTP.UploadFile.Label"));
        props.setLook(wlUploadFile);
        fdlUploadFile = new FormData();
        fdlUploadFile.left = new FormAttachment(0, 0);
        fdlUploadFile.top = new FormAttachment(lSeparator, margin + 15);
        fdlUploadFile.right = new FormAttachment(middle, -margin);
        wlUploadFile.setLayoutData(fdlUploadFile);
        wUploadFile = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wUploadFile);
        wUploadFile.setToolTipText(Messages.getString("JobHTTP.UploadFile.Tooltip"));
        wUploadFile.addModifyListener(lsMod);
        fdUploadFile = new FormData();
        fdUploadFile.left = new FormAttachment(middle, 0);
        fdUploadFile.top = new FormAttachment(lSeparator, margin + 15);
        fdUploadFile.right = new FormAttachment(100, 0);
        wUploadFile.setLayoutData(fdUploadFile);

        // UserName line
        wlUserName = new Label(shell, SWT.RIGHT);
        wlUserName.setText(Messages.getString("JobHTTP.UploadUser.Label"));
        props.setLook(wlUserName);
        fdlUserName = new FormData();
        fdlUserName.left = new FormAttachment(0, 0);
        fdlUserName.top = new FormAttachment(wUploadFile, margin * 5);
        fdlUserName.right = new FormAttachment(middle, -margin);
        wlUserName.setLayoutData(fdlUserName);
        wUserName = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wUserName);
        wUserName.setToolTipText(Messages.getString("JobHTTP.UploadUser.Tooltip"));
        wUserName.addModifyListener(lsMod);
        fdUserName = new FormData();
        fdUserName.left = new FormAttachment(middle, 0);
        fdUserName.top = new FormAttachment(wUploadFile, margin * 5);
        fdUserName.right = new FormAttachment(100, 0);
        wUserName.setLayoutData(fdUserName);

        // Password line
        wlPassword = new Label(shell, SWT.RIGHT);
        wlPassword.setText(Messages.getString("JobHTTP.UploadPassword.Label"));
        props.setLook(wlPassword);
        fdlPassword = new FormData();
        fdlPassword.left = new FormAttachment(0, 0);
        fdlPassword.top = new FormAttachment(wUserName, margin);
        fdlPassword.right = new FormAttachment(middle, -margin);
        wlPassword.setLayoutData(fdlPassword);
        wPassword = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wPassword);
        wPassword.setToolTipText(Messages.getString("JobHTTP.UploadPassword.Tooltip"));
        wPassword.setEchoChar('*');
        wPassword.addModifyListener(lsMod);
        fdPassword = new FormData();
        fdPassword.left = new FormAttachment(middle, 0);
        fdPassword.top = new FormAttachment(wUserName, margin);
        fdPassword.right = new FormAttachment(100, 0);
        wPassword.setLayoutData(fdPassword);

        // ProxyServer line
        wlProxyServer = new Label(shell, SWT.RIGHT);
        wlProxyServer.setText(Messages.getString("JobHTTP.ProxyHost.Label"));
        props.setLook(wlProxyServer);
        fdlProxyServer = new FormData();
        fdlProxyServer.left = new FormAttachment(0, 0);
        fdlProxyServer.top = new FormAttachment(wPassword, margin * 5);
        fdlProxyServer.right = new FormAttachment(middle, -margin);
        wlProxyServer.setLayoutData(fdlProxyServer);
        wProxyServer = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wProxyServer);
        wProxyServer.setToolTipText(Messages.getString("JobHTTP.ProxyHost.Tooltip"));
        wProxyServer.addModifyListener(lsMod);
        fdProxyServer = new FormData();
        fdProxyServer.left = new FormAttachment(middle, 0);
        fdProxyServer.top = new FormAttachment(wPassword, margin * 5);
        fdProxyServer.right = new FormAttachment(100, 0);
        wProxyServer.setLayoutData(fdProxyServer);

        // ProxyPort line
        wlProxyPort = new Label(shell, SWT.RIGHT);
        wlProxyPort.setText(Messages.getString("JobHTTP.ProxyPort.Label"));
        props.setLook(wlProxyPort);
        fdlProxyPort = new FormData();
        fdlProxyPort.left = new FormAttachment(0, 0);
        fdlProxyPort.top = new FormAttachment(wProxyServer, margin);
        fdlProxyPort.right = new FormAttachment(middle, -margin);
        wlProxyPort.setLayoutData(fdlProxyPort);
        wProxyPort = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wProxyPort);
        wProxyPort.setToolTipText(Messages.getString("JobHTTP.ProxyPort.Tooltip"));
        wProxyPort.addModifyListener(lsMod);
        fdProxyPort = new FormData();
        fdProxyPort.left = new FormAttachment(middle, 0);
        fdProxyPort.top = new FormAttachment(wProxyServer, margin);
        fdProxyPort.right = new FormAttachment(100, 0);
        wProxyPort.setLayoutData(fdProxyPort);

        // IgnoreHosts line
        wlNonProxyHosts = new Label(shell, SWT.RIGHT);
        wlNonProxyHosts.setText(Messages.getString("JobHTTP.ProxyIgnoreRegexp.Label"));
        props.setLook(wlNonProxyHosts);
        fdlNonProxyHosts = new FormData();
        fdlNonProxyHosts.left = new FormAttachment(0, 0);
        fdlNonProxyHosts.top = new FormAttachment(wProxyPort, margin);
        fdlNonProxyHosts.right = new FormAttachment(middle, -margin);
        wlNonProxyHosts.setLayoutData(fdlNonProxyHosts);
        wNonProxyHosts = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wNonProxyHosts);
        wNonProxyHosts.setToolTipText(Messages.getString("JobHTTP.ProxyIgnoreRegexp.Tooltip"));
        wNonProxyHosts.addModifyListener(lsMod);
        fdNonProxyHosts = new FormData();
        fdNonProxyHosts.left = new FormAttachment(middle, 0);
        fdNonProxyHosts.top = new FormAttachment(wProxyPort, margin);
        fdNonProxyHosts.right = new FormAttachment(100, 0);
        wNonProxyHosts.setLayoutData(fdNonProxyHosts);

        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin,
            wNonProxyHosts);

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
        wURL.addSelectionListener(lsDef);
        wTargetFile.addSelectionListener(lsDef);

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
        props.setDialogSize(shell, "JobHTTPDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
    }

    private void setFlags()
    {
        wlURL.setEnabled(!wRunEveryRow.getSelection());
        wURL.setEnabled(!wRunEveryRow.getSelection());
        wlFieldURL.setEnabled(wRunEveryRow.getSelection());
        wFieldURL.setEnabled(wRunEveryRow.getSelection());

        wlTargetExt.setEnabled(wDateTimeAdded.getSelection());
        wTargetExt.setEnabled(wDateTimeAdded.getSelection());
        wlAppend.setEnabled(!wDateTimeAdded.getSelection());
        wAppend.setEnabled(!wDateTimeAdded.getSelection());
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
        wName.selectAll();

        wURL.setText(Const.NVL(jobEntry.getUrl(), ""));
        wRunEveryRow.setSelection(jobEntry.isRunForEveryRow());
        wFieldURL.setText(Const.NVL(jobEntry.getUrlFieldname(), ""));
        wTargetFile.setText(Const.NVL(jobEntry.getTargetFilename(), ""));
        wAppend.setSelection(jobEntry.isFileAppended());
        wDateTimeAdded.setSelection(jobEntry.isDateTimeAdded());
        wTargetExt.setText(Const.NVL(jobEntry.getTargetFilenameExtention(), ""));

        wUploadFile.setText(Const.NVL(jobEntry.getUploadFilename(), ""));

        jobEntry.setDateTimeAdded(wDateTimeAdded.getSelection());
        jobEntry.setTargetFilenameExtention(wTargetExt.getText());

        wUserName.setText(Const.NVL(jobEntry.getUsername(), ""));
        wPassword.setText(Const.NVL(jobEntry.getPassword(), ""));

        wProxyServer.setText(Const.NVL(jobEntry.getProxyHostname(), ""));
        wProxyPort.setText(Const.NVL(jobEntry.getProxyPort(), ""));
        wNonProxyHosts.setText(Const.NVL(jobEntry.getNonProxyHosts(), ""));

        setFlags();
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
        jobEntry.setUrl(wURL.getText());
        jobEntry.setRunForEveryRow(wRunEveryRow.getSelection());
        jobEntry.setUrlFieldname(wFieldURL.getText());
        jobEntry.setTargetFilename(wTargetFile.getText());
        jobEntry.setFileAppended(wAppend.getSelection());

        jobEntry.setDateTimeAdded(wDateTimeAdded.getSelection());
        jobEntry.setTargetFilenameExtention(wTargetExt.getText());

        jobEntry.setUploadFilename(wUploadFile.getText());

        jobEntry.setUsername(wUserName.getText());
        jobEntry.setPassword(wPassword.getText());

        jobEntry.setProxyHostname(wProxyServer.getText());
        jobEntry.setProxyPort(wProxyPort.getText());
        jobEntry.setNonProxyHosts(wNonProxyHosts.getText());

        dispose();
    }

    public String toString()
    {
        return this.getClass().getName();
    }

    public boolean evaluates()
    {
        return true;
    }

    public boolean isUnconditional()
    {
        return false;
    }

}
