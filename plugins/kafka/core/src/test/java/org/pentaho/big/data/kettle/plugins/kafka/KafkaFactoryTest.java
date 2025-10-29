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

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.DoubleSerializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.hadoop.shim.api.cluster.NamedClusterService;
import org.pentaho.hadoop.shim.api.cluster.NamedCluster;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.locator.api.MetastoreLocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class KafkaFactoryTest {
  @Mock Function<Map<String, Object>, Consumer> consumerFun;
  @Mock Function<Map<String, Object>, Producer<Object, Object>> producerFun;
  @Mock NamedClusterService namedClusterService;
  @Mock MetastoreLocator metastoreLocator;
  @Mock IMetaStore metastore;
  @Mock NamedCluster namedCluster;
  @Mock TransMeta transMeta;
  KafkaConsumerInputMeta inputMeta;
  KafkaProducerOutputMeta outputMeta;
  StepMeta stepMeta;
  String oldKarafHome;

  @Before
  public void setUp() {
    KettleLogStore.init();
    String resourcesDir = getClass().getResource( "/abortSub.ktr" ).getPath();
    oldKarafHome = System.getProperty( "karaf.home" );
    System.setProperty( "karaf.home", resourcesDir );
    when( metastoreLocator.getMetastore() ).thenReturn( metastore );
    when( namedCluster.getKafkaBootstrapServers() ).thenReturn( "server:1234" );
    when( namedClusterService.getNamedClusterByName( "some_cluster", metastore ) ).thenReturn( namedCluster );
//    when( namedClusterServiceLocator.getService( namedCluster, JaasConfigService.class ) )
//      .thenReturn( jaasConfigService );
    when( transMeta.environmentSubstitute( "${clusterName}" ) ).thenReturn( "some_cluster" );

    inputMeta = new KafkaConsumerInputMeta();
    inputMeta.setNamedClusterService( namedClusterService );
    inputMeta.setMetastoreLocator( metastoreLocator );
    inputMeta.setClusterName( "${clusterName}" );
    inputMeta.setConnectionType( KafkaConsumerInputMeta.ConnectionType.CLUSTER );
    outputMeta = new KafkaProducerOutputMeta();
    outputMeta.setNamedClusterService( namedClusterService );
    outputMeta.setMetastoreLocator( metastoreLocator );
    outputMeta.setClusterName( "${clusterName}" );
    outputMeta.setConnectionType( KafkaProducerOutputMeta.ConnectionType.CLUSTER );

    stepMeta = new StepMeta();
    stepMeta.setParentTransMeta( transMeta );
    inputMeta.setParentStepMeta( stepMeta );
    outputMeta.setParentStepMeta( stepMeta );
  }

  @After public void tearDown() {
    System.setProperty( "karaf.home", null != oldKarafHome ? oldKarafHome : "" );
  }

  @Test
  public void testMapsConsumers() {
    ArrayList<String> topicList = new ArrayList<>();
    topicList.add( "topic" );
    inputMeta.setTopics( topicList );
    inputMeta.setConsumerGroup( "cg" );

    inputMeta.setKeyField( new KafkaConsumerField( KafkaConsumerField.Name.KEY, "key" ) );
    inputMeta.setMessageField( new KafkaConsumerField( KafkaConsumerField.Name.MESSAGE, "msg" ) );
//    inputMeta.setNamedClusterServiceLocator( namedClusterServiceLocator );
    inputMeta.setAutoCommit( false );

    Map<String, String> advancedConfig = new LinkedHashMap<>();
    advancedConfig.put( "advanced.config1", "advancedPropertyValue1" );
    advancedConfig.put( "advanced.config2", "advancedPropertyValue2" );
    inputMeta.setConfig( advancedConfig );

//    when( jaasConfigService.isKerberos() ).thenReturn( false );

    new KafkaFactory( consumerFun, producerFun ).consumer( inputMeta, Function.identity() );
    Map<String, Object> expectedMap = new HashMap<>();
    expectedMap.put( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "server:1234" );
    expectedMap.put( ConsumerConfig.GROUP_ID_CONFIG, "cg" );
    expectedMap.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class );
    expectedMap.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class );
    expectedMap.put( ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false );
    expectedMap.put( "advanced.config1", "advancedPropertyValue1" );
    expectedMap.put( "advanced.config2", "advancedPropertyValue2" );

    Mockito.verify( consumerFun ).apply( expectedMap  );
  }

  @Test
  public void testMapsConsumersWithDeserializer() {
    ArrayList<String> topicList = new ArrayList<>();
    topicList.add( "topic" );
    inputMeta.setTopics( topicList );
    inputMeta.setConsumerGroup( "cg" );

    inputMeta.setKeyField( new KafkaConsumerField( KafkaConsumerField.Name.KEY, "key", KafkaConsumerField.Type.Integer ) );
    inputMeta.setMessageField( new KafkaConsumerField( KafkaConsumerField.Name.MESSAGE, "msg", KafkaConsumerField.Type.Number ) );
//    inputMeta.setNamedClusterServiceLocator( namedClusterServiceLocator );
//    when( jaasConfigService.isKerberos() ).thenReturn( false );

    new KafkaFactory( consumerFun, producerFun ).consumer( inputMeta, Function.identity(), inputMeta.getKeyField().getOutputType(),
      inputMeta.getMessageField().getOutputType() );
    Map<String, Object> expectedMap = new HashMap<>();
    expectedMap.put( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "server:1234" );
    expectedMap.put( ConsumerConfig.GROUP_ID_CONFIG, "cg" );
    expectedMap.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, DoubleDeserializer.class );
    expectedMap.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class );
    expectedMap.put( ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true );
    Mockito.verify( consumerFun ).apply( expectedMap  );
  }

  @Test
  public void testMapsConsumersWithVariables() {
    inputMeta.setConsumerGroup( "${consumerGroup}" );
//    inputMeta.setNamedClusterServiceLocator( namedClusterServiceLocator );

    Map<String, String> advancedConfig = new LinkedHashMap<>();
    advancedConfig.put( "advanced.variable", "${advanced.var}" );
    inputMeta.setConfig( advancedConfig );

//    when( jaasConfigService.isKerberos() ).thenReturn( false );

    Variables variables = new Variables();
    variables.setVariable( "server", "server:1234" );
    variables.setVariable( "consumerGroup", "cg" );
    variables.setVariable( "advanced.var", "advancedVarValue" );
    new KafkaFactory( consumerFun, producerFun ).consumer( inputMeta, variables::environmentSubstitute );
    Map<String, Object> expectedMap = new HashMap<>();
    expectedMap.put( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "server:1234" );
    expectedMap.put( ConsumerConfig.GROUP_ID_CONFIG, "cg" );
    expectedMap.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class );
    expectedMap.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class );
    expectedMap.put( "advanced.variable", "advancedVarValue" );
    expectedMap.put( ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true );
    Mockito.verify( consumerFun ).apply( expectedMap  );
  }

  @Test
  public void testMapsProducers() {
    outputMeta.setTopic( "topic" );
    outputMeta.setClientId( "client" );
    outputMeta.setKeyField( "key" );
    outputMeta.setMessageField( "msg" );
//    outputMeta.setNamedClusterServiceLocator( namedClusterServiceLocator );

    Map<String, String> advancedConfig = new LinkedHashMap<>();
    advancedConfig.put( "advanced.config1", "advancedPropertyValue1" );
    advancedConfig.put( "advanced.config2", "advancedPropertyValue2" );
    outputMeta.setConfig( advancedConfig );

//    when( jaasConfigService.isKerberos() ).thenReturn( false );

    new KafkaFactory( consumerFun, producerFun ).producer( outputMeta, Function.identity() );
    Map<String, Object> expectedMap = new HashMap<>();
    expectedMap.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "server:1234" );
    expectedMap.put( ProducerConfig.CLIENT_ID_CONFIG, "client" );
    expectedMap.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class );
    expectedMap.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class );
    expectedMap.put( "advanced.config1", "advancedPropertyValue1" );
    expectedMap.put( "advanced.config2", "advancedPropertyValue2" );

    Mockito.verify( producerFun ).apply( expectedMap  );
  }

  @Test
  public void testMapsProducersWithSerializer() {
    outputMeta.setTopic( "topic" );
    outputMeta.setClientId( "client" );
    outputMeta.setKeyField( "key" );
    outputMeta.setMessageField( "msg" );
//    outputMeta.setNamedClusterServiceLocator( namedClusterServiceLocator );
//    when( jaasConfigService.isKerberos() ).thenReturn( false );

    new KafkaFactory( consumerFun, producerFun ).producer( outputMeta, Function.identity(),
      KafkaConsumerField.Type.Integer, KafkaConsumerField.Type.Number );
    Map<String, Object> expectedMap = new HashMap<>();
    expectedMap.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "server:1234" );
    expectedMap.put( ProducerConfig.CLIENT_ID_CONFIG, "client" );
    expectedMap.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, DoubleSerializer.class );
    expectedMap.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class );
    Mockito.verify( producerFun ).apply( expectedMap  );
  }

  @Test
  public void testMapsProducersWithVariables() {
    outputMeta.setClientId( "${client}" );
//    outputMeta.setNamedClusterServiceLocator( namedClusterServiceLocator );

    Map<String, String> advancedConfig = new LinkedHashMap<>();
    advancedConfig.put( "advanced.variable", "${advanced.var}" );
    outputMeta.setConfig( advancedConfig );

//    when( jaasConfigService.isKerberos() ).thenReturn( false );

    Variables variables = new Variables();
    variables.setVariable( "server", "server:1234" );
    variables.setVariable( "client", "myclient" );
    variables.setVariable( "advanced.var", "advancedVarValue" );

    new KafkaFactory( consumerFun, producerFun ).producer( outputMeta, variables::environmentSubstitute );
    Map<String, Object> expectedMap = new HashMap<>();
    expectedMap.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "server:1234" );
    expectedMap.put( ProducerConfig.CLIENT_ID_CONFIG, "myclient" );
    expectedMap.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class );
    expectedMap.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class );
    expectedMap.put( "advanced.variable", "advancedVarValue" );

    Mockito.verify( producerFun ).apply( expectedMap  );
  }

  @Test
  public void testNullMetaPropertiesResultInEmptyString() {
    outputMeta.setClusterName( null );
//    outputMeta.setNamedClusterServiceLocator( namedClusterServiceLocator );
//    when( jaasConfigService.isKerberos() ).thenReturn( false );
    Variables variables = new Variables();

    new KafkaFactory( consumerFun, producerFun ).producer( outputMeta, variables::environmentSubstitute );
    Map<String, Object> expectedMap = new HashMap<>();
    expectedMap.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "" );
    expectedMap.put( ProducerConfig.CLIENT_ID_CONFIG, "" );
    expectedMap.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class );
    expectedMap.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class );
    Mockito.verify( producerFun ).apply( expectedMap  );
  }

   /*
     Per https://jira.pentaho.com/browse/PDI-19585 this capability was never reproduced when the multishim
     capability was added.  It has been missing since Pentaho 9.0.
   */
