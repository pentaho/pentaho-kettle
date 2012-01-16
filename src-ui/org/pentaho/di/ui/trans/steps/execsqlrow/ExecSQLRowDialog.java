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

package org.pentaho.di.ui.trans.steps.execsqlrow;

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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.execsqlrow.ExecSQLRowMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class ExecSQLRowDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = ExecSQLRowMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private boolean   gotPreviousFields=false;
	private CCombo       wConnection;
    
    private Label        wlInsertField;
    private Text         wInsertField;
    private FormData     fdlInsertField, fdInsertField;
    
    private Label        wlUpdateField;
    private Text         wUpdateField;
    private FormData     fdlUpdateField, fdUpdateField;
    
    private Label        wlDeleteField;
    private Text         wDeleteField;
    private FormData     fdlDeleteField, fdDeleteField;
    
    private Label        wlReadField;
    private Text         wReadField;
    private FormData     fdlReadField, fdReadField;

	private Label        wlSQLFieldName;
	private CCombo       wSQLFieldName;
	private FormData     fdlSQLFieldName, fdSQLFieldName;
	
	private FormData     fdAdditionalFields;
	
	private Label        wlCommit;
	private Text         wCommit;
	private FormData     fdlCommit, fdCommit;
	
	private Group wAdditionalFields;

	private ExecSQLRowMeta input;
	
	
	private Label        wlSQLFromFile;
	private Button       wSQLFromFile;
	private FormData     fdlSQLFromFile, fdSQLFromFile;

	
	private Label        wlSendOneStatement;
	private Button       wSendOneStatement;
	private FormData     fdlSendOneStatement, fdSendOneStatement;
	
	
	public ExecSQLRowDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(ExecSQLRowMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "ExecSQLRowDialog.Shell.Label")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

        // Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "ExecSQLRowDialog.Stepname.Label")); //$NON-NLS-1$
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

		// Connection line
		wConnection = addConnectionLine(shell, wStepname, middle, margin);
		if (input.getDatabaseMeta()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);
		
		// Commit line
		wlCommit = new Label(shell, SWT.RIGHT);
		wlCommit.setText(BaseMessages.getString(PKG, "ExecSQLRowDialog.Commit.Label")); //$NON-NLS-1$
 		props.setLook(wlCommit);
		fdlCommit = new FormData();
		fdlCommit.left = new FormAttachment(0, 0);
		fdlCommit.top = new FormAttachment(wConnection, margin);
		fdlCommit.right = new FormAttachment(middle, -margin);
		wlCommit.setLayoutData(fdlCommit);
		wCommit = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wCommit);
		wCommit.addModifyListener(lsMod);
		fdCommit = new FormData();
		fdCommit.left = new FormAttachment(middle, 0);
		fdCommit.top = new FormAttachment(wConnection, margin);
		fdCommit.right = new FormAttachment(100, 0);
		wCommit.setLayoutData(fdCommit);
		
		wlSendOneStatement=new Label(shell, SWT.RIGHT);
		wlSendOneStatement.setText(BaseMessages.getString(PKG, "ExecSQLRowDialog.SendOneStatement.Label"));
 		props.setLook(wlSendOneStatement);
		fdlSendOneStatement=new FormData();
		fdlSendOneStatement.left = new FormAttachment(0, 0);
		fdlSendOneStatement.top  = new FormAttachment(wCommit, margin);
		fdlSendOneStatement.right= new FormAttachment(middle, -margin);
		wlSendOneStatement.setLayoutData(fdlSendOneStatement);
		wSendOneStatement=new Button(shell, SWT.CHECK );
		wSendOneStatement.setToolTipText(BaseMessages.getString(PKG, "ExecSQLRowDialog.SendOneStatement.Tooltip"));
 		props.setLook(wSendOneStatement);
		fdSendOneStatement=new FormData();
		fdSendOneStatement.left = new FormAttachment(middle, 0);
		fdSendOneStatement.top  = new FormAttachment(wCommit, margin);
		fdSendOneStatement.right= new FormAttachment(100, 0);
		wSendOneStatement.setLayoutData(fdSendOneStatement);
		wSendOneStatement.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);
		
		
		// SQLFieldName field
		wlSQLFieldName=new Label(shell, SWT.RIGHT);
		wlSQLFieldName.setText(BaseMessages.getString(PKG, "ExecSQLRowDialog.SQLFieldName.Label")); //$NON-NLS-1$
 		props.setLook(wlSQLFieldName);
		fdlSQLFieldName=new FormData();
		fdlSQLFieldName.left = new FormAttachment(0, 0);
		fdlSQLFieldName.right= new FormAttachment(middle, -margin);
		fdlSQLFieldName.top  = new FormAttachment(wSendOneStatement, 2*margin);
		wlSQLFieldName.setLayoutData(fdlSQLFieldName);
		wSQLFieldName=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
		wSQLFieldName.setEditable(true);
 		props.setLook(wSQLFieldName);
 		wSQLFieldName.addModifyListener(lsMod);
		fdSQLFieldName=new FormData();
		fdSQLFieldName.left = new FormAttachment(middle, 0);
		fdSQLFieldName.top  = new FormAttachment(wSendOneStatement, 2*margin);
		fdSQLFieldName.right= new FormAttachment(100, -margin);
		wSQLFieldName.setLayoutData(fdSQLFieldName);
		wSQLFieldName.addFocusListener(new FocusListener()
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
		
		wlSQLFromFile=new Label(shell, SWT.RIGHT);
		wlSQLFromFile.setText(BaseMessages.getString(PKG, "ExecSQLRowDialog.SQLFromFile.Label"));
 		props.setLook(wlSQLFromFile);
		fdlSQLFromFile=new FormData();
		fdlSQLFromFile.left = new FormAttachment(0, 0);
		fdlSQLFromFile.top  = new FormAttachment(wSQLFieldName, margin);
		fdlSQLFromFile.right= new FormAttachment(middle, -margin);
		wlSQLFromFile.setLayoutData(fdlSQLFromFile);
		wSQLFromFile=new Button(shell, SWT.CHECK );
		wSQLFromFile.setToolTipText(BaseMessages.getString(PKG, "ExecSQLRowDialog.SQLFromFile.Tooltip"));
 		props.setLook(wSQLFromFile);
		fdSQLFromFile=new FormData();
		fdSQLFromFile.left = new FormAttachment(middle, 0);
		fdSQLFromFile.top  = new FormAttachment(wSQLFieldName, margin);
		fdSQLFromFile.right= new FormAttachment(100, 0);
		wSQLFromFile.setLayoutData(fdSQLFromFile);
		wSQLFromFile.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);
		
		
		///////////////////////////////// 
		// START OF Additional Fields GROUP  //
		///////////////////////////////// 

		wAdditionalFields = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wAdditionalFields);
		wAdditionalFields.setText(BaseMessages.getString(PKG, "ExecSQLRowDialog.wAdditionalFields.Label"));
		
		FormLayout AdditionalFieldsgroupLayout = new FormLayout();
		AdditionalFieldsgroupLayout.marginWidth = 10;
		AdditionalFieldsgroupLayout.marginHeight = 10;
		wAdditionalFields.setLayout(AdditionalFieldsgroupLayout);
        
        // insert field
        wlInsertField=new Label(wAdditionalFields, SWT.RIGHT);
        wlInsertField.setText(BaseMessages.getString(PKG, "ExecSQLRowDialog.InsertField.Label")); //$NON-NLS-1$
 		props.setLook(        wlInsertField);
        fdlInsertField=new FormData();
        fdlInsertField.left = new FormAttachment(0, margin);
        fdlInsertField.right= new FormAttachment(middle, -margin);
        fdlInsertField.top  = new FormAttachment(wSQLFromFile, margin);
        wlInsertField.setLayoutData(fdlInsertField);
        wInsertField=new Text(wAdditionalFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(        wInsertField);
        wInsertField.addModifyListener(lsMod);
        fdInsertField=new FormData();
        fdInsertField.left = new FormAttachment(middle, 0);
        fdInsertField.top  = new FormAttachment(wSQLFromFile, margin);
        fdInsertField.right= new FormAttachment(100, 0);
        wInsertField.setLayoutData(fdInsertField);
        
        // Update field
        wlUpdateField=new Label(wAdditionalFields, SWT.RIGHT);
        wlUpdateField.setText(BaseMessages.getString(PKG, "ExecSQLRowDialog.UpdateField.Label")); //$NON-NLS-1$
 		props.setLook(        wlUpdateField);
        fdlUpdateField=new FormData();
        fdlUpdateField.left = new FormAttachment(0, margin);
        fdlUpdateField.right= new FormAttachment(middle, -margin);
        fdlUpdateField.top  = new FormAttachment(wInsertField, margin);
        wlUpdateField.setLayoutData(fdlUpdateField);
        wUpdateField=new Text(wAdditionalFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(        wUpdateField);
        wUpdateField.addModifyListener(lsMod);
        fdUpdateField=new FormData();
        fdUpdateField.left = new FormAttachment(middle, 0);
        fdUpdateField.top  = new FormAttachment(wInsertField, margin);
        fdUpdateField.right= new FormAttachment(100, 0);
        wUpdateField.setLayoutData(fdUpdateField);
        
        // Delete field
        wlDeleteField=new Label(wAdditionalFields, SWT.RIGHT);
        wlDeleteField.setText(BaseMessages.getString(PKG, "ExecSQLRowDialog.DeleteField.Label")); //$NON-NLS-1$
 		props.setLook(        wlDeleteField);
        fdlDeleteField=new FormData();
        fdlDeleteField.left = new FormAttachment(0, margin);
        fdlDeleteField.right= new FormAttachment(middle, -margin);
        fdlDeleteField.top  = new FormAttachment(wUpdateField, margin);
        wlDeleteField.setLayoutData(fdlDeleteField);
        wDeleteField=new Text(wAdditionalFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(        wDeleteField);
        wDeleteField.addModifyListener(lsMod);
        fdDeleteField=new FormData();
        fdDeleteField.left = new FormAttachment(middle, 0);
        fdDeleteField.top  = new FormAttachment(wUpdateField, margin);
        fdDeleteField.right= new FormAttachment(100, 0);
        wDeleteField.setLayoutData(fdDeleteField);
        
        // Read field
        wlReadField=new Label(wAdditionalFields, SWT.RIGHT);
        wlReadField.setText(BaseMessages.getString(PKG, "ExecSQLRowDialog.ReadField.Label")); //$NON-NLS-1$
 		props.setLook(        wlReadField);
        fdlReadField=new FormData();
        fdlReadField.left = new FormAttachment(0, 0);
        fdlReadField.right= new FormAttachment(middle, -margin);
        fdlReadField.top  = new FormAttachment(wDeleteField, margin);
        wlReadField.setLayoutData(fdlReadField);
        wReadField=new Text(wAdditionalFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(        wReadField);
        wReadField.addModifyListener(lsMod);
        fdReadField=new FormData();
        fdReadField.left = new FormAttachment(middle, 0);
        fdReadField.top  = new FormAttachment(wDeleteField, margin);
        fdReadField.right= new FormAttachment(100, 0);
        wReadField.setLayoutData(fdReadField);
        
		fdAdditionalFields = new FormData();
		fdAdditionalFields.left = new FormAttachment(0, margin);
		fdAdditionalFields.top = new FormAttachment(wSQLFromFile, 2*margin);
		fdAdditionalFields.right = new FormAttachment(100, -margin);
		wAdditionalFields.setLayoutData(fdAdditionalFields);
		
		///////////////////////////////// 
		// END OF Additional Fields GROUP  //
		///////////////////////////////// 
        

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wAdditionalFields);
		
		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
	
		
        wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		
		getData();
		input.setChanged(changed);

		// Set the shell size, based upon previous time...
		setSize();
		
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
		wCommit.setText(""+input.getCommitSize()); //$NON-NLS-1$
		if (input.getSqlFieldName() != null) wSQLFieldName.setText(input.getSqlFieldName());
		if (input.getDatabaseMeta() != null) wConnection.setText(input.getDatabaseMeta().getName());
        
        if (input.getUpdateField()!=null) wUpdateField.setText(input.getUpdateField());
        if (input.getInsertField()!=null) wInsertField.setText(input.getInsertField());
        if (input.getDeleteField()!=null) wDeleteField.setText(input.getDeleteField());
        if (input.getReadField()  !=null) wReadField  .setText(input.getReadField());
        wSQLFromFile.setSelection(input.isSqlFromfile());
        wSendOneStatement.setSelection(input.IsSendOneStatement());
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
		input.setCommitSize( Const.toInt( wCommit.getText(), 0) );
		stepname = wStepname.getText(); // return value
		input.setSqlFieldName(wSQLFieldName.getText());
		// copy info to TextFileInputMeta class (input)
		input.setDatabaseMeta( transMeta.findDatabase(wConnection.getText()) );
        
        input.setInsertField(wInsertField.getText());
        input.setUpdateField(wUpdateField.getText());
        input.setDeleteField(wDeleteField.getText());
        input.setReadField  (wReadField  .getText());
		input.setSqlFromfile(wSQLFromFile.getSelection());
		input.SetSendOneStatement(wSendOneStatement.getSelection());
		if (input.getDatabaseMeta()==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "ExecSQLRowDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(BaseMessages.getString(PKG, "ExecSQLRowDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
			return;
		}
		
		dispose();
    }

    private void get()
	{
    	if(!gotPreviousFields)
    	{
    		gotPreviousFields=true;
			try
			{
				String sqlfield=wSQLFieldName.getText();
				wSQLFieldName.removeAll();
				RowMetaInterface r = transMeta.getPrevStepFields(stepname);
				if (r!=null)
				{
					wSQLFieldName.removeAll();
					wSQLFieldName.setItems(r.getFieldNames());
				}
				if(sqlfield!=null) wSQLFieldName.setText(sqlfield);
			}
			catch(KettleException ke)
			{
				new ErrorDialog(shell, BaseMessages.getString(PKG, "ExecSQLRowDialog.FailedToGetFields.DialogTitle"), 
						BaseMessages.getString(PKG, "ExecSQLRowDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
    	}
	}
}
