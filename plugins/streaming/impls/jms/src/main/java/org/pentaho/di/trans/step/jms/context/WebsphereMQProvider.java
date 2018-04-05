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


import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.mq.jms.MQTopic;
import com.ibm.mq.jms.MQTopicConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.step.jms.JmsConstants;
import org.pentaho.di.trans.step.jms.JmsDelegate;

import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.regex.Pattern.compile;
import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.jms.context.JmsProvider.ConnectionType.WEBSPHERE;

public class WebsphereMQProvider implements JmsProvider {

  @Override public boolean supports( ConnectionType type ) {
    return type == WEBSPHERE;
  }

  @Override public JMSContext getContext( JmsDelegate meta, VariableSpace variableSpace ) {

    MQUrlResolver resolver = new MQUrlResolver( meta, variableSpace );

    MQConnectionFactory connFactory = isQueue( meta, variableSpace )
      ? new MQQueueConnectionFactory() : new MQTopicConnectionFactory();

    connFactory.setHostName( resolver.host );
    try {
      connFactory.setPort( resolver.port );
      connFactory.setQueueManager( resolver.queueManager );
      connFactory.setChannel( resolver.channel );
      connFactory.setTransportType( WMQConstants.WMQ_CM_CLIENT );
    } catch ( JMSException e ) {
      throw new RuntimeException( e );
    }
    return connFactory.createContext(
      variableSpace.environmentSubstitute( meta.ibmUsername ),
      variableSpace.environmentSubstitute( meta.ibmPassword ) );

  }

  @Override public Destination getDestination( JmsDelegate meta,
                                               VariableSpace variableSpace ) {
    checkNotNull( meta.destinationName, getString( JmsConstants.PKG, "JmsWebsphereMQ.DestinationNameRequired" ) );
    try {
      String destName = variableSpace.environmentSubstitute( meta.destinationName );
      return isQueue( meta, variableSpace )
        ? new MQQueue( destName )
        : new MQTopic( destName );
    } catch ( JMSException e ) {
      throw new RuntimeException( e );
    }
  }

  static class MQUrlResolver {
    private final JmsDelegate meta;
    private final Pattern pattern;

    private String host = null;
    private String queueManager = "default";
    private int port = 1414; // IBM default
    private String channel = "SYSTEM.DEF.SVRCONN"; // IBM default


    MQUrlResolver( JmsDelegate meta, VariableSpace space ) {
      this.pattern = compile(
        "mq://([\\p{Alnum}\\x2D\\x2E]*)(:(\\p{Digit}*))?/([\\p{Alnum}\\x2E]*)(\\x3F(channel=([^\\s=\\x26]*)))?" );
      this.meta = meta;
      resolve( space );
    }

    void resolve( VariableSpace space ) {
      Matcher matcher = pattern.matcher( space.environmentSubstitute( meta.ibmUrl ).trim() );
      if ( matcher.matches() ) {
        String value;

        host = matcher.group( 1 );
        queueManager = matcher.group( 4 );

        value = matcher.group( 3 );
        if ( value != null ) {
          port = Integer.parseInt( value );
        }

        value = matcher.group( 7 );
        if ( value != null ) {
          channel = value;
        }
      }
    }

  }

}
