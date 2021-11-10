/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.EnvUtil;
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
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
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
public class MQTTConsumerMetaTest {
  private static Class PKG = MQTTConsumerMetaTest.class;

  @Mock private IMetaStore metastore;
  @Mock private Repository rep;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init( true );
    String passwordEncoderPluginID =
      Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );
  }

  private MQTTConsumerMeta meta = new MQTTConsumerMeta();

  @Test
  public void testLoadAndSave() throws KettleXMLException {
    MQTTConsumerMeta startingMeta = getTestMeta();
    metaMatchesTestMetaFields( fromXml( startingMeta.getXML() ) );
  }


  @Test
  public void testXmlHasAllFields() {
    String serverName = "some_cluster";
    meta.setDefault();
    meta.setMqttServer( serverName );

    ArrayList<String> topicList = new ArrayList<>();
    topicList.add( "temperature" );
    meta.setTopics( topicList );
    meta.setQos( "1" );
    meta.setUsername( "testuser" );
    meta.setPassword( "test" );

    meta.setUseSsl( true );
    meta.setTransformationPath( "/home/pentaho/myKafkaTransformation.ktr" );
    meta.setBatchSize( "54321" );
    meta.setBatchDuration( "987" );

    StepMeta stepMeta = new StepMeta();
    TransMeta transMeta = mock( TransMeta.class );
    stepMeta.setParentTransMeta( transMeta );
    meta.setParentStepMeta( stepMeta );

    // tests serialization/deserialization round trip
    assertTrue( meta.equals( fromXml( meta.getXML() ) ) );
  }

  @Test
  public void testReadsFromRepository() throws Exception {
    StringObjectId stepId = new StringObjectId( "stepId" );

    MQTTConsumerMeta testMeta = getTestMeta();

    String xml = testMeta.getXML();
    when( rep.getStepAttributeString( stepId, "step-xml" ) ).thenReturn( xml );

    meta.readRep( rep, metastore, stepId, emptyList() );

    metaMatchesTestMetaFields( meta );


  }

  @Test
  public void testSavesToRepository() throws Exception {
    StringObjectId stepId = new StringObjectId( "step1" );
    StringObjectId transId = new StringObjectId( "trans1" );
    ArrayList<String> topicList = new ArrayList<>();
    topicList.add( "temperature" );
    MQTTConsumerMeta localMeta = getTestMeta2( topicList );

    localMeta.saveRep( rep, metastore, transId, stepId );

    verify( rep ).saveStepAttribute( transId, stepId, "step-xml", localMeta.getXML() );
    verifyNoMoreInteractions( rep );
  }

  private MQTTConsumerMeta getTestMeta2( ArrayList<String> topicList ) {
    MQTTConsumerMeta localMeta = new MQTTConsumerMeta();
    localMeta.setTopics( topicList );
    localMeta.setQos( "1" );
    localMeta.setTransformationPath( "/home/Pentaho/btrans.ktr" );
    localMeta.setBatchSize( "33" );
    localMeta.setBatchDuration( "10000" );
    localMeta.setMqttServer( "mqttServer:1883" );
    localMeta.setUsername( "testuser" );
    localMeta.setPassword( "test" );
    localMeta.setUseSsl( true );
    localMeta.setSslConfig( ImmutableMap.of( "key1", "val1",
      "key2", "val2",
      "password", "foobarbaz" ) );
    localMeta.setKeepAliveInterval( "1000" );
    localMeta.setMaxInflight( "2000" );
    localMeta.setConnectionTimeout( "3000" );
    localMeta.setCleanSession( "true" );
    localMeta.setStorageLevel( "/Users/noname/temp" );
    localMeta.setServerUris( "mqttHost2:1883" );
    localMeta.setMqttVersion( "3" );
    localMeta.setAutomaticReconnect( "true" );
    return localMeta;
  }

  @Test
  public void testSaveDefaultEmptyConnection() {
    MQTTConsumerMeta roundTrippedMeta = fromXml( meta.getXML() );
    assertThat( roundTrippedMeta, equalTo( meta ) );
    assertTrue( roundTrippedMeta.getMqttServer().isEmpty() );
  }

  @Test
  public void testGetSslConfig() {
    meta.setDefault();

    meta.setTopics( Collections.singletonList( "mytopic" ) );
    Map<String, String> config = meta.getSslConfig();
    assertTrue( config.size() > 0 );
    assertThat( meta.sslKeys.size(), equalTo( config.size() ) );
    assertTrue( config.keySet().containsAll( meta.sslKeys ) );
    for ( int i = 0; i < meta.sslKeys.size(); i++ ) {
      assertThat( config.get( meta.sslKeys.get( i ) ), equalTo( meta.sslValues.get( i ) ) );
    }
    MQTTConsumerMeta roundTrip = fromXml( meta.getXML() );

    assertTrue( meta.equals( roundTrip ) );
  }

  @Test
  public void testSetSslConfig() {
    meta.setDefault();
    meta.setTopics( asList( "foo", "bar", "bop" ) );
    Map<String, String> fakeConfig = ImmutableMap.of(
      "key1", "value1",
      "key2", "value2",
      "key3", "value3",
      "key4", "value4"
    );
    meta.setSslConfig( fakeConfig );

    MQTTConsumerMeta deserMeta = fromXml( meta.getXML() );
    assertThat( fakeConfig, equalTo( deserMeta.getSslConfig() ) );
    assertTrue( meta.equals( deserMeta ) );


  }

  @Test
  public void testRetrieveOptions() {
    List<String> keys = Arrays
      .asList( KEEP_ALIVE_INTERVAL, MAX_INFLIGHT, CONNECTION_TIMEOUT, CLEAN_SESSION, STORAGE_LEVEL, SERVER_URIS,
        MQTT_VERSION, AUTOMATIC_RECONNECT );

    MQTTConsumerMeta meta = new MQTTConsumerMeta();
    meta.setDefault();
    List<StepOption> options = meta.retrieveOptions();
    assertEquals( 8, options.size() );
    for ( StepOption option : options ) {
      assertEquals( "", option.getValue() );
      assertNotNull( option.getText() );
      Assert.assertTrue( keys.contains( option.getKey() ) );
    }
  }

  @Test
  public void testCheckDefaults() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    meta.check( remarks, null, null, null, null, null, null, new Variables(), null, null );

    assertEquals( 0, remarks.size() );
  }

  @Test
  public void testCheckFailAll() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    meta.setKeepAliveInterval( "asdf" );
    meta.setMaxInflight( "asdf" );
    meta.setConnectionTimeout( "asdf" );
    meta.setCleanSession( "asdf" );
    meta.setAutomaticReconnect( "adsf" );
    meta.setMqttVersion( "9" );
    meta.check( remarks, null, null, null, null, null, null, new Variables(), null, null );

    assertEquals( 6, remarks.size() );
    assertEquals( BaseMessages
        .getString( PKG, "MQTTMeta.CheckResult.NotANumber",
          BaseMessages.getString( PKG, "MQTTDialog.Options." + KEEP_ALIVE_INTERVAL ) ),
      remarks.get( 0 ).getText() );
    assertEquals( BaseMessages
        .getString( PKG, "MQTTMeta.CheckResult.NotANumber",
          BaseMessages.getString( PKG, "MQTTDialog.Options." + MAX_INFLIGHT ) ),
      remarks.get( 1 ).getText() );
    assertEquals( BaseMessages
        .getString( PKG, "MQTTMeta.CheckResult.NotANumber",
          BaseMessages.getString( PKG, "MQTTDialog.Options." + CONNECTION_TIMEOUT ) ),
      remarks.get( 2 ).getText() );
    assertEquals( BaseMessages
        .getString( PKG, "MQTTMeta.CheckResult.NotABoolean",
          BaseMessages.getString( PKG, "MQTTDialog.Options." + CLEAN_SESSION ) ),
      remarks.get( 3 ).getText() );
    assertEquals( BaseMessages
        .getString( PKG, "MQTTMeta.CheckResult.NotCorrectVersion",
          BaseMessages.getString( PKG, "MQTTDialog.Options." + MQTT_VERSION ) ),
      remarks.get( 4 ).getText() );
    assertEquals(
      BaseMessages
        .getString( PKG, "MQTTMeta.CheckResult.NotABoolean",
          BaseMessages.getString( PKG, "MQTTDialog.Options." + AUTOMATIC_RECONNECT ) ),
      remarks.get( 5 ).getText() );
  }

  @Test
  public void rowMetaUsesMessageDataType() {
    MQTTConsumerMeta meta = new MQTTConsumerMeta();
    meta.messageDataType = ValueMetaInterface.getTypeDescription( ValueMetaInterface.TYPE_BINARY );
    assertEquals( ValueMetaInterface.TYPE_BINARY, meta.getRowMeta( "", new Variables() ).getValueMeta( 0 ).getType() );
    meta.messageDataType = ValueMetaInterface.getTypeDescription( ValueMetaInterface.TYPE_STRING );
    assertEquals( ValueMetaInterface.TYPE_STRING, meta.getRowMeta( "", new Variables() ).getValueMeta( 0 ).getType() );
  }

  private void metaMatchesTestMetaFields( MQTTConsumerMeta consumerMeta ) {
    assertEquals( "one", consumerMeta.getTopics().get( 0 ) );
    assertEquals( "two", consumerMeta.getTopics().get( 1 ) );
    assertEquals( "1", consumerMeta.getQos() );
    assertEquals( "${Internal.Entry.Current.Directory}/write-to-log.ktr", consumerMeta.getTransformationPath() );
    assertEquals( "${Internal.Entry.Current.Directory}/write-to-log.ktr", consumerMeta.getFileName() );
    assertEquals( "5", consumerMeta.getBatchSize() );
    assertEquals( "60000", consumerMeta.getBatchDuration() );
    assertEquals( "mqttHost:1883", consumerMeta.getMqttServer() );
    assertEquals( "testuser", consumerMeta.getUsername() );
    assertEquals( "test", consumerMeta.getPassword() );
    assertEquals( "1000", consumerMeta.getKeepAliveInterval() );
    assertEquals( "2000", consumerMeta.getMaxInflight() );
    assertEquals( "3000", consumerMeta.getConnectionTimeout() );
    assertEquals( "true", consumerMeta.getCleanSession() );
    assertEquals( "/Users/noname/temp", consumerMeta.getStorageLevel() );
    assertEquals( "mqttHost2:1883", consumerMeta.getServerUris() );
    assertEquals( "3", consumerMeta.getMqttVersion() );
    assertEquals( "true", consumerMeta.getAutomaticReconnect() );
  }

  private MQTTConsumerMeta getTestMeta() {
    MQTTConsumerMeta startingMeta = new MQTTConsumerMeta();
    startingMeta.setTopics( asList( "one", "two" ) );
    startingMeta.setQos( "1" );
    startingMeta.setTransformationPath( "${Internal.Entry.Current.Directory}/write-to-log.ktr" );
    startingMeta.setFileName( "${Internal.Entry.Current.Directory}/write-to-log.ktr" );
    startingMeta.setBatchSize( "5" );
    startingMeta.setBatchDuration( "60000" );
    startingMeta.setMqttServer( "mqttHost:1883" );
    startingMeta.setUsername( "testuser" );
    startingMeta.setPassword( "test" );
    startingMeta.setKeepAliveInterval( "1000" );
    startingMeta.setMaxInflight( "2000" );
    startingMeta.setConnectionTimeout( "3000" );
    startingMeta.setCleanSession( "true" );
    startingMeta.setStorageLevel( "/Users/noname/temp" );
    startingMeta.setServerUris( "mqttHost2:1883" );
    startingMeta.setMqttVersion( "3" );
    startingMeta.setAutomaticReconnect( "true" );
    return startingMeta;
  }


  public static MQTTConsumerMeta fromXml( String metaXml ) {
    Document doc;
    try {
      doc = XMLHandler.loadXMLString( "<step>" + metaXml + "</step>" );
      Node stepNode = XMLHandler.getSubNode( doc, "step" );
      MQTTConsumerMeta mqttConsumerMeta = new MQTTConsumerMeta();
      mqttConsumerMeta.loadXML( stepNode, emptyList(), (IMetaStore) null );
      return mqttConsumerMeta;
    } catch ( KettleXMLException e ) {
      throw new RuntimeException( e );
    }
  }
}
