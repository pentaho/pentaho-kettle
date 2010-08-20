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
 

package org.pentaho.di.ui.trans.steps.salesforceupdate;


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
import org.eclipse.swt.graphics.Cursor;
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
import org.pentaho.di.trans.steps.salesforceupdate.SalesforceUpdateMeta;
import org.pentaho.di.trans.steps.salesforceinput.SalesforceConnection;
import org.pentaho.di.trans.steps.salesforceinput.SalesforceConnectionUtils;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.core.util.StringUtil;

public class SalesforceUpdateDialog extends BaseStepDialog implements StepDialogInterface {
	
	private static Class<?> PKG = SalesforceUpdateMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	private CTabFolder wTabFolder;
	private FormData fdTabFolder;
	
	private CTabItem wGeneralTab;

	private Composite wGeneralComp ;

	private FormData fdGeneralComp;
	
	private FormData fdlModule, fdModule;
	
	private FormData fdlBatchSize, fdBatchSize;
	
	private FormData fdUserName,fdURL,fdPassword;
	
	private Label wlModule,wlBatchSize;
	
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
	

	private SalesforceUpdateMeta input;

    private LabelTextVar wUserName,wURL,wPassword;
    
    private TextVar wBatchSize;

    private ComboVar  wModule;    
	
	private Button wTest;
	
	private FormData fdTest;
    private Listener lsTest;
	
	private Group wConnectionGroup;
	private FormData fdConnectionGroup;
	
	private Group wSettingsGroup;
	private FormData fdSettingsGroup;
	
    private Label wlUseCompression;
    private FormData fdlUseCompression;
    private Button wUseCompression;
    private FormData fdUseCompression; 
    
    private Label wlTimeOut;
    private FormData fdlTimeOut; 
    private TextVar wTimeOut;
    private FormData fdTimeOut; 
	
    
	/**
	 * List of ColumnInfo that should have the field names of the selected salesforce module
	 */
	private List<ColumnInfo> tableFieldColumns = new ArrayList<ColumnInfo>();
	
    private boolean  gotModule = false;
    
	private boolean gotFields=false;
    
    private boolean  getModulesListError = false;     /* True if error getting modules list */
    
	public SalesforceUpdateDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		input = (SalesforceUpdateMeta) in;
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
		shell.setText(BaseMessages.getString(PKG, "SalesforceUpdateDialog.DialogTitle"));

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
		wGeneralTab.setText(BaseMessages.getString(PKG, "SalesforceUpdateDialog.General.Tab"));

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
		wConnectionGroup.setText(BaseMessages.getString(PKG, "SalesforceUpdateDialog.ConnectionGroup.Label"));
		
		FormLayout connectionGroupLayout = new FormLayout();
		connectionGroupLayout.marginWidth = 10;
		connectionGroupLayout.marginHeight = 10;
		wConnectionGroup.setLayout(connectionGroupLayout);
		
	     // Webservice URL
        wURL = new LabelTextVar(transMeta,wConnectionGroup, BaseMessages.getString(PKG, "SalesforceUpdateDialog.URL.Label"), BaseMessages.getString(PKG, "SalesforceUpdateDialog.URL.Tooltip"));
        props.setLook(wURL);
        wURL.addModifyListener(lsMod);
        fdURL = new FormData();
        fdURL.left = new FormAttachment(0, 0);
        fdURL.top = new FormAttachment(wStepname, margin);
        fdURL.right = new FormAttachment(100, 0);
        wURL.setLayoutData(fdURL);
        

	     // UserName line
        wUserName = new LabelTextVar(transMeta,wConnectionGroup, BaseMessages.getString(PKG, "SalesforceUpdateDialog.User.Label"), BaseMessages.getString(PKG, "SalesforceUpdateDialog.User.Tooltip"));
        props.setLook(wUserName);
        wUserName.addModifyListener(lsMod);
        fdUserName = new FormData();
        fdUserName.left = new FormAttachment(0, 0);
        fdUserName.top = new FormAttachment(wURL, margin);
        fdUserName.right = new FormAttachment(100, 0);
        wUserName.setLayoutData(fdUserName);
		
