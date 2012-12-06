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

package org.pentaho.di.ui.trans.steps.zipfile;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Group;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.zipfile.ZipFileMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;



public class ZipFileDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = ZipFileMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlSourceFileNameField;
	private CCombo       wSourceFileNameField;
	private FormData     fdlSourceFileNameField, fdSourceFileNameField;
	
	private Label        wlTargetFileNameField;
	private CCombo       wTargetFileNameField;
	private FormData     fdlTargetFileNameField, fdTargetFileNameField;
	
	private Button       wAddResult;
	private FormData     fdAddResult,fdlAddResult;
	private Label        wlAddResult;
	
	private Button       wOverwriteZipEntry;
	private FormData     fdOverwriteTarget,fdlOverwriteTarget;
	private Label        wlOverwriteTarget;
	
	private Button       wCreateParentFolder;
	private FormData     fdCreateParentFolder,fdlCreateParentFolder;
	private Label        wlCreateParentFolder;
	
	
	private Button       wKeepFolders;
	private FormData     fdKeepFolders,fdlKeepFolders;
	private Label        wlKeepFolders;



	private Group wSettingsGroup;
	private FormData fdSettingsGroup;
	private ZipFileMeta input;

	private Label        wlBaseFolderField;
	private CCombo       wBaseFolderField;
	private FormData     fdlBaseFolderField, fdBaseFolderField;
	
	private Label 		wlOperation;
	private CCombo 		wOperation;
	private FormData    fdlOperation;
	private FormData    fdOperation;
	
	private Label        wlMoveToFolderField;
	private CCombo       wMoveToFolderField;
	private FormData     fdlMoveToFolderField, fdMoveToFolderField;


	

	private boolean gotPreviousFields=false;

	public ZipFileDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(ZipFileMeta)in;
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
		
		SelectionAdapter lsSel = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				input.setChanged();
				
			}
		};
        
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "ZipFileDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin=Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "ZipFileDialog.Stepname.Label")); //$NON-NLS-1$
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
		wSettingsGroup.setText(BaseMessages.getString(PKG, "ZipFileDialog.wSettingsGroup.Label"));
		
		FormLayout settingGroupLayout = new FormLayout();
		settingGroupLayout.marginWidth = 10;
		settingGroupLayout.marginHeight = 10;
		wSettingsGroup.setLayout(settingGroupLayout);

		// Create target parent folder?
		wlCreateParentFolder=new Label(wSettingsGroup, SWT.RIGHT);
		wlCreateParentFolder.setText(BaseMessages.getString(PKG, "ZipFileDialog.CreateParentFolder.Label"));
 		props.setLook(wlCreateParentFolder);
		fdlCreateParentFolder=new FormData();
		fdlCreateParentFolder.left = new FormAttachment(0, 0);
		fdlCreateParentFolder.top  = new FormAttachment(wStepname, margin);
		fdlCreateParentFolder.right= new FormAttachment(middle, -margin);
		wlCreateParentFolder.setLayoutData(fdlCreateParentFolder);
		wCreateParentFolder=new Button(wSettingsGroup, SWT.CHECK );
 		props.setLook(wCreateParentFolder);
		wCreateParentFolder.setToolTipText(BaseMessages.getString(PKG, "ZipFileDialog.CreateParentFolder.Tooltip"));
		fdCreateParentFolder=new FormData();
		fdCreateParentFolder.left = new FormAttachment(middle, 0);
		fdCreateParentFolder.top  = new FormAttachment(wStepname, margin);
		wCreateParentFolder.setLayoutData(fdCreateParentFolder);
		wCreateParentFolder.addSelectionListener(lsSel);
		
		
		// Overwrite target file?
		wlOverwriteTarget=new Label(wSettingsGroup, SWT.RIGHT);
		wlOverwriteTarget.setText(BaseMessages.getString(PKG, "ZipFileDialog.OverwriteTarget.Label"));
 		props.setLook(wlOverwriteTarget);
		fdlOverwriteTarget=new FormData();
		fdlOverwriteTarget.left = new FormAttachment(0, 0);
		fdlOverwriteTarget.top  = new FormAttachment(wCreateParentFolder, margin);
		fdlOverwriteTarget.right= new FormAttachment(middle, -margin);
		wlOverwriteTarget.setLayoutData(fdlOverwriteTarget);
		wOverwriteZipEntry=new Button(wSettingsGroup, SWT.CHECK );
 		props.setLook(wOverwriteZipEntry);
		wOverwriteZipEntry.setToolTipText(BaseMessages.getString(PKG, "ZipFileDialog.OverwriteTarget.Tooltip"));
		fdOverwriteTarget=new FormData();
		fdOverwriteTarget.left = new FormAttachment(middle, 0);
		fdOverwriteTarget.top  = new FormAttachment(wCreateParentFolder, margin);
		wOverwriteZipEntry.setLayoutData(fdOverwriteTarget);
		wOverwriteZipEntry.addSelectionListener(lsSel);
		
		// Add Target filename to result filenames?
		wlAddResult=new Label(wSettingsGroup, SWT.RIGHT);
		wlAddResult.setText(BaseMessages.getString(PKG, "ZipFileDialog.AddResult.Label"));
 		props.setLook(wlAddResult);
		fdlAddResult=new FormData();
		fdlAddResult.left = new FormAttachment(0, 0);
		fdlAddResult.top  = new FormAttachment(wOverwriteZipEntry, margin);
		fdlAddResult.right= new FormAttachment(middle, -margin);
		wlAddResult.setLayoutData(fdlAddResult);
		wAddResult=new Button(wSettingsGroup, SWT.CHECK );
 		props.setLook(wAddResult);
		wAddResult.setToolTipText(BaseMessages.getString(PKG, "ZipFileDialog.AddResult.Tooltip"));
		fdAddResult=new FormData();
		fdAddResult.left = new FormAttachment(middle, 0);
		fdAddResult.top  = new FormAttachment(wOverwriteZipEntry, margin);
		wAddResult.setLayoutData(fdAddResult);
		wAddResult.addSelectionListener(lsSel);
		
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
		wlSourceFileNameField.setText(BaseMessages.getString(PKG, "ZipFileDialog.SourceFileNameField.Label")); //$NON-NLS-1$
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
                get();
            }
        }
    );
		// TargetFileNameField field
		wlTargetFileNameField=new Label(shell, SWT.RIGHT);
		wlTargetFileNameField.setText(BaseMessages.getString(PKG, "ZipFileDialog.TargetFileNameField.Label")); //$NON-NLS-1$
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
                get();
            }
        }
    );
		

		wlKeepFolders=new Label(shell, SWT.RIGHT);
		wlKeepFolders.setText(BaseMessages.getString(PKG, "ZipFileDialog.KeepFolders.Label"));
 		props.setLook(wlKeepFolders);
		fdlKeepFolders=new FormData();
		fdlKeepFolders.left = new FormAttachment(0, 0);
		fdlKeepFolders.top  = new FormAttachment(wTargetFileNameField, margin);
		fdlKeepFolders.right= new FormAttachment(middle, -margin);
		wlKeepFolders.setLayoutData(fdlKeepFolders);
		wKeepFolders=new Button(shell, SWT.CHECK );
 		props.setLook(wKeepFolders);
		wKeepFolders.setToolTipText(BaseMessages.getString(PKG, "ZipFileDialog.KeepFolders.Tooltip"));
		fdKeepFolders=new FormData();
		fdKeepFolders.left = new FormAttachment(middle, 0);
		fdKeepFolders.top  = new FormAttachment(wTargetFileNameField, margin);
		wKeepFolders.setLayoutData(fdKeepFolders);
		wKeepFolders.addSelectionListener(lsSel);
		wKeepFolders.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				keepFolder();
				
			}

		});
		

		// BaseFolderField field
		wlBaseFolderField=new Label(shell, SWT.RIGHT);
		wlBaseFolderField.setText(BaseMessages.getString(PKG, "ZipFileDialog.BaseFolderField.Label")); //$NON-NLS-1$
 		props.setLook(wlBaseFolderField);
		fdlBaseFolderField=new FormData();
		fdlBaseFolderField.left = new FormAttachment(0, 0);
		fdlBaseFolderField.right= new FormAttachment(middle, -margin);
		fdlBaseFolderField.top  = new FormAttachment(wKeepFolders, margin);
		wlBaseFolderField.setLayoutData(fdlBaseFolderField);
		
		
		wBaseFolderField=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
		wBaseFolderField.setEditable(true);
 		props.setLook(wBaseFolderField);
 		wBaseFolderField.addModifyListener(lsMod);
		fdBaseFolderField=new FormData();
		fdBaseFolderField.left = new FormAttachment(middle, 0);
		fdBaseFolderField.top  = new FormAttachment(wKeepFolders, margin);
		fdBaseFolderField.right= new FormAttachment(100, -margin);
		wBaseFolderField.setLayoutData(fdBaseFolderField);
		wBaseFolderField.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                get();
            }
        }
    );
		
		// Operation
		wlOperation=new Label(shell, SWT.RIGHT);
		wlOperation.setText(BaseMessages.getString(PKG, "ZipFileDialog.Operation.Label")); //$NON-NLS-1$
 		props.setLook(wlOperation);
		fdlOperation=new FormData();
		fdlOperation.left = new FormAttachment(0, 0);
		fdlOperation.right= new FormAttachment(middle, -margin);
		fdlOperation.top  = new FormAttachment(wBaseFolderField, margin);
		wlOperation.setLayoutData(fdlOperation);
		
		wOperation=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wOperation);
 		wOperation.addModifyListener(lsMod);
		fdOperation=new FormData();
		fdOperation.left = new FormAttachment(middle, 0);
		fdOperation.top  = new FormAttachment(wBaseFolderField, margin);
		fdOperation.right= new FormAttachment(100, -margin);
		wOperation.setLayoutData(fdOperation);
		wOperation.setItems(ZipFileMeta.operationTypeDesc);
		wOperation.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				updateOperation();
				
			}
		});
		
		// MoveToFolderField field
		wlMoveToFolderField=new Label(shell, SWT.RIGHT);
		wlMoveToFolderField.setText(BaseMessages.getString(PKG, "ZipFileDialog.MoveToFolderField.Label")); //$NON-NLS-1$
 		props.setLook(wlMoveToFolderField);
		fdlMoveToFolderField=new FormData();
		fdlMoveToFolderField.left = new FormAttachment(0, 0);
		fdlMoveToFolderField.right= new FormAttachment(middle, -margin);
		fdlMoveToFolderField.top  = new FormAttachment(wOperation, margin);
		wlMoveToFolderField.setLayoutData(fdlMoveToFolderField);
		
		
		wMoveToFolderField=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
		wMoveToFolderField.setEditable(true);
 		props.setLook(wMoveToFolderField);
 		wMoveToFolderField.addModifyListener(lsMod);
		fdMoveToFolderField=new FormData();
		fdMoveToFolderField.left = new FormAttachment(middle, 0);
		fdMoveToFolderField.top  = new FormAttachment(wOperation, margin);
		fdMoveToFolderField.right= new FormAttachment(100, -margin);
		wMoveToFolderField.setLayoutData(fdMoveToFolderField);
		wMoveToFolderField.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                get();
            }
        }
    );
		

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wMoveToFolderField);

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
		keepFolder();
		updateOperation();
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
		if(log.isDebug()) log.logDebug(toString(), BaseMessages.getString(PKG, "ZipFileDialog.Log.GettingKeyInfo")); //$NON-NLS-1$
		if (input.getBaseFolderField() !=null)   wBaseFolderField.setText(input.getBaseFolderField());
		if (input.getDynamicSourceFileNameField() !=null)   wSourceFileNameField.setText(input.getDynamicSourceFileNameField());
		if (input.getDynamicTargetFileNameField() !=null)   wTargetFileNameField.setText(input.getDynamicTargetFileNameField());
		wOperation.setText(ZipFileMeta.getOperationTypeDesc(input.getOperationType()));
		if (input.getMoveToFolderField() !=null)   wMoveToFolderField.setText(input.getMoveToFolderField());
		
		wAddResult.setSelection(input.isaddTargetFileNametoResult());
		wOverwriteZipEntry.setSelection(input.isOverwriteZipEntry());
		wCreateParentFolder.setSelection(input.isCreateParentFolder());
		wKeepFolders.setSelection(input.isKeepSouceFolder());
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
		  if(Const.isEmpty(wStepname.getText())) {
	 			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
	 			mb.setMessage(BaseMessages.getString(PKG, "System.Error.StepNameMissing.Message"));
	 			mb.setText(BaseMessages.getString(PKG, "System.Error.StepNameMissing.Title"));
	 			mb.open(); 
	 			return;
	       }
		input.setBaseFolderField(wBaseFolderField.getText() );
		input.setDynamicSourceFileNameField(wSourceFileNameField.getText() );
		input.setDynamicTargetFileNameField(wTargetFileNameField.getText() );
		input.setaddTargetFileNametoResult(wAddResult.getSelection());
		input.setOverwriteZipEntry(wOverwriteZipEntry.getSelection());
		input.setCreateParentFolder(wCreateParentFolder.getSelection());
		input.setKeepSouceFolder(wKeepFolders.getSelection());
		input.setOperationType(ZipFileMeta.getOperationTypeByDesc(wOperation.getText()));
		input.setMoveToFolderField(wMoveToFolderField.getText() );
		stepname = wStepname.getText(); // return value
		
		dispose();
	}

	private void keepFolder() {
		wlBaseFolderField.setEnabled(wKeepFolders.getSelection());
		wBaseFolderField.setEnabled(wKeepFolders.getSelection());
	}
	 private void get()
		{
		 if(!gotPreviousFields)
		 {
			 gotPreviousFields=true;
				String source=wSourceFileNameField.getText();
				String target=wTargetFileNameField.getText();
				String base=wBaseFolderField.getText();
				
			try
			{

				wSourceFileNameField.removeAll();
				wTargetFileNameField.removeAll();
				wBaseFolderField.removeAll();
				RowMetaInterface r = transMeta.getPrevStepFields(stepname);
				if (r!=null)
				{
					String[] fields=r.getFieldNames();
					wSourceFileNameField.setItems(fields);
					wTargetFileNameField.setItems(fields);
					wBaseFolderField.setItems(fields);
				}
			}
			catch(KettleException ke)
			{
				new ErrorDialog(shell, BaseMessages.getString(PKG, "ZipFileDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "ZipFileDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}finally {
				if(source!=null) wSourceFileNameField.setText(source);
				if(target!=null) wTargetFileNameField.setText(target);
				if(base!=null) wBaseFolderField.setText(base);
			}
		 }
		}
	 
		private void updateOperation()
		{
			wlMoveToFolderField.setEnabled(ZipFileMeta.getOperationTypeByDesc(wOperation.getText())==ZipFileMeta.OPERATION_TYPE_MOVE);
			wMoveToFolderField.setEnabled(ZipFileMeta.getOperationTypeByDesc(wOperation.getText())==ZipFileMeta.OPERATION_TYPE_MOVE);

		}
}
