/*******************************************************************************
 *
 * Pentaho Big Data
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

package org.pentaho.hbase.mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.io.hfile.Compression;
import org.apache.hadoop.hbase.util.Bytes;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.hbaseinput.HBaseInputData;
import org.pentaho.di.trans.steps.hbaseinput.Messages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboValuesSelectionListener;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;

/**
 * A re-usable composite for creating and editing table mappings for
 * HBase. Also has the (optional) ability to create the table if the
 * table for which the mapping is being created does not exist. When creating
 * a new table, the name supplied may be optionally suffixed with some
 * parameters for compression and bloom filter type. If no parameters are
 * supplied then the HBase defaults of no compression and no bloom filter(s)
 * are used. The table name may be suffixed with 
 * @[NONE | GZ | LZO][@[NONE | ROW | ROWCOL]] for compression and bloom
 * filter type respectively. Note that LZO compression requires LZO libraries
 * to be installed on the HBase nodes. 
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 *
 */
public class MappingEditor extends Composite implements ConfigurationProducer {
  
  protected Shell m_shell;
  protected Composite m_parent;
  
  protected boolean m_allowTableCreate;
  
  protected TextVar m_zookeeperHostText;
  protected TextVar m_zookeeperPortText;
  
  // table name line
  protected CCombo m_existingTableNamesCombo;
  protected boolean m_familiesInvalidated;
  
  // mapping name line
  protected CCombo m_existingMappingNamesCombo;
  
  // fields view
  protected TableView m_fieldsView;
  protected ColumnInfo m_keyCI;
  protected ColumnInfo m_familyCI;
  protected ColumnInfo m_typeCI;
  
  protected Button m_saveBut;
  protected Button m_deleteBut;
  
  protected Button m_getFieldsBut;
  
  protected Button m_keyValueTupleBut;
  
  protected MappingAdmin m_admin;
  
  protected ConfigurationProducer m_configProducer;
  protected FieldProducer m_incomingFieldsProducer;
  
  /** default family name to use when creating a new table using incoming fields */
  protected static final String DEFAULT_FAMILY = "Family1";
  
  protected String m_currentConfiguration = "";
  protected boolean m_connectionProblem;
  
  protected TransMeta m_transMeta;
  
  public MappingEditor(Shell shell, Composite parent, ConfigurationProducer configProducer, 
      FieldProducer fieldProducer, int tableViewStyle, boolean allowTableCreate, 
      PropsUI props, TransMeta transMeta) {
//    super(parent, SWT.NO_BACKGROUND | SWT.NO_FOCUS | SWT.NO_MERGE_PAINTS);
    super(parent, SWT.NONE);
    
    m_shell = shell;
    m_parent = parent;
    m_transMeta = transMeta;
    boolean showConnectWidgets = false;
    m_configProducer = configProducer;
    if (m_configProducer != null) {
      m_currentConfiguration = m_configProducer.getCurrentConfiguration();
    } else {
      showConnectWidgets = true;
      m_configProducer = this;
    }
    
    m_incomingFieldsProducer = fieldProducer;
    
    m_allowTableCreate = allowTableCreate;
    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;
    
    FormLayout controlLayout = new FormLayout();
    /*controlLayout.marginLeft = 0;
    controlLayout.marginRight = 0;
    controlLayout.marginTop = 0;
    controlLayout.marginBottom = 0; */
    controlLayout.marginWidth = 3;
    controlLayout.marginHeight = 3;
    
    setLayout(controlLayout);
    props.setLook(this);
    
    if (showConnectWidgets) {
      Label zooHostLab = new Label(this, SWT.RIGHT);
      zooHostLab.setText("Zookeeper host");
      props.setLook(zooHostLab);
      FormData fd = new FormData();
      fd.left = new FormAttachment(0, 0);
      fd.top = new FormAttachment(0, margin);
      fd.right = new FormAttachment(middle, -margin);
      zooHostLab.setLayoutData(fd);
      
      m_zookeeperHostText = new TextVar(transMeta, this, 
          SWT.SINGLE | SWT.LEFT | SWT.BORDER);
      props.setLook(m_zookeeperHostText);
      fd = new FormData();
      fd.left = new FormAttachment(middle, 0);
      fd.top = new FormAttachment(0, margin);
      fd.right = new FormAttachment(100, 0);
      m_zookeeperHostText.setLayoutData(fd);
      
      Label zooPortLab = new Label(this, SWT.RIGHT);
      zooPortLab.setText("Zookeeper port");
      props.setLook(zooPortLab);
      fd = new FormData();
      fd.left = new FormAttachment(0, 0);
      fd.top = new FormAttachment(m_zookeeperHostText, margin);
      fd.right = new FormAttachment(middle, -margin);
      zooPortLab.setLayoutData(fd);
      
      m_zookeeperPortText = new TextVar(transMeta, this, 
          SWT.SINGLE | SWT.LEFT | SWT.BORDER);
      props.setLook(m_zookeeperPortText);
      fd = new FormData();
      fd.left = new FormAttachment(middle, 0);
      fd.top = new FormAttachment(m_zookeeperHostText, margin);
      fd.right = new FormAttachment(100, 0);
      m_zookeeperPortText.setLayoutData(fd);
      
      m_currentConfiguration = m_configProducer.getCurrentConfiguration();
    }
    
    // table names
    Label tableNameLab = new Label(this, SWT.RIGHT);
    tableNameLab.setText(Messages.getString("MappingDialog.TableName.Label"));
    props.setLook(tableNameLab);
    FormData fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    if (showConnectWidgets) {
      fd.top = new FormAttachment(m_zookeeperPortText, margin);
    } else {
      fd.top = new FormAttachment(0, margin);
    }
    fd.right = new FormAttachment(middle, -margin);
    tableNameLab.setLayoutData(fd);
    
/*    m_existingTableNamesBut = new Button(this, SWT.PUSH | SWT.CENTER);
    props.setLook(m_existingTableNamesBut);
    m_existingTableNamesBut.setText("Get existing table names");
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(0, 0);
    m_existingTableNamesBut.setLayoutData(fd); */
    
    m_existingTableNamesCombo = new CCombo(this, SWT.BORDER);
    props.setLook(m_existingTableNamesCombo);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    if (showConnectWidgets) {
      fd.top = new FormAttachment(m_zookeeperPortText, margin);
    } else {
      fd.top = new FormAttachment(0, margin);
    }
    fd.right = new FormAttachment(100, 0);
    m_existingTableNamesCombo.setLayoutData(fd);
    
    // allow or disallow table creation by enabling/disabling the ability
    // to type into this combo
    m_existingTableNamesCombo.setEditable(m_allowTableCreate);
    
    // mapping names
    Label mappingNameLab = new Label(this, SWT.RIGHT);
    mappingNameLab.setText(Messages.getString("MappingDialog.MappingName.Label"));
    props.setLook(tableNameLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_existingTableNamesCombo, margin);
    fd.right = new FormAttachment(middle, -margin);
    mappingNameLab.setLayoutData(fd);
    
    /*m_existingMappingNamesBut = new Button(this, SWT.PUSH | SWT.CENTER);
    props.setLook(m_existingMappingNamesBut);
    m_existingMappingNamesBut.setText("Get mapping names");
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_existingTableNamesCombo, 0);
    m_existingMappingNamesBut.setLayoutData(fd); */
    
    m_existingMappingNamesCombo = new CCombo(this, SWT.BORDER);
    props.setLook(m_existingMappingNamesCombo);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_existingTableNamesCombo, margin);
    //fd.right = new FormAttachment(m_existingMappingNamesBut, -margin);
    fd.right = new FormAttachment(100, 0);
    m_existingMappingNamesCombo.setLayoutData(fd);
    
