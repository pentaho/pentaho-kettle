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

package org.pentaho.di.trans.steps.cassandraoutput;

import java.util.ArrayList;
import java.util.List;

import org.apache.cassandra.thrift.InvalidRequestException;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Text;
import org.pentaho.cassandra.CassandraColumnMetaData;
import org.pentaho.cassandra.CassandraConnection;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.dialog.ShowMessageDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * Dialog class for the CassandraOutput step
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
public class CassandraOutputDialog extends BaseStepDialog implements
    StepDialogInterface {
  
  private static final Class<?> PKG = CassandraOutputMeta.class;
  
  private CassandraOutputMeta m_currentMeta;
  private CassandraOutputMeta m_originalMeta;
  
  /** various UI bits and pieces for the dialog */
  private Label m_stepnameLabel;
  private Text m_stepnameText;
  
  private Label m_hostLab;
  private TextVar m_hostText;
  private Label m_portLab;
  private TextVar m_portText;
  
  private Label m_userLab;
  private TextVar m_userText;
  private Label m_passLab;
  private TextVar m_passText;
  
  private Label m_keyspaceLab;
  private TextVar m_keyspaceText;
  
  private Label m_columnFamilyLab;
  private CCombo m_columnFamilyCombo;
  private Button m_getColumnFamiliesBut;
  
  private Label m_consistencyLab;
  private TextVar m_consistencyText;
  
  private Label m_batchSizeLab;
  private TextVar m_batchSizeText;
  
  private Label m_keyFieldLab;
  private CCombo m_keyFieldCombo;
  
  private Button m_getFieldsBut;
  
  private Label m_createColumnFamilyLab;
  private Button m_createColumnFamilyBut;
  
  private Label m_truncateColumnFamilyLab;
  private Button m_truncateColumnFamilyBut;
  
  private Label m_updateColumnFamilyMetaDataLab;
  private Button m_updateColumnFamilyMetaDataBut;
  
  private Label m_insertFieldsNotInColumnFamMetaLab;
  private Button m_insertFieldsNotInColumnFamMetaBut;

  private Label m_compressionLab;
  private Button m_useCompressionBut;
  
  private Button m_showSchemaBut;
  
  private Button m_aprioriCQLBut;
  
  private String m_aprioriCQL;
  
  public CassandraOutputDialog(Shell parent, Object in,
      TransMeta tr, String name) {
    
    super(parent, (BaseStepMeta)in, tr, name);
    
    m_currentMeta = (CassandraOutputMeta)in;
    m_originalMeta = (CassandraOutputMeta)m_currentMeta.clone();
  }

  public String open() {

    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = 
      new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);

    props.setLook(shell);
    setShellImage(shell, m_currentMeta);

    // used to listen to a text field (m_wStepname)
    final ModifyListener lsMod = new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          m_currentMeta.setChanged();
        }
      };

    changed = m_currentMeta.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "CassandraOutputDialog.Shell.Title"));
    
    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;
    
    // Stepname line
    m_stepnameLabel = new Label(shell, SWT.RIGHT);
    m_stepnameLabel.
      setText(BaseMessages.getString(PKG, "CassandraOutputDialog.StepName.Label"));
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
    
    // host line
    m_hostLab = new Label(shell, SWT.RIGHT);
    props.setLook(m_hostLab);
    m_hostLab.setText(BaseMessages.getString(PKG, 
        "CassandraOutputDialog.Hostname.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_stepnameText, margin);
    fd.right = new FormAttachment(middle, -margin);
    m_hostLab.setLayoutData(fd);
    
    m_hostText = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_hostText);
    m_hostText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_hostText.setToolTipText(transMeta.environmentSubstitute(m_hostText.getText()));
      }
    });
    m_hostText.addModifyListener(lsMod);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_stepnameText, margin);
    fd.left = new FormAttachment(middle, 0);
    m_hostText.setLayoutData(fd);
    
    // port line
    m_portLab = new Label(shell, SWT.RIGHT);
    props.setLook(m_portLab);
    m_portLab.setText(BaseMessages.getString(PKG, 
        "CassandraOutputDialog.Port.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_hostText, margin);
    fd.right = new FormAttachment(middle, -margin);
    m_portLab.setLayoutData(fd);
    
    m_portText = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_portText);
    m_portText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_portText.setToolTipText(transMeta.environmentSubstitute(m_portText.getText()));
      }
    });
    m_portText.addModifyListener(lsMod);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_hostText, margin);
    fd.left = new FormAttachment(middle, 0);
    m_portText.setLayoutData(fd);
    
    // username line
    m_userLab = new Label(shell, SWT.RIGHT);
    props.setLook(m_userLab);
    m_userLab.setText(BaseMessages.getString(PKG, 
        "CassandraOutputDialog.User.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_portText, margin);
    fd.right = new FormAttachment(middle, -margin);
    m_userLab.setLayoutData(fd);
    
    m_userText = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_userText);
    m_userText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_userText.setToolTipText(transMeta.environmentSubstitute(m_userText.getText()));
      }
    });
    m_userText.addModifyListener(lsMod);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_portText, margin);
    fd.left = new FormAttachment(middle, 0);
    m_userText.setLayoutData(fd);
    
    // password line
    m_passLab = new Label(shell, SWT.RIGHT);
    props.setLook(m_passLab);
    m_passLab.setText(BaseMessages.getString(PKG, 
        "CassandraOutputDialog.Password.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_userText, margin);
    fd.right = new FormAttachment(middle, -margin);
    m_passLab.setLayoutData(fd);
    
    m_passText = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_passText);
    m_passText.setEchoChar('*');
    // If the password contains a variable, don't hide it.
    m_passText.getTextWidget().addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        checkPasswordVisible();
      }
    });
    
    m_passText.addModifyListener(lsMod);

    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_userText, margin);
    fd.left = new FormAttachment(middle, 0);
    m_passText.setLayoutData(fd);
    
    
    // keyspace line
    m_keyspaceLab = new Label(shell, SWT.RIGHT);
    props.setLook(m_keyspaceLab);
    m_keyspaceLab.setText(BaseMessages.getString(PKG, 
        "CassandraOutputDialog.Keyspace.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_passText, margin);
    fd.right = new FormAttachment(middle, -margin);
    m_keyspaceLab.setLayoutData(fd);
    
    m_keyspaceText = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_keyspaceText);
    m_keyspaceText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_keyspaceText.setToolTipText(transMeta.environmentSubstitute(m_keyspaceText.getText()));
      }
    });
    m_keyspaceText.addModifyListener(lsMod);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_passText, margin);
    fd.left = new FormAttachment(middle, 0);
    m_keyspaceText.setLayoutData(fd);
    
    // column family line
    m_columnFamilyLab = new Label(shell, SWT.RIGHT);
    props.setLook(m_columnFamilyLab);
    m_columnFamilyLab.setText(BaseMessages.getString(PKG, 
        "CassandraOutputDialog.ColumnFamily.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_keyspaceText, margin);
    fd.right = new FormAttachment(middle, -margin);
    m_columnFamilyLab.setLayoutData(fd);
    
    m_getColumnFamiliesBut = new Button(shell, SWT.PUSH | SWT.CENTER);
    props.setLook(m_getColumnFamiliesBut);
    m_getColumnFamiliesBut.setText(BaseMessages.getString(PKG, "CassandraOutputDialog.GetColFam.Button"));
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_keyspaceText, 0);
    m_getColumnFamiliesBut.setLayoutData(fd);
    m_getColumnFamiliesBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setupColumnFamiliesCombo();
      }
    });
    
    m_columnFamilyCombo = new CCombo(shell, SWT.BORDER);
    props.setLook(m_columnFamilyCombo);
    m_columnFamilyCombo.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_columnFamilyCombo.setToolTipText(transMeta.environmentSubstitute(m_columnFamilyCombo.getText()));
      }
    });
    m_columnFamilyCombo.addModifyListener(lsMod);
    fd = new FormData();
    fd.right = new FormAttachment(m_getColumnFamiliesBut, -margin);
    fd.top = new FormAttachment(m_keyspaceText, margin);
    fd.left = new FormAttachment(middle, 0);
    m_columnFamilyCombo.setLayoutData(fd);
    
    // consistency line
    m_consistencyLab = new Label(shell, SWT.RIGHT);
    props.setLook(m_consistencyLab);
    m_consistencyLab.setText(BaseMessages.getString(PKG, 
        "CassandraOutputDialog.Consistency.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_columnFamilyCombo, margin);
    fd.right = new FormAttachment(middle, -margin);
    m_consistencyLab.setLayoutData(fd);
    m_consistencyLab.setToolTipText(BaseMessages.getString(PKG, 
      "CassandraOutputDialog.Consistency.Label.TipText"));
    
    m_consistencyText = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_consistencyText);
    m_consistencyText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_consistencyText.setToolTipText(transMeta.environmentSubstitute(m_consistencyText.getText()));
      }
    });
    m_consistencyText.addModifyListener(lsMod);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_columnFamilyCombo, margin);
    fd.left = new FormAttachment(middle, 0);
    m_consistencyText.setLayoutData(fd);
    
    // batch size line
    m_batchSizeLab = new Label(shell, SWT.RIGHT);
    props.setLook(m_batchSizeLab);
    m_batchSizeLab.setText(BaseMessages.getString(PKG, 
        "CassandraOutputDialog.BatchSize.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_consistencyText, margin);
    fd.right = new FormAttachment(middle, -margin);
    m_batchSizeLab.setLayoutData(fd);
    m_batchSizeLab.setToolTipText(BaseMessages.getString(PKG, 
      "CassandraOutputDialog.BatchSize.TipText"));
    
    m_batchSizeText = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_batchSizeText);
    m_batchSizeText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_batchSizeText.setToolTipText(transMeta.environmentSubstitute(m_batchSizeText.getText()));
      }
    });
    m_batchSizeText.addModifyListener(lsMod);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_consistencyText, margin);
    fd.left = new FormAttachment(middle, 0);
    m_batchSizeText.setLayoutData(fd);
    
    // key field line
    m_keyFieldLab = new Label(shell, SWT.RIGHT);
    props.setLook(m_keyFieldLab);
    m_keyFieldLab.setText(BaseMessages.getString(PKG, 
        "CassandraOutputDialog.KeyField.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_batchSizeText, margin);
    fd.right = new FormAttachment(middle, -margin);
    m_keyFieldLab.setLayoutData(fd);
    
    m_getFieldsBut = new Button(shell, SWT.PUSH | SWT.CENTER);
    props.setLook(m_getFieldsBut);
    m_getFieldsBut.setText(BaseMessages.getString(PKG, "CassandraOutputDialog.GetFields.Button"));
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_batchSizeText, 0);
    m_getFieldsBut.setLayoutData(fd);
    
    m_getFieldsBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setupFieldsCombo();
      }
    });
    
    m_keyFieldCombo = new CCombo(shell, SWT.BORDER);
    m_keyFieldCombo.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_keyFieldCombo.setToolTipText(transMeta.environmentSubstitute(m_keyFieldCombo.getText()));
      }
    });
    m_keyFieldCombo.addModifyListener(lsMod);
    fd = new FormData();
    fd.right = new FormAttachment(m_getFieldsBut, -margin);
    fd.top = new FormAttachment(m_batchSizeText, margin);
    fd.left = new FormAttachment(middle, 0);
    m_keyFieldCombo.setLayoutData(fd);    
    
    // create column family line
    m_createColumnFamilyLab = new Label(shell, SWT.RIGHT);
    props.setLook(m_createColumnFamilyLab);
    m_createColumnFamilyLab.setText(BaseMessages.getString(PKG, 
        "CassandraOutputDialog.CreateColumnFamily.Label"));
    m_createColumnFamilyLab.setToolTipText(BaseMessages.getString(PKG, 
        "CassandraOutputDialog.CreateColumnFamily.TipText"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_keyFieldCombo, margin);
    fd.right = new FormAttachment(middle, -margin);
    m_createColumnFamilyLab.setLayoutData(fd);
    
    m_createColumnFamilyBut = new Button(shell, SWT.CHECK);
    m_createColumnFamilyBut.setToolTipText(BaseMessages.getString(PKG, 
      "CassandraOutputDialog.CreateColumnFamily.TipText"));
    props.setLook(m_createColumnFamilyBut);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_keyFieldCombo, margin);
    fd.left = new FormAttachment(middle, 0);
    m_createColumnFamilyBut.setLayoutData(fd);
    m_createColumnFamilyBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        m_currentMeta.setChanged();
      }
    });    
    
    // truncate column family line
    m_truncateColumnFamilyLab = new Label(shell, SWT.RIGHT);
    props.setLook(m_truncateColumnFamilyLab);
    m_truncateColumnFamilyLab.setText(BaseMessages.getString(PKG, 
        "CassandraOutputDialog.TruncateColumnFamily.Label"));
    m_truncateColumnFamilyLab.setToolTipText(BaseMessages.getString(PKG, 
        "CassandraOutputDialog.TruncateColumnFamily.TipText"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_createColumnFamilyBut, margin);
    fd.right = new FormAttachment(middle, -margin);
    m_truncateColumnFamilyLab.setLayoutData(fd);
    
    m_truncateColumnFamilyBut = new Button(shell, SWT.CHECK);
    m_truncateColumnFamilyBut.setToolTipText(BaseMessages.getString(PKG, 
      "CassandraOutputDialog.TruncateColumnFamily.TipText"));
    props.setLook(m_truncateColumnFamilyBut);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_createColumnFamilyBut, margin);
    fd.left = new FormAttachment(middle, 0);
    m_truncateColumnFamilyBut.setLayoutData(fd);
    m_truncateColumnFamilyBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        m_currentMeta.setChanged();
      }
    });    
    
    // update column family meta data line
    m_updateColumnFamilyMetaDataLab = new Label(shell, SWT.RIGHT);
    props.setLook(m_updateColumnFamilyMetaDataLab);
    m_updateColumnFamilyMetaDataLab.setText(BaseMessages.getString(PKG, 
        "CassandraOutputDialog.UpdateColumnFamilyMetaData.Label"));
    m_updateColumnFamilyMetaDataLab.setToolTipText(BaseMessages.getString(PKG, 
        "CassandraOutputDialog.UpdateColumnFamilyMetaData.TipText"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_truncateColumnFamilyBut, margin);
    fd.right = new FormAttachment(middle, -margin);
    m_updateColumnFamilyMetaDataLab.setLayoutData(fd);
    
    m_updateColumnFamilyMetaDataBut = new Button(shell, SWT.CHECK);
    m_updateColumnFamilyMetaDataBut.setToolTipText(BaseMessages.getString(PKG, 
      "CassandraOutputDialog.UpdateColumnFamilyMetaData.TipText"));
    props.setLook(m_updateColumnFamilyMetaDataBut);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_truncateColumnFamilyBut, margin);
    fd.left = new FormAttachment(middle, 0);
    m_updateColumnFamilyMetaDataBut.setLayoutData(fd);
    m_updateColumnFamilyMetaDataBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        m_currentMeta.setChanged();
      }
    });    
    
    // insert fields not in meta line
    m_insertFieldsNotInColumnFamMetaLab = new Label(shell, SWT.RIGHT);
    props.setLook(m_insertFieldsNotInColumnFamMetaLab);
    m_insertFieldsNotInColumnFamMetaLab.setText(BaseMessages.getString(PKG, 
        "CassandraOutputDialog.InsertFieldsNotInColumnFamMetaData.Label"));
    m_insertFieldsNotInColumnFamMetaLab.setToolTipText(BaseMessages.getString(PKG, 
        "CassandraOutputDialog.InsertFieldsNotInColumnFamMetaData.TipText"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_updateColumnFamilyMetaDataBut, margin);
    fd.right = new FormAttachment(middle, -margin);
    m_insertFieldsNotInColumnFamMetaLab.setLayoutData(fd);
    
    m_insertFieldsNotInColumnFamMetaBut = new Button(shell, SWT.CHECK);
    m_insertFieldsNotInColumnFamMetaBut.setToolTipText(BaseMessages.getString(PKG, 
      "CassandraOutputDialog.InsertFieldsNotInColumnFamMetaData.TipText"));
    props.setLook(m_insertFieldsNotInColumnFamMetaBut);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_updateColumnFamilyMetaDataBut, margin);
    fd.left = new FormAttachment(middle, 0);
    m_insertFieldsNotInColumnFamMetaBut.setLayoutData(fd);
    m_insertFieldsNotInColumnFamMetaBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        m_currentMeta.setChanged();
      }
    });    
    
    
    // compression check box
    m_compressionLab = new Label(shell, SWT.RIGHT);
    props.setLook(m_compressionLab);
    m_compressionLab.setText(BaseMessages.getString(PKG, 
        "CassandraOutputDialog.UseCompression.Label"));
    m_compressionLab.setToolTipText(BaseMessages.getString(PKG, 
      "CassandraOutputDialog.UseCompression.TipText"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_insertFieldsNotInColumnFamMetaBut, margin);
    fd.right = new FormAttachment(middle, -margin);
    m_compressionLab.setLayoutData(fd);
    
    m_useCompressionBut = new Button(shell, SWT.CHECK);
    props.setLook(m_useCompressionBut);
    m_useCompressionBut.setToolTipText(BaseMessages.getString(PKG, 
      "CassandraOutputDialog.UseCompression.TipText"));
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_insertFieldsNotInColumnFamMetaBut, margin);
    m_useCompressionBut.setLayoutData(fd);
    m_useCompressionBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        m_currentMeta.setChanged();
      }
    });    
    
    // show schema button
    m_showSchemaBut = new Button(shell, SWT.PUSH);
    m_showSchemaBut.setText(BaseMessages.
        getString(PKG, "CassandraOutputDialog.Schema.Button"));
    props.setLook(m_showSchemaBut);
    fd = new FormData();
