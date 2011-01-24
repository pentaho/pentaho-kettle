
 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Samatar Hassan 
 * The Initial Developer is Samatar Hassan
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

/*
 * Created on 18-06-2008
 *
 */

package org.pentaho.di.ui.trans.steps.checksum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.checksum.CheckSumMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;


public class CheckSumDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = CheckSumMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CheckSumMeta input;
    private Label wlType;

    private CCombo wType;
    private FormData fdlType, fdType;

	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;
	
	private Label        wlResult;
	private Text         wResult;
	private FormData     fdlResult, fdResult;
	
	private Label        wlCompatibility;
	private Button       wCompatibility;
	private FormData     fdlCompatibility, fdCompatibility;
	
	private ColumnInfo[] colinf;
	
    private Map<String, Integer> inputFields;
    
	private Label wlResultType;
    private CCombo wResultType;
    private FormData fdlResultType, fdResultType;
	
	public CheckSumDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(CheckSumMeta)in;
        inputFields =new HashMap<String, Integer>();
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
		shell.setText(BaseMessages.getString(PKG, "CheckSumDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "CheckSumDialog.Stepname.Label")); //$NON-NLS-1$
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
		
		 // Type
        wlType = new Label(shell, SWT.RIGHT);
        wlType.setText(BaseMessages.getString(PKG, "CheckSumDialog.Type.Label"));
        props.setLook(wlType);
        fdlType = new FormData();
        fdlType.left = new FormAttachment(0, 0);
        fdlType.right = new FormAttachment(middle, -margin);
        fdlType.top = new FormAttachment(wStepname, margin);
        wlType.setLayoutData(fdlType);
        wType = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        wType.add(BaseMessages.getString(PKG, "CheckSumDialog.Type.CRC32"));
        wType.add(BaseMessages.getString(PKG, "CheckSumDialog.Type.ADLER32"));
        wType.add(BaseMessages.getString(PKG, "CheckSumDialog.Type.MD5"));
        wType.add(BaseMessages.getString(PKG, "CheckSumDialog.Type.SHA1"));
        wType.select(0); 
        props.setLook(wType);
        fdType = new FormData();
        fdType.left = new FormAttachment(middle, 0);
        fdType.top = new FormAttachment(wStepname, margin);
        fdType.right = new FormAttachment(100, 0);
        wType.setLayoutData(fdType);
        wType.addSelectionListener(new SelectionAdapter()
        {
        
            public void widgetSelected(SelectionEvent e)
            {
                activeResultType();
            }
        }
        );  
        
		 // ResultType
        wlResultType = new Label(shell, SWT.RIGHT);
        wlResultType.setText(BaseMessages.getString(PKG, "CheckSumDialog.ResultType.Label"));
        props.setLook(wlResultType);
        fdlResultType = new FormData();
        fdlResultType.left = new FormAttachment(0, 0);
        fdlResultType.right = new FormAttachment(middle, -margin);
        fdlResultType.top = new FormAttachment(wType, 2*margin);
        wlResultType.setLayoutData(fdlResultType);
        wResultType = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        wResultType.setItems(CheckSumMeta.resultTypeDesc);
        wResultType.select(0); 
        props.setLook(wResultType);
        fdResultType = new FormData();
        fdResultType.left = new FormAttachment(middle, 0);
        fdResultType.top = new FormAttachment(wType, 2*margin);
        fdResultType.right = new FormAttachment(100, 0);
        wResultType.setLayoutData(fdResultType);
        
        // Result line...
		wlResult=new Label(shell, SWT.RIGHT);
		wlResult.setText(BaseMessages.getString(PKG, "CheckSumDialog.Result.Label")); //$NON-NLS-1$
 		props.setLook(wlResult);
		fdlResult=new FormData();
		fdlResult.left = new FormAttachment(0, 0);
		fdlResult.right= new FormAttachment(middle, -margin);
		fdlResult.top  = new FormAttachment(wResultType, margin*2);
		wlResult.setLayoutData(fdlResult);
		wResult=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wResult);
		wResult.addModifyListener(lsMod);
		fdResult=new FormData();
		fdResult.left = new FormAttachment(middle, 0);
		fdResult.top  = new FormAttachment(wResultType, margin*2);
		fdResult.right= new FormAttachment(100, 0);
		wResult.setLayoutData(fdResult);
        
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "System.Button.GetFields"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		setButtonPositions(new Button[] { wOK, wGet, wCancel }, margin, null);
        
	   // Are we operating in compatibility mode?
    wlCompatibility=new Label(shell, SWT.RIGHT);
    wlCompatibility.setText(BaseMessages.getString(PKG, "CheckSumDialog.CompatibilityMode.Label")); //$NON-NLS-1$
    props.setLook(wlCompatibility);
    fdlCompatibility=new FormData();
    fdlCompatibility.left  = new FormAttachment(0, 0);
    fdlCompatibility.top   = new FormAttachment(wResult, margin);
    fdlCompatibility.right = new FormAttachment(middle, -margin);
    wlCompatibility.setLayoutData(fdlCompatibility);
    wCompatibility=new Button(shell, SWT.CHECK);
    wCompatibility.setToolTipText(BaseMessages.getString(PKG, "CheckSumDialog.CompatibilityMode.Tooltip")); //$NON-NLS-1$
    props.setLook(wCompatibility);
    fdCompatibility=new FormData();
    fdCompatibility.left  = new FormAttachment(middle, 0);
    fdCompatibility.top   = new FormAttachment(wResult, margin);
    fdCompatibility.right = new FormAttachment(100, 0);
    wCompatibility.setLayoutData(fdCompatibility);
    SelectionAdapter lsSelR = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
                input.setChanged();
            }
        };
    wCompatibility.addSelectionListener(lsSelR);
		
        // Table with fields
		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "CheckSumDialog.Fields.Label"));
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wCompatibility, margin);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsCols=1;
		final int FieldsRows=input.getFieldName().length;
		
		colinf=new ColumnInfo[FieldsCols];
		colinf[0]=new ColumnInfo(BaseMessages.getString(PKG, "CheckSumDialog.Fieldname.Column"),  
				ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false);
		wFields=new TableView(transMeta,shell,
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
		fdFields.bottom= new FormAttachment(wOK, -2*margin);
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
		activeResultType();
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	private void activeResultType()
	{
		boolean active=wType.getSelectionIndex()==2 ||wType.getSelectionIndex()==3;
		wlResultType.setEnabled(active);
		wResultType.setEnabled(active);
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
                            tableItem.setText(2, BaseMessages.getString(PKG, "System.Combo.Yes"));
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
	
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wType.select(input.getTypeByDesc());
		if (input.getResultFieldName()!=null) wResult.setText(input.getResultFieldName());
		wResultType.setText(CheckSumMeta.getResultTypeDesc(input.getResultType()));
		wCompatibility.setSelection(input.isCompatibilityMode());

		Table table = wFields.table;
		if (input.getFieldName().length>0) table.removeAll();
		for (int i=0;i<input.getFieldName().length;i++)
		{
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(0, ""+(i+1));
			ti.setText(1, input.getFieldName()[i]);
		}

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

		if(wType.getSelectionIndex()<0)
			input.setCheckSumType(0); 
		else
			input.setCheckSumType(wType.getSelectionIndex());
		
		input.setResultFieldName( wResult.getText() );
		input.setResultType(CheckSumMeta.getResultTypeByDesc(wResultType.getText()));
		
		input.setCompatibilityMode(wCompatibility.getSelection());
		
		int nrfields = wFields.nrNonEmpty();
		input.allocate(nrfields);
		for (int i=0;i<nrfields;i++)
		{
			TableItem ti = wFields.getNonEmpty(i);
			input.getFieldName()[i] = ti.getText(1);
		}
		dispose();
	}
}
