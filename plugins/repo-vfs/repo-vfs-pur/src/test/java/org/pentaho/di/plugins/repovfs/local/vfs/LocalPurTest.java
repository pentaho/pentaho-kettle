package org.pentaho.di.plugins.repovfs.local.vfs;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.pentaho.di.plugins.repofvs.pur.vfs.PurProvider;
import org.pentaho.di.plugins.repofvs.pur.vfs.PurProvider.RepositoryAccess;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.plugin.services.importexport.StreamConverter;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository.ICurrentUserProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LocalPurTest {

  private static DefaultFileSystemManager fsm;

  private static MockUnifiedRepository repository;

  @BeforeAll
  static void init() throws Exception {
    ICurrentUserProvider userProvider = getUserProvider( MockUnifiedRepository.root().getName(), "role" );
    repository = createMockRepository( userProvider );
    createTestSample();

    // assumes http resolver is registered
    fsm = (DefaultFileSystemManager) VFS.getManager();
    fsm.removeProvider( PurProvider.SCHEME_LOCAL );
    fsm.addProvider( PurProvider.SCHEME_LOCAL, new PurProvider( opts -> new RepositoryAccess() {

      @Override
      public IUnifiedRepository getPur() {
        return repository;
      }

      @Override
      public IRepositoryContentConverterHandler getContentHandler() {
        return LocalPurTest.getContentHandler( opts, repository );
      }

    } ) );
  }

  private static MockUnifiedRepository createMockRepository( ICurrentUserProvider userProvider ) {

    return new MockUnifiedRepository( userProvider ) {
      private RepositoryFileAcl getDefaultAcl() {
        return new RepositoryFileAcl.Builder( userProvider.getUser() ).entriesInheriting( true ).build();
      }

      public RepositoryFile createFolder(java.io.Serializable parentFolderId, RepositoryFile file, String versionMessage) {
        return super.createFolder( parentFolderId, file, getDefaultAcl(), versionMessage );
      }

      public RepositoryFile createFile(java.io.Serializable parentFolderId, RepositoryFile file, IRepositoryFileData data, String versionMessage) {
        return super.createFile( parentFolderId, file, data, getDefaultAcl(), versionMessage );
      }
    } ;
  }

  @Test
  void testReadContent() throws Exception {

    FileObject testFile = fsm.resolveFile( url( "/test/sample.txt" ) );
    assertTrue( testFile.exists() );

    try ( InputStream is = testFile.getContent().getInputStream() ) {
      String content = IOUtils.toString( is, StandardCharsets.UTF_8 );
      assertEquals( content, "Hello" );
    }
  }

  @Test
  void testCreateDeleteFolder() throws Exception {
    FileObject newFolder = fsm.resolveFile( url( "/testCreateDeleteFolder/" ) );
    assertFalse( newFolder.exists() );

    newFolder.createFolder();
    assertTrue( newFolder.exists() );
    assertTrue( newFolder.isFolder() );

    newFolder.delete();
    assertFalse( newFolder.exists() );
  }

  @Test
  void testCreateFileNewFolder() throws Exception {
    FileObject newFolder = fsm.resolveFile( url( "/testCreateFileNewFolder/" ) );
    newFolder.createFolder();
    FileObject newFile = newFolder.resolveFile( "new_file.txt" );
    newFile.createFile();
    try ( var out = newFile.getContent().getOutputStream() ) {
      IOUtils.write( "content", out, StandardCharsets.UTF_8 );
      out.flush();
    }
    try ( InputStream is = newFile.getContent().getInputStream() ) {
      String content = IOUtils.toString( is, StandardCharsets.UTF_8 );
      assertEquals( content, "content" );
    }
  }

  @Test
  void testCreatedFileExists() throws Exception {
    FileObject newFolder = fsm.resolveFile( url( "/testCreatedFileExists/" ) );
    newFolder.createFolder();
    FileObject newFile = newFolder.resolveFile( "spanking_new_file.txt" );
    newFile.createFile();
    assertTrue( newFile.exists() );
  }

  @Test
  void testDeleteFolderWithFiles() throws Exception {
    FileObject newFolder = fsm.resolveFile( url( "/multiFileDelete/" ) );
    newFolder.createFolder();
    FileObject file1 = newFolder.resolveFile( "file1.txt" );
    file1.createFile();
    writeToFile( file1, "aaa" );

    FileObject file2 = newFolder.resolveFile( "file2.txt" );
    file2.createFile();
    writeToFile( file2, "bbb" );

    assertTrue( file1.exists() );
    assertTrue( file2.exists() );

    newFolder.delete();

    assertFalse( newFolder.exists() );
    assertFalse( file1.exists() );
    assertFalse( file2.exists() );
  }

  @Test
  void testMoveFile() throws Exception {
    FileObject srcFolder = fsm.resolveFile( url( "/testMoveFile_src/" ) );
    srcFolder.createFolder();
    FileObject srcFile = srcFolder.resolveFile( "move_me.txt" );
    srcFile.createFile();
    writeToFile( srcFile, "move-content" );
    assertTrue( srcFile.exists() );

    FileObject destFolder = fsm.resolveFile( url( "/testMoveFile_dest" ) );
    destFolder.createFolder();
    FileObject destFile = destFolder.resolveFile( "moved.txt" );

    srcFile.moveTo( destFile );

    assertFalse( srcFile.exists() );

    // assertTrue( fsm.resolveFile( url( "/moveTestDest/moved.txt" ) ).exists() );
    assertTrue( destFile.exists() );

    try ( InputStream is = destFile.getContent().getInputStream() ) {
      String content = IOUtils.toString( is, StandardCharsets.UTF_8 );
      assertEquals( "move-content", content );
    }
  }

  private void writeToFile( FileObject file1, String textContent ) throws IOException, FileSystemException {
    try ( var out = file1.getContent().getOutputStream() ) {
      IOUtils.write( textContent, out, StandardCharsets.UTF_8 );
      out.flush();
    }
  }

  /** "Hello" > /test/sample.txt */
  private static void createTestSample() {
    RepositoryFile root = repository.getFile("/");
    assertNotNull( root.getId() );

    RepositoryFileAcl acl = createBasicAcl( MockUnifiedRepository.root().getName() );

    RepositoryFile testFolder = createFolderPur( root, "/test", acl );
    RepositoryFile fileFile = createTextFilePur( testFolder, "/test/sample.txt", "Hello", acl );
    assertNotNull( fileFile.getId() );

    RepositoryFile innerFolder = createFolderPur( testFolder, "/test/folder", acl );
    RepositoryFile fileInFolder = createTextFilePur( innerFolder, "/test/folder/file_inside", "meh", acl );
    RepositoryFile innerFolder2 = createFolderPur( testFolder, "/test/another_folder", acl );

    assertNotNull( fileInFolder.getId() );
    assertNotNull( innerFolder2.getId() );
  }

  private String url( String path ) {
    return String.format( "%s://%s", PurProvider.SCHEME_LOCAL, path );
  }

  private static RepositoryFile createFolderPur( RepositoryFile root, String path, RepositoryFileAcl acl ) {
    final String folderName = RepositoryFilenameUtils.getName( path );
    RepositoryFile testFolder = new RepositoryFile.Builder( folderName ).path( path ).folder( true ).build();
    return repository.createFolder( root.getId(), testFolder, acl, null );
  }

  private static RepositoryFile createTextFilePur( RepositoryFile parent, String filePath, String textContent, RepositoryFileAcl acl ) {
    String fileName = RepositoryFilenameUtils.getName( filePath );
    RepositoryFile fileFile = new RepositoryFile.Builder( fileName ).path( filePath ).folder( false ).build();
    SimpleRepositoryFileData fileContent = new SimpleRepositoryFileData(
      new ByteArrayInputStream( textContent.getBytes() ),
      "text/plain",
      "UTF-8"
    );
    return repository.createFile( parent.getId(), fileFile, fileContent, acl, null );
  }

  private static RepositoryFile createTransformationFilePur( RepositoryFile parent, String filePath, RepositoryFileAcl acl ) {
    String fileName = RepositoryFilenameUtils.getName( filePath );
    RepositoryFile fileFile = new RepositoryFile.Builder( fileName ).path( filePath ).folder( false ).build();
    DataNode node = new DataNode( "transformation" );
    NodeRepositoryFileData fileContent = new NodeRepositoryFileData( node );
    return repository.createFile( parent.getId(), fileFile, fileContent, acl, null );
  }

  private static RepositoryFile createJobFilePur( RepositoryFile parent, String filePath, RepositoryFileAcl acl ) {
    String fileName = RepositoryFilenameUtils.getName( filePath );
    RepositoryFile fileFile = new RepositoryFile.Builder( fileName ).path( filePath ).folder( false ).build();
    DataNode node = new DataNode( "job" );
    NodeRepositoryFileData fileContent = new NodeRepositoryFileData( node );
    return repository.createFile( parent.getId(), fileFile, fileContent, acl, null );
  }

  private static RepositoryFileAcl createBasicAcl( String user ) {
    return new RepositoryFileAcl.Builder( user ).entriesInheriting( true ).build();
  }

  private static IRepositoryContentConverterHandler getContentHandler( FileSystemOptions fileSystemOptions,
                                                                       IUnifiedRepository repo ) {
    return new IRepositoryContentConverterHandler() {

      @Override
      public Map<String, Converter> getConverters() {
        throw new UnsupportedOperationException( "Unimplemented method 'getConverters'" );
      }

      @Override
      public Converter getConverter( String extension ) {
        return new StreamConverter( repo );
      }

      @Override
      public void addConverter( String extension, Converter converter ) {
        throw new UnsupportedOperationException( "Unimplemented method 'addConverter'" );
      }

    };
  }

  static ICurrentUserProvider getUserProvider( String user, String...roles) {
    return new ICurrentUserProvider() {

        @Override
        public List<String> getRoles() {
          return Arrays.asList( roles );
        }

        @Override
        public String getUser() {
          return user;
        }
      };
  }

  @Test
  void testRenameTransformationFile_UpdatesTitleAndLocaleProperties() throws Exception {
    FileObject testFolder = fsm.resolveFile( url( "/testRenameTrans/" ) );
    testFolder.createFolder();
    
    // Create a transformation file (.ktr) directly in repository with NodeRepositoryFileData
    RepositoryFile testFolderRepo = repository.getFile( "/testRenameTrans" );
    RepositoryFileAcl acl = createBasicAcl( MockUnifiedRepository.root().getName() );
    RepositoryFile originalRepoFile = createTransformationFilePur( testFolderRepo, "/testRenameTrans/original_transform.ktr", acl );
    assertNotNull( originalRepoFile, "Original file should exist in repository before rename" );
    assertEquals( "original_transform.ktr", originalRepoFile.getName() );

    // Rename the file via VFS - doRename updates the locale properties as part of the rename
    FileObject transFile = testFolder.resolveFile( "original_transform.ktr" );
    FileObject renamedFile = testFolder.resolveFile( "renamed_transform.ktr" );
    transFile.moveTo( renamedFile );

    // Verify the file was successfully moved  
    assertTrue( renamedFile.exists() );
    assertFalse( transFile.exists() );
    assertEquals( "renamed_transform.ktr", renamedFile.getName().getBaseName() );
    
    // Verify that the title was updated in the repository
    RepositoryFile renamedRepoFile = repository.getFile( "/testRenameTrans/renamed_transform.ktr" );
    assertNotNull( renamedRepoFile, "Renamed file should exist in repository" );
    assertEquals( "renamed_transform.ktr", renamedRepoFile.getName() );
    assertEquals( "renamed_transform", renamedRepoFile.getTitle() );
    
    // Verify title property was persisted (extracted from localePropertiesMap)
    String title = repository.findTitle( renamedRepoFile );
    assertEquals( "renamed_transform", title, "Title should be updated to new filename without extension" );
    
    // Verify localePropertiesMap is properly set
    Map<String, Properties> localePropertiesMap = renamedRepoFile.getLocalePropertiesMap();
    assertNotNull( localePropertiesMap, "LocalePropertiesMap should not be null" );
    
    // Check that properties exist for default locale
    Properties properties = localePropertiesMap.get( RepositoryFile.DEFAULT_LOCALE );
    assertNotNull( properties, "Properties should exist for default locale" );
    assertEquals( "renamed_transform", properties.getProperty( RepositoryFile.FILE_TITLE ), 
        "FILE_TITLE should be set to new filename without extension" );
  }

  @Test
  void testRenameJobFile_UpdatesTitleAndLocaleProperties() throws Exception {
    FileObject testFolder = fsm.resolveFile( url( "/testRenameJob/" ) );
    testFolder.createFolder();
    
    // Create a job file (.kjb) directly in repository with NodeRepositoryFileData
    RepositoryFile testFolderRepo = repository.getFile( "/testRenameJob" );
    RepositoryFileAcl acl = createBasicAcl( MockUnifiedRepository.root().getName() );
    RepositoryFile originalRepoFile = createJobFilePur( testFolderRepo, "/testRenameJob/original_job.kjb", acl );
    assertNotNull( originalRepoFile, "Original file should exist in repository before rename" );
    assertEquals( "original_job.kjb", originalRepoFile.getName() );

    // Rename the file via VFS - doRename should update title/locale properties
    FileObject jobFile = testFolder.resolveFile( "original_job.kjb" );
    FileObject renamedFile = testFolder.resolveFile( "renamed_job.kjb" );
    jobFile.moveTo( renamedFile );

    // Verify the file was successfully moved
    assertTrue( renamedFile.exists() );
    assertFalse( jobFile.exists() );
    assertEquals( "renamed_job.kjb", renamedFile.getName().getBaseName() );
    
    // Verify that the title was updated in the repository
    RepositoryFile renamedRepoFile = repository.getFile( "/testRenameJob/renamed_job.kjb" );
    assertNotNull( renamedRepoFile, "Renamed file should exist in repository" );
    assertEquals( "renamed_job.kjb", renamedRepoFile.getName() );
    assertEquals( "renamed_job", renamedRepoFile.getTitle() );
    
    // Verify title property was persisted (extracted from localePropertiesMap)
    String title = repository.findTitle( renamedRepoFile );
    assertEquals( "renamed_job", title, "Title should be updated to new filename without extension" );
    
    // Verify localePropertiesMap is properly set
    Map<String, Properties> localePropertiesMap = renamedRepoFile.getLocalePropertiesMap();
    assertNotNull( localePropertiesMap, "LocalePropertiesMap should not be null" );
    
    // Check that properties exist for default locale
    Properties properties = localePropertiesMap.get( RepositoryFile.DEFAULT_LOCALE );
    assertNotNull( properties, "Properties should exist for default locale" );
    assertEquals( "renamed_job", properties.getProperty( RepositoryFile.FILE_TITLE ), 
        "FILE_TITLE should be set to new filename without extension" );
  }

  @Test
  void testRenameRegularFile_TitleAndLocalePropertiesNotUpdated() throws Exception {
    FileObject testFolder = fsm.resolveFile( url( "/testRenameRegularFile/" ) );
    testFolder.createFolder();
    
    // Create a regular text file (not .ktr or .kjb)
    FileObject textFile = testFolder.resolveFile( "myfile.txt" );
    textFile.createFile();
    writeToFile( textFile, "This is a regular file" );
    assertTrue( textFile.exists() );

    // Get the original file state
    RepositoryFile originalFile = repository.getFile( "/testRenameRegularFile/myfile.txt" );
    assertNotNull( originalFile );
    Map<String, Properties> originalLocaleMap = originalFile.getLocalePropertiesMap();

    // Rename the file
    FileObject renamedFile = testFolder.resolveFile( "renamed_file.txt" );
    textFile.moveTo( renamedFile );

    // Verify the file exists at new location
    assertTrue( renamedFile.exists() );
    assertFalse( textFile.exists() );

    // Verify the file was renamed
    RepositoryFile renamedRepositoryFile = repository.getFile( "/testRenameRegularFile/renamed_file.txt" );
    assertNotNull( renamedRepositoryFile );
    assertEquals( "renamed_file.txt", renamedRepositoryFile.getName() );
    
    // Verify no title logic was applied (locale properties remain unchanged)
    Map<String, Properties> renamedLocaleMap = renamedRepositoryFile.getLocalePropertiesMap();
    assertEquals( originalLocaleMap, renamedLocaleMap );
  }

}

