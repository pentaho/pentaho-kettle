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

package org.pentaho.di.plugins.fileopensave.providers.vfs;

import junit.framework.TestCase;
import org.junit.Test;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.vfs.VFSConnectionDetails;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSFile;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VFSFileProviderTest extends TestCase {

  public void testIsConnectionRoot() {

    VFSFileProvider testInstance = createTestInstance();

    // TEST - simple negative tests before "pvfs://domain"
    assertFalse( testInstance.isConnectionRoot( null ) );

    assertFalse( testInstance.isConnectionRoot( createTestInstance( null ) ) );

    assertFalse( testInstance.isConnectionRoot( createTestInstance( "" ) ) );

    assertFalse( testInstance.isConnectionRoot( createTestInstance( " " ) ) );

    assertFalse( testInstance.isConnectionRoot( createTestInstance( "pvfs" ) ) );

    assertFalse( testInstance.isConnectionRoot( createTestInstance( "pvfs:" ) ) );

    assertFalse( testInstance.isConnectionRoot( createTestInstance( "pvfs:/" ) ) );

    assertFalse( testInstance.isConnectionRoot( createTestInstance( "pvfs://" ) ) );

    assertFalse( testInstance.isConnectionRoot( createTestInstance( "pvfs:///" ) ) );

    // TEST "pvfs://domain"
    assertTrue( testInstance.isConnectionRoot( createTestInstance( "pvfs://someConnection" ) ) );

    assertTrue( testInstance.isConnectionRoot( createTestInstance( "pvfs://some_Connection" ) ) );

    assertTrue( testInstance.isConnectionRoot( createTestInstance( "pvfs://some-Connection" ) ) );

    assertTrue( testInstance.isConnectionRoot( createTestInstance( "pvfs://someConnection123" ) ) );

    assertTrue( testInstance.isConnectionRoot( createTestInstance( "pvfs://123someConnection123" ) ) );

    assertTrue( testInstance.isConnectionRoot( createTestInstance( "pvfs://someConnection/" ) ) );

    assertTrue( testInstance.isConnectionRoot( createTestInstance( "pvfs://some_Connection/" ) ) );

    assertTrue( testInstance.isConnectionRoot( createTestInstance( "pvfs://some-Connection/" ) ) );

    assertTrue( testInstance.isConnectionRoot( createTestInstance( "pvfs://someConnection123/" ) ) );

    assertTrue( testInstance.isConnectionRoot( createTestInstance( "pvfs://123someConnection123/" ) ) );

    assertTrue( testInstance.isConnectionRoot( createTestInstance( "pvfs://Special Character name &#! <>/" ) ) );

    assertTrue( testInstance.isConnectionRoot( createTestInstance( "pvfs://Special Character name &#! <>" ) ) );

    // TEST now we have past the root domain
    assertFalse( testInstance.isConnectionRoot( createTestInstance( "pvfs://someConnection/someFolderA" ) ) );

    assertFalse( testInstance.isConnectionRoot(
        createTestInstance( "pvfs://someConnection/someFolderA/directory2" ) ) );

    assertFalse( testInstance.isConnectionRoot(
        createTestInstance( "pvfs://someConnection/someFolderA/directory2/randomFileC.txt" ) ) );

    assertFalse( testInstance.isConnectionRoot(
      createTestInstance( "pvfs://Special Character name &#! <>/someFolderA/directory2/randomFileC.txt" ) ) );
  }

  public void testHasBuckets() {

    VFSFileProvider testInstance = createTestInstance();

    // TEST non VFSConnectionDetails
    ConnectionDetails mockConnectionDetails  = mock( ConnectionDetails.class );
    assertFalse( testInstance.hasBuckets( mockConnectionDetails ) );

    // TEST  does not have buckets
    VFSConnectionDetails mock_NoBuckets_VFSConnectionDetails  = mock( VFSConnectionDetails.class );
    when( mock_NoBuckets_VFSConnectionDetails.hasBuckets() ).thenReturn( false );
    assertFalse( testInstance.hasBuckets( mock_NoBuckets_VFSConnectionDetails ) );

    // TEST has buckets
    VFSConnectionDetails mock_Buckets_VFSConnectionDetails  = mock( VFSConnectionDetails.class );
    when( mock_Buckets_VFSConnectionDetails.hasBuckets() ).thenReturn( true );
    assertTrue( testInstance.hasBuckets( mock_Buckets_VFSConnectionDetails ) );
  }

  @Test
  public void testIsSupported() throws Exception {

    VFSFileProvider testInstance = createTestInstance();

    assertFalse( testInstance.isSupported( null ) );

    assertFalse( testInstance.isSupported( "" ) );

    assertFalse( testInstance.isSupported( "    " ) );

    assertFalse( testInstance.isSupported( "someGarbage" ) );

    assertFalse( testInstance.isSupported( "/someUser/someUnixFile" ) );

    assertFalse( testInstance.isSupported( "T:\\Users\\RandomSUser\\Documents\\someWindowsFile" ) );

    assertFalse( testInstance.isSupported( "//home/randomUser/randomFile.rpt" ) );

    assertTrue( testInstance.isSupported( "pvfs://someConnection/someFilePath" ) );

    assertTrue( testInstance.isSupported( "pvfs://Special Character name &#! <>/someFilePath" ) );
  }

  @Test
  public void testGetConnectionName() throws Exception {

    VFSFileProvider testInstance = createTestInstance();

    assertNull( testInstance.getConnectionName( createTestInstance( null ) ) );

    assertNull( testInstance.getConnectionName( createTestInstance( "" ) ) );

    assertNull( testInstance.getConnectionName( createTestInstance( "    " ) ) );

    assertNull( testInstance.getConnectionName( createTestInstance( "someGarbage" ) ) );

    assertNull( testInstance.getConnectionName( createTestInstance( "xyz:/123" ) ) ); // missing slash "/"

    assertNull( testInstance.getConnectionName( createTestInstance( "xyz://" ) ) );

    assertEquals( "abc", testInstance.getConnectionName( createTestInstance( "xyz://abc" ) ) );

    assertEquals( "abc", testInstance.getConnectionName( createTestInstance( "xyz://abc/" ) ) );

    assertEquals( "abc", testInstance.getConnectionName( createTestInstance( "xyz://abc/def/ghi/jkl/mno.csv" ) ) );

    assertEquals( "Special Character name &#! <>", testInstance.getConnectionName(
        createTestInstance( "xyz://Special Character name &#! <>/def/ghi/jkl/mno.csv" ) ) );
  }

  @Test
  public void testGetScheme() throws Exception {

    VFSFileProvider testInstance = createTestInstance();

    assertNull( testInstance.getScheme( createTestInstance( null ) ) );

    assertNull( testInstance.getScheme( createTestInstance( "" ) ) );

    assertNull( testInstance.getScheme( createTestInstance( "    " ) ) );

    assertNull( testInstance.getScheme( createTestInstance( "someGarbage" ) ) );

    assertEquals( "xyz", testInstance.getScheme( createTestInstance( "xyz://abc" ) ) );

    assertEquals( "xyz", testInstance.getScheme(
        createTestInstance( "xyz://abc/def/ghi/jkl/mno.csv" ) ) );

    assertEquals( "xyz", testInstance.getScheme(
        createTestInstance( "xyz://Special Character name &#! <>/def/ghi/jkl/mno.csv" ) ) );
  }

  @Test
  public void testIsSame_DifferentTypes() throws Exception {
    VFSFileProvider testInstance = createTestInstance();

    File file1 = mock( File.class );
    File file2 = mock( File.class );

    File vfsFile = createTestInstance( "xyz://abc/someDir/somePath/someFile.txt" );

    assertFalse( testInstance.isSame( file1, file2 ) );

    assertFalse( testInstance.isSame( file1, vfsFile ) );

    assertFalse( testInstance.isSame( vfsFile, file2 ) );

    File vfsFile_ABC_SomeDir = createTestInstance( "xyz://abc/someDir/anotherDir/someFile.txt" );

    File vfsFile_ABC_FolderA = createTestInstance( "xyz://abc/FolderA" );

    File vfsFile_MNO_Path1 = createTestInstance( "xyz://mno/Path1/Path2" );

    assertFalse( testInstance.isSame( vfsFile_ABC_SomeDir, vfsFile_MNO_Path1 ) );

    assertFalse( testInstance.isSame( vfsFile_MNO_Path1, vfsFile_ABC_SomeDir ) );

    assertFalse( testInstance.isSame( createTestInstance( "xyz://" ), vfsFile_ABC_SomeDir ) ); // malformed VFS files

    assertFalse( testInstance.isSame( createTestInstance( "xyz:/" ), vfsFile_ABC_SomeDir ) ); // malformed VFS files

    assertTrue( testInstance.isSame( vfsFile_ABC_SomeDir, vfsFile_ABC_SomeDir ) );

    assertTrue( testInstance.isSame( vfsFile_ABC_SomeDir, vfsFile_ABC_FolderA ) );

    File vfsFile_SpecialCharacters_Path1 = createTestInstance( "xyz://Special Character name &#! <>/Path1/Path2" );

    File vfsFile_SpecialCharacters_DirectoryA = createTestInstance( "xyz://Special Character name &#! <>/DirectoryA/DirectoryB/DirectoryC" );

    assertTrue( testInstance.isSame( vfsFile_SpecialCharacters_Path1, vfsFile_SpecialCharacters_DirectoryA ) );

    assertFalse( testInstance.isSame( vfsFile_SpecialCharacters_Path1, vfsFile_ABC_SomeDir ) );
  }

  protected VFSFile createTestInstance( String path ) {
    VFSFile vfsFile = new VFSFile();
    vfsFile.setPath( path );
    return vfsFile;
  }

  public VFSFileProvider createTestInstance() {
    VFSFileProvider testInstance = new VFSFileProvider( null, null );
    return testInstance;
  }

}
