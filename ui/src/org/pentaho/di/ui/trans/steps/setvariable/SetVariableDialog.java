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

package org.pentaho.di.ui.trans.steps.setvariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
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
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.setvariable.SetVariableMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;


public class SetVariableDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = SetVariableMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String STRING_USAGE_WARNING_PARAMETER = "SetVariableUsageWarning"; //$NON-NLS-1$
    
	private Label        wlStepname;
	private Text         wStepname;
    private FormData     fdlStepname, fdStepname;

	private Label        wlFormat;
	private Button       wFormat;
    private FormData     fdlFormat, fdFormat;

	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;
	
	private SetVariableMeta input;

    private Map<String, Integer> inputFields;
    
    private ColumnInfo[] colinf;
	
	public SetVariableDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(SetVariableMeta)in;
        inputFields =new HashMap<String, Integer>();
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
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "SetVariableDialog.DialogTitle")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "SetVariableDialog.Stepname.Label")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		wlFormat=new Label(shell, SWT.RIGHT);
		wlFormat.setText(BaseMessages.getString(PKG, "SetVariableDialog.Format.Label")); //$NON-NLS-1$
		wlFormat.setToolTipText(BaseMessages.getString(PKG, "SetVariableDialog.Format.Tooltip")); //$NON-NLS-1$
 		props.setLook(wlFormat);
		fdlFormat=new FormData();
		fdlFormat.left = new FormAttachment(0, 0);
		fdlFormat.right= new FormAttachment(middle, -margin);
		fdlFormat.top  = new FormAttachment(wStepname, margin);
		wlFormat.setLayoutData(fdlFormat);
		wFormat=new Button(shell, SWT.CHECK);
		wFormat.setToolTipText(BaseMessages.getString(PKG, "SetVariableDialog.Format.Tooltip")); //$NON-NLS-1$
 		props.setLook(wFormat);
		fdFormat=new FormData();
		fdFormat.left = new FormAttachment(middle, 0);
		fdFormat.top  = new FormAttachment(wStepname, margin);
		wFormat.setLayoutData(fdFormat);

		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "SetVariableDialog.Fields.Label")); //$NON-NLS-1$
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wFormat, margin);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsRows=input.getFieldName().length;
		colinf=new ColumnInfo[4];
		colinf[0]=  new ColumnInfo(BaseMessages.getString(PKG, "SetVariableDialog.Fields.Column.FieldName"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false);//$NON-NLS-1$
		colinf[1]=  new ColumnInfo(BaseMessages.getString(PKG, "SetVariableDialog.Fields.Column.VariableName"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$
		colinf[2]=  new ColumnInfo(BaseMessages.getString(PKG, "SetVariableDialog.Fields.Column.VariableType"), ColumnInfo.COLUMN_TYPE_CCOMBO, SetVariableMeta.getVariableTypeDescriptions(), false); //$NON-NLS-1$
		colinf[3]=  new ColumnInfo(BaseMessages.getString(PKG, "SetVariableDialog.Fields.Column.DefaultValue"), ColumnInfo.COLUMN_TYPE_TEXT, false);
		colinf[3].setUsingVariables(true);
		colinf[3].setToolTip(BaseMessages.getString(PKG, "SetVariableDialog.Fields.Column.DefaultValue.Tooltip"));
		
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
                    	logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
                    }
                }
            }
        };
        new Thread(runnable).start();
				
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
        wGet=new Button(shell, SWT.PUSH);
        wGet.setText(BaseMessages.getString(PKG, "System.Button.GetFields")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel , wGet }, margin, wFields);

		// Add listeners
		lsCancel = new Listener() { public void handleEvent(Event e) { cancel(); } };
        lsGet    = new Listener() { public void handleEvent(Event e) { get(); }    };
		lsOK     = new Listener() { public void handleEvent(Event e) { ok();       } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
        wGet.addListener   (SWT.Selection, lsGet   );
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
	protected void setComboBoxes()
    {
        // Something was changed in the row.
        //
        final Map<String, Integer> fields = new HashMap<String, Integer>();
        
        // Add the currentMeta fields...
        fields.putAll(inputFields);
        
        Set<String> keySet = fields.keySet();
        List<String> entries = new ArrayList<String>(keySet);

        String fieldNames[] = (String[]) entries.toArray(new String[entries.size()]);

        Const.sortStrings(fieldNames);
        colinf[0].setComboValues(fieldNames);
    }
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wStepname.setText(stepname);
		
        for (int i=0;i<input.getFieldName().length;i++)
		{
			TableItem item = wFields.table.getItem(i);
			String src = input.getFieldName()[i];
			String tgt = input.getVariableName()[i];
			String typ = SetVariableMeta.getVariableTypeDescription(input.getVariableType()[i]);
			String tvv = input.getDefaultValue()[i];
			
			if (src!=null) item.setText(1, src);
			if (tgt!=null) item.setText(2, tgt);
            if (typ!=null) item.setText(3, typ);
            if (tvv!=null) item.setText(4, tvv);
		}

        wFormat.setSelection(input.isUsingFormatting());
        
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

		int count = wFields.nrNonEmpty();
		input.allocate(count);
		
		for (int i=0;i<count;i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			input.getFieldName()[i]    = item.getText(1);
			input.getVariableName()[i] = item.getText(2);
            input.getVariableType()[i] = SetVariableMeta.getVariableType(item.getText(3));
            input.getDefaultValue()[i] = item.getText(4);
		}
		
		input.setUsingFormatting(wFormat.getSelection());
		
        // Show a warning (optional)
        //
        if ( "Y".equalsIgnoreCase( props.getCustomParameter(STRING_USAGE_WARNING_PARAMETER, "Y") )) //$NON-NLS-1$ //$NON-NLS-2$
        {
            MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
                 BaseMessages.getString(PKG, "SetVariableDialog.UsageWarning.DialogTitle"),  //$NON-NLS-1$
                 null,
                 BaseMessages.getString(PKG, "SetVariableDialog.UsageWarning.DialogMessage", Const.CR )+Const.CR, //$NON-NLS-1$ //$NON-NLS-2$
                 MessageDialog.WARNING,
                 new String[] { BaseMessages.getString(PKG, "SetVariableDialog.UsageWarning.Option1") }, //$NON-NLS-1$
                 0,
                 BaseMessages.getString(PKG, "SetVariableDialog.UsageWarning.Option2"), //$NON-NLS-1$
                 "N".equalsIgnoreCase( props.getCustomParameter(STRING_USAGE_WARNING_PARAMETER, "Y") ) //$NON-NLS-1$ //$NON-NLS-2$
            );
            MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
            md.open();
            props.setCustomParameter(STRING_USAGE_WARNING_PARAMETER, md.getToggleState()?"N":"Y"); //$NON-NLS-1$ //$NON-NLS-2$
            props.saveProps();
        }
		
		dispose();
	}
    
    private void get()
    {
        try
        {
            RowMetaInterface r = transMeta.getPrevStepFields(stepname);
            if (r!=null && !r.isEmpty())
            {
                BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1 }, new int[] {}, -1, -1, new TableItemInsertListener()
                    {
                	    public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
                        {
                            tableItem.setText(2, v.getName().toUpperCase());
                            tableItem.setText(3, SetVariableMeta.getVariableTypeDescription(SetVariableMeta.VARIABLE_TYPE_ROOT_JOB));
                            return true;
                        }
                    }
                );
            }
        }
        catch(KettleException ke)
        {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "SetVariableDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "Set.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}