        // Password line
        wPassword = new LabelTextVar(transMeta,wConnectionGroup, BaseMessages.getString(PKG, "SalesforceUpdateDialog.Password.Label"), BaseMessages.getString(PKG, "SalesforceUpdateDialog.Password.Tooltip"));
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
		wTest.setText(BaseMessages.getString(PKG, "SalesforceUpdateDialog.TestConnection.Label"));
 		props.setLook(wTest);
		fdTest=new FormData();
		wTest.setToolTipText(BaseMessages.getString(PKG, "SalesforceUpdateDialog.TestConnection.Tooltip"));
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
		wSettingsGroup.setText(BaseMessages.getString(PKG, "SalesforceUpdateDialog.SettingsGroup.Label"));
		
		FormLayout settingGroupLayout = new FormLayout();
		settingGroupLayout.marginWidth = 10;
		settingGroupLayout.marginHeight = 10;
		wSettingsGroup.setLayout(settingGroupLayout);
		
		// Timeout
		wlTimeOut = new Label(wSettingsGroup, SWT.RIGHT);
		wlTimeOut.setText(BaseMessages.getString(PKG, "SalesforceUpdateDialog.TimeOut.Label"));
		props.setLook(wlTimeOut);
		fdlTimeOut = new FormData();
		fdlTimeOut.left = new FormAttachment(0, 0);
		fdlTimeOut.top = new FormAttachment(wSettingsGroup, margin);
		fdlTimeOut.right = new FormAttachment(middle, -margin);
		wlTimeOut.setLayoutData(fdlTimeOut);
		wTimeOut = new TextVar(transMeta,wSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wTimeOut);
		wTimeOut.addModifyListener(lsMod);
		fdTimeOut = new FormData();
		fdTimeOut.left = new FormAttachment(middle, 0);
		fdTimeOut.top = new FormAttachment(wSettingsGroup, margin);
		fdTimeOut.right = new FormAttachment(100, 0);
		wTimeOut.setLayoutData(fdTimeOut);
		
		
		// Use compression?
		wlUseCompression=new Label(wSettingsGroup, SWT.RIGHT);
		wlUseCompression.setText(BaseMessages.getString(PKG, "SalesforceUpdateDialog.UseCompression.Label"));
 		props.setLook(wlUseCompression);
		fdlUseCompression=new FormData();
		fdlUseCompression.left = new FormAttachment(0, 0);
		fdlUseCompression.top  = new FormAttachment(wTimeOut, margin);
		fdlUseCompression.right= new FormAttachment(middle, -margin);
		wlUseCompression.setLayoutData(fdlUseCompression);
		wUseCompression=new Button(wSettingsGroup, SWT.CHECK );
 		props.setLook(wUseCompression);
		wUseCompression.setToolTipText(BaseMessages.getString(PKG, "SalesforceUpdateDialog.UseCompression.Tooltip"));
		fdUseCompression=new FormData();
		fdUseCompression.left = new FormAttachment(middle, 0);
		fdUseCompression.top  = new FormAttachment(wTimeOut, margin);
		wUseCompression.setLayoutData(fdUseCompression);

		
		
		// BatchSize value
		wlBatchSize = new Label(wSettingsGroup, SWT.RIGHT);
		wlBatchSize.setText(BaseMessages.getString(PKG, "SalesforceUpdateDialog.Limit.Label"));
		props.setLook(wlBatchSize);
		fdlBatchSize = new FormData();
		fdlBatchSize.left = new FormAttachment(0, 0);
		fdlBatchSize.top = new FormAttachment(wUseCompression, margin);
		fdlBatchSize.right = new FormAttachment(middle, -margin);
		wlBatchSize.setLayoutData(fdlBatchSize);
		wBatchSize = new TextVar(transMeta,wSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wBatchSize);
		wBatchSize.addModifyListener(lsMod);
		fdBatchSize = new FormData();
		fdBatchSize.left = new FormAttachment(middle, 0);
		fdBatchSize.top = new FormAttachment(wUseCompression, margin);
		fdBatchSize.right = new FormAttachment(100, 0);
		wBatchSize.setLayoutData(fdBatchSize);
		
	        
		
