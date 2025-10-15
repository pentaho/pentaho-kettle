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

package org.pentaho.di.core.ssh.mina;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClient.Attributes;
import org.apache.sshd.sftp.client.SftpClient.CloseableHandle;
import org.apache.sshd.sftp.client.SftpClient.DirEntry;
import org.apache.sshd.sftp.client.SftpClient.OpenMode;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.ssh.SftpFile;
import org.pentaho.di.core.ssh.exceptions.SftpException;

/**
 * Unit tests for MinaSftpSession.
 * Uses Mockito to mock SftpClient and test all operations.
 */
public class MinaSftpSessionTest {

  private SftpClient mockClient;
  private MinaSftpSession session;

  @Before
  public void setUp() {
    mockClient = mock( SftpClient.class );
    session = new MinaSftpSession( mockClient );
  }

  @Test
  public void testListDirectory() throws Exception {
    // Setup mock directory entries
    DirEntry entry1 = createMockDirEntry( "file1.txt", false, 1024, Instant.now() );
    DirEntry entry2 = createMockDirEntry( "subdir", true, 0, Instant.now() );
    DirEntry entry3 = createMockDirEntry( "file2.log", false, 2048, Instant.now() );

    when( mockClient.readDir( "/test/path" ) )
      .thenReturn( Arrays.asList( entry1, entry2, entry3 ) );

    List<SftpFile> files = session.list( "/test/path" );

    assertEquals( 3, files.size() );
    assertEquals( "file1.txt", files.get( 0 ).getName() );
    assertFalse( files.get( 0 ).isDirectory() );
    assertEquals( 1024, files.get( 0 ).getSize() );

    assertEquals( "subdir", files.get( 1 ).getName() );
    assertTrue( files.get( 1 ).isDirectory() );

    assertEquals( "file2.log", files.get( 2 ).getName() );
    assertEquals( 2048, files.get( 2 ).getSize() );

    verify( mockClient ).readDir( "/test/path" );
  }

  @Test
  public void testListEmptyDirectory() throws Exception {
    when( mockClient.readDir( "/empty" ) ).thenReturn( Arrays.asList() );

    List<SftpFile> files = session.list( "/empty" );

    assertTrue( files.isEmpty() );
    verify( mockClient ).readDir( "/empty" );
  }

  @Test( expected = SftpException.class )
  public void testListThrowsExceptionOnError() throws Exception {
    when( mockClient.readDir( "/invalid" ) ).thenThrow( new IOException( "Directory not found" ) );

    session.list( "/invalid" );
  }

  @Test
  public void testExistsReturnsTrue() throws Exception {
    Attributes mockAttrs = mock( Attributes.class );
    when( mockClient.stat( "/existing/file.txt" ) ).thenReturn( mockAttrs );

    boolean exists = session.exists( "/existing/file.txt" );

    assertTrue( exists );
    verify( mockClient ).stat( "/existing/file.txt" );
  }

  @Test
  public void testExistsReturnsFalse() throws Exception {
    when( mockClient.stat( "/nonexistent" ) ).thenThrow( new IOException( "No such file" ) );

    boolean exists = session.exists( "/nonexistent" );

    assertFalse( exists );
    verify( mockClient ).stat( "/nonexistent" );
  }

  @Test
  public void testIsDirectoryTrue() throws Exception {
    Attributes mockAttrs = mock( Attributes.class );
    when( mockAttrs.isDirectory() ).thenReturn( true );
    when( mockClient.stat( "/test/dir" ) ).thenReturn( mockAttrs );

    boolean isDir = session.isDirectory( "/test/dir" );

    assertTrue( isDir );
    verify( mockClient ).stat( "/test/dir" );
  }

  @Test
  public void testIsDirectoryFalse() throws Exception {
    Attributes mockAttrs = mock( Attributes.class );
    when( mockAttrs.isDirectory() ).thenReturn( false );
    when( mockClient.stat( "/test/file.txt" ) ).thenReturn( mockAttrs );

    boolean isDir = session.isDirectory( "/test/file.txt" );

    assertFalse( isDir );
    verify( mockClient ).stat( "/test/file.txt" );
  }

  @Test( expected = SftpException.class )
  public void testIsDirectoryThrowsException() throws Exception {
    when( mockClient.stat( "/invalid" ) ).thenThrow( new IOException( "Stat failed" ) );

    session.isDirectory( "/invalid" );
  }

  @Test
  public void testGetFileSize() throws Exception {
    Attributes mockAttrs = mock( Attributes.class );
    when( mockAttrs.getSize() ).thenReturn( 12345L );
    when( mockClient.stat( "/test/file.bin" ) ).thenReturn( mockAttrs );

    long size = session.size( "/test/file.bin" );

    assertEquals( 12345L, size );
    verify( mockClient ).stat( "/test/file.bin" );
  }

  @Test( expected = SftpException.class )
  public void testGetFileSizeThrowsException() throws Exception {
    when( mockClient.stat( "/invalid" ) ).thenThrow( new IOException( "Cannot get size" ) );

    session.size( "/invalid" );
  }

  @Test
  public void testDownloadFile() throws Exception {
    String fileContent = "test file content";
    ByteArrayInputStream inputStream = new ByteArrayInputStream( fileContent.getBytes( StandardCharsets.UTF_8 ) );
    when( mockClient.read( "/remote/file.txt" ) ).thenReturn( inputStream );

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    session.download( "/remote/file.txt", outputStream );

    assertEquals( fileContent, outputStream.toString( StandardCharsets.UTF_8 ) );
    verify( mockClient ).read( "/remote/file.txt" );
  }

