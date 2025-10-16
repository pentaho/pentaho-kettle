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

import java.io.InputStream;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.s3.vfs.S3FileObject;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.Copy;
import com.amazonaws.services.s3.transfer.TransferManager;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class S3TransferManagerTest {

  private TransferManager transferManager;
  private S3TransferManager s3TransferManager;
  private S3FileObject src;
  private S3FileObject dst;

  private static class TestS3FileObject extends S3FileObject {
    public TestS3FileObject( String bucketName, String key ) {
      super(
        new org.pentaho.s3.vfs.S3FileName( "s3", bucketName, "/" + bucketName + "/" + key, org.apache.commons.vfs2.FileType.FILE ),
        createFileSystemWithUseCount()
      );
      // Direct assignment since bucketName and key are no longer final or private
      this.bucketName = bucketName;
      this.key = key;
    }
    private static org.pentaho.s3.vfs.S3FileSystem createFileSystemWithUseCount() {
      org.pentaho.s3.vfs.S3FileSystem fs = org.mockito.Mockito.mock( org.pentaho.s3.vfs.S3FileSystem.class, org.mockito.Mockito.CALLS_REAL_METHODS );
      try {
        java.lang.reflect.Field useCountField = org.apache.commons.vfs2.provider.AbstractFileSystem.class.getDeclaredField( "useCount" );
        useCountField.setAccessible( true );
        useCountField.set( fs, new java.util.concurrent.atomic.AtomicLong() );
      } catch ( Exception e ) {
        throw new RuntimeException( e );
      }
      return fs;
    }
    @Override
    public String getQualifiedName() {
      return "s3://" + getBucketName() + "/" + getKey();
    }
    public String getBucketName() {
      try {
        java.lang.reflect.Field bucketField = S3CommonFileObject.class.getDeclaredField( "bucketName" );
        bucketField.setAccessible( true );
        return (String) bucketField.get( this );
      } catch ( Exception e ) {
        throw new RuntimeException( e );
      }
    }
    public String getKey() {
      try {
        java.lang.reflect.Field keyField = S3CommonFileObject.class.getDeclaredField( "key" );
        keyField.setAccessible( true );
        return (String) keyField.get( this );
      } catch ( Exception e ) {
        throw new RuntimeException( e );
      }
    }
  }

  @Before
  public void setUp() {
    transferManager = mock( TransferManager.class );
    s3TransferManager = new S3TransferManager( transferManager );
    src = new TestS3FileObject( "src-bucket", "src-key" );
    dst = new TestS3FileObject( "dst-bucket", "dst-key" );
  }

  @After
  public void tearDown() {
    transferManager = null;
    s3TransferManager = null;
    src = null;
    dst = null;
  }

  @Test
  public void testGetTransferManager() {
    assertEquals( transferManager, s3TransferManager.getTransferManager() );
  }

  @Test
  public void testCopy_Success() throws FileSystemException, InterruptedException {
    Copy copy = mock( Copy.class );
    when( transferManager.copy( anyString(), anyString(), anyString(), anyString() ) ).thenReturn( copy );
    doNothing().when( copy ).waitForCompletion();
    // Mock testConnection: getAmazonS3Client().getObjectMetadata/putObject/deleteObject
    var s3Client = mock( com.amazonaws.services.s3.AmazonS3.class );
    when( transferManager.getAmazonS3Client() ).thenReturn( s3Client );
    // No exception means access is fine
    s3TransferManager.copy( src, dst );
    verify( transferManager, times( 1 ) ).copy( "src-bucket", "src-key", "dst-bucket", "dst-key" );
    verify( copy, times( 1 ) ).waitForCompletion();
  }

  @Test( expected = FileSystemException.class )
  public void testCopy_NullArguments() throws FileSystemException {
    s3TransferManager.copy( null, dst );
  }

  @Test( expected = FileSystemException.class )
  public void testCopy_MissingBucketOrKey() throws FileSystemException, NoSuchFieldException, IllegalAccessException {
    java.lang.reflect.Field bucketField = S3CommonFileObject.class.getDeclaredField( "bucketName" );
    bucketField.setAccessible( true );
    bucketField.set( src, null );
    s3TransferManager.copy( src, dst );
  }

  @Test( expected = FileSystemException.class )
  public void testCopy_ReadAccessDenied() throws FileSystemException {
    var s3Client = mock( com.amazonaws.services.s3.AmazonS3.class );
    when( transferManager.getAmazonS3Client() ).thenReturn( s3Client );
    doThrow( new AmazonClientException( "no read" ) ).when( s3Client ).getObjectMetadata( anyString(), anyString() );
    s3TransferManager.copy( src, dst );
  }

  @Test( expected = FileSystemException.class )
  public void testCopy_WriteAccessDenied() throws FileSystemException {
    var s3Client = mock( com.amazonaws.services.s3.AmazonS3.class );
    when( transferManager.getAmazonS3Client() ).thenReturn( s3Client );
    // Read access OK
    // Write access denied
    doThrow( new AmazonClientException( "no write" ) ).when( s3Client ).putObject( anyString(), contains( ".acltest-" ), anyString() );
    s3TransferManager.copy( src, dst );
  }

  @Test( expected = FileSystemException.class )
  public void testCopy_AmazonClientExceptionDuringCopy() throws FileSystemException, InterruptedException {
    Copy copy = mock( Copy.class );
    when( transferManager.copy( anyString(), anyString(), anyString(), anyString() ) ).thenReturn( copy );
    doThrow( new AmazonClientException( "fail" ) ).when( copy ).waitForCompletion();
    var s3Client = mock( com.amazonaws.services.s3.AmazonS3.class );
    when( transferManager.getAmazonS3Client() ).thenReturn( s3Client );
    s3TransferManager.copy( src, dst );
  }

  @Test( expected = FileSystemException.class )
  public void testCopy_InterruptedException() throws FileSystemException, InterruptedException {
    Copy copy = mock( Copy.class );
    when( transferManager.copy( anyString(), anyString(), anyString(), anyString() ) ).thenReturn( copy );
    doThrow( new InterruptedException( "interrupted" ) ).when( copy ).waitForCompletion();
    var s3Client = mock( com.amazonaws.services.s3.AmazonS3.class );
    when( transferManager.getAmazonS3Client() ).thenReturn( s3Client );
    try {
      s3TransferManager.copy( src, dst );
    } finally {
      // Clear the interrupted status so it doesn't affect other tests
      Thread.interrupted();
    }
  }

  @Test
  public void testUpload_Success() throws Exception {
    FileObject srcMock = mock( FileObject.class );
    FileContent content = mock( FileContent.class );
    InputStream inputStream = mock( InputStream.class );
    when( srcMock.getContent() ).thenReturn( content );
    when( content.getInputStream() ).thenReturn( inputStream );
    when( content.getSize() ).thenReturn( 123L );
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength( 123L );
    com.amazonaws.services.s3.transfer.Upload upload = mock( com.amazonaws.services.s3.transfer.Upload.class );
    when( transferManager.upload( eq( "dst-bucket" ), eq( "dst-key" ), eq( inputStream ), any( ObjectMetadata.class ) ) ).thenReturn( upload );
    s3TransferManager.upload( srcMock, dst );
    verify( transferManager, times( 1 ) ).upload( eq( "dst-bucket" ), eq( "dst-key" ), eq( inputStream ), any( ObjectMetadata.class ) );
    verify( upload, times( 1 ) ).waitForUploadResult();
  }

  @Test( expected = FileSystemException.class )
  public void testUpload_NullArguments() throws Exception {
    s3TransferManager.upload( null, null );
  }

  @Test( expected = FileSystemException.class )
  public void testUpload_FileSystemException() throws Exception {
    FileObject srcMock = mock( FileObject.class );
    FileContent content = mock( FileContent.class );
    when( srcMock.getContent() ).thenReturn( content );
    when( content.getInputStream() ).thenThrow( new FileSystemException( "Input stream error" ) );
    s3TransferManager.upload( srcMock, dst );
  }

  @Test( expected = FileSystemException.class )
  public void testUpload_AmazonClientException() throws Exception {
    FileObject srcMock = mock( FileObject.class );
    FileContent content = mock( FileContent.class );
    InputStream inputStream = mock( InputStream.class );
    when( srcMock.getContent() ).thenReturn( content );
    when( content.getInputStream() ).thenReturn( inputStream );
    when( content.getSize() ).thenReturn( 123L );
    when( transferManager.upload( eq( "dst-bucket" ), eq( "dst-key" ), eq( inputStream ), any( ObjectMetadata.class ) ) )
      .thenThrow( new AmazonClientException( "Amazon error" ) );
    s3TransferManager.upload( srcMock, dst );
  }

  @Test( expected = FileSystemException.class )
  public void testUpload_InterruptedException() throws Exception {
    FileObject srcMock = mock( FileObject.class );
    FileContent content = mock( FileContent.class );
    InputStream inputStream = mock( InputStream.class );
    when( srcMock.getContent() ).thenReturn( content );
    when( content.getInputStream() ).thenReturn( inputStream );
    when( content.getSize() ).thenReturn( 123L );
    com.amazonaws.services.s3.transfer.Upload upload = mock( com.amazonaws.services.s3.transfer.Upload.class );
    when( transferManager.upload( eq( "dst-bucket" ), eq( "dst-key" ), eq( inputStream ), any( ObjectMetadata.class ) ) ).thenReturn( upload );
    doThrow( new InterruptedException( "interrupted" ) ).when( upload ).waitForUploadResult();
    try {
      s3TransferManager.upload( srcMock, dst );
    } finally {
      Thread.interrupted();
    }
  }
}
