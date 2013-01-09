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

package org.pentaho.di.ui.trans.steps.pgpencryptstream;

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
import org.eclipse.swt.widgets.FileDialog;
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
import org.pentaho.di.trans.steps.pgpencryptstream.PGPEncryptStreamMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class PGPEncryptStreamDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = PGPEncryptStreamMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	private boolean gotPreviousFields=false;
	
	private Label        wlGPGLocation;
	private TextVar      wGPGLocation;
	private FormData     fdlGPGLocation, fdGPGLocation;
	
	private Label        wlKeyName;
	private TextVar      wKeyName;
	private FormData     fdlKeyName, fdKeyName;


	private Label        wlStreamFieldName;
	private CCombo       wStreamFieldName;
	private FormData     fdlStreamFieldName, fdStreamFieldName;

	private TextVar      wResult;
	private FormData    fdResult,fdlResult;
	private Label       wlResult;
	private Button wbbGpgExe;
	private FormData fdbbGpgExe;
	private PGPEncryptStreamMeta input;
	
	private Button       wKeyNameFromField;
	private FormData     fdKeyNameFromField,fdlKeyNameFromField;
	private Label        wlKeyNameFromField;
	
	private Label        wlKeyNameFieldName;
	private CCombo       wKeyNameFieldName;
	private FormData     fdlKeyNameFieldName, fdKeyNameFieldName;


	private Group wGPGGroup;
	private FormData fdGPGGroup;
	
	private static final String[] FILETYPES = new String[] 
	{
			BaseMessages.getString(PKG, "PGPEncryptStreamDialog.Filetype.All") 
	};

	public PGPEncryptStreamDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(PGPEncryptStreamMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "PGPEncryptStreamDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin=Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "PGPEncryptStreamDialog.Stepname.Label")); //$NON-NLS-1$
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
		// START OF GPG Fields GROUP  //
		///////////////////////////////// 

		wGPGGroup = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wGPGGroup);
		wGPGGroup.setText(BaseMessages.getString(PKG, "PGPEncryptStreamDialog.GPGGroup.Label"));
		
		FormLayout GPGGroupgroupLayout = new FormLayout();
		GPGGroupgroupLayout.marginWidth = 10;
		GPGGroupgroupLayout.marginHeight = 10;
		wGPGGroup.setLayout(GPGGroupgroupLayout);
		

		// GPGLocation fieldname ...
		wlGPGLocation=new Label(wGPGGroup, SWT.RIGHT);
		wlGPGLocation.setText(BaseMessages.getString(PKG, "PGPEncryptStreamDialog.GPGLocationField.Label")); //$NON-NLS-1$
 		props.setLook(wlGPGLocation);
		fdlGPGLocation=new FormData();
		fdlGPGLocation.left = new FormAttachment(0, 0);
		fdlGPGLocation.right= new FormAttachment(middle, -margin);
		fdlGPGLocation.top  = new FormAttachment(wStepname, margin*2);
		wlGPGLocation.setLayoutData(fdlGPGLocation);
		
		// Browse Source files button ...
		wbbGpgExe=new Button(wGPGGroup, SWT.PUSH| SWT.CENTER);
		props.setLook(wbbGpgExe);
		wbbGpgExe.setText(BaseMessages.getString(PKG, "PGPEncryptStreamDialog.BrowseFiles.Label"));
		fdbbGpgExe=new FormData();
		fdbbGpgExe.right= new FormAttachment(100, -margin);
		fdbbGpgExe.top  = new FormAttachment(wStepname, margin);
		wbbGpgExe.setLayoutData(fdbbGpgExe);


		wbbGpgExe.addSelectionListener
			(
			new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						FileDialog dialog = new FileDialog(shell, SWT.OPEN);
						dialog.setFilterExtensions(new String[] {"*"});
						if (wGPGLocation.getText()!=null)
						{
							dialog.setFileName(transMeta.environmentSubstitute(wGPGLocation.getText()) );
						}
						dialog.setFilterNames(FILETYPES);
						if (dialog.open()!=null)
						{
							wGPGLocation.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
						}
					}
				}
			);

		wGPGLocation=new TextVar(transMeta, wGPGGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wGPGLocation.setToolTipText(BaseMessages.getString(PKG, "PGPEncryptStreamDialog.GPGLocationField.Tooltip"));
 		props.setLook(wGPGLocation);
		wGPGLocation.addModifyListener(lsMod);
		fdGPGLocation=new FormData();
		fdGPGLocation.left = new FormAttachment(middle, 0);
		fdGPGLocation.top  = new FormAttachment(wStepname, margin*2);
		fdGPGLocation.right= new FormAttachment(wbbGpgExe, -margin);
		wGPGLocation.setLayoutData(fdGPGLocation);
		
		
		// KeyName fieldname ...
		wlKeyName=new Label(wGPGGroup, SWT.RIGHT);
		wlKeyName.setText(BaseMessages.getString(PKG, "PGPEncryptStreamDialog.KeyNameField.Label")); //$NON-NLS-1$
 		props.setLook(wlKeyName);
		fdlKeyName=new FormData();
		fdlKeyName.left = new FormAttachment(0, 0);
		fdlKeyName.right= new FormAttachment(middle, -margin);
		fdlKeyName.top  = new FormAttachment(wGPGLocation, margin);
		wlKeyName.setLayoutData(fdlKeyName);

		wKeyName=new TextVar(transMeta, wGPGGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wKeyName.setToolTipText(BaseMessages.getString(PKG, "PGPEncryptStreamDialog.KeyNameField.Tooltip"));
 		props.setLook(wKeyName);
		wKeyName.addModifyListener(lsMod);
		fdKeyName=new FormData();
		fdKeyName.left = new FormAttachment(middle, 0);
		fdKeyName.top  = new FormAttachment(wGPGLocation, margin);
		fdKeyName.right= new FormAttachment(100, 0);
		wKeyName.setLayoutData(fdKeyName);
		
		wlKeyNameFromField=new Label(wGPGGroup, SWT.RIGHT);
		wlKeyNameFromField.setText(BaseMessages.getString(PKG, "PGPEncryptStreamDialog.KeyNameFromField.Label"));
 		props.setLook(wlKeyNameFromField);
		fdlKeyNameFromField=new FormData();
		fdlKeyNameFromField.left = new FormAttachment(0, 0);
		fdlKeyNameFromField.top  = new FormAttachment(wKeyName, margin);
		fdlKeyNameFromField.right= new FormAttachment(middle, -margin);
		wlKeyNameFromField.setLayoutData(fdlKeyNameFromField);
		wKeyNameFromField=new Button(wGPGGroup, SWT.CHECK );
 		props.setLook(wKeyNameFromField);
		wKeyNameFromField.setToolTipText(BaseMessages.getString(PKG, "PGPEncryptStreamDialog.KeyNameFromField.Tooltip"));
		fdKeyNameFromField=new FormData();
		fdKeyNameFromField.left = new FormAttachment(middle, 0);
		fdKeyNameFromField.top  = new FormAttachment(wKeyName, margin);
		wKeyNameFromField.setLayoutData(fdKeyNameFromField);
		
		wKeyNameFromField.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					keyNameFromField();
				}
			}
		);
		

		// Stream field
		wlKeyNameFieldName=new Label(wGPGGroup, SWT.RIGHT);
		wlKeyNameFieldName.setText(BaseMessages.getString(PKG, "PGPEncryptStreamDialog.KeyNameFieldName.Label")); //$NON-NLS-1$
 		props.setLook(wlKeyNameFieldName);
		fdlKeyNameFieldName=new FormData();
		fdlKeyNameFieldName.left = new FormAttachment(0, 0);
		fdlKeyNameFieldName.right= new FormAttachment(middle, -margin);
		fdlKeyNameFieldName.top  = new FormAttachment(wKeyNameFromField, margin);
		wlKeyNameFieldName.setLayoutData(fdlKeyNameFieldName);

		wKeyNameFieldName=new CCombo(wGPGGroup, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wKeyNameFieldName);
		wKeyNameFieldName.addModifyListener(lsMod);
		fdKeyNameFieldName=new FormData();
		fdKeyNameFieldName.left = new FormAttachment(middle, 0);
		fdKeyNameFieldName.top  = new FormAttachment(wKeyNameFromField, margin);
		fdKeyNameFieldName.right= new FormAttachment(100, -margin);
		wKeyNameFieldName.setLayoutData(fdKeyNameFieldName);
		wKeyNameFieldName.addFocusListener(new FocusListener()
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
		

		fdGPGGroup = new FormData();
		fdGPGGroup.left = new FormAttachment(0, margin);
		fdGPGGroup.top = new FormAttachment(wStepname, margin);
		fdGPGGroup.right = new FormAttachment(100, -margin);
		wGPGGroup.setLayoutData(fdGPGGroup);
		
		///////////////////////////////// 
		// END OF GPG GROUP  //
		///////////////////////////////// 

		
		// Stream field
		wlStreamFieldName=new Label(shell, SWT.RIGHT);
		wlStreamFieldName.setText(BaseMessages.getString(PKG, "PGPEncryptStreamDialog.StreamFieldName.Label")); //$NON-NLS-1$
 		props.setLook(wlStreamFieldName);
		fdlStreamFieldName=new FormData();
		fdlStreamFieldName.left = new FormAttachment(0, 0);
		fdlStreamFieldName.right= new FormAttachment(middle, -margin);
		fdlStreamFieldName.top  = new FormAttachment(wGPGGroup, 2*margin);
		wlStreamFieldName.setLayoutData(fdlStreamFieldName);
		
		
		wStreamFieldName=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wStreamFieldName);
		wStreamFieldName.addModifyListener(lsMod);
		fdStreamFieldName=new FormData();
		fdStreamFieldName.left = new FormAttachment(middle, 0);
		fdStreamFieldName.top  = new FormAttachment(wGPGGroup, 2*margin);
		fdStreamFieldName.right= new FormAttachment(100, -margin);
		wStreamFieldName.setLayoutData(fdStreamFieldName);
		wStreamFieldName.addFocusListener(new FocusListener()
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
		
		
		// Result fieldname ...
		wlResult=new Label(shell, SWT.RIGHT);
		wlResult.setText(BaseMessages.getString(PKG, "PGPEncryptStreamDialog.ResultField.Label")); //$NON-NLS-1$
 		props.setLook(wlResult);
		fdlResult=new FormData();
		fdlResult.left = new FormAttachment(0, 0);
		fdlResult.right= new FormAttachment(middle, -margin);
		fdlResult.top  = new FormAttachment(wStreamFieldName, margin*2);
		wlResult.setLayoutData(fdlResult);

		wResult=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wResult.setToolTipText(BaseMessages.getString(PKG, "PGPEncryptStreamDialog.ResultField.Tooltip"));
 		props.setLook(wResult);
		wResult.addModifyListener(lsMod);
		fdResult=new FormData();
		fdResult.left = new FormAttachment(middle, 0);
		fdResult.top  = new FormAttachment(wStreamFieldName, margin*2);
		fdResult.right= new FormAttachment(100, 0);
		wResult.setLayoutData(fdResult);
		




		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wResult);

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
		keyNameFromField();
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
		if (input.getGPGLocation()!=null)   wGPGLocation.setText(input.getGPGLocation());
		if (input.getStreamField() !=null)   wStreamFieldName.setText(input.getStreamField());
		if (input.getResultFieldName()!=null)   wResult.setText(input.getResultFieldName());
		if (input.getKeyName() !=null)   wKeyName.setText(input.getKeyName());
		wKeyNameFromField.setSelection(input.isKeynameInField());
		if(input.getKeynameFieldName()!=null) wKeyNameFieldName.setText(input.getKeynameFieldName());
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
		input.setStreamField(wStreamFieldName.getText() );
		input.setGPGPLocation(wGPGLocation.getText() );
		input.setKeyName(wKeyName.getText() );
		input.setResultfieldname(wResult.getText() );
		input.setKeynameInField(wKeyNameFromField.getSelection());
		input.setKeynameFieldName(wKeyNameFieldName.getText());
		stepname = wStepname.getText(); // return value
		
		dispose();
	}
	private void keyNameFromField()
	{
		wlKeyName.setEnabled(!wKeyNameFromField.getSelection());
		wKeyName.setEnabled(!wKeyNameFromField.getSelection());
		wlKeyNameFieldName.setEnabled(wKeyNameFromField.getSelection());
		wKeyNameFieldName.setEnabled(wKeyNameFromField.getSelection());
	}
	 private void get()
	 {
		 if(!gotPreviousFields) {
		 try{
	            String fieldvalue=wStreamFieldName.getText();
	            wStreamFieldName.removeAll();
	            String Keyfieldvalue=wKeyNameFieldName.getText();
	            wKeyNameFieldName.removeAll();
				RowMetaInterface r = transMeta.getPrevStepFields(stepname);
				if (r!=null)
				{
					wStreamFieldName.setItems(r.getFieldNames());
					wKeyNameFieldName.setItems(r.getFieldNames());
				}
				if(fieldvalue!=null) wStreamFieldName.setText(fieldvalue);
				if(Keyfieldvalue!=null) wKeyNameFieldName.setText(Keyfieldvalue);
				gotPreviousFields=true;
		 }catch(KettleException ke){
				new ErrorDialog(shell, BaseMessages.getString(PKG, "PGPEncryptStreamDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "PGPEncryptStreamDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
		 }
	 }
}
