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

package org.pentaho.s3.vfs;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.FilesCache;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.VfsComponentContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.StorageUnitConverter;
import org.pentaho.s3common.S3KettleProperty;
import org.pentaho.s3common.TestCleanupUtil;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * created by: dzmitry_bahdanovich date: 10/18/13
 */
public class S3FileObjectTest {

  public static final String HOST = "S3";
  public static final String SCHEME = "s3";
  public static final int PORT = 843;

  public static final String BUCKET_NAME = "bucket3";
  public static final String OBJECT_NAME = "obj";

  private S3FileName filename;
  private S3FileSystem fileSystem;
  private S3FileObject s3FileObjectBucketSpy;
  private S3FileObject s3FileObjectFileSpy;
  private S3FileObject s3FileObjectSpyRoot;
  private AmazonS3 s3ServiceMock;
  private ObjectListing childObjectListing;
  private S3Object s3ObjectMock;
  private S3ObjectInputStream s3ObjectInputStream;
  private ObjectMetadata s3ObjectMetadata;

  private final List<String> childObjectNameComp = new ArrayList<>();
  private final List<String> childBucketNameListComp = new ArrayList<>();
  private final long contentLength = 42;
  private final String origKey = "some/key";
  private final Date testDate = new Date();

  @BeforeClass
  public static void setClassUp() throws KettleException {
    KettleEnvironment.init( false );
  }

  @AfterClass
  public static void tearDownClass() {
    KettleEnvironment.shutdown();
    TestCleanupUtil.cleanUpLogsDir();
  }

