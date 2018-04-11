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

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
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
  }

  @Test
  public void testLoadAndSave() {
    MQTTProducerMeta fromMeta = testMeta();
    MQTTProducerMeta toMeta = fromXml( fromMeta.getXML() );

    assertEquals( "mqtthost:1883", toMeta.getMqttServer() );
    assertEquals( "client1", toMeta.getClientId() );
    assertEquals( "test-topic", toMeta.getTopic() );
    assertEquals( "1", toMeta.getQOS() );
    assertEquals( "tempvalue", toMeta.getMessageField() );
    assertEquals( "testuser", toMeta.getUsername() );
    assertEquals( "test", toMeta.getPassword() );
    assertEquals( "1000", toMeta.getKeepAliveInterval() );
    assertEquals( "2000", toMeta.getMaxInflight() );
    assertEquals( "3000", toMeta.getConnectionTimeout() );
    assertEquals( "true", toMeta.getCleanSession() );
    assertEquals( "/Users/noname/temp", toMeta.getStorageLevel() );
    assertEquals( "mqttHost2:1883", toMeta.getServerUris() );
    assertEquals( "3", toMeta.getMqttVersion() );
    assertEquals( "true", toMeta.getAutomaticReconnect() );

    assertThat( toMeta, equalTo( fromMeta ) );
  }


  @Test
  public void testFieldsArePreserved() {
    MQTTProducerMeta meta = new MQTTProducerMeta();
    meta.setMqttServer( "mqtthost:1883" );
    meta.setClientId( "client1" );
    meta.setTopic( "test-topic" );
    meta.setQOS( "2" );
    meta.setMessageField( "temp-message" );
    meta.setUsername( "testuser" );
    meta.setPassword( "test" );
    MQTTProducerMeta toMeta = fromXml( meta.getXML() );

    assertThat( toMeta, equalTo( meta ) );
  }

  @Test
  public void testRoundTripWithSSLStuff() {
    MQTTProducerMeta meta = new MQTTProducerMeta();
    meta.setMqttServer( "mqtthost:1883" );
    meta.setTopic( "test-topic" );
    meta.setQOS( "2" );
    meta.setMessageField( "temp-message" );
    meta.setSslConfig( of(
      "sslKey", "sslVal",
      "sslKey2", "sslVal2",
      "sslKey3", "sslVal3"
    ) );
    meta.setUseSsl( true );

    MQTTProducerMeta rehydrated = fromXml( meta.getXML() );

    assertThat( true, is( rehydrated.isUseSsl() ) );
    meta.getSslConfig().keySet().forEach( key ->
      assertThat( meta.getSslConfig().get( key ), is( rehydrated.getSslConfig().get( key ) ) ) );

  }

  @Test
  public void testReadFromRepository() throws Exception {
    MQTTProducerMeta testMeta = testMeta();
    testMeta.setAutomaticReconnect( "true" );
    testMeta.setServerUris( "mqttHost2:1883" );
    testMeta.setMqttServer( "mqttserver:1883" );
    StringObjectId stepId = new StringObjectId( "stepId" );

    String xml = testMeta.getXML();
    when( rep.getStepAttributeString( stepId, "step-xml" ) ).thenReturn( xml );

    MQTTProducerMeta meta = new MQTTProducerMeta();
    meta.readRep( rep, metaStore, stepId, emptyList() );

    meta.readRep( rep, metaStore, stepId, Collections.emptyList() );
    assertEquals( "mqttserver:1883", meta.getMqttServer() );
    assertEquals( "client1", meta.getClientId() );
    assertEquals( "test-topic", meta.getTopic() );
    assertEquals( "1", meta.getQOS() );
    assertEquals( "tempvalue", meta.getMessageField() );
    assertEquals( "testuser", meta.getUsername() );
    assertEquals( "test", meta.getPassword() );
    assertEquals( "1000", meta.getKeepAliveInterval() );
    assertEquals( "2000", meta.getMaxInflight() );
    assertEquals( "3000", meta.getConnectionTimeout() );
    assertEquals( "true", meta.getCleanSession() );
    assertEquals( "/Users/noname/temp", meta.getStorageLevel() );
    assertEquals( "mqttHost2:1883", meta.getServerUris() );
    assertEquals( "3", meta.getMqttVersion() );
    assertEquals( "true", meta.getAutomaticReconnect() );
  }

  @Test
  public void testSavingToRepository() throws Exception {

    StringObjectId stepId = new StringObjectId( "step1" );
    StringObjectId transId = new StringObjectId( "trans1" );

    MQTTProducerMeta localMeta = testMeta();
    localMeta.setTopic( "weather" );

    localMeta.saveRep( rep, metaStore, transId, stepId );

    verify( rep ).saveStepAttribute( transId, stepId, "step-xml", localMeta.getXML() );
    verifyNoMoreInteractions( rep );
  }

  @Test
  public void testSaveDefaultEmpty() throws KettleException {
    MQTTProducerMeta defaultMeta = new MQTTProducerMeta();
    defaultMeta.setDefault();

    MQTTProducerMeta toMeta = new MQTTProducerMeta();
    toMeta.setMqttServer( "something that's not default" );

    // loadXML into toMeta should overwrite the non-default val.
    toMeta.loadXML( getNode( defaultMeta.getXML() ), emptyList(), metaStore );
    assertEquals( toMeta, defaultMeta );
    assertThat( toMeta.getMqttServer(), is( "" ) );
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
    meta.setMqttServer( "theserver:1883" );
    meta.setClientId( "client100" );
    meta.setTopic( "newtopic" );
    meta.setQOS( "2" );
    meta.setMessageField( "Messages" );
    meta.setUsername( "testuser" );
    meta.setPassword( "test" );
    meta.setKeepAliveInterval( "1000" );
    meta.setMaxInflight( "2000" );
    meta.setConnectionTimeout( "3000" );
    meta.setCleanSession( "true" );
    meta.setStorageLevel( "/Users/noname/temp" );
    meta.setServerUris( "mqttHost2:1883" );
    meta.setMqttVersion( "3" );
    meta.setAutomaticReconnect( "true" );
    meta.check( remarks, null, null, null, null, null, null, new Variables(), null, null );

    assertEquals( 0, remarks.size() );
  }

  @Test
  public void testCheckOptionsFail() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    MQTTProducerMeta meta = new MQTTProducerMeta();
    meta.setMqttServer( "theserver:1883" );
    meta.setClientId( "client100" );
    meta.setTopic( "newtopic" );
    meta.setQOS( "2" );
    meta.setMessageField( "Messages" );
    meta.setUsername( "testuser" );
    meta.setKeepAliveInterval( "asdf" );
    meta.setMaxInflight( "asdf" );
    meta.setConnectionTimeout( "asdf" );
    meta.setCleanSession( "asdf" );
    meta.setAutomaticReconnect( "adsf" );
    meta.setMqttVersion( "asdf" );
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
    mqttProducerMeta.setMqttServer( "${server}" );
    mqttProducerMeta.setMessageField( "${message}" );
    mqttProducerMeta.setTopic( "${topic}" );
    mqttProducerMeta.setSslConfig( of( "key1", "${val1}", "key2", "${val2}" ) );

    VariableSpace variables = new Variables();
    variables.setVariable( "server", "myserver" );
    variables.setVariable( "message", "mymessage" );
    variables.setVariable( "topic", "mytopic" );
    variables.setVariable( "val1", "sslVal1" );
    variables.setVariable( "val2", "sslVal2" );

    MQTTProducerMeta substitutedMeta = (MQTTProducerMeta) mqttProducerMeta.withVariables( variables );

    assertThat( "myserver", equalTo( substitutedMeta.getMqttServer() ) );
    assertThat( "mymessage", equalTo( substitutedMeta.getMessageField() ) );
    assertThat( "mytopic", equalTo( substitutedMeta.getTopic() ) );
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
    meta.setMqttServer( "mqtthost:1883" );
    meta.setClientId( "client1" );
    meta.setTopic( "test-topic" );
    meta.setQOS( "1" );
    meta.setMessageField( "tempvalue" );
    meta.setUsername( "testuser" );
    meta.setPassword( "test" );
    meta.setKeepAliveInterval( "1000" );
    meta.setMaxInflight( "2000" );
    meta.setConnectionTimeout( "3000" );
    meta.setCleanSession( "true" );
    meta.setStorageLevel( "/Users/noname/temp" );
    meta.setServerUris( "mqttHost2:1883" );
    meta.setMqttVersion( "3" );
    meta.setAutomaticReconnect( "true" );
    return meta;
  }

}
