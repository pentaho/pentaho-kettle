/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/


package org.pentaho.di.ui.job.entries.ssh2get;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.ui.core.widget.LabelText;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.job.entries.ssh2get.JobEntrySSH2GET;
import org.pentaho.di.job.entries.ssh2get.Messages;

/**
 * This dialog allows you to edit the SSH2 GET job entry settings.
 * 
 * @author Samatar
 * @since 17-12-2007
 */
public class JobEntrySSH2GETDialog extends JobEntryDialog implements JobEntryDialogInterface
{
    private static final String[] FILETYPES = new String[] {
        Messages
            .getString("JobSSH2GET.Filetype.Pem"),
        Messages
            .getString("JobSSH2GET.Filetype.All") };
    
    private LabelText wName;

    private FormData fdName;

    private LabelTextVar wServerName;
    
    private FormData fdServerName;
    
    private LabelTextVar wHTTPProxyHost;

    private FormData fdHTTPProxyHost;

    private LabelTextVar wUserName,wHTTPProxyUsername;

    private FormData fdUserName,fdHTTPProxyUsername;

    private LabelTextVar wPassword,wHTTPProxyPassword;

    private FormData fdPassword,fdHTTPProxyPassword;
    
    private LabelTextVar wkeyfilePass;

    private FormData fdkeyfilePass;

    private LabelTextVar wFtpDirectory;

    private FormData fdFtpDirectory;

    private TextVar wLocalDirectory;
    
    private Label wlLocalDirectory;

    private FormData fdLocalDirectory,fdlLocalDirectory;

    private LabelTextVar wWildcard;

    private FormData fdWildcard;
    
    private Label wluseHTTPProxy;

    private Button wuseHTTPProxy;

    private FormData fdluseHTTPProxy, fduseHTTPProxy;
    
    private Label wlincludeSubFolders;

    private Button wincludeSubFolders;

    private FormData fdlincludeSubFolders, fdincludeSubFolders;
    
    private Label wlcreateTargetFolder;

    private Button wcreateTargetFolder;

    private FormData fdlcreateTargetFolder, fdcreateTargetFolder;
    
    
    private Label wlusePublicKey;

    private Button wusePublicKey;

    private FormData fdlusePublicKey, fdusePublicKey;

    private Label wlOnlyNew;

    private Button wOnlyNew;

    private FormData fdlOnlyNew, fdOnlyNew;
    
    private Label wlCreateDestinationFolder;

    private Button wCreateDestinationFolder;

    private FormData fdlCreateDestinationFolder, fdCreateDestinationFolder;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntrySSH2GET jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;
    
    private Label wlKeyFilename;

    private Button wbKeyFilename;

    private TextVar wKeyFilename;

    private FormData fdlKeyFilename, fdbKeyFilename, fdKeyFilename;
    
    private Label wlServerPort;

    private TextVar wServerPort;

    private FormData fdlServerPort, fdServerPort;
    
    private Label wlHTTPProxyPort;

    private TextVar wHTTPProxyPort;
    
    private Group wHTTPProxyGroup;
    private FormData fdHTTPProxyGroup ;

    private FormData fdlProxyPort, fdProxyPort;
    
	private Group wPublicKey;
	private FormData fdPublicKey ;
	
	private Group wHost;
	private FormData fdHost ;
	
	private Group wFiles;
	private FormData fdFiles ;

	private CTabFolder   wTabFolder;
	private Composite    wGeneralComp,wFilesComp;	
	private CTabItem     wGeneralTab,wFilesTab;
	private FormData	 fdGeneralComp,fdFilesComp;
	private FormData     fdTabFolder;
	
	private Button       wbLocalDirectory;
	private FormData     fdbLocalDirectory;

	private Label		 wluseBasicAuthentication;
	private Button		 wuseBasicAuthentication;
	private FormData	 fdluseBasicAuthentication,fduseBasicAuthentication;
	
	private Label wlDestinationFolder;
	private TextVar wDestinationFolder;
	private FormData fdlDestinationFolder, fdDestinationFolder;
	
	private Label wlAfterFTPPut;
	private CCombo wAfterFTPPut;
	private FormData fdlAfterFTPPut, fdAfterFTPPut;
	
	private Label  wlcacheHostKey;
	private Button wcacheHostKey;
	private FormData fdlcacheHostKey, fdcacheHostKey;
	
	private LabelTextVar wTimeout;
	private FormData fdTimeout;
	
    private boolean changed;
    
