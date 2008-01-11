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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.validator.Messages;
import org.pentaho.di.trans.steps.validator.ValidatorField;
import org.pentaho.di.trans.steps.validator.ValidatorMeta;
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
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$
		
		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

		// List of fields to the left...
		//
		Label wlFieldList = new Label(shell, SWT.RIGHT);
		wlFieldList.setText(Messages.getString("ValidatorDialog.FieldList.Label")); //$NON-NLS-1$
 		props.setLook(wlFieldList);
		FormData fdlFieldList = new FormData();
		fdlFieldList.left = new FormAttachment(0, 0);
		fdlFieldList.right= new FormAttachment(middle, -margin);
		fdlFieldList.top  = new FormAttachment(wStepname, margin);
		wlFieldList.setLayoutData(fdlFieldList);
		wFieldList=new List(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		
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
		
		int middle2 = middle + ( 100 - middle ) / 2;
		
		// Check for null?
		//
		wlNullAllowed=new Label(shell, SWT.RIGHT);
		wlNullAllowed.setText(Messages.getString("ValidatorDialog.NullAllowed.Label")); //$NON-NLS-1$
 		props.setLook(wlNullAllowed);
		FormData fdlNullAllowed = new FormData();
		fdlNullAllowed.left = new FormAttachment(middle, 0);
		fdlNullAllowed.right= new FormAttachment(middle2, -margin);
		fdlNullAllowed.top  = new FormAttachment(wlFieldList, margin);
		wlNullAllowed.setLayoutData(fdlNullAllowed);
		wNullAllowed=new Button(shell, SWT.CHECK);
 		props.setLook(wNullAllowed);
		FormData fdNullAllowed = new FormData();
		fdNullAllowed.left = new FormAttachment(middle2, margin);
		fdNullAllowed.right= new FormAttachment(100, 0);
		fdNullAllowed.top  = new FormAttachment(wlFieldList, margin);
		wNullAllowed.setLayoutData(fdNullAllowed);

		// Maximum length
		//
		wlMaxLength=new Label(shell, SWT.RIGHT);
		wlMaxLength.setText(Messages.getString("ValidatorDialog.MaxLength.Label")); //$NON-NLS-1$
 		props.setLook(wlMaxLength);
		FormData fdlMaxLength = new FormData();
		fdlMaxLength.left = new FormAttachment(middle, 0);
		fdlMaxLength.right= new FormAttachment(middle2, -margin);
		fdlMaxLength.top  = new FormAttachment(wNullAllowed, margin);
		wlMaxLength.setLayoutData(fdlMaxLength);
		wMaxLength=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMaxLength);
		FormData fdMaxLength = new FormData();
		fdMaxLength.left = new FormAttachment(middle2, margin);
		fdMaxLength.right= new FormAttachment(100, 0);
		fdMaxLength.top  = new FormAttachment(wNullAllowed, margin);
		wMaxLength.setLayoutData(fdMaxLength);
		
		// Minimum length
		//
		wlMinLength=new Label(shell, SWT.RIGHT);
		wlMinLength.setText(Messages.getString("ValidatorDialog.MinLength.Label")); //$NON-NLS-1$
 		props.setLook(wlMinLength);
		FormData fdlMinLength = new FormData();
		fdlMinLength.left = new FormAttachment(middle, 0);
		fdlMinLength.right= new FormAttachment(middle2, -margin);
		fdlMinLength.top  = new FormAttachment(wMaxLength, margin);
		wlMinLength.setLayoutData(fdlMinLength);
		wMinLength=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMinLength);
		FormData fdMinLength = new FormData();
		fdMinLength.left = new FormAttachment(middle2, margin);
		fdMinLength.right= new FormAttachment(100, 0);
		fdMinLength.top  = new FormAttachment(wMaxLength, margin);
		wMinLength.setLayoutData(fdMinLength);

		

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
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
	
	private void getValidatorFieldData(ValidatorField field) {
		wNullAllowed.setSelection(field.isNullAllowed());
		if (field.getMaximumLength()>=0) wMaxLength.setText(Integer.toString(field.getMaximumLength())); else wMaxLength.setText(""); 
		if (field.getMinimumLength()>=0) wMinLength.setText(Integer.toString(field.getMinimumLength())); else wMinLength.setText(""); 
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
	}

	private void saveChanges() {
		if (selectedField!=null) {
			// First grab the info from the dialog...
			// 
			selectedField.setNullAllowed(wNullAllowed.getSelection());
			selectedField.setMaximumLength(Const.toInt(wMaxLength.getText(), -1));
			selectedField.setMinimumLength(Const.toInt(wMinLength.getText(), -1));

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
