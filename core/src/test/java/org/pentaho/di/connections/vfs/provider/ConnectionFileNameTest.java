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
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ConnectionFileNameTest {

  @Test
  public void testPvfsRoot() throws Exception {
    assertPvsRoot( new ConnectionFileName( null ) );
    assertPvsRoot( new ConnectionFileName( "" ) );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testPvfsRootRequiresRootAbsPath() {
    new ConnectionFileName( null, "/folder", FileType.FOLDER );
  }

  @Test
  public void testConnectionRoot() throws Exception {
    assertConnectionRoot( "someConnection" );
    assertConnectionRoot( "some_Connection" );
    assertConnectionRoot( "some-Connection" );
    assertConnectionRoot( "someConnection123" );
    assertConnectionRoot( "123someConnection123" );
    assertConnectionRoot( "Special Character name &#! <>" );
    assertConnectionRoot( "Connection %25 With Percentage In Name", "Connection % With Percentage In Name" );

    assertConnectionRoot( new ConnectionFileName( "connection", null, FileType.FOLDER ), "connection", "connection" );
    assertConnectionRoot( new ConnectionFileName( "connection", "", FileType.FOLDER ), "connection", "connection" );
    assertConnectionRoot( new ConnectionFileName( "connection", "/", FileType.FOLDER ), "connection", "connection" );
  }

  @Test
  public void testNonRoot() throws Exception {
    assertNonRoot( "connection", "/file", FileType.FILE );
    assertNonRoot( "connection", "/folder/file", FileType.FILE );

    // Trims Trailing Path Separator
    assertComponents(
      new ConnectionFileName( "connection", "/folder/", FileType.FOLDER ),
      "connection", "/folder", FileType.FOLDER );

    // Special characters in path
    String absPath = "/my folder %25 with special Character name &#! <>";
    ConnectionFileName fileName =
      new ConnectionFileName( "connection", absPath, FileType.FOLDER );
    assertComponents( fileName, "connection", absPath, FileType.FOLDER );
    assertEquals( "/my folder % with special Character name &#! <>", fileName.getPathDecoded() );
  }

  private static void assertPvsRoot( ConnectionFileName fileName ) throws FileSystemException {
    assertTrue( fileName.isPvfsRoot() );
    assertFalse( fileName.isConnectionRoot() );
    assertEquals( ConnectionFileProvider.SCHEME, fileName.getScheme() );
    assertNull( fileName.getConnection() );
    assertNull( fileName.getConnectionDecoded() );
    assertEquals( FileName.SEPARATOR, fileName.getPath() );

    assertEquals( "pvfs://", fileName.getRootURI() );
    assertEquals( "pvfs://", fileName.getURI() );
    assertEquals( "pvfs://", fileName.getFriendlyURI() );
  }

  private static void assertConnectionRoot( @NonNull String encodedConnectionName ) throws FileSystemException {
    assertConnectionRoot( encodedConnectionName, encodedConnectionName );
  }

  private static void assertConnectionRoot( @NonNull String encodedConnectionName,
                                            @NonNull String decodedConnectionName ) throws FileSystemException {
    assertConnectionRoot(
      new ConnectionFileName( encodedConnectionName ),
      encodedConnectionName,
      decodedConnectionName );
  }

  private static void assertConnectionRoot( @NonNull ConnectionFileName fileName,
                                            @NonNull String encodedConnectionName,
                                            @NonNull String decodedConnectionName ) throws FileSystemException {
    assertComponents( fileName, encodedConnectionName, FileName.SEPARATOR, FileType.FOLDER );
    assertFalse( fileName.isPvfsRoot() );
    assertTrue( fileName.isConnectionRoot() );
    assertEquals( decodedConnectionName, fileName.getConnectionDecoded() );

    String uri = "pvfs://" + encodedConnectionName + "/";
    assertEquals( uri, fileName.getURI() );
    assertEquals( uri, fileName.getFriendlyURI() );
  }

  private static void assertNonRoot( @NonNull String encodedConnectionName,
                                     @Nullable String encodedPath,
                                     @NonNull FileType fileType ) {
    ConnectionFileName fileName = new ConnectionFileName( encodedConnectionName, encodedPath, fileType );

    assertComponents( fileName, encodedConnectionName, encodedPath, fileType );
    assertFalse( fileName.isPvfsRoot() );
    assertFalse( fileName.isConnectionRoot() );

    String uri = "pvfs://" + encodedConnectionName + encodedPath;
    assertEquals( uri, fileName.getURI() );
    assertEquals( uri, fileName.getFriendlyURI() );
  }

  private static void assertComponents( @NonNull ConnectionFileName fileName,
                                        @NonNull String encodedConnectionName,
                                        @Nullable String encodedPath,
                                        @NonNull FileType fileType ) {

    assertEquals( ConnectionFileProvider.SCHEME, fileName.getScheme() );
    assertEquals( encodedConnectionName, fileName.getConnection() );
    assertEquals( encodedPath, fileName.getPath() );
    assertEquals( fileType, fileName.getType() );
  }
}
