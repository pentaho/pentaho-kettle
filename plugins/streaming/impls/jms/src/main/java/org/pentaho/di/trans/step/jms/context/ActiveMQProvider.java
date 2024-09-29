/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.apache.activemq.artemis.jms.client.ActiveMQTopic;
import org.pentaho.di.trans.step.jms.JmsDelegate;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.jms.JmsConstants.PKG;
import static org.pentaho.di.trans.step.jms.context.JmsProvider.ConnectionType.ACTIVEMQ;

public class ActiveMQProvider implements JmsProvider {

  private static final String CHAR_ENCODING = "UTF-8";
  public static final String PW_DEBUG_MASK = "********";

  @Override public boolean supports( ConnectionType type ) {
    return type == ACTIVEMQ;
  }

  @SuppressWarnings ( "all" ) // suppressing autocloseable error.  inconsistent w/ other connectionfactory impls.
  @Override public JMSContext getContext( JmsDelegate delegate ) {
    String finalUrl = buildUrl( delegate, false );

    ConnectionFactory factory = new ActiveMQConnectionFactory( finalUrl );
    return factory.createContext( delegate.amqUsername, delegate.amqPassword );
  }

  @Override public String getConnectionDetails( JmsDelegate meta ) {
    return "User Name: "
      + ( isNullOrEmpty( meta.amqUsername ) ? "" : meta.amqUsername )
      + "\nPassword: "
      + PW_DEBUG_MASK
      + "\nURL: " + buildUrl( meta, true );
  }

  /**
   * Build the URL for the ActiveMQ connection based on settings in the JmsDelegate.
   *
   * @param delegate The JmsDelegate containing connection params
   * @return a URL with SSL options appended to it if required by the settings in the JmsDelegate
   */
  String buildUrl( JmsDelegate delegate, boolean debug ) {
    StringBuilder finalUrl = new StringBuilder( delegate.amqUrl.trim() );

    // verify user hit the checkbox on the dialogue *and* also has not specified these values on the URL already
    // end result: default to SSL settings in the URL if present, otherwise use data from the security tab
    if ( delegate.sslEnabled && !finalUrl.toString().contains( "sslEnabled" ) ) {
      appendSslOptions( delegate, finalUrl, debug );
    }

    return finalUrl.toString();
  }

  private void appendSslOptions( JmsDelegate delegate, StringBuilder finalUrl, boolean debug ) {
    StringBuilder urlQuery = new StringBuilder();
    // could already be other params on the URL
    if ( !finalUrl.toString().contains( "?" ) ) {
      urlQuery.append( "?" );
    } else if ( !finalUrl.toString().endsWith( "&" ) ) {
      urlQuery.append( "&" );
    }
    urlQuery.append( "sslEnabled=true" );

    if ( delegate.sslUseDefaultContext ) {
      // per Apache Artemis docs, other SSL params are ignored when this is set to true
      urlQuery.append( "&useDefaultSslContext=true" );
    } else {
      // the keystore is optional; use if client authentication is desired
      if ( !isNullOrEmpty( delegate.sslKeystorePath ) ) {
        // keystore password is always required if keystore present
        Preconditions.checkState( !isNullOrEmpty( delegate.sslKeystorePassword ),
          getString( PKG, "JmsDialog.Security.KeystorePasswordRequired" ) );
        urlQuery.append( "&keyStorePath=" ).append( urlEncode( delegate.sslKeystorePath.trim() ) );
        urlQuery.append( "&keyStorePassword=" ).append( ( debug ? PW_DEBUG_MASK : urlEncode( delegate.sslKeystorePassword.trim() ) ) );
      }
      // truststore always required for a client
      Preconditions.checkState( !isNullOrEmpty( delegate.sslTruststorePath.trim() ),
        getString( PKG, "JmsDialog.Security.TrustStorePathRequired" ) );
      addParam( "trustStorePath", delegate.sslTruststorePath, urlQuery, false );
      // truststore password not required but might be present
      addParam( "trustStorePassword", delegate.sslTruststorePassword, urlQuery, debug );
      addParam( "enabledCipherSuites", delegate.sslCipherSuite, urlQuery, false );
      addParam( "enabledProtocols", delegate.sslContextAlgorithm, urlQuery, false );
      // expect true or false per ActiveMQ docs; no need to decode/translate from y/n/yes/no
      addParam( "verifyHost", delegate.amqSslVerifyHost, urlQuery, false );
      // expect true or false per ActiveMQ docs; no need to decode/translate from y/n/yes/no
      addParam( "trustAll", delegate.amqSslTrustAll, urlQuery, false );
      // used to choose between JDK and OpenSSL if desired; user must provide OpenSSL natively
      addParam( "sslProvider", delegate.amqSslProvider, urlQuery, false );
    }

    finalUrl.append( urlQuery.toString() );
  }

  private void addParam( String paramName, String value, StringBuilder queryBuilder, boolean debug ) {
    String trimmed = value.trim();
    if ( !isNullOrEmpty( trimmed ) ) {
      queryBuilder
        .append( "&" )
        .append( paramName )
        .append( "=" )
        .append( debug ? PW_DEBUG_MASK : urlEncode( trimmed ) );
    }

  }

  private String urlEncode( String s ) {
    try {
      return URLEncoder.encode( s, CHAR_ENCODING );
    } catch ( UnsupportedEncodingException e ) {
      throw new IllegalStateException( e );
    }
  }

  @Override public Destination getDestination( JmsDelegate delegate ) {
    checkNotNull( delegate.destinationName, getString( PKG, "JmsWebsphereMQ.DestinationNameRequired" ) );
    String destName = delegate.destinationName;
    return isQueue( delegate )
      ? new ActiveMQQueue( destName )
      : new ActiveMQTopic( destName );
  }
}
