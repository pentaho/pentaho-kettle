/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2020 by Hitachi Vantara : http://www.pentaho.com
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

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith ( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest ( MQTTClientBuilder.class )
public class MQTTProducerTest {
  @Mock MqttClient mqttClient;
  @Mock LogChannelInterfaceFactory logChannelFactory;
  @Mock LogChannelInterface logChannel;

  private Trans trans;

  @BeforeClass
  public static void setupClass() throws Exception {

    StepPluginType.getInstance().handlePluginAnnotation(
      MQTTProducerMeta.class,
      MQTTProducerMeta.class.getAnnotation( Step.class ),
      Collections.emptyList(), false, null );
    KettleEnvironment.init();
  }

  @Before
  public void setup() throws Exception {
    KettleLogStore.setLogChannelInterfaceFactory( logChannelFactory );
    when( logChannelFactory.create( any(), any() ) ).thenReturn( logChannel );
    when( logChannelFactory.create( any() ) ).thenReturn( logChannel );

    TransMeta transMeta = new TransMeta( getClass().getResource( "/ProduceFourRows.ktr" ).getPath() );
    trans = new Trans( transMeta );
    trans.setVariable( "mqttServer", "127.0.0.1:1883" );
    trans.setVariable( "clientId", "client1" );
    trans.setVariable( "topic", "TestWinning" );
    trans.setVariable( "messageField", "message" );
    trans.setVariable( "qos", "0" );
    trans.prepareExecution( new String[] {} );
  }

  @Test
  public void testSendRowToProducer() throws Exception {
    when( mqttClient.isConnected() ).thenReturn( true );
    handleAsSecondRow( trans );
    doAnswer( invocation -> {
      String topic = (String) invocation.getArguments()[ 0 ];
      MqttMessage message = (MqttMessage) invocation.getArguments()[ 1 ];

      assertEquals( "TestWinning", topic );
      assertEquals( 0, message.getQos() );
      assertEquals( "#winning", new String( message.getPayload(), UTF_8 ) );
      return null;
    } ).when( mqttClient ).publish( any(), any() );

    trans.startThreads();
    trans.waitUntilFinished();

    verify( mqttClient ).disconnect();
    assertEquals( 4, trans.getSteps().get( 1 ).step.getLinesOutput() );
  }

  @Test
  public void testSendBinaryToProducer() throws Exception {
    TransMeta transMeta = new TransMeta( getClass().getResource( "/ProduceFourBinaryRows.ktr" ).getPath() );
    Trans binaryTrans = new Trans( transMeta );
    binaryTrans.prepareExecution( new String[] {} );
    when( mqttClient.isConnected() ).thenReturn( true );
    handleAsSecondRow( binaryTrans );
    doAnswer( invocation -> {
      String topic = (String) invocation.getArguments()[ 0 ];
      MqttMessage message = (MqttMessage) invocation.getArguments()[ 1 ];

      assertEquals( "TestLosing", topic );
      assertEquals( 0, message.getQos() );
      assertEquals( "#losing", new String( message.getPayload(), UTF_8 ) );
      return null;
    } ).when( mqttClient ).publish( any(), any() );

    binaryTrans.startThreads();
    binaryTrans.waitUntilFinished();

    verify( mqttClient ).disconnect();
    assertEquals( 4, binaryTrans.getSteps().get( 1 ).step.getLinesOutput() );
  }

  @Test
  public void testInvalidQOS() throws Exception {
    trans.setVariable( "qos", "hello" );
    trans.prepareExecution( new String[] {} );

    // Need to set first = false again since prepareExecution was called with the new variable.
    StepMetaDataCombi combi = trans.getSteps().get( 1 );
    MQTTProducer step = (MQTTProducer) combi.step;
    step.first = false;

    trans.startThreads();
    trans.waitUntilFinished();

    verify( logChannel )
      .logError( eq( "MQTT Producer - Quality of Service level hello is invalid. Please set a level of 0, 1, or 2" ) );
    verify( mqttClient, never() ).publish( any(), any() );
  }

  @Test
  public void testMqttClientIsMemoized() throws Exception {
    PowerMockito.mockStatic( MQTTClientBuilder.class );
    MQTTClientBuilder clientBuilder = spy( MQTTClientBuilder.class );
    MqttClient mqttClient = mock( MqttClient.class );
    doReturn( mqttClient ).when( clientBuilder ).buildAndConnect();
    PowerMockito.when( MQTTClientBuilder.builder() ).thenReturn( clientBuilder );

    trans.startThreads();
    trans.waitUntilFinished();

    StepMetaDataCombi combi = trans.getSteps().get( 1 );
    MQTTProducer step = (MQTTProducer) combi.step;

    // verify repeated retrieval of client returns the *same* client.
    assertEquals( step.client.get(), step.client.get() );
  }

  @Test
  public void testFeedbackSize() throws Exception {
    handleAsSecondRow( trans );

    StepMetaDataCombi combi = trans.getSteps().get( 1 );
    MQTTProducer step = (MQTTProducer) combi.step;

    when( logChannel.isBasic() ).thenReturn( true );
    step.getTransMeta().setFeedbackSize( 1 );

    trans.startThreads();
    trans.waitUntilFinished();

    verify( logChannel ).logBasic( eq( "Linenr1" ) );
  }

  @Test
  public void testMqttConnectException() throws Exception {
    PowerMockito.mockStatic( MQTTClientBuilder.class );
    MQTTClientBuilder clientBuilder = spy( MQTTClientBuilder.class );
    MqttException mqttException = mock( MqttException.class );
    when( mqttException.toString() ).thenReturn( "There was an error connecting" );
    doThrow( mqttException ).when( clientBuilder ).buildAndConnect();
    PowerMockito.when( MQTTClientBuilder.builder() ).thenReturn( clientBuilder );

    trans.startThreads();
    trans.waitUntilFinished();

    verify( logChannel ).logError( eq( "There was an error connecting" ) );
  }

  @Test
  public void testErrorOnPublishStopsAll() throws Exception {
    handleAsSecondRow( trans );

    MqttException mqttException = mock( MqttException.class );
    when( mqttException.getMessage() ).thenReturn( "publish failed" );
    when( mqttClient.isConnected() ).thenReturn( true, false );
    doThrow( mqttException ).when( mqttClient ).publish( any(), any() );

    trans.startThreads();
    trans.waitUntilFinished();

    verify( mqttClient ).disconnect();
    verify( logChannel ).logError(
      "MQTT Producer - Received an exception publishing the message."
        + "  Check that Quality of Service level 0 is supported by your MQTT Broker" );
    verify( logChannel ).logError( "publish failed", mqttException );
    assertEquals( 0, trans.getSteps().get( 1 ).step.getLinesOutput() );
  }

  private void handleAsSecondRow( Trans trans ) {
    StepMetaDataCombi combi = trans.getSteps().get( 1 );
    MQTTProducer step = (MQTTProducer) combi.step;
    step.client = () -> mqttClient;
    step.first = false;
  }
}
