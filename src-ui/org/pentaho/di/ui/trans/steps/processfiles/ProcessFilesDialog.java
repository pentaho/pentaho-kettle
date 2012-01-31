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

package org.pentaho.di.ui.trans.steps.processfiles;

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
import org.pentaho.di.trans.steps.processfiles.ProcessFilesMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class ProcessFilesDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = ProcessFilesMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlSourceFileNameField;
	private CCombo       wSourceFileNameField;
	private FormData     fdlSourceFileNameField, fdSourceFileNameField;
	
	private Label        wlTargetFileNameField;
	private CCombo       wTargetFileNameField;
	private FormData     fdlTargetFileNameField, fdTargetFileNameField;
	
	private Button       wAddResult;
	private FormData     fdAddResult,fdlAddResult;
	private Label        wlAddResult;
	
	private Button       wOverwriteTarget;
	private FormData     fdOverwriteTarget,fdlOverwriteTarget;
	private Label        wlOverwriteTarget;
	
	private Button       wCreateParentFolder;
	private FormData     fdCreateParentFolder,fdlCreateParentFolder;
	private Label        wlCreateParentFolder;
	
	private Button       wSimulate;
	private FormData     fdSimulate,fdlSimulate;
	private Label        wlSimulate;

	private Group wSettingsGroup;
	private FormData fdSettingsGroup;
	private ProcessFilesMeta input;
	
	private Label 		wlOperation;
	private CCombo 		wOperation;
	private FormData    fdlOperation;
	private FormData    fdOperation;
	
	private boolean gotPreviousFields=false;

	public ProcessFilesDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(ProcessFilesMeta)in;
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

      SelectionAdapter lsButtonChanged = new SelectionAdapter()
      {
          public void widgetSelected(SelectionEvent e)
          {
              input.setChanged();
          }
      };
        
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "ProcessFilesDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin=Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "ProcessFilesDialog.Stepname.Label")); //$NON-NLS-1$
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
		
		///////////////////////////////// 
		// START OF Settings GROUP  //
		///////////////////////////////// 

		wSettingsGroup = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wSettingsGroup);
		wSettingsGroup.setText(BaseMessages.getString(PKG, "ProcessFilesDialog.wSettingsGroup.Label"));
		
		FormLayout settingGroupLayout = new FormLayout();
		settingGroupLayout.marginWidth = 10;
		settingGroupLayout.marginHeight = 10;
		wSettingsGroup.setLayout(settingGroupLayout);
		
		// Operation
		wlOperation=new Label(wSettingsGroup, SWT.RIGHT);
		wlOperation.setText(BaseMessages.getString(PKG, "ProcessFilesDialog.Operation.Label")); //$NON-NLS-1$
 		props.setLook(wlOperation);
		fdlOperation=new FormData();
		fdlOperation.left = new FormAttachment(0, 0);
		fdlOperation.right= new FormAttachment(middle, -margin);
		fdlOperation.top  = new FormAttachment(wStepname, margin);
		wlOperation.setLayoutData(fdlOperation);
		
		wOperation=new CCombo(wSettingsGroup, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wOperation);
 		wOperation.addModifyListener(lsMod);
		fdOperation=new FormData();
		fdOperation.left = new FormAttachment(middle, 0);
		fdOperation.top  = new FormAttachment(wStepname, margin);
		fdOperation.right= new FormAttachment(100, -margin);
		wOperation.setLayoutData(fdOperation);
		wOperation.setItems(ProcessFilesMeta.operationTypeDesc);
		wOperation.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				updateOperation();
				
			}
		});
		// Create target parent folder?
		wlCreateParentFolder=new Label(wSettingsGroup, SWT.RIGHT);
		wlCreateParentFolder.setText(BaseMessages.getString(PKG, "ProcessFilesDialog.CreateParentFolder.Label"));
 		props.setLook(wlCreateParentFolder);
		fdlCreateParentFolder=new FormData();
		fdlCreateParentFolder.left = new FormAttachment(0, 0);
		fdlCreateParentFolder.top  = new FormAttachment(wOperation, margin);
		fdlCreateParentFolder.right= new FormAttachment(middle, -margin);
		wlCreateParentFolder.setLayoutData(fdlCreateParentFolder);
		wCreateParentFolder=new Button(wSettingsGroup, SWT.CHECK );
 		props.setLook(wCreateParentFolder);
		wCreateParentFolder.setToolTipText(BaseMessages.getString(PKG, "ProcessFilesDialog.CreateParentFolder.Tooltip"));
		wCreateParentFolder.addSelectionListener(lsButtonChanged);
		fdCreateParentFolder=new FormData();
		fdCreateParentFolder.left = new FormAttachment(middle, 0);
		fdCreateParentFolder.top  = new FormAttachment(wOperation, margin);
		wCreateParentFolder.setLayoutData(fdCreateParentFolder);
		
		// Overwrite target file?
		wlOverwriteTarget=new Label(wSettingsGroup, SWT.RIGHT);
		wlOverwriteTarget.setText(BaseMessages.getString(PKG, "ProcessFilesDialog.OverwriteTarget.Label"));
 		props.setLook(wlOverwriteTarget);
		fdlOverwriteTarget=new FormData();
		fdlOverwriteTarget.left = new FormAttachment(0, 0);
		fdlOverwriteTarget.top  = new FormAttachment(wCreateParentFolder, margin);
		fdlOverwriteTarget.right= new FormAttachment(middle, -margin);
		wlOverwriteTarget.setLayoutData(fdlOverwriteTarget);
		wOverwriteTarget=new Button(wSettingsGroup, SWT.CHECK );
 		props.setLook(wOverwriteTarget);
		wOverwriteTarget.setToolTipText(BaseMessages.getString(PKG, "ProcessFilesDialog.OverwriteTarget.Tooltip"));
		wOverwriteTarget.addSelectionListener(lsButtonChanged);
		fdOverwriteTarget=new FormData();
		fdOverwriteTarget.left = new FormAttachment(middle, 0);
		fdOverwriteTarget.top  = new FormAttachment(wCreateParentFolder, margin);
		wOverwriteTarget.setLayoutData(fdOverwriteTarget);
		
		// Add Target filename to result filenames?
		wlAddResult=new Label(wSettingsGroup, SWT.RIGHT);
		wlAddResult.setText(BaseMessages.getString(PKG, "ProcessFilesDialog.AddResult.Label"));
 		props.setLook(wlAddResult);
		fdlAddResult=new FormData();
		fdlAddResult.left = new FormAttachment(0, 0);
		fdlAddResult.top  = new FormAttachment(wOverwriteTarget, margin);
		fdlAddResult.right= new FormAttachment(middle, -margin);
		wlAddResult.setLayoutData(fdlAddResult);
		wAddResult=new Button(wSettingsGroup, SWT.CHECK );
 		props.setLook(wAddResult);
		wAddResult.setToolTipText(BaseMessages.getString(PKG, "ProcessFilesDialog.AddResult.Tooltip"));
		wAddResult.addSelectionListener(lsButtonChanged);
		fdAddResult=new FormData();
		fdAddResult.left = new FormAttachment(middle, 0);
		fdAddResult.top  = new FormAttachment(wOverwriteTarget, margin);
		wAddResult.setLayoutData(fdAddResult);
		
		// Simulation mode ON?
		wlSimulate=new Label(wSettingsGroup, SWT.RIGHT);
		wlSimulate.setText(BaseMessages.getString(PKG, "ProcessFilesDialog.Simulate.Label"));
 		props.setLook(wlSimulate);
		fdlSimulate=new FormData();
		fdlSimulate.left = new FormAttachment(0, 0);
		fdlSimulate.top  = new FormAttachment(wAddResult, margin);
		fdlSimulate.right= new FormAttachment(middle, -margin);
		wlSimulate.setLayoutData(fdlSimulate);
		wSimulate=new Button(wSettingsGroup, SWT.CHECK );
 		props.setLook(wSimulate);
		wSimulate.setToolTipText(BaseMessages.getString(PKG, "ProcessFilesDialog.Simulate.Tooltip"));
		wSimulate.addSelectionListener(lsButtonChanged);
		fdSimulate=new FormData();
		fdSimulate.left = new FormAttachment(middle, 0);
		fdSimulate.top  = new FormAttachment(wAddResult, margin);
		wSimulate.setLayoutData(fdSimulate);
		
		fdSettingsGroup = new FormData();
		fdSettingsGroup.left = new FormAttachment(0, margin);
		fdSettingsGroup.top = new FormAttachment(wStepname, margin);
		fdSettingsGroup.right = new FormAttachment(100, -margin);
		wSettingsGroup.setLayoutData(fdSettingsGroup);
		
		///////////////////////////////// 
		// END OF Settings Fields GROUP  //
		///////////////////////////////// 

		// SourceFileNameField field
		wlSourceFileNameField=new Label(shell, SWT.RIGHT);
		wlSourceFileNameField.setText(BaseMessages.getString(PKG, "ProcessFilesDialog.SourceFileNameField.Label")); //$NON-NLS-1$
 		props.setLook(wlSourceFileNameField);
		fdlSourceFileNameField=new FormData();
		fdlSourceFileNameField.left = new FormAttachment(0, 0);
		fdlSourceFileNameField.right= new FormAttachment(middle, -margin);
		fdlSourceFileNameField.top  = new FormAttachment(wSettingsGroup, 2*margin);
		wlSourceFileNameField.setLayoutData(fdlSourceFileNameField);
		
		
		wSourceFileNameField=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wSourceFileNameField);
 		wSourceFileNameField.setEditable(true);
 		wSourceFileNameField.addModifyListener(lsMod);
		fdSourceFileNameField=new FormData();
		fdSourceFileNameField.left = new FormAttachment(middle, 0);
		fdSourceFileNameField.top  = new FormAttachment(wSettingsGroup, 2*margin);
		fdSourceFileNameField.right= new FormAttachment(100, -margin);
		wSourceFileNameField.setLayoutData(fdSourceFileNameField);
		wSourceFileNameField.addFocusListener(new FocusListener()
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
		// TargetFileNameField field
		wlTargetFileNameField=new Label(shell, SWT.RIGHT);
		wlTargetFileNameField.setText(BaseMessages.getString(PKG, "ProcessFilesDialog.TargetFileNameField.Label")); //$NON-NLS-1$
 		props.setLook(wlTargetFileNameField);
		fdlTargetFileNameField=new FormData();
		fdlTargetFileNameField.left = new FormAttachment(0, 0);
		fdlTargetFileNameField.right= new FormAttachment(middle, -margin);
		fdlTargetFileNameField.top  = new FormAttachment(wSourceFileNameField, margin);
		wlTargetFileNameField.setLayoutData(fdlTargetFileNameField);
		
		
		wTargetFileNameField=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
		wTargetFileNameField.setEditable(true);
 		props.setLook(wTargetFileNameField);
 		wTargetFileNameField.addModifyListener(lsMod);
		fdTargetFileNameField=new FormData();
		fdTargetFileNameField.left = new FormAttachment(middle, 0);
		fdTargetFileNameField.top  = new FormAttachment(wSourceFileNameField, margin);
		fdTargetFileNameField.right= new FormAttachment(100, -margin);
		wTargetFileNameField.setLayoutData(fdTargetFileNameField);
		wTargetFileNameField.addFocusListener(new FocusListener()
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
		
		

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wTargetFileNameField);

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
		updateOperation();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	private void updateOperation()
	{
		wlOverwriteTarget.setEnabled(ProcessFilesMeta.getOperationTypeByDesc(wOperation.getText())!=ProcessFilesMeta.OPERATION_TYPE_DELETE);
		wOverwriteTarget.setEnabled(ProcessFilesMeta.getOperationTypeByDesc(wOperation.getText())!=ProcessFilesMeta.OPERATION_TYPE_DELETE);
		wlAddResult.setEnabled(ProcessFilesMeta.getOperationTypeByDesc(wOperation.getText())!=ProcessFilesMeta.OPERATION_TYPE_DELETE);
		wAddResult.setEnabled(ProcessFilesMeta.getOperationTypeByDesc(wOperation.getText())!=ProcessFilesMeta.OPERATION_TYPE_DELETE);
		wlTargetFileNameField.setEnabled(ProcessFilesMeta.getOperationTypeByDesc(wOperation.getText())!=ProcessFilesMeta.OPERATION_TYPE_DELETE);
		wTargetFileNameField.setEnabled(ProcessFilesMeta.getOperationTypeByDesc(wOperation.getText())!=ProcessFilesMeta.OPERATION_TYPE_DELETE);
		wlCreateParentFolder.setEnabled(ProcessFilesMeta.getOperationTypeByDesc(wOperation.getText())!=ProcessFilesMeta.OPERATION_TYPE_DELETE);
		wCreateParentFolder.setEnabled(ProcessFilesMeta.getOperationTypeByDesc(wOperation.getText())!=ProcessFilesMeta.OPERATION_TYPE_DELETE);
	}
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "ProcessFilesDialog.Log.GettingKeyInfo")); //$NON-NLS-1$

		if (input.getDynamicSourceFileNameField() !=null)   wSourceFileNameField.setText(input.getDynamicSourceFileNameField());
		if (input.getDynamicTargetFileNameField() !=null)   wTargetFileNameField.setText(input.getDynamicTargetFileNameField());
		wOperation.setText(ProcessFilesMeta.getOperationTypeDesc(input.getOperationType()));
		wAddResult.setSelection(input.isaddTargetFileNametoResult());
		wOverwriteTarget.setSelection(input.isOverwriteTargetFile());
		wCreateParentFolder.setSelection(input.isCreateParentFolder());
		wSimulate.setSelection(input.isSimulate());
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
		input.setDynamicSourceFileNameField(wSourceFileNameField.getText() );
		input.setDynamicTargetFileNameField(wTargetFileNameField.getText() );
		input.setOperationType(ProcessFilesMeta.getOperationTypeByDesc(wOperation.getText()));
		input.setaddTargetFileNametoResult(wAddResult.getSelection());
		input.setOverwriteTargetFile(wOverwriteTarget.getSelection());
		input.setCreateParentFolder(wCreateParentFolder.getSelection());
		input.setSimulate(wSimulate.getSelection());
		stepname = wStepname.getText(); // return value
		
		dispose();
	}

	 private void get()
		{
		 if(!gotPreviousFields)
		 {
			 gotPreviousFields=true;
			try
			{
				String source=wSourceFileNameField.getText();
				String target=wTargetFileNameField.getText();
				
				wSourceFileNameField.removeAll();
				wTargetFileNameField.removeAll();
				RowMetaInterface r = transMeta.getPrevStepFields(stepname);
				if (r!=null)
				{
					wSourceFileNameField.setItems(r.getFieldNames());
					wTargetFileNameField.setItems(r.getFieldNames());
					if(source!=null) wSourceFileNameField.setText(source);
					if(target!=null) wTargetFileNameField.setText(target);
				}
			}
			catch(KettleException ke)
			{
				new ErrorDialog(shell, BaseMessages.getString(PKG, "ProcessFilesDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "ProcessFilesDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
		 }
		}
}
