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

package org.pentaho.di.ui.job.entries.ftpput;

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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.core.Const;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.job.entries.ftpput.JobEntryFTPPUT;
import org.pentaho.di.job.entries.ftpput.Messages;



/**
 * This dialog allows you to edit the FTP Put job entry settings
 * 
 * @author Samatar
 * @since 15-09-2007
 */
public class JobEntryFTPPUTDialog extends JobEntryDialog implements JobEntryDialogInterface
{
    private Label wlName;

    private Text wName;

    private FormData fdlName, fdName;

    private Label wlServerName;

    private TextVar wServerName;

    private FormData fdlServerName, fdServerName;

    private Label wlServerPort;

    private TextVar wServerPort;

    private FormData fdlServerPort, fdServerPort;

    private Label wlUserName;

    private TextVar wUserName;

    private FormData fdlUserName, fdUserName;

    private Label wlPassword;

    private TextVar wPassword;

    private FormData fdlPassword, fdPassword;

    private Label wlLocalDirectory;

    private TextVar wLocalDirectory;

    private FormData fdlLocalDirectory, fdLocalDirectory;

    private Label wlRemoteDirectory;

    private TextVar wRemoteDirectory;

    private FormData fdlRemoteDirectory, fdRemoteDirectory;

    private Label wlWildcard;

    private TextVar wWildcard;

    private FormData fdlWildcard, fdWildcard;

    private Label wlRemove;

    private Button wRemove;

    private FormData fdlRemove, fdRemove;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntryFTPPUT jobEntry;

    private Shell shell;


    private SelectionAdapter lsDef;

    private boolean changed;
    
    private Label wlBinaryMode;

    private Button wBinaryMode;

    private FormData fdlBinaryMode, fdBinaryMode;

    private LabelTextVar wTimeout;

    private FormData fdTimeout;
    
    private Label wlOnlyNew;

    private Button wOnlyNew;

    private FormData fdlOnlyNew, fdOnlyNew;

    private Label wlActive;

    private Button wActive;

    private FormData fdlActive, fdActive;
    
    private Label        wlControlEncoding;
    
    private Combo        wControlEncoding;
    
    private FormData     fdlControlEncoding, fdControlEncoding;
    
    // These should not be translated, they are required to exist on all
    // platforms according to the documentation of "Charset".
    private static String[] encodings = { "US-ASCII",
    	                                  "ISO-8859-1",
    	                                  "UTF-8",
    	                                  "UTF-16BE",
    	                                  "UTF-16LE",
    	                                  "UTF-16" }; 
    
