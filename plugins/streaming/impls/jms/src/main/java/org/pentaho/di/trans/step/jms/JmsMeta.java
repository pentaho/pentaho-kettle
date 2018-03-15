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


import javax.jms.JMSContext;

import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.GenericStepData;
import org.pentaho.di.core.util.serialization.Sensitive;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.jms.context.JmsProvider;
import org.pentaho.di.trans.streaming.common.BaseStreamStepMeta;

import javax.jms.Destination;
import java.util.List;
import java.util.Objects;

import static org.pentaho.di.trans.step.jms.context.JmsProvider.ConnectionType.WEBSPHERE;
import static org.pentaho.di.trans.step.jms.context.JmsProvider.DestinationType.QUEUE;


@InjectionSupported ( localizationPrefix = "IBMMQConsumerMeta.Injection." )
public abstract class JmsMeta extends BaseStreamStepMeta {

  //TODO move these props to a container pojo that can be added to both
  // the consumer and producer metas, since BaseStreamStepMeta is just consumer.

  @Injection ( name = "DESTINATION" ) public String destinationName = "DEV.QUEUE.1";

  @Injection ( name = "URL" ) public String url = "mq://10.177.178.135:1414/QM1?channel=DEV.APP.SVRCONN";

  @Injection ( name = "USERNAME" ) public String username = "devuser";

  @Sensitive
  @Injection ( name = "PASSWORD" ) public String password = "password";

  @Injection ( name = "JNDI_URL" ) public String jndiUrl = "";

  @Injection ( name = "USE_JNDI" ) public boolean useJndi = false;

  @Injection ( name = "CONNECTION_TYPE" ) public String connectionType = WEBSPHERE.name();

  @Injection ( name = "DESTINATION_TYPE" ) public String destinationType = QUEUE.name();


  protected final List<JmsProvider> jmsProviders;

  protected JmsMeta( List<JmsProvider> jmsProviders ) {
    super();
    this.jmsProviders = jmsProviders;
    setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
  }

  @Override public RowMeta getRowMeta( String s, VariableSpace variableSpace ) throws KettleStepException {
    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "message" ) );
    return rowMeta;
  }

  @Override public StepDataInterface getStepData() {
    return new GenericStepData();
  }

  Destination getDestination() {
    return getJmsProvider().getDestination( this );
  }

  JMSContext getJmsContext() {
    return getJmsProvider().getContext( this );
  }

  private JmsProvider getJmsProvider() {
    return jmsProviders.stream()
      .filter( prov -> prov.supports( JmsProvider.ConnectionType.valueOf( connectionType ) ) )
      .filter( Objects::nonNull )
      .findFirst()
      .orElseThrow( () -> new RuntimeException( "FIXME" ) );
  }
}
