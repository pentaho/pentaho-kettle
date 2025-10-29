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
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.streaming.common.BaseStreamStep;
import org.pentaho.di.trans.streaming.common.FixedTimeStreamWindow;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Consume messages from a Kafka topic
 */
public class KafkaConsumerInput extends BaseStreamStep implements StepInterface {

  private static final Class<?> PKG = KafkaConsumerInputMeta.class;
  // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  protected KafkaConsumerInputMeta kafkaConsumerInputMeta;
  protected KafkaConsumerInputData kafkaConsumerInputData;
  protected KafkaFactory kafkaFactory;

  public KafkaConsumerInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                             Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
    setKafkaFactory( KafkaFactory.defaultFactory() );
  }
  protected void setKafkaFactory( KafkaFactory factory ) {
    this.kafkaFactory = factory;
  }

  /**
   * Initialize and do work where other steps need to wait for...
   *
   * @param stepMetaInterface The metadata to work with
   * @param stepDataInterface The data to initialize
   */
  @Override public boolean init( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) {
    kafkaConsumerInputMeta = (KafkaConsumerInputMeta) stepMetaInterface;
    kafkaConsumerInputData = (KafkaConsumerInputData) stepDataInterface;
    boolean superInit = super.init( kafkaConsumerInputMeta, kafkaConsumerInputData );
    if ( !superInit ) {
      logError( BaseMessages.getString( PKG, "KafkaConsumerInput.Error.InitFailed" ) );
      return false;
    }

    if ( !kafkaConsumerInputMeta.getKafkaFactory().checkKafkaConnectionStatus(
            kafkaConsumerInputMeta, variables, getLogChannel() ) ) {
      return false;
    }

    try {
      kafkaConsumerInputData.outputRowMeta = kafkaConsumerInputMeta.getRowMeta( getStepname(), this );
    } catch ( KettleStepException e ) {
      log.logError( e.getMessage(), e );
    }

    this.prepareConsumer( kafkaConsumerInputMeta, kafkaConsumerInputData );

    return true;
  }

  private void commitOffsets( Map.Entry<List<List<Object>>, Result> rowsAndResult ) {
    ( (KafkaStreamSource) source ).commitOffsets( rowsAndResult.getKey() );
  }

  protected void prepareConsumer( KafkaConsumerInputMeta kafkaConsumerInputMeta,
                                  KafkaConsumerInputData kafkaConsumerInputData ) {
    Consumer consumer = kafkaConsumerInputMeta.getKafkaFactory().consumer( kafkaConsumerInputMeta,
            this::environmentSubstitute, kafkaConsumerInputMeta.getKeyField().getOutputType(),
            kafkaConsumerInputMeta.getMessageField().getOutputType() );

    Set<String> topics =
            kafkaConsumerInputMeta.getTopics().stream().map( this::environmentSubstitute ).collect( Collectors.toSet() );
    consumer.subscribe( topics );

    source = new KafkaStreamSource( consumer, kafkaConsumerInputMeta, kafkaConsumerInputData, variables, this );
    window = new FixedTimeStreamWindow<>( getSubtransExecutor(), kafkaConsumerInputData.outputRowMeta, getDuration(),
            getBatchSize(), getParallelism(), kafkaConsumerInputMeta.isAutoCommit() ? p -> {
    } : this::commitOffsets );
  }

}
