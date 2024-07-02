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

package org.pentaho.di.connections.vfs.provider;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.pentaho.di.connections.vfs.provider.ConnectionFileNameParser.CONNECTION_NAME_INVALID_CHARACTERS;
import static org.pentaho.di.connections.vfs.provider.ConnectionFileNameParser.SPECIAL_CHARACTERS;

public class ConnectionFileNameParserTest {

  /**
   * Full set of alphanumeric characters the connection name can be.
   */
  public static final String ALPHANUMERIC_CHARACTERS_FULL_SET =
    "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

  /**
   * Full set of accepted characters the connection name can be.
   */
  public static final String ACCEPTED_CHARACTERS_FULL_SET =
    SPECIAL_CHARACTERS + ALPHANUMERIC_CHARACTERS_FULL_SET;

  private ConnectionFileNameParser fileNameParser;

  @Before
  public void setup() {
    fileNameParser = new ConnectionFileNameParser();
  }

  // region parseUri, isPvfsRoot, isConnectionRoot
  @Test
  public void testParseUri_Negative_Example_URIs() {
    assertParseUriThrows( fileNameParser, "" );
    assertParseUriThrows( fileNameParser, "   " );
    assertParseUriThrows( fileNameParser, "someGarbage" );
  }

  @Test
  public void testParseUri_Negative_Example_Non_URIs() {
    assertParseUriThrows( fileNameParser, "/someUser/someUnixFile" );
    assertParseUriThrows( fileNameParser, "T:\\Users\\RandomSUser\\Documents\\someWindowsFile" );
    assertParseUriThrows( fileNameParser, "//home/randomUser/randomFile.rpt" ); // Pentaho repository
  }

  @Test
  public void testParseUri_Negative_Example_Non_URIs_OtherSchemes() {
    assertParseUriThrows( fileNameParser, "xyz://" );
    assertParseUriThrows( fileNameParser, "xyz://domain/path" );
  }

  @Test
  public void testParseUri_Negative_Example_URIs_PVFSRoot() {
    assertParseUriThrows( fileNameParser, "pvfs" );
    assertParseUriThrows( fileNameParser, "pvfs:" );
    assertParseUriThrows( fileNameParser, "pvfs:/" );
  }

  @Test
  public void testParseUri_Negative_Example_URIs_ConnectionWithInvalidEncoding() {
    // Valid URI would be "pvfs://ConnectionName%25WithInvalidEncoding"
    assertParseUriThrows( fileNameParser, "pvfs://ConnectionName%WithInvalidEncoding" );
  }

  @Test
  public void testParseUri_Example_URIs_PVFSRoot() throws Exception {
    assertParseUriComponentsPvfsRoot( fileNameParser, "pvfs://" );
    assertParseUriComponentsPvfsRoot( fileNameParser, "pvfs:///" );
  }

  @Test
  public void testParseUri_Example_URIs_ConnectionRoot() throws Exception {
    assertParseUriComponentsConnectionRoot( fileNameParser, "pvfs://someConnection", "someConnection" );
    assertParseUriComponentsConnectionRoot( fileNameParser, "pvfs://some_Connection", "some_Connection" );
    assertParseUriComponentsConnectionRoot( fileNameParser, "pvfs://some-Connection", "some-Connection" );
    assertParseUriComponentsConnectionRoot( fileNameParser, "pvfs://someConnection123", "someConnection123" );
    assertParseUriComponentsConnectionRoot( fileNameParser, "pvfs://123someConnection123", "123someConnection123" );

    assertParseUriComponentsConnectionRoot( fileNameParser, "pvfs://someConnection/", "someConnection" );
    assertParseUriComponentsConnectionRoot( fileNameParser, "pvfs://some_Connection/", "some_Connection" );
    assertParseUriComponentsConnectionRoot( fileNameParser, "pvfs://some-Connection/", "some-Connection" );
    assertParseUriComponentsConnectionRoot( fileNameParser, "pvfs://someConnection123/", "someConnection123" );
    assertParseUriComponentsConnectionRoot( fileNameParser, "pvfs://123someConnection123/", "123someConnection123" );
  }

