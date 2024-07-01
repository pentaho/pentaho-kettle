/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2023-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.util;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class HttpClientManager {
  private static final int CONNECTIONS_PER_ROUTE = 100;
  private static final int TOTAL_CONNECTIONS = 200;

  private static HttpClientManager httpClientManager;
  private static PoolingHttpClientConnectionManager manager;

  private HttpClientManager() {
    manager = new PoolingHttpClientConnectionManager();
    manager.setDefaultMaxPerRoute( CONNECTIONS_PER_ROUTE );
    manager.setMaxTotal( TOTAL_CONNECTIONS );
  }

  public static HttpClientManager getInstance() {
    if ( httpClientManager == null ) {
      httpClientManager = new HttpClientManager();
    }
    return httpClientManager;
  }

  public CloseableHttpClient createDefaultClient() {
    return HttpClients.custom().setConnectionManager( manager ).build();
  }

  public HttpClientBuilderFacade createBuilder() {
    return new HttpClientBuilderFacade();
  }

  public class HttpClientBuilderFacade {
    private RedirectStrategy redirectStrategy;
    private CredentialsProvider provider;
    private int connectionTimeout;
    private int socketTimeout;
    private HttpHost proxy;
    private boolean ignoreSsl;

    public HttpClientBuilderFacade setConnectionTimeout( int connectionTimeout ) {
      this.connectionTimeout = connectionTimeout;
      return this;
    }

    public HttpClientBuilderFacade setSocketTimeout( int socketTimeout ) {
      this.socketTimeout = socketTimeout;
      return this;
    }

    public HttpClientBuilderFacade setCredentials(
      String user, String password, AuthScope authScope ) {
      CredentialsProvider provider = new BasicCredentialsProvider();
      UsernamePasswordCredentials credentials = new UsernamePasswordCredentials( user, password );
      provider.setCredentials( authScope, credentials );
      this.provider = provider;
      return this;
    }

    public HttpClientBuilderFacade setCredentials( String user, String password ) {
      return setCredentials( user, password, AuthScope.ANY );
    }

    public HttpClientBuilderFacade setProxy( String proxyHost, int proxyPort ) {
      setProxy( proxyHost, proxyPort, "http" );
      return this;
    }

    public HttpClientBuilderFacade setProxy( String proxyHost, int proxyPort, String scheme ) {
      this.proxy = new HttpHost( proxyHost, proxyPort, scheme );
      return this;
    }

    public HttpClientBuilderFacade setRedirect( RedirectStrategy redirectStrategy ) {
      this.redirectStrategy = redirectStrategy;
      return this;
    }

    public void ignoreSsl( boolean ignoreSsl ) {
      this.ignoreSsl = ignoreSsl;
    }

    public void ignoreSsl( HttpClientBuilder httpClientBuilder ) {
      TrustStrategy acceptingTrustStrategy = ( cert, authType ) -> true;
      SSLContext sslContext;
      try {
        sslContext = SSLContexts.custom().loadTrustMaterial( null, acceptingTrustStrategy ).build();
      } catch ( NoSuchAlgorithmException | KeyManagementException | KeyStoreException e ) {
        throw new RuntimeException( e );
      }

      SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory( sslContext,
        NoopHostnameVerifier.INSTANCE );

      Registry<ConnectionSocketFactory> socketFactoryRegistry =
        RegistryBuilder.<ConnectionSocketFactory>create()
          .register( "https", sslsf )
          .register( "http", new PlainConnectionSocketFactory() )
          .build();

      BasicHttpClientConnectionManager connectionManager =
        new BasicHttpClientConnectionManager( socketFactoryRegistry );

      httpClientBuilder.setSSLSocketFactory( sslsf ).setConnectionManager( connectionManager );
    }

    public CloseableHttpClient build() {
      HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
      httpClientBuilder.setConnectionManager( manager );

      RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
      if ( socketTimeout > 0 ) {
        requestConfigBuilder.setSocketTimeout( socketTimeout );
      }
      if ( connectionTimeout > 0 ) {
        requestConfigBuilder.setConnectTimeout( socketTimeout );
      }
      if ( proxy != null ) {
        requestConfigBuilder.setProxy( proxy );
      }
      httpClientBuilder.setDefaultRequestConfig( requestConfigBuilder.build() );

      if ( provider != null ) {
        httpClientBuilder.setDefaultCredentialsProvider( provider );
      }
      if ( redirectStrategy != null ) {
        httpClientBuilder.setRedirectStrategy( redirectStrategy );
      }
      if ( ignoreSsl ) {
        ignoreSsl( httpClientBuilder );
      }

      return httpClientBuilder.build();
    }
  }

  public static SSLContext getSslContextWithTrustStoreFile( FileInputStream trustFileStream, String trustStorePassword )
    throws NoSuchAlgorithmException, KeyStoreException,
    IOException, CertificateException, KeyManagementException {
    TrustManagerFactory tmf = TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
    // Using null here initialises the TMF with the default trust store.
    tmf.init( (KeyStore) null );

    // Get hold of the default trust manager
    X509TrustManager defaultTm = null;
    for ( TrustManager tm : tmf.getTrustManagers() ) {
      if ( tm instanceof X509TrustManager ) {
        defaultTm = (X509TrustManager) tm;
        break;
      }
    }

    // Load the trustStore which needs to be imported
    KeyStore trustStore = KeyStore.getInstance( KeyStore.getDefaultType() );
    trustStore.load( trustFileStream, trustStorePassword.toCharArray() );

    trustFileStream.close();

    tmf = TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
    tmf.init( trustStore );

    // Get hold of the default trust manager
    X509TrustManager trustManager = null;
    for ( TrustManager tm : tmf.getTrustManagers() ) {
      if ( tm instanceof X509TrustManager ) {
        trustManager = (X509TrustManager) tm;
        break;
      }
    }

    final X509TrustManager finalDefaultTm = defaultTm;
    final X509TrustManager finalTrustManager = trustManager;
    X509TrustManager customTm = new X509TrustManager() {
      @Override
      public X509Certificate[] getAcceptedIssuers() {
        return finalDefaultTm.getAcceptedIssuers();
      }

      @Override
      public void checkServerTrusted( X509Certificate[] chain, String authType ) throws CertificateException {
        try {
          finalTrustManager.checkServerTrusted( chain, authType );
        } catch ( CertificateException e ) {
          finalDefaultTm.checkServerTrusted( chain, authType );
        }
      }

      @Override
      public void checkClientTrusted( X509Certificate[] chain, String authType ) throws CertificateException {
        finalDefaultTm.checkClientTrusted( chain, authType );
      }
    };

    SSLContext sslContext = SSLContext.getInstance( "TLSv1.2" );
    sslContext.init( null, new TrustManager[] { customTm }, null );

    return sslContext;
  }

  public static SSLContext getTrustAllSslContext() throws NoSuchAlgorithmException, KeyManagementException {
    TrustManager[] trustAllCerts = new TrustManager[] {
      new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return null;
        }

        public void checkClientTrusted( X509Certificate[] certs, String authType ) {
        }

        public void checkServerTrusted( X509Certificate[] certs, String authType ) {
        }

      }
    };

    SSLContext sc = SSLContext.getInstance( "SSL" );
    sc.init( null, trustAllCerts, new java.security.SecureRandom() );
    return sc;
  }
}
