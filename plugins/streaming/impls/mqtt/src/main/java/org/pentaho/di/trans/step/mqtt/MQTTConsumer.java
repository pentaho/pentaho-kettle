/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.step.mqtt;

import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.streaming.common.BaseStreamStep;
import org.pentaho.di.trans.streaming.common.FixedTimeStreamWindow;

import static org.pentaho.di.i18n.BaseMessages.getString;

/**
 * Streaming consumer of MQTT input.  @see <a href="http://mqtt.org/">mqtt</a>
 */
public class MQTTConsumer extends BaseStreamStep implements StepInterface {
  private static final  Class<?> PKG = MQTTConsumer.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  public MQTTConsumer( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                       Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );

  }

  @Override public boolean init( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) {
    boolean init = super.init( stepMetaInterface, stepDataInterface );
    MQTTConsumerMeta mqttConsumerMeta = (MQTTConsumerMeta) this.variablizedStepMeta;

    try {
      RowMeta rowMeta = mqttConsumerMeta.getRowMeta( getStepname(), this );
      window =
        new FixedTimeStreamWindow<>( getSubtransExecutor(), rowMeta, getDuration(), getBatchSize(), getParallelism() );
      source = new MQTTStreamSource( mqttConsumerMeta, this );
    } catch ( Exception e ) {
      getLogChannel().logError( getString( PKG, "MQTTInput.Error.FailureGettingFields" ), e );
      init = false;
    }
    return init;
  }


}
