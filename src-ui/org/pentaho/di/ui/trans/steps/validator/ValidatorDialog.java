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

package org.pentaho.di.ui.trans.steps.validator;

import java.util.Collection;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.validator.Messages;
import org.pentaho.di.trans.steps.validator.ValidatorField;
import org.pentaho.di.trans.steps.validator.ValidatorMeta;
import org.pentaho.di.ui.core.dialog.EnterStringDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class ValidatorDialog extends BaseStepDialog implements StepDialogInterface
{
	private ValidatorMeta input;
	private List wFieldList;
	private RowMetaInterface inputFields;
	
	private ValidatorField selectedField;
	private Label wlNullAllowed;
	private Button wNullAllowed;
	private HashMap<String, ValidatorField> selectionMap;
	private Label wlMaxLength;
	private Text wMaxLength;
	private Label wlMinLength;
	private Text wMinLength;
	private Group wgData;
	private Group wgType;
	private Label wlDataTypeVerified;
	private Button wDataTypeVerified;
	private Label wlDataType;
	private Combo wDataType;
	private Label wlConversionMask;
	private Text wConversionMask;
	private Label wlDecimalSymbol;
	private Text wDecimalSymbol;
	private Label wlGroupingSymbol;
	private Text wGroupingSymbol;
	private Label wlMaxValue;
	private Text wMaxValue;
	private Label wlMinValue;
	private Text wMinValue;
	private Label wlAllowedValues;
	private List wAllowedValues;
	private Button wbAddAllowed;
	private Button wbRemoveAllowed;
	private Button wClear;

	public ValidatorDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(ValidatorMeta)in;
		
		selectedField = null;
		selectionMap = new HashMap<String, ValidatorField>();
		
		// Copy the data from the input into the map...
		//
		for (ValidatorField field : input.getValidatorField()) {
			selectionMap.put(field.getName(), field.clone());
		}
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
		shell.setText(Messages.getString("ValidatorDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		//
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("ValidatorDialog.Stepname.Label")); //$NON-NLS-1$
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
		
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wClear=new Button(shell, SWT.PUSH);
		wClear.setText(Messages.getString("ValidatorDialog.ClearButton.Label")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$
		
		setButtonPositions(new Button[] { wOK, wClear, wCancel }, margin, null);

		// List of fields to the left...
		//
		Label wlFieldList = new Label(shell, SWT.LEFT);
		wlFieldList.setText(Messages.getString("ValidatorDialog.FieldList.Label")); //$NON-NLS-1$
 		props.setLook(wlFieldList);
		FormData fdlFieldList = new FormData();
		fdlFieldList.left = new FormAttachment(0, 0);
		fdlFieldList.right= new FormAttachment(100, 0);
		fdlFieldList.top  = new FormAttachment(wStepname, margin);
		wlFieldList.setLayoutData(fdlFieldList);
		wFieldList=new List(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		
		// TODO: grab field list in thread in the background...
		//
		try {
			inputFields = transMeta.getPrevStepFields(stepMeta);
			wFieldList.setItems(inputFields.getFieldNames());
		} catch (KettleStepException ex) {
			new ErrorDialog(shell, Messages.getString("ValidatorDialog.Exception.CantGetFieldsFromPreviousSteps.Title"), Messages.getString("ValidatorDialog.Exception.CantGetFieldsFromPreviousSteps.Message"), ex);
		}
		
 		props.setLook(wFieldList);
 		wFieldList.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent event) {
				showSelectedValidatorField(wFieldList.getSelection()[0]);
			}
		
		});
		FormData fdFieldList = new FormData();
		fdFieldList.left   = new FormAttachment(0, 0);
		fdFieldList.top    = new FormAttachment(wlFieldList, margin);
		fdFieldList.right  = new FormAttachment(middle, -margin);
		fdFieldList.bottom = new FormAttachment(wOK, -margin*2);
		wFieldList.setLayoutData(fdFieldList);
		
		// Create a scrolled composite on the right side...
		//
		ScrolledComposite wSComp = new ScrolledComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		props.setLook(wSComp);
		wSComp.setLayout(new FillLayout());
		FormData fdComp = new FormData();
		fdComp.left   = new FormAttachment(middle, 0);
		fdComp.top    = new FormAttachment(wlFieldList, margin);
		fdComp.right  = new FormAttachment(100, 0);
		fdComp.bottom = new FormAttachment(wOK, -margin*2);
		wSComp.setLayoutData(fdComp);
		
		Composite wComp = new Composite(wSComp, SWT.NONE);
		props.setLook(wComp);
        FormLayout compLayout = new FormLayout();
        compLayout.marginWidth  = 3;
        compLayout.marginHeight = 3;
        wComp.setLayout(compLayout);
		
		
		// Data type validations & constants masks...
		// 
		wgType = new Group(wComp, SWT.NONE);
		props.setLook(wgType);
		wgType.setText(Messages.getString("ValidatorDialog.TypeGroup.Label"));
		FormLayout typeGroupLayout = new FormLayout();
		typeGroupLayout.marginHeight = Const.FORM_MARGIN;
		typeGroupLayout.marginWidth = Const.FORM_MARGIN;
		wgType.setLayout(typeGroupLayout);
		FormData fdType = new FormData();
		fdType.left = new FormAttachment(0, 0);
		fdType.right= new FormAttachment(100, 0);
		fdType.top  = new FormAttachment(0, 0);
		wgType.setLayoutData(fdType);
		
		// Check for data type correctness?
		//
		wlDataTypeVerified=new Label(wgType, SWT.RIGHT);
		wlDataTypeVerified.setText(Messages.getString("ValidatorDialog.DataTypeVerified.Label")); //$NON-NLS-1$
 		props.setLook(wlDataTypeVerified);
		FormData fdldataTypeVerified = new FormData();
		fdldataTypeVerified.left = new FormAttachment(0, 0);
		fdldataTypeVerified.right= new FormAttachment(middle, -margin);
		fdldataTypeVerified.top  = new FormAttachment(wlFieldList, margin);
		wlDataTypeVerified.setLayoutData(fdldataTypeVerified);
		wDataTypeVerified=new Button(wgType, SWT.CHECK);
 		props.setLook(wDataTypeVerified);
		FormData fddataTypeVerified = new FormData();
		fddataTypeVerified.left = new FormAttachment(middle, margin);
		fddataTypeVerified.right= new FormAttachment(100, 0);
		fddataTypeVerified.top  = new FormAttachment(wlFieldList, margin);
		wDataTypeVerified.setLayoutData(fddataTypeVerified);

		// Data type
		//
		wlDataType=new Label(wgType, SWT.RIGHT);
		wlDataType.setText(Messages.getString("ValidatorDialog.DataType.Label")); //$NON-NLS-1$
 		props.setLook(wlDataType);
		FormData fdlDataType = new FormData();
		fdlDataType.left = new FormAttachment(0, 0);
		fdlDataType.right= new FormAttachment(middle, -margin);
		fdlDataType.top  = new FormAttachment(wDataTypeVerified, margin);
		wlDataType.setLayoutData(fdlDataType);
		wDataType=new Combo(wgType, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wDataType.setItems(ValueMeta.getTypes());
 		props.setLook(wDataType);
		FormData fdDataType = new FormData();
		fdDataType.left = new FormAttachment(middle, margin);
		fdDataType.right= new FormAttachment(100, 0);
		fdDataType.top  = new FormAttachment(wDataTypeVerified, margin);
		wDataType.setLayoutData(fdDataType);
		
		// Conversion mask
		//
		wlConversionMask=new Label(wgType, SWT.RIGHT);
		wlConversionMask.setText(Messages.getString("ValidatorDialog.ConversionMask.Label")); //$NON-NLS-1$
 		props.setLook(wlConversionMask);
		FormData fdlConversionMask = new FormData();
		fdlConversionMask.left = new FormAttachment(0, 0);
		fdlConversionMask.right= new FormAttachment(middle, -margin);
		fdlConversionMask.top  = new FormAttachment(wDataType, margin);
		wlConversionMask.setLayoutData(fdlConversionMask);
		wConversionMask=new Text(wgType, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wConversionMask);
		FormData fdConversionMask = new FormData();
		fdConversionMask.left = new FormAttachment(middle, margin);
		fdConversionMask.right= new FormAttachment(100, 0);
		fdConversionMask.top  = new FormAttachment(wDataType, margin);
		wConversionMask.setLayoutData(fdConversionMask);

		
		// Decimal Symbol
		//
		wlDecimalSymbol=new Label(wgType, SWT.RIGHT);
		wlDecimalSymbol.setText(Messages.getString("ValidatorDialog.DecimalSymbol.Label")); //$NON-NLS-1$
 		props.setLook(wlDecimalSymbol);
		FormData fdlDecimalSymbol = new FormData();
		fdlDecimalSymbol.left = new FormAttachment(0, 0);
		fdlDecimalSymbol.right= new FormAttachment(middle, -margin);
		fdlDecimalSymbol.top  = new FormAttachment(wConversionMask, margin);
		wlDecimalSymbol.setLayoutData(fdlDecimalSymbol);
		wDecimalSymbol=new Text(wgType, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wDecimalSymbol);
		FormData fdDecimalSymbol = new FormData();
		fdDecimalSymbol.left = new FormAttachment(middle, margin);
		fdDecimalSymbol.right= new FormAttachment(100, 0);
		fdDecimalSymbol.top  = new FormAttachment(wConversionMask, margin);
		wDecimalSymbol.setLayoutData(fdDecimalSymbol);
		
		// Grouping Symbol
		//
		wlGroupingSymbol=new Label(wgType, SWT.RIGHT);
		wlGroupingSymbol.setText(Messages.getString("ValidatorDialog.GroupingSymbol.Label")); //$NON-NLS-1$
 		props.setLook(wlGroupingSymbol);
		FormData fdlGroupingSymbol = new FormData();
		fdlGroupingSymbol.left = new FormAttachment(0, 0);
		fdlGroupingSymbol.right= new FormAttachment(middle, -margin);
		fdlGroupingSymbol.top  = new FormAttachment(wDecimalSymbol, margin);
		wlGroupingSymbol.setLayoutData(fdlGroupingSymbol);
		wGroupingSymbol=new Text(wgType, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wGroupingSymbol);
		FormData fdGroupingSymbol = new FormData();
		fdGroupingSymbol.left = new FormAttachment(middle, margin);
		fdGroupingSymbol.right= new FormAttachment(100, 0);
		fdGroupingSymbol.top  = new FormAttachment(wDecimalSymbol, margin);
		wGroupingSymbol.setLayoutData(fdGroupingSymbol);
		
		
		
		
		
		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////
		//
		// The data group...
		//
		//
		wgData = new Group(wComp, SWT.NONE);
		props.setLook(wgData);
		wgData.setText(Messages.getString("ValidatorDialog.DataGroup.Label"));
		FormLayout dataGroupLayout = new FormLayout();
		dataGroupLayout.marginHeight = Const.FORM_MARGIN;
		dataGroupLayout.marginWidth = Const.FORM_MARGIN;
		wgData.setLayout(dataGroupLayout);
		FormData fdData = new FormData();
		fdData.left = new FormAttachment(0, 0);
		fdData.right= new FormAttachment(100, 0);
		fdData.top  = new FormAttachment(wgType, margin);
		wgData.setLayoutData(fdData);
		
		// Check for null?
		//
		wlNullAllowed=new Label(wgData, SWT.RIGHT);
		wlNullAllowed.setText(Messages.getString("ValidatorDialog.NullAllowed.Label")); //$NON-NLS-1$
 		props.setLook(wlNullAllowed);
		FormData fdlNullAllowed = new FormData();
		fdlNullAllowed.left = new FormAttachment(0, 0);
		fdlNullAllowed.right= new FormAttachment(middle, -margin);
		fdlNullAllowed.top  = new FormAttachment(wlFieldList, margin);
		wlNullAllowed.setLayoutData(fdlNullAllowed);
		wNullAllowed=new Button(wgData, SWT.CHECK);
 		props.setLook(wNullAllowed);
		FormData fdNullAllowed = new FormData();
		fdNullAllowed.left = new FormAttachment(middle, margin);
		fdNullAllowed.right= new FormAttachment(100, 0);
		fdNullAllowed.top  = new FormAttachment(wlFieldList, margin);
		wNullAllowed.setLayoutData(fdNullAllowed);

		// Maximum length
		//
		wlMaxLength=new Label(wgData, SWT.RIGHT);
		wlMaxLength.setText(Messages.getString("ValidatorDialog.MaxLength.Label")); //$NON-NLS-1$
 		props.setLook(wlMaxLength);
		FormData fdlMaxLength = new FormData();
		fdlMaxLength.left = new FormAttachment(0, 0);
		fdlMaxLength.right= new FormAttachment(middle, -margin);
		fdlMaxLength.top  = new FormAttachment(wNullAllowed, margin);
		wlMaxLength.setLayoutData(fdlMaxLength);
		wMaxLength=new Text(wgData, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMaxLength);
		FormData fdMaxLength = new FormData();
		fdMaxLength.left = new FormAttachment(middle, margin);
		fdMaxLength.right= new FormAttachment(100, 0);
		fdMaxLength.top  = new FormAttachment(wNullAllowed, margin);
		wMaxLength.setLayoutData(fdMaxLength);
		
		// Minimum length
		//
		wlMinLength=new Label(wgData, SWT.RIGHT);
		wlMinLength.setText(Messages.getString("ValidatorDialog.MinLength.Label")); //$NON-NLS-1$
 		props.setLook(wlMinLength);
		FormData fdlMinLength = new FormData();
		fdlMinLength.left = new FormAttachment(0, 0);
		fdlMinLength.right= new FormAttachment(middle, -margin);
		fdlMinLength.top  = new FormAttachment(wMaxLength, margin);
		wlMinLength.setLayoutData(fdlMinLength);
		wMinLength=new Text(wgData, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMinLength);
		FormData fdMinLength = new FormData();
		fdMinLength.left = new FormAttachment(middle, margin);
		fdMinLength.right= new FormAttachment(100, 0);
		fdMinLength.top  = new FormAttachment(wMaxLength, margin);
		wMinLength.setLayoutData(fdMinLength);

		// Maximum value
		//
		wlMaxValue=new Label(wgData, SWT.RIGHT);
		wlMaxValue.setText(Messages.getString("ValidatorDialog.MaxValue.Label")); //$NON-NLS-1$
 		props.setLook(wlMaxValue);
		FormData fdlMaxValue = new FormData();
		fdlMaxValue.left = new FormAttachment(0, 0);
		fdlMaxValue.right= new FormAttachment(middle, -margin);
		fdlMaxValue.top  = new FormAttachment(wMinLength, margin);
		wlMaxValue.setLayoutData(fdlMaxValue);
		wMaxValue=new Text(wgData, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMaxValue);
		FormData fdMaxValue = new FormData();
		fdMaxValue.left = new FormAttachment(middle, margin);
		fdMaxValue.right= new FormAttachment(100, 0);
		fdMaxValue.top  = new FormAttachment(wMinLength, margin);
		wMaxValue.setLayoutData(fdMaxValue);
		
		// Minimum value
		//
		wlMinValue=new Label(wgData, SWT.RIGHT);
		wlMinValue.setText(Messages.getString("ValidatorDialog.MinValue.Label")); //$NON-NLS-1$
 		props.setLook(wlMinValue);
		FormData fdlMinValue = new FormData();
		fdlMinValue.left = new FormAttachment(0, 0);
		fdlMinValue.right= new FormAttachment(middle, -margin);
		fdlMinValue.top  = new FormAttachment(wMaxValue, margin);
		wlMinValue.setLayoutData(fdlMinValue);
		wMinValue=new Text(wgData, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMinValue);
		FormData fdMinValue = new FormData();
		fdMinValue.left = new FormAttachment(middle, margin);
		fdMinValue.right= new FormAttachment(100, 0);
		fdMinValue.top  = new FormAttachment(wMaxValue, margin);
		wMinValue.setLayoutData(fdMinValue);
		
		// Allowed values: a list box.
		//
		// Add an entry
		wbAddAllowed = new Button(wgData, SWT.PUSH);
		// props.setLook(wbAddAllowed);
		wbAddAllowed.setText(Messages.getString("ValidatorDialog.ButtonAddAllowed.Label")); //$NON-NLS-1$
		FormData fdbAddAllowed = new FormData();
		fdbAddAllowed.right  = new FormAttachment(100, 0);
		fdbAddAllowed.top    = new FormAttachment(wMinValue, margin);
		wbAddAllowed.setLayoutData(fdbAddAllowed);
		wbAddAllowed.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { addAllowedValue(); } });

		// Remove an entry
		wbRemoveAllowed = new Button(wgData, SWT.PUSH);
		// props.setLook(wbRemoveAllowed);
		wbRemoveAllowed.setText(Messages.getString("ValidatorDialog.ButtonRemoveAllowed.Label")); //$NON-NLS-1$
		FormData fdbRemoveAllowed = new FormData();
		fdbRemoveAllowed.right  = new FormAttachment(100, 0);
		fdbRemoveAllowed.top    = new FormAttachment(wbAddAllowed, margin);
		wbRemoveAllowed.setLayoutData(fdbRemoveAllowed);
		wbRemoveAllowed.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { removeAllowedValue(); } });

		
		wlAllowedValues=new Label(wgData, SWT.RIGHT);
		wlAllowedValues.setText(Messages.getString("ValidatorDialog.AllowedValues.Label")); //$NON-NLS-1$
 		props.setLook(wlAllowedValues);
		FormData fdlAllowedValues = new FormData();
		fdlAllowedValues.left = new FormAttachment(0, 0);
		fdlAllowedValues.right= new FormAttachment(middle, -margin);
		fdlAllowedValues.top  = new FormAttachment(wMinValue, margin);
		wlAllowedValues.setLayoutData(fdlAllowedValues);
		wAllowedValues=new List(wgData, SWT.MULTI | SWT.LEFT | SWT.BORDER);
 		props.setLook(wAllowedValues);
 		FormData fdAllowedValues = new FormData();
 		fdAllowedValues.left   = new FormAttachment(middle, margin);
 		fdAllowedValues.right  = new FormAttachment(wbRemoveAllowed, -margin);
 		fdAllowedValues.top    = new FormAttachment(wMinValue, margin);
 		fdAllowedValues.bottom = new FormAttachment(wMinValue, 200);
 		wAllowedValues.setLayoutData(fdAllowedValues);



		
        wComp.pack();
        Rectangle bounds = wComp.getBounds();
		
        wSComp.setContent(wComp);
        wSComp.setExpandHorizontal(true);
        wSComp.setExpandVertical(true);
        wSComp.setMinWidth(bounds.width);
        wSComp.setMinHeight(bounds.height);


		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		wClear.addSelectionListener(new SelectionAdapter() {
		
			public void widgetSelected(SelectionEvent e) {
				// Clear the validation rules for a certain field...
				//
				int index = wFieldList.getSelectionIndex();
				if (index>=0) {
					String fieldName = wFieldList.getItem(index);
					selectionMap.remove(fieldName);
					selectedField=null;
					wFieldList.deselectAll();
					enableFields();
				}
			}
		
		});
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


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
	
	/**
	 * Remove the selected entries from the allowed entries
	 */
	protected void removeAllowedValue() {
		String[] selection = wAllowedValues.getSelection();
		for (String string : selection) {
			wAllowedValues.remove(string);
		}
	}

	/**
	 * Add one entry to the list of allowed values...
	 */
	protected void addAllowedValue() {
		EnterStringDialog dialog = new EnterStringDialog(shell, "", Messages.getString("ValidatorDialog.Dialog.AddAllowedValue.Title"), Messages.getString("ValidatorDialog.Dialog.AddAllowedValue.Message"));
		String value = dialog.open();
		if (!Const.isEmpty(value)) {
			wAllowedValues.add(value);
		}
	}

	private void getValidatorFieldData(ValidatorField field) {
		wDataTypeVerified.setSelection(field.isDataTypeVerified());
		wDataType.setText(ValueMeta.getTypeDesc(field.getDataType()));
		wConversionMask.setText(Const.NVL(field.getConversionMask(), ""));
		wGroupingSymbol.setText(Const.NVL(field.getGroupingSymbol(), ""));
		wDecimalSymbol.setText(Const.NVL(field.getDecimalSymbol(), ""));
		
		wNullAllowed.setSelection(field.isNullAllowed());
		if (field.getMaximumLength()>=0) wMaxLength.setText(Integer.toString(field.getMaximumLength())); else wMaxLength.setText(""); 
		if (field.getMinimumLength()>=0) wMinLength.setText(Integer.toString(field.getMinimumLength())); else wMinLength.setText(""); 
		wMaxValue.setText(Const.NVL(field.getMaximumValue(), ""));
		wMinValue.setText(Const.NVL(field.getMinimumValue(), ""));
		
		wAllowedValues.removeAll();
		if (field.getAllowedValues()!=null) {
			for (String allowedValue : field.getAllowedValues()) {
				wAllowedValues.add(Const.NVL(allowedValue, ""));
			}
		}
	}
	
	private void enableFields() {
		boolean visible = selectedField!=null;
		
		wgType.setVisible(visible);
		wgData.setVisible(visible);
		
		wlDataTypeVerified.setVisible(visible);
		wDataTypeVerified.setVisible(visible);
		wlDataType.setVisible(visible);
		wDataType.setVisible(visible);
		wlConversionMask.setVisible(visible);
		wConversionMask.setVisible(visible);
		wlGroupingSymbol.setVisible(visible);
		wGroupingSymbol.setVisible(visible);
		wlDecimalSymbol.setVisible(visible);
		wDecimalSymbol.setVisible(visible);
		
		wlNullAllowed.setVisible(visible);
		wNullAllowed.setVisible(visible);
		wlMaxLength.setVisible(visible);
		wMaxLength.setVisible(visible);
		wlMinLength.setVisible(visible);
		wMinLength.setVisible(visible);
		wlMaxValue.setVisible(visible);
		wMaxValue.setVisible(visible);
		wlMinValue.setVisible(visible);
		wMinValue.setVisible(visible);
	}

	private void showSelectedValidatorField(String selection) {
		// Someone hit a field...
		//
		saveChanges();
		
		ValidatorField field = selectionMap.get(selection);
		if (field==null) {
			field = new ValidatorField(selection);
		}
		
		selectedField = field;
		
		getValidatorFieldData(selectedField);
		
		enableFields();
	}

	private void saveChanges() {
		if (selectedField!=null) {
			// First grab the info from the dialog...
			// 
			selectedField.setDataTypeVerified(wDataTypeVerified.getSelection());
			selectedField.setDataType(ValueMeta.getType(wDataType.getText()));
			
			selectedField.setNullAllowed(wNullAllowed.getSelection());
			selectedField.setMaximumLength(Const.toInt(wMaxLength.getText(), -1));
			selectedField.setMinimumLength(Const.toInt(wMinLength.getText(), -1));
			selectedField.setMaximumValue(wMaxValue.getText());
			selectedField.setMinimumValue(wMinValue.getText());
			
			selectedField.setAllowedValues(wAllowedValues.getItems());

			// Save the old info in the map
			// 
			selectionMap.put(selectedField.getName(), selectedField);
		}
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wStepname.selectAll();
		enableFields();
		
		// Select the first available field...
		//
		if (input.getValidatorField().length>0) {
			ValidatorField validatorField = input.getValidatorField()[0];
			String name = validatorField.getName();
			int index = wFieldList.indexOf(name);
			if (index>=0) {
				wFieldList.select(index);
				showSelectedValidatorField(name);
			}
		}
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
		
		saveChanges();
		input.setChanged();
		Collection<ValidatorField> values = selectionMap.values();
		ValidatorField[] fields = values.toArray(new ValidatorField[values.size()]);
		input.setValidatorField(fields);
		
		dispose();
	}
}
