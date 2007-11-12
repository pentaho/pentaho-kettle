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

package org.pentaho.di.ui.job.entries.sftpput;

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
import org.pentaho.di.job.entries.sftpput.JobEntrySFTPPUT;
import org.pentaho.di.job.entries.sftpput.Messages;

/**
 * This dialog allows you to edit the SQL job entry settings. (select the connection and the sql script to be executed)
 *  
 * @author Matt
 * @since  19-06-2003
 */
public class JobEntrySFTPPUTDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	private Label        wlServerName;
	private TextVar      wServerName;
	private FormData     fdlServerName, fdServerName;
	
	private Label        wlServerPort;
	private TextVar      wServerPort;
	private FormData     fdlServerPort, fdServerPort;
	
	private Label        wlUserName;
	private TextVar      wUserName;
	private FormData     fdlUserName, fdUserName;
	
	private Label        wlPassword;
	private TextVar      wPassword;
	private FormData     fdlPassword, fdPassword;
	
	private Label        wlScpDirectory;
	private TextVar      wScpDirectory;
	private FormData     fdlScpDirectory, fdScpDirectory;
	
	private Label        wlLocalDirectory;
	private TextVar      wLocalDirectory;
	private FormData     fdlLocalDirectory, fdLocalDirectory;
	
	private Label        wlWildcard;
	private TextVar      wWildcard;
	private FormData     fdlWildcard, fdWildcard;
	
	private Label        wlRemove;
	private Button       wRemove;
	private FormData     fdlRemove, fdRemove;
	
	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntrySFTPPUT jobEntry;
	private Shell       	shell;

	private SelectionAdapter lsDef;

	private boolean changed;
	

    public JobEntrySFTPPUTDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntrySFTPPUT) jobEntryInt;
		if (this.jobEntry.getName() == null) this.jobEntry.setName(Messages.getString("JobSFTPPUT.Title"));
    }

	public JobEntryInterface open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
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

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("JobSFTPPUT.Title"));
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(Messages.getString("JobSFTPPUT.Name.Label"));
 		props.setLook(wlName);
		fdlName=new FormData();
		fdlName.left = new FormAttachment(0, 0);
		fdlName.right= new FormAttachment(middle, -margin);
		fdlName.top  = new FormAttachment(0, margin);
		wlName.setLayoutData(fdlName);
		wName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wName);
		wName.addModifyListener(lsMod);
		fdName=new FormData();
		fdName.left = new FormAttachment(middle, 0);
		fdName.top  = new FormAttachment(0, margin);
		fdName.right= new FormAttachment(100, 0);
		wName.setLayoutData(fdName);

		// ServerName line
		wlServerName=new Label(shell, SWT.RIGHT);
		wlServerName.setText(Messages.getString("JobSFTPPUT.Server.Label"));
 		props.setLook(wlServerName);
		fdlServerName=new FormData();
		fdlServerName.left = new FormAttachment(0, 0);
		fdlServerName.top  = new FormAttachment(wName, margin);
		fdlServerName.right= new FormAttachment(middle, -margin);
		wlServerName.setLayoutData(fdlServerName);
		wServerName=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wServerName);
		wServerName.addModifyListener(lsMod);
		fdServerName=new FormData();
		fdServerName.left = new FormAttachment(middle, 0);
		fdServerName.top  = new FormAttachment(wName, margin);
		fdServerName.right= new FormAttachment(100, 0);
		wServerName.setLayoutData(fdServerName);
		
		// ServerPort line
		wlServerPort=new Label(shell, SWT.RIGHT);
		wlServerPort.setText(Messages.getString("JobSFTPPUT.Port.Label"));
 		props.setLook(wlServerPort);
		fdlServerPort=new FormData();
		fdlServerPort.left = new FormAttachment(0, 0);
		fdlServerPort.top  = new FormAttachment(wServerName, margin);
		fdlServerPort.right= new FormAttachment(middle, -margin);
		wlServerPort.setLayoutData(fdlServerPort);
		wServerPort=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wServerPort);
		wServerPort.setToolTipText(Messages.getString("JobSFTPPUT.Port.Tooltip"));
		wServerPort.addModifyListener(lsMod);
		fdServerPort=new FormData();
		fdServerPort.left = new FormAttachment(middle, 0);
		fdServerPort.top  = new FormAttachment(wServerName, margin);
		fdServerPort.right= new FormAttachment(100, 0);
		wServerPort.setLayoutData(fdServerPort);

		// UserName line
		wlUserName=new Label(shell, SWT.RIGHT);
		wlUserName.setText(Messages.getString("JobSFTPPUT.Username.Label"));
 		props.setLook(wlUserName);
		fdlUserName=new FormData();
		fdlUserName.left = new FormAttachment(0, 0);
		fdlUserName.top  = new FormAttachment(wServerPort, margin);
		fdlUserName.right= new FormAttachment(middle, -margin);
		wlUserName.setLayoutData(fdlUserName);
		wUserName=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wUserName);
		wUserName.setToolTipText(Messages.getString("JobSFTPPUT.Username.Tooltip"));
		wUserName.addModifyListener(lsMod);
		fdUserName=new FormData();
		fdUserName.left = new FormAttachment(middle, 0);
		fdUserName.top  = new FormAttachment(wServerPort, margin);
		fdUserName.right= new FormAttachment(100, 0);
		wUserName.setLayoutData(fdUserName);

		// Password line
		wlPassword=new Label(shell, SWT.RIGHT);
		wlPassword.setText(Messages.getString("JobSFTPPUT.Password.Label"));
 		props.setLook(wlPassword);
		fdlPassword=new FormData();
		fdlPassword.left = new FormAttachment(0, 0);
		fdlPassword.top  = new FormAttachment(wUserName, margin);
		fdlPassword.right= new FormAttachment(middle, -margin);
		wlPassword.setLayoutData(fdlPassword);
		wPassword=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wPassword);
        wPassword.setEchoChar('*');
        wPassword.addModifyListener(lsMod);
		fdPassword=new FormData();
		fdPassword.left = new FormAttachment(middle, 0);
		fdPassword.top  = new FormAttachment(wUserName, margin);
		fdPassword.right= new FormAttachment(100, 0);
		wPassword.setLayoutData(fdPassword);

		// FtpDirectory line
		wlScpDirectory=new Label(shell, SWT.RIGHT);
		wlScpDirectory.setText(Messages.getString("JobSFTPPUT.RemoteDir.Label"));
 		props.setLook(wlScpDirectory);
		fdlScpDirectory=new FormData();
		fdlScpDirectory.left = new FormAttachment(0, 0);
		fdlScpDirectory.top  = new FormAttachment(wPassword, margin);
		fdlScpDirectory.right= new FormAttachment(middle, -margin);
		wlScpDirectory.setLayoutData(fdlScpDirectory);
		wScpDirectory=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wScpDirectory);
 		wScpDirectory.setToolTipText(Messages.getString("JobSFTPPUT.RemoteDir.Tooltip"));
        wScpDirectory.addModifyListener(lsMod);
		fdScpDirectory=new FormData();
		fdScpDirectory.left = new FormAttachment(middle, 0);
		fdScpDirectory.top  = new FormAttachment(wPassword, margin);
		fdScpDirectory.right= new FormAttachment(100, 0);
		wScpDirectory.setLayoutData(fdScpDirectory);

		// LocalDirectory line
		wlLocalDirectory=new Label(shell, SWT.RIGHT);
		wlLocalDirectory.setText(Messages.getString("JobSFTPPUT.LocalDir.Label"));
 		props.setLook(wlLocalDirectory);
		fdlLocalDirectory=new FormData();
		fdlLocalDirectory.left = new FormAttachment(0, 0);
		fdlLocalDirectory.top  = new FormAttachment(wScpDirectory, margin);
		fdlLocalDirectory.right= new FormAttachment(middle, -margin);
		wlLocalDirectory.setLayoutData(fdlLocalDirectory);
		wLocalDirectory=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLocalDirectory);
 		wLocalDirectory.setToolTipText(Messages.getString("JobSFTPPUT.LocalDir.Tooltip"));
		wLocalDirectory.addModifyListener(lsMod);
		fdLocalDirectory=new FormData();
		fdLocalDirectory.left = new FormAttachment(middle, 0);
		fdLocalDirectory.top  = new FormAttachment(wScpDirectory, margin);
		fdLocalDirectory.right= new FormAttachment(100, 0);
		wLocalDirectory.setLayoutData(fdLocalDirectory);

		// Wildcard line
		wlWildcard=new Label(shell, SWT.RIGHT);
		wlWildcard.setText(Messages.getString("JobSFTPPUT.Wildcard.Label"));
 		props.setLook(wlWildcard);
		fdlWildcard=new FormData();
		fdlWildcard.left = new FormAttachment(0, 0);
		fdlWildcard.top  = new FormAttachment(wLocalDirectory, margin);
		fdlWildcard.right= new FormAttachment(middle, -margin);
		wlWildcard.setLayoutData(fdlWildcard);
		wWildcard=new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wWildcard);
 		wWildcard.setToolTipText(Messages.getString("JobSFTPPUT.Wildcard.Tooltip"));
		wWildcard.addModifyListener(lsMod);
		fdWildcard=new FormData();
		fdWildcard.left = new FormAttachment(middle, 0);
		fdWildcard.top  = new FormAttachment(wLocalDirectory, margin);
		fdWildcard.right= new FormAttachment(100, 0);
		wWildcard.setLayoutData(fdWildcard);

		// Remove files after retrieval...
		wlRemove=new Label(shell, SWT.RIGHT);
		wlRemove.setText(Messages.getString("JobSFTPPUT.RemoveFiles.Label"));
 		props.setLook(wlRemove);
		fdlRemove=new FormData();
		fdlRemove.left = new FormAttachment(0, 0);
		fdlRemove.top  = new FormAttachment(wWildcard, margin);
		fdlRemove.right= new FormAttachment(middle, -margin);
		wlRemove.setLayoutData(fdlRemove);
		wRemove=new Button(shell, SWT.CHECK);
 		props.setLook(wRemove);
		fdRemove=new FormData();
		wRemove.setToolTipText(Messages.getString("JobSFTPPUT.RemoveFiles.Tooltip"));
		fdRemove.left = new FormAttachment(middle, 0);
		fdRemove.top  = new FormAttachment(wWildcard, margin);
		fdRemove.right= new FormAttachment(100, 0);
		wRemove.setLayoutData(fdRemove);

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wRemove);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wName.addSelectionListener( lsDef );
        wServerName.addSelectionListener( lsDef );
        wUserName.addSelectionListener( lsDef );
        wPassword.addSelectionListener( lsDef );
        wScpDirectory.addSelectionListener( lsDef );
        wLocalDirectory.addSelectionListener( lsDef );
        wWildcard.addSelectionListener( lsDef );
        			
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
				
		getData();
		
		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
		    if (!display.readAndDispatch()) display.sleep();
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
		if (jobEntry.getName()    != null) wName.setText( jobEntry.getName() );
		wName.selectAll();

		wServerName.setText(Const.NVL(jobEntry.getServerName(), ""));
		wServerPort.setText(jobEntry.getServerPort());
		wUserName.setText(Const.NVL(jobEntry.getUserName(), ""));
		wPassword.setText(Const.NVL(jobEntry.getPassword(), ""));
		wScpDirectory.setText(Const.NVL(jobEntry.getScpDirectory(), ""));
		wLocalDirectory.setText(Const.NVL(jobEntry.getLocalDirectory(), ""));
		wWildcard.setText(Const.NVL(jobEntry.getWildcard(), ""));
		wRemove.setSelection(jobEntry.getRemove());
	}
	
	private void cancel()
	{
		jobEntry.setChanged(changed);
		jobEntry=null;
		dispose();
	}
	
	private void ok()
	{
		jobEntry.setName(wName.getText());
		jobEntry.setServerName(wServerName.getText());
		jobEntry.setServerPort(wServerPort.getText());
		jobEntry.setUserName(wUserName.getText());
		jobEntry.setPassword(wPassword.getText());
		jobEntry.setScpDirectory(wScpDirectory.getText());
		jobEntry.setLocalDirectory(wLocalDirectory.getText());
		jobEntry.setWildcard(wWildcard.getText());
		jobEntry.setRemove(wRemove.getSelection());

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
