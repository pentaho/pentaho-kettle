/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.BooleanUtils;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.MQTT_VERSION;

public final class MQTTClientBuilder {
  private static final Class<?> PKG = MQTTClientBuilder.class;

  private static final String UNSECURE_PROTOCOL = "tcp://";
  private static final String SECURE_PROTOCOL = "ssl://";
  // the paho library specifies ssl prop names as com.ibm, though not necessarily using the ibm implementations
  private static final String SSL_PROP_PREFIX = "com.ibm.";

  static final ImmutableMap<String, String> DEFAULT_SSL_OPTS = ImmutableMap.<String, String>builder()
    .put( "ssl.protocol", "TLS" )
    .put( "ssl.contextProvider", "" )
    .put( "ssl.keyStore", "" )
    .put( "ssl.keyStorePassword", "" )
    .put( "ssl.keyStoreType", "JKS" )
    .put( "ssl.keyStoreProvider", "" )
    .put( "ssl.trustStore", "" )
    .put( "ssl.trustStorePassword", "" )
    .put( "ssl.trustStoreType", "" )
    .put( "ssl.trustStoreProvider", "" )
    .put( "ssl.enabledCipherSuites", "" )
    .put( "ssl.keyManager", "" )
    .put( "ssl.trustManager", "" )
    .build();

  private String broker;
  private List<String> topics;
  private String qos = "0";
  private boolean isSecure;
  private String username;
  private String password;
  private Map<String, String> sslConfig;
  private String keepAliveInterval;
  private String maxInflight;
  private String connectionTimeout;
  private String cleanSession;
  private String storageLevel;
  private String serverUris;
  private String mqttVersion;
  private String automaticReconnect;
  private MqttCallback callback;
  private String clientId = MqttAsyncClient.generateClientId();  // default
  private LogChannelInterface logChannel;
  private String stepName;


  @VisibleForTesting @FunctionalInterface interface ClientFactory {
    MqttClient getClient( String broker, String clientId, MqttClientPersistence persistence )
      throws MqttException;
  }

  @VisibleForTesting ClientFactory clientFactory = MqttClient::new;

  private MQTTClientBuilder() {
  }

  public static MQTTClientBuilder builder() {
    return new MQTTClientBuilder();
  }

  public MQTTClientBuilder withCallback( MqttCallback callback ) {
    this.callback = callback;
    return this;
  }

  public MQTTClientBuilder withBroker( String broker ) {
    this.broker = broker;
    return this;
  }

  public MQTTClientBuilder withTopics( List<String> topics ) {
    this.topics = topics;
    return this;
  }

  public MQTTClientBuilder withQos( String qos ) {
    this.qos = qos;
    return this;
  }

  public MQTTClientBuilder withIsSecure( boolean isSecure ) {
    this.isSecure = isSecure;
    return this;
  }

  public MQTTClientBuilder withClientId( String clientId ) {
    this.clientId = clientId;
    return this;
  }

  public MQTTClientBuilder withUsername( String username ) {
    this.username = username;
    return this;
  }

  public MQTTClientBuilder withPassword( String password ) {
    this.password = password;
    return this;
  }

  public MQTTClientBuilder withStep( StepInterface step ) {
    this.logChannel = step.getLogChannel();
    this.stepName = step.getStepMeta().getName();
    return this;
  }

  public MQTTClientBuilder withSslConfig( Map<String, String> sslConfig ) {
    this.sslConfig = sslConfig;
    return this;
  }

  public MQTTClientBuilder withKeepAliveInterval( String keepAliveInterval ) {
    this.keepAliveInterval = keepAliveInterval;
    return this;
  }

  public MQTTClientBuilder withMaxInflight( String maxInflight ) {
    this.maxInflight = maxInflight;
    return this;
  }

  public MQTTClientBuilder withConnectionTimeout( String connectionTimeout ) {
    this.connectionTimeout = connectionTimeout;
    return this;
  }

  public MQTTClientBuilder withCleanSession( String cleanSession ) {
    this.cleanSession = cleanSession;
    return this;
  }

  public MQTTClientBuilder withStorageLevel( String storageLevel ) {
    this.storageLevel = storageLevel;
    return this;
  }

  public MQTTClientBuilder withServerUris( String serverUris ) {
    this.serverUris = serverUris;
    return this;
  }

  public  MQTTClientBuilder withMqttVersion( String mqttVersion ) {
    this.mqttVersion = mqttVersion;
    return this;
  }

  public MQTTClientBuilder withAutomaticReconnect( String automaticReconnect ) {
    this.automaticReconnect = automaticReconnect;
    return this;
  }

