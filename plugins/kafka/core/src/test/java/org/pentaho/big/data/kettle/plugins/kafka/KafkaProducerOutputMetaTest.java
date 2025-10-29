/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.big.data.kettle.plugins.kafka;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.attributes.metastore.EmbeddedMetaStore;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.named.cluster.NamedClusterEmbedManager;
import org.pentaho.hadoop.shim.api.cluster.NamedClusterService;
import org.pentaho.hadoop.shim.api.cluster.NamedCluster;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.locator.api.MetastoreLocator;
import org.w3c.dom.Node;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaProducerOutputMeta.ADVANCED_CONFIG;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaProducerOutputMeta.CLIENT_ID;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaProducerOutputMeta.CLUSTER_NAME;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaProducerOutputMeta.CONNECTION_TYPE;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaProducerOutputMeta.ConnectionType.CLUSTER;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaProducerOutputMeta.ConnectionType.DIRECT;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaProducerOutputMeta.DIRECT_BOOTSTRAP_SERVERS;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaProducerOutputMeta.KEY_FIELD;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaProducerOutputMeta.MESSAGE_FIELD;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaProducerOutputMeta.TOPIC;

@RunWith( MockitoJUnitRunner.class )
public class KafkaProducerOutputMetaTest {
  @Mock IMetaStore metastore;
  @Mock Repository rep;
  @Mock NamedClusterService namedClusterService;
  @Mock MetastoreLocator metastoreLocator;

  @Before
  public void setup() {
    KettleLogStore.init();
  }

  @Test
  public void testLoadsFieldsFromXml() throws Exception {
    KafkaProducerOutputMeta meta = new KafkaProducerOutputMeta();
    String inputXml =
      "  <step>\n"
        + "    <name>Kafka Producer</name>\n"
        + "    <type>KafkaProducerOutput</type>\n"
        + "    <description />\n"
        + "    <distribute>Y</distribute>\n"
        + "    <custom_distribution />\n"
        + "    <copies>1</copies>\n"
        + "    <partitioning>\n"
        + "      <method>none</method>\n"
        + "      <schema_name />\n"
        + "    </partitioning>\n"
        + "    <connectionType>DIRECT</connectionType>\n"
        + "    <directBootstrapServers>localhost:9092</directBootstrapServers>\n"
        + "    <clusterName>some_cluster</clusterName>\n"
        + "    <clientId>clientid01</clientId>\n"
        + "    <topic>one</topic>\n"
        + "    <keyField>three</keyField>\n"
        + "    <messageField>four</messageField>\n"
        + "    <advancedConfig>\n"
        + "        <option property=\"advanced.property1\" value=\"advancedPropertyValue1\" />\n"
        + "        <option property=\"advanced.property2\" value=\"advancedPropertyValue2\" />\n"
        + "    </advancedConfig>\n"
        + "    <cluster_schema />\n"
        + "    <remotesteps>\n"
        + "      <input>\n"
        + "      </input>\n"
        + "      <output>\n"
        + "      </output>\n"
        + "    </remotesteps>\n"
        + "    <GUI>\n"
        + "      <xloc>208</xloc>\n"
        + "      <yloc>80</yloc>\n"
        + "      <draw>Y</draw>\n"
        + "    </GUI>\n"
        + "  </step>\n";
    Node node = XMLHandler.loadXMLString( inputXml ).getFirstChild();
    meta.loadXML( node, Collections.emptyList(), metastore );
    assertEquals( "some_cluster", meta.getClusterName() );
    assertEquals( "clientid01", meta.getClientId() );
    assertEquals( "one", meta.getTopic() );
    assertEquals( "three", meta.getKeyField() );
    assertEquals( "four", meta.getMessageField() );

    assertEquals( 2, meta.getConfig().size() );
    assertTrue( meta.getConfig().containsKey( "advanced.property1" ) );
    assertEquals( "advancedPropertyValue1", meta.getConfig().get( "advanced.property1" ) );
    assertTrue( meta.getConfig().containsKey( "advanced.property2" ) );
    assertEquals( "advancedPropertyValue2", meta.getConfig().get( "advanced.property2" ) );
  }

