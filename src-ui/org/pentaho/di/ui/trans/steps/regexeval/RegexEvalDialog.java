/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.trans.steps.regexeval;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.regexeval.RegexEvalMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class RegexEvalDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = RegexEvalMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlScript;
	private StyledTextComp         wScript;
	private FormData     fdlScript, fdScript,fdResultField,fdlfieldevaluate, fdfieldevaluate;
	
	private LabelTextVar wResultField;
	
    private CCombo       wfieldevaluate;

	private Label wlfieldevaluate;
    
	private RegexEvalMeta input;
	
	private Group wStepSettings,wRegexSettings;
	private FormData fdStepSettings,fdRegexSettings;
	
	private Label        wlCanonEq,wlCaseInsensitive, wlComment,wlDotAll,wlMultiline,wlUnicode,wlUnix,wlUseVar,wlAllowCaptureGroups,wlReplaceFields;
	private Button       wCanonEq,wCaseInsensitive, wComment,wDotAll,wMultiline,wUnicode,wUnix,wUseVar,wAllowCaptureGroups,wReplaceFields;
	private FormData     fdlCanonEq, fdCanonEq,fdlCaseInsensitive,fdCaseInsensitive,fdComment,
							fdlComment,fdDotAll,fdlDotAll,fdMultiline,fdlMultiline,fdUnicode,
							fdlUnicode,fdUnix,fdlUnix,fdUseVar,fdlUseVar,fdAllowCaptureGroups,fdlAllowCaptureGroups,fdReplaceFields,fdlReplaceFields;
	
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wGeneralTab,wContentTab;
	private Composite    wGeneralComp,wContentComp, wBottom;
	private FormData     fdGeneralComp,fdContentComp, fdBottom;
	
	private SashForm     wSash;
	private FormData     fdSash;

	private Label        wSeparator;
	private FormData     fdSeparator;

	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;
	
	private Button wbTestRegExScript;
	private FormData fdbTestRegExScript;
	private Listener lsbTestRegExScript;

	public RegexEvalDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(RegexEvalMeta)in;
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
		SelectionListener lsSel = new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "RegexEvalDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "RegexEvalDialog.Stepname.Label")); //$NON-NLS-1$
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

		wSash = new SashForm(shell, SWT.VERTICAL );
 		
		wTabFolder = new CTabFolder(wSash, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
 		
 		//////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////
		
		
		
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(BaseMessages.getString(PKG, "RegexEvalDialog.GeneralTab.TabTitle"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
		
		// Step Settings grouping?
		// ////////////////////////
		// START OF Step Settings GROUP
		// 

		wStepSettings = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wStepSettings);
		wStepSettings.setText(BaseMessages.getString(PKG, "RegexEvalDialog.Group.StepSettings.Label"));
		
		FormLayout groupLayout = new FormLayout();
		groupLayout.marginWidth = 10;
		groupLayout.marginHeight = 10;
		wStepSettings.setLayout(groupLayout);
		
		
		
		// fieldevaluate
		wlfieldevaluate=new Label(wStepSettings, SWT.RIGHT);
        wlfieldevaluate.setText(BaseMessages.getString(PKG, "RegexEvalDialog.Matcher.Label"));
        props.setLook(wlfieldevaluate);
        fdlfieldevaluate=new FormData();
        fdlfieldevaluate.left = new FormAttachment(0, 0);
        fdlfieldevaluate.top  = new FormAttachment(wStepname, margin);
        fdlfieldevaluate.right= new FormAttachment(middle, -margin);
        wlfieldevaluate.setLayoutData(fdlfieldevaluate);
        wfieldevaluate=new CCombo(wStepSettings, SWT.BORDER | SWT.READ_ONLY);
        wfieldevaluate.setEditable(true);
        props.setLook(wfieldevaluate);
        wfieldevaluate.addModifyListener(lsMod);
        fdfieldevaluate=new FormData();
        fdfieldevaluate.left = new FormAttachment(middle, margin);
        fdfieldevaluate.top  = new FormAttachment(wStepname, margin);
        fdfieldevaluate.right= new FormAttachment(100, -margin);
        wfieldevaluate.setLayoutData(fdfieldevaluate);
        wfieldevaluate.addSelectionListener(lsSel);
        wfieldevaluate.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    getPreviousFields();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );

		
	      // Output Fieldame
              
        wResultField = new LabelTextVar(transMeta, wStepSettings, 
        		BaseMessages.getString(PKG, "RegexEvalDialog.ResultField.Label"), 
        		BaseMessages.getString(PKG, "RegexEvalDialog.ResultField.Tooltip"));
        
        
        props.setLook(wResultField);
        wResultField .addModifyListener(lsMod);
        fdResultField  = new FormData();
        fdResultField .left = new FormAttachment(0, 0);
        fdResultField .top = new FormAttachment(wfieldevaluate, margin);
        fdResultField .right = new FormAttachment(100, 0);
        wResultField .setLayoutData(fdResultField );
        
        // Allow capture groups?
        wlAllowCaptureGroups=new Label(wStepSettings, SWT.RIGHT);
        wlAllowCaptureGroups.setText(BaseMessages.getString(PKG, "RegexEvalDialog.AllowCaptureGroups.Label"));
        props.setLook(wlAllowCaptureGroups);
        fdlAllowCaptureGroups=new FormData();
        fdlAllowCaptureGroups.left  = new FormAttachment(0, 0);
        fdlAllowCaptureGroups.top   = new FormAttachment(wResultField, margin);
        fdlAllowCaptureGroups.right = new FormAttachment(middle, -margin);
        wlAllowCaptureGroups.setLayoutData(fdlAllowCaptureGroups);
        wAllowCaptureGroups=new Button(wStepSettings, SWT.CHECK);
        wAllowCaptureGroups.setToolTipText(BaseMessages.getString(PKG, "RegexEvalDialog.AllowCaptureGroups.Tooltip"));
        props.setLook(wAllowCaptureGroups);
        fdAllowCaptureGroups=new FormData();
        fdAllowCaptureGroups.left  = new FormAttachment(middle, margin);
        fdAllowCaptureGroups.top   = new FormAttachment(wResultField, margin);
        fdAllowCaptureGroups.right = new FormAttachment(100,0);
        wAllowCaptureGroups.setLayoutData(fdAllowCaptureGroups);
        
        wAllowCaptureGroups.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                setFieldsEnabledStatus();
                input.setChanged();
            }
        });
        
        // Replace fields?
        wlReplaceFields=new Label(wStepSettings, SWT.RIGHT);
        wlReplaceFields.setText(BaseMessages.getString(PKG, "RegexEvalDialog.ReplaceFields.Label"));
        props.setLook(wlReplaceFields);
        fdlReplaceFields=new FormData();
        fdlReplaceFields.left  = new FormAttachment(0, 0);
        fdlReplaceFields.top   = new FormAttachment(wAllowCaptureGroups, margin);
        fdlReplaceFields.right = new FormAttachment(middle, -margin);
        wlReplaceFields.setLayoutData(fdlReplaceFields);
        wReplaceFields=new Button(wStepSettings, SWT.CHECK);
        wReplaceFields.setToolTipText(BaseMessages.getString(PKG, "RegexEvalDialog.ReplaceFields.Tooltip"));
        props.setLook(wReplaceFields);
        fdReplaceFields=new FormData();
        fdReplaceFields.left  = new FormAttachment(middle, margin);
        fdReplaceFields.top   = new FormAttachment(wAllowCaptureGroups, margin);
        fdReplaceFields.right = new FormAttachment(100,0);
        wReplaceFields.setLayoutData(fdReplaceFields);
        
        wReplaceFields.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                input.setChanged();
            }
        });
        
        //settings layout
    	fdStepSettings = new FormData();
		fdStepSettings.left = new FormAttachment(0, margin);
		fdStepSettings.top = new FormAttachment(wStepname, margin);
		fdStepSettings.right = new FormAttachment(100, -margin);
		wStepSettings.setLayoutData(fdStepSettings);
		
		// ///////////////////////////////////////////////////////////
		// / END OF STEP SETTINGS GROUP
		// ///////////////////////////////////////////////////////////
		

		
		// Script line
		wlScript=new Label(wGeneralComp, SWT.NONE);
		wlScript.setText(BaseMessages.getString(PKG, "RegexEvalDialog.Javascript.Label")); //$NON-NLS-1$
 		props.setLook(wlScript);
		fdlScript=new FormData();
		fdlScript.left = new FormAttachment(0, 0);
		fdlScript.top  = new FormAttachment(wStepSettings, margin);
		wlScript.setLayoutData(fdlScript);
		
		wbTestRegExScript=new Button(wGeneralComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbTestRegExScript);
		wbTestRegExScript.setText(BaseMessages.getString(PKG, "RegexEvalDialog.TestScript.Label")); 
		fdbTestRegExScript=new FormData();
		fdbTestRegExScript.right = new FormAttachment(100, -margin);
		fdbTestRegExScript.top   = new FormAttachment(wStepSettings, margin);
		wbTestRegExScript.setLayoutData(fdbTestRegExScript);
		
		
		wScript=new StyledTextComp(transMeta, wGeneralComp, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");
		wScript.setText(BaseMessages.getString(PKG, "RegexEvalDialog.Script.Label")); //$NON-NLS-1$
 		props.setLook(wScript, Props.WIDGET_STYLE_FIXED);
		wScript.addModifyListener(lsMod);
		fdScript=new FormData();
		fdScript.left   = new FormAttachment(0, 0);
		fdScript.top    = new FormAttachment(wbTestRegExScript, margin);
		fdScript.right  = new FormAttachment(100, -10);
		fdScript.bottom = new FormAttachment(100, -25);
		wScript.setLayoutData(fdScript);
        //SelectionAdapter lsVar = VariableButtonListenerFactory.getSelectionAdapter(shell, wScript);
        //wScript.addKeyListener(TextVar.getControlSpaceKeyListener(wScript, lsVar));

        
		// Variable substitution?
		wlUseVar=new Label(wGeneralComp, SWT.NONE);
		wlUseVar.setText(BaseMessages.getString(PKG, "RegexEvalDialog.UseVar.Label"));
 		props.setLook(wlUseVar);
		fdlUseVar=new FormData();
		fdlUseVar.left  = new FormAttachment(0, margin);
		fdlUseVar.top   = new FormAttachment(wScript, margin);

		wlUseVar.setLayoutData(fdlUseVar);
		wUseVar=new Button(wGeneralComp, SWT.CHECK);
		wUseVar.setToolTipText(BaseMessages.getString(PKG, "RegexEvalDialog.UseVar.Tooltip"));
 		props.setLook(wUseVar);
		fdUseVar=new FormData();
		fdUseVar.left  = new FormAttachment(wlUseVar, margin);
		fdUseVar.top   = new FormAttachment(wScript, margin);
		wUseVar.setLayoutData(fdUseVar);
		wUseVar.addSelectionListener(lsSel);
		wBottom = new Composite(wSash, SWT.NONE);
 		props.setLook(wBottom);

 		FormLayout bottomLayout  = new FormLayout ();
		bottomLayout.marginWidth  = Const.FORM_MARGIN;
		bottomLayout.marginHeight = Const.FORM_MARGIN;
		wBottom.setLayout(bottomLayout);
		
		wSeparator = new Label(wBottom, SWT.SEPARATOR | SWT.HORIZONTAL);
		fdSeparator= new FormData();
		fdSeparator.left  = new FormAttachment(0, 0);
		fdSeparator.right = new FormAttachment(100, 0);
		fdSeparator.top   = new FormAttachment(0, -margin+2);
		wSeparator.setLayoutData(fdSeparator);

		wlFields=new Label(wBottom, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "RegexEvalDialog.Fields.Label")); //$NON-NLS-1$
		wlFields.setToolTipText(BaseMessages.getString(PKG, "RegexEvalDialog.Fields.Tooltip")); //$NON-NLS-1$
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wSeparator, 0);
		wlFields.setLayoutData(fdlFields);
        

		final int fieldsRows=input.getFieldName().length;
		
		ColumnInfo[] columnInfo = new ColumnInfo[] {
			new ColumnInfo(BaseMessages.getString(PKG, "RegexEvalDialog.ColumnInfo.NewField"),    ColumnInfo.COLUMN_TYPE_TEXT,   false ), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "RegexEvalDialog.ColumnInfo.Type"),        ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes() ), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "RegexEvalDialog.ColumnInfo.Length"),      ColumnInfo.COLUMN_TYPE_TEXT,   false ), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "RegexEvalDialog.ColumnInfo.Precision"),   ColumnInfo.COLUMN_TYPE_TEXT,   false ), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "RegexEvalDialog.ColumnInfo.Format"),      ColumnInfo.COLUMN_TYPE_TEXT,   false ), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "RegexEvalDialog.ColumnInfo.Group"),       ColumnInfo.COLUMN_TYPE_TEXT,   false ), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "RegexEvalDialog.ColumnInfo.Decimal"),     ColumnInfo.COLUMN_TYPE_TEXT,   false ), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "RegexEvalDialog.ColumnInfo.Currency"),    ColumnInfo.COLUMN_TYPE_TEXT,   false ), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "RegexEvalDialog.ColumnInfo.Nullif"),      ColumnInfo.COLUMN_TYPE_TEXT,   false ), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "RegexEvalDialog.ColumnInfo.IfNull"),      ColumnInfo.COLUMN_TYPE_TEXT,   false ), //$NON-NLS-1$
			new ColumnInfo(BaseMessages.getString(PKG, "RegexEvalDialog.ColumnInfo.TrimType"),    ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.trimTypeDesc, true), //$NON-NLS-1$
		};
		
		wFields=new TableView(transMeta, wBottom, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
						      columnInfo, 
						      fieldsRows,  
						      lsMod,
							  props
						      );

		fdFields=new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top  = new FormAttachment(wlFields, margin);
		fdFields.right  = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, 0);
		wFields.setLayoutData(fdFields);

        
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
		// START OF CONTENT TAB///
		///
		wContentTab=new CTabItem(wTabFolder, SWT.NONE);
		wContentTab.setText(BaseMessages.getString(PKG, "RegexEvalDialog.ContentTab.TabTitle"));

		FormLayout contentLayout = new FormLayout ();
		contentLayout.marginWidth  = 3;
		contentLayout.marginHeight = 3;
		
		wContentComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wContentComp);
 		wContentComp.setLayout(contentLayout);
 		
		
		// Step RegexSettings grouping?
		// ////////////////////////
		// START OF RegexSettings GROUP
		// 

		wRegexSettings = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wRegexSettings);
		wRegexSettings.setText("Regex Settings");
		
		FormLayout regexLayout = new FormLayout();
		regexLayout.marginWidth = 10;
		regexLayout.marginHeight = 10;
		wRegexSettings.setLayout(regexLayout);
		
		
		// Canon_Eq?
		wlCanonEq=new Label(wRegexSettings, SWT.RIGHT);
		wlCanonEq.setText(BaseMessages.getString(PKG, "RegexEvalDialog.CanonEq.Label"));
 		props.setLook(wlCanonEq);
		fdlCanonEq=new FormData();
		fdlCanonEq.left  = new FormAttachment(0, 0);
		fdlCanonEq.top   = new FormAttachment(wStepSettings, margin);
		fdlCanonEq.right = new FormAttachment(middle, -margin);
		wlCanonEq.setLayoutData(fdlCanonEq);
		wCanonEq=new Button(wRegexSettings, SWT.CHECK);
		wCanonEq.setToolTipText(BaseMessages.getString(PKG, "RegexEvalDialog.CanonEq.Tooltip"));
 		props.setLook(wCanonEq);
		fdCanonEq=new FormData();
		fdCanonEq.left  = new FormAttachment(middle, 0);
		fdCanonEq.top   = new FormAttachment(wStepSettings, margin);
		fdCanonEq.right = new FormAttachment(100, 0);
		wCanonEq.setLayoutData(fdCanonEq);
		wCanonEq.addSelectionListener(lsSel);
		
		// CASE_INSENSITIVE?
		wlCaseInsensitive=new Label(wRegexSettings, SWT.RIGHT);
		wlCaseInsensitive.setText(BaseMessages.getString(PKG, "RegexEvalDialog.CaseInsensitive.Label"));
 		props.setLook(wlCaseInsensitive);
		fdlCaseInsensitive=new FormData();
		fdlCaseInsensitive.left  = new FormAttachment(0, 0);
		fdlCaseInsensitive.top   = new FormAttachment(wCanonEq, margin);
		fdlCaseInsensitive.right = new FormAttachment(middle, -margin);
		wlCaseInsensitive.setLayoutData(fdlCaseInsensitive);
		wCaseInsensitive=new Button(wRegexSettings, SWT.CHECK);
		wCaseInsensitive.setToolTipText(BaseMessages.getString(PKG, "RegexEvalDialog.CaseInsensitive.Tooltip"));
 		props.setLook(wCaseInsensitive);
		fdCaseInsensitive=new FormData();
		fdCaseInsensitive.left  = new FormAttachment(middle, 0);
		fdCaseInsensitive.top   = new FormAttachment(wCanonEq, margin);
		fdCaseInsensitive.right = new FormAttachment(100, 0);
		wCaseInsensitive.setLayoutData(fdCaseInsensitive);
		wCaseInsensitive.addSelectionListener(lsSel);
		
		// COMMENT?
		wlComment=new Label(wRegexSettings, SWT.RIGHT);
		wlComment.setText(BaseMessages.getString(PKG, "RegexEvalDialog.Comment.Label"));
 		props.setLook(wlComment);
		fdlComment=new FormData();
		fdlComment.left  = new FormAttachment(0, 0);
		fdlComment.top   = new FormAttachment(wCaseInsensitive, margin);
		fdlComment.right = new FormAttachment(middle, -margin);
		wlComment.setLayoutData(fdlComment);
		wComment=new Button(wRegexSettings, SWT.CHECK);
		wComment.setToolTipText(BaseMessages.getString(PKG, "RegexEvalDialog.Comment.Tooltip"));
 		props.setLook(wComment);
		fdComment=new FormData();
		fdComment.left  = new FormAttachment(middle, 0);
		fdComment.top   = new FormAttachment(wCaseInsensitive, margin);
		fdComment.right = new FormAttachment(100, 0);
		wComment.setLayoutData(fdComment);
		wComment.addSelectionListener(lsSel);
		
		// DOTALL?
		wlDotAll=new Label(wRegexSettings, SWT.RIGHT);
		wlDotAll.setText(BaseMessages.getString(PKG, "RegexEvalDialog.DotAll.Label"));
 		props.setLook(wlDotAll);
		fdlDotAll=new FormData();
		fdlDotAll.left  = new FormAttachment(0, 0);
		fdlDotAll.top   = new FormAttachment(wComment, margin);
		fdlDotAll.right = new FormAttachment(middle, -margin);
		wlDotAll.setLayoutData(fdlDotAll);
		wDotAll=new Button(wRegexSettings, SWT.CHECK);
		wDotAll.setToolTipText(BaseMessages.getString(PKG, "RegexEvalDialog.DotAll.Tooltip"));
 		props.setLook(wDotAll);
		fdDotAll=new FormData();
		fdDotAll.left  = new FormAttachment(middle, 0);
		fdDotAll.top   = new FormAttachment(wComment, margin);
		fdDotAll.right = new FormAttachment(100, 0);
		wDotAll.setLayoutData(fdDotAll);
		wDotAll.addSelectionListener(lsSel);
		
		// MULTILINE?
		wlMultiline=new Label(wRegexSettings, SWT.RIGHT);
		wlMultiline.setText(BaseMessages.getString(PKG, "RegexEvalDialog.Multiline.Label"));
 		props.setLook(wlMultiline);
		fdlMultiline=new FormData();
		fdlMultiline.left  = new FormAttachment(0, 0);
		fdlMultiline.top   = new FormAttachment(wDotAll, margin);
		fdlMultiline.right = new FormAttachment(middle, -margin);
		wlMultiline.setLayoutData(fdlMultiline);
		wMultiline=new Button(wRegexSettings, SWT.CHECK);
		wMultiline.setToolTipText(BaseMessages.getString(PKG, "RegexEvalDialog.Multiline.Tooltip"));
 		props.setLook(wMultiline);
		fdMultiline=new FormData();
		fdMultiline.left  = new FormAttachment(middle, 0);
		fdMultiline.top   = new FormAttachment(wDotAll, margin);
		fdMultiline.right = new FormAttachment(100, 0);
		wMultiline.setLayoutData(fdMultiline);
		wMultiline.addSelectionListener(lsSel);
		
		// UNICODE?
		wlUnicode=new Label(wRegexSettings, SWT.RIGHT);
		wlUnicode.setText(BaseMessages.getString(PKG, "RegexEvalDialog.Unicode.Label"));
 		props.setLook(wlUnicode);
		fdlUnicode=new FormData();
		fdlUnicode.left  = new FormAttachment(0, 0);
		fdlUnicode.top   = new FormAttachment(wMultiline, margin);
		fdlUnicode.right = new FormAttachment(middle, -margin);
		wlUnicode.setLayoutData(fdlUnicode);
		wUnicode=new Button(wRegexSettings, SWT.CHECK);
		wUnicode.setToolTipText(BaseMessages.getString(PKG, "RegexEvalDialog.Unicode.Tooltip"));
 		props.setLook(wUnicode);
		fdUnicode=new FormData();
		fdUnicode.left  = new FormAttachment(middle, 0);
		fdUnicode.top   = new FormAttachment(wMultiline, margin);
		fdUnicode.right = new FormAttachment(100, 0);
		wUnicode.setLayoutData(fdUnicode);
		wUnicode.addSelectionListener(lsSel);
		
		// UNIX?
		wlUnix=new Label(wRegexSettings, SWT.RIGHT);
		wlUnix.setText(BaseMessages.getString(PKG, "RegexEvalDialog.Unix.Label"));
 		props.setLook(wlUnix);
		fdlUnix=new FormData();
		fdlUnix.left  = new FormAttachment(0, 0);
		fdlUnix.top   = new FormAttachment(wUnicode, margin);
		fdlUnix.right = new FormAttachment(middle, -margin);
		wlUnix.setLayoutData(fdlUnix);
		wUnix=new Button(wRegexSettings, SWT.CHECK);
		wUnix.setToolTipText(BaseMessages.getString(PKG, "RegexEvalDialog.Unix.Tooltip"));
 		props.setLook(wUnix);
		fdUnix=new FormData();
		fdUnix.left  = new FormAttachment(middle, 0);
		fdUnix.top   = new FormAttachment(wUnicode, margin);
		fdUnix.right = new FormAttachment(100, 0);
		wUnix.setLayoutData(fdUnix);
		wUnix.addSelectionListener(lsSel);

		
	 	fdRegexSettings = new FormData();
		fdRegexSettings.left = new FormAttachment(0, margin);
		fdRegexSettings.top = new FormAttachment(wStepSettings, margin);
		fdRegexSettings.right = new FormAttachment(100, -margin);
		wRegexSettings.setLayoutData(fdRegexSettings);
		
		// ///////////////////////////////////////////////////////////
		// / END OF RegexSettings GROUP
		// ///////////////////////////////////////////////////////////
		
 		
 		
 		fdContentComp = new FormData();
 		fdContentComp.left  = new FormAttachment(0, 0);
 		fdContentComp.top   = new FormAttachment(0, 0);
 		fdContentComp.right = new FormAttachment(100, 0);
 		fdContentComp.bottom= new FormAttachment(100, 0);
 		wContentComp.setLayoutData(wContentComp);

		wContentComp.layout();
		wContentTab.setControl(wContentComp);


		/////////////////////////////////////////////////////////////
		/// END OF CONTENT TAB
		/////////////////////////////////////////////////////////////
		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);

		fdBottom=new FormData();
		fdBottom.left  = new FormAttachment(0, 0);
		fdBottom.top   = new FormAttachment(0, 0);
		fdBottom.right = new FormAttachment(100, 0);
		fdBottom.bottom= new FormAttachment(100, 0);
		wBottom.setLayoutData(fdBottom);

		fdSash = new FormData();
		fdSash.left  = new FormAttachment(0, 0);
		fdSash.top   = new FormAttachment(wStepname, 0);
		fdSash.right = new FormAttachment(100, 0);
		fdSash.bottom= new FormAttachment(100, -50);
		wSash.setLayoutData(fdSash);
		
		wSash.setWeights(new int[] {60,40});

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$

		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		lsbTestRegExScript   = new Listener() { public void handleEvent(Event e) {testRegExScript();  } };
		wbTestRegExScript.addListener  (SWT.Selection, lsbTestRegExScript);
		
		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);


		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();          } };
		
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();              } };
		
		wCancel.addListener(SWT.Selection, lsCancel);


		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
				
		wTabFolder.setSelection(0);
		
		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		
		setFieldsEnabledStatus();
		
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	 
	private void getPreviousFields()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null)
			{
	             r.getFieldNames();
	             
	             for (int i=0;i<r.getFieldNames().length;i++)
					{	
						wfieldevaluate.add(r.getFieldNames()[i]);					
						
					}
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "RegexEvalDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "RegexEvalDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	 
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
	
		if (input.getScript() != null) wScript.setText( input.getScript() );
		if (input.getResultFieldName() != null) wResultField.setText( input.getResultFieldName() );
		if (input.getMatcher() != null) wfieldevaluate.setText( input.getMatcher() );

		wUseVar.setSelection(input.isUseVariableInterpolationFlagSet());
		wReplaceFields.setSelection(input.isReplacefields());
		wAllowCaptureGroups.setSelection(input.isAllowCaptureGroupsFlagSet());
		wCanonEq.setSelection(input.isCanonicalEqualityFlagSet());
		wCaseInsensitive.setSelection(input.isCaseInsensitiveFlagSet());
		wComment.setSelection(input.isCommentFlagSet());
		wDotAll.setSelection(input.isDotAllFlagSet());
		wMultiline.setSelection(input.isMultilineFlagSet());
		wUnicode.setSelection(input.isUnicodeFlagSet());
		wUnix.setSelection(input.isUnixLineEndingsFlagSet());
		for (int i = 0; i < input.getFieldName().length; i++)
		{
			TableItem ti = wFields.table.getItem(i);
			if (input.getFieldName()[i]   != null) ti.setText( 1, input.getFieldName()[i]); 
	              ti.setText(2, ValueMeta.getTypeDesc(input.getFieldType()[i]));
	              ti.setText(3, input.getFieldLength()[i]>=0 ? ""+input.getFieldLength()[i] : "");  //$NON-NLS-1$
	              ti.setText(4, input.getFieldPrecision()[i]>=0 ? (""+input.getFieldPrecision()[i]) : "");  //$NON-NLS-1$
			if (input.getFieldFormat()[i]  != null) ti.setText(5, input.getFieldFormat()[i]); 
			if (input.getFieldGroup()[i]   != null) ti.setText(6, input.getFieldGroup()[i]); 
			if (input.getFieldDecimal()[i] != null) ti.setText(7, input.getFieldDecimal()[i]); 
			if (input.getFieldCurrency()[i]!= null) ti.setText(8, input.getFieldCurrency()[i]); 
            if (input.getFieldNullIf()[i] != null) ti.setText(9, input.getFieldNullIf()[i]);
            if (input.getFieldIfNull()[i] != null) ti.setText(10, input.getFieldIfNull()[i]);
            ti.setText(11, ValueMeta.getTrimTypeDesc(input.getFieldTrimType()[i]));
		}
		wFields.setRowNums();
		wFields.optWidth(true);

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

		stepname = wStepname.getText(); // return value

		setRegexOptions(input);

		int nrfields = wFields.nrNonEmpty();
		
		input.allocate(nrfields);
		
		for (int i = 0; i < input.getFieldName().length; i++)
		{
			TableItem ti      = wFields.getNonEmpty(i);
			input.getFieldName()     [i] = ti.getText( 1 ); 
			input.getFieldType()     [i] = ValueMeta.getType( ti.getText( 2 ) );
			input.getFieldLength()   [i] = Const.toInt( ti.getText( 3 ), -1 );
			input.getFieldPrecision()[i] = Const.toInt( ti.getText( 4 ), -1 );
			input.getFieldFormat()   [i] = ti.getText( 5 ); 
			input.getFieldGroup()    [i] = ti.getText( 6 ); 
			input.getFieldDecimal()  [i] = ti.getText( 7 ); 
			input.getFieldCurrency() [i] = ti.getText( 8 ); 
            input.getFieldNullIf()[i] = ti.getText(9);
            input.getFieldIfNull()[i] = ti.getText(10);
            input.getFieldTrimType()[i] = ValueMeta.getTrimTypeByDesc(ti.getText(11));
		}

		dispose();
	}
	
    private void setFieldsEnabledStatus()
    {
        wlFields.setEnabled(wAllowCaptureGroups.getSelection());
        wFields.setEnabled(wAllowCaptureGroups.getSelection());
        wlReplaceFields.setEnabled(wAllowCaptureGroups.getSelection());
        wReplaceFields.setEnabled(wAllowCaptureGroups.getSelection());
    }
    private void setRegexOptions(RegexEvalMeta input)
    {
    	input.setScript( wScript.getText() );
    	input.setResultFieldName(wResultField.getText() );
    	input.setMatcher(wfieldevaluate.getText() );
    	input.setUseVariableInterpolationFlag(wUseVar.getSelection());
    	input.setAllowCaptureGroupsFlag(wAllowCaptureGroups.getSelection());
    	input.setReplacefields(wReplaceFields.getSelection());
    	input.setCanonicalEqualityFlag(wCanonEq.getSelection());
    	input.setCaseInsensitiveFlag(wCaseInsensitive.getSelection());
    	input.setCommentFlag(wComment.getSelection());
    	input.setDotAllFlag(wDotAll.getSelection());
    	input.setMultilineFlag(wMultiline.getSelection());
    	input.setUnicodeFlag(wUnicode.getSelection());
    	input.setUnixLineEndingsFlag(wUnix.getSelection());
    }
    private void testRegExScript()
 	{
    	RegexEvalMeta meta = new RegexEvalMeta();
	    setRegexOptions(meta);
		RegexEvalHelperDialog d = new RegexEvalHelperDialog
                (
                        shell,
                        transMeta,
                        meta.getScript(),
                        meta.getRegexOptions(),
                        meta.isCanonicalEqualityFlagSet()
                );
 		wScript.setText(d.open());
 	}
}
