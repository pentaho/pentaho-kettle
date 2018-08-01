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


import com.google.common.base.Strings;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.mq.jms.MQTopic;
import com.ibm.mq.jms.MQTopicConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import org.pentaho.di.trans.step.jms.JmsConstants;
import org.pentaho.di.trans.step.jms.JmsDelegate;

import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
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

  @Override public String getConnectionDetails( JmsDelegate meta ) {
    StringBuilder connDetails = new StringBuilder();
    MQUrlResolver resolver = new MQUrlResolver( meta );
    connDetails.append( "Hostname: " ).append( resolver.host );
    connDetails.append( "\nPort: " ).append( resolver.port );
    connDetails.append( "\nChannel: " ).append( resolver.channel );
    connDetails.append( "\nQueueManager: " ).append( resolver.queueManager );
    connDetails.append( "\nUser Name: " ).append( meta.ibmUsername );
    connDetails.append( "\nPassword: " ).append( meta.ibmPassword );

    if ( meta.sslEnabled ) {
      connDetails.append( "\nSSL Enabled: true" );
      connDetails.append( "\nKey Store: " ).append( meta.sslKeystorePath );
      connDetails.append( "\nKey Store Pass: " ).append( meta.sslKeystorePassword );
      connDetails.append( "\nKey Store Type: " ).append( meta.sslKeystoreType );
      connDetails.append( "\nTrust Store: " ).append( meta.sslTruststorePath );
      connDetails.append( "\nTrust Store Pass: " ).append( meta.sslTruststorePassword );
      connDetails.append( "\nTrust Store Type: " ).append( meta.sslTruststoreType );
      connDetails.append( "\nSSL Context Algorithm: " ).append( meta.sslContextAlgorithm );
      connDetails.append( "\nCipher Suite: " ).append( meta.sslCipherSuite );
      connDetails.append( "\nFIPS Required: " ).append( meta.ibmSslFipsRequired );
    }

    return connDetails.toString();
  }

  @Override public JMSContext getContext( JmsDelegate meta ) {

    MQUrlResolver resolver = new MQUrlResolver( meta );

    MQConnectionFactory connFactory = isQueue( meta )
      ? new MQQueueConnectionFactory() : new MQTopicConnectionFactory();

    connFactory.setHostName( resolver.host );

    if ( meta.sslEnabled ) {
      // try to configure SSL settings
      try {
        KeyStore trustStore = KeyStore.getInstance( meta.sslTruststoreType );
        trustStore.load( new FileInputStream( meta.sslTruststorePath ),
          Strings.isNullOrEmpty( meta.sslTruststorePassword ) ? null : meta.sslTruststorePassword.toCharArray() );

        TrustManagerFactory trustManagerFactory =
          TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );

        trustManagerFactory.init( trustStore );

        KeyManagerFactory keyManagerFactory = null;
        // the keystore is optional; use if client authentication is desired
        if ( !Strings.isNullOrEmpty( meta.sslKeystorePath ) ) {
          KeyStore keyStore = KeyStore.getInstance( meta.sslKeystoreType );
          keyStore.load( new FileInputStream( meta.sslKeystorePath ), meta.sslKeystorePassword.toCharArray() );

          keyManagerFactory = KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );
          keyManagerFactory.init( keyStore, meta.sslKeystorePassword.toCharArray() );
        }

        SSLContext sslContext = SSLContext.getInstance( meta.sslContextAlgorithm );
        sslContext.init( ( null == keyManagerFactory ? null : keyManagerFactory.getKeyManagers() ),
          trustManagerFactory.getTrustManagers(), new SecureRandom() );

        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        connFactory.setSSLFipsRequired( meta.ibmSslFipsRequired.toLowerCase().startsWith( "t" )
          || meta.ibmSslFipsRequired.toLowerCase().startsWith( "y" ) );
        connFactory.setSSLSocketFactory( sslSocketFactory );
        connFactory.setSSLCipherSuite( meta.sslCipherSuite );

      } catch ( GeneralSecurityException | IOException e ) {
        throw new RuntimeException( e );
      }
    }

    try {
      connFactory.setPort( resolver.port );
      connFactory.setQueueManager( resolver.queueManager );
      connFactory.setChannel( resolver.channel );
      connFactory.setTransportType( WMQConstants.WMQ_CM_CLIENT );
    } catch ( JMSException e ) {
      throw new RuntimeException( e );
    }
    return connFactory.createContext( meta.ibmUsername, meta.ibmPassword );

  }

  @Override public Destination getDestination( JmsDelegate meta ) {
    checkNotNull( meta.destinationName, getString( JmsConstants.PKG, "JmsWebsphereMQ.DestinationNameRequired" ) );
    try {
      String destName = meta.destinationName;
      return isQueue( meta )
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


    MQUrlResolver( JmsDelegate meta ) {
      this.pattern = compile(
        "mq://([\\p{Alnum}\\x2D\\x2E]*)(:(\\p{Digit}*))?/([\\p{Alnum}\\x2E]*)(\\x3F(channel=([^\\s=\\x26]*)))?" );
      this.meta = meta;
      resolve();
    }

    void resolve() {
      Matcher matcher = pattern.matcher( meta.ibmUrl.trim() );
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