  @Test
  public void testParseUri_Example_URIs_NonRoot() throws Exception {
    assertParseUriComponentsNonRoot( fileNameParser, "pvfs://someConnection/someFileA",
      "someConnection", "/someFileA", FileType.FILE );
    assertParseUriComponentsNonRoot( fileNameParser, "pvfs://someConnection/someFolderA/",
      "someConnection", "/someFolderA", FileType.FOLDER );

    assertParseUriComponentsNonRoot( fileNameParser, "pvfs://someConnection/someFolderA/file2",
      "someConnection", "/someFolderA/file2", FileType.FILE );
    assertParseUriComponentsNonRoot( fileNameParser, "pvfs://someConnection/someFolderA/directory2/",
      "someConnection", "/someFolderA/directory2", FileType.FOLDER );

    assertParseUriComponentsNonRoot( fileNameParser, "pvfs://someConnection/someFolderA/directory2/randomFileC.txt",
      "someConnection", "/someFolderA/directory2/randomFileC.txt", FileType.FILE );
    assertParseUriComponentsNonRoot( fileNameParser, "pvfs://someConnection/someFolderA/directory2/randomFileC.txt/",
      "someConnection", "/someFolderA/directory2/randomFileC.txt", FileType.FOLDER );
  }

  @SuppressWarnings( "ConstantValue" )
  @Test
  public void testParseUri_Example_URIs_SpecialCharacters() throws Exception {

    assertParseUriComponentsConnectionRoot( fileNameParser, "pvfs://Special Character name &#! <>",
      "Special Character name &#! <>" );
    assertParseUriComponentsConnectionRoot( fileNameParser, "pvfs://Special Character name &#! <>/",
      "Special Character name &#! <>" );

    // Connection name cannot have % in name, not even encoded
    assertParseUriThrows( fileNameParser, "pvfs://Percentage% in name/" );
    assertParseUriThrows( fileNameParser, "pvfs://Percentage%25 in name/" );

    // ---

    assertParseUriComponentsNonRoot(
      fileNameParser,
      "pvfs://Special Character name &#! <>/someFolderA/directory2/randomFileC.txt",
      "Special Character name &#! <>",
      "/someFolderA/directory2/randomFileC.txt",
      FileType.FILE );

    // ---

    String specialCharacterConnectionName = shuffleString( ACCEPTED_CHARACTERS_FULL_SET );

    // want to make sure it has a good representation of characters
    assertTrue( specialCharacterConnectionName.length() > 60 );
    // sanity checks and guard against changes to source code
    assertTrue( specialCharacterConnectionName.contains( "<" ) && SPECIAL_CHARACTERS.contains( "<" ) );
    assertTrue( specialCharacterConnectionName.contains( "$" ) && SPECIAL_CHARACTERS.contains( "$" ) );
    assertTrue( specialCharacterConnectionName.contains( "-" ) && SPECIAL_CHARACTERS.contains( "-" ) );
    assertTrue( specialCharacterConnectionName.contains( "_" ) && SPECIAL_CHARACTERS.contains( "_" ) );
    assertTrue( specialCharacterConnectionName.contains( " " ) && SPECIAL_CHARACTERS.contains( " " ) );
    assertTrue( specialCharacterConnectionName.contains( "<" ) && SPECIAL_CHARACTERS.contains( "<" ) );
    assertTrue( specialCharacterConnectionName.contains( "!" ) && SPECIAL_CHARACTERS.contains( "!" ) );

    assertParseUriComponentsConnectionRoot(
      fileNameParser,
      "pvfs://" + specialCharacterConnectionName,
      specialCharacterConnectionName );

    assertParseUriComponentsConnectionRoot(
      fileNameParser,
      "pvfs://" + specialCharacterConnectionName + "/",
      specialCharacterConnectionName );

    String absolutePVFSPath = "/someFolderA/someFolderB/someFolderC/sales_data.csv";

    assertParseUriComponentsNonRoot(
      fileNameParser,
      "pvfs://" + specialCharacterConnectionName + absolutePVFSPath,
      specialCharacterConnectionName,
      absolutePVFSPath,
      FileType.FILE );

    String internationalConnectionName = "some_Cool_characters_é";

    assertParseUriComponentsNonRoot(
      fileNameParser,
      "pvfs://" + internationalConnectionName + absolutePVFSPath,
      internationalConnectionName,
      absolutePVFSPath,
      FileType.FILE );
  }

  void assertParseUriThrows( @NonNull ConnectionFileNameParser fileNameParser, String uri ) {
    try {
      fileNameParser.parseUri( uri );
      fail( "Expected `FileSystemException` to be thrown." );
    } catch ( FileSystemException e ) {
      assertNotNull( e );
    }
  }

  @NonNull
  ConnectionFileName assertParseUriComponents( @NonNull ConnectionFileNameParser fileNameParser,
                                               String uri,
                                               @Nullable String encodedConnection,
                                               @NonNull String encodedPath,
                                               @NonNull FileType fileType )
    throws FileSystemException {

    ConnectionFileName fileName = fileNameParser.parseUri( uri );

    assertEquals( ConnectionFileProvider.SCHEME, fileName.getScheme() );
    assertEquals( encodedConnection, fileName.getConnection() );
    assertEquals( encodedPath, fileName.getPath() );
    assertEquals( fileType, fileName.getType() );

    return fileName;
  }

