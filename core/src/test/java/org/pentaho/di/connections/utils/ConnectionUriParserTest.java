/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2024 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.di.connections.utils;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.pentaho.di.connections.utils.ConnectionUriParser.SPECIAL_CHARACTERS_FULL_SET;

public class ConnectionUriParserTest extends TestCase {

  /**
   * Full set of alphanumeric characters the connection name can be.
   */
  public static final String ALPHANUMERIC_CHARACTERS_FULL_SET =
    "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

  /**
   * Full set of accepted characters the connection name can be.
   */
  public static final String ACCEPTED_CHARACTERS_FULL_SET =
    SPECIAL_CHARACTERS_FULL_SET + ALPHANUMERIC_CHARACTERS_FULL_SET;

  @Test
  public void testConnectionUriParser_Negative_Example_URIs() throws Exception {

    assertNullValues( new ConnectionUriParser( null ) );

    assertNullValues( new ConnectionUriParser( "" ) );

    assertNullValues( new ConnectionUriParser( "      " ) );

    assertNullValues( new ConnectionUriParser( "someGarbage" ) );

  }

  @Test
  public void testConnectionUriParser_Negative_Example_Non_URIs() throws Exception {

    assertNullValues( new ConnectionUriParser( "/someUser/someUnixFile" ) );

    assertNullValues( new ConnectionUriParser(  "T:\\Users\\RandomSUser\\Documents\\someWindowsFile" ) );

    assertNullValues( new ConnectionUriParser(  "//home/randomUser/randomFile.rpt" ) ); // Pentaho repository

  }

  @Test
  public void testConnectionUriParser_Example_URIs() throws Exception {

    String uri_01 = "xyz://";
    assertEquals( "xyz", new ConnectionUriParser( uri_01 ) );

    String uri_02 = "xyz:///";
    assertEquals( "xyz", new ConnectionUriParser( uri_02 ) );

    String uri_03 = "pvfs://";
    assertEquals( "pvfs", new ConnectionUriParser( uri_03 ) );

    String uri_04 = "pvfs:///";
    assertEquals( "pvfs", new ConnectionUriParser( uri_04 ) );

    String uri_05 = "xyz://abc";
    assertEquals( "xyz", "abc", new ConnectionUriParser( uri_05 ) );

    String uri_06 = "xyz://abc/";
    assertEquals( "xyz", "abc", new ConnectionUriParser( uri_06 ) );

    String uri_07 = "xyz://abc/someDir/somePath/someFile.txt";
    assertEquals( "xyz",
      "abc",
      "/someDir/somePath/someFile.txt",
      new ConnectionUriParser( uri_07 ) );

    String uri_08 = "pvfs://some-ConnectionName_123/";
    assertEquals( "pvfs",
      "some-ConnectionName_123",
      new ConnectionUriParser( uri_08 ) );

    String uri_09 = "pvfs://some-ConnectionName_123";
    assertEquals( "pvfs",
      "some-ConnectionName_123",
      new ConnectionUriParser( uri_09 ) );

    String uri_10 = "pvfs://some-ConnectionName_123/someFolderA/someFolderB/someFolderC/sales_data.csv";
    assertEquals( "pvfs",
        "some-ConnectionName_123",
        "/someFolderA/someFolderB/someFolderC/sales_data.csv",
        new ConnectionUriParser( uri_10 ) );

    // TEST : can handle special characters, passing connection name as-is per "current" requirements based on connection creation logic
    String uri_11 = "pvfs://Special Character name &#! <> why would you do this/someFolderA/someFolderB/someFolderC/sales_data.csv";
    assertEquals( "pvfs",
      "Special Character name &#! <> why would you do this",
      "/someFolderA/someFolderB/someFolderC/sales_data.csv",
      new ConnectionUriParser( uri_11 ) );

    String uri_12 = "pvfs://Special Character name &#! <> why would you do this";
    assertEquals( "pvfs",
      "Special Character name &#! <> why would you do this",
      new ConnectionUriParser( uri_12 ) );

    String uri_13 = "pvfs://Special Character name &#! <> why would you do this/";
    assertEquals( "pvfs",
      "Special Character name &#! <> why would you do this",
      new ConnectionUriParser( uri_13 ) );

    String specialCharacterConnectionName = shuffleString( ACCEPTED_CHARACTERS_FULL_SET );

    // want to make sure it has a good representation of characters
    assertTrue( specialCharacterConnectionName.length() > 60 );
    // sanity checks and guard against changes to source code
    assertTrue( specialCharacterConnectionName.contains( "<" ) && SPECIAL_CHARACTERS_FULL_SET.contains( "<" ) );
    assertTrue( specialCharacterConnectionName.contains( "$" ) && SPECIAL_CHARACTERS_FULL_SET.contains( "$" ) );
    assertTrue( specialCharacterConnectionName.contains( "-" ) && SPECIAL_CHARACTERS_FULL_SET.contains( "-" ) );
    assertTrue( specialCharacterConnectionName.contains( "_" ) && SPECIAL_CHARACTERS_FULL_SET.contains( "_" ) );
    assertTrue( specialCharacterConnectionName.contains( " " ) && SPECIAL_CHARACTERS_FULL_SET.contains( " " ) );
    assertTrue( specialCharacterConnectionName.contains( "<" ) && SPECIAL_CHARACTERS_FULL_SET.contains( "<" ) );
    assertTrue( specialCharacterConnectionName.contains( "!" ) && SPECIAL_CHARACTERS_FULL_SET.contains( "!" ) );

    // TEST robust example of special characters
    String uri_14 = "pvfs://" + specialCharacterConnectionName;
    assertEquals( "pvfs",
      specialCharacterConnectionName,
      new ConnectionUriParser( uri_14 ) );

    String uri_15 = "pvfs://" + specialCharacterConnectionName + "/";
    assertEquals( "pvfs",
      specialCharacterConnectionName,
      new ConnectionUriParser( uri_15 ) );

    String absolutePVFSPath = "/someFolderA/someFolderB/someFolderC/sales_data.csv";

    String uri_16 = "pvfs://" + specialCharacterConnectionName + absolutePVFSPath;
    assertEquals( "pvfs",
      specialCharacterConnectionName,
      absolutePVFSPath,
      new ConnectionUriParser( uri_16 ) );

    // Test to verify internation characters are possible
    String internationalConnectionName = "some_Cool_characters_é";

    String uri_17 = "pvfs://" + internationalConnectionName + absolutePVFSPath;
    assertEquals( "pvfs",
      internationalConnectionName,
      absolutePVFSPath,
      new ConnectionUriParser( uri_17 ) );

  }

