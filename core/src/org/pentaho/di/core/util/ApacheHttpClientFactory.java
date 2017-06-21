/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.util;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * Created by Yury_Bakhmutski on 6/20/2017.
 */
public class ApacheHttpClientFactory {
  public static final int CONNECTIONS_PER_ROUTE = 100;
  public static final int TOTAL_CONNECTIONS = 200;

  private static ApacheHttpClientFactory httpClientProxy;
  private PoolingHttpClientConnectionManager manager;

  private ApacheHttpClientFactory() {
    manager = new PoolingHttpClientConnectionManager();
    manager.setDefaultMaxPerRoute( CONNECTIONS_PER_ROUTE );
    manager.setMaxTotal( TOTAL_CONNECTIONS );
  }

  public static ApacheHttpClientFactory getInstance() {
    if ( httpClientProxy == null ) {
      httpClientProxy = new ApacheHttpClientFactory();
    }
    return httpClientProxy;
  }

  public CloseableHttpClient createHttpClient() {
    return HttpClients.custom().setConnectionManager( manager )
      .build();
  }

  public CloseableHttpClient createHttpClient( int connectionTimeout, int socketTimeout ) {
    RequestConfig requestConfig = RequestConfig.custom()
      .setSocketTimeout( socketTimeout )
      .setConnectTimeout( connectionTimeout )
      .build();

    return
      HttpClientBuilder
        .create()
        .setDefaultRequestConfig( requestConfig )
        .setConnectionManager( manager )
        .build();
  }

  public CloseableHttpClient createHttpClient( int connectionTimeout, int socketTimeout, String proxyHost,
                                               int proxyPort ) {
    HttpHost httpHost = new HttpHost( proxyHost, proxyPort );

    RequestConfig requestConfig = RequestConfig.custom()
      .setSocketTimeout( socketTimeout )
      .setConnectTimeout( connectionTimeout )
      .setProxy( httpHost )
      .build();

    return
      HttpClientBuilder
        .create()
        .setDefaultRequestConfig( requestConfig )
        .setConnectionManager( manager )
        .build();
  }

  public CloseableHttpClient createHttpClient( int connectionTimeout, int socketTimeout, String user, String password,
                                               String proxyHost, int proxyPort ) {
    HttpHost httpHost = new HttpHost( proxyHost, proxyPort );

    RequestConfig requestConfig = RequestConfig.custom()
      .setSocketTimeout( socketTimeout )
      .setConnectTimeout( connectionTimeout )
      .setProxy( httpHost )
      .build();

    CredentialsProvider provider = new BasicCredentialsProvider();
    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials( user, password );
    provider.setCredentials( AuthScope.ANY, credentials );

    return
      HttpClientBuilder
        .create()
        .setDefaultCredentialsProvider( provider )
        .setDefaultRequestConfig( requestConfig )
        .setConnectionManager( manager )
        .build();
  }

  public CloseableHttpClient createHttpClient( int connectionTimeout, int socketTimeout, String user, String password,
                                               String proxyHost, int proxyPort, AuthScope authScope ) {
    HttpHost httpHost = new HttpHost( proxyHost, proxyPort );

    RequestConfig requestConfig = RequestConfig.custom()
      .setSocketTimeout( socketTimeout )
      .setConnectTimeout( connectionTimeout )
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

  public CloseableHttpClient createHttpClient( int socketTimeout, String user, String password,
                                               String proxyHost, int proxyPort ) {
    HttpHost httpHost = new HttpHost( proxyHost, proxyPort );

    RequestConfig requestConfig = RequestConfig.custom()
      .setSocketTimeout( socketTimeout )
      .setProxy( httpHost )
      .build();

    CredentialsProvider provider = new BasicCredentialsProvider();
    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials( user, password );
    provider.setCredentials( AuthScope.ANY, credentials );

    return
      HttpClientBuilder
        .create()
        .setDefaultCredentialsProvider( provider )
        .setDefaultRequestConfig( requestConfig )
        .setConnectionManager( manager )
        .build();
  }

  public CloseableHttpClient createHttpClient( String user, String password,
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

  public CloseableHttpClient createHttpClient( String user, String password,
                                               String proxyHost, int proxyPort ) {
    HttpHost httpHost = new HttpHost( proxyHost, proxyPort );

    RequestConfig requestConfig = RequestConfig.custom()
      .setProxy( httpHost )
      .build();

    CredentialsProvider provider = new BasicCredentialsProvider();
    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials( user, password );
    provider.setCredentials( AuthScope.ANY, credentials );

    return
      HttpClientBuilder
        .create()
        .setDefaultCredentialsProvider( provider )
        .setDefaultRequestConfig( requestConfig )
        .setConnectionManager( manager )
        .build();
  }

  public CloseableHttpClient createHttpClient( String proxyHost, int proxyPort ) {
    HttpHost httpHost = new HttpHost( proxyHost, proxyPort );

    RequestConfig requestConfig = RequestConfig.custom()
      .setProxy( httpHost )
      .build();

    return
      HttpClientBuilder
        .create()
        .setDefaultRequestConfig( requestConfig )
        .setConnectionManager( manager )
        .build();
  }

  public CloseableHttpClient createHttpClient( String user, String password ) {
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

  public void shutdown() {
    manager.shutdown();
  }

}
