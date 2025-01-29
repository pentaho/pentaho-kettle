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

public class JmsConsumerMetaInjectionTest extends BaseMetadataInjectionTest<JmsConsumerMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new JmsConsumerMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "PARALLELISM", () -> meta.getParallelism() );
    check( "MESSAGE_ID", () -> meta.messageId );
    check( "RECEIVE_TIMEOUT", () -> meta.getReceiveTimeout() );
    check( "MESSAGE_FIELD_NAME", () -> meta.messageField );
    check( "NUM_MESSAGES", () -> meta.getBatchSize() );
    check( "TRANSFORMATION_PATH", () -> meta.getTransformationPath() );
    check( "JMS_TIMESTAMP", () -> meta.jmsTimestamp );
    check( "PREFETCH_COUNT", () -> meta.getPrefetchCount() );
    check( "DESTINATION_FIELD_NAME", () -> meta.destinationField );
    check( "DURATION", () -> meta.getBatchDuration() );
    check( "JMS_REDELIVERED", () -> meta.jmsRedelivered );
    check( "SUB_STEP", () -> meta.getSubStep() );

    check( "AMQ_PASSWORD", () -> meta.jmsDelegate.amqPassword );
    check( "AMQ_SSL_PROVIDER", () -> meta.jmsDelegate.amqSslProvider );
    check( "AMQ_SSL_TRUST_ALL", () -> meta.jmsDelegate.amqSslTrustAll );
    check( "AMQ_SSL_VERIFY_HOST", () -> meta.jmsDelegate.amqSslVerifyHost );
    check( "AMQ_URL", () -> meta.jmsDelegate.amqUrl );
    check( "AMQ_USERNAME", () -> meta.jmsDelegate.amqUsername );
    check( "CONNECTION_TYPE", () -> meta.jmsDelegate.connectionType );
    check( "DESTINATION", () -> meta.jmsDelegate.destinationName );
    check( "DESTINATION_TYPE", () -> meta.jmsDelegate.destinationType );
    //check( "DISABLE_MESSAGE_ID", () -> meta.getDisableMessageId() );
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
