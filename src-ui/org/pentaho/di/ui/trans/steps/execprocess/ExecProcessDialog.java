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

package org.pentaho.di.ui.trans.steps.execprocess;


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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.execprocess.ExecProcessMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class ExecProcessDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = ExecProcessMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlProcess;
	private CCombo       wProcess;
	private FormData     fdlProcess, fdProcess;

	private Label        wlResult;
	private TextVar      wResult;
	private FormData     fdlResult, fdResult;
	
	private Label        wlExitValue;
	private TextVar      wExitValue;
	private FormData     fdlExitValue, fdExitValue;
	
	private Label        wlError;
	private TextVar      wError;
	private FormData     fdlError, fdError;

	private Group wOutputFields;
	private FormData fdOutputFields;
	
	
	private Label        wlFailWhenNotSuccess;
	private Button       wFailWhenNotSuccess;
	private FormData     fdlFailWhenNotSuccess, fdFailWhenNotSuccess;

	private ExecProcessMeta input;
	private boolean gotPreviousFields=false;

	public ExecProcessDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(ExecProcessMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "ExecProcessDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin=Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "ExecProcessDialog.Stepname.Label")); //$NON-NLS-1$
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

		// filename field
		wlProcess=new Label(shell, SWT.RIGHT);
		wlProcess.setText(BaseMessages.getString(PKG, "ExecProcessDialog.Process.Label")); //$NON-NLS-1$
 		props.setLook(wlProcess);
		fdlProcess=new FormData();
		fdlProcess.left = new FormAttachment(0, 0);
		fdlProcess.right= new FormAttachment(middle, -margin);
		fdlProcess.top  = new FormAttachment(wStepname, margin);
		wlProcess.setLayoutData(fdlProcess);
		
		
		wProcess=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
		wProcess.setEditable(true);
 		props.setLook(wProcess);
 		wProcess.addModifyListener(lsMod);
		fdProcess=new FormData();
		fdProcess.left = new FormAttachment(middle, 0);
		fdProcess.top  = new FormAttachment(wStepname, margin);
		fdProcess.right= new FormAttachment(100, -margin);
		wProcess.setLayoutData(fdProcess);
		wProcess.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                get();
                shell.setCursor(null);
                busy.dispose();
            }
        }
    );
		
		// Fail when status is different than 0
		wlFailWhenNotSuccess=new Label(shell, SWT.RIGHT);
		wlFailWhenNotSuccess.setText(BaseMessages.getString(PKG, "ExecProcessDialog.FailWhenNotSuccess.Label"));
 		props.setLook(wlFailWhenNotSuccess);
		fdlFailWhenNotSuccess=new FormData();
		fdlFailWhenNotSuccess.left = new FormAttachment(0, 0);
		fdlFailWhenNotSuccess.top  = new FormAttachment(wProcess, margin);
		fdlFailWhenNotSuccess.right= new FormAttachment(middle, -margin);
		wlFailWhenNotSuccess.setLayoutData(fdlFailWhenNotSuccess);
		wFailWhenNotSuccess=new Button(shell, SWT.CHECK );
		wFailWhenNotSuccess.setToolTipText(BaseMessages.getString(PKG, "ExecProcessDialog.FailWhenNotSuccess.Tooltip"));
 		props.setLook(wFailWhenNotSuccess);
		fdFailWhenNotSuccess=new FormData();
		fdFailWhenNotSuccess.left = new FormAttachment(middle, 0);
		fdFailWhenNotSuccess.top  = new FormAttachment(wProcess, margin);
		fdFailWhenNotSuccess.right= new FormAttachment(100, 0);
		wFailWhenNotSuccess.setLayoutData(fdFailWhenNotSuccess);
		wFailWhenNotSuccess.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);

		///////////////////////////////// 
		// START OF OUTPUT Fields GROUP  //
		///////////////////////////////// 

		wOutputFields= new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wOutputFields);
		wOutputFields.setText(BaseMessages.getString(PKG, "ExecProcessDialog.OutputFields.Label"));
		
		FormLayout OutputFieldsgroupLayout = new FormLayout();
		OutputFieldsgroupLayout.marginWidth = 10;
		OutputFieldsgroupLayout.marginHeight = 10;
		wOutputFields.setLayout(OutputFieldsgroupLayout);

		// Result fieldname ...
		wlResult=new Label(wOutputFields, SWT.RIGHT);
		wlResult.setText(BaseMessages.getString(PKG, "ExecProcessDialog.ResultField.Label")); //$NON-NLS-1$
 		props.setLook(wlResult);
		fdlResult=new FormData();
		fdlResult.left = new FormAttachment(0, 0);
		fdlResult.right= new FormAttachment(middle, -margin);
		fdlResult.top  = new FormAttachment(wFailWhenNotSuccess, margin*2);
		wlResult.setLayoutData(fdlResult);

		wResult=new TextVar(transMeta, wOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wResult.setToolTipText(BaseMessages.getString(PKG, "ExecProcessDialog.ResultField.Tooltip"));
 		props.setLook(wResult);
		wResult.addModifyListener(lsMod);
		fdResult=new FormData();
		fdResult.left = new FormAttachment(middle, 0);
		fdResult.top  = new FormAttachment(wFailWhenNotSuccess, margin*2);
		fdResult.right= new FormAttachment(100, 0);
		wResult.setLayoutData(fdResult);
		
		// Error fieldname ...
		wlError=new Label(wOutputFields, SWT.RIGHT);
		wlError.setText(BaseMessages.getString(PKG, "ExecProcessDialog.ErrorField.Label")); //$NON-NLS-1$
 		props.setLook(wlError);
		fdlError=new FormData();
		fdlError.left = new FormAttachment(0, 0);
		fdlError.right= new FormAttachment(middle, -margin);
		fdlError.top  = new FormAttachment(wResult, margin);
		wlError.setLayoutData(fdlError);

		wError=new TextVar(transMeta, wOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wError.setToolTipText(BaseMessages.getString(PKG, "ExecProcessDialog.ErrorField.Tooltip"));
 		props.setLook(wError);
		wError.addModifyListener(lsMod);
		fdError=new FormData();
		fdError.left = new FormAttachment(middle, 0);
		fdError.top  = new FormAttachment(wResult, margin);
		fdError.right= new FormAttachment(100, 0);
		wError.setLayoutData(fdError);
		
		// ExitValue fieldname ...
		wlExitValue=new Label(wOutputFields, SWT.RIGHT);
		wlExitValue.setText(BaseMessages.getString(PKG, "ExecProcessDialog.ExitValueField.Label")); //$NON-NLS-1$
 		props.setLook(wlExitValue);
		fdlExitValue=new FormData();
		fdlExitValue.left = new FormAttachment(0, 0);
		fdlExitValue.right= new FormAttachment(middle, -margin);
		fdlExitValue.top  = new FormAttachment(wError, margin);
		wlExitValue.setLayoutData(fdlExitValue);

		wExitValue=new TextVar(transMeta, wOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wExitValue.setToolTipText(BaseMessages.getString(PKG, "ExecProcessDialog.ExitValueField.Tooltip"));
 		props.setLook(wExitValue);
		wExitValue.addModifyListener(lsMod);
		fdExitValue=new FormData();
		fdExitValue.left = new FormAttachment(middle, 0);
		fdExitValue.top  = new FormAttachment(wError, margin);
		fdExitValue.right= new FormAttachment(100, 0);
		wExitValue.setLayoutData(fdExitValue);
		
		
		
		fdOutputFields= new FormData();
		fdOutputFields.left = new FormAttachment(0, margin);
		fdOutputFields.top = new FormAttachment(wFailWhenNotSuccess, 2*margin);
		fdOutputFields.right = new FormAttachment(100, -margin);
		wOutputFields.setLayoutData(fdOutputFields);
		
		///////////////////////////////// 
		// END OF OUTPUT Fields GROUP  //
		///////////////////////////////// 
		
	
		
		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wOutputFields);

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
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "ExecProcessDialog.Log.GettingKeyInfo")); //$NON-NLS-1$

		if (input.getProcessField() !=null)   wProcess.setText(input.getProcessField());
		if (input.getResultFieldName()!=null)   wResult.setText(input.getResultFieldName());	
		if (input.getErrorFieldName()!=null)   wError.setText(input.getErrorFieldName());	
		if (input.getExitValueFieldName()!=null)   wExitValue.setText(input.getExitValueFieldName());	
		wFailWhenNotSuccess.setSelection(input.isFailWhenNotSuccess());
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
		input.setProcessField(wProcess.getText() );
		input.setResultFieldName(wResult.getText() );
		input.setErrorFieldName(wError.getText() );
		input.setExitValueFieldName(wExitValue.getText() );
		input.setFailWhentNoSuccess(wFailWhenNotSuccess.getSelection());
		stepname = wStepname.getText(); // return value
		
		dispose();
	}

	 private void get()
	{
		 if(!gotPreviousFields)
		 {
			try
			{
				String fieldvalue=wProcess.getText();
				wProcess.removeAll();
				RowMetaInterface r = transMeta.getPrevStepFields(stepname);
				if (r!=null)
				{
					wProcess.setItems(r.getFieldNames());
				}
				if(fieldvalue!=null) wProcess.setText(fieldvalue);
				gotPreviousFields=true;
			}
			catch(KettleException ke)
			{
				new ErrorDialog(shell, BaseMessages.getString(PKG, "ExecProcessDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "ExecProcessDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
		 }
	}
}
