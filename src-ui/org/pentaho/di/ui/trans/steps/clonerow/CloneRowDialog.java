 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

/*
 * Created on 18-06-2008
 *
 */

package org.pentaho.di.ui.trans.steps.clonerow;

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
import org.pentaho.di.trans.steps.clonerow.CloneRowMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class CloneRowDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = CloneRowMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CloneRowMeta input;

	// nr clones
	private Label wlnrClone;
	private TextVar wnrClone;
	private FormData fdlnrClone, fdnrClone;
	
	private Label wlcloneFlagField, wladdCloneNum, wlCloneNumField;
	private TextVar wcloneFlagField, wCloneNumField;
	private FormData fdlcloneFlagField, fdcloneFlagField, fdladdCloneNum, fdCloneNumField;
	

	private FormData fdOutpuFields;
	private Group wOutpuFields;
	
	private Label        wladdCloneFlag;
	private Button       waddCloneFlag, waddCloneNum;
	private FormData     fdladdCloneFlag, fdaddCloneFlag, fdaddCloneNum;
	
    private Label wlisNrCloneInField,wlNrCloneField;
    private CCombo wNrCloneField;
    private FormData fdlisNrCloneInField,fdisNrCloneInField;
	private FormData fdlNrCloneField,fdNrCloneField;
	
	private Button wisNrCloneInField;
	
    private boolean gotPreviousFields=false;
	
	public CloneRowDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(CloneRowMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "CloneRowDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "CloneRowDialog.Stepname.Label")); //$NON-NLS-1$
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
		
		// Number of clones line
		wlnrClone = new Label(shell, SWT.RIGHT);
		wlnrClone.setText(BaseMessages.getString(PKG, "CloneRowDialog.nrClone.Label"));
		props.setLook(wlnrClone);
		fdlnrClone = new FormData();
		fdlnrClone.left = new FormAttachment(0, 0);
		fdlnrClone.right = new FormAttachment(middle, -margin);
		fdlnrClone.top = new FormAttachment(wStepname, margin*2);
		wlnrClone.setLayoutData(fdlnrClone);

		wnrClone = new TextVar(transMeta,shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wnrClone);
		wnrClone.setToolTipText(BaseMessages.getString(PKG, "CloneRowDialog.nrClone.Tooltip"));
		wnrClone.addModifyListener(lsMod);
		fdnrClone = new FormData();
		fdnrClone.left = new FormAttachment(middle, 0);
		fdnrClone.top = new FormAttachment(wStepname, margin*2);
		fdnrClone.right = new FormAttachment(100, 0);
		wnrClone.setLayoutData(fdnrClone);
		
		//Is Nr clones defined in a Field		
		wlisNrCloneInField = new Label(shell, SWT.RIGHT);
		wlisNrCloneInField.setText(BaseMessages.getString(PKG, "CloneRowDialog.isNrCloneInField.Label"));
		props.setLook(wlisNrCloneInField);
		fdlisNrCloneInField = new FormData();
		fdlisNrCloneInField.left = new FormAttachment(0, 0);
		fdlisNrCloneInField.top = new FormAttachment(wnrClone, margin);
		fdlisNrCloneInField.right = new FormAttachment(middle, -margin);
		wlisNrCloneInField.setLayoutData(fdlisNrCloneInField);
		
		
		wisNrCloneInField = new Button(shell, SWT.CHECK);
		props.setLook(wisNrCloneInField);
		wisNrCloneInField.setToolTipText(BaseMessages.getString(PKG, "CloneRowDialog.isNrCloneInField.Tooltip"));
		fdisNrCloneInField = new FormData();
		fdisNrCloneInField.left = new FormAttachment(middle, 0);
		fdisNrCloneInField.top = new FormAttachment(wnrClone, margin);
		wisNrCloneInField.setLayoutData(fdisNrCloneInField);		
		SelectionAdapter lisNrCloneInField = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	ActiveisNrCloneInField();
            	input.setChanged();
            }
        };
        wisNrCloneInField.addSelectionListener(lisNrCloneInField);
        
		// Filename field
		wlNrCloneField=new Label(shell, SWT.RIGHT);
        wlNrCloneField.setText(BaseMessages.getString(PKG, "CloneRowDialog.wlNrCloneField.Label"));
        props.setLook(wlNrCloneField);
        fdlNrCloneField=new FormData();
        fdlNrCloneField.left = new FormAttachment(0, 0);
        fdlNrCloneField.top  = new FormAttachment(wisNrCloneInField, margin);
        fdlNrCloneField.right= new FormAttachment(middle, -margin);
        wlNrCloneField.setLayoutData(fdlNrCloneField);
        
        
        wNrCloneField=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
        wNrCloneField.setEditable(true);
        props.setLook(wNrCloneField);
        wNrCloneField.addModifyListener(lsMod);
        fdNrCloneField=new FormData();
        fdNrCloneField.left = new FormAttachment(middle, 0);
        fdNrCloneField.top  = new FormAttachment(wisNrCloneInField, margin);
        fdNrCloneField.right= new FormAttachment(100, 0);
        wNrCloneField.setLayoutData(fdNrCloneField);
        wNrCloneField.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    setisNrCloneInField();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );   
		
		
		// ///////////////////////////////
		// START OF Origin files GROUP  //
		///////////////////////////////// 

		wOutpuFields= new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wOutpuFields);
		wOutpuFields.setText(BaseMessages.getString(PKG, "CloneRowDialog.wOutpuFields.Label"));
		
		FormLayout OutpuFieldsgroupLayout = new FormLayout();
		OutpuFieldsgroupLayout.marginWidth = 10;
		OutpuFieldsgroupLayout.marginHeight = 10;
		wOutpuFields.setLayout(OutpuFieldsgroupLayout);

		// add clone flag?
		wladdCloneFlag=new Label(wOutpuFields, SWT.RIGHT);
		wladdCloneFlag.setText(BaseMessages.getString(PKG, "CloneRowDialog.addCloneFlag.Label"));
		props.setLook(wladdCloneFlag);
		fdladdCloneFlag=new FormData();
		fdladdCloneFlag.left  = new FormAttachment(0, 0);
		fdladdCloneFlag.top   = new FormAttachment(wNrCloneField, 2*margin);
		fdladdCloneFlag.right = new FormAttachment(middle, -margin);
		wladdCloneFlag.setLayoutData(fdladdCloneFlag);
		waddCloneFlag=new Button(wOutpuFields, SWT.CHECK);
		waddCloneFlag.setToolTipText(BaseMessages.getString(PKG, "CloneRowDialog.addCloneFlag.Tooltip"));
 		props.setLook(waddCloneFlag);
		fdaddCloneFlag=new FormData();
		fdaddCloneFlag.left  = new FormAttachment(middle, 0);
		fdaddCloneFlag.top   = new FormAttachment(wNrCloneField, 2*margin);
		fdaddCloneFlag.right = new FormAttachment(100, 0);
		waddCloneFlag.setLayoutData(fdaddCloneFlag);
		SelectionAdapter lsSelR = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
                input.setChanged();
                activeaddCloneFlag();
            }
        };
		waddCloneFlag.addSelectionListener(lsSelR);
		
		// clone falg field line
		wlcloneFlagField = new Label(wOutpuFields, SWT.RIGHT);
		wlcloneFlagField.setText(BaseMessages.getString(PKG, "CloneRowDialog.cloneFlagField.Label"));
		props.setLook(wlcloneFlagField);
		fdlcloneFlagField = new FormData();
		fdlcloneFlagField.left = new FormAttachment(0, 0);
		fdlcloneFlagField.right = new FormAttachment(middle, -margin);
		fdlcloneFlagField.top = new FormAttachment(waddCloneFlag, margin*2);
		wlcloneFlagField.setLayoutData(fdlcloneFlagField);

		wcloneFlagField = new TextVar(transMeta,wOutpuFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wcloneFlagField);
		wcloneFlagField.setToolTipText(BaseMessages.getString(PKG, "CloneRowDialog.cloneFlagField.Tooltip"));
		wcloneFlagField.addModifyListener(lsMod);
		fdcloneFlagField = new FormData();
		fdcloneFlagField.left = new FormAttachment(middle, 0);
		fdcloneFlagField.top = new FormAttachment(waddCloneFlag, margin*2);
		fdcloneFlagField.right = new FormAttachment(100, 0);
		wcloneFlagField.setLayoutData(fdcloneFlagField);
				
		
		// add clone num?
		wladdCloneNum=new Label(wOutpuFields, SWT.RIGHT);
		wladdCloneNum.setText(BaseMessages.getString(PKG, "CloneRowDialog.addCloneNum.Label"));
		props.setLook(wladdCloneNum);
		fdladdCloneNum=new FormData();
		fdladdCloneNum.left  = new FormAttachment(0, 0);
		fdladdCloneNum.top   = new FormAttachment(wcloneFlagField, margin);
		fdladdCloneNum.right = new FormAttachment(middle, -margin);
		wladdCloneNum.setLayoutData(fdladdCloneNum);
		waddCloneNum=new Button(wOutpuFields, SWT.CHECK);
		waddCloneNum.setToolTipText(BaseMessages.getString(PKG, "CloneRowDialog.addCloneNum.Tooltip"));
 		props.setLook(waddCloneNum);
		fdaddCloneNum=new FormData();
		fdaddCloneNum.left  = new FormAttachment(middle, 0);
		fdaddCloneNum.top   = new FormAttachment(wcloneFlagField, margin);
		fdaddCloneNum.right = new FormAttachment(100, 0);
		waddCloneNum.setLayoutData(fdaddCloneNum);
		waddCloneNum.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
                input.setChanged();
                activeaddCloneNum();
            }
        });
		
		// clone num field line
		wlCloneNumField= new Label(wOutpuFields, SWT.RIGHT);
		wlCloneNumField.setText(BaseMessages.getString(PKG, "CloneRowDialog.cloneNumField.Label"));
		props.setLook(wlCloneNumField);
		fdlcloneFlagField= new FormData();
		fdlcloneFlagField.left = new FormAttachment(0, 0);
		fdlcloneFlagField.right = new FormAttachment(middle, -margin);
		fdlcloneFlagField.top = new FormAttachment(waddCloneNum, margin);
		wlCloneNumField.setLayoutData(fdlcloneFlagField);

		wCloneNumField= new TextVar(transMeta,wOutpuFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wCloneNumField);
		wCloneNumField.setToolTipText(BaseMessages.getString(PKG, "CloneRowDialog.cloneNumField.Tooltip"));
		wCloneNumField.addModifyListener(lsMod);
		fdCloneNumField= new FormData();
		fdCloneNumField.left = new FormAttachment(middle, 0);
		fdCloneNumField.top = new FormAttachment(waddCloneNum, margin);
		fdCloneNumField.right = new FormAttachment(100, 0);
		wCloneNumField.setLayoutData(fdCloneNumField);
				
		
		
		

		fdOutpuFields= new FormData();
		fdOutpuFields.left = new FormAttachment(0, margin);
		fdOutpuFields.top = new FormAttachment(wNrCloneField, 2*margin);
		fdOutpuFields.right = new FormAttachment(100, -margin);
		wOutpuFields.setLayoutData(fdOutpuFields);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Origin files GROUP
		// ///////////////////////////////////////////////////////////		

		
		
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wOutpuFields);

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
		activeaddCloneFlag();
		ActiveisNrCloneInField();
		activeaddCloneNum();
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	private void setisNrCloneInField()
	 {
		 if(!gotPreviousFields)
		 {
		 try{
	         String  field=wNrCloneField.getText(); 
	         wNrCloneField.removeAll();
			 RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			 if(r!=null) wNrCloneField.setItems(r.getFieldNames());
			 if(field!=null) wNrCloneField.setText(field);	
		 }catch(KettleException ke){
				new ErrorDialog(shell, BaseMessages.getString(PKG, "CloneRowDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "CloneRowDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
		 gotPreviousFields=true;
		 }
	 }
	private void ActiveisNrCloneInField()
	{
		wlNrCloneField.setEnabled(wisNrCloneInField.getSelection());
		wNrCloneField.setEnabled(wisNrCloneInField.getSelection());
		wlnrClone.setEnabled(!wisNrCloneInField.getSelection());
		wnrClone.setEnabled(!wisNrCloneInField.getSelection());
	}
	private void activeaddCloneFlag()
	{
		wlcloneFlagField.setEnabled(waddCloneFlag.getSelection());
		wcloneFlagField.setEnabled(waddCloneFlag.getSelection());
	}
	private void activeaddCloneNum()
	{
		wlCloneNumField.setEnabled(waddCloneNum.getSelection());
		wCloneNumField.setEnabled(waddCloneNum.getSelection());
	}
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wStepname.selectAll();
		if (input.getNrClones() !=null)   wnrClone.setText(input.getNrClones());
		waddCloneFlag.setSelection(input.isAddCloneFlag());
		if (input.getCloneFlagField() !=null)   wcloneFlagField.setText(input.getCloneFlagField());
		wisNrCloneInField.setSelection(input.isNrCloneInField());
		if (input.getNrCloneField() !=null)   wNrCloneField.setText(input.getNrCloneField());
		waddCloneNum.setSelection(input.isAddCloneNum());
		if (input.getCloneNumField() !=null)   wCloneNumField.setText(input.getCloneNumField());
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
		input.setNrClones(wnrClone.getText());
		input.setAddCloneFlag(waddCloneFlag.getSelection());
		input.setCloneFlagField(wcloneFlagField.getText());
		input.setNrCloneInField(wisNrCloneInField.getSelection());
		input.setNrCloneField(wNrCloneField.getText());
		input.setAddCloneNum(waddCloneNum.getSelection());
		input.setCloneNumField(wCloneNumField.getText());
		dispose();
	}
}
