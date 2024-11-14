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


package org.pentaho.di.cluster;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.pentaho.di.core.logging.LogChannel;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Encapsulates the Apache commons HTTP connection manager with a singleton. We can use this to limit the number of open
 * connections to slave servers.
 *
 * @author matt
 */
public class SlaveConnectionManager {

  private static final String SSL = "SSL";
  private static final String KEYSTORE_SYSTEM_PROPERTY = "javax.net.ssl.keyStore";

  private static SlaveConnectionManager slaveConnectionManager;

  private PoolingHttpClientConnectionManager manager;

  private SlaveConnectionManager() {
    if ( needToInitializeSSLContext() ) {
      try {
        SSLContext context = SSLContext.getInstance( SSL );
        context.init( new KeyManager[ 0 ], new X509TrustManager[] { getDefaultTrustManager() }, new SecureRandom() );
        SSLContext.setDefault( context );
      } catch ( Exception e ) {
        new LogChannel( "SlaveConnectionManager" )
          .logDebug( "Default SSL context hasn't been initialized.\n" + e.getMessage() );
      }
    }
    manager = new PoolingHttpClientConnectionManager();
    manager.setDefaultMaxPerRoute( 100 );
    manager.setMaxTotal( 200 );
  }

  private static boolean needToInitializeSSLContext() {
    return System.getProperty( KEYSTORE_SYSTEM_PROPERTY ) == null;
  }

  public static SlaveConnectionManager getInstance() {
    if ( slaveConnectionManager == null ) {
      slaveConnectionManager = new SlaveConnectionManager();
    }
    return slaveConnectionManager;
  }

  public HttpClient createHttpClient() {
    return HttpClients.custom().setConnectionManager( manager )
      .build();
  }

  public HttpClient createHttpClient( String user, String password ) {
    CredentialsProvider provider = new BasicCredentialsProvider();
    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials( user, password );
    provider.setCredentials( AuthScope.ANY, credentials );

    return
      HttpClientBuilder
        .create()
        .setDefaultCredentialsProvider( provider )
        .setConnectionManager( manager )
        .build();
  }

  public HttpClient createHttpClient( String user, String password,
                                               String proxyHost, int proxyPort, AuthScope authScope ) {
    HttpHost httpHost = new HttpHost( proxyHost, proxyPort );

    RequestConfig requestConfig = RequestConfig.custom()
      .setProxy( httpHost )
      .build();

    CredentialsProvider provider = new BasicCredentialsProvider();
    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials( user, password );
    provider.setCredentials( authScope, credentials );

    return
      HttpClientBuilder
        .create()
        .setDefaultCredentialsProvider( provider )
        .setDefaultRequestConfig( requestConfig )
        .setConnectionManager( manager )
        .build();
  }

  public void shutdown() {
    manager.shutdown();
  }

  private static X509TrustManager getDefaultTrustManager() {
    return new X509TrustManager() {
      @Override
      public void checkClientTrusted( X509Certificate[] certs, String param ) throws CertificateException {
        throw new CertificateException( "Client Authentication not implemented" );
      }

      @Override
      public void checkServerTrusted( X509Certificate[] certs, String param ) throws CertificateException {
        for ( X509Certificate cert : certs ) {
          cert.checkValidity(); // validate date
          // cert.verify( key ); // check by Public key
          // cert.getBasicConstraints()!=-1 // check by CA
        }
      }

      @Override
      public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
      }
    };
  }

  static void reset() {
    slaveConnectionManager = null;
  }
}
