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

import junit.framework.Assert;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.pentaho.di.core.variables.Variables;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPOutputStream;

public class HttpUtilTest {

  public static final String DEFAULT_ENCODING = "UTF-8";
  public static final String STANDART = "(\u256e\u00b0-\u00b0)\u256e\u2533\u2501\u2501\u2533\u30c6\u30fc\u30d6"
    + "\u30eb(\u256f\u00b0\u25a1\u00b0)\u256f\u253b\u2501\u2501\u253b\u30aa\u30d5";

  /**
   * [PDI-4325] Test that we can decode/encode Strings without loss of data.
   *
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  @Test
  public final void testDecodeBase64ZippedString() throws IOException, NoSuchAlgorithmException {
    String enc64 = this.canonicalBase64Encode( STANDART );
    // decode string
    String decoded = HttpUtil.decodeBase64ZippedString( enc64 );

    Assert.assertEquals( "Strings are the same after transformation", STANDART, decoded );
  }

  @Test
  public void testConstructUrl() throws Exception {
    Variables variables = new Variables();
    String expected = "hostname:1234/webAppName?param=value";

    Assert.assertEquals( "http://" + expected,
      HttpUtil.constructUrl( variables, "hostname", String.valueOf( 1234 ), "webAppName", "?param=value" ) );

    Assert.assertEquals( "http://" + expected,
      HttpUtil.constructUrl( variables, "hostname", String.valueOf( 1234 ), "webAppName", "?param=value", false ) );

    Assert.assertEquals( "https://" + expected,
      HttpUtil.constructUrl( variables, "hostname", String.valueOf( 1234 ), "webAppName", "?param=value", true ) );
  }

  /**
   * Test that we can encode and decode String using only static class-under-test methods.
   *
   *
   * @throws IOException
   */
  @Test
  public void testEncodeBase64ZippedString() throws IOException {
    String enc64 = HttpUtil.encodeBase64ZippedString( STANDART );
    String decoded = HttpUtil.decodeBase64ZippedString( enc64 );

    Assert.assertEquals( "Strings are the same after transformation", STANDART, decoded );
  }

  /**
   * https://www.securecoding.cert.org/confluence/display/java/IDS12-J.+Perform+lossless+conversion+
   * of+String+data+between+differing+character+encodings
   *
   * @param in
   *          string to encode
   * @return
   * @throws IOException
   */
  private String canonicalBase64Encode( String in ) throws IOException {
    Charset charset = Charset.forName( DEFAULT_ENCODING );
    CharsetEncoder encoder = charset.newEncoder();
    encoder.reset();
    ByteBuffer baosbf = encoder.encode( CharBuffer.wrap( in ) );
    byte[] bytes = new byte[baosbf.limit()];
    baosbf.get( bytes );

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    GZIPOutputStream gzos = new GZIPOutputStream( baos );
    gzos.write( bytes );
    gzos.close();
    String encoded = new String( Base64.encodeBase64( baos.toByteArray() ) );

    return encoded;
  }
}
