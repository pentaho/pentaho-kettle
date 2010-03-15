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

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.steps.validator.Validation;
import org.pentaho.di.trans.steps.validator.ValidatorMeta;
import org.pentaho.di.ui.core.dialog.EnterStringDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.warning.SimpleWarningMessage;
import org.pentaho.di.ui.core.widget.warning.SupportsWarningInterface;
import org.pentaho.di.ui.core.widget.warning.TextVarWarning;
import org.pentaho.di.ui.core.widget.warning.WarningInterface;
import org.pentaho.di.ui.core.widget.warning.WarningMessageInterface;
import org.pentaho.di.ui.core.widget.warning.WarningText;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class ValidatorDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = ValidatorMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private ValidatorMeta input;
	private List wValidationsList;
	private RowMetaInterface inputFields;

	private Button wValidateAll;
	
	private Validation selectedField;

	private Label wlDescription;
	private Text wDescription;

	private Label wlFieldName;
	private CCombo wFieldName;

	private Label wlNullAllowed;
	private Button wNullAllowed;

	private Label wlOnlyNullAllowed;
	private Button wOnlyNullAllowed;

	private Label wlOnlyNumeric;
	private Button wOnlyNumeric;

	private java.util.List<Validation> selectionList;
	private Label wlMaxLength;
	private WarningText wMaxLength;
	private Label wlMinLength;
	private WarningText wMinLength;
	private Group wgData;
	private Group wgType;
	private Label wlDataTypeVerified;
	private Button wDataTypeVerified;
	private Label wlDataType;
	private Combo wDataType;
	private Label wlConversionMask;
	private WarningText wConversionMask;
	private Label wlDecimalSymbol;
	private WarningText wDecimalSymbol;
	private Label wlGroupingSymbol;
	private WarningText wGroupingSymbol;
	private Label wlMaxValue;
	private WarningText wMaxValue;
	private Label wlMinValue;
	private WarningText wMinValue;
	private Label wlAllowedValues;
	private List wAllowedValues;
	private Label wlSourceValues;
	private Button wSourceValues;
	private Label wlSourceStep;
	private Combo wSourceStep;
	private Label wlSourceField;
	private Combo wSourceField;
	
	
	private Button wbAddAllowed;
	private Button wbRemoveAllowed;
	
	private Button wClear;
	private Button wNew;
	
	private Label wlStartStringExpected;
	private WarningText wStartStringExpected;
	private Label wlEndStringExpected;
	private WarningText wEndStringExpected;
	private Label wlStartStringDisallowed;
	private WarningText wStartStringDisallowed;
	private Label wlEndStringDisallowed;
	private WarningText wEndStringDisallowed;
	private Label wlRegExpExpected;
	private WarningText wRegExpExpected;
	private Label wlRegExpDisallowed;
	private WarningText wRegExpDisallowed;
	private Label wlErrorCode;
	private TextVarWarning wErrorCode;
	private Label wlErrorDescription;
	private TextVarWarning wErrorDescription;
	private Button	wConcatErrors;
	private Text	wConcatSeparator;

	public ValidatorDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(ValidatorMeta)in;

		// Just to make sure everything is nicely in sync...
		//
		java.util.List<StreamInterface> infoStreams = input.getStepIOMeta().getInfoStreams();
		for (int i=0;i<infoStreams.size();i++) {
			input.getValidations().get(i).setSourcingStepName(infoStreams.get(i).getStepname());
		}

		selectedField = null;
		selectionList = new ArrayList<Validation>();

		// Copy the data from the input into the map...
		//
		for (Validation field : input.getValidations()) {
			selectionList.add(field.clone());
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
		shell.setText(BaseMessages.getString(PKG, "ValidatorDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		Rectangle imageBounds = GUIResource.getInstance().getImageInfoHop().getBounds();
		
		// Stepname line
		//
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "ValidatorDialog.Stepname.Label")); //$NON-NLS-1$
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
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wNew=new Button(shell, SWT.PUSH);
		wNew.setText(BaseMessages.getString(PKG, "ValidatorDialog.NewButton.Label")); //$NON-NLS-1$
		wClear=new Button(shell, SWT.PUSH);
		wClear.setText(BaseMessages.getString(PKG, "ValidatorDialog.ClearButton.Label")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$
		
		setButtonPositions(new Button[] { wOK, wNew, wClear, wCancel }, margin, null);

		// List of fields to the left...
		//
		Label wlFieldList = new Label(shell, SWT.LEFT);
		wlFieldList.setText(BaseMessages.getString(PKG, "ValidatorDialog.FieldList.Label")); //$NON-NLS-1$
 		props.setLook(wlFieldList);
		FormData fdlFieldList = new FormData();
		fdlFieldList.left = new FormAttachment(0, 0);
		fdlFieldList.right= new FormAttachment(middle, -margin);
		fdlFieldList.top  = new FormAttachment(wStepname, margin);
		wlFieldList.setLayoutData(fdlFieldList);
		wValidationsList=new List(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		
 		props.setLook(wValidationsList);
 		wValidationsList.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent event) {
				showSelectedValidatorField(wValidationsList.getSelection()[0]);
			}
		});
 		
		FormData fdFieldList = new FormData();
		fdFieldList.left   = new FormAttachment(0, 0);
		fdFieldList.top    = new FormAttachment(wlFieldList, margin);
		fdFieldList.right  = new FormAttachment(middle, -margin);
		fdFieldList.bottom = new FormAttachment(wOK, -margin*2);
		wValidationsList.setLayoutData(fdFieldList);

		// General: an option to allow ALL the options to be checked.
		//
		wValidateAll=new Button(shell, SWT.CHECK);
		wValidateAll.setText(BaseMessages.getString(PKG, "ValidatorDialog.ValidateAll.Label")); //$NON-NLS-1$
 		props.setLook(wValidateAll);
		FormData fdValidateAll = new FormData();
		fdValidateAll.left = new FormAttachment(middle, 0);
		fdValidateAll.right= new FormAttachment(100, 0);
		fdValidateAll.top  = new FormAttachment(wStepname, margin);
		wValidateAll.setLayoutData(fdValidateAll);
		wValidateAll.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent arg0) { setFlags(); } });

		// General: When validating all options, still output a single row, errors concatenated
		//
		wConcatErrors=new Button(shell, SWT.CHECK);
		wConcatErrors.setText(BaseMessages.getString(PKG, "ValidatorDialog.ConcatErrors.Label")); //$NON-NLS-1$
 		props.setLook(wConcatErrors);
		FormData fdConcatErrors = new FormData();
		fdConcatErrors.left = new FormAttachment(middle, 0);
		fdConcatErrors.top  = new FormAttachment(wValidateAll, margin);
		wConcatErrors.setLayoutData(fdConcatErrors);

		// The separator
		//
		wConcatSeparator=new Text(shell, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
 		props.setLook(wConcatSeparator);
		FormData fdConcatSeparator = new FormData();
		fdConcatSeparator.left = new FormAttachment(wConcatErrors, margin);
		fdConcatSeparator.right= new FormAttachment(100, 0);
		fdConcatSeparator.top  = new FormAttachment(wValidateAll, margin);
		wConcatSeparator.setLayoutData(fdConcatSeparator);

		
		// Create a scrolled composite on the right side...
		//
		ScrolledComposite wSComp = new ScrolledComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		props.setLook(wSComp);
		wSComp.setLayout(new FillLayout());
		FormData fdComp = new FormData();
		fdComp.left   = new FormAttachment(middle, 0);
		fdComp.top    = new FormAttachment(wConcatSeparator, 2*margin);
		fdComp.right  = new FormAttachment(100, 0);
		fdComp.bottom = new FormAttachment(wOK, -margin*2);
		wSComp.setLayoutData(fdComp);
		
		Composite wComp = new Composite(wSComp, SWT.BORDER);
		props.setLook(wComp);
        FormLayout compLayout = new FormLayout();
        compLayout.marginWidth  = 3;
        compLayout.marginHeight = 3;
        wComp.setLayout(compLayout);
        
        int extra = imageBounds.width;
		
		// Description (list key)
		//
		wlDescription=new Label(wComp, SWT.RIGHT);
		wlDescription.setText(BaseMessages.getString(PKG, "ValidatorDialog.Description.Label")); //$NON-NLS-1$
 		props.setLook(wlDescription);
		FormData fdlDescription = new FormData();
		fdlDescription.left = new FormAttachment(0, 0);
		fdlDescription.right= new FormAttachment(middle, -margin);
		fdlDescription.top  = new FormAttachment(0, 0);
		wlDescription.setLayoutData(fdlDescription);
		wDescription=new Text(wComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wDescription);
		FormData fdDescription = new FormData();
		fdDescription.left = new FormAttachment(middle, margin+extra);
		fdDescription.right= new FormAttachment(100, 0);
		fdDescription.top  = new FormAttachment(0, 0);
		wDescription.setLayoutData(fdDescription);
		wDescription.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				// See if there is a selected Validation
				//
				if (wValidationsList!=null && wValidationsList.getItemCount()>0 && wValidationsList.getSelection().length==1) {
					int index = wValidationsList.getSelectionIndex();
					String description = wValidationsList.getItem(index);
					Validation validation = Validation.findValidation(selectionList, description);
					String newDescription = wDescription.getText();
					validation.setName(newDescription);
					wValidationsList.setItem(index, newDescription);
					wValidationsList.select(index);
				}
			}
		});

		// The name of the field to validate
		//
		wlFieldName=new Label(wComp, SWT.RIGHT);
		wlFieldName.setText(BaseMessages.getString(PKG, "ValidatorDialog.FieldName.Label")); //$NON-NLS-1$
 		props.setLook(wlFieldName);
		FormData fdlFieldName = new FormData();
		fdlFieldName.left = new FormAttachment(0, 0);
		fdlFieldName.right= new FormAttachment(middle, -margin);
		fdlFieldName.top  = new FormAttachment(wDescription, margin);
		wlFieldName.setLayoutData(fdlFieldName);
		wFieldName=new CCombo(wComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFieldName);
		FormData fdFieldName = new FormData();
		fdFieldName.left = new FormAttachment(middle, margin+extra);
		fdFieldName.right= new FormAttachment(100, 0);
		fdFieldName.top  = new FormAttachment(wDescription, margin);
		wFieldName.setLayoutData(fdFieldName);
		
		// TODO: grab field list in thread in the background...
		//
		try {
			inputFields = transMeta.getPrevStepFields(stepMeta);
			wFieldName.setItems(inputFields.getFieldNames());
		} catch (KettleStepException ex) {
			new ErrorDialog(shell, BaseMessages.getString(PKG, "ValidatorDialog.Exception.CantGetFieldsFromPreviousSteps.Title"), BaseMessages.getString(PKG, "ValidatorDialog.Exception.CantGetFieldsFromPreviousSteps.Message"), ex);
		}
		
		// ErrorCode
		//
		wlErrorCode=new Label(wComp, SWT.RIGHT);
		wlErrorCode.setText(BaseMessages.getString(PKG, "ValidatorDialog.ErrorCode.Label")); //$NON-NLS-1$
 		props.setLook(wlErrorCode);
		FormData fdlErrorCode = new FormData();
		fdlErrorCode.left = new FormAttachment(0, 0);
		fdlErrorCode.right= new FormAttachment(middle, -margin);
		fdlErrorCode.top  = new FormAttachment(wFieldName, margin);
		wlErrorCode.setLayoutData(fdlErrorCode);
		wErrorCode=new TextVarWarning(transMeta, wComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wErrorCode);
		FormData fdErrorCode = new FormData();
		fdErrorCode.left = new FormAttachment(middle, margin);
		fdErrorCode.right= new FormAttachment(100, 0);
		fdErrorCode.top  = new FormAttachment(wFieldName, margin);
		wErrorCode.setLayoutData(fdErrorCode);
		addSpacesWarning(wErrorCode);

		// ErrorDescription
		//
		wlErrorDescription=new Label(wComp, SWT.RIGHT);
		wlErrorDescription.setText(BaseMessages.getString(PKG, "ValidatorDialog.ErrorDescription.Label")); //$NON-NLS-1$
 		props.setLook(wlErrorDescription);
		FormData fdlErrorDescription = new FormData();
		fdlErrorDescription.left = new FormAttachment(0, 0);
		fdlErrorDescription.right= new FormAttachment(middle, -margin);
		fdlErrorDescription.top  = new FormAttachment(wErrorCode, margin);
		wlErrorDescription.setLayoutData(fdlErrorDescription);
		wErrorDescription=new TextVarWarning(transMeta, wComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wErrorDescription);
		FormData fdErrorDescription = new FormData();
		fdErrorDescription.left = new FormAttachment(middle, margin);
		fdErrorDescription.right= new FormAttachment(100, 0);
		fdErrorDescription.top  = new FormAttachment(wErrorCode, margin);
		wErrorDescription.setLayoutData(fdErrorDescription);
		addSpacesWarning(wErrorDescription);

		// Data type validations & constants masks...
		// 
		wgType = new Group(wComp, SWT.NONE);
		props.setLook(wgType);
		wgType.setText(BaseMessages.getString(PKG, "ValidatorDialog.TypeGroup.Label"));
		FormLayout typeGroupLayout = new FormLayout();
		typeGroupLayout.marginHeight = Const.FORM_MARGIN;
		typeGroupLayout.marginWidth = Const.FORM_MARGIN;
		wgType.setLayout(typeGroupLayout);
		FormData fdType = new FormData();
		fdType.left = new FormAttachment(0, 0);
		fdType.right= new FormAttachment(100, 0);
		fdType.top  = new FormAttachment(wErrorDescription, margin*2);
		wgType.setLayoutData(fdType);
		
		// Check for data type correctness?
		//
		wlDataTypeVerified=new Label(wgType, SWT.RIGHT);
		wlDataTypeVerified.setText(BaseMessages.getString(PKG, "ValidatorDialog.DataTypeVerified.Label")); //$NON-NLS-1$
 		props.setLook(wlDataTypeVerified);
		FormData fdldataTypeVerified = new FormData();
		fdldataTypeVerified.left = new FormAttachment(0, 0);
		fdldataTypeVerified.right= new FormAttachment(middle, -margin);
		fdldataTypeVerified.top  = new FormAttachment(0, 0);
		wlDataTypeVerified.setLayoutData(fdldataTypeVerified);
		wDataTypeVerified=new Button(wgType, SWT.CHECK);
 		props.setLook(wDataTypeVerified);
		FormData fddataTypeVerified = new FormData();
		fddataTypeVerified.left = new FormAttachment(middle, margin+extra);
		fddataTypeVerified.right= new FormAttachment(100, 0);
		fddataTypeVerified.top  = new FormAttachment(0, 0);
		wDataTypeVerified.setLayoutData(fddataTypeVerified);

		// Data type
		//
		wlDataType=new Label(wgType, SWT.RIGHT);
		wlDataType.setText(BaseMessages.getString(PKG, "ValidatorDialog.DataType.Label")); //$NON-NLS-1$
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
		fdDataType.left = new FormAttachment(middle, margin+extra);
		fdDataType.right= new FormAttachment(100, 0);
		fdDataType.top  = new FormAttachment(wDataTypeVerified, margin);
		wDataType.setLayoutData(fdDataType);
		
		// Conversion mask
		//
		wlConversionMask=new Label(wgType, SWT.RIGHT);
		wlConversionMask.setText(BaseMessages.getString(PKG, "ValidatorDialog.ConversionMask.Label")); //$NON-NLS-1$
 		props.setLook(wlConversionMask);
		FormData fdlConversionMask = new FormData();
		fdlConversionMask.left = new FormAttachment(0, 0);
		fdlConversionMask.right= new FormAttachment(middle, -margin);
		fdlConversionMask.top  = new FormAttachment(wDataType, margin);
		wlConversionMask.setLayoutData(fdlConversionMask);
		wConversionMask=new WarningText(wgType, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wConversionMask);
		FormData fdConversionMask = new FormData();
		fdConversionMask.left = new FormAttachment(middle, margin);
		fdConversionMask.right= new FormAttachment(100, 0);
		fdConversionMask.top  = new FormAttachment(wDataType, margin);
		wConversionMask.setLayoutData(fdConversionMask);
		addSpacesWarning(wConversionMask);

		// Decimal Symbol
		//
		wlDecimalSymbol=new Label(wgType, SWT.RIGHT);
		wlDecimalSymbol.setText(BaseMessages.getString(PKG, "ValidatorDialog.DecimalSymbol.Label")); //$NON-NLS-1$
 		props.setLook(wlDecimalSymbol);
		FormData fdlDecimalSymbol = new FormData();
		fdlDecimalSymbol.left = new FormAttachment(0, 0);
		fdlDecimalSymbol.right= new FormAttachment(middle, -margin);
		fdlDecimalSymbol.top  = new FormAttachment(wConversionMask, margin);
		wlDecimalSymbol.setLayoutData(fdlDecimalSymbol);
		wDecimalSymbol=new WarningText(wgType, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wDecimalSymbol);
		FormData fdDecimalSymbol = new FormData();
		fdDecimalSymbol.left = new FormAttachment(middle, margin);
		fdDecimalSymbol.right= new FormAttachment(100, 0);
		fdDecimalSymbol.top  = new FormAttachment(wConversionMask, margin);
		wDecimalSymbol.setLayoutData(fdDecimalSymbol);
		addSpacesWarning(wDecimalSymbol);
		
		// Grouping Symbol
		//
		wlGroupingSymbol=new Label(wgType, SWT.RIGHT);
		wlGroupingSymbol.setText(BaseMessages.getString(PKG, "ValidatorDialog.GroupingSymbol.Label")); //$NON-NLS-1$
 		props.setLook(wlGroupingSymbol);
		FormData fdlGroupingSymbol = new FormData();
		fdlGroupingSymbol.left = new FormAttachment(0, 0);
		fdlGroupingSymbol.right= new FormAttachment(middle, -margin);
		fdlGroupingSymbol.top  = new FormAttachment(wDecimalSymbol, margin);
		wlGroupingSymbol.setLayoutData(fdlGroupingSymbol);
		wGroupingSymbol=new WarningText(wgType, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wGroupingSymbol);
		FormData fdGroupingSymbol = new FormData();
		fdGroupingSymbol.left = new FormAttachment(middle, margin);
		fdGroupingSymbol.right= new FormAttachment(100, 0);
		fdGroupingSymbol.top  = new FormAttachment(wDecimalSymbol, margin);
		wGroupingSymbol.setLayoutData(fdGroupingSymbol);
		addSpacesWarning(wGroupingSymbol);
		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////
		//
		// The data group...
		//
		//
		wgData = new Group(wComp, SWT.NONE);
		props.setLook(wgData);
		wgData.setText(BaseMessages.getString(PKG, "ValidatorDialog.DataGroup.Label"));
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
		wlNullAllowed.setText(BaseMessages.getString(PKG, "ValidatorDialog.NullAllowed.Label")); //$NON-NLS-1$
 		props.setLook(wlNullAllowed);
		FormData fdlNullAllowed = new FormData();
		fdlNullAllowed.left = new FormAttachment(0, 0);
		fdlNullAllowed.right= new FormAttachment(middle, -margin);
		fdlNullAllowed.top  = new FormAttachment(0, 0);
		wlNullAllowed.setLayoutData(fdlNullAllowed);
		wNullAllowed=new Button(wgData, SWT.CHECK);
 		props.setLook(wNullAllowed);
		FormData fdNullAllowed = new FormData();
		fdNullAllowed.left = new FormAttachment(middle, margin+extra);
		fdNullAllowed.right= new FormAttachment(100, 0);
		fdNullAllowed.top  = new FormAttachment(0, 0);
		wNullAllowed.setLayoutData(fdNullAllowed);

		// Only null allowed?
		//
		wlOnlyNullAllowed=new Label(wgData, SWT.RIGHT);
		wlOnlyNullAllowed.setText(BaseMessages.getString(PKG, "ValidatorDialog.OnlyNullAllowed.Label")); //$NON-NLS-1$
 		props.setLook(wlOnlyNullAllowed);
		FormData fdlOnlyNullAllowed = new FormData();
		fdlOnlyNullAllowed.left = new FormAttachment(0, 0);
		fdlOnlyNullAllowed.right= new FormAttachment(middle, -margin);
		fdlOnlyNullAllowed.top  = new FormAttachment(wNullAllowed, margin);
		wlOnlyNullAllowed.setLayoutData(fdlOnlyNullAllowed);
		wOnlyNullAllowed=new Button(wgData, SWT.CHECK);
 		props.setLook(wOnlyNullAllowed);
		FormData fdOnlyNullAllowed = new FormData();
		fdOnlyNullAllowed.left = new FormAttachment(middle, margin+extra);
		fdOnlyNullAllowed.right= new FormAttachment(100, 0);
		fdOnlyNullAllowed.top  = new FormAttachment(wNullAllowed, margin);
		wOnlyNullAllowed.setLayoutData(fdOnlyNullAllowed);

		// Only numeric allowed?
		//
		wlOnlyNumeric=new Label(wgData, SWT.RIGHT);
		wlOnlyNumeric.setText(BaseMessages.getString(PKG, "ValidatorDialog.OnlyNumeric.Label")); //$NON-NLS-1$
 		props.setLook(wlOnlyNumeric);
		FormData fdlOnlyNumeric = new FormData();
		fdlOnlyNumeric.left = new FormAttachment(0, 0);
		fdlOnlyNumeric.right= new FormAttachment(middle, -margin);
		fdlOnlyNumeric.top  = new FormAttachment(wOnlyNullAllowed, margin);
		wlOnlyNumeric.setLayoutData(fdlOnlyNumeric);
		wOnlyNumeric=new Button(wgData, SWT.CHECK);
 		props.setLook(wOnlyNumeric);
		FormData fdOnlyNumeric = new FormData();
		fdOnlyNumeric.left = new FormAttachment(middle, margin+extra);
		fdOnlyNumeric.right= new FormAttachment(100, 0);
		fdOnlyNumeric.top  = new FormAttachment(wOnlyNullAllowed, margin);
		wOnlyNumeric.setLayoutData(fdOnlyNumeric);

		// Maximum length
		//
		wlMaxLength=new Label(wgData, SWT.RIGHT);
		wlMaxLength.setText(BaseMessages.getString(PKG, "ValidatorDialog.MaxLength.Label")); //$NON-NLS-1$
 		props.setLook(wlMaxLength);
		FormData fdlMaxLength = new FormData();
		fdlMaxLength.left = new FormAttachment(0, 0);
		fdlMaxLength.right= new FormAttachment(middle, -margin);
		fdlMaxLength.top  = new FormAttachment(wOnlyNumeric, margin);
		wlMaxLength.setLayoutData(fdlMaxLength);
		wMaxLength=new WarningText(wgData, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMaxLength);
		FormData fdMaxLength = new FormData();
		fdMaxLength.left = new FormAttachment(middle, margin);
		fdMaxLength.right= new FormAttachment(100, 0);
		fdMaxLength.top  = new FormAttachment(wOnlyNumeric, margin);
		wMaxLength.setLayoutData(fdMaxLength);
		addSpacesWarning(wMaxLength);

		// Minimum length
		//
		wlMinLength=new Label(wgData, SWT.RIGHT);
		wlMinLength.setText(BaseMessages.getString(PKG, "ValidatorDialog.MinLength.Label")); //$NON-NLS-1$
 		props.setLook(wlMinLength);
		FormData fdlMinLength = new FormData();
		fdlMinLength.left = new FormAttachment(0, 0);
		fdlMinLength.right= new FormAttachment(middle, -margin);
		fdlMinLength.top  = new FormAttachment(wMaxLength, margin);
		wlMinLength.setLayoutData(fdlMinLength);
		wMinLength=new WarningText(wgData, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMinLength);
		FormData fdMinLength = new FormData();
		fdMinLength.left = new FormAttachment(middle, margin);
		fdMinLength.right= new FormAttachment(100, 0);
		fdMinLength.top  = new FormAttachment(wMaxLength, margin);
		wMinLength.setLayoutData(fdMinLength);
		addSpacesWarning(wMinLength);

		// Maximum value
		//
		wlMaxValue=new Label(wgData, SWT.RIGHT);
		wlMaxValue.setText(BaseMessages.getString(PKG, "ValidatorDialog.MaxValue.Label")); //$NON-NLS-1$
 		props.setLook(wlMaxValue);
		FormData fdlMaxValue = new FormData();
		fdlMaxValue.left = new FormAttachment(0, 0);
		fdlMaxValue.right= new FormAttachment(middle, -margin);
		fdlMaxValue.top  = new FormAttachment(wMinLength, margin);
		wlMaxValue.setLayoutData(fdlMaxValue);
		wMaxValue=new WarningText(wgData, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMaxValue);
		FormData fdMaxValue = new FormData();
		fdMaxValue.left = new FormAttachment(middle, margin);
		fdMaxValue.right= new FormAttachment(100, 0);
		fdMaxValue.top  = new FormAttachment(wMinLength, margin);
		wMaxValue.setLayoutData(fdMaxValue);
		addSpacesWarning(wMaxValue);
		
		// Minimum value
		//
		wlMinValue=new Label(wgData, SWT.RIGHT);
		wlMinValue.setText(BaseMessages.getString(PKG, "ValidatorDialog.MinValue.Label")); //$NON-NLS-1$
 		props.setLook(wlMinValue);
		FormData fdlMinValue = new FormData();
		fdlMinValue.left = new FormAttachment(0, 0);
		fdlMinValue.right= new FormAttachment(middle, -margin);
		fdlMinValue.top  = new FormAttachment(wMaxValue, margin);
		wlMinValue.setLayoutData(fdlMinValue);
		wMinValue=new WarningText(wgData, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMinValue);
		FormData fdMinValue = new FormData();
		fdMinValue.left = new FormAttachment(middle, margin);
		fdMinValue.right= new FormAttachment(100, 0);
		fdMinValue.top  = new FormAttachment(wMaxValue, margin);
		wMinValue.setLayoutData(fdMinValue);
		addSpacesWarning(wMinValue);
		
		// Expected start string
		//
		wlStartStringExpected=new Label(wgData, SWT.RIGHT);
		wlStartStringExpected.setText(BaseMessages.getString(PKG, "ValidatorDialog.StartStringExpected.Label")); //$NON-NLS-1$
 		props.setLook(wlStartStringExpected);
		FormData fdlStartStringExpected = new FormData();
		fdlStartStringExpected.left = new FormAttachment(0, 0);
		fdlStartStringExpected.right= new FormAttachment(middle, -margin);
		fdlStartStringExpected.top  = new FormAttachment(wMinValue, margin);
		wlStartStringExpected.setLayoutData(fdlStartStringExpected);
		wStartStringExpected=new WarningText(wgData, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wStartStringExpected);
		FormData fdStartStringExpected = new FormData();
		fdStartStringExpected.left = new FormAttachment(middle, margin);
		fdStartStringExpected.right= new FormAttachment(100, 0);
		fdStartStringExpected.top  = new FormAttachment(wMinValue, margin);
		wStartStringExpected.setLayoutData(fdStartStringExpected);
		addSpacesWarning(wStartStringExpected);

		// Expected End string
		//
		wlEndStringExpected=new Label(wgData, SWT.RIGHT);
		wlEndStringExpected.setText(BaseMessages.getString(PKG, "ValidatorDialog.EndStringExpected.Label")); //$NON-NLS-1$
 		props.setLook(wlEndStringExpected);
		FormData fdlEndStringExpected = new FormData();
		fdlEndStringExpected.left = new FormAttachment(0, 0);
		fdlEndStringExpected.right= new FormAttachment(middle, -margin);
		fdlEndStringExpected.top  = new FormAttachment(wStartStringExpected, margin);
		wlEndStringExpected.setLayoutData(fdlEndStringExpected);
		wEndStringExpected=new WarningText(wgData, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wEndStringExpected);
		FormData fdEndStringExpected = new FormData();
		fdEndStringExpected.left = new FormAttachment(middle, margin);
		fdEndStringExpected.right= new FormAttachment(100, 0);
		fdEndStringExpected.top  = new FormAttachment(wStartStringExpected, margin);
		wEndStringExpected.setLayoutData(fdEndStringExpected);
		addSpacesWarning(wEndStringExpected);

		// Disallowed start string
		//
		wlStartStringDisallowed=new Label(wgData, SWT.RIGHT);
		wlStartStringDisallowed.setText(BaseMessages.getString(PKG, "ValidatorDialog.StartStringDisallowed.Label")); //$NON-NLS-1$
 		props.setLook(wlStartStringDisallowed);
		FormData fdlStartStringDisallowed = new FormData();
		fdlStartStringDisallowed.left = new FormAttachment(0, 0);
		fdlStartStringDisallowed.right= new FormAttachment(middle, -margin);
		fdlStartStringDisallowed.top  = new FormAttachment(wEndStringExpected, margin);
		wlStartStringDisallowed.setLayoutData(fdlStartStringDisallowed);
		wStartStringDisallowed=new WarningText(wgData, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wStartStringDisallowed);
		FormData fdStartStringDisallowed = new FormData();
		fdStartStringDisallowed.left = new FormAttachment(middle, margin);
		fdStartStringDisallowed.right= new FormAttachment(100, 0);
		fdStartStringDisallowed.top  = new FormAttachment(wEndStringExpected, margin);
		wStartStringDisallowed.setLayoutData(fdStartStringDisallowed);
		addSpacesWarning(wStartStringDisallowed);

		// Disallowed End string
		//
		wlEndStringDisallowed=new Label(wgData, SWT.RIGHT);
		wlEndStringDisallowed.setText(BaseMessages.getString(PKG, "ValidatorDialog.EndStringDisallowed.Label")); //$NON-NLS-1$
 		props.setLook(wlEndStringDisallowed);
		FormData fdlEndStringDisallowed = new FormData();
		fdlEndStringDisallowed.left = new FormAttachment(0, 0);
		fdlEndStringDisallowed.right= new FormAttachment(middle, -margin);
		fdlEndStringDisallowed.top  = new FormAttachment(wStartStringDisallowed, margin);
		wlEndStringDisallowed.setLayoutData(fdlEndStringDisallowed);
		wEndStringDisallowed=new WarningText(wgData, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wEndStringDisallowed);
		FormData fdEndStringDisallowed = new FormData();
		fdEndStringDisallowed.left = new FormAttachment(middle, margin);
		fdEndStringDisallowed.right= new FormAttachment(100, 0);
		fdEndStringDisallowed.top  = new FormAttachment(wStartStringDisallowed, margin);
		wEndStringDisallowed.setLayoutData(fdEndStringDisallowed);
		addSpacesWarning(wEndStringDisallowed);
		
		// Expected regular expression
		//
		wlRegExpExpected=new Label(wgData, SWT.RIGHT);
		wlRegExpExpected.setText(BaseMessages.getString(PKG, "ValidatorDialog.RegExpExpected.Label")); //$NON-NLS-1$
 		props.setLook(wlRegExpExpected);
		FormData fdlRegExpExpected = new FormData();
		fdlRegExpExpected.left = new FormAttachment(0, 0);
		fdlRegExpExpected.right= new FormAttachment(middle, -margin);
		fdlRegExpExpected.top  = new FormAttachment(wEndStringDisallowed, margin);
		wlRegExpExpected.setLayoutData(fdlRegExpExpected);
		wRegExpExpected=new WarningText(wgData, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wRegExpExpected);
		FormData fdRegExpExpected = new FormData();
		fdRegExpExpected.left = new FormAttachment(middle, margin);
		fdRegExpExpected.right= new FormAttachment(100, 0);
		fdRegExpExpected.top  = new FormAttachment(wEndStringDisallowed, margin);
		wRegExpExpected.setLayoutData(fdRegExpExpected);
		addSpacesWarning(wRegExpExpected);

		// Disallowed regular expression
		//
		wlRegExpDisallowed=new Label(wgData, SWT.RIGHT);
		wlRegExpDisallowed.setText(BaseMessages.getString(PKG, "ValidatorDialog.RegExpDisallowed.Label")); //$NON-NLS-1$
 		props.setLook(wlRegExpDisallowed);
		FormData fdlRegExpDisallowed = new FormData();
		fdlRegExpDisallowed.left = new FormAttachment(0, 0);
		fdlRegExpDisallowed.right= new FormAttachment(middle, -margin);
		fdlRegExpDisallowed.top  = new FormAttachment(wRegExpExpected, margin);
		wlRegExpDisallowed.setLayoutData(fdlRegExpDisallowed);
		wRegExpDisallowed=new WarningText(wgData, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wRegExpDisallowed);
		FormData fdRegExpDisallowed = new FormData();
		fdRegExpDisallowed.left = new FormAttachment(middle, margin);
		fdRegExpDisallowed.right= new FormAttachment(100, 0);
		fdRegExpDisallowed.top  = new FormAttachment(wRegExpExpected, margin);
		wRegExpDisallowed.setLayoutData(fdRegExpDisallowed);
		addSpacesWarning(wRegExpDisallowed);

		// Allowed values: a list box.
		//
		// Add an entry
		//
		wbAddAllowed = new Button(wgData, SWT.PUSH);
		wbAddAllowed.setText(BaseMessages.getString(PKG, "ValidatorDialog.ButtonAddAllowed.Label")); //$NON-NLS-1$
		FormData fdbAddAllowed = new FormData();
		fdbAddAllowed.right  = new FormAttachment(100, 0);
		fdbAddAllowed.top    = new FormAttachment(wRegExpDisallowed, margin);
		wbAddAllowed.setLayoutData(fdbAddAllowed);
		wbAddAllowed.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { addAllowedValue(); } });

		// Remove an entry
		//
		wbRemoveAllowed = new Button(wgData, SWT.PUSH);
		wbRemoveAllowed.setText(BaseMessages.getString(PKG, "ValidatorDialog.ButtonRemoveAllowed.Label")); //$NON-NLS-1$
		FormData fdbRemoveAllowed = new FormData();
		fdbRemoveAllowed.right  = new FormAttachment(100, 0);
		fdbRemoveAllowed.top    = new FormAttachment(wbAddAllowed, margin);
		wbRemoveAllowed.setLayoutData(fdbRemoveAllowed);
		wbRemoveAllowed.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { removeAllowedValue(); } });

		wlAllowedValues=new Label(wgData, SWT.RIGHT);
		wlAllowedValues.setText(BaseMessages.getString(PKG, "ValidatorDialog.AllowedValues.Label")); //$NON-NLS-1$
 		props.setLook(wlAllowedValues);
		FormData fdlAllowedValues = new FormData();
		fdlAllowedValues.left = new FormAttachment(0, 0);
		fdlAllowedValues.right= new FormAttachment(middle, -margin);
		fdlAllowedValues.top  = new FormAttachment(wRegExpDisallowed, margin);
		wlAllowedValues.setLayoutData(fdlAllowedValues);
		wAllowedValues=new List(wgData, SWT.MULTI | SWT.LEFT | SWT.BORDER);
 		props.setLook(wAllowedValues);
 		FormData fdAllowedValues = new FormData();
 		fdAllowedValues.left   = new FormAttachment(middle, margin+extra);
 		fdAllowedValues.right  = new FormAttachment(wbRemoveAllowed, -20);
 		fdAllowedValues.top    = new FormAttachment(wRegExpDisallowed, margin);
 		fdAllowedValues.bottom = new FormAttachment(wRegExpDisallowed, 150);
 		wAllowedValues.setLayoutData(fdAllowedValues);
 		
 		// Source allowed values from another step? 
		//
		wlSourceValues=new Label(wgData, SWT.RIGHT);
		wlSourceValues.setText(BaseMessages.getString(PKG, "ValidatorDialog.SourceValues.Label")); //$NON-NLS-1$
 		props.setLook(wlSourceValues);
		FormData fdlSourceValues = new FormData();
		fdlSourceValues.left = new FormAttachment(0, 0);
		fdlSourceValues.right= new FormAttachment(middle, -margin);
		fdlSourceValues.top  = new FormAttachment(wAllowedValues, margin);
		wlSourceValues.setLayoutData(fdlSourceValues);
		wSourceValues=new Button(wgData, SWT.CHECK);
 		props.setLook(wSourceValues);
		FormData fdSourceValues = new FormData();
		fdSourceValues.left = new FormAttachment(middle, margin+extra);
		fdSourceValues.right= new FormAttachment(100, 0);
		fdSourceValues.top  = new FormAttachment(wAllowedValues, margin);
		wSourceValues.setLayoutData(fdSourceValues);
		wSourceValues.addSelectionListener(new SelectionAdapter() {
		
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				enableFields();
			}
		});

		// Source allowed values : source step
		//
		wlSourceStep=new Label(wgData, SWT.RIGHT);
		wlSourceStep.setText(BaseMessages.getString(PKG, "ValidatorDialog.SourceStep.Label")); //$NON-NLS-1$
 		props.setLook(wlSourceStep);
		FormData fdlSourceStep = new FormData();
		fdlSourceStep.left = new FormAttachment(0, margin);
		fdlSourceStep.right= new FormAttachment(middle, -margin);
		fdlSourceStep.top  = new FormAttachment(wSourceValues, margin);
		wlSourceStep.setLayoutData(fdlSourceStep);
		wSourceStep=new Combo(wgData, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wSourceStep);
		FormData fdSourceStep = new FormData();
		fdSourceStep.left = new FormAttachment(middle, margin+extra);
		fdSourceStep.right= new FormAttachment(100, 0);
		fdSourceStep.top  = new FormAttachment(wSourceValues, margin);
		wSourceStep.setLayoutData(fdSourceStep);
		wSourceStep.addFocusListener(new FocusAdapter() { public void focusGained(org.eclipse.swt.events.FocusEvent e) { getSteps(); } } );
		wSourceStep.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent arg0) { getSteps(); } } );
		
		// Source allowed values : source field
		//
		wlSourceField=new Label(wgData, SWT.RIGHT);
		wlSourceField.setText(BaseMessages.getString(PKG, "ValidatorDialog.SourceField.Label")); //$NON-NLS-1$
 		props.setLook(wlSourceField);
		FormData fdlSourceField = new FormData();
		fdlSourceField.left = new FormAttachment(0, margin);
		fdlSourceField.right= new FormAttachment(middle, -margin);
		fdlSourceField.top  = new FormAttachment(wSourceStep, margin);
		wlSourceField.setLayoutData(fdlSourceField);
		wSourceField=new Combo(wgData, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wSourceField);
		FormData fdSourceField = new FormData();
		fdSourceField.left = new FormAttachment(middle, margin+extra);
		fdSourceField.right= new FormAttachment(100, 0);
		fdSourceField.top  = new FormAttachment(wSourceStep, margin);
		wSourceField.setLayoutData(fdSourceField);
		wSourceField.addFocusListener(new FocusAdapter() { public void focusGained(org.eclipse.swt.events.FocusEvent e) { getFields(); } } );
		wSourceField.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent arg0) { getFields(); } } );

		
 		wComp.layout();
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
				int index = wValidationsList.getSelectionIndex();
				if (index>=0) {
					selectionList.remove(index);
					selectedField=null;
					wValidationsList.remove(index);
					enableFields();
				}
			}
		});

		wNew.addSelectionListener(new SelectionAdapter() {
			
			public void widgetSelected(SelectionEvent e) {
				// Create a new validation rule page ...
				//
				EnterStringDialog enterStringDialog = new EnterStringDialog(shell, "", BaseMessages.getString(PKG, "ValidatorDialog.EnterValidationRuleName.Title"), BaseMessages.getString(PKG, "ValidatorDialog.EnterValidationRuleName.Message"));
				String description = enterStringDialog.open();
				if (description!=null) {
					if (Validation.findValidation(selectionList, description)!=null) {
						MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
						messageBox.setText(BaseMessages.getString(PKG, "ValidatorDialog.ValidationRuleNameAlreadyExists.Title"));
						messageBox.setMessage(BaseMessages.getString(PKG, "ValidatorDialog.ValidationRuleNameAlreadyExists.Message"));
						messageBox.open();
						return;
					}
					saveChanges();
					Validation validation = new Validation();
					validation.setName(description);
					selectionList.add(validation);
					selectedField = validation;
					refreshValidationsList();
					wValidationsList.select(selectionList.size()-1);
					getValidatorFieldData(validation);
				}
			}
		});

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wStepname.addSelectionListener( lsDef );
		wConversionMask.addSelectionListener(lsDef);
		wGroupingSymbol.addSelectionListener(lsDef);
		wDecimalSymbol.addSelectionListener(lsDef);
		
		wMaxLength.addSelectionListener(lsDef);
		wMinLength.addSelectionListener(lsDef);
		wMaxValue.addSelectionListener(lsDef);
		wMinValue.addSelectionListener(lsDef);
		
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
	
	protected void setFlags() {
		wConcatErrors.setEnabled(wValidateAll.getSelection());
		wConcatSeparator.setEnabled(wValidateAll.getSelection());
	}

	private void addSpacesWarning(SupportsWarningInterface warningText) {
		warningText.addWarningInterface(new WarningInterface() { public WarningMessageInterface getWarningSituation(String text, Control widget, Object subject) {
			return new SimpleWarningMessage( spacesValidation(text), BaseMessages.getString(PKG, "System.Warning.OnlySpaces")); }});
		warningText.addWarningInterface(new WarningInterface() { public WarningMessageInterface getWarningSituation(String text, Control widget, Object subject) {
			return new SimpleWarningMessage( text!=null && text.endsWith(" "), BaseMessages.getString(PKG, "System.Warning.TrailingSpaces")); }});
	}

	public boolean spacesValidation(String text) {
		return Const.onlySpaces(text) && !Const.isEmpty(text);
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
		EnterStringDialog dialog = new EnterStringDialog(shell, "", BaseMessages.getString(PKG, "ValidatorDialog.Dialog.AddAllowedValue.Title"), BaseMessages.getString(PKG, "ValidatorDialog.Dialog.AddAllowedValue.Message"));
		String value = dialog.open();
		if (!Const.isEmpty(value)) {
			wAllowedValues.add(value);
		}
	}

	private void getValidatorFieldData(Validation field) {
		
		wDescription.setText(Const.NVL(field.getName(), ""));
		wFieldName.setText(Const.NVL(field.getFieldName(), ""));

		wErrorCode.setText(Const.NVL(field.getErrorCode(), ""));
		wErrorDescription.setText(Const.NVL(field.getErrorDescription(), ""));

		wDataTypeVerified.setSelection(field.isDataTypeVerified());
		wDataType.setText(ValueMeta.getTypeDesc(field.getDataType()));
		wConversionMask.setText(Const.NVL(field.getConversionMask(), ""));
		wDecimalSymbol.setText(Const.NVL(field.getDecimalSymbol(), ""));
		wGroupingSymbol.setText(Const.NVL(field.getGroupingSymbol(), ""));
		
		wNullAllowed.setSelection(field.isNullAllowed());
		wOnlyNullAllowed.setSelection(field.isOnlyNullAllowed());
		wOnlyNumeric.setSelection(field.isOnlyNumericAllowed());
		
		if (field.getMaximumLength()>=0) wMaxLength.setText(Integer.toString(field.getMaximumLength())); else wMaxLength.setText(""); 
		if (field.getMinimumLength()>=0) wMinLength.setText(Integer.toString(field.getMinimumLength())); else wMinLength.setText(""); 
		wMaxValue.setText(Const.NVL(field.getMaximumValue(), ""));
		wMinValue.setText(Const.NVL(field.getMinimumValue(), ""));
		wStartStringExpected.setText(Const.NVL(field.getStartString(), ""));
		wEndStringExpected.setText(Const.NVL(field.getEndString(), ""));
		wStartStringDisallowed.setText(Const.NVL(field.getStartStringNotAllowed(), ""));
		wEndStringDisallowed.setText(Const.NVL(field.getEndStringNotAllowed(), ""));
		wRegExpExpected.setText(Const.NVL(field.getRegularExpression(), ""));
		wRegExpDisallowed.setText(Const.NVL(field.getRegularExpressionNotAllowed(), ""));
		
		wAllowedValues.removeAll();
		if (field.getAllowedValues()!=null) {
			for (String allowedValue : field.getAllowedValues()) {
				wAllowedValues.add(Const.NVL(allowedValue, ""));
			}
		}
		
		wSourceValues.setSelection(field.isSourcingValues());
		wSourceStep.setText( Const.NVL(field.getSourcingStepName(), "") );
		wSourceField.setText(Const.NVL(field.getSourcingField(), ""));
	}
	
	private void enableFields() {
		boolean visible = selectedField!=null;
		
		wgType.setVisible(visible);
		wgData.setVisible(visible);
		
		wlFieldName.setVisible(visible);
		wFieldName.setVisible(visible);
		wlDescription.setVisible(visible);
		wDescription.setVisible(visible);
		wlErrorCode.setVisible(visible);
		wErrorCode.setVisible(visible);
		wlErrorDescription.setVisible(visible);
		wErrorDescription.setVisible(visible);
		
		wlSourceStep.setEnabled(wSourceValues.getSelection());
		wSourceStep.setEnabled(wSourceValues.getSelection());
		wlSourceField.setEnabled(wSourceValues.getSelection());
		wSourceField.setEnabled(wSourceValues.getSelection());
		wlAllowedValues.setEnabled(!wSourceValues.getSelection());
		wAllowedValues.setEnabled(!wSourceValues.getSelection());
		wbAddAllowed.setEnabled(!wSourceValues.getSelection());
		wbRemoveAllowed.setEnabled(!wSourceValues.getSelection());
	}

	private void showSelectedValidatorField(String selection) {
		// Someone hit a field...
		//
		saveChanges();
		
		Validation field = Validation.findValidation(selectionList, selection);
		if (field==null) {
			field = new Validation(selection);
			ValueMetaInterface valueMeta = inputFields.searchValueMeta(selection);
			if (valueMeta!=null) {
				// Set the default data type
				//
				field.setDataType(valueMeta.getType());
			}
		}
		
		selectedField = field;
		
		getValidatorFieldData(selectedField);
		
		enableFields();
	}

	private void saveChanges() {
		if (selectedField!=null) {
			// First grab the info from the dialog...
			// 
			selectedField.setFieldName(wFieldName.getText());

			selectedField.setErrorCode(wErrorCode.getText());
			selectedField.setErrorDescription(wErrorDescription.getText());

			selectedField.setDataTypeVerified(wDataTypeVerified.getSelection());
			selectedField.setDataType(ValueMeta.getType(wDataType.getText()));
			selectedField.setConversionMask(wConversionMask.getText());
			selectedField.setDecimalSymbol(wDecimalSymbol.getText());
			selectedField.setGroupingSymbol(wGroupingSymbol.getText());
			
			selectedField.setNullAllowed(wNullAllowed.getSelection());
			selectedField.setOnlyNullAllowed(wOnlyNullAllowed.getSelection());
			selectedField.setOnlyNumericAllowed(wOnlyNumeric.getSelection());
			
			selectedField.setMaximumLength(Const.toInt(wMaxLength.getText(), -1));
			selectedField.setMinimumLength(Const.toInt(wMinLength.getText(), -1));
			selectedField.setMaximumValue(wMaxValue.getText());
			selectedField.setMinimumValue(wMinValue.getText());
			
			selectedField.setStartString(wStartStringExpected.getText());
			selectedField.setEndString(wEndStringExpected.getText());
			selectedField.setStartStringNotAllowed(wStartStringDisallowed.getText());
			selectedField.setEndStringNotAllowed(wEndStringDisallowed.getText());

			selectedField.setRegularExpression(wRegExpExpected.getText());
			selectedField.setRegularExpressionNotAllowed(wRegExpDisallowed.getText());

			selectedField.setAllowedValues(wAllowedValues.getItems());
			
			selectedField.setSourcingValues(wSourceValues.getSelection());
			selectedField.setSourcingField(wSourceField.getText());

			// Save the old info in the map
			// 
			// selectionList.add(selectedField);
		}
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		// Populate the list of validations...
		//
		refreshValidationsList();
		enableFields();
		
		wValidateAll.setSelection(input.isValidatingAll());
		wConcatErrors.setSelection(input.isConcatenatingErrors());
		wConcatSeparator.setText(Const.NVL(input.getConcatenationSeparator(), ""));

		// Select the first available field...
		//
		if (input.getValidations().size()>0) {
			Validation validatorField = input.getValidations().get(0);
			String description = validatorField.getName();
			int index = wValidationsList.indexOf(description);
			if (index>=0) {
				wValidationsList.select(index);
				showSelectedValidatorField(description);
			}
		}
		
		setFlags();
		
		wStepname.selectAll();
	}
	
	private void refreshValidationsList() {
		wValidationsList.removeAll();
		for (Validation validation : selectionList) {
			wValidationsList.add(validation.getName());
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
		input.setValidatingAll(wValidateAll.getSelection());
		input.setConcatenatingErrors(wConcatErrors.getSelection());
		input.setConcatenationSeparator(wConcatSeparator.getText());
		
		input.setValidations(selectionList);
		
		dispose();
	}

	private void getSteps() {
        Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
        shell.setCursor(busy);

		String fieldStep = wSourceStep.getText();
		
		wSourceStep.removeAll();
		wSourceStep.setItems(transMeta.getPrevStepNames(stepMeta));

		wSourceStep.setText(fieldStep);
		
        shell.setCursor(null);
        busy.dispose();
	}

	private void getFields() {
        Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
        shell.setCursor(busy);

		try {
			String sourceStepName = wSourceStep.getText();
			if (!Const.isEmpty(sourceStepName)) {
				String fieldName = wSourceField.getText();
				RowMetaInterface r = transMeta.getStepFields(sourceStepName);
				if (r != null) {
					wSourceField.setItems(r.getFieldNames());
				}
				wSourceField.setText(fieldName);
			}
	        shell.setCursor(null);
	        busy.dispose();
		} catch (KettleException ke) {
            shell.setCursor(null);
            busy.dispose();
			new ErrorDialog(shell, BaseMessages.getString(PKG, "ValidatorDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "ValidatorDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
