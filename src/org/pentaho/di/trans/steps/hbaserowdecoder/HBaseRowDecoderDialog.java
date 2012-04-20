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

package org.pentaho.di.trans.steps.hbaserowdecoder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.hbase.mapping.Mapping;
import org.pentaho.hbase.mapping.MappingEditor;

/**
 * UI dialog for the HBase row decoder step
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 */
public class HBaseRowDecoderDialog extends BaseStepDialog implements
    StepDialogInterface {
  
  private static final Class<?> PKG = HBaseRowDecoderMeta.class;
  
  /** various UI bits and pieces for the dialog */
  private Label m_stepnameLabel;
  private Text m_stepnameText;
  
  // The tabs of the dialog
  private CTabFolder m_wTabFolder;
  private CTabItem m_wConfigTab;    
  private CTabItem m_editorTab;
  
  private CCombo m_incomingKeyCombo;
  private CCombo m_incomingResultCombo;
  
  // mapping editor composite
  private MappingEditor m_mappingEditor;
  
  private HBaseRowDecoderMeta m_currentMeta;
  private HBaseRowDecoderMeta m_originalMeta;
  
  public HBaseRowDecoderDialog(Shell parent, Object in, 
      TransMeta tr, String name) {
    
    super(parent, (BaseStepMeta)in, tr, name);
    
    m_currentMeta = (HBaseRowDecoderMeta)in;
    m_originalMeta = (HBaseRowDecoderMeta)m_currentMeta.clone();
    
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
    shell.setText(BaseMessages.getString(PKG, "HBaseRowDecoderDialog.Shell.Title"));

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;
    
    // Stepname line
    m_stepnameLabel = new Label(shell, SWT.RIGHT);
    m_stepnameLabel.
      setText(BaseMessages.getString(PKG, "HBaseRowDecoderDialog.StepName.Label"));
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
      setText(BaseMessages.getString(PKG, "HBaseRowDecoderDialog.ConfigTab.TabTitle"));
    
    Composite wConfigComp = new Composite(m_wTabFolder, SWT.NONE);
    props.setLook(wConfigComp);
    
    FormLayout configLayout = new FormLayout();
    configLayout.marginWidth  = 3;
    configLayout.marginHeight = 3;
    wConfigComp.setLayout(configLayout);
    
    // incoming key field line
    Label inKeyLab = new Label(wConfigComp, SWT.RIGHT);
    inKeyLab.setText(BaseMessages.getString(PKG, "HBaseRowDecoderDialog.KeyField.Label"));
    props.setLook(inKeyLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(0, margin);
    fd.right = new FormAttachment(middle, -margin);
    inKeyLab.setLayoutData(fd);
    
    m_incomingKeyCombo = new CCombo(wConfigComp, SWT.BORDER);
    props.setLook(m_incomingKeyCombo);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(0, margin);
    fd.right = new FormAttachment(100, 0);
    m_incomingKeyCombo.setLayoutData(fd);
    
    m_incomingKeyCombo.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_currentMeta.setChanged();
        m_incomingKeyCombo.setToolTipText(transMeta.
            environmentSubstitute(m_incomingKeyCombo.getText()));
      }
    });
    
    // incoming result line
    Label inResultLab = new Label(wConfigComp, SWT.RIGHT);
    inResultLab.setText(BaseMessages.getString(PKG, "HBaseRowDecoderDialog.ResultField.Label"));
    props.setLook(inResultLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_incomingKeyCombo, margin);
    fd.right = new FormAttachment(middle, -margin);
    inResultLab.setLayoutData(fd);
    
    m_incomingResultCombo = new CCombo(wConfigComp, SWT.BORDER);
    props.setLook(m_incomingResultCombo);
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_incomingKeyCombo, margin);
    fd.right = new FormAttachment(100, 0);
    m_incomingResultCombo.setLayoutData(fd);
    
    m_incomingResultCombo.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_currentMeta.setChanged();
        m_incomingResultCombo.setToolTipText(transMeta.
            environmentSubstitute(m_incomingResultCombo.getText()));
      }
    });
    
    populateFieldsCombo();
    
    
    wConfigComp.layout();
    m_wConfigTab.setControl(wConfigComp);
    
    // --- mapping editor tab    
    m_editorTab = new CTabItem(m_wTabFolder, SWT.NONE);
    m_editorTab.
      setText(BaseMessages.getString(PKG, "HBaseRowDecoderDialog.MappingEditorTab.TabTitle"));
    
    m_mappingEditor = new MappingEditor(shell, m_wTabFolder, null, null,
        SWT.FULL_SELECTION | SWT.MULTI, false, props, transMeta);
    
    fd = new FormData();
    fd.top = new FormAttachment(0, 0);
    fd.left = new FormAttachment(0, 0);
    fd.bottom= new FormAttachment(100, -margin*2);    
    fd.right = new FormAttachment(100, 0);    
    m_mappingEditor.setLayoutData(fd);
    
    m_mappingEditor.layout();
    m_editorTab.setControl(m_mappingEditor);
    
    
    
    
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_stepnameText, margin);
    fd.right = new FormAttachment(100, 0);
    fd.bottom = new FormAttachment(100, -50);
    m_wTabFolder.setLayoutData(fd);
    
    
    // Buttons inherited from BaseStepDialog
    wOK = new Button(shell, SWT.PUSH);
    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    
    wCancel=new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
    
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
    if (Const.isEmpty(m_stepnameText.getText())) {
      return;
    }
    
    stepname = m_stepnameText.getText();
    
    m_currentMeta.setIncomingKeyField(m_incomingKeyCombo.getText());
    m_currentMeta.setIncomingResultField(m_incomingResultCombo.getText());
    Mapping mapping = m_mappingEditor.getMapping(false);
    if (mapping != null) {
      m_currentMeta.setMapping(mapping);
    }
    
    if (!m_originalMeta.equals(m_currentMeta)) {
      m_currentMeta.setChanged();
      changed = m_currentMeta.hasChanged();
    }
    
    dispose();
  }
  
  protected void getData() {
    if (!Const.isEmpty(m_currentMeta.getIncomingKeyField())) {
      m_incomingKeyCombo.setText(m_currentMeta.getIncomingKeyField());
    }
    
    if (!Const.isEmpty(m_currentMeta.getIncomingResultField())) {
      m_incomingResultCombo.setText(m_currentMeta.getIncomingResultField());
    }
    
    if (m_currentMeta.getMapping() != null) {
      m_mappingEditor.setMapping(m_currentMeta.getMapping());
    }
  }
  
  private void populateFieldsCombo() {
    StepMeta stepMeta = transMeta.findStep(stepname);
    String currentKey = m_incomingKeyCombo.getText();
    String currentResult = m_incomingResultCombo.getText();
    int keyIndex = -1;
    int valueIndex = -1;
    
    if (stepMeta != null) {
      try {
        RowMetaInterface rowMeta = transMeta.getPrevStepFields(stepMeta);
        if (rowMeta != null && rowMeta.size() > 0) {
          m_incomingKeyCombo.removeAll();
          m_incomingResultCombo.removeAll();
          for (int i = 0; i < rowMeta.size(); i++) {
            ValueMetaInterface vm = rowMeta.getValueMeta(i);
            String fieldName = vm.getName();
            if (fieldName.equalsIgnoreCase("key")) {
              keyIndex = i;
            } else if (fieldName.equalsIgnoreCase("value")) {
              valueIndex = i;
            }
            
            m_incomingKeyCombo.add(fieldName);
            m_incomingResultCombo.add(fieldName);
          }
          
          if (!Const.isEmpty(currentKey)) {
            m_incomingKeyCombo.setText(currentKey);            
          } else if (keyIndex >= 0) {
            // auto set key field
            m_incomingKeyCombo.select(keyIndex);
          }
          if (!Const.isEmpty(currentResult)) {
            m_incomingResultCombo.setText(currentResult);
          } else if (valueIndex >= 0) {
            // auto set value (Result) field
            m_incomingResultCombo.select(valueIndex);
          }
        }
      } catch (KettleException ex) {
      }
    }    
  }

}
