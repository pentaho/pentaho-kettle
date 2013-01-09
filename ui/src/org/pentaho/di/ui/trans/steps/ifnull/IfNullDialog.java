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

package org.pentaho.di.ui.trans.steps.ifnull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.ifnull.IfNullMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;


public class IfNullDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = IfNullMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private IfNullMeta input;
	
	private int FieldsRows=0;
	private ModifyListener lsMod ;
	private ModifyListener oldlsMod ;
	private int middle;
	private int margin;
	
	/**
	 * all fields from the previous steps
	 */
	private RowMetaInterface prevFields = null;
    
	/**
	 * List of ColumnInfo that should have the previous fields combo box
	 */
	private List<ColumnInfo> fieldColumns = new ArrayList<ColumnInfo>();
	
    private Label wlSelectFields,wlSelectValuesType;
    private FormData fdSelectFields,fdlSelectFields,fdSelectValuesType,fdlSelectValuesType;
    private Button wSelectFields,wSelectValuesType;

	private Label        wlFields,wlValueTypes;
	private TableView    wFields,wValueTypes;
	private FormData     fdlFields, fdFields,fdValueTypes,fdlValueTypes;
	
	private Label wlReplaceByValue;
	private FormData fdlReplaceByValue;
	
	private TextVar wReplaceByValue;
	private FormData fdReplaceByValue;
	
    private Label        wlMask;
    private CCombo       wMask;
    private FormData     fdlMask, fdMask;
    
	private FormData fdAllFields;
	private Group wAllFields;
	
    private Label        wlSetEmptyStringAll;
    private Button       wSetEmptyStringAll;
    private FormData     fdlSetEmptyStringAll, fdSetEmptyStringAll;

	
	public IfNullDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(IfNullMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
 		props.setLook(shell);
 		setShellImage(shell, input);
        
		lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};

        
		changed = input.hasChanged();
		oldlsMod=lsMod;
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		middle = props.getMiddlePct();
		margin = Const.MARGIN;
		
		FieldsRows=input.getFieldName().length;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "IfNullDialog.Shell.Title")); //$NON-NLS-1$

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "IfNullDialog.Stepname.Label")); //$NON-NLS-1$
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
		
		// ///////////////////////////////
		// START OF All Fields GROUP  //
		///////////////////////////////// 

		wAllFields = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wAllFields);
		wAllFields.setText(BaseMessages.getString(PKG, "IfNullDialog.AllFields.Label"));
		
		FormLayout AllFieldsgroupLayout = new FormLayout();
		AllFieldsgroupLayout.marginWidth = 10;
		AllFieldsgroupLayout.marginHeight = 10;
		wAllFields.setLayout(AllFieldsgroupLayout);

		
		// Replace by Value
		wlReplaceByValue = new Label(wAllFields, SWT.RIGHT);
		wlReplaceByValue.setText(BaseMessages.getString(PKG, "IfNullDialog.ReplaceByValue.Label"));
		props.setLook(wlReplaceByValue);
		fdlReplaceByValue = new FormData();
		fdlReplaceByValue.left = new FormAttachment(0, 0);
		fdlReplaceByValue.right = new FormAttachment(middle, -margin);
		fdlReplaceByValue.top = new FormAttachment(wStepname, margin*2);
		wlReplaceByValue.setLayoutData(fdlReplaceByValue);

		wReplaceByValue = new TextVar(transMeta, wAllFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wReplaceByValue.setToolTipText(BaseMessages.getString(PKG, "IfNullDialog.ReplaceByValue.Tooltip"));
 		props.setLook(wReplaceByValue);
		fdReplaceByValue=new FormData();
		fdReplaceByValue.left  = new FormAttachment(middle, 0);
		fdReplaceByValue.top   = new FormAttachment(wStepname, 2*margin);
		fdReplaceByValue.right = new FormAttachment(100, 0);
		wReplaceByValue.setLayoutData(fdReplaceByValue);
		
        // SetEmptyStringAll line
        wlSetEmptyStringAll=new Label(wAllFields, SWT.RIGHT);
        wlSetEmptyStringAll.setText(BaseMessages.getString(PKG, "IfNullDialog.SetEmptyStringAll.Label"));
        props.setLook(wlSetEmptyStringAll);
        fdlSetEmptyStringAll=new FormData();
        fdlSetEmptyStringAll.left  = new FormAttachment(0, 0);
        fdlSetEmptyStringAll.top   = new FormAttachment(wReplaceByValue, margin);
        fdlSetEmptyStringAll.right = new FormAttachment(middle, -margin);
        wlSetEmptyStringAll.setLayoutData(fdlSetEmptyStringAll);
        wSetEmptyStringAll=new Button(wAllFields, SWT.CHECK);
        wSetEmptyStringAll.setToolTipText(BaseMessages.getString(PKG, "IfNullDialog.SetEmptyStringAll.Tooltip"));
        props.setLook(wSetEmptyStringAll);
        fdSetEmptyStringAll=new FormData();
        fdSetEmptyStringAll.left  = new FormAttachment(middle, 0);
        fdSetEmptyStringAll.top   = new FormAttachment(wReplaceByValue, margin);
        fdSetEmptyStringAll.right = new FormAttachment(100, 0);
        wSetEmptyStringAll.setLayoutData(fdSetEmptyStringAll);
        wSetEmptyStringAll.addSelectionListener(new SelectionAdapter()
        {
        
            public void widgetSelected(SelectionEvent e)
            {
                input.setChanged();
                enableSetEmptyStringAll();
            }
        }
        );   
		
	    wlMask=new Label(wAllFields, SWT.RIGHT);
        wlMask.setText(BaseMessages.getString(PKG, "IfNullDialog.Mask.Label"));
        props.setLook(wlMask);
        fdlMask=new FormData();
        fdlMask.left = new FormAttachment(0, 0);
        fdlMask.top  = new FormAttachment(wSetEmptyStringAll, margin);
        fdlMask.right= new FormAttachment(middle, -margin);
        wlMask.setLayoutData(fdlMask);
        wMask=new CCombo(wAllFields, SWT.BORDER | SWT.READ_ONLY);
        wMask.setEditable(true);
        wMask.setItems(Const.getDateFormats());
        props.setLook(wMask);
        wMask.addModifyListener(lsMod);
        fdMask=new FormData();
        fdMask.left = new FormAttachment(middle, 0);
        fdMask.top  = new FormAttachment(wSetEmptyStringAll, margin);
        fdMask.right= new FormAttachment(100, 0);
        wMask.setLayoutData(fdMask);
	
		fdAllFields = new FormData();
		fdAllFields.left = new FormAttachment(0, margin);
		fdAllFields.top = new FormAttachment(wStepname, margin);
		fdAllFields.right = new FormAttachment(100, -margin);
		wAllFields.setLayoutData(fdAllFields);
		
		// ///////////////////////////////////////////////////////////
		// / END OF All Fields GROUP
		// ///////////////////////////////////////////////////////////		

		
		// Select fields?
		wlSelectFields=new Label(shell, SWT.RIGHT);
		wlSelectFields.setText(BaseMessages.getString(PKG, "IfNullDialog.SelectFields.Label"));
		props.setLook(wlSelectFields);
		fdlSelectFields=new FormData();
		fdlSelectFields.left  = new FormAttachment(0, 0);
		fdlSelectFields.top   = new FormAttachment(wAllFields, margin);
		fdlSelectFields.right = new FormAttachment(middle, -margin);
		wlSelectFields.setLayoutData(fdlSelectFields);
		wSelectFields=new Button(shell, SWT.CHECK);
		wSelectFields.setToolTipText(BaseMessages.getString(PKG, "IfNullDialog.SelectFields.Tooltip"));
 		props.setLook(wSelectFields);
		fdSelectFields=new FormData();
		fdSelectFields.left  = new FormAttachment(middle, 0);
		fdSelectFields.top   = new FormAttachment(wAllFields, margin);
		fdSelectFields.right = new FormAttachment(100, 0);
		wSelectFields.setLayoutData(fdSelectFields);
		
	
		// Select type?
		wlSelectValuesType=new Label(shell, SWT.RIGHT);
		wlSelectValuesType.setText(BaseMessages.getString(PKG, "IfNullDialog.SelectValuesType.Label"));
		props.setLook(wlSelectValuesType);
		fdlSelectValuesType=new FormData();
		fdlSelectValuesType.left  = new FormAttachment(0, 0);
		fdlSelectValuesType.top   = new FormAttachment(wSelectFields, margin);
		fdlSelectValuesType.right = new FormAttachment(middle, -margin);
		wlSelectValuesType.setLayoutData(fdlSelectValuesType);
		wSelectValuesType=new Button(shell, SWT.CHECK);
		wSelectValuesType.setToolTipText(BaseMessages.getString(PKG, "IfNullDialog.SelectValuesType.Tooltip"));
 		props.setLook(wSelectValuesType);
		fdSelectValuesType=new FormData();
		fdSelectValuesType.left  = new FormAttachment(middle, 0);
		fdSelectValuesType.top   = new FormAttachment(wSelectFields, margin);
		fdSelectValuesType.right = new FormAttachment(100, 0);
		wSelectValuesType.setLayoutData(fdSelectValuesType);
		
		wlValueTypes=new Label(shell, SWT.NONE);
		wlValueTypes.setText(BaseMessages.getString(PKG, "IfNullDialog.ValueTypes.Label"));
 		props.setLook(wlValueTypes);
		fdlValueTypes=new FormData();
		fdlValueTypes.left = new FormAttachment(0, 0);
		fdlValueTypes.top  = new FormAttachment(wSelectValuesType, margin);
		wlValueTypes.setLayoutData(fdlValueTypes);
		
		int ValueTypesRows=input.getFieldName().length;
		int FieldsCols=4;
		
		ColumnInfo[] colval=new ColumnInfo[FieldsCols];
		colval[0]=new ColumnInfo(BaseMessages.getString(PKG, "IfNullDialog.ValueType.Column"),  ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMetaInterface.typeCodes);
		colval[1]=new ColumnInfo(BaseMessages.getString(PKG, "IfNullDialog.Value.Column"), ColumnInfo.COLUMN_TYPE_TEXT , false);
		colval[2]=new ColumnInfo(BaseMessages.getString(PKG, "IfNullDialog.Value.ConversionMask"), ColumnInfo.COLUMN_TYPE_CCOMBO, Const.getDateFormats());
		colval[3]= new ColumnInfo(BaseMessages.getString(PKG, "IfNullDialog.Value.SetEmptyString"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { BaseMessages.getString(PKG, "System.Combo.Yes"), BaseMessages.getString(PKG, "System.Combo.No") } );
			
		colval[1].setUsingVariables(true);
		wValueTypes=new TableView(transMeta, shell, 
				  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
				  colval, 
				  ValueTypesRows,  
				  oldlsMod,
				  props
				  );

		fdValueTypes=new FormData();
		fdValueTypes.left  = new FormAttachment(0, 0);
		fdValueTypes.top   = new FormAttachment(wlValueTypes, margin);
		fdValueTypes.right = new FormAttachment(100, 0);
		fdValueTypes.bottom= new FormAttachment(wlValueTypes, 190);
	
		wValueTypes.setLayoutData(fdValueTypes);
		
		getFirstData();

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "System.Button.GetFields"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
		
		
		setButtonPositions(new Button[] { wOK, wGet, wCancel }, margin, null);
        
		addFields();
		
		wSelectValuesType.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	activeSelectValuesType();
            	input.setChanged();
            }
        });
        
		wSelectFields.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	activeSelectFields();
            	input.setChanged();
            }
        });

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


		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		enableSetEmptyStringAll();
		//setComboValues();
		activeSelectFields();
		activeSelectValuesType();
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	private void addFields()
	{
		int FieldsCols=4;
		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
		
        // Table with fields
		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "IfNullDialog.Fields.Label"));
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wValueTypes, margin);
		wlFields.setLayoutData(fdlFields);
		
		colinf[0]=new ColumnInfo(BaseMessages.getString(PKG, "IfNullDialog.Fieldname.Column"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[]{},false);
		colinf[1]=new ColumnInfo(BaseMessages.getString(PKG, "IfNullDialog.Value.Column"), ColumnInfo.COLUMN_TYPE_TEXT , false);
		colinf[2]=new ColumnInfo(BaseMessages.getString(PKG, "IfNullDialog.Value.ConversionMask"), ColumnInfo.COLUMN_TYPE_CCOMBO, Const.getDateFormats());
		colinf[1].setUsingVariables(true);
		colinf[3]= new ColumnInfo(BaseMessages.getString(PKG, "IfNullDialog.Value.SetEmptyString"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { BaseMessages.getString(PKG, "System.Combo.Yes"), BaseMessages.getString(PKG, "System.Combo.No") } );
			
		wFields=new TableView(transMeta, shell, 
				  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
				  colinf, 
				  FieldsRows,  
				  oldlsMod,
				  props
				  );

		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom= new FormAttachment(wOK, -2*margin);
	
		wFields.setLayoutData(fdFields);

		setComboValues();
		fieldColumns.add(colinf[0]);
		
	}
	private void activeSelectFields()
	{
		if(wSelectFields.getSelection())
		{
			wSelectValuesType.setSelection(false);
			wlValueTypes.setEnabled(false);
			wValueTypes.setEnabled(false);
		}
		activeFields();
	}
	private void activeSelectValuesType()
	{
		if(wSelectValuesType.getSelection())
		{
			wSelectFields.setSelection(false);
			wFields.setEnabled(false);
			wlFields.setEnabled(false);
		}
		activeFields();
	}
	private void activeFields()
	{
		wlFields.setEnabled(wSelectFields.getSelection());
		wFields.setEnabled(wSelectFields.getSelection());
		wGet.setEnabled(wSelectFields.getSelection());
		wlValueTypes.setEnabled(wSelectValuesType.getSelection());
		wValueTypes.setEnabled(wSelectValuesType.getSelection());
		wlReplaceByValue.setEnabled(!wSelectFields.getSelection() && !wSelectValuesType.getSelection());
		wReplaceByValue.setEnabled(!wSelectFields.getSelection() && !wSelectValuesType.getSelection());
		wlMask.setEnabled(!wSelectFields.getSelection() && !wSelectValuesType.getSelection());
		wMask.setEnabled(!wSelectFields.getSelection() && !wSelectValuesType.getSelection());
		wlSetEmptyStringAll.setEnabled(!wSelectFields.getSelection() && !wSelectValuesType.getSelection());
		wSetEmptyStringAll.setEnabled(!wSelectFields.getSelection() && !wSelectValuesType.getSelection());
	}
	private void get()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null)
			{
                TableItemInsertListener insertListener = new TableItemInsertListener() 
                {    
            	   public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
                        { 
                            return true;
                        } 
                    };
                    
                BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1 }, new int[] {}, -1, -1, insertListener);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Title"), BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"), ke);
		}

	}
	private void setComboValues() {
		Runnable fieldLoader = new Runnable() {
			public void run() {
				try {
					prevFields = transMeta.getPrevStepFields(stepname);

				} catch (KettleException e) {
					String msg = BaseMessages.getString(PKG, "IfNullDialog.DoMapping.UnableToFindInput");
					logError(msg);
				}
				String[] prevStepFieldNames = prevFields.getFieldNames();
				if(prevStepFieldNames!=null){
					Arrays.sort(prevStepFieldNames);
	
					for (int i = 0; i < fieldColumns.size(); i++) {
						ColumnInfo colInfo = (ColumnInfo) fieldColumns.get(i);
						if(colInfo!=null) colInfo.setComboValues(prevStepFieldNames);
					}
				}
			}
		};
		new Thread(fieldLoader).start();
	}
 
	public void getFirstData()
	{
		wSelectFields.setSelection(input.isSelectFields());
		wSelectValuesType.setSelection(input.isSelectValuesType());
	}
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
        if (input.getReplaceAllByValue()!= null)  wReplaceByValue.setText(input.getReplaceAllByValue());
        if (input.getReplaceAllMask()!= null)  wMask.setText(input.getReplaceAllMask());
        wSetEmptyStringAll.setSelection(input.isSetEmptyStringAll());
        
		wSelectFields.setSelection(input.isSelectFields());
		wSelectValuesType.setSelection(input.isSelectValuesType());
        
		Table table = wValueTypes.table;
		if (input.getTypeName().length>0) table.removeAll();
		for (int i=0;i<input.getTypeName().length;i++)
		{
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(0, ""+(i+1));
			if(input.getTypeName()[i]!=null) ti.setText(1, input.getTypeName()[i]);
			if(input.getTypeReplaceValue()[i]!=null) ti.setText(2, input.getTypeReplaceValue()[i]);
			if(input.getTypeReplaceMask()[i]!=null) ti.setText(3, input.getTypeReplaceMask()[i]);
			ti.setText(4, input.isSetTypeEmptyString()[i]?BaseMessages.getString(PKG, "System.Combo.Yes"):BaseMessages.getString(PKG, "System.Combo.No"));
			
		}

		wValueTypes.setRowNums();
		wValueTypes.removeEmptyRows();
		wValueTypes.optWidth(true);
        

		table = wFields.table;
		if (input.getFieldName().length>0) table.removeAll();
		for (int i=0;i<input.getFieldName().length;i++)
		{
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(0, ""+(i+1));
			if(input.getFieldName()[i]!=null) ti.setText(1, input.getFieldName()[i]);
			if(input.getReplaceValue()[i]!=null) ti.setText(2, input.getReplaceValue()[i]);
			if(input.getReplaceMask()[i]!=null) ti.setText(3, input.getReplaceMask()[i]);
			ti.setText(4, input.isSetEmptyString()[i]?BaseMessages.getString(PKG, "System.Combo.Yes"):BaseMessages.getString(PKG, "System.Combo.No"));
		}

        wFields.setRowNums();
        wValueTypes.removeEmptyRows();
		wFields.optWidth(true);
		
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	private void enableSetEmptyStringAll()
	{
		wMask.setText("");
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;
		stepname = wStepname.getText(); // return value
		
		input.setEmptyStringAll(wSetEmptyStringAll.getSelection());
		
		if(wSetEmptyStringAll.getSelection())
		{
			input.setReplaceAllByValue("");
			input.setReplaceAllMask("");
			
		}
		else
		{
			input.setReplaceAllByValue(wReplaceByValue.getText());
			input.setReplaceAllMask(wMask.getText());
		}
	

		
		input.setSelectFields(wSelectFields.getSelection());
		input.setSelectValuesType(wSelectValuesType.getSelection());
		
		int nrtypes = wValueTypes.nrNonEmpty();
		int nrfields = wFields.nrNonEmpty();
		input.allocate(nrtypes,nrfields);
		
		for (int i=0;i<nrtypes;i++)
		{
			TableItem ti = wValueTypes.getNonEmpty(i);
			input.getTypeName()[i] = ti.getText(1);
			input.isSetTypeEmptyString()[i] = BaseMessages.getString(PKG, "System.Combo.Yes").equalsIgnoreCase(ti.getText(4));
			if(input.isSetTypeEmptyString()[i])
			{
				input.getTypeReplaceValue()[i] = "";
				input.getTypeReplaceMask()[i]="";
			}
			else
			{
				input.getTypeReplaceValue()[i] = ti.getText(2);
				input.getTypeReplaceMask()[i] = ti.getText(3);
			}
			
		}
		

		for (int i=0;i<nrfields;i++)
		{
			TableItem ti = wFields.getNonEmpty(i);
			input.getFieldName()[i] = ti.getText(1);
			input.isSetEmptyString()[i] = BaseMessages.getString(PKG, "System.Combo.Yes").equalsIgnoreCase(ti.getText(4));
			if(input.isSetEmptyString()[i])
			{
				input.getReplaceValue()[i] ="";
				input.getReplaceMask()[i] = "";
			}
			else
			{
				input.getReplaceValue()[i] = ti.getText(2);
				input.getReplaceMask()[i] = ti.getText(3);
			}
		}
		dispose();
	}
}
