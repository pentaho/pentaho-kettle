/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;

public class HttpUtil {

  public static final int ZIP_BUFFER_SIZE = 8192;

  private static HttpClient getClient( VariableSpace space, String hostname, String port, String webAppName,
      String username, String password, String proxyHostname, String proxyPort, String nonProxyHosts ) {

    HttpClient client = SlaveConnectionManager.getInstance().createHttpClient();
    addCredentials( client, space, hostname, port, webAppName, username, password );
    addProxy( client, space, hostname, proxyHostname, proxyPort, nonProxyHosts );

    return client;
  }

  public static PostMethod execService( VariableSpace space, String hostname, String port, String webAppName,
      String urlString, String username, String password, String proxyHostname, String proxyPort,
      String nonProxyHosts, Iterable<Header> headers, Map<String, Object> parameters, Map<String, String> arguments )
      throws Exception {

    HttpClient
        client =
        getClient( space, hostname, port, webAppName, username, password, proxyHostname, proxyPort, nonProxyHosts );

    client.getHttpConnectionManager().getParams().setConnectionTimeout( 0 );
    client.getHttpConnectionManager().getParams().setSoTimeout( 0 );

    PostMethod method = new PostMethod( urlString );

    method.setDoAuthentication( true );

    for ( Header header : headers ) {
      method.addRequestHeader( header );
    }

    for ( Map.Entry<String, Object> parameter : parameters.entrySet() ) {
      method.getParams().setParameter( parameter.getKey(), parameter.getValue() );
    }

    for ( Map.Entry<String, String> arg : arguments.entrySet() ) {
      method.addParameter( arg.getKey(), arg.getValue() );
    }

    execMethod( client, method );
    return method;
  }

  public static String execService( VariableSpace space, String hostname, String port, String webAppName,
      String serviceAndArguments, String username, String password, String proxyHostname, String proxyPort,
      String nonProxyHosts ) throws Exception {

    HttpClient
        client =
        getClient( space, hostname, port, webAppName, username, password, proxyHostname, proxyPort, nonProxyHosts );

    String urlString = constructUrl( space, hostname, port, webAppName, serviceAndArguments );
    HttpMethod method = new GetMethod( urlString );

    try {
      execMethod( client, method );
      return method.getResponseBodyAsString();
    } finally {
      method.releaseConnection();
    }

  }

  public static int execMethod( HttpClient client, HttpMethod method ) throws Exception {
    int result;
    try {
      result = client.executeMethod( method );
    } catch ( Exception e ) {
      throw new KettleException(
          "You don't seem to be getting a connection to the server. Check the host and port you're using and make sure the sever is up and running." );
    }

    if ( result == 500 ) {
      throw new KettleException( "There was an error reading data from the server." );
    }

    if ( result == 401 ) {
      throw new KettleException(
          "Nice try-but we couldn't log you in. Check your username and password and try again." );
    }

    if ( result != 200 ) {
      throw new KettleException( method.getResponseBodyAsString() );
    }

    return result;
  }

  /**
   * Returns http GET request string using specified parameters.
   *
   * @param space
   * @param hostname
   * @param port
   * @param webAppName
   * @param serviceAndArguments
   * @return
   * @throws UnsupportedEncodingException
   */
  public static String constructUrl( VariableSpace space, String hostname, String port, String webAppName,
    String serviceAndArguments ) throws UnsupportedEncodingException {
    String realHostname = space.environmentSubstitute( hostname );
    if ( !StringUtils.isEmpty( webAppName ) ) {
      serviceAndArguments = "/" + space.environmentSubstitute( webAppName ) + serviceAndArguments;
    }
    String retval = "http://" + realHostname + getPortSpecification( space, port ) + serviceAndArguments;
    retval = Const.replace( retval, " ", "%20" );
    return retval;
  }

  public static String getPortSpecification( VariableSpace space, String port ) {
    String realPort = space.environmentSubstitute( port );
    String portSpec = ":" + realPort;
    if ( Const.isEmpty( realPort ) || port.equals( "80" ) ) {
      portSpec = "";
    }
    return portSpec;
  }

