/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
 package org.pentaho.di.ui.trans.steps.flattener;

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
import org.pentaho.di.trans.steps.flattener.FlattenerMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;




public class FlattenerDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = FlattenerMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;

    private Label        wlField;
    private CCombo       wField;
    private FormData     fdlField, fdField;
    
	private boolean gotPreviousFields=false;
    
    
	private FlattenerMeta input;

	public FlattenerDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(FlattenerMeta)in;
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
		backupChanged = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "FlattenerDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;
		
		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "FlattenerDialog.Stepname.Label")); //$NON-NLS-1$
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


        // Key field...
        wlField=new Label(shell, SWT.RIGHT);
        wlField.setText(BaseMessages.getString(PKG, "FlattenerDialog.FlattenField.Label")); //$NON-NLS-1$
        props.setLook(wlField);
        fdlField=new FormData();
        fdlField.left = new FormAttachment(0, 0);
        fdlField.right= new FormAttachment(middle, -margin);
        fdlField.top  = new FormAttachment(wStepname, margin);
        wlField.setLayoutData(fdlField);
        wField=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
        props.setLook(wField);
        wField.addModifyListener(lsMod);
        fdField=new FormData();
        fdField.left  = new FormAttachment(middle, 0);
        fdField.top   = new FormAttachment(wStepname, margin);
        fdField.right = new FormAttachment(100, 0);
        wField.setLayoutData(fdField);
        wField.addFocusListener(new FocusListener()
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

		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "FlattenerDialog.TargetField.Label")); //$NON-NLS-1$
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left  = new FormAttachment(0, 0);
		fdlFields.top   = new FormAttachment(wField, margin);
		wlFields.setLayoutData(fdlFields);

		int nrKeyCols=1;
		int nrKeyRows=(input.getTargetField()!=null?input.getTargetField().length:1);
		
		ColumnInfo[] ciKey=new ColumnInfo[nrKeyCols];
		ciKey[0]=new ColumnInfo(BaseMessages.getString(PKG, "FlattenerDialog.ColumnInfo.TargetField"),  ColumnInfo.COLUMN_TYPE_TEXT,   false); //$NON-NLS-1$
		
		wFields=new TableView(transMeta, shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
						      ciKey, 
						      nrKeyRows,  
						      lsMod,
							  props
						      );
		
		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, -margin);
		fdFields.bottom= new FormAttachment(100, -50);
		wFields.setLayoutData(fdFields);

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );



		// Set the shell size, based upon previous time...
		setSize();
				
		getData();
		input.setChanged(backupChanged);

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
		 try{
			 String field=wField.getText();
			 RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			 if(r!=null)
			  {
				 wField.setItems(r.getFieldNames());
			  }
			 if(field!=null) wField.setText(field);
		 	}catch(KettleException ke){
				new ErrorDialog(shell, BaseMessages.getString(PKG, "FlattenerDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "FlattenerDialog.FailedToGetFields.DialogMessage"), ke);
			}
		 	gotPreviousFields=true;
		}
	 }
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		int i;
		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "FlattenerDialog.Log.GettingKeyInfo")); //$NON-NLS-1$
		
        if (input.getFieldName()!= null) wField.setText(input.getFieldName());

		if (input.getTargetField()!=null)
		for (i=0;i<input.getTargetField().length;i++)
		{
			TableItem item = wFields.table.getItem(i);
			if (input.getTargetField()[i]   !=null) item.setText(1, input.getTargetField()[i]);
		}
		
		wStepname.selectAll();
		wFields.setRowNums();
		wFields.optWidth(true);
	}
	
	private void cancel()
	{
		stepname=null;
		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		int nrTargets = wFields.nrNonEmpty();
        
        input.setFieldName( wField.getText() );

		input.allocate(nrTargets);
				
		for (int i=0;i<nrTargets;i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			input.getTargetField()[i]    = item.getText(1);
		}
		stepname = wStepname.getText();
					
		dispose();
	}
}
