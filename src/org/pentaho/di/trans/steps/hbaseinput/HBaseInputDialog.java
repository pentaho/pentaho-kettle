/* Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
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

package org.pentaho.di.trans.steps.hbaseinput;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboValuesSelectionListener;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.hbase.mapping.ConfigurationProducer;
import org.pentaho.hbase.mapping.HBaseValueMeta;
import org.pentaho.hbase.mapping.Mapping;
import org.pentaho.hbase.mapping.MappingAdmin;
import org.pentaho.hbase.mapping.MappingEditor;

public class HBaseInputDialog extends BaseStepDialog implements
    StepDialogInterface, ConfigurationProducer {
  
  /** various UI bits and pieces for the dialog */
  private Label m_stepnameLabel;
  private Text m_stepnameText;
  
  // The tabs of the dialog
  private CTabFolder m_wTabFolder;
  private CTabItem m_wConfigTab;/*, m_wFieldsTab, m_wModelTab; */
  
  private CTabItem m_wFilterTab;
  
  private CTabItem m_editorTab;
  
  // Zookeeper host(s) line
  private TextVar m_zookeeperQuorumText;
  
  // Core config line
  private Button m_coreConfigBut;
  private TextVar m_coreConfigText;
  
  // Default config line
  private Button m_defaultConfigBut;
  private TextVar m_defaultConfigText;
  
  private HBaseInputMeta m_currentMeta;
  private HBaseInputMeta m_originalMeta;
  
  // Table name line
  private Button m_mappedTableNamesBut;
  private CCombo m_mappedTableNamesCombo;
  
  // Mapping name line
  private Button m_mappingNamesBut;
  private CCombo m_mappingNamesCombo;
  
  // Key start line
  private TextVar m_keyStartText;
  
  // Key stop line
  private TextVar m_keyStopText;
  
  // Rows to be cached by Scanner
  private TextVar m_scanCacheText;
  
  // Key as a column
//  private Button m_includeKey;
  
  // Key information
  private Label m_keyInfo;
  private Button m_getKeyInfoBut;
  
  // Fields table widget
  private TableView m_fieldsView;
  
  // filters fields widget
  private TableView m_filtersView;
  private ColumnInfo m_filterAliasCI;
  private Button m_matchAllBut;
  private Button m_matchAnyBut;
  
  // mapping editor composite
  private MappingEditor m_mappingEditor;
  
  // cached copy of the mapped columns
  private Map<String, HBaseValueMeta> m_mappedColumns;
  
  // lookup map for indexed columns
  private Map<String, String> m_indexedLookup = new HashMap<String, String>();
  
  protected class TableItemHB extends TableItem {
    protected String m_indexedItems;
    protected String m_tableName;
    protected String m_mappingName;
    protected boolean m_isKey;
    
    public TableItemHB(org.eclipse.swt.widgets.Table table, int mask) {
      super (table, mask);
    }

    protected void checkSubclass() { 
      //   Disable the check that prevents subclassing of SWT components 
    } 

    public void setKey(boolean key) {
      m_isKey = key;
    }
    
    public boolean isKey() {
      return m_isKey;
    }
    
    public void setIndexedItems(String s) {
      m_indexedItems = s;
    }
    
    public String getIndexedItems() {
      return m_indexedItems;
    }
    
    public void setTableName(String t) {
      m_tableName = t;
    }
    
    public String getTableName() {
      return m_tableName;
    }
    
    public void setMappingName(String m) {
      m_mappingName = m;
    }
    
    public String getMappingName() {
      return m_mappingName;
    }
  }
  
  public HBaseInputDialog(Shell parent, Object in,
      TransMeta tr, String name) {
    
    super(parent, (BaseStepMeta)in, tr, name);
    
    m_currentMeta = (HBaseInputMeta)in;
    m_originalMeta = (HBaseInputMeta)m_currentMeta.clone();
  }

  public String open() {
    
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = 
      new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);

    props.setLook(shell);
    setShellImage(shell, m_currentMeta);

    // used to listen to a text field (m_wStepname)
    ModifyListener lsMod = new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          m_currentMeta.setChanged();
        }
      };

    changed = m_currentMeta.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout(formLayout);
    shell.setText(Messages.getString("HBaseInputDialog.Shell.Title"));

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;
    
    // Stepname line
    m_stepnameLabel = new Label(shell, SWT.RIGHT);
    m_stepnameLabel.
      setText(Messages.getString("HBaseInputDialog.StepName.Label"));
    props.setLook(m_stepnameLabel);

    FormData fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    fd.top = new FormAttachment(0, margin);
    m_stepnameLabel.setLayoutData(fd);
    m_stepnameText = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    m_stepnameText.setText(stepname);
    props.setLook(m_stepnameText);
    m_stepnameText.addModifyListener(lsMod);
    
    // format the text field
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(0, margin);
    fd.right = new FormAttachment(100, 0);
    m_stepnameText.setLayoutData(fd);
    
    m_wTabFolder = new CTabFolder(shell, SWT.BORDER);
    props.setLook(m_wTabFolder, Props.WIDGET_STYLE_TAB);
    m_wTabFolder.setSimple(false);

    // Start of the config tab
    m_wConfigTab = new CTabItem(m_wTabFolder, SWT.NONE);
    m_wConfigTab.
      setText(Messages.getString("HBaseInputDialog.ConfigTab.TabTitle"));
    
    Composite wConfigComp = new Composite(m_wTabFolder, SWT.NONE);
    props.setLook(wConfigComp);
    
    FormLayout configLayout = new FormLayout();
    configLayout.marginWidth  = 3;
    configLayout.marginHeight = 3;
    wConfigComp.setLayout(configLayout);
    
    // zookeeper line
    Label zookeeperLab = new Label(wConfigComp, SWT.RIGHT);
    zookeeperLab.setText(Messages.getString("HBaseInputDialog.Zookeeper.Label"));
    zookeeperLab.setToolTipText(Messages.getString("HBaseInputDialog.Zookeeper.TipText"));
    props.setLook(zookeeperLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(0, margin);
    fd.right = new FormAttachment(middle, -margin);
    zookeeperLab.setLayoutData(fd);
    
    m_zookeeperQuorumText = new TextVar(transMeta, wConfigComp, 
        SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_zookeeperQuorumText);
    m_zookeeperQuorumText.addModifyListener(lsMod);
    // set the tool tip to the contents with any env variables expanded
    m_zookeeperQuorumText.addModifyListener(new ModifyListener() {      
      public void modifyText(ModifyEvent e) {
        m_zookeeperQuorumText.
          setToolTipText(transMeta.environmentSubstitute(m_zookeeperQuorumText.getText()));
      }
    });
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(0, 0);
    fd.left = new FormAttachment(middle, 0);
    m_zookeeperQuorumText.setLayoutData(fd);
    
    // core config line
    Label coreConfigLab = new Label(wConfigComp, SWT.RIGHT);
    coreConfigLab.setText(Messages.getString("HBaseInputDialog.CoreConfig.Label"));
    coreConfigLab.setToolTipText(Messages.getString("HBaseInputDialog.CoreConfig.TipText"));
    props.setLook(coreConfigLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_zookeeperQuorumText, margin);
    fd.right = new FormAttachment(middle, -margin);
    coreConfigLab.setLayoutData(fd);
    
    m_coreConfigBut = new Button(wConfigComp, SWT.PUSH | SWT.CENTER);
    props.setLook(m_coreConfigBut);
    m_coreConfigBut.setText(Messages.getString("System.Button.Browse"));
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_zookeeperQuorumText, 0);
    m_coreConfigBut.setLayoutData(fd);
    
    m_coreConfigBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        String[] extensions = null;
        String[] filterNames = null;

        extensions = new String[2];
        filterNames = new String[2];
        extensions[0] = "*.xml";
        filterNames[0] = 
          Messages.getString("HBaseInputDialog.FileType.XML");
        extensions[1] = "*";
        filterNames[1] = 
          Messages.getString("System.FileType.AllFiles");

        dialog.setFilterExtensions(extensions);
        
        if (dialog.open() != null) {
          m_coreConfigText.setText(dialog.getFilterPath() 
              + System.getProperty("file.separator")
              + dialog.getFileName());
        }
        
      }
  });
    
    m_coreConfigText = new TextVar(transMeta, wConfigComp, 
        SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_coreConfigText);
