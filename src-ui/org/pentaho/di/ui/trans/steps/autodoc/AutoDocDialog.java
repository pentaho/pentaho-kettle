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

package org.pentaho.di.ui.trans.steps.autodoc;

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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.autodoc.AutoDoc;
import org.pentaho.di.trans.steps.autodoc.AutoDocMeta;
import org.pentaho.di.trans.steps.autodoc.KettleReportBuilder;
import org.pentaho.di.trans.steps.autodoc.KettleReportBuilder.OutputType;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.LabelComboVar;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class AutoDocDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = AutoDoc.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private AutoDocMeta inputMeta;
	
	private LabelComboVar wFilenameField;
  private LabelComboVar wFileTypeField;
	private LabelTextVar  wTargetFilename;
	private LabelComboVar  wOutputType;

	private Button wInclName;
	private Button wInclDesc;
	private Button wInclExtDesc;
	private Button wInclCreated;
	private Button wInclModified;
	private Button wInclImage;
	private Button wInclLogging;
	private Button wInclLastExecResult;

	public AutoDocDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		inputMeta=(AutoDocMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
 		props.setLook(shell);
 		setShellImage(shell, inputMeta);
        
		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				inputMeta.setChanged();
			}
		};
		changed = inputMeta.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "AutoDoc.Step.Name")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Step name line
		//
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "AutoDocDialog.Stepname.Label")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
		Control lastControl = wStepname;
		
			RowMetaInterface previousFields;
			try {
				previousFields = transMeta.getPrevStepFields(stepMeta);
			}
			catch(KettleStepException e) {
				new ErrorDialog(shell, BaseMessages.getString(PKG, "AutoDocDialog.ErrorDialog.UnableToGetInputFields.Title"), BaseMessages.getString(PKG, "AutoDocDialog.ErrorDialog.UnableToGetInputFields.Message"), e);
				previousFields = new RowMeta();
			}
			
		// The filename field ...
		//
		wFilenameField=new LabelComboVar(transMeta, shell, BaseMessages.getString(PKG, "AutoDocDialog.FilenameField.Label"), BaseMessages.getString(PKG, "AutoDocDialog.FilenameField.Tooltip"));
		wFilenameField.setItems(previousFields.getFieldNames());
 		props.setLook(wFilenameField);
 		wFilenameField.addModifyListener(lsMod);
		FormData fdFilenameField = new FormData();
		fdFilenameField.top  = new FormAttachment(lastControl, margin);
		fdFilenameField.left = new FormAttachment(0, 0);
		fdFilenameField.right= new FormAttachment(100, 0);
		wFilenameField.setLayoutData(fdFilenameField);
		lastControl = wFilenameField;

    // The FileType field ...
    //
    wFileTypeField=new LabelComboVar(transMeta, shell, BaseMessages.getString(PKG, "AutoDocDialog.FileTypeField.Label"), BaseMessages.getString(PKG, "AutoDocDialog.FileTypeField.Tooltip"));
    wFileTypeField.setItems(previousFields.getFieldNames());
    props.setLook(wFileTypeField);
    wFileTypeField.addModifyListener(lsMod);
    FormData fdFileTypeField = new FormData();
    fdFileTypeField.top  = new FormAttachment(lastControl, margin);
    fdFileTypeField.left = new FormAttachment(0, 0);
    fdFileTypeField.right= new FormAttachment(100, 0);
    wFileTypeField.setLayoutData(fdFileTypeField);
    lastControl = wFileTypeField;

		// The target filename ...
		//
		wTargetFilename=new LabelTextVar(transMeta, shell, BaseMessages.getString(PKG, "AutoDocDialog.TargetFilename.Label"), BaseMessages.getString(PKG, "AutoDocDialog.TargetFilename.Tooltip"));
 		props.setLook(wTargetFilename);
 		wTargetFilename.addModifyListener(lsMod);
		FormData fdTargetFilename = new FormData();
		fdTargetFilename.top  = new FormAttachment(lastControl, margin);
		fdTargetFilename.left = new FormAttachment(0, 0);
		fdTargetFilename.right= new FormAttachment(100, 0);
		wTargetFilename.setLayoutData(fdTargetFilename);
		lastControl = wTargetFilename;

		// The output type ...
		//
		wOutputType=new LabelComboVar(transMeta, shell, BaseMessages.getString(PKG, "AutoDocDialog.OutputType.Label"), BaseMessages.getString(PKG, "AutoDocDialog.OutputType.Tooltip"));
		OutputType[] outputTypes = KettleReportBuilder.OutputType.values();
		String[] items=new String[outputTypes.length];
		for (int i=0;i<outputTypes.length;i++) {
			items[i] = outputTypes[i].name(); 
		}
		wOutputType.setItems(items);
 		props.setLook(wOutputType);
 		wOutputType.addModifyListener(lsMod);
 		wOutputType.getComboWidget().setEditable(false);
		FormData fdOutputType = new FormData();
		fdOutputType.top  = new FormAttachment(lastControl, margin);
		fdOutputType.left = new FormAttachment(0, 0);
		fdOutputType.right= new FormAttachment(100, 0);
		wOutputType.setLayoutData(fdOutputType);
    wOutputType.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent event) { 
      setFlags(); 
      } });
		lastControl = wOutputType;

		// Include name check-box
		//
		Label wlInclName = new Label(shell, SWT.RIGHT);
		wlInclName.setText(BaseMessages.getString(PKG, "AutoDocDialog.InclName.Label")); //$NON-NLS-1$
 		props.setLook(wlInclName);
		FormData fdlInclName = new FormData();
		fdlInclName.left = new FormAttachment(0, 0);
		fdlInclName.right= new FormAttachment(middle, -margin);
		fdlInclName.top  = new FormAttachment(lastControl, margin);
		wlInclName.setLayoutData(fdlInclName);
		wInclName=new Button(shell, SWT.CHECK | SWT.LEFT);
 		props.setLook(wInclName);
		FormData fdInclName = new FormData();
		fdInclName.left = new FormAttachment(middle, 0);
		fdInclName.right= new FormAttachment(100, 0);
		fdInclName.top  = new FormAttachment(lastControl, margin);
		wInclName.setLayoutData(fdInclName);
		lastControl = wInclName;

		// Include description check-box
		//
		Label wlInclDesc = new Label(shell, SWT.RIGHT);
		wlInclDesc.setText(BaseMessages.getString(PKG, "AutoDocDialog.InclDesc.Label")); //$NON-NLS-1$
 		props.setLook(wlInclDesc);
		FormData fdlInclDesc = new FormData();
		fdlInclDesc.left = new FormAttachment(0, 0);
		fdlInclDesc.right= new FormAttachment(middle, -margin);
		fdlInclDesc.top  = new FormAttachment(lastControl, margin);
		wlInclDesc.setLayoutData(fdlInclDesc);
		wInclDesc=new Button(shell, SWT.CHECK | SWT.LEFT);
 		props.setLook(wInclDesc);
		FormData fdInclDesc = new FormData();
		fdInclDesc.left = new FormAttachment(middle, 0);
		fdInclDesc.right= new FormAttachment(100, 0);
		fdInclDesc.top  = new FormAttachment(lastControl, margin);
		wInclDesc.setLayoutData(fdInclDesc);
		lastControl = wInclDesc;

		// Include extended description check-box
		//
		Label wlInclExtDesc = new Label(shell, SWT.RIGHT);
		wlInclExtDesc.setText(BaseMessages.getString(PKG, "AutoDocDialog.InclExtDesc.Label")); //$NON-NLS-1$
 		props.setLook(wlInclExtDesc);
		FormData fdlInclExtDesc = new FormData();
		fdlInclExtDesc.left = new FormAttachment(0, 0);
		fdlInclExtDesc.right= new FormAttachment(middle, -margin);
		fdlInclExtDesc.top  = new FormAttachment(lastControl, margin);
		wlInclExtDesc.setLayoutData(fdlInclExtDesc);
		wInclExtDesc=new Button(shell, SWT.CHECK | SWT.LEFT);
 		props.setLook(wInclExtDesc);
		FormData fdInclExtDesc = new FormData();
		fdInclExtDesc.left = new FormAttachment(middle, 0);
		fdInclExtDesc.right= new FormAttachment(100, 0);
		fdInclExtDesc.top  = new FormAttachment(lastControl, margin);
		wInclExtDesc.setLayoutData(fdInclExtDesc);
		lastControl = wInclExtDesc;

		// Include creation information (user/date) check-box
		//
		Label wlInclCreated = new Label(shell, SWT.RIGHT);
		wlInclCreated.setText(BaseMessages.getString(PKG, "AutoDocDialog.InclCreated.Label")); //$NON-NLS-1$
 		props.setLook(wlInclCreated);
		FormData fdlInclCreated = new FormData();
		fdlInclCreated.left = new FormAttachment(0, 0);
		fdlInclCreated.right= new FormAttachment(middle, -margin);
		fdlInclCreated.top  = new FormAttachment(lastControl, margin);
		wlInclCreated.setLayoutData(fdlInclCreated);
		wInclCreated=new Button(shell, SWT.CHECK | SWT.LEFT);
 		props.setLook(wInclCreated);
		FormData fdInclCreated = new FormData();
		fdInclCreated.left = new FormAttachment(middle, 0);
		fdInclCreated.right= new FormAttachment(100, 0);
		fdInclCreated.top  = new FormAttachment(lastControl, margin);
		wInclCreated.setLayoutData(fdInclCreated);
		lastControl = wInclCreated;

		// Include modified information (user/date) check-box
		//
		Label wlInclModified = new Label(shell, SWT.RIGHT);
		wlInclModified.setText(BaseMessages.getString(PKG, "AutoDocDialog.InclModified.Label")); //$NON-NLS-1$
 		props.setLook(wlInclModified);
		FormData fdlInclModified = new FormData();
		fdlInclModified.left = new FormAttachment(0, 0);
		fdlInclModified.right= new FormAttachment(middle, -margin);
		fdlInclModified.top  = new FormAttachment(lastControl, margin);
		wlInclModified.setLayoutData(fdlInclModified);
		wInclModified=new Button(shell, SWT.CHECK | SWT.LEFT);
 		props.setLook(wInclModified);
		FormData fdInclModified = new FormData();
		fdInclModified.left = new FormAttachment(middle, 0);
		fdInclModified.right= new FormAttachment(100, 0);
		fdInclModified.top  = new FormAttachment(lastControl, margin);
		wInclModified.setLayoutData(fdInclModified);
		lastControl = wInclModified;

		// Include image check-box
		//
		Label wlInclImage = new Label(shell, SWT.RIGHT);
		wlInclImage.setText(BaseMessages.getString(PKG, "AutoDocDialog.InclImage.Label")); //$NON-NLS-1$
 		props.setLook(wlInclImage);
		FormData fdlInclImage = new FormData();
		fdlInclImage.left = new FormAttachment(0, 0);
		fdlInclImage.right= new FormAttachment(middle, -margin);
		fdlInclImage.top  = new FormAttachment(lastControl, margin);
		wlInclImage.setLayoutData(fdlInclImage);
		wInclImage=new Button(shell, SWT.CHECK | SWT.LEFT);
 		props.setLook(wInclImage);
		FormData fdInclImage = new FormData();
		fdInclImage.left = new FormAttachment(middle, 0);
		fdInclImage.right= new FormAttachment(100, 0);
		fdInclImage.top  = new FormAttachment(lastControl, margin);
		wInclImage.setLayoutData(fdInclImage);
		lastControl = wInclImage;

		// Include logging check-box
		//
		Label wlInclLogging = new Label(shell, SWT.RIGHT);
		wlInclLogging.setText(BaseMessages.getString(PKG, "AutoDocDialog.InclLogging.Label")); //$NON-NLS-1$
 		props.setLook(wlInclLogging);
		FormData fdlInclLogging = new FormData();
		fdlInclLogging.left = new FormAttachment(0, 0);
		fdlInclLogging.right= new FormAttachment(middle, -margin);
		fdlInclLogging.top  = new FormAttachment(lastControl, margin);
		wlInclLogging.setLayoutData(fdlInclLogging);
		wInclLogging=new Button(shell, SWT.CHECK | SWT.LEFT);
 		props.setLook(wInclLogging);
		FormData fdInclLogging = new FormData();
		fdInclLogging.left = new FormAttachment(middle, 0);
		fdInclLogging.right= new FormAttachment(100, 0);
		fdInclLogging.top  = new FormAttachment(lastControl, margin);
		wInclLogging.setLayoutData(fdInclLogging);
		lastControl = wInclLogging;

		// Include last execution date check-box
		//
		Label wlInclLastExecResult = new Label(shell, SWT.RIGHT);
		wlInclLastExecResult.setText(BaseMessages.getString(PKG, "AutoDocDialog.InclLastExecResult.Label")); //$NON-NLS-1$
 		props.setLook(wlInclLastExecResult);
		FormData fdlInclLastExecResult = new FormData();
		fdlInclLastExecResult.left = new FormAttachment(0, 0);
		fdlInclLastExecResult.right= new FormAttachment(middle, -margin);
		fdlInclLastExecResult.top  = new FormAttachment(lastControl, margin);
		wlInclLastExecResult.setLayoutData(fdlInclLastExecResult);
		wInclLastExecResult=new Button(shell, SWT.CHECK | SWT.LEFT);
 		props.setLook(wInclLastExecResult);
		FormData fdInclLastExecResult = new FormData();
		fdInclLastExecResult.left = new FormAttachment(middle, 0);
		fdInclLastExecResult.right= new FormAttachment(100, 0);
		fdInclLastExecResult.top  = new FormAttachment(lastControl, margin);
		wInclLastExecResult.setLayoutData(fdInclLastExecResult);
		lastControl = wInclLastExecResult;
		
		// Some buttons first, so that the dialog scales nicely...
		//
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel, }, margin, lastControl);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

		wCancel.addListener (SWT.Selection, lsCancel );
		wOK.addListener     (SWT.Selection, lsOK     );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wFilenameField.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		inputMeta.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	public void setFlags() {
	  AutoDocMeta check = new AutoDocMeta();
	  getInfo(check);
	  
	  boolean enableTarget = check.getOutputType()!=OutputType.METADATA;
	  wTargetFilename.setEnabled(enableTarget);
	}
	
	public void getData()
	{
		getData(inputMeta);
		setFlags();
	}
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData(AutoDocMeta inputMeta)
	{
		wFilenameField.setText(Const.NVL(inputMeta.getFilenameField(), ""));
		wFileTypeField.setText(Const.NVL(inputMeta.getFileTypeField(), ""));
    wTargetFilename.setText(Const.NVL(inputMeta.getTargetFilename(), ""));
		wOutputType.setText(inputMeta.getOutputType().name());
		
		wInclName.setSelection(inputMeta.isIncludingName());
		wInclDesc.setSelection(inputMeta.isIncludingDescription());
		wInclExtDesc.setSelection(inputMeta.isIncludingExtendedDescription());
		wInclCreated.setSelection(inputMeta.isIncludingCreated());
		wInclModified.setSelection(inputMeta.isIncludingModified());
		wInclImage.setSelection(inputMeta.isIncludingImage());
		wInclLogging.setSelection(inputMeta.isIncludingLoggingConfiguration());
		wInclLastExecResult.setSelection(inputMeta.isIncludingLastExecutionResult());

		wStepname.setText(stepname);
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		inputMeta.setChanged(changed);
		dispose();
	}
	
	private void getInfo(AutoDocMeta inputMeta) {
		
		inputMeta.setFilenameField(wFilenameField.getText());
    inputMeta.setFileTypeField(wFileTypeField.getText());
		inputMeta.setTargetFilename(wTargetFilename.getText());
		try {
			inputMeta.setOutputType( KettleReportBuilder.OutputType.valueOf( wOutputType.getText()) );
		} catch(Exception e) {
			inputMeta.setOutputType( KettleReportBuilder.OutputType.PDF );
		}

		inputMeta.setIncludingName(wInclName.getSelection());
		inputMeta.setIncludingDescription(wInclDesc.getSelection());
		inputMeta.setIncludingExtendedDescription(wInclExtDesc.getSelection());
		inputMeta.setIncludingCreated(wInclCreated.getSelection());
		inputMeta.setIncludingModified(wInclModified.getSelection());
		inputMeta.setIncludingImage(wInclImage.getSelection());
		inputMeta.setIncludingLoggingConfiguration(wInclLogging.getSelection());
		inputMeta.setIncludingLastExecutionResult(wInclLastExecResult.getSelection());

		inputMeta.setChanged();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		getInfo(inputMeta);
		stepname = wStepname.getText();
		dispose();
	}	
}