  @Test
  public void testXmlHasAllFields() throws Exception {
    KafkaProducerOutputMeta meta = new KafkaProducerOutputMeta();
    meta.setConnectionType( DIRECT );
    meta.setDirectBootstrapServers( "localhost:9092" );
    meta.setClusterName( "some_cluster" );
    meta.setClientId( "id1" );
    meta.setTopic( "myTopic" );
    meta.setKeyField( "fieldOne" );
    meta.setMessageField( "message" );

    Map<String, String> advancedConfig = new LinkedHashMap<>();
    advancedConfig.put( "advanced.property1", "advancedPropertyValue1" );
    advancedConfig.put( "advanced.property2", "advancedPropertyValue2" );
    meta.setConfig( advancedConfig );

    NamedClusterEmbedManager namedClusterEmbedManager = mock( NamedClusterEmbedManager.class );
    TransMeta transMeta = mock( TransMeta.class );
    when( transMeta.getNamedClusterEmbedManager() ).thenReturn( namedClusterEmbedManager );
    StepMeta stepMeta = new StepMeta();
    stepMeta.setParentTransMeta( transMeta );
    meta.setParentStepMeta( stepMeta );

    assertEquals(
        "    <connectionType>DIRECT</connectionType>" + Const.CR
        + "    <directBootstrapServers>localhost:9092</directBootstrapServers>" + Const.CR
        + "    <clusterName>some_cluster</clusterName>" + Const.CR
        + "    <topic>myTopic</topic>" + Const.CR
        + "    <clientId>id1</clientId>" + Const.CR
        + "    <keyField>fieldOne</keyField>" + Const.CR
        + "    <messageField>message</messageField>" + Const.CR
        + "    <advancedConfig>" + Const.CR
        + "        <option property=\"advanced.property1\"  value=\"advancedPropertyValue1\" />" + Const.CR
        + "        <option property=\"advanced.property2\"  value=\"advancedPropertyValue2\" />" + Const.CR
        + "    </advancedConfig>" + Const.CR, meta.getXML()
    );
    verify( namedClusterEmbedManager ).registerUrl( "hc://some_cluster" );
  }

  @Test
  public void testReadsFromRepository() throws Exception {
    KafkaProducerOutputMeta meta = new KafkaProducerOutputMeta();
    StringObjectId stepId = new StringObjectId( "stepId" );
    when( rep.getStepAttributeString( stepId, CONNECTION_TYPE ) ).thenReturn( "DIRECT" );
    when( rep.getStepAttributeString( stepId, DIRECT_BOOTSTRAP_SERVERS ) ).thenReturn( "localhost:9092" );
    when( rep.getStepAttributeString( stepId, CLUSTER_NAME ) ).thenReturn( "some_cluster" );
    when( rep.getStepAttributeString( stepId, CLIENT_ID ) ).thenReturn( "client01" );
    when( rep.getStepAttributeString( stepId, TOPIC ) ).thenReturn( "readings" );
    when( rep.getStepAttributeString( stepId, KEY_FIELD ) ).thenReturn( "machineId" );
    when( rep.getStepAttributeString( stepId, MESSAGE_FIELD ) ).thenReturn( "reading" );

    when( rep.getStepAttributeInteger( stepId, ADVANCED_CONFIG + "_COUNT" ) ).thenReturn( 2L );
    when( rep.getStepAttributeString( stepId, 0, ADVANCED_CONFIG + "_NAME" ) ).thenReturn( "advanced.config1" );
    when( rep.getStepAttributeString( stepId, 0, ADVANCED_CONFIG + "_VALUE" ) ).thenReturn( "advancedPropertyValue1" );
    when( rep.getStepAttributeString( stepId, 1, ADVANCED_CONFIG + "_NAME" ) ).thenReturn( "advanced.config2" );
    when( rep.getStepAttributeString( stepId, 1, ADVANCED_CONFIG + "_VALUE" ) ).thenReturn( "advancedPropertyValue2" );

    meta.readRep( rep, metastore, stepId, Collections.emptyList() );
    assertThat( meta.getConnectionType(), is( DIRECT ) );
    assertThat( meta.getBootstrapServers(), is( "localhost:9092" ) );
    assertThat( meta.getDirectBootstrapServers(), is( "localhost:9092" ) );
    assertEquals( "some_cluster", meta.getClusterName() );
    assertEquals( "client01", meta.getClientId() );
    assertEquals( "readings", meta.getTopic() );
    assertEquals( "machineId", meta.getKeyField() );
    assertEquals( "reading", meta.getMessageField() );

    assertThat( meta.getConfig().size(), is( 2 ) );
    assertThat( meta.getConfig(), Matchers.hasEntry( "advanced.config1", "advancedPropertyValue1" ) );
    assertThat( meta.getConfig(), Matchers.hasEntry( "advanced.config2", "advancedPropertyValue2" ) );
  }

