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

package org.pentaho.di.ui.job.entries.ftp;

import java.util.ArrayList;

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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.LabelText;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.job.entries.ftp.JobEntryFTP;
import org.pentaho.di.job.entries.ftp.Messages;


/**
 * This dialog allows you to edit the SQL job entry settings. (select the connection and the sql
 * script to be executed)
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class JobEntryFTPDialog extends JobEntryDialog implements JobEntryDialogInterface
{
    private LabelText wName;

    private FormData fdName;

    private LabelTextVar wServerName;

    private FormData fdServerName;

    private LabelTextVar wUserName;

    private FormData fdUserName;

    private LabelTextVar wPassword;

    private FormData fdPassword;

    private LabelTextVar wFtpDirectory;

    private FormData fdFtpDirectory;

    private LabelTextVar wTargetDirectory;

    private FormData fdTargetDirectory;

    private LabelTextVar wWildcard;

    private FormData fdWildcard;

    private Label wlBinaryMode;

    private Button wBinaryMode;

    private FormData fdlBinaryMode, fdBinaryMode;

    private LabelTextVar wTimeout;

    private FormData fdTimeout;

    private Label wlRemove;

    private Button wRemove;

    private FormData fdlRemove, fdRemove;

    private Label wlOnlyNew;

    private Button wOnlyNew;

    private FormData fdlOnlyNew, fdOnlyNew;

    private Label wlActive;

    private Button wActive;

    private FormData fdlActive, fdActive;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntryFTP jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;
    
    private Label        wlControlEncoding;
    
    private Combo        wControlEncoding;
    
    private FormData     fdlControlEncoding, fdControlEncoding;
    
    private boolean changed;        
    
    // These should not be translated, they are required to exist on all
    // platforms according to the documentation of "Charset".
    private static String[] encodings = { "US-ASCII",
    	                                  "ISO-8859-1",
    	                                  "UTF-8",
    	                                  "UTF-16BE",
    	                                  "UTF-16LE",
    	                                  "UTF-16" }; 

    //
    // Original code used to fill encodings, this display all possibilities but
    // takes 10 seconds on my pc to fill.
    //
    // static {
    //     SortedMap charsetMap = Charset.availableCharsets();
    //    Set charsetSet = charsetMap.keySet();
    //    encodings = (String [])charsetSet.toArray(new String[0]);
    // }


    public JobEntryFTPDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryFTP) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(Messages.getString("JobFTP.Name.Default"));
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
        shell.setText(Messages.getString("JobFTP.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Job entry name line
        wName = new LabelText(shell, Messages.getString("JobFTP.Name.Label"), Messages.getString("JobFTP.Name.Tooltip"));
        wName.addModifyListener(lsMod);
        fdName = new FormData();
        fdName.top = new FormAttachment(0, 0);
        fdName.left = new FormAttachment(0, 0);
        fdName.right = new FormAttachment(100, 0);
        wName.setLayoutData(fdName);

        // ServerName line
        wServerName = new LabelTextVar(jobMeta, shell, Messages.getString("JobFTP.Server.Label"), Messages.getString("JobFTP.Server.Tooltip"));
        props.setLook(wServerName);
        wServerName.addModifyListener(lsMod);
        fdServerName = new FormData();
        fdServerName.left = new FormAttachment(0, 0);
        fdServerName.top = new FormAttachment(wName, margin);
        fdServerName.right = new FormAttachment(100, 0);
        wServerName.setLayoutData(fdServerName);

        // UserName line
        wUserName = new LabelTextVar(jobMeta, shell, Messages.getString("JobFTP.User.Label"), Messages.getString("JobFTP.User.Tooltip"));
        props.setLook(wUserName);
        wUserName.addModifyListener(lsMod);
        fdUserName = new FormData();
        fdUserName.left = new FormAttachment(0, 0);
        fdUserName.top = new FormAttachment(wServerName, margin);
        fdUserName.right = new FormAttachment(100, 0);
        wUserName.setLayoutData(fdUserName);

        // Password line
        wPassword = new LabelTextVar(jobMeta, shell, Messages.getString("JobFTP.Password.Label"), Messages.getString("JobFTP.Password.Tooltip"));
        props.setLook(wPassword);
        wPassword.setEchoChar('*');
        wPassword.addModifyListener(lsMod);
        fdPassword = new FormData();
        fdPassword.left = new FormAttachment(0, 0);
        fdPassword.top = new FormAttachment(wUserName, margin);
        fdPassword.right = new FormAttachment(100, 0);
        wPassword.setLayoutData(fdPassword);

        // OK, if the password contains a variable, we don't want to have the password hidden...
        wPassword.getTextWidget().addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                checkPasswordVisible();
            }
        });

        // FtpDirectory line
        wFtpDirectory = new LabelTextVar(jobMeta, shell, Messages.getString("JobFTP.RemoteDir.Label"),
            Messages.getString("JobFTP.RemoteDir.Tooltip"));
        props.setLook(wFtpDirectory);
        wFtpDirectory.addModifyListener(lsMod);
        fdFtpDirectory = new FormData();
        fdFtpDirectory.left = new FormAttachment(0, 0);
        fdFtpDirectory.top = new FormAttachment(wPassword, margin);
        fdFtpDirectory.right = new FormAttachment(100, 0);
        wFtpDirectory.setLayoutData(fdFtpDirectory);

        // TargetDirectory line
        wTargetDirectory = new LabelTextVar(jobMeta, shell, Messages.getString("JobFTP.TargetDir.Label"),
            Messages.getString("JobFTP.TargetDir.Tooltip"));
        props.setLook(wTargetDirectory);
        wTargetDirectory.addModifyListener(lsMod);
        fdTargetDirectory = new FormData();
        fdTargetDirectory.left = new FormAttachment(0, 0);
        fdTargetDirectory.top = new FormAttachment(wFtpDirectory, margin);
        fdTargetDirectory.right = new FormAttachment(100, 0);
        wTargetDirectory.setLayoutData(fdTargetDirectory);

        // Wildcard line
        wWildcard = new LabelTextVar(jobMeta, shell, Messages.getString("JobFTP.Wildcard.Label"), Messages.getString("JobFTP.Wildcard.Tooltip"));
        props.setLook(wWildcard);
        wWildcard.addModifyListener(lsMod);
        fdWildcard = new FormData();
        fdWildcard.left = new FormAttachment(0, 0);
        fdWildcard.top = new FormAttachment(wTargetDirectory, margin);
        fdWildcard.right = new FormAttachment(100, 0);
        wWildcard.setLayoutData(fdWildcard);

        // Binary mode selection...
        wlBinaryMode = new Label(shell, SWT.RIGHT);
        wlBinaryMode.setText(Messages.getString("JobFTP.BinaryMode.Label"));
        props.setLook(wlBinaryMode);
        fdlBinaryMode = new FormData();
        fdlBinaryMode.left = new FormAttachment(0, 0);
        fdlBinaryMode.top = new FormAttachment(wWildcard, margin);
        fdlBinaryMode.right = new FormAttachment(middle, 0);
        wlBinaryMode.setLayoutData(fdlBinaryMode);
        wBinaryMode = new Button(shell, SWT.CHECK);
        props.setLook(wBinaryMode);
        wBinaryMode.setToolTipText(Messages.getString("JobFTP.BinaryMode.Tooltip"));
        fdBinaryMode = new FormData();
        fdBinaryMode.left = new FormAttachment(middle, margin);
        fdBinaryMode.top = new FormAttachment(wWildcard, margin);
        fdBinaryMode.right = new FormAttachment(100, 0);
        wBinaryMode.setLayoutData(fdBinaryMode);

        // Timeout line
        wTimeout = new LabelTextVar(jobMeta, shell, Messages.getString("JobFTP.Timeout.Label"), Messages.getString("JobFTP.Timeout.Tooltip"));
        props.setLook(wTimeout);
        wTimeout.addModifyListener(lsMod);
        fdTimeout = new FormData();
        fdTimeout.left = new FormAttachment(0, 0);
        fdTimeout.top = new FormAttachment(wlBinaryMode, margin);
        fdTimeout.right = new FormAttachment(100, 0);
        wTimeout.setLayoutData(fdTimeout);

        // Remove files after retrieval...
        wlRemove = new Label(shell, SWT.RIGHT);
        wlRemove.setText(Messages.getString("JobFTP.RemoveFiles.Label"));
        props.setLook(wlRemove);
        fdlRemove = new FormData();
        fdlRemove.left = new FormAttachment(0, 0);
        fdlRemove.top = new FormAttachment(wTimeout, margin);
        fdlRemove.right = new FormAttachment(middle, 0);
        wlRemove.setLayoutData(fdlRemove);
        wRemove = new Button(shell, SWT.CHECK);
        wRemove.setToolTipText(Messages.getString("JobFTP.RemoveFiles.Tooltip"));
        props.setLook(wRemove);
        fdRemove = new FormData();
        fdRemove.left = new FormAttachment(middle, margin);
        fdRemove.top = new FormAttachment(wTimeout, margin);
        fdRemove.right = new FormAttachment(100, 0);
        wRemove.setLayoutData(fdRemove);

        // OnlyNew files after retrieval...
        wlOnlyNew = new Label(shell, SWT.RIGHT);
        wlOnlyNew.setText(Messages.getString("JobFTP.DontOverwrite.Label"));
        props.setLook(wlOnlyNew);
        fdlOnlyNew = new FormData();
        fdlOnlyNew.left = new FormAttachment(0, 0);
        fdlOnlyNew.top = new FormAttachment(wRemove, margin);
        fdlOnlyNew.right = new FormAttachment(middle, 0);
        wlOnlyNew.setLayoutData(fdlOnlyNew);
        wOnlyNew = new Button(shell, SWT.CHECK);
        wOnlyNew.setToolTipText(Messages.getString("JobFTP.DontOverwrite.Tooltip"));
        props.setLook(wOnlyNew);
        fdOnlyNew = new FormData();
        fdOnlyNew.left = new FormAttachment(middle, margin);
        fdOnlyNew.top = new FormAttachment(wRemove, margin);
        fdOnlyNew.right = new FormAttachment(100, 0);
        wOnlyNew.setLayoutData(fdOnlyNew);

        // active connection?
        wlActive = new Label(shell, SWT.RIGHT);
        wlActive.setText(Messages.getString("JobFTP.ActiveConns.Label"));
        props.setLook(wlActive);
        fdlActive = new FormData();
        fdlActive.left = new FormAttachment(0, 0);
        fdlActive.top = new FormAttachment(wOnlyNew, margin);
        fdlActive.right = new FormAttachment(middle, 0);
        wlActive.setLayoutData(fdlActive);
        wActive = new Button(shell, SWT.CHECK);
        wActive.setToolTipText(Messages.getString("JobFTP.ActiveConns.Tooltip"));
        props.setLook(wActive);
        fdActive = new FormData();
        fdActive.left = new FormAttachment(middle, margin);
        fdActive.top = new FormAttachment(wOnlyNew, margin);
        fdActive.right = new FormAttachment(100, 0);
        wActive.setLayoutData(fdActive);
        
        // Control encoding line
        //
        // The drop down is editable as it may happen an encoding may not be present
        // on one machine, but you may want to use it on your execution server
        //
        wlControlEncoding=new Label(shell, SWT.RIGHT);
        wlControlEncoding.setText(Messages.getString("JobFTP.ControlEncoding.Label"));
        props.setLook(wlControlEncoding);
        fdlControlEncoding=new FormData();
        fdlControlEncoding.left  = new FormAttachment(0, 0);
        fdlControlEncoding.top   = new FormAttachment(wActive, margin);
        fdlControlEncoding.right = new FormAttachment(middle, 0);
        wlControlEncoding.setLayoutData(fdlControlEncoding);
        wControlEncoding=new Combo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wControlEncoding.setToolTipText(Messages.getString("JobFTP.ControlEncoding.Tooltip"));
        wControlEncoding.setItems(encodings);
        props.setLook(wControlEncoding);
        fdControlEncoding=new FormData();
        fdControlEncoding.left = new FormAttachment(middle, margin);
        fdControlEncoding.top  = new FormAttachment(wActive, margin);
        fdControlEncoding.right= new FormAttachment(100, 0);        
        wControlEncoding.setLayoutData(fdControlEncoding);       

        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wControlEncoding);

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
        wServerName.addSelectionListener(lsDef);
        wUserName.addSelectionListener(lsDef);
        wPassword.addSelectionListener(lsDef);
        wFtpDirectory.addSelectionListener(lsDef);
        wTargetDirectory.addSelectionListener(lsDef);
        wFtpDirectory.addSelectionListener(lsDef);
        wWildcard.addSelectionListener(lsDef);
        wTimeout.addSelectionListener(lsDef);

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
        props.setDialogSize(shell, "JobFTPDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
    }

    public void checkPasswordVisible()
    {
        String password = wPassword.getText();
        java.util.List<String> list = new ArrayList<String>();
        StringUtil.getUsedVariables(password, list, true);
        if (list.isEmpty())
        {
            wPassword.setEchoChar('*');
        }
        else
        {
            wPassword.setEchoChar('\0'); // Show it all...
        }
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
        wName.getTextWidget().selectAll();

        wServerName.setText(Const.NVL(jobEntry.getServerName(), ""));
        wUserName.setText(Const.NVL(jobEntry.getUserName(), ""));
        wPassword.setText(Const.NVL(jobEntry.getPassword(), ""));
        wFtpDirectory.setText(Const.NVL(jobEntry.getFtpDirectory(), ""));
        wTargetDirectory.setText(Const.NVL(jobEntry.getTargetDirectory(), ""));
        wWildcard.setText(Const.NVL(jobEntry.getWildcard(), ""));
        wBinaryMode.setSelection(jobEntry.isBinaryMode());
        wTimeout.setText("" + jobEntry.getTimeout());
        wRemove.setSelection(jobEntry.getRemove());
        wOnlyNew.setSelection(jobEntry.isOnlyGettingNewFiles());
        wActive.setSelection(jobEntry.isActiveConnection());
        wControlEncoding.setText(jobEntry.getControlEncoding());
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
        jobEntry.setServerName(wServerName.getText());
        jobEntry.setUserName(wUserName.getText());
        jobEntry.setPassword(wPassword.getText());
        jobEntry.setFtpDirectory(wFtpDirectory.getText());
        jobEntry.setTargetDirectory(wTargetDirectory.getText());
        jobEntry.setWildcard(wWildcard.getText());
        jobEntry.setBinaryMode(wBinaryMode.getSelection());
        jobEntry.setTimeout(Const.toInt(wTimeout.getText(), 10000));
        jobEntry.setRemove(wRemove.getSelection());
        jobEntry.setOnlyGettingNewFiles(wOnlyNew.getSelection());
        jobEntry.setActiveConnection(wActive.getSelection());
        jobEntry.setControlEncoding(wControlEncoding.getText());

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
