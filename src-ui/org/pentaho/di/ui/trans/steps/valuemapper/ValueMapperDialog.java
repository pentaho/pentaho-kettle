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

package org.pentaho.di.ui.trans.steps.valuemapper;

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
import org.pentaho.di.trans.steps.valuemapper.ValueMapperMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class ValueMapperDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = ValueMapperMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlStepname;
	private Text         wStepname;
    private FormData     fdlStepname, fdStepname;
    
    private Label        wlFieldname;
	private CCombo       wFieldname;
    private FormData     fdlFieldname, fdFieldname;

    private Label        wlTargetFieldname;
    private Text         wTargetFieldname;
    private FormData     fdlTargetFieldname, fdTargetFieldname;

    private Label        wlNonMatchDefault;
    private Text         wNonMatchDefault;
    private FormData     fdlNonMatchDefault, fdNonMatchDefault;
    
	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;
	
	private ValueMapperMeta input;

	private boolean gotPreviousFields=false;
	
	public ValueMapperDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(ValueMapperMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "ValueMapperDialog.DialogTitle")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "ValueMapperDialog.Stepname.Label")); //$NON-NLS-1$
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

        // Fieldname line
        wlFieldname=new Label(shell, SWT.RIGHT);
        wlFieldname.setText(BaseMessages.getString(PKG, "ValueMapperDialog.FieldnameToUser.Label")); //$NON-NLS-1$
        props.setLook(wlFieldname);
        fdlFieldname=new FormData();
        fdlFieldname.left = new FormAttachment(0, 0);
        fdlFieldname.right= new FormAttachment(middle, -margin);
        fdlFieldname.top  = new FormAttachment(wStepname, margin);
        wlFieldname.setLayoutData(fdlFieldname);

        wFieldname=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
        props.setLook(wFieldname);
        wFieldname.addModifyListener(lsMod);
        fdFieldname=new FormData();
        fdFieldname.left = new FormAttachment(middle, 0);
        fdFieldname.top  = new FormAttachment(wStepname, margin);
        fdFieldname.right= new FormAttachment(100, 0);
        wFieldname.setLayoutData(fdFieldname);
        wFieldname.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                getFields();
                shell.setCursor(null);
                busy.dispose();
            }
        }
    );
        // TargetFieldname line
        wlTargetFieldname=new Label(shell, SWT.RIGHT);
        wlTargetFieldname.setText(BaseMessages.getString(PKG, "ValueMapperDialog.TargetFieldname.Label")); //$NON-NLS-1$
        props.setLook(wlTargetFieldname);
        fdlTargetFieldname=new FormData();
        fdlTargetFieldname.left = new FormAttachment(0, 0);
        fdlTargetFieldname.right= new FormAttachment(middle, -margin);
        fdlTargetFieldname.top  = new FormAttachment(wFieldname, margin);
        wlTargetFieldname.setLayoutData(fdlTargetFieldname);
        wTargetFieldname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wTargetFieldname);
        wTargetFieldname.addModifyListener(lsMod);
        fdTargetFieldname=new FormData();
        fdTargetFieldname.left = new FormAttachment(middle, 0);
        fdTargetFieldname.top  = new FormAttachment(wFieldname, margin);
        fdTargetFieldname.right= new FormAttachment(100, 0);
        wTargetFieldname.setLayoutData(fdTargetFieldname);
        
        // Non match default line
        wlNonMatchDefault=new Label(shell, SWT.RIGHT);
        wlNonMatchDefault.setText(BaseMessages.getString(PKG, "ValueMapperDialog.NonMatchDefault.Label")); //$NON-NLS-1$
        props.setLook(wlNonMatchDefault);
        fdlNonMatchDefault=new FormData();
        fdlNonMatchDefault.left = new FormAttachment(0, 0);
        fdlNonMatchDefault.right= new FormAttachment(middle, -margin);
        fdlNonMatchDefault.top  = new FormAttachment(wTargetFieldname, margin);
        wlNonMatchDefault.setLayoutData(fdlNonMatchDefault);
        wNonMatchDefault=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wNonMatchDefault);
        wNonMatchDefault.addModifyListener(lsMod);
        fdNonMatchDefault=new FormData();
        fdNonMatchDefault.left = new FormAttachment(middle, 0);
        fdNonMatchDefault.top  = new FormAttachment(wTargetFieldname, margin);
        fdNonMatchDefault.right= new FormAttachment(100, 0);
        wNonMatchDefault.setLayoutData(fdNonMatchDefault);        
        
		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "ValueMapperDialog.Fields.Label")); //$NON-NLS-1$
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wNonMatchDefault, margin);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsCols=2;
		final int FieldsRows=input.getSourceValue().length;
		
		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
		colinf[0]=new ColumnInfo(BaseMessages.getString(PKG, "ValueMapperDialog.Fields.Column.SourceValue"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$
		colinf[1]=new ColumnInfo(BaseMessages.getString(PKG, "ValueMapperDialog.Fields.Column.TargetValue"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$

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

				
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wFields);

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
	 private void getFields()
		{
		 if(!gotPreviousFields)
		 {
			 gotPreviousFields=true;
			try
			{
				String fieldname=wFieldname.getText();
				
				wFieldname.removeAll();
				RowMetaInterface r = transMeta.getPrevStepFields(stepname);
				if (r!=null)
				{
					wFieldname.setItems(r.getFieldNames());
					if(fieldname!=null) wFieldname.setText(fieldname);
				}
			}
			catch(KettleException ke)
			{
				new ErrorDialog(shell, BaseMessages.getString(PKG, "ValueMapperDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "ValueMapperDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
		 }
		}
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wStepname.setText(stepname);
		
        if (input.getFieldToUse()!=null) wFieldname.setText(input.getFieldToUse());
        if (input.getTargetField()!=null) wTargetFieldname.setText(input.getTargetField());
        if (input.getNonMatchDefault()!=null) wNonMatchDefault.setText(input.getNonMatchDefault());
        
		for (int i=0;i<input.getSourceValue().length;i++)
		{
			TableItem item = wFields.table.getItem(i);
			String src = input.getSourceValue()[i];
			String tgt = input.getTargetValue()[i];
			
			if (src!=null) item.setText(1, src);
			if (tgt!=null) item.setText(2, tgt);
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

        input.setFieldToUse(wFieldname.getText());
        input.setTargetField(wTargetFieldname.getText());
        input.setNonMatchDefault(wNonMatchDefault.getText());
        
		int count = wFields.nrNonEmpty();
		input.allocate(count);
		
		for (int i=0;i<count;i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			input.getSourceValue()[i]  = Const.isEmpty(item.getText(1))?null:item.getText(1);
			input.getTargetValue()[i]  = item.getText(2);
		}
		dispose();
	}
}