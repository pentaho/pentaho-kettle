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
package org.pentaho.di.ui.trans.steps.palo.celloutput;

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
import java.util.Arrays;
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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.palo.core.DimensionField;
import org.pentaho.di.palo.core.PaloNameComparator;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.palo.celloutput.PaloCellOutputData;
import org.pentaho.di.trans.steps.palo.celloutput.PaloCellOutputMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.steps.palo.cellinput.PaloCellInputDialog;

public class PaloCellOutputDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = PaloCellOutputMeta.class; // for i18n purposes,
                                                          // needed by
                                                          // Translator2!!
                                                          // $NON-NLS-1$

  public static void main(String[] args) {
    try {
      PaloCellOutputDialog window = new PaloCellOutputDialog(null, new PaloCellOutputMeta(), null, "noname");
      window.open();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private PaloCellOutputMeta meta;

  private TableView          tableViewFields;
  private Text               textStepName;
  private Combo              comboCube;
  private Combo              comboMeasureType;
  private Label              labelStepName;
  private Label              labelCube;
  private Label              labelMeasureType;
  private Button             buttonClearFields;
  private Button             buttonGetFields;
  private Button             buttonOk;
  private Button             buttonCancel;
  private Label              labelClearCube;
  private Button             buttonClearCube;
  private Label              labelCommitSize;
  private Text               textCommitSize;
  private Label              labelPreloadDimensionCache;
  private Button             buttonPreloadDimensionCache;
  private Label              labelEnableDimensionCache;
  private Button             buttonEnableDimensionCache;
  private CCombo             addConnectionLine;
  private ColumnInfo[]       colinf;

  public PaloCellOutputDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
    super(parent, (BaseStepMeta) in, transMeta, sname);
    this.meta = (PaloCellOutputMeta) in;
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

    labelCube = new Label(shell, SWT.RIGHT);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    fd.top = new FormAttachment(addConnectionLine, margin);
    labelCube.setLayoutData(fd);

    comboCube = new Combo(shell, SWT.READ_ONLY);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(addConnectionLine, margin);
    comboCube.setLayoutData(fd);

    labelMeasureType = new Label(shell, SWT.RIGHT);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    fd.top = new FormAttachment(comboCube, margin);
    labelMeasureType.setLayoutData(fd);

    comboMeasureType = new Combo(shell, SWT.READ_ONLY | SWT.FILL);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(comboCube, margin);
    comboMeasureType.setLayoutData(fd);
    
    labelCommitSize = new Label(shell, SWT.RIGHT);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    fd.top = new FormAttachment(comboMeasureType, margin);
    labelCommitSize.setLayoutData(fd);

    textCommitSize = new Text(shell, SWT.BORDER);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(comboMeasureType, margin);
    textCommitSize.setLayoutData(fd);

    labelClearCube = new Label(shell, SWT.RIGHT);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    fd.top = new FormAttachment(textCommitSize, margin);
    labelClearCube.setLayoutData(fd);

    buttonClearCube = new Button(shell, SWT.CHECK);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(textCommitSize, margin);
    buttonClearCube.setLayoutData(fd);
    
    labelEnableDimensionCache = new Label(shell, SWT.RIGHT);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    fd.top = new FormAttachment(buttonClearCube, margin);
    labelEnableDimensionCache.setLayoutData(fd);

    buttonEnableDimensionCache = new Button(shell, SWT.CHECK);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(buttonClearCube, margin);
    buttonEnableDimensionCache.setLayoutData(fd);
    
    labelPreloadDimensionCache = new Label(shell, SWT.RIGHT);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    fd.top = new FormAttachment(buttonEnableDimensionCache, margin);
    labelPreloadDimensionCache.setLayoutData(fd);

    buttonPreloadDimensionCache = new Button(shell, SWT.CHECK);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(buttonEnableDimensionCache, margin);
    buttonPreloadDimensionCache.setLayoutData(fd);

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        meta.setChanged();
      }
    };

    colinf = new ColumnInfo[] { new ColumnInfo(getLocalizedColumn(0), ColumnInfo.COLUMN_TYPE_TEXT, false, true), new ColumnInfo(getLocalizedColumn(1), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {}, true),};

    tableViewFields = new TableView(null, shell, SWT.NONE | SWT.BORDER, colinf, 10, true, lsMod, props);

    tableViewFields.setSize(477, 105);
    tableViewFields.setBounds(5, 250, 477, 105);
    tableViewFields.setReadonly(true);
    tableViewFields.setSortable(false);
    tableViewFields.table.removeAll();
    fd = new FormData();
    fd.left = new FormAttachment(0, margin);
    fd.top = new FormAttachment(buttonPreloadDimensionCache, 3 * margin);
    fd.right = new FormAttachment(100, -150);
    fd.bottom = new FormAttachment(100, -50);
    tableViewFields.setLayoutData(fd);

    buttonGetFields = new Button(shell, SWT.NONE);
    fd = new FormData();
    fd.left = new FormAttachment(tableViewFields, margin);
    fd.top = new FormAttachment(buttonPreloadDimensionCache, 3 * margin);
    fd.right = new FormAttachment(100, 0);
    buttonGetFields.setLayoutData(fd);

    buttonClearFields = new Button(shell, SWT.NONE);
    fd = new FormData();
    fd.left = new FormAttachment(tableViewFields, margin);
    fd.top = new FormAttachment(buttonGetFields, margin);
    fd.right = new FormAttachment(100, 0);
    buttonClearFields.setLayoutData(fd);

    buttonOk = new Button(shell, SWT.CENTER);
    buttonCancel = new Button(shell, SWT.CENTER);
    buttonOk.setText(BaseMessages.getString("System.Button.OK"));
    buttonCancel.setText(BaseMessages.getString("System.Button.Cancel"));
    setButtonPositions(new Button[] { buttonOk, buttonCancel }, margin, null);

    buttonGetFields.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        doGetFields();
      }
    });
    buttonClearFields.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        doClearFields();

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
    addConnectionLine.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        doSelectConnection(false);
      }
    });
    comboCube.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        doSelectCube();
      }
    });
    buttonEnableDimensionCache.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent arg0) {
			buttonPreloadDimensionCache.setEnabled(buttonEnableDimensionCache.getSelection());
		}
	});

    this.fillLocalizedData();
    this.fillStoredData();
    this.doSelectConnection(false);

    props.setLook(tableViewFields);
    props.setLook(textStepName);
    props.setLook(comboCube);
    props.setLook(comboMeasureType);
    props.setLook(labelStepName);
    props.setLook(labelCube);
    props.setLook(labelMeasureType);
    props.setLook(buttonClearFields);
    props.setLook(buttonGetFields);
    props.setLook(buttonOk);
    props.setLook(buttonCancel);
    props.setLook(addConnectionLine);
    props.setLook(buttonClearCube);
    props.setLook(labelClearCube);
    props.setLook(textCommitSize);
    props.setLook(labelCommitSize);
    props.setLook(labelPreloadDimensionCache);
    props.setLook(buttonPreloadDimensionCache);
    props.setLook(labelEnableDimensionCache);
    props.setLook(buttonEnableDimensionCache);
    
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
      return BaseMessages.getString(PKG, "PaloCellOutputDialog.ColumnDimension");
    case 1:
      return BaseMessages.getString(PKG, "PaloCellOutputDialog.ColumnField");
    case 2:
      return BaseMessages.getString(PKG, "PaloCellOutputDialog.ColumnType");
    default:
      return "";
    }
  }

  private void fillLocalizedData() {
    labelStepName.setText(BaseMessages.getString(PKG, "PaloCellOutputDialog.StepName"));
    shell.setText(BaseMessages.getString(PKG, "PaloCellOutputDialog.PaloCellOutput"));
    buttonGetFields.setText(BaseMessages.getString(PKG, "PaloCellOutputDialog.GetFields"));
    buttonClearFields.setText(BaseMessages.getString(PKG, "PaloCellOutputDialog.ClearFields"));
    labelCube.setText(BaseMessages.getString(PKG, "PaloCellOutputDialog.SelectCube"));
    labelMeasureType.setText(BaseMessages.getString(PKG, "PaloCellOutputDialog.SelectMeasureType"));
    labelClearCube.setText(BaseMessages.getString(PKG, "PaloCellOutputDialog.ClearCube"));
    labelCommitSize.setText(BaseMessages.getString(PKG, "PaloCellOutputDialog.CommitSize"));
    labelPreloadDimensionCache.setText(BaseMessages.getString(PKG, "PaloCellOutputDialog.PreloadDimensionCache"));
    labelEnableDimensionCache.setText(BaseMessages.getString(PKG, "PaloCellOutputDialog.EnableDimensionCache"));
  }

  private void fillStoredData() {
    if (stepname != null)
      textStepName.setText(stepname);

    int index = addConnectionLine.indexOf(meta.getDatabaseMeta() != null ? meta.getDatabaseMeta().getName() : "");
    if (index >= 0)
      addConnectionLine.select(index);

    if (meta.getCube() != null) {
      comboCube.add(meta.getCube());
      comboCube.select(0);
    }

    if (meta.getMeasureType() != null) {
      comboMeasureType.add(meta.getMeasureType());
      comboMeasureType.select(0);
    }
    
    textCommitSize.setText(String.valueOf(meta.getCommitSize()));
    buttonEnableDimensionCache.setSelection(meta.getEnableDimensionCache());
    buttonPreloadDimensionCache.setSelection(meta.getPreloadDimensionCache());
	buttonPreloadDimensionCache.setEnabled(buttonEnableDimensionCache.getSelection());

    comboMeasureType.setItems(new String[] { "Numeric", "String" });
    comboMeasureType.select(0);
    if (meta.getMeasureType() != null) {
      int indexType = comboMeasureType.indexOf(meta.getMeasureType());
      if (indexType >= 0)
        comboMeasureType.select(indexType);
    }

    tableViewFields.table.removeAll();

    if (meta.getFields().size() > 0) {
      for (DimensionField level : meta.getFields()) {
        tableViewFields.add(level.getDimensionName(), level.getFieldName());
      }
    }

    List<String> fieldNameList = null;
    try {
      RowMetaInterface r = transMeta.getPrevStepFields(stepname);
      fieldNameList =  Arrays.asList( r.getFieldNames());
      Collections.sort(fieldNameList);
    } catch (Exception e) {
    }
    tableViewFields.setColumnInfo(1, new ColumnInfo("Field", ColumnInfo.COLUMN_TYPE_CCOMBO, 
    		(fieldNameList == null ? null : fieldNameList.toArray(new String[0]))
    		, true));

    if (meta.getMeasure() != null) {
      final TableItem item = new TableItem(tableViewFields.table, SWT.NONE);
      item.setText(1, meta.getMeasure().getDimensionName());
      item.setText(2, meta.getMeasure().getFieldName());
      // item.setText(3,meta.getMeasure().getFieldType());
      item.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
    }
    tableViewFields.setRowNums();
    tableViewFields.optWidth(true);

    buttonClearCube.setSelection(meta.getClearCube());

  }

  private void doSelectConnection(boolean clearCurrentData) {
    try {
      if (clearCurrentData) {
        tableViewFields.table.removeAll();
        comboCube.removeAll();
      }

      if (addConnectionLine.getText() != null) {
        DatabaseMeta dbMeta = transMeta.findDatabase(addConnectionLine.getText());
        if (dbMeta != null) {
          PaloCellOutputData data = new PaloCellOutputData(dbMeta);
          data.helper.connect();
          List<String> cubes = data.helper.getCubesNames();
          Collections.sort(cubes, new PaloNameComparator());
          for (String cubeName : cubes) {
            if (comboCube.indexOf(cubeName) == -1)
              comboCube.add(cubeName);
          }
          data.helper.disconnect();
        }
      }
    } catch (Exception ex) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "PaloCellOutputDialog.RetreiveCubesErrorTitle"), BaseMessages.getString(PKG, "PaloCellOutputDialog.RetreiveCubesError"), ex);
    }
  }

  private void fillPreviousFieldTableViewColumn() throws KettleException {
    RowMetaInterface r = transMeta.getPrevStepFields(stepname);
    if (r != null) {
      List<String> fieldNameList =  Arrays.asList( r.getFieldNames());
      Collections.sort(fieldNameList);
      colinf[1] = new ColumnInfo(getLocalizedColumn(1), ColumnInfo.COLUMN_TYPE_CCOMBO, fieldNameList.toArray(new String[0]), true);
    }
  }

  private void doGetFields() {
    try {
      List<String> cubeDimensions = null;
      if (comboCube.getText() != null && comboCube.getText() != "") {
        if (addConnectionLine.getText() != null) {
          DatabaseMeta dbMeta = transMeta.findDatabase(addConnectionLine.getText());
          if (dbMeta != null) {
            PaloCellOutputData data = new PaloCellOutputData(dbMeta);
            data.helper.connect();
            cubeDimensions = data.helper.getCubeDimensions(comboCube.getText());
            data.helper.disconnect();
          }
        }
        tableViewFields.table.removeAll();

        for (int i = 0; i < cubeDimensions.size(); i++) {
          final TableItem item = new TableItem(tableViewFields.table, SWT.NONE);
          item.setText(1, cubeDimensions.get(i));
          // item.setText(3, "String");

        }
        final TableItem item = new TableItem(tableViewFields.table, SWT.NONE);
        item.setText(1, "Cube Measure");
        item.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));

        tableViewFields.removeEmptyRows();
        tableViewFields.setRowNums();
        tableViewFields.optWidth(true);
        tableViewFields.setReadonly(true);

      } else {
        new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Title"), BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"), new Exception(BaseMessages.getString(PKG,
            "PaloCellOutputDialog.SelectCubeFirstError")));
      }

      this.fillPreviousFieldTableViewColumn();

    } catch (KettleException ke) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Title"), BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"), ke);
    }
  }

  private void doClearFields() {
    tableViewFields.table.removeAll();
  }

  private void doSelectCube() {
    // tableViewFields.table.removeAll();
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
      new ErrorDialog(shell, BaseMessages.getString(PKG, "PaloCellOutputDialog.FailedToSaveDataErrorTitle"), BaseMessages.getString(PKG, "PaloCellOutputDialog.FailedToSaveDataError"), e);
    }
  }

  private void getInfo(PaloCellOutputMeta myMeta) throws KettleException {
    stepname = textStepName.getText();
    List<DimensionField> fields = new ArrayList<DimensionField>();

    try{
    	Integer.parseInt(this.textCommitSize.getText());
    }
    catch (Exception e){
    	throw new KettleException(BaseMessages.getString(PKG, "PaloCellOutputDialog.CommitSizeErrorMessage"));
    }


    for (int i = 0; i < tableViewFields.table.getItemCount(); i++) {

      DimensionField field = new DimensionField(tableViewFields.table.getItem(i).getText(1), tableViewFields.table.getItem(i).getText(2), ""// tableViewFields.table.getItem(i).getText(3)
      );

      if (i != tableViewFields.table.getItemCount() - 1) {
        // if(tableViewFields.table.getItem(i).getText(3)!="String")
        // throw new
        // KettleException("Dimension input field must be from String type");
        fields.add(field);
      } else
        myMeta.setMeasureField(field);
    }
    
    
    myMeta.setCube(this.comboCube.getText());
    myMeta.setMeasureType(this.comboMeasureType.getText());
    myMeta.setLevels(fields);
    myMeta.setClearCube(this.buttonClearCube.getSelection());
    myMeta.setDatabaseMeta(transMeta.findDatabase(addConnectionLine.getText()));
    myMeta.setCommitSize(Integer.parseInt(this.textCommitSize.getText()));
    myMeta.setEnableDimensionCache(this.buttonEnableDimensionCache.getSelection());
    if (this.buttonEnableDimensionCache.getSelection())
    	myMeta.setPreloadDimensionCache(this.buttonPreloadDimensionCache.getSelection());
    else
    	myMeta.setPreloadDimensionCache(false);
    myMeta.setChanged(true);

  }
}
