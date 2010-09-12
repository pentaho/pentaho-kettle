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
 * Created on 18-mei-2003
 *
 */

package org.pentaho.di.ui.trans.steps.filelocked;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.filelocked.FileLockedMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class FileLockedDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = FileLockedMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlFileName;
	private CCombo       wFileName;
	private FormData     fdlFileName, fdfileName;

	private Label        wlResult;
	private TextVar      wResult;
	private FormData     fdlResult, fdResult;
	
	private Button       wAddResult;
	private FormData     fdAddResult,fdlAddResult;
	private Label        wlAddResult;

	private FileLockedMeta input;

	private boolean gotPreviousFields=false;
	
	public FileLockedDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(FileLockedMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "FileLockedDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin=Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "FileLockedDialog.Stepname.Label")); //$NON-NLS-1$
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
		wlFileName=new Label(shell, SWT.RIGHT);
		wlFileName.setText(BaseMessages.getString(PKG, "FileLockedDialog.FileName.Label")); //$NON-NLS-1$
 		props.setLook(wlFileName);
		fdlFileName=new FormData();
		fdlFileName.left = new FormAttachment(0, 0);
		fdlFileName.right= new FormAttachment(middle, -margin);
		fdlFileName.top  = new FormAttachment(wStepname, margin);
		wlFileName.setLayoutData(fdlFileName);
		
		
		wFileName=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
		wFileName.setEditable(true);
 		props.setLook(wFileName);
 		wFileName.addModifyListener(lsMod);
		fdfileName=new FormData();
		fdfileName.left = new FormAttachment(middle, 0);
		fdfileName.top  = new FormAttachment(wStepname, margin);
		fdfileName.right= new FormAttachment(100, -margin);
		wFileName.setLayoutData(fdfileName);
		wFileName.addFocusListener(new FocusListener()
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
		
		// Result fieldname ...
		wlResult=new Label(shell, SWT.RIGHT);
		wlResult.setText(BaseMessages.getString(PKG, "FileLockedDialog.ResultField.Label")); //$NON-NLS-1$
 		props.setLook(wlResult);
		fdlResult=new FormData();
		fdlResult.left = new FormAttachment(0, 0);
		fdlResult.right= new FormAttachment(middle, -margin);
		fdlResult.top  = new FormAttachment(wFileName, margin*2);
		wlResult.setLayoutData(fdlResult);

		wResult=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wResult.setToolTipText(BaseMessages.getString(PKG, "FileLockedDialog.ResultField.Tooltip"));
 		props.setLook(wResult);
		wResult.addModifyListener(lsMod);
		fdResult=new FormData();
		fdResult.left = new FormAttachment(middle, 0);
		fdResult.top  = new FormAttachment(wFileName, margin*2);
		fdResult.right= new FormAttachment(100, 0);
		wResult.setLayoutData(fdResult);
		
		// Add filename to result filenames?
		wlAddResult=new Label(shell, SWT.RIGHT);
		wlAddResult.setText(BaseMessages.getString(PKG, "FileLockedDialog.AddResult.Label"));
 		props.setLook(wlAddResult);
		fdlAddResult=new FormData();
		fdlAddResult.left = new FormAttachment(0, 0);
		fdlAddResult.top  = new FormAttachment(wResult, 2*margin);
		fdlAddResult.right= new FormAttachment(middle, -margin);
		wlAddResult.setLayoutData(fdlAddResult);
		wAddResult=new Button(shell, SWT.CHECK );
 		props.setLook(wAddResult);
		wAddResult.setToolTipText(BaseMessages.getString(PKG, "FileLockedDialog.AddResult.Tooltip"));
		fdAddResult=new FormData();
		fdAddResult.left = new FormAttachment(middle, 0);
		fdAddResult.top  = new FormAttachment(wResult, 2*margin);
		wAddResult.setLayoutData(fdAddResult);
		
		

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
		if(log.isDebug()) log.logDebug( BaseMessages.getString(PKG, "FileLockedDialog.Log.GettingKeyInfo")); //$NON-NLS-1$

		if (input.getDynamicFilenameField() !=null)   wFileName.setText(input.getDynamicFilenameField());
		if (input.getResultFieldName()!=null)   wResult.setText(input.getResultFieldName());
		wAddResult.setSelection(input.addResultFilenames());
		
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
		input.setDynamicFilenameField(wFileName.getText() );
		input.setResultFieldName(wResult.getText() );
		input.setaddResultFilenames(wAddResult.getSelection());
		stepname = wStepname.getText(); // return value
		
		dispose();
	}

	 private void get() {
		 if(!gotPreviousFields) {
			try {
				String filefield=wFileName.getText();
				wFileName.removeAll();
				RowMetaInterface r = transMeta.getPrevStepFields(stepname);
				if (r!=null) {
					wFileName.setItems(r.getFieldNames());
				}
				if(filefield!=null) wFileName.setText(filefield);
			} catch(KettleException ke){
				new ErrorDialog(shell, BaseMessages.getString(PKG, "FileLockedDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "FileLockedDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
			gotPreviousFields=true;
		}
	}
	public String toString()
	{
		return this.getClass().getName();
	}
}
