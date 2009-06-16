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

package org.pentaho.di.ui.job.entries.sftp;

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
import org.pentaho.di.job.entries.sftp.JobEntrySFTP;
import org.pentaho.di.job.entries.sftp.SFTPClient;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog allows you to edit the SFTP job entry settings.
 * 
 * @author Matt
 * @since 19-06-2003
 */

public class JobEntrySFTPDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = JobEntrySFTP.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

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

    private Label wlScpDirectory;

    private TextVar wScpDirectory;

    private FormData fdlScpDirectory, fdScpDirectory;

    private Label wlTargetDirectory;

    private TextVar wTargetDirectory;

    private FormData fdlTargetDirectory, fdTargetDirectory;

    private Label wlWildcard;

    private TextVar wWildcard;

    private FormData fdlWildcard, fdWildcard;

    private Label wlRemove;

    private Button wRemove;

    private FormData fdlRemove, fdRemove;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;
    
	private Listener lsCheckChangeFolder;

    private JobEntrySFTP jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;
    
    private boolean changed;
    
	private Button wTest;
	
	private FormData fdTest;
	
	private Listener lsTest;
	
	private Button wbTestChangeFolderExists;
	
	private FormData fdbTestChangeFolderExists;
	
    private Button wbTargetDirectory;
    
    private FormData fdbTargetDirectory;  
    
    private Group wServerSettings;
    
    private FormData fdServerSettings;
    
    private Group wSourceFiles;
    
    private FormData fdSourceFiles;
    
    private Group wTargetFiles;
    
    private FormData fdTargetFiles;
	
    private Label wlAddFilenameToResult;

    private Button wAddFilenameToResult;
    
    private FormData fdlAddFilenameToResult,fdAddFilenameToResult;
    
    private Label wlCreateTargetFolder;
    
    private Button wCreateTargetFolder;
    
    private FormData fdlCreateTargetFolder,fdCreateTargetFolder;
    
    private Label wlgetPrevious;

    private Button wgetPrevious;

    private FormData fdlgetPrevious, fdgetPrevious;
    
	private SFTPClient sftpclient = null;


    public JobEntrySFTPDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntrySFTP) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(BaseMessages.getString(PKG, "JobSFTP.Name.Default"));
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
            	sftpclient=null;
                jobEntry.setChanged();
            }
        };
        changed = jobEntry.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "JobSFTP.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Filename line
        wlName = new Label(shell, SWT.RIGHT);
        wlName.setText(BaseMessages.getString(PKG, "JobSFTP.Name.Label"));
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
        
        
	     // ////////////////////////
	     // START OF SERVER SETTINGS GROUP///
	     // /
	    wServerSettings = new Group(shell, SWT.SHADOW_NONE);
	    props.setLook(wServerSettings);
	    wServerSettings.setText(BaseMessages.getString(PKG, "JobSFTP.ServerSettings.Group.Label"));
	    FormLayout ServerSettingsgroupLayout = new FormLayout();
	    ServerSettingsgroupLayout.marginWidth = 10;
	    ServerSettingsgroupLayout.marginHeight = 10;
	    wServerSettings.setLayout(ServerSettingsgroupLayout);

        // ServerName line
        wlServerName = new Label(wServerSettings, SWT.RIGHT);
        wlServerName.setText(BaseMessages.getString(PKG, "JobSFTP.Server.Label"));
        props.setLook(wlServerName);
        fdlServerName = new FormData();
        fdlServerName.left = new FormAttachment(0, 0);
        fdlServerName.top = new FormAttachment(wName, margin);
        fdlServerName.right = new FormAttachment(middle, -margin);
        wlServerName.setLayoutData(fdlServerName);
        wServerName = new TextVar(jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wServerName);
        wServerName.addModifyListener(lsMod);
        fdServerName = new FormData();
        fdServerName.left = new FormAttachment(middle, 0);
        fdServerName.top = new FormAttachment(wName, margin);
        fdServerName.right = new FormAttachment(100, 0);
        wServerName.setLayoutData(fdServerName);

        // ServerPort line
        wlServerPort = new Label(wServerSettings, SWT.RIGHT);
        wlServerPort.setText(BaseMessages.getString(PKG, "JobSFTP.Port.Label"));
        props.setLook(wlServerPort);
        fdlServerPort = new FormData();
        fdlServerPort.left = new FormAttachment(0, 0);
        fdlServerPort.top = new FormAttachment(wServerName, margin);
        fdlServerPort.right = new FormAttachment(middle, -margin);
        wlServerPort.setLayoutData(fdlServerPort);
        wServerPort = new TextVar(jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wServerPort);
        wServerPort.setToolTipText(BaseMessages.getString(PKG, "JobSFTP.Port.Tooltip"));
        wServerPort.addModifyListener(lsMod);
        fdServerPort = new FormData();
        fdServerPort.left = new FormAttachment(middle, 0);
        fdServerPort.top = new FormAttachment(wServerName, margin);
        fdServerPort.right = new FormAttachment(100, 0);
        wServerPort.setLayoutData(fdServerPort);

        // UserName line
        wlUserName = new Label(wServerSettings, SWT.RIGHT);
        wlUserName.setText(BaseMessages.getString(PKG, "JobSFTP.Username.Label"));
        props.setLook(wlUserName);
        fdlUserName = new FormData();
        fdlUserName.left = new FormAttachment(0, 0);
        fdlUserName.top = new FormAttachment(wServerPort, margin);
        fdlUserName.right = new FormAttachment(middle, -margin);
        wlUserName.setLayoutData(fdlUserName);
        wUserName = new TextVar(jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wUserName);
        wUserName.addModifyListener(lsMod);
        fdUserName = new FormData();
        fdUserName.left = new FormAttachment(middle, 0);
        fdUserName.top = new FormAttachment(wServerPort, margin);
        fdUserName.right = new FormAttachment(100, 0);
        wUserName.setLayoutData(fdUserName);

        // Password line
        wlPassword = new Label(wServerSettings, SWT.RIGHT);
        wlPassword.setText(BaseMessages.getString(PKG, "JobSFTP.Password.Label"));
        props.setLook(wlPassword);
        fdlPassword = new FormData();
        fdlPassword.left = new FormAttachment(0, 0);
        fdlPassword.top = new FormAttachment(wUserName, margin);
        fdlPassword.right = new FormAttachment(middle, -margin);
        wlPassword.setLayoutData(fdlPassword);
        wPassword = new TextVar(jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wPassword);
        wPassword.setEchoChar('*');
        wPassword.addModifyListener(lsMod);
        fdPassword = new FormData();
        fdPassword.left = new FormAttachment(middle, 0);
        fdPassword.top = new FormAttachment(wUserName, margin);
        fdPassword.right = new FormAttachment(100, 0);
        wPassword.setLayoutData(fdPassword);
        
		// Test connection button
		wTest=new Button(wServerSettings,SWT.PUSH);
		wTest.setText(BaseMessages.getString(PKG, "JobSFTP.TestConnection.Label"));
	 	props.setLook(wTest);
		fdTest=new FormData();
		wTest.setToolTipText(BaseMessages.getString(PKG, "JobSFTP.TestConnection.Tooltip"));
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
	     wSourceFiles.setText(BaseMessages.getString(PKG, "JobSFTP.SourceFiles.Group.Label"));
	     FormLayout SourceFilesgroupLayout = new FormLayout();
	     SourceFilesgroupLayout.marginWidth = 10;
	     SourceFilesgroupLayout.marginHeight = 10;
	     wSourceFiles.setLayout(SourceFilesgroupLayout);
		
        // Get arguments from previous result...
        wlgetPrevious = new Label(wSourceFiles, SWT.RIGHT);
        wlgetPrevious.setText(BaseMessages.getString(PKG, "JobSFTP.getPrevious.Label"));
        props.setLook(wlgetPrevious);
        fdlgetPrevious = new FormData();
        fdlgetPrevious.left = new FormAttachment(0, 0);
        fdlgetPrevious.top = new FormAttachment(wServerSettings, 2*margin);
        fdlgetPrevious.right = new FormAttachment(middle, -margin);
        wlgetPrevious.setLayoutData(fdlgetPrevious);
        wgetPrevious = new Button(wSourceFiles, SWT.CHECK);
        props.setLook(wgetPrevious);
        wgetPrevious.setToolTipText(BaseMessages.getString(PKG, "JobSFTP.getPrevious.Tooltip"));
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
        
	     
        // FtpDirectory line
        wlScpDirectory = new Label(wSourceFiles, SWT.RIGHT);
        wlScpDirectory.setText(BaseMessages.getString(PKG, "JobSFTP.RemoteDir.Label"));
        props.setLook(wlScpDirectory);
        fdlScpDirectory = new FormData();
        fdlScpDirectory.left = new FormAttachment(0, 0);
        fdlScpDirectory.top = new FormAttachment(wgetPrevious, 2*margin);
        fdlScpDirectory.right = new FormAttachment(middle, -margin);
        wlScpDirectory.setLayoutData(fdlScpDirectory);
        
	    // Test remote folder  button ...
		wbTestChangeFolderExists=new Button(wSourceFiles, SWT.PUSH| SWT.CENTER);
		props.setLook(wbTestChangeFolderExists);
		wbTestChangeFolderExists.setText(BaseMessages.getString(PKG, "JobSFTP.TestFolderExists.Label"));
		fdbTestChangeFolderExists=new FormData();
		fdbTestChangeFolderExists.right= new FormAttachment(100, 0);
		fdbTestChangeFolderExists.top  = new FormAttachment(wgetPrevious, 2*margin);
		wbTestChangeFolderExists.setLayoutData(fdbTestChangeFolderExists);
        
        wScpDirectory = new TextVar(jobMeta, wSourceFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER, BaseMessages.getString(PKG, "JobSFTP.RemoteDir.Tooltip"));
        props.setLook(wScpDirectory);
        wScpDirectory.addModifyListener(lsMod);
        fdScpDirectory = new FormData();
        fdScpDirectory.left = new FormAttachment(middle, 0);
        fdScpDirectory.top = new FormAttachment(wgetPrevious, 2*margin);
        fdScpDirectory.right = new FormAttachment(wbTestChangeFolderExists, -margin);
        wScpDirectory.setLayoutData(fdScpDirectory);
        
        // Wildcard line
        wlWildcard = new Label(wSourceFiles, SWT.RIGHT);
        wlWildcard.setText(BaseMessages.getString(PKG, "JobSFTP.Wildcard.Label"));
        props.setLook(wlWildcard);
        fdlWildcard = new FormData();
        fdlWildcard.left = new FormAttachment(0, 0);
        fdlWildcard.top = new FormAttachment(wScpDirectory, margin);
        fdlWildcard.right = new FormAttachment(middle, -margin);
        wlWildcard.setLayoutData(fdlWildcard);
        wWildcard = new TextVar(jobMeta, wSourceFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER, BaseMessages.getString(PKG, "JobSFTP.Wildcard.Tooltip"));
        props.setLook(wWildcard);
        wWildcard.addModifyListener(lsMod);
        fdWildcard = new FormData();
        fdWildcard.left = new FormAttachment(middle, 0);
        fdWildcard.top = new FormAttachment(wScpDirectory, margin);
        fdWildcard.right = new FormAttachment(100, 0);
        wWildcard.setLayoutData(fdWildcard);
        
        // Remove files after retrieval...
        wlRemove = new Label(wSourceFiles, SWT.RIGHT);
        wlRemove.setText(BaseMessages.getString(PKG, "JobSFTP.RemoveFiles.Label"));
        props.setLook(wlRemove);
        fdlRemove = new FormData();
        fdlRemove.left = new FormAttachment(0, 0);
        fdlRemove.top = new FormAttachment(wWildcard, margin);
        fdlRemove.right = new FormAttachment(middle, -margin);
        wlRemove.setLayoutData(fdlRemove);
        wRemove = new Button(wSourceFiles, SWT.CHECK);
        props.setLook(wRemove);
        wRemove.setToolTipText(BaseMessages.getString(PKG, "JobSFTP.RemoveFiles.Tooltip"));
        fdRemove = new FormData();
        fdRemove.left = new FormAttachment(middle, 0);
        fdRemove.top = new FormAttachment(wWildcard, margin);
        fdRemove.right = new FormAttachment(100, 0);
        wRemove.setLayoutData(fdRemove);

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
	     wTargetFiles.setText(BaseMessages.getString(PKG, "JobSFTP.TargetFiles.Group.Label"));
	     FormLayout TargetFilesgroupLayout = new FormLayout();
	     TargetFilesgroupLayout.marginWidth = 10;
	     TargetFilesgroupLayout.marginHeight = 10;
	     wTargetFiles.setLayout(TargetFilesgroupLayout);
        
        // TargetDirectory line
        wlTargetDirectory = new Label(wTargetFiles, SWT.RIGHT);
        wlTargetDirectory.setText(BaseMessages.getString(PKG, "JobSFTP.TargetDir.Label"));
        props.setLook(wlTargetDirectory);
        fdlTargetDirectory = new FormData();
        fdlTargetDirectory.left = new FormAttachment(0, 0);
        fdlTargetDirectory.top = new FormAttachment(wSourceFiles, margin);
        fdlTargetDirectory.right = new FormAttachment(middle, -margin);
        wlTargetDirectory.setLayoutData(fdlTargetDirectory);
        
        
	    // Browse folders button ...
		wbTargetDirectory=new Button(wTargetFiles, SWT.PUSH| SWT.CENTER);
		props.setLook(wbTargetDirectory);
		wbTargetDirectory.setText(BaseMessages.getString(PKG, "JobSFTP.BrowseFolders.Label"));
		fdbTargetDirectory=new FormData();
		fdbTargetDirectory.right= new FormAttachment(100, 0);
		fdbTargetDirectory.top  = new FormAttachment(wSourceFiles, margin);
		wbTargetDirectory.setLayoutData(fdbTargetDirectory);
		wbTargetDirectory.addSelectionListener
			(
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						DirectoryDialog ddialog = new DirectoryDialog(shell, SWT.OPEN);
						if (wTargetDirectory.getText()!=null)
						{
							ddialog.setFilterPath(jobMeta.environmentSubstitute(wTargetDirectory.getText()) );
						}
						
						 // Calling open() will open and run the dialog.
				        // It will return the selected directory, or
				        // null if user cancels
				        String dir = ddialog.open();
				        if (dir != null) {
				          // Set the text box to the new selection
				        	wTargetDirectory.setText(dir);
				        }
						
					}
				}
			);
        
        wTargetDirectory = new TextVar(jobMeta, wTargetFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER, BaseMessages.getString(PKG, "JobSFTP.TargetDir.Tooltip"));
        props.setLook(wTargetDirectory);
        wTargetDirectory.addModifyListener(lsMod);
        fdTargetDirectory = new FormData();
        fdTargetDirectory.left = new FormAttachment(middle, 0);
        fdTargetDirectory.top = new FormAttachment(wSourceFiles, margin);
        fdTargetDirectory.right = new FormAttachment(wbTargetDirectory, -margin);
        wTargetDirectory.setLayoutData(fdTargetDirectory);

        // Create target folder if necessary...
        wlCreateTargetFolder = new Label(wTargetFiles, SWT.RIGHT);
        wlCreateTargetFolder.setText(BaseMessages.getString(PKG, "JobSFTP.CreateTargetFolder.Label"));
        props.setLook(wlCreateTargetFolder);
        fdlCreateTargetFolder = new FormData();
        fdlCreateTargetFolder.left = new FormAttachment(0, 0);
        fdlCreateTargetFolder.top = new FormAttachment(wTargetDirectory, margin);
        fdlCreateTargetFolder.right = new FormAttachment(middle, -margin);
        wlCreateTargetFolder.setLayoutData(fdlCreateTargetFolder);
        wCreateTargetFolder = new Button(wTargetFiles, SWT.CHECK);
        wCreateTargetFolder.setToolTipText(BaseMessages.getString(PKG, "JobSFTP.CreateTargetFolder.Tooltip"));
        props.setLook(wCreateTargetFolder);
        fdCreateTargetFolder = new FormData();
        fdCreateTargetFolder.left = new FormAttachment(middle, 0);
        fdCreateTargetFolder.top = new FormAttachment(wTargetDirectory, margin);
        fdCreateTargetFolder.right = new FormAttachment(100, 0);
        wCreateTargetFolder.setLayoutData(fdCreateTargetFolder);
        
        
        // Add filenames to result filenames...
        wlAddFilenameToResult = new Label(wTargetFiles, SWT.RIGHT);
        wlAddFilenameToResult.setText(BaseMessages.getString(PKG, "JobSFTP.AddfilenametoResult.Label"));
        props.setLook(wlAddFilenameToResult);
        fdlAddFilenameToResult = new FormData();
        fdlAddFilenameToResult.left = new FormAttachment(0, 0);
        fdlAddFilenameToResult.top = new FormAttachment(wCreateTargetFolder, margin);
        fdlAddFilenameToResult.right = new FormAttachment(middle, -margin);
        wlAddFilenameToResult.setLayoutData(fdlAddFilenameToResult);
        wAddFilenameToResult = new Button(wTargetFiles, SWT.CHECK);
        wAddFilenameToResult.setToolTipText(BaseMessages.getString(PKG, "JobSFTP.AddfilenametoResult.Tooltip"));
        props.setLook(wAddFilenameToResult);
        fdAddFilenameToResult = new FormData();
        fdAddFilenameToResult.left = new FormAttachment(middle, 0);
        fdAddFilenameToResult.top = new FormAttachment(wCreateTargetFolder, margin);
        fdAddFilenameToResult.right = new FormAttachment(100, 0);
        wAddFilenameToResult.setLayoutData(fdAddFilenameToResult);
        

	     fdTargetFiles = new FormData();
	     fdTargetFiles.left = new FormAttachment(0, margin);
	     fdTargetFiles.top = new FormAttachment(wSourceFiles, margin);
	     fdTargetFiles.right = new FormAttachment(100, -margin);
	     wTargetFiles.setLayoutData(fdTargetFiles);
	     // ///////////////////////////////////////////////////////////
	     // / END OF Target files GROUP
	     // ///////////////////////////////////////////////////////////


        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wTargetFiles);

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
        lsTest     = new Listener() { public void handleEvent(Event e) { test(); } };
        lsCheckChangeFolder     = new Listener() { public void handleEvent(Event e) { checkRemoteFolder(); } };
        
        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);
        wTest.addListener    (SWT.Selection, lsTest    );
        wbTestChangeFolderExists.addListener    (SWT.Selection, lsCheckChangeFolder    );

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
        wScpDirectory.addSelectionListener(lsDef);
        wTargetDirectory.addSelectionListener(lsDef);
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
        activeCopyFromPrevious();

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
    private void test()
    {
    	if(connectToSFTP(false,null))
    	{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
			mb.setMessage(BaseMessages.getString(PKG, "JobSFTP.Connected.OK",wServerName.getText()) +Const.CR);
			mb.setText(BaseMessages.getString(PKG, "JobSFTP.Connected.Title.Ok"));
			mb.open();
		}else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "JobSFTP.Connected.NOK.ConnectionBad",wServerName.getText()) +Const.CR);
			mb.setText(BaseMessages.getString(PKG, "JobSFTP.Connected.Title.Bad"));
			mb.open(); 
	    }
	   
    }
    private void activeCopyFromPrevious()
    {
    	wlWildcard.setEnabled(!wgetPrevious.getSelection());
    	wWildcard.setEnabled(!wgetPrevious.getSelection());
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
			mb.setMessage(BaseMessages.getString(PKG, "JobSFTP.ErrorConnect.NOK",wServerName.getText(),e.getMessage()) +Const.CR );
			mb.setText(BaseMessages.getString(PKG, "JobSFTP.ErrorConnect.Title.Bad"));
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
				mb.setMessage(BaseMessages.getString(PKG, "JobSFTP.FolderExists.OK",changeFTPFolder) +Const.CR);
				mb.setText(BaseMessages.getString(PKG, "JobSFTP.FolderExists.Title.Ok"));
				mb.open();
			}else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage(BaseMessages.getString(PKG, "JobSFTP.FolderExists.NOK",changeFTPFolder) +Const.CR);
				mb.setText(BaseMessages.getString(PKG, "JobSFTP.FolderExists.Title.Bad"));
				mb.open(); 
		    }
    	}
    }
    public void dispose()
    {
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
        if (jobEntry.getName() != null)
            wName.setText(jobEntry.getName());
        wName.selectAll();

        wServerName.setText(Const.NVL(jobEntry.getServerName(), ""));
        wServerPort.setText(jobEntry.getServerPort());
        wUserName.setText(Const.NVL(jobEntry.getUserName(), ""));
        wPassword.setText(Const.NVL(jobEntry.getPassword(), ""));
        wScpDirectory.setText(Const.NVL(jobEntry.getScpDirectory(), ""));
        wTargetDirectory.setText(Const.NVL(jobEntry.getTargetDirectory(), ""));
        wWildcard.setText(Const.NVL(jobEntry.getWildcard(), ""));
        wRemove.setSelection(jobEntry.getRemove());
    	wAddFilenameToResult.setSelection(jobEntry.isAddToResult());
    	wCreateTargetFolder.setSelection(jobEntry.iscreateTargetFolder());
        wgetPrevious.setSelection(jobEntry.isCopyPrevious());
    	
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
        jobEntry.setServerName(wServerName.getText());
        jobEntry.setServerPort(wServerPort.getText());
        jobEntry.setUserName(wUserName.getText());
        jobEntry.setPassword(wPassword.getText());
        jobEntry.setScpDirectory(wScpDirectory.getText());
        jobEntry.setTargetDirectory(wTargetDirectory.getText());
        jobEntry.setWildcard(wWildcard.getText());
        jobEntry.setRemove(wRemove.getSelection());
    	jobEntry.setAddToResult(wAddFilenameToResult.getSelection());
    	jobEntry.setcreateTargetFolder(wCreateTargetFolder.getSelection());
	    jobEntry.setCopyPrevious(wgetPrevious.getSelection());
    	
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
