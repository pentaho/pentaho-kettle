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

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.PartitionInfo;
import org.json.simple.JSONObject;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.pentaho.big.data.kettle.plugins.kafka.KafkaProducerOutputMeta.ConnectionType.CLUSTER;

public class KafkaProducerOutputHelper extends BaseStepHelper {

  protected static final String GET_CLUSTER_LIST = "getClusterList";
  protected static final String GET_TOPIC_LIST = "getTopicList";
  protected static final String CONNECTION_TYPE = "connectionType";
  protected static final String CLUSTER_NAME = "clusterName";
  protected static final String DIRECT_BOOTSTRAP_SERVERS = "directBootstrapServers";
  protected static final String CONFIG_PREFIX = "config.";
  public static final String CLUSTER_LIST = "clusterList";
  public static final String TOPIC_LIST = "topicList";

  private final KafkaProducerOutputMeta kafkaProducerOutputMeta;
  private final KafkaFactory kafkaFactory;

  public KafkaProducerOutputHelper( KafkaProducerOutputMeta kafkaProducerOutputMeta ) {
    this( kafkaProducerOutputMeta, KafkaFactory.defaultFactory() );
  }

  KafkaProducerOutputHelper( KafkaProducerOutputMeta kafkaProducerOutputMeta, KafkaFactory kafkaFactory ) {
    this.kafkaProducerOutputMeta = kafkaProducerOutputMeta;
    this.kafkaFactory = kafkaFactory;
  }

  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    if ( StringUtils.isBlank( method ) ) {
      response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
      return response;
    }

    if ( kafkaProducerOutputMeta == null ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      return response;
    }

    try {
      if ( GET_CLUSTER_LIST.equals( method ) ) {
        response = getClusterList( transMeta );
      } else if ( GET_TOPIC_LIST.equals( method ) ) {
        response = getTopicList( transMeta, queryParams );
      } else {
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
      }
    } catch ( Exception ex ) {
      log.logError( ex.getMessage(), ex );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  private JSONObject getClusterList( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    try {
      if ( transMeta == null || kafkaProducerOutputMeta.getNamedClusterService() == null ) {
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return response;
      }

      response.put( ACTION_STATUS, SUCCESS_RESPONSE );
      response.put( CLUSTER_LIST, kafkaProducerOutputMeta.getNamedClusterService().listNames( transMeta.getMetaStore() ) );
    } catch ( MetaStoreException ex ) {
      log.logError( "Failed to get defined named clusters", ex );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    } catch ( Exception ex ) {
      log.logError( ex.getMessage(), ex );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  private JSONObject getTopicList( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      if ( transMeta == null ) {
        response.put( ACTION_STATUS, FAILURE_RESPONSE );
        return response;
      }

      List<String> topics = fetchTopicNames( queryParams );
      response.put( ACTION_STATUS, SUCCESS_RESPONSE );
      response.put( TOPIC_LIST, topics );
    } catch ( Exception ex ) {
      log.logError( ex.getMessage(), ex );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  protected List<String> fetchTopicNames( Map<String, String> queryParams ) {
    Map<String, List<PartitionInfo>> topicMap = listTopics( queryParams );
    List<String> topics = new ArrayList<>( topicMap.keySet() );
    topics.removeIf( "__consumer_offsets"::equals );
    Collections.sort( topics );
    return topics;
  }

  protected Map<String, List<PartitionInfo>> listTopics( Map<String, String> queryParams ) {
    Consumer<?, ?> kafkaConsumer = null;
    try {
      KafkaConsumerInputMeta localMeta = new KafkaConsumerInputMeta();
      localMeta.setNamedClusterService( kafkaProducerOutputMeta.getNamedClusterService() );
      localMeta.setMetastoreLocator( kafkaProducerOutputMeta.getMetastoreLocator() );
      localMeta.setConnectionType( resolveConnectionType( queryParams ) );
      localMeta.setClusterName( resolveClusterName( queryParams ) );
      localMeta.setDirectBootstrapServers( resolveDirectBootstrapServers( queryParams ) );
      localMeta.setConfig( resolveConfig( queryParams ) );
      localMeta.setParentStepMeta( kafkaProducerOutputMeta.getParentStepMeta() );
      kafkaConsumer = kafkaFactory.consumer( localMeta, Function.identity() );
      @SuppressWarnings( "unchecked" )
      Map<String, List<PartitionInfo>> topicMap = kafkaConsumer.listTopics();
      return topicMap;
    } catch ( Exception ex ) {
      log.logDebug( ex.getMessage(), ex );
      return Collections.emptyMap();
    } finally {
      if ( kafkaConsumer != null ) {
        kafkaConsumer.close();
      }
    }
  }

  private KafkaConsumerInputMeta.ConnectionType resolveConnectionType( Map<String, String> queryParams ) {
    String connectionType = queryParams == null ? null : queryParams.get( CONNECTION_TYPE );
    return CLUSTER.name().equalsIgnoreCase( connectionType )
      ? KafkaConsumerInputMeta.ConnectionType.CLUSTER
      : KafkaConsumerInputMeta.ConnectionType.DIRECT;
  }

  private String resolveClusterName( Map<String, String> queryParams ) {
    String clusterName = queryParams == null ? null : queryParams.get( CLUSTER_NAME );
    return StringUtils.defaultIfBlank( clusterName, kafkaProducerOutputMeta.getClusterName() );
  }

  private String resolveDirectBootstrapServers( Map<String, String> queryParams ) {
    String bootstrapServers = queryParams == null ? null : queryParams.get( DIRECT_BOOTSTRAP_SERVERS );
    return StringUtils.defaultIfBlank( bootstrapServers, kafkaProducerOutputMeta.getDirectBootstrapServers() );
  }

  private Map<String, String> resolveConfig( Map<String, String> queryParams ) {
    Map<String, String> config = new java.util.LinkedHashMap<>( kafkaProducerOutputMeta.getConfig() );
    List<String> knownConfigKeys = KafkaDialogHelper.getProducerAdvancedConfigOptionNames();
    if ( queryParams == null ) {
      return config;
    }

    for ( String configKey : knownConfigKeys ) {
      String queryValue = queryParams.get( CONFIG_PREFIX + configKey );
      if ( queryValue == null ) {
        queryValue = queryParams.get( configKey );
      }
      if ( queryValue != null ) {
        config.put( configKey, queryValue );
      }
    }
    return config;
  }
}