//  @Test
//  public void testProvidesJaasConfig() {
//    ArrayList<String> topicList = new ArrayList<>();
//    topicList.add( "topic" );
//    inputMeta.setTopics( topicList );
//    inputMeta.setConsumerGroup( "cg" );
//
//    inputMeta.setKeyField( new KafkaConsumerField( KafkaConsumerField.Name.KEY, "key" ) );
//    inputMeta.setMessageField( new KafkaConsumerField( KafkaConsumerField.Name.MESSAGE, "msg" ) );
//    inputMeta.setNamedClusterServiceLocator( namedClusterServiceLocator );
//    when( jaasConfigService.isKerberos() ).thenReturn( true );
//    when( jaasConfigService.getJaasConfig() ).thenReturn( "some jaas config" );
//
//    new KafkaFactory( consumerFun, producerFun ).consumer( inputMeta, Function.identity() );
//    Map<String, Object> expectedMap = new HashMap<>();
//    expectedMap.put( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "server:1234" );
//    expectedMap.put( ConsumerConfig.GROUP_ID_CONFIG, "cg" );
//    expectedMap.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class );
//    expectedMap.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class );
//    expectedMap.put( SaslConfigs.SASL_JAAS_CONFIG, "some jaas config" );
//    expectedMap.put( ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true );
//    expectedMap.put( "security.protocol", "SASL_PLAINTEXT" );
//    Mockito.verify( consumerFun ).apply( expectedMap  );
//  }
}
