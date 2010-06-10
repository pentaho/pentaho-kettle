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
package org.pentaho.di.ui.trans.steps.palo.cellinput;

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
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with PaloKettlePlugin.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *   Copyright 2008 Stratebi Business Solutions, S.L.
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.palo.core.DimensionField;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.palo.cellinput.PaloCellInputData;
import org.pentaho.di.trans.steps.palo.cellinput.PaloCellInputMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class PaloCellInputDialog extends BaseStepDialog implements StepDialogInterface {
  public static final String      STRING_PALO_LIB_WARNING_PARAMETER = "PaloLibWarning";

  private static Class<?>         PKG                               = PaloCellInputMeta.class; // for
                                                                                               // i18n
                                                                                               // purposes,
                                                                                               // needed
                                                                                               // by
                                                                                               // Translator2!!
                                                                                               // $NON-NLS-1$

  private final PaloCellInputMeta meta;

  private TableView               tableViewFields;
  private Combo                   comboCube;
  private Text                    textStepName;
  private Text                    textMeasureName;
  private Label                   labelStepName;
  private Label                   labelMeasureName;
  private Button                  buttonGetDimensions;
  private Button                  buttonClearDimensions;
  private Label                   labelCube;
  private Button                  buttonOk;
  private Button                  buttonCancel;
  private Button                  buttonPreview;
  private CCombo                  addConnectionLine;
  private Label                   labelMeasureType;
  private Combo                   comboMeasureType;
  private ColumnInfo[]            colinf;

  public PaloCellInputDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
    super(parent, (BaseStepMeta) in, transMeta, sname);
    this.meta = (PaloCellInputMeta) in;
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

    textStepName = new Text(shell, SWT.BORDER | SWT.FILL);
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

    comboCube = new Combo(shell, SWT.READ_ONLY | SWT.FILL);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(addConnectionLine, margin);
    comboCube.setLayoutData(fd);

    labelMeasureName = new Label(shell, SWT.RIGHT);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    fd.top = new FormAttachment(comboCube, margin);
    labelMeasureName.setLayoutData(fd);

    textMeasureName = new Text(shell, SWT.BORDER | SWT.FILL);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(comboCube, margin);
    textMeasureName.setLayoutData(fd);

    labelMeasureType = new Label(shell, SWT.RIGHT);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    fd.top = new FormAttachment(textMeasureName, margin);
    labelMeasureType.setLayoutData(fd);

    comboMeasureType = new Combo(shell, SWT.READ_ONLY | SWT.FILL);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(textMeasureName, margin);
    comboMeasureType.setLayoutData(fd);

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        meta.setChanged();
      }
    };
    colinf = new ColumnInfo[] { new ColumnInfo(getLocalizedColumn(0), ColumnInfo.COLUMN_TYPE_TEXT, false, true), new ColumnInfo(getLocalizedColumn(1), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(getLocalizedColumn(2), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "String", "Number" }, true) };

    tableViewFields = new TableView(null, shell, SWT.FILL | SWT.BORDER, colinf, 10, true, lsMod, props);

    tableViewFields.setSize(477, 280);
    tableViewFields.setBounds(5, 150, 477, 280);
    tableViewFields.setReadonly(true);
    tableViewFields.setSortable(false);
    fd = new FormData();
    fd.left = new FormAttachment(0, margin);
    fd.top = new FormAttachment(comboMeasureType, 3 * margin);
    fd.right = new FormAttachment(100, -150);
    fd.bottom = new FormAttachment(100, -50);
    tableViewFields.setLayoutData(fd);

    buttonGetDimensions = new Button(shell, SWT.NONE);
    fd = new FormData();
    fd.left = new FormAttachment(tableViewFields, margin);
    fd.top = new FormAttachment(comboMeasureType, 3 * margin);
    fd.right = new FormAttachment(100, 0);
    buttonGetDimensions.setLayoutData(fd);

    buttonClearDimensions = new Button(shell, SWT.NONE);
    fd = new FormData();
    fd.left = new FormAttachment(tableViewFields, margin);
    fd.top = new FormAttachment(buttonGetDimensions, margin);
    fd.right = new FormAttachment(100, 0);
    buttonClearDimensions.setLayoutData(fd);

    buttonOk = new Button(shell, SWT.CENTER);
    buttonCancel = new Button(shell, SWT.CENTER);
    buttonPreview = new Button(shell, SWT.CENTER);
    buttonOk.setText(BaseMessages.getString("System.Button.OK"));
    buttonPreview.setText(BaseMessages.getString("System.Button.Preview"));
    buttonCancel.setText(BaseMessages.getString("System.Button.Cancel"));

    setButtonPositions(new Button[] { buttonOk, buttonPreview, buttonCancel }, margin, null);

    buttonGetDimensions.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        doGetDimensions();
      }
    });
    buttonClearDimensions.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        doClearDimensions();

      }
    });
    comboCube.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        doSelectCube();
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
    buttonPreview.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        preview();
      }
    });
    addConnectionLine.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        doSelectConnection(false);
      }
    });

    this.fillLocalizedData();
    this.fillStoredData();
    this.doSelectConnection(false);

    props.setLook(tableViewFields);
    props.setLook(comboCube);
    props.setLook(textStepName);
    props.setLook(textMeasureName);
    props.setLook(labelStepName);
    props.setLook(labelMeasureName);
    props.setLook(buttonGetDimensions);
    props.setLook(buttonClearDimensions);
    props.setLook(labelCube);
    props.setLook(buttonOk);
    props.setLook(buttonCancel);
    props.setLook(buttonPreview);
    props.setLook(addConnectionLine);
    props.setLook(labelMeasureType);
    props.setLook(comboMeasureType);

    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        cancel();
      }
    });
    meta.setChanged(changed);

    setSize();

    shell.open();

    showPaloLibWarningDialog(shell);

    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    return stepname;
  }

  private void fillStoredData() {
    if (stepname != null)
      textStepName.setText(stepname);

    int index = addConnectionLine.indexOf(meta.getDatabaseMeta() != null ? meta.getDatabaseMeta().getName() : "");
    if (index >= 0)
      addConnectionLine.select(index);

    tableViewFields.table.removeAll();
    if (meta.getFields().size() > 0) {
      for (DimensionField level : meta.getFields()) {
        tableViewFields.add(level.getDimensionName(), level.getFieldName(), level.getFieldType());
      }
      tableViewFields.setRowNums();
      tableViewFields.optWidth(true);
    }

    if (meta.getCube() != null) {
      comboCube.add(meta.getCube());
      comboCube.select(0);
    }

    comboMeasureType.setItems(new String[] { "String", "Number" });
    if (meta.getCubeMeasure() != null) {
      if (meta.getCubeMeasure().getFieldType() != null) {
        int indexType = comboMeasureType.indexOf(meta.getCubeMeasure().getFieldType());
        if (indexType >= 0)
          comboMeasureType.select(indexType);
      }
    }

    if (meta.getCubeMeasure() != null)
      if (meta.getCubeMeasure().getFieldName() != null)
        textMeasureName.setText(meta.getCubeMeasure().getFieldName());

  }

  private String getLocalizedColumn(int columnIndex) {
    switch (columnIndex) {
    case 0:
      return BaseMessages.getString(PKG, "PaloCellInputDialog.ColumnDimension");
    case 1:
      return BaseMessages.getString(PKG, "PaloCellInputDialog.ColumnField");
    case 2:
      return BaseMessages.getString(PKG, "PaloCellInputDialog.ColumnType");
    default:
      return "";
    }
  }

  private void fillLocalizedData() {
    buttonGetDimensions.setText(BaseMessages.getString(PKG, "PaloCellInputDialog.GetDimensions"));
    buttonClearDimensions.setText(BaseMessages.getString(PKG, "PaloCellInputDialog.ClearDimensions"));
    labelCube.setText(BaseMessages.getString(PKG, "PaloCellInputDialog.SelectCube"));
    labelStepName.setText(BaseMessages.getString(PKG, "PaloCellInputDialog.StepName"));
    shell.setText(BaseMessages.getString(PKG, "PaloCellInputDialog.PaloCellInput"));
    labelMeasureName.setText(BaseMessages.getString(PKG, "PaloCellInputDialog.MeasureName"));
    labelMeasureType.setText(BaseMessages.getString(PKG, "PaloCellInputDialog.MeasureType"));
  }

  private void doClearDimensions() {
    tableViewFields.table.removeAll();
  }

  private void doGetDimensions() {
    try {
      if (comboCube.getText() != null && comboCube.getText() != "") {
        if (addConnectionLine.getText() != null) {
          DatabaseMeta dbMeta = transMeta.findDatabase(addConnectionLine.getText());
          if (dbMeta != null) {
            PaloCellInputData data = new PaloCellInputData(dbMeta);
            data.helper.connect();
            List<String> cubeDimensions = data.helper.getCubeDimensions(comboCube.getText());
            tableViewFields.table.removeAll();
            for (int i = 0; i < cubeDimensions.size(); i++) {
              final TableItem item = new TableItem(tableViewFields.table, SWT.NONE);
              item.setText(1, cubeDimensions.get(i));
              item.setText(2, cubeDimensions.get(i));
            }
            tableViewFields.removeEmptyRows();
            tableViewFields.setRowNums();
            tableViewFields.optWidth(true);
            tableViewFields.setReadonly(true);
            data.helper.disconnect();
          }
        }
      } else {
        new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Title"), BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"), new Exception(BaseMessages.getString(PKG,
            "PaloCellInputDialog.SelectCubeFirstError")));
      }
    } catch (KettleException ke) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Title"), BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"), ke);
    }
  }

  private void doSelectCube() {
    // tableViewFields.table.removeAll();
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
          PaloCellInputData data = new PaloCellInputData(dbMeta);
          data.helper.connect();
          List<String> cubes = data.helper.getCubesNames();
          for (String cubeName : cubes) {
            if (comboCube.indexOf(cubeName) == -1)
              comboCube.add(cubeName);
          }
          data.helper.disconnect();
        }
      }
    } catch (Exception ex) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "PaloCellInputDialog.RetreiveCubesErrorTitle"), BaseMessages.getString(PKG, "PaloCellInputDialog.RetreiveCubesError"), ex);
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
      new ErrorDialog(shell, BaseMessages.getString(PKG, "PaloCellInputDialog.FailedToSaveDataErrorTitle"), BaseMessages.getString(PKG, "PaloCellInputDialog.FailedToSaveDataError"), e);
    }
  }

  public static void showPaloLibWarningDialog(Shell shell) {
    PropsUI props = PropsUI.getInstance();

    if ("Y".equalsIgnoreCase(props.getCustomParameter(STRING_PALO_LIB_WARNING_PARAMETER, "Y"))) //$NON-NLS-1$ //$NON-NLS-2$
    {
      MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
          BaseMessages.getString(PKG, "PaloCellInputDialog.PaloLibWarningDialog.DialogTitle"), //$NON-NLS-1$
          null, 
          BaseMessages.getString(PKG, "PaloCellInputDialog.PaloLibWarningDialog.DialogMessage", Const.CR) + Const.CR, //$NON-NLS-1$ //$NON-NLS-2$
          MessageDialog.WARNING, new String[] { 
            BaseMessages.getString(PKG, "PaloCellInputDialog.PaloLibWarningDialog.Option1") }, //$NON-NLS-1$
            0, 
            BaseMessages.getString(PKG, "PaloCellInputDialog.PaloLibWarningDialog.Option2"), //$NON-NLS-1$
          "N".equalsIgnoreCase(props.getCustomParameter(STRING_PALO_LIB_WARNING_PARAMETER, "Y")) //$NON-NLS-1$ //$NON-NLS-2$
      );
      MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
      md.open();
      props.setCustomParameter(STRING_PALO_LIB_WARNING_PARAMETER, md.getToggleState() ? "N" : "Y"); //$NON-NLS-1$ //$NON-NLS-2$
      props.saveProps();
    }
  }

  private void getInfo(PaloCellInputMeta myMeta) throws KettleException {
    stepname = textStepName.getText();

    List<DimensionField> fields = new ArrayList<DimensionField>();

    for (int i = 0; i < tableViewFields.table.getItemCount(); i++) {
      DimensionField field = new DimensionField(tableViewFields.table.getItem(i).getText(1), tableViewFields.table.getItem(i).getText(2), tableViewFields.table.getItem(i).getText(3));
      fields.add(field);
    }
    myMeta.setDatabaseMeta(transMeta.findDatabase(addConnectionLine.getText()));
    myMeta.setCubeMeasureName(new DimensionField("Measure", textMeasureName.getText(), comboMeasureType.getText()));
    myMeta.setLevels(fields);
    myMeta.setCube(comboCube.getText());
    myMeta.setChanged(true);
  }

  private void preview() {
    PaloCellInputMeta oneMeta = new PaloCellInputMeta();
    try {
      getInfo(oneMeta);
      if (oneMeta.getFields() == null || oneMeta.getFields().size() == 0) {
        throw new KettleException("Fields must be defined to do a preview");
      } else {
        for (DimensionField field : oneMeta.getFields()) {
          if (Const.isEmpty(field.getFieldType()))
            throw new KettleException("All fields must have an output type to do the preview");
        }
      }
    } catch (KettleException e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "RowGeneratorDialog.Illegal.Dialog.Settings.Title"), BaseMessages.getString(PKG, "RowGeneratorDialog.Illegal.Dialog.Settings.Message"), e);
      return;
    }

    TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, textStepName.getText());

    EnterNumberDialog numberDialog = new EnterNumberDialog(shell, 500, BaseMessages.getString(PKG, "System.Dialog.EnterPreviewSize.Title"), BaseMessages.getString(PKG, "System.Dialog.EnterPreviewSize.Message"));
    int previewSize = numberDialog.open();
    if (previewSize > 0) {
      TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { textStepName.getText() }, new int[] { previewSize });
      progressDialog.open();

      Trans trans = progressDialog.getTrans();
      String loggingText = progressDialog.getLoggingText();

      if (!progressDialog.isCancelled()) {
        if (trans.getResult() != null && trans.getResult().getNrErrors() > 0) {
          EnterTextDialog etd = new EnterTextDialog(shell, BaseMessages.getString(PKG, "System.Dialog.PreviewError.Title"), BaseMessages.getString(PKG, "System.Dialog.PreviewError.Message"), loggingText, true);
          etd.setReadOnly();
          etd.open();
        }
      }

      PreviewRowsDialog prd = new PreviewRowsDialog(shell, transMeta, SWT.NONE, textStepName.getText(), progressDialog.getPreviewRowsMeta(textStepName.getText()), progressDialog.getPreviewRows(textStepName.getText()), loggingText);
      prd.open();
    }
  }

}
