/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.s3common;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.util.StorageUnitConverter;
import org.pentaho.s3common.DummyS3CommonObjects.DummyS3FileName;
import org.pentaho.s3common.DummyS3CommonObjects.DummyS3FileObject;
import org.pentaho.s3common.DummyS3CommonObjects.DummyS3FileSystem;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class S3TestFileObjectCopyFromTest {

  private DummyS3FileObject dst;
  private DummyS3FileObject src;
  private FileSelector selector;
  private DummyS3FileSystem fileSystem;

  @Before
  public void setUp() {
    DummyS3FileName fileName = new DummyS3FileName( "s3", "bucket", "/bucket/key", org.apache.commons.vfs2.FileType.FILE );
    S3KettleProperty kettleProperty = new S3KettleProperty();
    StorageUnitConverter storageUnitConverter = new StorageUnitConverter();
    DummyS3FileSystem realFileSystem = new DummyS3FileSystem( fileName, new org.apache.commons.vfs2.FileSystemOptions(), storageUnitConverter, kettleProperty );
    fileSystem = Mockito.spy( realFileSystem );
    dst = Mockito.spy( new DummyS3FileObject( fileName, fileSystem ) );
    src = Mockito.spy( new DummyS3FileObject( fileName, fileSystem ) );
    selector = mock( FileSelector.class );
  }

  @After
  public void tearDown() {
    dst = null;
    src = null;
    selector = null;
    fileSystem = null;
  }

  @Test
  @SuppressWarnings( "unchecked" )
  public void testCopyFrom_S3ToS3_Success() throws FileSystemException {
    // Arrange: setup source file as a FILE type that exists
    doReturn( org.apache.commons.vfs2.FileType.FILE ).when( src ).getType();
    doReturn( true ).when( src ).exists();
    
    // Setup dst as a FOLDER type
    doReturn( org.apache.commons.vfs2.FileType.FOLDER ).when( dst ).getType();
    
    // Mock findFiles to add the source file to the list
    Mockito.doAnswer( invocation -> {
      java.util.List<FileObject> list = invocation.getArgument( 2 );
      list.add( src );
      return null;
    } ).when( src ).findFiles( any( FileSelector.class ), any( Boolean.class ), any( java.util.List.class ) );
    
    // Mock getName for relative path calculation
    when( src.getName() ).thenReturn( new DummyS3FileName( "s3", "bucket", "/bucket/srckey", org.apache.commons.vfs2.FileType.FILE ) );
    
    // Mock calculateDestination - need to return a valid S3FileObject
    DummyS3FileName dstFileName = new DummyS3FileName( "s3", "bucket", "/bucket/destkey", org.apache.commons.vfs2.FileType.FILE );
    DummyS3FileObject calculatedDst = Mockito.spy( new DummyS3FileObject( dstFileName, fileSystem ) );
    doReturn( calculatedDst ).when( fileSystem ).resolveFile( any( String.class ) );
    
    // Arrange: dst.fileSystem.copy should succeed
    doNothing().when( fileSystem ).copy( any( DummyS3FileObject.class ), any( DummyS3FileObject.class ) );
    
    // Act
    dst.copyFrom( src, selector );
    
    // Assert: fileSystem.copy was called
    verify( fileSystem, times( 1 ) ).copy( any( DummyS3FileObject.class ), any( DummyS3FileObject.class ) );
  }

  @Test
  @SuppressWarnings( "unchecked" )
  public void testCopyFrom_NonS3Source_FallbackToDefault() throws FileSystemException {
    FileObject nonS3 = mock( FileObject.class );
    when( nonS3.exists() ).thenReturn( true );
    when( nonS3.getType() ).thenReturn( org.apache.commons.vfs2.FileType.FILE );
    
    // Mock findFiles to add the non-S3 file to the list
    Mockito.doAnswer( invocation -> {
      java.util.List<FileObject> list = invocation.getArgument( 2 );
      list.add( nonS3 );
      return null;
    } ).when( nonS3 ).findFiles( any( FileSelector.class ), any( Boolean.class ), any( java.util.List.class ) );
    
    // Mock getName for relative path calculation
    when( nonS3.getName() ).thenReturn( new DummyS3FileName( "file", "local", "/tmp/file", org.apache.commons.vfs2.FileType.FILE ) );
    
    // Setup dst as a FOLDER type
    doReturn( org.apache.commons.vfs2.FileType.FOLDER ).when( dst ).getType();
    
    // Mock calculateDestination - need to return a valid S3FileObject
    DummyS3FileName dstFileName = new DummyS3FileName( "s3", "bucket", "/bucket/destkey", org.apache.commons.vfs2.FileType.FILE );
    DummyS3FileObject calculatedDst = Mockito.spy( new DummyS3FileObject( dstFileName, fileSystem ) );
    doReturn( calculatedDst ).when( fileSystem ).resolveFile( any( String.class ) );
    
    // Mock fileSystem.upload to succeed
    doNothing().when( fileSystem ).upload( any( FileObject.class ), any( DummyS3FileObject.class ) );
    
    // Act
    dst.copyFrom( nonS3, selector );
    
    // Assert: fileSystem.copy was not called and fileSystem.upload was called
    verify( fileSystem, times( 0 ) ).copy( any( DummyS3FileObject.class ), any( DummyS3FileObject.class ) );
    verify( fileSystem, times( 1 ) ).upload( any( FileObject.class ), any( DummyS3FileObject.class ) );
  }

  @Test
  @SuppressWarnings( "unchecked" )
  public void testCopyFrom_S3ToS3_Exception_FallbackToDefault() throws FileSystemException {
    // Arrange: fileSystem.copy(S3TestFileObject, S3TestFileObject) throws, should fallback to default
    doThrow( new FileSystemException( "fail" ) ).when( fileSystem ).copy( any( DummyS3FileObject.class ), any( DummyS3FileObject.class ) );
    
    // Mock fileSystem.upload to succeed on fallback
    doNothing().when( fileSystem ).upload( any( FileObject.class ), any( DummyS3FileObject.class ) );
    
    FileSelector sel = mock( FileSelector.class );
    doReturn( org.apache.commons.vfs2.FileType.FOLDER ).when( dst ).getType();
    doReturn( org.apache.commons.vfs2.FileType.FILE ).when( src ).getType();
    doReturn( true ).when( dst ).exists();
    doReturn( true ).when( src ).exists();
    
    // Mock findFiles to add the source file to the list
    Mockito.doAnswer( invocation -> {
      java.util.List<FileObject> list = invocation.getArgument( 2 );
      list.add( src );
      return null;
    } ).when( src ).findFiles( any( FileSelector.class ), any( Boolean.class ), any( java.util.List.class ) );
    
    // Mock getName for relative path calculation
    when( src.getName() ).thenReturn( new DummyS3FileName( "s3", "bucket", "/bucket/srckey", org.apache.commons.vfs2.FileType.FILE ) );
    
    // Mock calculateDestination - need to return a valid S3FileObject
    DummyS3FileName dstFileName = new DummyS3FileName( "s3", "bucket", "/bucket/destkey", org.apache.commons.vfs2.FileType.FILE );
    DummyS3FileObject calculatedDst = Mockito.spy( new DummyS3FileObject( dstFileName, fileSystem ) );
    doReturn( calculatedDst ).when( fileSystem ).resolveFile( any( String.class ) );
    
    dst.copyFrom( src, sel );
    
    // Assert: fileSystem.copy was called once and fallback to fileSystem.upload was called
    verify( fileSystem, times( 1 ) ).copy( any( DummyS3FileObject.class ), any( DummyS3FileObject.class ) );
    verify( fileSystem, times( 1 ) ).upload( any( FileObject.class ), any( DummyS3FileObject.class ) );
  }

  /**
   * Test copying a folder with multiple files - verifies all files are copied.
   */
  @Test
  @SuppressWarnings( "unchecked" )
  public void testCopyFrom_FolderWithMultipleFiles() throws FileSystemException {
    // Arrange: setup src as a FOLDER type that exists
    doReturn( org.apache.commons.vfs2.FileType.FOLDER ).when( src ).getType();
    doReturn( true ).when( src ).exists();
    when( src.getName() ).thenReturn( new DummyS3FileName( "s3", "bucket", "/bucket/src", org.apache.commons.vfs2.FileType.FOLDER ) );
    
    // Create 3 mock files with names
    DummyS3FileObject file1 = mock( DummyS3FileObject.class );
    DummyS3FileObject file2 = mock( DummyS3FileObject.class );
    DummyS3FileObject file3 = mock( DummyS3FileObject.class );
    
    when( file1.getType() ).thenReturn( org.apache.commons.vfs2.FileType.FILE );
    when( file2.getType() ).thenReturn( org.apache.commons.vfs2.FileType.FILE );
    when( file3.getType() ).thenReturn( org.apache.commons.vfs2.FileType.FILE );
    when( file1.getName() ).thenReturn( new DummyS3FileName( "s3", "bucket", "/bucket/src/file1.txt", org.apache.commons.vfs2.FileType.FILE ) );
    when( file2.getName() ).thenReturn( new DummyS3FileName( "s3", "bucket", "/bucket/src/file2.txt", org.apache.commons.vfs2.FileType.FILE ) );
    when( file3.getName() ).thenReturn( new DummyS3FileName( "s3", "bucket", "/bucket/src/file3.txt", org.apache.commons.vfs2.FileType.FILE ) );
    
    // Mock findFiles to return all 3 files
    Mockito.doAnswer( invocation -> {
      java.util.List<FileObject> list = invocation.getArgument( 2 );
      list.add( file1 );
      list.add( file2 );
      list.add( file3 );
      return null;
    } ).when( src ).findFiles( any( FileSelector.class ), any( Boolean.class ), any( java.util.List.class ) );
    
    // Mock destination as FOLDER
    doReturn( org.apache.commons.vfs2.FileType.FOLDER ).when( dst ).getType();
    
    // Mock resolveFile to return destination objects - use doReturn to avoid calling real method
    DummyS3FileObject calculatedDst = mock( DummyS3FileObject.class );
    doReturn( calculatedDst ).when( fileSystem ).resolveFile( any( String.class ) );
    
    // Mock upload to succeed (since files aren't S3FileObjects)
    doNothing().when( fileSystem ).upload( any( FileObject.class ), any( DummyS3FileObject.class ) );
    
    // Act
    dst.copyFrom( src, selector );
    
    // Assert: upload was called 3 times (once for each file)
    verify( fileSystem, times( 3 ) ).upload( any( FileObject.class ), any( DummyS3FileObject.class ) );
  }

  /**
   * Test that folders in the file list are skipped (only files are copied).
   */
  @Test
  @SuppressWarnings( "unchecked" )
  public void testCopyFrom_SkipsFoldersInFileList() throws FileSystemException {
    // Arrange: setup src as a FOLDER type that exists
    doReturn( org.apache.commons.vfs2.FileType.FOLDER ).when( src ).getType();
    doReturn( true ).when( src ).exists();
    when( src.getName() ).thenReturn( new DummyS3FileName( "s3", "bucket", "/bucket/src", org.apache.commons.vfs2.FileType.FOLDER ) );
    
    DummyS3FileObject subFolder = mock( DummyS3FileObject.class );
    DummyS3FileObject file = mock( DummyS3FileObject.class );
    
    when( subFolder.getType() ).thenReturn( org.apache.commons.vfs2.FileType.FOLDER );
    when( file.getType() ).thenReturn( org.apache.commons.vfs2.FileType.FILE );
    when( file.getName() ).thenReturn( new DummyS3FileName( "s3", "bucket", "/bucket/src/file.txt", org.apache.commons.vfs2.FileType.FILE ) );
    
    // Mock findFiles to return both folder and file
    Mockito.doAnswer( invocation -> {
      java.util.List<FileObject> list = invocation.getArgument( 2 );
      list.add( subFolder ); // This should be skipped
      list.add( file );       // This should be copied
      return null;
    } ).when( src ).findFiles( any( FileSelector.class ), any( Boolean.class ), any( java.util.List.class ) );
    
    doReturn( org.apache.commons.vfs2.FileType.FOLDER ).when( dst ).getType();
    
    // Mock resolveFile - use doReturn to avoid calling real method
    DummyS3FileObject calculatedDst = mock( DummyS3FileObject.class );
    doReturn( calculatedDst ).when( fileSystem ).resolveFile( any( String.class ) );
    
    doNothing().when( fileSystem ).upload( any( FileObject.class ), any( DummyS3FileObject.class ) );
    
    // Act
    dst.copyFrom( src, selector );
    
    // Assert: upload was called only once (for the file, not the folder)
    verify( fileSystem, times( 1 ) ).upload( any( FileObject.class ), any( DummyS3FileObject.class ) );
  }

  /**
   * Test copying empty folder (no files) - no copy operations should occur.
   */
  @Test
  @SuppressWarnings( "unchecked" )
  public void testCopyFrom_EmptyFolder() throws FileSystemException {
    // Arrange: setup src as a FOLDER type that exists
    doReturn( org.apache.commons.vfs2.FileType.FOLDER ).when( src ).getType();
    doReturn( true ).when( src ).exists();
    
    // Mock findFiles to return empty list
    Mockito.doAnswer( invocation -> {
      // Don't add anything to the list
      return null;
    } ).when( src ).findFiles( any( FileSelector.class ), any( Boolean.class ), any( java.util.List.class ) );
    
    doReturn( org.apache.commons.vfs2.FileType.FOLDER ).when( dst ).getType();
    
    // Act
    dst.copyFrom( src, selector );
    
    // Assert: no copy or upload operations
    verify( fileSystem, times( 0 ) ).copy( any( DummyS3FileObject.class ), any( DummyS3FileObject.class ) );
    verify( fileSystem, times( 0 ) ).upload( any( FileObject.class ), any( DummyS3FileObject.class ) );
  }

  /**
   * Test that exception is thrown when source doesn't exist.
   */
  @Test( expected = FileSystemException.class )
  public void testCopyFrom_SourceDoesNotExist() throws FileSystemException {
    // Arrange: Non-existent source
    doReturn( false ).when( src ).exists();
    
    // Act - should throw FileSystemException
    dst.copyFrom( src, selector );
    
    // Assert: Exception is thrown (handled by @Test annotation)
  }

  /**
   * Test copying nested folder structure preserves relative paths.
   */
  @Test
  @SuppressWarnings( "unchecked" )
  public void testCopyFrom_NestedFolderStructure() throws FileSystemException {
    // Arrange: setup src as a FOLDER type that exists
    doReturn( org.apache.commons.vfs2.FileType.FOLDER ).when( src ).getType();
    doReturn( true ).when( src ).exists();
    when( src.getName() ).thenReturn( new DummyS3FileName( "s3", "bucket", "/bucket/src", org.apache.commons.vfs2.FileType.FOLDER ) );
    
    DummyS3FileObject nestedFile = mock( DummyS3FileObject.class );
    when( nestedFile.getType() ).thenReturn( org.apache.commons.vfs2.FileType.FILE );
    when( nestedFile.getName() ).thenReturn( new DummyS3FileName( "s3", "bucket", "/bucket/src/subdir/nested.txt", org.apache.commons.vfs2.FileType.FILE ) );
    
    // Mock findFiles to return the nested file
    Mockito.doAnswer( invocation -> {
      java.util.List<FileObject> list = invocation.getArgument( 2 );
      list.add( nestedFile );
      return null;
    } ).when( src ).findFiles( any( FileSelector.class ), any( Boolean.class ), any( java.util.List.class ) );
    
    doReturn( org.apache.commons.vfs2.FileType.FOLDER ).when( dst ).getType();
    
    // Mock resolveFile - use doReturn to avoid calling real method
    DummyS3FileObject calculatedDst = mock( DummyS3FileObject.class );
    doReturn( calculatedDst ).when( fileSystem ).resolveFile( any( String.class ) );
    
    doNothing().when( fileSystem ).upload( any( FileObject.class ), any( DummyS3FileObject.class ) );
    
    // Act
    dst.copyFrom( src, selector );
    
    // Assert: upload was called once and resolveFile was called to calculate destination
    verify( fileSystem, times( 1 ) ).upload( any( FileObject.class ), any( DummyS3FileObject.class ) );
    verify( fileSystem, times( 1 ) ).resolveFile( any( String.class ) );
  }

}