  @Test
  public void testSavesToRepository() throws Exception {
    KafkaProducerOutputMeta meta = new KafkaProducerOutputMeta();
    StringObjectId stepId = new StringObjectId( "step1" );
    StringObjectId transId = new StringObjectId( "trans1" );
    meta.setConnectionType( DIRECT );
    meta.setDirectBootstrapServers( "localhost:9092" );
    meta.setClusterName( "some_cluster" );
    meta.setClientId( "client01" );
    meta.setTopic( "temperature" );
    meta.setKeyField( "kafkaKey" );
    meta.setMessageField( "kafkaMessage" );

    Map<String, String> advancedConfig = new LinkedHashMap<>();
    advancedConfig.put( "advanced.property1", "advancedPropertyValue1" );
    advancedConfig.put( "advanced.property2", "advancedPropertyValue2" );
    meta.setConfig( advancedConfig );

    meta.saveRep( rep, metastore, transId, stepId );
    verify( rep ).saveStepAttribute( transId, stepId, CONNECTION_TYPE, "DIRECT" );
    verify( rep ).saveStepAttribute( transId, stepId, DIRECT_BOOTSTRAP_SERVERS, "localhost:9092" );
    verify( rep ).saveStepAttribute( transId, stepId, CLUSTER_NAME, "some_cluster" );
    verify( rep ).saveStepAttribute( transId, stepId, CLIENT_ID, "client01" );
    verify( rep ).saveStepAttribute( transId, stepId, TOPIC, "temperature" );
    verify( rep ).saveStepAttribute( transId, stepId, KEY_FIELD, "kafkaKey" );
    verify( rep ).saveStepAttribute( transId, stepId, MESSAGE_FIELD, "kafkaMessage" );

    verify( rep, times( 1 ) ).saveStepAttribute( transId, stepId, ADVANCED_CONFIG + "_COUNT", 2 );
    verify( rep ).saveStepAttribute( transId, stepId, 0, ADVANCED_CONFIG + "_NAME", "advanced.property1" );
    verify( rep ).saveStepAttribute( transId, stepId, 0, ADVANCED_CONFIG + "_VALUE", "advancedPropertyValue1" );
    verify( rep ).saveStepAttribute( transId, stepId, 1, ADVANCED_CONFIG + "_NAME", "advanced.property2" );
    verify( rep ).saveStepAttribute( transId, stepId, 1, ADVANCED_CONFIG + "_VALUE", "advancedPropertyValue2" );
  }

  @Test
  public void testReadsBootstrapServersFromNamedCluster() {
    String resourcesDir = getClass().getResource( "/abortSub.ktr" ).getPath();
    String oldKarafHome = System.getProperty( "karaf.home" );
    System.setProperty( "karaf.home", resourcesDir );
    NamedCluster namedCluster = mock( NamedCluster.class );
    when( namedCluster.getKafkaBootstrapServers() ).thenReturn( "server:11111" );

    NamedClusterService namedClusterService = mock( NamedClusterService.class );
    when( namedClusterService.getNamedClusterByName( eq( "my_cluster" ), nullable( IMetaStore.class ) ) )
        .thenReturn( namedCluster );

    KafkaProducerOutputMeta meta = new KafkaProducerOutputMeta();
    meta.setConnectionType( CLUSTER );
    meta.setNamedClusterService( namedClusterService );
    meta.setMetastoreLocator( metastoreLocator );
    meta.setClusterName( "${clusterName}" );

    TransMeta transMeta = mock( TransMeta.class );
    when( transMeta.environmentSubstitute( "${clusterName}" ) ).thenReturn( "my_cluster" );
    StepMeta stepMeta = new StepMeta();
    stepMeta.setParentTransMeta( transMeta );
    meta.setParentStepMeta( stepMeta );

    assertThat( meta.getBootstrapServers(), is( "server:11111" ) );
    System.setProperty( "karaf.home", null != oldKarafHome ? oldKarafHome : "" );
  }

