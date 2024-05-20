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
import org.pentaho.di.connections.vfs.VFSConnectionManagerHelper;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSFile;
import org.pentaho.di.plugins.fileopensave.providers.vfs.service.KettleVFSService;

import static org.mockito.Mockito.mock;

public class VFSFileProviderTest extends TestCase {

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

    assertFalse( testInstance.isSupported( "xyz://some/path" ) );

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

    assertNull( testInstance.getConnectionName( createTestInstance( "pvfs:/123" ) ) ); // missing slash "/"

    assertNull( testInstance.getConnectionName( createTestInstance( "pvfs://" ) ) );

    assertNull( testInstance.getConnectionName( createTestInstance( "xyz://" ) ) );

    assertEquals( "abc", testInstance.getConnectionName( createTestInstance( "pvfs://abc" ) ) );

    assertEquals( "abc", testInstance.getConnectionName( createTestInstance( "pvfs://abc/" ) ) );

    assertEquals( "abc", testInstance.getConnectionName( createTestInstance( "pvfs://abc/def/ghi/jkl/mno.csv" ) ) );

    assertEquals( "Special Character name &#! <>", testInstance.getConnectionName(
        createTestInstance( "pvfs://Special Character name &#! <>/def/ghi/jkl/mno.csv" ) ) );
  }

  @Test
  public void testIsSame_DifferentTypes() throws Exception {
    VFSFileProvider testInstance = createTestInstance();

    File file1 = mock( File.class );
    File file2 = mock( File.class );

    File vfsFile = createTestInstance( "pvfs://abc/someDir/somePath/someFile.txt" );

    assertFalse( testInstance.isSame( file1, file2 ) );

    assertFalse( testInstance.isSame( file1, vfsFile ) );

    assertFalse( testInstance.isSame( vfsFile, file2 ) );

    // ---

    // Different schemes
    assertFalse( testInstance.isSame( createTestInstance( "xyz://abc" ), createTestInstance( "pvfs://abc" ) ) );
    assertFalse( testInstance.isSame( createTestInstance( "pvfs://abc" ), createTestInstance( "xyz://abc" ) ) );

    // ---

    File vfsFile_ABC_SomeDir = createTestInstance( "pvfs://abc/someDir/anotherDir/someFile.txt" );

    File vfsFile_ABC_FolderA = createTestInstance( "pvfs://abc/FolderA" );

    File vfsFile_MNO_Path1 = createTestInstance( "pvfs://mno/Path1/Path2" );

    assertFalse( testInstance.isSame( vfsFile_ABC_SomeDir, vfsFile_MNO_Path1 ) );

    assertFalse( testInstance.isSame( vfsFile_MNO_Path1, vfsFile_ABC_SomeDir ) );

    assertFalse( testInstance.isSame( createTestInstance( "pvfs://" ), vfsFile_ABC_SomeDir ) ); // malformed VFS files

    assertFalse( testInstance.isSame( createTestInstance( "pvfs:/" ), vfsFile_ABC_SomeDir ) ); // malformed VFS files

    assertTrue( testInstance.isSame( vfsFile_ABC_SomeDir, vfsFile_ABC_SomeDir ) );

    assertTrue( testInstance.isSame( vfsFile_ABC_SomeDir, vfsFile_ABC_FolderA ) );

    File vfsFile_SpecialCharacters_Path1 = createTestInstance( "pvfs://Special Character name &#! <>/Path1/Path2" );

    File vfsFile_SpecialCharacters_DirectoryA =
      createTestInstance( "pvfs://Special Character name &#! <>/DirectoryA/DirectoryB/DirectoryC" );

    assertTrue( testInstance.isSame( vfsFile_SpecialCharacters_Path1, vfsFile_SpecialCharacters_DirectoryA ) );

    assertFalse( testInstance.isSame( vfsFile_SpecialCharacters_Path1, vfsFile_ABC_SomeDir ) );
  }

  protected VFSFile createTestInstance( String path ) {
    VFSFile vfsFile = new VFSFile();
    vfsFile.setPath( path );
    return vfsFile;
  }

  public VFSFileProvider createTestInstance() {
    return new VFSFileProvider(
      mock( Bowl.class ), mock( KettleVFSService.class ), new VFSConnectionManagerHelper() );
  }
}
