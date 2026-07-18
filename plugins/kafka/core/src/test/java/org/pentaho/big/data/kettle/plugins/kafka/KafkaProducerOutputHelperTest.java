/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.big.data.kettle.plugins.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.PartitionInfo;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.junit.runner.RunWith;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.hadoop.shim.api.cluster.NamedClusterService;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_RESPONSE;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_METHOD_NOT_FOUND_RESPONSE;
import static org.pentaho.di.trans.step.StepHelperInterface.SUCCESS_RESPONSE;

@RunWith( MockitoJUnitRunner.class )
public class KafkaProducerOutputHelperTest {

  @Mock
  private LogChannelInterfaceFactory logChannelFactory;

  @Mock
  private LogChannelInterface logChannel;

  @Mock
  private NamedClusterService namedClusterService;

  @Mock
  private IMetaStore metaStore;

  private KafkaProducerOutputMeta meta;
  private TransMeta transMeta;

  @BeforeClass
  public static void init() throws Exception {
    KettleClientEnvironment.init();
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();
    if ( !Props.isInitialized() ) {
      Props.init( 0 );
    }
    StepPluginType.getInstance().handlePluginAnnotation(
      KafkaProducerOutputMeta.class,
      KafkaProducerOutputMeta.class.getAnnotation( org.pentaho.di.core.annotations.Step.class ),
      Collections.emptyList(), false, null );
  }

  @Before
  public void setUp() {
    KettleLogStore.setLogChannelInterfaceFactory( logChannelFactory );
    lenient().doReturn( LogLevel.BASIC ).when( logChannel ).getLogLevel();
    when( logChannelFactory.create( any() ) ).thenReturn( logChannel );

    meta = new KafkaProducerOutputMeta();
    meta.setNamedClusterService( namedClusterService );
    meta.setClusterName( "saved-cluster" );
    meta.setDirectBootstrapServers( "saved-host:9092" );
    meta.setConfig( new LinkedHashMap<>() );

    transMeta = org.mockito.Mockito.mock( TransMeta.class );
    when( transMeta.getMetaStore() ).thenReturn( metaStore );
  }

  @Test
  public void testGetClusterListReturnsNames() throws Exception {
    when( namedClusterService.listNames( metaStore ) ).thenReturn( Arrays.asList( "clusterA", "clusterB" ) );

    KafkaProducerOutputHelper helper = new KafkaProducerOutputHelper( meta );
    JSONObject response = helper.stepAction( "getClusterList", transMeta, new HashMap<>() );

    assertEquals( SUCCESS_RESPONSE, response.get( "actionStatus" ) );
    assertEquals( Arrays.asList( "clusterA", "clusterB" ), response.get( "clusterList" ) );
  }

  @Test
  public void testGetTopicListUsesCurrentQueryParamsAndFiltersInternalTopic() {
    KafkaProducerOutputHelper helper = new KafkaProducerOutputHelper( meta ) {
      @Override
      protected java.util.Map<String, java.util.List<org.apache.kafka.common.PartitionInfo>> listTopics( Map<String, String> queryParams ) {
        assertEquals( "DIRECT", queryParams.get( "connectionType" ) );
        assertEquals( "live-host:9092", queryParams.get( "directBootstrapServers" ) );
        assertEquals( "override", queryParams.get( "config.compression.type" ) );

        Map<String, java.util.List<org.apache.kafka.common.PartitionInfo>> topicMap = new LinkedHashMap<>();
        topicMap.put( "z-topic", Collections.emptyList() );
        topicMap.put( "__consumer_offsets", Collections.emptyList() );
        topicMap.put( "a-topic", Collections.emptyList() );
        return topicMap;
      }
    };

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "connectionType", "DIRECT" );
    queryParams.put( "directBootstrapServers", "live-host:9092" );
    queryParams.put( "config.compression.type", "override" );

    JSONObject response = helper.stepAction( "getTopicList", transMeta, queryParams );