  @Test
  public void testLooksForEmbeddedMetastore() {
    String resourcesDir = getClass().getResource( "/abortSub.ktr" ).getPath();
    String oldKarafHome = System.getProperty( "karaf.home" );
    System.setProperty( "karaf.home", resourcesDir );
    NamedCluster namedCluster = mock( NamedCluster.class );
    when( namedCluster.getKafkaBootstrapServers() ).thenReturn( "server:11111" );

    EmbeddedMetaStore embeddedMetaStore = mock( EmbeddedMetaStore.class );
    NamedClusterService namedClusterService = mock( NamedClusterService.class );
    when( namedClusterService.getNamedClusterByName( "my_cluster", embeddedMetaStore ) )
      .thenReturn( namedCluster );

    KafkaProducerOutputMeta meta = new KafkaProducerOutputMeta();
    meta.setConnectionType( CLUSTER );
    meta.setNamedClusterService( namedClusterService );
    meta.setMetastoreLocator( metastoreLocator );
    meta.setClusterName( "${clusterName}" );

    TransMeta transMeta = mock( TransMeta.class );
    when( metastoreLocator.getMetastore() ).thenReturn( metastore );
    when( transMeta.environmentSubstitute( "${clusterName}" ) ).thenReturn( "my_cluster" );
    when( transMeta.getEmbeddedMetaStore() ).thenReturn( embeddedMetaStore );
    StepMeta stepMeta = new StepMeta();
    stepMeta.setParentTransMeta( transMeta );
    meta.setParentStepMeta( stepMeta );

    assertThat( meta.getBootstrapServers(), is( "server:11111" ) );
    verify( namedClusterService ).getNamedClusterByName( "my_cluster", metastore );
    System.setProperty( "karaf.home", null != oldKarafHome ? oldKarafHome : "" );
  }

   /*
     Per https://jira.pentaho.com/browse/PDI-19585 this capability was never reproduced when the multishim
     capability was added.  It has been missing since Pentaho 9.0.
   */
//  @Test
//  public void testGetJaasConfig() throws Exception {
//    NamedClusterServiceLocator namedClusterLocator = mock( NamedClusterServiceLocator.class );
//    NamedClusterManager namedClusterService = mock( NamedClusterManager.class );
//    JaasConfigService jaasConfigService = mock( JaasConfigService.class );
//    NamedCluster namedCluster =  mock( NamedCluster.class );
//    when( metastoreLocator.getMetastore() ).thenReturn( metastore );
//    when( namedClusterService.getNamedClusterByName( "kurtsCluster", metastore ) ).thenReturn( namedCluster );
//    when( namedClusterLocator.getService( namedCluster, JaasConfigService.class ) ).thenReturn( jaasConfigService );
//    KafkaProducerOutputMeta inputMeta = new KafkaProducerOutputMeta();
//    inputMeta.setNamedClusterServiceLocator( namedClusterLocator );
//    inputMeta.setNamedClusterService( namedClusterService );
//    inputMeta.setClusterName( "kurtsCluster" );
//    inputMeta.setMetastoreLocator( metastoreLocator );
//    assertEquals( jaasConfigService, inputMeta.getJaasConfigService().get() );
//  }
//
//  @Test
//  public void testGetJaasConfigException() throws Exception {
//    NamedClusterServiceLocator namedClusterLocator = mock( NamedClusterServiceLocator.class );
//    NamedClusterService namedClusterService = mock( NamedClusterService.class );
//    NamedCluster namedCluster =  mock( NamedCluster.class );
//    when( metastoreLocator.getMetastore() ).thenReturn( metastore );
//    when( namedClusterService.getNamedClusterByName( "kurtsCluster", metastore ) ).thenReturn( namedCluster );
//    when( namedClusterLocator.getService( namedCluster, JaasConfigService.class ) )
//      .thenThrow( new ClusterInitializationException( new Exception( "oops" ) ) );
//    KafkaProducerOutputMeta inputMeta = new KafkaProducerOutputMeta();
//    inputMeta.setNamedClusterServiceLocator( namedClusterLocator );
//    inputMeta.setNamedClusterService( namedClusterService );
//    inputMeta.setClusterName( "kurtsCluster" );
//    inputMeta.setMetastoreLocator( metastoreLocator );
//    assertFalse( inputMeta.getJaasConfigService().isPresent() );
//  }

  @Test
  public void testDirectIsDefault() {
    assertEquals( DIRECT, new KafkaProducerOutputMeta().getConnectionType() );
  }

  @Test
  public void testMDI() {
    KafkaProducerOutputMeta meta = new KafkaProducerOutputMeta();
    meta.injectedConfigNames = singletonList( "injectedName" );
    meta.injectedConfigValues = singletonList( "injectedValue" );
    meta.applyInjectedProperties();
    assertThat( meta.getConfig().size(), Matchers.is( 1 ) );
    assertThat( meta.getConfig(), hasEntry( "injectedName", "injectedValue" ) );
  }
}
