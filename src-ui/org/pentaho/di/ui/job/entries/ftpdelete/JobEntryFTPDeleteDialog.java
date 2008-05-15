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

package org.pentaho.di.ui.job.entries.ftpdelete;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
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
import org.pentaho.di.job.entries.ftpdelete.JobEntryFTPDelete;
import org.pentaho.di.job.entries.ftpdelete.Messages;
import org.pentaho.di.job.entries.sftp.SFTPClient;
import org.pentaho.di.ui.core.widget.TextVar;
import org.eclipse.swt.widgets.FileDialog;

import com.enterprisedt.net.ftp.FTPClient;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.HTTPProxyData;
import com.trilead.ssh2.SFTPv3Client;
import com.trilead.ssh2.SFTPv3FileAttributes;

/**
 * This dialog allows you to edit the FTP Delete job entry settings. 
 * 
 * @author Samatar
 * @since 27-04-2008
 */
public class JobEntryFTPDeleteDialog extends JobEntryDialog implements JobEntryDialogInterface
{
    private LabelText wName;

    private FormData fdName;

    private LabelTextVar wServerName;

    private FormData fdServerName;

    private LabelTextVar wUserName;

    private FormData fdUserName;

    private LabelTextVar wPassword;

    private FormData fdPassword;

    private TextVar wFtpDirectory;
    private Label wlFtpDirectory;

    private FormData fdFtpDirectory;
    private FormData fdlFtpDirectory;

    private LabelTextVar wWildcard;

    private FormData fdWildcard;
    
    private Label wluseProxy;

    private Button wuseProxy;

    private FormData fdluseProxy, fduseProxy;

    private LabelTextVar wTimeout;

    private FormData fdTimeout;

    private Label wlActive;

    private Button wActive;

    private FormData fdlActive, fdActive;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntryFTPDelete jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;
    
    
    private Label        wlProtocol;
    
    private Combo        wProtocol;
    
    private FormData     fdlProtocol, fdProtocol;
    
    private Label wlusePublicKey;

    private Button wusePublicKey;

    private FormData fdlusePublicKey, fdusePublicKey;

    private boolean changed;
  
	private Group wServerSettings;
    private FormData fdServerSettings;
    
	private CTabFolder   wTabFolder;
	private Composite    wGeneralComp,wFilesComp;	
	private CTabItem     wGeneralTab,wFilesTab;
	private FormData	 fdGeneralComp,fdFilesComp;
	private FormData     fdTabFolder;
	
    private LabelTextVar wPort;

    private FormData     fdPort;
	
    private LabelTextVar wProxyHost;

    private FormData     fdProxyHost;

    private LabelTextVar wProxyPort;

    private FormData     fdProxyPort;

    private LabelTextVar wProxyUsername;

    private FormData     fdProxyUsername;
    
    private LabelTextVar wProxyPassword;
    
    private FormData     fdProxyPasswd;
    
	private Button wTest;
	
	private FormData fdTest;
	
	private Listener lsTest;
	
	private Listener lsCheckFolder;
	
	private Group wAdvancedSettings;
    private FormData fdAdvancedSettings;
    
	private Group wRemoteSettings;
    private FormData fdRemoteSettings; 
    
	
	private Button wbTestChangeFolderExists;
	private FormData fdbTestChangeFolderExists;
	
	private Group wSuccessOn;
    private FormData fdSuccessOn;
    
	
	private Label wlNrErrorsLessThan;
	private TextVar wNrErrorsLessThan;
	private FormData fdlNrErrorsLessThan, fdNrErrorsLessThan;
	
	private Label wlSuccessCondition;
	private CCombo wSuccessCondition;
	private FormData fdlSuccessCondition, fdSuccessCondition;
	
    private LabelTextVar wkeyfilePass;

    private FormData fdkeyfilePass;
    
    
    private Label wlKeyFilename;

    private Button wbKeyFilename;

    private TextVar wKeyFilename;

    private FormData fdlKeyFilename, fdbKeyFilename, fdKeyFilename;
    
    private Label wlgetPrevious;

    private Button wgetPrevious;

    private FormData fdlgetPrevious, fdgetPrevious;
    
	
	private FTPClient ftpclient = null;
	private SFTPClient sftpclient = null;
	private Connection conn = null;
    
    
    private static final String[] FILETYPES = new String[] {
        Messages.getString("JobFTPDelete.Filetype.Pem"),
        Messages.getString("JobFTPDelete.Filetype.All") };

    //
    // Original code used to fill encodings, this display all possibilities but
    // takes 10 seconds on my pc to fill.
    //
    // static {
    //     SortedMap charsetMap = Charset.availableCharsets();
    //    Set charsetSet = charsetMap.keySet();
    //    encodings = (String [])charsetSet.toArray(new String[0]);
    // }

    public JobEntryFTPDeleteDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryFTPDelete) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(Messages.getString("JobFTPDelete.Name.Default"));
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
            	ftpclient=null;
            	sftpclient=null;
            	conn=null;
                jobEntry.setChanged();
            }
        };
        changed = jobEntry.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(Messages.getString("JobFTPDelete.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Job entry name line
        wName = new LabelText(shell, Messages.getString("JobFTPDelete.Name.Label"), Messages
            .getString("JobFTPDelete.Name.Tooltip"));
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
		wGeneralTab.setText(Messages.getString("JobFTPDelete.Tab.General.Label"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
        
	     // ////////////////////////
	     // START OF SERVER SETTINGS GROUP///
	     // /
	    wServerSettings = new Group(wGeneralComp, SWT.SHADOW_NONE);
	    props.setLook(wServerSettings);
	    wServerSettings.setText(Messages.getString("JobFTPDelete.ServerSettings.Group.Label"));

	    FormLayout ServerSettingsgroupLayout = new FormLayout();
	    ServerSettingsgroupLayout.marginWidth = 10;
	    ServerSettingsgroupLayout.marginHeight = 10;

	    wServerSettings.setLayout(ServerSettingsgroupLayout);

	    // Protocol
        wlProtocol=new Label(wServerSettings, SWT.RIGHT);
        wlProtocol.setText(Messages.getString("JobFTPDelete.Protocol.Label"));
        props.setLook(wlProtocol);
        fdlProtocol=new FormData();
        fdlProtocol.left  = new FormAttachment(0, 0);
        fdlProtocol.top   = new FormAttachment(wName, margin);
        fdlProtocol.right = new FormAttachment(middle, 0);
        wlProtocol.setLayoutData(fdlProtocol);
        wProtocol=new Combo(wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wProtocol.setToolTipText(Messages.getString("JobFTPDelete.Protocol.Tooltip"));
        wProtocol.add("FTP");
        wProtocol.add("SFTP");
        wProtocol.add("SSH");
        props.setLook(wProtocol);
        fdProtocol=new FormData();
        fdProtocol.left = new FormAttachment(middle, margin);
        fdProtocol.top  = new FormAttachment(wName, margin);
        fdProtocol.right= new FormAttachment(100, 0);        
        wProtocol.setLayoutData(fdProtocol);
        wProtocol.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				 activeFTPProtocol();
				jobEntry.setChanged();
			}
		});
	    
       
	    
        // ServerName line
        wServerName = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTPDelete.Server.Label"), Messages
            .getString("JobFTPDelete.Server.Tooltip"));
        props.setLook(wServerName);
        wServerName.addModifyListener(lsMod);
        fdServerName = new FormData();
        fdServerName.left = new FormAttachment(0, 0);
        fdServerName.top = new FormAttachment(wProtocol, margin);
        fdServerName.right = new FormAttachment(100, 0);
        wServerName.setLayoutData(fdServerName);
        
        // Proxy port line
        wPort = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTPDelete.Port.Label"), Messages.getString("JobFTPDelete.Port.Tooltip"));
        props.setLook(wPort);
        wPort.addModifyListener(lsMod);
        fdPort = new FormData();
        fdPort.left 	= new FormAttachment(0, 0);
        fdPort.top  	= new FormAttachment(wServerName, margin);
        fdPort.right	= new FormAttachment(100, 0);
        wPort.setLayoutData(fdPort);

        // UserName line
        wUserName = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTPDelete.User.Label"), Messages
            .getString("JobFTPDelete.User.Tooltip"));
        props.setLook(wUserName);
        wUserName.addModifyListener(lsMod);
        fdUserName = new FormData();
        fdUserName.left = new FormAttachment(0, 0);
        fdUserName.top = new FormAttachment(wPort, margin);
        fdUserName.right = new FormAttachment(100, 0);
        wUserName.setLayoutData(fdUserName);

        // Password line
        wPassword = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTPDelete.Password.Label"), Messages
            .getString("JobFTPDelete.Password.Tooltip"));
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

        
        // Use proxy...
        wluseProxy = new Label(wServerSettings, SWT.RIGHT);
        wluseProxy.setText(Messages.getString("JobFTPDelete.useProxy.Label"));
        props.setLook(wluseProxy);
        fdluseProxy = new FormData();
        fdluseProxy.left = new FormAttachment(0, 0);
        fdluseProxy.top = new FormAttachment(wPassword, margin);
        fdluseProxy.right = new FormAttachment(middle, 0);
        wluseProxy.setLayoutData(fdluseProxy);
        wuseProxy = new Button(wServerSettings, SWT.CHECK);
        props.setLook(wuseProxy);
        wuseProxy.setToolTipText(Messages.getString("JobFTPDelete.useProxy.Tooltip"));
        fduseProxy = new FormData();
        fduseProxy.left = new FormAttachment(middle, margin);
        fduseProxy.top = new FormAttachment(wPassword, margin);
        fduseProxy.right = new FormAttachment(100, 0);
        wuseProxy.setLayoutData(fduseProxy);
        wuseProxy.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				 activeProxy();
				jobEntry.setChanged();
			}
		});
      
        
        // Proxy host line
        wProxyHost = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTPDelete.ProxyHost.Label"), Messages.getString("JobFTPDelete.ProxyHost.Tooltip"));
        props.setLook(wProxyHost);
        wProxyHost.addModifyListener(lsMod);
        fdProxyHost = new FormData();
        fdProxyHost.left 	= new FormAttachment(0, 0);
        fdProxyHost.top		= new FormAttachment(wuseProxy, margin);
        fdProxyHost.right	= new FormAttachment(100, 0);
        wProxyHost.setLayoutData(fdProxyHost);

        // Proxy port line
        wProxyPort = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTPDelete.ProxyPort.Label"), Messages.getString("JobFTPDelete.ProxyPort.Tooltip"));
        props.setLook(wProxyPort);
        wProxyPort.addModifyListener(lsMod);
        fdProxyPort = new FormData();
        fdProxyPort.left 	= new FormAttachment(0, 0);
        fdProxyPort.top  	= new FormAttachment(wProxyHost, margin);
        fdProxyPort.right	= new FormAttachment(100, 0);
        wProxyPort.setLayoutData(fdProxyPort);

        // Proxy username line
        wProxyUsername = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTPDelete.ProxyUsername.Label"), Messages.getString("JobFTPDelete.ProxyUsername.Tooltip"));
        props.setLook(wProxyUsername);
        wProxyUsername.addModifyListener(lsMod);
        fdProxyUsername = new FormData();
        fdProxyUsername.left = new FormAttachment(0, 0);
        fdProxyUsername.top  = new FormAttachment(wProxyPort, margin);
        fdProxyUsername.right= new FormAttachment(100, 0);
        wProxyUsername.setLayoutData(fdProxyUsername);
        
        // Proxy password line
        wProxyPassword = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTPDelete.ProxyPassword.Label"), Messages.getString("JobFTPDelete.ProxyPassword.Tooltip"));
        props.setLook(wProxyPassword);
        wProxyPassword.addModifyListener(lsMod);
        fdProxyPasswd=new FormData();
        fdProxyPasswd.left = new FormAttachment(0, 0);
        fdProxyPasswd.top  = new FormAttachment(wProxyUsername, margin);
        fdProxyPasswd.right= new FormAttachment(100, 0);
        wProxyPassword.setLayoutData(fdProxyPasswd);
        
        
        // usePublicKey
        wlusePublicKey = new Label(wServerSettings, SWT.RIGHT);
        wlusePublicKey.setText(Messages.getString("JobFTPDelete.usePublicKeyFiles.Label"));
        props.setLook(wlusePublicKey);
        fdlusePublicKey = new FormData();
        fdlusePublicKey.left = new FormAttachment(0, 0);
        fdlusePublicKey.top = new FormAttachment(wProxyPassword, margin);
        fdlusePublicKey.right = new FormAttachment(middle, 0);
        wlusePublicKey.setLayoutData(fdlusePublicKey);
        wusePublicKey = new Button(wServerSettings, SWT.CHECK);
        wusePublicKey.setToolTipText(Messages.getString("JobFTPDelete.usePublicKeyFiles.Tooltip"));
        props.setLook(wusePublicKey);
        fdusePublicKey = new FormData();
        fdusePublicKey.left = new FormAttachment(middle, margin);
        fdusePublicKey.top = new FormAttachment(wProxyPassword, margin);
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
        wlKeyFilename = new Label(wServerSettings, SWT.RIGHT);
        wlKeyFilename.setText(Messages.getString("JobFTPDelete.KeyFilename.Label"));
        props.setLook(wlKeyFilename);
        fdlKeyFilename = new FormData();
        fdlKeyFilename.left = new FormAttachment(0, 0);
        fdlKeyFilename.top = new FormAttachment(wusePublicKey, margin);
        fdlKeyFilename.right = new FormAttachment(middle, -margin);
        wlKeyFilename.setLayoutData(fdlKeyFilename);

        wbKeyFilename = new Button(wServerSettings, SWT.PUSH | SWT.CENTER);
        props.setLook(wbKeyFilename);
        wbKeyFilename.setText(Messages.getString("System.Button.Browse"));
        fdbKeyFilename = new FormData();
        fdbKeyFilename.right = new FormAttachment(100, 0);
        fdbKeyFilename.top = new FormAttachment(wusePublicKey, 0);
        // fdbKeyFilename.height = 22;
        wbKeyFilename.setLayoutData(fdbKeyFilename);

        wKeyFilename = new TextVar(jobMeta,wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wKeyFilename.setToolTipText(Messages.getString("JobFTPDelete.KeyFilename.Tooltip"));
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
        wkeyfilePass = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTPDelete.keyfilePass.Label"), 
        		Messages.getString("JobFTPDelete.keyfilePass.Tooltip"));
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
            	DatabaseDialog.checkPasswordVisible(wkeyfilePass.getTextWidget());
            }
        });
        


        
        
        
		// Test connection button
		wTest=new Button(wServerSettings,SWT.PUSH);
		wTest.setText(Messages.getString("JobFTPDelete.TestConnection.Label"));
	 	props.setLook(wTest);
		fdTest=new FormData();
		wTest.setToolTipText(Messages.getString("JobFTPDelete.TestConnection.Tooltip"));
		//fdTest.left = new FormAttachment(middle, 0);
		fdTest.top  = new FormAttachment(wkeyfilePass, margin);
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
        

	   

		fdGeneralComp=new FormData();
		fdGeneralComp.left  = new FormAttachment(0, 0);
		fdGeneralComp.top   = new FormAttachment(0, 0);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.bottom= new FormAttachment(100, 0);
		wGeneralComp.setLayoutData(fdGeneralComp);
		
		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);
 		props.setLook(wGeneralComp);
 		
 		
 		
		/////////////////////////////////////////////////////////////
		/// END OF GENERAL TAB
		/////////////////////////////////////////////////////////////
		
 		//////////////////////////
		// START OF Advanced TAB   ///
		//////////////////////////
		
		
		
		wFilesTab=new CTabItem(wTabFolder, SWT.NONE);
		wFilesTab.setText(Messages.getString("JobFTPDelete.Tab.Files.Label"));
		
		wFilesComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wFilesComp);

		FormLayout AdvancedLayout = new FormLayout();
		AdvancedLayout.marginWidth  = 3;
		AdvancedLayout.marginHeight = 3;
		wFilesComp.setLayout(AdvancedLayout);
		
		 // ////////////////////////
	     // START OF Advanced SETTINGS GROUP///
	     // /
	     wAdvancedSettings = new Group(wFilesComp, SWT.SHADOW_NONE);
	     props.setLook(wAdvancedSettings);
	     wAdvancedSettings.setText(Messages.getString("JobFTPDelete.AdvancedSettings.Group.Label"));

	     FormLayout AdvancedSettingsgroupLayout = new FormLayout();
	     AdvancedSettingsgroupLayout.marginWidth = 10;
	     AdvancedSettingsgroupLayout.marginHeight = 10;

	     wAdvancedSettings.setLayout(AdvancedSettingsgroupLayout);
	     
	     
     // Timeout line
     wTimeout = new LabelTextVar(jobMeta,wAdvancedSettings, Messages.getString("JobFTPDelete.Timeout.Label"), Messages
         .getString("JobFTPDelete.Timeout.Tooltip"));
     props.setLook(wTimeout);
     wTimeout.addModifyListener(lsMod);
     fdTimeout = new FormData();
     fdTimeout.left = new FormAttachment(0, 0);
     fdTimeout.top = new FormAttachment(wActive, margin);
     fdTimeout.right = new FormAttachment(100, 0);
     wTimeout.setLayoutData(fdTimeout);


	     // active connection?
	     wlActive = new Label(wAdvancedSettings, SWT.RIGHT);
	     wlActive.setText(Messages.getString("JobFTPDelete.ActiveConns.Label"));
	     props.setLook(wlActive);
	     fdlActive = new FormData();
	     fdlActive.left = new FormAttachment(0, 0);
	     fdlActive.top = new FormAttachment(wTimeout, margin);
	     fdlActive.right = new FormAttachment(middle, 0);
	     wlActive.setLayoutData(fdlActive);
	     wActive = new Button(wAdvancedSettings, SWT.CHECK);
	     wActive.setToolTipText(Messages.getString("JobFTPDelete.ActiveConns.Tooltip"));
	     props.setLook(wActive);
	     fdActive = new FormData();
	     fdActive.left = new FormAttachment(middle, margin);
	     fdActive.top = new FormAttachment(wTimeout, margin);
	     fdActive.right = new FormAttachment(100, 0);
	     wActive.setLayoutData(fdActive);
 
	    
	     fdAdvancedSettings = new FormData();
	     fdAdvancedSettings.left = new FormAttachment(0, margin);
	     fdAdvancedSettings.top = new FormAttachment(0, margin);
	     fdAdvancedSettings.right = new FormAttachment(100, -margin);
	     wAdvancedSettings.setLayoutData(fdAdvancedSettings);
	     // ///////////////////////////////////////////////////////////
	     // / END OF Advanced SETTINGS GROUP
	     // ///////////////////////////////////////////////////////////
		
		
		
		 // ////////////////////////
	     // START OF Remote SETTINGS GROUP///
	     // /
	    wRemoteSettings = new Group(wFilesComp, SWT.SHADOW_NONE);
	    props.setLook(wRemoteSettings);
	    wRemoteSettings.setText(Messages.getString("JobFTPDelete.RemoteSettings.Group.Label"));

	    FormLayout RemoteSettinsgroupLayout = new FormLayout();
	    RemoteSettinsgroupLayout.marginWidth = 10;
	    RemoteSettinsgroupLayout.marginHeight = 10;

	    wRemoteSettings.setLayout(RemoteSettinsgroupLayout);
	    
	    
	    

        // Get arguments from previous result...
        wlgetPrevious = new Label(wRemoteSettings, SWT.RIGHT);
        wlgetPrevious.setText(Messages.getString("JobFTPDelete.getPrevious.Label"));
        props.setLook(wlgetPrevious);
        fdlgetPrevious = new FormData();
        fdlgetPrevious.left = new FormAttachment(0, 0);
        fdlgetPrevious.top = new FormAttachment(wAdvancedSettings, margin);
        fdlgetPrevious.right = new FormAttachment(middle, 0);
        wlgetPrevious.setLayoutData(fdlgetPrevious);
        wgetPrevious = new Button(wRemoteSettings, SWT.CHECK);
        props.setLook(wgetPrevious);
        wgetPrevious.setToolTipText(Messages.getString("JobFTPDelete.getPrevious.Tooltip"));
        fdgetPrevious = new FormData();
        fdgetPrevious.left = new FormAttachment(middle, margin);
        fdgetPrevious.top = new FormAttachment(wAdvancedSettings, margin);
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
	    
       // FTP directory
       wlFtpDirectory = new Label(wRemoteSettings, SWT.RIGHT);
       wlFtpDirectory.setText(Messages.getString("JobFTPDelete.RemoteDir.Label"));
       props.setLook(wlFtpDirectory);
       fdlFtpDirectory= new FormData();
       fdlFtpDirectory.left = new FormAttachment(0, 0);
       fdlFtpDirectory.top = new FormAttachment(wgetPrevious, margin);
       fdlFtpDirectory.right = new FormAttachment(middle, 0);
       wlFtpDirectory.setLayoutData(fdlFtpDirectory);
	    
	    // Test remote folder  button ...
		wbTestChangeFolderExists=new Button(wRemoteSettings, SWT.PUSH| SWT.CENTER);
		props.setLook(wbTestChangeFolderExists);
		wbTestChangeFolderExists.setText(Messages.getString("JobFTPDelete.TestFolderExists.Label"));
		fdbTestChangeFolderExists=new FormData();
		fdbTestChangeFolderExists.right= new FormAttachment(100, 0);
		fdbTestChangeFolderExists.top  = new FormAttachment(wgetPrevious, margin);
		wbTestChangeFolderExists.setLayoutData(fdbTestChangeFolderExists);

       wFtpDirectory = new TextVar(jobMeta,wRemoteSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages
          .getString("JobFTPDelete.RemoteDir.Tooltip"));
      props.setLook(wFtpDirectory);
      wFtpDirectory.addModifyListener(lsMod);
      fdFtpDirectory = new FormData();
      fdFtpDirectory.left = new FormAttachment(middle, margin);
      fdFtpDirectory.top = new FormAttachment(wgetPrevious, margin);
      fdFtpDirectory.right = new FormAttachment(wbTestChangeFolderExists, -margin);
      wFtpDirectory.setLayoutData(fdFtpDirectory);
      
       
       // Wildcard line
       wWildcard = new LabelTextVar(jobMeta,wRemoteSettings, Messages.getString("JobFTPDelete.Wildcard.Label"), Messages
           .getString("JobFTPDelete.Wildcard.Tooltip"));
       props.setLook(wWildcard);
       wWildcard.addModifyListener(lsMod);
       fdWildcard = new FormData();
       fdWildcard.left = new FormAttachment(0, 0);
       fdWildcard.top = new FormAttachment(wFtpDirectory, margin);
       fdWildcard.right = new FormAttachment(100, 0);
       wWildcard.setLayoutData(fdWildcard);
       
     
     
     
      
     
       fdRemoteSettings = new FormData();
       fdRemoteSettings.left = new FormAttachment(0, margin);
       fdRemoteSettings.top = new FormAttachment(wAdvancedSettings, margin);
       fdRemoteSettings.right = new FormAttachment(100, -margin);
       wRemoteSettings.setLayoutData(fdRemoteSettings);
      // ///////////////////////////////////////////////////////////
      // / END OF Remote SETTINGSGROUP
      // ///////////////////////////////////////////////////////////
		
		
		
		 // SuccessOngrouping?
	     // ////////////////////////
	     // START OF SUCCESS ON GROUP///
	     // /
	    wSuccessOn= new Group(wFilesComp, SWT.SHADOW_NONE);
	    props.setLook(wSuccessOn);
	    wSuccessOn.setText(Messages.getString("JobFTPDelete.SuccessOn.Group.Label"));

	    FormLayout successongroupLayout = new FormLayout();
	    successongroupLayout.marginWidth = 10;
	    successongroupLayout.marginHeight = 10;

	    wSuccessOn.setLayout(successongroupLayout);
	    

	    //Success Condition
	  	wlSuccessCondition = new Label(wSuccessOn, SWT.RIGHT);
	  	wlSuccessCondition.setText(Messages.getString("JobFTPDelete.SuccessCondition.Label") + " ");
	  	props.setLook(wlSuccessCondition);
	  	fdlSuccessCondition = new FormData();
	  	fdlSuccessCondition.left = new FormAttachment(0, 0);
	  	fdlSuccessCondition.right = new FormAttachment(middle, 0);
	  	fdlSuccessCondition.top = new FormAttachment(wRemoteSettings, margin);
	  	wlSuccessCondition.setLayoutData(fdlSuccessCondition);
	  	wSuccessCondition = new CCombo(wSuccessOn, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
	  	wSuccessCondition.add(Messages.getString("JobFTPDelete.SuccessWhenAllWorksFine.Label"));
	  	wSuccessCondition.add(Messages.getString("JobFTPDelete.SuccessWhenAtLeat.Label"));
	  	wSuccessCondition.add(Messages.getString("JobFTPDelete.SuccessWhenNrErrorsLessThan.Label"));
	  	wSuccessCondition.select(0); // +1: starts at -1
	  	
		props.setLook(wSuccessCondition);
		fdSuccessCondition= new FormData();
		fdSuccessCondition.left = new FormAttachment(middle, 0);
		fdSuccessCondition.top = new FormAttachment(wRemoteSettings, margin);
		fdSuccessCondition.right = new FormAttachment(100, 0);
		wSuccessCondition.setLayoutData(fdSuccessCondition);
		wSuccessCondition.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				activeSuccessCondition();
				
			}
		});

		// Success when number of errors less than
		wlNrErrorsLessThan= new Label(wSuccessOn, SWT.RIGHT);
		wlNrErrorsLessThan.setText(Messages.getString("JobFTPDelete.NrBadFormedLessThan.Label") + " ");
		props.setLook(wlNrErrorsLessThan);
		fdlNrErrorsLessThan= new FormData();
		fdlNrErrorsLessThan.left = new FormAttachment(0, 0);
		fdlNrErrorsLessThan.top = new FormAttachment(wSuccessCondition, margin);
		fdlNrErrorsLessThan.right = new FormAttachment(middle, -margin);
		wlNrErrorsLessThan.setLayoutData(fdlNrErrorsLessThan);
		
		
		wNrErrorsLessThan= new TextVar(jobMeta,wSuccessOn, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages
			.getString("JobFTPDelete.NrBadFormedLessThan.Tooltip"));
		props.setLook(wNrErrorsLessThan);
		wNrErrorsLessThan.addModifyListener(lsMod);
		fdNrErrorsLessThan= new FormData();
		fdNrErrorsLessThan.left = new FormAttachment(middle, 0);
		fdNrErrorsLessThan.top = new FormAttachment(wSuccessCondition, margin);
		fdNrErrorsLessThan.right = new FormAttachment(100, -margin);
		wNrErrorsLessThan.setLayoutData(fdNrErrorsLessThan);
		
	
	    fdSuccessOn= new FormData();
	    fdSuccessOn.left = new FormAttachment(0, margin);
	    fdSuccessOn.top = new FormAttachment(wRemoteSettings, margin);
	    fdSuccessOn.right = new FormAttachment(100, -margin);
	    wSuccessOn.setLayoutData(fdSuccessOn);
	     // ///////////////////////////////////////////////////////////
	     // / END OF Success ON GROUP
	     // ///////////////////////////////////////////////////////////

		
		
		
		
		fdFilesComp=new FormData();
		fdFilesComp.left  = new FormAttachment(0, 0);
		fdFilesComp.top   = new FormAttachment(0, 0);
		fdFilesComp.right = new FormAttachment(100, 0);
		fdFilesComp.bottom= new FormAttachment(100, 0);
		wFilesComp.setLayoutData(fdFilesComp);
		
		wFilesComp.layout();
		wFilesTab.setControl(wFilesComp);
 		props.setLook(wFilesComp);
 		
 		
 		
		/////////////////////////////////////////////////////////////
		/// END OF Advanced TAB
		/////////////////////////////////////////////////////////////
		
		
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
        lsTest     = new Listener() { public void handleEvent(Event e) { test(); } };
        lsCheckFolder     = new Listener() { public void handleEvent(Event e) { checkFTPFolder(); } };
        
        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);
        wTest.addListener    (SWT.Selection, lsTest    );
        wbTestChangeFolderExists.addListener    (SWT.Selection, lsCheckFolder    );

        
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
        activeSuccessCondition();
        activeUsePublicKey();
        activeProxy();
        activeFTPProtocol();
        activeCopyFromPrevious();
        
        wTabFolder.setSelection(0);
        BaseStepDialog.setSize(shell);

        shell.open();
        props.setDialogSize(shell, "JobFTPDeleteDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
    }
    private void activeCopyFromPrevious()
    {
    	wFtpDirectory.setEnabled(!wgetPrevious.getSelection());
    	wlFtpDirectory.setEnabled(!wgetPrevious.getSelection());
    	wWildcard.setEnabled(!wgetPrevious.getSelection());
    	wbTestChangeFolderExists.setEnabled(!wgetPrevious.getSelection());
    }
    private void activeUsePublicKey()
    {
    	wlKeyFilename.setEnabled(wusePublicKey.getSelection());
    	wKeyFilename.setEnabled(wusePublicKey.getSelection());
    	wbKeyFilename.setEnabled(wusePublicKey.getSelection());
    	wkeyfilePass.setEnabled(wusePublicKey.getSelection());
    }
    private void activeProxy()
    {
    	wProxyHost.setEnabled(wuseProxy.getSelection());
    	wProxyPassword.setEnabled(wuseProxy.getSelection());
    	wProxyPort.setEnabled(wuseProxy.getSelection());
    	wProxyUsername.setEnabled(wuseProxy.getSelection());
    }
    private void activeFTPProtocol()
    {
    	if(wProtocol.getText().equals("SSH"))
    	{
    		wlusePublicKey.setEnabled(true);
    		wusePublicKey.setEnabled(true);

    	}else
    	{
    		wusePublicKey.setSelection(false);
    		activeUsePublicKey();
    		wlusePublicKey.setEnabled(false);
    		wusePublicKey.setEnabled(false);
    	}
    }
    /**
     * Checks if a directory exists
     * 
     * @param sftpClient
     * @param directory
     * @return true, if directory exists
     */
    public boolean sshDirectoryExists(SFTPv3Client sftpClient, String directory)  {
    try {
           SFTPv3FileAttributes attributes = sftpClient.stat(directory);
              
            if (attributes != null) {
                 return (attributes.isDirectory());
              } else {
                  return false;
              }
              
        } catch (Exception e) {
              return false;
        }
    }
    private void checkFTPFolder()
    {
    	boolean folderexists=false;
    	String errmsg="";
    	try
    	{
	    	String realfoldername=jobMeta.environmentSubstitute(wFtpDirectory.getText());
	    	if(!Const.isEmpty(realfoldername))
	    	{
	    		if(connect())
	    		{
	    			if(wProtocol.getText().equals("FTP"))
	    			{
	    				ftpclient.chdir(realfoldername);
	    				folderexists=true;
	    			}
	    			else if(wProtocol.getText().equals("FTP"))
	    			{
	    				sftpclient.chdir(realfoldername);
	    				folderexists=true;
	    			}
	    			else if(wProtocol.getText().equals("SSH"))
	    			{
	    				SFTPv3Client client = new SFTPv3Client(conn);
	    				boolean folderexist=sshDirectoryExists(client,realfoldername);
	    				client.close();
	    				if(folderexist)
	    				{
	    					// Folder exists
	    					folderexists=true;
	    				}else
	    				{
	    					// we can not find folder
	    					folderexists=false;
	    				}
	    			}
	    			
	    		}
	    	}
    	}catch(Exception e)
    	{
    		errmsg=e.getMessage();
    	}
    	if(folderexists)
    	{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
			mb.setMessage(Messages.getString("JobFTPDelete.FolderExists.OK",wFtpDirectory.getText()) +Const.CR);
			mb.setText(Messages.getString("JobFTPDelete.FolderExists.Title.Ok"));
			mb.open();	
    	}else
    	{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("JobFTPDelete.FolderExists.NOK",wFtpDirectory.getText()) +Const.CR + errmsg);
			mb.setText(Messages.getString("JobFTPDelete.FolderExists.Title.Bad"));
			mb.open(); 
    	}
    }
    private boolean connect()
    {
    	boolean connexion=false;
		if(wProtocol.getText().equals("FTP"))
			connexion=connectToFTP();
		else if(wProtocol.getText().equals("SFTP"))
			connexion=connectToSFTP();
		else if(wProtocol.getText().equals("SSH"))
			connexion=connectToSSH();
		return connexion;
    }
    private void test()
    {
		
    	if(connect())
    	{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
			mb.setMessage(Messages.getString("JobFTPDelete.Connected.OK",wServerName.getText()) +Const.CR);
			mb.setText(Messages.getString("JobFTPDelete.Connected.Title.Ok"));
			mb.open();
		}else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("JobFTPDelete.Connected.NOK.ConnectionBad",wServerName.getText()) +Const.CR);
			mb.setText(Messages.getString("JobFTPDelete.Connected.Title.Bad"));
			mb.open(); 
	    }
	   
    }
   
    private boolean connectToFTP()
    {
    	boolean retval=false;
		try
		{
			if(ftpclient==null || !ftpclient.connected())
			{
		    	 // Create ftp client to host:port ...
		        ftpclient = new FTPClient();
		        String realServername = jobMeta.environmentSubstitute(wServerName.getText());
		        ftpclient.setRemoteAddr(InetAddress.getByName(realServername));
		        
		        if (!Const.isEmpty(wProxyHost.getText())) 
		        {
		      	  String realProxy_host = jobMeta.environmentSubstitute(wProxyHost.getText());
		      	  ftpclient.setRemoteAddr(InetAddress.getByName(realProxy_host));
		      	  if(Const.isEmpty(wPort.getText()))
		      		  ftpclient.setRemotePort(Const.toInt(wPort.getText(),21));
		
		      	  // FIXME: Proper default port for proxy    	  
		      	  int port = Const.toInt(jobMeta.environmentSubstitute(wProxyHost.getText()), 21);
		      	  if (port != 0) 
		      	  {
		      	     ftpclient.setRemotePort(port);
		      	  }
		        } 
		        else 
		        {
		            ftpclient.setRemoteAddr(InetAddress.getByName(realServername));
		                           
		        }
	
		        // login to ftp host ...
		        ftpclient.connect();     
		        String realUsername = jobMeta.environmentSubstitute(wUserName.getText()) +
		                              (!Const.isEmpty(wProxyHost.getText()) ? "@" + realServername : "") + 
		                              (!Const.isEmpty(wProxyUsername.getText()) ? " " + jobMeta.environmentSubstitute(wProxyUsername.getText()) 
		                          		                           : ""); 
		           		            
		        String realPassword = jobMeta.environmentSubstitute(wPassword.getText()) + 
		                              (!Const.isEmpty(wProxyPassword.getText()) ? " " + jobMeta.environmentSubstitute(wProxyPassword.getText()) : "" );
		        // login now ...
		        ftpclient.login(realUsername, realPassword);
			}  
	       
	        	
	        retval=true;
		}
	     catch (Exception e)
	    {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("JobFTPDelete.ErrorConnect.NOK",e.getMessage()) +Const.CR);
			mb.setText(Messages.getString("JobFTPDelete.ErrorConnect.Title.Bad"));
			mb.open(); 
	    } 
	    return retval;
    }
    private boolean connectToSFTP()
    {
    	boolean retval=false;
		try
		{
			if(sftpclient==null)
			{
				// Create sftp client to host ...
				sftpclient = new SFTPClient(InetAddress.getByName(jobMeta.environmentSubstitute(wServerName.getText())), 
						Const.toInt(jobMeta.environmentSubstitute(wPort.getText()), 22), 
						jobMeta.environmentSubstitute(wUserName.getText()));
			
				// login to ftp host ...
				sftpclient.login(jobMeta.environmentSubstitute(wPassword.getText()));
			}  
	       
	        	
	        retval=true;
		}
	     catch (Exception e)
	    {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("JobFTPDelete.ErrorConnect.NOK",e.getMessage()) +Const.CR);
			mb.setText(Messages.getString("JobFTPDelete.ErrorConnect.Title.Bad"));
			mb.open(); 
	    } 
	    return retval;
    }
    private boolean connectToSSH()
    {
    	boolean retval=false;
		try	{
			if(conn==null){	// Create a connection instance 
				conn = new Connection(jobMeta.environmentSubstitute(wServerName.getText()),Const.toInt(jobMeta.environmentSubstitute(wPort.getText()), 22));				

				/* We want to connect through a HTTP proxy */
				if(wuseProxy.getSelection()){
					/* Now connect */
					// if the proxy requires basic authentication:
					if(!Const.isEmpty(wProxyUsername.getText())){
						conn.setProxyData(new HTTPProxyData(jobMeta.environmentSubstitute(wProxyHost.getText()), 
								Const.toInt(wProxyPort.getText(),22), 
								jobMeta.environmentSubstitute(wProxyUsername.getText()), 
								jobMeta.environmentSubstitute(wProxyPassword.getText())));
					}else{
						conn.setProxyData(new HTTPProxyData(jobMeta.environmentSubstitute(wProxyHost.getText()), 
								Const.toInt(wProxyPort.getText(),22)));
					}
				}
				
				conn.connect();

				// Authenticate
				if(wusePublicKey.getSelection()){
					retval=conn.authenticateWithPublicKey(jobMeta.environmentSubstitute(wUserName.getText()), 
							new java.io.File(jobMeta.environmentSubstitute(wKeyFilename.getText())), jobMeta.environmentSubstitute(wkeyfilePass.getText()));
				}else{
					retval=conn.authenticateWithPassword(jobMeta.environmentSubstitute(wUserName.getText()), jobMeta.environmentSubstitute(wPassword.getText()));
				}
			}  
	
	        retval=true;
		}
	     catch (Exception e) {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("JobFTPDelete.ErrorConnect.NOK",e.getMessage()) +Const.CR);
			mb.setText(Messages.getString("JobFTPDelete.ErrorConnect.Title.Bad"));
			mb.open(); 
	    } 
	    return retval;
    }
	private void activeSuccessCondition()
	{
		wlNrErrorsLessThan.setEnabled(wSuccessCondition.getSelectionIndex()!=0);
		wNrErrorsLessThan.setEnabled(wSuccessCondition.getSelectionIndex()!=0);	
	}
	
    public void checkPasswordVisible()
    {
        String password = wPassword.getText();
        List<String> list = new ArrayList<String>();
        StringUtil.getUsedVariables(password, list, true);
        if (list.size() == 0)
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
        wName.getTextWidget().selectAll();
        
        if (jobEntry.getProtocol() != null)
            wProtocol.setText(jobEntry.getProtocol());
        else
        	wProtocol.setText("FTP");

        wPort.setText(Const.NVL(jobEntry.getPort(), ""));
        wServerName.setText(Const.NVL(jobEntry.getServerName(), ""));
        wUserName.setText(Const.NVL(jobEntry.getUserName(), ""));
        wPassword.setText(Const.NVL(jobEntry.getPassword(), ""));
        wFtpDirectory.setText(Const.NVL(jobEntry.getFtpDirectory(), ""));
        wWildcard.setText(Const.NVL(jobEntry.getWildcard(), ""));
        wTimeout.setText("" + jobEntry.getTimeout());
        wActive.setSelection(jobEntry.isActiveConnection());
 
        wuseProxy.setSelection(jobEntry.isUseProxy());
        wProxyHost.setText(Const.NVL(jobEntry.getProxyHost(), ""));       
        wProxyPort.setText(Const.NVL(jobEntry.getProxyPort(), ""));
        wProxyUsername.setText(Const.NVL(jobEntry.getProxyUsername(), ""));
        wProxyPassword.setText(Const.NVL(jobEntry.getProxyPassword(), ""));
        
    	if (jobEntry.getLimitSuccess()!= null) 
			wNrErrorsLessThan.setText( jobEntry.getLimitSuccess());
		else
			wNrErrorsLessThan.setText("10");
		
		
		if(jobEntry.getSuccessCondition()!=null)
		{
			if(jobEntry.getSuccessCondition().equals(jobEntry.SUCCESS_IF_AT_LEAST_X_FILES_DOWNLOADED))
				wSuccessCondition.select(1);
			else if(jobEntry.getSuccessCondition().equals(jobEntry.SUCCESS_IF_ERRORS_LESS))
				wSuccessCondition.select(2);
			else
				wSuccessCondition.select(0);	
		}else wSuccessCondition.select(0);
		
        wusePublicKey.setSelection(jobEntry.isUsePublicKey());
        if(jobEntry.getKeyFilename()!=null) wKeyFilename.setText(jobEntry.getKeyFilename());
        if(jobEntry.getKeyFilePass()!=null) wkeyfilePass.setText(jobEntry.getKeyFilePass());
        
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
        jobEntry.setName(wName.getText());
        jobEntry.setProtocol(wProtocol.getText()); 
        jobEntry.setPort(wPort.getText());
        jobEntry.setServerName(wServerName.getText());
        jobEntry.setUserName(wUserName.getText());
        jobEntry.setPassword(wPassword.getText());
        jobEntry.setFtpDirectory(wFtpDirectory.getText());
        jobEntry.setWildcard(wWildcard.getText());
        jobEntry.setTimeout(Const.toInt(wTimeout.getText(), 10000));
        jobEntry.setActiveConnection(wActive.getSelection());
    	
        jobEntry.setUseProxy(wuseProxy.getSelection());
        jobEntry.setProxyHost(wProxyHost.getText()); 
        jobEntry.setProxyPort(wProxyPort.getText());
        jobEntry.setProxyUsername(wProxyUsername.getText());
        jobEntry.setProxyPassword(wProxyPassword.getText());
       
        jobEntry.setLimitSuccess(wNrErrorsLessThan.getText());

		
		if(wSuccessCondition.getSelectionIndex()==1)
			jobEntry.setSuccessCondition(jobEntry.SUCCESS_IF_AT_LEAST_X_FILES_DOWNLOADED);
		else if(wSuccessCondition.getSelectionIndex()==2)
			jobEntry.setSuccessCondition(jobEntry.SUCCESS_IF_ERRORS_LESS);
		else
			jobEntry.setSuccessCondition(jobEntry.SUCCESS_IF_ALL_FILES_DOWNLOADED);	
		
		
	     jobEntry.setUsePublicKey(wusePublicKey.getSelection());
	     jobEntry.setKeyFilename(wKeyFilename.getText());
	     jobEntry.setKeyFilePass(wkeyfilePass.getText());
	        
	     jobEntry.setCopyPrevious(wgetPrevious.getSelection());
	     
        dispose();
    }
	private void closeFTPConnections()
	{
		// Close FTP connection if necessary
		if (ftpclient != null && ftpclient.connected())
	      {
	        try
	        {
	          ftpclient.quit();
	          ftpclient=null;
	        } catch (Exception e) {}
	      }
		
		// Close SecureFTP connection if necessary
		if (sftpclient != null)
	      {
	        try
	        {
	        	sftpclient.disconnect();
	        	sftpclient=null;
	        } catch (Exception e) {}
	      }
		// Close SSH connection if necessary
		if (conn != null)
	      {
	        try
	        {
	        	conn.close();
	        	conn=null;
	        	
	        } catch (Exception e) {}
	      }
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