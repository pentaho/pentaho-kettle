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

import org.apache.activemq.artemis.junit.EmbeddedJMSResource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.jms.JmsConsumerMeta;
import org.pentaho.di.trans.step.jms.JmsDelegate;
import org.pentaho.di.trans.step.jms.JmsProducerMeta;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class ActiveMQProviderTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Mock LogChannelInterfaceFactory logChannelFactory;
  @Mock LogChannelInterface logChannel;

  @Rule public EmbeddedJMSResource resource = new EmbeddedJMSResource( 0 );

  private static final String AMQ_URL_BASE = "tcp://AMQ_URL_BASE:1234";

  private static final String AMQ_USERNAME_VAL = "AMQ_USERNAME_VAL";
  private static final String AMQ_PASSWORD_VAL = "AMQ_PASSWORD_VAL";
  
  private static final String TRUST_STORE_PATH_VAL = "TRUST_STORE_PATH_VAL";
  private static final String TRUST_STORE_PASS_VAL = "TRUST_STORE_PASS_VAL";
  private static final String KEY_STORE_PATH_VAL = "KEY_STORE_PATH_VAL";
  private static final String KEY_STORE_PASS_VAL = "KEY_STORE_PASS_VAL";
  private static final String ENABLED_CIPHER_SUITES_VAL = "ENABLED_CIPHER_SUITES_VAL";
  private static final String ENABLED_PROTOCOLS_VAL = "ENABLED_PROTOCOLS_VAL";
  private static final String VERIFY_HOST_VAL = "VERIFY_HOST_VAL";
  private static final String TRUST_ALL_VAL = "TRUST_ALL_VAL";
  private static final String SSL_PROVIDER_VAL= "SSL_PROVIDER_VAL";

  @BeforeClass
  public static void setupClass() throws Exception {
    StepPluginType.getInstance().handlePluginAnnotation(
      JmsProducerMeta.class,
      JmsProducerMeta.class.getAnnotation( org.pentaho.di.core.annotations.Step.class ),
      Collections.emptyList(), false, null );
    StepPluginType.getInstance().handlePluginAnnotation(
      JmsConsumerMeta.class,
      JmsConsumerMeta.class.getAnnotation( org.pentaho.di.core.annotations.Step.class ),
      Collections.emptyList(), false, null );
  }

  @Before
  public void setUp() {
    KettleLogStore.setLogChannelInterfaceFactory( logChannelFactory );
    doReturn( LogLevel.BASIC ).when( logChannel ).getLogLevel();
    lenient().when( logChannelFactory.create( any(), any() ) ).thenReturn( logChannel );
    lenient().when( logChannelFactory.create( any() ) ).thenReturn( logChannel );
  }

  @Test public void testFullCircle() throws KettleException, InterruptedException, TimeoutException,
    ExecutionException {
    TransMeta consumerMeta = new TransMeta( getClass().getResource( "/amq-consumer.ktr" ).getPath() );
    Trans consumerTrans = new Trans( consumerMeta );
    consumerTrans.prepareExecution( new String[] {} );
    consumerTrans.startThreads();

    TransMeta producerMeta = new TransMeta( getClass().getResource( "/amq-producer.ktr" ).getPath() );
    Trans producerTrans = new Trans( producerMeta );
    producerTrans.prepareExecution( new String[] {} );
    producerTrans.startThreads();
    producerTrans.waitUntilFinished();

    Future<?> future = Executors.newSingleThreadExecutor().submit( () -> {
      while ( true ) {
        if ( consumerTrans.getSteps().get( 0 ).step.getLinesWritten() == 10 ) {
          break;
        }
      }
    } );
    future.get( 5, TimeUnit.SECONDS );
    consumerTrans.safeStop();
    assertEquals( 10, consumerTrans.getSteps().get( 0 ).step.getLinesWritten() );
  }

  /**
   * Verifies URI builder works with SSL off (ignore any values in the table)
   */
  @Test public void testUrlBuildSslOptionsNoSsl() {

    ActiveMQProvider provider = new ActiveMQProvider();
    JmsDelegate delegate = new JmsDelegate( Collections.singletonList( provider ) );

    delegate.amqUrl = AMQ_URL_BASE;
    delegate.sslEnabled = false;
    delegate.sslTruststorePath = TRUST_STORE_PATH_VAL;
    delegate.sslTruststorePassword = TRUST_STORE_PASS_VAL;

    String urlString = provider.buildUrl( delegate, false );

    try {
      URI url = new URI( urlString );
    } catch ( URISyntaxException e ) {
      fail( e.getMessage() );
    }

    assertFalse( "SSL disabled; should ignore params", urlString.contains( TRUST_STORE_PATH_VAL ) );

    assertTrue( "URL base incorrect", urlString.compareTo( AMQ_URL_BASE ) == 0 );
  }

  /**
   * Verifies URI builder works with SSL on and params already exist on the URI
   */
  @Test public void testUrlBuildSslOptionsParamsExist() {

    ActiveMQProvider provider = new ActiveMQProvider();
    JmsDelegate delegate = new JmsDelegate( Collections.singletonList( provider ) );

    delegate.amqUrl = AMQ_URL_BASE + "?foo=bar";
    delegate.sslEnabled = true;
    delegate.sslTruststorePath = TRUST_STORE_PATH_VAL;
    delegate.sslTruststorePassword = TRUST_STORE_PASS_VAL;

    String urlString = provider.buildUrl( delegate, false );

    try {
      URI url = new URI( urlString );
    } catch ( URISyntaxException e ) {
      fail( e.getMessage() );
    }

    assertTrue( "Missing trust store path", urlString.contains( "trustStorePath=" + TRUST_STORE_PATH_VAL ) );
    assertTrue( "Missing trust store password", urlString.contains( "trustStorePassword=" + TRUST_STORE_PASS_VAL ) );

    assertTrue( "URL base incorrect", urlString.startsWith( AMQ_URL_BASE + "?" ) );

    delegate.amqUrl += ";";

    urlString = provider.buildUrl( delegate,false );

    try {
      URI url = new URI( urlString );
    } catch ( URISyntaxException e ) {
      fail( e.getMessage() );
    }

    assertTrue( "Missing trust store path", urlString.contains( "trustStorePath=" + TRUST_STORE_PATH_VAL ) );
    assertTrue( "Missing trust store password", urlString.contains( "trustStorePassword=" + TRUST_STORE_PASS_VAL ) );

    assertTrue( "URL base incorrect", urlString.startsWith( AMQ_URL_BASE + "?" ) );
  }

  /**
   * Verifies URI builder works with all possible SSL options filled out
   * Note: not a realistic scenario but ensures code coverage
   */
  @Test public void testUrlBuildSslOptionsAllParams() {

    ActiveMQProvider provider = new ActiveMQProvider();
    JmsDelegate delegate = new JmsDelegate( Collections.singletonList( provider ) );

    delegate.amqUrl = AMQ_URL_BASE;
    delegate.sslEnabled = true;
    delegate.sslTruststorePath = TRUST_STORE_PATH_VAL;
    delegate.sslTruststorePassword = TRUST_STORE_PASS_VAL;

    delegate.sslKeystorePath = KEY_STORE_PATH_VAL;
    delegate.sslKeystorePassword = KEY_STORE_PASS_VAL;

    delegate.sslCipherSuite = ENABLED_CIPHER_SUITES_VAL;
    delegate.sslContextAlgorithm = ENABLED_PROTOCOLS_VAL;
    delegate.amqSslVerifyHost = VERIFY_HOST_VAL;
    delegate.amqSslTrustAll = TRUST_ALL_VAL;
    delegate.amqSslProvider = SSL_PROVIDER_VAL;
    delegate.sslUseDefaultContext = false;

    String urlString = provider.buildUrl( delegate, false );

    try {
      URI url = new URI( urlString );
    } catch ( URISyntaxException e ) {
      fail( e.getMessage() );
    }

    assertTrue( "Missing trust store path", urlString.contains( "trustStorePath=" + TRUST_STORE_PATH_VAL ) );
    assertTrue( "Missing trust store password", urlString.contains( "trustStorePassword=" + TRUST_STORE_PASS_VAL ) );

    assertTrue( "Missing key store path", urlString.contains( "keyStorePath=" + KEY_STORE_PATH_VAL ) );
    assertTrue( "Missing key store password", urlString.contains( "keyStorePassword=" + KEY_STORE_PASS_VAL ) );

    assertTrue( "Missing cipher suite", urlString.contains( "enabledCipherSuites=" + ENABLED_CIPHER_SUITES_VAL ) );
    assertTrue( "Missing protocols", urlString.contains( "enabledProtocols=" + ENABLED_PROTOCOLS_VAL ) );
    assertTrue( "Missing verify host", urlString.contains( "verifyHost=" + VERIFY_HOST_VAL ) );
    assertTrue( "Missing trust all", urlString.contains( "trustAll=" + TRUST_ALL_VAL ) );
    assertTrue( "Missing ssl provider", urlString.contains( "sslProvider=" + SSL_PROVIDER_VAL ) );

    assertTrue( "URL base incorrect", urlString.startsWith( AMQ_URL_BASE + "?" ) );
  }

  /**
   * Verifies URI builder works when user chooses Use Default SSL Context
   */
  @Test public void testUseDefaultSslContext() {

    ActiveMQProvider provider = new ActiveMQProvider();
    JmsDelegate delegate = new JmsDelegate( Collections.singletonList( provider ) );

    delegate.amqUrl = AMQ_URL_BASE;
    delegate.sslEnabled = true;
    delegate.sslTruststorePath = TRUST_STORE_PATH_VAL;
    delegate.sslTruststorePassword = TRUST_STORE_PASS_VAL;

    delegate.sslKeystorePath = KEY_STORE_PATH_VAL;
    delegate.sslKeystorePassword = KEY_STORE_PASS_VAL;

    delegate.sslCipherSuite = ENABLED_CIPHER_SUITES_VAL;
    delegate.sslContextAlgorithm = ENABLED_PROTOCOLS_VAL;
    delegate.amqSslVerifyHost = VERIFY_HOST_VAL;
    delegate.amqSslTrustAll = TRUST_ALL_VAL;
    delegate.amqSslProvider = SSL_PROVIDER_VAL;
    delegate.sslUseDefaultContext = true;

    String urlString = provider.buildUrl( delegate, false );

    try {
      URI url = new URI( urlString );
    } catch ( URISyntaxException e ) {
      fail( e.getMessage() );
    }

    assertFalse( "Should not include trust store path", urlString.contains( "trustStorePath=" + TRUST_STORE_PATH_VAL ) );
    assertFalse( "Should not include trust store password", urlString.contains( "trustStorePassword=" + TRUST_STORE_PASS_VAL ) );

    assertFalse( "Should not include key store path", urlString.contains( "keyStorePath=" + KEY_STORE_PATH_VAL ) );
    assertFalse( "Should not include key store password", urlString.contains( "keyStorePassword=" + KEY_STORE_PASS_VAL ) );

    assertFalse( "Should not include cipher suite", urlString.contains( "enabledCipherSuites=" + ENABLED_CIPHER_SUITES_VAL ) );
    assertFalse( "Should not include protocols", urlString.contains( "enabledProtocols=" + ENABLED_PROTOCOLS_VAL ) );
    assertFalse( "Should not include verify host", urlString.contains( "verifyHost=" + VERIFY_HOST_VAL ) );
    assertFalse( "Should not include trust all", urlString.contains( "trustAll=" + TRUST_ALL_VAL ) );
    assertFalse( "Should not include ssl provider", urlString.contains( "sslProvider=" + SSL_PROVIDER_VAL ) );
    
    assertTrue( "Missing Use default SSL context", urlString.contains( "useDefaultSslContext=true" ) );

    assertTrue( "URL base incorrect", urlString.startsWith( AMQ_URL_BASE + "?" ) );
  }

  /**
   * Verifies getConnectionParams works as expected; should return the same URL used to connect.
   */
  @Test public void testGetConnectionParams() {
    ActiveMQProvider provider = new ActiveMQProvider();
    JmsDelegate delegate = new JmsDelegate( Collections.singletonList( provider ) );

    delegate.amqUrl = AMQ_URL_BASE;
    delegate.amqUsername = AMQ_USERNAME_VAL;
    delegate.amqPassword = AMQ_PASSWORD_VAL;

    delegate.sslEnabled = true;
    delegate.sslTruststorePath = TRUST_STORE_PATH_VAL;
    delegate.sslTruststorePassword = TRUST_STORE_PASS_VAL;

    delegate.sslKeystorePath = KEY_STORE_PATH_VAL;
    delegate.sslKeystorePassword = KEY_STORE_PASS_VAL;

    delegate.sslCipherSuite = ENABLED_CIPHER_SUITES_VAL;
    delegate.sslContextAlgorithm = ENABLED_PROTOCOLS_VAL;
    delegate.amqSslVerifyHost = VERIFY_HOST_VAL;
    delegate.amqSslTrustAll = TRUST_ALL_VAL;
    delegate.amqSslProvider = SSL_PROVIDER_VAL;
    String PASSWORD_MASK = "********";

    String urlString = provider.buildUrl( delegate, false );
    String paramString = provider.getConnectionDetails( delegate );

    try {
      URI url = new URI( urlString );
    } catch ( URISyntaxException e ) {
      fail( e.getMessage() );
    }

    assertTrue( "Missing trust store path", urlString.contains( "trustStorePath=" + TRUST_STORE_PATH_VAL ) );
    assertTrue( "Missing trust store password", urlString.contains( "trustStorePassword=" + TRUST_STORE_PASS_VAL ) );

    assertTrue( "Missing key store path", urlString.contains( "keyStorePath=" + KEY_STORE_PATH_VAL ) );
    assertTrue( "Missing key store password", urlString.contains( "keyStorePassword=" + KEY_STORE_PASS_VAL ) );

    assertTrue( "Missing cipher suite", urlString.contains( "enabledCipherSuites=" + ENABLED_CIPHER_SUITES_VAL ) );
    assertTrue( "Missing protocols", urlString.contains( "enabledProtocols=" + ENABLED_PROTOCOLS_VAL ) );
    assertTrue( "Missing verify host", urlString.contains( "verifyHost=" + VERIFY_HOST_VAL ) );
    assertTrue( "Missing trust all", urlString.contains( "trustAll=" + TRUST_ALL_VAL ) );
    assertTrue( "Missing ssl provider", urlString.contains( "sslProvider=" + SSL_PROVIDER_VAL ) );

    assertTrue( "URL base incorrect", urlString.startsWith( AMQ_URL_BASE + "?" ) );

    assertTrue( "Connection params missing URL",
      paramString.contains( "URL: " + urlString.replaceFirst( AMQ_PASSWORD_VAL, PASSWORD_MASK )
        .replaceFirst( TRUST_STORE_PASS_VAL, PASSWORD_MASK )
        .replaceFirst( KEY_STORE_PASS_VAL, PASSWORD_MASK ) ) );
    assertTrue( "Connection params missing user name", paramString.contains( "User Name: " + AMQ_USERNAME_VAL ) );
    assertTrue( "Connection params missing password", paramString.contains( "Password: " + PASSWORD_MASK ) );
  }
}
