/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.annotations.PluginDialog;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.core.widget.TopicSelection;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import static com.google.common.base.Strings.nullToEmpty;
import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.AUTOMATIC_RECONNECT;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.CLEAN_SESSION;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.CONNECTION_TIMEOUT;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.KEEP_ALIVE_INTERVAL;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.MAX_INFLIGHT;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.MQTT_VERSION;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.SERVER_URIS;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.STORAGE_LEVEL;
import static org.pentaho.di.ui.core.WidgetUtils.createFieldDropDown;
import static org.pentaho.di.ui.core.WidgetUtils.formDataBelow;

@SuppressWarnings ( "unused" )
@PluginDialog ( id = "MQTTProducer", image = "MQTTProducer.svg", pluginType = PluginDialog.PluginType.STEP,
  documentationUrl = "mk-95pdia003/pdi-transformation-steps/mqtt-producer" )
public class MQTTProducerDialog extends BaseStepDialog implements StepDialogInterface {
  private static final int SHELL_MIN_WIDTH = 527;
  private static final int SHELL_MIN_HEIGHT = 650;
  private static final int INPUT_WIDTH = 350;

  private static final Class<?> PKG = MQTTProducerDialog.class;

  private MQTTProducerMeta meta;
  private ModifyListener lsMod;
  private TextVar wMqttServer;
  private TextVar wClientId;
  private TopicSelection topicSelection;
  private ComboVar wQOS;
  private ComboVar wMessageField;
  private CTabFolder wTabFolder;

  private MqttDialogSecurityLayout securityLayout;
  private MqttDialogOptionsLayout optionsLayout;

  public MQTTProducerDialog( Shell parent, Object in, TransMeta transMeta, String stepName ) {
    super( parent, (BaseStepMeta) in, transMeta, stepName );
    meta = (MQTTProducerMeta) in;
  }

  @Override
  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();
    changed = meta.hasChanged();

    lsMod = e -> meta.setChanged();
    lsCancel = e -> cancel();
    lsOK = e -> ok();
    lsDef = new SelectionAdapter() {
      @Override public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE );
    props.setLook( shell );
    setShellImage( shell, meta );
    shell.setMinimumSize( SHELL_MIN_WIDTH, SHELL_MIN_HEIGHT );
    shell.setText( getString( PKG, "MQTTProducerDialog.Shell.Title" ) );

    Label wicon = new Label( shell, SWT.RIGHT );
    wicon.setImage( getImage() );
    FormData fdlicon = new FormData();
    fdlicon.top = new FormAttachment( 0, 0 );
    fdlicon.right = new FormAttachment( 100, 0 );
    wicon.setLayoutData( fdlicon );
    props.setLook( wicon );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 15;
    formLayout.marginHeight = 15;
    shell.setLayout( formLayout );
    shell.addShellListener( new ShellAdapter() {
      @Override public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( getString( PKG, "MQTTProducerDialog.Stepname.Label" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.top = new FormAttachment( 0, 0 );
    wlStepname.setLayoutData( fdlStepname );

    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.width = 250;
    fdStepname.left = new FormAttachment( 0, 0 );
    fdStepname.top = new FormAttachment( wlStepname, 5 );
    wStepname.setLayoutData( fdStepname );
    wStepname.addSelectionListener( lsDef );

    Label topSeparator = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    FormData fdSpacer = new FormData();
    fdSpacer.height = 2;
    fdSpacer.left = new FormAttachment( 0, 0 );
    fdSpacer.top = new FormAttachment( wStepname, 15 );
    fdSpacer.right = new FormAttachment( 100, 0 );
    topSeparator.setLayoutData( fdSpacer );

    // Start of tabbed display
    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );
    wTabFolder.setSimple( false );
    wTabFolder.setUnselectedCloseVisible( true );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( getString( PKG, "System.Button.Cancel" ) );
    FormData fdCancel = new FormData();
    fdCancel.right = new FormAttachment( 100, 0 );
    fdCancel.bottom = new FormAttachment( 100, 0 );
    wCancel.setLayoutData( fdCancel );
    wCancel.addListener( SWT.Selection, lsCancel );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( getString( PKG, "System.Button.OK" ) );
    FormData fdOk = new FormData();
    fdOk.right = new FormAttachment( wCancel, -5 );
    fdOk.bottom = new FormAttachment( 100, 0 );
    wOK.setLayoutData( fdOk );
    wOK.addListener( SWT.Selection, lsOK );

    Label bottomSeparator = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    props.setLook( bottomSeparator );
    FormData fdBottomSeparator = new FormData();
    fdBottomSeparator.height = 2;
    fdBottomSeparator.left = new FormAttachment( 0, 0 );
    fdBottomSeparator.bottom = new FormAttachment( wCancel, -15 );
    fdBottomSeparator.right = new FormAttachment( 100, 0 );
    bottomSeparator.setLayoutData( fdBottomSeparator );

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( topSeparator, 15 );
    fdTabFolder.bottom = new FormAttachment( bottomSeparator, -15 );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    wTabFolder.setLayoutData( fdTabFolder );

    buildSetupTab();

    securityLayout =
      new MqttDialogSecurityLayout( props, wTabFolder, lsMod, transMeta, meta.getSslConfig(), meta.useSsl );
    securityLayout.buildSecurityTab();

    optionsLayout = new MqttDialogOptionsLayout( props, wTabFolder, lsMod, transMeta,
      meta.retrieveOptions() );
    optionsLayout.buildTab();

    getData();

    setSize();

    meta.setChanged( changed );

    wTabFolder.setSelection( 0 );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }

    return stepname;
  }

  private void buildSetupTab() {
    CTabItem wSetupTab = new CTabItem( wTabFolder, SWT.NONE );
    wSetupTab.setText( getString( PKG, "MQTTProducerDialog.SetupTab" ) );

    Composite wSetupComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wSetupComp );
    FormLayout setupLayout = new FormLayout();
    setupLayout.marginHeight = 15;
    setupLayout.marginWidth = 15;
    wSetupComp.setLayout( setupLayout );

    Label wlMqttServer = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlMqttServer );
    wlMqttServer.setText( getString( PKG, "MQTTProducerDialog.Connection" ) );
    FormData fdlBootstrapServers = new FormData();
    fdlBootstrapServers.left = new FormAttachment( 0, 0 );
    fdlBootstrapServers.top = new FormAttachment( 0, 0 );
    fdlBootstrapServers.right = new FormAttachment( 0, INPUT_WIDTH );
    wlMqttServer.setLayoutData( fdlBootstrapServers );

