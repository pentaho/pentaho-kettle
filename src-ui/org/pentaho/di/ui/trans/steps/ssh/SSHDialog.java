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
 * Created on 18-mei-2003
 *
 */

package org.pentaho.di.ui.trans.steps.ssh;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Group;
import com.trilead.ssh2.Connection;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.ssh.SSHMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class SSHDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = SSHMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wGeneralTab, wSettingsTab;

	private Composite    wGeneralComp, wSettingsComp;
	private FormData     fdGeneralComp, fdSettingsComp;

	private Label        wlCommandField;
	private CCombo       wCommandField;
	private FormData     fdlCommandField, fdCommandField;
	
	private Group wSettingsGroup;
	private FormData fdSettingsGroup;
	
	private Group wOutput;
	private FormData fdOutput;
	
    private FormData     fdTimeOut;
    private LabelTextVar wTimeOut;
	private SSHMeta input;
	
    private Group wCommands;
    private FormData fdLogSettings;
    
    private Label wldynamicCommand;
    private FormData fdlynamicBase;
    private Button wdynamicCommand;
    private FormData fdynamicCommand;
    
    private FormData     fdPort;
    private LabelTextVar wPort;
    
    private LabelTextVar wUserName;
    private FormData fdUserName;
    
    private LabelTextVar wPassword;
    private FormData fdPassword;
    
    private Label wlUseKey;
    private FormData fdlUseKey;
    private Button wUseKey;
    private FormData fdUseKey;
    
    private LabelTextVar wPrivateKey;
    private FormData fdPrivateKey;
    
    private LabelTextVar wPassphrase;
    private FormData fdPassphrase;
    
    private LabelTextVar wResultOutFieldName, wResultErrFieldName;
    private FormData fdResultOutFieldName, fdResultErrFieldName;
    
    
    private Label wlCommand;
    private StyledTextComp wCommand;
    private FormData fdlCommand, fdCommand;
    
	private Button wTest;
	
	private FormData fdTest;
	
	private Listener lsTest;
	
	   
    private LabelTextVar wProxyHost;
    private FormData fdProxyHost;
    
    private LabelTextVar wProxyPort;
    private FormData fdProxyPort;
    
    private LabelTextVar wProxyUsername;
    private FormData fdProxyUsername;
    
    private LabelTextVar wProxyPassword;
    private FormData fdProxyPassword;
	
    private LabelTextVar wServerName;
    private FormData fdServerName;
    
    
	private Button       wbFilename;
	private FormData     fdbFilename;
	
	private boolean gotPreviousFields=false;
	

	public SSHDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(SSHMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
        setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};

        
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "SSHDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin=Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "SSHDialog.Stepname.Label")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
		
		
		wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, PropsUI.WIDGET_STYLE_TAB);

		//////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(BaseMessages.getString(PKG, "SSHDialog.General.Tab"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout fileLayout = new FormLayout();
		fileLayout.marginWidth  = 3;
		fileLayout.marginHeight = 3;
		wGeneralComp.setLayout(fileLayout);
		
		///////////////////////////////// 
		// START OF Settings GROUP  //
		///////////////////////////////// 

		wSettingsGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wSettingsGroup);
		wSettingsGroup.setText(BaseMessages.getString(PKG, "SSHDialog.wSettingsGroup.Label"));
		
		FormLayout settingGroupLayout = new FormLayout();
		settingGroupLayout.marginWidth = 10;
		settingGroupLayout.marginHeight = 10;
		wSettingsGroup.setLayout(settingGroupLayout);
		
		 // Server port line
        wServerName = new LabelTextVar(transMeta, wSettingsGroup, BaseMessages.getString(PKG, "SSHDialog.Server.Label"), BaseMessages.getString(PKG, "SSHDialog.Server.Tooltip"));
        props.setLook(wServerName);
        wServerName.addModifyListener(lsMod);
        fdServerName = new FormData();
        fdServerName.left 	= new FormAttachment(0, 0);
        fdServerName.top  	= new FormAttachment(wStepname, margin);
        fdServerName.right	= new FormAttachment(100, 0);
        wServerName.setLayoutData(fdServerName);
        
        // Server port line
        wPort = new LabelTextVar(transMeta, wSettingsGroup, BaseMessages.getString(PKG, "SSHDialog.Port.Label"), BaseMessages.getString(PKG, "SSHDialog.Port.Tooltip"));
        props.setLook(wPort);
        wPort.addModifyListener(lsMod);
        fdPort = new FormData();
        fdPort.left 	= new FormAttachment(0, 0);
        fdPort.top  	= new FormAttachment(wServerName, margin);
        fdPort.right	= new FormAttachment(100, 0);
        wPort.setLayoutData(fdPort);
        
        // Server TimeOut line
        wTimeOut = new LabelTextVar(transMeta, wSettingsGroup, BaseMessages.getString(PKG, "SSHDialog.TimeOut.Label"), BaseMessages.getString(PKG, "SSHDialog.TimeOut.Tooltip"));
        props.setLook(wTimeOut);
        wTimeOut.addModifyListener(lsMod);
        fdTimeOut = new FormData();
        fdTimeOut.left 	= new FormAttachment(0, 0);
        fdTimeOut.top  	= new FormAttachment(wPort, margin);
        fdTimeOut.right	= new FormAttachment(100, 0);
        wTimeOut.setLayoutData(fdTimeOut);
		
		// Usernameline
        wUserName = new LabelTextVar(transMeta, wSettingsGroup, BaseMessages.getString(PKG, "SSHDialog.UserName.Label"), BaseMessages.getString(PKG, "SSHDialog.UserName.Tooltip"));
        props.setLook(wUserName);
        wUserName.addModifyListener(lsMod);
        fdUserName = new FormData();
        fdUserName.left 	= new FormAttachment(0, 0);
        fdUserName.top  	= new FormAttachment(wTimeOut, margin);
        fdUserName.right	= new FormAttachment(100, 0);
        wUserName.setLayoutData(fdUserName);
        
		
		// Passwordline
        wPassword = new LabelTextVar(transMeta, wSettingsGroup, BaseMessages.getString(PKG, "SSHDialog.Password.Label"), BaseMessages.getString(PKG, "SSHDialog.Password.Tooltip"));
        props.setLook(wPassword);
        wPassword.addModifyListener(lsMod);
        fdPassword = new FormData();
        fdPassword.left 	= new FormAttachment(0, 0);
        fdPassword.top  	= new FormAttachment(wUserName, margin);
        fdPassword.right	= new FormAttachment(100, 0);
        wPassword.setLayoutData(fdPassword);
        wPassword.setEchoChar('*');

        // Use key?
        wlUseKey = new Label(wSettingsGroup, SWT.RIGHT);
        wlUseKey.setText(BaseMessages.getString(PKG, "SSHDialog.UseKey.Label"));
        props.setLook(wlUseKey);
        fdlUseKey = new FormData();
        fdlUseKey.left = new FormAttachment(0, 0);
        fdlUseKey.top = new FormAttachment(wPassword,margin);
        fdlUseKey.right = new FormAttachment(middle, -margin);
        wlUseKey.setLayoutData(fdlUseKey);
        wUseKey = new Button(wSettingsGroup, SWT.CHECK);
        props.setLook(wUseKey);
        wUseKey.setToolTipText(BaseMessages.getString(PKG, "SSHDialog.UseKey.Tooltip"));
        fdUseKey = new FormData();
        fdUseKey.left = new FormAttachment(middle, margin);
        fdUseKey.top = new FormAttachment(wPassword, margin);
        fdUseKey.right = new FormAttachment(100, 0);
        wUseKey.setLayoutData(fdUseKey);
        wUseKey.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                input.setChanged();
                activateKey();
            }
        });
        
        wbFilename=new Button(wSettingsGroup, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFilename);
		wbFilename.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
		fdbFilename=new FormData();
		fdbFilename.right= new FormAttachment(100, -margin);
		fdbFilename.top  = new FormAttachment(wUseKey, margin);
		wbFilename.setLayoutData(fdbFilename);
		wbFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					FileDialog dialog = new FileDialog(shell, SWT.SAVE);
					dialog.setFilterExtensions(new String[] {"*.pem", "*"});
					if (wPrivateKey.getText()!=null)
					{
						dialog.setFileName(transMeta.environmentSubstitute(wPrivateKey.getText()));
					}
					dialog.setFilterNames(new String[] {BaseMessages.getString(PKG, "System.FileType.PEMFiles"), 
							BaseMessages.getString(PKG, "System.FileType.AllFiles")});
					if (dialog.open()!=null)
					{
						wPrivateKey.setText(dialog.getFilterPath()+System.getProperty("file.separator")+dialog.getFileName());
					}
				}
			}
		);
		
		// Private key
        wPrivateKey= new LabelTextVar(transMeta, wSettingsGroup, BaseMessages.getString(PKG, "SSHDialog.PrivateKey.Label"), BaseMessages.getString(PKG, "SSHDialog.PrivateKey.Tooltip"));
        props.setLook(wPassword);
        wPrivateKey.addModifyListener(lsMod);
        fdPrivateKey= new FormData();
        fdPrivateKey.left 	= new FormAttachment(0, 5*margin);
        fdPrivateKey.top  	= new FormAttachment(wUseKey, margin);
        fdPrivateKey.right	= new FormAttachment(wbFilename, -margin);
        wPrivateKey.setLayoutData(fdPrivateKey);
        
	    
		// Passphraseline
        wPassphrase= new LabelTextVar(transMeta, wSettingsGroup, BaseMessages.getString(PKG, "SSHDialog.Passphrase.Label"), BaseMessages.getString(PKG, "SSHDialog.Passphrase.Tooltip"));
        props.setLook(wPassphrase);
        wPassphrase.addModifyListener(lsMod);
        fdPassphrase= new FormData();
        fdPassphrase.left 	= new FormAttachment(0, 0);
        fdPassphrase.top  	= new FormAttachment(wbFilename, margin);
        fdPassphrase.right	= new FormAttachment(100, 0);
        wPassphrase.setLayoutData(fdPassphrase);
        
     // ProxyHostline
        wProxyHost = new LabelTextVar(transMeta, wSettingsGroup, BaseMessages.getString(PKG, "SSHDialog.ProxyHost.Label"), BaseMessages.getString(PKG, "SSHDialog.ProxyHost.Tooltip"));
        props.setLook(wProxyHost);
        wProxyHost.addModifyListener(lsMod);
        fdProxyHost = new FormData();
        fdProxyHost.left 	= new FormAttachment(0, 0);
        fdProxyHost.top  	= new FormAttachment(wPassphrase, 2*margin);
        fdProxyHost.right	= new FormAttachment(100, 0);
        wProxyHost.setLayoutData(fdProxyHost);
        
		// ProxyPortline
        wProxyPort = new LabelTextVar(transMeta, wSettingsGroup, BaseMessages.getString(PKG, "SSHDialog.ProxyPort.Label"), BaseMessages.getString(PKG, "SSHDialog.ProxyPort.Tooltip"));
        props.setLook(wProxyPort);
        wProxyPort.addModifyListener(lsMod);
        fdProxyPort = new FormData();
        fdProxyPort.left 	= new FormAttachment(0, 0);
        fdProxyPort.top  	= new FormAttachment(wProxyHost, margin);
        fdProxyPort.right	= new FormAttachment(100, 0);
        wProxyPort.setLayoutData(fdProxyPort);
        
		// ProxyUsernameline
        wProxyUsername = new LabelTextVar(transMeta, wSettingsGroup, BaseMessages.getString(PKG, "SSHDialog.ProxyUsername.Label"), BaseMessages.getString(PKG, "SSHDialog.ProxyUsername.Tooltip"));
        props.setLook(wProxyUsername);
        wProxyUsername.addModifyListener(lsMod);
        fdProxyUsername = new FormData();
        fdProxyUsername.left 	= new FormAttachment(0, 0);
        fdProxyUsername.top  	= new FormAttachment(wProxyPort, margin);
        fdProxyUsername.right	= new FormAttachment(100, 0);
        wProxyUsername.setLayoutData(fdProxyUsername);
        
		// ProxyUsernameline
        wProxyPassword= new LabelTextVar(transMeta, wSettingsGroup, BaseMessages.getString(PKG, "SSHDialog.ProxyPassword.Label"), BaseMessages.getString(PKG, "SSHDialog.ProxyPassword.Tooltip"));
        props.setLook(wProxyUsername);
        wProxyPassword.addModifyListener(lsMod);
        fdProxyPassword= new FormData();
        fdProxyPassword.left 	= new FormAttachment(0, 0);
        fdProxyPassword.top  	= new FormAttachment(wProxyUsername, margin);
        fdProxyPassword.right	= new FormAttachment(100, 0);
        wProxyPassword.setLayoutData(fdProxyPassword);
        wProxyPassword.setEchoChar('*');
        
		
		// Test connection button
		wTest=new Button(wSettingsGroup,SWT.PUSH);
		wTest.setText(BaseMessages.getString(PKG, "SSHDialog.TestConnection.Label"));
 		props.setLook(wTest);
		fdTest=new FormData();
		wTest.setToolTipText(BaseMessages.getString(PKG, "SSHDialog.TestConnection.Tooltip"));
		fdTest.top  = new FormAttachment(wProxyPassword, 2*margin);
		fdTest.right= new FormAttachment(100, 0);
		wTest.setLayoutData(fdTest);
        
		fdSettingsGroup = new FormData();
		fdSettingsGroup.left = new FormAttachment(0, margin);
		fdSettingsGroup.top = new FormAttachment(wStepname, margin);
		fdSettingsGroup.right = new FormAttachment(100, -margin);
		wSettingsGroup.setLayoutData(fdSettingsGroup);
		
		///////////////////////////////// 
		// END OF Settings Fields GROUP  //
		
		
		fdGeneralComp=new FormData();
		fdGeneralComp.left  = new FormAttachment(0, 0);
		fdGeneralComp.top   = new FormAttachment(0, 0);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.bottom= new FormAttachment(100, 0);
		wGeneralComp.setLayoutData(fdGeneralComp);
	
		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);
		
		/////////////////////////////////////////////////////////////
		/// END OF GENERAL TAB
		/////////////////////////////////////////////////////////////

		
		//////////////////////////
		// START OF Settings TAB///
		///
		wSettingsTab=new CTabItem(wTabFolder, SWT.NONE);
		wSettingsTab.setText(BaseMessages.getString(PKG, "SSHDialog.Settings.Tab"));

		FormLayout SettingsLayout = new FormLayout ();
		SettingsLayout.marginWidth  = 3;
		SettingsLayout.marginHeight = 3;
		
		wSettingsComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wSettingsComp);
		wSettingsComp.setLayout(SettingsLayout);
		
		
		///////////////////////////////// 
		// START OF Output GROUP  //
		///////////////////////////////// 

		wOutput = new Group(wSettingsComp, SWT.SHADOW_NONE);
		props.setLook(wOutput);
		wOutput.setText(BaseMessages.getString(PKG, "SSHDialog.wOutput.Label"));
		
		FormLayout outputGroupLayout = new FormLayout();
		outputGroupLayout.marginWidth = 10;
		outputGroupLayout.marginHeight = 10;
		wOutput.setLayout(outputGroupLayout);
		
		// ResultOutFieldNameline
        wResultOutFieldName= new LabelTextVar(transMeta, wOutput, BaseMessages.getString(PKG, "SSHDialog.ResultOutFieldName.Label"), BaseMessages.getString(PKG, "SSHDialog.ResultOutFieldName.Tooltip"));
        props.setLook(wResultOutFieldName);
        wResultOutFieldName.addModifyListener(lsMod);
        fdResultOutFieldName= new FormData();
        fdResultOutFieldName.left 	= new FormAttachment(0, 0);
        fdResultOutFieldName.top  	= new FormAttachment(wStepname, margin);
        fdResultOutFieldName.right	= new FormAttachment(100, 0);
        wResultOutFieldName.setLayoutData(fdResultOutFieldName);
        
    	// ResultErrFieldNameline
        wResultErrFieldName= new LabelTextVar(transMeta, wOutput, BaseMessages.getString(PKG, "SSHDialog.ResultErrFieldName.Label"), BaseMessages.getString(PKG, "SSHDialog.ResultErrFieldName.Tooltip"));
        props.setLook(wResultErrFieldName);
        wResultErrFieldName.addModifyListener(lsMod);
        fdResultErrFieldName= new FormData();
        fdResultErrFieldName.left 	= new FormAttachment(0, 0);
        fdResultErrFieldName.top  	= new FormAttachment(wResultOutFieldName, margin);
        fdResultErrFieldName.right	= new FormAttachment(100, 0);
        wResultErrFieldName.setLayoutData(fdResultErrFieldName);

		fdOutput = new FormData();
		fdOutput.left = new FormAttachment(0, margin);
		fdOutput.top = new FormAttachment(wStepname, margin);
		fdOutput.right = new FormAttachment(100, -margin);
		wOutput.setLayoutData(fdOutput);
		
		///////////////////////////////// 
		// END OF Output Fields GROUP  //
		
		
		// ////////////////////////
	    // START OF Commands SETTINGS GROUP///
	    // /
	    wCommands = new Group(wSettingsComp, SWT.SHADOW_NONE);
	    props.setLook(wCommands);
	    wCommands.setText(BaseMessages.getString(PKG, "SSHDialog.LogSettings.Group.Label"));

	    FormLayout LogSettingsgroupLayout = new FormLayout();
	    LogSettingsgroupLayout.marginWidth = 10;
	    LogSettingsgroupLayout.marginHeight = 10;

	    wCommands.setLayout(LogSettingsgroupLayout);
	    

		//Is command defined in a Field		
		wldynamicCommand= new Label(wCommands, SWT.RIGHT);
		wldynamicCommand.setText(BaseMessages.getString(PKG, "SSHDialog.dynamicCommand.Label"));
		props.setLook(wldynamicCommand);
		fdlynamicBase= new FormData();
		fdlynamicBase.left = new FormAttachment(0, margin);
		fdlynamicBase.top = new FormAttachment(wOutput, margin);
		fdlynamicBase.right = new FormAttachment(middle, -margin);
		wldynamicCommand.setLayoutData(fdlynamicBase);
		
		wdynamicCommand= new Button(wCommands, SWT.CHECK);
		props.setLook(wdynamicCommand);
		wdynamicCommand.setToolTipText(BaseMessages.getString(PKG, "SSHDialog.dynamicCommand.Tooltip"));
		fdynamicCommand= new FormData();
		fdynamicCommand.left = new FormAttachment(middle, margin);
		fdynamicCommand.top = new FormAttachment(wOutput, margin);
		wdynamicCommand.setLayoutData(fdynamicCommand);		
		SelectionAdapter ldynamicCommand= new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	activateDynamicCommand();
            	input.setChanged();
            }
        };
        wdynamicCommand.addSelectionListener(ldynamicCommand);
	  

		// CommandField field
		wlCommandField=new Label(wCommands, SWT.RIGHT);
		wlCommandField.setText(BaseMessages.getString(PKG, "SSHDialog.MessageNameField.Label")); //$NON-NLS-1$
 		props.setLook(wlCommandField);
		fdlCommandField=new FormData();
		fdlCommandField.left = new FormAttachment(0, margin);
		fdlCommandField.right= new FormAttachment(middle, -margin);
		fdlCommandField.top  = new FormAttachment(wdynamicCommand, margin);
		wlCommandField.setLayoutData(fdlCommandField);
	
		wCommandField=new CCombo(wCommands, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wCommandField);
 		wCommandField.setEditable(true);
 		wCommandField.addModifyListener(lsMod);
		fdCommandField=new FormData();
		fdCommandField.left = new FormAttachment(middle,margin);
		fdCommandField.top  = new FormAttachment(wdynamicCommand, margin);
		fdCommandField.right= new FormAttachment(100, 0);
		wCommandField.setLayoutData(fdCommandField);
		wCommandField.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                get();
            }
        }
    );

		
		
		// Command String
        wlCommand = new Label(wCommands, SWT.RIGHT);
        wlCommand.setText(BaseMessages.getString(PKG, "SSHDialog.Command.Label"));
        props.setLook(wlCommand);
        fdlCommand = new FormData();
        fdlCommand.left = new FormAttachment(0, margin);
        fdlCommand.top = new FormAttachment(wCommandField, margin);
        fdlCommand.right = new FormAttachment(middle, -2*margin);
        wlCommand.setLayoutData(fdlCommand);

        wCommand=new StyledTextComp(transMeta, wCommands, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");
        wCommand.setToolTipText(BaseMessages.getString(PKG, "SSHDialog.Command.Tooltip"));
        props.setLook(wCommand);
        wCommand.addModifyListener(lsMod);
        fdCommand = new FormData();
        fdCommand.left = new FormAttachment(middle, margin);
        fdCommand.top = new FormAttachment(wCommandField,margin);
        fdCommand.right = new FormAttachment(100, -2*margin);
        fdCommand.bottom = new FormAttachment(100, -margin);
        wCommand.setLayoutData(fdCommand);
		
		
		
	    fdLogSettings = new FormData();
	    fdLogSettings.left = new FormAttachment(0, margin);
	    fdLogSettings.top = new FormAttachment(wOutput, margin);
	    fdLogSettings.right = new FormAttachment(100, -margin);
	    fdLogSettings.bottom = new FormAttachment(100, -margin);
	    wCommands.setLayoutData(fdLogSettings);
	    // ///////////////////////////////////////////////////////////
	    // / END OF Log SETTINGS GROUP
	    // ///////////////////////////////////////////////////////////

		fdSettingsComp=new FormData();
		fdSettingsComp.left  = new FormAttachment(0, 0);
		fdSettingsComp.top   = new FormAttachment(0, 0);
		fdSettingsComp.right = new FormAttachment(100, 0);
		fdSettingsComp.bottom= new FormAttachment(100, 0);
		wSettingsComp.setLayoutData(fdSettingsComp);
	
		wSettingsComp.layout();
		wSettingsTab.setControl(wSettingsComp);
		
		/////////////////////////////////////////////////////////////
		/// END OF Settings TAB
		/////////////////////////////////////////////////////////////


		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);
		

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wPreview=new Button(shell, SWT.PUSH);
		wPreview.setText(BaseMessages.getString(PKG, "SSHDialog.Button.PreviewRows"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin, wTabFolder);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsPreview  = new Listener() { public void handleEvent(Event e) { preview();   } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		lsTest     = new Listener() { public void handleEvent(Event e) { test(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		wPreview.addListener    (SWT.Selection, lsPreview    );
		wTest.addListener    (SWT.Selection, lsTest    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		wTabFolder.setSelection(0);
		// Set the shell size, based upon previous time...
		setSize();
		getData();
		activateKey();
		activateDynamicCommand();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wdynamicCommand.setSelection(input.isDynamicCommand());
		if(input.getCommand()!=null) wCommand.setText(input.getCommand());
		if(input.getcommandfieldname()!=null) wCommandField.setText(input.getcommandfieldname());
		if(input.getServerName()!=null) wServerName.setText(input.getServerName());
		if(input.getPort()!=null) wPort.setText(input.getPort());
		if(input.getuserName() !=null)   wUserName.setText(input.getuserName());
		if(input.getpassword() !=null)   wPassword.setText(input.getpassword());
        wUseKey.setSelection(input.isusePrivateKey());
        if(input.getKeyFileName()!=null) wPrivateKey.setText(input.getKeyFileName());
        if(input.getPassphrase()!=null) wPassphrase.setText(input.getPassphrase());
        if(input.getStdOutFieldName()!=null) wResultOutFieldName.setText(input.getStdOutFieldName());
        if(input.getStdErrFieldName()!=null) wResultErrFieldName.setText(input.getStdErrFieldName());
        wTimeOut.setText(Const.NVL(input.getTimeOut(), "0"));
        if(input.getProxyHost()!=null) wProxyHost.setText(input.getProxyHost());
        if(input.getProxyPort()!=null) wProxyPort.setText(input.getProxyPort());
        if(input.getProxyUsername()!=null) wProxyUsername.setText(input.getProxyUsername());
        if(input.getProxyPassword()!=null) wProxyPassword.setText(input.getProxyPassword());
        
		wStepname.selectAll();
	}

	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	private void getInfo(SSHMeta in) throws KettleException
	{
		stepname = wStepname.getText(); // return value
		
		in.setDynamicCommand(wdynamicCommand.getSelection());
		in.setCommand(wCommand.getText());
		in.setcommandfieldname(wCommandField.getText());
        in.setServerName(wServerName.getText());
        in.setPort(wPort.getText());
        in.setuserName(wUserName.getText());
        in.setpassword(wPassword.getText());
        in.usePrivateKey(wUseKey.getSelection());
        in.setKeyFileName(wPrivateKey.getText());
        in.setPassphrase(wPassphrase.getText());
        in.setstdOutFieldName(wResultOutFieldName.getText());
        in.setStdErrFieldName(wResultErrFieldName.getText());
        in.setTimeOut(wTimeOut.getText());
        in.setProxyHost(wProxyHost.getText());
        in.setProxyPort(wProxyPort.getText());
        in.setProxyUsername(wProxyUsername.getText());
        in.setProxyPassword(wProxyPassword.getText());
	}
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;
		
		try
        {
            getInfo(input);
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, "Error", "Error while previewing data", e);
        }
		
		dispose();
	}
	
	private void activateKey()
	{
		wPrivateKey.setEnabled(wUseKey.getSelection());
		wPassphrase.setEnabled(wUseKey.getSelection());
	}
	private void activateDynamicCommand()
	{
		wlCommand.setEnabled(!wdynamicCommand.getSelection());
		wCommand.setEnabled(!wdynamicCommand.getSelection());
		wlCommandField.setEnabled(wdynamicCommand.getSelection());
		wCommandField.setEnabled(wdynamicCommand.getSelection());
		wPreview.setEnabled(!wdynamicCommand.getSelection());
	}
	 private void get()
	 {
		 if(!gotPreviousFields)
		 {
			 gotPreviousFields=true;
			try
			{
				String source=wCommandField.getText();
				
				wCommandField.removeAll();
				RowMetaInterface r = transMeta.getPrevStepFields(stepname);
				if (r!=null)
				{
					wCommandField.setItems(r.getFieldNames());
					if(source!=null) wCommandField.setText(source);
				}
			}
			catch(KettleException ke)
			{
				new ErrorDialog(shell, BaseMessages.getString(PKG, "SSHDialog.FailedToGetFields.DialogTitle"), 
						BaseMessages.getString(PKG, "SSHDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
		 }
		}
	private void test()
    {
    	boolean testOK=false;
    	String errMsg=null;
    	String servername=transMeta.environmentSubstitute(wServerName.getText());
		int nrPort=Const.toInt(transMeta.environmentSubstitute(wPort.getText()), 22);
		String username = transMeta.environmentSubstitute(wUserName.getText());
		String password = transMeta.environmentSubstitute(wPassword.getText());
		String keyFilename = transMeta.environmentSubstitute(wPrivateKey.getText());
		String passphrase = transMeta.environmentSubstitute(wPassphrase.getText());
   		int timeOut = Const.toInt(transMeta.environmentSubstitute(wTimeOut.getText()), 0);
   		String proxyhost = transMeta.environmentSubstitute(wProxyHost.getText());
   		int proxyport = Const.toInt(transMeta.environmentSubstitute(wProxyPort.getText()), 0);
   		String proxyusername = transMeta.environmentSubstitute(wProxyUsername.getText());
   		String proxypassword = transMeta.environmentSubstitute(wProxyPassword.getText());
   		
   		
    	Connection conn= null;
    	try{
			conn = SSHMeta.OpenConnection(servername, nrPort, username, password, 
					wUseKey.getSelection(), keyFilename, passphrase, timeOut, transMeta,
					proxyhost, proxyport, proxyusername, proxypassword);
    		testOK=true;
	    	
    	}catch(Exception e) {
    		errMsg=e.getMessage();
    	}finally {
    		if(conn!=null) try { conn.close();}catch(Exception e){};
    	}
    	if(testOK) {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
			mb.setMessage(BaseMessages.getString(PKG, "SSHDialog.Connected.OK",servername, username) +Const.CR);
			mb.setText(BaseMessages.getString(PKG, "SSHDialog.Connected.Title.Ok"));
			mb.open();
		}else {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "SSHDialog.Connected.NOK.ConnectionBad",servername, username) +Const.CR+errMsg+Const.CR);
			mb.setText(BaseMessages.getString(PKG, "SSHDialog.Connected.Title.Bad"));
			mb.open(); 
	    }
	   
    }
	 /**
     * Preview the data generated by this step.
     * This generates a transformation using this step & a dummy and previews it.
     *
     */
    private void preview()
    {    	
    	  try
          {
              // Create the Access input step
              SSHMeta oneMeta = new SSHMeta();
              getInfo(oneMeta);
      		
              TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
              EnterNumberDialog numberDialog = new EnterNumberDialog(shell, 1, 
            		  BaseMessages.getString(PKG, "SSHDialog.NumberRows.DialogTitle"), 
            		  BaseMessages.getString(PKG, "SSHDialog.NumberRows.DialogMessage"));
              
              int previewSize = numberDialog.open();
              if (previewSize>0)
              {
                  TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
                  progressDialog.open();
                  
                  if (!progressDialog.isCancelled())
                  {
                      Trans trans = progressDialog.getTrans();
                      String loggingText = progressDialog.getLoggingText();
                      
                      if (trans.getResult()!=null && trans.getResult().getNrErrors()>0)
                      {
                      	EnterTextDialog etd = new EnterTextDialog(shell, 
                      			BaseMessages.getString(PKG, "System.Dialog.PreviewError.Title"),  
                      			BaseMessages.getString(PKG, "System.Dialog.PreviewError.Message"), loggingText, true );
                      	etd.setReadOnly();
                      	etd.open();
                      }
                      PreviewRowsDialog prd = new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(),
  							progressDialog.getPreviewRowsMeta(wStepname.getText()), progressDialog
  									.getPreviewRows(wStepname.getText()), loggingText);
  					 prd.open();
                      
                  }
              }
          }
          catch(KettleException e)
          {
              new ErrorDialog(shell, "Error", "Error while previewing data", e);
         }
    }
}
