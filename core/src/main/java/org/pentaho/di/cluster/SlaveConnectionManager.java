/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
        //log.logError( "Default SSL context hasn't been initialized", e );
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
        return null;
      }
    };
  }

  static void reset() {
    slaveConnectionManager = null;
  }
}
