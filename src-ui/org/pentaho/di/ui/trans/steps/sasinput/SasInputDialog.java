/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.trans.steps.sasinput;

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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.sasinput.SasInputField;
import org.pentaho.di.trans.steps.sasinput.SasInputMeta;
import org.pentaho.di.trans.steps.sasinput.SasInputHelper;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboValuesSelectionListener;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class SasInputDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = SasInputMeta.class; // for i18n purposes, needed
                                                    // by Translator2!!
                                                    // $NON-NLS-1$

  private CCombo          wAccField;

  private SasInputMeta    input;
  private boolean         backupChanged;
  private TableView       wFields;

  public SasInputDialog(Shell parent, Object in, TransMeta tr, String sname) {
    super(parent, (BaseStepMeta) in, tr, sname);
    input = (SasInputMeta) in;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
    props.setLook(shell);
    setShellImage(shell, input);

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        input.setChanged();
      }
    };
    backupChanged = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "SASInputDialog.Dialog.Title")); //$NON-NLS-1$

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label(shell, SWT.RIGHT);
    wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName")); //$NON-NLS-1$
    props.setLook(wlStepname);
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment(0, 0);
    fdlStepname.right = new FormAttachment(middle, -margin);
    fdlStepname.top = new FormAttachment(0, margin);
    wlStepname.setLayoutData(fdlStepname);
    wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wStepname.setText(stepname);
    props.setLook(wStepname);
    wStepname.addModifyListener(lsMod);
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment(middle, 0);
    fdStepname.top = new FormAttachment(0, margin);
    fdStepname.right = new FormAttachment(100, 0);
    wStepname.setLayoutData(fdStepname);
    Control lastControl = wStepname;

    // Which field do we read from?
    //
    Label wlAccField = new Label(shell, SWT.RIGHT);
    wlAccField.setText(BaseMessages.getString(PKG, "SASInputDialog.AcceptField.Label"));
    props.setLook(wlAccField);
    FormData fdlAccField = new FormData();
    fdlAccField.top = new FormAttachment(lastControl, margin);
    fdlAccField.left = new FormAttachment(0, 0);
    fdlAccField.right = new FormAttachment(middle, -margin);
    wlAccField.setLayoutData(fdlAccField);
    wAccField = new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wAccField.setToolTipText(BaseMessages.getString(PKG, "SASInputDialog.AcceptField.Tooltip"));
    props.setLook(wAccField);
    FormData fdAccField = new FormData();
    fdAccField.top = new FormAttachment(lastControl, margin);
    fdAccField.left = new FormAttachment(middle, 0);
    fdAccField.right = new FormAttachment(100, 0);
    wAccField.setLayoutData(fdAccField);
    lastControl = wAccField;

    // Fill in the source fields...
    //
    try {
      RowMetaInterface fields = transMeta.getPrevStepFields(stepMeta);
      wAccField.setItems(fields.getFieldNames());
    } catch (Exception e) {
      LogChannel.GENERAL.logError("Couldn't get input fields for step '" + stepMeta + "'", e);
    }

    // Some buttons
    wOK = new Button(shell, SWT.PUSH);
    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
    wOK.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { ok(); } });
    wGet = new Button(shell, SWT.PUSH);
    wGet.setText(BaseMessages.getString(PKG, "System.Button.GetFields")); //$NON-NLS-1$
    wGet.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { get(); } });
    wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$
    wCancel.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { cancel(); } });

    setButtonPositions(new Button[] { wOK, wGet, wCancel }, margin, null);
    
    
    Label wlFields = new Label(shell, SWT.LEFT);
    wlFields.setText(BaseMessages.getString(PKG, "SASInputDialog.Fields.Label"));
    props.setLook(wlFields);
    FormData fdlFields = new FormData();
    fdlFields.top = new FormAttachment(lastControl, margin);
    fdlFields.left = new FormAttachment(0, 0);
    fdlFields.right = new FormAttachment(100, 0);
    wlFields.setLayoutData(fdlFields);
    lastControl = wlFields;
    
    // Fields
    ColumnInfo[] colinf = new ColumnInfo[] { 
        new ColumnInfo(BaseMessages.getString(PKG, "SASInputDialog.OutputFieldColumn.Name"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(BaseMessages.getString(PKG, "SASInputDialog.OutputFieldColumn.Rename"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(BaseMessages.getString(PKG, "SASInputDialog.OutputFieldColumn.Type"), ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes(), true),
        new ColumnInfo(BaseMessages.getString(PKG, "SASInputDialog.OutputFieldColumn.Mask"), ColumnInfo.COLUMN_TYPE_FORMAT, 2),
        new ColumnInfo(BaseMessages.getString(PKG, "SASInputDialog.OutputFieldColumn.Length"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(BaseMessages.getString(PKG, "SASInputDialog.OutputFieldColumn.Precision"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(BaseMessages.getString(PKG, "SASInputDialog.OutputFieldColumn.Decimal"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(BaseMessages.getString(PKG, "SASInputDialog.OutputFieldColumn.Group"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(BaseMessages.getString(PKG, "SASInputDialog.OutputFieldColumn.TrimType"), ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.trimTypeDesc), };

    colinf[3].setComboValuesSelectionListener(new ComboValuesSelectionListener() {

      public String[] getComboValues(TableItem tableItem, int rowNr, int colNr) {
        String[] comboValues = new String[] {};
        int type = ValueMeta.getType(tableItem.getText(colNr - 1));
        switch (type) {
        case ValueMetaInterface.TYPE_DATE:
          comboValues = Const.getDateFormats();
          break;
        case ValueMetaInterface.TYPE_INTEGER:
        case ValueMetaInterface.TYPE_BIGNUMBER:
        case ValueMetaInterface.TYPE_NUMBER:
          comboValues = Const.getNumberFormats();
          break;
        default:
          break;
        }
        return comboValues;
      }

    });

    wFields = new TableView(transMeta, shell, SWT.FULL_SELECTION | SWT.MULTI, colinf, 1, lsMod, props);

    FormData fdFields = new FormData();
    fdFields.top = new FormAttachment(lastControl, margin * 2);
    fdFields.bottom = new FormAttachment(wOK, -margin * 2);
    fdFields.left = new FormAttachment(0, 0);
    fdFields.right = new FormAttachment(100, 0);
    wFields.setLayoutData(fdFields);

   
    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected(SelectionEvent e) {
        ok();
      }
    };

    wStepname.addSelectionListener(lsDef);

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        cancel();
      }
    });

    getData();
    input.setChanged(changed);

    // Set the shell size, based upon previous time...
    setSize();

    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    return stepname;
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wAccField.setText(Const.NVL(input.getAcceptingField(), ""));

    for (int i=0;i<input.getOutputFields().size();i++) {
      SasInputField field = input.getOutputFields().get(i);
      
      TableItem item = new TableItem(wFields.table, SWT.NONE);
      int colnr=1;
      item.setText(colnr++, Const.NVL(field.getName(), ""));
      item.setText(colnr++, Const.NVL(field.getRename(), ""));
      item.setText(colnr++, ValueMeta.getTypeDesc(field.getType()));
      item.setText(colnr++, Const.NVL(field.getConversionMask(), ""));
      item.setText(colnr++, field.getLength()>=0?Integer.toString(field.getLength()):"") ;
      item.setText(colnr++, field.getPrecision()>=0?Integer.toString(field.getPrecision()):"") ;
      item.setText(colnr++, Const.NVL(field.getDecimalSymbol(), ""));
      item.setText(colnr++, Const.NVL(field.getGroupingSymbol(), ""));
      item.setText(colnr++, Const.NVL(field.getTrimTypeDesc(), ""));
    }
    wFields.removeEmptyRows();
    wFields.setRowNums();
    wFields.optWidth(true);

    wStepname.selectAll();
  }

  private void cancel() {
    stepname = null;
    input.setChanged(backupChanged);
    dispose();
  }

  public void getInfo(SasInputMeta meta) throws KettleStepException {
    // copy info to Meta class (input)
    meta.setAcceptingField(wAccField.getText());
    
    int nrNonEmptyFields = wFields.nrNonEmpty(); 
    meta.getOutputFields().clear();

  for (int i=0;i<nrNonEmptyFields;i++) {
    TableItem item = wFields.getNonEmpty(i);
    
    int colnr=1;
    SasInputField field = new SasInputField();
    field.setName( item.getText(colnr++) );
    field.setRename( item.getText(colnr++) );
    if (Const.isEmpty(field.getRename())) field.setRename(field.getName());
    field.setType( ValueMeta.getType( item.getText(colnr++) ) );
    field.setConversionMask( item.getText(colnr++) );
    field.setLength( Const.toInt(item.getText(colnr++), -1) );
    field.setPrecision( Const.toInt(item.getText(colnr++), -1) );
    field.setDecimalSymbol( item.getText(colnr++) );
    field.setGroupingSymbol( item.getText(colnr++) );
    field.setTrimType(ValueMeta.getTrimTypeByDesc( item.getText(colnr++) ));

    meta.getOutputFields().add(field);
  }
  wFields.removeEmptyRows();
  wFields.setRowNums();
  wFields.optWidth(true);

  }

  private void ok() {
    if (Const.isEmpty(wStepname.getText()))
      return;

    try {
      stepname = wStepname.getText(); // return value
      getInfo(input);
    } catch (KettleStepException e) {
      MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
      mb.setMessage(e.toString());
      mb.setText(BaseMessages.getString(PKG, "System.Warning")); //$NON-NLS-1$
      mb.open();
    }
    dispose();
  }

  public void get() {
    try {
      
      // As the user for a file to use as a reference
      //
      FileDialog dialog = new FileDialog(shell, SWT.OPEN);
      dialog.setFilterExtensions(new String[] {"*.sas7bdat;*.SAS7BDAT", "*.*"});
      dialog.setFilterNames(new String[] {BaseMessages.getString(PKG, "SASInputDialog.FileType.SAS7BAT")+", "+BaseMessages.getString(PKG, "System.FileType.TextFiles"), BaseMessages.getString(PKG, "System.FileType.CSVFiles"), BaseMessages.getString(PKG, "System.FileType.TextFiles"), BaseMessages.getString(PKG, "System.FileType.AllFiles")});
      if (dialog.open()!=null) {
        String filename = dialog.getFilterPath()+System.getProperty("file.separator")+dialog.getFileName();
        SasInputHelper helper = new SasInputHelper(filename);
        BaseStepDialog.getFieldsFromPrevious(
            helper.getRowMeta(), wFields, 1, 
            new int[] { 1 }, new int[] { 3 }, 4, 5, null
            );
      }

      
    } catch(Exception e) {
      new ErrorDialog(shell, "Error", "Error reading information from file", e);
    }
  }
}
