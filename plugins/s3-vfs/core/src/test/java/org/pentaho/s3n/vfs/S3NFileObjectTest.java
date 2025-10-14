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

package org.pentaho.s3n.vfs;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.FilesCache;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.VfsComponentContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;

/**
 * created by: dzmitry_bahdanovich date: 10/18/13
 */
public class S3NFileObjectTest {

  public static final String HOST = "S3";
  public static final String SCHEME = "s3n";
  public static final int PORT = 843;

  public static final String BUCKET_NAME = "bucket3";
  public static final String OBJECT_NAME = "obj";

  private S3NFileName filename;
  private S3NFileSystem fileSystem;
  private S3NFileObject s3FileObjectBucketSpy;
  private S3NFileObject s3FileObjectFileSpy;
  private S3NFileObject s3FileObjectSpyRoot;
  private AmazonS3 s3ServiceMock;
  private ObjectListing childObjectListing;
  private S3Object s3ObjectMock;
  private S3ObjectInputStream s3ObjectInputStream;
  private ObjectMetadata s3ObjectMetadata;
  private List<String> childObjectNameComp = new ArrayList<>();
  private List<String> childBucketNameListComp = new ArrayList<>();
  private long contentLength = 42;
  private final String origKey = "some/key";
  private Date testDate = new Date();

  @Before
  public void setUp() throws Exception {

    s3ServiceMock = mock( AmazonS3.class );
    S3Object s3Object = new S3Object();
    s3Object.setKey( OBJECT_NAME );
    s3Object.setBucketName( BUCKET_NAME );

    filename = new S3NFileName( SCHEME, BUCKET_NAME, "/" + BUCKET_NAME, FileType.FOLDER );
    S3NFileName rootFileName = new S3NFileName( SCHEME, BUCKET_NAME, "", FileType.FOLDER );
    fileSystem = new S3NFileSystem( rootFileName, new FileSystemOptions() ) {
      @Override
      public AmazonS3 getS3Client() {
        return s3ServiceMock;
      }
    };
    VfsComponentContext context = mock( VfsComponentContext.class );
    final DefaultFileSystemManager fsm = new DefaultFileSystemManager();
    FilesCache cache = mock( FilesCache.class );
    fsm.setFilesCache( cache );
    fsm.setCacheStrategy( CacheStrategy.ON_RESOLVE );
    when( context.getFileSystemManager() ).thenReturn( fsm );
    fileSystem.setContext( context );

    S3NFileObject s3FileObject = new S3NFileObject( filename, fileSystem );
    s3FileObjectBucketSpy = spy( s3FileObject );

    s3FileObjectFileSpy = spy( new S3NFileObject(
      new S3NFileName( SCHEME, BUCKET_NAME, "/" + BUCKET_NAME + "/" + origKey, FileType.IMAGINARY ), fileSystem ) );

    S3NFileObject s3FileObjectRoot = new S3NFileObject( rootFileName, fileSystem );
    s3FileObjectSpyRoot = spy( s3FileObjectRoot );

    // specify the behaviour of S3 Service
    when( s3ServiceMock.getObject( BUCKET_NAME, OBJECT_NAME ) ).thenReturn( s3Object );
    when( s3ServiceMock.getObject( BUCKET_NAME, OBJECT_NAME ) ).thenReturn( s3Object );
    when( s3ServiceMock.listBuckets() ).thenReturn( createBuckets() );

    when( s3ServiceMock.doesBucketExistV2( BUCKET_NAME ) ).thenReturn( true );

    childObjectListing = mock( ObjectListing.class );
    when( childObjectListing.getObjectSummaries() ).thenReturn( createObjectSummaries( 0 ) ).thenReturn( new ArrayList<>() );
    when( childObjectListing.getCommonPrefixes() ).thenReturn( new ArrayList<>() ).thenReturn( createCommonPrefixes( 3 ) );
    when( childObjectListing.isTruncated() ).thenReturn( true ).thenReturn( false );

    when( s3ServiceMock.listObjects( any( ListObjectsRequest.class ) ) ).thenReturn( childObjectListing );
    when( s3ServiceMock.listObjects( anyString(), anyString() ) ).thenReturn( childObjectListing );
    when( s3ServiceMock.listNextBatchOfObjects( any( ObjectListing.class ) ) ).thenReturn( childObjectListing );

    s3ObjectMock = mock( S3Object.class );
    s3ObjectInputStream = mock( S3ObjectInputStream.class );
    s3ObjectMetadata = mock( ObjectMetadata.class );
    when( s3ObjectMock.getObjectContent() ).thenReturn( s3ObjectInputStream );
    when( s3ServiceMock.getObjectMetadata( anyString(), anyString() ) ).thenReturn( s3ObjectMetadata );
    when( s3ObjectMetadata.getContentLength() ).thenReturn( contentLength );
    when( s3ObjectMetadata.getLastModified() ).thenReturn( testDate );
    when( s3ServiceMock.getObject( anyString(), anyString() ) ).thenReturn( s3ObjectMock );
  }