//  fd.left = new FormAttachment(middle, 0);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_useCompressionBut, margin);
    m_showSchemaBut.setLayoutData(fd);
    m_showSchemaBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        popupSchemaInfo();
      }
    });
    
    // Apriori CQL button
    m_aprioriCQLBut = new Button(shell, SWT.PUSH | SWT.CENTER);
    props.setLook(m_aprioriCQLBut);
    m_aprioriCQLBut.setText(BaseMessages.getString(PKG, 
        "CassandraOutputDialog.CQL.Button"));
    fd = new FormData();
    fd.right = new FormAttachment(m_showSchemaBut, 0);
    fd.top = new FormAttachment(m_useCompressionBut, margin);
    m_aprioriCQLBut.setLayoutData(fd);
    m_aprioriCQLBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        popupCQLEditor(lsMod);
      }
    });
    
    
    // Buttons inherited from BaseStepDialog
    wOK = new Button(shell, SWT.PUSH);
    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    
    wCancel=new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
    
    setButtonPositions(new Button[] { wOK, wCancel }, 
                       margin, m_showSchemaBut);
    
    
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
  
  protected void setupColumnFamiliesCombo() {
    CassandraConnection conn = null;
    
    try {
      String hostS = transMeta.environmentSubstitute(m_hostText.getText());
      String portS = transMeta.environmentSubstitute(m_portText.getText());
      String userS = m_userText.getText();
      String passS = m_passText.getText();
      if (!Const.isEmpty(userS) && !Const.isEmpty(passS)) {
        userS = transMeta.environmentSubstitute(userS);
        passS = transMeta.environmentSubstitute(passS);
      }
      String keyspaceS = transMeta.environmentSubstitute(m_keyspaceText.getText());
      
      conn = CassandraOutputData.
        getCassandraConnection(hostS, Integer.parseInt(portS), userS, passS);
      
      try {
        conn.setKeyspace(keyspaceS);
      } catch (InvalidRequestException ire) {
        MessageDialog.openError(shell, 
            BaseMessages.getString(PKG, "CassandraOutputDialog.Error.ProblemGettingSchemaInfo.Title"), 
            BaseMessages.getString(PKG, "CassandraOutputDialog.Error.ProblemGettingSchemaInfo.Message") 
            + ":\n\n" 
            + ire.why);
        return;
      }
      
      List<String> colFams = CassandraColumnMetaData.getColumnFamilyNames(conn);
      m_columnFamilyCombo.removeAll();
      for (String famName : colFams) {
        m_columnFamilyCombo.add(famName);        
      }
      
    } catch (Exception ex) {
      MessageDialog.openError(shell, 
          BaseMessages.getString(PKG, "CassandraOutputDialog.Error.ProblemGettingSchemaInfo.Title"), 
          BaseMessages.getString(PKG, "CassandraOutputDialog.Error.ProblemGettingSchemaInfo.Message") 
          + ":\n\n" 
          + ex.getMessage());
      ex.printStackTrace();      
    }
  }
  
  protected void setupFieldsCombo() {
    // try and set up from incoming fields from previous step
    
    StepMeta stepMeta = transMeta.findStep(stepname);
    
    if (stepMeta != null) {
      try {
        RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);
        
        if (row.size() == 0) {
          MessageDialog.openError(shell, 
              BaseMessages.getString(PKG, "CassandraOutputData.Message.NoIncomingFields.Title"),
              BaseMessages.getString(PKG, "CassandraOutputData.Message.NoIncomingFields"));
          
          return;
        }
        
        m_keyFieldCombo.removeAll();
        for (int i = 0; i < row.size(); i++) {
          ValueMetaInterface vm = row.getValueMeta(i);
          m_keyFieldCombo.add(vm.getName());
        }
      } catch (KettleException ex) {
        MessageDialog.openError(shell, 
            BaseMessages.getString(PKG, "CassandraOutputData.Message.NoIncomingFields.Title"),
            BaseMessages.getString(PKG, "CassandraOutputData.Message.NoIncomingFields"));
      }
    }
  }
  
  protected void ok() {
    if (Const.isEmpty(m_stepnameText.getText())) {
      return;
    }
    
    stepname = m_stepnameText.getText();
    m_currentMeta.setCassandraHost(m_hostText.getText());
    m_currentMeta.setCassandraPort(m_portText.getText());
    m_currentMeta.setUsername(m_userText.getText());
    m_currentMeta.setPassword(m_passText.getText());
    m_currentMeta.setCassandraKeyspace(m_keyspaceText.getText());
    m_currentMeta.setColumnFamilyName(m_columnFamilyCombo.getText());
    m_currentMeta.setConsistency(m_consistencyText.getText());
    m_currentMeta.setBatchSize(m_batchSizeText.getText());
    m_currentMeta.setKeyField(m_keyFieldCombo.getText());
    
    m_currentMeta.setCreateColumnFamily(m_createColumnFamilyBut.getSelection());
    m_currentMeta.setTruncateColumnFamily(m_truncateColumnFamilyBut.getSelection());
    m_currentMeta.setUpdateCassandraMeta(m_updateColumnFamilyMetaDataBut.getSelection());
    m_currentMeta.setInsertFieldsNotInMeta(m_insertFieldsNotInColumnFamMetaBut.getSelection());    
    m_currentMeta.setUseCompression(m_useCompressionBut.getSelection());
    m_currentMeta.setAprioriCQL(m_aprioriCQL);
    
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
  
  protected void popupCQLEditor(ModifyListener lsMod) {
    
    EnterCQLDialog ecd = new EnterCQLDialog(shell, transMeta, lsMod, 
        "CQL to execute before inserting first row", m_aprioriCQL);
    
    m_aprioriCQL = ecd.open();
  }
  
  protected void popupSchemaInfo() {

    CassandraConnection conn = null;
    try {
//      CassandraInputMeta tempMeta = (CassandraInputMeta)m_currentMeta.clone();
      String hostS = transMeta.environmentSubstitute(m_hostText.getText());
      String portS = transMeta.environmentSubstitute(m_portText.getText());
      String userS = m_userText.getText();
      String passS = m_passText.getText();
      if (!Const.isEmpty(userS) && !Const.isEmpty(passS)) {
        userS = transMeta.environmentSubstitute(userS);
        passS = transMeta.environmentSubstitute(passS);
      }
      String keyspaceS = transMeta.environmentSubstitute(m_keyspaceText.getText());
//        tempMeta.setCassandraHost(hostS); tempMeta.setCassandraPort(portS);
//      tempMeta.setCassandraKeyspace(keyspaceS);                    
      
      conn = CassandraOutputData.
        getCassandraConnection(hostS, Integer.parseInt(portS), userS, passS);
      try {
        conn.setKeyspace(keyspaceS);
      } catch (InvalidRequestException ire) {
        MessageDialog.openError(shell, 
            BaseMessages.getString(PKG, "CassandraInputDialog.Error.ProblemGettingSchemaInfo.Title"), 
            BaseMessages.getString(PKG, "CassandraInputDialog.Error.ProblemGettingSchemaInfo.Message") 
            + ":\n\n" 
            + ire.why);
        ire.printStackTrace();
        return;
      }
      
      String colFam = transMeta.environmentSubstitute(m_columnFamilyCombo.getText());
      if (Const.isEmpty(colFam)) {
        throw new Exception("No colummn family (table) name specified!");
      }
      
      if (!CassandraColumnMetaData.columnFamilyExists(conn, colFam)) {
        throw new Exception("The column family '" + colFam + "' does not " +
            "seem to exist in the keyspace '" + keyspaceS);
      }
      
/*          tempMeta.getFields(outputF, stepname, null, null, transMeta);
      String colFam = outputF.getValueMeta(0).getName(); */
      
      CassandraColumnMetaData cassMeta = new CassandraColumnMetaData(conn, colFam);
      String schemaDescription = cassMeta.getSchemaDescription();
      ShowMessageDialog smd = new ShowMessageDialog(shell, 
          SWT.ICON_INFORMATION | SWT.OK, "Schema info", schemaDescription, true);
      smd.open();
    } catch (Exception e1) {
      MessageDialog.openError(shell, 
          BaseMessages.getString(PKG, "CassandraOutputDialog.Error.ProblemGettingSchemaInfo.Title"), 
          BaseMessages.getString(PKG, "CassandraOutputDialog.Error.ProblemGettingSchemaInfo.Message") 
          + ":\n\n" 
          + e1.getMessage());
      e1.printStackTrace();
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }
  
  protected void getData() {
    
    if (!Const.isEmpty(m_currentMeta.getCassandraHost())) {
      m_hostText.setText(m_currentMeta.getCassandraHost());
    }
    
    if (!Const.isEmpty(m_currentMeta.getCassandraPort())) {
      m_portText.setText(m_currentMeta.getCassandraPort());
    }
    
    if (!Const.isEmpty(m_currentMeta.getUsername())) {
      m_userText.setText(m_currentMeta.getUsername());
    }
    
    if (!Const.isEmpty(m_currentMeta.getPassword())) {
      m_passText.setText(m_currentMeta.getPassword());
    }
    
    if (!Const.isEmpty(m_currentMeta.getCassandraKeyspace())) {
      m_keyspaceText.setText(m_currentMeta.getCassandraKeyspace());
    }

    if (!Const.isEmpty(m_currentMeta.getColumnFamilyName())) {
      m_columnFamilyCombo.setText(m_currentMeta.getColumnFamilyName());
    }
    
    if (!Const.isEmpty(m_currentMeta.getConsistency())) {
      m_consistencyText.setText(m_currentMeta.getConsistency());
    }
    
    if (!Const.isEmpty(m_currentMeta.getBatchSize())) {
      m_batchSizeText.setText(m_currentMeta.getBatchSize());
    }
    
    if (!Const.isEmpty(m_currentMeta.getKeyField())) {
      m_keyFieldCombo.setText(m_currentMeta.getKeyField());
    }

    m_createColumnFamilyBut.setSelection(m_currentMeta.getCreateColumnFamily());
    m_truncateColumnFamilyBut.setSelection(m_currentMeta.getTruncateColumnFamily());
    m_updateColumnFamilyMetaDataBut.setSelection(m_currentMeta.getUpdateCassandraMeta());
    m_insertFieldsNotInColumnFamMetaBut.setSelection(m_currentMeta.getInsertFieldsNotInMeta());    
    m_useCompressionBut.setSelection(m_currentMeta.getUseCompression());
    
    m_aprioriCQL = m_currentMeta.getAprioriCQL();
    if (m_aprioriCQL == null) {
      m_aprioriCQL = "";
    }
  }
  
  private void checkPasswordVisible() {
    String password = m_passText.getText();
    ArrayList<String> list = new ArrayList<String>();
    StringUtil.getUsedVariables(password, list, true);
    if (list.size() == 0) {
      m_passText.setEchoChar('*');      
    } else {
      m_passText.setEchoChar('\0'); // show everything
    }
  }
}
