/*************************************************************************************** 
 * Copyright (C) 2007 webdetails.  All rights reserved. 
 * This software was developed by webdetails and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is webdetails.  
 * The Initial Developer is webdetails.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/
package org.pentaho.di.ui.trans.steps.elasticsearchbulk;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.status.IndicesStatusResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.action.admin.cluster.state.ClusterStateRequestBuilder;
import org.elasticsearch.client.action.admin.indices.status.IndicesStatusRequestBuilder;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.discovery.MasterNotDiscoveredException;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.elasticsearchbulk.ElasticSearchBulkMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.LabelComboVar;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class ElasticSearchBulkDialog extends BaseStepDialog implements StepDialogInterface
{
  
  private ElasticSearchBulkMeta model;

  private static Class<?> PKG = ElasticSearchBulkMeta.class;
  private CTabFolder wTabFolder;
  private FormData fdTabFolder;
  private CTabItem wGeneralTab;
  private Composite wGeneralComp;
  private FormData fdGeneralComp;
  private Label wlBatchSize;

  private TextVar wBatchSize;
  private LabelTextVar wIdOutField;
  private Group wIndexGroup;
  private FormData fdIndexGroup;
  private Group wSettingsGroup;
  private FormData fdSettingsGroup;
  
  private String[] fieldNames;
  
  private CTabItem wFieldsTab;
  
  private LabelTextVar wIndex;
  private LabelTextVar wType;
  
  private ModifyListener lsMod;

  private Button wIsJson;

  private Label wlIsJson;

  private Label wlUseOutput;

  private Button wUseOutput;

  private LabelComboVar wJsonField;

  private TableView wFields;

  private CTabItem wServersTab;

  private TableView wServers;

  private CTabItem wSettingsTab;

  private TableView wSettings;

  private LabelTimeComposite wTimeOut;

  private Label wlStopOnError;

  private Button wStopOnError;

  private Button wTest;

  private Button wTestCl;

  private LabelComboVar wIdInField;

  private Button wIsOverwrite;

  private Label wlIsOverwrite;
  

  public ElasticSearchBulkDialog(Shell parent, Object in, TransMeta transMeta, String sname)
  {
    super(parent, (BaseStepMeta) in, transMeta, sname);
    model = (ElasticSearchBulkMeta) in;
  }

  public String open()
  {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX
            | SWT.MIN);
    props.setLook(shell);
    setShellImage(shell, model);

    lsMod = new ModifyListener()
    {

      public void modifyText(ModifyEvent e)
      {
        model.setChanged();
      }
    };

    changed = model.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.DialogTitle"));

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label(shell, SWT.RIGHT);
    wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
    props.setLook(wlStepname);
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment(0, 0);
    fdlStepname.top = new FormAttachment(0, margin);
    fdlStepname.right = new FormAttachment(middle, -margin);
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

    wTabFolder = new CTabFolder(shell, SWT.BORDER);
    props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

    //  GENERAL TAB 
    addGeneralTab();
    
    // Servers TAB 
    addServersTab();
    
    // Fields TAB 
    addFieldsTab();
   
    // Settings TAB 
    addSettingsTab();
    
    //////////////
    // BUTTONS //
    ////////////
    wOK = new Button(shell, SWT.PUSH);
    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
    wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

    setButtonPositions(new Button[]
            {
              wOK, wCancel
            }, margin, null);

    fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment(0, 0);
    fdTabFolder.top = new FormAttachment(wStepname, margin);
    fdTabFolder.right = new FormAttachment(100, 0);
    fdTabFolder.bottom = new FormAttachment(wOK, -margin);
    wTabFolder.setLayoutData(fdTabFolder);

    ////////////////////
    // Std Listeners //
    //////////////////
    addStandardListeners();

    wTabFolder.setSelection(0);

    // Set the shell size, based upon previous time...
    setSize();
    getData(model);
    model.setChanged(changed);

    shell.open();
    while (!shell.isDisposed())
    {
      if (!display.readAndDispatch())
      {
        display.sleep();
      }
    }
    return stepname;
  }
  
  private void addStandardListeners() {
    // Add listeners
    lsOK = new Listener()
    {
      public void handleEvent(Event e)
      {
        ok();
      }
    };

    lsCancel = new Listener()
    {

      public void handleEvent(Event e)
      {
        cancel();
      }
    };
    
    lsMod = new ModifyListener()
    {
      public void modifyText(ModifyEvent event) {
        model.setChanged();
      } 
    };

    wOK.addListener(SWT.Selection, lsOK);
    wCancel.addListener(SWT.Selection, lsCancel);

    lsDef = new SelectionAdapter()
    {
      public void widgetDefaultSelected(SelectionEvent e)
      {
        ok();
      }
    };

    wStepname.addSelectionListener(lsDef);

    // window close
    shell.addShellListener(new ShellAdapter()
    {

      public void shellClosed(ShellEvent e)
      {
        cancel();
      }
    });
  }

  /**
   */
  private void addGeneralTab() {
    wGeneralTab = new CTabItem(wTabFolder, SWT.NONE);
    wGeneralTab.setText(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.General.Tab"));

    wGeneralComp = new Composite(wTabFolder, SWT.NONE);
    props.setLook(wGeneralComp);

    FormLayout generalLayout = new FormLayout();
    generalLayout.marginWidth = 3;
    generalLayout.marginHeight = 3;
    wGeneralComp.setLayout(generalLayout);

    // Index GROUP 
    fillIndexGroup(wGeneralComp);

    // Options GROUP
    fillOptionsGroup(wGeneralComp);

    fdGeneralComp = new FormData();
    fdGeneralComp.left = new FormAttachment(0, 0);
    fdGeneralComp.top = new FormAttachment(wStepname, Const.MARGIN);
    fdGeneralComp.right = new FormAttachment(100, 0);
    fdGeneralComp.bottom = new FormAttachment(100, 0);
    wGeneralComp.setLayoutData(fdGeneralComp);

    wGeneralComp.layout();
    wGeneralTab.setControl(wGeneralComp);
  }

  private void fillIndexGroup(Composite parentTab) {
    wIndexGroup = new Group(parentTab, SWT.SHADOW_NONE);
    props.setLook(wIndexGroup);
    wIndexGroup.setText(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.IndexGroup.Label"));

    FormLayout indexGroupLayout = new FormLayout();
    indexGroupLayout.marginWidth = 10;
    indexGroupLayout.marginHeight = 10;
    wIndexGroup.setLayout(indexGroupLayout);
    
    //Index
    wIndex = new LabelTextVar(transMeta, wIndexGroup, BaseMessages.getString(PKG, "ElasticSearchBulkDialog.Index.Label"), BaseMessages.getString(PKG, "ElasticSearchBulkDialog.Index.Tooltip"));
    wIndex.addModifyListener(lsMod);
    
    //Type
    wType = new LabelTextVar(transMeta, wIndexGroup, BaseMessages.getString(PKG, "ElasticSearchBulkDialog.Type.Label"), BaseMessages.getString(PKG, "ElasticSearchBulkDialog.Type.Tooltip"));
    wType.addModifyListener(lsMod);
    
    //Test button
    wTest=new Button(wIndexGroup,SWT.PUSH);
    wTest.setText(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.TestIndex.Label"));
    wTest.setToolTipText(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.TestIndex.Tooltip"));
    
    wTest.addListener(SWT.Selection, new Listener() {
      
      public void handleEvent(Event arg0) {
        test(TestType.INDEX);
      }
    });
    
    Control[] connectionControls = new Control[]{ wIndex, wType };
    placeControls(wIndexGroup, connectionControls); 
    
    BaseStepDialog.positionBottomButtons(wIndexGroup, new Button[] {wTest}, Const.MARGIN, wType);
    
    fdIndexGroup = new FormData();
    fdIndexGroup.left = new FormAttachment(0, Const.MARGIN);
    fdIndexGroup.top = new FormAttachment(wStepname, Const.MARGIN);
    fdIndexGroup.right = new FormAttachment(100, -Const.MARGIN);
    wIndexGroup.setLayoutData(fdIndexGroup);
  }

  private void addServersTab(){
    wServersTab = new CTabItem(wTabFolder, SWT.NONE);
    wServersTab.setText(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.ServersTab.TabTitle"));
    
    FormLayout serversLayout = new FormLayout ();
    serversLayout.marginWidth  = Const.FORM_MARGIN;
    serversLayout.marginHeight = Const.FORM_MARGIN;
    
    Composite wServersComp = new Composite(wTabFolder, SWT.NONE);
    wServersComp.setLayout(serversLayout);
    props.setLook(wServersComp);
    
    //Test button
    wTestCl=new Button(wServersComp,SWT.PUSH);
    wTestCl.setText(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.TestCluster.Label"));
    wTestCl.setToolTipText(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.TestCluster.Tooltip"));
    
    wTestCl.addListener(SWT.Selection, new Listener() {
      
      public void handleEvent(Event arg0) {
        test(TestType.CLUSTER);
      }
    });

    setButtonPositions(new Button[] { wTestCl }, Const.MARGIN, null);
    
    ColumnInfo[] columnsMeta = new ColumnInfo[2];
    columnsMeta[0]=new ColumnInfo(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.ServersTab.Address.Column"), ColumnInfo.COLUMN_TYPE_TEXT, false);
    columnsMeta[1]=new ColumnInfo(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.ServersTab.Port.Column"),  ColumnInfo.COLUMN_TYPE_TEXT, true);
    
    wServers = new TableView(transMeta, wServersComp, 
        SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
        columnsMeta, 
        1,  
        lsMod,
        props
        );
    FormData fdServers = new FormData();
    fdServers.left  = new FormAttachment(0, Const.MARGIN);
    fdServers.top   = new FormAttachment(0, Const.MARGIN);
    fdServers.right = new FormAttachment(100, -Const.MARGIN);
    fdServers.bottom= new FormAttachment(wTestCl, -Const.MARGIN);
    wServers.setLayoutData(fdServers);

    FormData fdServersComp = new FormData();
    fdServersComp.left  = new FormAttachment(0, 0);
    fdServersComp.top   = new FormAttachment(0, 0);
    fdServersComp.right = new FormAttachment(100, 0);
    fdServersComp.bottom= new FormAttachment(100, 0);
    wServersComp.setLayoutData(fdServersComp);
    wServersComp.layout();
    wServersTab.setControl(wServersComp);
  }
  
  private void addSettingsTab(){
    wSettingsTab = new CTabItem(wTabFolder, SWT.NONE);
    wSettingsTab.setText(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.SettingsTab.TabTitle"));
    
    FormLayout serversLayout = new FormLayout ();
    serversLayout.marginWidth  = Const.FORM_MARGIN;
    serversLayout.marginHeight = Const.FORM_MARGIN;
    
    Composite wSettingsComp = new Composite(wTabFolder, SWT.NONE);
    wSettingsComp.setLayout(serversLayout);
    props.setLook(wSettingsComp);
    
    ColumnInfo[] columnsMeta = new ColumnInfo[2];
    columnsMeta[0]=new ColumnInfo(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.SettingsTab.Property.Column"), ColumnInfo.COLUMN_TYPE_TEXT, false);
    columnsMeta[1]=new ColumnInfo(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.SettingsTab.Value.Column"),  ColumnInfo.COLUMN_TYPE_TEXT, false);
    
    wSettings = new TableView(transMeta, wSettingsComp, 
        SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
        columnsMeta, 
        1,  
        lsMod,
        props
        );
    FormData fdServers = new FormData();
    fdServers.left  = new FormAttachment(0, Const.MARGIN);
    fdServers.top   = new FormAttachment(0, Const.MARGIN);
    fdServers.right = new FormAttachment(100, -Const.MARGIN);
    fdServers.bottom= new FormAttachment(100, -Const.MARGIN);
    wSettings.setLayoutData(fdServers);

    FormData fdServersComp = new FormData();
    fdServersComp.left  = new FormAttachment(0, 0);
    fdServersComp.top   = new FormAttachment(0, 0);
    fdServersComp.right = new FormAttachment(100, 0);
    fdServersComp.bottom= new FormAttachment(100, 0);
    wSettingsComp.setLayoutData(fdServersComp);

    wSettingsComp.layout();
    wSettingsTab.setControl(wSettingsComp);
  }
  
  
  private void addFieldsTab() {
    
    wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
    wFieldsTab.setText(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.FieldsTab.TabTitle"));
    
    FormLayout fieldsLayout = new FormLayout ();
    fieldsLayout.marginWidth  = Const.FORM_MARGIN;
    fieldsLayout.marginHeight = Const.FORM_MARGIN;
    
    Composite wFieldsComp = new Composite(wTabFolder, SWT.NONE);
    wFieldsComp.setLayout(fieldsLayout);
    props.setLook(wFieldsComp);

    wGet=new Button(wFieldsComp, SWT.PUSH);
    wGet.setText(BaseMessages.getString(PKG, "System.Button.GetFields"));
    wGet.setToolTipText(BaseMessages.getString(PKG, "System.Tooltip.GetFields"));

    lsGet = new Listener() { public void handleEvent(Event e) { getPreviousFields(wFields); } };
    wGet.addListener(SWT.Selection, lsGet);
    
    setButtonPositions(new Button[] { wGet }, Const.MARGIN, null);

    final int fieldsRowCount = model.getFields().keySet().size();
    
    String[] names = this.fieldNames != null ? this.fieldNames : new String[]{""};
    ColumnInfo[] columnsMeta = new ColumnInfo[2];
    columnsMeta[0]=new ColumnInfo(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.NameColumn.Column"), ColumnInfo.COLUMN_TYPE_CCOMBO, names, false);
    columnsMeta[1]=new ColumnInfo(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.TargetNameColumn.Column"),  ColumnInfo.COLUMN_TYPE_TEXT,   false);
    
    wFields = new TableView(transMeta, wFieldsComp, 
                          SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
                          columnsMeta, 
                          fieldsRowCount,  
                          lsMod,
                          props
                          );

    FormData fdFields = new FormData();
    fdFields.left  = new FormAttachment(0, Const.MARGIN);
    fdFields.top   = new FormAttachment(0, Const.MARGIN);
    fdFields.right = new FormAttachment(100, -Const.MARGIN);
    fdFields.bottom= new FormAttachment(wGet, -Const.MARGIN);
    wFields.setLayoutData(fdFields);

    FormData fdFieldsComp = new FormData();
    fdFieldsComp.left  = new FormAttachment(0, 0);
    fdFieldsComp.top   = new FormAttachment(0, 0);
    fdFieldsComp.right = new FormAttachment(100, 0);
    fdFieldsComp.bottom= new FormAttachment(100, 0);
    wFieldsComp.setLayoutData(fdFieldsComp);

    wFieldsComp.layout();
    wFieldsTab.setControl(wFieldsComp);
  }

  
  private void fillOptionsGroup(Composite parentTab) {

    int margin = Const.MARGIN;
    
    wSettingsGroup = new Group(parentTab, SWT.SHADOW_NONE);
    props.setLook(wSettingsGroup);
    wSettingsGroup.setText(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.SettingsGroup.Label"));

    FormLayout settingGroupLayout = new FormLayout();
    settingGroupLayout.marginWidth = 10;
    settingGroupLayout.marginHeight = 10;
    wSettingsGroup.setLayout(settingGroupLayout);


    // Timeout
    wTimeOut = new LabelTimeComposite( wSettingsGroup, 
        BaseMessages.getString(PKG, "ElasticSearchBulkDialog.TimeOut.Label"), 
        BaseMessages.getString(PKG, "ElasticSearchBulkDialog.TimeOut.Tooltip"));
    props.setLook(wTimeOut);
    wTimeOut.addModifyListener(lsMod);
    
    // BatchSize
    wlBatchSize = new Label(wSettingsGroup, SWT.RIGHT);
    wlBatchSize.setText(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.BatchSize.Label"));
    props.setLook(wlBatchSize);

    wBatchSize = new TextVar(transMeta, wSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);

    props.setLook(wBatchSize);
    wBatchSize.addModifyListener(lsMod);
    
    //Stop on error
    wlStopOnError = new Label(wSettingsGroup, SWT.RIGHT);
    wlStopOnError.setText( BaseMessages.getString(PKG, "ElasticSearchBulkDialog.StopOnError.Label"));
    
    wStopOnError = new Button(wSettingsGroup, SWT.CHECK | SWT.RIGHT);
    wStopOnError.setToolTipText( BaseMessages.getString(PKG, "ElasticSearchBulkDialog.StopOnError.Tooltip"));
    
    //ID input
    wIdInField = new LabelComboVar(transMeta, wSettingsGroup, 
        BaseMessages.getString(PKG, "ElasticSearchBulkDialog.IdField.Label"), 
        BaseMessages.getString(PKG, "ElasticSearchBulkDialog.IdField.Tooltip"));
    props.setLook(wIdInField);
    wIdInField.getComboWidget().setEditable(true);
    wIdInField.addModifyListener(lsMod);
    wIdInField.addFocusListener(new FocusListener() {
      public void focusLost(org.eclipse.swt.events.FocusEvent e) {
      }
      public void focusGained(org.eclipse.swt.events.FocusEvent e) {
        getPreviousFields(wIdInField);
      }
    });
    getPreviousFields(wIdInField);
    
    
    wlIsOverwrite = new Label(wSettingsGroup, SWT.RIGHT);
    wlIsOverwrite.setText(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.Overwrite.Label"));
    
    wIsOverwrite = new Button(wSettingsGroup, SWT.CHECK | SWT.RIGHT);
    wIsOverwrite.setToolTipText(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.Overwrite.Tooltip"));
    
    //Output rows
    wlUseOutput = new Label(wSettingsGroup, SWT.RIGHT);
    wlUseOutput.setText( BaseMessages.getString(PKG, "ElasticSearchBulkDialog.UseOutput.Label") );
    
    wUseOutput = new Button(wSettingsGroup, SWT.CHECK | SWT.RIGHT);
    wUseOutput.setToolTipText(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.UseOutput.Tooltip"));
    
    wUseOutput.addSelectionListener(new SelectionListener(){
      
      public void widgetDefaultSelected(SelectionEvent arg0) {
        widgetSelected(arg0);
      }

      public void widgetSelected(SelectionEvent arg0) {
        wIdOutField.setEnabled(wUseOutput.getSelection());
      }
      
    });
    
    //ID out field
    wIdOutField = new LabelTextVar(transMeta, wSettingsGroup, 
        BaseMessages.getString(PKG, "ElasticSearchBulkDialog.IdOutField.Label"), 
        BaseMessages.getString(PKG, "ElasticSearchBulkDialog.IdOutField.Tooltip"));
    props.setLook(wIdOutField);
    wIdOutField.setEnabled(wUseOutput.getSelection());
    wIdOutField.addModifyListener(lsMod);
    
    //use json
    wlIsJson = new Label(wSettingsGroup, SWT.RIGHT);
    wlIsJson.setText(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.IsJson.Label"));
    
    wIsJson = new Button(wSettingsGroup, SWT.CHECK | SWT.RIGHT);
    wIsJson.setToolTipText(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.IsJson.Tooltip"));
    
    wIsJson.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent arg0) {
        widgetSelected(arg0);
      }
      public void widgetSelected(SelectionEvent arg0) {
        wJsonField.setEnabled( wIsJson.getSelection() );
        wFields.setEnabled(!wIsJson.getSelection());
        wFields.setVisible(!wIsJson.getSelection());
        wGet.setEnabled(!wIsJson.getSelection());
      }
    });
    
    //Json field
    wJsonField = new LabelComboVar(transMeta, wSettingsGroup, 
        BaseMessages.getString(PKG, "ElasticSearchBulkDialog.JsonField.Label"), 
        BaseMessages.getString(PKG, "ElasticSearchBulkDialog.JsonField.Tooltip"));
    wJsonField.getComboWidget().setEditable(true);
    props.setLook(wJsonField);
    wJsonField.addModifyListener(lsMod);
    wJsonField.addFocusListener(new FocusListener() {
      public void focusLost(org.eclipse.swt.events.FocusEvent e) {
      }
      public void focusGained(org.eclipse.swt.events.FocusEvent e) {
        getPreviousFields(wJsonField);
      }
    });
    getPreviousFields(wJsonField);
    wJsonField.setEnabled( wIsJson.getSelection() );
    
    Control[] settingsControls = new Control[]{
        wlBatchSize, wBatchSize,
        wlStopOnError, wStopOnError,
        wTimeOut,
        wIdInField,
        wlIsOverwrite, wIsOverwrite,
        wlUseOutput, wUseOutput,
        wIdOutField,
        wlIsJson, wIsJson,
        wJsonField
    };
    placeControls(wSettingsGroup, settingsControls);

    fdSettingsGroup = new FormData();
    fdSettingsGroup.left = new FormAttachment(0, margin);
    fdSettingsGroup.top = new FormAttachment(wIndexGroup, margin);
    fdSettingsGroup.right = new FormAttachment(100, -margin);
    wSettingsGroup.setLayoutData(fdSettingsGroup);
  }

  private void getPreviousFields(LabelComboVar combo) {
    String value = combo.getText();
    combo.removeAll();
    combo.setItems(getInputFieldNames());
    if (value != null) {
      combo.setText(value);
    }
  }
  
  private String[] getInputFieldNames(){
    if (this.fieldNames == null) {
      try {
        RowMetaInterface r = transMeta.getPrevStepFields(stepname);
        if (r != null) {
          fieldNames = r.getFieldNames();
        }
      } catch (KettleException ke) {
        new ErrorDialog(shell, BaseMessages.getString(PKG, "ElasticSearchBulkDialog.FailedToGetFields.DialogTitle"), //$NON-NLS-1$
            BaseMessages.getString(PKG, "ElasticSearchBulkDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$
        return new String[0];
      }
    }
    
    return fieldNames;
  }
  
  private void getPreviousFields(TableView table) {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields(stepname);
      if (r != null) {
        BaseStepDialog.getFieldsFromPrevious(r, table, 1, new int[] { 1,2 }, null, 0, 0, null);
      }
    } catch (KettleException ke) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Title"), BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"), ke);
    }
  }
  
  private void placeControls(Group group, Control[] controls ){
    
    Control previousAbove = group;
    Control previousLeft = group;
    
    for(Control control : controls){
      if( control instanceof Label ){
        addLabelAfter(control, previousAbove);
        previousLeft = control;
      }
      else {
        addWidgetAfter(control, previousAbove, previousLeft);
        previousAbove = control;
        previousLeft = group;
      }
    }
    
  }
  
  private void addWidgetAfter(Control widget, Control widgetAbove, Control widgetLeft){
    props.setLook(widget);
    FormData fData = new FormData();
    fData.left = new FormAttachment(widgetLeft, Const.MARGIN);
    fData.top = new FormAttachment(widgetAbove, Const.MARGIN);
    fData.right = new FormAttachment(100, - Const.MARGIN);
    widget.setLayoutData(fData);
  }
  
  private void addLabelAfter(Control widget, Control widgetAbove){
    props.setLook(widget);
    FormData fData = new FormData();
    fData.top = new FormAttachment(widgetAbove, Const.MARGIN);
    fData.right = new FormAttachment(Const.MIDDLE_PCT, - Const.MARGIN);
    widget.setLayoutData(fData);
  }
  
 

  /**
   * Read the data from the ElasticSearchBulkMeta object and show it in this
   * dialog.
   *
   * @param in The ElasticSearchBulkMeta object to obtain the data from.
   */
  public void getData(ElasticSearchBulkMeta in)
  {
    wStepname.selectAll();
    
    wIndex.setText(Const.NVL(in.getIndex(),""));
    wType.setText(Const.NVL(in.getType(),""));
    
    wBatchSize.setText(Const.NVL(in.getBatchSize(), "" + ElasticSearchBulkMeta.DEFAULT_BATCH_SIZE));
    
    wStopOnError.setSelection(in.isStopOnError());
    
    wTimeOut.setText(Const.NVL(in.getTimeOut(), ""));
    wTimeOut.setTimeUnit(in.getTimeoutUnit());
    
    wIdInField.setText(Const.NVL(in.getIdInField(), ""));
    
    wIsOverwrite.setSelection( in.isOverWriteIfSameId() );
    
    wIsJson.setSelection(in.isJsonInsert());
    
    wJsonField.setText( Const.NVL(in.getJsonField(), "")); 
    wJsonField.setEnabled(wIsJson.getSelection());//listener not working here
    
    wUseOutput.setSelection(in.isUseOutput());
    
    wIdOutField.setText( Const.NVL(in.getIdOutField(), "") ); 
    wIdOutField.setEnabled(wUseOutput.getSelection());//listener not working here
    
    //Fields
    mapToTableView(model.getFields(), wFields);
    
    //Servers
    for(InetSocketTransportAddress server : model.getServers()){
      String addr = server.address().getAddress().getHostAddress();
      int port = server.address().getPort(); 
      wServers.add(addr, "" + port);
    }
    wServers.removeEmptyRows();
    wServers.setRowNums();
    
    //Settings
    mapToTableView(model.getSettings(), wSettings);
  }

  private void mapToTableView(Map<String,String> map, TableView table ) {
    for(String key : map.keySet()){
      table.add(key, map.get(key));
    }
    table.removeEmptyRows();
    table.setRowNums();
  }

  private void cancel()
  {
    stepname = null;
    model.setChanged(changed);
    dispose();
  }

  private void ok()
  {
    try
    {
      toModel(model);
    }
    catch (KettleException e)
    {
      new ErrorDialog(
              shell, BaseMessages.getString(PKG, "ElasticSearchBulkDialog.ErrorValidateData.DialogTitle"),
              BaseMessages.getString(PKG, "ElasticSearchBulkDialog.ErrorValidateData.DialogMessage"), e);
    }
    dispose();
  }

  private void toModel(ElasticSearchBulkMeta in) throws KettleException
  {// copy info to ElasticSearchBulkMeta 
    stepname = wStepname.getText();
    
    in.setType(wType.getText());
    in.setIndex(wIndex.getText());

    in.setBatchSize(wBatchSize.getText());
    in.setTimeOut(Const.NVL(wTimeOut.getText(), null));
    in.setTimeoutUnit(wTimeOut.getTimeUnit());
    
    in.setIdInField(wIdInField.getText());
    in.setOverWriteIfSameId( StringUtils.isNotBlank(wIdInField.getText()) && wIsOverwrite.getSelection() );
    
    in.setStopOnError(wStopOnError.getSelection());
    
    in.setJsonInsert(wIsJson.getSelection());

    in.setJsonField(wIsJson.getSelection() ? wJsonField.getText() : null );
    in.setIdOutField(wIdOutField.getText());
    in.setUseOutput(wUseOutput.getSelection());
    
    in.clearFields();
    if(!wIsJson.getSelection()){
      for(int i=0; i< wFields.getItemCount(); i++){
         String[] row = wFields.getItem(i);
         if(StringUtils.isNotBlank(row[0])){
           in.addField(row[0], row[1]);    
         }
      }
    }
    
    in.clearServers();
    for(int i = 0; i < wServers.getItemCount(); i++){
      String[] row = wServers.getItem(i);
      if(StringUtils.isNotBlank(row[0])){
        try{
        in.addServer(row[0], Integer.parseInt(row[1]));
        } catch(NumberFormatException nfe){
          in.addServer(row[0], ElasticSearchBulkMeta.DEFAULT_PORT);
        }
      }
    }
    
    in.clearSettings();
    for (int i = 0; i < wSettings.getItemCount(); i++) {
      String[] row = wSettings.getItem(i);
      in.addSetting(row[0], row[1]);
    }
  }
  
  private enum TestType{
    INDEX,
    CLUSTER,
  }
  
  private void test(TestType testType){
    
    Client client = null;
    Node node = null;
    try {
      
      ElasticSearchBulkMeta tempMeta = new ElasticSearchBulkMeta();
      toModel(tempMeta);
      
      ImmutableSettings.Builder settingsBuilder = ImmutableSettings.settingsBuilder();
      settingsBuilder.put(ImmutableSettings.Builder.EMPTY_SETTINGS);//keep default classloader
      settingsBuilder.put(tempMeta.getSettings());
      Settings settings = settingsBuilder.build();

      
      if(tempMeta.getServers().length > 0){
        node = null;
        TransportClient tClient = new TransportClient(settings);
        for(InetSocketTransportAddress address : tempMeta.getServers()){
          tClient.addTransportAddress(address);
        }
        client = tClient;
      }
      else {
        NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder();
        nodeBuilder.settings(settings);
        node = nodeBuilder.client(true).node();
        client = node.client();
        node.start();
      }
      
      AdminClient admin = client.admin();
      
      switch(testType){
        case INDEX:
          if(StringUtils.isBlank(tempMeta.getIndex())){
            showError(BaseMessages.getString(PKG, "ElasticSearchBulk.Error.NoIndex"));
            break;
          }
          IndicesStatusRequestBuilder indicesBld = admin.indices().prepareStatus(tempMeta.getIndex());
          ListenableActionFuture<IndicesStatusResponse>  lafInd = indicesBld.execute();
          //IndicesStatusResponse isr = 
          lafInd.actionGet();
          String shards = "" + lafInd.get().getSuccessfulShards() + "/" + lafInd.get().getTotalShards();
          showMessage(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.TestIndex.TestOK", shards));
          break;
        case CLUSTER:
          ClusterStateRequestBuilder clusterBld = admin.cluster().prepareState();
          ListenableActionFuture<ClusterStateResponse> lafClu = clusterBld.execute();
          ClusterStateResponse cluResp = lafClu.actionGet();
          String name = cluResp.getClusterName().value();
          ClusterState cluState = cluResp.getState();
          int numNodes = cluState.getNodes().size();
          showMessage(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.TestCluster.TestOK", name, numNodes));
          break;
      }
    } catch (NoNodeAvailableException e){
      showError(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.Error.NoNodesFound"));
    } catch(MasterNotDiscoveredException e){
      showError(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.Error.NoNodesFound"));
    }
    catch (Exception e){
      if(e.getCause() instanceof IndexMissingException){
        showError(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.Error.NoIndex"));
      }
      else showError(e.getLocalizedMessage());
    }
    finally{
      if (client != null) {
        client.close();
      }
      if (node != null) {
        node.close();
      }
    } 
    
  }
  
  private void showError(String message){
    MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
    mb.setMessage(message);
    mb.setText(BaseMessages.getString(PKG, "System.Dialog.Error.Title"));
    mb.open(); 
  }
  private void showMessage(String message){
    MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
    mb.setMessage(message);
    mb.setText(BaseMessages.getString(PKG, "ElasticSearchBulkDialog.Test.TestOKTitle"));
    mb.open(); 
  }

  @Override
  public String toString()
  {
    return this.getClass().getName();
  }
}
