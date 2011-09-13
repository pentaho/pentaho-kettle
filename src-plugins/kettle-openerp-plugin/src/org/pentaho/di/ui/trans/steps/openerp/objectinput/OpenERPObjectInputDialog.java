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

package org.pentaho.di.ui.trans.steps.openerp.objectinput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.openerp.core.FieldMapping;
import org.pentaho.di.openerp.core.ReadFilter;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.openerp.objectinput.OpenERPObjectInputData;
import org.pentaho.di.trans.steps.openerp.objectinput.OpenERPObjectInputMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class OpenERPObjectInputDialog extends BaseStepDialog implements StepDialogInterface {

	private static Class<?> PKG = OpenERPObjectInputMeta.class; // for i18n purposes, needed by Translator2!! // $NON-NLS-1$
	private static Class<?> PKGStepInterface = StepInterface.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	ArrayList<FieldMapping> sourceListMapping;
	
	private ColumnInfo[] filterViewColinf;
	
	private final OpenERPObjectInputMeta meta;
	private Label                  labelStepName;
	private Text                   textStepName;
	private CCombo                 addConnectionLine;
	private Label                  labelModelName;
	private CCombo                 comboModelName;
	private Label                  labelReadBatchSize;
	private Text                   textReadBatchSize;
	private Label                  labelFilter;
	private TableView              tableViewFilter;
	private Button                 buttonHelpFilter;
	private Label                  labelFields;
	private TableView              tableViewFields;
	private Button                 buttonGetFields;
	private Button                 buttonOk;
	private Button                 buttonCancel;
	private Button                 buttonPreview;

	public OpenERPObjectInputDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		this.meta = (OpenERPObjectInputMeta) in;
	}

	@Override
	public String open() {

		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				meta.setChanged();
			}
		};

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

		labelReadBatchSize = new Label(shell, SWT.RIGHT);
		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(middle, -margin);
		fd.top = new FormAttachment(comboModelName, margin);
		labelReadBatchSize.setLayoutData(fd);

		textReadBatchSize = new Text(shell, SWT.BORDER);
		fd = new FormData();
		fd.left = new FormAttachment(middle, 0);
		fd.right = new FormAttachment(100, 0);
		fd.top = new FormAttachment(comboModelName, margin);
		textReadBatchSize.setLayoutData(fd);
		
		labelFilter = new Label(shell, SWT.NONE);
		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.top = new FormAttachment(textReadBatchSize, margin);
		labelFilter.setLayoutData(fd);
		
		filterViewColinf = new ColumnInfo[] { 
				new ColumnInfo(getLocalizedFilterColumn(0), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false), 
				new ColumnInfo(getLocalizedFilterColumn(1), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
				new ColumnInfo(getLocalizedFilterColumn(2), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false),
				new ColumnInfo(getLocalizedFilterColumn(3), ColumnInfo.COLUMN_TYPE_TEXT, false, false)};
		
		tableViewFilter = new TableView(null, shell, SWT.MULTI | SWT.BORDER, filterViewColinf, 0, true, lsMod, props);
		tableViewFilter.setReadonly(false);
		tableViewFilter.setSortable(false);
		fd = new FormData();
		fd.left = new FormAttachment(0, margin);
		fd.top = new FormAttachment(labelFilter, 3 * margin);
		fd.right = new FormAttachment(100, -150);
		fd.bottom = new FormAttachment(labelFilter, 200);
		tableViewFilter.setLayoutData(fd);
		
		buttonHelpFilter = new Button(shell, SWT.NONE);
		fd = new FormData();
		fd.left = new FormAttachment(tableViewFilter, margin);
		fd.top = new FormAttachment(labelFilter, 3 * margin);
		fd.right = new FormAttachment(100, 0);
		buttonHelpFilter.setLayoutData(fd);

		labelFields = new Label(shell, SWT.NONE);
		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.top = new FormAttachment(tableViewFilter, margin);
		labelFields.setLayoutData(fd);
		
		ColumnInfo[] colinf = new ColumnInfo[] { 
				new ColumnInfo(getLocalizedColumn(0), ColumnInfo.COLUMN_TYPE_TEXT, false, true), 
				new ColumnInfo(getLocalizedColumn(1), ColumnInfo.COLUMN_TYPE_TEXT, false, true),
				new ColumnInfo(getLocalizedColumn(2), ColumnInfo.COLUMN_TYPE_TEXT, false, true)};

		tableViewFields = new TableView(null, shell, SWT.FILL | SWT.BORDER, colinf, 0, true, lsMod, props);
		tableViewFields.setSize(477, 280);
		tableViewFields.setBounds(5, 125, 477, 280);
		tableViewFields.setReadonly(false);
		tableViewFields.setSortable(true);
		fd = new FormData();
		fd.left = new FormAttachment(0, margin);
		fd.top = new FormAttachment(labelFields, 3 * margin);
		fd.right = new FormAttachment(100, -150);
		fd.bottom = new FormAttachment(100, -50);
		tableViewFields.setLayoutData(fd);
		
		buttonGetFields = new Button(shell, SWT.NONE);
		fd = new FormData();
		fd.left = new FormAttachment(tableViewFields, margin);
		fd.top = new FormAttachment(labelFields, 3 * margin);
		fd.right = new FormAttachment(100, 0);
		buttonGetFields.setLayoutData(fd);
		
		buttonOk = new Button(shell, SWT.CENTER);
		buttonCancel = new Button(shell, SWT.CENTER);
		buttonPreview = new Button(shell, SWT.CENTER);
		buttonOk.setText(BaseMessages.getString("System.Button.OK"));
		buttonPreview.setText(BaseMessages.getString("System.Button.Preview"));
		buttonCancel.setText(BaseMessages.getString("System.Button.Cancel"));
		setButtonPositions(new Button[] { buttonOk, buttonPreview, buttonCancel }, margin, null);

		addConnectionLine.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fillModelCombo();
			}
		});
		comboModelName.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fillFilterCombos();
			}
		});
		comboModelName.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent arg0) {
				fillFilterCombos();
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});
		
		comboModelName.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fillFilterCombos();
			}
		});
		buttonHelpFilter.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showHelp();
			}
		});
		buttonGetFields.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getFields();
			}
		});
		buttonCancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				cancel();
			}
		});
		buttonPreview.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				preview();
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
				fillModelCombo();
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
		props.setLook(labelReadBatchSize);
		props.setLook(textReadBatchSize);
		props.setLook(labelFilter);
		props.setLook(tableViewFilter);
		props.setLook(labelFields);
		props.setLook(tableViewFields);

		meta.setChanged(changed);
		setSize();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return stepname;
	}

	private String getLocalizedColumn(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return BaseMessages.getString(PKG, "OpenERPObjectInputDialog.TableViewLabel");
		case 1:
			return BaseMessages.getString(PKG, "OpenERPObjectInputDialog.TableViewModelName");
		case 2:
			return BaseMessages.getString(PKG, "OpenERPObjectInputDialog.TableViewFieldName");  
		default:
			return "";
		}
	}
	
	private String getLocalizedFilterColumn(int columnIndex) {
		switch (columnIndex) {
		
		case 0:
			return BaseMessages.getString(PKG, "OpenERPObjectInputDialog.TableViewFilterOperator");
		case 1:
			return BaseMessages.getString(PKG, "OpenERPObjectInputDialog.TableViewFilterField");
		case 2:
			return BaseMessages.getString(PKG, "OpenERPObjectInputDialog.TableViewFilterComparator");
		case 3:
			return BaseMessages.getString(PKG, "OpenERPObjectInputDialog.TableViewFilterValue");  
		default:
			return "";
		}
	}

	private void showHelp(){
		EnterTextDialog text = new EnterTextDialog(shell, 
				BaseMessages.getString(PKG,"OpenERPObjectInputDialog.FilterHelp.Title"),"", 
				BaseMessages.getString(PKG,"OpenERPObjectInputDialog.FilterHelp.Text"));
		text.setReadOnly();
		text.open();
	}
	private void getFields(){
		ArrayList<FieldMapping> mappings = getFieldMappings(false);
		
		if (mappings != null)
			populateFielsTable(mappings);
		else {
			// See if the model exists in the database
			String [] modelList = getModelList();
			
			// Server connect problem
			if (modelList == null){
				getFieldMappings(true);
				return;
			}
			
			boolean found = false;
			for (String model : modelList)
				if (model.equals(comboModelName.getText())){
					found = true;
					break;
				}
			
			if (!found){
				new ErrorDialog(shell, BaseMessages.getString(PKG, "OpenERPObjectInputDialog.ConnectionErrorTitle"), 
						BaseMessages.getString(PKG, "OpenERPObjectInputDialog.ConnectionErrorString"), 
						new Exception(BaseMessages.getString(PKG, "OpenERPObjectInputDialog.ModelNotFoundError", comboModelName.getText())));
				return;
			}
		}
	}

	private void populateFielsTable(ArrayList<FieldMapping> mappings){
		
		int choice = 0;
		
		if (tableViewFields.table.getItemCount() > 0) {
			// Ask what we should do with the existing data in the step.
			MessageDialog md = new MessageDialog(tableViewFields.getShell(), BaseMessages.getString(PKGStepInterface, "BaseStepDialog.GetFieldsChoice.Title"),//"Warning!"  //$NON-NLS-1$
					null, BaseMessages.getString(PKGStepInterface, "BaseStepDialog.GetFieldsChoice.Message", "" + tableViewFields.table.getItemCount(), "" + mappings.size()), //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
					MessageDialog.WARNING, new String[] { BaseMessages.getString(PKGStepInterface, "BaseStepDialog.AddNew"), //$NON-NLS-1$
				BaseMessages.getString(PKGStepInterface, "BaseStepDialog.ClearAndAdd"), //$NON-NLS-1$  //$NON-NLS-2$
				BaseMessages.getString(PKGStepInterface, "BaseStepDialog.Cancel"), }, 0); //$NON-NLS-1$
			MessageDialog.setDefaultImage(GUIResource.getInstance().getImageSpoon());
			int idx = md.open();
			choice = idx & 0xFF;
		}

		if (choice == 2 || choice == 255 /* 255 = escape pressed */)
			return; // Cancel clicked

		if (choice == 1)
			tableViewFields.table.removeAll();
		
		// Make a list of the old elements
		Hashtable<String, Object> currentMaps = new Hashtable<String, Object>();
		for (int i = 0; i < tableViewFields.table.getItemCount(); i++)
			currentMaps.put(tableViewFields.table.getItem(i).getText(1)
					+ tableViewFields.table.getItem(i).getText(2)
					+ tableViewFields.table.getItem(i).getText(3), true);

   	    sourceListMapping = mappings;
		for (FieldMapping map : mappings)
			// Only add new elements
			if (!currentMaps.containsKey(map.target_field_label
					+ map.target_model
					+ map.target_field))
				tableViewFields.add(
						map.target_field_label, 
						map.target_model,
						map.target_field, 
						map.source_model,
						map.source_field,
						String.valueOf(map.source_index),
						String.valueOf(map.target_field_type));
		
		tableViewFields.setRowNums();
		tableViewFields.optWidth(true);		
	}
	
	private void populateFiltersTable(ArrayList<ReadFilter> filters){
		tableViewFilter.table.removeAll();

		for (ReadFilter filter : filters)
			tableViewFilter.add(
					filter.getOperator(),
					filter.getFieldName(), 
					filter.getComparator(),
					filter.getValue());

		tableViewFilter.add("","","","");

		tableViewFilter.setRowNums();
		tableViewFilter.optWidth(true);		
	}

	private void fillModelCombo(){

		String [] modelList = getModelList();
		
		if (modelList != null)
			for(String modelName : modelList){
				if (comboModelName.indexOf(modelName) == -1)
					comboModelName.add(modelName);
			}
	}
	
	private String [] getModelList(){
		String [] modelList = null;
		
		if (addConnectionLine.getText() != null) {
			DatabaseMeta dbMeta = transMeta.findDatabase(addConnectionLine.getText());

			if (dbMeta != null) {

				OpenERPObjectInputData data = null;
				try{
					data = new OpenERPObjectInputData(dbMeta);
					data.helper.StartSession();
					modelList = data.helper.getModelList();
				}
				catch (Exception e){
					return null;
				}
			}
		}
		return modelList; 
	}
	
	private void fillFilterCombos(ArrayList<FieldMapping> mappings){
		ArrayList<String> fieldList = new ArrayList<String>();
		for(FieldMapping map : mappings)
			if (!fieldList.contains(map.source_field))
				fieldList.add(map.source_field);

		String [] fieldStringList = new String[fieldList.size()];
		fieldStringList = fieldList.toArray(fieldStringList);
		Arrays.sort(fieldStringList,String.CASE_INSENSITIVE_ORDER);
		
		filterViewColinf[0].setComboValues(new String [] {"", "NOT", "OR"});
		filterViewColinf[1].setComboValues(fieldStringList);
		filterViewColinf[2].setComboValues(new String [] {"=", "!=", ">", ">=", "<", "<=", "like", "ilike", "is null", "is not null", "in", "not in", "child_of", "parent_left", "parent_right"});
		tableViewFilter.optWidth(true);
	}
	
	private void fillFilterCombos(){
		ArrayList<FieldMapping> mappings = getFieldMappings(false);
		if (mappings != null)
			fillFilterCombos(mappings);
	}

	private ArrayList<FieldMapping> getFieldMappings(boolean showError){
		if (addConnectionLine.getText() != null) {

			DatabaseMeta dbMeta = transMeta.findDatabase(addConnectionLine.getText());

			if (dbMeta != null) {
				try {
					OpenERPObjectInputData data = new OpenERPObjectInputData(dbMeta);
					data.helper.StartSession();
					ArrayList<FieldMapping> mappings = data.helper.getDefaultFieldMappings(comboModelName.getText());
					return mappings;
				} catch (Exception e) {
					if (showError)
						new ErrorDialog(shell, BaseMessages.getString(PKG, "OpenERPObjectInputDialog.ConnectionErrorTitle"), BaseMessages.getString(PKG, "OpenERPObjectInputDialog.ConnectionErrorString"), e);
					return null;
				}
			}
		}
		return null;
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

		textReadBatchSize.setText(String.valueOf(meta.getReadBatchSize()));
		tableViewFields.table.removeAll();
		tableViewFilter.table.removeAll();

		populateFielsTable(meta.getMappings());
		populateFiltersTable(meta.getFilterList());
		fillFilterCombos();

	}

	private void fillLocalizationData() {
		shell.setText(BaseMessages.getString(PKG, "OpenERPObjectInputDialog.Title"));
		labelStepName.setText(BaseMessages.getString(PKG, "OpenERPObjectInputDialog.StepName"));
		labelModelName.setText(BaseMessages.getString(PKG, "OpenERPObjectInputDialog.ModelName"));
		labelReadBatchSize.setText(BaseMessages.getString(PKG, "OpenERPObjectInputDialog.ReadBatchSize"));
		labelFilter.setText(BaseMessages.getString(PKG, "OpenERPObjectInputDialog.LabelFilterSpecify"));
		labelFields.setText(BaseMessages.getString(PKG, "OpenERPObjectInputDialog.LabelSpecifyFields"));
		buttonHelpFilter.setText(BaseMessages.getString(PKG, "OpenERPObjectInputDialog.ButtonFilterHelp"));
		buttonGetFields.setText(BaseMessages.getString(PKG, "OpenERPObjectInputDialog.ButtonGetFields"));
	}

	private void cancel() {
		stepname = null;
		meta.setChanged(changed);
		dispose();
	}

	private void ok() {
		if (SaveToMeta(meta));
		  dispose();
	}

	private boolean SaveToMeta(OpenERPObjectInputMeta targetMeta) {
		stepname = textStepName.getText();

		DatabaseMeta dbMeta = transMeta.findDatabase(addConnectionLine.getText());
		if (dbMeta != null) {
			try {
				new OpenERPObjectInputData(dbMeta);
			} catch (KettleException e) {
				new ErrorDialog(shell, BaseMessages.getString(PKG, "OpenERPObjectInputDialog.ConnectionTypeErrorTitle"), BaseMessages.getString(PKG, "OpenERPObjectInputDialog.ConnectionTypeErrorString"), e);
				return false;
			}
		}

		int readBatchSize = 0;
		try{
			readBatchSize = Integer.parseInt(textReadBatchSize.getText());
		}
		catch (NumberFormatException e){
			new ErrorDialog(shell, BaseMessages.getString(PKG, "OpenERPObjectInputDialog.ParseErrorTitle"), BaseMessages.getString(PKG, "OpenERPObjectInputDialog.ParseErrorString", textReadBatchSize.getText()), e);
			return false;
		}


		ArrayList<FieldMapping> mappings = new ArrayList<FieldMapping>();
		for (int i = 0; i < tableViewFields.table.getItemCount(); i++) {
			
			FieldMapping map = null;
			
			for (FieldMapping sourceMap : sourceListMapping){
				if (sourceMap.target_field_label.equals(tableViewFields.table.getItem(i).getText(1))
						&&sourceMap.target_model.equals(tableViewFields.table.getItem(i).getText(2))
						&& sourceMap.target_field.equals(tableViewFields.table.getItem(i).getText(3)))
					map = sourceMap.Clone();
			}
			
			if (map == null){
				new ErrorDialog(shell, BaseMessages.getString(PKG, "OpenERPObjectInputDialog.MappingErrorTitle"), BaseMessages.getString(PKG, "OpenERPObjectInputDialog.MappingErrorTitle"), new Exception(BaseMessages.getString(PKG, "OpenERPObjectInputDialog.MappingErrorString", tableViewFields.table.getItem(i).getText(2))));
				return false;
			}

			mappings.add(map);

		}
		
		ArrayList<ReadFilter> filters = new ArrayList<ReadFilter>();
		for (int i = 0; i < tableViewFilter.table.getItemCount(); i++) {
			
			ReadFilter filter = new ReadFilter();
			filter.setOperator(tableViewFilter.table.getItem(i).getText(1));
			filter.setFieldName(tableViewFilter.table.getItem(i).getText(2));
			filter.setComparator(tableViewFilter.table.getItem(i).getText(3));
			filter.setValue(tableViewFilter.table.getItem(i).getText(4));
			
			if (filter.getFieldName() != "")
				filters.add(filter);
		}

		targetMeta.setDatabaseMeta(transMeta.findDatabase(addConnectionLine.getText()));
		targetMeta.setModelName(comboModelName.getText());
		targetMeta.setReadBatchSize(readBatchSize);
		targetMeta.setMappings(mappings);
		targetMeta.setFilterList(filters);
		targetMeta.setChanged(true);

		return true;

	}

	private void preview(){
		OpenERPObjectInputMeta testMeta = new OpenERPObjectInputMeta();
		if (!SaveToMeta(testMeta))
			return;

		TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, testMeta, textStepName.getText());

		EnterNumberDialog numberDialog = new EnterNumberDialog(shell, 500, BaseMessages.getString("System.Dialog.EnterPreviewSize.Title"), BaseMessages.getString("System.Dialog.EnterPreviewSize.Message"));
		int previewSize = numberDialog.open();
		if (previewSize > 0) {
			TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { textStepName.getText() }, new int[] { previewSize });
			progressDialog.open();

			Trans trans = progressDialog.getTrans();
			String loggingText = progressDialog.getLoggingText();

			if (!progressDialog.isCancelled()) {
				if (trans.getResult() != null && trans.getResult().getNrErrors() > 0) {
					EnterTextDialog etd = new EnterTextDialog(shell, BaseMessages.getString("System.Dialog.PreviewError.Title"), BaseMessages.getString("System.Dialog.PreviewError.Message"), loggingText, true);
					etd.setReadOnly();
					etd.open();
				}
			}

			PreviewRowsDialog prd = new PreviewRowsDialog(shell, transMeta, SWT.NONE, textStepName.getText(), progressDialog.getPreviewRowsMeta(textStepName.getText()), progressDialog.getPreviewRows(textStepName.getText()), loggingText);
			prd.open();
		}
	}
}