    public JobEntrySSH2GETDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntrySSH2GET) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(Messages.getString("JobSSH2GET.Name.Default"));
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
        shell.setText(Messages.getString("JobSSH2GET.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Job entry name line
        wName = new LabelText(shell, Messages.getString("JobSSH2GET.Name.Label"), Messages
            .getString("JobSSH2GET.Name.Tooltip"));
        wName.addModifyListener(lsMod);
        fdName = new FormData();
        fdName.top = new FormAttachment(0, 0);
        fdName.left = new FormAttachment(0, 0);
        fdName.right = new FormAttachment(100, 0);
        wName.setLayoutData(fdName);
        
       
        wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
 		
        //////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////
		
		
		
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(Messages.getString("JobSSH2GET.Tab.General.Label"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);

        
		// ////////////////////////
		// START OF Host GROUP
		// ////////////////////////

		wHost = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wHost);
		wHost.setText(Messages.getString("JobMail.Group.Host.Label"));
		FormLayout HostLayout = new FormLayout();
		HostLayout.marginWidth = 10;
		HostLayout.marginHeight = 10;
		wHost.setLayout(HostLayout);

        // ServerName line
        wServerName = new LabelTextVar(jobMeta,wHost, Messages.getString("JobSSH2GET.Server.Label"), Messages
            .getString("JobSSH2GET.Server.Tooltip"));
        props.setLook(wServerName);
        wServerName.addModifyListener(lsMod);
        fdServerName = new FormData();
        fdServerName.left = new FormAttachment(0, 0);
        fdServerName.top = new FormAttachment(wName, margin);
        fdServerName.right = new FormAttachment(100, 0);
        wServerName.setLayoutData(fdServerName);
        
        // ServerPort line
        wlServerPort = new Label(wHost, SWT.RIGHT);
        wlServerPort.setText(Messages.getString("JobSSH2GET.Port.Label"));
        props.setLook(wlServerPort);
        fdlServerPort = new FormData();
        fdlServerPort.left = new FormAttachment(0, 0);
        fdlServerPort.top = new FormAttachment(wServerName, margin);
        fdlServerPort.right = new FormAttachment(middle, -margin);
        wlServerPort.setLayoutData(fdlServerPort);
        wServerPort = new TextVar(jobMeta,wHost, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wServerPort);
        wServerPort.setToolTipText(Messages.getString("JobSSH2GET.Port.Tooltip"));
        wServerPort.addModifyListener(lsMod);
        fdServerPort = new FormData();
        fdServerPort.left = new FormAttachment(middle, margin);
        fdServerPort.top = new FormAttachment(wServerName, margin);
        fdServerPort.right = new FormAttachment(100, 0);
        wServerPort.setLayoutData(fdServerPort);
        
        // cacheHostKey
        wlcacheHostKey = new Label(wHost, SWT.RIGHT);
        wlcacheHostKey.setText(Messages.getString("JobSSH2GET.cacheHostKeyFiles.Label"));
        props.setLook(wlcacheHostKey);
        fdlcacheHostKey = new FormData();
        fdlcacheHostKey.left = new FormAttachment(0, 0);
        fdlcacheHostKey.top = new FormAttachment(wServerPort, margin);
        fdlcacheHostKey.right = new FormAttachment(middle, 0);
        wlcacheHostKey.setLayoutData(fdlcacheHostKey);
        wcacheHostKey = new Button(wHost, SWT.CHECK);
        wcacheHostKey.setToolTipText(Messages.getString("JobSSH2GET.cacheHostKeyFiles.Tooltip"));
        props.setLook(wcacheHostKey);
        fdcacheHostKey = new FormData();
        fdcacheHostKey.left = new FormAttachment(middle, margin);
        fdcacheHostKey.top = new FormAttachment(wServerPort, margin);
        fdcacheHostKey.right = new FormAttachment(100, 0);
        wcacheHostKey.setLayoutData(fdcacheHostKey);
        
        wcacheHostKey.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
        
        
        // UserName line
        wUserName = new LabelTextVar(jobMeta,wHost, Messages.getString("JobSSH2GET.User.Label"), Messages
            .getString("JobSSH2GET.User.Tooltip"));
        props.setLook(wUserName);
        wUserName.addModifyListener(lsMod);
        fdUserName = new FormData();
        fdUserName.left = new FormAttachment(0, 0);
        fdUserName.top = new FormAttachment(wcacheHostKey, margin);
        fdUserName.right = new FormAttachment(100, 0);
        wUserName.setLayoutData(fdUserName);

        // Password line
        wPassword = new LabelTextVar(jobMeta,wHost, Messages.getString("JobSSH2GET.Password.Label"), Messages
            .getString("JobSSH2GET.Password.Tooltip"));
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
                checkPasswordVisible(wPassword);
            }
        });
        
        // Timeout line
        wTimeout = new LabelTextVar(jobMeta,wHost, Messages.getString("JobSSH2GET.Timeout.Label"), Messages
            .getString("JobSSH2GET.Timeout.Tooltip"));
        props.setLook(wTimeout);
        wTimeout.addModifyListener(lsMod);
        fdTimeout = new FormData();
        fdTimeout.left = new FormAttachment(0, 0);
        fdTimeout.top = new FormAttachment(wPassword, margin);
        fdTimeout.right = new FormAttachment(100, 0);
        wTimeout.setLayoutData(fdTimeout);
        
        
    	fdHost = new FormData();
    	fdHost.left = new FormAttachment(0, margin);
    	fdHost.top = new FormAttachment(wName, margin);
    	fdHost.right = new FormAttachment(100, -margin);
		wHost.setLayoutData(fdHost);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Host GROUP
		///////////////////////////////////////////////////////////
        

		// ////////////////////////
		// START OF HTTPProxy GROUP
		// ////////////////////////

		wHTTPProxyGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wHTTPProxyGroup);
		wHTTPProxyGroup.setText(Messages.getString("JobSSH2GET.Group.HTTPProxyGroup.Label"));
		
		FormLayout HTTPProxyGroupLayout = new FormLayout();
		HTTPProxyGroupLayout.marginWidth = 10;
		HTTPProxyGroupLayout.marginHeight = 10;
		wHTTPProxyGroup.setLayout(HTTPProxyGroupLayout);
		
        // useHTTPProxy
        wluseHTTPProxy = new Label(wHTTPProxyGroup, SWT.RIGHT);
        wluseHTTPProxy.setText(Messages.getString("JobSSH2GET.useHTTPProxyFiles.Label"));
        props.setLook(wluseHTTPProxy);
        fdluseHTTPProxy = new FormData();
        fdluseHTTPProxy.left = new FormAttachment(0, 0);
        fdluseHTTPProxy.top = new FormAttachment(wHost, margin);
        fdluseHTTPProxy.right = new FormAttachment(middle, 0);
        wluseHTTPProxy.setLayoutData(fdluseHTTPProxy);
        wuseHTTPProxy = new Button(wHTTPProxyGroup, SWT.CHECK);
        wuseHTTPProxy.setToolTipText(Messages.getString("JobSSH2GET.useHTTPProxyFiles.Tooltip"));
        props.setLook(wuseHTTPProxy);
        fduseHTTPProxy = new FormData();
        fduseHTTPProxy.left = new FormAttachment(middle, margin);
        fduseHTTPProxy.top = new FormAttachment(wHost, margin);
        fduseHTTPProxy.right = new FormAttachment(100, 0);
        wuseHTTPProxy.setLayoutData(fduseHTTPProxy);
        
        wuseHTTPProxy.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				activeUseHttpProxy();
				jobEntry.setChanged();
			}
		});
        
        // ProxyHost line
        wHTTPProxyHost = new LabelTextVar(jobMeta,wHTTPProxyGroup, Messages.getString("JobSSH2GET.ProxyHost.Label"), Messages
            .getString("JobSSH2GET.Server.Tooltip"));
        props.setLook(wHTTPProxyHost);
        wHTTPProxyHost.addModifyListener(lsMod);
        fdHTTPProxyHost = new FormData();
        fdHTTPProxyHost.left = new FormAttachment(0, 0);
        fdHTTPProxyHost.top = new FormAttachment(wuseHTTPProxy, margin);
        fdHTTPProxyHost.right = new FormAttachment(100, 0);
        wHTTPProxyHost.setLayoutData(fdHTTPProxyHost);
        
        // ProxyPort line
        wlHTTPProxyPort = new Label(wHTTPProxyGroup, SWT.RIGHT);
        wlHTTPProxyPort.setText(Messages.getString("JobSSH2GET.ProxyPort.Label"));
        props.setLook(wlHTTPProxyPort);
        fdlProxyPort = new FormData();
        fdlProxyPort.left = new FormAttachment(0, 0);
        fdlProxyPort.top = new FormAttachment(wHTTPProxyHost, margin);
        fdlProxyPort.right = new FormAttachment(middle, -margin);
        wlHTTPProxyPort.setLayoutData(fdlProxyPort);
        wHTTPProxyPort = new TextVar(jobMeta,wHTTPProxyGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wHTTPProxyPort);
        wHTTPProxyPort.setToolTipText(Messages.getString("JobSSH2GET.ProxyPort.Tooltip"));
        wHTTPProxyPort.addModifyListener(lsMod);
        fdProxyPort = new FormData();
        fdProxyPort.left = new FormAttachment(middle, margin);
        fdProxyPort.top = new FormAttachment(wHTTPProxyHost, margin);
        fdProxyPort.right = new FormAttachment(100, 0);
        wHTTPProxyPort.setLayoutData(fdProxyPort);
        
        // useBasicAutentication
        wluseBasicAuthentication = new Label(wHTTPProxyGroup, SWT.RIGHT);
        wluseBasicAuthentication.setText(Messages.getString("JobSSH2GET.useBasicAuthentication.Label"));
        props.setLook(wluseBasicAuthentication);
        fdluseBasicAuthentication = new FormData();
        fdluseBasicAuthentication.left = new FormAttachment(0, 0);
        fdluseBasicAuthentication.top = new FormAttachment(wHTTPProxyPort, margin);
        fdluseBasicAuthentication.right = new FormAttachment(middle, 0);
        wluseBasicAuthentication.setLayoutData(fdluseBasicAuthentication);
        wuseBasicAuthentication = new Button(wHTTPProxyGroup, SWT.CHECK);
        wuseBasicAuthentication.setToolTipText(Messages.getString("JobSSH2GET.useBasicAuthentication.Tooltip"));
        props.setLook(wuseBasicAuthentication);
        fduseBasicAuthentication = new FormData();
        fduseBasicAuthentication.left = new FormAttachment(middle, margin);
        fduseBasicAuthentication.top = new FormAttachment(wHTTPProxyPort, margin);
        fduseBasicAuthentication.right = new FormAttachment(100, 0);
        wuseBasicAuthentication.setLayoutData(fduseBasicAuthentication);
        
        wuseBasicAuthentication.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				activeuseBasicAutentication();
				jobEntry.setChanged();
			}
		});
        
        // ProxyUsername line
        wHTTPProxyUsername = new LabelTextVar(jobMeta,wHTTPProxyGroup, Messages.getString("JobSSH2GET.HttpProxyUsername.Label"), Messages
            .getString("JobSSH2GET.HttpProxyUsername.Tooltip"));
        props.setLook(wHTTPProxyUsername);
        wHTTPProxyUsername.addModifyListener(lsMod);
        fdHTTPProxyUsername = new FormData();
        fdHTTPProxyUsername.left = new FormAttachment(0, 0);
        fdHTTPProxyUsername.top = new FormAttachment(wuseBasicAuthentication, margin);
        fdHTTPProxyUsername.right = new FormAttachment(100, 0);
        wHTTPProxyUsername.setLayoutData(fdHTTPProxyUsername);
        
        // HttpProxyPassword line
        wHTTPProxyPassword = new LabelTextVar(jobMeta,wHTTPProxyGroup, Messages.getString("JobSSH2GET.HttpProxyPassword.Label"), Messages
            .getString("JobSSH2GET.HttpProxyPassword.Tooltip"));
        props.setLook(wHTTPProxyPassword);
        wHTTPProxyPassword.setEchoChar('*');
        wHTTPProxyPassword.addModifyListener(lsMod);
        fdHTTPProxyPassword = new FormData();
        fdHTTPProxyPassword.left = new FormAttachment(0, 0);
        fdHTTPProxyPassword.top = new FormAttachment(wHTTPProxyUsername, margin);
        fdHTTPProxyPassword.right = new FormAttachment(100, 0);
        wHTTPProxyPassword.setLayoutData(fdHTTPProxyPassword);

        // OK, if the HttpProxyPassword contains a variable, we don't want to have the HttpProxyPassword hidden...
        wHTTPProxyPassword.getTextWidget().addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                checkPasswordVisible(wHTTPProxyPassword);
            }
        });
        
    	fdHTTPProxyGroup = new FormData();
    	fdHTTPProxyGroup.left = new FormAttachment(0, margin);
    	fdHTTPProxyGroup.top = new FormAttachment(wHost, margin);
    	fdHTTPProxyGroup.right = new FormAttachment(100, -margin);
		wHTTPProxyGroup.setLayoutData(fdHTTPProxyGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF HTTPProxy  GROUP
		///////////////////////////////////////////////////////////

       
		// ////////////////////////
		// START OF PublicKey GROUP
		// ////////////////////////

		wPublicKey = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wPublicKey);
		wPublicKey.setText(Messages.getString("JobSSH2GET.Group.PublicKey.Label"));
		
		FormLayout PublicKeyLayout = new FormLayout();
		PublicKeyLayout.marginWidth = 10;
		PublicKeyLayout.marginHeight = 10;
		wPublicKey.setLayout(PublicKeyLayout);
		
        // usePublicKey
        wlusePublicKey = new Label(wPublicKey, SWT.RIGHT);
        wlusePublicKey.setText(Messages.getString("JobSSH2GET.usePublicKeyFiles.Label"));
        props.setLook(wlusePublicKey);
        fdlusePublicKey = new FormData();
        fdlusePublicKey.left = new FormAttachment(0, 0);
        fdlusePublicKey.top = new FormAttachment(wHTTPProxyGroup, margin);
        fdlusePublicKey.right = new FormAttachment(middle, 0);
        wlusePublicKey.setLayoutData(fdlusePublicKey);
        wusePublicKey = new Button(wPublicKey, SWT.CHECK);
        wusePublicKey.setToolTipText(Messages.getString("JobSSH2GET.usePublicKeyFiles.Tooltip"));
        props.setLook(wusePublicKey);
        fdusePublicKey = new FormData();
        fdusePublicKey.left = new FormAttachment(middle, margin);
        fdusePublicKey.top = new FormAttachment(wHTTPProxyGroup, margin);
        fdusePublicKey.right = new FormAttachment(100, 0);
        wusePublicKey.setLayoutData(fdusePublicKey);
        wusePublicKey.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				activeUsePublicKey();
				jobEntry.setChanged();
			}
		});
        
        // Key File
        wlKeyFilename = new Label(wPublicKey, SWT.RIGHT);
        wlKeyFilename.setText(Messages.getString("JobSSH2GET.KeyFilename.Label"));
        props.setLook(wlKeyFilename);
        fdlKeyFilename = new FormData();
        fdlKeyFilename.left = new FormAttachment(0, 0);
        fdlKeyFilename.top = new FormAttachment(wusePublicKey, margin);
        fdlKeyFilename.right = new FormAttachment(middle, -margin);
        wlKeyFilename.setLayoutData(fdlKeyFilename);

        wbKeyFilename = new Button(wPublicKey, SWT.PUSH | SWT.CENTER);
        props.setLook(wbKeyFilename);
        wbKeyFilename.setText(Messages.getString("System.Button.Browse"));
        fdbKeyFilename = new FormData();
        fdbKeyFilename.right = new FormAttachment(100, 0);
        fdbKeyFilename.top = new FormAttachment(wusePublicKey, 0);
        // fdbKeyFilename.height = 22;
        wbKeyFilename.setLayoutData(fdbKeyFilename);

        wKeyFilename = new TextVar(jobMeta,wPublicKey, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wKeyFilename.setToolTipText(Messages.getString("JobSSH2GET.KeyFilename.Tooltip"));
        props.setLook(wKeyFilename);
        wKeyFilename.addModifyListener(lsMod);
        fdKeyFilename = new FormData();
        fdKeyFilename.left = new FormAttachment(middle, margin);
        fdKeyFilename.top = new FormAttachment(wusePublicKey, margin);
        fdKeyFilename.right = new FormAttachment(wbKeyFilename, -margin);
        wKeyFilename.setLayoutData(fdKeyFilename);

        // Whenever something changes, set the tooltip to the expanded version:
        wKeyFilename.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                wKeyFilename.setToolTipText(jobMeta.environmentSubstitute(wKeyFilename.getText()));
            }
        });

        wbKeyFilename.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setFilterExtensions(new String[] { "*.pem", "*" });
                if (wKeyFilename.getText() != null)
                {
                    dialog.setFileName(jobMeta.environmentSubstitute(wKeyFilename.getText()));
                }
                dialog.setFilterNames(FILETYPES);
                if (dialog.open() != null)
                {
                    wKeyFilename.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName());
                }
            }
        });

        // keyfilePass line
        wkeyfilePass = new LabelTextVar(jobMeta,wPublicKey, Messages.getString("JobSSH2GET.keyfilePass.Label"), Messages
            .getString("JobSSH2GET.keyfilePass.Tooltip"));
        props.setLook(wkeyfilePass);
        wkeyfilePass.setEchoChar('*');
        wkeyfilePass.addModifyListener(lsMod);
        fdkeyfilePass = new FormData();
        fdkeyfilePass.left = new FormAttachment(0, 0);
        fdkeyfilePass.top = new FormAttachment(wKeyFilename, margin);
        fdkeyfilePass.right = new FormAttachment(100, 0);
        wkeyfilePass.setLayoutData(fdkeyfilePass);

        // OK, if the keyfilePass contains a variable, we don't want to have the keyfilePass hidden...
        wkeyfilePass.getTextWidget().addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
            	checkPasswordVisible(wkeyfilePass);
            }
        });
        


    	fdPublicKey = new FormData();
    	fdPublicKey.left = new FormAttachment(0, margin);
    	fdPublicKey.top = new FormAttachment(wHTTPProxyGroup, margin);
    	fdPublicKey.right = new FormAttachment(100, -margin);
		wPublicKey.setLayoutData(fdPublicKey);
		
		// ///////////////////////////////////////////////////////////
		// / END OF PublicKey GROUP
		///////////////////////////////////////////////////////////
        
		


		fdGeneralComp=new FormData();
		fdGeneralComp.left  = new FormAttachment(0, 0);
		fdGeneralComp.top   = new FormAttachment(0, 0);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.bottom= new FormAttachment(500, -margin);
		wGeneralComp.setLayoutData(fdGeneralComp);
		
		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);
 		props.setLook(wGeneralComp);
 		
 		
 		
		/////////////////////////////////////////////////////////////
		/// END OF GENERAL TAB
		/////////////////////////////////////////////////////////////

 		//////////////////////////
		// START OF FILES TAB   ///
		//////////////////////////

		wFilesTab=new CTabItem(wTabFolder, SWT.NONE);
		wFilesTab.setText(Messages.getString("JobSSH2GET.Tab.Files.Label"));
		
		wFilesComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wFilesComp);

		FormLayout FilesTabLayout = new FormLayout();
		FilesTabLayout.marginWidth  = 3;
		FilesTabLayout.marginHeight = 3;
		wFilesComp.setLayout(FilesTabLayout);
		
		// ////////////////////////
		// START OF Files GROUP
		// ////////////////////////

		wFiles = new Group(wFilesComp, SWT.SHADOW_NONE);
		props.setLook(wFiles);
		wFiles.setText(Messages.getString("JobSSH2GET.Group.Files.Label"));
		
		FormLayout FilesLayout = new FormLayout();
		FilesLayout.marginWidth = 10;
		FilesLayout.marginHeight = 10;
		wFiles.setLayout(FilesLayout);
       
        
        // FtpDirectory line
        wFtpDirectory = new LabelTextVar(jobMeta,wFiles, Messages.getString("JobSSH2GET.RemoteDir.Label"),
            Messages.getString("JobSSH2GET.RemoteDir.Tooltip"));
        props.setLook(wFtpDirectory);
        wFtpDirectory.addModifyListener(lsMod);
        fdFtpDirectory = new FormData();
        fdFtpDirectory.left = new FormAttachment(0, 0);
        fdFtpDirectory.top = new FormAttachment(0, margin);
        fdFtpDirectory.right = new FormAttachment(100, 0);
        wFtpDirectory.setLayoutData(fdFtpDirectory);
        // Whenever something changes, set the tooltip to the expanded version:
        wFtpDirectory.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				wFtpDirectory.setToolTipText(jobMeta.environmentSubstitute( wFtpDirectory.getText() ) );
			}
		}
		);  
        
        // includeSubFolders 
        wlincludeSubFolders = new Label(wFiles, SWT.RIGHT);
        wlincludeSubFolders.setText(Messages.getString("JobSSH2GET.includeSubFolders.Label"));
        props.setLook(wlincludeSubFolders);
        fdlincludeSubFolders = new FormData();
        fdlincludeSubFolders.left = new FormAttachment(0, 0);
        fdlincludeSubFolders.top = new FormAttachment(wFtpDirectory, margin);
        fdlincludeSubFolders.right = new FormAttachment(middle, 0);
        wlincludeSubFolders.setLayoutData(fdlincludeSubFolders);
        wincludeSubFolders = new Button(wFiles, SWT.CHECK);
        wincludeSubFolders.setToolTipText(Messages.getString("JobSSH2GET.includeSubFolders.Tooltip"));
        props.setLook(wincludeSubFolders);
        fdincludeSubFolders = new FormData();
        fdincludeSubFolders.left = new FormAttachment(middle, margin);
        fdincludeSubFolders.top = new FormAttachment(wFtpDirectory, margin);
        fdincludeSubFolders.right = new FormAttachment(100, 0);
        wincludeSubFolders.setLayoutData(fdincludeSubFolders);
        
        wincludeSubFolders.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				activeuseBasicAutentication();
			}
		});
        
        
        // Wildcard line
        wWildcard = new LabelTextVar(jobMeta,wFiles, Messages.getString("JobSSH2GET.Wildcard.Label"), Messages
            .getString("JobSSH2GET.Wildcard.Tooltip"));
        props.setLook(wWildcard);
        wWildcard.addModifyListener(lsMod);
        fdWildcard = new FormData();
        fdWildcard.left = new FormAttachment(0, 0);
        fdWildcard.top = new FormAttachment(wincludeSubFolders, margin);
        fdWildcard.right = new FormAttachment(100, 0);
        wWildcard.setLayoutData(fdWildcard);
		
        wlLocalDirectory=new Label(wFiles, SWT.RIGHT);
		wlLocalDirectory.setText(Messages.getString("JobSSH2GET.TargetDir.Label"));
		props.setLook(wlLocalDirectory);
		fdlLocalDirectory=new FormData();
		fdlLocalDirectory.left = new FormAttachment(0, 0);
		fdlLocalDirectory.top  = new FormAttachment(wWildcard, 2*margin);
		fdlLocalDirectory.right= new FormAttachment(middle, -margin);
		wlLocalDirectory.setLayoutData(fdlLocalDirectory);
        
    	// Browse Local folders button ...
		wbLocalDirectory=new Button(wFiles, SWT.PUSH| SWT.CENTER);
		props.setLook(wbLocalDirectory);
		wbLocalDirectory.setText(Messages.getString("JobSSH2GET.BrowseFolders.Label"));
		fdbLocalDirectory=new FormData();
		fdbLocalDirectory.right= new FormAttachment(100, -margin);
		fdbLocalDirectory.top  = new FormAttachment(wWildcard, margin);
		wbLocalDirectory.setLayoutData(fdbLocalDirectory);
		
        // LocalDirectory line
		
		wLocalDirectory=new TextVar(jobMeta,wFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wLocalDirectory);
		wLocalDirectory.addModifyListener(lsMod);
		fdLocalDirectory=new FormData();
		fdLocalDirectory.left = new FormAttachment(middle, margin);
		fdLocalDirectory.top  = new FormAttachment(wWildcard, 2*margin);
		fdLocalDirectory.right= new FormAttachment(wbLocalDirectory, 0);
		wLocalDirectory.setLayoutData(fdLocalDirectory);
	

		// Whenever something changes, set the tooltip to the expanded version:
        wLocalDirectory.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				wLocalDirectory.setToolTipText(jobMeta.environmentSubstitute( wLocalDirectory.getText() ) );
			}
		}
		);

		
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
        // createTargetFolder 
        wlcreateTargetFolder = new Label(wFiles, SWT.RIGHT);
        wlcreateTargetFolder.setText(Messages.getString("JobSSH2GET.createTargetFolder.Label"));
        props.setLook(wlcreateTargetFolder);
        fdlcreateTargetFolder = new FormData();
        fdlcreateTargetFolder.left = new FormAttachment(0, 0);
        fdlcreateTargetFolder.top = new FormAttachment(wLocalDirectory, margin);
        fdlcreateTargetFolder.right = new FormAttachment(middle, 0);
        wlcreateTargetFolder.setLayoutData(fdlcreateTargetFolder);
        wcreateTargetFolder = new Button(wFiles, SWT.CHECK);
        wcreateTargetFolder.setToolTipText(Messages.getString("JobSSH2GET.createTargetFolder.Tooltip"));
        props.setLook(wcreateTargetFolder);
        fdcreateTargetFolder = new FormData();
        fdcreateTargetFolder.left = new FormAttachment(middle, margin);
        fdcreateTargetFolder.top = new FormAttachment(wLocalDirectory, margin);
        fdcreateTargetFolder.right = new FormAttachment(100, 0);
        wcreateTargetFolder.setLayoutData(fdcreateTargetFolder);
        
        wcreateTargetFolder.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				activeuseBasicAutentication();
			}
		});
        
		
        // OnlyNew files after retrieval...
        wlOnlyNew = new Label(wFiles, SWT.RIGHT);
        wlOnlyNew.setText(Messages.getString("JobSSH2GET.DontOverwrite.Label"));
        props.setLook(wlOnlyNew);
        fdlOnlyNew = new FormData();
        fdlOnlyNew.left = new FormAttachment(0, 0);
        fdlOnlyNew.top = new FormAttachment(wcreateTargetFolder, margin);
        fdlOnlyNew.right = new FormAttachment(middle, 0);
        wlOnlyNew.setLayoutData(fdlOnlyNew);
        wOnlyNew = new Button(wFiles, SWT.CHECK);
        wOnlyNew.setToolTipText(Messages.getString("JobSSH2GET.DontOverwrite.Tooltip"));
        props.setLook(wOnlyNew);
        fdOnlyNew = new FormData();
        fdOnlyNew.left = new FormAttachment(middle, margin);
        fdOnlyNew.top = new FormAttachment(wcreateTargetFolder, margin);
        fdOnlyNew.right = new FormAttachment(100, 0);
        wOnlyNew.setLayoutData(fdOnlyNew);
        
    	//After FTP Put
		wlAfterFTPPut = new Label(wFiles, SWT.RIGHT);
		wlAfterFTPPut.setText(Messages.getString("JobSSH2GET.AfterFTPPut.Label"));
		props.setLook(wlAfterFTPPut);
		fdlAfterFTPPut = new FormData();
		fdlAfterFTPPut.left = new FormAttachment(0, 0);
		fdlAfterFTPPut.right = new FormAttachment(middle, 0);
		fdlAfterFTPPut.top = new FormAttachment(wOnlyNew, 2*margin);
		wlAfterFTPPut.setLayoutData(fdlAfterFTPPut);
		wAfterFTPPut = new CCombo(wFiles, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wAfterFTPPut.add(Messages.getString("JobSSH2GET.Do_Nothing_AfterFTPPut.Label"));
		wAfterFTPPut.add(Messages.getString("JobSSH2GET.Delete_Files_AfterFTPPut.Label"));
		wAfterFTPPut.add(Messages.getString("JobSSH2GET.Move_Files_AfterFTPPut.Label"));

		wAfterFTPPut.select(0); // +1: starts at -1

		props.setLook(wAfterFTPPut);
		fdAfterFTPPut= new FormData();
		fdAfterFTPPut.left = new FormAttachment(middle, margin);
		fdAfterFTPPut.top = new FormAttachment(wOnlyNew, 2*margin);
		fdAfterFTPPut.right = new FormAttachment(100, 0);
		wAfterFTPPut.setLayoutData(fdAfterFTPPut);

		wAfterFTPPut.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				AfterFTPPutActivate();
				
			}
		});

		// moveTo Directory
		wlDestinationFolder = new Label(wFiles, SWT.RIGHT);
		wlDestinationFolder.setText(Messages.getString("JobSSH2GET.DestinationFolder.Label"));
		props.setLook(wlDestinationFolder);
		fdlDestinationFolder = new FormData();
		fdlDestinationFolder.left = new FormAttachment(0, 0);
		fdlDestinationFolder.top = new FormAttachment(wAfterFTPPut, margin);
		fdlDestinationFolder.right = new FormAttachment(middle, -margin);
		wlDestinationFolder.setLayoutData(fdlDestinationFolder);
		
		
		wDestinationFolder = new TextVar(jobMeta,wFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages
			.getString("JobSSH2GET.DestinationFolder.Tooltip"));
		props.setLook(wDestinationFolder);
		wDestinationFolder.addModifyListener(lsMod);
		fdDestinationFolder = new FormData();
		fdDestinationFolder.left = new FormAttachment(middle, margin);
		fdDestinationFolder.top = new FormAttachment(wAfterFTPPut, margin);
		fdDestinationFolder.right = new FormAttachment(100, -margin);
		wDestinationFolder.setLayoutData(fdDestinationFolder);
		
		   // Whenever something changes, set the tooltip to the expanded version:
		wDestinationFolder.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				wDestinationFolder.setToolTipText(jobMeta.environmentSubstitute( wDestinationFolder.getText() ) );
			}
		}
		);

        
        // Create destination folder if necessary ...
        wlCreateDestinationFolder = new Label(wFiles, SWT.RIGHT);
        wlCreateDestinationFolder.setText(Messages.getString("JobSSH2GET.CreateDestinationFolder.Label"));
        props.setLook(wlCreateDestinationFolder);
        fdlCreateDestinationFolder = new FormData();
        fdlCreateDestinationFolder.left = new FormAttachment(0, 0);
        fdlCreateDestinationFolder.top = new FormAttachment(wDestinationFolder, margin);
        fdlCreateDestinationFolder.right = new FormAttachment(middle, 0);
        wlCreateDestinationFolder.setLayoutData(fdlCreateDestinationFolder);
        wCreateDestinationFolder = new Button(wFiles, SWT.CHECK);
        wCreateDestinationFolder.setToolTipText(Messages.getString("JobSSH2GET.CreateDestinationFolder.Tooltip"));
        props.setLook(wCreateDestinationFolder);
        fdCreateDestinationFolder = new FormData();
        fdCreateDestinationFolder.left = new FormAttachment(middle, margin);
        fdCreateDestinationFolder.top = new FormAttachment(wDestinationFolder, margin);
        fdCreateDestinationFolder.right = new FormAttachment(100, 0);
        wCreateDestinationFolder.setLayoutData(fdCreateDestinationFolder);
        
    	fdFiles = new FormData();
    	fdFiles.left = new FormAttachment(0, margin);
    	fdFiles.top = new FormAttachment(0, margin);
    	fdFiles.right = new FormAttachment(100, -margin);
		wFiles.setLayoutData(fdFiles);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Files GROUP
		/////////////////////////////////////////////////////////// 

		
		


		fdFilesComp=new FormData();
		fdFilesComp.left  = new FormAttachment(0, 0);
		fdFilesComp.top   = new FormAttachment(0, 0);
		fdFilesComp.right = new FormAttachment(100, 0);
		fdFilesComp.bottom= new FormAttachment(500, -margin);
		wFilesComp.setLayoutData(fdFilesComp);
		/////////////////////////////////////////////////////////////
		/// END OF GENERAL TAB
		/////////////////////////////////////////////////////////////
		
		wFilesComp.layout();
		wFilesTab.setControl(wFilesComp);
 		props.setLook(wFilesComp);
		
		
		
		
		
		
		
 		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wName, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);

 		
		
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wTabFolder);

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
        wLocalDirectory.addSelectionListener(lsDef);
        wFtpDirectory.addSelectionListener(lsDef);
        wWildcard.addSelectionListener(lsDef);
        //wTimeout.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

        getData();
        activeUseHttpProxy();
        AfterFTPPutActivate();
        activeUsePublicKey();

        BaseStepDialog.setSize(shell);

        shell.open();
        props.setDialogSize(shell, "JobFTPDialogSize");
        wTabFolder.setSelection(0);
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
    }

    public void checkPasswordVisible(LabelTextVar variable)
    {
        String password = variable.getText();
        List list = new ArrayList();
        //StringUtil.getUsedVariables(password, list, true);
        if (list.size() == 0)
        {
        	variable.setEchoChar('*');
        }
        else
        {
        	variable.setEchoChar('\0'); // Show it all...
        }
    }

    public void dispose()
    {
        WindowProperty winprop = new WindowProperty(shell);
        props.setScreen(winprop);
        shell.dispose();
    }
    private void activeUseHttpProxy()
    {
    	wHTTPProxyHost.setEnabled(wuseHTTPProxy.getSelection());
    	wlHTTPProxyPort.setEnabled(wuseHTTPProxy.getSelection());
    	wHTTPProxyPort.setEnabled(wuseHTTPProxy.getSelection());
 
    	wluseBasicAuthentication.setEnabled(wuseHTTPProxy.getSelection());
    	wuseBasicAuthentication.setEnabled(wuseHTTPProxy.getSelection());
    	activeuseBasicAutentication();
    	
    }

    private void activeUsePublicKey()
    {
    	wlKeyFilename.setEnabled(wusePublicKey.getSelection());
    	wKeyFilename.setEnabled(wusePublicKey.getSelection());
    	wbKeyFilename.setEnabled(wusePublicKey.getSelection());
    	wkeyfilePass.setEnabled(wusePublicKey.getSelection());
    }
    private void activeuseBasicAutentication()
    {
    	wHTTPProxyUsername.setEnabled(wuseBasicAuthentication.getSelection());
    	wHTTPProxyPassword.setEnabled(wuseBasicAuthentication.getSelection());
    }
    private void AfterFTPPutActivate()
    {
    	wlDestinationFolder.setEnabled(wAfterFTPPut.getSelectionIndex()==2);
    	wDestinationFolder.setEnabled(wAfterFTPPut.getSelectionIndex()==2);
    	wlCreateDestinationFolder.setEnabled(wAfterFTPPut.getSelectionIndex()==2);
    	wCreateDestinationFolder.setEnabled(wAfterFTPPut.getSelectionIndex()==2);
    	
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
        if(jobEntry.getServerPort()!=null) wServerPort.setText(jobEntry.getServerPort());
        wFtpDirectory.setText(Const.NVL(jobEntry.getFtpDirectory(), ""));
        wLocalDirectory.setText(Const.NVL(jobEntry.getlocalDirectory(), ""));
        wWildcard.setText(Const.NVL(jobEntry.getWildcard(), ""));
        wOnlyNew.setSelection(jobEntry.isOnlyGettingNewFiles());
        
        wuseHTTPProxy.setSelection(jobEntry.isUseHTTPProxy());
        if(jobEntry.getHTTPProxyHost()!=null) wHTTPProxyHost.setText(jobEntry.getHTTPProxyHost());
        if(jobEntry.getHTTPProxyPort()!=null) wHTTPProxyPort.setText(jobEntry.getHTTPProxyPort());
        if(jobEntry.getHTTPProxyUsername()!=null) wHTTPProxyUsername.setText(jobEntry.getHTTPProxyUsername());
        if(jobEntry.getHTTPProxyPassword()!=null) wHTTPProxyPassword.setText(jobEntry.getHTTPProxyPassword());
        
        wusePublicKey.setSelection(jobEntry.isUsePublicKey());
        if(jobEntry.getKeyFilename()!=null) wKeyFilename.setText(jobEntry.getKeyFilename());
        if(jobEntry.getKeyFilepass()!=null) wkeyfilePass.setText(jobEntry.getKeyFilepass());
        
        wuseBasicAuthentication.setSelection(jobEntry.isUseBasicAuthentication());
        if(jobEntry.getAfterFTPPut()!=null)
        {
        	if(jobEntry.getAfterFTPPut().equals("delete_file"))
        		wAfterFTPPut.select(1);
        	else if(jobEntry.getAfterFTPPut().equals("move_file"))
        		wAfterFTPPut.select(2);
        	else
        		wAfterFTPPut.select(0);
        }else wAfterFTPPut.select(0);
        
        if(jobEntry.getDestinationFolder()!=null) wDestinationFolder.setText(jobEntry.getDestinationFolder());
        wCreateDestinationFolder.setSelection(jobEntry.isCreateDestinationFolder());
        wcacheHostKey.setSelection(jobEntry.isCacheHostKey());
        if(jobEntry.getTimeout()>0)
        	wTimeout.setText(""+jobEntry.getTimeout());
        else
        	wTimeout.setText("0");
        
        wcreateTargetFolder.setSelection(jobEntry.isCreateTargetFolder());
        wincludeSubFolders.setSelection(jobEntry.isIncludeSubFolders());

        
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
        jobEntry.setServerPort(wServerPort.getText());
        jobEntry.setFtpDirectory(wFtpDirectory.getText());
        jobEntry.setlocalDirectory(wLocalDirectory.getText());
        jobEntry.setWildcard(wWildcard.getText());
        jobEntry.setOnlyGettingNewFiles(wOnlyNew.getSelection());
        
        jobEntry.setUseHTTPProxy(wuseHTTPProxy.getSelection());
        jobEntry.setHTTPProxyHost(wHTTPProxyHost.getText());
        jobEntry.setHTTPProxyPort(wHTTPProxyPort.getText());
        jobEntry.setHTTPProxyUsername(wHTTPProxyUsername.getText());
        jobEntry.setHTTPProxyPassword(wHTTPProxyPassword.getText());
        
        jobEntry.setUsePublicKey(wusePublicKey.getSelection());
        jobEntry.setKeyFilename(wKeyFilename.getText());
        jobEntry.setKeyFilepass(wkeyfilePass.getText());
        
        jobEntry.setUseBasicAuthentication(wuseBasicAuthentication.getSelection());
        
     
        if(wAfterFTPPut.getSelectionIndex()==1)
        	jobEntry.setAfterFTPPut("delete_file");
        else if(wAfterFTPPut.getSelectionIndex()==2)
        	jobEntry.setAfterFTPPut("move_file");
        else
        	jobEntry.setAfterFTPPut("do_nothing");
        
        
        jobEntry.setDestinationFolder(wDestinationFolder.getText());
        jobEntry.setCreateDestinationFolder(wCreateDestinationFolder.getSelection());
        jobEntry.setCacheHostKey(wcacheHostKey.getSelection());
        jobEntry.setTimeout(Const.toInt(wTimeout.getText(),0));
        
        jobEntry.setCreateTargetFolder(wcreateTargetFolder.getSelection());
        jobEntry.setIncludeSubFolders(wincludeSubFolders.getSelection());
        
        
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
