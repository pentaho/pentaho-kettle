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

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.VFSConnectionDetails;
import org.pentaho.di.connections.vfs.VFSConnectionManagerHelper;
import org.pentaho.di.connections.vfs.VFSConnectionProvider;
import org.pentaho.di.connections.vfs.VFSRoot;
import org.pentaho.di.connections.vfs.provider.ConnectionFileName;
import org.pentaho.di.connections.vfs.provider.ConnectionFileNameParser;
import org.pentaho.di.connections.vfs.provider.ConnectionFileObject;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSDirectory;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSFile;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSLocation;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSTree;
import org.pentaho.di.plugins.fileopensave.providers.vfs.service.KettleVFSService;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VFSFileProviderTest {

  private VFSFileProvider vfsFileProvider;

  private Bowl bowl;

  private ConnectionManager connectionManager;

  private VFSConnectionManagerHelper vfsConnectionManagerHelper;

  private KettleVFSService kettleVFSService;

  @Before
  public void setup() throws Exception {
    bowl = mock( Bowl.class );
    connectionManager = mock( ConnectionManager.class );
    vfsConnectionManagerHelper = mock( VFSConnectionManagerHelper.class );
    kettleVFSService = mock( KettleVFSService.class );

    when( vfsConnectionManagerHelper.getConnectionRootFileName( any() ) ).thenCallRealMethod();

    when( bowl.getConnectionManager() ).thenReturn( connectionManager );

    vfsFileProvider = new VFSFileProvider(
      bowl,
      kettleVFSService,
      vfsConnectionManagerHelper,
      new ConnectionFileNameParser() );
  }

  @Test
  public void testIsSupported() throws Exception {

    assertFalse( vfsFileProvider.isSupported( null ) );

    assertFalse( vfsFileProvider.isSupported( "" ) );

    assertFalse( vfsFileProvider.isSupported( "    " ) );

    assertFalse( vfsFileProvider.isSupported( "someGarbage" ) );

    assertFalse( vfsFileProvider.isSupported( "/someUser/someUnixFile" ) );

    assertFalse( vfsFileProvider.isSupported( "T:\\Users\\RandomSUser\\Documents\\someWindowsFile" ) );

    assertFalse( vfsFileProvider.isSupported( "//home/randomUser/randomFile.rpt" ) );

    assertFalse( vfsFileProvider.isSupported( "xyz://some/path" ) );

    assertTrue( vfsFileProvider.isSupported( "pvfs://someConnection/someFilePath" ) );

    assertTrue( vfsFileProvider.isSupported( "pvfs://Special Character name &#! <>/someFilePath" ) );
  }

  @Test
  public void testGetConnectionName() throws Exception {

    assertNull( vfsFileProvider.getConnectionName( createTestFile( null ) ) );

    assertNull( vfsFileProvider.getConnectionName( createTestFile( "" ) ) );

    assertNull( vfsFileProvider.getConnectionName( createTestFile( "    " ) ) );

    assertNull( vfsFileProvider.getConnectionName( createTestFile( "someGarbage" ) ) );

    assertNull( vfsFileProvider.getConnectionName( createTestFile( "pvfs:/123" ) ) ); // missing slash "/"

    assertNull( vfsFileProvider.getConnectionName( createTestFile( "pvfs://" ) ) );

    assertNull( vfsFileProvider.getConnectionName( createTestFile( "xyz://" ) ) );

    assertEquals( "abc", vfsFileProvider.getConnectionName( createTestFile( "pvfs://abc" ) ) );

    assertEquals( "abc", vfsFileProvider.getConnectionName( createTestFile( "pvfs://abc/" ) ) );

    assertEquals( "abc", vfsFileProvider.getConnectionName( createTestFile( "pvfs://abc/def/ghi/jkl/mno.csv" ) ) );

    assertEquals( "Special Character name &#! <>", vfsFileProvider.getConnectionName(
      createTestFile( "pvfs://Special Character name &#! <>/def/ghi/jkl/mno.csv" ) ) );
  }

  @Test
  public void testIsSame_DifferentTypes() throws Exception {

    File file1 = mock( File.class );
    File file2 = mock( File.class );

    File vfsFile = createTestFile( "pvfs://abc/someDir/somePath/someFile.txt" );

    assertFalse( vfsFileProvider.isSame( file1, file2 ) );

    assertFalse( vfsFileProvider.isSame( file1, vfsFile ) );

    assertFalse( vfsFileProvider.isSame( vfsFile, file2 ) );

    // ---

    // Different schemes
    assertFalse( vfsFileProvider.isSame( createTestFile( "xyz://abc" ), createTestFile( "pvfs://abc" ) ) );
    assertFalse( vfsFileProvider.isSame( createTestFile( "pvfs://abc" ), createTestFile( "xyz://abc" ) ) );

    // ---

    File vfsFile_ABC_SomeDir = createTestFile( "pvfs://abc/someDir/anotherDir/someFile.txt" );

    File vfsFile_ABC_FolderA = createTestFile( "pvfs://abc/FolderA" );

    File vfsFile_MNO_Path1 = createTestFile( "pvfs://mno/Path1/Path2" );

    assertFalse( vfsFileProvider.isSame( vfsFile_ABC_SomeDir, vfsFile_MNO_Path1 ) );

    assertFalse( vfsFileProvider.isSame( vfsFile_MNO_Path1, vfsFile_ABC_SomeDir ) );

    assertFalse( vfsFileProvider.isSame( createTestFile( "pvfs://" ), vfsFile_ABC_SomeDir ) ); // malformed VFS files

    assertFalse( vfsFileProvider.isSame( createTestFile( "pvfs:/" ), vfsFile_ABC_SomeDir ) ); // malformed VFS files

    assertTrue( vfsFileProvider.isSame( vfsFile_ABC_SomeDir, vfsFile_ABC_SomeDir ) );

    assertTrue( vfsFileProvider.isSame( vfsFile_ABC_SomeDir, vfsFile_ABC_FolderA ) );

    File vfsFile_SpecialCharacters_Path1 = createTestFile( "pvfs://Special Character name &#! <>/Path1/Path2" );

    File vfsFile_SpecialCharacters_DirectoryA =
      createTestFile( "pvfs://Special Character name &#! <>/DirectoryA/DirectoryB/DirectoryC" );

    assertTrue( vfsFileProvider.isSame( vfsFile_SpecialCharacters_Path1, vfsFile_SpecialCharacters_DirectoryA ) );

    assertFalse( vfsFileProvider.isSame( vfsFile_SpecialCharacters_Path1, vfsFile_ABC_SomeDir ) );
  }

  // region getTree(..)
  @Test
  public void testGetTreeReturnsTreeWithOneVFSLocationPerConnection() throws Exception {
    VFSConnectionDetails details1 = mockDetails( "connection1" );
    VFSConnectionDetails details2 = mockDetails( "connection2" );
    VFSConnectionDetails details3 = mockDetails( "connection3" );

    when( vfsConnectionManagerHelper.getAllDetails( connectionManager ) )
      .thenReturn( List.of( details1, details2, details3 ) );

    VFSTree resultTree = (VFSTree) vfsFileProvider.getTree();

    assertEquals( VFSFileProvider.NAME, resultTree.getName() );

    List<VFSLocation> vfsConnectionFiles = resultTree.getChildren();

    assertNotNull( vfsConnectionFiles );
    assertEquals( 3, vfsConnectionFiles.size() );

    assertLocation( vfsConnectionFiles.get( 0 ), "connection1", "pvfs://connection1/" );
    assertLocation( vfsConnectionFiles.get( 1 ), "connection2", "pvfs://connection2/" );
    assertLocation( vfsConnectionFiles.get( 2 ), "connection3", "pvfs://connection3/" );
  }

  @Test
  public void testGetTreeReturnsVFSLocationsWhoseCanAddChildrenIsOppositeOfUsesBuckets() throws KettleException {
    VFSConnectionDetails details1 = mockDetails( "connection1" );
    mockDetailsUsesBuckets( details1, true );

    VFSConnectionDetails details2 = mockDetails( "connection2" );
    mockDetailsUsesBuckets( details2, false );

    when( vfsConnectionManagerHelper.getAllDetails( connectionManager ) )
      .thenReturn( List.of( details1, details2 ) );

    VFSTree resultTree = (VFSTree) vfsFileProvider.getTree();

    assertEquals( VFSFileProvider.NAME, resultTree.getName() );

    List<VFSLocation> vfsConnectionFiles = resultTree.getChildren();

    assertNotNull( vfsConnectionFiles );
    assertEquals( 2, vfsConnectionFiles.size() );

    assertFalse( vfsConnectionFiles.get( 0 ).isCanAddChildren() );
    assertTrue( vfsConnectionFiles.get( 1 ).isCanAddChildren() );
  }

  @Test
  public void testGetTreeWithConnectionTypeListFiltersReturnedLocations() throws Exception {
    VFSConnectionDetails details1 = mockDetails( "connection1", "typeA" );
    VFSConnectionDetails details2 = mockDetails( "connection2", "typeB" );
    VFSConnectionDetails details3 = mockDetails( "connection3", "typeC" );

    when( vfsConnectionManagerHelper.getAllDetails( connectionManager ) )
      .thenReturn( List.of( details1, details2, details3 ) );

    VFSTree resultTree = vfsFileProvider.getTree( List.of( "typeA", "typeC" ) );

    assertEquals( VFSFileProvider.NAME, resultTree.getName() );

    List<VFSLocation> vfsConnectionFiles = resultTree.getChildren();

    assertNotNull( vfsConnectionFiles );
    assertEquals( 2, vfsConnectionFiles.size() );

    assertEquals( "connection1", vfsConnectionFiles.get( 0 ).getName() );
    assertEquals( "connection3", vfsConnectionFiles.get( 1 ).getName() );
  }

  void assertLocation( VFSLocation location, String name, String uri ) {
    assertEquals( name, location.getName() );
    assertEquals( VFSFileProvider.NAME, location.getRoot() );
    assertEquals( uri, location.getPath() );
    assertTrue( location.hasChildren() );
    assertFalse( location.isCanDelete() );
    assertTrue( location.isCanAddChildren() );
  }
  // endregion

  // region getFiles of connection using buckets
  class GetFilesOfConnectionUsingBucketsScenario {
    public final VFSConnectionProvider<VFSConnectionDetails> provider;
    public final VFSConnectionDetails details1;

    public final VFSRoot bucketRoot1;
    public final VFSRoot bucketRoot2;

    public final VFSFile connectionRootFile;

    public GetFilesOfConnectionUsingBucketsScenario() throws KettleException {
      provider = mockProvider( "scheme1" );
      details1 = mockDetails( "connection1", "scheme1" );

      mockDetailsUsesBuckets( details1, true );
      mockDetailsProvider( details1, provider );

      bucketRoot1 = new VFSRoot( "bucket1", new Date( 1001 ) );
      bucketRoot2 = new VFSRoot( "bucket2", new Date( 1002 ) );

      mockDetailsProviderLocations( details1, provider, bucketRoot1, bucketRoot2 );

      connectionRootFile = createTestFile( "pvfs://connection1/" );
    }
  }

  @Test( expected = FileException.class )
  public void testGetFilesOfConnectionUsingBucketsThrowsIfEmptyBuckets() throws Exception {
    GetFilesOfConnectionUsingBucketsScenario scenario = new GetFilesOfConnectionUsingBucketsScenario();
    mockDetailsProviderLocations( scenario.details1, scenario.provider );

    vfsFileProvider.getFiles( scenario.connectionRootFile, null, mock( VariableSpace.class ) );
  }

  @Test( expected = FileException.class )
  public void testGetFilesOfConnectionUsingBucketsThrowsIfNullBuckets() throws Exception {
    GetFilesOfConnectionUsingBucketsScenario scenario = new GetFilesOfConnectionUsingBucketsScenario();
    when( scenario.provider.getLocations( scenario.details1 ) ).thenReturn( null );

    vfsFileProvider.getFiles( scenario.connectionRootFile, null, mock( VariableSpace.class ) );
  }

  @Test
  public void testGetFilesOfConnectionUsingBucketsReturnsOneFilePerBucket() throws Exception {
    GetFilesOfConnectionUsingBucketsScenario scenario = new GetFilesOfConnectionUsingBucketsScenario();

    List<VFSFile> files =
      vfsFileProvider.getFiles( scenario.connectionRootFile, null, mock( VariableSpace.class ) );

    assertEquals( 2, files.size() );

    assertBucket(
      files.get( 0 ),
      scenario.bucketRoot1.getName(),
      scenario.bucketRoot1.getModifiedDate().getTime(),
      "pvfs://connection1/bucket1",
      scenario.connectionRootFile.getPath() );

    assertBucket(
      files.get( 1 ),
      scenario.bucketRoot2.getName(),
      scenario.bucketRoot2.getModifiedDate().getTime(),
      "pvfs://connection1/bucket2",
      scenario.connectionRootFile.getPath() );
  }

  @Test
  public void testGetFilesOfConnectionUsingBucketsCachesResult() throws Exception {
    GetFilesOfConnectionUsingBucketsScenario scenario = new GetFilesOfConnectionUsingBucketsScenario();

    List<VFSFile> files1 =
      vfsFileProvider.getFiles( scenario.connectionRootFile, null, mock( VariableSpace.class ) );

    assertNotNull( files1 );

    List<VFSFile> files2 =
      vfsFileProvider.getFiles( scenario.connectionRootFile, null, mock( VariableSpace.class ) );

    assertSame( files2, files1 );
  }

  @Test( expected = FileException.class )
  public void testGetFilesOfConnectionUsingBucketsThrowsOnInvalidConnection() throws Exception {
    GetFilesOfConnectionUsingBucketsScenario scenario = new GetFilesOfConnectionUsingBucketsScenario();

    when( scenario.provider.getLocations( scenario.details1 ) ).thenThrow( RuntimeException.class );

    vfsFileProvider.getFiles( scenario.connectionRootFile, null, mock( VariableSpace.class ) );
  }

  void assertBucket( VFSFile bucketRootFile, String name, long modifiedDate, String path, String parentPath ) {
    assertTrue( bucketRootFile instanceof VFSDirectory );

    VFSDirectory bucketRootFolder = (VFSDirectory) bucketRootFile;
    assertEquals( name, bucketRootFolder.getName() );

    assertNotNull( bucketRootFolder.getDate() );
    assertEquals( modifiedDate, bucketRootFolder.getDate().getTime() );
    assertTrue( bucketRootFolder.isHasChildren() );
    assertTrue( bucketRootFolder.isCanAddChildren() );
    assertEquals( path, bucketRootFolder.getPath() );
    assertEquals( parentPath, bucketRootFolder.getParent() );
  }
  // endregion

  // region getFiles of root of connection NOT using buckets
  @Test
  public void testGetFilesOfConnectionNotUsingBuckets() throws Exception {
    VFSConnectionProvider<VFSConnectionDetails> provider = mockProvider( "scheme1" );
    VFSConnectionDetails details1 = mockDetails( "connection1", "scheme1" );

    mockDetailsUsesBuckets( details1, false );
    mockDetailsProvider( details1, provider );

    VFSFile connectionRootFile = createTestFile( "pvfs://connection1/" );

    ConnectionFileObject connectionRootFileObject = mock( ConnectionFileObject.class );
    when( connectionRootFileObject.getType() ).thenReturn( FileType.FOLDER );
    when( connectionRootFileObject.getChildren() ).thenReturn( new FileObject[ 0 ] );

    mockGetFileObject( "pvfs://connection1/", connectionRootFileObject );

    List<VFSFile> files =
      vfsFileProvider.getFiles( connectionRootFile, null, mock( VariableSpace.class ) );

    // This scenario has no children.
    // Children are tested in the (next) scenario, where the base file is not a connection root.
    assertTrue( files.isEmpty() );
  }
  // endregion

  // region getFiles of sub-folder (not a connection root folder)
  class GetFilesOfSubFolderScenario {
    public final VFSConnectionProvider<VFSConnectionDetails> provider;
    public final VFSConnectionDetails details1;

    public final VFSFile baseFile;
    public final ConnectionFileObject baseFileObject;

    public final ConnectionFileObject child1FileObject;
    public final ConnectionFileObject child2FolderObject;

    public GetFilesOfSubFolderScenario() throws Exception {
      provider = mockProvider( "scheme1" );
      details1 = mockDetails( "connection1", "scheme1" );

      baseFile = createTestFile( "pvfs://connection1/baseFolder" );

      baseFileObject = mock( ConnectionFileObject.class );
      when( baseFileObject.getType() ).thenReturn( FileType.FOLDER );
      mockGetFileObject( "pvfs://connection1/baseFolder", baseFileObject );

      child1FileObject = mockConnectionFileObject( "pvfs://connection1/baseFolder/child1", FileType.FILE );
      child2FolderObject = mockConnectionFileObject( "pvfs://connection1/baseFolder/child2", FileType.FOLDER );

      when( baseFileObject.getChildren() ).thenReturn( new FileObject[] {
        child1FileObject,
        child2FolderObject
      } );
    }
  }

  @Test
  public void testGetFilesOfSubFolder() throws Exception {
    GetFilesOfSubFolderScenario scenario = new GetFilesOfSubFolderScenario();

    List<VFSFile> files = vfsFileProvider.getFiles( scenario.baseFile, null, mock( VariableSpace.class ) );

    assertNotNull( files );
    assertEquals( 2, files.size() );
    assertFile( files.get( 0 ), scenario.child1FileObject );
    assertFolder( files.get( 1 ), scenario.child2FolderObject );
  }

  void assertFile( VFSFile file, ConnectionFileObject fileObject ) {
    assertFalse( file instanceof VFSDirectory );

    assertFileCommon( file, fileObject );

    assertTrue( file.isCanEdit() );
  }

  void assertFolder( VFSFile file, ConnectionFileObject fileObject ) {
    assertTrue( file instanceof VFSDirectory );

    VFSDirectory directory = (VFSDirectory) file;
    assertFileCommon( directory, fileObject );
  }

  void assertFileCommon( VFSFile file, ConnectionFileObject fileObject ) {
    assertEquals( fileObject.getName().getBaseName(), file.getName() );
    assertEquals( fileObject.getName().getURI(), file.getPath() );
    assertEquals( fileObject.getName().getParent().getURI(), file.getParent() );
  }
  // endregion

  // region Helpers
  protected VFSFile createTestFile( String path ) {
    VFSFile vfsFile = new VFSFile();
    vfsFile.setPath( path );
    return vfsFile;
  }

  VFSConnectionDetails mockDetails( String name ) throws KettleException {
    VFSConnectionDetails details = mock( VFSConnectionDetails.class );
    when( details.getName() ).thenReturn( name );

    when( connectionManager.getExistingDetails( name ) ).thenReturn( details );

    return details;
  }

  VFSConnectionDetails mockDetails( String name, String type ) throws KettleException {
    VFSConnectionDetails details = mockDetails( name );
    when( details.getType() ).thenReturn( type );
    return details;
  }

  @SuppressWarnings( "unchecked" )
  VFSConnectionProvider<VFSConnectionDetails> mockProvider( String type ) {
    VFSConnectionProvider<VFSConnectionDetails> provider =
      (VFSConnectionProvider<VFSConnectionDetails>) mock( VFSConnectionProvider.class );

    when( provider.getKey() ).thenReturn( type );

    return provider;
  }

  void mockDetailsUsesBuckets( VFSConnectionDetails details, boolean usesBuckets ) throws KettleException {
    when( vfsConnectionManagerHelper.usesBuckets( details ) ).thenReturn( usesBuckets );
  }

  void mockDetailsProvider( VFSConnectionDetails details, VFSConnectionProvider<VFSConnectionDetails> provider )
    throws KettleException {
    when( vfsConnectionManagerHelper.getExistingProvider( connectionManager, details ) ).thenReturn( provider );
  }

  void mockDetailsProviderLocations( VFSConnectionDetails details,
                                     VFSConnectionProvider<VFSConnectionDetails> provider,
                                     VFSRoot... bucketRoots ) {
    when( provider.getLocations( details ) ).thenReturn( List.of( bucketRoots ) );
  }

  ConnectionFileObject mockConnectionFileObject( String path, FileType fileType ) throws Exception {
    ConnectionFileObject fileObject = mock( ConnectionFileObject.class );

    when( fileObject.getType() ).thenReturn( fileType );

    ConnectionFileName fileName = ConnectionFileNameParser.getInstance().parseUri( path );
    when( fileObject.getName() ).thenReturn( fileName );

    mockGetFileObject( path, fileObject );

    return fileObject;
  }

  void mockGetFileObject( String path, FileObject fileObject ) throws FileException {
    when( kettleVFSService.getFileObject( eq( path ), any() ) )
      .thenReturn( fileObject );
  }
  // endregion
}
