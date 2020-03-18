/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.step.mqtt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.streaming.common.BaseStreamStepMeta;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStreamingDialog;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_BINARY;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_STRING;
import static org.pentaho.di.core.row.ValueMetaInterface.getTypeDescription;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.AUTOMATIC_RECONNECT;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.CLEAN_SESSION;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.CONNECTION_TIMEOUT;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.KEEP_ALIVE_INTERVAL;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.MAX_INFLIGHT;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.MQTT_VERSION;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.SERVER_URIS;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.STORAGE_LEVEL;
import static org.pentaho.di.ui.core.WidgetUtils.formDataBelow;

@SuppressWarnings( "unused" )
public class MQTTConsumerDialog extends BaseStreamingDialog implements StepDialogInterface {

  private static final Class<?> PKG = MQTTConsumerMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private MQTTConsumerMeta mqttMeta;
  private TextVar wConnection;
  private TextVar wClientId;
  private TableView topicsTable;
  private ComboVar wQOS;

  private final Point startingDimensions = new Point( 527, 676 );

  private MqttDialogSecurityLayout securityLayout;
  private MqttDialogOptionsLayout optionsLayout;

  public MQTTConsumerDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, in, tr, sname );

    mqttMeta = (MQTTConsumerMeta) in;
  }

  @Override protected void getData() {
    super.getData();
    wConnection.setText( mqttMeta.getMqttServer() );
    wClientId.setText( mqttMeta.getClientId() );
    populateTopicsData();
    wQOS.setText( mqttMeta.getQos() );

    securityLayout.setUsername( mqttMeta.getUsername() );
    securityLayout.setPassword( mqttMeta.getPassword() );

    securityLayout.setUIText();
  }

  private void populateTopicsData() {
    List<String> topics = mqttMeta.getTopics();
    int rowIndex = 0;
    for ( String topic : topics ) {
      TableItem key = topicsTable.getTable().getItem( rowIndex++ );
      if ( topic != null ) {
        key.setText( 1, topic );
      }
    }
  }

  @Override protected String getDialogTitle() {
    return BaseMessages.getString( PKG, "MQTTConsumerDialog.Shell.Title" );
  }

  @Override
  public void setSize() {
    setSize( shell );  // sets shell location and preferred size
    shell.setMinimumSize( startingDimensions );
    shell.setSize( startingDimensions ); // force initial size
  }

  @Override protected void buildSetup( Composite wSetupComp ) {
    props.setLook( wSetupComp );
    FormLayout setupLayout = new FormLayout();
    setupLayout.marginHeight = 15;
    setupLayout.marginWidth = 15;
    wSetupComp.setLayout( setupLayout );

    Label wlConnection = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlConnection );
    wlConnection.setText( BaseMessages.getString( PKG, "MQTTConsumerDialog.Connection" ) );
    FormData fdlConnection = new FormData();
    fdlConnection.left = new FormAttachment( 0, 0 );
    fdlConnection.top = new FormAttachment( 0, 0 );
    fdlConnection.right = new FormAttachment( 50, 0 );
    wlConnection.setLayoutData( fdlConnection );

    wConnection = new TextVar( transMeta, wSetupComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wConnection );
    wConnection.addModifyListener( lsMod );
    FormData fdConnection = formDataBelow( wlConnection, INPUT_WIDTH, 5 );
    wConnection.setLayoutData( fdConnection );

    Label wlClientId = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlClientId );
    wlClientId.setText( BaseMessages.getString( PKG, "MQTTConsumerDialog.ClientId" ) );
    FormData fdlClientId = formDataBelow( wConnection, INPUT_WIDTH, 5 );
    wlClientId.setLayoutData( fdlClientId );

    wClientId = new TextVar( transMeta, wSetupComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wClientId );
    wClientId.addModifyListener( lsMod );
    FormData fdClientId = formDataBelow( wlClientId, INPUT_WIDTH, 5 );
    wClientId.setLayoutData( fdClientId );

    Label wlTopics = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlTopics );
    wlTopics.setText( BaseMessages.getString( PKG, "MQTTConsumerDialog.Topics" ) );
    FormData fdlTopics = formDataBelow( wClientId, INPUT_WIDTH, 5 );
    wlTopics.setLayoutData( fdlTopics );

    wQOS = new ComboVar( transMeta, wSetupComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wQOS );
    wQOS.addModifyListener( lsMod );
    FormData fdQOS = new FormData();
    fdQOS.left = new FormAttachment( 0, 0 );
    fdQOS.bottom = new FormAttachment( 100, 0 );
    fdQOS.width = 135;
    wQOS.setLayoutData( fdQOS );
    wQOS.add( "0" );
    wQOS.add( "1" );
    wQOS.add( "2" );

    Label wlQOS = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlQOS );
    wlQOS.setText( BaseMessages.getString( PKG, "MQTTConsumerDialog.QOS" ) );
    FormData fdlQOS = new FormData();
    fdlQOS.left = new FormAttachment( 0, 0 );
    fdlQOS.bottom = new FormAttachment( wQOS, -5 );
    fdlQOS.right = new FormAttachment( 50, 0 );
    wlQOS.setLayoutData( fdlQOS );

    // Put last so it expands with the dialog. Anchoring itself to QOS Label and the Topics Label
    buildTopicsTable( wSetupComp, wlTopics, wlQOS );
  }

  private void buildTopicsTable( Composite parentWidget, Control controlAbove, Control controlBelow ) {
    ColumnInfo[] columns =
      new ColumnInfo[] { new ColumnInfo( BaseMessages.getString( PKG, "MQTTConsumerDialog.TopicHeading" ),
        ColumnInfo.COLUMN_TYPE_TEXT, new String[ 1 ], false ) };

    columns[ 0 ].setUsingVariables( true );

    int topicsCount = mqttMeta.getTopics().size();

    topicsTable = new TableView(
      transMeta,
      parentWidget,
      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI,
      columns,
      topicsCount,
      false,
      lsMod,
      props,
      false
    );

    topicsTable.setSortable( false );
    topicsTable.getTable().addListener( SWT.Resize, event -> {
      Table table = (Table) event.widget;
      table.getColumn( 1 ).setWidth( 330 );
    } );

    FormData fdData = new FormData();
    fdData.left = new FormAttachment( 0, 0 );
    fdData.top = new FormAttachment( controlAbove, 5 );
    fdData.right = new FormAttachment( 0, 350 );
    fdData.bottom = new FormAttachment( controlBelow, -10 );

    // resize the columns to fit the data in them
    stream( topicsTable.getTable().getColumns() ).forEach( column -> {
      if ( column.getWidth() > 0 ) {
        // don't pack anything with a 0 width, it will resize it to make it visible (like the index column)
        column.setWidth( 120 );
      }
    } );

    topicsTable.setLayoutData( fdData );
  }

  @Override protected void createAdditionalTabs() {
    // Set the height so the topics table has approximately 5 rows
    shell.setMinimumSize( 527, 600 );
    securityLayout = new MqttDialogSecurityLayout( props, wTabFolder, lsMod, transMeta, mqttMeta.getSslConfig(), mqttMeta.isUseSsl() );
    securityLayout.buildSecurityTab();
    buildFieldsTab();
    optionsLayout = new MqttDialogOptionsLayout( props, wTabFolder, lsMod, transMeta,
      mqttMeta.retrieveOptions() );
    optionsLayout.buildTab();
  }

  private void buildFieldsTab() {
    CTabItem wFieldsTab = new CTabItem( wTabFolder, SWT.NONE, 3 );
    wFieldsTab.setText( BaseMessages.getString( PKG, "MQTTConsumerDialog.FieldsTab" ) );

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

  private void buildFieldTable( Composite parentWidget, Control relativePosition ) {
    ColumnInfo[] columns = getFieldColumns();

    fieldsTable = new TableView(
      transMeta,
      parentWidget,
      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI,
      columns,
      2,
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

  private void populateFieldData() {
    TableItem messageItem = fieldsTable.getTable().getItem( 0 );
    messageItem.setText( 1, BaseMessages.getString( PKG, "MQTTConsumerDialog.InputName.Message" ) );
    messageItem.setText( 2, mqttMeta.getMsgOutputName() );
    messageItem.setText( 3, getTypeDescription( mqttMeta.getMessageDataType() ) );

    TableItem topicItem = fieldsTable.getTable().getItem( 1 );
    topicItem.setText( 1, BaseMessages.getString( PKG, "MQTTConsumerDialog.InputName.Topic" ) );
    topicItem.setText( 2, mqttMeta.getTopicOutputName() );
    topicItem.setText( 3, getTypeDescription( TYPE_STRING ) );
  }

  private static String[] messageTypes =
    Stream.of( TYPE_BINARY, TYPE_STRING ).map( ValueMetaInterface::getTypeDescription ).toArray( String[]::new );

  private ColumnInfo[] getFieldColumns() {
    ColumnInfo referenceName = new ColumnInfo( BaseMessages.getString( PKG, "MQTTConsumerDialog.Column.Ref" ),
      ColumnInfo.COLUMN_TYPE_TEXT, false, true );

    ColumnInfo name = new ColumnInfo( BaseMessages.getString( PKG, "MQTTConsumerDialog.Column.Name" ),
      ColumnInfo.COLUMN_TYPE_TEXT, false, false );

    ColumnInfo type = new ColumnInfo( BaseMessages.getString( PKG, "MQTTConsumerDialog.Column.Type" ),
      ColumnInfo.COLUMN_TYPE_CCOMBO, messageTypes, false );

    type.setDisabledListener(
      rowNumber -> !BaseMessages.getString( PKG, "MQTTConsumerDialog.InputName.Message" )
        .equals( fieldsTable.getTable().getItem( rowNumber ).getText( 1 ) ) );

    return new ColumnInfo[] { referenceName, name, type };
  }


  @Override protected void additionalOks( BaseStreamStepMeta meta ) {
    mqttMeta.setMqttServer( wConnection.getText() );
    mqttMeta.setClientId( wClientId.getText() );
    mqttMeta.setTopics( stream( topicsTable.getTable().getItems() )
      .map( item -> item.getText( 1 ) )
      .filter( t -> !"".equals( t ) )
      .distinct()
      .collect( Collectors.toList() ) );
    mqttMeta.setMsgOutputName( fieldsTable.getTable().getItem( 0 ).getText( 2 ) );
    mqttMeta.setTopicOutputName( fieldsTable.getTable().getItem( 1 ).getText( 2 ) );
    mqttMeta.setQos( wQOS.getText() );
    mqttMeta.setUsername( securityLayout.getUsername() );
    mqttMeta.setPassword( securityLayout.getPassword() );
    mqttMeta.setUseSsl( securityLayout.useSsl() );
    mqttMeta.setSslConfig( securityLayout.sslConfig() );
    mqttMeta.messageDataType = fieldsTable.getTable().getItem( 0 ).getText( 3 );

    optionsLayout.retrieveOptions()
      .forEach( option -> {
        switch ( option.getKey() ) {
          case KEEP_ALIVE_INTERVAL:
            mqttMeta.setKeepAliveInterval( option.getValue() );
            break;
          case MAX_INFLIGHT:
            mqttMeta.setMaxInflight( option.getValue() );
            break;
          case CONNECTION_TIMEOUT:
            mqttMeta.setConnectionTimeout( option.getValue() );
            break;
          case CLEAN_SESSION:
            mqttMeta.setCleanSession( option.getValue() );
            break;
          case STORAGE_LEVEL:
            mqttMeta.setStorageLevel( option.getValue() );
            break;
          case SERVER_URIS:
            mqttMeta.setServerUris( option.getValue() );
            break;
          case MQTT_VERSION:
            mqttMeta.setMqttVersion( option.getValue() );
            break;
          case AUTOMATIC_RECONNECT:
            mqttMeta.setAutomaticReconnect( option.getValue() );
            break;
          default:
            log.logBasic( BaseMessages.getString( PKG, "MQTTDialog.Error.UnknownOption", option.getKey() ) );
        }
      } );
  }

}
