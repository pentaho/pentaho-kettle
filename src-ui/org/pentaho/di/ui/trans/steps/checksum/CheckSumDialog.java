/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved.
* This software was developed by Pentaho Corporation and is provided under the terms
* of the GNU Lesser General Public License, Version 2.1. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Samatar Hassan
* The Initial Developer is Samatar Hassan
*
* Software distributed under the GNU Lesser Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.*/

/*
 * Created on 18-06-2008
 *
 */

package org.pentaho.di.ui.trans.steps.checksum;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.checksum.CheckSumMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.BaseStepXulDialog;
import org.pentaho.di.ui.trans.step.StepTableDataObject;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.util.AbstractModelList;


public class CheckSumDialog extends BaseStepXulDialog implements StepDialogInterface {
  private static Class<?> PKG = CheckSumMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private CheckSumMeta input;

  private Map<String, Integer> inputFields;

  private List<String> checkSumTypes = new ArrayList<String>();
  private String[] fieldNames;
  private XulMenuList checkTypeCombo, resultTypeCombo;
  private XulTree fieldTable;
  private List<Field> fields;
  private XulTextbox resultName;
  private XulTextbox stepName;

  public CheckSumDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super("org/pentaho/di/ui/trans/steps/checksum/dialog.xul", parent, (BaseStepMeta) in, tr, sname);
    init();
    
    input = (CheckSumMeta) in;
    inputFields = new HashMap<String, Integer>();
  }

  public void init() {
    checkSumTypes = new ArrayList<String>();
    fields = new AbstractModelList<Field>();

    checkSumTypes.add(BaseMessages.getString(PKG, "CheckSumDialog.Type.CRC32"));
    checkSumTypes.add(BaseMessages.getString(PKG, "CheckSumDialog.Type.ADLER32"));
    checkSumTypes.add(BaseMessages.getString(PKG, "CheckSumDialog.Type.MD5"));
    checkSumTypes.add(BaseMessages.getString(PKG, "CheckSumDialog.Type.SHA1"));

    checkTypeCombo = (XulMenuList) document.getElementById("checkType");
    resultTypeCombo = (XulMenuList) document.getElementById("resultType");
    fieldTable = (XulTree) document.getElementById("fieldsTable");
    resultName = (XulTextbox) document.getElementById("resultField");
    stepName = (XulTextbox) document.getElementById("stepName");

    checkTypeCombo.setElements(checkSumTypes);
    resultTypeCombo.setElements(Arrays.asList(CheckSumMeta.resultTypeDesc));
    bf.setBindingType(Binding.Type.ONE_WAY);
    try {
      bf.createBinding(fields, "children", fieldTable, "elements").fireSourceChanged();
    } catch (Exception e) {
      e.printStackTrace();
    }

    final Runnable runnable = new Runnable() {
      public void run() {
        StepMeta stepMeta = transMeta.findStep(stepname);
        if (stepMeta != null) {
          try {
            RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);

            // Remember these fields...
            for (int i = 0; i < row.size(); i++) {
              inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
            }

            document.invokeLater(new Runnable() {
              public void run() {
                setComboBoxes();
                loadFromMeta();
                activeResultType();
              }
            });
          }
          catch (KettleException e) {
            logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
          }
        }
      }
    };
    new Thread(runnable).start();
  }

  private void loadFromMeta() {
    for (int i = 0; i < input.getFieldName().length; i++) {
      fields.add(new Field(input.getFieldName()[i]));
    }

    checkTypeCombo.setSelectedIndex(input.getTypeByDesc());
    if (input.getResultFieldName() != null) {
      resultName.setValue(input.getResultFieldName());
    }
    resultTypeCombo.setSelectedItem(CheckSumMeta.getResultTypeDesc(input.getResultType()));
    stepName.setValue(stepname);
    stepName.selectAll();

    if (input.getFieldName().length == 0) {
      addNewRow();
    }
  }


  public void activeResultType() {
    boolean active = checkTypeCombo.getSelectedIndex() == 2 || checkTypeCombo.getSelectedIndex() == 3;
    resultTypeCombo.setDisabled(!active);
  }

  protected void setComboBoxes() {
    // Something was changed in the row.
    final Map<String, Integer> fields = new HashMap<String, Integer>();

    // Add the currentMeta fields...
    fields.putAll(inputFields);

    Set<String> keySet = fields.keySet();
    List<String> entries = new ArrayList<String>(keySet);
    this.fieldNames = (String[]) entries.toArray(new String[entries.size()]);
    Const.sortStrings(this.fieldNames);
  }

  public void addNewRow() {
    fields.add(new Field());
  }

  public void getFields() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields(stepname);
      if (r != null) {
        TableItemInsertListener insertListener = new TableItemInsertListener() {
          public boolean tableItemInserted( TableItem tableItem, ValueMetaInterface v ) {
            tableItem.setText(2, BaseMessages.getString(PKG, "System.Combo.Yes"));
            return true;
          }
        };
        BaseStepXulDialog.getFieldsFromPrevious(r, fieldTable, fields, new Field(), null);
      }
    }
    catch (KettleException ke) {
      new ErrorDialog(dialogShell, BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Title"),
          BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"), ke);
    }
  }

  @Override
  protected Class<?> getClassForMessages() {
    return PKG;
  }

  public void onCancel() {
    stepname = null;
    input.setChanged(changed);
    dispose();
  }

  public void onAccept() {
    if (Const.isEmpty(stepName.getValue())) {
      return;
    }
    stepname = stepName.getValue(); // return value

    if (checkTypeCombo.getSelectedIndex() < 0) {
      input.setCheckSumType(0);
    } else {
      input.setCheckSumType(checkTypeCombo.getSelectedIndex());
    }

    input.setResultFieldName(resultName.getValue());
    input.setResultType(CheckSumMeta.getResultTypeByDesc(resultTypeCombo.getSelectedItem()));

    input.allocate(fields.size());
    int i = 0;
    for (Field f : fields) {
      input.getFieldName()[i++] = f.getName();
    }
    dispose();
  }


  public class Field extends XulEventSourceAdapter implements StepTableDataObject {
    private String selectedField;
    private Vector fieldOptions = new Vector();

    public Field() {
      buildOptionsList();
    }

    public Field( String name ) {
      this();
      for (String f : fieldNames) {
        if (f.equals(name) == true) {
          setSelectedField(f);
        }
      }
    }

    private void buildOptionsList() {
      if (fieldNames == null) {
        return;
      }
      fieldOptions.clear();
      for (String f : fieldNames) {
        fieldOptions.add(f);
      }
    }

    public Vector getFieldOptions() {
      return fieldOptions;
    }

    public void setSelectedField( String field ) {
      String prevVal = this.selectedField;
      this.selectedField = field;
    }

    public String getSelectedField() {
      return selectedField;
    }

    public String getName() {
      return selectedField;
    }

    public String getDataType() {
      return null;
    }

    public int getLength() {
      return -1;
    }

    public int getPrecision() {
      return -1;
    }

    public StepTableDataObject createNew( ValueMetaInterface val ) {
      return new Field(val.getName());
    }
  }
}
