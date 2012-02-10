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

package org.pentaho.di.trans.steps.hbaseoutput;

import java.io.IOException;
import java.net.URL;
import java.util.List;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.hbase.mapping.ConfigurationProducer;
import org.pentaho.hbase.mapping.FieldProducer;
import org.pentaho.hbase.mapping.MappingAdmin;
import org.pentaho.hbase.mapping.MappingEditor;

/**
 * Dialog class for HBaseOutput
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 *
 */
public class HBaseOutputDialog extends BaseStepDialog implements
  StepDialogInterface, ConfigurationProducer, FieldProducer {

  private HBaseOutputMeta m_currentMeta;
  private HBaseOutputMeta m_originalMeta;
  private HBaseOutputMeta m_configurationMeta;

  /** various UI bits and pieces for the dialog */
  private Label m_stepnameLabel;
  private Text m_stepnameText;

  // The tabs of the dialog
  private CTabFolder m_wTabFolder;
  private CTabItem m_wConfigTab;

  private CTabItem m_editorTab;

  // Zookeeper host(s) line
  private TextVar m_zookeeperQuorumText;
  
  // Zookeeper port
  private TextVar m_zookeeperPortText;

  // Core config line
  private Button m_coreConfigBut;
  private TextVar m_coreConfigText;

  // Default config line
  private Button m_defaultConfigBut;
  private TextVar m_defaultConfigText;

  // Table name line
  private Button m_mappedTableNamesBut;
  private CCombo m_mappedTableNamesCombo;

  // Mapping name line
  private Button m_mappingNamesBut;
  private CCombo m_mappingNamesCombo;

  // Disable write to WAL check box
  private Button m_disableWriteToWALBut;

  // Write buffer size line
  private TextVar m_writeBufferSizeText;

  // mapping editor composite
  private MappingEditor m_mappingEditor;

  public HBaseOutputDialog(Shell parent, Object in,
      TransMeta tr, String name) {

    super(parent, (BaseStepMeta)in, tr, name);
    
    m_currentMeta = (HBaseOutputMeta)in;
    m_originalMeta = (HBaseOutputMeta)m_currentMeta.clone();
    m_configurationMeta = (HBaseOutputMeta)m_currentMeta.clone();
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
    shell.setText(Messages.getString("HBaseOutputDialog.Shell.Title"));

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    m_stepnameLabel = new Label(shell, SWT.RIGHT);
    m_stepnameLabel.
    setText(Messages.getString("HBaseOutputDialog.StepName.Label"));
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
    setText(Messages.getString("HBaseOutputDialog.ConfigTab.TabTitle"));

    Composite wConfigComp = new Composite(m_wTabFolder, SWT.NONE);
    props.setLook(wConfigComp);

    FormLayout configLayout = new FormLayout();
    configLayout.marginWidth  = 3;
    configLayout.marginHeight = 3;
    wConfigComp.setLayout(configLayout);

    // zookeeper line
    Label zookeeperLab = new Label(wConfigComp, SWT.RIGHT);
    zookeeperLab.setText(Messages.getString("HBaseOutputDialog.Zookeeper.Label"));
    zookeeperLab.setToolTipText(Messages.getString("HBaseOutputDialog.Zookeeper.TipText"));
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
    
    // zookeeper port
    Label zookeeperPortLab = new Label(wConfigComp, SWT.RIGHT);
    zookeeperPortLab.setText(Messages.getString("HBaseOutputDialog.ZookeeperPort.Label"));
    props.setLook(zookeeperPortLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_zookeeperQuorumText, margin);
    fd.right = new FormAttachment(middle, -margin);
    zookeeperPortLab.setLayoutData(fd);
    
    m_zookeeperPortText = new TextVar(transMeta, wConfigComp, 
        SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_zookeeperPortText);
    m_zookeeperPortText.addModifyListener(lsMod);
    // set the tool tip to the contents with any env variables expanded
    m_zookeeperPortText.addModifyListener(new ModifyListener() {      
      public void modifyText(ModifyEvent e) {
        m_zookeeperPortText.
        setToolTipText(transMeta.environmentSubstitute(m_zookeeperPortText.getText()));
      }
    });
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_zookeeperQuorumText, margin);
    fd.left = new FormAttachment(middle, 0);
    m_zookeeperPortText.setLayoutData(fd);
    

    // core config line
    Label coreConfigLab = new Label(wConfigComp, SWT.RIGHT);
    coreConfigLab.setText(Messages.getString("HBaseOutputDialog.CoreConfig.Label"));
    coreConfigLab.setToolTipText(Messages.getString("HBaseOutputDialog.CoreConfig.TipText"));
    props.setLook(coreConfigLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_zookeeperPortText, margin);
    fd.right = new FormAttachment(middle, -margin);
    coreConfigLab.setLayoutData(fd);

    m_coreConfigBut = new Button(wConfigComp, SWT.PUSH | SWT.CENTER);
    props.setLook(m_coreConfigBut);
    m_coreConfigBut.setText(Messages.getString("System.Button.Browse"));
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_zookeeperPortText, 0);
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
          Messages.getString("HBaseOutputDialog.FileType.XML");
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
    m_coreConfigText.addModifyListener(lsMod);

    // set the tool tip to the contents with any env variables expanded
    m_coreConfigText.addModifyListener(new ModifyListener() {      
      public void modifyText(ModifyEvent e) {
        m_coreConfigText.
        setToolTipText(transMeta.environmentSubstitute(m_coreConfigText.getText()));
      }
    });
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_zookeeperPortText, margin);
    fd.right = new FormAttachment(m_coreConfigBut, -margin);
    m_coreConfigText.setLayoutData(fd);

    // default config line
    Label defaultConfigLab = new Label(wConfigComp, SWT.RIGHT);
    defaultConfigLab.setText(Messages.getString("HBaseOutputDialog.DefaultConfig.Label"));
    defaultConfigLab.setToolTipText(Messages.getString("HBaseOutputDialog.DefaultConfig.TipText"));
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
    m_defaultConfigText.addModifyListener(lsMod);

    // set the tool tip to the contents with any env variables expanded
    m_defaultConfigText.addModifyListener(new ModifyListener() {      
      public void modifyText(ModifyEvent e) {
        m_defaultConfigText.
        setToolTipText(transMeta.environmentSubstitute(m_defaultConfigText.getText()));
      }
    });
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_coreConfigText, margin);
    fd.right = new FormAttachment(m_defaultConfigBut, -margin);
    m_defaultConfigText.setLayoutData(fd);



    // table name
    Label tableNameLab = new Label(wConfigComp, SWT.RIGHT);
    tableNameLab.setText(Messages.getString("HBaseOutputDialog.TableName.Label"));
    tableNameLab.setToolTipText(Messages.getString("HBaseOutputDialog.TableName.TipText"));
    props.setLook(tableNameLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_defaultConfigText, margin);
    fd.right = new FormAttachment(middle, -margin);
    tableNameLab.setLayoutData(fd);

    m_mappedTableNamesBut = new Button(wConfigComp, SWT.PUSH | SWT.CENTER);
    props.setLook(m_mappedTableNamesBut);
    m_mappedTableNamesBut.setText(Messages.getString("HBaseOutputDialog.TableName.Button"));
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
    mappingNameLab.setText(Messages.getString("HBaseOutputDialog.MappingName.Label"));
    mappingNameLab.setToolTipText(Messages.getString("HBaseOutputDialog.MappingName.TipText"));
    props.setLook(mappingNameLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_mappedTableNamesCombo, margin);
    fd.right = new FormAttachment(middle, -margin);
    mappingNameLab.setLayoutData(fd);

    m_mappingNamesBut = new Button(wConfigComp, SWT.PUSH | SWT.CENTER);
    props.setLook(m_mappingNamesBut);
    m_mappingNamesBut.setText(Messages.getString("HBaseOutputDialog.MappingName.Button"));
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

        m_mappingNamesCombo.setToolTipText(transMeta.
            environmentSubstitute(m_mappingNamesCombo.getText()));
      }
    });
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_mappedTableNamesCombo, margin);
    fd.right = new FormAttachment(m_mappingNamesBut, -margin);
    m_mappingNamesCombo.setLayoutData(fd);


    // disable write to WAL
    Label disableWALLab = new Label(wConfigComp, SWT.RIGHT);
    disableWALLab.setText(Messages.getString("HBaseOutputDialog.DisableWAL.Label"));
    disableWALLab.setToolTipText(Messages.getString("HBaseOutputDialog.DisableWAL.TipText"));
    props.setLook(disableWALLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_mappingNamesCombo, margin);
    fd.right = new FormAttachment(middle, -margin);
    disableWALLab.setLayoutData(fd);
    
    m_disableWriteToWALBut = new Button(wConfigComp, SWT.CHECK | SWT.CENTER);
    m_disableWriteToWALBut.
      setToolTipText(Messages.getString("HBaseOutputDialog.DisableWAL.TipText"));
    props.setLook(m_disableWriteToWALBut);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_mappingNamesCombo, margin);
    //fd.right = new FormAttachment(middle, -margin);
    m_disableWriteToWALBut.setLayoutData(fd);
    
    
    // write buffer size line
    Label writeBufferLab = new Label(wConfigComp, SWT.RIGHT);
    writeBufferLab.setText(Messages.
        getString("HBaseOutputDialog.WriteBufferSize.Label"));
    writeBufferLab.setToolTipText(Messages.
        getString("HBaseOutputDialog.WriteBufferSize.TipText"));
    props.setLook(writeBufferLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_disableWriteToWALBut, margin);
    fd.right = new FormAttachment(middle, -margin);
    writeBufferLab.setLayoutData(fd);
    
    m_writeBufferSizeText = new TextVar(transMeta, wConfigComp, 
        SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_writeBufferSizeText);
    m_writeBufferSizeText.addModifyListener(new ModifyListener() {      
      public void modifyText(ModifyEvent e) {
        m_writeBufferSizeText.
          setToolTipText(transMeta.environmentSubstitute(m_writeBufferSizeText.getText()));
      }
    });
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_disableWriteToWALBut, margin);
    fd.right = new FormAttachment(100, 0);
    m_writeBufferSizeText.setLayoutData(fd);                        
    
    
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    fd.bottom = new FormAttachment(100, 0);
    wConfigComp.setLayoutData(fd);
    
    wConfigComp.layout();
    m_wConfigTab.setControl(wConfigComp);
    
    
    // mapping editor tab
    m_editorTab = new CTabItem(m_wTabFolder, SWT.NONE);
    m_editorTab.
      setText(Messages.getString("HBaseOutputDialog.MappingEditorTab.TabTitle"));
    
    m_mappingEditor = new MappingEditor(shell, m_wTabFolder, this, this,
        SWT.FULL_SELECTION | SWT.MULTI, true, props, transMeta);
    
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
    
    
    
    // -----------------
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
  
  protected void cancel() {
    stepname = null;
    m_currentMeta.setChanged(changed);
    
    dispose();
  }
  
  protected void ok() {
    stepname = m_stepnameText.getText();
    
    updateMetaConnectionDetails(m_currentMeta);
    
    if (!m_originalMeta.equals(m_currentMeta)) {
      m_currentMeta.setChanged();
      changed = m_currentMeta.hasChanged();
    }
            
    dispose();
  }
  
  protected void updateMetaConnectionDetails(HBaseOutputMeta meta) {
    if (Const.isEmpty(m_stepnameText.getText())) {
      return;
    }    
    
    meta.setZookeeperHosts(m_zookeeperQuorumText.getText());
    meta.setZookeeperPort(m_zookeeperPortText.getText());
    meta.setCoreConfigURL(m_coreConfigText.getText());    
    meta.setDefaulConfigURL(m_defaultConfigText.getText());
    meta.setTargetTableName(m_mappedTableNamesCombo.getText());
    meta.setTargetMappingName(m_mappingNamesCombo.getText());
    
    meta.setDisableWriteToWAL(m_disableWriteToWALBut.getSelection());
    meta.setWriteBufferSize(m_writeBufferSizeText.getText());
    

  }
  
  private void getData() {
    if (!Const.isEmpty(m_currentMeta.getZookeeperHosts())) {
      m_zookeeperQuorumText.setText(m_currentMeta.getZookeeperHosts());
    }
    if (!Const.isEmpty(m_currentMeta.getZookeeperPort())) {
      m_zookeeperPortText.setText(m_currentMeta.getZookeeperPort());
    }
    
    if (!Const.isEmpty(m_currentMeta.getCoreConfigURL())) {
      m_coreConfigText.setText(m_currentMeta.getCoreConfigURL());
    }
    
    if (!Const.isEmpty(m_currentMeta.getDefaultConfigURL())) {
      m_defaultConfigText.setText(m_currentMeta.getDefaultConfigURL());
    }
    
    if (!Const.isEmpty(m_currentMeta.getTargetTableName())) {
      m_mappedTableNamesCombo.setText(m_currentMeta.getTargetTableName());
    }
    
    if (!Const.isEmpty(m_currentMeta.getTargetMappingName())) {
      m_mappingNamesCombo.setText(m_currentMeta.getTargetMappingName());
    }
    
    m_disableWriteToWALBut.setSelection(m_currentMeta.getDisableWriteToWAL());
    
    if (!Const.isEmpty(m_currentMeta.getWriteBufferSize())) {
      m_writeBufferSizeText.setText(m_currentMeta.getWriteBufferSize());
    }    
  }

  public Configuration getHBaseConnection() throws IOException {
    Configuration conf = null;

    URL coreConf = null;
    URL defaultConf = null;
    String zookeeperHosts = null;
    String zookeeperPort = null;

    if (!Const.isEmpty(m_coreConfigText.getText())) {
      coreConf = HBaseOutputData.stringToURL(transMeta.
          environmentSubstitute(m_coreConfigText.getText()));
    }

    if (!Const.isEmpty(m_defaultConfigText.getText())) {
      defaultConf = HBaseOutputData.stringToURL(transMeta.
          environmentSubstitute(m_defaultConfigText.getText()));
    }

    if (!Const.isEmpty(m_zookeeperQuorumText.getText())) {
      zookeeperHosts = transMeta.environmentSubstitute(m_zookeeperQuorumText.getText());
    }
    
    if (!Const.isEmpty(m_zookeeperPortText.getText())) {
      zookeeperPort = transMeta.environmentSubstitute(m_zookeeperPortText.getText());
    }

    conf = HBaseOutputData.getHBaseConnection(zookeeperHosts, zookeeperPort, 
        coreConf, defaultConf);

    return conf;
  }

  private void setupMappedTableNames() {
    m_mappedTableNamesCombo.removeAll();

    try {
      MappingAdmin admin = new MappingAdmin();

      Configuration connection = getHBaseConnection();
      admin.setConnection(connection);
      Set<String> tableNames = admin.getMappedTables();

      for (String s : tableNames) {
        m_mappedTableNamesCombo.add(s);
      }


    } catch (Exception ex) {
      new ErrorDialog(shell, Messages.getString("HBaseOutputDialog.ErrorMessage." +
      "UnableToConnect"), Messages.
      getString("HBaseOutputDialog.ErrorMessage.UnableToConnect"), ex);
      ex.printStackTrace();
    }
  }

  private void setupMappingNamesForTable(boolean quiet) {
    m_mappingNamesCombo.removeAll();

    if (!Const.isEmpty(m_mappedTableNamesCombo.getText())) {
      try {      
        MappingAdmin admin = new MappingAdmin();

        Configuration connection = getHBaseConnection();
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
  
  public RowMetaInterface getIncomingFields() {
    StepMeta stepMeta = transMeta.findStep(stepname);
    RowMetaInterface result = null;
    
    try {
      if (stepMeta != null) {
        result = transMeta.getPrevStepFields(stepMeta);
      }
    } catch (KettleException ex) {
      // quiely ignore
    }
    
    return result;
  }
  
  public String getCurrentConfiguration() {
    updateMetaConnectionDetails(m_configurationMeta);
    return m_configurationMeta.getXML();
  }

}
