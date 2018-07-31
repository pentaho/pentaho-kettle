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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.apache.activemq.artemis.jms.client.ActiveMQTopic;
import org.pentaho.di.trans.step.jms.JmsConstants;
import org.pentaho.di.trans.step.jms.JmsDelegate;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.jms.context.JmsProvider.ConnectionType.ACTIVEMQ;

public class ActiveMQProvider implements JmsProvider {

  private final String urlCharEncoding = "UTF-8";

  @Override public boolean supports( ConnectionType type ) {
    return type == ACTIVEMQ;
  }

  @Override public JMSContext getContext( JmsDelegate delegate ) {
    String finalUrl = buildUrl( delegate );

    ConnectionFactory factory = new ActiveMQConnectionFactory( finalUrl );
    return factory.createContext( delegate.amqUsername, delegate.amqPassword );
  }

  @Override public String getConnectionDetails( JmsDelegate meta )  {
    return "User Name: "
      + ( Strings.isNullOrEmpty( meta.amqUsername ) ? "" : meta.amqUsername )
      + "\nPassword: "
      + ( Strings.isNullOrEmpty( meta.amqPassword ) ? "" : meta.amqPassword )
      + "\nURL: " + buildUrl( meta );
  }

  /**
   * Build the URL for the ActiveMQ connection based on settings in the JmsDelegate.
   *
   * @param delegate The JmsDelegate containing connection params
   * @return a URL with SSL options appended to it if required by the settings in the JmsDelegate
   */
  protected String buildUrl( JmsDelegate delegate ) {
    StringBuilder finalUrl = new StringBuilder( delegate.amqUrl.trim() );

    // verify user hit the checkbox on the dialogue *and* also has not specified these values on the URL already
    // end result: default to SSL settings in the URL if present, otherwise use data from the security tab
    if ( delegate.sslEnabled && !finalUrl.toString().contains( "sslEnabled" ) ) {

      StringBuilder urlQuery = new StringBuilder();
      try {
        // could already be other params on the URL
        if ( !finalUrl.toString().contains( "?" ) ) {
          urlQuery.append( "?" );
        } else if ( !finalUrl.toString().endsWith( "&" ) ) {
          urlQuery.append( "&" );
        }

        urlQuery.append( "sslEnabled=true" );

        // the keystore is optional; use if client authentication is desired
        if ( !Strings.isNullOrEmpty( delegate.sslKeystorePath ) ) {
          // keystore password is always required if keystore present
          Preconditions.checkNotNull( delegate.sslKeystorePassword, "SSL keystore password must be specified if keystore path is specified" );
          urlQuery.append( "&keyStorePath=" ).append( urlEncode( delegate.sslKeystorePath.trim() ) );
          urlQuery.append( "&keyStorePassword=" ).append( urlEncode( delegate.sslKeystorePassword.trim() ) );
        }

        // truststore always required for a client
        urlQuery.append( "&trustStorePath=" ).append( urlEncode( delegate.sslTruststorePath.trim() ) );
        // truststore password not required but might be present
        if ( !Strings.isNullOrEmpty( delegate.sslTruststorePassword ) ) {
          urlQuery.append( "&trustStorePassword=" ).append( urlEncode( delegate.sslTruststorePassword.trim() ) );
        }

        if ( !Strings.isNullOrEmpty( delegate.sslCipherSuite ) ) {
          urlQuery.append( "&enabledCipherSuites=" ).append( urlEncode( delegate.sslCipherSuite.trim() ) );
        }

        if ( !Strings.isNullOrEmpty( delegate.sslContextAlgorithm ) ) {
          urlQuery.append( "&enabledProtocols=" ).append( urlEncode( delegate.sslContextAlgorithm.trim() ) );
        }

        // expect true or false per ActiveMQ docs; no need to decode/translate from y/n/yes/no
        if ( !Strings.isNullOrEmpty( delegate.amqSslVerifyHost ) ) {
          urlQuery.append( "&verifyHost=" ).append( urlEncode( delegate.amqSslVerifyHost.trim() ) );
        }

        // expect true or false per ActiveMQ docs; no need to decode/translate from y/n/yes/no
        if ( !Strings.isNullOrEmpty( delegate.amqSslTrustAll ) ) {
          urlQuery.append( "&trustAll=" ).append( urlEncode( delegate.amqSslTrustAll.trim() ) );
        }

        // useDefaultSslContext not supported directly here; maybe later
        // can be put in URL field by user if necessary

        // used to choose between JDK and OpenSSL if desired; user must provide OpenSSL natively
        if ( !Strings.isNullOrEmpty( delegate.amqSslProvider ) ) {
          urlQuery.append( "&sslProvider=" ).append( urlEncode( delegate.amqSslProvider.trim() ) );
        }

      } catch ( UnsupportedEncodingException e ) {
        throw new RuntimeException( e );
      }

      finalUrl.append( urlQuery.toString() );
    }

    return finalUrl.toString();
  }

  private String urlEncode( String s ) throws UnsupportedEncodingException {
    return URLEncoder.encode( s, urlCharEncoding );
  }

  @Override public Destination getDestination( JmsDelegate delegate ) {
    checkNotNull( delegate.destinationName, getString( JmsConstants.PKG, "JmsWebsphereMQ.DestinationNameRequired" ) );
    String destName = delegate.destinationName;
    return isQueue( delegate )
      ? new ActiveMQQueue( destName )
      : new ActiveMQTopic( destName );
  }
}
