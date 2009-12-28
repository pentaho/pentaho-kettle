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
 

package org.pentaho.di.ui.trans.steps.salesforceupsert;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.custom.CCombo;

import com.sforce.soap.partner.Field;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.salesforceinput.SalesforceConnection;
import org.pentaho.di.trans.steps.salesforceinput.SalesforceConnectionUtils;
import org.pentaho.di.trans.steps.salesforceupsert.SalesforceUpsertMeta;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.core.util.StringUtil;

public class SalesforceUpsertDialog extends BaseStepDialog implements StepDialogInterface {
	
	private static Class<?> PKG = SalesforceUpsertMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	private CTabFolder wTabFolder;
	private FormData fdTabFolder;
	
	private CTabItem wGeneralTab;

	private Composite wGeneralComp ;

	private FormData fdGeneralComp;
	
	private FormData fdlModule, fdModule;

	private FormData fdlUpsertField, fdUpsertField;
	
	private FormData fdlBatchSize, fdBatchSize;
	
	private FormData fdUserName,fdURL,fdPassword;
	
	private Label wlModule,wlBatchSize;
	
	private Label wlUpsertField;
	
    private Map<String, Integer> inputFields;
    
    private ColumnInfo[] ciReturn;
    
	private Button     wDoMapping;
	private FormData   fdDoMapping;
	
	private Label				wlReturn;
	private TableView			wReturn;
	private FormData			fdlReturn, fdReturn;
	
	private Button				wGetLU;
	private FormData			fdGetLU;
	private Listener			lsGetLU;
	

	private SalesforceUpsertMeta input;

    private LabelTextVar wUserName,wURL,wPassword;
    
    private TextVar wBatchSize;

    private ComboVar  wModule;

    private CCombo wUpsertField;
    
	
	private Button wTest;
	
	private FormData fdTest;
    private Listener lsTest;
	
	private Group wConnectionGroup;
	private FormData fdConnectionGroup;
	
	private Group wSettingsGroup, wOutFieldsGroup;
	private FormData fdSettingsGroup, fdOutFieldsGroup;
	
	private Label wlSalesforceIDFieldName;
	private FormData fdlSalesforceIDFieldName;
	
	private TextVar wSalesforceIDFieldName;
	private FormData fdSalesforceIDFieldName;
    
	/**
	 * List of ColumnInfo that should have the field names of the selected database table
	 */
	private List<ColumnInfo> tableFieldColumns = new ArrayList<ColumnInfo>();
    
