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


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.step.jms.JmsDelegate;

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;
import static org.pentaho.di.trans.step.jms.context.JmsProvider.ConnectionType.ACTIVEMQ;
import static org.pentaho.di.trans.step.jms.context.JmsProvider.ConnectionType.WEBSPHERE;
import static org.pentaho.di.trans.step.jms.context.JmsProvider.DestinationType.QUEUE;
import static org.pentaho.di.trans.step.jms.context.JmsProvider.DestinationType.TOPIC;

@RunWith ( MockitoJUnitRunner.class )
public class WebsphereMQProviderTest {

  private JmsProvider jmsProvider = new WebsphereMQProvider();
  @Mock private JmsDelegate jmsDelegate;

  VariableSpace variableSpace = new Variables();


  @Test
  public void onlySupportsWebsphere() {
    assertTrue( jmsProvider.supports( WEBSPHERE ) );
    assertFalse( jmsProvider.supports( ACTIVEMQ ) );
  }

  @Test
  public void getQueueDestination() {
    jmsDelegate.destinationType = QUEUE.name();
    jmsDelegate.destinationName = "somename";
    Destination dest = jmsProvider.getDestination( jmsDelegate, variableSpace );
    assertTrue( dest instanceof Queue );
  }

  @Test
  public void getTopicDestination() {
    jmsDelegate.destinationType = TOPIC.name();
    jmsDelegate.destinationName = "somename";
    Destination dest = jmsProvider.getDestination( jmsDelegate, variableSpace );
    assertTrue( dest instanceof Topic );
  }

  @Test
  public void noDestinationNameSetCausesError() {
    jmsDelegate.destinationType = QUEUE.name();
    jmsDelegate.destinationName = null;

    try {
      jmsProvider.getDestination( jmsDelegate, variableSpace );
      fail();
    } catch ( Exception e ) {
      assertTrue( e.getMessage().contains( "Destination name must be set." ) );
    }

  }
}