    wMqttServer = new TextVar( transMeta, wSetupComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wMqttServer );
    wMqttServer.addModifyListener( lsMod );
    FormData fdBootstrapServers = new FormData();
    fdBootstrapServers.left = new FormAttachment( 0, 0 );
    fdBootstrapServers.top = new FormAttachment( wlMqttServer, 5 );
    fdBootstrapServers.right = new FormAttachment( 0, INPUT_WIDTH );
    wMqttServer.setLayoutData( fdBootstrapServers );

    Label wlClientId = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlClientId );
    wlClientId.setText( getString( PKG, "MQTTProducerDialog.ClientId" ) );
    FormData fdlClientId = new FormData();
    fdlClientId.left = new FormAttachment( 0, 0 );
    fdlClientId.top = new FormAttachment( wMqttServer, 10 );
    fdlClientId.right = new FormAttachment( 50, 0 );
    wlClientId.setLayoutData( fdlClientId );

    wClientId = new TextVar( transMeta, wSetupComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wClientId );
    wClientId.addModifyListener( lsMod );
    FormData o = formDataBelow( wlClientId, INPUT_WIDTH, 5 );
    wClientId.setLayoutData( o );

    topicSelection = new TopicSelection.Builder().
      setComposite( wSetupComp ).
      setStyle( SWT.LEFT ).
      setTransMeta( transMeta ).
      setStepMeta( meta ).
      setProps( props ).
      setLsMod( lsMod ).
      setTopicInField( meta.topicInField ).
      setTopicGroupLabel( getString( PKG, "MQTTProducerDialog.Topic" ) ).
      setFieldTopicRadioLabel( getString( PKG, "MQTTProducerDialog.GetDataFromField" ) ).
      setTextTopicRadioLabel( getString( PKG, "MQTTProducerDialog.SpecifyTopic" ) ).
      setFieldTopicLabel( getString( PKG, "MQTTProducerDialog.FieldName" ) ).
      setTextTopicLabel( getString( PKG, "MQTTProducerDialog.TopicName" ) ).
      build();

    props.setLook( topicSelection );
    FormData fdTopicSelection = new FormData();
    fdTopicSelection.left = new FormAttachment( 0, 0 );
    fdTopicSelection.top = new FormAttachment( wClientId, 0 );
    fdTopicSelection.right = new FormAttachment( 100, 0 );
    topicSelection.setLayoutData( fdTopicSelection );

    Label wlQOS = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlQOS );
    wlQOS.setText( getString( PKG, "MQTTProducerDialog.QOS" ) );
    FormData fdlQOS = new FormData();
    fdlQOS.left = new FormAttachment( 0, 0 );
    fdlQOS.top = new FormAttachment( topicSelection, 10 );
    fdlQOS.width = 120;
    wlQOS.setLayoutData( fdlQOS );

    wQOS = new ComboVar( transMeta, wSetupComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wQOS );
    wQOS.addModifyListener( lsMod );
    FormData fdQOS = new FormData();
    fdQOS.left = new FormAttachment( 0, 0 );
    fdQOS.top = new FormAttachment( wlQOS, 5 );
    fdQOS.width = 135;
    wQOS.setLayoutData( fdQOS );
    wQOS.add( "0" );
    wQOS.add( "1" );
    wQOS.add( "2" );

    Label wlMessageField = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlMessageField );
    wlMessageField.setText( getString( PKG, "MQTTProducerDialog.MessageField" ) );
    FormData fdlMessageField = new FormData();
    fdlMessageField.left = new FormAttachment( 0, 0 );
    fdlMessageField.top = new FormAttachment( wQOS, 10 );
    fdlMessageField.right = new FormAttachment( 50, 0 );
    wlMessageField.setLayoutData( fdlMessageField );

    wMessageField = createFieldDropDown( wSetupComp, props, meta, formDataBelow( wlMessageField, INPUT_WIDTH, 5 ) );

    FormData fdSetupComp = new FormData();
    fdSetupComp.left = new FormAttachment( 0, 0 );
    fdSetupComp.top = new FormAttachment( 0, 0 );
    fdSetupComp.right = new FormAttachment( 100, 0 );
    fdSetupComp.bottom = new FormAttachment( 100, 0 );
    wSetupComp.setLayoutData( fdSetupComp );
    wSetupComp.layout();
    wSetupTab.setControl( wSetupComp );
  }

  @Override
  public void setSize() {
    setSize( shell );  // sets shell location and preferred size
    shell.setMinimumSize( SHELL_MIN_WIDTH, SHELL_MIN_HEIGHT );
    shell.setSize( SHELL_MIN_WIDTH, SHELL_MIN_HEIGHT ); // force initial size
  }

  private void getData() {
    wMqttServer.setText( nullToEmpty( meta.mqttServer ) );
    wClientId.setText( nullToEmpty( meta.clientId ) );
    topicSelection.setTopicFieldText( nullToEmpty( meta.fieldTopic ) );
    topicSelection.setTopicText( nullToEmpty( meta.topic ) );
    wQOS.setText( nullToEmpty( meta.qos ) );
    wMessageField.setText( nullToEmpty( meta.messageField ) );
    securityLayout.setPassword( meta.password );
    securityLayout.setUsername( meta.username );
    securityLayout.setUIText();
  }

  private void cancel() {
    meta.setChanged( false );
    dispose();
  }

  private void ok() {
    stepname = wStepname.getText();
    meta.mqttServer = wMqttServer.getText();
    meta.clientId = wClientId.getText();

    meta.qos = wQOS.getText();
    meta.messageField = wMessageField.getText();
    meta.username = securityLayout.getUsername();
    meta.password = securityLayout.getPassword();
    meta.useSsl = securityLayout.useSsl();
    meta.setSslConfig( securityLayout.sslConfig() );

    meta.topicInField = topicSelection.isTopicInField();
    meta.fieldTopic = topicSelection.getTopicFieldText();
    meta.topic = topicSelection.getTopicText();

    optionsLayout.retrieveOptions()
      .forEach( option -> {
        switch ( option.getKey() ) {
          case KEEP_ALIVE_INTERVAL:
            meta.keepAliveInterval = option.getValue();
            break;
          case MAX_INFLIGHT:
            meta.maxInflight = option.getValue();
            break;
          case CONNECTION_TIMEOUT:
            meta.connectionTimeout = option.getValue();
            break;
          case CLEAN_SESSION:
            meta.cleanSession = option.getValue();
            break;
          case STORAGE_LEVEL:
            meta.storageLevel = option.getValue();
            break;
          case SERVER_URIS:
            meta.serverUris = option.getValue();
            break;
          case MQTT_VERSION:
            meta.mqttVersion = option.getValue();
            break;
          case AUTOMATIC_RECONNECT:
            meta.automaticReconnect = option.getValue();
            break;
          default:
            log.logBasic( BaseMessages.getString( PKG, "MQTTDialog.Error.UnknownOption", option.getKey() ) );
        }
      } );

    dispose();
  }

  private Image getImage() {
    PluginInterface plugin =
      PluginRegistry.getInstance().getPlugin( StepPluginType.class, stepMeta.getStepMetaInterface() );
    String id = plugin.getIds()[ 0 ];
    if ( id != null ) {
      return GUIResource.getInstance().getImagesSteps().get( id ).getAsBitmapForSize( shell.getDisplay(),
        ConstUI.LARGE_ICON_SIZE, ConstUI.LARGE_ICON_SIZE );
    }
    return null;
  }
}
