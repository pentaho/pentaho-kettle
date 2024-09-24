/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.cluster;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class HttpUtil {

  public static final int ZIP_BUFFER_SIZE = 8192;
  private static final String PROTOCOL_UNSECURE = "http";
  private static final String PROTOCOL_SECURE = "https";

  private HttpUtil() {
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
    return constructUrl( space, hostname, port, webAppName, serviceAndArguments, false );
  }

  public static String constructUrl( VariableSpace space, String hostname, String port, String webAppName,
                                     String serviceAndArguments, boolean isSecure )
    throws UnsupportedEncodingException {
    String realHostname = space.environmentSubstitute( hostname );
    if ( !StringUtils.isEmpty( webAppName ) ) {
      serviceAndArguments = "/" + space.environmentSubstitute( webAppName ) + serviceAndArguments;
    }
    String protocol = isSecure ? PROTOCOL_SECURE : PROTOCOL_UNSECURE;
    String retval = protocol + "://" + realHostname + getPortSpecification( space, port ) + serviceAndArguments;
    retval = Const.replace( retval, " ", "%20" );
    return retval;
  }

  public static String getPortSpecification( VariableSpace space, String port ) {
    String realPort = space.environmentSubstitute( port );
    String portSpec = ":" + realPort;
    if ( Utils.isEmpty( realPort ) || port.equals( "80" ) ) {
      portSpec = "";
    }
    return portSpec;
  }

  /**
   * Base 64 decode, unzip and extract text using {@link Const#XML_ENCODING} predefined charset value for byte-wise
   * multi-byte character handling.
   *
   * @param loggingString64 base64 zip archive string representation
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

    // PDI-4325 originally used xml encoding in servlet
    try ( GZIPInputStream unzip = new GZIPInputStream( zip, HttpUtil.ZIP_BUFFER_SIZE );
           BufferedInputStream in = new BufferedInputStream( unzip, HttpUtil.ZIP_BUFFER_SIZE );
           InputStreamReader reader = new InputStreamReader( in, Const.XML_ENCODING ) ) {

      // use same buffer size
      char[] buff = new char[ HttpUtil.ZIP_BUFFER_SIZE ];
      for ( int length; ( length = reader.read( buff ) ) > 0; ) {
        writer.write( buff, 0, length );
      }
    }
    return writer.toString();
  }

  public static String encodeBase64ZippedString( String in ) throws IOException {
    Charset charset = Charset.forName( Const.XML_ENCODING );
    ByteArrayOutputStream baos = new ByteArrayOutputStream( 1024 );
    try ( Base64OutputStream base64OutputStream = new Base64OutputStream( baos );
          GZIPOutputStream gzos = new GZIPOutputStream( base64OutputStream ) ) {
      gzos.write( in.getBytes( charset ) );
    }
    return baos.toString();
  }
}
