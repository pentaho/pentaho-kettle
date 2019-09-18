/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.step.jms;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.BooleanUtils;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.util.serialization.BaseSerializingMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Optional.ofNullable;

public class JmsProducer extends BaseStep implements StepInterface {

  JmsProducerMeta meta;
  @VisibleForTesting
  JMSProducer producer;
  private Destination destination;
  private int messageIndex;
  private JMSContext jmsContext;
  private AtomicBoolean closed = new AtomicBoolean( false );

  public JmsProducer( StepMeta stepMeta,
                      StepDataInterface stepDataInterface, int copyNr,
                      TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public boolean init( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) {
    boolean isInitalized = super.init( stepMetaInterface, stepDataInterface );
    meta = ( (JmsProducerMeta) ( (BaseSerializingMeta) stepMetaInterface ).withVariables( this ) );

    List<CheckResultInterface> remarks = new ArrayList<>();
    meta.check(
      remarks, getTransMeta(), meta.getParentStepMeta(),
      null, null, null, null, //these parameters are not used inside the method
      variables, getRepository(), getMetaStore() );
    @SuppressWarnings( "squid:S3864" ) //usage of peek is appropriate here
    boolean errorsPresent =
      remarks.stream().filter( result -> result.getType() == CheckResultInterface.TYPE_RESULT_ERROR )
        .peek( result -> logError( result.getText() ) )
        .count() > 0;
    if ( errorsPresent ) {
      return false;
    }

    return isInitalized;
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    Object[] row = getRow();

    if ( null == row ) {
      jmsContext.close();
      setOutputDone();
      return false;  // indicates done
    }

    if ( first ) {
      // init connections
      log.logDebug( "Connection Details: "
        + meta.jmsDelegate.getJmsProvider().getConnectionDetails( meta.jmsDelegate ) );
      jmsContext = meta.jmsDelegate.getJmsContext();
      producer = jmsContext.createProducer();
      destination = meta.jmsDelegate.getDestination();
      messageIndex = getInputRowMeta().indexOfValue( meta.getFieldToSend() );

      setOptions( producer );

      for ( Map.Entry<String, String> entry : meta.getPropertyValuesByName().entrySet() ) {
        String key = entry.getKey();
        String value = entry.getValue();
        logDebug( "Setting Jms Property Name: " + key + ", Value: " + value );
        producer.setProperty( key, value );
      }

      first = false;
    }
    if ( closed.get() ) {
      return false;
    }

    // send row to JMS
    producer.send( destination, ofNullable( row[ messageIndex ] )
      .map( Object::toString )
      .orElse( null ) );

    // send to next steps
    putRow( getInputRowMeta(), row );
    return true;
  }

  @Override public void stopRunning( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) {
    if ( jmsContext != null && !closed.getAndSet( true ) ) {
      jmsContext.close();
    }
  }

  private void setOptions( JMSProducer producer ) {
    String optionValue = meta.getDisableMessageId();
    getLogChannel().logDebug( "Disable Message ID is set to " + optionValue );
    if ( !StringUtil.isEmpty( optionValue ) ) {
      producer.setDisableMessageID( BooleanUtils.toBoolean( optionValue ) );
    }

    optionValue = meta.getDisableMessageTimestamp();
    getLogChannel().logDebug( "Disable Message Timestamp is set to " + optionValue );
    if ( !StringUtil.isEmpty( optionValue ) ) {
      producer.setDisableMessageTimestamp( BooleanUtils.toBoolean( optionValue ) );
    }

    optionValue = meta.getDeliveryMode();
    getLogChannel().logDebug( "Delivery Mode is set to " + optionValue );
    if ( !StringUtil.isEmpty( optionValue ) ) {
      producer.setDeliveryMode( Integer.parseInt( optionValue ) );
    }

    optionValue = meta.getPriority();
    getLogChannel().logDebug( "Priority is set to " + optionValue );
    if ( !StringUtil.isEmpty( optionValue ) ) {
      producer.setPriority( Integer.parseInt( optionValue ) );
    }

    optionValue = meta.getTimeToLive();
    getLogChannel().logDebug( "Time to Live is set to " + optionValue );
    if ( !StringUtil.isEmpty( optionValue ) ) {
      producer.setTimeToLive( Long.parseLong( optionValue ) );
    }

    optionValue = meta.getDeliveryDelay();
    getLogChannel().logDebug( "Delivery Delay is set to " + optionValue );
    if ( !StringUtil.isEmpty( optionValue ) ) {
      producer.setDeliveryDelay( Long.parseLong( optionValue ) );
    }

    optionValue = meta.getJmsCorrelationId();
    getLogChannel().logDebug( "JMS Correlation ID is set to " + optionValue );
    if ( !StringUtil.isEmpty( optionValue ) ) {
      producer.setJMSCorrelationID( optionValue );
    }

    optionValue = meta.getJmsType();
    getLogChannel().logDebug( "JMS Type is set to " + optionValue );
    if ( !StringUtil.isEmpty( optionValue ) ) {
      producer.setJMSType( optionValue );
    }
  }
}
