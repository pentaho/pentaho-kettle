/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.charactersupport;

import junit.framework.TestCase;

import org.apache.commons.vfs2.provider.UriParser;
import org.junit.Test;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sample class to capture what characters to support for PVFS, VFS URI
 * and general working code to parse and manipulate the URIs.
 *
 * Please add test cases along with code.
 */
public class CharacterSupportExamplesTest extends TestCase {

  private static final String KEY_SCHEME = "scheme";
  private static final String KEY_CONNECTION_NAME = "connectionName";
  private static final String KEY_CONNECTION_PATH = "connectionPath";

  private static final String KEY_FILENAME = "filename";

  @Test
  public void test_PVFS_Restrictive_Character_Set_1() throws Exception {
    // example of 9.3.0.4 PDI allowed URI, only alphanumeric + "_", white spaces
    String pvfsUri = "pvfs://some_connection_name/segment1/segment2/file.txt";
    Map<String, String> map = parseUsingJavaStandardLibraries( pvfsUri );
    assertEqualsPvfsMap( "pvfs",
      "some_connection_name",
      "/segment1/segment2/file.txt",
      "file.txt",
      map );
  }

  @Test
  public void test_PVFS_Restrictive_Character_Set_2() throws Exception {
    // example of 9.3.0.4 PDI allowed URI, only alphanumeric + "_", white spaces
    String pvfsUriWithSpaces = "pvfs://some_connection_name with spaces/segment1/segment2/file.txt";
    // With some special characters have to encode before using URI library
    String encodedPvfsURI = UriParser.encode( pvfsUriWithSpaces, new char[]{' '}  ); // NOTE can be more characters
    Map<String, String> map = parseUsingJavaStandardLibraries( encodedPvfsURI );
    assertEqualsPvfsMap( "pvfs",
      "some_connection_name with spaces",
      "/segment1/segment2/file.txt",
      "file.txt",
      map );
  }
  @Test
  public void test_PVFS_Restrictive_Character_Set_3() throws Exception {
    // example of 9.3.0.4 PDI allowed URI, only alphanumeric + "_", white spaces
    String pvfsUriWithSpaces = "pvfs://some_connection_name @@@@@$$$$$! with spaces/segment1/segment2/this segment has spaces too!!!/file.txt";
    // With some special characters have to encode before using URI library
    String encodedPvfsURI = UriParser.encode( pvfsUriWithSpaces, new char[]{' ', '@', '$', '!'}  ); // NOTE can be more characters
    Map<String, String> map = parseUsingJavaStandardLibraries( encodedPvfsURI );
    assertEqualsPvfsMap( "pvfs",
      "some_connection_name @@@@@$$$$$! with spaces",
      "/segment1/segment2/this segment has spaces too!!!/file.txt",
      "file.txt",
      map );
  }


  @Test
  public void test_PVFS_Restrictive_Character_Set_4() throws Exception {
    // example of 9.3.0.4 PDI allowed URI, only alphanumeric + "_", white spaces
    // Also includes extended ASCII characters
    String pvfsUriWithSpaces = "pvfs://some_connection_name @@@@@$$$$$! with spaces/segment1/segment2/this segment has spaces too!!!/ExtendedAscii_Æ_ß_€_ñ/file.txt";
    // With some special characters have to encode before using URI library
    String encodedPvfsURI = UriParser.encode( pvfsUriWithSpaces, new char[]{' ', '@', '$', '!'}  ); // NOTE can be more characters
    Map<String, String> map = parseUsingJavaStandardLibraries( encodedPvfsURI );
    assertEqualsPvfsMap( "pvfs",
      "some_connection_name @@@@@$$$$$! with spaces",
      "/segment1/segment2/this segment has spaces too!!!/ExtendedAscii_Æ_ß_€_ñ/file.txt",
      "file.txt",
      map );
  }

  @Test
  public void test_PVFS_UTF_Character_Set_1() throws Exception {
    // example of ASCII and UTF-8 characters
    String pvfsUriWithSpaces = "pvfs://ABC導字會/segment1/segment2/this segment has spaces too!!!/ExtendedAscii_Æ_ß_€_ñ/file.txt";
    // With some special characters have to encode before using URI library
    String encodedPvfsURI = UriParser.encode( pvfsUriWithSpaces, new char[]{' ', '@', '$', '!'}  ); // NOTE can be more characters
    Map<String, String> map = parseUsingJavaStandardLibraries( encodedPvfsURI );
    assertEqualsPvfsMap( "pvfs",
      "ABC導字會",
      "/segment1/segment2/this segment has spaces too!!!/ExtendedAscii_Æ_ß_€_ñ/file.txt",
      "file.txt",
      map );
  }