  void assertParseUriComponentsPvfsRoot( @NonNull ConnectionFileNameParser fileNameParser, String uri )
    throws FileSystemException {

    ConnectionFileName fileName = assertParseUriComponents( fileNameParser, uri, null, "/", FileType.FOLDER );

    assertTrue( fileName.isPvfsRoot() );
    assertFalse( fileName.isConnectionRoot() );
  }

  void assertParseUriComponentsConnectionRoot( @NonNull ConnectionFileNameParser fileNameParser,
                                               String uri,
                                               @Nullable String encodedConnection )
    throws FileSystemException {

    ConnectionFileName fileName =
      assertParseUriComponents( fileNameParser, uri, encodedConnection, "/", FileType.FOLDER );

    assertFalse( fileName.isPvfsRoot() );
    assertTrue( fileName.isConnectionRoot() );
  }

  void assertParseUriComponentsNonRoot( @NonNull ConnectionFileNameParser fileNameParser,
                                        String uri,
                                        @Nullable String encodedConnection,
                                        @NonNull String encodedPath,
                                        @NonNull FileType fileType )
    throws FileSystemException {

    ConnectionFileName fileName =
      assertParseUriComponents( fileNameParser, uri, encodedConnection, encodedPath, fileType );

    assertFalse( fileName.isPvfsRoot() );
    assertFalse( fileName.isConnectionRoot() );
  }

  protected String shuffleString( String characterSet ) {
    List<Character> characterSetList = characterSet.chars().mapToObj( c -> (char) c ).collect( Collectors.toList() );
    Collections.shuffle( characterSetList );
    StringBuilder sb = new StringBuilder( characterSet.length() );
    characterSetList.forEach( sb::append );
    return sb.toString();
  }
  // endregion

  // region validateConnectionName
  @Test
  public void testValidateConnectionNameThrowsOnNullOrEmptyName() {
    assertValidateConnectionNameThrows( fileNameParser, null );
    assertValidateConnectionNameThrows( fileNameParser, "" );
  }

  @Test
  public void testValidateConnectionNameThrowsOnInvalidCharacters() {
    for ( char c : CONNECTION_NAME_INVALID_CHARACTERS.toCharArray() ) {
      assertValidateConnectionNameThrows( fileNameParser, "connection" + c + "name" );
    }
  }

  @Test
  public void testValidateConnectionNameAcceptsValidCharacters() throws FileSystemException {
    for ( char c : ACCEPTED_CHARACTERS_FULL_SET.toCharArray() ) {
      fileNameParser.validateConnectionName( "connection" + c + "name" );
    }
  }

  void assertValidateConnectionNameThrows( @NonNull ConnectionFileNameParser fileNameParser, String name ) {
    try {
      fileNameParser.validateConnectionName( name );
      fail( "Expected `FileSystemException` to be thrown." );
    } catch ( FileSystemException e ) {
      assertNotNull( e );
    }
  }
  // endregion

  // region isValidConnectionNameCharacter
  @Test
  public void testIsValidConnectionNameCharacterReturnsFalseForInvalidCharacters() {
    for ( char c : CONNECTION_NAME_INVALID_CHARACTERS.toCharArray() ) {
      assertFalse( fileNameParser.isValidConnectionNameCharacter( c ) );
    }
  }

  @Test
  public void testIsValidConnectionNameCharacterReturnsTrueOnValidCharacters() {
    for ( char c : ACCEPTED_CHARACTERS_FULL_SET.toCharArray() ) {
      assertTrue( fileNameParser.isValidConnectionNameCharacter( c ) );
    }
  }
  // endregion

  // region sanitizeConnectionName
  @Test
  public void testSanitizeConnectionNameRemovesInvalidCharacters() {
    String sanitizedName = fileNameParser.sanitizeConnectionName(
      "connection"
        + CONNECTION_NAME_INVALID_CHARACTERS
        + ACCEPTED_CHARACTERS_FULL_SET
        + "name" );

    for ( char c : CONNECTION_NAME_INVALID_CHARACTERS.toCharArray() ) {
      assertFalse( sanitizedName.indexOf( c ) >= 0 );
    }
  }

  @Test
  public void testSanitizeConnectionNamePreservesValidCharacters() {
    String sanitizedName = fileNameParser.sanitizeConnectionName(
      "connection"
        + CONNECTION_NAME_INVALID_CHARACTERS
        + ACCEPTED_CHARACTERS_FULL_SET
        + "name" );

    for ( char c : ACCEPTED_CHARACTERS_FULL_SET.toCharArray() ) {
      assertTrue( sanitizedName.indexOf( c ) >= 0 );
    }
  }
  // endregion
}