    private Button wbLocalDirectory;
    private FormData fdbLocalDirectory;


    
    public JobEntryFTPPUTDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryFTPPUT) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(Messages.getString("JobFTPPUT.Name.Default"));
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
        shell.setText(Messages.getString("JobFTPPUT.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Filename line
        wlName = new Label(shell, SWT.RIGHT);
        wlName.setText(Messages.getString("JobFTPPUT.Name.Label"));
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

        // ServerName line
        wlServerName = new Label(shell, SWT.RIGHT);
        wlServerName.setText(Messages.getString("JobFTPPUT.Server.Label"));
        props.setLook(wlServerName);
        fdlServerName = new FormData();
        fdlServerName.left = new FormAttachment(0, 0);
        fdlServerName.top = new FormAttachment(wName, margin);
        fdlServerName.right = new FormAttachment(middle, -margin);
        wlServerName.setLayoutData(fdlServerName);
        wServerName = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wServerName);
        wServerName.addModifyListener(lsMod);
        fdServerName = new FormData();
        fdServerName.left = new FormAttachment(middle, 0);
        fdServerName.top = new FormAttachment(wName, margin);
        fdServerName.right = new FormAttachment(100, 0);
        wServerName.setLayoutData(fdServerName);

        // ServerPort line
        wlServerPort = new Label(shell, SWT.RIGHT);
        wlServerPort.setText(Messages.getString("JobFTPPUT.Port.Label"));
        props.setLook(wlServerPort);
        fdlServerPort = new FormData();
        fdlServerPort.left = new FormAttachment(0, 0);
        fdlServerPort.top = new FormAttachment(wServerName, margin);
        fdlServerPort.right = new FormAttachment(middle, -margin);
        wlServerPort.setLayoutData(fdlServerPort);
        wServerPort = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wServerPort);
        wServerPort.setToolTipText(Messages.getString("JobFTPPUT.Port.Tooltip"));
        wServerPort.addModifyListener(lsMod);
        fdServerPort = new FormData();
        fdServerPort.left = new FormAttachment(middle, 0);
        fdServerPort.top = new FormAttachment(wServerName, margin);
        fdServerPort.right = new FormAttachment(100, 0);
        wServerPort.setLayoutData(fdServerPort);

        // UserName line
        wlUserName = new Label(shell, SWT.RIGHT);
        wlUserName.setText(Messages.getString("JobFTPPUT.Username.Label"));
        props.setLook(wlUserName);
        fdlUserName = new FormData();
        fdlUserName.left = new FormAttachment(0, 0);
        fdlUserName.top = new FormAttachment(wServerPort, margin);
        fdlUserName.right = new FormAttachment(middle, -margin);
        wlUserName.setLayoutData(fdlUserName);
        wUserName = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wUserName);
        wUserName.addModifyListener(lsMod);
        fdUserName = new FormData();
        fdUserName.left = new FormAttachment(middle, 0);
        fdUserName.top = new FormAttachment(wServerPort, margin);
        fdUserName.right = new FormAttachment(100, 0);
        wUserName.setLayoutData(fdUserName);

        // Password line
        wlPassword = new Label(shell, SWT.RIGHT);
        wlPassword.setText(Messages.getString("JobFTPPUT.Password.Label"));
        props.setLook(wlPassword);
        fdlPassword = new FormData();
        fdlPassword.left = new FormAttachment(0, 0);
        fdlPassword.top = new FormAttachment(wUserName, margin);
        fdlPassword.right = new FormAttachment(middle, -margin);
        wlPassword.setLayoutData(fdlPassword);
        wPassword = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wPassword);
        wPassword.setEchoChar('*');
        wPassword.addModifyListener(lsMod);
        fdPassword = new FormData();
        fdPassword.left = new FormAttachment(middle, 0);
        fdPassword.top = new FormAttachment(wUserName, margin);
        fdPassword.right = new FormAttachment(100, 0);
        wPassword.setLayoutData(fdPassword);


        // Local (source) directory line
        wlLocalDirectory = new Label(shell, SWT.RIGHT);
        wlLocalDirectory.setText(Messages.getString("JobFTPPUT.LocalDir.Label"));
        props.setLook(wlLocalDirectory);
        fdlLocalDirectory = new FormData();
        fdlLocalDirectory.left = new FormAttachment(0, 0);
        fdlLocalDirectory.top = new FormAttachment(wPassword, margin);
        fdlLocalDirectory.right = new FormAttachment(middle, -margin);
        wlLocalDirectory.setLayoutData(fdlLocalDirectory);
        
        // Browse folders button ...
		wbLocalDirectory=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbLocalDirectory);
		wbLocalDirectory.setText(Messages.getString("JobFTPPUT.BrowseFolders.Label"));
		fdbLocalDirectory=new FormData();
		fdbLocalDirectory.right= new FormAttachment(100, 0);
		fdbLocalDirectory.top  = new FormAttachment(wPassword, margin);
		wbLocalDirectory.setLayoutData(fdbLocalDirectory);
        
        wLocalDirectory = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages
            .getString("JobFTPPUT.LocalDir.Tooltip"));
        props.setLook(wLocalDirectory);
        wLocalDirectory.addModifyListener(lsMod);
        fdLocalDirectory = new FormData();
        fdLocalDirectory.left = new FormAttachment(middle, 0);
        fdLocalDirectory.top = new FormAttachment(wPassword, margin);
        fdLocalDirectory.right = new FormAttachment(wbLocalDirectory, -margin);
        wLocalDirectory.setLayoutData(fdLocalDirectory);

        // Remote Directory line
        wlRemoteDirectory = new Label(shell, SWT.RIGHT);
        wlRemoteDirectory.setText(Messages.getString("JobFTPPUT.RemoteDir.Label"));
        props.setLook(wlRemoteDirectory);
        fdlRemoteDirectory = new FormData();
        fdlRemoteDirectory.left = new FormAttachment(0, 0);
        fdlRemoteDirectory.top = new FormAttachment(wLocalDirectory, margin);
        fdlRemoteDirectory.right = new FormAttachment(middle, -margin);
        wlRemoteDirectory.setLayoutData(fdlRemoteDirectory);
        wRemoteDirectory = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages
            .getString("JobFTPPUT.RemoteDir.Tooltip"));
        props.setLook(wRemoteDirectory);
        wRemoteDirectory.addModifyListener(lsMod);
        fdRemoteDirectory = new FormData();
        fdRemoteDirectory.left = new FormAttachment(middle, 0);
        fdRemoteDirectory.top = new FormAttachment(wLocalDirectory, margin);
        fdRemoteDirectory.right = new FormAttachment(100, 0);
        wRemoteDirectory.setLayoutData(fdRemoteDirectory);
        
        // Wildcard line
        wlWildcard = new Label(shell, SWT.RIGHT);
        wlWildcard.setText(Messages.getString("JobFTPPUT.Wildcard.Label"));
        props.setLook(wlWildcard);
        fdlWildcard = new FormData();
        fdlWildcard.left = new FormAttachment(0, 0);
        fdlWildcard.top = new FormAttachment(wRemoteDirectory, margin);
        fdlWildcard.right = new FormAttachment(middle, -margin);
        wlWildcard.setLayoutData(fdlWildcard);
        wWildcard = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages.getString("JobFTPPUT.Wildcard.Tooltip"));
        props.setLook(wWildcard);
        wWildcard.addModifyListener(lsMod);
        fdWildcard = new FormData();
        fdWildcard.left = new FormAttachment(middle, 0);
        fdWildcard.top = new FormAttachment(wRemoteDirectory, margin);
        fdWildcard.right = new FormAttachment(100, 0);
        wWildcard.setLayoutData(fdWildcard);
        
        // Binary mode selection...
        wlBinaryMode = new Label(shell, SWT.RIGHT);
        wlBinaryMode.setText(Messages.getString("JobFTPPUT.BinaryMode.Label"));
        props.setLook(wlBinaryMode);
        fdlBinaryMode = new FormData();
        fdlBinaryMode.left = new FormAttachment(0, 0);
        fdlBinaryMode.top = new FormAttachment(wWildcard, margin);
        fdlBinaryMode.right = new FormAttachment(middle, 0);
        wlBinaryMode.setLayoutData(fdlBinaryMode);
        wBinaryMode = new Button(shell, SWT.CHECK);
        props.setLook(wBinaryMode);
        wBinaryMode.setToolTipText(Messages.getString("JobFTPPUT.BinaryMode.Tooltip"));
        fdBinaryMode = new FormData();
        fdBinaryMode.left = new FormAttachment(middle, 0);
        fdBinaryMode.top = new FormAttachment(wWildcard, margin);
        fdBinaryMode.right = new FormAttachment(100, 0);
        wBinaryMode.setLayoutData(fdBinaryMode);

        // Timeout line
        wTimeout = new LabelTextVar(jobMeta, shell, Messages.getString("JobFTPPUT.Timeout.Label"), Messages.getString("JobFTPPUT.Timeout.Tooltip"));
        props.setLook(wTimeout);
        wTimeout.addModifyListener(lsMod);
        fdTimeout = new FormData();
        fdTimeout.left = new FormAttachment(0, 0);
        fdTimeout.top = new FormAttachment(wlBinaryMode, margin);
        fdTimeout.right = new FormAttachment(100, 0);
        wTimeout.setLayoutData(fdTimeout);


        // Remove files after retrieval...
        wlRemove = new Label(shell, SWT.RIGHT);
        wlRemove.setText(Messages.getString("JobFTPPUT.RemoveFiles.Label"));
        props.setLook(wlRemove);
        fdlRemove = new FormData();
        fdlRemove.left = new FormAttachment(0, 0);
        fdlRemove.top = new FormAttachment(wTimeout, margin);
        fdlRemove.right = new FormAttachment(middle, -margin);
        wlRemove.setLayoutData(fdlRemove);
        wRemove = new Button(shell, SWT.CHECK);
        props.setLook(wRemove);
        wRemove.setToolTipText(Messages.getString("JobFTPPUT.RemoveFiles.Tooltip"));
        fdRemove = new FormData();
        fdRemove.left = new FormAttachment(middle, 0);
        fdRemove.top = new FormAttachment(wTimeout, margin);
        fdRemove.right = new FormAttachment(100, 0);
        wRemove.setLayoutData(fdRemove);
        
        // OnlyNew files after retrieval...
        wlOnlyNew = new Label(shell, SWT.RIGHT);
        wlOnlyNew.setText(Messages.getString("JobFTPPUT.DontOverwrite.Label"));
        props.setLook(wlOnlyNew);
        fdlOnlyNew = new FormData();
        fdlOnlyNew.left = new FormAttachment(0, 0);
        fdlOnlyNew.top = new FormAttachment(wRemove, margin);
        fdlOnlyNew.right = new FormAttachment(middle, 0);
        wlOnlyNew.setLayoutData(fdlOnlyNew);
        wOnlyNew = new Button(shell, SWT.CHECK);
        wOnlyNew.setToolTipText(Messages.getString("JobFTPPUT.DontOverwrite.Tooltip"));
        props.setLook(wOnlyNew);
        fdOnlyNew = new FormData();
        fdOnlyNew.left = new FormAttachment(middle, 0);
        fdOnlyNew.top = new FormAttachment(wRemove, margin);
        fdOnlyNew.right = new FormAttachment(100, 0);
        wOnlyNew.setLayoutData(fdOnlyNew);

        // active connection?
        wlActive = new Label(shell, SWT.RIGHT);
        wlActive.setText(Messages.getString("JobFTPPUT.ActiveConns.Label"));
        props.setLook(wlActive);
        fdlActive = new FormData();
        fdlActive.left = new FormAttachment(0, 0);
        fdlActive.top = new FormAttachment(wOnlyNew, margin);
        fdlActive.right = new FormAttachment(middle, 0);
        wlActive.setLayoutData(fdlActive);
        wActive = new Button(shell, SWT.CHECK);
        wActive.setToolTipText(Messages.getString("JobFTPPUT.ActiveConns.Tooltip"));
        props.setLook(wActive);
        fdActive = new FormData();
        fdActive.left = new FormAttachment(middle, 0);
        fdActive.top = new FormAttachment(wOnlyNew, margin);
        fdActive.right = new FormAttachment(100, 0);
        wActive.setLayoutData(fdActive);
        
        // Control encoding line
        //
        // The drop down is editable as it may happen an encoding may not be present
        // on one machine, but you may want to use it on your execution server
        //
        wlControlEncoding=new Label(shell, SWT.RIGHT);
        wlControlEncoding.setText(Messages.getString("JobFTPPUT.ControlEncoding.Label"));
        props.setLook(wlControlEncoding);
        fdlControlEncoding=new FormData();
        fdlControlEncoding.left  = new FormAttachment(0, 0);
        fdlControlEncoding.top   = new FormAttachment(wActive, margin);
        fdlControlEncoding.right = new FormAttachment(middle, 0);
        wlControlEncoding.setLayoutData(fdlControlEncoding);
        wControlEncoding=new Combo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wControlEncoding.setToolTipText(Messages.getString("JobFTPPUT.ControlEncoding.Tooltip"));
        wControlEncoding.setItems(encodings);
        props.setLook(wControlEncoding);
        fdControlEncoding=new FormData();
        fdControlEncoding.left = new FormAttachment(middle, 0);
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
        
        wbLocalDirectory.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					DirectoryDialog ddialog = new DirectoryDialog(shell, SWT.OPEN);
					if (wLocalDirectory.getText()!=null)
					{
						ddialog.setFilterPath(jobMeta.environmentSubstitute(wLocalDirectory.getText()) );
					}
					
					 // Calling open() will open and run the dialog.
			        // It will return the selected directory, or
			        // null if user cancels
			        String dir = ddialog.open();
			        if (dir != null) {
			          // Set the text box to the new selection
			        	wLocalDirectory.setText(dir);
			        }
					
				}
			}
		);
		

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
        wRemoteDirectory.addSelectionListener(lsDef);
        wLocalDirectory.addSelectionListener(lsDef);
        wWildcard.addSelectionListener(lsDef);

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
        props.setDialogSize(shell, "JobSFTPDialogSize");
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
        wName.selectAll();

        wServerName.setText(Const.NVL(jobEntry.getServerName(), ""));
        wServerPort.setText(jobEntry.getServerPort());
        wUserName.setText(Const.NVL(jobEntry.getUserName(), ""));
        wPassword.setText(Const.NVL(jobEntry.getPassword(), ""));
        wRemoteDirectory.setText(Const.NVL(jobEntry.getRemoteDirectory(), ""));
        wLocalDirectory.setText(Const.NVL(jobEntry.getLocalDirectory(), ""));
        wWildcard.setText(Const.NVL(jobEntry.getWildcard(), ""));
        wRemove.setSelection(jobEntry.getRemove());
        wBinaryMode.setSelection(jobEntry.isBinaryMode());
        wTimeout.setText("" + jobEntry.getTimeout());
        wOnlyNew.setSelection(jobEntry.isOnlyPuttingNewFiles());
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
        jobEntry.setServerPort(wServerPort.getText());
        jobEntry.setUserName(wUserName.getText());
        jobEntry.setPassword(wPassword.getText());
        jobEntry.setRemoteDirectory(wRemoteDirectory.getText());
        jobEntry.setLocalDirectory(wLocalDirectory.getText());
        jobEntry.setWildcard(wWildcard.getText());
        jobEntry.setRemove(wRemove.getSelection());
        jobEntry.setBinaryMode(wBinaryMode.getSelection());
        jobEntry.setTimeout(Const.toInt(wTimeout.getText(), 10000));
        jobEntry.setOnlyPuttingNewFiles(wOnlyNew.getSelection());
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
