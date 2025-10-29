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
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by rfellows on 6/2/17.
 */
@SuppressWarnings( "squid:S4276" ) //Cannot refactor to UnaryOperator because usage of .andThen
public class KafkaFactory {
  private Function<Map<String, Object>, Consumer> consumerFunction;
  private Function<Map<String, Object>, Producer<Object, Object>> producerFunction;

  public static KafkaFactory defaultFactory() {
    return new KafkaFactory( KafkaConsumer::new, KafkaProducer::new );
  }

  public KafkaFactory(
    Function<Map<String, Object>, Consumer> consumerFunction,
    Function<Map<String, Object>, Producer<Object, Object>> producerFunction ) {
    this.consumerFunction = consumerFunction;
    this.producerFunction = producerFunction;
  }

  public Consumer consumer( KafkaConsumerInputMeta meta, Function<String, String> variablesFunction ) {
    return consumer( meta, variablesFunction, KafkaConsumerField.Type.String, KafkaConsumerField.Type.String );
  }

  public Consumer consumer( KafkaConsumerInputMeta meta, Function<String, String> variablesFunction,
    KafkaConsumerField.Type keyDeserializerType, KafkaConsumerField.Type msgDeserializerType ) {

    HashMap<String, Object> kafkaConfig = new HashMap<>();
    Function<String, String> variableNonNull = variablesFunction.andThen( KafkaFactory::nullToEmpty );
    kafkaConfig.put( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, variableNonNull.apply( meta.getBootstrapServers() ) );
    kafkaConfig.put( ConsumerConfig.GROUP_ID_CONFIG, variableNonNull.apply( meta.getConsumerGroup() ) );
    kafkaConfig.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, msgDeserializerType.getKafkaDeserializerClass() );
    kafkaConfig.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializerType.getKafkaDeserializerClass() );
    kafkaConfig.put( ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, meta.isAutoCommit() );
    //meta.getJaasConfigService().ifPresent( jaasConfigService -> putKerberosConfig( kafkaConfig, jaasConfigService ) );
    setConsumerConfigValue( meta, variableNonNull, kafkaConfig );

    return consumerFunction.apply( kafkaConfig );
  }

   /*
     Per https://jira.pentaho.com/browse/PDI-19585 this capability was never reproduced when the multishim
     capability was added.  It has been missing since Pentaho 9.0.
   */
//  public void putKerberosConfig( Map<String, Object> kafkaConfig, JaasConfigService jaasConfigService ) {
//    if ( jaasConfigService.isKerberos() ) {
//      kafkaConfig.put( SaslConfigs.SASL_JAAS_CONFIG, jaasConfigService.getJaasConfig() );
//      kafkaConfig.put( CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT" );
//    }
//  }

  public Producer<Object, Object> producer(
    KafkaProducerOutputMeta meta, Function<String, String> variablesFunction ) {
    return producer( meta, variablesFunction, KafkaConsumerField.Type.String, KafkaConsumerField.Type.String );
  }

  public Producer<Object, Object> producer(
    KafkaProducerOutputMeta meta, Function<String, String> variablesFunction,
    KafkaConsumerField.Type keySerializerType, KafkaConsumerField.Type msgSerializerType ) {

    Function<String, String> variableNonNull = variablesFunction.andThen( KafkaFactory::nullToEmpty );
    HashMap<String, Object> kafkaConfig = new HashMap<>();
    kafkaConfig.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, variableNonNull.apply( meta.getBootstrapServers() ) );
    kafkaConfig.put( ProducerConfig.CLIENT_ID_CONFIG, variableNonNull.apply( meta.getClientId() ) );
    kafkaConfig.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, msgSerializerType.getKafkaSerializerClass() );
    kafkaConfig.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializerType.getKafkaSerializerClass() );
    //meta.getJaasConfigService().ifPresent( jaasConfigService -> putKerberosConfig( kafkaConfig, jaasConfigService ) );
    setProducerConfigValue( meta, variableNonNull, kafkaConfig );

    return producerFunction.apply( kafkaConfig );
  }

  private static String nullToEmpty( String value ) {
    return value == null ? "" : value;
  }

  protected void setProducerConfigValue( KafkaProducerOutputMeta meta, Function<String, String> variableNonNull,
                                         HashMap<String, Object> kafkaConfig ) {
    meta.getConfig().entrySet()
      .forEach( ( entry -> kafkaConfig.put( entry.getKey(), variableNonNull.apply(
         (String) entry.getValue() ) ) ) );
  }

  protected void setConsumerConfigValue( KafkaConsumerInputMeta meta, Function<String, String> variableNonNull,
                                         HashMap<String, Object> kafkaConfig ) {
    meta.getConfig().entrySet()
      .forEach( ( entry -> kafkaConfig.put( entry.getKey(), variableNonNull.apply(
        (String) entry.getValue() ) ) ) );
  }

  public boolean checkKafkaConnectionStatus( KafkaConsumerInputMeta meta, VariableSpace variables,
                                             LogChannelInterface logChannel ) {
    return true;
  }

}
