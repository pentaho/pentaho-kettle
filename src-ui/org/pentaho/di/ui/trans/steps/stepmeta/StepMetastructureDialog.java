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

package org.pentaho.di.ui.trans.steps.stepmeta;

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
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.stepmeta.StepMetastructureMeta;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class StepMetastructureDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = StepMetastructureMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private StepMetastructureMeta input;
	
	private Label        wlOutputRowcount;
    private Button       wOutputRowcount;
    private FormData     fdlOutputRowcount, fdOutputRowcount;
	
	private Label        wlRowcountField;
	private TextVar      wRowcountField;
	private FormData     fdlRowcountField, fdRowcountField;


	public StepMetastructureDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(StepMetastructureMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "StepMetastructureDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "StepMetastructureDialog.Stepname.Label")); //$NON-NLS-1$
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
		
		// Rowcout Output
        wlOutputRowcount=new Label(shell, SWT.RIGHT);
        wlOutputRowcount.setText(BaseMessages.getString(PKG, "StepMetastructureDialog.outputRowcount.Label"));
        props.setLook(wlOutputRowcount);
        fdlOutputRowcount=new FormData();
        fdlOutputRowcount.left = new FormAttachment(0, 0);
        fdlOutputRowcount.top  = new FormAttachment(wStepname, margin);
        fdlOutputRowcount.right= new FormAttachment(middle, -margin);
        wlOutputRowcount.setLayoutData(fdlOutputRowcount);
        wOutputRowcount=new Button(shell, SWT.CHECK );
        props.setLook(wOutputRowcount);
        fdOutputRowcount=new FormData();
        fdOutputRowcount.left = new FormAttachment(middle, 0);
        fdOutputRowcount.top  = new FormAttachment(wStepname, margin);
        fdOutputRowcount.right= new FormAttachment(100, 0);
        wOutputRowcount.setLayoutData(fdOutputRowcount);
        wOutputRowcount.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent e) 
                {
                	input.setChanged();
                	
                	if(wOutputRowcount.getSelection()){
                		wRowcountField.setEnabled(true);
                	}
                	else{
                		wRowcountField.setEnabled(false);
                	}              
                }
            }
        );

		// Rowcout Field
		wlRowcountField=new Label(shell, SWT.RIGHT);
		wlRowcountField.setText(BaseMessages.getString(PKG, "StepMetastructureDialog.RowcountField.Label")); //$NON-NLS-1$
 		props.setLook(wlRowcountField);
		fdlRowcountField=new FormData();
		fdlRowcountField.left = new FormAttachment(0, 0);
		fdlRowcountField.right= new FormAttachment(middle, -margin);
		fdlRowcountField.top  = new FormAttachment(wOutputRowcount, margin);
		wlRowcountField.setLayoutData(fdlRowcountField);

    	wRowcountField=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wRowcountField);
		wRowcountField.addModifyListener(lsMod);
		fdRowcountField=new FormData();
		fdRowcountField.left = new FormAttachment(middle, 0);
		fdRowcountField.top  = new FormAttachment(wOutputRowcount, margin);
		fdRowcountField.right= new FormAttachment(100, -margin);
		wRowcountField.setLayoutData(fdRowcountField);
		wRowcountField.setEnabled(false);
		
		
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wRowcountField);

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
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wStepname.selectAll();
		if (input.getRowcountField()     != null) wRowcountField.setText(input.getRowcountField());
		
		if(input.isOutputRowcount()){
        	wRowcountField.setEnabled(true);
        }
        
        wOutputRowcount.setSelection(input.isOutputRowcount());
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
		
		getInfo(input);
		
		dispose();
	}
	
	private void getInfo(StepMetastructureMeta tfoi)
    {
    	tfoi.setOutputRowcount(wOutputRowcount.getSelection());
    	tfoi.setRowcountField(wRowcountField.getText());
    }
}