 		// Module
		wlModule=new Label(wSettingsGroup, SWT.RIGHT);
        wlModule.setText(BaseMessages.getString(PKG, "SalesforceUpdateDialog.Module.Label"));
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
        wModule.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            	getModulesListError = false;
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                // check if the URL and login credentials passed and not just had error 
            	if (Const.isEmpty(wURL.getText()) || 
               		Const.isEmpty(wUserName.getText()) ||
            		Const.isEmpty(wPassword.getText()) ||
            		(getModulesListError )) return; 


                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                getModulesList();
                shell.setCursor(null);
                busy.dispose();
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
		
		
        
		// THE UPDATE/INSERT TABLE
		wlReturn = new Label(wGeneralComp, SWT.NONE);
		wlReturn.setText(BaseMessages.getString(PKG, "SalesforceUpdateDialog.UpdateFields.Label")); //$NON-NLS-1$
 		props.setLook(wlReturn);
		fdlReturn = new FormData();
		fdlReturn.left = new FormAttachment(0, 0);
		fdlReturn.top = new FormAttachment(wSettingsGroup, margin);
		wlReturn.setLayoutData(fdlReturn);

		int UpInsCols = 3;
		int UpInsRows = (input.getUpdateLookup() != null ? input.getUpdateLookup().length : 1);

