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

package org.pentaho.di.trans.step.jms.context;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.apache.activemq.artemis.jms.client.ActiveMQTopic;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.step.jms.JmsConstants;
import org.pentaho.di.trans.step.jms.JmsDelegate;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.jms.context.JmsProvider.ConnectionType.ACTIVEMQ;

public class ActiveMQProvider implements JmsProvider {
  @Override public boolean supports( ConnectionType type ) {
    return type == ACTIVEMQ;
  }

  @Override public JMSContext getContext( JmsDelegate delegate, VariableSpace variableSpace ) {
    ConnectionFactory factory = new ActiveMQConnectionFactory(
      variableSpace.environmentSubstitute( delegate.amqUrl ).trim() );

    return factory.createContext(
      variableSpace.environmentSubstitute( delegate.amqUsername ),
      variableSpace.environmentSubstitute( delegate.amqPassword ) );

  }

  @Override public Destination getDestination( JmsDelegate delegate, VariableSpace variableSpace ) {
    checkNotNull( delegate.destinationName, getString( JmsConstants.PKG, "JmsWebsphereMQ.DestinationNameRequired" ) );
    String destName = variableSpace.environmentSubstitute( delegate.destinationName );
    return isQueue( delegate, variableSpace )
      ? new ActiveMQQueue( destName )
      : new ActiveMQTopic( destName );
  }
}
