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

import com.google.common.base.Objects;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.serialization.BaseSerializingMeta;
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
import org.pentaho.metastore.api.IMetaStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.mqtt.MQTTClientBuilder.DEFAULT_SSL_OPTS;
import static org.pentaho.di.trans.step.mqtt.MQTTClientBuilder.checkVersion;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.AUTOMATIC_RECONNECT;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.CLEAN_SESSION;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.CLIENT_ID;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.CONNECTION_TIMEOUT;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.KEEP_ALIVE_INTERVAL;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.MAX_INFLIGHT;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.MESSAGE_FIELD;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.MQTT_SERVER;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.MQTT_VERSION;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.PASSWORD;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.QOS;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.SERVER_URIS;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.SSL_GROUP;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.SSL_KEYS;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.SSL_VALUES;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.STORAGE_LEVEL;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.TOPIC;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.USERNAME;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.USE_SSL;
import static org.pentaho.di.core.util.serialization.ConfigHelper.conf;

@Step ( id = "MQTTProducer", image = "MQTTProducer.svg",
  i18nPackageName = "org.pentaho.di.trans.step.mqtt",
  name = "MQTTProducer.TypeLongDesc",
  description = "MQTTProducer.TypeTooltipDesc",
  categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Streaming" )
@InjectionSupported ( localizationPrefix = "MQTTProducerMeta.Injection.", groups = { "SSL" }  )
public class MQTTProducerMeta extends BaseSerializingMeta implements StepMetaInterface {
  private static Class<?> PKG = MQTTProducerMeta.class;

  @Injection ( name = MQTT_SERVER )
  private String mqttServer;

  @Injection ( name = CLIENT_ID )
  private String clientId;

  @Injection ( name = TOPIC )
  private String topic;

  @Injection ( name = QOS )
  private String qos;

  @Injection ( name = MESSAGE_FIELD )
  private String messageField;

  @Injection ( name = USERNAME )
  private String username;

  @Sensitive
  @Injection ( name = PASSWORD )
  private String password;

  @Injection ( name = USE_SSL, group = SSL_GROUP )
  private Boolean useSsl = false;

  @Injection ( name = SSL_KEYS, group = SSL_GROUP )
  private List<String> sslKeys = new ArrayList<>();

  @Sensitive
  @Injection ( name = SSL_VALUES, group = SSL_GROUP )
  private List<String> sslValues = new ArrayList<>();

  @Injection( name = KEEP_ALIVE_INTERVAL )
  private String keepAliveInterval;

  @Injection( name = MAX_INFLIGHT )
  private String maxInflight;

  @Injection( name = CONNECTION_TIMEOUT )
  private String connectionTimeout;

  @Injection( name = CLEAN_SESSION )
  private String cleanSession;

  @Injection( name = STORAGE_LEVEL )
  private String storageLevel;

  @Injection( name = SERVER_URIS )
  private String serverUris;

  @Injection( name = MQTT_VERSION )
  private String mqttVersion;

  @Injection( name = AUTOMATIC_RECONNECT )
  private String automaticReconnect;

  public MQTTProducerMeta() {
    super();
  }

  @Override
  public void setDefault() {
    mqttServer = "";
    topic = "";
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
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                                Trans trans ) {
    return new MQTTProducer( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new MQTTProducerData();
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

  public List<StepOption> retrieveOptions() {
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

  @SuppressWarnings ( { "deprecation" } )
  // can be removed once the new @PluginDialog annotation supports OSGi
  @Override
  public String getDialogClassName() {
    return "org.pentaho.di.trans.step.mqtt.MQTTProducerDialog";
  }

  public String getMqttServer() {
    return mqttServer;
  }

  public void setMqttServer( String mqttServer ) {
    this.mqttServer = mqttServer;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId( String clientId ) {
    this.clientId = clientId;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic( String topic ) {
    this.topic = topic;
  }

  public String getQOS() {
    return qos;
  }

  public void setQOS( String qos ) {
    this.qos = qos;
  }

  public String getMessageField() {
    return messageField;
  }

  public void setMessageField( String messageField ) {
    this.messageField = messageField;
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

  @Override public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }
    MQTTProducerMeta that = (MQTTProducerMeta) o;
    return Objects.equal( mqttServer, that.mqttServer )
      && Objects.equal( clientId, that.clientId )
      && Objects.equal( topic, that.topic )
      && Objects.equal( qos, that.qos )
      && Objects.equal( messageField, that.messageField )
      && Objects.equal( username, that.username )
      && Objects.equal( password, that.password )
      && Objects.equal( useSsl, that.useSsl )
      && Objects.equal( sslKeys, that.sslKeys )
      && Objects.equal( sslValues, that.sslValues )
      && Objects.equal( keepAliveInterval, that.keepAliveInterval )
      && Objects.equal( maxInflight, that.maxInflight )
      && Objects.equal( connectionTimeout, that.connectionTimeout )
      && Objects.equal( cleanSession, that.cleanSession )
      && Objects.equal( storageLevel, that.storageLevel )
      && Objects.equal( serverUris, that.serverUris )
      && Objects.equal( mqttVersion, that.mqttVersion )
      && Objects.equal( automaticReconnect, that.automaticReconnect );
  }

  @Override public int hashCode() {
    return Objects
      .hashCode( mqttServer, clientId, topic, qos, messageField, username, password, useSsl, sslKeys, sslValues,
        keepAliveInterval, maxInflight, connectionTimeout, cleanSession, storageLevel, serverUris, mqttVersion,
        automaticReconnect );
  }

  @Override public String toString() {
    return Objects.toStringHelper( this )
      .add( "mqttServer", mqttServer )
      .add( "clientId", clientId )
      .add( "topic", topic )
      .add( "qos", qos )
      .add( "messageField", messageField )
      .add( "username", username )
      .add( "password", password )
      .add( "useSsl", useSsl )
      .add( "sslKeys", sslKeys )
      .add( "sslValues", sslValues )
      .add( "keepAliveInterval", keepAliveInterval )
      .add( "maxInflight", maxInflight )
      .add( "connectionTimeout", connectionTimeout )
      .add( "cleanSession", cleanSession )
      .add( "storageLevel", storageLevel )
      .add( "serverUris", serverUris )
      .add( "mqttVersion", mqttVersion )
      .add( "automaticReconnect", automaticReconnect )
      .toString();
  }

  public void setAutomaticReconnect( String automaticReconnect ) {
    this.automaticReconnect = automaticReconnect;
  }
}
