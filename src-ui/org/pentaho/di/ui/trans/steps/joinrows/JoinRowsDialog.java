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

package org.pentaho.di.ui.trans.steps.joinrows;

import java.util.List;

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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.joinrows.JoinRowsMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ConditionEditor;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class JoinRowsDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = JoinRowsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlSortDir;
	private Button       wbSortDir;
	private TextVar      wSortDir;
	private FormData     fdlSortDir, fdbSortDir, fdSortDir;

	private Label        wlPrefix;
	private Text         wPrefix;
	private FormData     fdlPrefix, fdPrefix;

	private Label        wlCache;
	private Text         wCache;
	private FormData     fdlCache, fdCache;

	private Label        wlMainStep;
	private CCombo       wMainStep;
	private FormData     fdlMainStep, fdMainStep;
	
	private Label           wlCondition;
	private ConditionEditor wCondition;
	private FormData        fdlCondition, fdCondition;

	private JoinRowsMeta input;
	private Condition      condition;
	
	private Condition      backupCondition;
	
	public JoinRowsDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(JoinRowsMeta)in;
		condition = input.getCondition();
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
		backupCondition = (Condition)condition.clone(); 

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "JoinRowsDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "JoinRowsDialog.Stepname.Label")); //$NON-NLS-1$
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
		wlSortDir=new Label(shell, SWT.RIGHT);
		wlSortDir.setText(BaseMessages.getString(PKG, "JoinRowsDialog.TempDir.Label")); //$NON-NLS-1$
 		props.setLook(wlSortDir);
		fdlSortDir=new FormData();
		fdlSortDir.left = new FormAttachment(0, 0);
		fdlSortDir.right= new FormAttachment(middle, -margin);
		fdlSortDir.top  = new FormAttachment(wStepname, margin);
		wlSortDir.setLayoutData(fdlSortDir);

		wbSortDir=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbSortDir);
		wbSortDir.setText(BaseMessages.getString(PKG, "JoinRowsDialog.Browse.Button")); //$NON-NLS-1$
		fdbSortDir=new FormData();
		fdbSortDir.right= new FormAttachment(100, 0);
		fdbSortDir.top  = new FormAttachment(wStepname, margin);
        wbSortDir.setLayoutData(fdbSortDir);

    	wSortDir=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wSortDir.setText(BaseMessages.getString(PKG, "JoinRowsDialog.Temp.Label")); //$NON-NLS-1$
 		props.setLook(wSortDir);
		wSortDir.addModifyListener(lsMod);
		fdSortDir=new FormData();
		fdSortDir.left = new FormAttachment(middle, 0);
		fdSortDir.top  = new FormAttachment(wStepname, margin);
		fdSortDir.right= new FormAttachment(wbSortDir, -margin);
		wSortDir.setLayoutData(fdSortDir);

        wbSortDir.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent arg0)
                {
                    DirectoryDialog dd = new DirectoryDialog(shell, SWT.NONE);
                    dd.setFilterPath(wSortDir.getText());
                    String dir = dd.open();
                    if (dir!=null)
                    {
                        wSortDir.setText(dir);
                    }
                }
            }
        );

        // Whenever something changes, set the tooltip to the expanded version:
        wSortDir.addModifyListener(new ModifyListener()
            {
                public void modifyText(ModifyEvent e)
                {
                    wSortDir.setToolTipText(transMeta.environmentSubstitute( wSortDir.getText() ) );
                }
            }
        );

		// Table line...
		wlPrefix=new Label(shell, SWT.RIGHT);
		wlPrefix.setText(BaseMessages.getString(PKG, "JoinRowsDialog.TempFilePrefix.Label")); //$NON-NLS-1$
 		props.setLook(wlPrefix);
		fdlPrefix=new FormData();
		fdlPrefix.left = new FormAttachment(0, 0);
		fdlPrefix.right= new FormAttachment(middle, -margin);
		fdlPrefix.top  = new FormAttachment(wbSortDir, margin*2);
		wlPrefix.setLayoutData(fdlPrefix);
		wPrefix=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wPrefix);
		wPrefix.addModifyListener(lsMod);
		fdPrefix=new FormData();
		fdPrefix.left  = new FormAttachment(middle, 0);
		fdPrefix.top   = new FormAttachment(wbSortDir, margin*2);
		fdPrefix.right = new FormAttachment(100, 0);
		wPrefix.setLayoutData(fdPrefix);
		wPrefix.setText(BaseMessages.getString(PKG, "JoinRowsDialog.Prefix.Label")); //$NON-NLS-1$

		// Cache size...
		wlCache=new Label(shell, SWT.RIGHT);
		wlCache.setText(BaseMessages.getString(PKG, "JoinRowsDialog.Cache.Label")); //$NON-NLS-1$
 		props.setLook(wlCache);
		fdlCache=new FormData();
		fdlCache.left = new FormAttachment(0, 0);
		fdlCache.right= new FormAttachment(middle, -margin);
		fdlCache.top  = new FormAttachment(wPrefix, margin*2);
		wlCache.setLayoutData(fdlCache);
		wCache=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
 		props.setLook(wCache);
		wCache.addModifyListener(lsMod);
		fdCache=new FormData();
		fdCache.left  = new FormAttachment(middle, 0);
		fdCache.top   = new FormAttachment(wPrefix, margin*2);
		fdCache.right = new FormAttachment(100, 0);
		wCache.setLayoutData(fdCache);

		// Read date from...
		wlMainStep=new Label(shell, SWT.RIGHT);
		wlMainStep.setText(BaseMessages.getString(PKG, "JoinRowsDialog.MainStep.Label")); //$NON-NLS-1$
 		props.setLook(wlMainStep);
		fdlMainStep=new FormData();
		fdlMainStep.left = new FormAttachment(0, 0);
		fdlMainStep.right= new FormAttachment(middle, -margin);
		fdlMainStep.top  = new FormAttachment(wCache, margin);
		wlMainStep.setLayoutData(fdlMainStep);
		wMainStep=new CCombo(shell, SWT.BORDER );
 		props.setLook(wMainStep);
 		
 		List<StepMeta> prevSteps = transMeta.findPreviousSteps(transMeta.findStep(stepname));
		for (StepMeta stepMeta : prevSteps) {
			wMainStep.add(stepMeta.getName());
		}
		
		wMainStep.addModifyListener(lsMod);
		fdMainStep=new FormData();
		fdMainStep.left = new FormAttachment(middle, 0);
		fdMainStep.top  = new FormAttachment(wCache, margin);
		fdMainStep.right= new FormAttachment(100, 0);
		wMainStep.setLayoutData(fdMainStep);


		// Condition widget...
		wlCondition=new Label(shell, SWT.NONE);
		wlCondition.setText(BaseMessages.getString(PKG, "JoinRowsDialog.Condition.Label")); //$NON-NLS-1$
 		props.setLook(wlCondition);
		fdlCondition=new FormData();
		fdlCondition.left  = new FormAttachment(0, 0);
		fdlCondition.top   = new FormAttachment(wMainStep, margin);
		wlCondition.setLayoutData(fdlCondition);
		
		RowMetaInterface inputfields = null;
		try
		{
			inputfields = transMeta.getPrevStepFields(stepname);
		}
		catch(KettleException ke)
		{
			inputfields = new RowMeta();
			new ErrorDialog(shell, BaseMessages.getString(PKG, "JoinRowsDialog.FailedToGetFields.DialogTitle"), BaseMessages.getString(PKG, "JoinRowsDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

		wCondition = new ConditionEditor(shell, SWT.BORDER, condition, inputfields);
		
		fdCondition=new FormData();
		fdCondition.left  = new FormAttachment(0, 0);
		fdCondition.top   = new FormAttachment(wlCondition, margin);
		fdCondition.right = new FormAttachment(100, 0);
		fdCondition.bottom= new FormAttachment(wOK, -2*margin);
		wCondition.setLayoutData(fdCondition);
		wCondition.addModifyListener(lsMod);



		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wSortDir.addSelectionListener( lsDef );
		wPrefix.addSelectionListener( lsDef );
		
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
		if (input.getPrefix() != null) wPrefix.setText(input.getPrefix());
		if (input.getDirectory() != null) wSortDir.setText(input.getDirectory());
		wCache.setText(""+input.getCacheSize()); //$NON-NLS-1$
		if (input.getLookupStepname() != null) wMainStep.setText(input.getLookupStepname());
		
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		input.setCondition(backupCondition);
		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		if (wCondition.getLevel()>0) 
		{
			wCondition.goUp();
		}
		else
		{
		    stepname = wStepname.getText(); // return value
		    
			input.setPrefix( wPrefix.getText() );
			input.setDirectory( wSortDir.getText() );
			input.setCacheSize( Const.toInt(wCache.getText(), -1) );
			input.setMainStep( transMeta.findStep( wMainStep.getText() ) );
			
			dispose();
		}
	}
	
}