    m_existingTableNamesCombo.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        m_familiesInvalidated = true;
        populateMappingComboAndFamilyStuff();
      }
      
      public void widgetDefaultSelected(SelectionEvent e) {
        m_familiesInvalidated = true;
        populateMappingComboAndFamilyStuff();
      }
    });
    
    m_existingTableNamesCombo.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        m_familiesInvalidated = true;
      }
    });
    
    m_existingTableNamesCombo.addFocusListener(new FocusListener() {      
      public void focusGained(FocusEvent e) {
        populateTableCombo(false);
      }
      
      public void focusLost(FocusEvent e) {
        m_familiesInvalidated = true;
        populateMappingComboAndFamilyStuff();
      }
    });
    
    m_existingMappingNamesCombo.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        loadTableViewFromMapping();
      }
      
      public void widgetDefaultSelected(SelectionEvent e) {
        loadTableViewFromMapping();
      }
    });
    
    // fields
    ColumnInfo[] colinf=new ColumnInfo[] {
        new ColumnInfo(Messages.getString("HBaseInputDialog.Fields.FIELD_ALIAS"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(Messages.getString("HBaseInputDialog.Fields.FIELD_KEY"), ColumnInfo.COLUMN_TYPE_CCOMBO, true),
        new ColumnInfo(Messages.getString("HBaseInputDialog.Fields.FIELD_FAMILY"), ColumnInfo.COLUMN_TYPE_CCOMBO, true),
        new ColumnInfo(Messages.getString("HBaseInputDialog.Fields.FIELD_NAME"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(Messages.getString("HBaseInputDialog.Fields.FIELD_TYPE"), ColumnInfo.COLUMN_TYPE_CCOMBO, true),         
        new ColumnInfo(Messages.getString("HBaseInputDialog.Fields.FIELD_INDEXED"), ColumnInfo.COLUMN_TYPE_TEXT, false),};
    
    m_keyCI = colinf[1];
    m_keyCI.setComboValues(new String[] {"N", "Y"});
    m_familyCI = colinf[2];
    m_familyCI.setComboValues(new String[] {""});
    m_typeCI = colinf[4];
    // default types for non-key fields
    m_typeCI.setComboValues(new String[] {"String", "Integer", "Long", "Float", 
        "Double", "Date", "BigNumber", "Serializable", "Binary"});
    
    m_keyCI.setComboValuesSelectionListener(new ComboValuesSelectionListener() {
      public String[] getComboValues(TableItem tableItem, int rowNr, int colNr) {
        
        tableItem.setText(5, "");
        return m_keyCI.getComboValues();
      }
    });
    
    m_typeCI.setComboValuesSelectionListener(new ComboValuesSelectionListener() {
      public String[] getComboValues(TableItem tableItem, int rowNr, int colNr) {
        String[] comboValues = null;
        
        String keyOrNot = tableItem.getText(2);
        if (Const.isEmpty(keyOrNot) || keyOrNot.equalsIgnoreCase("N")) {
          comboValues = new String[] {"String", "Integer", "Long", "Float", 
              "Double", "Boolean", "Date", "BigNumber", "Serializable", "Binary"};
        } else {
          comboValues = new String[] {"String", "Integer", "UnsignedInteger", 
              "Long", "UnsignedLong", "Date", "UnsignedDate", "Binary"};
        }
        
        return comboValues;
      }
    });
    
    m_saveBut = new Button(this, SWT.PUSH | SWT.CENTER);
    props.setLook(m_saveBut);
    m_saveBut.setText(Messages.getString("MappingDialog.SaveMapping"));
    fd = new FormData();
    fd.left = new FormAttachment(0, margin);
    fd.bottom = new FormAttachment(100, -margin*2);
    m_saveBut.setLayoutData(fd);
    
    m_saveBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        saveMapping();
      }
    });
    
    m_deleteBut = new Button(this, SWT.PUSH | SWT.CENTER);
    props.setLook(m_deleteBut);
    m_deleteBut.setText(Messages.getString("MappingDialog.DeleteMapping"));
    fd = new FormData();
    fd.left = new FormAttachment(m_saveBut, margin);
    fd.bottom = new FormAttachment(100, -margin*2);
    m_deleteBut.setLayoutData(fd);
    m_deleteBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        deleteMapping();
      }
    });
    
    if (m_allowTableCreate) {
      m_getFieldsBut = new Button(this, SWT.PUSH | SWT.CENTER);
      props.setLook(m_getFieldsBut);
      m_getFieldsBut.setText(Messages.getString("MappingDialog.GetIncomingFields"));
      fd = new FormData();
      //fd.left = new FormAttachment(0, margin);
      fd.right = new FormAttachment(100, 0);
      fd.bottom = new FormAttachment(100, -margin*2);
      m_getFieldsBut.setLayoutData(fd);
      
      m_getFieldsBut.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          populateTableWithIncomingFields();
        }
      });
    } else {


      m_keyValueTupleBut = new Button(this, SWT.PUSH | SWT.CENTER);
      props.setLook(m_keyValueTupleBut);
      m_keyValueTupleBut.setText(Messages.getString("MappingDialog.KeyValueTemplate"));
      m_keyValueTupleBut.setToolTipText(Messages.getString("MappingDialog.KeyValueTemplate.TipText"));
      fd = new FormData();
      //fd.left = new FormAttachment(0, margin);
      fd.right = new FormAttachment(100, 0);
      fd.bottom = new FormAttachment(100, -margin*2);
      m_keyValueTupleBut.setLayoutData(fd);
      
      m_keyValueTupleBut.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          populateTableWithTupleTemplate();
        }
      });
      
