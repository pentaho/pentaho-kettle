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

package org.pentaho.di.ui.trans.steps.numberrange;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.numberrange.NumberRangeMeta;
import org.pentaho.di.trans.steps.numberrange.NumberRangeRule;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


/**
 * Configuration dialog for the NumberRange
 * 
 * @author ronny.roeller@fredhopper.com
 *
 */
public class NumberRangeDialog extends BaseStepDialog implements
	 		StepDialogInterface 
 {
	private static Class<?> PKG = NumberRangeMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	 
	 	private NumberRangeMeta input;
	 
	 	private CCombo inputFieldControl;
	 	private Text outputFieldControl;
	 	private Text fallBackValueControl;
	 	private TableView rulesControl;
	 
	 	private FormData fdFields;
	 
	 	public NumberRangeDialog(Shell parent, Object in,
						TransMeta transMeta, String sname) 
 {
	 		super(parent, (BaseStepMeta) in, transMeta, sname);
	 		input = (NumberRangeMeta) in;
	 	}
	 
	 	public String open() 
		{
					Shell parent = getParent();
					Display display = parent.getDisplay();
			
					shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN| SWT.MAX);
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
			
						FormLayout formLayout = new FormLayout();
					formLayout.marginWidth = Const.FORM_MARGIN;
					formLayout.marginHeight = Const.FORM_MARGIN;
			
					shell.setLayout(formLayout);
					shell.setText(BaseMessages.getString(PKG, "NumberRange.TypeLongDesc")); //$NON-NLS-1$
			
						// Create controls
						wStepname = createLine(lsMod, BaseMessages.getString(PKG, "NumberRange.StepName"), null);
					inputFieldControl = createLineCombo(lsMod, BaseMessages.getString(PKG, "NumberRange.InputField"), wStepname);
					outputFieldControl = createLine(lsMod, BaseMessages.getString(PKG, "NumberRange.OutputField"),inputFieldControl);
					
					inputFieldControl.addFocusListener(new FocusListener()
				            {
				                public void focusLost(org.eclipse.swt.events.FocusEvent e)
				                {
				                }
				            
				                public void focusGained(org.eclipse.swt.events.FocusEvent e)
				                {
				                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
				                    shell.setCursor(busy);
									loadComboOptions();
				                    shell.setCursor(null);
				                    busy.dispose();
				                }
				            }
				        );       
					fallBackValueControl = createLine(lsMod,BaseMessages.getString(PKG, "NumberRange.DefaultValue"), outputFieldControl);
			
						createRulesTable(lsMod);
			
						// Some buttons
						wOK = new Button(shell, SWT.PUSH);
					wOK.setText("OK"); //$NON-NLS-1$
					wCancel = new Button(shell, SWT.PUSH);
					wCancel.setText("Cancel"); //$NON-NLS-1$
			
						BaseStepDialog.positionBottomButtons(shell,
								new Button[] { wOK, wCancel }, Const.MARGIN, rulesControl);
			
						// Add listeners
						lsCancel = new Listener() 
			{
							public void handleEvent(Event e) 
							{
												cancel();
											}
						};
					lsOK = new Listener() 
			{
							public void handleEvent(Event e) 
							{
												ok();
											}
						};
			
						wCancel.addListener(SWT.Selection, lsCancel);
					wOK.addListener(SWT.Selection, lsOK);
			
						lsDef = new SelectionAdapter() 
			{
							public void widgetDefaultSelected(SelectionEvent e) 
							{
												ok();
											}
						};
			
						wStepname.addSelectionListener(lsDef);
					inputFieldControl.addSelectionListener(lsDef);
			
						// Detect X or ALT-F4 or something that kills this window...
						shell.addShellListener(new ShellAdapter() 
			{
							public void shellClosed(ShellEvent e) 
							{
												cancel();
											}
						});
			
						// Set the shell size, based upon previous time...
						setSize();
			
						getData();
					input.setChanged(changed);
			
						shell.open();
					while (!shell.isDisposed()) 
					{
									if (!display.readAndDispatch())
														display.sleep();
								}
					return stepname;
				}
	 
	 	/**
	 * Creates the table of rules
	 */
	 	private void createRulesTable(ModifyListener lsMod) 
		{
					Label rulesLable = new Label(shell, SWT.NONE);
					rulesLable.setText(BaseMessages.getString(PKG, "NumberRange.Ranges")); //$NON-NLS-1$
					props.setLook(rulesLable);
					FormData lableFormData = new FormData();
					lableFormData.left = new FormAttachment(0, 0);
					lableFormData.right = new FormAttachment(props.getMiddlePct(),
								-Const.MARGIN);
					lableFormData.top = new FormAttachment(fallBackValueControl,
								Const.MARGIN);
					rulesLable.setLayoutData(lableFormData);
			
						final int FieldsRows = input.getRules().size();
			
						ColumnInfo[] colinf = new ColumnInfo[3];
					colinf[0] = new ColumnInfo(BaseMessages.getString(PKG, "NumberRange.LowerBound"), ColumnInfo.COLUMN_TYPE_TEXT,
								false);
					colinf[1] = new ColumnInfo(BaseMessages.getString(PKG, "NumberRange.UpperBound"), ColumnInfo.COLUMN_TYPE_TEXT,
								false);
					colinf[2] = new ColumnInfo(BaseMessages.getString(PKG, "NumberRange.Value"), ColumnInfo.COLUMN_TYPE_TEXT, false);
			
					rulesControl = new TableView(transMeta,shell, SWT.BORDER
								| SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod,props);
			
					fdFields = new FormData();
					fdFields.left = new FormAttachment(0, 0);
					fdFields.top = new FormAttachment(rulesLable, Const.MARGIN);
					fdFields.right = new FormAttachment(100, 0);
					fdFields.bottom = new FormAttachment(100, -50);
					rulesControl.setLayoutData(fdFields);
				}
	 
	 	private Text createLine(ModifyListener lsMod, String lableText,
						Control prevControl) 
	 {
		 		// Value line
			 		Label lable = new Label(shell, SWT.RIGHT);
		 		lable.setText(lableText); //$NON-NLS-1$
		 		props.setLook(lable);
		 		FormData lableFormData = new FormData();
		 		lableFormData.left = new FormAttachment(0, 0);
		 		lableFormData.right = new FormAttachment(props.getMiddlePct(),
			 				-Const.MARGIN);
		 		// In case it is the first control
			 		if (prevControl != null)
									lableFormData.top = new FormAttachment(prevControl, Const.MARGIN);
		 		else
			 			lableFormData.top = new FormAttachment(0, Const.MARGIN);
		 		lable.setLayoutData(lableFormData);
		 
			 		Text control = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		 		props.setLook(control);
		 		control.addModifyListener(lsMod);
		 		FormData widgetFormData = new FormData();
		 		widgetFormData.left = new FormAttachment(props.getMiddlePct(), 0);
		 		// In case it is the first control
			 		if (prevControl != null)
									widgetFormData.top = new FormAttachment(prevControl, Const.MARGIN);
		 		else
			 			widgetFormData.top = new FormAttachment(0, Const.MARGIN);
		 		widgetFormData.right = new FormAttachment(100, 0);
		 		control.setLayoutData(widgetFormData);
		 
			 		return control;
		 	}
	 
	 	private CCombo createLineCombo(ModifyListener lsMod, String lableText,
						Control prevControl) 
	 {
		 		// Value line
			 	Label lable = new Label(shell, SWT.RIGHT);
		 		lable.setText(lableText); //$NON-NLS-1$
		 		props.setLook(lable);
		 		FormData lableFormData = new FormData();
		 		lableFormData.left = new FormAttachment(0, 0);
		 		lableFormData.right = new FormAttachment(props.getMiddlePct(),
			 				-Const.MARGIN);
		 		// In case it is the first control
			 	if (prevControl != null)
						lableFormData.top = new FormAttachment(prevControl, Const.MARGIN);
		 		else
			 			lableFormData.top = new FormAttachment(0, Const.MARGIN);
		 		lable.setLayoutData(lableFormData);
		 
			 		CCombo control = new CCombo(shell, SWT.BORDER);
		 		props.setLook(control);
		 		control.addModifyListener(lsMod);
		 		FormData widgetFormData = new FormData();
		 		widgetFormData.left = new FormAttachment(props.getMiddlePct(), 0);
		 		// In case it is the first control
			 		if (prevControl != null)
			 			widgetFormData.top = new FormAttachment(prevControl, Const.MARGIN);
		 		else
			 			widgetFormData.top = new FormAttachment(0, Const.MARGIN);
		 		widgetFormData.right = new FormAttachment(100, 0);
		 		control.setLayoutData(widgetFormData);
		 
			 	return control;
		 	}
	 
	 	// Read data from input (TextFileInputInfo)
	 	public void getData() 
		{
			// Get fields

			wStepname.setText(stepname);
	
			String inputField = input.getInputField();
			if (inputField != null)	inputFieldControl.setText(inputField);
	
			String outputField = input.getOutputField();
			if (outputField != null) outputFieldControl.setText(outputField);
	
			String fallBackValue = input.getFallBackValue();
			if (fallBackValue != null)	fallBackValueControl.setText(fallBackValue);

			for (int i=0; i<input.getRules().size();i++) {
				NumberRangeRule rule=input.getRules().get(i);
				TableItem item = rulesControl.table.getItem(i);

				// Empty value is equal to minimal possible value
				if (rule.getLowerBound() > -Double.MAX_VALUE) {
					String lowerBoundStr = String.valueOf(rule.getLowerBound());
					item.setText(1, lowerBoundStr);
				}

				// Empty value is equal to maximal possible value
				if (rule.getUpperBound() < Double.MAX_VALUE) {
					String upperBoundStr = String.valueOf(rule.getUpperBound());
					item.setText(2, upperBoundStr);
				}

				item.setText(3, rule.getValue());
			}
			rulesControl.setRowNums();
			rulesControl.optWidth(true);
	}
	 
	 	private void cancel() 
		{
			stepname = null;
			input.setChanged(changed);
			dispose();
		}
	 
	 	private void ok() 
		{
	 		if (Const.isEmpty(wStepname.getText())) return;
	 		
			stepname = wStepname.getText(); // return value
	
			String inputField = inputFieldControl.getText();
			input.setInputField(inputField);
	
			input.emptyRules();
	
			String fallBackValue = fallBackValueControl.getText();
			input.setFallBackValue(fallBackValue);
			
			input.setOutputField(outputFieldControl.getText());
			
			int count = rulesControl.nrNonEmpty();
			for (int i = 0; i < count; i++) 
			{
					TableItem item = rulesControl.getNonEmpty(i);
					String lowerBoundStr = Const.isEmpty(item.getText(1)) ? 
							String.valueOf(-Double.MAX_VALUE) : item.getText(1);
					String upperBoundStr = Const.isEmpty(item.getText(2)) ? 
							String.valueOf(Double.MAX_VALUE) : item.getText(2);
					String value = item.getText(3);
		
				try 
				{
					double lowerBound = Double.parseDouble(lowerBoundStr);
					double upperBound = Double.parseDouble(upperBoundStr);
	
					input.addRule(lowerBound, upperBound, value);
				} 
				catch (NumberFormatException e) 
				{
						throw new IllegalArgumentException(
								"Bounds of this rule are not numeric: lowerBound="
								+ lowerBoundStr  +", upperBound="
								+ upperBoundStr  +", value="  +value, e);
				}
			}
			
				dispose();
		}
	 
	 	private void loadComboOptions() 
		{
			try 
			{
				String fieldvalue=null;
				if(inputFieldControl.getText()!=null) fieldvalue=inputFieldControl.getText();
				RowMetaInterface r = transMeta.getPrevStepFields(stepname);
				if(r!=null) inputFieldControl.setItems(r.getFieldNames());
				if(fieldvalue!=null) inputFieldControl.setText(fieldvalue);
		
			} 
			catch (KettleException ke) 
			{
				new ErrorDialog(shell, BaseMessages.getString(PKG, "NumberRange.TypeLongDesc"), "Can't get fields",	ke);
			}
		}
	 
	 }