		ciReturn = new ColumnInfo[UpInsCols];
		ciReturn[0] = new ColumnInfo(BaseMessages.getString(PKG, "SalesforceUpdateDialog.ColumnInfo.TableField"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciReturn[1] = new ColumnInfo(BaseMessages.getString(PKG, "SalesforceUpdateDialog.ColumnInfo.StreamField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciReturn[2] = new ColumnInfo(BaseMessages.getString(PKG, "SalesforceUpdateDialog.ColumnInfo.UseExternalId"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {"Y","N"}); //$NON-NLS-1$
		ciReturn[2].setToolTip(BaseMessages.getString(PKG, "SalesforceUpdateDialog.ColumnInfo.UseExternalId.Tooltip"));
		tableFieldColumns.add(ciReturn[0]);
		wReturn = new TableView(transMeta, wGeneralComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
				ciReturn, UpInsRows, lsMod, props);

		wGetLU = new Button(wGeneralComp, SWT.PUSH);
		wGetLU.setText(BaseMessages.getString(PKG, "SalesforceUpdateDialog.GetAndUpdateFields.Label")); //$NON-NLS-1$
		fdGetLU = new FormData();
		fdGetLU.top   = new FormAttachment(wlReturn, margin);
		fdGetLU.right = new FormAttachment(100, 0);
		wGetLU.setLayoutData(fdGetLU);
		
		
		wDoMapping = new Button(wGeneralComp, SWT.PUSH);
		wDoMapping.setText(BaseMessages.getString(PKG, "SalesforceUpdateDialog.EditMapping.Label")); //$NON-NLS-1$
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
                        tableItem.setText(3, "N");
                        return true;
                    }
                };
                BaseStepDialog.getFieldsFromPrevious(r, wReturn, 1, new int[] { 1, 2}, new int[] {}, -1, -1, listener);
			}
		}
		catch (KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "SalesforceUpdateDialog.FailedToGetFields.DialogTitle"), //$NON-NLS-1$
					BaseMessages.getString(PKG, "SalesforceUpdateDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$
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
			SalesforceUpdateMeta meta = new SalesforceUpdateMeta();
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
			mb.setMessage(BaseMessages.getString(PKG, "SalesforceUpdateDialog.Connected.OK",wUserName.getText()) +Const.CR);
			mb.setText(BaseMessages.getString(PKG, "SalesforceUpdateDialog.Connected.Title.Ok")); 
			mb.open();
		}else{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "SalesforceUpdateDialog.Connected.NOK",wUserName.getText(),msgError));
			mb.setText(BaseMessages.getString(PKG, "SalesforceUpdateDialog.Connected.Title.Error")); 
			mb.open(); 
		}
		
	}
 

	/**
	 * Read the data from the TextFileInputMeta object and show it in this
	 * dialog.
	 * 
	 * @param in
	 *            The SalesforceUpdateMeta object to obtain the data from.
	 */
	public void getData(SalesforceUpdateMeta in) 
	{
		wURL.setText(Const.NVL(in.getTargetURL(),""));
		wUserName.setText(Const.NVL(in.getUserName(),""));
		wPassword.setText(Const.NVL(in.getPassword(),""));
		wBatchSize.setText(in.getBatchSize());
		wModule.setText(Const.NVL(in.getModule(), "Account"));
		wBatchSize.setText("" + in.getBatchSize());

		if(log.isDebug()) log.logDebug(toString(), BaseMessages.getString(PKG, "SalesforceUpdateDialog.Log.GettingFieldsInfo"));

		if (input.getUpdateLookup() != null)
		{
			for (int i = 0; i < input.getUpdateLookup().length; i++)
			{
				TableItem item = wReturn.table.getItem(i);
				if (input.getUpdateLookup()[i] != null)
					item.setText(1, input.getUpdateLookup()[i]);
				if (input.getUpdateStream()[i] != null)
					item.setText(2, input.getUpdateStream()[i]);
				if (input.getUseExternalId()[i]==null||input.getUseExternalId()[i].booleanValue()) {
					item.setText(3,"Y");
				} else {
					item.setText(3,"N");
				}
			}
		}

		wReturn.removeEmptyRows();
		wReturn.setRowNums();
		wReturn.optWidth(true);
		wTimeOut.setText(Const.NVL(in.getTimeOut(), SalesforceConnectionUtils.DEFAULT_TIMEOUT));
		wUseCompression.setSelection(in.isUsingCompression());
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
					shell,BaseMessages.getString(PKG, "SalesforceUpdateDialog.ErrorValidateData.DialogTitle"),
					BaseMessages.getString(PKG, "SalesforceUpdateDialog.ErrorValidateData.DialogMessage"),	e);
		}
		dispose();
	}

	private void getInfo(SalesforceUpdateMeta in) throws KettleException {
		stepname = wStepname.getText(); // return value

		// copy info to SalesforceUpdateMeta class (input)
		in.setTargetURL(Const.NVL(wURL.getText(),SalesforceConnectionUtils.TARGET_DEFAULT_URL));
		in.setUserName(wUserName.getText());
		in.setPassword(wPassword.getText());
		in.setModule(Const.NVL(wModule.getText(),"Account"));
		in.setBatchSize(wBatchSize.getText());

		int nrfields = wReturn.nrNonEmpty();

		in.allocate(nrfields);

		for (int i = 0; i < nrfields; i++)
		{
			TableItem item = wReturn.getNonEmpty(i);
			in.getUpdateLookup()[i] = item.getText(1);
			in.getUpdateStream()[i] = item.getText(2);
			in.getUseExternalId()[i] = Boolean.valueOf("Y".equals(item.getText(3)));
		}
		in.setUseCompression(wUseCompression.getSelection());
		in.setTimeOut(Const.NVL(wTimeOut.getText(),"0"));
	}

	// check if module, username is given
	private boolean checkInput(){
        if (Const.isEmpty(wModule.getText()))
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
            mb.setMessage(BaseMessages.getString(PKG, "SalesforceUpdateDialog.ModuleMissing.DialogMessage"));
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
            mb.setMessage(BaseMessages.getString(PKG, "SalesforceUpdateDialog.UsernameMissing.DialogMessage"));
            mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
            mb.open(); 
            return false;
        }
        
        return true;
	}
	public String toString() {
		return this.getClass().getName();
	}
	
	private String[] getModuleFields() throws KettleException
	{
		  SalesforceUpdateMeta meta = new SalesforceUpdateMeta();
		  getInfo(meta);

		  SalesforceConnection connection=null;
		  String url = transMeta.environmentSubstitute(meta.getTargetURL());
		  try {
			  String selectedModule=transMeta.environmentSubstitute(meta.getModule());

			  // Define a new Salesforce connection
			  connection=new SalesforceConnection(log, url, transMeta.environmentSubstitute(meta.getUserName()),transMeta.environmentSubstitute(meta.getPassword())); 
			  int realTimeOut=Const.toInt(transMeta.environmentSubstitute(meta.getTimeOut()),0);
			  connection.setTimeOut(realTimeOut);
			  // connect to Salesforce
			  connection.connect();
			  // return fieldsname for the module
			  return connection.getFields(selectedModule);
		   } catch(Exception e) {
			  throw new KettleException("Erreur getting fields from module [" + url + "]!", e);
		   } finally{
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
		
		if(!checkInput()) return;
		
		// Determine the source and target fields...
		//
		RowMetaInterface sourceFields;
		RowMetaInterface targetFields = new RowMeta();

		try {
			sourceFields = transMeta.getPrevStepFields(stepMeta);
		} catch(KettleException e) {
			new ErrorDialog(shell, BaseMessages.getString(PKG, "SalesforceUpdateDialog.DoMapping.UnableToFindSourceFields.Title"), BaseMessages.getString(PKG, "SalesforceUpdateDialog.DoMapping.UnableToFindSourceFields.Message"), e);
			return;
		}

		try {
			  
			  String[] fields = getModuleFields();
			  for (int i = 0; i < fields.length; i++)  {
	            	targetFields.addValueMeta(new ValueMeta(fields[i]));
	           } 
		  }catch(Exception e) {
				new ErrorDialog(shell, BaseMessages.getString(PKG, "SalesforceUpdateDialog.DoMapping.UnableToFindTargetFields.Title"), BaseMessages.getString(PKG, "SalesforceUpdateDialog.DoMapping.UnableToFindTargetFields.Message"), e);
				return;
		  }
		  
		String[] inputNames = new String[sourceFields.size()];
		for (int i = 0; i < sourceFields.size(); i++) {
			ValueMetaInterface value = sourceFields.getValueMeta(i);
			inputNames[i] = value.getName()+ EnterMappingDialog.STRING_ORIGIN_SEPARATOR+value.getOrigin()+")";
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
				message+=BaseMessages.getString(PKG, "SalesforceUpdateDialog.DoMapping.SomeSourceFieldsNotFound", missingSourceFields.toString())+Const.CR;
			}
			if (missingTargetFields.length()>0) {
				message+=BaseMessages.getString(PKG, "SalesforceUpdateDialog.DoMapping.SomeTargetFieldsNotFound", missingSourceFields.toString())+Const.CR;
			}
			message+=Const.CR;
			message+=BaseMessages.getString(PKG, "SalesforceUpdateDialog.DoMapping.SomeFieldsNotFoundContinue")+Const.CR;
			MessageDialog.setDefaultImage(GUIResource.getInstance().getImageSpoon());
			boolean goOn = MessageDialog.openConfirm(shell, BaseMessages.getString(PKG, "SalesforceUpdateDialog.DoMapping.SomeFieldsNotFoundTitle"), message);
			if (!goOn) {
				return;
			}
		}
		EnterMappingDialog d = new EnterMappingDialog(SalesforceUpdateDialog.this.shell, sourceFields.getFieldNames(), targetFields.getFieldNames(), mappings);
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
	 private void getModulesList()
	  {
		  if (!gotModule){
			  SalesforceConnection connection=null;

			  try{
				  SalesforceUpdateMeta meta = new SalesforceUpdateMeta();
				  getInfo(meta);
				  String url = transMeta.environmentSubstitute(meta.getTargetURL());
				  
				  String selectedField= meta.getModule();
				  wModule.removeAll();

				  // Define a new Salesforce connection
				  connection=new SalesforceConnection(log, url, transMeta.environmentSubstitute(meta.getUserName()),transMeta.environmentSubstitute(meta.getPassword())); 
				  // connect to Salesforce
				  connection.connect();
				  // return 
				  wModule.setItems(connection.getModules());				  
				  
				  if(!Const.isEmpty(selectedField)) wModule.setText(selectedField);
				  
			      gotModule = true;
	        	  getModulesListError = false;
				  
			  }catch(Exception e)
			  {
					new ErrorDialog(shell,BaseMessages.getString(PKG, "SalesforceUpdateDialog.ErrorRetrieveModules.DialogTitle"),
							BaseMessages.getString(PKG, "SalesforceUpdateDialog.ErrorRetrieveData.ErrorRetrieveModules"),e);
					getModulesListError = true;
			  } finally{
				  if(connection!=null) {
						try {connection.close();}catch(Exception e){};
					}
		 	 }
		  }
	  }
	
	public void setModuleFieldCombo() {
		if(gotFields) return;
		gotFields=true;	
		Display display = shell.getDisplay();
		if (!(display==null || display.isDisposed())) {
			display.asyncExec(new Runnable () {
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
						    String[] fieldsName = getModuleFields();

						    if(fieldsName!=null) {
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
			});
		}
	}
	
}