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
import org.pentaho.di.core.util.GenericStepData;
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
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.TOPIC_IN_FIELD;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.USERNAME;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.USE_SSL;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.FIELD_TOPIC;
import static org.pentaho.di.core.util.serialization.ConfigHelper.conf;

@Step ( id = "MQTTProducer", image = "MQTTProducer.svg",
  i18nPackageName = "org.pentaho.di.trans.step.mqtt",
  name = "MQTTProducer.TypeLongDesc",
  description = "MQTTProducer.TypeTooltipDesc",
  categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Streaming",
  documentationUrl = "Products/Data_Integration/Transformation_Step_Reference/MQTT_Producer" )
@InjectionSupported ( localizationPrefix = "MQTTProducerMeta.Injection.", groups = { "SSL" } )
public class MQTTProducerMeta extends BaseSerializingMeta implements StepMetaInterface {
  private static Class<?> PKG = MQTTProducerMeta.class;

  @Injection ( name = MQTT_SERVER ) String mqttServer;

  @Injection ( name = CLIENT_ID ) String clientId;

  @Injection ( name = TOPIC ) String topic;

  @Injection ( name = FIELD_TOPIC ) String fieldTopic;

  @Injection ( name = TOPIC_IN_FIELD ) Boolean topicInField = false;

  @Injection ( name = QOS ) String qos;

  @Injection ( name = MESSAGE_FIELD ) String messageField;

  @Injection ( name = USERNAME ) String username;

  @Sensitive
  @Injection ( name = PASSWORD ) String password;

  @Injection ( name = USE_SSL, group = SSL_GROUP ) Boolean useSsl = false;

  @Injection ( name = SSL_KEYS, group = SSL_GROUP ) private List<String> sslKeys = new ArrayList<>();

  @Sensitive
  @Injection ( name = SSL_VALUES, group = SSL_GROUP ) private List<String> sslValues = new ArrayList<>();

  @Injection ( name = KEEP_ALIVE_INTERVAL ) String keepAliveInterval;

  @Injection ( name = MAX_INFLIGHT ) String maxInflight;

  @Injection ( name = CONNECTION_TIMEOUT ) String connectionTimeout;

  @Injection ( name = CLEAN_SESSION ) String cleanSession;

  @Injection ( name = STORAGE_LEVEL ) String storageLevel;

  @Injection ( name = SERVER_URIS ) String serverUris;

  @Injection ( name = MQTT_VERSION ) String mqttVersion;

  @Injection ( name = AUTOMATIC_RECONNECT ) String automaticReconnect;

  public MQTTProducerMeta() {
    super();
  }

  @Override
  public void setDefault() {
    mqttServer = "";
    topic = "";
    fieldTopic = "";
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
    return new GenericStepData();
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

  @SuppressWarnings ( { "deprecation" } )
  // can be removed once the new @PluginDialog annotation supports OSGi
  @Override
  public String getDialogClassName() {
    return "org.pentaho.di.trans.step.mqtt.MQTTProducerDialog";
  }


  Map<String, String> getSslConfig() {
    return conf( sslKeys, sslValues ).asMap();
  }

  void setSslConfig( Map<String, String> sslConfig ) {
    sslKeys = conf( sslConfig ).keys();
    sslValues = conf( sslConfig ).vals();
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
      && Objects.equal( fieldTopic, that.fieldTopic )
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
      .hashCode( mqttServer, clientId, topic, fieldTopic, qos, messageField, username, password, useSsl, sslKeys, sslValues,
        keepAliveInterval, maxInflight, connectionTimeout, cleanSession, storageLevel, serverUris, mqttVersion,
        automaticReconnect );
  }

  @Override public String toString() {
    return Objects.toStringHelper( this )
      .add( "mqttServer", mqttServer )
      .add( "clientId", clientId )
      .add( "topic", topic )
      .add( "fieldTopic", fieldTopic )
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

  @Override public MQTTProducerMeta copyObject() {
    MQTTProducerMeta newClone = (MQTTProducerMeta) this.clone();
    newClone.sslKeys = new ArrayList<>( this.sslKeys );
    newClone.sslValues = new ArrayList<>( this.sslValues );
    return newClone;
  }
}
