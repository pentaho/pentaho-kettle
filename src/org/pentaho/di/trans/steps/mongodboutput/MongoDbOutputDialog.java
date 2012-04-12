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

package org.pentaho.di.trans.steps.mongodboutput;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.ShowMessageDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.i18n.BaseMessages;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Dialog class for the MongoDB output step
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
public class MongoDbOutputDialog extends BaseStepDialog implements
    StepDialogInterface {
  
  private static final Class<?> PKG = MongoDbOutputMeta.class;
  
  protected MongoDbOutputMeta m_currentMeta;
  protected MongoDbOutputMeta m_originalMeta;
  
  /** various UI bits and pieces for the dialog */
  private Label m_stepnameLabel;
  private Text m_stepnameText;
  
  // The tabs of the dialog
  private CTabFolder m_wTabFolder;
  private CTabItem m_wConfigTab;
  private CTabItem m_wMongoFieldsTab;
  private Button m_getFieldsBut;
  private Button m_previewDocStructBut;
  private CTabItem m_wMongoIndexesTab;
  private Button m_showIndexesBut;
  
  private TextVar m_hostnameField;
  private TextVar m_portField;
  private TextVar m_usernameField;
  private TextVar m_passField;
  private CCombo m_dbNameField;
  private Button m_getDBsBut;
  private CCombo m_collectionField;
  private Button m_getCollectionsBut;
  
  private TextVar m_batchInsertSizeField;
  
  private Button m_truncateBut;
  private Button m_upsertBut;
  private Button m_multiBut;
  private Button m_modifierUpdateBut;
  
  private TableView m_mongoFieldsView;
  private TableView m_mongoIndexesView;
  
  public MongoDbOutputDialog(Shell parent, Object in,
      TransMeta tr, String name) {
    
    super(parent, (BaseStepMeta)in, tr, name);
    
    m_currentMeta = (MongoDbOutputMeta)in;
    m_originalMeta = (MongoDbOutputMeta)m_currentMeta.clone();
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
    shell.setText(BaseMessages.getString(PKG, "MongoDbOutputDialog.Shell.Title"));

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;
    
    // Stepname line
    m_stepnameLabel = new Label(shell, SWT.RIGHT);
    m_stepnameLabel.
      setText(BaseMessages.getString(PKG, "MongoDbOutputDialog.StepName.Label"));
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
      setText(BaseMessages.getString(PKG, "MongoDbOutputDialog.ConfigTab.TabTitle"));
    
    Composite wConfigComp = new Composite(m_wTabFolder, SWT.NONE);
    props.setLook(wConfigComp);
    
    FormLayout configLayout = new FormLayout();
    configLayout.marginWidth  = 3;
    configLayout.marginHeight = 3;
    wConfigComp.setLayout(configLayout);
    
    // hostname line
    Label hostnameLab = new Label(wConfigComp, SWT.RIGHT);
    hostnameLab.setText(BaseMessages.getString(PKG, "MongoDbOutputDialog.Hostname.Label"));
    hostnameLab.setToolTipText(BaseMessages.getString(PKG, "MongoDbOutputDialog.Hostname.TipText"));
    props.setLook(hostnameLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(0, margin);
    fd.right = new FormAttachment(middle, -margin);
    hostnameLab.setLayoutData(fd);
    
    m_hostnameField = new TextVar(transMeta, wConfigComp, 
        SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_hostnameField);
    m_hostnameField.addModifyListener(lsMod);
    // set the tool tip to the contents with any env variables expanded
    m_hostnameField.addModifyListener(new ModifyListener() {      
      public void modifyText(ModifyEvent e) {
        m_hostnameField.
          setToolTipText(transMeta.environmentSubstitute(m_hostnameField.getText()));
      }
    });
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(0, 0);
    fd.left = new FormAttachment(middle, 0);
    m_hostnameField.setLayoutData(fd);
    
    // port line
    Label portLab = new Label(wConfigComp, SWT.RIGHT);
    portLab.setText(BaseMessages.getString(PKG, "MongoDbOutputDialog.Port.Label"));
    props.setLook(portLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_hostnameField, margin);
    fd.right = new FormAttachment(middle, -margin);
    portLab.setLayoutData(fd);
    
    m_portField = new TextVar(transMeta, wConfigComp, 
        SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_portField);
    m_portField.addModifyListener(lsMod);
    // set the tool tip to the contents with any env variables expanded
    m_portField.addModifyListener(new ModifyListener() {      
      public void modifyText(ModifyEvent e) {
        m_portField.
          setToolTipText(transMeta.environmentSubstitute(m_portField.getText()));
      }
    });
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_hostnameField, margin);
    fd.left = new FormAttachment(middle, 0);
    m_portField.setLayoutData(fd);
    
    // username field
    Label userLab = new Label(wConfigComp, SWT.RIGHT);
    userLab.setText(BaseMessages.getString(PKG, "MongoDbOutputDialog.Username.Label"));
    props.setLook(userLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_portField, margin);
    fd.right = new FormAttachment(middle, -margin);
    userLab.setLayoutData(fd);
    
    m_usernameField = new TextVar(transMeta, wConfigComp, 
        SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_usernameField);
    m_usernameField.addModifyListener(lsMod);
    // set the tool tip to the contents with any env variables expanded
    m_usernameField.addModifyListener(new ModifyListener() {      
      public void modifyText(ModifyEvent e) {
        m_usernameField.
          setToolTipText(transMeta.environmentSubstitute(m_usernameField.getText()));
      }
    });
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_portField, margin);
    fd.left = new FormAttachment(middle, 0);
    m_usernameField.setLayoutData(fd);
    
    // password field
    Label passLab = new Label(wConfigComp, SWT.RIGHT);
    passLab.setText(BaseMessages.getString(PKG, "MongoDbOutputDialog.Password.Label"));
    props.setLook(passLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_usernameField, margin);
    fd.right = new FormAttachment(middle, -margin);
    passLab.setLayoutData(fd);
    
    m_passField = new TextVar(transMeta, wConfigComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_passField);
    m_passField.setEchoChar('*');
    // If the password contains a variable, don't hide it.
    m_passField.getTextWidget().addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        checkPasswordVisible();
      }
    });
    
    m_passField.addModifyListener(lsMod);

    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_usernameField, margin);
    fd.left = new FormAttachment(middle, 0);
    m_passField.setLayoutData(fd);
    
    // DB name
    Label dbNameLab = new Label(wConfigComp, SWT.RIGHT);
    dbNameLab.setText(BaseMessages.getString(PKG, "MongoDbInputDialog.DBName.Label"));
    dbNameLab.setToolTipText(BaseMessages.getString(PKG, "MongoDbInputDialog.DBName.TipText"));
    props.setLook(dbNameLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_passField, margin);
    fd.right = new FormAttachment(middle, -margin);
    dbNameLab.setLayoutData(fd);
    
    m_getDBsBut = new Button(wConfigComp, SWT.PUSH | SWT.CENTER);
    props.setLook(m_getDBsBut);
    m_getDBsBut.setText(BaseMessages.getString(PKG, "MongoDbInputDialog.GetDBs.Button"));
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_passField, 0);
    m_getDBsBut.setLayoutData(fd);
    
    m_dbNameField = new CCombo(wConfigComp, SWT.BORDER);
    props.setLook(m_dbNameField);

    m_dbNameField.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_currentMeta.setChanged();
        m_dbNameField.
          setToolTipText(transMeta.environmentSubstitute(m_dbNameField.getText()));
      }
    });
    
    m_dbNameField.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setupCollectionNamesForDB(true);
      }
      
      public void widgetDefaultSelected(SelectionEvent e) {
        setupCollectionNamesForDB(true);
      }
    });
    m_dbNameField.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
        
      }
      
      public void focusLost(FocusEvent e) {
        setupCollectionNamesForDB(true);
      }
    });
    
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_passField, margin);
    fd.right = new FormAttachment(m_getDBsBut, -margin);
    m_dbNameField.setLayoutData(fd);
    
    m_getDBsBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        setupDBNames();
      }      
    });
    
    // collection line
    Label collectionLab = new Label(wConfigComp, SWT.RIGHT);
    collectionLab.setText(BaseMessages.getString(PKG, "MongoDbInputDialog.Collection.Label"));
    collectionLab.setToolTipText(BaseMessages.getString(PKG, "MongoDbInputDialog.Collection.TipText"));
    props.setLook(collectionLab);
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_dbNameField, margin);
    fd.right = new FormAttachment(middle, -margin);
    collectionLab.setLayoutData(fd);
    
    m_getCollectionsBut = new Button(wConfigComp, SWT.PUSH | SWT.CENTER);
    props.setLook(m_getCollectionsBut);
    m_getCollectionsBut.setText(BaseMessages.getString(PKG, "MongoDbInputDialog.GetCollections.Button"));
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_dbNameField, 0);
    m_getCollectionsBut.setLayoutData(fd);
    
    m_getCollectionsBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        //setupMappingNamesForTable(false);
        setupCollectionNamesForDB(false);
      }
    });
    

    m_collectionField = new CCombo(wConfigComp, SWT.BORDER);
    props.setLook(m_collectionField);
    m_collectionField.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_currentMeta.setChanged();
        
        m_collectionField.setToolTipText(transMeta.
            environmentSubstitute(m_collectionField.getText()));
      }
    });
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_dbNameField, margin);
    fd.right = new FormAttachment(m_getCollectionsBut, -margin);
    m_collectionField.setLayoutData(fd);
    
    
    // batch insert line
    Label batchLab = new Label(wConfigComp, SWT.RIGHT);
    batchLab.setText(BaseMessages.getString(PKG, "MongoDbOutputDialog.BatchInsertSize.Label"));
    props.setLook(batchLab);
    batchLab.setToolTipText(BaseMessages.getString(PKG, "MongoDbOutputDialog.BatchInsertSize.TipText"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_collectionField, margin);
    fd.right = new FormAttachment(middle, -margin);
    batchLab.setLayoutData(fd);
    
    m_batchInsertSizeField = new TextVar(transMeta, wConfigComp, 
        SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_batchInsertSizeField);
    m_batchInsertSizeField.addModifyListener(lsMod);
    // set the tool tip to the contents with any env variables expanded
    m_batchInsertSizeField.addModifyListener(new ModifyListener() {      
      public void modifyText(ModifyEvent e) {
        m_batchInsertSizeField.
          setToolTipText(transMeta.environmentSubstitute(m_batchInsertSizeField.getText()));
      }
    });
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_collectionField, margin);
    fd.left = new FormAttachment(middle, 0);
    m_batchInsertSizeField.setLayoutData(fd);
    
    // truncate line
    Label truncateLab = new Label(wConfigComp, SWT.RIGHT);
    truncateLab.setText(BaseMessages.getString(PKG, "MongoDbOutputDialog.Truncate.Label"));
    props.setLook(truncateLab);
    truncateLab.setToolTipText(BaseMessages.getString(PKG, "MongoDbOutputDialog.Truncate.TipText"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_batchInsertSizeField, margin);
    fd.right = new FormAttachment(middle, -margin);
    truncateLab.setLayoutData(fd);
    
    m_truncateBut = new Button(wConfigComp, SWT.CHECK);
    props.setLook(m_truncateBut);
    m_truncateBut.setToolTipText(BaseMessages.getString(PKG, "MongoDbOutputDialog.Truncate.TipText"));
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_batchInsertSizeField, margin);
    fd.left = new FormAttachment(middle, 0);
    m_truncateBut.setLayoutData(fd);
    m_truncateBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        m_currentMeta.setChanged();
      }
    });
    
    // upsert line
    Label upsertLab = new Label(wConfigComp, SWT.RIGHT);
    upsertLab.setText(BaseMessages.getString(PKG, "MongoDbOutputDialog.Upsert.Label"));
    props.setLook(upsertLab);
    upsertLab.setToolTipText(BaseMessages.getString(PKG, "MongoDbOutputDialog.Upsert.TipText"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_truncateBut, margin);
    fd.right = new FormAttachment(middle, -margin);
    upsertLab.setLayoutData(fd);
    
    m_upsertBut = new Button(wConfigComp, SWT.CHECK);
    props.setLook(m_upsertBut);
    m_upsertBut.setToolTipText(BaseMessages.getString(PKG, "MongoDbOutputDialog.Upsert.TipText"));
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_truncateBut, margin);
    fd.left = new FormAttachment(middle, 0);
    m_upsertBut.setLayoutData(fd);
    m_upsertBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        m_currentMeta.setChanged();
        m_modifierUpdateBut.setEnabled(m_upsertBut.getSelection());
        m_multiBut.setEnabled(m_upsertBut.getSelection());
        if (!m_upsertBut.getSelection()) {
          m_modifierUpdateBut.setSelection(false);
          m_multiBut.setSelection(false);
        }
        m_multiBut.setEnabled(m_modifierUpdateBut.getSelection());
        if (!m_multiBut.getEnabled()) {
          m_multiBut.setSelection(false);
        }
      }
    });
    
    // multi line
    Label multiLab = new Label(wConfigComp, SWT.RIGHT);
    multiLab.setText(BaseMessages.getString(PKG, "MongoDbOutputDialog.Multi.Label"));
    props.setLook(multiLab);
    multiLab.setToolTipText(BaseMessages.getString(PKG, "MongoDbOutputDialog.Multi.TipText"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_upsertBut, margin);
    fd.right = new FormAttachment(middle, -margin);
    multiLab.setLayoutData(fd);
    
    m_multiBut = new Button(wConfigComp, SWT.CHECK);
    props.setLook(m_multiBut);
    m_multiBut.setToolTipText(BaseMessages.getString(PKG, "MongoDbOutputDialog.Multi.TipText"));
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_upsertBut, margin);
    fd.left = new FormAttachment(middle, 0);
    m_multiBut.setLayoutData(fd);
    m_multiBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        m_currentMeta.setChanged();                
      }
    });
    
    
    // modifier update
    Label modifierLab = new Label(wConfigComp, SWT.RIGHT);
    modifierLab.setText(BaseMessages.getString(PKG, "MongoDbOutputDialog.Modifier.Label"));
    props.setLook(modifierLab);
    modifierLab.setToolTipText(BaseMessages.getString(PKG, "MongoDbOutputDialog.Modifier.TipText"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_multiBut, margin);
    fd.right = new FormAttachment(middle, -margin);
    modifierLab.setLayoutData(fd);
    
    m_modifierUpdateBut = new Button(wConfigComp, SWT.CHECK);
    props.setLook(m_modifierUpdateBut);
    m_modifierUpdateBut.setToolTipText(BaseMessages.getString(PKG, "MongoDbOutputDialog.Modifier.TipText"));
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_multiBut, margin);
    fd.left = new FormAttachment(middle, 0);
    m_modifierUpdateBut.setLayoutData(fd);
    m_modifierUpdateBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        m_currentMeta.setChanged();
        
        m_multiBut.setEnabled(m_modifierUpdateBut.getSelection());
        
        if (!m_modifierUpdateBut.getSelection()) {
          m_multiBut.setSelection(false);          
        }        
      }
    });
    
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    fd.bottom = new FormAttachment(100, 0);
    wConfigComp.setLayoutData(fd);
    
    wConfigComp.layout();
    m_wConfigTab.setControl(wConfigComp);
    
    
    
    // --- start of the fields tab
    m_wMongoFieldsTab = new CTabItem(m_wTabFolder, SWT.NONE);
    m_wMongoFieldsTab.setText(BaseMessages.getString(PKG, "MongoDbOutputDialog.FieldsTab.TabTitle"));
    Composite wFieldsComp = new Composite(m_wTabFolder, SWT.NONE);
    props.setLook(wFieldsComp);
    FormLayout filterLayout = new FormLayout();
    filterLayout.marginWidth  = 3;
    filterLayout.marginHeight = 3;
    wFieldsComp.setLayout(filterLayout);
    
    final ColumnInfo[] colInf = new ColumnInfo[] {
        new ColumnInfo(BaseMessages.getString(PKG, "MongoDbOutputDialog.Fields.Incoming"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(BaseMessages.getString(PKG, "MongoDbOutputDialog.Fields.Path"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(BaseMessages.getString(PKG, "MongoDbOutputDialog.Fields.UseIncomingName"), ColumnInfo.COLUMN_TYPE_CCOMBO, false),
        new ColumnInfo(BaseMessages.getString(PKG, "MongoDbOutputDialog.Fields.UpdateMatchField"), ColumnInfo.COLUMN_TYPE_CCOMBO, false),
        new ColumnInfo(BaseMessages.getString(PKG, "MongoDbOutputDialog.Fields.ModifierUpdateOperation"), ColumnInfo.COLUMN_TYPE_CCOMBO, false),
    };
    
    colInf[2].setComboValues(new String[] {"Y", "N"});
    colInf[2].setReadOnly(true);
    colInf[3].setComboValues(new String[] {"Y", "N"});
    colInf[3].setReadOnly(true);
    colInf[4].setComboValues(new String[] {"N/A", "$set", "$inc", "$push"});
    
    
    // get fields but
    m_getFieldsBut = new Button(wFieldsComp, SWT.PUSH | SWT.CENTER);
    props.setLook(m_getFieldsBut);
    m_getFieldsBut.setText(BaseMessages.getString(PKG, "MongoDbOutputDialog.GetFieldsBut"));
    fd = new FormData();
    //fd.right = new FormAttachment(100, 0);
    fd.bottom = new FormAttachment(100, -margin*2);
    fd.left = new FormAttachment(0, margin);
    m_getFieldsBut.setLayoutData(fd);
    
    m_getFieldsBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        getFields();
      }
    });
    
    m_previewDocStructBut = new Button(wFieldsComp, SWT.PUSH | SWT.CENTER);
    props.setLook(m_previewDocStructBut);
    m_previewDocStructBut.setText(BaseMessages.getString(PKG, "MongoDbOutputDialog.PreviewDocStructBut"));
    fd = new FormData();
    //fd.right = new FormAttachment(100, 0);
    fd.bottom = new FormAttachment(100, -margin*2);
    fd.left = new FormAttachment(m_getFieldsBut, margin);
    m_previewDocStructBut.setLayoutData(fd);
    m_previewDocStructBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        previewDocStruct();
      }
    });
    
    
    m_mongoFieldsView = new TableView(transMeta, wFieldsComp,
        SWT.FULL_SELECTION | SWT.MULTI,
        colInf,
        1,
        lsMod,
        props);
    
    fd = new FormData();
    fd.top   = new FormAttachment(0, margin*2);
    fd.bottom= new FormAttachment(m_getFieldsBut, -margin*2);
    fd.left  = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    m_mongoFieldsView.setLayoutData(fd);
                
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    fd.bottom = new FormAttachment(100, 0);
    wFieldsComp.setLayoutData(fd);
    
    wFieldsComp.layout();
    m_wMongoFieldsTab.setControl(wFieldsComp);
    

    
    // indexes tab ------------------
    m_wMongoIndexesTab = new CTabItem(m_wTabFolder, SWT.NONE);
    m_wMongoIndexesTab.setText(BaseMessages.getString(PKG, "MongoDbOutputDialog.IndexesTab.TabTitle"));
    Composite wIndexesComp = new Composite(m_wTabFolder, SWT.NONE);
    props.setLook(wIndexesComp);
    FormLayout indexesLayout = new FormLayout();
    indexesLayout.marginWidth  = 3;
    indexesLayout.marginHeight = 3;
    wIndexesComp.setLayout(indexesLayout);
    final ColumnInfo[] colInf2 = new ColumnInfo[] {
        new ColumnInfo(BaseMessages.getString(PKG, "MongoDbOutputDialog.Indexes.IndexFields"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(BaseMessages.getString(PKG, "MongoDbOutputDialog.Indexes.IndexOpp"), ColumnInfo.COLUMN_TYPE_CCOMBO, false),
        new ColumnInfo(BaseMessages.getString(PKG, "MongoDbOutputDialog.Indexes.Unique"), ColumnInfo.COLUMN_TYPE_CCOMBO, false),
        new ColumnInfo(BaseMessages.getString(PKG, "MongoDbOutputDialog.Indexes.Sparse"), ColumnInfo.COLUMN_TYPE_CCOMBO, false),
    };
    colInf2[1].setComboValues(new String[] {"Create", "Drop"});
    colInf2[1].setReadOnly(true);
    colInf2[2].setComboValues(new String[] {"Y", "N"});
    colInf2[2].setReadOnly(true);
    colInf2[3].setComboValues(new String[] {"Y", "N"});
    colInf2[3].setReadOnly(true);
    
    // get indexes but
    m_showIndexesBut = new Button(wIndexesComp, SWT.PUSH | SWT.CENTER);
    props.setLook(m_showIndexesBut);
    m_showIndexesBut.setText(BaseMessages.getString(PKG, "MongoDbOutputDialog.ShowIndexesBut"));
    fd = new FormData();
    //fd.right = new FormAttachment(100, 0);
    fd.bottom = new FormAttachment(100, -margin*2);
    fd.left = new FormAttachment(0, margin);
    m_showIndexesBut.setLayoutData(fd);
    
    m_showIndexesBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        showIndexInfo();
      }
    });

    m_mongoIndexesView = new TableView(transMeta, wIndexesComp,
        SWT.FULL_SELECTION | SWT.MULTI,
        colInf2,
        1,
        lsMod,
        props);
    
    fd = new FormData();
    fd.top   = new FormAttachment(0, margin*2);
    fd.bottom= new FormAttachment(m_showIndexesBut, -margin*2);
    fd.left  = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    m_mongoIndexesView.setLayoutData(fd);
    
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    fd.bottom = new FormAttachment(100, 0);
    wIndexesComp.setLayoutData(fd);
    
    wIndexesComp.layout();
    m_wMongoIndexesTab.setControl(wIndexesComp);
    
    
    
    
    
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
  
  private void ok() {
    if (Const.isEmpty(m_stepnameText.getText())) {
      return;
    }
    
    stepname = m_stepnameText.getText();
    
    m_currentMeta.setHostname(m_hostnameField.getText());
    m_currentMeta.setPort(m_portField.getText());
    m_currentMeta.setUsername(m_usernameField.getText());
    m_currentMeta.setPassword(m_passField.getText());
    m_currentMeta.setDBName(m_dbNameField.getText());
    m_currentMeta.setCollection(m_collectionField.getText());
    m_currentMeta.setBatchInsertSize(m_batchInsertSizeField.getText());
    m_currentMeta.setUpsert(m_upsertBut.getSelection());
    m_currentMeta.setMulti(m_multiBut.getSelection());
    m_currentMeta.setTruncate(m_truncateBut.getSelection());
    m_currentMeta.setModifierUpdate(m_modifierUpdateBut.getSelection());
    
    
    m_currentMeta.setMongoFields(tableToMongoFieldList());
    
    // indexes
    int numNonEmpty = m_mongoIndexesView.nrNonEmpty();
    List<MongoDbOutputMeta.MongoIndex> mongoIndexes = 
      new ArrayList<MongoDbOutputMeta.MongoIndex>();
    if (numNonEmpty > 0) {            
      for (int i = 0; i < numNonEmpty; i++) {
        TableItem item = m_mongoIndexesView.getNonEmpty(i);
        
        String indexFieldList = item.getText(1).trim();
        String indexOpp = item.getText(2).trim();
        String unique = item.getText(3).trim();
        String sparse = item.getText(4).trim();
        
        MongoDbOutputMeta.MongoIndex newIndex = new MongoDbOutputMeta.MongoIndex();
        newIndex.m_pathToFields = indexFieldList;
        newIndex.m_drop = indexOpp.equals("Drop");
        newIndex.m_unique = unique.equals("Y");
        newIndex.m_sparse = sparse.equals("Y");
        
        mongoIndexes.add(newIndex);
      }      
    } 
    m_currentMeta.setMongoIndexes(mongoIndexes);
    
    if (!m_originalMeta.equals(m_currentMeta)) {
      m_currentMeta.setChanged();
      changed = m_currentMeta.hasChanged();
    }
        
    
    dispose();
  }
  
  private List<MongoDbOutputMeta.MongoField> tableToMongoFieldList() {
    int numNonEmpty = m_mongoFieldsView.nrNonEmpty();
    if (numNonEmpty > 0) {
      List<MongoDbOutputMeta.MongoField> mongoFields = 
        new ArrayList<MongoDbOutputMeta.MongoField>();
      
      for (int i = 0; i < numNonEmpty; i++) {
        TableItem item = m_mongoFieldsView.getNonEmpty(i);
        String incoming = item.getText(1).trim();
        String path = item.getText(2).trim();
        String useIncoming = item.getText(3).trim();
        String updateMatch = item.getText(4).trim();
        String modifierOp = item.getText(5).trim();
        
        MongoDbOutputMeta.MongoField newField = new MongoDbOutputMeta.MongoField();
        newField.m_incomingFieldName = incoming;
        newField.m_mongoDocPath = path;
        newField.m_useIncomingFieldNameAsMongoFieldName = ((useIncoming.length() > 0) ? 
            useIncoming.equals("Y") : true);
        newField.m_updateMatchField = (updateMatch.equals("Y"));
        if (modifierOp.length() == 0) {
          newField.m_modifierUpdateOperation = "N/A";
        } else {
          newField.m_modifierUpdateOperation = modifierOp;
        }
        mongoFields.add(newField);
      }
      
      return mongoFields;
    }
    
    return null;
  }
  
  private void getData() {
    m_hostnameField.setText(Const.NVL(m_currentMeta.getHostname(), ""));
    m_portField.setText(Const.NVL(m_currentMeta.getPort(), ""));    
    m_usernameField.setText(Const.NVL(m_currentMeta.getUsername(), ""));
    m_passField.setText(Const.NVL(m_currentMeta.getPassword(), ""));
    m_dbNameField.setText(Const.NVL(m_currentMeta.getDBName(), ""));
    m_collectionField.setText(Const.NVL(m_currentMeta.getCollection(), ""));
    m_batchInsertSizeField.setText(Const.NVL(m_currentMeta.getBatchInsertSize(), ""));
    m_upsertBut.setSelection(m_currentMeta.getUpsert());
    m_multiBut.setSelection(m_currentMeta.getMulti());
    m_truncateBut.setSelection(m_currentMeta.getTruncate());
    m_modifierUpdateBut.setSelection(m_currentMeta.getModifierUpdate());
    
    m_modifierUpdateBut.setEnabled(m_upsertBut.getSelection());
    m_multiBut.setEnabled(m_upsertBut.getSelection());
    if (!m_upsertBut.getSelection()) {
      m_modifierUpdateBut.setSelection(false);
      m_multiBut.setSelection(false);
    }
    m_multiBut.setEnabled(m_modifierUpdateBut.getSelection());
    if (!m_multiBut.getEnabled()) {
      m_multiBut.setSelection(false);
    }
    
    List<MongoDbOutputMeta.MongoField> mongoFields = m_currentMeta.getMongoFields();
    
    if (mongoFields != null && mongoFields.size() > 0) {
      for (MongoDbOutputMeta.MongoField field : mongoFields) {
        TableItem item = new TableItem(m_mongoFieldsView.table, SWT.NONE);
        
        item.setText(1, Const.NVL(field.m_incomingFieldName, ""));
        item.setText(2, Const.NVL(field.m_mongoDocPath, ""));
        item.setText(3, Const.NVL(field.m_useIncomingFieldNameAsMongoFieldName ? "Y" : "N", ""));
        item.setText(4, Const.NVL(field.m_updateMatchField ? "Y" : "N", ""));
        item.setText(5, Const.NVL(field.m_modifierUpdateOperation, ""));
      }
      
      m_mongoFieldsView.removeEmptyRows();
      m_mongoFieldsView.setRowNums();
      m_mongoFieldsView.optWidth(true);
    }
        
    List<MongoDbOutputMeta.MongoIndex> mongoIndexes = m_currentMeta.getMongoIndexes();
    
    if (mongoIndexes != null && mongoIndexes.size() > 0) {
      for (MongoDbOutputMeta.MongoIndex index : mongoIndexes) {
        TableItem item = new TableItem(m_mongoIndexesView.table, SWT.None);
        
        item.setText(1, Const.NVL(index.m_pathToFields, ""));
        if (index.m_drop) {
          item.setText(2, "Drop");
        } else {
          item.setText(2, "Create");
        }
        
        item.setText(3, Const.NVL(index.m_unique ? "Y" : "N", "N"));
        item.setText(4, Const.NVL(index.m_sparse ? "Y" : "N", "N"));
      }
      
      m_mongoIndexesView.removeEmptyRows();
      m_mongoIndexesView.setRowNums();
      m_mongoIndexesView.optWidth(true);
    }
  }
  
  private void checkPasswordVisible() {
    String password = m_passField.getText();
    ArrayList<String> list = new ArrayList<String>();
    StringUtil.getUsedVariables(password, list, true);
    if (list.size() == 0) {
      m_passField.setEchoChar('*');      
    } else {
      m_passField.setEchoChar('\0'); // show everything
    }
  }
  
  private void setupCollectionNamesForDB(boolean quiet) {
    
    String hostname = transMeta.environmentSubstitute(m_hostnameField.getText());
    String portS = transMeta.environmentSubstitute(m_portField.getText());
    String dB = transMeta.environmentSubstitute(m_dbNameField.getText());
    String username = transMeta.environmentSubstitute(m_usernameField.getText());
    String realPass = Encr.
      decryptPasswordOptionallyEncrypted(transMeta.environmentSubstitute(m_passField.getText()));
    
    if (Const.isEmpty(dB)) {
      return;
    }
    
    m_collectionField.removeAll();
    
    if (!Const.isEmpty(hostname)) {
      int port = 27017;
      
      if (!Const.isEmpty(portS)) {
        port = Integer.parseInt(portS);
      }
      
      try {
        Mongo conn = new Mongo(hostname, port);
        DB theDB = conn.getDB(dB);
        
        if (!Const.isEmpty(username) || !Const.isEmpty(realPass)) {
          if (!theDB.authenticate(username, realPass.toCharArray())) {
            throw new Exception("Unable to authenticate to database!");
          }
        }

        Set<String> collections = theDB.getCollectionNames();
        for (String c : collections) {
          m_collectionField.add(c);
        }        
        
        conn.close();
        conn = null;        
      } catch (Exception e) {
        new ErrorDialog(shell, BaseMessages.getString(PKG, "MongoDbInputDialog.ErrorMessage." +
          "UnableToConnect"),
          BaseMessages.getString(PKG, "MongoDbInputDialog.ErrorMessage.UnableToConnect"), e);
        e.printStackTrace();
      }
      
    }
  }
  
  private void setupDBNames() {
    m_dbNameField.removeAll();
    
    String hostname = transMeta.environmentSubstitute(m_hostnameField.getText());
    String portS = transMeta.environmentSubstitute(m_portField.getText());
    
    if (!Const.isEmpty(hostname)) {
      int port = 27017;
      
      if (!Const.isEmpty(portS)) {
        port = Integer.parseInt(portS);
      }
      
      try {
        Mongo conn = new Mongo(hostname, port);
        List<String> dbNames = conn.getDatabaseNames();
    
        for (String s : dbNames) {
          m_dbNameField.add(s);
        }
        
        conn.close();
        conn = null;
      } catch (Exception e) {
        new ErrorDialog(shell, BaseMessages.getString(PKG, "MongoDbInputDialog.ErrorMessage." +
          "UnableToConnect"),
        BaseMessages.getString(PKG, "MongoDbInputDialog.ErrorMessage.UnableToConnect"), e);
        e.printStackTrace();
      }
    }    
  }
  
  private void getFields() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields(stepname);
      if (r != null) {
        BaseStepDialog.getFieldsFromPrevious(r, m_mongoFieldsView, 1, new int[] {1}, null, -1, -1, null);
      }
    } catch (KettleException e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Title"), 
          BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"), e);
    }
  }
  
  private void previewDocStruct() {
    List<MongoDbOutputMeta.MongoField> mongoFields = tableToMongoFieldList();
    
    if (mongoFields == null || mongoFields.size() == 0) {
      return;
    }
      
    // Try and get meta data on incoming fields
    RowMetaInterface actualR = null;
    RowMetaInterface r;
    boolean gotGenuineRowMeta = false;
    try {
      actualR = transMeta.getPrevStepFields(stepname);
      gotGenuineRowMeta = true;
    } catch (KettleException e) {
      // don't complain if we can't
    }    
    r = new RowMeta();

    Object[] dummyRow = new Object[mongoFields.size()];
    int i = 0;
    for (MongoDbOutputMeta.MongoField field : mongoFields) {
      // set up dummy row meta
      ValueMetaInterface vm = new ValueMeta();
      vm.setName(field.m_incomingFieldName);
      vm.setType(ValueMetaInterface.TYPE_STRING);
      r.addValueMeta(vm);
      
      String val = "";
      if (gotGenuineRowMeta && actualR.indexOfValue(field.m_incomingFieldName) >= 0) {
        int index = actualR.indexOfValue(field.m_incomingFieldName);
        switch (actualR.getValueMeta(index).getType()) {
        case ValueMetaInterface.TYPE_STRING :
          val = "<string val>";
          break;
        case ValueMetaInterface.TYPE_INTEGER :
          val = "<integer val>";
          break;
        case ValueMetaInterface.TYPE_NUMBER :
          val = "<number val>";
          break;
        case ValueMetaInterface.TYPE_BOOLEAN :
          val = "<bool val>";
          break;
        case ValueMetaInterface.TYPE_DATE :
          val = "<date val>";
          break;
        case ValueMetaInterface.TYPE_BINARY :
          val = "<binary val>";
          break;
          default :
            val = "<unsupported value type>";            
        }        
      } else {
        val = "<value>";
      }
      
      dummyRow[i++] = val;
    }
    
    VariableSpace vs = new Variables();
    MongoDbOutputData.MongoTopLevel topLevelStruct = 
      MongoDbOutputData.checkTopLevelConsistency(mongoFields, vs);
    for (MongoDbOutputMeta.MongoField m : mongoFields) {
      m.init(vs);
    }
    try {
      DBObject result = 
        MongoDbOutputData.kettleRowToMongo(mongoFields, r, dummyRow, vs, topLevelStruct);

      ShowMessageDialog smd = new ShowMessageDialog(shell, 
          SWT.ICON_INFORMATION | SWT.OK, "Output Mongo document structure", result.toString(), true);
      smd.open();
    } catch (Exception ex) {
      MessageDialog.openError(shell, 
          BaseMessages.getString(PKG, "MongoDbInputDialog.ErrorMessage.ProblemPreviewingDocStructure.Title"), 
          BaseMessages.getString(PKG, "MongoDbInputDialog.ErrorMessage.ProblemPreviewingDocStructure.Message") 
          + ":\n\n" 
          + ex.getMessage());
      ex.printStackTrace();
      return;
    }        
  }
  
  private void showIndexInfo() {
    String hostname = transMeta.environmentSubstitute(m_hostnameField.getText());
    int port = Const.toInt(transMeta.environmentSubstitute(m_portField.getText()), 27017);
    String dbName = transMeta.environmentSubstitute(m_dbNameField.getText());
    String collection = transMeta.environmentSubstitute(m_collectionField.getText());
    
    try {
      Mongo mongo = new Mongo(hostname, port);
      DB db = mongo.getDB(dbName);
      
      if (db == null) {
        throw new Exception(BaseMessages.getString(PKG, "MongoDbInputDialog.ErrorMessage.NonExistentDB", 
            dbName));
      }
      
      String realUser = transMeta.environmentSubstitute(m_usernameField.getText());
      String realPass = Encr.
        decryptPasswordOptionallyEncrypted(transMeta.environmentSubstitute(m_passField.getText()));
      
      if (!Const.isEmpty(realUser) || !Const.isEmpty(realPass)) {
        if (!db.authenticate(realUser, realPass.toCharArray())) {
          throw new Exception(BaseMessages.getString(PKG, 
              "MongoDbInputDialog.ErrorMessage.UnableToAuthenticate"));
        }
      }
      
      if (Const.isEmpty(collection)) {
        throw new Exception(BaseMessages.getString(PKG, 
            "MongoDbInputDialog.ErrorMessage.NoCollectionSpecified"));
      }
      
      if (!db.collectionExists(collection)) {
        db.createCollection(collection, null);
      }
      
      DBCollection coll = db.getCollection(collection);
      if (coll == null) {
        throw new Exception(BaseMessages.getString(PKG, 
            "MongoDbInputDialog.ErrorMessage.UnableToGetInfoForCollection", collection));
      }
      
      List<DBObject> collInfo = coll.getIndexInfo();
      StringBuffer result = new StringBuffer();
      if (collInfo == null || collInfo.size() == 0) {
        result.append(BaseMessages.getString(PKG, 
            "MongoDbInputDialog.ErrorMessage.UnableToGetInfoForCollection", collection));
      }
      for (DBObject index : collInfo) {
        result.append(index).append("\n\n");
      }
      
      ShowMessageDialog smd = new ShowMessageDialog(shell, 
          SWT.ICON_INFORMATION | SWT.OK, BaseMessages.getString(PKG, "MongoDbOutputDialog.IndexInfo", 
              collection), result.toString(), true);
      smd.open();
    } catch (UnknownHostException e) {
      MessageDialog.openError(shell, 
          BaseMessages.getString(PKG, "MongoDbInputDialog.ErrorMessage.IndexPreview.Title"), 
          BaseMessages.getString(PKG, "MongoDbInputDialog.ErrorMessage.UnknownHost.Message", hostname) 
          + ":\n\n" 
          + e.getMessage());
      e.printStackTrace();
      return;
    } catch (MongoException e) {
      MessageDialog.openError(shell, 
          BaseMessages.getString(PKG, "MongoDbInputDialog.ErrorMessage.IndexPreview.Title"), 
          BaseMessages.getString(PKG, "MongoDbInputDialog.ErrorMessage.MongoException.Message") 
          + ":\n\n" 
          + e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      MessageDialog.openError(shell, 
          BaseMessages.getString(PKG, "MongoDbInputDialog.ErrorMessage.IndexPreview.Title"), 
          BaseMessages.getString(PKG, "MongoDbInputDialog.ErrorMessage.GeneralError.Message") 
          + ":\n\n" 
          + e.getMessage());
      e.printStackTrace();
    }    
  }
}