  @Test
  public void testGetS3Object() throws Exception {
    when( s3ServiceMock.getObject( anyString(), anyString() ) ).thenReturn( new S3Object() );
    S3NFileObject s3FileObject = new S3NFileObject( filename, fileSystem );
    S3Object s3Object = s3FileObject.getS3Object();
    assertNotNull( s3Object );
  }

  @Test
  public void testDoGetInputStream() throws Exception {
    assertNotNull( s3FileObjectBucketSpy.getInputStream() );
  }

  @Test
  public void testDoGetTypeImaginary() throws Exception {
    assertEquals( FileType.IMAGINARY, s3FileObjectFileSpy.getType() );
  }

  @Test
  public void testDoGetTypeFolder() throws Exception {
    FileName mockFile = mock( FileName.class );
    when( s3FileObjectBucketSpy.getName() ).thenReturn( mockFile );
    when( mockFile.getPath() ).thenReturn( S3NFileObject.DELIMITER );
    assertEquals( FileType.FOLDER, s3FileObjectBucketSpy.getType() );
  }

  @Test
  public void testDoCreateFolder() throws Exception {
    S3NFileObject notRootBucket = spy(
      new S3NFileObject( new S3NFileName( SCHEME, BUCKET_NAME, "/" + BUCKET_NAME + "/" + origKey, FileType.IMAGINARY ),
        fileSystem ) );
    notRootBucket.createFolder();
    ArgumentCaptor<PutObjectRequest> putObjectRequestArgumentCaptor = ArgumentCaptor.forClass( PutObjectRequest.class );
    verify( s3ServiceMock ).putObject( putObjectRequestArgumentCaptor.capture() );
    assertEquals( BUCKET_NAME, putObjectRequestArgumentCaptor.getValue().getBucketName() );
    assertEquals( "some/key/", putObjectRequestArgumentCaptor.getValue().getKey() );
  }

  @Test
  public void testCanRenameTo() throws Exception {
    FileObject newFile = mock( FileObject.class );
    assertFalse( s3FileObjectBucketSpy.canRenameTo( newFile ) );
    when( s3FileObjectBucketSpy.getType() ).thenReturn( FileType.FOLDER );
    assertFalse( s3FileObjectBucketSpy.canRenameTo( newFile ) );
  }

  @Test( expected = NullPointerException.class )
  public void testCanRenameToNullFile() throws Exception {
    // This is a bug / weakness in VFS itself
    s3FileObjectBucketSpy.canRenameTo( null );
  }

  @Test
  public void testDoRename() throws Exception {
    String someNewBucketName = "someNewBucketName";
    String someNewKey = "some/newKey";
    S3NFileName newFileName = new S3NFileName( SCHEME, someNewBucketName, "/" + someNewBucketName + "/" + someNewKey, FileType.FILE );
    S3NFileObject newFile = new S3NFileObject( newFileName, fileSystem );
    ArgumentCaptor<CopyObjectRequest> copyObjectRequestArgumentCaptor = ArgumentCaptor.forClass( CopyObjectRequest.class );
    when( s3ServiceMock.doesBucketExistV2( someNewBucketName ) ).thenReturn( true );
    s3FileObjectFileSpy.moveTo( newFile );

    verify( s3ServiceMock ).copyObject( copyObjectRequestArgumentCaptor.capture() );
    assertEquals( someNewBucketName, copyObjectRequestArgumentCaptor.getValue().getDestinationBucketName() );
    assertEquals( someNewKey, copyObjectRequestArgumentCaptor.getValue().getDestinationKey() );
    assertEquals( BUCKET_NAME, copyObjectRequestArgumentCaptor.getValue().getSourceBucketName() );
    assertEquals( origKey, copyObjectRequestArgumentCaptor.getValue().getSourceKey() );
  }

  @Test
  public void testDoGetLastModifiedTime() throws Exception {
    s3FileObjectFileSpy.doAttach();
    assertEquals( testDate.getTime(), s3FileObjectFileSpy.doGetLastModifiedTime() );
  }

  @Test
  public void testDoGetLastModifiedTimeWhenNoLastModifiedDateIsAvailable() throws Exception {
    s3FileObjectFileSpy.doAttach();
    when( s3ObjectMetadata.getLastModified() ).thenReturn( new Date(0L) );
    assertEquals( 0L, s3FileObjectFileSpy.doGetLastModifiedTime() );
  }


  @Test
  public void testListChildrenNotRoot() throws FileSystemException {
    fileSystem.init();
    FileObject[] children = s3FileObjectBucketSpy.getChildren();
    assertTrue( children.length == 6 );
    List<String> childNameArray = Arrays.asList( children ).stream()
      .map( child -> child.getName().getPath() )
      .collect( Collectors.toList() );
    assertEquals( childObjectNameComp, childNameArray );
  }

