/*
 *   This software is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This software is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this software.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *   Copyright 2011 De Bortoli Wines Pty Limited (Australia)
 */

package org.pentaho.di.ui.trans.steps.openerp.objectdelete;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.openerp.objectdelete.OpenERPObjectDeleteData;
import org.pentaho.di.trans.steps.openerp.objectdelete.OpenERPObjectDeleteMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class OpenERPObjectDeleteDialog extends BaseStepDialog implements StepDialogInterface {

	private static Class<?> PKG = OpenERPObjectDeleteMeta.class; // for i18n purposes, needed by Translator2!! // $NON-NLS-1$

	private final OpenERPObjectDeleteMeta meta;
	private Label                  labelStepName;
	private Text                   textStepName;
	private CCombo                 addConnectionLine;
	private Label                  labelModelName;
	private CCombo                 comboModelName;
	private Label                  labelCommitBatchSize;
	private Text                   textCommitBatchSize;
	private Label                  labelIDFieldName;
	private CCombo                 comboIDFieldName;
	private Button                 buttonOk;
	private Button                 buttonCancel;

	public OpenERPObjectDeleteDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		this.meta = (OpenERPObjectDeleteMeta) in;
	}

	@Override
	public String open() {

		final Display display = getParent().getDisplay();
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
		props.setLook(shell);
		setShellImage(shell, meta);
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		FormData fd;

		labelStepName = new Label(shell, SWT.RIGHT);
		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(middle, -margin);
		fd.top = new FormAttachment(0, margin);
		labelStepName.setLayoutData(fd);

		textStepName = new Text(shell, SWT.BORDER);
		fd = new FormData();
		fd.left = new FormAttachment(middle, 0);
		fd.right = new FormAttachment(100, 0);
		fd.top = new FormAttachment(0, margin);
		textStepName.setLayoutData(fd);

		addConnectionLine = addConnectionLine(shell, textStepName, Const.MIDDLE_PCT, margin);

		labelModelName = new Label(shell, SWT.RIGHT);
		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(middle, -margin);
		fd.top = new FormAttachment(addConnectionLine, margin);
		labelModelName.setLayoutData(fd);

		comboModelName = new CCombo(shell, SWT.BORDER);
		fd = new FormData();
		fd.left = new FormAttachment(middle, 0);
		fd.right = new FormAttachment(100, 0);
		fd.top = new FormAttachment(addConnectionLine, margin);
		comboModelName.setLayoutData(fd);

		labelCommitBatchSize = new Label(shell, SWT.RIGHT);
		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(middle, -margin);
		fd.top = new FormAttachment(comboModelName, margin);
		labelCommitBatchSize.setLayoutData(fd);

		textCommitBatchSize = new Text(shell, SWT.BORDER);
		fd = new FormData();
		fd.left = new FormAttachment(middle, 0);
		fd.right = new FormAttachment(100, 0);
		fd.top = new FormAttachment(comboModelName, margin);
		textCommitBatchSize.setLayoutData(fd);

		labelIDFieldName = new Label(shell, SWT.RIGHT);
		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(middle, -margin);
		fd.top = new FormAttachment(textCommitBatchSize, margin);
		labelIDFieldName.setLayoutData(fd);

		comboIDFieldName = new CCombo(shell, SWT.BORDER);
		fd = new FormData();
		fd.left = new FormAttachment(middle, 0);
		fd.right = new FormAttachment(100, 0);
		fd.top = new FormAttachment(textCommitBatchSize, margin);
		comboIDFieldName.setLayoutData(fd);
	    
		buttonOk = new Button(shell, SWT.CENTER);
		buttonCancel = new Button(shell, SWT.CENTER);
		buttonOk.setText(BaseMessages.getString("System.Button.OK"));
		buttonCancel.setText(BaseMessages.getString("System.Button.Cancel"));
		setButtonPositions(new Button[] { buttonOk, buttonCancel }, margin, null);

		addConnectionLine.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setModelComboOptions();
			}
		});

		buttonCancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				cancel();
			}
		});
		buttonOk.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ok();
			}
		});

		// 
		// Search the fields in the background
		//
		final Runnable runnable = new Runnable()
		{
			public void run()
			{
				setModelComboOptions();
				setIDComboOptions();
			}
		};
		display.asyncExec(runnable);

		this.fillLocalizationData();
		this.fillStoredData();

		props.setLook(labelStepName);
		props.setLook(textStepName);
		props.setLook(addConnectionLine);
		props.setLook(labelModelName);
		props.setLook(comboModelName);
		props.setLook(labelCommitBatchSize);
		props.setLook(textCommitBatchSize);
		props.setLook(labelIDFieldName);
		props.setLook(comboIDFieldName);

		meta.setChanged(changed);
		setSize();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return stepname;
	}

	private void fillLocalizationData() {
		shell.setText(BaseMessages.getString(PKG, "OpenERPObjectDeleteDialog.Title"));
		labelStepName.setText(BaseMessages.getString(PKG, "OpenERPObjectDeleteDialog.StepName"));
		labelModelName.setText(BaseMessages.getString(PKG, "OpenERPObjectDeleteDialog.ModelName"));
		labelCommitBatchSize.setText(BaseMessages.getString(PKG, "OpenERPObjectDeleteDialog.CommitBatchSize"));
		labelIDFieldName.setText(BaseMessages.getString(PKG, "OpenERPObjectDeleteDialog.IDFieldName"));
	}
	
	private void setModelComboOptions(){
		String [] objectList = getModelList();
		if (objectList == null)
			return;
		
		for(String objectName : objectList){
			if (comboModelName.indexOf(objectName) == -1)
				comboModelName.add(objectName);
		}
	}
	
	private String [] getModelList(){
		String [] objectList = null;
		// Fill object name
		if (addConnectionLine.getText() != null) {
			DatabaseMeta dbMeta = transMeta.findDatabase(addConnectionLine.getText());

			if (dbMeta != null) {

				OpenERPObjectDeleteData data = null;
				try{
					data = new OpenERPObjectDeleteData(dbMeta);
					data.helper.StartSession();
					objectList = data.helper.getModelList();
				}
				catch (Exception e){
					return null;
				}
			}
		}
		return objectList;
	}
	
	private String[] getSteamFieldsNames(boolean showError){
		String [] fields = null;

		// Set stream fields
		RowMetaInterface row;
		try {
			row = transMeta.getPrevStepFields(stepMeta);
			fields = new String[row.size()];
			for (int i=0;i<row.size();i++)
				fields[i] = row.getValueMeta(i).getName();
		} catch (KettleStepException e) {
			if (showError)
				new ErrorDialog(shell, BaseMessages.getString(PKG, "OpenERPObjectOutputDialog.UnableToFindStreamFieldsTitle"), BaseMessages.getString(PKG, "OpenERPObjectOutputDialog.UnableToFindStreamFieldsMessage"), e);
			return null;
		}

		return fields;
	}
	
	private void setIDComboOptions() {
		String [] steamFields = getSteamFieldsNames(false);

		if (steamFields != null)
			for (String streamField : steamFields)
					comboIDFieldName.add(streamField);
		
	}

	private void fillStoredData() {

		if (stepname != null)
			textStepName.setText(stepname);

		int index = addConnectionLine.indexOf(meta.getDatabaseMeta() != null ? meta.getDatabaseMeta().getName() : "");
		if (index >= 0)
			addConnectionLine.select(index);

		if (meta.getModelName() != null){
			comboModelName.add(meta.getModelName());
			comboModelName.select(0);
		}

		textCommitBatchSize.setText(String.valueOf(meta.getCommitBatchSize()));
		
		comboIDFieldName.setText(meta.getIdFieldName());
	}

	private void cancel() {
		stepname = null;
		meta.setChanged(changed);
		dispose();
	}

	private void ok() {
		if (SaveToMeta(meta))
			dispose();
	}

	private boolean SaveToMeta(OpenERPObjectDeleteMeta targetMeta) {
		stepname = textStepName.getText();

		DatabaseMeta dbMeta = transMeta.findDatabase(addConnectionLine.getText());
		if (dbMeta != null) {
			try {
				new OpenERPObjectDeleteData(dbMeta);
			} catch (KettleException e) {
				new ErrorDialog(shell, BaseMessages.getString(PKG, "OpenERPObjectDeleteDialog.ConnectionTypeErrorTitle"), BaseMessages.getString(PKG, "OpenERPObjectDeleteDialog.ConnectionTypeErrorString"), e);
				return false;
			}
		}

		int commitBatchSize = 0;
		try{
			commitBatchSize = Integer.parseInt(textCommitBatchSize.getText());
		}
		catch (NumberFormatException e){
			new ErrorDialog(shell, BaseMessages.getString(PKG, "OpenERPObjectDeleteDialog.ParseErrorTitle"), BaseMessages.getString(PKG, "OpenERPObjectDeleteDialog.ParseErrorString", textCommitBatchSize.getText()), e);
			return false;
		}

		targetMeta.setIdFieldName(comboIDFieldName.getText());
		targetMeta.setDatabaseMeta(transMeta.findDatabase(addConnectionLine.getText()));
		targetMeta.setModelName(comboModelName.getText());
		targetMeta.setCommitBatchSize(commitBatchSize);
		targetMeta.setChanged(true);

		return true;

	}
}

