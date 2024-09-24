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

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.serialization.Sensitive;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepOption;
import org.pentaho.di.trans.streaming.common.BaseStreamStepMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metaverse.api.analyzer.kettle.annotations.Metaverse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_STRING;
import static org.pentaho.di.core.row.ValueMetaInterface.getTypeDescription;
import static org.pentaho.di.core.util.serialization.ConfigHelper.conf;
import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.mqtt.MQTTClientBuilder.DEFAULT_SSL_OPTS;
import static org.pentaho.di.trans.step.mqtt.MQTTClientBuilder.checkVersion;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.AUTOMATIC_RECONNECT;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.CLEAN_SESSION;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.CLIENT_ID;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.CONNECTION_TIMEOUT;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.KEEP_ALIVE_INTERVAL;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.MAX_INFLIGHT;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.MQTT_SERVER;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.MQTT_VERSION;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.MSG_OUTPUT_NAME;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.PASSWORD;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.QOS;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.SERVER_URIS;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.SSL_GROUP;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.SSL_KEYS;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.SSL_VALUES;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.STORAGE_LEVEL;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.TOPIC;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.TOPICS;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.TOPIC_OUTPUT_NAME;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.USERNAME;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.USE_SSL;
import static org.pentaho.di.trans.step.mqtt.MQTTProducerMeta.MQTT_SERVER_METAVERSE;
import static org.pentaho.di.trans.step.mqtt.MQTTProducerMeta.MQTT_TOPIC_METAVERSE;
import static org.pentaho.dictionary.DictionaryConst.CATEGORY_DATASOURCE;
import static org.pentaho.dictionary.DictionaryConst.CATEGORY_MESSAGE_QUEUE;
import static org.pentaho.dictionary.DictionaryConst.LINK_CONTAINS_CONCEPT;
import static org.pentaho.dictionary.DictionaryConst.LINK_INPUTS;
import static org.pentaho.dictionary.DictionaryConst.LINK_PARENT_CONCEPT;
import static org.pentaho.dictionary.DictionaryConst.LINK_READBY;
import static org.pentaho.dictionary.DictionaryConst.NODE_TYPE_EXTERNAL_CONNECTION;
import static org.pentaho.metaverse.api.analyzer.kettle.annotations.Metaverse.FALSE;
import static org.pentaho.metaverse.api.analyzer.kettle.annotations.Metaverse.SUBTRANS_INPUT;
import static org.pentaho.metaverse.api.analyzer.kettle.step.ExternalResourceStepAnalyzer.RESOURCE;

@Step ( id = "MQTTConsumer", image = "MQTTConsumer.svg",
  i18nPackageName = "org.pentaho.di.trans.step.mqtt",
  name = "MQTTConsumer.TypeLongDesc",
  description = "MQTTConsumer.TypeTooltipDesc",
  categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Streaming",
  documentationUrl = "mk-95pdia003/pdi-transformation-steps/mqtt-consumer" )
@InjectionSupported ( localizationPrefix = "MQTTConsumerMeta.Injection.", groups = { "SSL" } )
@Metaverse.CategoryMap ( entity = MQTT_TOPIC_METAVERSE, category = CATEGORY_MESSAGE_QUEUE )
@Metaverse.CategoryMap ( entity = MQTT_SERVER_METAVERSE, category = CATEGORY_DATASOURCE )
@Metaverse.EntityLink ( entity = MQTT_SERVER_METAVERSE, link = LINK_PARENT_CONCEPT, parentEntity =
  NODE_TYPE_EXTERNAL_CONNECTION )
@Metaverse.EntityLink ( entity = MQTT_TOPIC_METAVERSE, link = LINK_CONTAINS_CONCEPT, parentEntity = MQTT_SERVER_METAVERSE )
@Metaverse.EntityLink ( entity = MQTT_TOPIC_METAVERSE, link = LINK_PARENT_CONCEPT )
public class MQTTConsumerMeta extends BaseStreamStepMeta implements StepMetaInterface {
  private static final Class<?> PKG = MQTTConsumerMeta.class;