	public SalesforceUpsertDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		input = (SalesforceUpsertMeta) in;
        inputFields =new HashMap<String, Integer>();
	}

	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX
				| SWT.MIN);
		props.setLook(shell);
		setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
			}
		};
		ModifyListener lsTableMod = new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				input.setChanged();
				setModuleFieldCombo();
			}
		};
		SelectionAdapter lsSelection = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e) 
			{
				input.setChanged();
				setModuleFieldCombo();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "SalesforceUpsertDialog.DialogTitle"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.top = new FormAttachment(0, margin);
		fdlStepname.right = new FormAttachment(middle, -margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		wTabFolder = new CTabFolder(shell, SWT.BORDER);
		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

		// ////////////////////////
		// START OF FILE TAB ///
		// ////////////////////////
		wGeneralTab = new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(BaseMessages.getString(PKG, "SalesforceUpsertDialog.General.Tab"));

		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
		
		///////////////////////////////// 
		// START OF Connection GROUP  //
		///////////////////////////////// 

		wConnectionGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wConnectionGroup);
		wConnectionGroup.setText(BaseMessages.getString(PKG, "SalesforceUpsertDialog.ConnectionGroup.Label"));
		
		FormLayout connectionGroupLayout = new FormLayout();
		connectionGroupLayout.marginWidth = 10;
		connectionGroupLayout.marginHeight = 10;
		wConnectionGroup.setLayout(connectionGroupLayout);
		
	     // Webservice URL
        wURL = new LabelTextVar(transMeta,wConnectionGroup, BaseMessages.getString(PKG, "SalesforceUpsertDialog.URL.Label"), BaseMessages.getString(PKG, "SalesforceUpsertDialog.URL.Tooltip"));
        props.setLook(wURL);
        wURL.addModifyListener(lsMod);
        fdURL = new FormData();
        fdURL.left = new FormAttachment(0, 0);
        fdURL.top = new FormAttachment(wStepname, margin);
        fdURL.right = new FormAttachment(100, 0);
        wURL.setLayoutData(fdURL);
        

	     // UserName line
        wUserName = new LabelTextVar(transMeta,wConnectionGroup, BaseMessages.getString(PKG, "SalesforceUpsertDialog.User.Label"), BaseMessages.getString(PKG, "SalesforceUpsertDialog.User.Tooltip"));
        props.setLook(wUserName);
        wUserName.addModifyListener(lsMod);
        fdUserName = new FormData();
        fdUserName.left = new FormAttachment(0, 0);
        fdUserName.top = new FormAttachment(wURL, margin);
        fdUserName.right = new FormAttachment(100, 0);
        wUserName.setLayoutData(fdUserName);
		
        // Password line
        wPassword = new LabelTextVar(transMeta,wConnectionGroup, BaseMessages.getString(PKG, "SalesforceUpsertDialog.Password.Label"), BaseMessages.getString(PKG, "SalesforceUpsertDialog.Password.Tooltip"));
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

		// Test Salesforce connection button
		wTest=new Button(wConnectionGroup,SWT.PUSH);
		wTest.setText(BaseMessages.getString(PKG, "SalesforceUpsertDialog.TestConnection.Label"));
 		props.setLook(wTest);
		fdTest=new FormData();
		wTest.setToolTipText(BaseMessages.getString(PKG, "SalesforceUpsertDialog.TestConnection.Tooltip"));
		//fdTest.left = new FormAttachment(middle, 0);
		fdTest.top  = new FormAttachment(wPassword, margin);
		fdTest.right= new FormAttachment(100, 0);
		wTest.setLayoutData(fdTest);
		
		fdConnectionGroup = new FormData();
		fdConnectionGroup.left = new FormAttachment(0, margin);
		fdConnectionGroup.top = new FormAttachment(wStepname, margin);
		fdConnectionGroup.right = new FormAttachment(100, -margin);
		wConnectionGroup.setLayoutData(fdConnectionGroup);
		
		///////////////////////////////// 
		// END OF Connection GROUP  //
		///////////////////////////////// 
		
		///////////////////////////////// 
		// START OF Settings GROUP  //
		///////////////////////////////// 

		wSettingsGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wSettingsGroup);
		wSettingsGroup.setText(BaseMessages.getString(PKG, "SalesforceUpsertDialog.SettingsGroup.Label"));
		
		FormLayout settingGroupLayout = new FormLayout();
		settingGroupLayout.marginWidth = 10;
		settingGroupLayout.marginHeight = 10;
		wSettingsGroup.setLayout(settingGroupLayout);
		
		// BatchSize value
		wlBatchSize = new Label(wSettingsGroup, SWT.RIGHT);
		wlBatchSize.setText(BaseMessages.getString(PKG, "SalesforceUpsertDialog.Limit.Label"));
		props.setLook(wlBatchSize);
		fdlBatchSize = new FormData();
		fdlBatchSize.left = new FormAttachment(0, 0);
		fdlBatchSize.top = new FormAttachment(wSettingsGroup, margin);
		fdlBatchSize.right = new FormAttachment(middle, -margin);
		wlBatchSize.setLayoutData(fdlBatchSize);
		wBatchSize = new TextVar(transMeta,wSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wBatchSize);
		wBatchSize.addModifyListener(lsMod);
		fdBatchSize = new FormData();
		fdBatchSize.left = new FormAttachment(middle, 0);
		fdBatchSize.top = new FormAttachment(wSettingsGroup, margin);
		fdBatchSize.right = new FormAttachment(100, 0);
		wBatchSize.setLayoutData(fdBatchSize);
		
	        
		
 		// Module
		wlModule=new Label(wSettingsGroup, SWT.RIGHT);
        wlModule.setText(BaseMessages.getString(PKG, "SalesforceUpsertDialog.Module.Label"));
        props.setLook(wlModule);
        fdlModule=new FormData();
        fdlModule.left = new FormAttachment(0, 0);
        fdlModule.top  = new FormAttachment(wBatchSize, margin);
        fdlModule.right= new FormAttachment(middle, -margin);
        wlModule.setLayoutData(fdlModule);
        wModule=new ComboVar(transMeta, wSettingsGroup, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        wModule.setEditable(true);
        props.setLook(wModule);
        wModule.addModifyListener(lsTableMod);
        wModule.addSelectionListener(lsSelection);
        fdModule=new FormData();
        fdModule.left = new FormAttachment(middle, 0);
        fdModule.top  = new FormAttachment(wBatchSize, margin);
        fdModule.right= new FormAttachment(100, -margin);
        wModule.setLayoutData(fdModule);
        wModule.setItems(SalesforceConnectionUtils.modulesList);
        
    	// Upsert Field
		wlUpsertField=new Label(wSettingsGroup, SWT.RIGHT);
        wlUpsertField.setText(BaseMessages.getString(PKG, "SalesforceUpsertDialog.Upsert.Label"));
        props.setLook(wlUpsertField);
        fdlUpsertField=new FormData();
        fdlUpsertField.left = new FormAttachment(0, 0);
        fdlUpsertField.top  = new FormAttachment(wModule, margin);
        fdlUpsertField.right= new FormAttachment(middle, -margin);
        wlUpsertField.setLayoutData(fdlUpsertField);
        wUpsertField=new CCombo(wSettingsGroup, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        wUpsertField.setEditable(true);
        props.setLook(wUpsertField);
        wUpsertField.addModifyListener(lsMod);
        fdUpsertField=new FormData();
        fdUpsertField.left = new FormAttachment(middle, 0);
        fdUpsertField.top  = new FormAttachment(wModule, margin);
        fdUpsertField.right= new FormAttachment(100, -margin);
        wUpsertField.setLayoutData(fdUpsertField);
        wUpsertField.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    getFieldsList();
                }
            }
        );

		fdSettingsGroup = new FormData();
		fdSettingsGroup.left = new FormAttachment(0, margin);
		fdSettingsGroup.top = new FormAttachment(wConnectionGroup, margin);
		fdSettingsGroup.right = new FormAttachment(100, -margin);
		wSettingsGroup.setLayoutData(fdSettingsGroup);
		
		///////////////////////////////// 
		// END OF Settings GROUP  //
		///////////////////////////////// 
		
		
		///////////////////////////////// 
		// START OF OutFields GROUP  //
		///////////////////////////////// 

		wOutFieldsGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wOutFieldsGroup);
		wOutFieldsGroup.setText(BaseMessages.getString(PKG, "SalesforceUpsertDialog.OutFieldsGroup.Label"));
		
		FormLayout OutFieldsGroupLayout = new FormLayout();
		OutFieldsGroupLayout.marginWidth = 10;
		OutFieldsGroupLayout.marginHeight = 10;
		wOutFieldsGroup.setLayout(OutFieldsGroupLayout);
		
		// SalesforceIDFieldName 
		wlSalesforceIDFieldName= new Label(wOutFieldsGroup, SWT.RIGHT);
		wlSalesforceIDFieldName.setText(BaseMessages.getString(PKG, "SalesforceUpsertDialog.SalesforceIDFieldName.Label"));
		props.setLook(wlSalesforceIDFieldName);
		fdlSalesforceIDFieldName= new FormData();
		fdlSalesforceIDFieldName.left = new FormAttachment(0, 0);
		fdlSalesforceIDFieldName.top = new FormAttachment(wSettingsGroup, margin);
		fdlSalesforceIDFieldName.right = new FormAttachment(middle, -margin);
		wlSalesforceIDFieldName.setLayoutData(fdlSalesforceIDFieldName);
		wSalesforceIDFieldName= new TextVar(transMeta,wOutFieldsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wSalesforceIDFieldName);
		wSalesforceIDFieldName.setToolTipText(BaseMessages.getString(PKG, "SalesforceUpsertDialog.SalesforceIDFieldName.Tooltip"));
		wSalesforceIDFieldName.addModifyListener(lsMod);
		fdSalesforceIDFieldName= new FormData();
		fdSalesforceIDFieldName.left = new FormAttachment(middle, 0);
		fdSalesforceIDFieldName.top = new FormAttachment(wSettingsGroup, margin);
		fdSalesforceIDFieldName.right = new FormAttachment(100, 0);
		wSalesforceIDFieldName.setLayoutData(fdSalesforceIDFieldName);
	
		fdOutFieldsGroup = new FormData();
		fdOutFieldsGroup.left = new FormAttachment(0, margin);
		fdOutFieldsGroup.top = new FormAttachment(wSettingsGroup, margin);
		fdOutFieldsGroup.right = new FormAttachment(100, -margin);
		wOutFieldsGroup.setLayoutData(fdOutFieldsGroup);
		
		///////////////////////////////// 
		// END OF OutFields GROUP  //
		///////////////////////////////// 
		
        
		// THE UPDATE/INSERT TABLE
		wlReturn = new Label(wGeneralComp, SWT.NONE);
		wlReturn.setText(BaseMessages.getString(PKG, "SalesforceUpsertDialog.UpdateFields.Label")); //$NON-NLS-1$
 		props.setLook(wlReturn);
		fdlReturn = new FormData();
		fdlReturn.left = new FormAttachment(0, 0);
		fdlReturn.top = new FormAttachment(wOutFieldsGroup, margin);
		wlReturn.setLayoutData(fdlReturn);

		int UpInsCols = 2;
		int UpInsRows = (input.getUpdateLookup() != null ? input.getUpdateLookup().length : 1);

		ciReturn = new ColumnInfo[UpInsCols];
		ciReturn[0] = new ColumnInfo(BaseMessages.getString(PKG, "SalesforceUpsertDialog.ColumnInfo.TableField"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciReturn[1] = new ColumnInfo(BaseMessages.getString(PKG, "SalesforceUpsertDialog.ColumnInfo.StreamField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		tableFieldColumns.add(ciReturn[0]);
		wReturn = new TableView(transMeta, wGeneralComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
				ciReturn, UpInsRows, lsMod, props);

		wGetLU = new Button(wGeneralComp, SWT.PUSH);
		wGetLU.setText(BaseMessages.getString(PKG, "SalesforceUpsertDialog.GetAndUpdateFields.Label")); //$NON-NLS-1$
		fdGetLU = new FormData();
		fdGetLU.top   = new FormAttachment(wlReturn, margin);
		fdGetLU.right = new FormAttachment(100, 0);
		wGetLU.setLayoutData(fdGetLU);
		
		
		wDoMapping = new Button(wGeneralComp, SWT.PUSH);
		wDoMapping.setText(BaseMessages.getString(PKG, "SalesforceUpsertDialog.EditMapping.Label")); //$NON-NLS-1$
		fdDoMapping = new FormData();
		fdDoMapping.top   = new FormAttachment(wGetLU, margin);
		fdDoMapping.right = new FormAttachment(100, 0);
		wDoMapping.setLayoutData(fdDoMapping);

		wDoMapping.addListener(SWT.Selection, new Listener() { 	public void handleEvent(Event arg0) { generateMappings();}});


		fdReturn = new FormData();
		fdReturn.left = new FormAttachment(0, 0);
		fdReturn.top = new FormAttachment(wlReturn, margin);
		fdReturn.right = new FormAttachment(wGetLU, -5*margin);
		fdReturn.bottom = new FormAttachment(100, -2*margin);
		wReturn.setLayoutData(fdReturn);
		
		
	    // 
        // Search the fields in the background
        //
        
        final Runnable runnable = new Runnable()
        {
            public void run()
            {
                StepMeta stepMeta = transMeta.findStep(stepname);
                if (stepMeta!=null)
                {
                    try
                    {
                    	RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);
                        
                        // Remember these fields...
                        for (int i=0;i<row.size();i++)
                        {
                        	inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
                        }
                        
                        setComboBoxes(); 
                    }
                    catch(KettleException e)
                    {
                        logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
                    }
                }
            }
        };
        new Thread(runnable).start();
        
        
		fdGeneralComp = new FormData();
		fdGeneralComp.left = new FormAttachment(0, 0);
		fdGeneralComp.top = new FormAttachment(wStepname, margin);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.bottom = new FormAttachment(100, 0);
		wGeneralComp.setLayoutData(fdGeneralComp);

		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);

	

		// THE BUTTONS
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);
		
		fdTabFolder = new FormData();
		fdTabFolder.left   = new FormAttachment(0, 0);
		fdTabFolder.top    = new FormAttachment(wStepname, margin);
		fdTabFolder.right  = new FormAttachment(100, 0);		
		fdTabFolder.bottom = new FormAttachment(wOK, -margin);
		wTabFolder.setLayoutData(fdTabFolder);
		
		// Add listeners
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};
		lsTest     = new Listener() { public void handleEvent(Event e) { test(); } };
		
		lsGetLU = new Listener()
		{
			public void handleEvent(Event e)
			{
				getUpdate();
			}
		};
		lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};

		wOK.addListener(SWT.Selection, lsOK);
		wGetLU.addListener(SWT.Selection, lsGetLU);
		wTest.addListener    (SWT.Selection, lsTest    );	
		wCancel.addListener(SWT.Selection, lsCancel);

		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};

		wStepname.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				cancel();
			}
		});

		wTabFolder.setSelection(0);

		// Set the shell size, based upon previous time...
		setSize();
		getData(input);
		setModuleFieldCombo();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
	}
	private void getUpdate()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r != null)
			{
                TableItemInsertListener listener = new TableItemInsertListener()
                {
                    public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
                    {
                        tableItem.setText(3, "Y");
                        return true;
                    }
                };
                BaseStepDialog.getFieldsFromPrevious(r, wReturn, 1, new int[] { 1, 2}, new int[] {}, -1, -1, listener);
			}
		}
		catch (KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "SalesforceUpsertDialog.FailedToGetFields.DialogTitle"), //$NON-NLS-1$
					BaseMessages.getString(PKG, "SalesforceUpsertDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$
		}
	}
 public void checkPasswordVisible()
    {
        String password = wPassword.getText();
        List<String> list = new ArrayList<String>();
        StringUtil.getUsedVariables(password, list, true);
        if (list.size() == 0)
            wPassword.setEchoChar('*');
        else
            wPassword.setEchoChar('\0'); // Show it all...
    }
	  
 
 private void test()
 {
	 boolean successConnection=true;
	 String msgError=null;
	 SalesforceConnection connection=null;
	try
     {
			SalesforceUpsertMeta meta = new SalesforceUpsertMeta();
			getInfo(meta);
			
			// check if the user is given
			if (!checkUser()) return;
			
			connection=new SalesforceConnection(log, transMeta.environmentSubstitute(meta.getTargetURL()),
					transMeta.environmentSubstitute(meta.getUserName()),
					transMeta.environmentSubstitute(meta.getPassword())); 
			connection.connect();
			
			successConnection=true;
	
		}
		catch(Exception e) {
			successConnection=false;
			msgError=e.getMessage();
		} finally{
			if(connection!=null) {
				try {connection.close();}catch(Exception e){};
			}
		}
		
		if(successConnection) {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
			mb.setMessage(BaseMessages.getString(PKG, "SalesforceUpsertDialog.Connected.OK",wUserName.getText()) +Const.CR);
			mb.setText(BaseMessages.getString(PKG, "SalesforceUpsertDialog.Connected.Title.Ok")); 
			mb.open();
		}else{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "SalesforceUpsertDialog.Connected.NOK",wUserName.getText(),msgError));
			mb.setText(BaseMessages.getString(PKG, "SalesforceUpsertDialog.Connected.Title.Error")); 
			mb.open(); 
		}
		
	}
 
  private void getFieldsList()
  {
	  try{
			  String selectedField=wUpsertField.getText();
			  wUpsertField.removeAll();

		    	  	
	      	  // loop through the objects and find build the list of fields
	          Field[] fields = getModuleFields();
	          String[] fieldList = new String[fields.length];    
	            for (int i = 0; i < fields.length; i++) 
	            {
	            	fieldList[i] = fields[i].getName();
	            } //for
	           wUpsertField.setItems(fieldList);
		 
			  
			  if(!Const.isEmpty(selectedField)) wUpsertField.setText(selectedField);
		  }catch(Exception e)
		  {
				new ErrorDialog(shell,BaseMessages.getString(PKG, "SalesforceUpsertDialog.ErrorRetrieveModules.DialogTitle"),
						BaseMessages.getString(PKG, "SalesforceUpsertDialog.ErrorRetrieveData.ErrorRetrieveModules"),e);
		  }

  }
	/**
	 * Read the data from the TextFileInputMeta object and show it in this
	 * dialog.
	 * 
	 * @param in
	 *            The SalesforceUpsertMeta object to obtain the data from.
	 */
	public void getData(SalesforceUpsertMeta in) 
	{
		wURL.setText(Const.NVL(in.getTargetURL(),""));
		wUserName.setText(Const.NVL(in.getUserName(),""));
		wPassword.setText(Const.NVL(in.getPassword(),""));
		wBatchSize.setText(in.getBatchSize());
		wModule.setText(Const.NVL(in.getModule(), "Account"));
		wUpsertField.setText(Const.NVL(in.getUpsertField(), "Id"));
		wBatchSize.setText("" + in.getBatchSize());
		wSalesforceIDFieldName.setText(Const.NVL(in.getSalesforceIDFieldName(),""));
		if(log.isDebug()) log.logDebug(toString(), BaseMessages.getString(PKG, "SalesforceUpsertDialog.Log.GettingFieldsInfo"));

		if (input.getUpdateLookup() != null)
			for (int i = 0; i < input.getUpdateLookup().length; i++)
			{
				TableItem item = wReturn.table.getItem(i);
				if (input.getUpdateLookup()[i] != null)
					item.setText(1, input.getUpdateLookup()[i]);
				if (input.getUpdateStream()[i] != null)
					item.setText(2, input.getUpdateStream()[i]);
			}
		

		wReturn.removeEmptyRows();
		wReturn.setRowNums();
		wReturn.optWidth(true);

		wStepname.selectAll();
	}

	private void cancel() {
		stepname = null;
		input.setChanged(changed);
		dispose();
	}

	private void ok() {
		try {
			getInfo(input);
		} catch (KettleException e) {
			new ErrorDialog(
					shell,BaseMessages.getString(PKG, "SalesforceUpsertDialog.ErrorValidateData.DialogTitle"),
					BaseMessages.getString(PKG, "SalesforceUpsertDialog.ErrorValidateData.DialogMessage"),	e);
		}
		dispose();
	}

	private void getInfo(SalesforceUpsertMeta in) throws KettleException {
		stepname = wStepname.getText(); // return value

		// copy info to SalesforceUpsertMeta class (input)
		in.setTargetURL(Const.NVL(wURL.getText(),SalesforceConnectionUtils.TARGET_DEFAULT_URL));
		in.setUserName(wUserName.getText());
		in.setPassword(wPassword.getText());
		in.setModule(Const.NVL(wModule.getText(),"Account"));
		in.setUpsertField(Const.NVL(wUpsertField.getText(), "Id"));
		in.setSalesforceIDFieldName(wSalesforceIDFieldName.getText());
		in.setBatchSize(wBatchSize.getText());

		int nrfields = wReturn.nrNonEmpty();

		in.allocate(nrfields);

		for (int i = 0; i < nrfields; i++)
		{
			TableItem item = wReturn.getNonEmpty(i);
			in.getUpdateLookup()[i] = item.getText(1);
			in.getUpdateStream()[i] = item.getText(2);
		}

	}

	// check if module, username is given
	private boolean checkInput(){
        if (Const.isEmpty(wModule.getText()))
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
            mb.setMessage(BaseMessages.getString(PKG, "SalesforceUpsertDialog.ModuleMissing.DialogMessage"));
            mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
            mb.open(); 
            return false;
        }
        return checkUser();
	}
	// check if module, username is given
	private boolean checkUser() {

        if (Const.isEmpty(wUserName.getText()))
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
            mb.setMessage(BaseMessages.getString(PKG, "SalesforceUpsertDialog.UsernameMissing.DialogMessage"));
            mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
            mb.open(); 
            return false;
        }
        
        return true;
	}
	public String toString() {
		return this.getClass().getName();
	}
	
	private Field[] getModuleFields() throws KettleException
	{
		  SalesforceUpsertMeta meta = new SalesforceUpsertMeta();
		  getInfo(meta);
		  
		  SalesforceConnection connection=null;
		  try {
			  
			  String selectedModule=transMeta.environmentSubstitute(meta.getModule());
			  connection=new SalesforceConnection(log, transMeta.environmentSubstitute(meta.getTargetURL()),
					  transMeta.environmentSubstitute(meta.getUserName()),
					  transMeta.environmentSubstitute(meta.getPassword())); 
			  connection.connect();
			
			  return connection.getModuleFields(selectedModule);
		  }
		  catch(Exception e) {
			  throw new KettleException("Erreur getting fields from module [" + transMeta.environmentSubstitute(meta.getTargetURL()) + "]!", e);
		  }
		  finally{
			  if(connection!=null) {
					try {connection.close();}catch(Exception e){};
				}
		  }
	}
	/**
	 * Reads in the fields from the previous steps and from the ONE next step and opens an 
	 * EnterMappingDialog with this information. After the user did the mapping, those information 
	 * is put into the Select/Rename table.
	 */
	private void generateMappings() {

		// Determine the source and target fields...
		//
		RowMetaInterface sourceFields;
		RowMetaInterface targetFields = new RowMeta();

		try {
			sourceFields = transMeta.getPrevStepFields(stepMeta);
		} catch(KettleException e) {
			new ErrorDialog(shell, BaseMessages.getString(PKG, "SalesforceUpsertDialog.DoMapping.UnableToFindSourceFields.Title"), BaseMessages.getString(PKG, "SalesforceUpsertDialog.DoMapping.UnableToFindSourceFields.Message"), e);
			return;
		}

		  SalesforceConnection connection=null;
		  try {
			  
			  SalesforceUpsertMeta meta = new SalesforceUpsertMeta();
			  getInfo(meta);
			  
			  // get real values
			  String selectedModule=transMeta.environmentSubstitute(wModule.getText());
			  
			  checkInput();
			  
			  connection=new SalesforceConnection(log, transMeta.environmentSubstitute(meta.getTargetURL()),
					  transMeta.environmentSubstitute(meta.getUserName()),
					  transMeta.environmentSubstitute(meta.getPassword())); 
			  connection.connect();
			
			  Field[] fields = connection.getModuleFields(selectedModule);
			  for (int i = 0; i < fields.length; i++)  {
	            	targetFields.addValueMeta(new ValueMeta(fields[i].getName()));
	           } 
		  }catch(Exception e) {
				new ErrorDialog(shell, BaseMessages.getString(PKG, "SalesforceUpsertDialog.DoMapping.UnableToFindTargetFields.Title"), BaseMessages.getString(PKG, "SalesforceUpsertDialog.DoMapping.UnableToFindTargetFields.Message"), e);
				return;
		  } finally{
				if(connection!=null) {
					try {connection.close();}catch(Exception e){};
				}
		  }
		  
		String[] inputNames = new String[sourceFields.size()];
		for (int i = 0; i < sourceFields.size(); i++) {
			ValueMetaInterface value = sourceFields.getValueMeta(i);
			inputNames[i] = value.getName()+
			     EnterMappingDialog.STRING_ORIGIN_SEPARATOR+value.getOrigin()+")";
		}

		// Create the existing mapping list...
		//
		List<SourceToTargetMapping> mappings = new ArrayList<SourceToTargetMapping>();
		StringBuffer missingSourceFields = new StringBuffer();
		StringBuffer missingTargetFields = new StringBuffer();

		int nrFields = wReturn.nrNonEmpty();
		for (int i = 0; i < nrFields ; i++) {
			TableItem item = wReturn.getNonEmpty(i);
			String source = item.getText(2);
			String target = item.getText(1);
			
			int sourceIndex = sourceFields.indexOfValue(source); 
			if (sourceIndex<0) {
				missingSourceFields.append(Const.CR + "   " + source+" --> " + target);
			}
			int targetIndex = targetFields.indexOfValue(target);
			if (targetIndex<0) {
				missingTargetFields.append(Const.CR + "   " + source+" --> " + target);
			}
			if (sourceIndex<0 || targetIndex<0) {
				continue;
			}

			SourceToTargetMapping mapping = new SourceToTargetMapping(sourceIndex, targetIndex);
			mappings.add(mapping);
		}

		// show a confirm dialog if some missing field was found
		//
		if (missingSourceFields.length()>0 || missingTargetFields.length()>0){
			
			String message="";
			if (missingSourceFields.length()>0) {
				message+=BaseMessages.getString(PKG, "SalesforceUpsertDialog.DoMapping.SomeSourceFieldsNotFound", missingSourceFields.toString())+Const.CR;
			}
			if (missingTargetFields.length()>0) {
				message+=BaseMessages.getString(PKG, "SalesforceUpsertDialog.DoMapping.SomeTargetFieldsNotFound", missingSourceFields.toString())+Const.CR;
			}
			message+=Const.CR;
			message+=BaseMessages.getString(PKG, "SalesforceUpsertDialog.DoMapping.SomeFieldsNotFoundContinue")+Const.CR;
			MessageDialog.setDefaultImage(GUIResource.getInstance().getImageSpoon());
			boolean goOn = MessageDialog.openConfirm(shell, BaseMessages.getString(PKG, "SalesforceUpsertDialog.DoMapping.SomeFieldsNotFoundTitle"), message);
			if (!goOn) {
				return;
			}
		}
		EnterMappingDialog d = new EnterMappingDialog(SalesforceUpsertDialog.this.shell, sourceFields.getFieldNames(), targetFields.getFieldNames(), mappings);
		mappings = d.open();

		// mappings == null if the user pressed cancel
		//
		if (mappings!=null) {
			// Clear and re-populate!
			//
			wReturn.table.removeAll();
			wReturn.table.setItemCount(mappings.size());
			for (int i = 0; i < mappings.size(); i++) {
				SourceToTargetMapping mapping = (SourceToTargetMapping) mappings.get(i);
				TableItem item = wReturn.table.getItem(i);
				item.setText(2, sourceFields.getValueMeta(mapping.getSourcePosition()).getName());
				item.setText(1, targetFields.getValueMeta(mapping.getTargetPosition()).getName());
			}
			wReturn.setRowNums();
			wReturn.optWidth(true);
		}
	}
	protected void setComboBoxes()
    {
        // Something was changed in the row.
        //
		final Map<String, Integer> fields = new HashMap<String, Integer>();
        
        // Add the currentMeta fields...
        fields.putAll(inputFields);
        
        Set<String> keySet = fields.keySet();
        List<String> entries = new ArrayList<String>(keySet);
        
        String[] fieldNames= (String[]) entries.toArray(new String[entries.size()]);
        Const.sortStrings(fieldNames);
        // return fields
        ciReturn[1].setComboValues(fieldNames);
    }
	private void setModuleFieldCombo(){

		Runnable fieldLoader = new Runnable() {
			public void run() {
				//clear
				for (int i = 0; i < tableFieldColumns.size(); i++) {
					ColumnInfo colInfo = (ColumnInfo) tableFieldColumns.get(i);
					colInfo.setComboValues(new String[] {});
				}
				if(wModule.isDisposed()) return;
				String selectedModule= transMeta.environmentSubstitute(wModule.getText());
				if (!Const.isEmpty(selectedModule)) {
					try {
			    		
							// loop through the objects and find build the list of fields
					        Field[] fields = getModuleFields(); 
					        String[] fieldsName = new String[fields.length];
					        for (int i = 0; i < fields.length; i++)  {
					        	fieldsName[i]=fields[i].getName();
				            } 
							if (null != fields) {
								for (int i = 0; i < tableFieldColumns.size(); i++) {
									ColumnInfo colInfo = (ColumnInfo) tableFieldColumns.get(i);
									colInfo.setComboValues(fieldsName);
								}
							}
					}catch (Exception e) {
						for (int i = 0; i < tableFieldColumns.size(); i++) {
							ColumnInfo colInfo = (ColumnInfo) tableFieldColumns	.get(i);
							colInfo.setComboValues(new String[] {});
						}
						// ignore any errors here. drop downs will not be
						// filled, but no problem for the user
					}
				}
			}
		};
		shell.getDisplay().asyncExec(fieldLoader);
	}
}