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
 

package org.pentaho.di.ui.trans.steps.salesforceinput;

import java.util.ArrayList;
import java.util.List;

import org.apache.axis.message.MessageElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
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
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.salesforceinput.SalesforceConnection;
import org.pentaho.di.trans.steps.salesforceinput.SalesforceConnectionUtils;
import org.pentaho.di.trans.steps.salesforceinput.SalesforceInputField;
import org.pentaho.di.trans.steps.salesforceinput.SalesforceInputMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import com.sforce.soap.partner.Field;

public class SalesforceInputDialog extends BaseStepDialog implements StepDialogInterface {
	
	private static Class<?> PKG = SalesforceInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String DEFAULT_DATE_FORMAT="yyyy-MM-dd";
	
	private CTabFolder wTabFolder;
	
	private CTabItem wFileTab, wContentTab, wFieldsTab;

	private Composite wFileComp, wContentComp, wFieldsComp;

	private FormData fdTabFolder,fdFileComp, fdContentComp, fdFieldsComp,fdlInclURLField;
	
	private FormData fdInclURLField,fdlInclModuleField, fdlInclRownumField,  fdlModule, fdModule;
	
	private FormData fdInclModuleField,fdlInclModule,fdlInclURL, fdInclURL,fdlLimit, fdLimit;
	
	private FormData fdlTimeOut,fdTimeOut,fdFields,fdUserName,fdURL,fdPassword,fdCondition;
	
	private FormData fdlCondition,fdlInclRownum,fdRownum,fdInclRownumField;

	private Button wInclURL,wInclModule,wInclRownum;
	
	private FormData fdInclSQLField;
	
	private FormData fdInclTimestampField;

	private Label wlInclURL,wlInclURLField,wlInclModule,wlInclRownum,wlInclRownumField;
	
	private Label wlInclModuleField,wlLimit,wlTimeOut,wlCondition,wlModule,wlInclSQLField,wlInclSQL;
	
	private Group wConnectionGroup, wSettingsGroup;
	
	private Label wlInclTimestampField,wlInclTimestamp;
	
	private FormData fdlInclSQL,fdInclSQL,fdlInclSQLField;
	
	private FormData fdlInclTimestamp,fdInclTimestamp,fdlInclTimestampField;

	private Button wInclSQL;
	
	private TextVar wInclURLField,wInclModuleField,wInclRownumField,wInclSQLField;
	
	private Button wInclTimestamp;
	
	private TextVar wInclTimestampField;

	private TableView wFields;

	private SalesforceInputMeta input;

    private LabelTextVar wUserName,wURL,wPassword;
    
    private StyledTextComp  wCondition;
    
    private Button wspecifyQuery;
    private FormData fdspecifyQuery;
    private Label wlspecifyQuery;
    private FormData fdlspecifyQuery;
    
	private StyledTextComp         wQuery;
	private FormData    fdQuery;
	private Label wlQuery;
	private FormData fdlQuery;
    
    private TextVar wTimeOut,wLimit;

    private ComboVar  wModule;

	private Group wAdditionalFields, wAdvancedGroup;
	
	private FormData fdAdditionalFields, fdAdvancedGroup;
	
	private Label 		wlRecordsFilter;
	private CCombo 		wRecordsFilter;
	private FormData    fdlRecordsFilter;
	private FormData    fdRecordsFilter;
	
	private Label        wlReadFrom;
	private TextVar      wReadFrom;
	private FormData     fdlReadFrom, fdReadFrom;
	private Button		open;
	
	private Label        wlReadTo;
	private TextVar      wReadTo;
	private FormData     fdlReadTo, fdReadTo;
	private Button		opento;
	
	private Button wTest;
	
	private FormData fdTest;
    private Listener lsTest;
  
    
	public SalesforceInputDialog(Shell parent, Object in, TransMeta transMeta,
			String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		input = (SalesforceInputMeta) in;
	}

	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
		props.setLook(shell);
		setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.DialogTitle"));

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
		wFileTab = new CTabItem(wTabFolder, SWT.NONE);
		wFileTab.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.File.Tab"));

		wFileComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wFileComp);

		FormLayout fileLayout = new FormLayout();
		fileLayout.marginWidth = 3;
		fileLayout.marginHeight = 3;
		wFileComp.setLayout(fileLayout);
		
		
        //////////////////////////
        // START CONNECTION GROUP

        wConnectionGroup = new Group(wFileComp, SWT.SHADOW_ETCHED_IN);
        wConnectionGroup.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.ConnectionGroup.Label")); //$NON-NLS-1$;
        FormLayout fconnLayout = new FormLayout();
        fconnLayout .marginWidth = 3;
        fconnLayout .marginHeight = 3;
        wConnectionGroup.setLayout(fconnLayout );
        props.setLook(wConnectionGroup);
		
	      // Webservice URL
        wURL = new LabelTextVar(transMeta,wConnectionGroup, BaseMessages.getString(PKG, "SalesforceInputDialog.URL.Label"), 
        		BaseMessages.getString(PKG, "SalesforceInputDialog.URL.Tooltip"));
        props.setLook(wURL);
        wURL.addModifyListener(lsMod);
        fdURL = new FormData();
        fdURL.left = new FormAttachment(0, 0);
        fdURL.top = new FormAttachment(0, margin);
        fdURL.right = new FormAttachment(100, 0);
        wURL.setLayoutData(fdURL);
        

	      // UserName line
        wUserName = new LabelTextVar(transMeta,wConnectionGroup, BaseMessages.getString(PKG, "SalesforceInputDialog.User.Label"), 
        		BaseMessages.getString(PKG, "SalesforceInputDialog.User.Tooltip"));
        props.setLook(wUserName);
        wUserName.addModifyListener(lsMod);
        fdUserName = new FormData();
        fdUserName.left = new FormAttachment(0, 0);
        fdUserName.top = new FormAttachment(wURL, margin);
        fdUserName.right = new FormAttachment(100, 0);
        wUserName.setLayoutData(fdUserName);
		
        // Password line
        wPassword = new LabelTextVar(transMeta,wConnectionGroup, BaseMessages.getString(PKG, "SalesforceInputDialog.Password.Label"), 
        		BaseMessages.getString(PKG, "SalesforceInputDialog.Password.Tooltip"));
        props.setLook(wPassword);
        //wPassword.setEchoChar('*');
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
		wTest.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.TestConnection.Label"));
 		props.setLook(wTest);
		fdTest=new FormData();
		wTest.setToolTipText(BaseMessages.getString(PKG, "SalesforceInputDialog.TestConnection.Tooltip"));
		//fdTest.left = new FormAttachment(middle, 0);
		fdTest.top  = new FormAttachment(wPassword, margin);
		fdTest.right= new FormAttachment(100, 0);
		wTest.setLayoutData(fdTest);
		
        FormData fdConnectionGroup= new FormData();
        fdConnectionGroup.left = new FormAttachment(0, 0);
        fdConnectionGroup.right = new FormAttachment(100, 0);
        fdConnectionGroup.top = new FormAttachment(0, margin);
        wConnectionGroup.setLayoutData(fdConnectionGroup);

        // END CONNECTION  GROUP
        //////////////////////////
		
        //////////////////////////
        // START SETTINGS GROUP

        wSettingsGroup = new Group(wFileComp, SWT.SHADOW_ETCHED_IN);
        wSettingsGroup.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.HttpAuthGroup.Label")); //$NON-NLS-1$;
        FormLayout fsettingsLayout = new FormLayout();
        fsettingsLayout .marginWidth = 3;
        fsettingsLayout .marginHeight = 3;
        wSettingsGroup.setLayout(fsettingsLayout );
        props.setLook(wSettingsGroup);
        
        wlspecifyQuery = new Label(wSettingsGroup, SWT.RIGHT);
		wlspecifyQuery.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.specifyQuery.Label"));
		props.setLook(wlspecifyQuery);
		fdlspecifyQuery = new FormData();
		fdlspecifyQuery.left = new FormAttachment(0, 0);
		fdlspecifyQuery.top = new FormAttachment(wConnectionGroup, 2*margin);
		fdlspecifyQuery.right = new FormAttachment(middle, -margin);
		wlspecifyQuery.setLayoutData(fdlspecifyQuery);
		wspecifyQuery = new Button(wSettingsGroup, SWT.CHECK);
		props.setLook(wspecifyQuery);
		wspecifyQuery.setToolTipText(BaseMessages.getString(PKG, "SalesforceInputDialog.specifyQuery.Tooltip"));
		fdspecifyQuery = new FormData();
		fdspecifyQuery.left = new FormAttachment(middle, 0);
		fdspecifyQuery.top = new FormAttachment(wConnectionGroup, 2*margin);
		wspecifyQuery.setLayoutData(fdspecifyQuery);
		wspecifyQuery.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				setEnableQuery();
			}
		}
	);
        
 		// Module
		wlModule=new Label(wSettingsGroup, SWT.RIGHT);
        wlModule.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.Module.Label"));
        props.setLook(wlModule);
        fdlModule=new FormData();
        fdlModule.left = new FormAttachment(0, 0);
        fdlModule.top  = new FormAttachment(wspecifyQuery, margin);
        fdlModule.right= new FormAttachment(middle, -margin);
        wlModule.setLayoutData(fdlModule);
        wModule=new ComboVar(transMeta,wSettingsGroup, SWT.BORDER | SWT.READ_ONLY);
        wModule.setEditable(true);
        props.setLook(wModule);
        wModule.addModifyListener(lsMod);
        fdModule=new FormData();
        fdModule.left = new FormAttachment(middle, margin);
        fdModule.top  = new FormAttachment(wspecifyQuery, margin);
        fdModule.right= new FormAttachment(100, -margin);
        wModule.setLayoutData(fdModule);
        wModule.setItems(SalesforceConnectionUtils.modulesList);
       
	    // condition
        wlCondition = new Label(wSettingsGroup, SWT.RIGHT);
        wlCondition.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.Condition.Label"));
        props.setLook(wlCondition);
        fdlCondition = new FormData();
        fdlCondition.left = new FormAttachment(0, -margin);
        fdlCondition.top = new FormAttachment(wModule, margin);
        fdlCondition.right = new FormAttachment(middle, -margin);
        wlCondition.setLayoutData(fdlCondition);

        wCondition=new StyledTextComp(wSettingsGroup, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");
        wCondition.setToolTipText(BaseMessages.getString(PKG, "SalesforceInputDialog.Condition.Tooltip"));
        props.setLook(wCondition, Props.WIDGET_STYLE_FIXED);
        wCondition.addModifyListener(lsMod);
        fdCondition = new FormData();
        fdCondition.left = new FormAttachment(middle, margin);
        fdCondition.top = new FormAttachment(wModule, margin);
        fdCondition.right = new FormAttachment(100, -margin);
        fdCondition.bottom = new FormAttachment(100, -margin);
        wCondition.setLayoutData(fdCondition);


	    // Query
        wlQuery = new Label(wSettingsGroup, SWT.RIGHT);
        wlQuery.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.Query.Label"));
        props.setLook(wlQuery);
        fdlQuery = new FormData();
        fdlQuery.left = new FormAttachment(0, -margin);
        fdlQuery.top = new FormAttachment(wspecifyQuery, margin);
        fdlQuery.right = new FormAttachment(middle, -margin);
        wlQuery.setLayoutData(fdlQuery);
        
		wQuery=new StyledTextComp(wSettingsGroup, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");
 		props.setLook(wQuery, Props.WIDGET_STYLE_FIXED);
		wQuery.addModifyListener(lsMod);
		fdQuery=new FormData();
		fdQuery.left  = new FormAttachment(middle, margin);
		fdQuery.top   = new FormAttachment(wspecifyQuery, margin );
		fdQuery.right = new FormAttachment(100, -margin);
		fdQuery.bottom= new FormAttachment(100, -margin );
		wQuery.setLayoutData(fdQuery);
		wQuery.addModifyListener(new ModifyListener()
            {
                public void modifyText(ModifyEvent arg0)
                {
                    setQueryToolTip();
                }
            }
        );
		
		
		
        FormData fdSettingsGroup= new FormData();
        fdSettingsGroup.left = new FormAttachment(0, 0);
        fdSettingsGroup.right = new FormAttachment(100, 0);
        fdSettingsGroup.bottom = new FormAttachment(100, 0);
        fdSettingsGroup.top = new FormAttachment(wConnectionGroup, margin);
        wSettingsGroup.setLayoutData(fdSettingsGroup);

        // END SETTINGS GROUP
        //////////////////////////
        
        
		fdFileComp = new FormData();
		fdFileComp.left = new FormAttachment(0, 0);
		fdFileComp.top = new FormAttachment(0, 0);
		fdFileComp.right = new FormAttachment(100, 0);
		fdFileComp.bottom = new FormAttachment(100, 0);
		wFileComp.setLayoutData(fdFileComp);

		wFileComp.layout();
		wFileTab.setControl(wFileComp);

		// ///////////////////////////////////////////////////////////
		// / END OF FILE TAB
		// ///////////////////////////////////////////////////////////

		// ////////////////////////
		// START OF CONTENT TAB///
		// /
		wContentTab = new CTabItem(wTabFolder, SWT.NONE);
		wContentTab.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.Content.Tab"));

		FormLayout contentLayout = new FormLayout();
		contentLayout.marginWidth = 3;
		contentLayout.marginHeight = 3;

		wContentComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wContentComp);
		wContentComp.setLayout(contentLayout);

		
		// ///////////////////////////////
		// START OF Advanced GROUP  //
		///////////////////////////////// 

		wAdvancedGroup= new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wAdvancedGroup);
		wAdvancedGroup.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.AdvancedGroup.Label"));
		
		FormLayout advancedgroupLayout = new FormLayout();
		advancedgroupLayout .marginWidth = 10;
		advancedgroupLayout .marginHeight = 10;
		wAdvancedGroup.setLayout(advancedgroupLayout );
		

		// RecordsFilter
		wlRecordsFilter=new Label(wAdvancedGroup, SWT.RIGHT);
		wlRecordsFilter.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.RecordsFilter.Label")); //$NON-NLS-1$
 		props.setLook(wlRecordsFilter);
		fdlRecordsFilter=new FormData();
		fdlRecordsFilter.left = new FormAttachment(0, 0);
		fdlRecordsFilter.right= new FormAttachment(middle, -margin);
		fdlRecordsFilter.top  = new FormAttachment(0, 2*margin);
		wlRecordsFilter.setLayoutData(fdlRecordsFilter);
		
		wRecordsFilter=new CCombo(wAdvancedGroup, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wRecordsFilter);
 		wRecordsFilter.addModifyListener(lsMod);
		fdRecordsFilter=new FormData();
		fdRecordsFilter.left = new FormAttachment(middle, 0);
		fdRecordsFilter.top  = new FormAttachment(0, 2*margin);
		fdRecordsFilter.right= new FormAttachment(100, -margin);
		wRecordsFilter.setLayoutData(fdRecordsFilter);
		wRecordsFilter.setItems(SalesforceConnectionUtils.recordsFilterDesc);
		wRecordsFilter.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				updateRecordsFilter();
			}
		});

		open = new Button (wAdvancedGroup, SWT.PUSH);
		open.setImage(GUIResource.getInstance().getImageCalendar());
		open.setToolTipText (BaseMessages.getString(PKG, "SalesforceInputDialog.OpenCalendar"));
	    FormData fdlButton=new FormData();
	    fdlButton.top  = new FormAttachment(wRecordsFilter, margin);
	    fdlButton.right= new FormAttachment(100, 0);
	    open.setLayoutData(fdlButton);
	    open.addSelectionListener (new SelectionAdapter () {
			public void widgetSelected (SelectionEvent e) {
				final Shell dialog = new Shell (shell, SWT.DIALOG_TRIM );
				dialog.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.SelectDate"));
				dialog.setImage(GUIResource.getInstance().getImageSpoon());
				dialog.setLayout (new GridLayout (3, false));

				final DateTime calendar = new DateTime (dialog, SWT.CALENDAR);
			    final DateTime time = new DateTime (dialog, SWT.TIME | SWT.TIME);
				new Label (dialog, SWT.NONE);
				new Label (dialog, SWT.NONE);

				Button ok = new Button (dialog, SWT.PUSH);
				ok.setText(BaseMessages.getString(PKG, "System.Button.OK"));
				ok.setLayoutData(new GridData (SWT.FILL, SWT.CENTER, false, false));
				ok.addSelectionListener (new SelectionAdapter () {
					public void widgetSelected (SelectionEvent e) {
						wReadFrom.setText(calendar.getYear()+"-"+
								((calendar.getMonth () + 1)<10 ? "0"+(calendar.getMonth () + 1) : (calendar.getMonth () + 1)) 
										+"-"+(calendar.getDay()<10 ? "0"+calendar.getDay () : calendar.getDay())
										+" "+(time.getHours()<10 ? "0"+time.getHours() : time.getHours())
										+":"+(time.getMinutes()<10 ? "0"+time.getMinutes() : time.getMinutes())
										+":"+(time.getMinutes()<10 ? "0"+time.getMinutes() : time.getMinutes())					
						); 
				          
						dialog.close ();
					}
				});
				dialog.setDefaultButton (ok);
				dialog.pack ();
				dialog.open ();
			}
		});
		
		wlReadFrom=new Label(wAdvancedGroup, SWT.RIGHT);
		wlReadFrom.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.ReadFrom.Label"));
 		props.setLook(wlReadFrom);
		fdlReadFrom=new FormData();
		fdlReadFrom.left = new FormAttachment(0, 0);
		fdlReadFrom.top  = new FormAttachment(wRecordsFilter, margin);
		fdlReadFrom.right= new FormAttachment(middle, -margin);
		wlReadFrom.setLayoutData(fdlReadFrom);
		wReadFrom=new TextVar(transMeta, wAdvancedGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wReadFrom.setToolTipText(BaseMessages.getString(PKG, "SalesforceInputDialog.ReadFrom.Tooltip"));
 		props.setLook(wReadFrom);
		wReadFrom.addModifyListener(lsMod);
		fdReadFrom=new FormData();
		fdReadFrom.left = new FormAttachment(middle, 0);
		fdReadFrom.top  = new FormAttachment(wRecordsFilter, margin);
		fdReadFrom.right= new FormAttachment(open, -margin);
		wReadFrom.setLayoutData(fdReadFrom);

		
		opento = new Button (wAdvancedGroup, SWT.PUSH);
		opento.setImage(GUIResource.getInstance().getImageCalendar());
		opento.setToolTipText (BaseMessages.getString(PKG, "SalesforceInputDialog.OpenCalendar"));
	    FormData fdlButtonto=new FormData();
	    fdlButtonto.top  = new FormAttachment(wReadFrom, 2*margin);
	    fdlButtonto.right= new FormAttachment(100, 0);
	    opento.setLayoutData(fdlButtonto);
	    opento.addSelectionListener (new SelectionAdapter () {
			public void widgetSelected (SelectionEvent e) {
				final Shell dialogto = new Shell (shell, SWT.DIALOG_TRIM );
				dialogto.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.SelectDate"));
				dialogto.setImage(GUIResource.getInstance().getImageSpoon());
				dialogto.setLayout (new GridLayout (3, false));

				final DateTime calendarto = new DateTime (dialogto, SWT.CALENDAR | SWT.BORDER);
			    final DateTime timeto = new DateTime (dialogto, SWT.TIME | SWT.TIME);
				new Label (dialogto, SWT.NONE);
				new Label (dialogto, SWT.NONE);
				Button okto = new Button (dialogto, SWT.PUSH);
				okto.setText(BaseMessages.getString(PKG, "System.Button.OK"));
				okto.setLayoutData(new GridData (SWT.FILL, SWT.CENTER, false, false));
				okto.addSelectionListener (new SelectionAdapter () {
					public void widgetSelected (SelectionEvent e) {
						wReadTo.setText(calendarto.getYear()+"-"+
								((calendarto.getMonth() + 1)<10 ? "0"+(calendarto.getMonth () + 1) : (calendarto.getMonth () + 1)) 
								+"-"+(calendarto.getDay()<10 ? "0"+calendarto.getDay() : calendarto.getDay())
								+" "+(timeto.getHours()<10 ? "0"+timeto.getHours() : timeto.getHours())
								+":"+(timeto.getMinutes()<10 ? "0"+timeto.getMinutes() : timeto.getMinutes())
								+":"+(timeto.getSeconds()<10 ? "0"+timeto.getSeconds() : timeto.getSeconds())
						); 
						dialogto.close ();
					}
				});
				dialogto.setDefaultButton (okto);
				dialogto.pack ();
				dialogto.open ();
			}
		});
		
		wlReadTo=new Label(wAdvancedGroup, SWT.RIGHT);
		wlReadTo.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.ReadTo.Label"));
 		props.setLook(wlReadTo);
		fdlReadTo=new FormData();
		fdlReadTo.left = new FormAttachment(0, 0);
		fdlReadTo.top  = new FormAttachment(wReadFrom, 2*margin);
		fdlReadTo.right= new FormAttachment(middle, -margin);
		wlReadTo.setLayoutData(fdlReadTo);
		wReadTo=new TextVar(transMeta, wAdvancedGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wReadTo.setToolTipText(BaseMessages.getString(PKG, "SalesforceInputDialog.ReadTo.Tooltip"));
 		props.setLook(wReadTo);
		wReadTo.addModifyListener(lsMod);
		fdReadTo=new FormData();
		fdReadTo.left = new FormAttachment(middle, 0);
		fdReadTo.top  = new FormAttachment(wReadFrom, 2*margin);
		fdReadTo.right= new FormAttachment(opento, -margin);
		wReadTo.setLayoutData(fdReadTo);

		
		fdAdvancedGroup= new FormData();
		fdAdvancedGroup.left = new FormAttachment(0, margin);
		fdAdvancedGroup.top = new FormAttachment(0, 2*margin);
		fdAdvancedGroup.right = new FormAttachment(100, -margin);
		wAdvancedGroup.setLayoutData(fdAdvancedGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Advanced GROUP
		// ///////////////////////////////////////////////////////////	
	
		
		// ///////////////////////////////
		// START OF Additional Fields GROUP  //
		///////////////////////////////// 

		wAdditionalFields = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wAdditionalFields);
		wAdditionalFields.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.wAdditionalFields.Label"));
		
		FormLayout AdditionalFieldsgroupLayout = new FormLayout();
		AdditionalFieldsgroupLayout.marginWidth = 10;
		AdditionalFieldsgroupLayout.marginHeight = 10;
		wAdditionalFields.setLayout(AdditionalFieldsgroupLayout);
		
		// Add Salesforce URL in the output stream ?
		wlInclURL = new Label(wAdditionalFields, SWT.RIGHT);
		wlInclURL.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.InclURL.Label"));
		props.setLook(wlInclURL);
		fdlInclURL = new FormData();
		fdlInclURL.left = new FormAttachment(0, 0);
		fdlInclURL.top = new FormAttachment(wAdvancedGroup, margin);
		fdlInclURL.right = new FormAttachment(middle, -margin);
		wlInclURL.setLayoutData(fdlInclURL);
		wInclURL = new Button(wAdditionalFields, SWT.CHECK);
		props.setLook(wInclURL);
		wInclURL.setToolTipText(BaseMessages.getString(PKG, "SalesforceInputDialog.InclURL.Tooltip"));
		fdInclURL = new FormData();
		fdInclURL.left = new FormAttachment(middle, 0);
		fdInclURL.top = new FormAttachment(wAdvancedGroup, margin);
		wInclURL.setLayoutData(fdInclURL);
		wInclURL.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				setEnableInclTargetURL();
			}
		}
	);

		wlInclURLField = new Label(wAdditionalFields, SWT.LEFT);
		wlInclURLField.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.InclURLField.Label"));
		props.setLook(wlInclURLField);
		fdlInclURLField = new FormData();
		fdlInclURLField.left = new FormAttachment(wInclURL, margin);
		fdlInclURLField.top = new FormAttachment(wAdvancedGroup, margin);
		wlInclURLField.setLayoutData(fdlInclURLField);
		wInclURLField = new TextVar(transMeta,wAdditionalFields, SWT.SINGLE | SWT.LEFT	| SWT.BORDER);
		props.setLook(wlInclURLField);
		wInclURLField.addModifyListener(lsMod);
		fdInclURLField = new FormData();
		fdInclURLField.left = new FormAttachment(wlInclURLField,margin);
		fdInclURLField.top = new FormAttachment(wAdvancedGroup,  margin);
		fdInclURLField.right = new FormAttachment(100, 0);
		wInclURLField.setLayoutData(fdInclURLField);
		
		
		//	Add module in the output stream ?
		wlInclModule = new Label(wAdditionalFields, SWT.RIGHT);
		wlInclModule.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.InclModule.Label"));
		props.setLook(wlInclModule);
		fdlInclModule = new FormData();
		fdlInclModule.left = new FormAttachment(0, 0);
		fdlInclModule.top = new FormAttachment(wInclURLField, margin);
		fdlInclModule.right = new FormAttachment(middle, -margin);
		wlInclModule.setLayoutData(fdlInclModule);
		wInclModule = new Button(wAdditionalFields, SWT.CHECK);
		props.setLook(wInclModule);
		wInclModule.setToolTipText(BaseMessages.getString(PKG, "SalesforceInputDialog.InclModule.Tooltip"));
		fdModule = new FormData();
		fdModule.left = new FormAttachment(middle, 0);
		fdModule.top = new FormAttachment(wInclURLField, margin);
		wInclModule.setLayoutData(fdModule);

		wInclModule.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				setEnableInclModule();
			}
		}
	);
		
		wlInclModuleField = new Label(wAdditionalFields, SWT.RIGHT);
		wlInclModuleField.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.InclModuleField.Label"));
		props.setLook(wlInclModuleField);
		fdlInclModuleField = new FormData();
		fdlInclModuleField.left = new FormAttachment(wInclModule, margin);
		fdlInclModuleField.top = new FormAttachment(wInclURLField, margin);
		wlInclModuleField.setLayoutData(fdlInclModuleField);
		wInclModuleField = new TextVar(transMeta,wAdditionalFields, SWT.SINGLE | SWT.LEFT
				| SWT.BORDER);
		props.setLook(wInclModuleField);
		wInclModuleField.addModifyListener(lsMod);
		fdInclModuleField = new FormData();
		fdInclModuleField.left = new FormAttachment(wlInclModuleField, margin);
		fdInclModuleField.top = new FormAttachment(wInclURLField, margin);
		fdInclModuleField.right = new FormAttachment(100, 0);
		wInclModuleField.setLayoutData(fdInclModuleField);


		// Add SQL in the output stream ?
		wlInclSQL = new Label(wAdditionalFields, SWT.RIGHT);
		wlInclSQL.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.InclSQL.Label"));
		props.setLook(wlInclSQL);
		fdlInclSQL = new FormData();
		fdlInclSQL.left = new FormAttachment(0, 0);
		fdlInclSQL.top = new FormAttachment(wInclModuleField, margin);
		fdlInclSQL.right = new FormAttachment(middle, -margin);
		wlInclSQL.setLayoutData(fdlInclSQL);
		wInclSQL = new Button(wAdditionalFields, SWT.CHECK);
		props.setLook(wInclSQL);
		wInclSQL.setToolTipText(BaseMessages.getString(PKG, "SalesforceInputDialog.InclSQL.Tooltip"));
		fdInclSQL = new FormData();
		fdInclSQL.left = new FormAttachment(middle, 0);
		fdInclSQL.top = new FormAttachment(wInclModuleField, margin);
		wInclSQL.setLayoutData(fdInclSQL);
		wInclSQL.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				setEnableInclSQL();
			}
		}
	);

		wlInclSQLField = new Label(wAdditionalFields, SWT.LEFT);
		wlInclSQLField.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.InclSQLField.Label"));
		props.setLook(wlInclSQLField);
		fdlInclSQLField = new FormData();
		fdlInclSQLField.left = new FormAttachment(wInclSQL, margin);
		fdlInclSQLField.top = new FormAttachment(wInclModuleField, margin);
		wlInclSQLField.setLayoutData(fdlInclSQLField);
		wInclSQLField = new TextVar(transMeta,wAdditionalFields, SWT.SINGLE | SWT.LEFT	| SWT.BORDER);
		props.setLook(wlInclSQLField);
		wInclSQLField.addModifyListener(lsMod);
		fdInclSQLField = new FormData();
		fdInclSQLField.left = new FormAttachment(wlInclSQLField,margin);
		fdInclSQLField.top = new FormAttachment(wInclModuleField,  margin);
		fdInclSQLField.right = new FormAttachment(100, 0);
		wInclSQLField.setLayoutData(fdInclSQLField);
		
		// Add Timestamp in the output stream ?
		wlInclTimestamp = new Label(wAdditionalFields, SWT.RIGHT);
		wlInclTimestamp.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.InclTimestamp.Label"));
		props.setLook(wlInclTimestamp);
		fdlInclTimestamp = new FormData();
		fdlInclTimestamp.left = new FormAttachment(0, 0);
		fdlInclTimestamp.top = new FormAttachment(wInclSQLField, margin);
		fdlInclTimestamp.right = new FormAttachment(middle, -margin);
		wlInclTimestamp.setLayoutData(fdlInclTimestamp);
		wInclTimestamp = new Button(wAdditionalFields, SWT.CHECK);
		props.setLook(wInclTimestamp);
		wInclTimestamp.setToolTipText(BaseMessages.getString(PKG, "SalesforceInputDialog.InclTimestamp.Tooltip"));
		fdInclTimestamp = new FormData();
		fdInclTimestamp.left = new FormAttachment(middle, 0);
		fdInclTimestamp.top = new FormAttachment(wInclSQLField, margin);
		wInclTimestamp.setLayoutData(fdInclTimestamp);
		wInclTimestamp.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				setEnableInclTimestamp();
			}
		}
	);

		wlInclTimestampField = new Label(wAdditionalFields, SWT.LEFT);
		wlInclTimestampField.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.InclTimestampField.Label"));
		props.setLook(wlInclTimestampField);
		fdlInclTimestampField = new FormData();
		fdlInclTimestampField.left = new FormAttachment(wInclTimestamp, margin);
		fdlInclTimestampField.top = new FormAttachment(wInclSQLField, margin);
		wlInclTimestampField.setLayoutData(fdlInclTimestampField);
		wInclTimestampField = new TextVar(transMeta,wAdditionalFields, SWT.SINGLE | SWT.LEFT	| SWT.BORDER);
		props.setLook(wlInclTimestampField);
		wInclTimestampField.addModifyListener(lsMod);
		fdInclTimestampField = new FormData();
		fdInclTimestampField.left = new FormAttachment(wlInclTimestampField,margin);
		fdInclTimestampField.top = new FormAttachment(wInclSQLField,  margin);
		fdInclTimestampField.right = new FormAttachment(100, 0);
		wInclTimestampField.setLayoutData(fdInclTimestampField);
		
		
		// Include Rownum in output stream?
		wlInclRownum=new Label(wAdditionalFields, SWT.RIGHT);
		wlInclRownum.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.InclRownum.Label"));
 		props.setLook(wlInclRownum);
		fdlInclRownum=new FormData();
		fdlInclRownum.left = new FormAttachment(0, 0);
		fdlInclRownum.top  = new FormAttachment(wInclTimestampField, margin);
		fdlInclRownum.right= new FormAttachment(middle, -margin);
		wlInclRownum.setLayoutData(fdlInclRownum);
		wInclRownum=new Button(wAdditionalFields, SWT.CHECK );
 		props.setLook(wInclRownum);
		wInclRownum.setToolTipText(BaseMessages.getString(PKG, "SalesforceInputDialog.InclRownum.Tooltip"));
		fdRownum=new FormData();
		fdRownum.left = new FormAttachment(middle, 0);
		fdRownum.top  = new FormAttachment(wInclTimestampField, margin);
		wInclRownum.setLayoutData(fdRownum);

		wInclRownum.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				setEnableInclRownum();
			}
		}
	);
		
		wlInclRownumField=new Label(wAdditionalFields, SWT.RIGHT);
		wlInclRownumField.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.InclRownumField.Label"));
 		props.setLook(wlInclRownumField);
		fdlInclRownumField=new FormData();
		fdlInclRownumField.left = new FormAttachment(wInclRownum, margin);
		fdlInclRownumField.top  = new FormAttachment(wInclTimestampField, margin);
		wlInclRownumField.setLayoutData(fdlInclRownumField);
		wInclRownumField=new TextVar(transMeta,wAdditionalFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInclRownumField);
		wInclRownumField.addModifyListener(lsMod);
		fdInclRownumField=new FormData();
		fdInclRownumField.left = new FormAttachment(wlInclRownumField, margin);
		fdInclRownumField.top  = new FormAttachment(wInclTimestampField, margin);
		fdInclRownumField.right= new FormAttachment(100, 0);
		wInclRownumField.setLayoutData(fdInclRownumField);

		
		fdAdditionalFields = new FormData();
		fdAdditionalFields.left = new FormAttachment(0, margin);
		fdAdditionalFields.top = new FormAttachment(wAdvancedGroup, margin);
		fdAdditionalFields.right = new FormAttachment(100, -margin);
		wAdditionalFields.setLayoutData(fdAdditionalFields);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Additional Fields GROUP
		// ///////////////////////////////////////////////////////////	
	
		// Timeout
		wlTimeOut = new Label(wContentComp, SWT.RIGHT);
		wlTimeOut.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.TimeOut.Label"));
		props.setLook(wlTimeOut);
		fdlTimeOut = new FormData();
		fdlTimeOut.left = new FormAttachment(0, 0);
		fdlTimeOut.top = new FormAttachment(wAdditionalFields, 2*margin);
		fdlTimeOut.right = new FormAttachment(middle, -margin);
		wlTimeOut.setLayoutData(fdlTimeOut);
		wTimeOut = new TextVar(transMeta,wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wTimeOut);
		wTimeOut.addModifyListener(lsMod);
		fdTimeOut = new FormData();
		fdTimeOut.left = new FormAttachment(middle, 0);
		fdTimeOut.top = new FormAttachment(wAdditionalFields, 2*margin);
		fdTimeOut.right = new FormAttachment(100, 0);
		wTimeOut.setLayoutData(fdTimeOut);
		
		// Limit rows
		wlLimit = new Label(wContentComp, SWT.RIGHT);
		wlLimit.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.Limit.Label"));
		props.setLook(wlLimit);
		fdlLimit = new FormData();
		fdlLimit.left = new FormAttachment(0, 0);
		fdlLimit.top = new FormAttachment(wTimeOut, margin);
		fdlLimit.right = new FormAttachment(middle, -margin);
		wlLimit.setLayoutData(fdlLimit);
		wLimit = new TextVar(transMeta,wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit = new FormData();
		fdLimit.left = new FormAttachment(middle, 0);
		fdLimit.top = new FormAttachment(wTimeOut, margin);
		fdLimit.right = new FormAttachment(100, 0);
		wLimit.setLayoutData(fdLimit);

		fdContentComp = new FormData();
		fdContentComp.left = new FormAttachment(0, 0);
		fdContentComp.top = new FormAttachment(0, 0);
		fdContentComp.right = new FormAttachment(100, 0);
		fdContentComp.bottom = new FormAttachment(100, 0);
		wContentComp.setLayoutData(fdContentComp);

		wContentComp.layout();
		wContentTab.setControl(wContentComp);

		// ///////////////////////////////////////////////////////////
		// / END OF CONTENT TAB
		// ///////////////////////////////////////////////////////////

		// Fields tab...
		//
		wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
		wFieldsTab.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.Fields.Tab"));

		FormLayout fieldsLayout = new FormLayout();
		fieldsLayout.marginWidth = Const.FORM_MARGIN;
		fieldsLayout.marginHeight = Const.FORM_MARGIN;

		wFieldsComp = new Composite(wTabFolder, SWT.NONE);
		wFieldsComp.setLayout(fieldsLayout);
		props.setLook(wFieldsComp);

		wGet = new Button(wFieldsComp, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.GetFields.Button"));
		fdGet = new FormData();
		fdGet.left = new FormAttachment(50, 0);
		fdGet.bottom = new FormAttachment(100, 0);
		wGet.setLayoutData(fdGet);


		final int FieldsRows = input.getInputFields().length;

		ColumnInfo[] colinf = new ColumnInfo[] {
				new ColumnInfo(BaseMessages.getString(PKG, "SalesforceInputDialog.FieldsTable.Name.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false),
				new ColumnInfo(
						BaseMessages.getString(PKG, "SalesforceInputDialog.FieldsTable.Field.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false),
				new ColumnInfo(BaseMessages.getString(PKG, "SalesforceInputDialog.FieldsTable.Type.Column"),
						ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes(), true),
				new ColumnInfo(
						BaseMessages.getString(PKG, "SalesforceInputDialog.FieldsTable.Format.Column"),
						ColumnInfo.COLUMN_TYPE_FORMAT, 3),
				new ColumnInfo(
						BaseMessages.getString(PKG, "SalesforceInputDialog.FieldsTable.Length.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false),
				new ColumnInfo(
						BaseMessages.getString(PKG, "SalesforceInputDialog.FieldsTable.Precision.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false),
				new ColumnInfo(
						BaseMessages.getString(PKG, "SalesforceInputDialog.FieldsTable.Currency.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false),
				new ColumnInfo(
						BaseMessages.getString(PKG, "SalesforceInputDialog.FieldsTable.Decimal.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false),
				new ColumnInfo(BaseMessages.getString(PKG, "SalesforceInputDialog.FieldsTable.Group.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false),
				new ColumnInfo(
						BaseMessages.getString(PKG, "SalesforceInputDialog.FieldsTable.TrimType.Column"),
						ColumnInfo.COLUMN_TYPE_CCOMBO,
						SalesforceInputField.trimTypeDesc, true),
				new ColumnInfo(
						BaseMessages.getString(PKG, "SalesforceInputDialog.FieldsTable.Repeat.Column"),
						ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {
								BaseMessages.getString(PKG, "System.Combo.Yes"),
								BaseMessages.getString(PKG, "System.Combo.No") }, true),

		};

		colinf[0].setUsingVariables(true);
		colinf[0].setToolTip(BaseMessages.getString(PKG, "SalesforceInputDialog.FieldsTable.Name.Column.Tooltip"));
		colinf[1].setUsingVariables(true);
		colinf[1].setToolTip(BaseMessages.getString(PKG, "SalesforceInputDialog.FieldsTable.Field.Column.Tooltip"));

		wFields=new TableView(transMeta,wFieldsComp, 
			      SWT.FULL_SELECTION | SWT.MULTI, 
			      colinf, 
			      FieldsRows,  
			      lsMod,
				  props
			      );
		
		fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(0, 0);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(wGet, -margin);
		wFields.setLayoutData(fdFields);

		fdFieldsComp = new FormData();
		fdFieldsComp.left = new FormAttachment(0, 0);
		fdFieldsComp.top = new FormAttachment(0, 0);
		fdFieldsComp.right = new FormAttachment(100, 0);
		fdFieldsComp.bottom = new FormAttachment(100, 0);
		wFieldsComp.setLayoutData(fdFieldsComp);

		wFieldsComp.layout();
		wFieldsTab.setControl(wFieldsComp);

		fdTabFolder = new FormData();
		fdTabFolder.left = new FormAttachment(0, 0);
		fdTabFolder.top = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom = new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);
		
		

		// THE BUTTONS
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));

		wPreview = new Button(shell, SWT.PUSH);
		wPreview.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.Button.PreviewRows"));

		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin, wTabFolder);

		// Add listeners
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};
		lsTest     = new Listener() { public void handleEvent(Event e) { test(); } };
		lsGet = new Listener() {
			public void handleEvent(Event e) {
		        Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
		        shell.setCursor(busy);
				get();
		        shell.setCursor(null);
		        busy.dispose();
			}
		};
		lsPreview = new Listener() {
			public void handleEvent(Event e) {
				preview();
			}
		};
		lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};

		wOK.addListener(SWT.Selection, lsOK);
		wGet.addListener(SWT.Selection, lsGet);
		wTest.addListener    (SWT.Selection, lsTest    );	
		wPreview.addListener(SWT.Selection, lsPreview);
		wCancel.addListener(SWT.Selection, lsCancel);

		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};

		wStepname.addSelectionListener(lsDef);
		wLimit.addSelectionListener(lsDef);
		wInclModuleField.addSelectionListener(lsDef);
		wInclURLField.addSelectionListener(lsDef);


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
		setEnableInclTargetURL();
		setEnableInclSQL();
		setEnableInclTimestamp();
		setEnableInclModule();
		setEnableInclRownum();
		setEnableQuery();
	
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
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
	  
 private void setEnableInclTargetURL()
 {
	wInclURLField.setEnabled(wInclURL.getSelection());
	wlInclURLField.setEnabled(wInclURL.getSelection());
 }
 private void setEnableQuery()
 {
	wQuery.setVisible(wspecifyQuery.getSelection());
	wlCondition.setVisible(!wspecifyQuery.getSelection());
	wCondition.setVisible(!wspecifyQuery.getSelection());
	wlModule.setVisible(!wspecifyQuery.getSelection());
	wModule.setVisible(!wspecifyQuery.getSelection());
	
	if(wspecifyQuery.getSelection()){
		if(wInclModule.getSelection()) wInclModule.setSelection(false);
		wRecordsFilter.setText(SalesforceConnectionUtils.getRecordsFilterDesc(SalesforceConnectionUtils.RECORDS_FILTER_ALL));
	}
	wlInclModule.setEnabled(!wspecifyQuery.getSelection());
	wInclModule.setEnabled(!wspecifyQuery.getSelection());
	setEnableInclModule();
	wlRecordsFilter.setEnabled(!wspecifyQuery.getSelection());
	wRecordsFilter.setEnabled(!wspecifyQuery.getSelection());
	updateRecordsFilter();
 }
 private void setEnableInclSQL()
 {
	wInclSQLField.setEnabled(wInclSQL.getSelection());
	wlInclSQLField.setEnabled(wInclSQL.getSelection());
 }
 private void setEnableInclTimestamp()
 {
	wInclTimestampField.setEnabled(wInclTimestamp.getSelection());
	wlInclTimestampField.setEnabled(wInclTimestamp.getSelection());
 }
 
 private void setEnableInclModule()
 {
	wInclModuleField.setEnabled(wInclModule.getSelection() && !wspecifyQuery.getSelection());
	wlInclModuleField.setEnabled(wInclModule.getSelection() && !wspecifyQuery.getSelection());
 }
 private void setEnableInclRownum()
 {
	wInclRownumField.setEnabled(wInclRownum.getSelection());
	wlInclRownumField.setEnabled(wInclRownum.getSelection());
 }
 private void test(){
	 
	 boolean successConnection=true;
	 String msgError=null;
	 SalesforceConnection connection=null;
	 try
     {
			SalesforceInputMeta meta = new SalesforceInputMeta();
			getInfo(meta);
			
			// get real values
			String realURL=transMeta.environmentSubstitute(meta.getTargetURL());
			String realUsername=transMeta.environmentSubstitute(meta.getUserName());
			String realPassword=transMeta.environmentSubstitute(meta.getPassword());
			int realTimeOut=Const.toInt(transMeta.environmentSubstitute(meta.getTimeOut()),0);

			connection=new SalesforceConnection(log, realURL,realUsername,realPassword,realTimeOut); 
			connection.connect();
			
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
			mb.setMessage(BaseMessages.getString(PKG, "SalesforceInputDialog.Connected.OK",wUserName.getText()) +Const.CR);
			mb.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.Connected.Title.Ok")); 
			mb.open();
		}else{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "SalesforceInputDialog.Connected.NOK",wUserName.getText(),msgError));
			mb.setText(BaseMessages.getString(PKG, "SalesforceInputDialog.Connected.Title.Error")); 
			mb.open(); 
		}
	}


  protected void setQueryToolTip()
  {
     wQuery.setToolTipText(transMeta.environmentSubstitute(wQuery.getText()));
  }
 private void get() 
 { 
	 SalesforceConnection connection=null;
	try {
		
		SalesforceInputMeta meta = new SalesforceInputMeta();
		getInfo(meta);
		
		// Clear Fields Grid
		wFields.removeAll();
		
	    // get real values
	    String realModule=transMeta.environmentSubstitute(meta.getModule());
		String realURL=transMeta.environmentSubstitute(meta.getTargetURL());
		String realUsername=transMeta.environmentSubstitute(meta.getUserName());
		String realPassword=transMeta.environmentSubstitute(meta.getPassword());
		int realTimeOut=Const.toInt(transMeta.environmentSubstitute(meta.getTimeOut()),0);

		connection=new SalesforceConnection(log, realURL,realUsername,realPassword,realTimeOut); 
		if(meta.isSpecifyQuery())   {
			// Free hand SOQL
			String realQuery=transMeta.environmentSubstitute(meta.getQuery());
			connection.setSQL(realQuery);
			connection.connect();
		    // We are connected, do let's query
			MessageElement[] fields =  connection.getElements();
			int nrFields=fields.length;
			
			for(int i=0; i<nrFields;i++) {
				// get fieldname
				String fieldname=fields[i].getName();
				// return first value
				String firstValue=fields[i].getValue();

	            TableItem item = new TableItem(wFields.table,SWT.NONE);
				item.setText(1, fieldname);
				item.setText(2, fieldname);
			
	            // Try to guess field type
				// I know it's not clean (see up)
	            if(StringUtil.IsDate(firstValue,"yyyy-MM-dd")){
	            	item.setText(3, ValueMeta.getTypeDesc(ValueMeta.TYPE_DATE));
	            	item.setText(4, "yyyy-MM-dd");		    			
	            } else if(StringUtil.IsInteger(firstValue)) {
	            	item.setText(3, ValueMeta.getTypeDesc(ValueMeta.TYPE_INTEGER));
	            	item.setText(5, ""+ValueMeta.DEFAULT_INTEGER_LENGTH);
	            } else if(StringUtil.IsNumber(firstValue)){
	            	item.setText(3, ValueMeta.getTypeDesc(ValueMeta.TYPE_NUMBER)); 
	            } else if(firstValue.equals("true")||firstValue.equals("false")){
	            	item.setText(3, ValueMeta.getTypeDesc(ValueMeta.TYPE_BOOLEAN));     
	            }else
	            	item.setText(3, ValueMeta.getTypeDesc(ValueMeta.TYPE_STRING));    
			}
		}else{
			connection.connect();

            Field[] fields = connection.getModuleFields(realModule);
            
            for (int i = 0; i < fields.length; i++)  {
            	String FieldLabel= fields[i].getLabel();	
            	String FieldName= fields[i].getName();	
            	String FieldType=fields[i].getType().getValue();
            	String FieldLengh =  fields[i].getLength() + "";
            	
            	TableItem item = new TableItem(wFields.table,SWT.NONE);
				item.setText(1, FieldLabel);
				item.setText(2, FieldName);
			
				// Try to get the Type
				if (FieldType.equals("boolean")) {
					item.setText(3, "Boolean");
				} else if (FieldType.equals("datetime") || FieldType.equals("date")) {
					item.setText(3, "Date");
					item.setText(4, DEFAULT_DATE_FORMAT);
				} else if (FieldType.equals("double")) {
					item.setText(3, "Number");
                } else if (FieldType.equals("int")) {
					item.setText(3, "Integer");
				}
		        else {
		        item.setText(3, "String");
		        }
				
				// Get length
				if (!FieldType.equals("boolean") && !FieldType.equals("datetime") && !FieldType.equals("date"))
				{
					item.setText(5, FieldLengh);
				}					
	       }
        }
		wFields.removeEmptyRows();
		wFields.setRowNums();
		wFields.optWidth(true);
	} catch (KettleException e) {
		new ErrorDialog(shell,BaseMessages.getString(PKG, "SalesforceInputMeta.ErrorRetrieveData.DialogTitle"),
				BaseMessages.getString(PKG, "SalesforceInputMeta.ErrorRetrieveData.DialogMessage"),	e);
	} catch (Exception e) {
		new ErrorDialog(shell,BaseMessages.getString(PKG, "SalesforceInputMeta.ErrorRetrieveData.DialogTitle"),
				BaseMessages.getString(PKG, "SalesforceInputMeta.ErrorRetrieveData.DialogMessage"),e);
	}finally{
		if(connection!=null) {
			try {connection.close();}catch(Exception e){};
		}
	}
 }
  private void updateRecordsFilter()
  {
	  boolean activeFilter=(!wspecifyQuery.getSelection() && SalesforceConnectionUtils.getRecordsFilterByDesc(wRecordsFilter.getText())!=SalesforceConnectionUtils.RECORDS_FILTER_ALL);

	  wlReadFrom.setEnabled(activeFilter);
	  wReadFrom.setEnabled(activeFilter);
	  open.setEnabled(activeFilter);
	  wlReadTo.setEnabled(activeFilter);
	  wReadTo.setEnabled(activeFilter);
	  opento.setEnabled(activeFilter);
			  
  }

	/**
	 * Read the data from the TextFileInputMeta object and show it in this
	 * dialog.
	 * 
	 * @param in
	 *            The SalesforceInputMeta object to obtain the data from.
	 */
	public void getData(SalesforceInputMeta in) 
	{
		wURL.setText(Const.NVL(in.getTargetURL(),""));
		wUserName.setText(Const.NVL(in.getUserName(),""));
		wPassword.setText(Const.NVL(in.getPassword(),""));
		wModule.setText(Const.NVL(in.getModule(), "Account"));
		wCondition.setText(Const.NVL(in.getCondition(),""));
		
		wspecifyQuery.setSelection(in.isSpecifyQuery());
		wQuery.setText(Const.NVL(in.getQuery(),""));
		wRecordsFilter.setText(SalesforceConnectionUtils.getRecordsFilterDesc(input.getRecordsFilter()));
		wInclURLField.setText(Const.NVL(in.getTargetURLField(),""));
		wInclURL.setSelection(in.includeTargetURL());
		
		wInclSQLField.setText(Const.NVL(in.getSQLField(),""));
		wInclSQL.setSelection(in.includeSQL());
		
		wInclTimestampField.setText(Const.NVL(in.getTimestampField(),""));
		wInclTimestamp.setSelection(in.includeTimestamp());
		
		
		wInclModuleField.setText(Const.NVL(in.getModuleField(),""));
		wInclModule.setSelection(in.includeModule());
		
		wInclRownumField.setText(Const.NVL(in.getRowNumberField(),""));
		wInclRownum.setSelection(in.includeRowNumber());
		
		wTimeOut.setText("" + in.getTimeOut());
			
		wLimit.setText("" + in.getRowLimit());

		wReadFrom.setText(Const.NVL(in.getReadFrom(),""));
		wReadTo.setText(Const.NVL(in.getReadTo(),""));
		
		
		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "SalesforceInputDialog.Log.GettingFieldsInfo"));
		for (int i = 0; i < in.getInputFields().length; i++) 
		{
			SalesforceInputField field = in.getInputFields()[i];

			if (field != null) {
				TableItem item = wFields.table.getItem(i);
				String name = field.getName();
				String xpath = field.getField();
				String type = field.getTypeDesc();
				String format = field.getFormat();
				String length = "" + field.getLength();
				String prec = "" + field.getPrecision();
				String curr = field.getCurrencySymbol();
				String group = field.getGroupSymbol();
				String decim = field.getDecimalSymbol();
				String trim = field.getTrimTypeDesc();
				String rep = field.isRepeated() ? 
						BaseMessages.getString(PKG, "System.Combo.Yes") : 
						BaseMessages.getString(PKG, "System.Combo.No");

				if (name != null)
					item.setText(1, name);
				if (xpath != null)
					item.setText(2, xpath);
				if (type != null)
					item.setText(3, type);
				if (format != null)
					item.setText(4, format);
				if (length != null && !"-1".equals(length))
					item.setText(5, length);
				if (prec != null && !"-1".equals(prec))
					item.setText(6, prec);
				if (curr != null)
					item.setText(7, curr);
				if (decim != null)
					item.setText(8, decim);
				if (group != null)
					item.setText(9, group);
				if (trim != null)
					item.setText(10, trim);
				if (rep != null)
					item.setText(11, rep);
			}
		}

		wFields.removeEmptyRows();
		wFields.setRowNums();
		wFields.optWidth(true);

		wStepname.selectAll();
	}

	private void cancel() {
		stepname = null;
		input.setChanged(changed);
		dispose();
	}

	private void ok() {
		if (Const.isEmpty(wStepname.getText())) return;

		stepname = wStepname.getText();
		try {
			getInfo(input);
		} catch (KettleException e) {
			new ErrorDialog(
					shell,BaseMessages.getString(PKG, "SalesforceInputDialog.ErrorValidateData.DialogTitle"),
					BaseMessages.getString(PKG, "SalesforceInputDialog.ErrorValidateData.DialogMessage"),	e);
		}
		dispose();
	}

	private void getInfo(SalesforceInputMeta in) throws KettleException {
		stepname = wStepname.getText(); // return value

		// copy info to SalesforceInputMeta class (input)
		in.setTargetURL(Const.NVL(wURL.getText(),SalesforceConnectionUtils.TARGET_DEFAULT_URL));
		in.setUserName(Const.NVL(wUserName.getText(),""));
		in.setPassword(Const.NVL(wPassword.getText(),""));
		in.setModule(Const.NVL(wModule.getText(),"Account"));
		in.setCondition(Const.NVL(wCondition.getText(),""));
		
		in.setSpecifyQuery(wspecifyQuery.getSelection());
		in.setQuery(Const.NVL(wQuery.getText(),""));
		
		in.setTimeOut(Const.NVL(wTimeOut.getText(),"0"));
		in.setRowLimit(Const.NVL(wLimit.getText(),"0"));
		in.setTargetURLField(Const.NVL(wInclURLField.getText(),""));
		in.setSQLField(Const.NVL(wInclSQLField.getText(),""));
		in.setTimestampField(Const.NVL(wInclTimestampField.getText(),""));
		in.setModuleField(Const.NVL(wInclModuleField.getText(),""));
		in.setRowNumberField(Const.NVL(wInclRownumField.getText(),""));
		in.setRecordsFilter(SalesforceConnectionUtils.getRecordsFilterByDesc(wRecordsFilter.getText()));
		in.setIncludeTargetURL(wInclURL.getSelection());
		in.setIncludeSQL(wInclSQL.getSelection());
		in.setIncludeTimestamp(wInclTimestamp.getSelection());
		in.setIncludeModule(wInclModule.getSelection());
		in.setIncludeRowNumber(wInclRownum.getSelection());
		in.setReadFrom(wReadFrom.getText());
		in.setReadTo(wReadTo.getText());
		int nrFields = wFields.nrNonEmpty();

		in.allocate(nrFields);

		for (int i = 0; i < nrFields; i++) {
			SalesforceInputField field = new SalesforceInputField();

			TableItem item = wFields.getNonEmpty(i);

			field.setName(item.getText(1));
			field.setField(item.getText(2));
			field.setType(ValueMeta.getType(item.getText(3)));
			field.setFormat(item.getText(4));
			field.setLength(Const.toInt(item.getText(5), -1));
			field.setPrecision(Const.toInt(item.getText(6), -1));
			field.setCurrencySymbol(item.getText(7));
			field.setDecimalSymbol(item.getText(8));
			field.setGroupSymbol(item.getText(9));
			field.setTrimType(SalesforceInputField.getTrimTypeByDesc(item.getText(10)));
			field.setRepeated(BaseMessages.getString(PKG, "System.Combo.Yes").equalsIgnoreCase(item.getText(11)));

			in.getInputFields()[i] = field;
		}
	}

	// Preview the data
	private void preview() {
		try {
			SalesforceInputMeta oneMeta = new SalesforceInputMeta();
			getInfo(oneMeta);

			// check if the path is given
			
			 TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
	            
			EnterNumberDialog numberDialog = new EnterNumberDialog(
					shell,
					500,
					BaseMessages.getString(PKG, "SalesforceInputDialog.NumberRows.DialogTitle"),
					BaseMessages.getString(PKG, "SalesforceInputDialog.NumberRows.DialogMessage"));
			int previewSize = numberDialog.open();
			if (previewSize > 0) {
				TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(
						shell, previewMeta,
						new String[] { wStepname.getText() },
						new int[] { previewSize });
				progressDialog.open();

				if (!progressDialog.isCancelled()) {
					Trans trans = progressDialog.getTrans();
					String loggingText = progressDialog.getLoggingText();

					if (trans.getResult() != null
							&& trans.getResult().getNrErrors() > 0) {
						EnterTextDialog etd = new EnterTextDialog(
								shell,BaseMessages.getString(PKG, "System.Dialog.PreviewError.Title"),
								BaseMessages.getString(PKG, "System.Dialog.PreviewError.Message"),loggingText, true);
						etd.setReadOnly();
						etd.open();
					}

                    PreviewRowsDialog prd = new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(),
							progressDialog.getPreviewRowsMeta(wStepname.getText()), progressDialog
									.getPreviewRows(wStepname.getText()), loggingText);
					prd.open();
				}
			}
		} catch (KettleException e) {
			new ErrorDialog(
					shell,
					BaseMessages.getString(PKG, "SalesforceInputDialog.ErrorPreviewingData.DialogTitle"),
					BaseMessages.getString(PKG, "SalesforceInputDialog.ErrorPreviewingData.DialogMessage"),
					e);
		}
	}
	public String toString() {
		return this.getClass().getName();
	}
}