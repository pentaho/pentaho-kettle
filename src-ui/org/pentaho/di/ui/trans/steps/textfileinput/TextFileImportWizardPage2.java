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

package org.pentaho.di.ui.trans.steps.textfileinput;

import java.util.Vector;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.gui.TextFileInputFieldInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;
import org.pentaho.di.ui.core.PropsUI;

public class TextFileImportWizardPage2 extends WizardPage
{
	private static Class<?> PKG = TextFileInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private List wFields;
	private FormData fdFields;

	private List wSamples;
	private FormData fdSamples;
	
	private Label    wlFieldname;
	private Text     wFieldname;
	private FormData fdlFieldname, fdFieldname;

	private Label    wlPosition;
	private Text     wPosition;
	private FormData fdlPosition, fdPosition;

	private Label    wlLength;
	private Text     wLength;
	private FormData fdlLength, fdLength;

	private Label        wlFieldtype;
	private CCombo       wFieldtype;
	private FormData     fdlFieldtype, fdFieldtype;

	private Label    wlFormat;
	private Text     wFormat;
	private FormData fdlFormat, fdFormat;

	private Label        wlTrimtype;
	private CCombo       wTrimtype;
	private FormData     fdlTrimtype, fdTrimtype;

	private Label    wlPrecision;
	private Text     wPrecision;
	private FormData fdlPrecision, fdPrecision;

	private Label    wlDecimal;
	private Text     wDecimal;
	private FormData fdlDecimal, fdDecimal;

	private Label    wlGroup;
	private Text     wGroup;
	private FormData fdlGroup, fdGroup;

	private Label    wlCurrency;
	private Text     wCurrency;
	private FormData fdlCurrency, fdCurrency;

	private Label        wlIgnore;
	private Button       wIgnore;
	private FormData     fdlIgnore, fdIgnore;

	private Label        wlRepeat;
	private Button       wRepeat;
	private FormData     fdlRepeat, fdRepeat;

	private Label    wlNull;
	private Text     wNull;
	private FormData fdlNull, fdNull;


	private Button   wGuess, wGuessAll;
	private FormData fdGuess, fdGuessAll;

	private Button   wPrev, wNext;
	private FormData fdPrev, fdNext;
	
	private PropsUI props;
	private java.util.List<String> rows;
	private Vector<TextFileInputFieldInterface> fields;
	
	private Shell   shell  ;
	 
	public TextFileImportWizardPage2(String arg, PropsUI props, java.util.List<String> rows, Vector<TextFileInputFieldInterface> fields)
	{
		super(arg);
		this.props=props;
		this.rows=rows;
		this.fields=fields;
		
		setTitle(BaseMessages.getString(PKG, "TextFileImportWizardPage2.DialogTitle"));
		setDescription("Give a name to the fields in this text file");
	}
	
	public void createControl(Composite parent)
	{
		shell   = parent.getShell();

		int margin  = Const.MARGIN;		
		int left    = props.getMiddlePct()/2;
		int middle  = props.getMiddlePct();
		int right   = middle+left;
		
		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NONE);
        props.setLook(composite);
	    
	    FormLayout compLayout = new FormLayout();
	    compLayout.marginHeight = Const.FORM_MARGIN;
	    compLayout.marginWidth  = Const.FORM_MARGIN;
		composite.setLayout(compLayout);

		wFields = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		props.setLook(wFields);
		fdFields = new FormData();
		fdFields.top    = new FormAttachment(  0,  0);
		fdFields.left   = new FormAttachment(  0,  0);
		fdFields.right  = new FormAttachment(left,  0);
		fdFields.bottom = new FormAttachment(100,  0);
		wFields.setLayoutData(fdFields);
		
		refreshFields();
				
		// Fieldname line
		wlFieldname=new Label(composite, SWT.RIGHT);
		wlFieldname.setText(BaseMessages.getString(PKG, "TextFileImportWizardPage2.Fieldname.Label"));
        props.setLook(wlFieldname);
		fdlFieldname=new FormData();
		fdlFieldname.left = new FormAttachment(wFields, 0);
		fdlFieldname.top  = new FormAttachment(0, 0);
		fdlFieldname.right= new FormAttachment(middle, 0);
		wlFieldname.setLayoutData(fdlFieldname);
		wFieldname=new Text(composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wFieldname);
		fdFieldname=new FormData();
		fdFieldname.left = new FormAttachment(middle, margin);
		fdFieldname.right= new FormAttachment(right, 0);
		fdFieldname.top  = new FormAttachment(0, 0);
		wFieldname.setLayoutData(fdFieldname);

