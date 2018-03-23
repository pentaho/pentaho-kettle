/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import javax.jms.Destination;
import javax.jms.JMSProducer;

public class JmsProducer extends BaseStep implements StepInterface {

  private JmsProducerMeta meta;
  private JMSProducer producer;
  private Destination destination;
  private int messageIndex;

  public JmsProducer( StepMeta stepMeta,
                      StepDataInterface stepDataInterface, int copyNr,
                      TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public boolean init( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) {
    boolean isInitalized = super.init( stepMetaInterface, stepDataInterface );
    meta = ( (JmsProducerMeta) stepMetaInterface );

    return isInitalized;
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = ( (JmsProducerMeta) smi );
    Object[] row = getRow();

    if ( null == row ) {
      setOutputDone();
      return false;  // indicates done
    }

    if ( first ) {
      // init connections
      producer = meta.jmsDelegate.getJmsContext( this ).createProducer();
      destination = meta.jmsDelegate.getDestination( this );
      messageIndex = getInputRowMeta().indexOfValue( environmentSubstitute( meta.getFieldToSend() ) );
      first = false;
    }

    // send row to JMS
    producer.send( destination, row[ messageIndex ].toString() );

    // send to next steps
    putRow( getInputRowMeta(), row );
    return true;
  }
}
