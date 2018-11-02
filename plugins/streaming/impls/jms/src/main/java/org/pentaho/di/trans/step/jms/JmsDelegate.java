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

import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.serialization.Sensitive;
import org.pentaho.di.trans.step.jms.context.JmsProvider;

import javax.jms.Destination;
import javax.jms.JMSContext;
import java.util.List;

import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.jms.JmsConstants.PKG;
import static org.pentaho.di.trans.step.jms.context.JmsProvider.ConnectionType.ACTIVEMQ;
import static org.pentaho.di.trans.step.jms.context.JmsProvider.DestinationType.QUEUE;


/**
 * Handles creation of JMSContext and JMS Destination via the appropriate {@link JmsProvider} implementation.
 *
 * JMS related connection info is captured here as @Injection attributes so the same attributes can be shared
 * by any Meta implementation requiring Jms connection support.
 */
public class JmsDelegate {

  @Injection ( name = "DESTINATION" ) public String destinationName = "";

  @Injection ( name = "IBMMQ_URL" ) public String ibmUrl = "";

  @Injection ( name = "IBMMQ_USERNAME" ) public String ibmUsername = "";

  @Sensitive @Injection ( name = "IBMMQ_PASSWORD" ) public String ibmPassword = "";

  @Injection ( name = "AMQ_URL" ) public String amqUrl = "";

  @Injection ( name = "AMQ_USERNAME" ) public String amqUsername = "";

  @Sensitive @Injection ( name = "AMQ_PASSWORD" ) public String amqPassword = "";

  @Injection( name = "CONNECTION_TYPE" ) public String connectionType = ACTIVEMQ.name();

  @Injection ( name = "DESTINATION_TYPE" ) public String destinationType = QUEUE.name();

  @Injection ( name = "RECEIVE_TIMEOUT" ) public String receiveTimeout = "0";

  @Injection ( name = "MESSAGE_FIELD_NAME" ) public String messageField = "message";

  @Injection ( name = "DESTINATION_FIELD_NAME" ) public String destinationField = "destination";

  @Injection ( name = "SSL_ENABLED", group = "SSL_GROUP" ) public boolean sslEnabled = false;

  @Injection ( name = "SSL_KEYSTORE_PATH", group = "SSL_GROUP" ) public String sslKeystorePath = "";

  @Injection ( name = "SSL_KEYSTORE_TYPE", group = "SSL_GROUP" ) public String sslKeystoreType = "";

  @Sensitive
  @Injection ( name = "SSL_KEYSTORE_PASSWORD", group = "SSL_GROUP" ) public String sslKeystorePassword = "";

  @Injection ( name = "SSL_TRUSTSTORE_PATH", group = "SSL_GROUP" ) public String sslTruststorePath = "";

  @Injection ( name = "SSL_TRUSTSTORE_TYPE", group = "SSL_GROUP" ) public String sslTruststoreType = "";

  @Sensitive
  @Injection ( name = "SSL_TRUSTSTORE_PASSWORD", group = "SSL_GROUP" ) public String sslTruststorePassword = "";

  @Injection ( name = "SSL_CONTEXT_ALGORITHM", group = "SSL_GROUP" ) public String sslContextAlgorithm = "";

  @Injection ( name = "SSL_CIPHERSUITE", group = "SSL_GROUP" ) public String sslCipherSuite = "";

  @Injection ( name = "IBM_SSL_FIPSREQUIRED", group = "SSL_GROUP" ) public String ibmSslFipsRequired = "";

  @Injection ( name = "AMQ_SSL_PROVIDER", group = "SSL_GROUP" ) public String amqSslProvider = "";

  @Injection ( name = "AMQ_SSL_VERIFY_HOST", group = "SSL_GROUP" ) public String amqSslVerifyHost = "";

  @Injection ( name = "AMQ_SSL_TRUST_ALL", group = "SSL_GROUP" ) public String amqSslTrustAll = "";

  private final List<JmsProvider> jmsProviders;

  public JmsDelegate( List<JmsProvider> jmsProviders ) {
    super();
    this.jmsProviders = jmsProviders;
  }

  public JmsDelegate( JmsDelegate orig ) {
    super();
    this.jmsProviders = orig.jmsProviders;
    this.destinationName = orig.destinationName;
    this.ibmUrl = orig.ibmUrl;
    this.ibmUsername  = orig.ibmUsername;
    this.ibmPassword = orig.ibmPassword;
    this.amqUrl = orig.amqUrl;
    this.amqUsername = orig.amqUsername;
    this.amqPassword = orig.amqPassword;
    this.connectionType = orig.connectionType;
    this.destinationType = orig.destinationType;
    this.receiveTimeout = orig.receiveTimeout;
    this.messageField = orig.messageField;
    this.destinationField = orig.destinationField;
    this.sslEnabled = orig.sslEnabled;
    this.sslKeystorePath = orig.sslKeystorePath;
    this.sslKeystoreType = orig.sslKeystoreType;
    this.sslKeystorePassword = orig.sslKeystorePassword;
    this.sslTruststorePath = orig.sslTruststorePath;
    this.sslTruststoreType = orig.sslTruststoreType;
    this.sslTruststorePassword = orig.sslTruststorePassword;
    this.sslContextAlgorithm = orig.sslContextAlgorithm;
    this.sslCipherSuite = orig.sslCipherSuite;
    this.ibmSslFipsRequired = orig.ibmSslFipsRequired;
    this.amqSslProvider = orig.amqSslProvider;
    this.amqSslVerifyHost = orig.amqSslVerifyHost;
    this.amqSslTrustAll = orig.amqSslTrustAll;
  }

  Destination getDestination( ) {
    return getJmsProvider().getDestination( this );
  }

  JMSContext getJmsContext() {
    return getJmsProvider().getContext( this );
  }

  JmsProvider getJmsProvider() {
    return jmsProviders.stream()
      .filter( prov -> prov.supports( JmsProvider.ConnectionType.valueOf( connectionType ) ) )
      .findFirst()
      .orElseThrow( () -> new RuntimeException( getString( PKG, "JmsDelegate.UnsupportedConnectionType" ) ) );
  }

  public String getConnectionType() {
    return connectionType;
  }

  public String getConnectionUrl() {
    return connectionType.equals( ACTIVEMQ ) ? amqUrl : ibmUrl;
  }

  public String getDestinationName() {
    return destinationName;
  }

  public String getDestinationType() {
    return destinationType;
  }

  public String getReceiveTimeout() {
    return receiveTimeout;
  }

  /**
   * Creates a rowMeta for output field names
   */
  RowMetaInterface getRowMeta() {
    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( messageField ) );
    rowMeta.addValueMeta( new ValueMetaString( destinationField ) );
    return rowMeta;
  }
}