//    m_coreConfigText.setToolTipText(Messages.getString("HBaseInputDialog.CoreConfig.TipText"));
    m_coreConfigText.addModifyListener(lsMod);
    
    // set the tool tip to the contents with any env variables expanded
    m_coreConfigText.addModifyListener(new ModifyListener() {      
      public void modifyText(ModifyEvent e) {
        m_coreConfigText.
          setToolTipText(transMeta.environmentSubstitute(m_coreConfigText.getText()));
       // checkKeyInformation(true);
      }
    });
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_zookeeperQuorumText, margin);
    fd.right = new FormAttachment(m_coreConfigBut, -margin);
    m_coreConfigText.setLayoutData(fd);
    
    // default config line
    Label defaultConfigLab = new Label(wConfigComp, SWT.RIGHT);
    defaultConfigLab.setText(Messages.getString("HBaseInputDialog.DefaultConfig.Label"));
    defaultConfigLab.setToolTipText(Messages.getString("HBaseInputDialog.DefaultConfig.TipText"));
    props.setLook(defaultConfigLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_coreConfigText, margin);
    fd.right = new FormAttachment(middle, -margin);
    defaultConfigLab.setLayoutData(fd);
    
    m_defaultConfigBut = new Button(wConfigComp, SWT.PUSH | SWT.CENTER);
    props.setLook(m_defaultConfigBut);
    m_defaultConfigBut.setText(Messages.getString("System.Button.Browse"));
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_coreConfigText, 0);
    m_defaultConfigBut.setLayoutData(fd);
    
    m_defaultConfigBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        String[] extensions = null;
        String[] filterNames = null;

        extensions = new String[2];
        filterNames = new String[2];
        extensions[0] = "*.xml";
        filterNames[0] = 
          Messages.getString("HBaseInputDialog.FileType.XML");
        extensions[1] = "*";
        filterNames[1] = 
          Messages.getString("System.FileType.AllFiles");

        dialog.setFilterExtensions(extensions);
        
        if (dialog.open() != null) {
          m_defaultConfigText.setText(dialog.getFilterPath() 
              + System.getProperty("file.separator")
              + dialog.getFileName());
        }
        
      }
  });
    
    m_defaultConfigText = new TextVar(transMeta, wConfigComp, 
        SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_defaultConfigText);
    // m_defaultConfigText.setToolTipText(Messages.getString("HBaseInputDialog.DefaultConfig.TipText"));
    m_defaultConfigText.addModifyListener(lsMod);
    
    // set the tool tip to the contents with any env variables expanded
    m_defaultConfigText.addModifyListener(new ModifyListener() {      
      public void modifyText(ModifyEvent e) {
        m_defaultConfigText.
        setToolTipText(transMeta.environmentSubstitute(m_defaultConfigText.getText()));
        //checkKeyInformation(true);
      }
    });
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_coreConfigText, margin);
    fd.right = new FormAttachment(m_defaultConfigBut, -margin);
    m_defaultConfigText.setLayoutData(fd);
    
    // table name
    Label tableNameLab = new Label(wConfigComp, SWT.RIGHT);
    tableNameLab.setText(Messages.getString("HBaseInputDialog.TableName.Label"));
    tableNameLab.setToolTipText(Messages.getString("HBaseInputDialog.TableName.TipText"));
    props.setLook(tableNameLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_defaultConfigText, margin);
    fd.right = new FormAttachment(middle, -margin);
    tableNameLab.setLayoutData(fd);
    
    m_mappedTableNamesBut = new Button(wConfigComp, SWT.PUSH | SWT.CENTER);
    props.setLook(m_mappedTableNamesBut);
    m_mappedTableNamesBut.setText(Messages.getString("HBaseInputDialog.TableName.Button"));
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_defaultConfigText, 0);
    m_mappedTableNamesBut.setLayoutData(fd);        
    // TODO tip text
    
    m_mappedTableNamesCombo = new CCombo(wConfigComp, SWT.BORDER);
    props.setLook(m_mappedTableNamesCombo);
    // TODO set tool tip
    m_mappedTableNamesCombo.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_currentMeta.setChanged();
        m_mappedTableNamesCombo.
          setToolTipText(transMeta.environmentSubstitute(m_mappedTableNamesCombo.getText()));
