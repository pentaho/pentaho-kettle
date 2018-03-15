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

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.jms.context.JmsProvider;

import java.util.List;

@InjectionSupported ( localizationPrefix = "JmsConsumerMeta.Injection." )
@Step ( id = "JmsConsumer", image = "JmsConsumer.svg", name = "JMS Consumer",
  description = "", categoryDescription = "Streaming" )
public class JmsConsumerMeta extends JmsMeta {

  public JmsConsumerMeta( List<JmsProvider> jmsProviders ) {
    super( jmsProviders );
  }

  @SuppressWarnings( "deprecated" )
  public String getDialogClassName() {
    return "org.pentaho.di.trans.step.jms.ui.JmsConsumerDialog";
  }


  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                                Trans trans ) {
    return new JmsConsumer( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }
}
