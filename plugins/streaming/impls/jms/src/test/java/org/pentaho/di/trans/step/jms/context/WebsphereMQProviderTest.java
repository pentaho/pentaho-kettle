/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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
import org.mockito.junit.MockitoJUnitRunner;
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

@RunWith ( MockitoJUnitRunner.StrictStubs.class )
public class WebsphereMQProviderTest {


  private static final String HOST_NAME_VAL = "HOSTNAMEVAL";
  private static final String PORT_VAL = "1234";
  private static final String QUEUE_MANAGER_VAL = "QUEUEMANAGERVAL";
  private static final String CHANNEL_VAL = "CHANNELVAL";
  private static final String IBM_URL_BASE
    = "mq://" + HOST_NAME_VAL + ":" + PORT_VAL + "/" + QUEUE_MANAGER_VAL + "?channel=" + CHANNEL_VAL;

  private static final String TRUST_STORE_PATH_VAL = "TRUST_STORE_PATH_VAL";
  private static final String TRUST_STORE_PASS_VAL = "TRUST_STORE_PASS_VAL";
  private static final String TRUST_STORE_TYPE_VAL = "TRUST_STORE_TYPE_VAL";
  private static final String KEY_STORE_PATH_VAL = "KEY_STORE_PATH_VAL";
  private static final String KEY_STORE_PASS_VAL = "KEY_STORE_PASS_VAL";
  private static final String KEY_STORE_TYPE_VAL = "KEY_STORE_TPYE_VAL";
  private static final String ENABLED_CIPHER_SUITES_VAL = "ENABLED_CIPHER_SUITES_VAL";
  private static final String ENABLED_PROTOCOLS_VAL = "ENABLED_PROTOCOLS_VAL";
  private static final String VERIFY_HOST_VAL = "VERIFY_HOST_VAL";
  private static final String TRUST_ALL_VAL = "TRUST_ALL_VAL";
  private static final String SSL_PROVIDER_VAL= "SSL_PROVIDER_VAL";
  private static final String FIPS_REQUIRED_VAL = "FIPS_REQUIRED_VAL";
  private static final String IBM_USERNAME_VAL = "IBM_USERNAME_VAL";
  private static final String IBM_PASSWORD_VAL = "IBM_PASSWORD_VAL";
  private static final String PASSWORD_MASK = "********";
  private static final boolean USE_DEFAULT_SSL_CONTEXT_VAL = true;

  private JmsProvider jmsProvider = new WebsphereMQProvider();
  @Mock private JmsDelegate jmsDelegate;

  @Test
  public void onlySupportsWebsphere() {
    assertTrue( jmsProvider.supports( WEBSPHERE ) );
    assertFalse( jmsProvider.supports( ACTIVEMQ ) );
  }

  @Test
  public void getQueueDestination() {
    jmsDelegate.destinationType = QUEUE.name();
    jmsDelegate.destinationName = "somename";
    Destination dest = jmsProvider.getDestination( jmsDelegate );
    assertTrue( dest instanceof Queue );
  }

  @Test
  public void getTopicDestination() {
    jmsDelegate.destinationType = TOPIC.name();
    jmsDelegate.destinationName = "somename";
    Destination dest = jmsProvider.getDestination( jmsDelegate );
    assertTrue( dest instanceof Topic );
  }

  @Test
  public void noDestinationNameSetCausesError() {
    jmsDelegate.destinationType = QUEUE.name();
    jmsDelegate.destinationName = null;

    try {
      jmsProvider.getDestination( jmsDelegate );
      fail();
    } catch ( Exception e ) {
      assertTrue( e.getMessage().contains( "Destination name must be set." ) );
    }
  }

  @Test
  public void getConnectionParams() {
    jmsDelegate.ibmUrl = IBM_URL_BASE;
    jmsDelegate.ibmUsername = IBM_USERNAME_VAL;
    jmsDelegate.ibmPassword = IBM_PASSWORD_VAL;

    jmsDelegate.sslEnabled = true;

    jmsDelegate.sslTruststorePath = TRUST_STORE_PATH_VAL;
    jmsDelegate.sslTruststorePassword = TRUST_STORE_PASS_VAL;
    jmsDelegate.sslTruststoreType = TRUST_STORE_TYPE_VAL;

    jmsDelegate.sslKeystorePath = KEY_STORE_PATH_VAL;
    jmsDelegate.sslKeystorePassword = KEY_STORE_PASS_VAL;
    jmsDelegate.sslKeystoreType = KEY_STORE_TYPE_VAL;

    jmsDelegate.sslCipherSuite = ENABLED_CIPHER_SUITES_VAL;
    jmsDelegate.sslContextAlgorithm = ENABLED_PROTOCOLS_VAL;
    jmsDelegate.ibmSslFipsRequired = FIPS_REQUIRED_VAL;
    jmsDelegate.sslUseDefaultContext = USE_DEFAULT_SSL_CONTEXT_VAL;

    String debugString = jmsProvider.getConnectionDetails( jmsDelegate );

    assertTrue( "Missing trust store path", debugString.contains( "Trust Store: " + TRUST_STORE_PATH_VAL ) );
    assertTrue( "Missing trust store password", debugString.contains( "Trust Store Pass: " + PASSWORD_MASK ) );
    assertTrue( "Missing trust store type", debugString.contains( "Trust Store Type: " + TRUST_STORE_TYPE_VAL ) );

    assertTrue( "Missing key store path", debugString.contains( "Key Store: " + KEY_STORE_PATH_VAL ) );
    assertTrue( "Missing key store password", debugString.contains( "Key Store Pass: " + PASSWORD_MASK ) );
    assertTrue( "Missing key store type", debugString.contains( "Key Store Type: " + KEY_STORE_TYPE_VAL ) );

    assertTrue( "Missing cipher suite", debugString.contains( "Cipher Suite: " + ENABLED_CIPHER_SUITES_VAL ) );
    assertTrue( "Missing protocols", debugString.contains( "SSL Context Algorithm: " + ENABLED_PROTOCOLS_VAL ) );

    assertTrue( "Missing host name", debugString.contains( "Hostname: " + HOST_NAME_VAL ) );
    assertTrue( "Missing port", debugString.contains( "Port: " + PORT_VAL ) );
    assertTrue( "Missing channel", debugString.contains( "Channel: " + CHANNEL_VAL ) );
    assertTrue( "Missing queue manager", debugString.contains( "QueueManager: " + QUEUE_MANAGER_VAL ) );
    assertTrue( "Missing username", debugString.contains( "User Name: " + IBM_USERNAME_VAL ) );
    assertTrue( "Missing password", debugString.contains( "Password: " + PASSWORD_MASK ) );
    assertTrue( "Missing use default SSL context",
      debugString.contains( "Use Default SSL Context:" + USE_DEFAULT_SSL_CONTEXT_VAL ) );


  }
}

