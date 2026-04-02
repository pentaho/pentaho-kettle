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


package org.pentaho.di.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * Single entry point for all {@link org.apache.http.client.HttpClient HttpClient instances} usages in pentaho projects.
 * Contains {@link org.apache.http.impl.conn.PoolingHttpClientConnectionManager Connection pool} of 200 connections.
 * Maximum connections per one route is 100.
 * Provides inner builder class for creating {@link org.apache.http.client.HttpClient HttpClients}.
 *
 * @author Yury_Bakhmutski
 * @since 06-23-2017
 *
 */
public class HttpClientManager {
  private static final int CONNECTIONS_PER_ROUTE = 100;
  private static final int TOTAL_CONNECTIONS = 200;

  private static final HttpClientManager httpClientManager = new HttpClientManager();
  private final PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();

  private HttpClientManager() {
    manager.setDefaultMaxPerRoute( CONNECTIONS_PER_ROUTE );
    manager.setMaxTotal( TOTAL_CONNECTIONS );
  }

  public static HttpClientManager getInstance() {
    return httpClientManager;
  }

  public CloseableHttpClient createDefaultClient() {
    return HttpClients.custom()
      .setConnectionManager( manager )
      .setConnectionManagerShared( true )
      .build();
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
    private InputStream trustStoreStream;
    private String trustStorePassword;
    private InputStream keyStoreStream;
    private String keyStorePassword;
    private String keyPassword;

    public HttpClientBuilderFacade setConnectionTimeout( int connectionTimeout ) {
      this.connectionTimeout = connectionTimeout;
      return this;
    }

    public HttpClientBuilderFacade setSocketTimeout( int socketTimeout ) {
      this.socketTimeout = socketTimeout;
      return this;
    }

    public HttpClientBuilderFacade setCredentials( String user, String password, AuthScope authScope ) {
      CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials( authScope, new UsernamePasswordCredentials( user, password ) );
      this.provider = credentialsProvider;
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

    public HttpClientBuilderFacade ignoreSsl( boolean ignoreSsl ) {
      this.ignoreSsl = ignoreSsl;
      return this;
    }

    public HttpClientBuilderFacade setTrustStore( InputStream trustStoreStream, String trustStorePassword ) {
      this.trustStoreStream = trustStoreStream;
      this.trustStorePassword = trustStorePassword;
      return this;
    }

    public HttpClientBuilderFacade setKeyStore( InputStream keyStoreStream, String keyStorePassword,
                                                String keyPassword ) {
      this.keyStoreStream = keyStoreStream;
      this.keyStorePassword = keyStorePassword;
      this.keyPassword = keyPassword;
      return this;
    }

    public CloseableHttpClient build() {
      HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
      httpClientBuilder.setConnectionManager( manager );
      httpClientBuilder.setConnectionManagerShared( true );

      RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
      if ( socketTimeout > 0 ) {
        requestConfigBuilder.setSocketTimeout( socketTimeout );
      }
      if ( connectionTimeout > 0 ) {
        requestConfigBuilder.setConnectTimeout( connectionTimeout );
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

      if ( trustStoreStream != null || keyStoreStream != null || ignoreSsl ) {
        try {
          SSLContext sslContext =
            HttpClientManager.getSslContext( ignoreSsl, trustStoreStream, trustStorePassword, keyStoreStream,
              keyStorePassword, keyPassword );
          httpClientBuilder.setSSLContext( sslContext );
        } catch ( Exception e ) {
          throw new RuntimeException( "Failed to set SSL context: " + e.getMessage(), e );
        }
      }

      return httpClientBuilder.build();
    }
  }

  public static SSLContext getSslContext( boolean ignoreSSLValidation, InputStream trustFileStream,
                                          String trustStorePassword )
    throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, KeyManagementException,
    UnrecoverableKeyException {
    return getSslContext( ignoreSSLValidation, trustFileStream, trustStorePassword, null, null, null );
  }

  /**
   * @param ignoreSSLValidation if {@code true} will accept all certificates and any supplied trust file will be ignored
   * @param trustFileStream trust store file
   * @param trustStorePassword trust store password
   * @param keyStoreFileStream key store file
   * @param keyStorePassword key store password
   * @param keyPassword
   */
  public static SSLContext getSslContext( boolean ignoreSSLValidation, InputStream trustFileStream,
                                          String trustStorePassword, InputStream keyStoreFileStream,
                                          String keyStorePassword, String keyPassword )
    throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, KeyManagementException,
    UnrecoverableKeyException {

    String sslContextProtocol = ignoreSSLValidation ? "SSL" : "TLS";

    TrustManager[] trustManagers = null;
    if ( ignoreSSLValidation ) {
      trustManagers = createPhonyTrustManagers();
    } else if ( trustFileStream != null ) {
      trustManagers = createTrustManagersFromTrustStore( trustFileStream, trustStorePassword );
    }

    KeyManager[] keyManagers = null;
    if ( keyStoreFileStream != null ) {
      keyManagers = createKeyManagersFromKeyStore( keyStoreFileStream, keyStorePassword, keyPassword );
    }

    SSLContext sslContext = SSLContext.getInstance( sslContextProtocol );
    sslContext.init( keyManagers, trustManagers, new java.security.SecureRandom() );

    return sslContext;
  }

  private static KeyManager[] createKeyManagersFromKeyStore( InputStream keyStoreFileStream, String keyStorePassword, String keyPassword )
    throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
    KeyStore keyStore = KeyStore.getInstance( KeyStore.getDefaultType() );
    keyStore.load( keyStoreFileStream, toCharArray( keyStorePassword ) );

    KeyManagerFactory kmf = KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );
    kmf.init( keyStore, toCharArray( keyPassword ) );
    return kmf.getKeyManagers();
  }

  private static TrustManager[] createTrustManagersFromTrustStore( InputStream trustFileStream, String trustStorePassword )
    throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {
    TrustManagerFactory tmf = TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
    KeyStore trustStore = KeyStore.getInstance( KeyStore.getDefaultType() );
    trustStore.load( trustFileStream, toCharArray( trustStorePassword ) );
    tmf.init( trustStore );
    return tmf.getTrustManagers();
  }

  private static char[] toCharArray( String value ) {
    return value != null ? value.toCharArray() : null;
  }


  // it's supposed to be insecure, sonar
  @SuppressWarnings( "java:S4830" )
  /** @return TrustManager accepting ALL certificates */
  private static TrustManager[] createPhonyTrustManagers() {
    return new TrustManager[] {
      new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
          return new X509Certificate[ 0 ];
        }

        public void checkClientTrusted( X509Certificate[] certs, String authType ) {
          // trust all
        }

        public void checkServerTrusted( X509Certificate[] certs, String authType ) {
          // trust all
        }
      }
    };
  }

}