/*      colinf[0].setReadOnly(true);
      colinf[1].setReadOnly(true);
      colinf[2].setReadOnly(true); 
      colinf[4].setReadOnly(true); */
    }
    
    
    
    m_fieldsView = new TableView(transMeta, this, 
        tableViewStyle,
        colinf, 1, null, props);
                        
    fd = new FormData();
    fd.top   = new FormAttachment(m_existingMappingNamesCombo, margin*2);
    fd.bottom= new FormAttachment(m_saveBut, -margin*2);
    fd.left  = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    m_fieldsView.setLayoutData(fd);
    
    
    // --
    //layout();
    //pack();    
  }
  
  private void populateTableWithTupleTemplate() {
    Table table = m_fieldsView.table;
    
    Set<String> existingRowAliases = new HashSet<String>();
    for (int i = 0; i < table.getItemCount(); i++) {
      TableItem tableItem = table.getItem(i);
      String alias = tableItem.getText(1);
      if (!Const.isEmpty(alias)) {
        existingRowAliases.add(alias);
      }
    }
    
    int choice = 0;
    if (existingRowAliases.size() > 0) {
      // Ask what we should do with existing mapping data
      MessageDialog md = new MessageDialog(m_shell, 
          Messages.getString("MappingDialog.GetFieldsChoice.Title"),
          null, Messages.getString("MappingDialog.GetFieldsChoice.Message", 
              "" + existingRowAliases.size(), 
              "" + 5),
          MessageDialog.WARNING, 
          new String[] {
              Messages.getString("MappingOutputDialog.ClearAndAdd"),
              Messages.getString("MappingOutputDialog.Cancel"), }, 
              0);
      MessageDialog.setDefaultImage(GUIResource.getInstance().getImageSpoon());
      int idx = md.open();
      choice = idx & 0xFF;
    }
    
    if (choice == 1 || choice == 255 /* 255 = escape pressed */) {
      return; // Cancel
    }
    
    m_fieldsView.clearAll();
    TableItem item = new TableItem(table, SWT.NONE);
    item.setText(1, "KEY"); item.setText(2, "Y");
    item = new TableItem(table, SWT.NONE);
    item.setText(1, "Family"); item.setText(2, "N");
    item.setText(5, "String");
    item = new TableItem(table, SWT.NONE);
    item.setText(1, "Column"); item.setText(2, "N");
    item = new TableItem(table, SWT.NONE);
    item.setText(1, "Value"); item.setText(2, "N");
    item = new TableItem(table, SWT.NONE);
    item.setText(1, "Timestamp"); item.setText(2, "N");
    item.setText(5, "Long");
    
    m_fieldsView.removeEmptyRows();
    m_fieldsView.setRowNums();
    m_fieldsView.optWidth(true);
  }
  
  private void populateTableWithIncomingFields() {
    if (m_incomingFieldsProducer != null) {
      RowMetaInterface incomingRowMeta = m_incomingFieldsProducer.getIncomingFields();
      
      Table table = m_fieldsView.table;
      if (incomingRowMeta != null) {
        Set<String> existingRowAliases = new HashSet<String>();
        for (int i = 0; i < table.getItemCount(); i++) {
          TableItem tableItem = table.getItem(i);
          String alias = tableItem.getText(1);
          if (!Const.isEmpty(alias)) {
            existingRowAliases.add(alias);
          }
        }
        
        int choice = 0;
        if (existingRowAliases.size() > 0) {
          // Ask what we should do with existing mapping data
          MessageDialog md = new MessageDialog(m_shell, 
              Messages.getString("MappingDialog.GetFieldsChoice.Title"),
              null, Messages.getString("MappingDialog.GetFieldsChoice.Message", 
                  "" + existingRowAliases.size(), 
                  "" + incomingRowMeta.size()),
              MessageDialog.WARNING, 
              new String[] { Messages.getString("MappingDialog.AddNew"),
                  Messages.getString("MappingOutputDialog.Add"), 
                  Messages.getString("MappingOutputDialog.ClearAndAdd"),
                  Messages.getString("MappingOutputDialog.Cancel"), }, 
                  0);
          MessageDialog.setDefaultImage(GUIResource.getInstance().getImageSpoon());
          int idx = md.open();
          choice = idx & 0xFF;
        }
        
        if (choice == 3 || choice == 255 /* 255 = escape pressed */) {
          return; // Cancel
        }
        
        if (choice == 2) {
          m_fieldsView.clearAll();
        }
        
        for (int i = 0; i < incomingRowMeta.size(); i++) {
          ValueMetaInterface vm = incomingRowMeta.getValueMeta(i);
          boolean addIt = true;
          
          if (choice == 0) {
            // only add if its not already in the table
            if (existingRowAliases.contains(vm.getName())) {
              addIt = false;
            }
          }
          
          if (addIt) {
            TableItem item = new TableItem(m_fieldsView.table, SWT.NONE);
            item.setText(1, vm.getName());
            item.setText(2, "N");
            
            if (m_familyCI.getComboValues()[0].length() > 0) {
              // use existing first column family name as the default
              item.setText(3, m_familyCI.getComboValues()[0]);
            } else {
              // default
              item.setText(3, DEFAULT_FAMILY);
            }
            
            item.setText(4, vm.getName());
            item.setText(5, vm.getTypeDesc());
            if (vm.getType() == ValueMetaInterface.TYPE_INTEGER) {
              item.setText(5, "Long");
            }
            if (vm.getType() == ValueMetaInterface.TYPE_NUMBER) {
              item.setText(5, "Double");
            }
            if (vm.getStorageType() == ValueMetaInterface.STORAGE_TYPE_INDEXED) {
              Object[] indexValus = vm.getIndex();
              String indexValsS = HBaseValueMeta.objectIndexValuesToString(indexValus);
              item.setText(6, indexValsS);
            }
          }
        }

        m_fieldsView.removeEmptyRows();
        m_fieldsView.setRowNums();
        m_fieldsView.optWidth(true);
      }
    }
  }
  
  private void populateTableCombo(boolean force) {

    if (m_configProducer == null) {
      return;
    }
    
    if (m_connectionProblem) {
      if (!m_currentConfiguration.equals(m_configProducer.getCurrentConfiguration())) {
        // try again - perhaps the user has corrected connection information
        m_connectionProblem = false;
        m_currentConfiguration = m_configProducer.getCurrentConfiguration();
      }
    }

    if ((m_existingTableNamesCombo.getItemCount() == 0 || force) && 
        !m_connectionProblem) {
      String existingName = m_existingTableNamesCombo.getText();
      m_existingTableNamesCombo.removeAll();
      //m_existingMappingNamesCombo.removeAll();
      try  {
        Configuration conf = m_configProducer.getHBaseConnection();
        MappingAdmin.checkHBaseAvailable(conf);
        
        m_admin = new MappingAdmin();
        m_admin.setConnection(conf);

        HBaseAdmin admin = new HBaseAdmin(conf);
        HTableDescriptor[] tables = admin.listTables();

        for (int i = 0; i < tables.length; i++) {
          String currentTableName = tables[i].getNameAsString();
          m_existingTableNamesCombo.add(currentTableName);
        }
        // restore any previous value
        if (!Const.isEmpty(existingName)) {
          m_existingTableNamesCombo.setText(existingName);
        }
      } catch (MasterNotRunningException m) {
        System.err.println("HBase master does not seem to be running.");
        m.printStackTrace();
        m_connectionProblem = true;
        showConnectionErrorDialog(m);
      } catch (ZooKeeperConnectionException z) {
        System.err.println("Unable to connect to zookeeper.");
        z.printStackTrace();
        m_connectionProblem = true;
        showConnectionErrorDialog(z);
      } catch (Exception ex) {
        ex.printStackTrace();
        m_connectionProblem = true;
        showConnectionErrorDialog(ex);
      }
    }
  }
  
  private void showConnectionErrorDialog(Exception ex) {
    new ErrorDialog(m_shell, 
        Messages.getString("MappingDialog.Error.Title.UnableToConnect"), 
        Messages.getString("MappingDialog.Error.Message.UnableToConnect")
        + "\n\n", ex);
  }
  
  private void deleteMapping() {
    String tableName = "";
    if (!Const.isEmpty(m_existingTableNamesCombo.getText().trim())) {
      tableName = m_existingTableNamesCombo.getText().trim();

      if (tableName.indexOf('@') > 0) {
        tableName = tableName.substring(0, tableName.indexOf('@'));
      }
    }
    if (Const.isEmpty(tableName) ||
        Const.isEmpty(m_existingMappingNamesCombo.getText().trim())) {
      MessageDialog.openError(m_shell, 
          Messages.getString("MappingDialog.Error.Title.MissingTableMappingName"),
          Messages.getString("MappingDialog.Error.Message.MissingTableMappingName"));
      return;
    }
    
    try {
      boolean ok = MessageDialog.openConfirm(m_shell, 
          Messages.getString("MappingDialog.Info.Title.ConfirmDelete"), 
          Messages.getString("MappingDialog.Info.Message.ConfirmDelete",
              m_existingMappingNamesCombo.getText().trim(), 
              tableName));
      
      if (ok) {
        boolean result = m_admin.deleteMapping(m_existingTableNamesCombo.getText().trim(), 
            m_existingMappingNamesCombo.getText().trim());
        if (result) {
          MessageDialog.openConfirm(m_shell,
              Messages.getString("MappingDialog.Info.Title.MappingDeleted"),
              Messages.getString("MappingDialog.Info.Message.MappingDeleted",
                  m_existingMappingNamesCombo.getText().trim(), 
                  tableName));

          // make sure that the list of mappings for the selected table gets updated.
          populateMappingComboAndFamilyStuff();
        } else {
          MessageDialog.openError(m_shell, 
              Messages.getString("MappingDialog.Error.Title.DeleteMapping"),
              Messages.getString("MappingDialog.Error.Message.DeleteMapping", 
                  m_existingMappingNamesCombo.getText().trim(), 
                  tableName));
        }
      }
      return;
    } catch (IOException ex) {
      MessageDialog.openError(m_shell, 
          Messages.getString("MappingDialog.Error.Title.DeleteMapping"),
          Messages.getString("MappingDialog.Error.Message.DeleteMappingIO", 
              m_existingMappingNamesCombo.getText().trim(), 
              tableName, ex.getMessage()));
    }
  }
  
  public Mapping getMapping(boolean checkForMissingTableAndMapping) {
    String tableName = "";
    if (!Const.isEmpty(m_existingTableNamesCombo.getText().trim())) {
      tableName = m_existingTableNamesCombo.getText().trim();

      if (tableName.indexOf('@') > 0) {
        tableName = tableName.substring(0, tableName.indexOf('@'));
      }
    }
    
    if (checkForMissingTableAndMapping && 
        (Const.isEmpty(m_existingTableNamesCombo.getText().trim()) ||
        Const.isEmpty(tableName))) {
      MessageDialog.openError(m_shell, 
          Messages.getString("MappingDialog.Error.Title.MissingTableMappingName"),
          Messages.getString("MappingDialog.Error.Message.MissingTableMappingName"));
      return null;
    }
    
    // do we have any non-empty rows in the table?
    if (m_fieldsView.nrNonEmpty() == 0) {
      MessageDialog.openError(m_shell, 
          Messages.getString("MappingDialog.Error.Title.NoFieldsDefined"), 
          Messages.getString("MappingDialog.Error.Message.NoFieldsDefined"));
      return null;
    }
    // do we have a key defined in the table?
    Mapping theMapping = new Mapping(tableName,
        m_existingMappingNamesCombo.getText().trim());
    boolean keyDefined = false;
    boolean moreThanOneKey = false;
    List<String> missingFamilies = new ArrayList<String>();
    List<String> missingColumnNames = new ArrayList<String>();
    List<String> missingTypes = new ArrayList<String>();
    
    int nrNonEmpty = m_fieldsView.nrNonEmpty();
    
    // is the mapping a tuple mapping?
    boolean isTupleMapping = false;
    int tupleIdCount = 0;
    if (nrNonEmpty == 5) {
      for (int i = 0; i < nrNonEmpty; i++) {
        if (m_fieldsView.getNonEmpty(i).getText(1).equals("KEY") ||
        m_fieldsView.getNonEmpty(i).getText(1).equals("Family") ||
        m_fieldsView.getNonEmpty(i).getText(1).equals("Column") ||
        m_fieldsView.getNonEmpty(i).getText(1).equals("Value") ||
        m_fieldsView.getNonEmpty(i).getText(1).equals("Timestamp")) {
          tupleIdCount++;
        }
      }
    }
/*    if (nrNonEmpty == 5 && m_fieldsView.getNonEmpty(0).getText(1).equals("KEY") &&
        m_fieldsView.getNonEmpty(1).getText(1).equals("Family") &&
        m_fieldsView.getNonEmpty(2).getText(1).equals("Column") &&
        m_fieldsView.getNonEmpty(3).getText(1).equals("Value") && 
        m_fieldsView.getNonEmpty(4).getText(1).equals("Timestamp")) {
      isTupleMapping = true;
      theMapping.setTupleMapping(true);
    } */
    if (tupleIdCount == 5) {
      isTupleMapping = true;
      theMapping.setTupleMapping(true);
    }
    
    for (int i = 0; i < nrNonEmpty; i++) {
      TableItem item = m_fieldsView.getNonEmpty(i);
      boolean isKey = false;
      String alias = null;
      if (!Const.isEmpty(item.getText(1))) {
        alias = item.getText(1).trim();
      }
      if (!Const.isEmpty(item.getText(2))) {
        isKey = item.getText(2).trim().equalsIgnoreCase("Y");
        
        if (isKey && keyDefined) {
          // more than one key, break here
          moreThanOneKey = true;
          break;
        }
        if (isKey) {
          keyDefined = true;
        }
      }
      //String family = null;
      String family = "";
      if (!Const.isEmpty(item.getText(3))) {
        family = item.getText(3);
      } else {
        if (!isKey && !isTupleMapping) {
          missingFamilies.add(item.getText(0));
        }
      }
      //String colName = null;
      String colName = "";
      if (!Const.isEmpty(item.getText(4))) {
        colName = item.getText(4);
      } else {
        if (!isKey && !isTupleMapping) {
          missingColumnNames.add(item.getText(0));
        }
      }
      String type = null;
      if (!Const.isEmpty(item.getText(5))) {
        type = item.getText(5);
      } else {
        missingTypes.add(item.getText(0));
      }
      String indexedVals = null;
      if (!Const.isEmpty(item.getText(6))) {
        indexedVals = item.getText(6);
      }/* else {
        if (!isKey && type != null && type.equalsIgnoreCase("String")) {
          missingIndexedValues.add(item.getText(0));
        }
      }*/
      
      // only add if we have all data and its all correct
      if (isKey && !moreThanOneKey) {
        if (Const.isEmpty(alias)) {
          // pop up an error dialog - key must have an alias because it does not
          // belong to a column family or have a column name
          MessageDialog.openError(m_shell, 
              Messages.getString("MappingDialog.Error.Title.NoAliasForKey"),
              Messages.getString("MappingDialog.Error.Message.NoAliasForKey"));
          return null;
        }
        
        if (Const.isEmpty(type)) {
          // pop up an error dialog - must have a type for the key
          MessageDialog.openError(m_shell, 
              Messages.getString("MappingDialog.Error.Title.NoTypeForKey"),
              Messages.getString("MappingDialog.Error.Message.NoTypeForKey"));
          return null;
        }
        
        if (moreThanOneKey) {
          // popup an error and then return
          MessageDialog.openError(m_shell, 
              Messages.getString("MappingDialog.Error.Title.MoreThanOneKey"),
              Messages.getString("MappingDialog.Error.Message.MoreThanOneKey"));
          return null;
        }
        
        if (isTupleMapping) {
          theMapping.setKeyName(alias);
          theMapping.setTupleFamilies(family);
        } else {
          theMapping.setKeyName(alias);
        }
        try {
          theMapping.setKeyTypeAsString(type);
        } catch (Exception ex) {
          
        }
      } else {
        // don't bother adding if there are any errors
        if (missingFamilies.size() == 0 && missingColumnNames.size() == 0 && 
            missingTypes.size() == 0) {
          String combinedName = family + HBaseValueMeta.SEPARATOR + colName;
          if (!Const.isEmpty(alias)) {
            combinedName += (HBaseValueMeta.SEPARATOR + alias);
          }
          HBaseValueMeta vm = new HBaseValueMeta(combinedName, 0, -1, -1);
          try {
            vm.setHBaseTypeFromString(type);
          } catch (IllegalArgumentException e) {
            // TODO pop up an error dialog for this one
            return null;
          }
          if (vm.isString() && indexedVals != null && indexedVals.length() > 0) {
            Object[] vals = HBaseValueMeta.stringIndexListToObjects(indexedVals);
            vm.setIndex(vals);
            vm.setStorageType(ValueMetaInterface.STORAGE_TYPE_INDEXED);
          }
          
          try {
            theMapping.addMappedColumn(vm, isTupleMapping);
          } catch (Exception ex) {
            // popup an error if this family:column is already in the mapping and
            // then return.
            MessageDialog.openError(m_shell, 
                Messages.getString("MappingDialog.Error.Title.DuplicateColumn"), 
                Messages.getString("MappingDialog.Error.Message1.DuplicateColumn") +
                family + HBaseValueMeta.SEPARATOR + colName + 
                Messages.getString("MappingDialog.Error.Message2.DuplicateColumn"));
            ex.printStackTrace();
            return null;
          }
        }
      }
    }
    
    // now check for any errors in our Lists
    if (!keyDefined) {
      MessageDialog.openError(m_shell,
          Messages.getString("MappingDialog.Error.Title.NoKeyDefined"), 
          Messages.getString("MappingDialog.Error.Message.NoKeyDefined"));
      return null;
    }
    
    if (missingFamilies.size() > 0 || missingColumnNames.size() > 0 || 
        missingTypes.size() > 0) {
      StringBuffer buff = new StringBuffer();
      buff.append(Messages.
          getString("MappingDialog.Error.Message.IssuesPreventingSaving") + ":\n\n");
      if (missingFamilies.size() > 0) {
        buff.append(Messages.getString("MappingDialog.Error.Message.FamilyIssue") + ":\n");
        buff.append(missingFamilies.toString()).append("\n\n");
      }
      if (missingColumnNames.size() > 0) {
        buff.append(Messages.getString("MappingDialog.Error.Message.ColumnIssue") + ":\n");
        buff.append(missingColumnNames.toString()).append("\n\n");
      }
      if (missingTypes.size() > 0) {
        buff.append(Messages.getString("MappingDialog.Error.Message.TypeIssue") + ":\n");
        buff.append(missingTypes.toString()).append("\n\n");
      }
      
      MessageDialog.openError(m_shell, 
          Messages.getString("MappingDialog.Error.Title.IssuesPreventingSaving"), 
          buff.toString());
      return null;
    }
    
    return theMapping;
  }
  
  private void saveMapping() {

    Mapping theMapping = getMapping(true);
    if (theMapping == null) {
      // some problem with the mapping (user will have been informed via dialog)
      return;
    }
    
    String tableName = theMapping.getTableName();
    
    if (m_allowTableCreate) {
      // check for existence of the table. If table doesn't exist
      // prompt for creation
      Configuration conf = m_admin.getConnection();

      try {
        HBaseAdmin admin = new HBaseAdmin(conf);
        //String tableName = m_existingTableNamesCombo.getText().trim();
        if (!admin.tableExists(tableName)) {
          boolean result = MessageDialog.openConfirm(m_shell, "Create table", 
              "Table \"" + tableName + "\" does not exist. Create it?");
          
          if (!result) {
            return;
          }
          
          if (theMapping.getMappedColumns().size() == 0) {
            MessageDialog.openError(m_shell, "No columns defined", 
                "A HBase table requires at least one column family to be defined.");
            return;
          }
          
          // collect up all the column families so that we can create the table
          Set<String> cols = theMapping.getMappedColumns().keySet();
          Set<String> families = new TreeSet<String>();
          for (String col : cols) {
            String family = theMapping.getMappedColumns().get(col).getColumnFamily();
            families.add(family);
          }
          
          // do we have additional parameters supplied in the table name field
          String compression = Compression.Algorithm.NONE.getName();
          String bloomFilter = "NONE";
          String[] opts = m_existingTableNamesCombo.getText().trim().split("@");
          if (opts.length > 1) {
            compression = opts[1];
            if (opts.length == 3) {
              bloomFilter = opts[2];
            }
          }
          
          HTableDescriptor tableDescription = new HTableDescriptor(tableName);
          for (String familyName : families) {
            //HColumnDescriptor colDescriptor = new HColumnDescriptor(familyName);
            HColumnDescriptor colDescriptor = new HColumnDescriptor(Bytes.toBytes(familyName),
                HColumnDescriptor.DEFAULT_VERSIONS, compression, 
                HColumnDescriptor.DEFAULT_IN_MEMORY,
                HColumnDescriptor.DEFAULT_BLOCKCACHE, HColumnDescriptor.DEFAULT_TTL,
                bloomFilter);
            tableDescription.addFamily(colDescriptor);
          }
          
          // create the table
          admin.createTable(tableDescription);
          
          // refresh the table combo
          populateTableCombo(true);
        }
      } catch (IOException ex) {
        new ErrorDialog(m_shell, 
            Messages.getString("MappingDialog.Error.Title.ErrorCreatingTable"), 
            Messages.getString("MappingDialog.Error.Message.ErrorCreatingTable")
            + " \"" + m_existingTableNamesCombo.getText().trim() + "\"", ex);
        return;
      }
    }

    try {
      // now check to see if the mapping exists
      if (m_admin.mappingExists(tableName, 
          m_existingMappingNamesCombo.getText().trim())) {
        // prompt for overwrite
        boolean result = 
          MessageDialog.openConfirm(m_shell,
              Messages.getString("MappingDialog.Info.Title.MappingExists"),
              Messages.getString("MappingDialog.Info.Message1.MappingExists") 
            + m_existingMappingNamesCombo.getText().trim() + 
            Messages.getString("MappingDialog.Info.Message2.MappingExists")
            + tableName 
            + Messages.getString("MappingDialog.Info.Message3.MappingExists"));
        if (!result) {
          return;
        }
        
        // TODO possibly check for consistency against any other mappings
        // for this table?
        
      }
      // finally add the mapping.
      m_admin.putMapping(theMapping, true);
      MessageDialog.openConfirm(m_shell,
          Messages.getString("MappingDialog.Info.Title.MappingSaved"),
          Messages.getString("MappingDialog.Info.Message1.MappingSaved")
          + m_existingMappingNamesCombo.getText().trim() 
          + Messages.getString("MappingDialog.Info.Message2.MappingSaved")
          + tableName 
          + Messages.getString("MappingDialog.Info.Message3.MappingSaved"));
    } catch (IOException ex) {
      // inform the user via popup
      new ErrorDialog(m_shell, 
          Messages.getString("MappingDialog.Error.Title.ErrorSaving"), 
          Messages.getString("MappingDialog.Error.Message.ErrorSaving"), ex);
      ex.printStackTrace();
    }
  }
  
  public void setMapping(Mapping mapping) {
    if (mapping == null) {
      return;
    }
    
    m_fieldsView.clearAll();
    
    // do the key first
    TableItem keyItem = new TableItem(m_fieldsView.table, SWT.NONE);
    keyItem.setText(1, mapping.getKeyName());
    keyItem.setText(2, "Y");
    keyItem.setText(5, mapping.getKeyType().toString());
    if (mapping.isTupleMapping() && !Const.isEmpty(mapping.getTupleFamilies())) {
      keyItem.setText(3, mapping.getTupleFamilies());
    }
    
    // the rest of the fields in the mapping
    Map<String, HBaseValueMeta> mappedFields = mapping.getMappedColumns();
    for (String alias : mappedFields.keySet()) {
      HBaseValueMeta vm = mappedFields.get(alias);
      TableItem item = new TableItem(m_fieldsView.table, SWT.NONE);
      item.setText(1, alias);
      item.setText(2, "N");
      item.setText(3, vm.getColumnFamily());
      item.setText(4, vm.getColumnName());
      
      if (vm.isInteger()) {
        if (vm.getIsLongOrDouble()) {
          item.setText(5, "Long");
        } else {
          item.setText(5, "Integer");
        }
      } else if (vm.isNumber()) {
        if (vm.getIsLongOrDouble()) {
          item.setText(5, "Double");
        } else {
          item.setText(5, "Float");
        }
      }  else {
        item.setText(5, vm.getTypeDesc());
      }
      
      if (vm.getStorageType() == ValueMetaInterface.STORAGE_TYPE_INDEXED) {
        item.setText(6, HBaseValueMeta.objectIndexValuesToString(vm.getIndex()));
      }
    }
    
    m_fieldsView.removeEmptyRows();
    m_fieldsView.setRowNums();
    m_fieldsView.optWidth(true);
  }
  
  private void loadTableViewFromMapping() {
    String tableName = "";
    if (!Const.isEmpty(m_existingTableNamesCombo.getText().trim())) {
      tableName = m_existingTableNamesCombo.getText().trim();

      if (tableName.indexOf('@') > 0) {
        tableName = tableName.substring(0, tableName.indexOf('@'));
      }
    }
    
    try {
      if (m_admin.mappingExists(tableName, 
          m_existingMappingNamesCombo.getText().trim())) {

        Mapping mapping = 
          m_admin.getMapping(tableName, 
              m_existingMappingNamesCombo.getText().trim());
        
        setMapping(mapping);
        
        /*m_fieldsView.clearAll();
        
        // do the key first
        TableItem keyItem = new TableItem(m_fieldsView.table, SWT.NONE);
        keyItem.setText(1, mapping.getKeyName());
        keyItem.setText(2, "Y");
        keyItem.setText(5, mapping.getKeyType().toString());
        
        // the rest of the fields in the mapping
        Map<String, HBaseValueMeta> mappedFields = mapping.getMappedColumns();
        for (String alias : mappedFields.keySet()) {
          HBaseValueMeta vm = mappedFields.get(alias);
          TableItem item = new TableItem(m_fieldsView.table, SWT.NONE);
          item.setText(1, alias);
          item.setText(2, "N");
          item.setText(3, vm.getColumnFamily());
          item.setText(4, vm.getColumnName());
          
          if (vm.isInteger()) {
            if (vm.getIsLongOrDouble()) {
              item.setText(5, "Long");
            } else {
              item.setText(5, "Integer");
            }
          } else if (vm.isNumber()) {
            if (vm.getIsLongOrDouble()) {
              item.setText(5, "Double");
            } else {
              item.setText(5, "Float");
            }
          }  else {
            item.setText(5, vm.getTypeDesc());
          }
          
          if (vm.getStorageType() == ValueMetaInterface.STORAGE_TYPE_INDEXED) {
            item.setText(6, HBaseValueMeta.objectIndexValuesToString(vm.getIndex()));
          }
        }
        
        m_fieldsView.removeEmptyRows();
        m_fieldsView.setRowNums();
        m_fieldsView.optWidth(true); */
      }

    } catch (IOException ex) {
      // inform the user via popup
      new ErrorDialog(m_shell, 
          Messages.getString("MappingDialog.Error.Title.ErrorLoadingMapping"),
          Messages.getString("MappingDialog.Error.Message.ErrorLoadingMapping"), ex);
      ex.printStackTrace();
    }
  }
  
  private void populateMappingComboAndFamilyStuff() {
    String tableName = "";
    if (!Const.isEmpty(m_existingTableNamesCombo.getText().trim())) {
      tableName = m_existingTableNamesCombo.getText().trim();

      if (tableName.indexOf('@') > 0) {
        tableName = tableName.substring(0, tableName.indexOf('@'));
      }
    }
    
    // defaults if we fail to connect, table doesn't exist etc..
    m_familyCI.setComboValues(new String[] {""});
    m_existingMappingNamesCombo.removeAll();
    
    if (m_admin != null && !Const.isEmpty(tableName)) {
      try {
        
        // first get the existing mapping names (if any)
        List<String> mappingNames = 
          m_admin.getMappingNames(tableName);
        for (String m : mappingNames) {
          m_existingMappingNamesCombo.add(m);
        }
        
        // now get family information for this table
        Configuration conf = m_admin.getConnection();
        HBaseAdmin admin = new HBaseAdmin(conf);
        
        if (admin.tableExists(tableName)) {
          HTableDescriptor descriptor = 
            admin.getTableDescriptor(Bytes.toBytes(tableName));
          
          Collection<HColumnDescriptor> families = descriptor.getFamilies();
          String[] familyNames = new String[families.size()];
          int i = 0;
          for (HColumnDescriptor d : families) {
            familyNames[i++] = d.getNameAsString();            
          }
          
          m_familyCI.setComboValues(familyNames);          
        } else {
          m_familyCI.setComboValues(new String[] {""});
        }
        
        m_familiesInvalidated = false;
        return;
        
      } catch (Exception e) {
        // TODO popup error dialog
        e.printStackTrace();
      }
    }    
  }

  public Configuration getHBaseConnection() throws IOException {
    Configuration conf = null;
    String zookeeperHosts = null;
    String zookeeperPort = null;
    
    if (!Const.isEmpty(m_zookeeperHostText.getText())) {
      zookeeperHosts = m_transMeta.environmentSubstitute(m_zookeeperHostText.getText());
    }
    
    if (!Const.isEmpty(m_zookeeperPortText.getText())) {
      zookeeperPort = m_transMeta.environmentSubstitute(m_zookeeperPortText.getText());
    }
    
    conf = HBaseInputData.getHBaseConnection(zookeeperHosts, zookeeperPort, 
        null, null);
    
    return conf;
  }

  public String getCurrentConfiguration() {
    String host = "";
    String port = "";
    if (!Const.isEmpty(m_zookeeperHostText.getText())) {
      host = m_zookeeperHostText.getText();
    }
    if (!Const.isEmpty(m_zookeeperPortText.getText())) {
      port =  m_zookeeperPortText.getText();
    }
    return host + ":" + port;
  }
}
