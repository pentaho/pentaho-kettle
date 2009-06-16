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

import java.net.InetAddress;

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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.sftp.SFTPClient;
import org.pentaho.di.job.entries.sftpput.JobEntrySFTPPUT;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog allows you to edit the FTP Put job entry settings. 
 * @author Matt
 * @since  19-06-2003
 */
public class JobEntrySFTPPUTDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntrySFTPPUT.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

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
	
    private Group wServerSettings;
    
    private FormData fdServerSettings;
    
    private Group wSourceFiles;
    
    private FormData fdSourceFiles;
    
    private Group wTargetFiles;
    
    private FormData fdTargetFiles;
    
    private Button wbLocalDirectory;
    
    private FormData fdbLocalDirectory;  

	private boolean changed;
	
	private Button wTest;
	
	private FormData fdTest;
	
	private Listener lsTest;
	
	private Listener lsCheckChangeFolder;
	
	private Button wbTestChangeFolderExists;
	
	private FormData fdbTestChangeFolderExists;
	
    private Label wlgetPrevious;

    private Button wgetPrevious;

    private FormData fdlgetPrevious, fdgetPrevious;
    
	
    private Label wlAddFilenameToResult;

    private Button wAddFilenameToResult;
    
    private FormData fdlAddFilenameToResult,fdAddFilenameToResult;
	
	private SFTPClient sftpclient = null;
	

    public JobEntrySFTPPUTDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntrySFTPPUT) jobEntryInt;
		if (this.jobEntry.getName() == null) this.jobEntry.setName(BaseMessages.getString(PKG, "JobSFTPPUT.Title"));
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
				sftpclient=null;
				jobEntry.setChanged();
			}
		};
		changed = jobEntry.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "JobSFTPPUT.Title"));
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(BaseMessages.getString(PKG, "JobSFTPPUT.Name.Label"));
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
		
		 // ////////////////////////
	     // START OF SERVER SETTINGS GROUP///
	     // /
	    wServerSettings = new Group(shell, SWT.SHADOW_NONE);
	    props.setLook(wServerSettings);
	    wServerSettings.setText(BaseMessages.getString(PKG, "JobSFTPPUT.ServerSettings.Group.Label"));
	    FormLayout ServerSettingsgroupLayout = new FormLayout();
	    ServerSettingsgroupLayout.marginWidth = 10;
	    ServerSettingsgroupLayout.marginHeight = 10;
	    wServerSettings.setLayout(ServerSettingsgroupLayout);

		// ServerName line
		wlServerName=new Label(wServerSettings, SWT.RIGHT);
		wlServerName.setText(BaseMessages.getString(PKG, "JobSFTPPUT.Server.Label"));
 		props.setLook(wlServerName);
		fdlServerName=new FormData();
		fdlServerName.left = new FormAttachment(0, 0);
		fdlServerName.top  = new FormAttachment(wName, margin);
		fdlServerName.right= new FormAttachment(middle, -margin);
		wlServerName.setLayoutData(fdlServerName);
		wServerName=new TextVar(jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wServerName);
		wServerName.addModifyListener(lsMod);
		fdServerName=new FormData();
		fdServerName.left = new FormAttachment(middle, 0);
		fdServerName.top  = new FormAttachment(wName, margin);
		fdServerName.right= new FormAttachment(100, 0);
		wServerName.setLayoutData(fdServerName);
		
		// ServerPort line
		wlServerPort=new Label(wServerSettings, SWT.RIGHT);
		wlServerPort.setText(BaseMessages.getString(PKG, "JobSFTPPUT.Port.Label"));
 		props.setLook(wlServerPort);
		fdlServerPort=new FormData();
		fdlServerPort.left = new FormAttachment(0, 0);
		fdlServerPort.top  = new FormAttachment(wServerName, margin);
		fdlServerPort.right= new FormAttachment(middle, -margin);
		wlServerPort.setLayoutData(fdlServerPort);
		wServerPort=new TextVar(jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wServerPort);
		wServerPort.setToolTipText(BaseMessages.getString(PKG, "JobSFTPPUT.Port.Tooltip"));
		wServerPort.addModifyListener(lsMod);
		fdServerPort=new FormData();
		fdServerPort.left = new FormAttachment(middle, 0);
		fdServerPort.top  = new FormAttachment(wServerName, margin);
		fdServerPort.right= new FormAttachment(100, 0);
		wServerPort.setLayoutData(fdServerPort);

		// UserName line
		wlUserName=new Label(wServerSettings, SWT.RIGHT);
		wlUserName.setText(BaseMessages.getString(PKG, "JobSFTPPUT.Username.Label"));
 		props.setLook(wlUserName);
		fdlUserName=new FormData();
		fdlUserName.left = new FormAttachment(0, 0);
		fdlUserName.top  = new FormAttachment(wServerPort, margin);
		fdlUserName.right= new FormAttachment(middle, -margin);
		wlUserName.setLayoutData(fdlUserName);
		wUserName=new TextVar(jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wUserName);
		wUserName.setToolTipText(BaseMessages.getString(PKG, "JobSFTPPUT.Username.Tooltip"));
		wUserName.addModifyListener(lsMod);
		fdUserName=new FormData();
		fdUserName.left = new FormAttachment(middle, 0);
		fdUserName.top  = new FormAttachment(wServerPort, margin);
		fdUserName.right= new FormAttachment(100, 0);
		wUserName.setLayoutData(fdUserName);

		// Password line
		wlPassword=new Label(wServerSettings, SWT.RIGHT);
		wlPassword.setText(BaseMessages.getString(PKG, "JobSFTPPUT.Password.Label"));
 		props.setLook(wlPassword);
		fdlPassword=new FormData();
		fdlPassword.left = new FormAttachment(0, 0);
		fdlPassword.top  = new FormAttachment(wUserName, margin);
		fdlPassword.right= new FormAttachment(middle, -margin);
		wlPassword.setLayoutData(fdlPassword);
		wPassword=new TextVar(jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wPassword);
        wPassword.setEchoChar('*');
        wPassword.addModifyListener(lsMod);
		fdPassword=new FormData();
		fdPassword.left = new FormAttachment(middle, 0);
		fdPassword.top  = new FormAttachment(wUserName, margin);
		fdPassword.right= new FormAttachment(100, 0);
		wPassword.setLayoutData(fdPassword);
		
		// Test connection button
		wTest=new Button(wServerSettings,SWT.PUSH);
		wTest.setText(BaseMessages.getString(PKG, "JobSFTPPUT.TestConnection.Label"));
	 	props.setLook(wTest);
		fdTest=new FormData();
		wTest.setToolTipText(BaseMessages.getString(PKG, "JobSFTPPUT.TestConnection.Tooltip"));
		fdTest.top  = new FormAttachment(wPassword, margin);
		fdTest.right= new FormAttachment(100, 0);
		wTest.setLayoutData(fdTest);
		

	     fdServerSettings = new FormData();
	     fdServerSettings.left = new FormAttachment(0, margin);
	     fdServerSettings.top = new FormAttachment(wName, margin);
	     fdServerSettings.right = new FormAttachment(100, -margin);
	     wServerSettings.setLayoutData(fdServerSettings);
	     // ///////////////////////////////////////////////////////////
	     // / END OF SERVER SETTINGS GROUP
	     // ///////////////////////////////////////////////////////////
	     
	     // ////////////////////////
	     // START OF Source files GROUP///
	     // /
	     wSourceFiles = new Group(shell, SWT.SHADOW_NONE);
	     props.setLook(wSourceFiles);
	     wSourceFiles.setText(BaseMessages.getString(PKG, "JobSFTPPUT.SourceFiles.Group.Label"));
	     FormLayout SourceFilesgroupLayout = new FormLayout();
	     SourceFilesgroupLayout.marginWidth = 10;
	     SourceFilesgroupLayout.marginHeight = 10;
	     wSourceFiles.setLayout(SourceFilesgroupLayout);
	     
        // Get arguments from previous result...
        wlgetPrevious = new Label(wSourceFiles, SWT.RIGHT);
        wlgetPrevious.setText(BaseMessages.getString(PKG, "JobSFTPPUT.getPrevious.Label"));
        props.setLook(wlgetPrevious);
        fdlgetPrevious = new FormData();
        fdlgetPrevious.left = new FormAttachment(0, 0);
        fdlgetPrevious.top = new FormAttachment(wServerSettings, 2*margin);
        fdlgetPrevious.right = new FormAttachment(middle, -margin);
        wlgetPrevious.setLayoutData(fdlgetPrevious);
        wgetPrevious = new Button(wSourceFiles, SWT.CHECK);
        props.setLook(wgetPrevious);
        wgetPrevious.setToolTipText(BaseMessages.getString(PKG, "JobSFTPPUT.getPrevious.Tooltip"));
        fdgetPrevious = new FormData();
        fdgetPrevious.left = new FormAttachment(middle, 0);
        fdgetPrevious.top = new FormAttachment(wServerSettings, 2*margin);
        fdgetPrevious.right = new FormAttachment(100, 0);
        wgetPrevious.setLayoutData(fdgetPrevious);
        wgetPrevious.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				 activeCopyFromPrevious();
				jobEntry.setChanged();
			}
		});

		// Local Directory line
		wlLocalDirectory=new Label(wSourceFiles, SWT.RIGHT);
		wlLocalDirectory.setText(BaseMessages.getString(PKG, "JobSFTPPUT.LocalDir.Label"));
 		props.setLook(wlLocalDirectory);
		fdlLocalDirectory=new FormData();
		fdlLocalDirectory.left = new FormAttachment(0, 0);
		fdlLocalDirectory.top  = new FormAttachment(wgetPrevious, margin);
		fdlLocalDirectory.right= new FormAttachment(middle, -margin);
		wlLocalDirectory.setLayoutData(fdlLocalDirectory);
		
		 // Browse folders button ...
		wbLocalDirectory=new Button(wSourceFiles, SWT.PUSH| SWT.CENTER);
		props.setLook(wbLocalDirectory);
		wbLocalDirectory.setText(BaseMessages.getString(PKG, "JobSFTPPUT.BrowseFolders.Label"));
		fdbLocalDirectory=new FormData();
		fdbLocalDirectory.right= new FormAttachment(100, 0);
		fdbLocalDirectory.top  = new FormAttachment(wgetPrevious, margin);
		wbLocalDirectory.setLayoutData(fdbLocalDirectory);
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
		
		
		
		wLocalDirectory=new TextVar(jobMeta, wSourceFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLocalDirectory);
 		wLocalDirectory.setToolTipText(BaseMessages.getString(PKG, "JobSFTPPUT.LocalDir.Tooltip"));
		wLocalDirectory.addModifyListener(lsMod);
		fdLocalDirectory=new FormData();
		fdLocalDirectory.left = new FormAttachment(middle, 0);
		fdLocalDirectory.top  = new FormAttachment(wgetPrevious, margin);
		fdLocalDirectory.right= new FormAttachment(wbLocalDirectory, -margin);
		wLocalDirectory.setLayoutData(fdLocalDirectory);

		// Wildcard line
		wlWildcard=new Label(wSourceFiles, SWT.RIGHT);
		wlWildcard.setText(BaseMessages.getString(PKG, "JobSFTPPUT.Wildcard.Label"));
 		props.setLook(wlWildcard);
		fdlWildcard=new FormData();
		fdlWildcard.left = new FormAttachment(0, 0);
		fdlWildcard.top  = new FormAttachment(wLocalDirectory, margin);
		fdlWildcard.right= new FormAttachment(middle, -margin);
		wlWildcard.setLayoutData(fdlWildcard);
		wWildcard=new TextVar(jobMeta, wSourceFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wWildcard);
 		wWildcard.setToolTipText(BaseMessages.getString(PKG, "JobSFTPPUT.Wildcard.Tooltip"));
		wWildcard.addModifyListener(lsMod);
		fdWildcard=new FormData();
		fdWildcard.left = new FormAttachment(middle, 0);
		fdWildcard.top  = new FormAttachment(wLocalDirectory, margin);
		fdWildcard.right= new FormAttachment(100, 0);
		wWildcard.setLayoutData(fdWildcard);

		// Remove files after retrieval...
		wlRemove=new Label(wSourceFiles, SWT.RIGHT);
		wlRemove.setText(BaseMessages.getString(PKG, "JobSFTPPUT.RemoveFiles.Label"));
 		props.setLook(wlRemove);
		fdlRemove=new FormData();
		fdlRemove.left = new FormAttachment(0, 0);
		fdlRemove.top  = new FormAttachment(wWildcard, margin);
		fdlRemove.right= new FormAttachment(middle, -margin);
		wlRemove.setLayoutData(fdlRemove);
		wRemove=new Button(wSourceFiles, SWT.CHECK);
 		props.setLook(wRemove);
		fdRemove=new FormData();
		wRemove.setToolTipText(BaseMessages.getString(PKG, "JobSFTPPUT.RemoveFiles.Tooltip"));
		fdRemove.left = new FormAttachment(middle, 0);
		fdRemove.top  = new FormAttachment(wWildcard, margin);
		fdRemove.right= new FormAttachment(100, 0);
		wRemove.setLayoutData(fdRemove);
		wRemove.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				 activeRemoveFilename();
				jobEntry.setChanged();
			}
		});
		
		// Add filenames to result filenames...
        wlAddFilenameToResult = new Label(wSourceFiles, SWT.RIGHT);
        wlAddFilenameToResult.setText(BaseMessages.getString(PKG, "JobSFTPPUT.AddfilenametoResult.Label"));
        props.setLook(wlAddFilenameToResult);
        fdlAddFilenameToResult = new FormData();
        fdlAddFilenameToResult.left = new FormAttachment(0, 0);
        fdlAddFilenameToResult.top = new FormAttachment(wRemove, margin);
        fdlAddFilenameToResult.right = new FormAttachment(middle, -margin);
        wlAddFilenameToResult.setLayoutData(fdlAddFilenameToResult);
        wAddFilenameToResult = new Button(wSourceFiles, SWT.CHECK);
        wAddFilenameToResult.setToolTipText(BaseMessages.getString(PKG, "JobSFTPPUT.AddfilenametoResult.Tooltip"));
        props.setLook(wAddFilenameToResult);
        fdAddFilenameToResult = new FormData();
        fdAddFilenameToResult.left = new FormAttachment(middle, 0);
        fdAddFilenameToResult.top = new FormAttachment(wRemove, margin);
        fdAddFilenameToResult.right = new FormAttachment(100, 0);
        wAddFilenameToResult.setLayoutData(fdAddFilenameToResult);

        
		
	     fdSourceFiles = new FormData();
	     fdSourceFiles.left = new FormAttachment(0, margin);
	     fdSourceFiles.top = new FormAttachment(wServerSettings, 2*margin);
	     fdSourceFiles.right = new FormAttachment(100, -margin);
	     wSourceFiles.setLayoutData(fdSourceFiles);
	     // ///////////////////////////////////////////////////////////
	     // / END OF Source files GROUP
	     // ///////////////////////////////////////////////////////////
	   
	     // ////////////////////////
	     // START OF Target files GROUP///
	     // /
	     wTargetFiles = new Group(shell, SWT.SHADOW_NONE);
	     props.setLook(wTargetFiles);
	     wTargetFiles.setText(BaseMessages.getString(PKG, "JobSFTPPUT.TargetFiles.Group.Label"));
	     FormLayout TargetFilesgroupLayout = new FormLayout();
	     TargetFilesgroupLayout.marginWidth = 10;
	     TargetFilesgroupLayout.marginHeight = 10;
	     wTargetFiles.setLayout(TargetFilesgroupLayout);
	     
		// FtpDirectory line
		wlScpDirectory=new Label(wTargetFiles, SWT.RIGHT);
		wlScpDirectory.setText(BaseMessages.getString(PKG, "JobSFTPPUT.RemoteDir.Label"));
 		props.setLook(wlScpDirectory);
		fdlScpDirectory=new FormData();
		fdlScpDirectory.left = new FormAttachment(0, 0);
		fdlScpDirectory.top  = new FormAttachment(wSourceFiles, margin);
		fdlScpDirectory.right= new FormAttachment(middle, -margin);
		wlScpDirectory.setLayoutData(fdlScpDirectory);
		
	    // Test remote folder  button ...
		wbTestChangeFolderExists=new Button(wTargetFiles, SWT.PUSH| SWT.CENTER);
		props.setLook(wbTestChangeFolderExists);
		wbTestChangeFolderExists.setText(BaseMessages.getString(PKG, "JobSFTPPUT.TestFolderExists.Label"));
		fdbTestChangeFolderExists=new FormData();
		fdbTestChangeFolderExists.right= new FormAttachment(100, 0);
		fdbTestChangeFolderExists.top  = new FormAttachment(wSourceFiles, margin);
		wbTestChangeFolderExists.setLayoutData(fdbTestChangeFolderExists);
		
		// Target (remote) folder
		wScpDirectory=new TextVar(jobMeta, wTargetFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wScpDirectory);
 		wScpDirectory.setToolTipText(BaseMessages.getString(PKG, "JobSFTPPUT.RemoteDir.Tooltip"));
        wScpDirectory.addModifyListener(lsMod);
		fdScpDirectory=new FormData();
		fdScpDirectory.left = new FormAttachment(middle, 0);
		fdScpDirectory.top  = new FormAttachment(wSourceFiles, margin);
		fdScpDirectory.right= new FormAttachment(wbTestChangeFolderExists, -margin);
		wScpDirectory.setLayoutData(fdScpDirectory);
		
	     fdTargetFiles = new FormData();
	     fdTargetFiles.left = new FormAttachment(0, margin);
	     fdTargetFiles.top = new FormAttachment(wSourceFiles, margin);
	     fdTargetFiles.right = new FormAttachment(100, -margin);
	     wTargetFiles.setLayoutData(fdTargetFiles);
	     // ///////////////////////////////////////////////////////////
	     // / END OF Target files GROUP
	     // ///////////////////////////////////////////////////////////


		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wTargetFiles);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsTest     = new Listener() { public void handleEvent(Event e) { test(); } };
	    lsCheckChangeFolder     = new Listener() { public void handleEvent(Event e) { checkRemoteFolder(); } };
	        
	        
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		wTest.addListener    (SWT.Selection, lsTest    );
	    wbTestChangeFolderExists.addListener    (SWT.Selection, lsCheckChangeFolder    );

	        lsDef = new SelectionAdapter()
	        {
	            public void widgetDefaultSelected(SelectionEvent e)
	            {
	                ok();
	            }
	        };
		
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
		activeCopyFromPrevious();
		activeRemoveFilename();
		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
		    if (!display.readAndDispatch()) display.sleep();
		}
		return jobEntry;
	}
	private void activeRemoveFilename()
	{
		wAddFilenameToResult.setEnabled(!wRemove.getSelection());
		wlAddFilenameToResult.setEnabled(!wRemove.getSelection());
		if(wRemove.getSelection())	wAddFilenameToResult.setSelection(false);
	}
    private void activeCopyFromPrevious()
    {
    	wLocalDirectory.setEnabled(!wgetPrevious.getSelection());
    	wlLocalDirectory.setEnabled(!wgetPrevious.getSelection());
    	wbLocalDirectory.setEnabled(!wgetPrevious.getSelection());
    	wlWildcard.setEnabled(!wgetPrevious.getSelection());
    	wWildcard.setEnabled(!wgetPrevious.getSelection());
    	wbTestChangeFolderExists.setEnabled(!wgetPrevious.getSelection());
    }
	 private void test()
	    {
			
	    	if(connectToSFTP(false,null))
	    	{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
				mb.setMessage(BaseMessages.getString(PKG, "JobSFTPPUT.Connected.OK",wServerName.getText()) +Const.CR);
				mb.setText(BaseMessages.getString(PKG, "JobSFTPPUT.Connected.Title.Ok"));
				mb.open();
			}else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage(BaseMessages.getString(PKG, "JobSFTPPUT.Connected.NOK.ConnectionBad",wServerName.getText()) +Const.CR);
				mb.setText(BaseMessages.getString(PKG, "JobSFTPPUT.Connected.Title.Bad"));
				mb.open(); 
		    }
		   
	    }
	    private void closeFTPConnections()
		{	
			// Close SecureFTP connection if necessary
			if (sftpclient != null)
		      {
		        try
		        {
		        	sftpclient.disconnect();
		        	sftpclient=null;
		        } catch (Exception e) {}
		      }
		}
	    private boolean connectToSFTP(boolean checkFolder, String Remotefoldername)
	    {
	    	boolean retval=false;
			try
			{
				if(sftpclient==null)
				{
					// Create sftp client to host ...
					sftpclient = new SFTPClient(InetAddress.getByName(jobMeta.environmentSubstitute(wServerName.getText())), 
							Const.toInt(jobMeta.environmentSubstitute(wServerPort.getText()), 22), 
							jobMeta.environmentSubstitute(wUserName.getText()));
				
					// login to ftp host ...
					sftpclient.login(jobMeta.environmentSubstitute(wPassword.getText()));
					
					retval=true;
				}  
		       if(checkFolder) retval=sftpclient.folderExists(Remotefoldername);   	
		        
			}
		     catch (Exception e)
		    {
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage(BaseMessages.getString(PKG, "JobSFTPPUT.ErrorConnect.NOK",wServerName.getText(),e.getMessage()) +Const.CR);
				mb.setText(BaseMessages.getString(PKG, "JobSFTPPUT.ErrorConnect.Title.Bad"));
				mb.open(); 
		    } 
		    return retval;
	    }
	    private void checkRemoteFolder()
	    {
	    	String changeFTPFolder=jobMeta.environmentSubstitute(wScpDirectory.getText());
	    	if(!Const.isEmpty(changeFTPFolder))
	    	{
		    	if(connectToSFTP(true,changeFTPFolder))
		    	{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
					mb.setMessage(BaseMessages.getString(PKG, "JobSFTPPUT.FolderExists.OK",changeFTPFolder) +Const.CR);
					mb.setText(BaseMessages.getString(PKG, "JobSFTPPUT.FolderExists.Title.Ok"));
					mb.open();
				}else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
					mb.setMessage(BaseMessages.getString(PKG, "JobSFTPPUT.FolderExists.NOK",changeFTPFolder) +Const.CR);
					mb.setText(BaseMessages.getString(PKG, "JobSFTPPUT.FolderExists.Title.Bad"));
					mb.open(); 
			    }
	    	}
	    }
	public void dispose()
	{
		// Close open connections
		closeFTPConnections();
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
        wgetPrevious.setSelection(jobEntry.isCopyPrevious());
        wAddFilenameToResult.setSelection(jobEntry.isAddFilenameResut());
        
	}
	
	private void cancel()
	{
		jobEntry.setChanged(changed);
		jobEntry=null;
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
		jobEntry.setServerName(wServerName.getText());
		jobEntry.setServerPort(wServerPort.getText());
		jobEntry.setUserName(wUserName.getText());
		jobEntry.setPassword(wPassword.getText());
		jobEntry.setScpDirectory(wScpDirectory.getText());
		jobEntry.setLocalDirectory(wLocalDirectory.getText());
		jobEntry.setWildcard(wWildcard.getText());
		jobEntry.setRemove(wRemove.getSelection());
	    jobEntry.setCopyPrevious(wgetPrevious.getSelection());
	    jobEntry.setAddFilenameResut(wAddFilenameToResult.getSelection());
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
