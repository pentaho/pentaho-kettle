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

package org.pentaho.di.ui.trans.steps.stepsmetrics;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.trans.steps.stepsmetrics.StepsMetrics;
import org.pentaho.di.trans.steps.stepsmetrics.StepsMetricsMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class StepsMetricsDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = StepsMetrics.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static final String[] YES_NO_COMBO = new String[] { BaseMessages.getString(PKG, "System.Combo.No"), BaseMessages.getString(PKG, "System.Combo.Yes") };
	
	private String previousSteps[] ;
	private StepsMetricsMeta input;

	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;
	
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wGeneralTab,wFieldsTab;
	
	private FormData     fdGeneralComp,fdFieldsComp;
	
	private Label        wlStepnameField;
	private TextVar      wStepnameField;
	private FormData     fdlStepnameField, fdStepnameField;
	
	private Label        wlStepidField;
	private TextVar      wStepidField;
	private FormData     fdlStepidField, fdStepidField;
	
	private Label        wlLinesinputField;
	private TextVar      wLinesinputField;
	private FormData     fdlLinesinputField, fdLinesinputField;
	
	private Label        wlLinesoutputField;
	private TextVar      wLinesoutputField;
	private FormData     fdlLinesoutputField, fdLinesoutputField;
	
	private Label        wlLinesreadField;
	private TextVar      wLinesreadField;
	private FormData     fdlLinesreadField, fdLinesreadField;
	
	private Label        wlLineswrittenField;
	private TextVar      wLineswrittenField;
	private FormData     fdlLineswrittenField, fdLineswrittenField;
	
	private Label        wlLineserrorsField;
	private TextVar      wLineserrorsField;
	private FormData     fdlLineserrorsField, fdLineserrorsField;
	
	private Label        wlSecondsField;
	private TextVar      wSecondsField;
	private FormData     fdlSecondsField, fdSecondsField;
	
	private Label        wlLinesupdatedField;
	private TextVar      wLinesupdatedField;
	private FormData     fdlLinesupdatedField, fdLinesupdatedField;
	
	
	public StepsMetricsDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(StepsMetricsMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
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
		shell.setText(BaseMessages.getString(PKG, "StepsMetricsDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "StepsMetricsDialog.Stepname.Label")); //$NON-NLS-1$
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
		// START OF GENERAL TAB///
		///
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText("General");
		Composite wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);
		FormLayout fileLayout = new FormLayout();
		fileLayout.marginWidth  = 3;
		fileLayout.marginHeight = 3;
		wGeneralComp.setLayout(fileLayout);
		
        // Get the previous steps...
        setStepNames();
        
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "StepsMetricsDialog.getSteps.Label"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		setButtonPositions(new Button[] { wOK, wGet, wCancel }, margin, null);
        
	
        // Table with fields
		wlFields=new Label(wGeneralComp, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "StepsMetricsDialog.Fields.Label"));
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wStepname, margin);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsCols=3;
		final int FieldsRows=input.getStepName().length;
		
		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
		colinf[0]=new ColumnInfo(BaseMessages.getString(PKG, "StepsMetricsDialog.Fieldname.Step"),  ColumnInfo.COLUMN_TYPE_CCOMBO,  previousSteps, false);
		colinf[1]=new ColumnInfo(BaseMessages.getString(PKG, "StepsMetricsDialog.Fieldname.CopyNr"),  ColumnInfo.COLUMN_TYPE_TEXT, false);
		colinf[2]=new ColumnInfo(BaseMessages.getString(PKG, "StepsMetricsDialog.Required.Column"), ColumnInfo.COLUMN_TYPE_CCOMBO,  YES_NO_COMBO );
		colinf[1].setUsingVariables(true);
		wFields=new TableView(transMeta, wGeneralComp, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
							  colinf, 
							  FieldsRows,  
							  lsMod,
							  props
							  );

		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom= new FormAttachment(100, -2*margin);
		wFields.setLayoutData(fdFields);
		
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
		// START OF FIELDS TAB///
		///
		wFieldsTab=new CTabItem(wTabFolder, SWT.NONE);
		wFieldsTab.setText(BaseMessages.getString(PKG, "StepsMetricsDialog.Group.Fields"));
		Composite wFieldsComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wFieldsComp);
		FormLayout fieldsLayout = new FormLayout();
		fieldsLayout.marginWidth  = 3;
		fieldsLayout.marginHeight = 3;
		wFieldsComp.setLayout(fieldsLayout);
		
		// Stepname line
		wlStepnameField=new Label(wFieldsComp, SWT.RIGHT);
		wlStepnameField.setText(BaseMessages.getString(PKG, "StepsMetricsDialog.Label.StepnameField"));
 		props.setLook(wlStepnameField);
		fdlStepnameField=new FormData();
		fdlStepnameField.left = new FormAttachment(0, 0);
		fdlStepnameField.top  = new FormAttachment(0, margin);
		fdlStepnameField.right= new FormAttachment(middle, -margin);
		wlStepnameField.setLayoutData(fdlStepnameField);
		wStepnameField=new TextVar(transMeta, wFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepnameField.setText("");
 		props.setLook(wStepnameField);
		wStepnameField.addModifyListener(lsMod);
		fdStepnameField=new FormData();
		fdStepnameField.left = new FormAttachment(middle, 0);
		fdStepnameField.top  = new FormAttachment(0, margin);
		fdStepnameField.right= new FormAttachment(100, -margin);
		wStepnameField.setLayoutData(fdStepnameField);
		
		// Stepid line
		wlStepidField=new Label(wFieldsComp, SWT.RIGHT);
		wlStepidField.setText(BaseMessages.getString(PKG, "StepsMetricsDialog.Label.StepidField"));
 		props.setLook(wlStepidField);
		fdlStepidField=new FormData();
		fdlStepidField.left = new FormAttachment(0, 0);
		fdlStepidField.top  = new FormAttachment(wStepnameField, margin);
		fdlStepidField.right= new FormAttachment(middle, -margin);
		wlStepidField.setLayoutData(fdlStepidField);
		wStepidField=new TextVar(transMeta, wFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepidField.setText("");
 		props.setLook(wStepidField);
		wStepidField.addModifyListener(lsMod);
		fdStepidField=new FormData();
		fdStepidField.left = new FormAttachment(middle, 0);
		fdStepidField.top  = new FormAttachment(wStepnameField, margin);
		fdStepidField.right= new FormAttachment(100, -margin);
		wStepidField.setLayoutData(fdStepidField);
		
		// Linesinput line
		wlLinesinputField=new Label(wFieldsComp, SWT.RIGHT);
		wlLinesinputField.setText(BaseMessages.getString(PKG, "StepsMetricsDialog.Label.LinesinputField"));
 		props.setLook(wlLinesinputField);
		fdlLinesinputField=new FormData();
		fdlLinesinputField.left = new FormAttachment(0, 0);
		fdlLinesinputField.top  = new FormAttachment(wStepidField, margin);
		fdlLinesinputField.right= new FormAttachment(middle, -margin);
		wlLinesinputField.setLayoutData(fdlLinesinputField);
		wLinesinputField=new TextVar(transMeta, wFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wLinesinputField.setText("");
 		props.setLook(wLinesinputField);
		wLinesinputField.addModifyListener(lsMod);
		fdLinesinputField=new FormData();
		fdLinesinputField.left = new FormAttachment(middle, 0);
		fdLinesinputField.top  = new FormAttachment(wStepidField, margin);
		fdLinesinputField.right= new FormAttachment(100, -margin);
		wLinesinputField.setLayoutData(fdLinesinputField);
		
		// Linesoutput line
		wlLinesoutputField=new Label(wFieldsComp, SWT.RIGHT);
		wlLinesoutputField.setText(BaseMessages.getString(PKG, "StepsMetricsDialog.Label.LinesoutputField"));
 		props.setLook(wlLinesoutputField);
		fdlLinesoutputField=new FormData();
		fdlLinesoutputField.left = new FormAttachment(0, 0);
		fdlLinesoutputField.top  = new FormAttachment(wLinesinputField, margin);
		fdlLinesoutputField.right= new FormAttachment(middle, -margin);
		wlLinesoutputField.setLayoutData(fdlLinesoutputField);
		wLinesoutputField=new TextVar(transMeta, wFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wLinesoutputField.setText("");
 		props.setLook(wLinesoutputField);
		wLinesoutputField.addModifyListener(lsMod);
		fdLinesoutputField=new FormData();
		fdLinesoutputField.left = new FormAttachment(middle, 0);
		fdLinesoutputField.top  = new FormAttachment(wLinesinputField, margin);
		fdLinesoutputField.right= new FormAttachment(100, -margin);
		wLinesoutputField.setLayoutData(fdLinesoutputField);
		
		// Linesread line
		wlLinesreadField=new Label(wFieldsComp, SWT.RIGHT);
		wlLinesreadField.setText(BaseMessages.getString(PKG, "StepsMetricsDialog.Label.LinesreadField"));
 		props.setLook(wlLinesreadField);
		fdlLinesreadField=new FormData();
		fdlLinesreadField.left = new FormAttachment(0, 0);
		fdlLinesreadField.top  = new FormAttachment(wLinesoutputField, margin);
		fdlLinesreadField.right= new FormAttachment(middle, -margin);
		wlLinesreadField.setLayoutData(fdlLinesreadField);
		wLinesreadField=new TextVar(transMeta, wFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wLinesreadField.setText("");
 		props.setLook(wLinesreadField);
		wLinesreadField.addModifyListener(lsMod);
		fdLinesreadField=new FormData();
		fdLinesreadField.left = new FormAttachment(middle, 0);
		fdLinesreadField.top  = new FormAttachment(wLinesoutputField, margin);
		fdLinesreadField.right= new FormAttachment(100, -margin);
		wLinesreadField.setLayoutData(fdLinesreadField);
		
		// Linesupdated line
		wlLinesupdatedField=new Label(wFieldsComp, SWT.RIGHT);
		wlLinesupdatedField.setText(BaseMessages.getString(PKG, "StepsMetricsDialog.Label.LinesupdatedField"));
 		props.setLook(wlLinesupdatedField);
		fdlLinesupdatedField=new FormData();
		fdlLinesupdatedField.left = new FormAttachment(0, 0);
		fdlLinesupdatedField.top  = new FormAttachment(wLinesreadField, margin);
		fdlLinesupdatedField.right= new FormAttachment(middle, -margin);
		wlLinesupdatedField.setLayoutData(fdlLinesupdatedField);
		wLinesupdatedField=new TextVar(transMeta, wFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wLinesupdatedField.setText("");
 		props.setLook(wLinesupdatedField);
		wLinesupdatedField.addModifyListener(lsMod);
		fdLinesupdatedField=new FormData();
		fdLinesupdatedField.left = new FormAttachment(middle, 0);
		fdLinesupdatedField.top  = new FormAttachment(wLinesreadField, margin);
		fdLinesupdatedField.right= new FormAttachment(100, -margin);
		wLinesupdatedField.setLayoutData(fdLinesupdatedField);
		
		// Lineswritten line
		wlLineswrittenField=new Label(wFieldsComp, SWT.RIGHT);
		wlLineswrittenField.setText(BaseMessages.getString(PKG, "StepsMetricsDialog.Label.LineswrittenField"));
 		props.setLook(wlLineswrittenField);
		fdlLineswrittenField=new FormData();
		fdlLineswrittenField.left = new FormAttachment(0, 0);
		fdlLineswrittenField.top  = new FormAttachment(wLinesupdatedField, margin);
		fdlLineswrittenField.right= new FormAttachment(middle, -margin);
		wlLineswrittenField.setLayoutData(fdlLineswrittenField);
		wLineswrittenField=new TextVar(transMeta, wFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wLineswrittenField.setText("");
 		props.setLook(wLineswrittenField);
		wLineswrittenField.addModifyListener(lsMod);
		fdLineswrittenField=new FormData();
		fdLineswrittenField.left = new FormAttachment(middle, 0);
		fdLineswrittenField.top  = new FormAttachment(wLinesupdatedField, margin);
		fdLineswrittenField.right= new FormAttachment(100, -margin);
		wLineswrittenField.setLayoutData(fdLineswrittenField);
		
		// Lineserrors line
		wlLineserrorsField=new Label(wFieldsComp, SWT.RIGHT);
		wlLineserrorsField.setText(BaseMessages.getString(PKG, "StepsMetricsDialog.Label.LineserrorsField"));
 		props.setLook(wlLineserrorsField);
		fdlLineserrorsField=new FormData();
		fdlLineserrorsField.left = new FormAttachment(0, 0);
		fdlLineserrorsField.top  = new FormAttachment(wLineswrittenField, margin);
		fdlLineserrorsField.right= new FormAttachment(middle, -margin);
		wlLineserrorsField.setLayoutData(fdlLineserrorsField);
		wLineserrorsField=new TextVar(transMeta, wFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wLineserrorsField.setText("");
 		props.setLook(wLineserrorsField);
		wLineserrorsField.addModifyListener(lsMod);
		fdLineserrorsField=new FormData();
		fdLineserrorsField.left = new FormAttachment(middle, 0);
		fdLineserrorsField.top  = new FormAttachment(wLineswrittenField, margin);
		fdLineserrorsField.right= new FormAttachment(100, -margin);
		wLineserrorsField.setLayoutData(fdLineserrorsField);
		
		// Seconds line
		wlSecondsField=new Label(wFieldsComp, SWT.RIGHT);
		wlSecondsField.setText(BaseMessages.getString(PKG, "StepsMetricsDialog.Label.DurationField"));
 		props.setLook(wlSecondsField);
		fdlSecondsField=new FormData();
		fdlSecondsField.left = new FormAttachment(0, 0);
		fdlSecondsField.top  = new FormAttachment(wLineserrorsField, margin);
		fdlSecondsField.right= new FormAttachment(middle, -margin);
		wlSecondsField.setLayoutData(fdlSecondsField);
		wSecondsField=new TextVar(transMeta, wFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wSecondsField.setText("");
 		props.setLook(wSecondsField);
		wSecondsField.addModifyListener(lsMod);
		fdSecondsField=new FormData();
		fdSecondsField.left = new FormAttachment(middle, 0);
		fdSecondsField.top  = new FormAttachment(wLineserrorsField, margin);
		fdSecondsField.right= new FormAttachment(100, -margin);
		wSecondsField.setLayoutData(fdSecondsField);
		
		
		fdFieldsComp=new FormData();
		fdFieldsComp.left  = new FormAttachment(0, 0);
		fdFieldsComp.top   = new FormAttachment(0, 0);
		fdFieldsComp.right = new FormAttachment(100, 0);
		fdFieldsComp.bottom= new FormAttachment(100, 0);
		wFieldsComp.setLayoutData(fdFieldsComp);
		wFieldsComp.layout();
		wFieldsTab.setControl(wFieldsComp);

		/////////////////////////////////////////////////////////////
		/// END OF FIELDS TAB
		/////////////////////////////////////////////////////////////

		
		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();    } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		wTabFolder.setSelection(0);
		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	private void setStepNames()
	{
		previousSteps=transMeta.getStepNames();
		String[] nextSteps=transMeta.getNextStepNames(stepMeta);
		
		List<String> entries = new ArrayList<String>();
		for(int i=0;i<previousSteps.length;i++) {
			if(!previousSteps[i].equals(stepname)) {	
				if(nextSteps!=null) {
					for(int j=0;j<nextSteps.length;j++) {
						if(!nextSteps[j].equals(previousSteps[i])) entries.add(previousSteps[i]);	
					}
				}
			}
		}
		previousSteps = (String[]) entries.toArray(new String[entries.size()]);
	}
	private void get()
	{
		wFields.removeAll();
		Table table = wFields.table;

		for(int i=0;i<previousSteps.length;i++) {
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(0, ""+(i+1));
			ti.setText(1,previousSteps[i]);
			ti.setText(2,"0");
			ti.setText(3,BaseMessages.getString(PKG, "System.Combo.No"));
		}
		wFields.removeEmptyRows();
        wFields.setRowNums();
		wFields.optWidth(true);
		
	}
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		Table table = wFields.table;
		if (input.getStepName().length>0) table.removeAll();
		for (int i=0;i<input.getStepName().length;i++)
		{
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(0, ""+(i+1));
			if(input.getStepName()[i]!=null)
			{
				ti.setText(1, input.getStepName()[i]);
				ti.setText(2, String.valueOf(Const.toInt(input.getStepCopyNr()[i],0)));
				ti.setText(3, input.getRequiredStepsDesc(input.getStepRequired()[i]));	
			}
		}
		

		wFields.removeEmptyRows();
        wFields.setRowNums();
		wFields.optWidth(true);
		
		if(input.getStepNameFieldName()!=null) wStepnameField.setText(input.getStepNameFieldName());
		if(input.getStepIdFieldName()!=null) wStepidField.setText(input.getStepIdFieldName());
		if(input.getStepLinesInputFieldName()!=null) wLinesinputField.setText(input.getStepLinesInputFieldName());
		if(input.getStepLinesOutputFieldName()!=null) wLinesoutputField.setText(input.getStepLinesOutputFieldName());
		if(input.getStepLinesReadFieldName()!=null) wLinesreadField.setText(input.getStepLinesReadFieldName());
		if(input.getStepLinesWrittenFieldName()!=null) wLineswrittenField.setText(input.getStepLinesWrittenFieldName());
		if(input.getStepLinesUpdatedFieldName()!=null) wLinesupdatedField.setText(input.getStepLinesUpdatedFieldName());
		if(input.getStepLinesErrorsFieldName()!=null) wLineserrorsField.setText(input.getStepLinesErrorsFieldName());
		if(input.getStepSecondsFieldName()!=null) wSecondsField.setText(input.getStepSecondsFieldName());
		
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
		
		getInfo(input);
		dispose();
	}
	private void getInfo(StepsMetricsMeta in)  {
		stepname = wStepname.getText(); // return value
		int nrsteps = wFields.nrNonEmpty();
		in.allocate(nrsteps);
		for (int i=0;i<nrsteps;i++)
		{
			TableItem ti = wFields.getNonEmpty(i);
			StepMeta tm=transMeta.findStep(ti.getText(1));
			if(tm!=null){
				in.getStepName()[i] =tm.getName();
				in.getStepCopyNr()[i] =""+Const.toInt(ti.getText(2),0);
				in.getStepRequired()[i]=in.getRequiredStepsCode(ti.getText(3)); 
			}

		}
		
		in.setStepNameFieldName(wStepnameField.getText());
		in.setStepIdFieldName(wStepidField.getText());
		in.setStepLinesInputFieldName(wLinesinputField.getText());
		in.setStepLinesOutputFieldName(wLinesoutputField.getText());
		in.setStepLinesReadFieldName(wLinesreadField.getText());
		in.setStepLinesWrittenFieldName(wLineswrittenField.getText());
		in.setStepLinesUpdatedFieldName(wLinesupdatedField.getText());
		in.setStepLinesErrorsFieldName(wLineserrorsField.getText());
		in.setStepSecondsFieldName(wSecondsField.getText());

	}
}