		// Position line
		wlPosition=new Label(composite, SWT.RIGHT);
		wlPosition.setText(BaseMessages.getString(PKG, "TextFileImportWizardPage2.FieldPosition.Label"));
        props.setLook(wlPosition);
		fdlPosition=new FormData();
		fdlPosition.left = new FormAttachment(wFields, 0);
		fdlPosition.top  = new FormAttachment(wFieldname, margin);
		fdlPosition.right= new FormAttachment(middle, 0);
		wlPosition.setLayoutData(fdlPosition);
		wPosition=new Text(composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wPosition);
		fdPosition=new FormData();
		fdPosition.left = new FormAttachment(middle, margin);
		fdPosition.top  = new FormAttachment(wFieldname, margin);
		fdPosition.right= new FormAttachment(right, 0);
		wPosition.setLayoutData(fdPosition);
		wPosition.setEnabled(false);

		// Topos line
		wlLength=new Label(composite, SWT.RIGHT);
		wlLength.setText(BaseMessages.getString(PKG, "TextFileImportWizardPage2.FieldLength.Label"));
        props.setLook(wlLength);
		fdlLength=new FormData();
		fdlLength.left = new FormAttachment(wFields, 0);
		fdlLength.top  = new FormAttachment(wPosition, margin);
		fdlLength.right= new FormAttachment(middle, 0);
		wlLength.setLayoutData(fdlLength);
		wLength=new Text(composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wLength);
        fdLength=new FormData();
		fdLength.left = new FormAttachment(middle, margin);
		fdLength.top  = new FormAttachment(wPosition, margin);
		fdLength.right= new FormAttachment(right, 0);
		wLength.setLayoutData(fdLength);
		wLength.setEnabled(false);

		// Fieldtype line
		wlFieldtype=new Label(composite, SWT.RIGHT);
		wlFieldtype.setText(BaseMessages.getString(PKG, "TextFileImportWizardPage2.FieldType.Label"));
        props.setLook(wlFieldtype);
		fdlFieldtype=new FormData();
		fdlFieldtype.left = new FormAttachment(wFields, 0);
		fdlFieldtype.top  = new FormAttachment(wLength, margin);
		fdlFieldtype.right= new FormAttachment(middle, 0);
		wlFieldtype.setLayoutData(fdlFieldtype);
		wFieldtype=new CCombo(composite, SWT.BORDER | SWT.READ_ONLY);
        props.setLook(wFieldtype);
        for (int i=0;i<ValueMeta.getTypes().length;i++)
		{
			wFieldtype.add(ValueMeta.getTypes()[i]);
		}		
		fdFieldtype=new FormData();
		fdFieldtype.left = new FormAttachment(middle, margin);
		fdFieldtype.top  = new FormAttachment(wLength, margin);
		fdFieldtype.right= new FormAttachment(right, 0);
		wFieldtype.setLayoutData(fdFieldtype);

		// Format line
		wlFormat=new Label(composite, SWT.RIGHT);
		wlFormat.setText(BaseMessages.getString(PKG, "TextFileImportWizardPage2.FieldFormatter.Label"));
        props.setLook(wlFormat);
        fdlFormat=new FormData();
		fdlFormat.left = new FormAttachment(wFields, 0);
		fdlFormat.top  = new FormAttachment(wFieldtype, margin);
		fdlFormat.right= new FormAttachment(middle, 0);
		wlFormat.setLayoutData(fdlFormat);
		wFormat=new Text(composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wFormat);
		fdFormat=new FormData();
		fdFormat.left = new FormAttachment(middle, margin);
		fdFormat.top  = new FormAttachment(wFieldtype, margin);
		fdFormat.right= new FormAttachment(right, 0);
		wFormat.setLayoutData(fdFormat);

