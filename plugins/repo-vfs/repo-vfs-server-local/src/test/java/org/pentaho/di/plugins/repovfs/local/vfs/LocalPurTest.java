package org.pentaho.di.plugins.repovfs.local.vfs;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.pentaho.di.plugins.repofvs.local.vfs.LocalPurProvider;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
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
    fsm.removeProvider( LocalPurProvider.SCHEME );
    fsm.addProvider( LocalPurProvider.SCHEME, new TestLocalPurProvider( repository ) );
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
    return String.format( "%s://%s", LocalPurProvider.SCHEME, path );
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

  private static RepositoryFileAcl createBasicAcl( String user ) {
    return new RepositoryFileAcl.Builder( user ).entriesInheriting( true ).build();
  }

  private static class TestLocalPurProvider extends LocalPurProvider {

    private final IUnifiedRepository repo;

    public TestLocalPurProvider(IUnifiedRepository repo) {
      this.repo = repo;
    }

    @Override
    protected IUnifiedRepository createRepository(FileSystemOptions fileSystemOptions) {
      return repo;
    }

    @Override
    protected IRepositoryContentConverterHandler getContentHandler(FileSystemOptions fileSystemOptions) {
      return new IRepositoryContentConverterHandler() {

        @Override
        public Map<String, Converter> getConverters() {
          throw new UnsupportedOperationException("Unimplemented method 'getConverters'");
        }

        @Override
        public Converter getConverter(String extension) {
          return new StreamConverter(repo);
        }

        @Override
        public void addConverter(String extension, Converter converter) {
          throw new UnsupportedOperationException("Unimplemented method 'addConverter'");
        }

      };
    }
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

}