  @Test( expected = SftpException.class )
  public void testDownloadThrowsException() throws Exception {
    when( mockClient.read( "/remote/missing.txt" ) ).thenThrow( new IOException( "File not found" ) );

    session.download( "/remote/missing.txt", new ByteArrayOutputStream() );
  }

  @Test
  public void testUploadFile() throws Exception {
    String content = "upload content";
    ByteArrayInputStream inputStream = new ByteArrayInputStream( content.getBytes( StandardCharsets.UTF_8 ) );
    CloseableHandle mockHandle = mock( CloseableHandle.class );

    when( mockClient.open( eq( "/remote/upload.txt" ), any( OpenMode.class ), any( OpenMode.class ),
        any( OpenMode.class ) ) ).thenReturn( mockHandle );

    session.upload( inputStream, "/remote/upload.txt", true );

    verify( mockClient ).open( eq( "/remote/upload.txt" ), any( OpenMode.class ), any( OpenMode.class ),
        any( OpenMode.class ) );
    verify( mockClient ).write( eq( mockHandle ), anyLong(), any( byte[].class ), any( int.class ),
        any( int.class ) );
    verify( mockHandle ).close();
  }

  @Test( expected = SftpException.class )
  public void testUploadThrowsException() throws Exception {
    when( mockClient.open( any(), any(), any(), any() ) )
      .thenThrow( new IOException( "Permission denied" ) );

    session.upload( new ByteArrayInputStream( "test".getBytes() ), "/remote/file.txt", true );
  }

  @Test
  public void testMkdir() throws Exception {
    session.mkdir( "/remote/newdir" );

    verify( mockClient ).mkdir( "/remote/newdir" );
  }

  @Test( expected = SftpException.class )
  public void testMkdirThrowsException() throws Exception {
    doThrow( new IOException( "Cannot create directory" ) ).when( mockClient ).mkdir( "/invalid" );

    session.mkdir( "/invalid" );
  }

  @Test
  public void testDeleteFile() throws Exception {
    Attributes mockAttrs = mock( Attributes.class );
    when( mockAttrs.isDirectory() ).thenReturn( false );
    when( mockClient.stat( "/remote/file.txt" ) ).thenReturn( mockAttrs );

    session.delete( "/remote/file.txt" );

    verify( mockClient ).remove( "/remote/file.txt" );
    verify( mockClient, never() ).rmdir( any() );
  }

  @Test
  public void testDeleteDirectory() throws Exception {
    Attributes mockAttrs = mock( Attributes.class );
    when( mockAttrs.isDirectory() ).thenReturn( true );
    when( mockClient.stat( "/remote/dir" ) ).thenReturn( mockAttrs );

    session.delete( "/remote/dir" );

    verify( mockClient ).rmdir( "/remote/dir" );
    verify( mockClient, never() ).remove( any() );
  }

  @Test( expected = SftpException.class )
  public void testDeleteThrowsException() throws Exception {
    when( mockClient.stat( "/invalid" ) ).thenThrow( new IOException( "Cannot stat" ) );

    session.delete( "/invalid" );
  }

  @Test
  public void testRename() throws Exception {
    session.rename( "/old/path.txt", "/new/path.txt" );

    verify( mockClient ).rename( "/old/path.txt", "/new/path.txt" );
  }

  @Test( expected = SftpException.class )
  public void testRenameThrowsException() throws Exception {
    doThrow( new IOException( "Rename failed" ) ).when( mockClient )
      .rename( "/old/path", "/new/path" );

    session.rename( "/old/path", "/new/path" );
  }

  @Test
  public void testClose() throws Exception {
    session.close();

    verify( mockClient ).close();
  }

  @Test
  public void testCloseIgnoresIOException() throws Exception {
    doThrow( new IOException( "Close error" ) ).when( mockClient ).close();

    // Should not throw exception
    session.close();

    verify( mockClient ).close();
  }

  @Test
  public void testListWithNullModifyTime() throws Exception {
    DirEntry entry = createMockDirEntry( "file.txt", false, 100, null );
    when( mockClient.readDir( "/test" ) ).thenReturn( Arrays.asList( entry ) );

    List<SftpFile> files = session.list( "/test" );

    assertEquals( 1, files.size() );
    assertEquals( Instant.EPOCH, files.get( 0 ).getModified() );
  }

  /**
   * Helper method to create a mock DirEntry
   */
  private DirEntry createMockDirEntry( String name, boolean isDir, long size, Instant modified ) {
    DirEntry entry = mock( DirEntry.class );
    Attributes attrs = mock( Attributes.class );

    when( entry.getFilename() ).thenReturn( name );
    when( entry.getAttributes() ).thenReturn( attrs );
    when( attrs.isDirectory() ).thenReturn( isDir );
    when( attrs.getSize() ).thenReturn( size );

    if ( modified != null ) {
      FileTime fileTime = FileTime.fromMillis( modified.toEpochMilli() );
      when( attrs.getModifyTime() ).thenReturn( fileTime );
    } else {
      when( attrs.getModifyTime() ).thenReturn( null );
    }

    return entry;
  }
}
