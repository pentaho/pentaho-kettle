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

import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.streaming.common.BaseStreamStep;
import org.pentaho.di.trans.streaming.common.FixedTimeStreamWindow;

import static java.util.Objects.requireNonNull;
import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.jms.JmsConstants.PKG;

public class JmsConsumer extends BaseStreamStep {


  public JmsConsumer( StepMeta stepMeta,
                      StepDataInterface stepDataInterface, int copyNr,
                      TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean init( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) {
    boolean superStatus = super.init( stepMetaInterface, stepDataInterface );

    JmsConsumerMeta meta = (JmsConsumerMeta) stepMetaInterface;

    if ( !validateParams( meta ) ) {
      return false;
    }


    window = new FixedTimeStreamWindow<>(
      subtransExecutor, meta.jmsDelegate.getRowMeta(), getDuration(), getBatchSize() );
    source = new JmsStreamSource( this, requireNonNull( meta.jmsDelegate ), getReceiverTimeout( meta ) );
    return superStatus;
  }

  public int getReceiverTimeout( JmsConsumerMeta meta ) {
    try {
      return Integer.parseInt( this.environmentSubstitute( meta.jmsDelegate.receiveTimeout ) );
    } catch ( NumberFormatException nfe ) {
      logError( getString( PKG, "JmsConsumer.ReceiveTimeoutInvalid" ) );
    }
    return -1;
  }

  public boolean validateParams( JmsConsumerMeta meta ) {
    return getReceiverTimeout( meta ) > -1;
  }

}