    assertNotNull( response );
    assertEquals( SUCCESS_RESPONSE, response.get( "actionStatus" ) );
    assertEquals( Arrays.asList( "a-topic", "z-topic" ), response.get( "topicList" ) );
  }

  @Test
  public void testGetTopicListWithNullTransMetaFails() {
    KafkaProducerOutputHelper helper = new KafkaProducerOutputHelper( meta );

    JSONObject response = helper.stepAction( "getTopicList", null, new HashMap<>() );

    assertEquals( FAILURE_RESPONSE, response.get( "actionStatus" ) );
  }

  @Test
  public void testProducerMetaExposesHelper() {
    assertEquals( KafkaProducerOutputHelper.class, meta.getStepHelperInterface().getClass() );
  }

  @Test
  public void testBlankMethodFailsWithMethodNotFound() {
    KafkaProducerOutputHelper helper = new KafkaProducerOutputHelper( meta );

    JSONObject response = helper.stepAction( "", transMeta, new HashMap<>() );

    assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( "actionStatus" ) );
  }

  @Test
  public void testUnknownMethodFailsWithMethodNotFound() {
    KafkaProducerOutputHelper helper = new KafkaProducerOutputHelper( meta );

    JSONObject response = helper.stepAction( "unknown", transMeta, new HashMap<>() );

    assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( "actionStatus" ) );
  }

  @Test
  public void testNullMetaFails() {
    KafkaProducerOutputHelper helper = new KafkaProducerOutputHelper( null );

    JSONObject response = helper.stepAction( "getTopicList", transMeta, new HashMap<>() );

    assertEquals( FAILURE_RESPONSE, response.get( "actionStatus" ) );
  }

  @Test
  public void testGetClusterListFailsWhenNamedClusterServiceMissing() {
    meta.setNamedClusterService( null );
    KafkaProducerOutputHelper helper = new KafkaProducerOutputHelper( meta );

    JSONObject response = helper.stepAction( "getClusterList", transMeta, new HashMap<>() );

    assertEquals( FAILURE_RESPONSE, response.get( "actionStatus" ) );
  }

  @Test
  public void testGetClusterListFailsOnMetaStoreException() throws Exception {
    when( namedClusterService.listNames( metaStore ) ).thenThrow( new MetaStoreException( "boom" ) );
    KafkaProducerOutputHelper helper = new KafkaProducerOutputHelper( meta );

    JSONObject response = helper.stepAction( "getClusterList", transMeta, new HashMap<>() );

    assertEquals( FAILURE_RESPONSE, response.get( "actionStatus" ) );
  }

  @Test
  public void testGetTopicListFailsWhenTopicFetchThrows() {
    KafkaProducerOutputHelper helper = new KafkaProducerOutputHelper( meta ) {
      @Override
      protected List<String> fetchTopicNames( Map<String, String> queryParams ) {
        throw new RuntimeException( "topic-fetch-failed" );
      }
    };

    JSONObject response = helper.stepAction( "getTopicList", transMeta, new HashMap<>() );

    assertEquals( FAILURE_RESPONSE, response.get( "actionStatus" ) );
  }

  @Test
  @SuppressWarnings( "unchecked" )
  public void testListTopicsResolvesQueryOverridesAndClosesConsumer() {
    Map<String, String> baseConfig = new LinkedHashMap<>();
    baseConfig.put( "compression.type", "none" );
    meta.setConfig( baseConfig );

    Consumer<?, ?> kafkaConsumer = mock( Consumer.class );
    Map<String, List<PartitionInfo>> topicMap = new LinkedHashMap<>();
    topicMap.put( "topic-a", Collections.emptyList() );
    when( kafkaConsumer.listTopics() ).thenReturn( topicMap );

    KafkaFactory kafkaFactory = mock( KafkaFactory.class );
    when( kafkaFactory.consumer( any( KafkaConsumerInputMeta.class ), any( Function.class ) ) ).thenAnswer( invocation -> {
      KafkaConsumerInputMeta localMeta = invocation.getArgument( 0 );
      assertEquals( KafkaConsumerInputMeta.ConnectionType.CLUSTER, localMeta.getConnectionType() );
      assertEquals( "query-cluster", localMeta.getClusterName() );
      assertEquals( "query-host:9092", localMeta.getDirectBootstrapServers() );
      assertEquals( "gzip", localMeta.getConfig().get( "compression.type" ) );
      return kafkaConsumer;
    } );

    KafkaProducerOutputHelper helper = new KafkaProducerOutputHelper( meta, kafkaFactory );
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "connectionType", "CLUSTER" );
    queryParams.put( "clusterName", "query-cluster" );
    queryParams.put( "directBootstrapServers", "query-host:9092" );
    queryParams.put( "config.compression.type", "gzip" );

    Map<String, List<PartitionInfo>> response = helper.listTopics( queryParams );

    assertEquals( 1, response.size() );
    verify( kafkaConsumer ).close();
  }

  @Test
  @SuppressWarnings( "unchecked" )
  public void testListTopicsFallsBackToMetaValuesWhenQueryMissing() {
    Map<String, String> baseConfig = new LinkedHashMap<>();
    baseConfig.put( "compression.type", "snappy" );
    meta.setConfig( baseConfig );

    Consumer<?, ?> kafkaConsumer = mock( Consumer.class );
    when( kafkaConsumer.listTopics() ).thenReturn( Collections.emptyMap() );

    KafkaFactory kafkaFactory = mock( KafkaFactory.class );
    when( kafkaFactory.consumer( any( KafkaConsumerInputMeta.class ), any( Function.class ) ) ).thenAnswer( invocation -> {
      KafkaConsumerInputMeta localMeta = invocation.getArgument( 0 );
      assertEquals( KafkaConsumerInputMeta.ConnectionType.DIRECT, localMeta.getConnectionType() );
      assertEquals( "saved-cluster", localMeta.getClusterName() );
      assertEquals( "saved-host:9092", localMeta.getDirectBootstrapServers() );
      assertEquals( "snappy", localMeta.getConfig().get( "compression.type" ) );
      return kafkaConsumer;
    } );

    KafkaProducerOutputHelper helper = new KafkaProducerOutputHelper( meta, kafkaFactory );

    helper.listTopics( null );

    verify( kafkaConsumer ).close();
  }

  @Test
  @SuppressWarnings( "unchecked" )
  public void testListTopicsReturnsEmptyWhenKafkaFactoryThrows() {
    KafkaFactory kafkaFactory = mock( KafkaFactory.class );
    when( kafkaFactory.consumer( any( KafkaConsumerInputMeta.class ), any( Function.class ) ) )
      .thenThrow( new RuntimeException( "boom" ) );

    KafkaProducerOutputHelper helper = new KafkaProducerOutputHelper( meta, kafkaFactory );

    Map<String, List<PartitionInfo>> response = helper.listTopics( new HashMap<>() );

    assertEquals( Collections.emptyMap(), response );
  }
}