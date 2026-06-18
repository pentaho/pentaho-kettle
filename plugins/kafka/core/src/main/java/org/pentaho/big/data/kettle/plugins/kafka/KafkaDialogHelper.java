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

import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.config.SslConfigs;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.hadoop.shim.api.cluster.NamedClusterService;
import org.pentaho.metastore.locator.api.MetastoreLocator;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaConsumerInputMeta.ConnectionType.CLUSTER;
import static org.pentaho.big.data.kettle.plugins.kafka.KafkaConsumerInputMeta.ConnectionType.DIRECT;

public class KafkaDialogHelper {
  protected ComboVar wTopic;
  protected ComboVar wClusterName;
  protected Button wbCluster;
  protected TextVar wBootstrapServers;
  private KafkaFactory kafkaFactory;
  private NamedClusterService namedClusterService;
  private MetastoreLocator metastoreLocator;
  //private NamedClusterServiceLocator namedClusterServiceLocator;
  protected TableView optionsTable;
  private StepMeta parentMeta;
  private VariableSpace variableSpace;

  // squid:S00107 cannot consolidate params because they can come from either KafkaConsumerInputMeta or
  // KafkaProducerOutputMeta which do not share a common interface.  Would increase complexity for the trivial gain of
  // less parameters in the constructor
  @SuppressWarnings( "squid:S00107" )
  public KafkaDialogHelper( ComboVar wClusterName, ComboVar wTopic, Button wbCluster, TextVar wBootstrapServers,
                            KafkaFactory kafkaFactory, NamedClusterService namedClusterService, MetastoreLocator metastoreLocator,
                            TableView optionsTable, StepMeta parentMeta ) {
    this.wClusterName = wClusterName;
    this.wTopic = wTopic;
    this.wbCluster = wbCluster;
    this.wBootstrapServers = wBootstrapServers;
    this.kafkaFactory = kafkaFactory;
    this.namedClusterService = namedClusterService;
    this.metastoreLocator = metastoreLocator;
    //this.namedClusterServiceLocator = namedClusterServiceLocator;
    this.optionsTable = optionsTable;
    this.parentMeta = parentMeta;
  }

  @SuppressWarnings ( "unused" ) public void clusterNameChanged( Event event ) {
    if ( ( wbCluster.getSelection() && StringUtil.isEmpty( wClusterName.getText() ) )
      || !wbCluster.getSelection() && StringUtil.isEmpty( wBootstrapServers.getText() ) ) {
      return;
    }
    String current = wTopic.getText();
    String clusterName = wClusterName.getText();
    boolean isCluster = wbCluster.getSelection();
    String directBootstrapServers = wBootstrapServers == null ? "" : wBootstrapServers.getText();
    Map<String, String> config = getConfig( optionsTable );
    if ( !wTopic.getCComboWidget().isDisposed() ) {
      wTopic.getCComboWidget().removeAll();
    }

    CompletableFuture
      .supplyAsync( () -> listTopics( clusterName, isCluster, directBootstrapServers, config ) )
      .thenAccept( topicMap -> Display.getDefault().syncExec( () -> populateTopics( topicMap, current ) ) );
  }

  protected void populateTopics( Map<String, List<PartitionInfo>> topicMap, String current ) {

    topicMap.keySet().stream()
      .filter( key -> !"__consumer_offsets".equals( key ) ).sorted()
      .forEach( key -> {
        if ( !wTopic.isDisposed() ) {
          wTopic.add( key );
        }
      } );
    if ( !wTopic.getCComboWidget().isDisposed() ) {
      wTopic.getCComboWidget().setText( current );
    }
  }

  protected Map<String, List<PartitionInfo>> listTopics( final String clusterName, final boolean isCluster, final String directBootstrapServers,
                                                          Map<String, String> config ) {
    Consumer kafkaConsumer = null;
    try {
      KafkaConsumerInputMeta localMeta = new KafkaConsumerInputMeta();
      localMeta.setNamedClusterService( namedClusterService );
      //localMeta.setNamedClusterServiceLocator( namedClusterServiceLocator );
      localMeta.setMetastoreLocator( metastoreLocator );
      localMeta.setConnectionType( isCluster ? CLUSTER : DIRECT );
      localMeta.setClusterName( clusterName );
      localMeta.setDirectBootstrapServers( directBootstrapServers );
      localMeta.setConfig( config );
      localMeta.setParentStepMeta( parentMeta );
      kafkaConsumer = kafkaFactory.consumer( localMeta, Function.identity() );
      @SuppressWarnings ( "unchecked" ) Map<String, List<PartitionInfo>> topicMap = kafkaConsumer.listTopics();
      return topicMap;
    } catch ( Exception e ) {
      return Collections.emptyMap();
    } finally {
      if ( kafkaConsumer != null ) {
        kafkaConsumer.close();
      }
    }
  }

  public static void populateFieldsList( TransMeta transMeta, ComboVar comboVar, String stepName ) {
    String current = comboVar.getText();
    comboVar.getCComboWidget().removeAll();
    comboVar.setText( current );
    try {
      RowMetaInterface rmi = transMeta.getPrevStepFields( stepName );
      List ls = rmi.getValueMetaList();
      for ( Object l : ls ) {
        ValueMetaBase vmb = (ValueMetaBase) l;
        comboVar.add( vmb.getName() );
      }
    } catch ( KettleStepException ex ) {
      // do nothing
    }
  }

  public static List<String> getConsumerAdvancedConfigOptionNames() {
    return Arrays.asList( ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
      SslConfigs.SSL_KEY_PASSWORD_CONFIG, SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,
      SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
      SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG );
  }

  public static List<String> getProducerAdvancedConfigOptionNames() {
    return Arrays.asList( ProducerConfig.COMPRESSION_TYPE_CONFIG,
      SslConfigs.SSL_KEY_PASSWORD_CONFIG, SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,
      SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
      SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG );
  }

  public static Map<String, String> getConfig( TableView optionsTable ) {
    int itemCount = optionsTable.getItemCount();
    Map<String, String> advancedConfig = new LinkedHashMap<>();

    for ( int rowIndex = 0; rowIndex < itemCount; rowIndex++ ) {
      TableItem row = optionsTable.getTable().getItem( rowIndex );
      String config = row.getText( 1 );
      String value = row.getText( 2 );
      if ( !StringUtils.isBlank( config ) && !advancedConfig.containsKey( config ) ) {
        advancedConfig.put( config, value );
      }
    }
    return advancedConfig;
  }
}
