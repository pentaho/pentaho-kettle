/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.trans.step.jms.context;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.apache.activemq.artemis.jms.client.ActiveMQTopic;
import org.junit.Test;
import org.pentaho.di.trans.step.jms.JmsDelegate;

import javax.jms.Destination;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Pins the Artemis client API surface this plugin actually consumes, so that a change of the shipped
 * Artemis artifact is proven to keep the concrete types, the queue/topic distinction and the failure
 * behaviour the JMS steps rely on. Every assertion runs against the real library, never a mock.
 */
public class ActiveMQProviderLibraryGuardTest {

  private static final String DESTINATION_NAME = "aDestination";

  private static JmsDelegate delegate() {
    ActiveMQProvider provider = new ActiveMQProvider();
    return new JmsDelegate( Collections.singletonList( provider ) );
  }

  @Test public void getDestinationReturnsArtemisQueue() {
    ActiveMQProvider provider = new ActiveMQProvider();
    JmsDelegate delegate = delegate();
    delegate.destinationType = JmsProvider.DestinationType.QUEUE.name();
    delegate.destinationName = DESTINATION_NAME;

    Destination destination = provider.getDestination( delegate );

    assertTrue( "expected the Artemis client's own queue type",
      destination instanceof ActiveMQQueue );
    assertEquals( DESTINATION_NAME, ( (ActiveMQQueue) destination ).getQueueName() );
  }

  @Test public void getDestinationReturnsArtemisTopic() {
    ActiveMQProvider provider = new ActiveMQProvider();
    JmsDelegate delegate = delegate();
    delegate.destinationType = JmsProvider.DestinationType.TOPIC.name();
    delegate.destinationName = DESTINATION_NAME;

    Destination destination = provider.getDestination( delegate );

    assertTrue( "expected the Artemis client's own topic type",
      destination instanceof ActiveMQTopic );
    assertEquals( DESTINATION_NAME, ( (ActiveMQTopic) destination ).getTopicName() );
  }

  @Test( expected = NullPointerException.class )
  public void getDestinationRejectsMissingDestinationName() {
    ActiveMQProvider provider = new ActiveMQProvider();
    JmsDelegate delegate = delegate();
    delegate.destinationType = JmsProvider.DestinationType.QUEUE.name();
    delegate.destinationName = null;

    provider.getDestination( delegate );
  }

  @Test( expected = IllegalArgumentException.class )
  public void getDestinationRejectsUnknownDestinationType() {
    ActiveMQProvider provider = new ActiveMQProvider();
    JmsDelegate delegate = delegate();
    delegate.destinationType = "NOT_A_DESTINATION_TYPE";
    delegate.destinationName = DESTINATION_NAME;

    provider.getDestination( delegate );
  }

  /**
   * The connection factory accepts a URL without contacting anything, so this stays offline and fast.
   */
  @Test public void connectionFactoryAcceptsTheUrlTheProviderBuilds() {
    ActiveMQProvider provider = new ActiveMQProvider();
    JmsDelegate delegate = delegate();
    delegate.amqUrl = "tcp://localhost:61616";
    delegate.sslEnabled = true;
    delegate.sslTruststorePath = "/tmp/truststore";
    delegate.sslTruststorePassword = "aPassword";

    String url = provider.buildUrl( delegate, false );
    assertTrue( "SSL options should be appended to the broker URL", url.contains( "sslEnabled=true" ) );

    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory( url );
    assertNotNull( factory );
  }

  /**
   * An unparseable broker URL must be rejected rather than silently producing a factory that can never
   * connect -- the error path the JMS steps surface to the user.
   */
  @Test( timeout = 30000 )
  public void connectionFactoryRejectsAnUnparseableUrl() {
    try {
      new ActiveMQConnectionFactory( "not a broker url at all" );
      fail( "expected the Artemis client to reject a malformed broker URL" );
    } catch ( RuntimeException expected ) {
      assertNotNull( expected );
    }
  }
}