		// Ignore checkbox
		wlIgnore=new Label(composite, SWT.RIGHT);
		wlIgnore.setText(BaseMessages.getString(PKG, "TextFileImportWizardPage2.Ignore.Label"));
        props.setLook(wlIgnore);
		fdlIgnore=new FormData();
		fdlIgnore.left = new FormAttachment(wFields, 0);
		fdlIgnore.top  = new FormAttachment(wFormat, margin);
		fdlIgnore.right= new FormAttachment(middle, 0);
		wlIgnore.setLayoutData(fdlIgnore);
		wIgnore=new Button(composite, SWT.CHECK);
        props.setLook(wIgnore);
		fdIgnore=new FormData();
		fdIgnore.left = new FormAttachment(middle, margin);
		fdIgnore.top  = new FormAttachment(wFormat, margin);
		fdIgnore.right= new FormAttachment(right, 0);
		wIgnore.setLayoutData(fdIgnore);

		// Trimtype line
		wlTrimtype=new Label(composite, SWT.RIGHT);
		wlTrimtype.setText(BaseMessages.getString(PKG, "TextFileImportWizardPage2.TrimType.Label"));
        props.setLook(wlTrimtype);
		fdlTrimtype=new FormData();
		fdlTrimtype.left = new FormAttachment(wFields, 0);
		fdlTrimtype.top  = new FormAttachment(wIgnore, margin);
		fdlTrimtype.right= new FormAttachment(middle, 0);
		wlTrimtype.setLayoutData(fdlTrimtype);
		wTrimtype=new CCombo(composite, SWT.BORDER | SWT.READ_ONLY);
        props.setLook(wTrimtype);
		for (int i=0;i< ValueMeta.trimTypeDesc.length;i++)
		{
			wTrimtype.add( ValueMeta.trimTypeDesc[i] );
		}		
		fdTrimtype=new FormData();
		fdTrimtype.left = new FormAttachment(middle, margin);
		fdTrimtype.top  = new FormAttachment(wIgnore, margin);
		fdTrimtype.right= new FormAttachment(right, 0);
		wTrimtype.setLayoutData(fdTrimtype);

		// Precision line
		wlPrecision=new Label(composite, SWT.RIGHT);
		wlPrecision.setText(BaseMessages.getString(PKG, "TextFileImportWizardPage2.Precision.Label"));
        props.setLook(wlPrecision);
		fdlPrecision=new FormData();
		fdlPrecision.left = new FormAttachment(wFields, 0);
		fdlPrecision.top  = new FormAttachment(wTrimtype, margin);
		fdlPrecision.right= new FormAttachment(middle, 0);
		wlPrecision.setLayoutData(fdlPrecision);
		wPrecision=new Text(composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wPrecision);
		fdPrecision=new FormData();
		fdPrecision.left = new FormAttachment(middle, margin);
		fdPrecision.top  = new FormAttachment(wTrimtype, margin);
		fdPrecision.right= new FormAttachment(right, 0);
		wPrecision.setLayoutData(fdPrecision);

		// Currency line
		wlCurrency=new Label(composite, SWT.RIGHT);
		wlCurrency.setText(BaseMessages.getString(PKG, "TextFileImportWizardPage2.Currency.Label"));
        props.setLook(wlCurrency);
		fdlCurrency=new FormData();
		fdlCurrency.left = new FormAttachment(wFields, 0);
		fdlCurrency.top  = new FormAttachment(wPrecision, margin);
		fdlCurrency.right= new FormAttachment(middle, 0);
		wlCurrency.setLayoutData(fdlCurrency);
		wCurrency=new Text(composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wCurrency);
		fdCurrency=new FormData();
		fdCurrency.left = new FormAttachment(middle, margin);
		fdCurrency.top  = new FormAttachment(wPrecision, margin);
		fdCurrency.right= new FormAttachment(right, 0);
		wCurrency.setLayoutData(fdCurrency);
		
		// Decimal line
		wlDecimal=new Label(composite, SWT.RIGHT);
		wlDecimal.setText(BaseMessages.getString(PKG, "TextFileImportWizardPage2.Decimal.Label"));
        props.setLook(wlDecimal);
		fdlDecimal=new FormData();
		fdlDecimal.left = new FormAttachment(wFields, 0);
		fdlDecimal.top  = new FormAttachment(wCurrency, margin);
		fdlDecimal.right= new FormAttachment(middle, 0);
		wlDecimal.setLayoutData(fdlDecimal);
		wDecimal=new Text(composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wDecimal);
		fdDecimal=new FormData();
		fdDecimal.left = new FormAttachment(middle, margin);
		fdDecimal.top  = new FormAttachment(wCurrency, margin);
		fdDecimal.right= new FormAttachment(right, 0);
		wDecimal.setLayoutData(fdDecimal);

