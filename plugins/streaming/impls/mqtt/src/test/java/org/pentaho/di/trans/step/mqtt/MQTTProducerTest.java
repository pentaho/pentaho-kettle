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

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

@RunWith( PowerMockRunner.class )
@PrepareForTest( MQTTClientBuilder.class )
public class MQTTProducerTest {
  @Mock MqttClient mqttClient;
  @Mock LogChannelInterfaceFactory logChannelFactory;
  @Mock LogChannelInterface logChannel;

  private Trans trans;

  @BeforeClass
  public static void setupClass() throws Exception {

    StepPluginType.getInstance().handlePluginAnnotation(
      MQTTProducerMeta.class,
      MQTTProducerMeta.class.getAnnotation( org.pentaho.di.core.annotations.Step.class ),
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
    handleAsSecondRow();
    doAnswer( invocation -> {
      String topic = (String) invocation.getArguments()[ 0 ];
      MqttMessage message = (MqttMessage) invocation.getArguments()[ 1 ];

      assertEquals( "TestWinning", topic );
      assertEquals( 0, message.getQos() );
      assertEquals( "#winning", new String( message.getPayload() ) );
      return null;
    } ).when( mqttClient ).publish( any(), any() );

    trans.startThreads();
    trans.waitUntilFinished();

    verify( mqttClient ).disconnect();
    assertEquals( 4, trans.getSteps().get( 1 ).step.getLinesOutput() );
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

    verify( logChannel ).logError( eq( "Unexpected error" ), any( KettleStepException.class ) );
    verify( mqttClient, never() ).publish( any(), any() );
  }

  @Test
  public void testProcessFirstRow() throws Exception {
    PowerMockito.mockStatic( MQTTClientBuilder.class );
    MQTTClientBuilder clientBuilder = spy( MQTTClientBuilder.class );
    MqttClient mqttClient = mock( MqttClient.class );
    doReturn( mqttClient ).when( clientBuilder ).buildAndConnect();
    PowerMockito.when( MQTTClientBuilder.builder() ).thenReturn( clientBuilder );

    trans.startThreads();
    trans.waitUntilFinished();

    StepMetaDataCombi combi = trans.getSteps().get( 1 );
    MQTTProducer step = (MQTTProducer) combi.step;

    assertFalse( step.first );
  }

  @Test
  public void testFeedbackSize() throws Exception {
    handleAsSecondRow();

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

    verify( logChannel ).logError( "There was an error connecting" );
  }

  @Test
  public void testErrorOnPublishStopsAll() throws Exception {
    handleAsSecondRow();

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

  private void handleAsSecondRow() {
    StepMetaDataCombi combi = trans.getSteps().get( 1 );
    MQTTProducer step = (MQTTProducer) combi.step;
    MQTTProducerData data = (MQTTProducerData) combi.data;
    data.mqttClient = mqttClient;
    step.first = false;
  }
}