  @Metaverse.Node ( name = MQTT_SERVER_METAVERSE, type = MQTT_SERVER_METAVERSE )
  @Metaverse.Property ( name = MQTT_SERVER_METAVERSE, parentNodeName = MQTT_SERVER_METAVERSE )
  @Injection ( name = MQTT_SERVER ) public String mqttServer = "";

  @Metaverse.Node ( name = MQTT_SERVER_METAVERSE, type = MQTT_SERVER_METAVERSE )
  @Metaverse.Property ( name = CLIENT_ID, parentNodeName = MQTT_SERVER_METAVERSE )
  @Injection ( name = CLIENT_ID ) String clientId = "";

  @Metaverse.Node ( name = MQTT_TOPIC_METAVERSE, type = MQTT_TOPIC_METAVERSE, link = LINK_READBY )
  @Metaverse.Property ( name = TOPIC, parentNodeName = MQTT_TOPIC_METAVERSE )
  @Injection ( name = TOPICS ) public List<String> topics = new ArrayList<>();

  @Metaverse.Node ( name = MSG_OUTPUT_NAME, type = RESOURCE, link = LINK_INPUTS, nameFromValue = FALSE, subTransLink = SUBTRANS_INPUT )
  @Metaverse.Property ( name = MSG_OUTPUT_NAME, parentNodeName = MSG_OUTPUT_NAME )
  @Metaverse.NodeLink ( nodeName = MSG_OUTPUT_NAME, parentNodeName = MQTT_TOPIC_METAVERSE, linkDirection = "OUT" )
  @Injection ( name = "MSG_OUTPUT_NAME" ) public String msgOutputName = "Message";

  @Metaverse.Node ( name = TOPIC_OUTPUT_NAME, type = RESOURCE, link = LINK_INPUTS, nameFromValue = FALSE, subTransLink = SUBTRANS_INPUT )
  @Metaverse.Property ( name = TOPIC_OUTPUT_NAME, parentNodeName = TOPIC_OUTPUT_NAME )
  @Metaverse.NodeLink ( nodeName = TOPIC_OUTPUT_NAME, parentNodeName = MQTT_TOPIC_METAVERSE, linkDirection = "OUT" )
  @Injection ( name = "TOPIC_OUTPUT_NAME" ) public String topicOutputName = "Topic";

  @Injection ( name = QOS ) public String qos = "0";

  @Metaverse.Property ( name = USERNAME, parentNodeName = MQTT_SERVER_METAVERSE )
  @Injection ( name = USERNAME ) public String username = "";

  @Sensitive
  @Injection ( name = PASSWORD ) private String password = "";

  @Metaverse.Property ( name = USE_SSL, parentNodeName = MQTT_SERVER_METAVERSE )
  @Injection ( name = USE_SSL, group = SSL_GROUP ) public Boolean useSsl = false;

  @Injection ( name = SSL_KEYS, group = SSL_GROUP ) List<String> sslKeys = new ArrayList<>();

  @Sensitive
  @Injection ( name = SSL_VALUES, group = SSL_GROUP ) List<String> sslValues = new ArrayList<>();

  @Injection ( name = KEEP_ALIVE_INTERVAL )
  private String keepAliveInterval = "";

  @Injection ( name = MAX_INFLIGHT )
  private String maxInflight = "";

  @Injection ( name = CONNECTION_TIMEOUT )
  private String connectionTimeout = "";

  @Injection ( name = CLEAN_SESSION )
  private String cleanSession = "";

  @Injection ( name = STORAGE_LEVEL )
  private String storageLevel = "";

  @Metaverse.Property ( name = SERVER_URIS, parentNodeName = MQTT_SERVER_METAVERSE )
  @Injection ( name = SERVER_URIS )
  public String serverUris = "";

  @Injection ( name = MQTT_VERSION )
  private String mqttVersion = "";

  @Injection ( name = AUTOMATIC_RECONNECT )
  private String automaticReconnect = "";

  @Injection( name = MESSAGE_DATA_TYPE )
  public String messageDataType = getTypeDescription( TYPE_STRING );

  public MQTTConsumerMeta() {
    super();
    setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
  }

