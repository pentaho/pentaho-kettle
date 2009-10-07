 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

/*
 * Created on 18-mei-2003
 *
 */

package org.pentaho.di.ui.trans.steps.closure;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.closure.ClosureGeneratorMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class ClosureGeneratorDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = ClosureGeneratorMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlRootZero;
	private Button       wRootZero;
	private FormData     fdlRootZero, fdRootZero;

	private Label        wlParent;
	private CCombo       wParent;
	private FormData     fdlParent, fdParent;

	private Label        wlChild;
	private CCombo       wChild;
	private FormData     fdlChild, fdChild;

	private Label        wlDistance;
	private Text         wDistance;
	private FormData     fdlDistance, fdDistance;

	private ClosureGeneratorMeta input;
	
	private RowMetaInterface inputFields;

	public ClosureGeneratorDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(ClosureGeneratorMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "ClosureGeneratorDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

        // Stepname line
		//
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "ClosureGeneratorDialog.StepName")); //$NON-NLS-1$
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

		// Parent ...
		//
		wlParent=new Label(shell, SWT.RIGHT);
		wlParent.setText(BaseMessages.getString(PKG, "ClosureGeneratorDialog.ParentField.Label")); //$NON-NLS-1$
 		props.setLook(wlParent);
		fdlParent=new FormData();
		fdlParent.left = new FormAttachment(0, 0);
		fdlParent.right= new FormAttachment(middle, -margin);
		fdlParent.top  = new FormAttachment(wStepname, margin);
		wlParent.setLayoutData(fdlParent);

		wParent=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wParent);
		wParent.addModifyListener(lsMod);
		fdParent=new FormData();
		fdParent.left = new FormAttachment(middle, 0);
		fdParent.right= new FormAttachment(100, 0);
		fdParent.top  = new FormAttachment(wStepname, margin);
		wParent.setLayoutData(fdParent);

		// Child ...
		//
		wlChild=new Label(shell, SWT.RIGHT);
		wlChild.setText(BaseMessages.getString(PKG, "ClosureGeneratorDialog.ChildField.Label")); //$NON-NLS-1$
 		props.setLook(wlChild);
		fdlChild=new FormData();
		fdlChild.left = new FormAttachment(0, 0);
		fdlChild.right= new FormAttachment(middle, -margin);
		fdlChild.top  = new FormAttachment(wParent, margin);
		wlChild.setLayoutData(fdlChild);

		wChild=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wChild);
		wChild.addModifyListener(lsMod);
		fdChild=new FormData();
		fdChild.left = new FormAttachment(middle, 0);
		fdChild.right= new FormAttachment(100, 0);
		fdChild.top  = new FormAttachment(wParent, margin);
		wChild.setLayoutData(fdChild);

		// Distance ...
		//
		wlDistance=new Label(shell, SWT.RIGHT);
		wlDistance.setText(BaseMessages.getString(PKG, "ClosureGeneratorDialog.DistanceField.Label")); //$NON-NLS-1$
 		props.setLook(wlDistance);
		fdlDistance=new FormData();
		fdlDistance.left = new FormAttachment(0, 0);
		fdlDistance.right= new FormAttachment(middle, -margin);
		fdlDistance.top  = new FormAttachment(wChild, margin);
		wlDistance.setLayoutData(fdlDistance);

		wDistance=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wDistance);
		wDistance.addModifyListener(lsMod);
		fdDistance=new FormData();
		fdDistance.left = new FormAttachment(middle, 0);
		fdDistance.right= new FormAttachment(100, 0);
		fdDistance.top  = new FormAttachment(wChild, margin);
		wDistance.setLayoutData(fdDistance);

		// Root is zero(Integer)?
		//
		wlRootZero=new Label(shell, SWT.RIGHT);
		wlRootZero.setText(BaseMessages.getString(PKG, "ClosureGeneratorDialog.RootZero.Label")); //$NON-NLS-1$
 		props.setLook(wlRootZero);
		fdlRootZero=new FormData();
		fdlRootZero.left = new FormAttachment(0, 0);
		fdlRootZero.right= new FormAttachment(middle, -margin);
		fdlRootZero.top  = new FormAttachment(wDistance, margin);
		wlRootZero.setLayoutData(fdlRootZero);

		wRootZero=new Button(shell, SWT.CHECK);
 		props.setLook(wRootZero);
		fdRootZero=new FormData();
		fdRootZero.left = new FormAttachment(middle, 0);
		fdRootZero.right= new FormAttachment(100, 0);
		fdRootZero.top  = new FormAttachment(wDistance, margin);
		wRootZero.setLayoutData(fdRootZero);
		
		
        // Search the fields in the background
        //
        final Runnable runnable = new Runnable()
        {
            public void run()
            {
                StepMeta stepMeta = transMeta.findStep(stepname);
                if (stepMeta!=null)
                {
                    try
                    {
                        inputFields = transMeta.getPrevStepFields(stepMeta);
                        setComboBoxes();
                    }
                    catch(KettleException e)
                    {
                        logError(BaseMessages.getString(PKG, "ClosureGeneratorDialog.Log.UnableToFindInput"));
                    }
                }
            }
        };
        new Thread(runnable).start();

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel, }, margin, null);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();  } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();      } };
        
		wCancel.addListener  (SWT.Selection, lsCancel);
		wOK.addListener      (SWT.Selection, lsOK    );
        
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wDistance.addSelectionListener( lsDef );
		
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
	
	protected void setComboBoxes()
    {
        shell.getDisplay().syncExec(new Runnable()
            {
                public void run()
                {
                	if (inputFields!=null) {
                		String[] fieldNames = inputFields.getFieldNames();
                		wParent.setItems(fieldNames);
                		wChild.setItems(fieldNames);
                	}
                }
            }
        );
    }


	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (input.getParentIdFieldName() != null) wParent.setText(input.getParentIdFieldName());
		if (input.getChildIdFieldName() != null) wChild.setText(input.getChildIdFieldName());
		if (input.getDistanceFieldName() != null) wDistance.setText(input.getDistanceFieldName());
		wRootZero.setSelection(input.isRootIdZero());
               
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
    private void getInfo(ClosureGeneratorMeta meta)
    {
    	meta.setParentIdFieldName(wParent.getText());
    	meta.setChildIdFieldName(wChild.getText());
    	meta.setDistanceFieldName(wDistance.getText());
    	meta.setRootIdZero(wRootZero.getSelection());
    }
    
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		stepname = wStepname.getText(); // return value
        getInfo(input);
        
		dispose();
	}	
}
