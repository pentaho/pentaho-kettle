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

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


public class KafkaProducerOutput extends BaseStep implements StepInterface, Callback {

  private static final Class<?> PKG = KafkaConsumerInputMeta.class;
  private KafkaProducerOutputMeta meta;
  private KafkaProducerOutputData data;
  protected KafkaFactory kafkaFactory;
  // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  public KafkaProducerOutput( StepMeta stepMeta,
                              StepDataInterface stepDataInterface, int copyNr,
                              TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
    setKafkaFactory( KafkaFactory.defaultFactory() );
  }

  public void setKafkaFactory( KafkaFactory factory ) {
    this.kafkaFactory = factory;
  }

  /**
   * Initialize and do work where other steps need to wait for...
   *
   * @param stepMetaInterface The metadata to work with
   * @param stepDataInterface The data to initialize
   */
  @Override public boolean init( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) {
    super.init( stepMetaInterface, stepDataInterface );
    meta = ( (KafkaProducerOutputMeta) stepMetaInterface );
    data = ( (KafkaProducerOutputData) stepDataInterface );

    return true;
  }

  @Override public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    Object[] r = getRow(); // get row, set busy!
    if ( r == null ) {
      // no more input to be expected...
      setOutputDone();
      if ( data.kafkaProducer != null ) {
        data.kafkaProducer.close();
      }
      return false;
    }
    if ( first ) {
      data.keyFieldIndex = getInputRowMeta().indexOfValue( environmentSubstitute( meta.getKeyField() ) );
      data.messageFieldIndex = getInputRowMeta().indexOfValue( environmentSubstitute( meta.getMessageField() ) );
      ValueMetaInterface keyValueMeta = getInputRowMeta().getValueMeta( data.keyFieldIndex );
      ValueMetaInterface msgValueMeta = getInputRowMeta().getValueMeta( data.messageFieldIndex );

      data.kafkaProducer = kafkaFactory.producer( meta, this::environmentSubstitute,
        KafkaConsumerField.Type.fromValueMetaInterface( keyValueMeta ),
        KafkaConsumerField.Type.fromValueMetaInterface( msgValueMeta ) );

      data.isOpen = true;

      first = false;
    }

    if ( !data.isOpen ) {
      return false;
    }
    ProducerRecord<Object, Object> producerRecord;
    // allow for null keys
    if ( data.keyFieldIndex < 0 || r[ data.keyFieldIndex ] == null || StringUtil
       .isEmpty( r[ data.keyFieldIndex ].toString() ) ) {
      producerRecord = new ProducerRecord<>( environmentSubstitute( meta.getTopic() ), r[ data.messageFieldIndex ] );
    } else {
      producerRecord = new ProducerRecord<>( environmentSubstitute( meta.getTopic() ), r[ data.keyFieldIndex ],
        r[ data.messageFieldIndex ] );
    }

    data.kafkaProducer.send( producerRecord, this );
    incrementLinesOutput();

    putRow( getInputRowMeta(), r ); // copy row to possible alternate rowset(s).

    if ( checkFeedback( getLinesRead() ) && log.isBasic() ) {
      logBasic( BaseMessages.getString( PKG, "KafkaConsumerInput.Log.LineNumber" ) + getLinesRead() );
    }

    return true;
  }

  @Override
  public void stopRunning( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) {
    if ( data.kafkaProducer != null && data.isOpen ) {
      data.isOpen = false;
      data.kafkaProducer.flush();
      data.kafkaProducer.close();
    }
  }

  /**
   * Callback for the Kafka producer, not to be called externally.  Used to log debug messages from successful sends
   * and catch any exceptions from errors.
   * @param metadata
   * @param exception
   */
  @Override
  public void onCompletion( RecordMetadata metadata, Exception exception ) {
    if ( null != metadata && log.isDebug() ) {
      logDebug( metadata.toString() );
    } else if ( null != exception ) {
      logError( BaseMessages.getString( PKG, "KafkaProducer.Error.CallbackException" ), exception );
      stopAll();
    }
  }

}
