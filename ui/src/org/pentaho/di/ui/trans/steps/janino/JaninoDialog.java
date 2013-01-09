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

package org.pentaho.di.ui.trans.steps.janino;

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
import org.pentaho.di.trans.steps.janino.JaninoMeta;
import org.pentaho.di.trans.steps.janino.JaninoMetaFunction;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class JaninoDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = JaninoMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private Label        wlStepname;
    private Text         wStepname;
    private FormData     fdlStepname, fdStepname;
    
    private Label        wlFields;
    private TableView    wFields;
    private FormData     fdlFields, fdFields;
    
	private JaninoMeta currentMeta;
	private JaninoMeta originalMeta;
    
    private Map<String, Integer> inputFields;
    private ColumnInfo[] colinf;

	public JaninoDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		
		// The order here is important... currentMeta is looked at for changes
		currentMeta=(JaninoMeta)in;
		originalMeta=(JaninoMeta)currentMeta.clone();
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
		shell.setText(BaseMessages.getString(PKG, "JaninoDialog.DialogTitle"));
		
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
        wlFields.setText(BaseMessages.getString(PKG, "JaninoDialog.Fields.Label"));
 		props.setLook(wlFields);
        fdlFields=new FormData();
        fdlFields.left = new FormAttachment(0, 0);
        fdlFields.top  = new FormAttachment(wStepname, margin);
        wlFields.setLayoutData(fdlFields);
        
        final int FieldsRows=currentMeta.getFormula()!=null ? currentMeta.getFormula().length : 1;
        
        colinf=new ColumnInfo[]
               {
                    new ColumnInfo(BaseMessages.getString(PKG, "JaninoDialog.NewField.Column"),     ColumnInfo.COLUMN_TYPE_TEXT,   false),
                    new ColumnInfo(BaseMessages.getString(PKG, "JaninoDialog.Janino.Column"),      ColumnInfo.COLUMN_TYPE_TEXT,   false),
                    new ColumnInfo(BaseMessages.getString(PKG, "JaninoDialog.ValueType.Column"),    ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes() ),
                    new ColumnInfo(BaseMessages.getString(PKG, "JaninoDialog.Length.Column"),       ColumnInfo.COLUMN_TYPE_TEXT,   false),
                    new ColumnInfo(BaseMessages.getString(PKG, "JaninoDialog.Precision.Column"),    ColumnInfo.COLUMN_TYPE_TEXT,   false),
                    new ColumnInfo(BaseMessages.getString(PKG, "JaninoDialog.Replace.Column"),      ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {  } ),
               };   
        
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
                            inputFields.put(row.getValueMeta(i).getName(), new Integer(i));
                        }
                        
                        setComboBoxes();
                    }
                    catch(KettleException e)
                    {
                        logError(BaseMessages.getString(PKG, "JaninoDialog.Log.UnableToFindInput"));
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
                	/*
                    int nrNonEmptyFields = wFields.nrNonEmpty();
                    for (int i=0;i<nrNonEmptyFields;i++)
                    {
                        TableItem item = wFields.getNonEmpty(i);
                        fields.put(item.getText(1), new Integer(1000000+i));  // The number is just to debug the origin of the fieldname
                    }
                    */
                    
                    Set<String> keySet = fields.keySet();
                    List<String> entries = new ArrayList<String>(keySet);
                    
                    String fieldNames[] = entries.toArray(new String[entries.size()]);

                    Const.sortStrings(fieldNames);
                    
                    colinf[5].setComboValues(fieldNames);
                }
            }
        );
        
    }

    /**
	 * Copy information from the meta-data currentMeta to the dialog fields.
	 */ 
	public void getData()
	{
		wStepname.selectAll();
        
        if (currentMeta.getFormula()!=null)
        for (int i=0;i<currentMeta.getFormula().length;i++)
        {
            JaninoMetaFunction fn = currentMeta.getFormula()[i];
            TableItem item = wFields.table.getItem(i);
            item.setText(1, Const.NVL(fn.getFieldName(), ""));
            item.setText(2, Const.NVL(fn.getFormula(), ""));
            item.setText(3, Const.NVL(ValueMeta.getTypeDesc(fn.getValueType()), ""));
            if (fn.getValueLength()>=0) item.setText(4, ""+fn.getValueLength());
            if (fn.getValuePrecision()>=0) item.setText(5, ""+fn.getValuePrecision());
            item.setText(6, Const.NVL(fn.getReplaceField(), ""));
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
		
        currentMeta.allocate(wFields.nrNonEmpty());
        
        int nrNonEmptyFields = wFields.nrNonEmpty();
        for (int i=0;i<nrNonEmptyFields;i++)
        {
            TableItem item = wFields.getNonEmpty(i);
            
            String fieldName       = item.getText(1);
            String formula         = item.getText(2);
            int    valueType       = ValueMeta.getType( item.getText(3) );
            int    valueLength     = Const.toInt( item.getText(4), -1 );
            int    valuePrecision  = Const.toInt( item.getText(5), -1 );
            String replaceField    = item.getText(6);
                        
            currentMeta.getFormula()[i] = new JaninoMetaFunction(fieldName, formula, valueType, valueLength, valuePrecision, replaceField);
        }
        
        if ( ! originalMeta.equals(currentMeta) )
        {
        	currentMeta.setChanged();
        	changed = currentMeta.hasChanged();
        }
        
		dispose();
	}
}