//        checkKeyInformation(true);
      }
    });
    m_mappedTableNamesCombo.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setupMappingNamesForTable(true);
      }
      
      public void widgetDefaultSelected(SelectionEvent e) {
        setupMappingNamesForTable(true);
      }
    });
    m_mappedTableNamesCombo.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
        
      }
      
      public void focusLost(FocusEvent e) {
        setupMappingNamesForTable(true);
      }
    });
    
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_defaultConfigText, margin);
    fd.right = new FormAttachment(m_mappedTableNamesBut, -margin);
    m_mappedTableNamesCombo.setLayoutData(fd);
    
    m_mappedTableNamesBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setupMappedTableNames();
      }      
    });
            
    // mapping name
    Label mappingNameLab = new Label(wConfigComp, SWT.RIGHT);
    mappingNameLab.setText(Messages.getString("HBaseInputDialog.MappingName.Label"));
    mappingNameLab.setToolTipText(Messages.getString("HBaseInputDialog.MappingName.TipText"));
    props.setLook(mappingNameLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_mappedTableNamesCombo, margin);
    fd.right = new FormAttachment(middle, -margin);
    mappingNameLab.setLayoutData(fd);
    
    m_mappingNamesBut = new Button(wConfigComp, SWT.PUSH | SWT.CENTER);
    props.setLook(m_mappingNamesBut);
    m_mappingNamesBut.setText(Messages.getString("HBaseInputDialog.MappingName.Button"));
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_mappedTableNamesCombo, 0);
    m_mappingNamesBut.setLayoutData(fd);
    // TODO tip text
    
    m_mappingNamesBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setupMappingNamesForTable(false);
      }
    });
    
    m_mappingNamesCombo = new CCombo(wConfigComp, SWT.BORDER);
    props.setLook(m_mappingNamesCombo);
    // TODO set tool tip
    m_mappingNamesCombo.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_currentMeta.setChanged();
        //checkKeyInformation(true);
        
        m_mappingNamesCombo.setToolTipText(transMeta.
            environmentSubstitute(m_mappingNamesCombo.getText()));
      }
    });
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_mappedTableNamesCombo, margin);
    fd.right = new FormAttachment(m_mappingNamesBut, -margin);
    m_mappingNamesCombo.setLayoutData(fd);
    
    // keystart
    Label keyStartLab = new Label(wConfigComp, SWT.RIGHT);
    keyStartLab.setText(Messages.getString("HBaseInputDialog.KeyStart.Label"));
    keyStartLab.setToolTipText(Messages.getString("HBaseInputDialog.KeyStart.TipText"));
    props.setLook(keyStartLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_mappingNamesCombo, margin);
    fd.right = new FormAttachment(middle, -margin);
    keyStartLab.setLayoutData(fd);
    
    m_keyStartText = new TextVar(transMeta, wConfigComp, 
        SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    m_keyStartText.setToolTipText(Messages.getString("HBaseInputDialog.KeyStart.TipText"));
    m_keyStartText.addModifyListener(lsMod);
    props.setLook(m_keyStartText);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_mappingNamesCombo, margin);
    m_keyStartText.setLayoutData(fd);
    
    // keystop
    Label keyStopLab = new Label(wConfigComp, SWT.RIGHT);
    keyStopLab.setText(Messages.getString("HBaseInputDialog.KeyStop.Label"));
    keyStopLab.setToolTipText(Messages.getString("HBaseInputDialog.KeyStop.TipText"));
    props.setLook(keyStopLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_keyStartText, margin);
    fd.right = new FormAttachment(middle, -margin);
    keyStopLab.setLayoutData(fd);
    
    m_keyStopText = new TextVar(transMeta, wConfigComp, 
        SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    m_keyStopText.setToolTipText(Messages.getString("HBaseInputDialog.KeyStop.TipText"));
    m_keyStopText.addModifyListener(lsMod);
    props.setLook(m_keyStopText);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_keyStartText, margin);
    m_keyStopText.setLayoutData(fd);
    
    // Scanner caching
    Label scannerCacheLab = new Label(wConfigComp, SWT.RIGHT);
    scannerCacheLab.setText(Messages.getString("HBaseInputDialog.ScannerCache.Label"));
    scannerCacheLab.setToolTipText(Messages.getString("HBaseInputDialog.ScannerCache.TipText"));
    props.setLook(scannerCacheLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_keyStopText, margin);
    fd.right = new FormAttachment(middle, -margin);
    scannerCacheLab.setLayoutData(fd);
    
    m_scanCacheText = new TextVar(transMeta, wConfigComp,
        SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    m_scanCacheText.setToolTipText(Messages.getString("HBaseInputDialog.ScannerCache.TipText"));
    props.setLook(m_scanCacheText);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_keyStopText, margin);
    m_scanCacheText.setLayoutData(fd);
    
    // key as a column
