/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.big.data.kettle.plugins.kafka;

import com.google.common.collect.ImmutableMap;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.PluginDialog;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.streaming.common.BaseStreamStepMeta;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.BaseStreamingDialog;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.stream;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaConsumerInputMeta.ConnectionType.CLUSTER;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaConsumerInputMeta.ConnectionType.DIRECT;

@SuppressWarnings ( { "FieldCanBeLocal", "unused" } )
@PluginDialog ( id = "KafkaConsumerInput", pluginType = PluginDialog.PluginType.STEP, image = "KafkaConsumerInput.svg",
        documentationUrl = "mk-95pdia003/pdi-transformation-steps/kafka-consumer" )
public class KafkaConsumerInputDialog extends BaseStreamingDialog implements StepDialogInterface {

  private static final int INPUT_WIDTH = 350;
  protected static final int SHELL_MIN_WIDTH = 527;
  protected static final int SHELL_MIN_HEIGHT = 682;
  private static final Class<?> PKG = KafkaConsumerInputMeta.class;
  // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private static final ImmutableMap<String, String> DEFAULT_OPTION_VALUES =
    ImmutableMap.of( ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest" );
  private final KafkaFactory kafkaFactory = KafkaFactory.defaultFactory();

  private KafkaConsumerInputMeta consumerMeta;
  protected Spoon  spoonInstance;

  private Label wlClusterName;
  protected ComboVar wClusterName;
  private Composite wOptionsComp;

  private TextVar wConsumerGroup;
  private TableView topicsTable;
  protected TableView optionsTable;


  private Button wbDirect;
  protected Button wbCluster;
  private Label wlBootstrapServers;
  protected TextVar wBootstrapServers;
  private Button wbAutoCommit;
  private Button wbManualCommit;
  private static final String REPOS_DELIM = "/";

  public KafkaConsumerInputDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, in, tr, sname );
    consumerMeta = (KafkaConsumerInputMeta) in;
    spoonInstance = Spoon.getInstance();
  }

  @Override protected String getDialogTitle() {
    return BaseMessages.getString( PKG, "KafkaConsumerInputDialog.Shell.Title" );
  }

  @Override protected void createAdditionalTabs() {
    shell.setMinimumSize( SHELL_MIN_WIDTH, SHELL_MIN_HEIGHT );
    buildFieldsTab();
    buildOptionsTab();
    buildOffsetManagement();
  }

  private void buildOffsetManagement() {
    Group wOffsetGroup = new Group( wBatchComp, SWT.SHADOW_ETCHED_IN );
    wOffsetGroup.setText( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.OffsetManagement" ) );
    FormLayout flOffsetGroup = new FormLayout();
    flOffsetGroup.marginHeight = 15;
    flOffsetGroup.marginWidth = 15;
    wOffsetGroup.setLayout( flOffsetGroup );

    FormData fdOffsetGroup = new FormData();
    fdOffsetGroup.top = new FormAttachment( wPrefetchCount, 15 );
    fdOffsetGroup.left = new FormAttachment( 0, 0 );
    fdOffsetGroup.right = new FormAttachment( 100, 0 );
    wOffsetGroup.setLayoutData( fdOffsetGroup );
    props.setLook( wOffsetGroup );

    wbAutoCommit = new Button( wOffsetGroup, SWT.RADIO );
    wbAutoCommit.setText( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.AutoOffset" ) );
    FormData fdbAutoCommit = new FormData();
    fdbAutoCommit.top = new FormAttachment( 0, 0 );
    fdbAutoCommit.left = new FormAttachment( 0, 0 );
    wbAutoCommit.setLayoutData( fdbAutoCommit );
    props.setLook( wbAutoCommit );

    wbManualCommit = new Button( wOffsetGroup, SWT.RADIO );
    wbManualCommit.setText( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.ManualOffset" ) );
    FormData fdbManualCommit = new FormData();
    fdbManualCommit.left = new FormAttachment( 0, 0 );
    fdbManualCommit.top = new FormAttachment( wbAutoCommit, 10, SWT.BOTTOM );
    wbManualCommit.setLayoutData( fdbManualCommit );
    props.setLook( wbManualCommit );
  }

  @Override protected void buildSetup( Composite wSetupComp ) {
    props.setLook( wSetupComp );
    FormLayout setupLayout = new FormLayout();
    setupLayout.marginHeight = 15;
    setupLayout.marginWidth = 15;
    wSetupComp.setLayout( setupLayout );

    Group wConnectionGroup = new Group( wSetupComp, SWT.SHADOW_ETCHED_IN );
    wConnectionGroup.setText( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.Connection" ) );
    FormLayout flConnection = new FormLayout();
    flConnection.marginHeight = 15;
    flConnection.marginWidth = 15;
    wConnectionGroup.setLayout( flConnection );

    FormData fdConnectionGroup = new FormData();
    fdConnectionGroup.left = new FormAttachment( 0, 0 );
    fdConnectionGroup.top = new FormAttachment( 0, 0 );
    fdConnectionGroup.right = new FormAttachment( 100, 0 );
    fdConnectionGroup.width = INPUT_WIDTH;
    wConnectionGroup.setLayoutData( fdConnectionGroup );

    props.setLook( wConnectionGroup );

    wbDirect = new Button( wConnectionGroup, SWT.RADIO );
    wbDirect.setText( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.Direct" ) );
    FormData fdbDirect = new FormData();
    fdbDirect.left = new FormAttachment( 0, 0 );
    fdbDirect.top = new FormAttachment( 0, 0 );
    wbDirect.setLayoutData( fdbDirect );
    wbDirect.addSelectionListener( new SelectionListener() {
      @Override public void widgetSelected( final SelectionEvent selectionEvent ) {
        lsMod.modifyText( null );
        toggleVisibility( true );
      }

      @Override public void widgetDefaultSelected( final SelectionEvent selectionEvent ) {
        toggleVisibility( true );
      }
    } );
    props.setLook( wbDirect );

    wbCluster = new Button( wConnectionGroup, SWT.RADIO );
    wbCluster.setText( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.Cluster" ) );
    FormData fdbCluster = new FormData();
    fdbCluster.left = new FormAttachment( 0, 0 );
    fdbCluster.top = new FormAttachment( wbDirect, 10 );
    wbCluster.setLayoutData( fdbCluster );
    wbCluster.addSelectionListener( new SelectionListener() {
      @Override public void widgetSelected( final SelectionEvent selectionEvent ) {
        lsMod.modifyText( null );
        toggleVisibility( false );
      }

      @Override public void widgetDefaultSelected( final SelectionEvent selectionEvent ) {
        toggleVisibility( false );
      }
    } );
    props.setLook( wbCluster );

    wbCluster.setEnabled( consumerMeta.getNamedClusterService() != null );

    Label environmentSeparator = new Label( wConnectionGroup, SWT.SEPARATOR | SWT.VERTICAL );
    FormData fdenvironmentSeparator = new FormData();
    fdenvironmentSeparator.top = new FormAttachment( wbDirect, 0, SWT.TOP );
    fdenvironmentSeparator.left = new FormAttachment( wbCluster, 15 );
    fdenvironmentSeparator.bottom = new FormAttachment( wbCluster, 0, SWT.BOTTOM );
    environmentSeparator.setLayoutData( fdenvironmentSeparator );

    wlClusterName = new Label( wConnectionGroup, SWT.LEFT );
    props.setLook( wlClusterName );
    wlClusterName.setText( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.HadoopCluster" ) );
    FormData fdlClusterName = new FormData();
    fdlClusterName.left = new FormAttachment( environmentSeparator, 15 );
    fdlClusterName.top = new FormAttachment( 0, 0 );
    fdlClusterName.right = new FormAttachment( 78, 0 );
    wlClusterName.setLayoutData( fdlClusterName );

    wClusterName = new ComboVar( transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wClusterName );
    wClusterName.addModifyListener( lsMod );
    FormData fdClusterName = new FormData();
    fdClusterName.left = new FormAttachment( wlClusterName, 0, SWT.LEFT );
    fdClusterName.top = new FormAttachment( wlClusterName, 5 );
    fdClusterName.right = new FormAttachment( 78, 0 );
    wClusterName.setLayoutData( fdClusterName );

    wlBootstrapServers = new Label( wConnectionGroup, SWT.LEFT );
    props.setLook( wlBootstrapServers );
    wlBootstrapServers.setText( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.BootstrapServers" ) );
    FormData fdlBootstrapServers = new FormData();
    fdlBootstrapServers.left = new FormAttachment( environmentSeparator, 15 );
    fdlBootstrapServers.top = new FormAttachment( 0, 0 );
    fdlBootstrapServers.right = new FormAttachment( 78, 0 );
    wlBootstrapServers.setLayoutData( fdlBootstrapServers );

    wBootstrapServers = new TextVar( transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wBootstrapServers );
    wBootstrapServers.addModifyListener( lsMod );
    FormData fdBootstrapServers = new FormData();
    fdBootstrapServers.left = new FormAttachment( wlBootstrapServers, 0, SWT.LEFT );
    fdBootstrapServers.top = new FormAttachment( wlBootstrapServers, 5 );
    fdBootstrapServers.right = new FormAttachment( 78, 0 );
    wBootstrapServers.setLayoutData( fdBootstrapServers );

    Label wlTopic = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlTopic );
    wlTopic.setText( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.Topics" ) );
    FormData fdlTopic = new FormData();
    fdlTopic.left = new FormAttachment( 0, 0 );
    fdlTopic.top = new FormAttachment( wConnectionGroup, 10 );
    fdlTopic.right = new FormAttachment( 50, 0 );
    wlTopic.setLayoutData( fdlTopic );

    wConsumerGroup = new TextVar( transMeta, wSetupComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wConsumerGroup );
    wConsumerGroup.addModifyListener( lsMod );
    FormData fdConsumerGroup = new FormData();
    fdConsumerGroup.left = new FormAttachment( 0, 0 );
    fdConsumerGroup.bottom = new FormAttachment( 100, 0 );
    fdConsumerGroup.width = INPUT_WIDTH;
    wConsumerGroup.setLayoutData( fdConsumerGroup );
    Label wlConsumerGroup = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlConsumerGroup );
    wlConsumerGroup.setText( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.ConsumerGroup" ) );
    FormData fdlConsumerGroup = new FormData();
    fdlConsumerGroup.left = new FormAttachment( 0, 0 );
    fdlConsumerGroup.bottom = new FormAttachment( wConsumerGroup, -5, SWT.TOP );
    fdlConsumerGroup.right = new FormAttachment( 50, 0 );
    wlConsumerGroup.setLayoutData( fdlConsumerGroup );

    buildTopicsTable( wSetupComp, wlTopic, wlConsumerGroup );

    FormData fdSetupComp = new FormData();
    fdSetupComp.left = new FormAttachment( 0, 0 );
    fdSetupComp.top = new FormAttachment( 0, 0 );
    fdSetupComp.right = new FormAttachment( 100, 0 );
    fdSetupComp.bottom = new FormAttachment( 100, 0 );
    wSetupComp.setLayoutData( fdSetupComp );
    wSetupComp.layout();
    wSetupTab.setControl( wSetupComp );

    toggleVisibility( true );
  }

  private void toggleVisibility( final boolean isDirect ) {
    wlBootstrapServers.setVisible( isDirect );
    wBootstrapServers.setVisible( isDirect );
    wlClusterName.setVisible( !isDirect );
    wClusterName.setVisible( !isDirect );
  }

  private void buildFieldsTab() {
    CTabItem wFieldsTab = new CTabItem( wTabFolder, SWT.NONE, 2 );
    wFieldsTab.setText( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.FieldsTab" ) );

    Composite wFieldsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wFieldsComp );
    FormLayout fieldsLayout = new FormLayout();
    fieldsLayout.marginHeight = 15;
    fieldsLayout.marginWidth = 15;
    wFieldsComp.setLayout( fieldsLayout );

    FormData fieldsFormData = new FormData();
    fieldsFormData.left = new FormAttachment( 0, 0 );
    fieldsFormData.top = new FormAttachment( wFieldsComp, 0 );
    fieldsFormData.right = new FormAttachment( 100, 0 );
    fieldsFormData.bottom = new FormAttachment( 100, 0 );
    wFieldsComp.setLayoutData( fieldsFormData );

    buildFieldTable( wFieldsComp, wFieldsComp );

    wFieldsComp.layout();
    wFieldsTab.setControl( wFieldsComp );
  }

  private void buildOptionsTab() {
    CTabItem wOptionsTab = new CTabItem( wTabFolder, SWT.NONE );
    wOptionsTab.setText( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.OptionsTab" ) );

    Composite wOptionsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wOptionsComp );
    FormLayout fieldsLayout = new FormLayout();
    fieldsLayout.marginHeight = 15;
    fieldsLayout.marginWidth = 15;
    wOptionsComp.setLayout( fieldsLayout );

    FormData optionsFormData = new FormData();
    optionsFormData.left = new FormAttachment( 0, 0 );
    optionsFormData.top = new FormAttachment( wOptionsComp, 0 );
    optionsFormData.right = new FormAttachment( 100, 0 );
    optionsFormData.bottom = new FormAttachment( 100, 0 );
    wOptionsComp.setLayoutData( optionsFormData );

    buildOptionsTable( wOptionsComp );

    wOptionsComp.layout();
    wOptionsTab.setControl( wOptionsComp );
  }

  private void buildFieldTable( Composite parentWidget, Control relativePosition ) {
    ColumnInfo[] columns = getFieldColumns();

    int fieldCount = KafkaConsumerField.Name.values().length;

    fieldsTable = new TableView(
      transMeta,
      parentWidget,
      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI,
      columns,
      fieldCount,
      true,
      lsMod,
      props,
      false
    );

    fieldsTable.setSortable( false );
    fieldsTable.getTable().addListener( SWT.Resize, event -> {
      Table table = (Table) event.widget;
      table.getColumn( 1 ).setWidth( 147 );
      table.getColumn( 2 ).setWidth( 147 );
      table.getColumn( 3 ).setWidth( 147 );
    } );

    populateFieldData();

    FormData fdData = new FormData();
    fdData.left = new FormAttachment( 0, 0 );
    fdData.top = new FormAttachment( relativePosition, 5 );
    fdData.right = new FormAttachment( 100, 0 );

    // resize the columns to fit the data in them
    stream( fieldsTable.getTable().getColumns() ).forEach( column -> {
      if ( column.getWidth() > 0 ) {
        // don't pack anything with a 0 width, it will resize it to make it visible (like the index column)
        column.setWidth( 120 );
      }
    } );

    // don't let any rows get deleted or added (this does not affect the read-only state of the cells)
    fieldsTable.setReadonly( true );
    fieldsTable.setLayoutData( fdData );
  }

  private void buildOptionsTable( Composite parentWidget ) {
    ColumnInfo[] columns = getOptionsColumns();

    if ( consumerMeta.getConfig().size() == 0 ) {
      // inital call
      List<String> list = KafkaDialogHelper.getConsumerAdvancedConfigOptionNames();
      Map<String, String> advancedConfig = new LinkedHashMap<>();
      for ( String item : list ) {
        advancedConfig.put( item, DEFAULT_OPTION_VALUES.getOrDefault( item, "" ) );
      }
      consumerMeta.setConfig( advancedConfig );
    }
    int fieldCount = consumerMeta.getConfig().size();

    optionsTable = new TableView(
      transMeta,
      parentWidget,
      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI,
      columns,
      fieldCount,
      false,
      lsMod,
      props,
      false
    );

    optionsTable.setSortable( false );
    optionsTable.getTable().addListener( SWT.Resize, event -> {
      Table table = (Table) event.widget;
      table.getColumn( 1 ).setWidth( 220 );
      table.getColumn( 2 ).setWidth( 220 );
    } );

    populateOptionsData();

    FormData fdData = new FormData();
    fdData.left = new FormAttachment( 0, 0 );
    fdData.top = new FormAttachment( 0, 0 );
    fdData.right = new FormAttachment( 100, 0 );
    fdData.bottom = new FormAttachment( 100, 0 );

    // resize the columns to fit the data in them
    stream( optionsTable.getTable().getColumns() ).forEach( column -> {
      if ( column.getWidth() > 0 ) {
        // don't pack anything with a 0 width, it will resize it to make it visible (like the index column)
        column.setWidth( 120 );
      }
    } );

    optionsTable.setLayoutData( fdData );
  }

  private ColumnInfo[] getFieldColumns() {
    KafkaConsumerField.Type[] values = KafkaConsumerField.Type.values();
    String[] supportedTypes = stream( values ).map( KafkaConsumerField.Type::toString ).toArray( String[]::new );

    ColumnInfo referenceName = new ColumnInfo( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.Column.Ref" ),
      ColumnInfo.COLUMN_TYPE_TEXT, false, true );

    ColumnInfo name = new ColumnInfo( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.Column.Name" ),
      ColumnInfo.COLUMN_TYPE_TEXT, false, false );

    ColumnInfo type = new ColumnInfo( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.Column.Type" ),
      ColumnInfo.COLUMN_TYPE_CCOMBO, supportedTypes, false );

    // don't let the user edit the type for anything other than key & msg fields
    type.setDisabledListener( rowNumber -> {
      String ref = fieldsTable.getTable().getItem( rowNumber ).getText( 1 );
      KafkaConsumerField.Name refName = KafkaConsumerField.Name.valueOf( ref.toUpperCase() );

      return !( refName == KafkaConsumerField.Name.KEY || refName == KafkaConsumerField.Name.MESSAGE );
    } );

    return new ColumnInfo[] { referenceName, name, type };
  }

  private ColumnInfo[] getOptionsColumns() {

    ColumnInfo optionName = new ColumnInfo( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.NameField" ),
      ColumnInfo.COLUMN_TYPE_TEXT, false, false );

    ColumnInfo value = new ColumnInfo( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.Column.Value" ),
      ColumnInfo.COLUMN_TYPE_TEXT, false, false );
    value.setUsingVariables( true );

    return new ColumnInfo[] { optionName, value };
  }

  private void populateFieldData() {
    List<KafkaConsumerField> fieldDefinitions = consumerMeta.getFieldDefinitions();
    int rowIndex = 0;
    for ( KafkaConsumerField field : fieldDefinitions ) {
      TableItem key = fieldsTable.getTable().getItem( rowIndex++ );

      if ( field.getKafkaName() != null ) {
        key.setText( 1, field.getKafkaName().toString() );
      }

      if ( field.getOutputName() != null ) {
        key.setText( 2, field.getOutputName() );
      }

      if ( field.getOutputType() != null ) {
        key.setText( 3, field.getOutputType().toString() );
      }
    }
  }

  private void populateOptionsData() {
    int rowIndex = 0;
    for ( Map.Entry<String, String> entry : consumerMeta.getConfig().entrySet() ) {
      TableItem key = optionsTable.getTable().getItem( rowIndex++ );
      key.setText( 1, entry.getKey() );
      key.setText( 2, entry.getValue() );
    }
  }

  private void populateTopicsData() {
    List<String> topics = consumerMeta.getTopics();
    int rowIndex = 0;
    for ( String topic : topics ) {
      TableItem key = topicsTable.getTable().getItem( rowIndex++ );
      if ( topic != null ) {
        key.setText( 1, topic );
      }
    }
  }

  private void buildTopicsTable( Composite parentWidget, Control controlAbove, Control controlBelow ) {
    ColumnInfo[] columns =
      new ColumnInfo[] { new ColumnInfo( BaseMessages.getString( PKG, "KafkaConsumerInputDialog.NameField" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[ 1 ], false ) };

    columns[ 0 ].setUsingVariables( true );

    int topicsCount = consumerMeta.getTopics().size();

    Listener lsFocusInTopic =  e -> {
      CCombo ccom = (CCombo) e.widget;
      ComboVar cvar = (ComboVar) ccom.getParent();

      KafkaDialogHelper kdh = getDialogHelper( cvar );
      kdh.clusterNameChanged( e );
    };
    topicsTable = new TableView(
      transMeta,
      parentWidget,
      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI,
      columns,
      topicsCount,
      false,
      lsMod,
      props,
      false,
      lsFocusInTopic
    );

    topicsTable.setSortable( false );
    topicsTable.getTable().addListener( SWT.Resize, event -> {
      Table table = (Table) event.widget;
      table.getColumn( 1 ).setWidth( 316 );
    } );

    populateTopicsData();

    FormData fdData = new FormData();
    fdData.left = new FormAttachment( 0, 0 );
    fdData.top = new FormAttachment( controlAbove, 5 );
    fdData.right = new FormAttachment( 0, 337 );
    fdData.bottom = new FormAttachment( controlBelow, -10, SWT.TOP );

    // resize the columns to fit the data in them
    stream( topicsTable.getTable().getColumns() ).forEach( column -> {
      if ( column.getWidth() > 0 ) {
        // don't pack anything with a 0 width, it will resize it to make it visible (like the index column)
        column.setWidth( 120 );
      }
    } );

    topicsTable.setLayoutData( fdData );
  }

  @Override
  protected void getData() {
    if ( meta.getTransformationPath() != null ) {
      wFileSection.wFileName.setText( meta.getTransformationPath() );
    }

    if ( consumerMeta.getNamedClusterService() != null ) {
      try {
        List<String> names = consumerMeta.getNamedClusterService().listNames(spoonInstance.getMetaStore());
        wClusterName.setItems(names.toArray(new String[0]));
      } catch (MetaStoreException e) {
        log.logError("Failed to get defined named clusters", e);
      }

      if (consumerMeta.getClusterName() != null) {
        wClusterName.setText(consumerMeta.getClusterName());
      }
    }
    if ( consumerMeta.getDirectBootstrapServers() != null ) {
      wBootstrapServers.setText( consumerMeta.getDirectBootstrapServers() );
    }

    populateTopicsData();
    if ( consumerMeta.getSubStep() != null ) {
      wSubStep.setText( consumerMeta.getSubStep() );
    }

    if ( consumerMeta.getConsumerGroup() != null ) {
      wConsumerGroup.setText( consumerMeta.getConsumerGroup() );
    }

    if ( meta.getBatchSize() != null ) {
      wBatchSize.setText( meta.getBatchSize() );
    }
    if ( meta.getBatchDuration() != null ) {
      wBatchDuration.setText( meta.getBatchDuration() );
    }
    if ( meta.getParallelism() != null ) {
      wParallelism.setText( meta.getParallelism() );
    }
    if ( meta.getPrefetchCount() != null ) {
      wPrefetchCount.setText( meta.getPrefetchCount() );
    }

    wbCluster.setSelection( !isDirect() );
    wbDirect.setSelection( isDirect() );

    toggleVisibility( isDirect() );

    wbAutoCommit.setSelection( consumerMeta.isAutoCommit() );
    wbManualCommit.setSelection( !consumerMeta.isAutoCommit() );

    specificationMethod = meta.getSpecificationMethod();
    switch ( specificationMethod ) {
      case FILENAME:
        wFileSection.wFileName.setText( Const.NVL( meta.getFileName(), "" ) );
        break;
      case REPOSITORY_BY_NAME:
        String fullPath = Const.NVL( meta.getDirectoryPath(), "" ) + REPOS_DELIM + Const.NVL( meta.getTransName(), "" );
        wFileSection.wFileName.setText( fullPath );
        break;
      default:
        break;
    }


    populateFieldData();
  }

  private boolean isDirect() {
    return DIRECT.equals( consumerMeta.getConnectionType() );
  }

  @Override protected void additionalOks( BaseStreamStepMeta meta ) {
    setTopicsFromTable();

    consumerMeta.setClusterName( wClusterName.getText() );
    consumerMeta.setConsumerGroup( wConsumerGroup.getText() );
    consumerMeta.setConnectionType( wbDirect.getSelection() ? DIRECT : CLUSTER );
    consumerMeta.setDirectBootstrapServers( wBootstrapServers.getText() );
    consumerMeta.setAutoCommit( wbAutoCommit.getSelection() );
    setFieldsFromTable();
    setOptionsFromTable();
  }

  private void setFieldsFromTable() {
    int itemCount = fieldsTable.getItemCount();
    for ( int rowIndex = 0; rowIndex < itemCount; rowIndex++ ) {
      TableItem row = fieldsTable.getTable().getItem( rowIndex );
      String kafkaName = row.getText( 1 );
      String outputName = row.getText( 2 );
      String outputType = row.getText( 3 );
      try {
        KafkaConsumerField.Name ref = KafkaConsumerField.Name.valueOf( kafkaName.toUpperCase() );
        KafkaConsumerField field = new KafkaConsumerField(
          ref,
          outputName,
          KafkaConsumerField.Type.valueOf( outputType )
        );
        consumerMeta.setField( field );
      } catch ( IllegalArgumentException e ) {
        if ( isDebug() ) {
          logDebug( e.getMessage(), e );
        }
      }
    }
  }

  private void setTopicsFromTable() {
    int itemCount = topicsTable.getItemCount();
    ArrayList<String> tableTopics = new ArrayList<>();
    for ( int rowIndex = 0; rowIndex < itemCount; rowIndex++ ) {
      TableItem row = topicsTable.getTable().getItem( rowIndex );
      String topic = row.getText( 1 );
      if ( !"".equals( topic ) && tableTopics.indexOf( topic ) == -1 ) {
        tableTopics.add( topic );
      }
    }
    consumerMeta.setTopics( tableTopics );
  }

  private void setOptionsFromTable() {
    consumerMeta.setConfig( KafkaDialogHelper.getConfig( optionsTable ) );
  }

  protected KafkaDialogHelper getDialogHelper( ComboVar cvar ) {
    KafkaDialogHelper helper = new KafkaDialogHelper(
      wClusterName, cvar, wbCluster, wBootstrapServers, kafkaFactory,
      consumerMeta.getNamedClusterService(), // consumerMeta.getNamedClusterServiceLocator(),
      consumerMeta.getMetastoreLocator(), optionsTable,
      meta.getParentStepMeta() );
    return helper;
  }

}
