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
package org.pentaho.di.ui.trans.steps.palo.dimoutput;

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
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.palo.core.PaloDimensionLevel;
import org.pentaho.di.palo.core.PaloNameComparator;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.palo.dimoutput.PaloDimOutputData;
import org.pentaho.di.trans.steps.palo.dimoutput.PaloDimOutputMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.steps.palo.cellinput.PaloCellInputDialog;

public class PaloDimOutputDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = PaloDimOutputMeta.class; // for i18n purposes,
                                                         // needed by
                                                         // Translator2!!
                                                         // $NON-NLS-1$

  public static void main(String[] args) {
    try {
      PaloDimOutputDialog window = new PaloDimOutputDialog(null, new PaloDimOutputMeta(), null, "noname");
      window.open();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private final PaloDimOutputMeta meta;
  private Text                    textStepName;
  private Combo                   comboDimension;
  private Combo                   comboElementType;
  private TableView               tableViewFields;
  private ColumnInfo[]            colinf;
  private Button                  buttonOk;
  private Button                  buttonCancel;
  private Button                  buttonCreateDimension;
  private Label                   labelCreateDimension;
  private Button                  buttonClearDimension;
  private Label                   labelClearDimension;
  private Button                  buttonClearConsolidations;
  private Label                   labelClearConsolidations;
  private Button                  buttonEnableElementCache;
  private Label                   labelEnableElementCache;
  private Button                  buttonPreloadElementCache;
  private Label                   labelPreloadElementCache;
  private Button                  buttonRecreateDimension;
  private Label                   labelRecreateDimension;
  private Label                   labelDimension;
  private Label                   labelElementType;
  private Button                  buttonClearLevels;
  private Button                  buttonGetLevels;
  private Label                   labelStepName;
  private CCombo                  addConnectionLine;
  

  public PaloDimOutputDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
    super(parent, (BaseStepMeta) in, transMeta, sname);
    this.meta = (PaloDimOutputMeta) in;
  }

  public String open() {

    final Display display = getParent().getDisplay();
    shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
    props.setLook(shell);
    setShellImage(shell, meta);
    setShellImage(shell, meta);
    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout(formLayout);
    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    FormData fd;

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        for (int i = 0; i < tableViewFields.table.getItemCount(); i++) {
          tableViewFields.setText(String.valueOf(i), 1, i);
        }
      }
    };

    labelStepName = new Label(shell, SWT.RIGHT);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    fd.top = new FormAttachment(0, margin);
    labelStepName.setLayoutData(fd);

    textStepName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(0, margin);
    textStepName.setLayoutData(fd);

    addConnectionLine = addConnectionLine(shell, textStepName, Const.MIDDLE_PCT, margin);

    labelCreateDimension = new Label(shell, SWT.RIGHT);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    fd.top = new FormAttachment(addConnectionLine, margin);
    labelCreateDimension.setLayoutData(fd);

    buttonCreateDimension = new Button(shell, SWT.CHECK);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(addConnectionLine, margin);
    buttonCreateDimension.setLayoutData(fd);

    labelRecreateDimension = new Label(shell, SWT.RIGHT);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right= new FormAttachment(middle, -margin);
    fd.top  = new FormAttachment(buttonCreateDimension, margin);
    labelRecreateDimension.setLayoutData(fd);
    
    buttonRecreateDimension = new Button(shell, SWT.CHECK);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.right= new FormAttachment(100, 0);
    fd.top  = new FormAttachment(buttonCreateDimension, margin);
    buttonRecreateDimension.setLayoutData(fd);
    
    labelClearDimension = new Label(shell, SWT.RIGHT);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    fd.top = new FormAttachment(buttonRecreateDimension, margin);
    labelClearDimension.setLayoutData(fd);

    buttonClearDimension = new Button(shell, SWT.CHECK);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(buttonRecreateDimension, margin);
    buttonClearDimension.setLayoutData(fd);
    
    labelClearConsolidations = new Label(shell, SWT.RIGHT);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    fd.top = new FormAttachment(buttonClearDimension, margin);
    labelClearConsolidations.setLayoutData(fd);

    buttonClearConsolidations = new Button(shell, SWT.CHECK);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(buttonClearDimension, margin);
    buttonClearConsolidations.setLayoutData(fd);
    
    labelEnableElementCache = new Label(shell, SWT.RIGHT);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    fd.top = new FormAttachment(buttonClearConsolidations, margin);
    labelEnableElementCache.setLayoutData(fd);

    buttonEnableElementCache = new Button(shell, SWT.CHECK);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(buttonClearConsolidations, margin);
    buttonEnableElementCache.setLayoutData(fd);
    
    labelPreloadElementCache = new Label(shell, SWT.RIGHT);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    fd.top = new FormAttachment(buttonEnableElementCache, margin);
    labelPreloadElementCache.setLayoutData(fd);

    buttonPreloadElementCache = new Button(shell, SWT.CHECK);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(buttonEnableElementCache, margin);
    buttonPreloadElementCache.setLayoutData(fd);
    
    labelDimension = new Label(shell, SWT.RIGHT);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    fd.top = new FormAttachment(buttonPreloadElementCache, margin);
    labelDimension.setLayoutData(fd);

    comboDimension = new Combo(shell, SWT.SIMPLE | SWT.DROP_DOWN);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(buttonPreloadElementCache, margin);
    comboDimension.setLayoutData(fd);

    labelElementType = new Label(shell, SWT.RIGHT);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    fd.top = new FormAttachment(comboDimension, margin);
    labelElementType.setLayoutData(fd);

    comboElementType = new Combo(shell, SWT.READ_ONLY | SWT.FILL);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(comboDimension, margin);
    comboElementType.setLayoutData(fd);

    colinf = new ColumnInfo[] { new ColumnInfo(getLocalizedColumn(0), ColumnInfo.COLUMN_TYPE_TEXT, false, true), 
    		new ColumnInfo(getLocalizedColumn(1), ColumnInfo.COLUMN_TYPE_TEXT, false, false),
    		new ColumnInfo(getLocalizedColumn(2), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {}, true),
    		new ColumnInfo(getLocalizedColumn(3), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {}, true),

    };

    tableViewFields = new TableView(null, shell, SWT.FILL | SWT.BORDER, colinf, 10, true, lsMod, props);

    tableViewFields.setSize(477, 105);
    tableViewFields.setBounds(5, 250, 477, 105);
    tableViewFields.setReadonly(false);
    tableViewFields.table.removeAll();
    tableViewFields.setSortable(false);
    fd = new FormData();
    fd.left = new FormAttachment(0, margin);
    fd.top = new FormAttachment(comboElementType, 3 * margin);
    fd.right = new FormAttachment(100, -150);
    fd.bottom = new FormAttachment(100, -50);
    tableViewFields.setLayoutData(fd);

    buttonGetLevels = new Button(shell, SWT.CENTER);
    fd = new FormData();
    fd.left = new FormAttachment(tableViewFields, margin);
    fd.top = new FormAttachment(comboElementType, 3 * margin);
    fd.right = new FormAttachment(100, 0);
    buttonGetLevels.setLayoutData(fd);

    buttonClearLevels = new Button(shell, SWT.CENTER);
    fd = new FormData();
    fd.left = new FormAttachment(tableViewFields, margin);
    fd.top = new FormAttachment(buttonGetLevels, margin);
    fd.right = new FormAttachment(100, 0);
    buttonClearLevels.setLayoutData(fd);

    buttonOk = new Button(shell, SWT.CENTER);
    buttonCancel = new Button(shell, SWT.CENTER);
    buttonOk.setText(BaseMessages.getString("System.Button.OK"));
    buttonCancel.setText(BaseMessages.getString("System.Button.Cancel"));
    setButtonPositions(new Button[] { buttonOk, buttonCancel }, margin, null);

    addConnectionLine.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        doSelectConnection(false);
      }
    });

    buttonGetLevels.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        doGetFields();
      }
    });

    buttonOk.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        ok();
      }
    });
    buttonCancel.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        cancel();
      }
    });
    buttonClearLevels.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        doClearFields();
      }
    });
    comboDimension.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        doSelectDimension();
      }
    });
    buttonRecreateDimension.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent arg0) {
			buttonClearDimension.setEnabled(!buttonRecreateDimension.getSelection());
			buttonClearConsolidations.setEnabled(!buttonRecreateDimension.getSelection());
		}
	});
    buttonClearDimension.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent arg0) {
			buttonClearConsolidations.setEnabled(!buttonClearDimension.getSelection());
		}
	});
    
    buttonEnableElementCache.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent arg0) {
			buttonPreloadElementCache.setEnabled(buttonEnableElementCache.getSelection());
		}
	});

    fillLocalizedData();
    fillStoredData();
    doSelectConnection(false);
    try {
      this.fillPreviousFieldTableViewColumn();
    } catch (Exception e) {

    }
    
    props.setLook(textStepName);
    props.setLook(comboDimension);
    props.setLook(comboElementType);
    props.setLook(tableViewFields);
    props.setLook(buttonOk);
    props.setLook(buttonCancel);
    props.setLook(labelDimension);
    props.setLook(labelElementType);
    props.setLook(buttonClearLevels);
    props.setLook(buttonGetLevels);
    props.setLook(labelStepName);
    props.setLook(addConnectionLine);
    props.setLook(buttonCreateDimension);
    props.setLook(labelCreateDimension);
    props.setLook(buttonClearDimension);
    props.setLook(labelClearDimension);
    props.setLook(buttonClearConsolidations);
    props.setLook(labelClearConsolidations);
    props.setLook(buttonRecreateDimension);
    props.setLook(labelRecreateDimension);
    props.setLook(buttonEnableElementCache);
    props.setLook(labelEnableElementCache);
    props.setLook(buttonPreloadElementCache);
    props.setLook(labelPreloadElementCache);
    
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

  private void doSelectDimension() {
    // tableViewFields.table.removeAll();
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
          PaloDimOutputData data = new PaloDimOutputData(dbMeta);
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
      new ErrorDialog(shell, BaseMessages.getString(PKG, "PaloDimOutputDialog.RetreiveDimensionsErrorTitle"), BaseMessages.getString(PKG, "PaloDimOutputDialog.RetreiveDimensionsError"), ex);
    }
  }

  private void fillPreviousFieldTableViewColumn() throws KettleException {
    RowMetaInterface r = transMeta.getPrevStepFields(stepname);
    if (r != null) {
      String[] fieldNames = r.getFieldNames();
      colinf[2] = new ColumnInfo(getLocalizedColumn(2), ColumnInfo.COLUMN_TYPE_CCOMBO, fieldNames, true);

      /* Get all number fields to include in the consolidation factor column */
      String [] fieldTypes = r.getFieldNamesAndTypes(0);
      ArrayList<String> intFields = new ArrayList<String>();
      intFields.add(BaseMessages.getString(PKG, "PaloDimOutputDialog.ConsolidationFactorDefault"));
      for (int i = 0; i < fieldNames.length; i++)
    	  if (fieldTypes[i].toLowerCase().indexOf("integer") >= 0 || fieldTypes[i].toLowerCase().indexOf("number") > 0)
    		  intFields.add(fieldNames[i]);
      
      String [] fieldNamesDefault = intFields.toArray(new String [intFields.size()]);
      colinf[3] = new ColumnInfo(getLocalizedColumn(3), ColumnInfo.COLUMN_TYPE_CCOMBO, fieldNamesDefault, true);
    }
  }

  private void doGetFields() {
    try {
      if (comboDimension.getText() != null) {
        if (addConnectionLine.getText() != null) {
          DatabaseMeta dbMeta = transMeta.findDatabase(addConnectionLine.getText());
          if (dbMeta != null) {
            PaloDimOutputData data = new PaloDimOutputData(dbMeta);
            tableViewFields.table.removeAll();
            data.helper.connect();
            List<PaloDimensionLevel> levels = data.helper.getDimensionLevels(comboDimension.getText());
            for (PaloDimensionLevel level : levels) {
              tableViewFields.add(String.valueOf(level.getLevelNumber()), level.getLevelName(), level.getFieldName());
            }
            tableViewFields.removeEmptyRows();
            tableViewFields.setRowNums();
            tableViewFields.optWidth(true);

            data.helper.disconnect();
          }
        }
      } else {
        new ErrorDialog(shell, BaseMessages.getString("System.Dialog.GetFieldsFailed.Title"), BaseMessages.getString("System.Dialog.GetFieldsFailed.Message"), new Exception(BaseMessages
            .getString(PKG, "PaloDimOutputDialog.SelectDimensionsFirstError")));
      }

      this.fillPreviousFieldTableViewColumn();

    } catch (KettleException ke) {
      new ErrorDialog(shell, BaseMessages.getString("System.Dialog.GetFieldsFailed.Title"), BaseMessages.getString("System.Dialog.GetFieldsFailed.Message"), ke);
      tableViewFields.add();
    }
  }

  private void doClearFields() {
    tableViewFields.table.removeAll();
    tableViewFields.add();
  }

  private String getLocalizedColumn(int columnIndex) {
    switch (columnIndex) {
    case 0:
      return BaseMessages.getString(PKG, "PaloDimOutputDialog.ColumnLevelNumber");
    case 1:
      return BaseMessages.getString(PKG, "PaloDimOutputDialog.ColumnLevelName");
    case 2:
      return BaseMessages.getString(PKG, "PaloDimOutputDialog.ColumnField");
    case 3:
      return BaseMessages.getString(PKG, "PaloDimOutputDialog.ColumnConsolidationFactor");
    default:
      return "";
    }
  }

  private void fillStoredData() {
    if (stepname != null)
      textStepName.setText(stepname);

    int index = addConnectionLine.indexOf(meta.getDatabaseMeta() != null ? meta.getDatabaseMeta().getName() : "");
    if (index >= 0)
      addConnectionLine.select(index);

    tableViewFields.table.removeAll();
    if (meta.getLevels().size() > 0) {
      for (PaloDimensionLevel level : meta.getLevels()) {
    	String consolidationFactor = level.getConsolidationFieldName();
        if (consolidationFactor == null)
        	consolidationFactor = BaseMessages.getString(PKG, "PaloDimOutputDialog.ConsolidationFactorDefault");
        	
        tableViewFields.add(String.valueOf(level.getLevelNumber()), level.getLevelName(), level.getFieldName(), consolidationFactor);
      }
      tableViewFields.setRowNums();
      tableViewFields.optWidth(true);
    }
    if (meta.getDimension() != null) {
      comboDimension.add(meta.getDimension());
      comboDimension.select(0);
    }
    if (meta.getElementType() != null) {
      comboElementType.add(meta.getElementType());
      comboElementType.select(0);
    }

    comboElementType.setItems(new String[] { "Numeric", "String" });
    comboElementType.select(0);
    if (meta.getElementType() != null) {
      int indexType = comboElementType.indexOf(meta.getElementType());
      if (indexType >= 0)
        comboElementType.select(indexType);
    }

    buttonCreateDimension.setSelection(meta.getCreateNewDimension());
    buttonClearDimension.setSelection(meta.getClearDimension());
    buttonClearConsolidations.setSelection(meta.getClearConsolidations());
    buttonEnableElementCache.setSelection(meta.getEnableElementCache());
    buttonPreloadElementCache.setSelection(meta.getPreloadElementCache());
    buttonRecreateDimension.setSelection(meta.getRecreateDimension());
    
    buttonClearDimension.setEnabled(!buttonRecreateDimension.getSelection());
	buttonClearConsolidations.setEnabled(!buttonRecreateDimension.getSelection());
	buttonPreloadElementCache.setEnabled(buttonEnableElementCache.getSelection());
	
	if (buttonClearDimension.getEnabled())
		buttonClearConsolidations.setEnabled(!buttonClearDimension.getSelection());
	

  }

  private void fillLocalizedData() {
    buttonGetLevels.setText(BaseMessages.getString(PKG, "PaloDimOutputDialog.GetLevels"));
    buttonClearLevels.setText(BaseMessages.getString(PKG, "PaloDimOutputDialog.ClearLevels"));
    labelStepName.setText(BaseMessages.getString(PKG, "PaloDimOutputDialog.StepName"));
    shell.setText(BaseMessages.getString(PKG, "PaloDimOutputDialog.PaloDimOutput"));
    labelDimension.setText(BaseMessages.getString(PKG, "PaloDimOutputDialog.SelectDimension"));
    labelElementType.setText(BaseMessages.getString(PKG, "PaloDimOutputDialog.SelectElementType"));
    labelCreateDimension.setText(BaseMessages.getString(PKG, "PaloDimOutputDialog.CreateNewDimension"));
    labelClearDimension.setText(BaseMessages.getString(PKG, "PaloDimOutputDialog.ClearDimension"));
    labelClearConsolidations.setText(BaseMessages.getString(PKG, "PaloDimOutputDialog.ClearConsolidations"));
    labelRecreateDimension.setText(BaseMessages.getString(PKG, "PaloDimOutputDialog.RecreateDimension"));
    labelEnableElementCache.setText(BaseMessages.getString(PKG, "PaloDimOutputDialog.EnableElementCache"));
    labelPreloadElementCache.setText(BaseMessages.getString(PKG, "PaloDimOutputDialog.PreloadElementCache"));
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
      new ErrorDialog(shell, BaseMessages.getString(PKG, "PaloDimOutputDialog.FailedToSaveDataErrorTitle"), BaseMessages.getString(PKG, "PaloDimOutputDialog.FailedToSaveDataError"), e);
    }
  }

  private void getInfo(PaloDimOutputMeta myMeta) throws KettleException {
    stepname = textStepName.getText();
    List<PaloDimensionLevel> levels = new ArrayList<PaloDimensionLevel>();

    tableViewFields.removeEmptyRows();
    for (int i = 0; i < tableViewFields.table.getItemCount(); i++) {
      String consolidationFactor = tableViewFields.table.getItem(i).getText(4);
      if (consolidationFactor.equals(BaseMessages.getString(PKG, "PaloDimOutputDialog.ConsolidationFactorDefault")))
    		consolidationFactor = null;
    	
      PaloDimensionLevel level = new PaloDimensionLevel(tableViewFields.table.getItem(i).getText(2), i, tableViewFields.table.getItem(i).getText(3), "String",consolidationFactor);
      levels.add(level);
    }
    myMeta.setDatabaseMeta(transMeta.findDatabase(addConnectionLine.getText()));
    myMeta.setLevels(levels);
    myMeta.setCreateNewDimension(this.buttonCreateDimension.getSelection());
    myMeta.setClearDimension(this.buttonClearDimension.getSelection());
    
    if (this.buttonClearDimension.getSelection())
    	myMeta.setClearConsolidations(false);
    else myMeta.setClearConsolidations(buttonClearConsolidations.getSelection());
    
    myMeta.setEnableElementCache(buttonEnableElementCache.getSelection());
    
    if (!this.buttonEnableElementCache.getSelection())
    	myMeta.setPreloadElementCache(false);
    else myMeta.setPreloadElementCache(buttonPreloadElementCache.getSelection());
    
    myMeta.setRecreateDimension(this.buttonRecreateDimension.getSelection());
    myMeta.setDimension(this.comboDimension.getText());
    myMeta.setElementType(this.comboElementType.getText());
    myMeta.setChanged(true);
  }

}
