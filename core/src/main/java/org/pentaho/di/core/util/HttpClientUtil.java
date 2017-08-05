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

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Utility class contained useful methods while working with {@link org.apache.http.client.HttpClient HttpClient}
 *
 * @author Yury_Bakhmutski
 * @since 06-27-2017
 *
 */
public class HttpClientUtil {

  /**
   *
   * @param response the httpresponse for processing
   * @return HttpEntity in String representation using "UTF-8" encoding
   * @throws IOException
   */
  public static String responseToString( HttpResponse response ) throws IOException {
    return responseToString( response, Charset.forName( StandardCharsets.UTF_8.name() ) );
  }

  /**
   *
   * @param response the httpresponse for processing
   * @param charset the charset used for getting HttpEntity
   * @return HttpEntity in decoded String representation using provided charset
   * @throws IOException
   */
  public static String responseToString( HttpResponse response, Charset charset ) throws IOException {
    return responseToString( response, charset, false );
  }

  /**
   *
   * @param response the httpresponse for processing
   * @param charset the charset used for getting HttpEntity
   * @param decode determines if the result should be decoded or not
   * @return HttpEntity in String representation using provided charset
   * @throws IOException
   */
  public static String responseToString( HttpResponse response, Charset charset, boolean decode ) throws IOException {
    HttpEntity entity = response.getEntity();
    String result = EntityUtils.toString( entity, charset );
    EntityUtils.consume( entity );
    if ( decode ) {
      result = URLDecoder.decode( result, StandardCharsets.UTF_8.name() );
    }
    return result;
  }

  public static InputStream responseToInputStream( HttpResponse response ) throws IOException {
    return response.getEntity().getContent();
  }

  public static byte[] responseToByteArray( HttpResponse response ) throws IOException {
    return EntityUtils.toByteArray( response.getEntity() );
  }

  /**
   * Returns context with AuthCache or null in case of any exception was thrown.
   *
   * @param host
   * @param port
   * @param user
   * @param password
   * @param schema
   * @return {@link org.apache.http.client.protocol.HttpClientContext HttpClientContext}
   */
  public static HttpClientContext createPreemptiveBasicAuthentication( String host, int port, String user,
                                                                       String password, String schema ) {
    HttpClientContext localContext = null;
    try {
      HttpHost target = new HttpHost( host, port, schema );
      CredentialsProvider credsProvider = new BasicCredentialsProvider();
      credsProvider.setCredentials(
        new AuthScope( target.getHostName(), target.getPort() ),
        new UsernamePasswordCredentials( user, password ) );

      // Create AuthCache instance
      AuthCache authCache = new BasicAuthCache();
      // Generate BASIC scheme object and add it to the local
      // auth cache
      BasicScheme basicAuth = new BasicScheme();
      authCache.put( target, basicAuth );

      // Add AuthCache to the execution context
      localContext = HttpClientContext.create();
      localContext.setAuthCache( authCache );
    } catch ( Exception e ) {
      return null;
    }
    return localContext;
  }

  /**
   * Returns context with AuthCache or null in case of any exception was thrown.
   * Use "http" schema.
   *
   * @param host
   * @param port
   * @param user
   * @param password
   * @return {@link org.apache.http.client.protocol.HttpClientContext HttpClientContext}
   */
  public static HttpClientContext createPreemptiveBasicAuthentication( String host, int port, String user,
                                                                       String password ) {
    return createPreemptiveBasicAuthentication( host, port, user, password, "http" );
  }

}
