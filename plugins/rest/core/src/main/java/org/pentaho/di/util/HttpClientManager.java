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

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
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
import java.security.UnrecoverableKeyException;
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
    private FileInputStream trustStoreStream;
    private String trustStorePassword;
    private FileInputStream keyStoreStream;
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

    public HttpClientBuilderFacade setTrustStore(FileInputStream trustStoreStream, String trustStorePassword) {
      this.trustStoreStream = trustStoreStream;
      this.trustStorePassword = trustStorePassword;
      return this;
    }

    public HttpClientBuilderFacade setKeyStore(FileInputStream keyStoreStream, String keyStorePassword, String keyPassword) {
      this.keyStoreStream = keyStoreStream;
      this.keyStorePassword = keyStorePassword;
      this.keyPassword = keyPassword;
      return this;
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

      if ( trustStoreStream != null || keyStoreStream != null || ignoreSsl ) {
        try {
          SSLContext sslContext = HttpClientManager.getSslContext( ignoreSsl, trustStoreStream, trustStorePassword, keyStoreStream, keyStorePassword, keyPassword );
          httpClientBuilder.setSSLContext( sslContext );
        } catch ( Exception e ) {
          throw new RuntimeException( "Failed to set SSL context: " + e.getMessage(), e );
        }
      }

      return httpClientBuilder.build();
    }
  }


  public static SSLContext getSslContext(boolean ignoreSSLValidation, FileInputStream trustFileStream, String trustStorePassword)
	      throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, KeyManagementException, UnrecoverableKeyException {
	  return getSslContext(ignoreSSLValidation,trustFileStream,trustStorePassword,null,null,null);
  }

  
  public static SSLContext getSslContext(boolean ignoreSSLValidation, FileInputStream trustFileStream, String trustStorePassword,
      FileInputStream keyStoreFileStream, String keyStorePassword, String keyPassword)
      throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, KeyManagementException, UnrecoverableKeyException {

    String SSLContextProtocol = ignoreSSLValidation ? "SSL" : "TLSv1.2";
    TrustManager[] trustManagers = null;
    KeyManager[] keyManagers = null;

    if (ignoreSSLValidation) {
      trustManagers = new TrustManager[] {
        new X509TrustManager() {
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
          }

          public void checkClientTrusted(X509Certificate[] certs, String authType) {
          }

          public void checkServerTrusted(X509Certificate[] certs, String authType) {
          }
        }
      };
    } else if (trustFileStream != null) {
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
      trustStore.load(trustFileStream, trustStorePassword != null ? trustStorePassword.toCharArray() : null);
      trustFileStream.close();

      tmf.init(trustStore);

      X509TrustManager trustManager = null;
      for (TrustManager tm : tmf.getTrustManagers()) {
        if (tm instanceof X509TrustManager) {
          trustManager = (X509TrustManager) tm;
          break;
        }
      }

      final X509TrustManager finalTrustManager = trustManager;
      trustManagers = new TrustManager[] {
        new X509TrustManager() {
          @Override
          public X509Certificate[] getAcceptedIssuers() {
            return finalTrustManager.getAcceptedIssuers();
          }

          @Override
          public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            finalTrustManager.checkServerTrusted(chain, authType);
          }

          @Override
          public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            finalTrustManager.checkClientTrusted(chain, authType);
          }
        }
      };
    }

    if (keyStoreFileStream != null) {
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(keyStoreFileStream, keyStorePassword != null ? keyStorePassword.toCharArray() : null);
      keyStoreFileStream.close();

      KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(keyStore, keyPassword != null ? keyPassword.toCharArray() : null);
      keyManagers = kmf.getKeyManagers();
    }

    SSLContext sslContext = SSLContext.getInstance(SSLContextProtocol);
    sslContext.init(keyManagers, trustManagers, new java.security.SecureRandom());

    return sslContext;
  }

  /**
   * @deprecated This method is deprecated because is replaced by getSSLContext. 
   *             Use {@link #getSslContext(boolean, FileInputStream, String)} instead.
   */
  @Deprecated
  public static SSLContext getTrustAllSslContext() throws NoSuchAlgorithmException, KeyManagementException {
    try {
      return getSslContext(true, null, null, null, null, null);
    } catch (UnrecoverableKeyException | IOException | CertificateException | KeyStoreException e) {
      // This should never happen since we're passing null for keystore parameters
      throw new KeyManagementException("Unexpected error: " + e.getMessage(), e );
    }  
  }

  /**
   * @deprecated This method is deprecated because is replaced by getSSLContext. 
   *             Use {@link #getSslContext(boolean, FileInputStream, String)} instead.
   */
  @Deprecated
  public static SSLContext getSslContextWithTrustStoreFile( FileInputStream trustFileStream, String trustStorePassword)
    throws NoSuchAlgorithmException, KeyStoreException,
    IOException, CertificateException, KeyManagementException {
    try {
      return getSslContext( false,trustFileStream, trustStorePassword);
    } catch ( UnrecoverableKeyException e ) {
      // This should never happen since we're passing null for keystore parameters
      throw new KeyManagementException( "Unexpected error: " + e.getMessage(), e );
    }
  }

}