  protected String shuffleString( String characterSet ) {
    List<Character> characterSetList = characterSet.chars().mapToObj( c -> (char) c ).collect( Collectors.toList() );
    Collections.shuffle( characterSetList );
    StringBuilder sb = new StringBuilder( characterSet.length() );
    characterSetList.stream().forEach( sb::append );
    return sb.toString();

  }

  protected void assertEquals( String expectedScheme, String expectedConnectionName, String expectedPvfsPath,
                              ConnectionUriParser actualConnectionUriParser ) {
    assertEquals( expectedScheme, actualConnectionUriParser.getScheme() );
    assertEquals( expectedConnectionName, actualConnectionUriParser.getConnectionName() );
    assertEquals( expectedPvfsPath, actualConnectionUriParser.getConnectionPath() );
  }

  protected void assertEquals( String expectedScheme, String expectedConnectionName,
                               ConnectionUriParser actualConnectionUriParser ) {
    assertEquals( expectedScheme, actualConnectionUriParser.getScheme() );
    assertEquals( expectedConnectionName, actualConnectionUriParser.getConnectionName() );
    assertEquals( null, actualConnectionUriParser.getConnectionPath() );
  }

  protected void assertEquals( String expectedScheme,
                               ConnectionUriParser actualConnectionUriParser ) {
    assertEquals( expectedScheme, actualConnectionUriParser.getScheme() );
    assertEquals( null, actualConnectionUriParser.getConnectionName() );
    assertEquals( null, actualConnectionUriParser.getConnectionPath() );
  }

  protected void assertNullValues( ConnectionUriParser actualConnectionUriParser ) {
    assertEquals( null, null, null, actualConnectionUriParser );
  }

}