		// Group line
		wlGroup=new Label(composite, SWT.RIGHT);
		wlGroup.setText(BaseMessages.getString(PKG, "TextFileImportWizardPage2.Group.Label"));
		props.setLook(wlGroup);
		fdlGroup=new FormData();
		fdlGroup.left = new FormAttachment(wFields, 0);
		fdlGroup.top  = new FormAttachment(wDecimal, margin);
		fdlGroup.right= new FormAttachment(middle, 0);
		wlGroup.setLayoutData(fdlGroup);
		wGroup=new Text(composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wGroup);
        fdGroup=new FormData();
		fdGroup.left = new FormAttachment(middle, margin);
		fdGroup.top  = new FormAttachment(wDecimal, margin);
		fdGroup.right= new FormAttachment(right, 0);
		wGroup.setLayoutData(fdGroup);

		// Ignore checkbox
		wlRepeat=new Label(composite, SWT.RIGHT);
		wlRepeat.setText(BaseMessages.getString(PKG, "TextFileImportWizardPage2.Repeat.Label"));
        props.setLook(wlRepeat);
		fdlRepeat=new FormData();
		fdlRepeat.left = new FormAttachment(wFields, 0);
		fdlRepeat.top  = new FormAttachment(wGroup, margin);
		fdlRepeat.right= new FormAttachment(middle, 0);
		wlRepeat.setLayoutData(fdlRepeat);
		wRepeat=new Button(composite, SWT.CHECK);
        props.setLook(wRepeat);
		fdRepeat=new FormData();
		fdRepeat.left = new FormAttachment(middle, margin);
		fdRepeat.top  = new FormAttachment(wGroup, margin);
		fdRepeat.right= new FormAttachment(right, 0);
		wRepeat.setLayoutData(fdRepeat);

		// Null line
		wlNull=new Label(composite, SWT.RIGHT);
		wlNull.setText(BaseMessages.getString(PKG, "TextFileImportWizardPage2.Null.Label"));
		props.setLook(wlNull);
		fdlNull=new FormData();
		fdlNull.left = new FormAttachment(wFields, 0);
		fdlNull.top  = new FormAttachment(wRepeat, margin);
		fdlNull.right= new FormAttachment(middle, 0);
		wlNull.setLayoutData(fdlNull);
		wNull=new Text(composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wNull);
        fdNull=new FormData();
		fdNull.left = new FormAttachment(middle, margin);
		fdNull.top  = new FormAttachment(wRepeat, margin);
		fdNull.right= new FormAttachment(right, 0);
		wNull.setLayoutData(fdNull);

		// The buttons
		wPrev = new Button(composite, SWT.PUSH);
		wPrev.setText(BaseMessages.getString(PKG, "TextFileImportWizardPage2.Previous.Button"));
		fdPrev=new FormData();
		fdPrev.left   = new FormAttachment(left, margin);
		fdPrev.bottom = new FormAttachment(100, 0);
		wPrev.setLayoutData(fdPrev);

		// Guess button
		wGuess = new Button(composite, SWT.PUSH);
		wGuess.setText(BaseMessages.getString(PKG, "TextFileImportWizardPage2.Guess.Button"));
		fdGuess=new FormData();
		fdGuess.left   = new FormAttachment(wPrev, margin);
		fdGuess.bottom = new FormAttachment(100, 0);
		wGuess.setLayoutData(fdGuess);

		// GuessAll button
		wGuessAll = new Button(composite, SWT.PUSH);
		wGuessAll.setText(BaseMessages.getString(PKG, "TextFileImportWizardPage2.GuessAll.Button"));
		fdGuessAll=new FormData();
		fdGuessAll.left   = new FormAttachment(wGuess, 0);
		fdGuessAll.bottom = new FormAttachment(100, 0);
		wGuessAll.setLayoutData(fdGuessAll);

