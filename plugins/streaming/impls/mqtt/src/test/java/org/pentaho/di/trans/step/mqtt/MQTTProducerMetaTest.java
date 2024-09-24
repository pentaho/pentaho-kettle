/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2021 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepOption;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.AUTOMATIC_RECONNECT;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.CLEAN_SESSION;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.CONNECTION_TIMEOUT;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.KEEP_ALIVE_INTERVAL;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.MAX_INFLIGHT;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.MQTT_VERSION;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.SERVER_URIS;
import static org.pentaho.di.trans.step.mqtt.MQTTConstants.STORAGE_LEVEL;

@RunWith ( MockitoJUnitRunner.class )
public class MQTTProducerMetaTest {
  private static Class PKG = MQTTProducerMetaTest.class;

  @Mock private IMetaStore metaStore;
  @Mock private Repository rep;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init();
    StepPluginType.getInstance().handlePluginAnnotation(
      MQTTProducerMeta.class,
      MQTTProducerMeta.class.getAnnotation( org.pentaho.di.core.annotations.Step.class ),
      Collections.emptyList(), false, null );
  }

  @Test
  public void testLoadAndSave() {
    MQTTProducerMeta fromMeta = testMeta();
    MQTTProducerMeta toMeta = fromXml( fromMeta.getXML() );

    assertEquals( "mqtthost:1883", toMeta.mqttServer );
    assertEquals( "client1", toMeta.clientId );
    assertEquals( "test-topic", toMeta.topic );
    assertEquals( "field-topic", toMeta.fieldTopic );
    assertEquals( "1", toMeta.qos );
    assertEquals( "tempvalue", toMeta.messageField );
    assertEquals( "testuser", toMeta.username );
    assertEquals( "test", toMeta.password );
    assertEquals( "1000", toMeta.keepAliveInterval );
    assertEquals( "2000", toMeta.maxInflight );
    assertEquals( "3000", toMeta.connectionTimeout );
    assertEquals( "true", toMeta.cleanSession );
    assertEquals( "/Users/noname/temp", toMeta.storageLevel );
    assertEquals( "mqttHost2:1883", toMeta.serverUris );
    assertEquals( "3", toMeta.mqttVersion );
    assertEquals( "true", toMeta.automaticReconnect );

    assertThat( toMeta, equalTo( fromMeta ) );
  }


  @Test
  public void testFieldsArePreserved() {
    MQTTProducerMeta meta = new MQTTProducerMeta();
    meta.mqttServer = "mqtthost:1883";
    meta.clientId = "client1";
    meta.topic = "test-topic";
    meta.fieldTopic = "field-topic";
    meta.qos = "2";
    meta.messageField = "temp-message";
    meta.username = "testuser";
    meta.password = "test";
    MQTTProducerMeta toMeta = fromXml( meta.getXML() );

    assertThat( toMeta, equalTo( meta ) );
  }

  @Test
  public void testRoundTripWithSSLStuff() {
    MQTTProducerMeta meta = new MQTTProducerMeta();
    meta.mqttServer = "mqtthost:1883";
    meta.topic = "test-topic";
    meta.qos = "2";
    meta.messageField = "temp-message";
    meta.setSslConfig( of(
      "sslKey", "sslVal",
      "sslKey2", "sslVal2",
      "sslKey3", "sslVal3"
    ) );
    meta.useSsl = true;

    MQTTProducerMeta rehydrated = fromXml( meta.getXML() );

    assertThat( true, is( rehydrated.useSsl ) );
    meta.getSslConfig().keySet().forEach( key ->
      assertThat( meta.getSslConfig().get( key ), is( rehydrated.getSslConfig().get( key ) ) ) );

  }

  @Test
  public void testReadFromRepository() throws Exception {
    MQTTProducerMeta testMeta = testMeta();
    testMeta.automaticReconnect = "true";
    testMeta.serverUris = "mqttHost2:1883";
    testMeta.mqttServer = "mqttserver:1883";
    StringObjectId stepId = new StringObjectId( "stepId" );

    String xml = testMeta.getXML();
    when( rep.getStepAttributeString( stepId, "step-xml" ) ).thenReturn( xml );

    MQTTProducerMeta meta = new MQTTProducerMeta();
    meta.readRep( rep, metaStore, stepId, emptyList() );

    meta.readRep( rep, metaStore, stepId, Collections.emptyList() );
    assertEquals( "mqttserver:1883", meta.mqttServer );
    assertEquals( "client1", meta.clientId );
    assertEquals( "test-topic", meta.topic );
    assertEquals( "1", meta.qos );
    assertEquals( "tempvalue", meta.messageField );
    assertEquals( "testuser", meta.username );
    assertEquals( "test", meta.password );
    assertEquals( "1000", meta.keepAliveInterval );
    assertEquals( "2000", meta.maxInflight );
    assertEquals( "3000", meta.connectionTimeout );
    assertEquals( "true", meta.cleanSession );
    assertEquals( "/Users/noname/temp", meta.storageLevel );
    assertEquals( "mqttHost2:1883", meta.serverUris );
    assertEquals( "3", meta.mqttVersion );
    assertEquals( "true", meta.automaticReconnect );
  }

  @Test
  public void testSavingToRepository() throws Exception {

    StringObjectId stepId = new StringObjectId( "step1" );
    StringObjectId transId = new StringObjectId( "trans1" );

    MQTTProducerMeta localMeta = testMeta();
    localMeta.topic = "weather";

    localMeta.saveRep( rep, metaStore, transId, stepId );

    verify( rep ).saveStepAttribute( transId, stepId, "step-xml", localMeta.getXML() );
    verifyNoMoreInteractions( rep );
  }

  @Test
  public void testSaveDefaultEmpty() throws KettleException {
    MQTTProducerMeta defaultMeta = new MQTTProducerMeta();
    defaultMeta.setDefault();

    MQTTProducerMeta toMeta = new MQTTProducerMeta();
    toMeta.mqttServer = "something that's not default";

    // loadXML into toMeta should overwrite the non-default val.
    toMeta.loadXML( getNode( defaultMeta.getXML() ), emptyList(), metaStore );
    assertEquals( toMeta, defaultMeta );
    assertThat( toMeta.mqttServer, is( "" ) );
  }

  @Test
  public void testRetrieveOptions() {
    List<String> keys = Arrays
      .asList( KEEP_ALIVE_INTERVAL, MAX_INFLIGHT, CONNECTION_TIMEOUT, CLEAN_SESSION, STORAGE_LEVEL, SERVER_URIS,
        MQTT_VERSION, AUTOMATIC_RECONNECT );

    MQTTProducerMeta meta = new MQTTProducerMeta();
    meta.setDefault();
    List<StepOption> options = meta.retrieveOptions();
    assertEquals( 8, options.size() );
    for ( StepOption option : options ) {
      assertEquals( "", option.getValue() );
      assertNotNull( option.getText() );
      assertTrue( keys.contains( option.getKey() ) );
    }
  }

  @Test
  public void testCheckOptions() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    MQTTProducerMeta meta = new MQTTProducerMeta();
    meta.mqttServer = "theserver:1883";
    meta.clientId = "client100";
    meta.topic = "newtopic";
    meta.qos = "2";
    meta.messageField = "Messages";
    meta.username = "testuser";
    meta.password = "test";
    meta.keepAliveInterval = "1000";
    meta.maxInflight = "2000";
    meta.connectionTimeout = "3000";
    meta.cleanSession = "true";
    meta.storageLevel = "/Users/noname/temp";
    meta.serverUris = "mqttHost2:1883";
    meta.mqttVersion = "3";
    meta.automaticReconnect = "true";
    meta.check( remarks, null, null, null, null, null, null, new Variables(), null, null );

    assertEquals( 0, remarks.size() );
  }

  @Test
  public void testCheckOptionsFail() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    MQTTProducerMeta meta = new MQTTProducerMeta();
    meta.mqttServer = "theserver:1883";
    meta.clientId = "client100";
    meta.topic = "newtopic";
    meta.qos = "2";
    meta.messageField = "Messages";
    meta.username = "testuser";
    meta.keepAliveInterval = "asdf";
    meta.maxInflight = "asdf";
    meta.connectionTimeout = "asdf";
    meta.cleanSession = "asdf";
    meta.automaticReconnect = "adsf";
    meta.mqttVersion = "asdf";
    meta.check( remarks, null, null, null, null, null, null, new Variables(), null, null );

    assertEquals( 6, remarks.size() );
    assertTrue( remarks.get( 0 ).getText()
      .contains( BaseMessages.getString( PKG, "MQTTDialog.Options." + KEEP_ALIVE_INTERVAL ) ) );
    assertTrue(
      remarks.get( 1 ).getText().contains( BaseMessages.getString( PKG, "MQTTDialog.Options." + MAX_INFLIGHT ) ) );
    assertTrue( remarks.get( 2 ).getText()
      .contains( BaseMessages.getString( PKG, "MQTTDialog.Options." + CONNECTION_TIMEOUT ) ) );
    assertTrue(
      remarks.get( 3 ).getText().contains( BaseMessages.getString( PKG, "MQTTDialog.Options." + CLEAN_SESSION ) ) );
    assertTrue(
      remarks.get( 4 ).getText().contains( BaseMessages.getString( PKG, "MQTTDialog.Options." + MQTT_VERSION ) ) );
    assertTrue( remarks.get( 5 ).getText()
      .contains( BaseMessages.getString( PKG, "MQTTDialog.Options." + AUTOMATIC_RECONNECT ) ) );
  }

  @Test
  public void testVarSubstitution() {
    MQTTProducerMeta mqttProducerMeta = new MQTTProducerMeta();
    mqttProducerMeta.mqttServer = "${server}";
    mqttProducerMeta.messageField = "${message}";
    mqttProducerMeta.topic = "${topic}";
    mqttProducerMeta.fieldTopic = "${fieldTopic}";
    mqttProducerMeta.setSslConfig( of( "key1", "${val1}", "key2", "${val2}" ) );
    StepMeta stepMeta = new StepMeta( "mqtt step", mqttProducerMeta );
    stepMeta.setParentTransMeta( new TransMeta( new Variables() ) );
    mqttProducerMeta.setParentStepMeta( stepMeta );

    VariableSpace variables = new Variables();
    variables.setVariable( "server", "myserver" );
    variables.setVariable( "message", "mymessage" );
    variables.setVariable( "topic", "mytopic" );
    variables.setVariable( "fieldTopic", "myfieldtopic" );
    variables.setVariable( "val1", "sslVal1" );
    variables.setVariable( "val2", "sslVal2" );

    MQTTProducerMeta substitutedMeta = (MQTTProducerMeta) mqttProducerMeta.withVariables( variables );

    assertThat( "myserver", equalTo( substitutedMeta.mqttServer ) );
    assertThat( "mymessage", equalTo( substitutedMeta.messageField ) );
    assertThat( "mytopic", equalTo( substitutedMeta.topic ) );
    assertThat( "myfieldtopic", equalTo( substitutedMeta.fieldTopic ) );
    assertThat( "sslVal1", equalTo( substitutedMeta.getSslConfig().get( "key1" ) ) );
    assertThat( "sslVal2", equalTo( substitutedMeta.getSslConfig().get( "key2" ) ) );
  }

  public static MQTTProducerMeta fromXml( String metaXml ) {
    try {
      Node stepNode = getNode( metaXml );
      MQTTProducerMeta mqttProducerMeta = new MQTTProducerMeta();
      mqttProducerMeta.loadXML( stepNode, Collections.emptyList(), (IMetaStore) null );
      return mqttProducerMeta;
    } catch ( KettleXMLException e ) {
      throw new RuntimeException( e );
    }
  }

  private static Node getNode( String metaXml ) throws KettleXMLException {
    Document doc;
    doc = XMLHandler.loadXMLString( "<step>" + metaXml + "</step>" );
    return XMLHandler.getSubNode( doc, "step" );
  }

  private MQTTProducerMeta testMeta() {
    MQTTProducerMeta meta = new MQTTProducerMeta();
    meta.mqttServer = "mqtthost:1883";
    meta.clientId = "client1";
    meta.topic = "test-topic";
    meta.fieldTopic = "field-topic";
    meta.qos = "1";
    meta.messageField = "tempvalue";
    meta.username = "testuser";
    meta.password = "test";
    meta.keepAliveInterval = "1000";
    meta.maxInflight = "2000";
    meta.connectionTimeout = "3000";
    meta.cleanSession = "true";
    meta.storageLevel = "/Users/noname/temp";
    meta.serverUris = "mqttHost2:1883";
    meta.mqttVersion = "3";
    meta.automaticReconnect = "true";
    return meta;
  }

}
