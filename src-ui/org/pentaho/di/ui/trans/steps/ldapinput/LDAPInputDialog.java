/*************************************************************************************** 
 * Copyright (C) 2007 Samatar, Brahim.  All rights reserved. 
 * This software was developed by Samatar, Brahim and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar, Brahim.  
 * The Initial Developer is Samatar, Brahim.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/

/*
 * Created on 21-09-2007
 *
 */

package org.pentaho.di.ui.trans.steps.ldapinput;

import java.text.SimpleDateFormat;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.ldapinput.LDAPInputField;
import org.pentaho.di.trans.steps.ldapinput.LDAPInputMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ControlSpaceKeyAdapter;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.core.encryption.Encr;

public class LDAPInputDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = LDAPInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wGeneralTab, wContentTab, wFieldsTab;

	private Composite    wGeneralComp, wContentComp, wFieldsComp;
	private FormData     fdGeneralComp, fdContentComp, fdFieldsComp;

	private Label        wlInclRownum;
	private Button       wInclRownum;
	private FormData     fdlInclRownum, fdRownum;
	
	private Label        wlsetPaging;
	private Button       wsetPaging;
	private FormData     fdlsetPaging, fdsetPaging;
	
	private Label        wlPageSize;
	private TextVar       wPageSize;
	private FormData     fdlPageSize, fdPageSize;
	
	private Label        wlusingAuthentication;
	private Button       wusingAuthentication;
	private FormData     fdlusingAuthentication;

	private Label        wlInclRownumField;
	private TextVar      wInclRownumField;
	private FormData     fdlInclRownumField, fdInclRownumField;
	
	private Label        wlLimit;
	private Text         wLimit;
	private FormData     fdlLimit, fdLimit;
	
	private Label        wlTimeLimit;
	private TextVar      wTimeLimit;
	private FormData     fdlTimeLimit, fdTimeLimit;
	
	private Label        wlMultiValuedSeparator;
	private TextVar      wMultiValuedSeparator;
	private FormData     fdlMultiValuedSeparator, fdMultiValuedSeparator;
   
	private TableView    wFields;
	private FormData     fdFields;

	private LDAPInputMeta input;
	
	private Group wAdditionalGroup;
	private FormData fdAdditionalGroup;
    
	private Group wConnectionGroup;
	private FormData fdConnectionGroup;
	
	private Group wSearchGroup;
	private FormData fdSearchGroup;
	
	
	private Label        wlHost;
	private TextVar      wHost;
	private FormData     fdlHost, fdHost;
	
	private Label        wlUserName;
	private TextVar      wUserName;
	private FormData     fdlUserName, fdUserName;
	
	private Label        wlPassword;
	private TextVar      wPassword;
	private FormData     fdlPassword, fdPassword;
	
	private Label        wlPort;
	private TextVar      wPort;
	private FormData     fdlPort, fdPort;
	
	private Button wTest;
	private FormData fdTest;
	
	
	private Label        wlSearchBase;
	private TextVar      wSearchBase;
	private FormData     fdlSearchBase, fdSearchBase;
	
    private Label wlFilterString;
    private Text wFilterString;
    private FormData fdlFilterString, fdFilterString;
    
    private Listener lsTest;
	
	public static final int dateLengths[] = new int[]
	{
		23, 19, 14, 10, 10, 10, 10, 8, 8, 8, 8, 6, 6
	};

	public LDAPInputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(LDAPInputMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "LDAPInputDialog.DialogTitle"));
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.top  = new FormAttachment(0, margin);
		fdlStepname.right= new FormAttachment(middle, -margin);
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
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

		//////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(BaseMessages.getString(PKG, "LDAPInputDialog.General.Tab"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout fileLayout = new FormLayout();
		fileLayout.marginWidth  = 3;
		fileLayout.marginHeight = 3;
		wGeneralComp.setLayout(fileLayout);
		
		// /////////////////////////////////
		// START OF Connection GROUP
		// /////////////////////////////////

		wConnectionGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wConnectionGroup);
		wConnectionGroup.setText(BaseMessages.getString(PKG, "LDAPInputDialog.Group.ConnectionGroup.Label"));
		
		FormLayout connectiongroupLayout = new FormLayout();
		connectiongroupLayout.marginWidth = 10;
		connectiongroupLayout.marginHeight = 10;
		wConnectionGroup.setLayout(connectiongroupLayout);

		// Host line
		wlHost=new Label(wConnectionGroup, SWT.RIGHT);
		wlHost.setText(BaseMessages.getString(PKG, "LDAPInputDialog.Host.Label"));
 		props.setLook(wlHost);
		fdlHost=new FormData();
		fdlHost.left = new FormAttachment(0, 0);
		fdlHost.top  = new FormAttachment(wStepname, margin);
		fdlHost.right= new FormAttachment(middle, -margin);
		wlHost.setLayoutData(fdlHost);
		wHost=new TextVar(transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wHost.setToolTipText(BaseMessages.getString(PKG, "LDAPInputDialog.Host.Tooltip"));
		props.setLook(wHost);
		wHost.addModifyListener(lsMod);
		fdHost=new FormData();
		fdHost.left = new FormAttachment(middle, 0);
		fdHost.top  = new FormAttachment(wStepname, margin);
		fdHost.right= new FormAttachment(100, 0);
		wHost.setLayoutData(fdHost);
		
		// Port line
		wlPort=new Label(wConnectionGroup, SWT.RIGHT);
		wlPort.setText(BaseMessages.getString(PKG, "LDAPInputDialog.Port.Label"));
 		props.setLook(wlPort);
		fdlPort=new FormData();
		fdlPort.left = new FormAttachment(0, 0);
		fdlPort.top  = new FormAttachment(wHost, margin);
		fdlPort.right= new FormAttachment(middle, -margin);
		wlPort.setLayoutData(fdlPort);
		wPort=new TextVar(transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wPort);
		wPort.setToolTipText(BaseMessages.getString(PKG, "LDAPInputDialog.Port.Tooltip"));
		wPort.addModifyListener(lsMod);
		fdPort=new FormData();
		fdPort.left = new FormAttachment(middle, 0);
		fdPort.top  = new FormAttachment(wHost, margin);
		fdPort.right= new FormAttachment(100, 0);
		wPort.setLayoutData(fdPort);
		
		// using authentication ?
		wlusingAuthentication=new Label(wConnectionGroup, SWT.RIGHT);
		wlusingAuthentication.setText(BaseMessages.getString(PKG, "LDAPInputDialog.usingAuthentication.Label"));
 		props.setLook(wlusingAuthentication);
		fdlusingAuthentication=new FormData();
		fdlusingAuthentication.left = new FormAttachment(0, 0);
		fdlusingAuthentication.top  = new FormAttachment(wPort, margin);
		fdlusingAuthentication.right= new FormAttachment(middle, -margin);
		wlusingAuthentication.setLayoutData(fdlusingAuthentication);
		wusingAuthentication=new Button(wConnectionGroup, SWT.CHECK );
 		props.setLook(wusingAuthentication);
		wusingAuthentication.setToolTipText(BaseMessages.getString(PKG, "LDAPInputDialog.usingAuthentication.Tooltip"));
		fdRownum=new FormData();
		fdRownum.left = new FormAttachment(middle, 0);
		fdRownum.top  = new FormAttachment(wPort, margin);
		wusingAuthentication.setLayoutData(fdRownum);
		
		wusingAuthentication.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				useAuthentication();
			}
		}
	);
	
		// UserName line
		wlUserName=new Label(wConnectionGroup, SWT.RIGHT);
		wlUserName.setText(BaseMessages.getString(PKG, "LDAPInputDialog.Username.Label"));
 		props.setLook(wlUserName);
		fdlUserName=new FormData();
		fdlUserName.left = new FormAttachment(0, 0);
		fdlUserName.top  = new FormAttachment(wusingAuthentication, margin);
		fdlUserName.right= new FormAttachment(middle, -margin);
		wlUserName.setLayoutData(fdlUserName);
		wUserName=new TextVar(transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wUserName);
		wUserName.setToolTipText(BaseMessages.getString(PKG, "LDAPInputDialog.Username.Tooltip"));
		wUserName.addModifyListener(lsMod);
		fdUserName=new FormData();
		fdUserName.left = new FormAttachment(middle, 0);
		fdUserName.top  = new FormAttachment(wusingAuthentication, margin);
		fdUserName.right= new FormAttachment(100, 0);
		wUserName.setLayoutData(fdUserName);

		// Password line
		wlPassword=new Label(wConnectionGroup, SWT.RIGHT);
		wlPassword.setText(BaseMessages.getString(PKG, "LDAPInputDialog.Password.Label"));
 		props.setLook(wlPassword);
		fdlPassword=new FormData();
		fdlPassword.left = new FormAttachment(0, 0);
		fdlPassword.top  = new FormAttachment(wUserName, margin);
		fdlPassword.right= new FormAttachment(middle, -margin);
		wlPassword.setLayoutData(fdlPassword);
		wPassword=new TextVar(transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wPassword.setToolTipText(BaseMessages.getString(PKG, "LDAPInputDialog.Password.Tooltip"));
 		props.setLook(wPassword);
        wPassword.setEchoChar('*');
        wPassword.addModifyListener(lsMod);
		fdPassword=new FormData();
		fdPassword.left = new FormAttachment(middle, 0);
		fdPassword.top  = new FormAttachment(wUserName, margin);
		fdPassword.right= new FormAttachment(100, 0);
		wPassword.setLayoutData(fdPassword);
		

		
		// Test LDAP connection button
		wTest=new Button(wConnectionGroup,SWT.PUSH);
		wTest.setText(BaseMessages.getString(PKG, "LDAPInputDialog.TestConnection.Label"));
 		props.setLook(wTest);
		fdTest=new FormData();
		wTest.setToolTipText(BaseMessages.getString(PKG, "LDAPInputDialog.TestConnection.Tooltip"));
		//fdTest.left = new FormAttachment(middle, 0);
		fdTest.top  = new FormAttachment(wPassword, margin);
		fdTest.right= new FormAttachment(100, 0);
		wTest.setLayoutData(fdTest);
		
		
		fdConnectionGroup = new FormData();
		fdConnectionGroup.left = new FormAttachment(0, margin);
		fdConnectionGroup.top = new FormAttachment(0, margin);
		fdConnectionGroup.right = new FormAttachment(100, -margin);
		wConnectionGroup.setLayoutData(fdConnectionGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF CONNECTION  GROUP
		// ///////////////////////////////////////////////////////////
		
		// /////////////////////////////////
		// START OF Search GROUP
		// /////////////////////////////////

		wSearchGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wSearchGroup);
		wSearchGroup.setText(BaseMessages.getString(PKG, "LDAPInputDialog.Group.SearchGroup.Label"));
		
		FormLayout searchgroupLayout = new FormLayout();
		searchgroupLayout.marginWidth = 10;
		searchgroupLayout.marginHeight = 10;
		wSearchGroup.setLayout(searchgroupLayout);
		
		// SearchBase line
		wlSearchBase=new Label(wSearchGroup, SWT.RIGHT);
		wlSearchBase.setText(BaseMessages.getString(PKG, "LDAPInputDialog.SearchBase.Label"));
 		props.setLook(wlSearchBase);
		fdlSearchBase=new FormData();
		fdlSearchBase.left = new FormAttachment(0, 0);
		fdlSearchBase.top  = new FormAttachment(wConnectionGroup, margin);
		fdlSearchBase.right= new FormAttachment(middle, -margin);
		wlSearchBase.setLayoutData(fdlSearchBase);
		wSearchBase=new TextVar(transMeta, wSearchGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wSearchBase);
		wSearchBase.setToolTipText(BaseMessages.getString(PKG, "LDAPInputDialog.SearchBase.Tooltip"));
		wSearchBase.addModifyListener(lsMod);
		fdSearchBase=new FormData();
		fdSearchBase.left = new FormAttachment(middle, 0);
		fdSearchBase.top  = new FormAttachment(wConnectionGroup, margin);
		fdSearchBase.right= new FormAttachment(100, 0);
		wSearchBase.setLayoutData(fdSearchBase);
		
		
		// Filter String
        wlFilterString = new Label(wSearchGroup, SWT.RIGHT);
        wlFilterString.setText(BaseMessages.getString(PKG, "LDAPInputDialog.FilterString.Label"));
        props.setLook(wlFilterString);
        fdlFilterString = new FormData();
        fdlFilterString.left = new FormAttachment(0, 0);
        fdlFilterString.top = new FormAttachment(wSearchBase, 2*margin);
        fdlFilterString.right = new FormAttachment(middle, -margin);
        wlFilterString.setLayoutData(fdlFilterString);

        wFilterString = new Text(wSearchGroup, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        wFilterString.setToolTipText(BaseMessages.getString(PKG, "LDAPInputDialog.FilterString.Tooltip"));
        props.setLook(wFilterString);
        wFilterString.addModifyListener(lsMod);
        fdFilterString = new FormData();
        fdFilterString.left = new FormAttachment(middle, 0);
        fdFilterString.top = new FormAttachment(wSearchBase, 2*margin);
        fdFilterString.right = new FormAttachment(100, 0);
        fdFilterString.bottom = new FormAttachment(100, -margin);
        wFilterString.setLayoutData(fdFilterString);
        wFilterString.addKeyListener(new ControlSpaceKeyAdapter(transMeta, wFilterString));
        
		
		
		fdSearchGroup = new FormData();
		fdSearchGroup.left = new FormAttachment(0, margin);
		fdSearchGroup.top = new FormAttachment(wConnectionGroup, margin);
		fdSearchGroup.right = new FormAttachment(100, -margin);
		fdSearchGroup.bottom = new FormAttachment(100, -margin);
		wSearchGroup.setLayoutData(fdSearchGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Search  GROUP
		// ///////////////////////////////////////////////////////////

	
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
		// START OF CONTENT TAB///
		///
		wContentTab=new CTabItem(wTabFolder, SWT.NONE);
		wContentTab.setText(BaseMessages.getString(PKG, "LDAPInputDialog.Content.Tab"));

		FormLayout contentLayout = new FormLayout ();
		contentLayout.marginWidth  = 3;
		contentLayout.marginHeight = 3;
		
		wContentComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wContentComp);
		wContentComp.setLayout(contentLayout);
		

        
 
		// /////////////////////////////////
		// START OF Additional Fields GROUP
		// /////////////////////////////////

		wAdditionalGroup = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wAdditionalGroup);
		wAdditionalGroup.setText(BaseMessages.getString(PKG, "LDAPInputDialog.Group.AdditionalGroup.Label"));
		
		FormLayout additionalgroupLayout = new FormLayout();
		additionalgroupLayout.marginWidth = 10;
		additionalgroupLayout.marginHeight = 10;
		wAdditionalGroup.setLayout(additionalgroupLayout);

		
	
		
		wlInclRownum=new Label(wAdditionalGroup, SWT.RIGHT);
		wlInclRownum.setText(BaseMessages.getString(PKG, "LDAPInputDialog.InclRownum.Label"));
 		props.setLook(wlInclRownum);
		fdlInclRownum=new FormData();
		fdlInclRownum.left = new FormAttachment(0, 0);
		fdlInclRownum.top  = new FormAttachment(0, margin);
		fdlInclRownum.right= new FormAttachment(middle, -margin);
		wlInclRownum.setLayoutData(fdlInclRownum);
		wInclRownum=new Button(wAdditionalGroup, SWT.CHECK );
 		props.setLook(wInclRownum);
		wInclRownum.setToolTipText(BaseMessages.getString(PKG, "LDAPInputDialog.InclRownum.Tooltip"));
		fdRownum=new FormData();
		fdRownum.left = new FormAttachment(middle, 0);
		fdRownum.top  = new FormAttachment(0, margin);
		wInclRownum.setLayoutData(fdRownum);

		wlInclRownumField=new Label(wAdditionalGroup, SWT.RIGHT);
		wlInclRownumField.setText(BaseMessages.getString(PKG, "LDAPInputDialog.InclRownumField.Label"));
 		props.setLook(wlInclRownumField);
		fdlInclRownumField=new FormData();
		fdlInclRownumField.left = new FormAttachment(wInclRownum, margin);
		fdlInclRownumField.top  = new FormAttachment(0, margin);
		wlInclRownumField.setLayoutData(fdlInclRownumField);
		wInclRownumField=new TextVar(transMeta,wAdditionalGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInclRownumField);
		wInclRownumField.addModifyListener(lsMod);
		fdInclRownumField=new FormData();
		fdInclRownumField.left = new FormAttachment(wlInclRownumField, margin);
		fdInclRownumField.top  = new FormAttachment(0, margin);
		fdInclRownumField.right= new FormAttachment(100, 0);
		wInclRownumField.setLayoutData(fdInclRownumField);
		
		

		
		fdAdditionalGroup = new FormData();
		fdAdditionalGroup.left = new FormAttachment(0, margin);
		fdAdditionalGroup.top = new FormAttachment(0, margin);
		fdAdditionalGroup.right = new FormAttachment(100, -margin);
		wAdditionalGroup.setLayoutData(fdAdditionalGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF DESTINATION ADDRESS  GROUP
		// ///////////////////////////////////////////////////////////
		
		
		wlLimit=new Label(wContentComp, SWT.RIGHT);
		wlLimit.setText(BaseMessages.getString(PKG, "LDAPInputDialog.Limit.Label"));
 		props.setLook(wlLimit);
		fdlLimit=new FormData();
		fdlLimit.left = new FormAttachment(0, 0);
		fdlLimit.top  = new FormAttachment(wAdditionalGroup, 2*margin);
		fdlLimit.right= new FormAttachment(middle, -margin);
		wlLimit.setLayoutData(fdlLimit);
		wLimit=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit=new FormData();
		fdLimit.left = new FormAttachment(middle, 0);
		fdLimit.top  = new FormAttachment(wAdditionalGroup, 2*margin);
		fdLimit.right= new FormAttachment(100, 0);
		wLimit.setLayoutData(fdLimit);
		
		// TimeLimit
		wlTimeLimit=new Label(wContentComp, SWT.RIGHT);
		wlTimeLimit.setText(BaseMessages.getString(PKG, "LDAPInputDialog.TimeLimit.Label"));
 		props.setLook(wlTimeLimit);
		fdlTimeLimit=new FormData();
		fdlTimeLimit.left = new FormAttachment(0, 0);
		fdlTimeLimit.top  = new FormAttachment(wLimit, margin);
		fdlTimeLimit.right= new FormAttachment(middle, -margin);
		wlTimeLimit.setLayoutData(fdlTimeLimit);
		wTimeLimit=new TextVar(transMeta,wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTimeLimit);
 		wTimeLimit.setToolTipText(BaseMessages.getString(PKG, "LDAPInputDialog.TimeLimit.Tooltip"));
		wTimeLimit.addModifyListener(lsMod);
		
		fdTimeLimit=new FormData();
		fdTimeLimit.left = new FormAttachment(middle, 0);
		fdTimeLimit.top  = new FormAttachment(wLimit, margin);
		fdTimeLimit.right= new FormAttachment(100, 0);
		wTimeLimit.setLayoutData(fdTimeLimit);
		
		// Multi valued field separator
		wlMultiValuedSeparator=new Label(wContentComp, SWT.RIGHT);
		wlMultiValuedSeparator.setText(BaseMessages.getString(PKG, "LDAPInputDialog.MultiValuedSeparator.Label"));
 		props.setLook(wlMultiValuedSeparator);
		fdlMultiValuedSeparator=new FormData();
		fdlMultiValuedSeparator.left = new FormAttachment(0, 0);
		fdlMultiValuedSeparator.top  = new FormAttachment(wTimeLimit, margin);
		fdlMultiValuedSeparator.right= new FormAttachment(middle, -margin);
		wlMultiValuedSeparator.setLayoutData(fdlMultiValuedSeparator);
		wMultiValuedSeparator=new TextVar(transMeta,wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMultiValuedSeparator);
 		wMultiValuedSeparator.setToolTipText(BaseMessages.getString(PKG, "LDAPInputDialog.MultiValuedSeparator.Tooltip"));
		wMultiValuedSeparator.addModifyListener(lsMod);
		fdMultiValuedSeparator=new FormData();
		fdMultiValuedSeparator.left = new FormAttachment(middle, 0);
		fdMultiValuedSeparator.top  = new FormAttachment(wTimeLimit, margin);
		fdMultiValuedSeparator.right= new FormAttachment(100, 0);
		wMultiValuedSeparator.setLayoutData(fdMultiValuedSeparator);
       
		// Use page ranging?
		wlsetPaging=new Label(wContentComp, SWT.RIGHT);
		wlsetPaging.setText(BaseMessages.getString(PKG, "LDAPInputDialog.setPaging.Label"));
 		props.setLook(wlsetPaging);
		fdlsetPaging=new FormData();
		fdlsetPaging.left = new FormAttachment(0, 0);
		fdlsetPaging.top  = new FormAttachment(wMultiValuedSeparator, margin);
		fdlsetPaging.right= new FormAttachment(middle, -margin);
		wlsetPaging.setLayoutData(fdlsetPaging);
		wsetPaging=new Button(wContentComp, SWT.CHECK );
 		props.setLook(wsetPaging);
		wsetPaging.setToolTipText(BaseMessages.getString(PKG, "LDAPInputDialog.setPaging.Tooltip"));
		fdsetPaging=new FormData();
		fdsetPaging.left = new FormAttachment(middle, 0);
		fdsetPaging.top  = new FormAttachment(wMultiValuedSeparator, margin);
		wsetPaging.setLayoutData(fdsetPaging);
		wsetPaging.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					setPaging();
				}
			}
		);
		wlPageSize=new Label(wContentComp, SWT.RIGHT);
		wlPageSize.setText(BaseMessages.getString(PKG, "LDAPInputDialog.PageSize.Label"));
 		props.setLook(wlPageSize);
		fdlPageSize=new FormData();
		fdlPageSize.left = new FormAttachment(wsetPaging, margin);
		fdlPageSize.top  = new FormAttachment(wMultiValuedSeparator, margin);
		wlPageSize.setLayoutData(fdlPageSize);
		wPageSize=new TextVar(transMeta,wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wPageSize);
 		wPageSize.addModifyListener(lsMod);
 		fdPageSize=new FormData();
 		fdPageSize.left = new FormAttachment(wlPageSize, margin);
 		fdPageSize.top  = new FormAttachment(wMultiValuedSeparator, margin);
 		fdPageSize.right= new FormAttachment(100, 0);
		wPageSize.setLayoutData(fdPageSize);
		
		
		fdContentComp = new FormData();
		fdContentComp.left  = new FormAttachment(0, 0);
		fdContentComp.top   = new FormAttachment(0, 0);
		fdContentComp.right = new FormAttachment(100, 0);
		fdContentComp.bottom= new FormAttachment(100, 0);
		wContentComp.setLayoutData(fdContentComp);

		wContentComp.layout();
		wContentTab.setControl(wContentComp);

		// ///////////////////////////////////////////////////////////
		// / END OF CONTENT TAB
		// ///////////////////////////////////////////////////////////


		// Fields tab...
		//
		wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
		wFieldsTab.setText(BaseMessages.getString(PKG, "LDAPInputDialog.Fields.Tab"));
		
		FormLayout fieldsLayout = new FormLayout ();
		fieldsLayout.marginWidth  = Const.FORM_MARGIN;
		fieldsLayout.marginHeight = Const.FORM_MARGIN;
		
		wFieldsComp = new Composite(wTabFolder, SWT.NONE);
		wFieldsComp.setLayout(fieldsLayout);
 		props.setLook(wFieldsComp);
		
 		wGet=new Button(wFieldsComp, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "LDAPInputDialog.GetFields.Button"));
		fdGet=new FormData();
		fdGet.left=new FormAttachment(50, 0);
		fdGet.bottom =new FormAttachment(100, 0);
		wGet.setLayoutData(fdGet);
		
		final int FieldsRows=input.getInputFields().length;
		
		ColumnInfo[] colinf=new ColumnInfo[]
            {
			 new ColumnInfo(
         BaseMessages.getString(PKG, "LDAPInputDialog.FieldsTable.Name.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
         new ColumnInfo(BaseMessages.getString(PKG, "LDAPInputDialog.FieldsTable.Attribute.Column"),ColumnInfo.COLUMN_TYPE_TEXT,false),
		 new ColumnInfo(BaseMessages.getString(PKG, "LDAPInputDialog.FieldsTable.Type.Column"),ColumnInfo.COLUMN_TYPE_CCOMBO,ValueMeta.getTypes(),true ),
		 new ColumnInfo(BaseMessages.getString(PKG, "LDAPInputDialog.FieldsTable.Format.Column"),
         ColumnInfo.COLUMN_TYPE_FORMAT, 3),
         new ColumnInfo(
         BaseMessages.getString(PKG, "LDAPInputDialog.FieldsTable.Length.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         BaseMessages.getString(PKG, "LDAPInputDialog.FieldsTable.Precision.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         BaseMessages.getString(PKG, "LDAPInputDialog.FieldsTable.Currency.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         BaseMessages.getString(PKG, "LDAPInputDialog.FieldsTable.Decimal.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         BaseMessages.getString(PKG, "LDAPInputDialog.FieldsTable.Group.Column"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         BaseMessages.getString(PKG, "LDAPInputDialog.FieldsTable.TrimType.Column"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,
         LDAPInputField.trimTypeDesc,
         true ),
			 new ColumnInfo(
         BaseMessages.getString(PKG, "LDAPInputDialog.FieldsTable.Repeat.Column"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,
         new String[] { BaseMessages.getString(PKG, "System.Combo.Yes"), BaseMessages.getString(PKG, "System.Combo.No") },
         true ),
     
    };
		
		colinf[0].setUsingVariables(true);
		colinf[0].setToolTip(BaseMessages.getString(PKG, "LDAPInputDialog.FieldsTable.Name.Column.Tooltip"));
		colinf[1].setUsingVariables(true);
		colinf[1].setToolTip(BaseMessages.getString(PKG, "LDAPInputDialog.FieldsTable.Attribute.Column.Tooltip"));
		
		wFields=new TableView(transMeta,wFieldsComp, 
						      SWT.FULL_SELECTION | SWT.MULTI, 
						      colinf, 
						      FieldsRows,  
						      lsMod,
							  props
						      );

		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(0, 0);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom= new FormAttachment(wGet, -margin);
		wFields.setLayoutData(fdFields);

		fdFieldsComp=new FormData();
		fdFieldsComp.left  = new FormAttachment(0, 0);
		fdFieldsComp.top   = new FormAttachment(0, 0);
		fdFieldsComp.right = new FormAttachment(100, 0);
		fdFieldsComp.bottom= new FormAttachment(100, 0);
		wFieldsComp.setLayoutData(fdFieldsComp);
		
		wFieldsComp.layout();
		wFieldsTab.setControl(wFieldsComp);
		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);
		
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));

		wPreview=new Button(shell, SWT.PUSH);
		wPreview.setText(BaseMessages.getString(PKG, "LDAPInputDialog.Button.PreviewRows"));
		
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
		
		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin, wTabFolder);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();      } };
		lsTest     = new Listener() { public void handleEvent(Event e) { test(); } };
		lsPreview  = new Listener() { public void handleEvent(Event e) { preview();   } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();     } };
		
		wOK.addListener     (SWT.Selection, lsOK     );
		wGet.addListener    (SWT.Selection, lsGet    );
		wTest.addListener    (SWT.Selection, lsTest    );		
		wPreview.addListener(SWT.Selection, lsPreview);
		wCancel.addListener (SWT.Selection, lsCancel );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wLimit.addSelectionListener( lsDef );
		wInclRownumField.addSelectionListener( lsDef );

		
		
			
		// Enable/disable the right fields to allow a row number to be added to each row...
		wInclRownum.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					setIncludeRownum();
				}
			}
		);
		

		
		
		
	
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		wTabFolder.setSelection(0);

		// Set the shell size, based upon previous time...
		setSize();
		getData(input);
		useAuthentication();
		setPaging();
		input.setChanged(changed);
	
		wFields.optWidth(true);
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	private void test()
	{
		try
        {
		
			LDAPInputMeta meta = new LDAPInputMeta();
			getInfo(meta);
			
			DirContext ctx = connectServerLdap(transMeta.environmentSubstitute(meta.getHost()),
					transMeta.environmentSubstitute(meta.getUserName()), 
					Encr.decryptPasswordOptionallyEncrypted(transMeta.environmentSubstitute(meta.getPassword())),
					transMeta.environmentSubstitute(meta.getPort()));
			
		    if(Const.isEmpty(wSearchBase.getText()))
		    {
			     // get Search Base
			     Attributes attrs = ctx.getAttributes("", new String[] { "namingContexts" });
				 Attribute attr = attrs.get("namingContexts");
				 
				 // Update Search Base
				 wSearchBase.setText(attr.get().toString());
		    } 
			
			if(ctx!=null)
			{
				
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
				mb.setMessage(BaseMessages.getString(PKG, "LDAPInputDialog.Connected.OK") +Const.CR); //$NON-NLS-1$
				mb.setText(BaseMessages.getString(PKG, "LDAPInputDialog.Connected.Title.Ok")); //$NON-NLS-1$
				mb.open();
				
			}
			else
			{	
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage(BaseMessages.getString(PKG, "LDAPInputDialog.Connected.NOK.DirecttoryContextNull"));
				mb.setText(BaseMessages.getString(PKG, "LDAPInputDialog.Connected.Title.Error")); //$NON-NLS-1$
				mb.open(); 
				
			}
			
				
		}
		catch(Exception e)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "LDAPInputDialog.Connected.NOK",e.getMessage()));
			mb.setText(BaseMessages.getString(PKG, "LDAPInputDialog.Connected.Title.Error")); //$NON-NLS-1$
			mb.open(); 
		} 
	}

	private void get()
	{
        try
        {
		
    		LDAPInputMeta meta = new LDAPInputMeta();
    		getInfo(meta);
            
            // Clear Fields Grid
            wFields.removeAll();

    		String port=transMeta.environmentSubstitute(meta.getPort());
    		String hostname=transMeta.environmentSubstitute(meta.getHost());
    		String username=transMeta.environmentSubstitute(meta.getUserName());
    		String password=Encr.decryptPasswordOptionallyEncrypted(transMeta.environmentSubstitute(meta.getPassword()));
            //Set the filter string.  The more exact of the search string
    		String filter=transMeta.environmentSubstitute(meta.getFilterString());
    		//Set the Search base.This is the place where the search will
    		String searchbase=transMeta.environmentSubstitute(meta.getSearchBase());
    	
    		NamingEnumeration<SearchResult> results=null;
    		
    		DirContext ctx = connectServerLdap(hostname,username, password,port);
			
		     
		     log.logBasic("Connection", "Connected to server [{0}]",hostname);	      
		     
		     SearchControls controls = new SearchControls();
	         controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	         // Set search
	         results = ctx.search(searchbase,filter, controls);
	        
	        // Get all attributes
	        SearchResult searchAttr = results.next();
	        
	        Attributes listattributes = searchAttr.getAttributes(); 
	       
	        NamingEnumeration<? extends Attribute> ne = listattributes.getAll();
		   
	        Attribute attr = null;
	        
	        while (ne.hasMore()) 
	        {
	        	attr = ne.next();
	    	    
	    		String fieldName = attr.getID();
				
				// Get Column Name
	            TableItem item = new TableItem(wFields.table, SWT.NONE);
	            item.setText(1, fieldName);
	            item.setText(2, fieldName);
	            
	            String attributeValue=attr.getID();
	            // Try to get the Type
	            if(IsDate(attributeValue))
        		{
        			item.setText(3, "Date");
        		}
	            else if(IsInteger(attributeValue))
        		{
        			item.setText(3, "Integer");
        		}
	            else if(IsNumber(attributeValue))
        		{
        			item.setText(3, "Number");
        		}	
	           
	            else
	            {
	            	item.setText(3, "String");	    		            
	            }  
	    	    
	        }
	        
	        
         
            wFields.removeEmptyRows();
            wFields.setRowNums();
            wFields.optWidth(true);            
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "LDAPInputDialog.ErrorGettingColums.DialogTitle"), BaseMessages.getString(PKG, "LDAPInputDialog.ErrorGettingColums.DialogMessage"), e);
        }
    	catch(Exception e)
		{
    		 new ErrorDialog(shell, BaseMessages.getString(PKG, "LDAPInputDialog.ErrorGettingColums.DialogTitle"), BaseMessages.getString(PKG, "LDAPInputDialog.ErrorGettingColums.DialogMessage"), e);

		}  
	}
	 public InitialDirContext connectServerLdap(String hostname,String username, String password,String port) throws NamingException {

	        Hashtable<String, String> env = new Hashtable<String, String>();

	        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	        env.put(Context.PROVIDER_URL, "ldap://"+hostname + ":" +  Const.toInt(port,389));
	        env.put(Context.SECURITY_AUTHENTICATION, "simple" );
	        if(wusingAuthentication.getSelection())
	        {
		        env.put(Context.SECURITY_PRINCIPAL, username);
		        env.put(Context.SECURITY_CREDENTIALS, password); 
	        }
	       

	        return new InitialDirContext(env);
	    }
	private boolean IsInteger(String str)
	{
		  try 
		  {
		    Integer.parseInt(str);
		  }
		  catch(NumberFormatException e)   {return false; }
		  return true;
	}
	private void setPaging()
	{
		wlPageSize.setEnabled(wsetPaging.getSelection());
		wPageSize.setEnabled(wsetPaging.getSelection());
	}
	private boolean IsNumber(String str)
	{
		  try 
		  {
		     Float.parseFloat(str);
		  }
		  catch(Exception e)   {return false; }
		  return true;
	}
	
	private boolean IsDate(String str)
	{
		  // TODO: What about other dates? Maybe something for a CRQ
		  try 
		  {
		        SimpleDateFormat fdate = new SimpleDateFormat("yy-mm-dd");
		        fdate.parse(str);
		  }
		  catch(Exception e)   {return false; }
		  return true;
	}

	public void setIncludeRownum()
	{
		wlInclRownumField.setEnabled(wInclRownum.getSelection());
		wInclRownumField.setEnabled(wInclRownum.getSelection());
	}
	



	/**
	 * Read the data from the LDAPInputMeta object and show it in this dialog.
	 * 
	 * @param in The LDAPInputMeta object to obtain the data from.
	 */
	public void getData(LDAPInputMeta in)
	{
		
		wInclRownum.setSelection(in.includeRowNumber());
		if (in.getRowNumberField()!=null) wInclRownumField.setText(in.getRowNumberField());
		
		wusingAuthentication.setSelection(in.UseAuthentication());
		wsetPaging.setSelection(in.isPaging());
		if (in.getPageSize()!=null) wPageSize.setText(in.getPageSize());
		
		wLimit.setText(""+in.getRowLimit());
		wTimeLimit.setText(""+in.getTimeLimit());
		if(in.getMultiValuedSeparator()!=null)	wMultiValuedSeparator.setText(in.getMultiValuedSeparator());

		if (in.getHost() != null)  wHost.setText(in.getHost());
		if (in.getUserName() != null)  wUserName.setText(in.getUserName());
	    if (in.getPassword() != null)  wPassword.setText(in.getPassword());
		if (in.getPort() != null)  wPort.setText(in.getPort());
		
		if (in.getFilterString() != null)  wFilterString.setText(in.getFilterString());
		if (in.getSearchBase()!= null)  wSearchBase.setText(in.getSearchBase());
		
		
		logDebug(BaseMessages.getString(PKG, "LDAPInputDialog.Log.GettingFieldsInfo"));
		for (int i=0;i<in.getInputFields().length;i++)
		{
		    LDAPInputField field = in.getInputFields()[i];
		    
            if (field!=null)
            {
    			TableItem item  = wFields.table.getItem(i);
    			String name     = field.getName();
    			String xpath	= field.getAttribute();
    			String type     = field.getTypeDesc();
    			String format   = field.getFormat();
    			String length   = ""+field.getLength();
    			String prec     = ""+field.getPrecision();
    			String curr     = field.getCurrencySymbol();
    			String group    = field.getGroupSymbol();
    			String decim    = field.getDecimalSymbol();
    			String trim     = field.getTrimTypeDesc();
    			String rep      = field.isRepeated()?BaseMessages.getString(PKG, "System.Combo.Yes"):BaseMessages.getString(PKG, "System.Combo.No");
    			
                if (name    !=null) item.setText( 1, name);
                if (xpath   !=null) item.setText( 2, xpath);
    			if (type    !=null) item.setText( 3, type);
    			if (format  !=null) item.setText( 4, format);
    			if (length  !=null && !"-1".equals(length)) item.setText( 5, length);
    			if (prec    !=null && !"-1".equals(prec)) item.setText( 6, prec);
    			if (curr    !=null) item.setText( 7, curr);
    			if (decim   !=null) item.setText( 8, decim);
    			if (group   !=null) item.setText( 9, group);
    			if (trim    !=null) item.setText(10, trim);
    			if (rep     !=null) item.setText(11, rep);                
            }
		}
        
        wFields.removeEmptyRows();
        wFields.setRowNums();
        wFields.optWidth(true);

		setIncludeRownum();

		wStepname.selectAll();
	}

	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		stepname = wStepname.getText();
        try
        {
            getInfo(input);
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "LDAPInputDialog.ErrorParsingData.DialogTitle"), BaseMessages.getString(PKG, "LDAPInputDialog.ErrorParsingData.DialogMessage"), e);
        }
		dispose();
	}
	
	private void getInfo(LDAPInputMeta in) throws KettleException
	{
		stepname = wStepname.getText(); // return value

		// copy info to TextFileInputMeta class (input)
		in.setRowLimit( Const.toLong(wLimit.getText(), 0L) );
		in.setTimeLimit( Const.toInt(wTimeLimit.getText(), 0) );
		in.setMultiValuedSeparator(wMultiValuedSeparator.getText());
		in.setIncludeRowNumber( wInclRownum.getSelection() );
		in.setUseAuthentication( wusingAuthentication.getSelection() );
		in.setPaging(wsetPaging.getSelection() );
		in.setPageSize(wPageSize.getText());
		in.setRowNumberField( wInclRownumField.getText() );
		in.setHost( wHost.getText() );
		in.setUserName( wUserName.getText() );
		in.setPassword(wPassword.getText());
		in.setPort( wPort.getText() );
		in.setFilterString( wFilterString.getText() );
		in.setSearchBase( wSearchBase.getText() );
		
		int nrFields    = wFields.nrNonEmpty();
         
		in.allocate(nrFields);


		for (int i=0;i<nrFields;i++)
		{
		    LDAPInputField field = new LDAPInputField();
		    
			TableItem item  = wFields.getNonEmpty(i);
            
			field.setName( item.getText(1) );
			field.setAttribute(item.getText(2));
			field.setType(ValueMeta.getType(item.getText(3)));
			field.setFormat( item.getText(4) );
			field.setLength( Const.toInt(item.getText(5), -1) );
			field.setPrecision( Const.toInt(item.getText(6), -1) );
			field.setCurrencySymbol( item.getText(7) );
			field.setDecimalSymbol( item.getText(8) );
			field.setGroupSymbol( item.getText(9) );
			field.setTrimType( LDAPInputField.getTrimTypeByDesc(item.getText(10)) );
			field.setRepeated( BaseMessages.getString(PKG, "System.Combo.Yes").equalsIgnoreCase(item.getText(11)) );		
            
			in.getInputFields()[i] = field;
		}	
	}
	
	private void useAuthentication()
	{
		wUserName.setEnabled(wusingAuthentication.getSelection());
		wlUserName.setEnabled(wusingAuthentication.getSelection());
		wPassword.setEnabled(wusingAuthentication.getSelection());
		wlPassword.setEnabled(wusingAuthentication.getSelection());
	}
	
		
	// Preview the data
	private void preview()
	{
        try
        {
            // Create the XML input step
            LDAPInputMeta oneMeta = new LDAPInputMeta();
            getInfo(oneMeta);
        

        
            TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
            
            EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), BaseMessages.getString(PKG, "LDAPInputDialog.NumberRows.DialogTitle"), BaseMessages.getString(PKG, "LDAPInputDialog.NumberRows.DialogMessage"));
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
                    	EnterTextDialog etd = new EnterTextDialog(shell, BaseMessages.getString(PKG, "System.Dialog.PreviewError.Title"),  
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
            new ErrorDialog(shell, BaseMessages.getString(PKG, "LDAPInputDialog.ErrorPreviewingData.DialogTitle"), BaseMessages.getString(PKG, "LDAPInputDialog.ErrorPreviewingData.DialogMessage"), e);
       }
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}
	
	
}