		wNext = new Button(composite, SWT.PUSH);
		wNext.setText(BaseMessages.getString(PKG, "TextFileImportWizardPage2.Next.Button"));
		fdNext=new FormData();
		fdNext.left   = new FormAttachment(wGuessAll, 0);
		fdNext.bottom = new FormAttachment(100, 0);
		wNext.setLayoutData(fdNext);

		// Sample list on the right...
		wSamples = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        props.setLook(wSamples, Props.WIDGET_STYLE_FIXED);
        fdSamples = new FormData();
		fdSamples.top    = new FormAttachment(  0,  0);
		fdSamples.left   = new FormAttachment( right,  0);
		fdSamples.right  = new FormAttachment(100,  0);
		fdSamples.bottom = new FormAttachment(100,  0);
		wSamples.setLayoutData(fdSamples);

		wFields.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					showInfo();
				}
			}
		);
		
		if (wFields.getItemCount()>0) 
		{
			wFields.select(0);
			showInfo();
		}
		
		wFieldtype.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					int idx = wFields.getSelectionIndex();
					if (idx>=0)
					{
						int valtype = ValueMeta.getType(wFieldtype.getText());
						TextFileInputField field = (TextFileInputField)fields.get(idx);
						field.setType(valtype);
					}
				}
			}
		);

		wFieldname.addModifyListener(new ModifyListener() 
			{
				public void modifyText(ModifyEvent e) 
				{
					int idx = wFields.getSelectionIndex();
					if (idx>=0)
					{
						TextFileInputField field = (TextFileInputField)fields.get(idx);
						field.setName(wFieldname.getText());
						wFields.setItem(idx, wFieldname.getText());
					}
				}
			}
		); 

		wFormat.addModifyListener(new ModifyListener() 
			{
				public void modifyText(ModifyEvent e) 
				{
					int idx = wFields.getSelectionIndex();
					if (idx>=0)
					{
						TextFileInputField field = (TextFileInputField)fields.get(idx);
						field.setFormat(wFormat.getText());
					}
				}
			}
		); 

		wNext.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					int idx = wFields.getSelectionIndex();
					if (idx>=0 && idx<wFields.getItemCount()-1)
					{
						wFields.select(idx+1);
						wFields.showSelection();
						showInfo();
					}
				}
			}
		);

		wPrev.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					int idx = wFields.getSelectionIndex();
					if (idx>0)
					{
						wFields.select(idx-1);
						wFields.showSelection();
						showInfo();
					}
				}
			}
		);
		
		wIgnore.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					int idx = wFields.getSelectionIndex();
					if (idx>=0)
					{
						TextFileInputField field = (TextFileInputField)fields.get(idx);
						field.flipIgnored();
					}
				}
			}
		);
		
		wGuess.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					int idx = wFields.getSelectionIndex();
					if (idx>=0)
					{
						TextFileInputField field = (TextFileInputField)fields.get(idx);
						field.setSamples(wSamples.getItems());
						field.guess();
						showInfo();						
					}
				}
			}
		);

		wGuessAll.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION );
					mb.setMessage(BaseMessages.getString(PKG, "TextFileImportWizardPage2.OverwriteTypeSettings.DialogMessage"));
					mb.setText(BaseMessages.getString(PKG, "TextFileImportWizardPage2.OverwriteTypeSettings.DialogTitle"));
					int answer = mb.open();
					if (answer == SWT.YES)
					{
						for (int i=0;i<fields.size();i++)
						{
							TextFileInputField field = (TextFileInputField)fields.get(i);
							field.setSamples(getRowSamples(field.getPosition(), field.getLength()));
							field.guess();

							wFields.select(i);
							wFields.showSelection();
							showInfo();
						}
					}
					
					if (wFields.getItemCount()>0)
					{
						wFields.select(0);
						wFields.showSelection();
					}
					showInfo();
				}
			}
		);

		wRepeat.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					int idx = wFields.getSelectionIndex();
					if (idx>=0)
					{
						TextFileInputField field = (TextFileInputField)fields.get(idx);
						field.flipRepeated();
					}
				}
			}
		);

		wCurrency.addModifyListener(new ModifyListener() 
			{
				public void modifyText(ModifyEvent e) 
				{
					int idx = wFields.getSelectionIndex();
					if (idx>=0)
					{
						TextFileInputField field = (TextFileInputField)fields.get(idx);
						field.setCurrencySymbol(wCurrency.getText());
					}
				}
			}
		); 

		wGroup.addModifyListener(new ModifyListener() 
			{
				public void modifyText(ModifyEvent e) 
				{
					int idx = wFields.getSelectionIndex();
					if (idx>=0)
					{
						TextFileInputField field = (TextFileInputField)fields.get(idx);
						field.setGroupSymbol(wGroup.getText());
					}
				}
			}
		); 

		wDecimal.addModifyListener(new ModifyListener() 
			{
				public void modifyText(ModifyEvent e) 
				{
					int idx = wFields.getSelectionIndex();
					if (idx>=0)
					{
						TextFileInputField field = (TextFileInputField)fields.get(idx);
						field.setDecimalSymbol(wDecimal.getText());
					}
				}
			}
		); 

		wNull.addModifyListener(new ModifyListener() 
			{
				public void modifyText(ModifyEvent e) 
				{
					int idx = wFields.getSelectionIndex();
					if (idx>=0)
					{
						TextFileInputField field = (TextFileInputField)fields.get(idx);
						field.setNullString(wNull.getText());
					}
				}
			}
		); 
        
        wTrimtype.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent e) 
                {
                    int idx = wFields.getSelectionIndex();
                    if (idx>=0)
                    {
                        int trimType = ValueMeta.getTrimTypeByDesc(wTrimtype.getText());
                        TextFileInputField field = (TextFileInputField)fields.get(idx);
                        field.setTrimType(trimType);
                    }
                }
            }
        );

		// set the composite as the control for this page
		setControl(composite);
	}
	
	
	public boolean canFlipToNextPage()
	{
		refreshFields();
		if (wFields.getItemCount()>0)
		{
			wFields.select(0);
			showInfo();
		}
		return false;
	}	
	
	private void refreshFields()
	{
		wFields.removeAll();
		for (int i=0;i<fields.size();i++)
		{
			wFields.add( ((TextFileInputField)fields.get(i)).getName() );
		}
	}
	
	private String[] getRowSamples(int position, int length)
	{
		if (position<0 || position+length<0) return new String[0];
		
		String retval[] = new String[rows.size()];
		
		for (int i=0;i<rows.size();i++)
		{
			String line = (String)rows.get(i);
	
            if (position<line.length())
            {
    			if (position+length>=line.length()) retval[i] = line.substring(position);
    			else                                retval[i] = line.substring(position, position+length);
            }
            else
            {
                retval[i]="";
            }
		}
		
		return retval;
	}
	
	
	private void showInfo()
	{
		int idx = wFields.getSelectionIndex();
		if (idx>=0)
		{
			TextFileInputField field = (TextFileInputField)fields.get(idx);
			
			String name     = field.getName();
			int from        = field.getPosition();
			int length      = field.getLength();
			String type     = field.getTypeDesc();
			boolean ignore  = field.isIgnored();
			String format   = field.getFormat();
			String trimtype = field.getTrimTypeDesc();
			int precision   = field.getPrecision();
			String currency = field.getCurrencySymbol();
			String decimal  = field.getDecimalSymbol();
			String group    = field.getGroupSymbol();
			boolean repeat  = field.isRepeated(); 
			String nullif   = field.getNullString();
			
			if (name!=null)     wFieldname.setText(name);
			                    wPosition.setText(""+from);
			                    wLength.setText(""+length);
			if (type!=null)     wFieldtype.setText(type);
			                    wIgnore.setSelection(ignore);
			if (format!=null)   wFormat.setText(format);
			if (trimtype!=null) wTrimtype.setText(trimtype);
			                    wPrecision.setText(""+precision);
			if (currency!=null) wCurrency.setText(currency);
			if (decimal!=null)  wDecimal.setText(decimal);
			if (group!=null)    wGroup.setText(group);
			                    wRepeat.setSelection(repeat);
			if (nullif!=null)   wNull.setText(nullif);
			
			// Clear the sample list...
			wSamples.removeAll();
			String samples[] = getRowSamples(from, length);
			for (int i=0;i<samples.length;i++) wSamples.add(samples[i]);
			
		}
	}
}