  @Test
  public void testListChildrenRoot() throws FileSystemException {
    fileSystem.init();
    FileObject[] children = s3FileObjectSpyRoot.getChildren();
    assertTrue( children.length == 4 );
    List<String> childNameArray = Arrays.asList( children ).stream()
      .map( child -> child.getName().getPath() )
      .collect( Collectors.toList() );
    assertEquals( childBucketNameListComp, childNameArray );
  }

  @Test
  public void testHandleAttachException() throws FileSystemException {
    AmazonS3Exception exception = new AmazonS3Exception( "NoSuchKey" );
    exception.setErrorCode( "NoSuchKey" );
    //test the case where the folder exists and contains things; no exception should be thrown
    when( s3ServiceMock.getObjectMetadata( BUCKET_NAME, origKey ) ).thenThrow( exception );
    try {
      s3FileObjectFileSpy.doAttach();
    } catch ( Exception e ) {
      fail( "Caught exception " + e.getMessage() );
    }
    assertEquals( FileType.FOLDER, s3FileObjectFileSpy.getType() );
  }

  @Test
  public void testHandleAttachExceptionEmptyFolder() throws FileSystemException {
    AmazonS3Exception exception = new AmazonS3Exception( "NoSuchKey" );
    exception.setErrorCode( "NoSuchKey" );

    //test the case where the folder exists and contains things; no exception should be thrown
    when( s3ServiceMock.getObject( BUCKET_NAME, origKey ) ).thenThrow( exception );
    when( s3ServiceMock.getObject( BUCKET_NAME, origKey + "/" ) ).thenThrow( exception );
    childObjectListing = mock( ObjectListing.class );
    when( childObjectListing.getObjectSummaries() ).thenReturn( new ArrayList<>() );
    when( childObjectListing.getCommonPrefixes() ).thenReturn( new ArrayList<>() );
    when( s3ServiceMock.listObjects( any( ListObjectsRequest.class ) ) ).thenReturn( childObjectListing );
    try {
      s3FileObjectFileSpy.doAttach();
    } catch ( Exception e ) {
      fail( "Caught exception " + e.getMessage() );
    }
    assertEquals( FileType.IMAGINARY, s3FileObjectFileSpy.getType() );
  }

  @Test
  public void testHandleAttachExceptionFileNotFound() throws FileSystemException {
    AmazonS3Exception notFoundException = new AmazonS3Exception( "404 Not Found" );
    notFoundException.setErrorCode( "404 Not Found" );
    AmazonS3Exception noSuchKeyException = new AmazonS3Exception( "NoSuchKey" );
    noSuchKeyException.setErrorCode( "NoSuchKey" );

    //test the case where the file is not found; no exception should be thrown
    when( s3ServiceMock.getObject( BUCKET_NAME, origKey ) ).thenThrow( notFoundException );
    when( s3ServiceMock.getObject( BUCKET_NAME, origKey + "/" ) ).thenThrow( noSuchKeyException );
    childObjectListing = mock( ObjectListing.class );
    when( childObjectListing.getObjectSummaries() ).thenReturn( new ArrayList<>() );
    when( childObjectListing.getCommonPrefixes() ).thenReturn( new ArrayList<>() );
    when( s3ServiceMock.listObjects( any( ListObjectsRequest.class ) ) ).thenReturn( childObjectListing );
    try {
      s3FileObjectFileSpy.doAttach();
    } catch ( Exception e ) {
      fail( "Caught exception " + e.getMessage() );
    }
    assertEquals( FileType.IMAGINARY, s3FileObjectFileSpy.getType() );
  }

  private List<Bucket> createBuckets() {
    List<Bucket> buckets = new ArrayList<>();
    buckets.add( new Bucket( "bucket1" ) );
    buckets.add( new Bucket( "bucket2" ) );
    buckets.add( new Bucket( "bucket3" ) );
    buckets.add( new Bucket( "bucket4" ) );
    childBucketNameListComp.addAll(
      buckets.stream().map( bucket -> "/" + bucket.getName() ).collect( Collectors.toList() ) );
    return buckets;
  }

  private List<S3ObjectSummary> createObjectSummaries( int startnum ) {
    List<S3ObjectSummary> summaries = new ArrayList<>();
    for ( int i = startnum; i < startnum + 3; i++ ) {
      S3ObjectSummary summary = new S3ObjectSummary();
      summary.setBucketName( BUCKET_NAME );
      summary.setKey( "key" + i );
      summaries.add( summary );
      childObjectNameComp.add( "/" + BUCKET_NAME + "/" + "key" + i );
    }
    return summaries;
  }

  private List<String> createCommonPrefixes( int startnum ) {
    List<String> prefixes = new ArrayList<>();
    for ( int i = startnum; i < startnum + 3; i++ ) {
      prefixes.add( "key" + i );
      childObjectNameComp.add( "/" + BUCKET_NAME + "/" + "key" + i );
    }
    return prefixes;
  }

}