  @Before
  public void setUp() throws FileSystemException {
    s3ServiceMock = mock( AmazonS3.class );
    S3Object s3Object = new S3Object();
    s3Object.setKey( OBJECT_NAME );
    s3Object.setBucketName( BUCKET_NAME );

    filename = new S3FileName( SCHEME, BUCKET_NAME, BUCKET_NAME, FileType.FOLDER );
    S3FileName rootFileName = new S3FileName( SCHEME, BUCKET_NAME, "", FileType.FOLDER );
    S3KettleProperty s3KettleProperty  = mock( S3KettleProperty.class );
    when( s3KettleProperty.getPartSize() ).thenReturn( "5MB" );
    fileSystem =
        new S3FileSystem( rootFileName, new FileSystemOptions(), new StorageUnitConverter(), s3KettleProperty ) {
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

    S3FileObject s3FileObject = new S3FileObject( filename, fileSystem );
    s3FileObjectBucketSpy = spy( s3FileObject );

    s3FileObjectFileSpy = spy( new S3FileObject(
      new S3FileName( SCHEME, BUCKET_NAME, BUCKET_NAME + "/" + origKey, FileType.IMAGINARY ), fileSystem ) );

    S3FileObject s3FileObjectRoot = new S3FileObject( rootFileName, fileSystem );
    s3FileObjectSpyRoot = spy( s3FileObjectRoot );

    // specify the behaviour of S3 Service
    //when( s3ServiceMock.getBucket( BUCKET_NAME ) ).thenReturn( testBucket );
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
  public void testGetS3Object() throws FileSystemException {
    when( s3ServiceMock.getObject( anyString(), anyString() ) ).thenReturn( new S3Object() );
    try ( S3FileObject s3FileObject = new S3FileObject( filename, fileSystem ) ) {
      S3Object s3Object = s3FileObject.getS3Object();
      assertNotNull( s3Object );
    }
  }

  @Test
  public void testGetS3BucketName() {
    filename = new S3FileName( SCHEME, BUCKET_NAME, "", FileType.FOLDER );
    when( s3FileObjectBucketSpy.getName() ).thenReturn( filename );
    s3FileObjectBucketSpy.getS3BucketName();
  }

  @Test
  public void testDoGetInputStream() throws FileSystemException {
    assertNotNull( s3FileObjectBucketSpy.getInputStream() );
  }

  @Test
  public void testDoGetTypeImaginary() throws FileSystemException {
    assertEquals( FileType.IMAGINARY, s3FileObjectFileSpy.getType() );
  }

  @Test
  public void testDoGetTypeFolder() throws FileSystemException {
    FileName mockFile = mock( FileName.class );
    when( s3FileObjectBucketSpy.getName() ).thenReturn( mockFile );
    when( mockFile.getPath() ).thenReturn( S3FileObject.DELIMITER );
    assertEquals( FileType.FOLDER, s3FileObjectBucketSpy.getType() );
  }

  @Test
  public void testDoCreateFolder() throws FileSystemException {
    S3FileObject notRootBucket =
        spy( new S3FileObject( new S3FileName( SCHEME, BUCKET_NAME, BUCKET_NAME + "/" + origKey, FileType.IMAGINARY ),
            fileSystem ) );
    notRootBucket.createFolder();
    ArgumentCaptor<PutObjectRequest> putObjectRequestArgumentCaptor = ArgumentCaptor.forClass( PutObjectRequest.class );
    verify( s3ServiceMock ).putObject( putObjectRequestArgumentCaptor.capture() );
    assertEquals( BUCKET_NAME, putObjectRequestArgumentCaptor.getValue().getBucketName() );
    assertEquals( "some/key/", putObjectRequestArgumentCaptor.getValue().getKey() );
  }

  @Test
  public void testCanRenameTo() throws FileSystemException {
    FileObject newFile = mock( FileObject.class );
    assertFalse( s3FileObjectBucketSpy.canRenameTo( newFile ) );
    when( s3FileObjectBucketSpy.getType() ).thenReturn( FileType.FOLDER );
    assertFalse( s3FileObjectBucketSpy.canRenameTo( newFile ) );
  }

  @Test( expected = NullPointerException.class )
  public void testCanRenameToNullFile() {
    // This is a bug / weakness in VFS itself
    s3FileObjectBucketSpy.canRenameTo( null );
  }

  @Test
  public void testDoDelete() throws FileSystemException {
    fileSystem.init();
    s3FileObjectBucketSpy.doDelete();
    verify( s3ServiceMock ).deleteObject( "bucket3", "/" );
  }

  @Test
  public void testDoRename() throws Exception {
    String someNewBucketName = "someNewBucketName";
    String someNewKey = "some/newKey";
    S3FileName newFileName = new S3FileName( SCHEME, someNewBucketName, someNewBucketName + "/" + someNewKey, FileType.FILE );
    S3FileObject newFile = new S3FileObject( newFileName, fileSystem );
    ArgumentCaptor<CopyObjectRequest> copyObjectRequestArgumentCaptor = ArgumentCaptor.forClass( CopyObjectRequest.class );
    when( s3ServiceMock.doesBucketExistV2( someNewBucketName ) ).thenReturn( true );
    s3FileObjectFileSpy.doAttach();
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
    when( s3ObjectMetadata.getLastModified() ).thenReturn( new Date( 0L ) );
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
  public void testFixFilePathToFile() {
    String bucketName = "s3:/";
    String key = "bucketName/some/key/path";
    SimpleEntry<String, String> newPath = s3FileObjectBucketSpy.fixFilePath( key, bucketName );
    assertEquals( "bucketName", newPath.getValue() );
    assertEquals( "some/key/path", newPath.getKey() );
  }

  @Test
  public void testFixFilePathToFolder() {
    String bucketName = "s3:/";
    String key = "bucketName";
    SimpleEntry<String, String> newPath = s3FileObjectBucketSpy.fixFilePath( key, bucketName );
    assertEquals( "bucketName", newPath.getValue() );
    assertEquals( "", newPath.getKey() );
  }

  @Test
  public void testHandleAttachException() throws FileSystemException {
    String testKey = BUCKET_NAME + "/" + origKey;
    String testBucket = "badBucketName";
    AmazonS3Exception exception = new AmazonS3Exception( "NoSuchKey" );

    //test the case where the folder exists and contains things; no exception should be thrown
    when( s3ServiceMock.getObject( BUCKET_NAME, origKey + "/" ) ).thenThrow( exception );
    try {
      s3FileObjectFileSpy.handleAttachException( testKey, testBucket );
    } catch ( FileSystemException e ) {
      fail( "Caught exception " + e.getMessage() );
    }
    assertEquals( FileType.FOLDER, s3FileObjectFileSpy.getType() );
  }

  @Test
  public void testHandleAttachExceptionEmptyFolder() throws FileSystemException {
    String testKey = BUCKET_NAME + "/" + origKey;
    String testBucket = "badBucketName";
    AmazonS3Exception exception = new AmazonS3Exception( "NoSuchKey" );
    exception.setErrorCode( "NoSuchKey" );

    //test the case where the folder exists and contains things; no exception should be thrown
    when( s3ServiceMock.getObject( BUCKET_NAME, origKey + "/" ) ).thenThrow( exception );
    childObjectListing = mock( ObjectListing.class );
    when( childObjectListing.getObjectSummaries() ).thenReturn( new ArrayList<>() );
    when( childObjectListing.getCommonPrefixes() ).thenReturn( new ArrayList<>() );
    when( s3ServiceMock.listObjects( any( ListObjectsRequest.class ) ) ).thenReturn( childObjectListing );
    try {
      s3FileObjectFileSpy.handleAttachException( testKey, testBucket );
    } catch ( FileSystemException e ) {
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