  @Test
  public void test_regex_Extended_ASCII_Fail_1() throws Exception {
    String fullAsciiStr = "ABCÆß€ñ"; // extended ascii alpha numeric and euro currency symbol
    Pattern patternASCII = Pattern.compile( "\\w+" ); // DID not set unicode

    Matcher matcherASCII = patternASCII.matcher( fullAsciiStr );
    assertTrue( matcherASCII.find() );
    assertEquals( fullAsciiStr, matcherASCII.group() ); // fails here
  }

  @Test
  public void test_regex_Extended_ASCII_Fail_2() throws Exception {
    String fullAsciiStr = "ABCÆß€ñ";  // extended ascii alpha numeric and euro currency symbol
    Pattern patternASCII = Pattern.compile( "[A-Za-zÀ-ȕ0-9(),-_., ]+" ); // DID not set unicode

    Matcher matcherASCII = patternASCII.matcher( fullAsciiStr );
    assertTrue( matcherASCII.find() );
    assertEquals( fullAsciiStr, matcherASCII.group() ); // fails here
  }

  @Test
  public void test_regex_Extended_ASCII_Pass() throws Exception {
    String fullAsciiStr = "ABCÆß€ñ";  // extended ascii alpha numeric and euro currency symbol
    Pattern patternASCII = Pattern.compile( "[\\p{L}\\p{S}]+" ); // DID not set unicode

    Matcher matcherASCII = patternASCII.matcher( fullAsciiStr );
    assertTrue( matcherASCII.find() );
    assertEquals( fullAsciiStr, matcherASCII.group() ); // fails here
  }

  @Test
  public void test_regex_Extended_ASCII_Pass_wildcard() throws Exception {
    String fullAsciiStr = "ABCÆß€ñ";  // extended ascii alpha numeric and euro currency symbol
    Pattern patternASCII = Pattern.compile( ".+" ); // wild card match

    Matcher matcherASCII = patternASCII.matcher( fullAsciiStr );
    assertTrue( matcherASCII.find() );
    assertEquals( fullAsciiStr, matcherASCII.group() ); // fails here
  }

  @Test
  public void test_regex_UTF_Fail() throws Exception {
    String engStr = "ABC";
    String chinesStr = "導字會";
    String engChinesStr = engStr + chinesStr;
    Pattern patternASCII = Pattern.compile( "\\w+" ); // DID not set unicode

    Matcher matcherASCII = patternASCII.matcher( engChinesStr );
    assertTrue( matcherASCII.find() );
    assertTrue( matcherASCII.group().contains( engStr )  );
    assertEquals( engChinesStr, matcherASCII.group() ); // fails here
  }

  public void test_regex_UTF_Pass_1() throws Exception {
    String engStr = "ABC";
    String chinesStr = "導字會";
    String engChinesStr = engStr + chinesStr;
    Pattern patternUnicode = Pattern.compile( "\\w+", Pattern.UNICODE_CHARACTER_CLASS ); // setting unicode

    Matcher matcherASCII = patternUnicode.matcher( engChinesStr );
    assertTrue( matcherASCII.find() );
    assertTrue( matcherASCII.group().contains( engStr )  );
    assertEquals( engChinesStr, matcherASCII.group() );
  }

  public void test_regex_UTF_Pass_2() throws Exception {
    String engStr = "ABC";
    String chinesStr = "導字會";
    String engChinesStr = engStr + chinesStr;
    Pattern patternUnicode = Pattern.compile( "\\p{L}+", Pattern.UNICODE_CHARACTER_CLASS ); // setting unicode

    Matcher matcherASCII = patternUnicode.matcher( engChinesStr );
    assertTrue( matcherASCII.find() );
    assertTrue( matcherASCII.group().contains( engStr )  );
    assertEquals( engChinesStr, matcherASCII.group() );
  }

  protected Map<String, String> parseUsingJavaStandardLibraries( String uriString ) throws Exception {
    Map<String, String> map = new HashMap<>();
    URI uri = new URI( uriString );
    map.put( KEY_SCHEME, uri.getScheme() );
    map.put( KEY_CONNECTION_NAME, uri.getAuthority() );
    map.put( KEY_CONNECTION_PATH, uri.getPath() );

    // get last segment
    Path path = Paths.get( uri.toString() );
    map.put( KEY_FILENAME, path.getNameCount() > 0
        ? path.getName(  path.getNameCount() - 1 ).toString()
        : null );
    return map;
  }

  protected void assertEqualsPvfsMap( String scheme, String connectionName, String connectionPath, String filename, Map<String, String> map ) {
    assertEquals( scheme, map.getOrDefault( KEY_SCHEME, "<not-found-scheme>" ) );
    assertEquals( connectionName, map.getOrDefault( KEY_CONNECTION_NAME, "<not-found-connection-name>" ) );
    assertEquals( connectionPath, map.getOrDefault( KEY_CONNECTION_PATH, "<not-found-connection-path>" ) );
    assertEquals( filename, map.getOrDefault( KEY_FILENAME, "<not-found-filename>" ) );
  }

}