  public static void addProxy( HttpClient client, VariableSpace space, String hostname, String proxyHostname,
    String proxyPort, String nonProxyHosts ) {
    String host = space.environmentSubstitute( hostname );
    String phost = space.environmentSubstitute( proxyHostname );
    String pport = space.environmentSubstitute( proxyPort );
    String nonprox = space.environmentSubstitute( nonProxyHosts );

    /** added by shingo.yamagami@ksk-sol.jp **/
    if ( !Const.isEmpty( phost ) && !Const.isEmpty( pport ) ) {
      // skip applying proxy if non-proxy host matches
      if ( !Const.isEmpty( nonprox ) && !Const.isEmpty( host ) && host.matches( nonprox ) ) {
        return;
      }
      client.getHostConfiguration().setProxy( phost, Integer.parseInt( pport ) );
    }
    /** added by shingo.yamagami@ksk-sol.jp **/
  }

  public static void addCredentials( HttpClient client, VariableSpace space, String hostname, String port,
    String webAppName, String username, String password ) {
    if ( StringUtils.isEmpty( webAppName ) ) {
      client.getState().setCredentials(
        new AuthScope( space.environmentSubstitute( hostname ), Const.toInt(
          space.environmentSubstitute( port ), 80 ), "Kettle" ),
        new UsernamePasswordCredentials( space.environmentSubstitute( username ), Encr
          .decryptPasswordOptionallyEncrypted( space.environmentSubstitute( password ) ) ) );
    } else {
      Credentials creds =
        new UsernamePasswordCredentials( space.environmentSubstitute( username ), Encr
          .decryptPasswordOptionallyEncrypted( space.environmentSubstitute( password ) ) );
      client.getState().setCredentials( AuthScope.ANY, creds );
      client.getParams().setAuthenticationPreemptive( true );
    }
  }

  /**
   * Base 64 decode, unzip and extract text using {@link Const#XML_ENCODING} predefined charset value for byte-wise
   * multi-byte character handling.
   *
   * @param loggingString64
   *          base64 zip archive string representation
   * @return text from zip archive
   * @throws IOException
   */
  public static String decodeBase64ZippedString( String loggingString64 ) throws IOException {
    if ( loggingString64 == null || loggingString64.isEmpty() ) {
      return "";
    }
    StringWriter writer = new StringWriter();
    // base 64 decode
    byte[] bytes64 = Base64.decodeBase64( loggingString64.getBytes() );
    // unzip to string encoding-wise
    ByteArrayInputStream zip = new ByteArrayInputStream( bytes64 );

    GZIPInputStream unzip = null;
    InputStreamReader reader = null;
    BufferedInputStream in = null;
    try {
      unzip = new GZIPInputStream( zip, HttpUtil.ZIP_BUFFER_SIZE );
      in = new BufferedInputStream( unzip, HttpUtil.ZIP_BUFFER_SIZE );
      // PDI-4325 originally used xml encoding in servlet
      reader = new InputStreamReader( in, Const.XML_ENCODING );
      writer = new StringWriter();

      // use same buffer size
      char[] buff = new char[HttpUtil.ZIP_BUFFER_SIZE];
      for ( int length = 0; ( length = reader.read( buff ) ) > 0; ) {
        writer.write( buff, 0, length );
      }
    } finally {
      // close resources
      if ( reader != null ) {
        try {
          reader.close();
        } catch ( IOException e ) {
          // Suppress
        }
      }
      if ( in != null ) {
        try {
          in.close();
        } catch ( IOException e ) {
          // Suppress
        }
      }
      if ( unzip != null ) {
        try {
          unzip.close();
        } catch ( IOException e ) {
          // Suppress
        }
      }
    }
    return writer.toString();
  }

  public static String encodeBase64ZippedString( String in ) throws IOException {
    Charset charset = Charset.forName( Const.XML_ENCODING );
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    GZIPOutputStream gzos = new GZIPOutputStream( baos );
    gzos.write( in.getBytes( charset ) );
    gzos.close();

    return new String( Base64.encodeBase64( baos.toByteArray() ) );
  }
}
