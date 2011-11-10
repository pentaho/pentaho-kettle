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
package org.pentaho.di.ui.trans.steps.palo.diminput;

/*
 *   This file is part of PaloKettlePlugin.
 *
 *   PaloKettlePlugin is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   PaloKettlePlugin is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with PaloKettlePlugin.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *   Copyright 2008 Stratebi Business Solutions, S.L.
 *   Copyright 2011 De Bortoli Wines Pty Limited (Australia)
 */

import java.util.ArrayList;
import java.util.Collections;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.palo.core.PaloDimensionLevel;
import org.pentaho.di.palo.core.PaloNameComparator;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.palo.diminput.PaloDimInputData;
import org.pentaho.di.trans.steps.palo.diminput.PaloDimInputMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.steps.palo.cellinput.PaloCellInputDialog;

public class PaloDimInputDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?>        PKG = PaloDimInputMeta.class; // for i18n
                                                               // purposes,
                                                               // needed by
                                                               // Translator2!!
                                                               // $NON-NLS-1$

  private final PaloDimInputMeta meta;
  private TableView              tableViewFields;
  private Combo                  comboDimension;
  private Text                   textStepName;
  private Button                 buttonClearLevels;
  private Button                 buttonGetLevels;
  private Label                  labelStepName;
  private Label                  labelDimension;
  private Button                 buttonOk;
  private Button                 buttonCancel;
  private Button                 buttonPreview;
  private CCombo                 addConnectionLine;
  private Label                  labelBaseElementsOnly;
  private Button                 buttonBaseElementsOnly;

  public PaloDimInputDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
    super(parent, (BaseStepMeta) in, transMeta, sname);
    this.meta = (PaloDimInputMeta) in;
  }

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

    labelDimension = new Label(shell, SWT.RIGHT);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    fd.top = new FormAttachment(addConnectionLine, margin);
    labelDimension.setLayoutData(fd);

    comboDimension = new Combo(shell, SWT.READ_ONLY);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(addConnectionLine, margin);
    comboDimension.setLayoutData(fd);
    
    labelBaseElementsOnly = new Label(shell, SWT.RIGHT);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    fd.top = new FormAttachment(comboDimension, margin);
    labelBaseElementsOnly.setLayoutData(fd);

    buttonBaseElementsOnly = new Button(shell, SWT.CHECK);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(comboDimension, margin);
    buttonBaseElementsOnly.setLayoutData(fd);

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        meta.setChanged();
      }
    };
    ColumnInfo[] colinf = new ColumnInfo[] { new ColumnInfo(getLocalizedColumn(0), ColumnInfo.COLUMN_TYPE_TEXT, false, true), new ColumnInfo(getLocalizedColumn(1), ColumnInfo.COLUMN_TYPE_TEXT, false, true),
        new ColumnInfo(getLocalizedColumn(2), ColumnInfo.COLUMN_TYPE_TEXT, false, false), new ColumnInfo(getLocalizedColumn(3), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "String", "Number" }, true) };

    tableViewFields = new TableView(null, shell, SWT.FILL | SWT.BORDER, colinf, 10, true, lsMod, props);

    tableViewFields.setSize(477, 280);
    tableViewFields.setBounds(5, 125, 477, 280);
    tableViewFields.setReadonly(true);
    tableViewFields.setSortable(false);
    fd = new FormData();
    fd.left = new FormAttachment(0, margin);
    fd.top = new FormAttachment(buttonBaseElementsOnly, 3 * margin);
    fd.right = new FormAttachment(100, -150);
    fd.bottom = new FormAttachment(100, -50);
    tableViewFields.setLayoutData(fd);

    buttonGetLevels = new Button(shell, SWT.NONE);
    fd = new FormData();
    fd.left = new FormAttachment(tableViewFields, margin);
    fd.top = new FormAttachment(buttonBaseElementsOnly, 3 * margin);
    fd.right = new FormAttachment(100, 0);
    buttonGetLevels.setLayoutData(fd);

    buttonClearLevels = new Button(shell, SWT.NONE);
    fd = new FormData();
    fd.left = new FormAttachment(tableViewFields, margin);
    fd.top = new FormAttachment(buttonGetLevels, margin);
    fd.right = new FormAttachment(100, 0);
    buttonClearLevels.setLayoutData(fd);

    buttonOk = new Button(shell, SWT.CENTER);
    buttonCancel = new Button(shell, SWT.CENTER);
    buttonPreview = new Button(shell, SWT.CENTER);
    buttonOk.setText(BaseMessages.getString("System.Button.OK"));
    buttonPreview.setText(BaseMessages.getString("System.Button.Preview"));
    buttonCancel.setText(BaseMessages.getString("System.Button.Cancel"));
    setButtonPositions(new Button[] { buttonOk, buttonPreview, buttonCancel }, margin, null);

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
    buttonClearLevels.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        doClearLevels();
      }
    });
    buttonGetLevels.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        doGetLevels();
      }
    });
    comboDimension.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        doSelectDimension();
      }
    });
    addConnectionLine.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        doSelectConnection(false);
      }
    });

    this.fillLocalizationData();
    this.fillStoredData();
    this.doSelectConnection(false);

    props.setLook(tableViewFields);
    props.setLook(comboDimension);
    props.setLook(textStepName);
    props.setLook(buttonClearLevels);
    props.setLook(buttonGetLevels);
    props.setLook(labelStepName);
    props.setLook(labelDimension);
    props.setLook(buttonOk);
    props.setLook(buttonCancel);
    props.setLook(buttonPreview);
    props.setLook(addConnectionLine);
    props.setLook(labelBaseElementsOnly);
    props.setLook(buttonBaseElementsOnly);
    

    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        cancel();
      }
    });
    meta.setChanged(changed);
    setSize();
    shell.open();

    PaloCellInputDialog.showPaloLibWarningDialog(shell);

    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    return stepname;
  }

  private String getLocalizedColumn(int columnIndex) {
    switch (columnIndex) {
    case 0:
      return BaseMessages.getString(PKG, "PaloDimInputDialog.ColumnLevelName");
    case 1:
      return BaseMessages.getString(PKG, "PaloDimInputDialog.ColumnLevelNumber");
    case 2:
      return BaseMessages.getString(PKG, "PaloDimInputDialog.ColumnField");
    case 3:
      return BaseMessages.getString(PKG, "PaloDimInputDialog.ColumnType");
    default:
      return "";
    }
  }

  private void fillLocalizationData() {
    labelDimension.setText(BaseMessages.getString(PKG, "PaloDimInputDialog.SelectDimension"));
    labelStepName.setText(BaseMessages.getString(PKG, "PaloDimInputDialog.StepName"));
    shell.setText(BaseMessages.getString(PKG, "PaloDimInputDialog.PaloDimInput"));
    buttonGetLevels.setText(BaseMessages.getString(PKG, "PaloDimInputDialog.GetLevels"));
    buttonClearLevels.setText(BaseMessages.getString(PKG, "PaloDimInputDialog.ClearLevels"));
    labelBaseElementsOnly.setText(BaseMessages.getString(PKG, "PaloDimInputDialog.BaseElementsOnly"));
  }

  private void fillStoredData() {

    if (stepname != null)
      textStepName.setText(stepname);

    int index = addConnectionLine.indexOf(meta.getDatabaseMeta() != null ? meta.getDatabaseMeta().getName() : "");
    if (index >= 0)
      addConnectionLine.select(index);

    if (meta.getDimension() != null) {
      comboDimension.add(meta.getDimension());
      comboDimension.select(0);
    }
    
    buttonBaseElementsOnly.setSelection(meta.getBaseElementsOnly());
    
    tableViewFields.table.removeAll();
    if (meta.getLevels().size() > 0) {
      for (PaloDimensionLevel level : meta.getLevels()) {
        tableViewFields.add(level.getLevelName(), String.valueOf(level.getLevelNumber()), level.getFieldName(), level.getFieldType());
      }
      tableViewFields.setRowNums();
      tableViewFields.optWidth(true);
    }
  }

  private void doSelectConnection(boolean clearCurrentData) {
    try {
      if (clearCurrentData) {
        tableViewFields.table.removeAll();
        comboDimension.removeAll();
      }

      if (addConnectionLine.getText() != null) {
        DatabaseMeta dbMeta = transMeta.findDatabase(addConnectionLine.getText());
        if (dbMeta != null) {
          PaloDimInputData data = new PaloDimInputData(dbMeta);
          data.helper.connect();
          List<String> dimensions = data.helper.getDimensionsNames();
          Collections.sort(dimensions, new PaloNameComparator());
          for (String dimensionName : dimensions) {
            if (comboDimension.indexOf(dimensionName) == -1)
              comboDimension.add(dimensionName);
          }
          data.helper.disconnect();
        }
      }
    } catch (Exception ex) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "PaloDimInputDialog.RetreiveDimensionsErrorTitle"), BaseMessages.getString(PKG, "PaloDimInputDialog.RetreiveDimensionsError"), ex);
    }
  }

  private void doSelectDimension() {
    // tableViewFields.table.removeAll();
  }

  private void doClearLevels() {
    tableViewFields.table.removeAll();
  }

  private void doGetLevels() {
	  if (buttonBaseElementsOnly.getSelection()){
		  tableViewFields.table.removeAll();
		  tableViewFields.add(BaseMessages.getString(PKG, "PaloDimInputDialog.BaseElementName"), "0", comboDimension.getText(),"String");
	  }
	  else if (comboDimension.getText() != null && comboDimension.getText() != "") {
		  try {
			  if (addConnectionLine.getText() != null) {
				  DatabaseMeta dbMeta = transMeta.findDatabase(addConnectionLine.getText());
				  if (dbMeta != null) {
					  PaloDimInputData data = new PaloDimInputData(dbMeta);
					  tableViewFields.table.removeAll();
					  data.helper.connect();
					  List<PaloDimensionLevel> levels = data.helper.getDimensionLevels(comboDimension.getText());
					  for (int i = 0; i < levels.size(); i++){
  					      PaloDimensionLevel level = levels.get(i);
					      tableViewFields.add(level.getLevelName(), String.valueOf(level.getLevelNumber()), level.getFieldName());
					  }
					  tableViewFields.setRowNums();
					  tableViewFields.optWidth(true);
					  data.helper.disconnect();
				  }
			  }
		  } catch (Exception ex) {
			  new ErrorDialog(shell, BaseMessages.getString("System.Dialog.GetFieldsFailed.Title"), BaseMessages.getString("System.Dialog.GetFieldsFailed.Message"), ex);
		  }
	  } else {
		  new ErrorDialog(shell, BaseMessages.getString("System.Dialog.GetFieldsFailed.Title"), BaseMessages.getString("System.Dialog.GetFieldsFailed.Message"), new Exception(BaseMessages.getString(PKG, "PaloDimInputDialog.SelectDimensionFirstError")));
	  }

  }

  private void cancel() {
    stepname = null;
    meta.setChanged(changed);
    dispose();
  }

  private void ok() {
    try {
      getInfo(this.meta);
      dispose();
    } catch (KettleException e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "PaloDimInputDialog.FailedToSaveDataErrorTitle"), BaseMessages.getString(PKG, "PaloDimInputDialog.FailedToSaveDataError"), e);
    }
  }

  private void getInfo(PaloDimInputMeta myMeta) throws KettleException {
    stepname = textStepName.getText();
    List<PaloDimensionLevel> levels = new ArrayList<PaloDimensionLevel>();

    for (int i = 0; i < tableViewFields.table.getItemCount(); i++) {
      PaloDimensionLevel level = new PaloDimensionLevel(
    		  tableViewFields.table.getItem(i).getText(1), 
    		  Integer.parseInt(tableViewFields.table.getItem(i).getText(2)), 
    		  tableViewFields.table.getItem(i).getText(3), 
    		  tableViewFields.table.getItem(i).getText(4));
      levels.add(level);
    }
    myMeta.setDatabaseMeta(transMeta.findDatabase(addConnectionLine.getText()));
    myMeta.setLevels(levels);
    myMeta.setDimension(comboDimension.getText());
    myMeta.setBaseElementsOnly(buttonBaseElementsOnly.getSelection());
    myMeta.setChanged(true);

  }

  private void preview() {
    PaloDimInputMeta oneMeta = new PaloDimInputMeta();
    try {
      getInfo(oneMeta);
    } catch (KettleException e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "PaloInputDialog.Illegal.Dialog.Settings.Title"), BaseMessages.getString(PKG, "PaloInputDialog.Illegal.Dialog.Settings.Message"), e);
      return;
    }

    TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, textStepName.getText());

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
