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
 * Created on 2-jul-2003
 *
 */

package org.pentaho.di.ui.trans.steps.getslavesequence;

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
import org.pentaho.di.trans.steps.getslavesequence.GetSlaveSequenceMeta;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class GetSlaveSequenceDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = GetSlaveSequenceMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private Label        wlValuename;
  private Text         wValuename;

  private Label        wlSlaveServer;
  private ComboVar     wSlaveServer;

  private Label        wlSeqname;
  private Text         wSeqname;

  private Label        wlIncrement;
  private Text         wIncrement;

	private GetSlaveSequenceMeta input;
	
	public GetSlaveSequenceDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(GetSlaveSequenceMeta)in;
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
		shell.setText(BaseMessages.getString(PKG, "GetSequenceDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "GetSequenceDialog.StepName.Label")); //$NON-NLS-1$
 		props.setLook(		wlStepname);
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

		// Valuename line
		wlValuename=new Label(shell, SWT.RIGHT);
		wlValuename.setText(BaseMessages.getString(PKG, "GetSequenceDialog.Valuename.Label")); //$NON-NLS-1$
 		props.setLook(wlValuename);
		FormData fdlValuename = new FormData();
		fdlValuename.left = new FormAttachment(0, 0);
		fdlValuename.top  = new FormAttachment(wStepname, margin);
		fdlValuename.right= new FormAttachment(middle, -margin);
		wlValuename.setLayoutData(fdlValuename);
		wValuename=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wValuename.setText(""); //$NON-NLS-1$
 		props.setLook(wValuename);
		wValuename.addModifyListener(lsMod);
		FormData fdValuename = new FormData();
		fdValuename.left = new FormAttachment(middle, 0);
		fdValuename.top  = new FormAttachment(wStepname, margin);
		fdValuename.right= new FormAttachment(100, 0);
		wValuename.setLayoutData(fdValuename);
    
		// Connection line
		//
		wlSlaveServer = new Label(shell, SWT.RIGHT);
		wlSlaveServer.setText(BaseMessages.getString(PKG, "GetSequenceDialog.SlaveServer.Label")); //$NON-NLS-1$
    props.setLook(wlSlaveServer);
    FormData fdlSlaveServer = new FormData();
    fdlSlaveServer.left = new FormAttachment(0, 0);
    fdlSlaveServer.top  = new FormAttachment(wValuename, margin);
    fdlSlaveServer.right= new FormAttachment(middle, -margin);
    wlSlaveServer.setLayoutData(fdlSlaveServer);
    wSlaveServer = new ComboVar(transMeta, shell, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
    wSlaveServer.setItems(transMeta.getSlaveServerNames());
    FormData fdSlaveServer = new FormData();
    fdSlaveServer.left = new FormAttachment(middle, 0);
    fdSlaveServer.top  = new FormAttachment(wValuename, margin);
    fdSlaveServer.right= new FormAttachment(100, 0);
    wSlaveServer.setLayoutData(fdSlaveServer);

		// Seqname line
		wlSeqname=new Label(shell, SWT.RIGHT);
		wlSeqname.setText(BaseMessages.getString(PKG, "GetSequenceDialog.Seqname.Label")); //$NON-NLS-1$
 		props.setLook(wlSeqname);
		FormData fdlSeqname = new FormData();
		fdlSeqname.left = new FormAttachment(0, 0);
		fdlSeqname.right= new FormAttachment(middle, -margin);
		fdlSeqname.top  = new FormAttachment(wSlaveServer, margin);
		wlSeqname.setLayoutData(fdlSeqname);
		
		wSeqname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wSeqname.setText(""); //$NON-NLS-1$
 		props.setLook(wSeqname);
		wSeqname.addModifyListener(lsMod);
		FormData fdSeqname = new FormData();
		fdSeqname.left = new FormAttachment(middle, 0);
		fdSeqname.top  = new FormAttachment(wSlaveServer, margin);
		fdSeqname.right= new FormAttachment(100, 0);
		wSeqname.setLayoutData(fdSeqname);

    // Increment line
    wlIncrement=new Label(shell, SWT.RIGHT);
    wlIncrement.setText(BaseMessages.getString(PKG, "GetSequenceDialog.Increment.Label")); //$NON-NLS-1$
    props.setLook(wlIncrement);
    FormData fdlIncrement = new FormData();
    fdlIncrement.left = new FormAttachment(0, 0);
    fdlIncrement.right= new FormAttachment(middle, -margin);
    fdlIncrement.top  = new FormAttachment(wSeqname, margin);
    wlIncrement.setLayoutData(fdlIncrement);
    
    wIncrement=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wIncrement.setText(""); //$NON-NLS-1$
    props.setLook(wIncrement);
    wIncrement.addModifyListener(lsMod);
    FormData fdIncrement = new FormData();
    fdIncrement.left = new FormAttachment(middle, 0);
    fdIncrement.top  = new FormAttachment(wSeqname, margin);
    fdIncrement.right= new FormAttachment(100, 0);
    wIncrement.setLayoutData(fdIncrement);

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wIncrement);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wValuename.addSelectionListener( lsDef);
		wSeqname.addSelectionListener( lsDef );
    wIncrement.addSelectionListener( lsDef );
		
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
		logDebug(BaseMessages.getString(PKG, "GetSequenceDialog.Log.GettingKeyInfo")); //$NON-NLS-1$

		wValuename.setText( Const.NVL(input.getValuename(), ""));
		wSlaveServer.setText( Const.NVL(input.getSlaveServerName(), "") );
		wSeqname.setText(Const.NVL(input.getSequenceName(), ""));
    wIncrement.setText(Const.NVL(input.getIncrement(), ""));

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
		
		stepname = wStepname.getText(); // return value

		input.setSlaveServerName(wSlaveServer.getText());
		input.setValuename(wValuename.getText());
    input.setSequenceName(wSeqname.getText());
    input.setIncrement(wIncrement.getText());
		
		dispose();
	}
	
}