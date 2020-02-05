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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.pentaho.di.trans.streaming.common.BlockingQueueStreamSource;

import java.util.List;

import static java.util.Collections.singletonList;

/**
 * StreamSource implementation which supplies a blocking iterable of MQTT messages based on the specified topics,
 * broker, and qos. The parent class .rows() method is responsible for creating the blocking iterable.
 */
public class MQTTStreamSource extends BlockingQueueStreamSource<List<Object>> {
  private final MQTTConsumerMeta mqttConsumerMeta;
  private final MQTTConsumer mqttConsumer;

  @VisibleForTesting MqttClient mqttClient;

  private MqttCallback callback = new MqttCallback() {
    @Override public void connectionLost( Throwable cause ) {
      error( cause );
    }

    @Override public void messageArrived( String topic, MqttMessage message ) {
      acceptRows( singletonList( ImmutableList.of( readBytes( message.getPayload() ), topic ) ) );
    }

    @Override public void deliveryComplete( IMqttDeliveryToken token ) {
      // no op
    }
  };

  MQTTStreamSource( MQTTConsumerMeta mqttConsumerMeta, MQTTConsumer mqttConsumer ) {
    super( mqttConsumer );
    this.mqttConsumer = mqttConsumer;
    this.mqttConsumerMeta = mqttConsumerMeta;
  }

  @Override public void open() {
    try {
      mqttClient = MQTTClientBuilder.builder()
        .withClientId( mqttConsumerMeta.getClientId() )
        .withBroker( mqttConsumerMeta.getMqttServer() )
        .withTopics( mqttConsumerMeta.getTopics() )
        .withQos( mqttConsumerMeta.getQos() )
        .withStep( mqttConsumer )
        .withCallback( callback )
        .withUsername( mqttConsumerMeta.getUsername() )
        .withPassword( mqttConsumerMeta.getPassword() )
        .withIsSecure( mqttConsumerMeta.isUseSsl() )
        .withSslConfig( mqttConsumerMeta.getSslConfig() )
        .withKeepAliveInterval( mqttConsumerMeta.getKeepAliveInterval() )
        .withMaxInflight( mqttConsumerMeta.getMaxInflight() )
        .withConnectionTimeout( mqttConsumerMeta.getConnectionTimeout() )
        .withCleanSession( mqttConsumerMeta.getCleanSession() )
        .withStorageLevel( mqttConsumerMeta.getStorageLevel() )
        .withServerUris( mqttConsumerMeta.getServerUris() )
        .withMqttVersion( mqttConsumerMeta.getMqttVersion() )
        .withAutomaticReconnect( mqttConsumerMeta.getAutomaticReconnect() )
        .buildAndConnect();
    } catch ( Exception e ) {
      mqttConsumer.stopAll();
      mqttConsumer.logError( e.toString() );
    }
  }

  @Override public void close() {
    super.close();
    try {
      // Check if connected so subsequent calls does not produce an already stopped exception
      if ( mqttClient != null && mqttClient.isConnected() ) {
        mqttClient.disconnect();
        mqttClient.close();
      }
    } catch ( MqttException e ) {
      mqttConsumer.logError( e.getMessage() );
    }
  }
}
