/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.ui.trans.steps.splitfieldtorows;


import org.eclipse.swt.SWT;
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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.splitfieldtorows.SplitFieldToRowsMeta;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;




public class SplitFieldToRowsDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = SplitFieldToRowsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlSplitfield;
	private ComboVar     wSplitfield;
	private FormData     fdlSplitfield, fdSplitfield;

	private Label        wlDelimiter;
	private TextVar         wDelimiter;
	private FormData     fdlDelimiter, fdDelimiter;

	private Label        wlValName;
	private TextVar         wValName;
	private FormData     fdlValName, fdValName;
	
	private Label        wlInclRownum;
	private Button       wInclRownum;
	private FormData     fdlInclRownum, fdRownum;

	private Label        wlInclRownumField;
	private TextVar      wInclRownumField;
	private FormData     fdlInclRownumField, fdInclRownumField;
	
	private Label        wlResetRownum;
	private Button       wResetRownum;
	private FormData     fdlResetRownum;
	
	private Group wAdditionalFields;
	private FormData fdAdditionalFields;

    private SplitFieldToRowsMeta  input;

	public SplitFieldToRowsDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(SplitFieldToRowsMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "SplitFieldToRowsDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "SplitFieldToRowsDialog.Stepname.Label")); //$NON-NLS-1$
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

		// Typefield line
		wlSplitfield=new Label(shell, SWT.RIGHT);
		wlSplitfield.setText(BaseMessages.getString(PKG, "SplitFieldToRowsDialog.SplitField.Label")); //$NON-NLS-1$
 		props.setLook(wlSplitfield);
		fdlSplitfield=new FormData();
		fdlSplitfield.left = new FormAttachment(0, 0);
		fdlSplitfield.right= new FormAttachment(middle, -margin);
		fdlSplitfield.top  = new FormAttachment(wStepname, margin);
		wlSplitfield.setLayoutData(fdlSplitfield);
		
		wSplitfield=new ComboVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wSplitfield.setToolTipText(BaseMessages.getString(PKG, "SplitFieldToRowsDialog.UrlField.Tooltip"));
		props.setLook(wSplitfield);
		wSplitfield.addModifyListener(lsMod);
		fdSplitfield=new FormData();
		fdSplitfield.left = new FormAttachment(middle, 0);
		fdSplitfield.top  = new FormAttachment(wStepname, margin);
		fdSplitfield.right= new FormAttachment(100, 0);
		wSplitfield.setLayoutData(fdSplitfield);
		wSplitfield.addFocusListener(new FocusListener()
         {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
             {
             }
             public void focusGained(org.eclipse.swt.events.FocusEvent e)
             {
                 Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                 shell.setCursor(busy);
                 BaseStepDialog.getFieldsFromPrevious(wSplitfield, transMeta, stepMeta);
                 shell.setCursor(null);
                 busy.dispose();
             }
         }
     );

		// Delimiter line
		wlDelimiter=new Label(shell, SWT.RIGHT);
		wlDelimiter.setText(BaseMessages.getString(PKG, "SplitFieldToRowsDialog.Delimiter.Label")); //$NON-NLS-1$
 		props.setLook(wlDelimiter);
		fdlDelimiter=new FormData();
		fdlDelimiter.left = new FormAttachment(0, 0);
		fdlDelimiter.right= new FormAttachment(middle, -margin);
		fdlDelimiter.top  = new FormAttachment(wSplitfield, margin);
		wlDelimiter.setLayoutData(fdlDelimiter);
		wDelimiter=new TextVar(transMeta,shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wDelimiter.setText(""); //$NON-NLS-1$
 		props.setLook(wDelimiter);
		wDelimiter.addModifyListener(lsMod);
		fdDelimiter=new FormData();
		fdDelimiter.left = new FormAttachment(middle, 0);
		fdDelimiter.top  = new FormAttachment(wSplitfield, margin);
		fdDelimiter.right= new FormAttachment(100, 0);
		wDelimiter.setLayoutData(fdDelimiter);

		// ValName line
		wlValName=new Label(shell, SWT.RIGHT);
		wlValName.setText(BaseMessages.getString(PKG, "SplitFieldToRowsDialog.NewFieldName.Label")); //$NON-NLS-1$
        props.setLook( wlValName );
		fdlValName=new FormData();
		fdlValName.left = new FormAttachment(0, 0);
		fdlValName.right= new FormAttachment(middle, -margin);
		fdlValName.top  = new FormAttachment(wDelimiter, margin);
		wlValName.setLayoutData(fdlValName);
		wValName=new TextVar(transMeta,shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wValName.setText(""); //$NON-NLS-1$
        props.setLook( wValName );
		wValName.addModifyListener(lsMod);
		fdValName=new FormData();
		fdValName.left = new FormAttachment(middle, 0);
		fdValName.right= new FormAttachment(100, 0);
		fdValName.top  = new FormAttachment(wDelimiter, margin);
		wValName.setLayoutData(fdValName);

		///////////////////////////////// 
		// START OF Additional Fields GROUP  //
		///////////////////////////////// 

		wAdditionalFields = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wAdditionalFields);
		wAdditionalFields.setText(BaseMessages.getString(PKG, "SplitFieldToRowsDialog.wAdditionalFields.Label"));
		
		FormLayout AdditionalFieldsgroupLayout = new FormLayout();
		AdditionalFieldsgroupLayout.marginWidth = 10;
		AdditionalFieldsgroupLayout.marginHeight = 10;
		wAdditionalFields.setLayout(AdditionalFieldsgroupLayout);
		
		wlInclRownum=new Label(wAdditionalFields, SWT.RIGHT);
		wlInclRownum.setText(BaseMessages.getString(PKG, "SplitFieldToRowsDialog.InclRownum.Label"));
 		props.setLook(wlInclRownum);
		fdlInclRownum=new FormData();
		fdlInclRownum.left = new FormAttachment(0, 0);
		fdlInclRownum.top  = new FormAttachment(wValName, margin);
		fdlInclRownum.right= new FormAttachment(middle, -margin);
		wlInclRownum.setLayoutData(fdlInclRownum);
		wInclRownum=new Button(wAdditionalFields, SWT.CHECK );
 		props.setLook(wInclRownum);
		wInclRownum.setToolTipText(BaseMessages.getString(PKG, "SplitFieldToRowsDialog.InclRownum.Tooltip"));
		fdRownum=new FormData();
		fdRownum.left = new FormAttachment(middle, 0);
		fdRownum.top  = new FormAttachment(wValName, margin);
		wInclRownum.setLayoutData(fdRownum);
		wInclRownum.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				setIncludeRownum();
			}
		}
	);

		wlInclRownumField=new Label(wAdditionalFields, SWT.RIGHT);
		wlInclRownumField.setText(BaseMessages.getString(PKG, "SplitFieldToRowsDialog.InclRownumField.Label"));
 		props.setLook(wlInclRownumField);
		fdlInclRownumField=new FormData();
		fdlInclRownumField.left = new FormAttachment(wInclRownum, margin);
		fdlInclRownumField.top  = new FormAttachment(wValName, margin);
		wlInclRownumField.setLayoutData(fdlInclRownumField);
		wInclRownumField=new TextVar(transMeta,wAdditionalFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInclRownumField);
		wInclRownumField.addModifyListener(lsMod);
		fdInclRownumField=new FormData();
		fdInclRownumField.left = new FormAttachment(wlInclRownumField, margin);
		fdInclRownumField.top  = new FormAttachment(wValName, margin);
		fdInclRownumField.right= new FormAttachment(100, 0);
		wInclRownumField.setLayoutData(fdInclRownumField);
		
		
		wlResetRownum=new Label(wAdditionalFields, SWT.RIGHT);
		wlResetRownum.setText(BaseMessages.getString(PKG, "SplitFieldToRowsDialog.ResetRownum.Label"));
 		props.setLook(wlResetRownum);
		fdlResetRownum=new FormData();
		fdlResetRownum.left = new FormAttachment(wInclRownum, margin);
		fdlResetRownum.top  = new FormAttachment(wInclRownumField, margin);
		wlResetRownum.setLayoutData(fdlResetRownum);
		wResetRownum=new Button(wAdditionalFields, SWT.CHECK );
 		props.setLook(wResetRownum);
		wResetRownum.setToolTipText(BaseMessages.getString(PKG, "SplitFieldToRowsDialog.ResetRownum.Tooltip"));
		fdRownum=new FormData();
		fdRownum.left = new FormAttachment(wlResetRownum, margin);
		fdRownum.top  = new FormAttachment(wInclRownumField, margin);	
		wResetRownum.setLayoutData(fdRownum);
		
		
		fdAdditionalFields = new FormData();
		fdAdditionalFields.left = new FormAttachment(0, margin);
		fdAdditionalFields.top = new FormAttachment(wValName, margin);
		fdAdditionalFields.right = new FormAttachment(100, -margin);
		wAdditionalFields.setLayoutData(fdAdditionalFields);
		
		///////////////////////////////// 
		// END OF Additional Fields GROUP  //
		///////////////////////////////// 
		
		
        wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel}, margin, wAdditionalFields);
        
		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wValName.addSelectionListener( lsDef );
				
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
		    if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return stepname;
	}
	public void setIncludeRownum()
	{
		wlInclRownumField.setEnabled(wInclRownum.getSelection());
		wInclRownumField.setEnabled(wInclRownum.getSelection());
		wlResetRownum.setEnabled(wInclRownum.getSelection());
		wResetRownum.setEnabled(wInclRownum.getSelection());
	}
	public void getData()
	{	
		wStepname.selectAll();

		wSplitfield.setText(Const.NVL(input.getSplitField(), ""));
		wDelimiter.setText(Const.NVL(input.getDelimiter(), ""));
		wValName.setText(Const.NVL(input.getNewFieldname(), ""));
		wInclRownum.setSelection(input.includeRowNumber());
		if (input.getRowNumberField()!=null) wInclRownumField.setText(input.getRowNumberField());
		wResetRownum.setSelection(input.resetRowNumber());
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
		input.setSplitField( wSplitfield.getText() );
		input.setDelimiter( wDelimiter.getText() );
		input.setNewFieldname(wValName.getText());
		input.setIncludeRowNumber( wInclRownum.getSelection() );
		input.setRowNumberField( wInclRownumField.getText() );
		input.setResetRowNumber( wResetRownum.getSelection() );
		dispose();
	}
}