  public MqttClient buildAndConnect() throws MqttException {
    validateArgs();

    String protocolBroker = getProtocol() + this.broker;
    MqttClientPersistence persistence = new MemoryPersistence();
    if ( StringUtil.isEmpty( storageLevel ) ) {
      logChannel.logDebug( "Using Memory Storage Level" );
    } else {
      logChannel.logDebug( "Using File Storage Level to " + storageLevel );
      persistence = new MqttDefaultFilePersistence( storageLevel );
    }

    if ( StringUtil.isEmpty( clientId ) ) {
      clientId = MqttAsyncClient.generateClientId();
    }

    MqttClient client = clientFactory.getClient( protocolBroker, clientId, persistence );

    client.setCallback( callback );

    logChannel.logDebug( "Subscribing to topics with a quality of service level of " + qos );
    logChannel.logDebug( "Server URIs is set to " + serverUris );
    logChannel.logDebug( "Max Inflight is set to " + maxInflight );
    logChannel.logDebug( "Automatic Reconnect is set to " + automaticReconnect );
    logChannel.logDebug( loggableOptions().toString() );

    client.connect( getOptions() );
    if ( topics != null && !topics.isEmpty() ) {
      client.subscribe(
        topics.toArray( new String[ 0 ] ),
        initializedIntAray( Integer.parseInt( this.qos ) )
      );
    }
    return client;
  }

  private String getProtocol() {
    return isSecure ? SECURE_PROTOCOL : UNSECURE_PROTOCOL;
  }

  private void validateArgs() {
    // expectation that the broker will contain the server:port.
    checkArgument( this.broker.matches( "^[^ :/]+:\\d+" ),
      getString( PKG, "MQTTInput.Error.ConnectionURL" ) );
    try {
      int qosVal = Integer.parseInt( this.qos );
      checkArgument( qosVal >= 0 && qosVal <= 2 );
    } catch ( Exception e ) {
      throw new IllegalArgumentException( getString( PKG, "MQTTClientBuilder.Error.QOS", stepName, qos ) );
    }
  }

  private int[] initializedIntAray( int val ) {
    return IntStream.range( 0, topics.size() ).map( i -> val ).toArray();
  }

  private MqttConnectOptions getOptions() {
    MqttConnectOptions options = new MqttConnectOptions();

    if ( isSecure ) {
      setSSLProps( options );
    }
    if ( !StringUtil.isEmpty( username ) ) {
      options.setUserName( username );
    }
    if ( !StringUtil.isEmpty( password ) ) {
      options.setPassword( password.toCharArray() );
    }

    if ( !StringUtil.isEmpty( keepAliveInterval ) ) {
      options.setKeepAliveInterval( Integer.parseInt( keepAliveInterval ) );
    }

    if ( !StringUtil.isEmpty( maxInflight ) ) {
      options.setMaxInflight( Integer.parseInt( maxInflight ) );
    }

    if ( !StringUtil.isEmpty( connectionTimeout ) ) {
      options.setConnectionTimeout( Integer.parseInt( connectionTimeout ) );
    }

    if ( !StringUtil.isEmpty( cleanSession ) ) {
      options.setCleanSession( BooleanUtils.toBoolean( cleanSession ) );
    }

    if ( !StringUtil.isEmpty( serverUris ) ) {
      options.setServerURIs(
        Arrays.stream( serverUris.split( ";" ) ).map( uri -> getProtocol() + uri ).toArray( String[]::new ) );
    }

    if ( !StringUtil.isEmpty( mqttVersion ) ) {
      options.setMqttVersion( Integer.parseInt( mqttVersion ) );
    }

    if ( !StringUtil.isEmpty( automaticReconnect ) ) {
      options.setAutomaticReconnect( BooleanUtils.toBoolean( automaticReconnect ) );
    }

    return options;
  }

  /**
   * Returns a copy of loggable options with sensitive data stripped.
   */
  private MqttConnectOptions loggableOptions() {
    MqttConnectOptions loggableOptions = getOptions();

    Optional.ofNullable( loggableOptions.getSSLProperties() )
      .orElseGet( Properties::new )
      .keySet().stream()
      .filter( key -> key.toString().toUpperCase().contains( "PASSWORD" ) )
      .forEach( key -> loggableOptions.getSSLProperties().put( key, "*****" ) );
    return loggableOptions;
  }

  private void setSSLProps( MqttConnectOptions options ) {
    Properties props = new Properties();
    props.putAll(
      sslConfig.entrySet().stream()
        .filter( entry -> !isNullOrEmpty( entry.getValue() ) )
        .collect( Collectors.toMap( e -> SSL_PROP_PREFIX + e.getKey(),
          Map.Entry::getValue ) ) );
    options.setSSLProperties( props );
  }

  static void checkVersion( List<CheckResultInterface> remarks, StepMeta stepMeta, VariableSpace space,
                            String value ) {
    String version = space.environmentSubstitute( value );
    if ( !StringUtil.isEmpty( version ) ) {
      try {
        ( new MqttConnectOptions() ).setMqttVersion( Integer.parseInt( version ) );
      } catch ( Exception e ) {
        remarks.add( new CheckResult(
          CheckResultInterface.TYPE_RESULT_ERROR,
          getString( PKG, "MQTTMeta.CheckResult.NotCorrectVersion",
            getString( PKG, "MQTTDialog.Options." + MQTT_VERSION ), version ),
          stepMeta ) );
      }
    }
  }
}