/*    Label includeKeyLab = new Label(wConfigComp, SWT.RIGHT);
    includeKeyLab.setText(Messages.getString("HBaseInputDialog.IncludeKey.Label"));
    props.setLook(includeKeyLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_keyStopText, margin);
    fd.right = new FormAttachment(middle, -margin);
    includeKeyLab.setLayoutData(fd);
    
    m_includeKey = new Button(wConfigComp, SWT.CHECK);
    props.setLook(m_includeKey);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_keyStopText, margin);
    m_includeKey.setLayoutData(fd);
    m_includeKey.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        m_currentMeta.setChanged();
      }
    }); */
    
    
    m_getKeyInfoBut = new Button(wConfigComp, SWT.PUSH);
    m_getKeyInfoBut.setText("Get Key/Fields Info");
    props.setLook(m_getKeyInfoBut);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    //fd.top = new FormAttachment(m_keyStopText, 0);
    fd.bottom = new FormAttachment(100, -margin*2);
    m_getKeyInfoBut.setLayoutData(fd);
    m_getKeyInfoBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        checkKeyInformation(false, true);
      }
    });
    
    
    Group keyGroup = new Group(wConfigComp, SWT.SHADOW_ETCHED_IN);
    //keyGroup.setText("Table key information");
    FormLayout keyLayout = new FormLayout();
    //keyLayout.marginWidth = 3; keyLayout.marginHeight = 3;
    keyGroup.setLayout(keyLayout);
    props.setLook(keyGroup);
    
    m_keyInfo = new Label(keyGroup, SWT.RIGHT);
    m_keyInfo.setText("-- Key details --");
    props.setLook(m_keyInfo);
    fd = new FormData();
    fd.top = new FormAttachment(0, margin);
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, -margin);
    m_keyInfo.setLayoutData(fd);
        
    
    fd = new FormData();
    //fd.top = new FormAttachment(m_keyStopText, margin);
    fd.right = new FormAttachment(m_getKeyInfoBut, -margin);
    fd.left = new FormAttachment(middle, 0);
    fd.bottom = new FormAttachment(100, -margin*2);
    keyGroup.setLayoutData(fd);
    
    // fields stuff
    ColumnInfo[] colinf=new ColumnInfo[] {
        new ColumnInfo(Messages.getString("HBaseInputDialog.Fields.FIELD_ALIAS"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(Messages.getString("HBaseInputDialog.Fields.FIELD_KEY"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(Messages.getString("HBaseInputDialog.Fields.FIELD_FAMILY"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(Messages.getString("HBaseInputDialog.Fields.FIELD_NAME"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(Messages.getString("HBaseInputDialog.Fields.FIELD_TYPE"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(Messages.getString("HBaseInputDialog.Fields.FIELD_FORMAT"), ColumnInfo.COLUMN_TYPE_FORMAT, 3), 
        new ColumnInfo(Messages.getString("HBaseInputDialog.Fields.FIELD_INDEXED"), ColumnInfo.COLUMN_TYPE_TEXT, false),};
    
      colinf[0].setReadOnly(true);
      colinf[1].setReadOnly(true);
      colinf[2].setReadOnly(true);
      colinf[3].setReadOnly(true);
      colinf[4].setReadOnly(true);
      colinf[5].setReadOnly(true);

    colinf[5].setComboValuesSelectionListener(new ComboValuesSelectionListener() {

      public String[] getComboValues(TableItem tableItem, int rowNr, int colNr) {
        String[] comboValues = new String[] { };
        int type = ValueMeta.getType( tableItem.getText(colNr-1) );
        switch(type) {
        case ValueMetaInterface.TYPE_DATE: comboValues = Const.getDateFormats(); break;
        case ValueMetaInterface.TYPE_INTEGER: 
        case ValueMetaInterface.TYPE_BIGNUMBER:
        case ValueMetaInterface.TYPE_NUMBER: comboValues = Const.getNumberFormats(); break;
        default: break;
        }
        return comboValues;
      }
    });
    
    m_fieldsView = new TableView(transMeta, wConfigComp,
        SWT.FULL_SELECTION | SWT.MULTI,
        colinf,
        1,
        lsMod,
        props);

    
    fd = new FormData();
    //fd.top   = new FormAttachment(keyGroup, margin*2);
    fd.top   = new FormAttachment(m_scanCacheText, margin*2);
    //fd.bottom= new FormAttachment(100, -margin*2);
    fd.bottom= new FormAttachment(m_getKeyInfoBut, -margin*2);
    fd.left  = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    m_fieldsView.setLayoutData(fd);
    
    
    
    
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    fd.bottom = new FormAttachment(100, 0);
    wConfigComp.setLayoutData(fd);
    
    wConfigComp.layout();
    m_wConfigTab.setControl(wConfigComp);
    
    
    
    
    
    
    // ----- Start of the filter tab --------
    m_wFilterTab = new CTabItem(m_wTabFolder, SWT.NONE);
    m_wFilterTab.
      setText(Messages.getString("HBaseInputDialog.FilterTab.TabTitle"));
    
    Composite wFilterComp = new Composite(m_wTabFolder, SWT.NONE);
    props.setLook(wFilterComp);
    
    FormLayout filterLayout = new FormLayout();
    filterLayout.marginWidth  = 3;
    filterLayout.marginHeight = 3;
    wFilterComp.setLayout(filterLayout);
    

    m_matchAllBut = new Button(wFilterComp, SWT.RADIO);
    m_matchAllBut.setText(Messages.getString("HBaseInputDialog.Filters.RADIO_ALL"));
    props.setLook(m_matchAllBut);
    fd = new FormData();
    fd.top = new FormAttachment(0, 0);
    fd.left = new FormAttachment(0, 0);
//    fd.right = new FormAttachment(100, -margin);
    m_matchAllBut.setLayoutData(fd);
    m_matchAllBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        m_currentMeta.setChanged();
      }
    });
    
    m_matchAnyBut = new Button(wFilterComp, SWT.RADIO);
    m_matchAnyBut.setText(Messages.getString("HBaseInputDialog.Filters.RADIO_ANY"));
    props.setLook(m_matchAnyBut);
    fd = new FormData();
    fd.top = new FormAttachment(0, 0);
    fd.left = new FormAttachment(m_matchAllBut, 0);
    fd.right = new FormAttachment(100, -margin);
    m_matchAnyBut.setLayoutData(fd);
    m_matchAnyBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        m_currentMeta.setChanged();
      }
    });
    
    m_matchAllBut.setSelection(true);

    final ColumnInfo[] colinf2 = new ColumnInfo[] {
        new ColumnInfo(Messages.getString("HBaseInputDialog.Filters.FIELD_ALIAS") + "     ", ColumnInfo.COLUMN_TYPE_CCOMBO, false),
        /*new ColumnInfo(Messages.getString("HBaseInputDialog.Filters.FIELD_FAMILY"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(Messages.getString("HBaseInputDialog.Filters.FIELD_NAME"), ColumnInfo.COLUMN_TYPE_TEXT, false),*/
        new ColumnInfo(Messages.getString("HBaseInputDialog.Filters.FIELD_TYPE"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(Messages.getString("HBaseInputDialog.Filters.FIELD_OPERATOR"), ColumnInfo.COLUMN_TYPE_CCOMBO, false), 
        new ColumnInfo(Messages.getString("HBaseInputDialog.Filters.FIELD_COMPARISON"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(Messages.getString("HBaseInputDialog.Filters.FIELD_FORMAT"), ColumnInfo.COLUMN_TYPE_CCOMBO, false),
        new ColumnInfo(Messages.getString("HBaseInputDialog.Filters.FIELD_SIGNED"), ColumnInfo.COLUMN_TYPE_CCOMBO, false), };

    colinf2[0].setReadOnly(false);
/*    colinf2[1].setReadOnly(true);
    colinf2[2].setReadOnly(true); */
    colinf2[1].setReadOnly(true);
    colinf2[2].setReadOnly(true);
    colinf2[3].setReadOnly(false);
    colinf2[4].setReadOnly(false);
    colinf2[5].setReadOnly(true);
    
    m_filterAliasCI = colinf2[0];
    m_filterAliasCI.setComboValues(new String[] {""});
    colinf2[2].setComboValues(ColumnFilter.getAllOperators());
    colinf2[5].setComboValues(new String[] {"Y", "N"});
    
    colinf2[2].setComboValuesSelectionListener(new ComboValuesSelectionListener() {

      public String[] getComboValues(TableItem tableItem, int rowNr, int colNr) {
        String[] comboValues = colinf2[2].getComboValues();
        
        // try to fill in the type
        String alias = tableItem.getText(1);
        HBaseValueMeta vm = null;
        if (!Const.isEmpty(alias)) {
          vm = setFilterTableTypeColumn(tableItem);
        }
          
        if (vm != null) {
          if (vm.isNumeric() || vm.isDate() || vm.isBoolean()) {
            comboValues = ColumnFilter.getNumericOperators();
          } else if (vm.isString()) {
            comboValues = ColumnFilter.getStringOperators();
          } else {
            comboValues = new String[1]; comboValues[0] = "";
          }
        }

        return comboValues;
      }
    });
    
    colinf2[4].setComboValuesSelectionListener(new ComboValuesSelectionListener() {
      public String[] getComboValues(TableItem tableItem, int rowNr, int colNr) {
        String[] comboValues = new String[] { };
        
        // try to fill in the type
        String alias = tableItem.getText(1);
        HBaseValueMeta vm = null;
        if (!Const.isEmpty(alias)) {
          vm = setFilterTableTypeColumn(tableItem);
        }
        int type = ValueMeta.getType( tableItem.getText(2));
        switch(type) {
        case ValueMetaInterface.TYPE_DATE: comboValues = Const.getDateFormats(); break;
        case ValueMetaInterface.TYPE_INTEGER: 
        case ValueMetaInterface.TYPE_BIGNUMBER:
        case ValueMetaInterface.TYPE_NUMBER: comboValues = Const.getNumberFormats(); break;
        default: break;
        }
        return comboValues;
      }
    });


    m_filtersView = new TableView(transMeta, wFilterComp,
        SWT.FULL_SELECTION | SWT.MULTI,
        colinf2,
        1,
        lsMod,
        props);

    fd = new FormData();
    fd.top   = new FormAttachment(m_matchAllBut, margin*2);
    fd.bottom= new FormAttachment(100, -margin*2);
    fd.left  = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    m_filtersView.setLayoutData(fd);
    
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    fd.bottom = new FormAttachment(100, 0);
    wFilterComp.setLayoutData(fd);
    
    wFilterComp.layout();
    m_wFilterTab.setControl(wFilterComp);
    
    
    
    // --- mapping editor tab    
    m_editorTab = new CTabItem(m_wTabFolder, SWT.NONE);
    m_editorTab.
      setText(Messages.getString("HBaseInputDialog.MappingEditorTab.TabTitle"));
    
    
    m_mappingEditor = new MappingEditor(shell, m_wTabFolder, this,
        SWT.FULL_SELECTION | SWT.MULTI, false, props, transMeta);
    
    fd = new FormData();
    fd.top = new FormAttachment(0, 0);
    fd.left = new FormAttachment(0, 0);
    m_mappingEditor.setLayoutData(fd);
    
    
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(0, 0);
    fd.bottom= new FormAttachment(100, -margin*2);    
    fd.right = new FormAttachment(100, 0);    
    m_mappingEditor.setLayoutData(fd);
    
    m_mappingEditor.layout();
    m_editorTab.setControl(m_mappingEditor);
    // -----
    
    
    
    
    
    
    
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_stepnameText, margin);
    fd.right = new FormAttachment(100, 0);
    fd.bottom = new FormAttachment(100, -50);
    m_wTabFolder.setLayoutData(fd);
    
    
    // Buttons inherited from BaseStepDialog
    wOK = new Button(shell, SWT.PUSH);
    wOK.setText(Messages.getString("System.Button.OK"));
    
    wCancel=new Button(shell, SWT.PUSH);
    wCancel.setText(Messages.getString("System.Button.Cancel"));
    
    setButtonPositions(new Button[] { wOK, wCancel }, 
                       margin, m_wTabFolder);
    
    // Add listeners
    lsCancel = new Listener() {
        public void handleEvent(Event e) {
          cancel();
        }
      };
      
    lsOK = new Listener() {
        public void handleEvent(Event e) {
          ok();
        }
      };

    wCancel.addListener(SWT.Selection, lsCancel);
    wOK.addListener(SWT.Selection, lsOK);
    
    lsDef = new SelectionAdapter() {
        public void widgetDefaultSelected(SelectionEvent e) {
          ok();
        }
      };
    
    m_stepnameText.addSelectionListener(lsDef);
    
    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
        public void shellClosed(ShellEvent e) {
          cancel();
        }
      });
    
    
    m_wTabFolder.setSelection(0);    
    setSize();
    
    getData();
    
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }

    return stepname;    
  }
  
  protected HBaseValueMeta setFilterTableTypeColumn(TableItem tableItem) {
    // try to fill in the type
    String alias = tableItem.getText(1).trim();
    if (!Const.isEmpty(alias)) {
      // try using the mapping information first since it is complete
      if (m_mappedColumns != null) {        
        HBaseValueMeta vm = m_mappedColumns.get(transMeta.environmentSubstitute(alias));
        if (vm != null) {
          String type = ValueMeta.getTypeDesc(vm.getType());
          tableItem.setText(2, type);
          return vm;
        }
      } else if (m_currentMeta.getOutputFields() != null &&
          m_currentMeta.getOutputFields().size() > 0) {
        
        // use the user-selected fields information
        for (HBaseValueMeta vm : m_currentMeta.getOutputFields()) {
          String aliasF = vm.getAlias();
          if (alias.equals(aliasF)) {
            String type = ValueMeta.getTypeDesc(vm.getType());
            tableItem.setText(2, type);
            return vm;
          }
        }
      }
    }
    
    return null;
  }
  
  protected void ok() {
    if (Const.isEmpty(m_stepnameText.getText())) {
      return;
    }
    
    stepname = m_stepnameText.getText();
    
    m_currentMeta.setZookeeperHosts(m_zookeeperQuorumText.getText());
    m_currentMeta.setCoreConfigURL(m_coreConfigText.getText());    
    m_currentMeta.setDefaulConfigURL(m_defaultConfigText.getText());
    m_currentMeta.setSourceTableName(m_mappedTableNamesCombo.getText());
    m_currentMeta.setSourceMappingName(m_mappingNamesCombo.getText());
    m_currentMeta.setKeyStartValue(m_keyStartText.getText());
    m_currentMeta.setKeyStopValue(m_keyStopText.getText());
    m_currentMeta.setScannerCacheSize(m_scanCacheText.getText());
    m_currentMeta.setMatchAnyFilter(m_matchAnyBut.getSelection());
    
//    m_currentMeta.setOutputKeyAsField(m_includeKey.getSelection());
    
    int numNonEmpty = m_fieldsView.nrNonEmpty();
    if (numNonEmpty > 0) {
      List<HBaseValueMeta> outputFields = new ArrayList<HBaseValueMeta>();
      
      for (int i = 0; i < numNonEmpty; i++) {
        TableItem item = m_fieldsView.getNonEmpty(i);
        String alias = item.getText(1).trim();
        String isKey = item.getText(2).trim();
        String family = item.getText(3).trim();
        String column = item.getText(4).trim();
        String type = item.getText(5).trim();
        String format = item.getText(6).trim();
//        String indexed = item.getText(7).trim();
/*        String tableName = item.getText(7);
        String mappingName = item.getText(8); */

        HBaseValueMeta vm = new HBaseValueMeta(family + HBaseValueMeta.SEPARATOR +
            column + HBaseValueMeta.SEPARATOR + alias, 
            ValueMeta.getType(type), -1, -1);
        
        vm.setTableName(m_mappedTableNamesCombo.getText());
        vm.setMappingName(m_mappingNamesCombo.getText());
        vm.setKey(isKey.equalsIgnoreCase("Y"));
        String indexItems = m_indexedLookup.get(alias);
        if (indexItems != null) {
          Object[] values = HBaseValueMeta.
            stringIndexListToObjects(indexItems);
          vm.setIndex(values);
          vm.setStorageType(ValueMetaInterface.STORAGE_TYPE_INDEXED);
        }
        vm.setConversionMask(format);
        
        outputFields.add(vm);
      }
      m_currentMeta.setOutputFields(outputFields);
    } else {
      m_currentMeta.setOutputFields(null); // output everything
    }
    
    numNonEmpty = m_filtersView.nrNonEmpty();
    if (numNonEmpty > 0) {
      List<ColumnFilter> filters = new ArrayList<ColumnFilter>();
      
      for (int i = 0; i < m_filtersView.nrNonEmpty(); i++) {
        TableItem item = m_filtersView.getNonEmpty(i);
        String alias = item.getText(1).trim();
        String type = item.getText(2).trim();
        String operator = item.getText(3).trim();
        String comparison = item.getText(4).trim();
        String signed = item.getText(6).trim();
        String format = item.getText(5).trim();
        ColumnFilter f = new ColumnFilter(alias);
        f.setFieldType(type);
        f.setComparisonOperator(ColumnFilter.stringToOpp(operator));
        f.setConstant(comparison);
        f.setSignedComparison(signed.equalsIgnoreCase("Y"));
        f.setFormat(format);
        filters.add(f);
      }
      
      m_currentMeta.setColumnFilters(filters);
    } else {
      m_currentMeta.setColumnFilters(null);
    }
    
    if (!m_originalMeta.equals(m_currentMeta)) {
      m_currentMeta.setChanged();
      changed = m_currentMeta.hasChanged();
    }
        
    
    dispose();
  }
  
  protected void cancel() {
    stepname = null;
    m_currentMeta.setChanged(changed);
    
    dispose();
  }
  
  private void getData() {
    
    if (!Const.isEmpty(m_currentMeta.getZookeeperHosts())) {
      m_zookeeperQuorumText.setText(m_currentMeta.getZookeeperHosts());
    }
    
    if (!Const.isEmpty(m_currentMeta.getCoreConfigURL())) {
      m_coreConfigText.setText(m_currentMeta.getCoreConfigURL());
    }
    
    if (!Const.isEmpty(m_currentMeta.getDefaultConfigURL())) {
      m_defaultConfigText.setText(m_currentMeta.getDefaultConfigURL());
    }
    
    if (!Const.isEmpty(m_currentMeta.getSourceTableName())) {
      m_mappedTableNamesCombo.setText(m_currentMeta.getSourceTableName());
    }
    
    if (!Const.isEmpty(m_currentMeta.getSourceMappingName())) {
      m_mappingNamesCombo.setText(m_currentMeta.getSourceMappingName());
    }
    
    if (!Const.isEmpty(m_currentMeta.getKeyStartValue())) {
      m_keyStartText.setText(m_currentMeta.getKeyStartValue());
    }
    
    if (!Const.isEmpty(m_currentMeta.getKeyStopValue())) {
      m_keyStopText.setText(m_currentMeta.getKeyStopValue());
    }
    
    if (!Const.isEmpty(m_currentMeta.getScannerCacheSize())) {
      m_scanCacheText.setText(m_currentMeta.getScannerCacheSize());
    }
    
    m_matchAnyBut.setSelection(m_currentMeta.getMatchAnyFilter());
    m_matchAllBut.setSelection(!m_currentMeta.getMatchAnyFilter());
    
//    m_includeKey.setSelection(m_currentMeta.getOutputKeyAsField());
    
    // do the key and columns
    checkKeyInformation(true, false);
    
    // filters
    if (m_currentMeta.getColumnFilters() != null && 
        m_currentMeta.getColumnFilters().size() > 0) {
      for (ColumnFilter f : m_currentMeta.getColumnFilters()) {
        TableItem item = new TableItem(m_filtersView.table, SWT.NONE);
        
        if(!Const.isEmpty(f.getFieldAlias())) {
          item.setText(1, f.getFieldAlias());
        }
        if (!Const.isEmpty(f.getFieldType())) {
          item.setText(2, f.getFieldType());
        }
        if (f.getComparisonOperator() != null) {
          item.setText(3, f.getComparisonOperator().toString());
        }
        if (!Const.isEmpty(f.getConstant())) {
          item.setText(4, f.getConstant());
        }
        item.setText(6, (f.getSignedComparison()) ? "Y" : "N");
        if (!Const.isEmpty(f.getFormat())) {
          item.setText(5, f.getFormat());
        }
      }
      
      m_filtersView.removeEmptyRows();
      m_filtersView.setRowNums();
      m_filtersView.optWidth(true);
    }
  }
  
  public Configuration getHBaseConnection() throws IOException {
    Configuration conf = null;
    
    URL coreConf = null;
    URL defaultConf = null;
    String zookeeperHosts = null;
    
    if (!Const.isEmpty(m_coreConfigText.getText())) {
      coreConf = HBaseInputData.stringToURL(transMeta.
          environmentSubstitute(m_coreConfigText.getText()));
    }
    
    if (!Const.isEmpty(m_defaultConfigText.getText())) {
      defaultConf = HBaseInputData.stringToURL(transMeta.
          environmentSubstitute(m_defaultConfigText.getText()));
    }
    
    if (!Const.isEmpty(m_zookeeperQuorumText.getText())) {
      zookeeperHosts = transMeta.environmentSubstitute(m_zookeeperQuorumText.getText());
    }
    
    conf = HBaseInputData.getHBaseConnection(zookeeperHosts, coreConf, defaultConf);
    
    return conf;
  }
  
  private void checkKeyInformation(boolean quiet, boolean readFieldsFromMapping) {
    if ((!Const.isEmpty(m_coreConfigText.getText()) || 
          !Const.isEmpty(m_zookeeperQuorumText.getText())) && 
        !Const.isEmpty(m_mappedTableNamesCombo.getText()) &&
        !Const.isEmpty(m_mappingNamesCombo.getText())) {
      try {
        m_indexedLookup = new HashMap<String, String>();
        
        MappingAdmin admin = new MappingAdmin();
/*        URL coreConf = null;
        URL defaultConf = null;
        
        if (!Const.isEmpty(m_coreConfigText.getText())) {
          coreConf = HBaseInputData.stringToURL(transMeta.
              environmentSubstitute(m_coreConfigText.getText()));
        }
        
        if (!Const.isEmpty(m_defaultConfigText.getText())) {
          defaultConf = HBaseInputData.stringToURL(transMeta.
              environmentSubstitute(m_defaultConfigText.getText()));
        } */
        
        Mapping current = null;
        Map<String, HBaseValueMeta> mappedColumns = null;
        String keyName = null;
        String keyType = null;
        boolean filterAliasesDone = false;
        try {
          //Configuration connection = HBaseInputData.getHBaseConnection(coreConf, defaultConf);
          Configuration connection = getHBaseConnection();
          admin.setConnection(connection);
          
          // set the admin on the mapping editor
          //m_mappingEditor.setMappingAdmin(admin);

          current = 
            admin.getMapping(transMeta.environmentSubstitute(m_mappedTableNamesCombo.getText()), 
                transMeta.environmentSubstitute(m_mappingNamesCombo.getText()));
          
          
          // Key information
          keyName = current.getKeyName();
          keyType = current.getKeyType().toString();
          m_keyInfo.setText("HBase Key: " + keyName + " (" + keyType + ")");
          
          mappedColumns = current.getMappedColumns();
          m_mappedColumns = mappedColumns; // cached copy
          
          // Set up the alias combo box in the filters tab
          List<String> filterAliasNames = new ArrayList<String>();
          for (String alias : mappedColumns.keySet()) {
            HBaseValueMeta column = mappedColumns.get(alias);
            String aliasS = column.getAlias();
            if (column.isNumeric() || column.isDate() || column.isString() || column.isBoolean()) {
              filterAliasNames.add(aliasS);
            }
          }
          String[] filterAliasNamesA = filterAliasNames.toArray(new String[1]);
          m_filterAliasCI.setComboValues(filterAliasNamesA);
          filterAliasesDone = true;
        } catch (Exception ex) {
          if (!quiet) {
            new ErrorDialog(shell, 
                Messages.getString("HBaseInputDialog.ErrorMessage.UnableToGetMapping"),
                Messages.getString("HBaseInputDialog.ErrorMessage.UnableToGetMapping") +
            " \"" + transMeta.environmentSubstitute(m_mappedTableNamesCombo.getText() + "," 
            + transMeta.environmentSubstitute(m_mappingNamesCombo.getText()) + "\"")
            , ex);
            ex.printStackTrace();
          }
          m_keyInfo.setText(Messages.getString("HBaseInputDialog.ErrorMessage.UnableToGetMapping"));
        }
                
        
        // Fields information
        m_fieldsView.clearAll(false);
//        boolean keyDone = false;
        if (current != null && 
            (readFieldsFromMapping || m_currentMeta.getOutputFields() == null || 
                m_currentMeta.getOutputFields().size() == 0)) {
          TableItem item = new TableItem(m_fieldsView.table, SWT.NONE);
//          item.setTableName(current.getTableName());
//          item.setMappingName(current.getMappingName());
          item.setText(1, keyName);
  //        item.setKey(true);
          item.setText(2, "Y");
          item.setText(7, "N");
          if (current.getKeyType() == Mapping.KeyType.DATE ||
              current.getKeyType() == Mapping.KeyType.UNSIGNED_DATE) {
            item.setText(5, ValueMeta.getTypeDesc(ValueMetaInterface.TYPE_DATE));            
          } else if (current.getKeyType() == Mapping.KeyType.STRING) {
            item.setText(5, ValueMeta.getTypeDesc(ValueMetaInterface.TYPE_STRING));
          } else {
            item.setText(5, ValueMeta.getTypeDesc(ValueMetaInterface.TYPE_INTEGER));
          }
  //        keyDone = true;
        }
        
        if (!readFieldsFromMapping && m_currentMeta.getOutputFields() != null && 
            m_currentMeta.getOutputFields().size() > 0) {          
          
          // user has selected some fields from the mapping to output
          List<String> filterAliasNames = new ArrayList<String>(); 
          for (HBaseValueMeta column : m_currentMeta.getOutputFields()) {
            TableItem item = new TableItem(m_fieldsView.table, SWT.NONE);
//            item.setTableName(column.getTableName());
//            item.setMappingName(column.getMappingName());            
            
            String aliasS = column.getAlias();
            String type = column.getTypeDesc();
            item.setText(1, aliasS);
            item.setText(5, type);                     
            
            if (column.isKey()) {
  //            item.setKey(true);
              item.setText(2, "Y");
              item.setText(7, "N");
              continue; // skip the rest
            }
            
            item.setText(2, "N");

            if (column.isNumeric() || column.isDate() || column.isString()) {
              if (!filterAliasesDone) {
                filterAliasNames.add(aliasS);
              }
            }
            String family = column.getColumnFamily();
            String name = column.getColumnName();
            String format = column.getConversionMask();

            if (column.getStorageType() == ValueMetaInterface.STORAGE_TYPE_INDEXED) {              
              String valuesString = HBaseValueMeta.objectIndexValuesToString(column.getIndex());
//              item.setIndexedItems(valuesString);
              m_indexedLookup.put(aliasS, valuesString);
              item.setText(7, "Y");
            } else {
              item.setText(7, "N");
            }
            
            item.setText(3, family);
            item.setText(4, name);
            
            if (!Const.isEmpty(format)) {
              item.setText(6, format);
            }
          }
          
          // set the allowable combo values for the selectable columns in the filter tab
          if (!filterAliasesDone) {
            String[] filterAliasNamesA = filterAliasNames.toArray(new String[1]);
            m_filterAliasCI.setComboValues(filterAliasNamesA);
            filterAliasesDone = true;
          }
        } else if (current != null) {
          // get all the fields from the mapping
          
          for (String alias : mappedColumns.keySet()) {
            HBaseValueMeta column = mappedColumns.get(alias);
            String aliasS = column.getAlias();
            String family = column.getColumnFamily();
            String name = column.getColumnName();
            String type = column.getTypeDesc();
            String format = column.getConversionMask();

            TableItem item = new TableItem(m_fieldsView.table, SWT.NONE);
//            item.setTableName(column.getTableName());
//            item.setMappingName(column.getMappingName());
            if (column.getStorageType() == ValueMetaInterface.STORAGE_TYPE_INDEXED) {
              String valuesString = HBaseValueMeta.objectIndexValuesToString(column.getIndex());
              //                item.setIndexedItems(valuesString);
              m_indexedLookup.put(aliasS, valuesString);
              item.setText(7, "Y");
            } else {
              item.setText(7, "N");
            }

            item.setText(1, aliasS);
            item.setText(2, "N");
            item.setText(3, family);
            item.setText(4, name);
            item.setText(5, type);
            if (!Const.isEmpty(format)) {
              item.setText(6, format);   
            }
          }
        }
        m_fieldsView.removeEmptyRows();
        m_fieldsView.setRowNums();
        m_fieldsView.optWidth(true);
        
      } catch (Exception ex) {
        if (!quiet) {
          new ErrorDialog(shell, 
              Messages.getString("HBaseInputDialog.ErrorMessage.UnableToGetMapping"),
              Messages.getString("HBaseInputDialog.ErrorMessage.UnableToGetMapping") +
          " \"" + transMeta.environmentSubstitute(m_mappedTableNamesCombo.getText() + "," 
          + transMeta.environmentSubstitute(m_mappingNamesCombo.getText()) + "\"")
          , ex);
        }
        m_keyInfo.setText(Messages.getString("HBaseInputDialog.ErrorMessage.UnableToGetMapping"));
        ex.printStackTrace();
      }
    } else {
      m_keyInfo.setText("");
    }
  }
  
  private void setupMappedTableNames() {
    m_mappedTableNamesCombo.removeAll();
    
    try {
      MappingAdmin admin = new MappingAdmin();
      /*URL coreConf = null;
      URL defaultConf = null;
      
      if (!Const.isEmpty(m_coreConfigText.getText())) {
        coreConf = HBaseInputData.stringToURL(transMeta.
            environmentSubstitute(m_coreConfigText.getText()));        
      }
      
      if (!Const.isEmpty(m_defaultConfigText.getText())) {
        defaultConf = HBaseInputData.
          stringToURL(transMeta.environmentSubstitute(m_defaultConfigText.getText()));
      } */
  
      Configuration connection = getHBaseConnection();
//      Configuration connection = HBaseInputData.getHBaseConnection(coreConf, defaultConf);
      admin.setConnection(connection);
      Set<String> tableNames = admin.getMappedTables();

      for (String s : tableNames) {
        m_mappedTableNamesCombo.add(s);
      }

      
    } catch (Exception e) {
      new ErrorDialog(shell, Messages.getString("HBaseInputDialog.ErrorMessage." +
      		"UnableToConnect"),
          Messages.getString("HBaseInputDialog.ErrorMessage.UnableToConnect"), e);
      e.printStackTrace();
    }
  }
  
  private void setupMappingNamesForTable(boolean quiet) {
    m_mappingNamesCombo.removeAll();
    
    if (!Const.isEmpty(m_mappedTableNamesCombo.getText())) {
      try {      
        MappingAdmin admin = new MappingAdmin();
        /*URL coreConf = null;
        URL defaultConf = null;

        if (!Const.isEmpty(m_coreConfigText.getText())) {
          coreConf = HBaseInputData.stringToURL(transMeta.
              environmentSubstitute(m_coreConfigText.getText()));
        }

        if (!Const.isEmpty(m_defaultConfigText.getText())) {
          defaultConf = HBaseInputData.stringToURL(transMeta.
              environmentSubstitute(m_defaultConfigText.getText()));
        } */

        Configuration connection = getHBaseConnection();
        //Configuration connection = HBaseInputData.getHBaseConnection(coreConf, defaultConf);
        admin.setConnection(connection);
        
        List<String> mappingNames = admin.
          getMappingNames(m_mappedTableNamesCombo.getText().trim());
        
        for (String n : mappingNames) {
          m_mappingNamesCombo.add(n);
        }
      } catch (Exception ex) {
        if (!quiet) {
          new ErrorDialog(shell, Messages.getString("HBaseInputDialog.ErrorMessage." +
          "UnableToConnect"),
          Messages.getString("HBaseInputDialog.ErrorMessage.UnableToConnect"), ex);
          ex.printStackTrace();
        }
      }
    }    
  }
}