  @Override public void setDefault() {
    super.setDefault();
    mqttServer = "";
    qos = "0";
    username = "";
    password = "";
    sslKeys = DEFAULT_SSL_OPTS
      .keySet().stream()
      .sorted()
      .collect( toList() );
    sslValues = sslKeys.stream()
      .map( DEFAULT_SSL_OPTS::get )
      .collect( toList() );

    keepAliveInterval = "";
    maxInflight = "";
    connectionTimeout = "";
    cleanSession = "";
    storageLevel = "";
    serverUris = "";
    mqttVersion = "";
    automaticReconnect = "";
    messageDataType = getTypeDescription( TYPE_STRING );
  }

  @Override
  public int getMessageDataType() {
    return ValueMetaInterface.getTypeCode( messageDataType );
  }

  @Override public String getFileName() {
    return getTransformationPath();
  }


  @Override
  public RowMeta getRowMeta( String origin, VariableSpace space ) {
    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaBase( msgOutputName, getMessageDataType() ) );
    rowMeta.addValueMeta( new ValueMetaString( topicOutputName ) );
    return rowMeta;
  }

  @Override public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
                                Trans trans ) {
    return new MQTTConsumer( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override public StepDataInterface getStepData() {
    return new MQTTConsumerData();
  }

  @SuppressWarnings ( { "deprecation" } )
  // can be removed once the new @PluginDialog annotation supports OSGi
  public String getDialogClassName() {
    return "org.pentaho.di.trans.step.mqtt.MQTTConsumerDialog";
  }

  public String getMqttServer() {
    return mqttServer;
  }

  public void setMqttServer( String mqttServer ) {
    this.mqttServer = mqttServer;
  }

  public List<String> getTopics() {
    return topics;
  }

  public void setTopics( List<String> topics ) {
    this.topics = topics;
  }

  public void setClientId( String clientId ) {
    this.clientId = clientId;
  }

  public String getClientId() {
    return clientId;
  }

  public String getMsgOutputName() {
    return msgOutputName;
  }

  public void setMsgOutputName( String msgOutputName ) {
    this.msgOutputName = msgOutputName;
  }

  public String getTopicOutputName() {
    return topicOutputName;
  }

  public void setTopicOutputName( String topicOutputName ) {
    this.topicOutputName = topicOutputName;
  }

  public String getQos() {
    return qos;
  }

  public void setQos( String qos ) {
    this.qos = qos;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername( String username ) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword( String password ) {
    this.password = password;
  }

  public Map<String, String> getSslConfig() {
    return conf( sslKeys, sslValues ).asMap();
  }

  public void setSslConfig( Map<String, String> sslConfig ) {
    sslKeys = conf( sslConfig ).keys();
    sslValues = conf( sslConfig ).vals();
  }

  public boolean isUseSsl() {
    return useSsl;
  }

  public void setUseSsl( boolean useSsl ) {
    this.useSsl = useSsl;
  }

  public String getKeepAliveInterval() {
    return keepAliveInterval;
  }

  public void setKeepAliveInterval( String keepAliveInterval ) {
    this.keepAliveInterval = keepAliveInterval;
  }

  public String getMaxInflight() {
    return maxInflight;
  }

  public void setMaxInflight( String maxInflight ) {
    this.maxInflight = maxInflight;
  }

  public String getConnectionTimeout() {
    return connectionTimeout;
  }

  public void setConnectionTimeout( String connectionTimeout ) {
    this.connectionTimeout = connectionTimeout;
  }

  public String getCleanSession() {
    return cleanSession;
  }

  public void setCleanSession( String cleanSession ) {
    this.cleanSession = cleanSession;
  }

  public String getStorageLevel() {
    return storageLevel;
  }

  public void setStorageLevel( String storageLevel ) {
    this.storageLevel = storageLevel;
  }

  public String getServerUris() {
    return serverUris;
  }

  public void setServerUris( String serverUris ) {
    this.serverUris = serverUris;
  }

  public String getMqttVersion() {
    return mqttVersion;
  }

  public void setMqttVersion( String mqttVersion ) {
    this.mqttVersion = mqttVersion;
  }

  public String getAutomaticReconnect() {
    return automaticReconnect;
  }

  public void setAutomaticReconnect( String automaticReconnect ) {
    this.automaticReconnect = automaticReconnect;
  }

  List<StepOption> retrieveOptions() {
    return Arrays.asList(
      new StepOption( KEEP_ALIVE_INTERVAL, getString( PKG, "MQTTDialog.Options.KEEP_ALIVE_INTERVAL" ),
        keepAliveInterval ),
      new StepOption( MAX_INFLIGHT, getString( PKG, "MQTTDialog.Options.MAX_INFLIGHT" ), maxInflight ),
      new StepOption( CONNECTION_TIMEOUT, getString( PKG, "MQTTDialog.Options.CONNECTION_TIMEOUT" ),
        connectionTimeout ),
      new StepOption( CLEAN_SESSION, getString( PKG, "MQTTDialog.Options.CLEAN_SESSION" ),
        cleanSession ),
      new StepOption( STORAGE_LEVEL, getString( PKG, "MQTTDialog.Options.STORAGE_LEVEL" ),
        storageLevel ),
      new StepOption( SERVER_URIS, getString( PKG, "MQTTDialog.Options.SERVER_URIS" ), serverUris ),
      new StepOption( MQTT_VERSION, getString( PKG, "MQTTDialog.Options.MQTT_VERSION" ), mqttVersion ),
      new StepOption( AUTOMATIC_RECONNECT, getString( PKG, "MQTTDialog.Options.AUTOMATIC_RECONNECT" ),
        automaticReconnect )
    );
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta,
                     StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output,
                     RowMetaInterface info, VariableSpace space, Repository repository,
                     IMetaStore metaStore ) {
    super.check( remarks, transMeta, stepMeta, prev, input, output, info, space, repository, metaStore );

    StepOption.checkInteger( remarks, stepMeta, space, getString( PKG, "MQTTDialog.Options.KEEP_ALIVE_INTERVAL" ),
      keepAliveInterval );
    StepOption
      .checkInteger( remarks, stepMeta, space, getString( PKG, "MQTTDialog.Options.MAX_INFLIGHT" ), maxInflight );
    StepOption.checkInteger( remarks, stepMeta, space, getString( PKG, "MQTTDialog.Options.CONNECTION_TIMEOUT" ),
      connectionTimeout );
    StepOption
      .checkBoolean( remarks, stepMeta, space, getString( PKG, "MQTTDialog.Options.CLEAN_SESSION" ), cleanSession );
    checkVersion( remarks, stepMeta, space, mqttVersion );
    StepOption.checkBoolean( remarks, stepMeta, space, getString( PKG, "MQTTDialog.Options.AUTOMATIC_RECONNECT" ),
      automaticReconnect );
  }

  @Override public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }
    MQTTConsumerMeta that = (MQTTConsumerMeta) o;
    return Objects.equals( useSsl, that.useSsl )
      && Objects.equals( mqttServer, that.mqttServer )
      && Objects.equals( topics, that.topics )
      && Objects.equals( msgOutputName, that.msgOutputName )
      && Objects.equals( topicOutputName, that.topicOutputName )
      && Objects.equals( qos, that.qos )
      && Objects.equals( sslKeys, that.sslKeys )
      && Objects.equals( sslValues, that.sslValues )
      && Objects.equals( username, that.username )
      && Objects.equals( password, that.password )
      && Objects.equals( keepAliveInterval, that.keepAliveInterval )
      && Objects.equals( maxInflight, that.maxInflight )
      && Objects.equals( connectionTimeout, that.connectionTimeout )
      && Objects.equals( cleanSession, that.cleanSession )
      && Objects.equals( storageLevel, that.storageLevel )
      && Objects.equals( serverUris, that.serverUris )
      && Objects.equals( mqttVersion, that.mqttVersion )
      && Objects.equals( automaticReconnect, that.automaticReconnect );
  }

  @Override public int hashCode() {
    return Objects
      .hash( mqttServer, topics, msgOutputName, topicOutputName, qos, useSsl, sslKeys,
        sslValues, username, password, keepAliveInterval, maxInflight, connectionTimeout,
        cleanSession, storageLevel, serverUris, mqttVersion, automaticReconnect );
  }
}
