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

package org.pentaho.di.ui.trans.steps.calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.calculator.CalculatorMeta;
import org.pentaho.di.trans.steps.calculator.CalculatorMetaFunction;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class CalculatorDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = CalculatorMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private Label        wlStepname;
    private Text         wStepname;
    private FormData     fdlStepname, fdStepname;
    
    private Label        wlFields;
    private TableView    wFields;
    private FormData     fdlFields, fdFields;
    
	private CalculatorMeta currentMeta;
	private CalculatorMeta originalMeta;
    
    private Map<String, Integer> inputFields;
    private ColumnInfo[] colinf;

	public CalculatorDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		
		// The order here is important... currentMeta is looked at for changes
		currentMeta=(CalculatorMeta)in;
		originalMeta=(CalculatorMeta)currentMeta.clone();
        inputFields =new HashMap<String, Integer>();
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
 		props.setLook(shell);
        setShellImage(shell, currentMeta);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				currentMeta.setChanged();
			}
		};
		changed = currentMeta.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "CalculatorDialog.DialogTitle"));
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
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
		
        wlFields=new Label(shell, SWT.NONE);
        wlFields.setText(BaseMessages.getString(PKG, "CalculatorDialog.Fields.Label"));
 		props.setLook(wlFields);
        fdlFields=new FormData();
        fdlFields.left = new FormAttachment(0, 0);
        fdlFields.top  = new FormAttachment(wStepname, margin);
        wlFields.setLayoutData(fdlFields);
        
        final int FieldsRows=currentMeta.getCalculation()!=null ? currentMeta.getCalculation().length : 1;
        
        colinf=new ColumnInfo[]
               {
                    new ColumnInfo(BaseMessages.getString(PKG, "CalculatorDialog.NewFieldColumn.Column"),     ColumnInfo.COLUMN_TYPE_TEXT,   false),
                    new ColumnInfo(BaseMessages.getString(PKG, "CalculatorDialog.CalculationColumn.Column"),  ColumnInfo.COLUMN_TYPE_TEXT,   false),
                    new ColumnInfo(BaseMessages.getString(PKG, "CalculatorDialog.FieldAColumn.Column"),       ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
                    new ColumnInfo(BaseMessages.getString(PKG, "CalculatorDialog.FieldBColumn.Column"),       ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
                    new ColumnInfo(BaseMessages.getString(PKG, "CalculatorDialog.FieldCColumn.Column"),       ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
                    new ColumnInfo(BaseMessages.getString(PKG, "CalculatorDialog.ValueTypeColumn.Column"),    ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes() ),
                    new ColumnInfo(BaseMessages.getString(PKG, "CalculatorDialog.LengthColumn.Column"),       ColumnInfo.COLUMN_TYPE_TEXT,   false),
                    new ColumnInfo(BaseMessages.getString(PKG, "CalculatorDialog.PrecisionColumn.Column"),    ColumnInfo.COLUMN_TYPE_TEXT,   false),
                    new ColumnInfo(BaseMessages.getString(PKG, "CalculatorDialog.RemoveColumn.Column"),       ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { BaseMessages.getString(PKG, "System.Combo.No"), BaseMessages.getString(PKG, "System.Combo.Yes") } ),
                    new ColumnInfo(BaseMessages.getString(PKG, "CalculatorDialog.ConversionMask.Column"),     ColumnInfo.COLUMN_TYPE_FORMAT, 6),
                    new ColumnInfo(BaseMessages.getString(PKG, "CalculatorDialog.DecimalSymbol.Column"),      ColumnInfo.COLUMN_TYPE_TEXT,   false),
                    new ColumnInfo(BaseMessages.getString(PKG, "CalculatorDialog.GroupingSymbol.Column"),     ColumnInfo.COLUMN_TYPE_TEXT,   false),
                    new ColumnInfo(BaseMessages.getString(PKG, "CalculatorDialog.CurrencySymbol.Column"),     ColumnInfo.COLUMN_TYPE_TEXT,   false),
               };
        
        colinf[1].setSelectionAdapter(
            new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    EnterSelectionDialog esd = new EnterSelectionDialog(shell, CalculatorMetaFunction.calcLongDesc, BaseMessages.getString(PKG, "CalculatorDialog.SelectCalculationType.Title"), BaseMessages.getString(PKG, "CalculatorDialog.SelectCalculationType.Message"));
                    String string = esd.open();
                    if (string!=null)
                    {
                        TableView tv = (TableView)e.widget;
                        tv.setText(string, e.x, e.y);
                        currentMeta.setChanged();
                    }
                }
            }
        );

        wFields=new TableView(transMeta, shell, 
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
        fdFields.bottom= new FormAttachment(100, -50);
        wFields.setLayoutData(fdFields);

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
                        logError(BaseMessages.getString(PKG, "CalculatorDialog.Log.UnableToFindInput"));
                    }
                }
            }
        };
        new Thread(runnable).start();
        
        wFields.addModifyListener(new ModifyListener()
            {
                public void modifyText(ModifyEvent arg0)
                {
                    // Now set the combo's
                    shell.getDisplay().asyncExec(new Runnable()
                    {
                        public void run()
                        {
                            setComboBoxes();
                        }
                    
                    });
                    
                }
            }
        );
        
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

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
		currentMeta.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	protected void setComboBoxes()
    {
        // Something was changed in the row.
        //
        final Map<String, Integer> fields = new HashMap<String, Integer>();
        
        // Add the currentMeta fields...
        fields.putAll(inputFields);
        
        shell.getDisplay().syncExec(new Runnable()
            {
                public void run()
                {
                    // Add the newly create fields.
                    //
                	int nrNonEmptyFields = wFields.nrNonEmpty(); 
                    for (int i=0;i<nrNonEmptyFields;i++)
                    {
                        TableItem item = wFields.getNonEmpty(i);
                        fields.put(item.getText(1), Integer.valueOf(1000000+i));  // The number is just to debug the origin of the fieldname
                    }
                }
            }
        );
        
        Set<String> keySet = fields.keySet();
        List<String> entries = new ArrayList<String>(keySet);
        
        String fieldNames[] = (String[]) entries.toArray(new String[entries.size()]);

        Const.sortStrings(fieldNames);
        colinf[2].setComboValues(fieldNames);
        colinf[3].setComboValues(fieldNames);
        colinf[4].setComboValues(fieldNames);
    }

    /**
	 * Copy information from the meta-data currentMeta to the dialog fields.
	 */ 
	public void getData()
	{
		wStepname.selectAll();
        
        if (currentMeta.getCalculation()!=null)
        for (int i=0;i<currentMeta.getCalculation().length;i++)
        {
            CalculatorMetaFunction fn = currentMeta.getCalculation()[i];
            TableItem item = wFields.table.getItem(i);
            item.setText( 1, Const.NVL(fn.getFieldName(), ""));
            item.setText( 2, Const.NVL(fn.getCalcTypeLongDesc(), ""));
            item.setText( 3, Const.NVL(fn.getFieldA(), ""));
            item.setText( 4, Const.NVL(fn.getFieldB(), ""));
            item.setText( 5, Const.NVL(fn.getFieldC(), ""));
            item.setText( 6, Const.NVL(ValueMeta.getTypeDesc(fn.getValueType()), ""));
            if (fn.getValueLength()>=0) {
            	item.setText( 7, ""+fn.getValueLength());
            }
            if (fn.getValuePrecision()>=0) {
            	item.setText( 8, ""+fn.getValuePrecision());
            }
            item.setText( 9, fn.isRemovedFromResult()?BaseMessages.getString(PKG, "System.Combo.Yes"):BaseMessages.getString(PKG, "System.Combo.No"));
            item.setText(10, Const.NVL(fn.getConversionMask(), ""));
            item.setText(11, Const.NVL(fn.getDecimalSymbol(), ""));
            item.setText(12, Const.NVL(fn.getGroupingSymbol(), ""));
            item.setText(13, Const.NVL(fn.getCurrencySymbol(), ""));
        }
        
        wFields.setRowNums();
        wFields.optWidth(true);
	}
	
	private void cancel()
	{
		stepname=null;
		currentMeta.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		stepname = wStepname.getText(); // return value
		
    	int nrNonEmptyFields = wFields.nrNonEmpty(); 
        currentMeta.allocate(nrNonEmptyFields);
        
        for (int i=0;i<nrNonEmptyFields;i++)
        {
            TableItem item = wFields.getNonEmpty(i);
            
            String fieldName       = item.getText(1);
            int    calcType        = CalculatorMetaFunction.getCalcFunctionType(item.getText(2));
            String fieldA          = item.getText(3);
            String fieldB          = item.getText(4);
            String fieldC          = item.getText(5);
            int    valueType       = ValueMeta.getType( item.getText(6) );
            int    valueLength     = Const.toInt( item.getText(7), -1 );
            int    valuePrecision  = Const.toInt( item.getText(8), -1 );
            boolean removed        = BaseMessages.getString(PKG, "System.Combo.Yes").equalsIgnoreCase( item.getText(9) );
            String conversionMask  = item.getText(10);
            String decimalSymbol   = item.getText(11);
            String groupingSymbol  = item.getText(12);
            String currencySymbol  = item.getText(13);
                        
            currentMeta.getCalculation()[i] = new CalculatorMetaFunction(fieldName, calcType, fieldA, fieldB, fieldC, valueType, valueLength, valuePrecision, removed, conversionMask, decimalSymbol, groupingSymbol, currencySymbol);
        }
        
        if ( ! originalMeta.equals(currentMeta) )
        {
        	currentMeta.setChanged();
        	changed = currentMeta.hasChanged();
        }
        
		dispose();
	}
}
