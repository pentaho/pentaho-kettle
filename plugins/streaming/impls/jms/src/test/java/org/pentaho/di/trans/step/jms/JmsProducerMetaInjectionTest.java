/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.trans.step.jms;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JmsProducerMetaInjectionTest extends BaseMetadataInjectionTest<JmsProducerMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new JmsProducerMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "DELIVERY_DELAY", () -> meta.getDeliveryDelay() );
    check( "DELIVERY_MODE", () -> meta.getDeliveryMode() );
    check( "DISABLE_MESSAGE_TIMESTAMP", () -> meta.getDisableMessageTimestamp() );
    check( "FIELD_TO_SEND", () -> meta.getFieldToSend() );
    check( "JMS_CORRELATION_ID", () -> meta.getJmsCorrelationId() );
    check( "JMS_TYPE", () -> meta.getJmsType() );
    check( "PROPERTY_NAMES", () -> meta.propertyNames );
    check( "PROPERTY_VALUES", () -> meta.propertyValues );
    check( "PRIORITY", () -> meta.getPriority() );
    check( "TIME_TO_LIVE", () -> meta.getTimeToLive() );

    check( "AMQ_PASSWORD", () -> meta.jmsDelegate.amqPassword );
    check( "AMQ_SSL_PROVIDER", () -> meta.jmsDelegate.amqSslProvider );
    check( "AMQ_SSL_TRUST_ALL", () -> meta.jmsDelegate.amqSslTrustAll );
    check( "AMQ_SSL_VERIFY_HOST", () -> meta.jmsDelegate.amqSslVerifyHost );
    check( "AMQ_URL", () -> meta.jmsDelegate.amqUrl );
    check( "AMQ_USERNAME", () -> meta.jmsDelegate.amqUsername );
    check( "CONNECTION_TYPE", () -> meta.jmsDelegate.connectionType );
    check( "DESTINATION", () -> meta.jmsDelegate.destinationName );
    check( "DESTINATION_TYPE", () -> meta.jmsDelegate.destinationType );
    check( "DISABLE_MESSAGE_ID", () -> meta.getDisableMessageId() );
    check( "IBMMQ_USERNAME", () -> meta.jmsDelegate.ibmUsername );
    check( "IBMMQ_URL", () -> meta.jmsDelegate.ibmUrl );
    check( "IBMMQ_PASSWORD", () -> meta.jmsDelegate.ibmPassword );
    check( "IBM_SSL_FIPSREQUIRED", () -> meta.jmsDelegate.ibmSslFipsRequired );
    check( "SSL_CIPHERSUITE", () -> meta.jmsDelegate.sslCipherSuite );
    check( "SSL_ENABLED", () -> meta.jmsDelegate.sslEnabled );
    check( "SSL_KEYSTORE_PASSWORD", () -> meta.jmsDelegate.sslKeystorePassword );
    check( "SSL_KEYSTORE_PATH", () -> meta.jmsDelegate.sslKeystorePath );
    check( "SSL_KEYSTORE_TYPE", () -> meta.jmsDelegate.sslKeystoreType );
    check( "SSL_CONTEXT_ALGORITHM", () -> meta.jmsDelegate.sslContextAlgorithm );
    check( "SSL_TRUSTSTORE_PASSWORD", () -> meta.jmsDelegate.sslTruststorePassword );
    check( "SSL_TRUSTSTORE_PATH", () -> meta.jmsDelegate.sslTruststorePath );
    check( "SSL_TRUSTSTORE_TYPE", () -> meta.jmsDelegate.sslTruststoreType );
    check( "SSL_USE_DEFAULT_CONTEXT", () -> meta.jmsDelegate.sslUseDefaultContext );
  }

}
