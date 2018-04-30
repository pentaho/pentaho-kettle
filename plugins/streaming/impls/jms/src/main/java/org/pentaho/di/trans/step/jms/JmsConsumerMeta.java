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

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.GenericStepData;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.jms.context.ActiveMQProvider;
import org.pentaho.di.trans.streaming.common.BaseStreamStepMeta;

import static java.util.Collections.singletonList;
import static org.pentaho.di.core.ObjectLocationSpecificationMethod.FILENAME;

@InjectionSupported ( localizationPrefix = "JmsConsumerMeta.Injection." )
@Step ( id = "Jms2Consumer", image = "JMSC.svg", name = "JMS Consumer",
  description = "JmsConsumerDialog.TypeLongDesc", categoryDescription = "Streaming",
  documentationUrl = "Products/Data_Integration/Transformation_Step_Reference/JMS_Consumer" )
public class JmsConsumerMeta extends BaseStreamStepMeta {

  @InjectionDeep
  public final JmsDelegate jmsDelegate;

  @VisibleForTesting
  public JmsConsumerMeta() {
    this( new JmsDelegate( singletonList( new ActiveMQProvider() ) ) );
  }

  public JmsConsumerMeta( JmsDelegate jmsDelegate ) {
    setSpecificationMethod( FILENAME );
    this.jmsDelegate = jmsDelegate;
  }

  @SuppressWarnings( "deprecated" )
  public String getDialogClassName() {
    return "org.pentaho.di.trans.step.jms.ui.JmsConsumerDialog";
  }

  public RowMeta getRowMeta( String s, VariableSpace variableSpace ) {
    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "message" ) );
    return rowMeta;
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                                Trans trans ) {
    return new JmsConsumer( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override public StepDataInterface getStepData() {
    return new GenericStepData();
  }

}
