/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.s3.vfs;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.pentaho.s3common.S3CommonFileObject;
import org.pentaho.s3common.S3CommonFileSystem;
import org.pentaho.s3common.S3KettleProperty;
import org.pentaho.s3common.TestCleanupUtil;

import com.amazonaws.SdkClientException;
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
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
    when( s3KettleProperty.getCacheTtlSeconds() ).thenReturn( S3KettleProperty.S3VFS_CACHE_TTL_SECONDS_DEFAULT );
    when( s3KettleProperty.isCacheEnabled() ).thenReturn( S3KettleProperty.S3VFS_CACHE_ENABLED_DEFAULT );
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
  public void testDoAttachReusesListingMetadataWithoutExtraS3Calls() throws Exception {
    fileSystem.init();

    // Simulate what "Get File Names" sees when listing a flat S3 folder: a single file, with its
    // size already returned by the listing, no truncation/subfolders.
    ObjectListing singleFileListing = mock( ObjectListing.class );
    S3ObjectSummary summary = new S3ObjectSummary();
    summary.setBucketName( BUCKET_NAME );
    summary.setKey( "cachedFile.txt" );
    summary.setSize( 12345L );
    when( singleFileListing.getObjectSummaries() ).thenReturn( Collections.singletonList( summary ) );
    when( singleFileListing.getCommonPrefixes() ).thenReturn( Collections.emptyList() );
    when( singleFileListing.isTruncated() ).thenReturn( false );
    when( s3ServiceMock.listObjects( any( ListObjectsRequest.class ) ) ).thenReturn( singleFileListing );

    // Listing the bucket's children must cache "cachedFile.txt"'s metadata.
    s3FileObjectBucketSpy.getChildren();

    // Resolving/attaching that same child right afterwards, as Commons-VFS traversal does, must
    // reuse the cached metadata instead of issuing a redundant getObjectMetadata request.
    S3FileObject cachedChild = new S3FileObject(
      new S3FileName( SCHEME, BUCKET_NAME, BUCKET_NAME + "/cachedFile.txt", FileType.IMAGINARY ), fileSystem );
    cachedChild.doAttach();

    assertEquals( FileType.FILE, cachedChild.getType() );
    assertEquals( 12345L, cachedChild.getContent().getSize() );
    verify( s3ServiceMock, never() ).getObjectMetadata( BUCKET_NAME, "cachedFile.txt" );
  }

  @Test
  public void testDoAttachReusesCachedFolderMetadataFromListingWithoutExtraS3Calls() throws Exception {
    fileSystem.init();

    // Simulate a listing where a subfolder is only known via a common prefix (no zero-byte
    // marker object), exactly like a "virtual folder" inferred from object keys.
    ObjectListing folderListing = mock( ObjectListing.class );
    when( folderListing.getObjectSummaries() ).thenReturn( Collections.emptyList() );
    when( folderListing.getCommonPrefixes() ).thenReturn( Collections.singletonList( "cachedFolder/" ) );
    when( folderListing.isTruncated() ).thenReturn( false );
    when( s3ServiceMock.listObjects( any( ListObjectsRequest.class ) ) ).thenReturn( folderListing );

    // Listing the bucket's children must cache "cachedFolder/" as a folder.
    s3FileObjectBucketSpy.getChildren();

    // Resolving/attaching that same child right afterwards, as Commons-VFS traversal does, must
    // reuse the cached folder marker instead of issuing any redundant S3 calls.
    S3FileObject cachedFolderChild = new S3FileObject(
      new S3FileName( SCHEME, BUCKET_NAME, BUCKET_NAME + "/cachedFolder", FileType.IMAGINARY ), fileSystem );
    cachedFolderChild.doAttach();

    assertEquals( FileType.FOLDER, cachedFolderChild.getType() );
    verify( s3ServiceMock, never() ).getObjectMetadata( BUCKET_NAME, "cachedFolder" );
    verify( s3ServiceMock, never() ).getObject( BUCKET_NAME, "cachedFolder/" );
    // Only the one listing call (from getChildren()) should have happened; the cache hit must not
    // trigger any additional listObjects fallback call.
    verify( s3ServiceMock, times( 1 ) ).listObjects( any( ListObjectsRequest.class ) );
  }

  @Test
  public void testDoAttachTreatsListedFolderMarkerSummaryAsFolderWithoutExtraS3Calls() throws Exception {
    fileSystem.init();

    S3ObjectSummary marker = new S3ObjectSummary();
    marker.setBucketName( BUCKET_NAME );
    marker.setKey( "markerFolder/" );
    marker.setSize( 0L );
    ObjectListing markerListing = mock( ObjectListing.class );
    when( markerListing.getObjectSummaries() ).thenReturn( Collections.singletonList( marker ) );
    when( markerListing.getCommonPrefixes() ).thenReturn( Collections.emptyList() );
    when( markerListing.isTruncated() ).thenReturn( false );
    when( s3ServiceMock.listObjects( any( ListObjectsRequest.class ) ) ).thenReturn( markerListing );

    s3FileObjectBucketSpy.getChildren();

    S3FileObject markerChild = new S3FileObject(
      new S3FileName( SCHEME, BUCKET_NAME, BUCKET_NAME + "/markerFolder", FileType.IMAGINARY ), fileSystem );
    markerChild.doAttach();

    assertEquals( FileType.FOLDER, markerChild.getType() );
    verify( s3ServiceMock, never() ).getObjectMetadata( BUCKET_NAME, "markerFolder" );
    verify( s3ServiceMock, never() ).getObject( BUCKET_NAME, "markerFolder/" );
  }

  private S3FileObject listAndAttachCachedFile( String name ) throws Exception {
    fileSystem.init();
    ObjectListing listing = mock( ObjectListing.class );
    S3ObjectSummary summary = new S3ObjectSummary();
    summary.setBucketName( BUCKET_NAME );
    summary.setKey( name );
    summary.setSize( 12345L );
    when( listing.getObjectSummaries() ).thenReturn( Collections.singletonList( summary ) );
    when( listing.getCommonPrefixes() ).thenReturn( Collections.emptyList() );
    when( listing.isTruncated() ).thenReturn( false );
    when( s3ServiceMock.listObjects( any( ListObjectsRequest.class ) ) ).thenReturn( listing );
    s3FileObjectBucketSpy.getChildren();

    S3FileObject child = new S3FileObject(
      new S3FileName( SCHEME, BUCKET_NAME, BUCKET_NAME + "/" + name, FileType.IMAGINARY ), fileSystem );
    child.doAttach();
    verify( s3ServiceMock, never() ).getObjectMetadata( BUCKET_NAME, name );
    return child;
  }

  private void assertFreshAttachHitsS3( String name ) throws Exception {
    S3FileObject fresh = new S3FileObject(
      new S3FileName( SCHEME, BUCKET_NAME, BUCKET_NAME + "/" + name, FileType.IMAGINARY ), fileSystem );
    fresh.doAttach();
    verify( s3ServiceMock, atLeastOnce() ).getObjectMetadata( BUCKET_NAME, name );
  }

  @Test
  public void testDoDeleteInvalidatesCachedListingEntry() throws Exception {
    S3FileObject child = listAndAttachCachedFile( "deleteMe.txt" );

    child.doDelete();

    verify( s3ServiceMock ).deleteObject( BUCKET_NAME, "deleteMe.txt" );
    assertFreshAttachHitsS3( "deleteMe.txt" );
  }

  @Test
  public void testDoDeleteOfOnlyChildInvalidatesCachedVirtualParentFolder() throws Exception {
    fileSystem.init();
    S3ObjectSummary summary = new S3ObjectSummary();
    summary.setBucketName( BUCKET_NAME );
    summary.setKey( "virtualDir/only-child.txt" );
    summary.setSize( 7L );
    fileSystem.cacheListedFile( BUCKET_NAME, summary );
    fileSystem.cacheListedFolder( BUCKET_NAME, "virtualDir/" );
    assertTrue( fileSystem.isCachedFolder( BUCKET_NAME, "virtualDir/" ) );
    S3FileObject child = new S3FileObject(
      new S3FileName( SCHEME, BUCKET_NAME, BUCKET_NAME + "/virtualDir/only-child.txt", FileType.IMAGINARY ),
      fileSystem );
    child.doAttach();

    child.doDelete();

    assertFalse( "the now-empty virtual folder must not stay cached", fileSystem.isCachedFolder( BUCKET_NAME, "virtualDir/" ) );
  }

  @Test
  public void testDoCreateFolderInvalidatesCachedListingEntry() throws Exception {
    S3FileObject child = listAndAttachCachedFile( "replaced" );

    Method doCreateFolder = S3CommonFileObject.class.getDeclaredMethod( "doCreateFolder" );
    doCreateFolder.setAccessible( true );
    doCreateFolder.invoke( child );

    verify( s3ServiceMock ).putObject( any( PutObjectRequest.class ) );
    assertFreshAttachHitsS3( "replaced" );
  }

  private void cacheFileOnFileSystem( String bucket, String key ) {
    S3ObjectSummary summary = new S3ObjectSummary();
    summary.setBucketName( bucket );
    summary.setKey( key );
    summary.setSize( 99L );
    fileSystem.cacheListedFile( bucket, summary );
    assertTrue( "sanity check: entry must be cached",
      fileSystem.getCachedFileMetadata( bucket, key ) != null || fileSystem.isCachedFolder( bucket, key ) );
  }

  @Test
  public void testDoCreateFolderInvalidatesRewrittenPutRequestDestination() throws Exception {
    fileSystem.init();
    S3FileObject folderObject = new S3FileObject(
      new S3FileName( SCHEME, BUCKET_NAME, BUCKET_NAME + "/" + origKey, FileType.IMAGINARY ), fileSystem ) {
      @Override
      protected PutObjectRequest createPutObjectRequest( String bucketName, String key,
                                                         InputStream inputStream,
                                                         ObjectMetadata objectMetadata ) {
        // Simulates the legacy-path rewrite done by fixFilePath(): a different bucket and key than the fields.
        return new PutObjectRequest( "rewrittenBucket", "rewritten/folder/", inputStream, objectMetadata );
      }
    };
    cacheFileOnFileSystem( "rewrittenBucket", "rewritten/folder/" );

    Method doCreateFolder = S3CommonFileObject.class.getDeclaredMethod( "doCreateFolder" );
    doCreateFolder.setAccessible( true );
    doCreateFolder.invoke( folderObject );

    assertNull( fileSystem.getCachedFileMetadata( "rewrittenBucket", "rewritten/folder/" ) );
    assertFalse( fileSystem.isCachedFolder( "rewrittenBucket", "rewritten/folder/" ) );
  }

  @Test
  public void testDoRenameInvalidatesRewrittenCopyRequestDestination() throws Exception {
    fileSystem.init();
    S3FileObject source = new S3FileObject(
      new S3FileName( SCHEME, BUCKET_NAME, BUCKET_NAME + "/" + origKey, FileType.IMAGINARY ), fileSystem ) {
      @Override
      protected CopyObjectRequest createCopyObjectRequest( String sourceBucket, String sourceKey,
                                                           String destBucket, String destKey ) {
        return new CopyObjectRequest( sourceBucket, sourceKey, "rewrittenBucket", "rewritten/key" );
      }
    };
    S3FileObject target = new S3FileObject(
      new S3FileName( SCHEME, "otherBucket", "otherBucket/other/key", FileType.FILE ), fileSystem );
    when( s3ServiceMock.doesBucketExistV2( "otherBucket" ) ).thenReturn( true );
    cacheFileOnFileSystem( "rewrittenBucket", "rewritten/key" );
    cacheFileOnFileSystem( "otherBucket", "other/key" );

    source.doAttach();
    source.moveTo( target );

    verify( s3ServiceMock ).copyObject( any( CopyObjectRequest.class ) );
    assertNull( "the destination actually sent to S3 must be invalidated",
      fileSystem.getCachedFileMetadata( "rewrittenBucket", "rewritten/key" ) );
  }

  @Test
  public void testListingFromClientReplacedWhileAcquiringIsNotCached() throws Exception {
    S3KettleProperty props = mock( S3KettleProperty.class );
    when( props.getPartSize() ).thenReturn( "5MB" );
    when( props.getCacheTtlSeconds() ).thenReturn( S3KettleProperty.S3VFS_CACHE_TTL_SECONDS_DEFAULT );
    when( props.isCacheEnabled() ).thenReturn( true );
    S3FileSystem replacingFs = new S3FileSystem( new S3FileName( SCHEME, BUCKET_NAME, "", FileType.FOLDER ),
      new FileSystemOptions(), new StorageUnitConverter(), props ) {
      @Override
      public AmazonS3 getS3Client() {
        // Simulates another thread replacing the client (and clearing the caches) right as this call acquires it.
        try {
          Method clear =
            S3CommonFileSystem.class.getDeclaredMethod( "clearMetadataCaches" );
          clear.setAccessible( true );
          clear.invoke( this );
        } catch ( ReflectiveOperationException e ) {
          throw new IllegalStateException( e );
        }
        return s3ServiceMock;
      }
    };
    VfsComponentContext context = mock( VfsComponentContext.class );
    DefaultFileSystemManager fsm = new DefaultFileSystemManager();
    fsm.setFilesCache( mock( FilesCache.class ) );
    fsm.setCacheStrategy( CacheStrategy.ON_RESOLVE );
    when( context.getFileSystemManager() ).thenReturn( fsm );
    replacingFs.setContext( context );
    replacingFs.init();

    S3ObjectSummary summary = new S3ObjectSummary();
    summary.setBucketName( BUCKET_NAME );
    summary.setKey( "listed.txt" );
    summary.setSize( 5L );
    ObjectListing listing = mock( ObjectListing.class );
    when( listing.getObjectSummaries() ).thenReturn( Collections.singletonList( summary ) );
    when( listing.getCommonPrefixes() ).thenReturn( Collections.singletonList( "listedDir/" ) );
    when( listing.isTruncated() ).thenReturn( false );
    when( s3ServiceMock.listObjects( any( ListObjectsRequest.class ) ) ).thenReturn( listing );

    new S3FileObject( new S3FileName( SCHEME, BUCKET_NAME, BUCKET_NAME, FileType.FOLDER ), replacingFs )
      .getChildren();

    assertNull( "a listing from a replaced client must not publish metadata",
      replacingFs.getCachedFileMetadata( BUCKET_NAME, "listed.txt" ) );
    assertFalse( replacingFs.isCachedFolder( BUCKET_NAME, "listedDir/" ) );
  }

  @Test
  public void testGetChildrenDoesNotCacheFolderSelfMarkerAsFile() throws Exception {
    fileSystem.init();

    String testKey = BUCKET_NAME + "/" + origKey;
    String testBucket = "badBucketName";
    AmazonS3Exception exception = new AmazonS3Exception( "NoSuchKey" );

    // Attach the folder itself first (mirrors testHandleAttachException), so its key ends up as
    // "some/key/" -- the exact realKey used when it is later listed.
    when( s3ServiceMock.getObject( BUCKET_NAME, origKey + "/" ) ).thenThrow( exception );
    s3FileObjectFileSpy.handleAttachException( testKey, testBucket );
    assertEquals( FileType.FOLDER, s3FileObjectFileSpy.getType() );

    // Simulate a listing of that folder that includes its own self-referencing marker object
    // (key == realKey) alongside one real child file.
    String selfKey = origKey + "/";
    S3ObjectSummary selfSummary = new S3ObjectSummary();
    selfSummary.setBucketName( BUCKET_NAME );
    selfSummary.setKey( selfKey );
    S3ObjectSummary childSummary = new S3ObjectSummary();
    childSummary.setBucketName( BUCKET_NAME );
    childSummary.setKey( selfKey + "child.txt" );

    ObjectListing selfMarkerListing = mock( ObjectListing.class );
    when( selfMarkerListing.getObjectSummaries() ).thenReturn( Arrays.asList( selfSummary, childSummary ) );
    when( selfMarkerListing.getCommonPrefixes() ).thenReturn( Collections.emptyList() );
    when( selfMarkerListing.isTruncated() ).thenReturn( false );
    when( s3ServiceMock.listObjects( any( ListObjectsRequest.class ) ) ).thenReturn( selfMarkerListing );

    s3FileObjectFileSpy.getChildren();

    assertNull( "the folder's own self-marker entry must not be cached as a FILE",
      fileSystem.getCachedFileMetadata( BUCKET_NAME, selfKey ) );
    assertNotNull( "the real child file must still be cached",
      fileSystem.getCachedFileMetadata( BUCKET_NAME, selfKey + "child.txt" ) );
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
  public void testBucketExistsCheckIsCachedAcrossCalls() {
    // fixFilePath() (and therefore its underlying doesBucketExistV2() check) is invoked on virtually
    // every VFS operation (isRootBucket(), getS3Object(), doDelete(), etc.). Bucket existence effectively
    // never changes during a single listing/traversal operation, so the underlying AmazonS3 call must only
    // be made once per bucket (per S3CommonFileSystem#isBucketExists TTL window), not once per invocation -
    // otherwise every file attached during a folder listing would trigger its own live network round-trip.
    for ( int i = 0; i < 5; i++ ) {
      s3FileObjectBucketSpy.fixFilePath( origKey, BUCKET_NAME );
    }
    verify( s3ServiceMock, times( 1 ) ).doesBucketExistV2( BUCKET_NAME );
  }

  @Test
  public void testBucketExistsCheckIsCachedPerBucket() {
    String otherBucketName = "otherBucket";
    when( s3ServiceMock.doesBucketExistV2( otherBucketName ) ).thenReturn( true );

    s3FileObjectBucketSpy.fixFilePath( origKey, BUCKET_NAME );
    s3FileObjectBucketSpy.fixFilePath( origKey, otherBucketName );
    s3FileObjectBucketSpy.fixFilePath( origKey, BUCKET_NAME );
    s3FileObjectBucketSpy.fixFilePath( origKey, otherBucketName );

    verify( s3ServiceMock, times( 1 ) ).doesBucketExistV2( BUCKET_NAME );
    verify( s3ServiceMock, times( 1 ) ).doesBucketExistV2( otherBucketName );
  }

  @Test
  public void testBucketExistsReturnsFalseWhenClientThrowsSdkClientException() {
    // If the AWS client can't be reached at all (e.g. network failure) doesBucketExistV2() throws
    // SdkClientException rather than returning a value. This must be treated as "bucket doesn't exist"
    // (falling through to the legacy-path-compatibility handling in fixFilePath()) rather than blowing up
    // the whole VFS operation.
    String unreachableBucket = "unreachableBucket";
    when( s3ServiceMock.doesBucketExistV2( unreachableBucket ) ).thenThrow( new SdkClientException( "boom" ) );

    SimpleEntry<String, String> newPath = s3FileObjectBucketSpy.fixFilePath( "some/key", unreachableBucket );

    assertEquals( "some", newPath.getValue() );
    assertEquals( "key", newPath.getKey() );
  }

  @Test
  public void testBucketExistsSdkClientExceptionResultIsNotCached() {
    // A failed check (e.g. transient network/credentials error) must not be cached: the failing call falls
    // back to the legacy-path handling, but the next call must retry the live check.
    String unreachableBucket = "unreachableBucket";
    when( s3ServiceMock.doesBucketExistV2( unreachableBucket ) ).thenThrow( new SdkClientException( "boom" ) );

    s3FileObjectBucketSpy.fixFilePath( "some/key", unreachableBucket );
    s3FileObjectBucketSpy.fixFilePath( "some/key", unreachableBucket );

    verify( s3ServiceMock, times( 2 ) ).doesBucketExistV2( unreachableBucket );
  }

  @Test
  public void testBucketExistsRecoversAfterTransientSdkClientException() {
    String flakyBucket = "flakyBucket";
    when( s3ServiceMock.doesBucketExistV2( flakyBucket ) )
      .thenThrow( new SdkClientException( "transient" ) ).thenReturn( true );

    SimpleEntry<String, String> failed = s3FileObjectBucketSpy.fixFilePath( "some/key", flakyBucket );
    assertEquals( "some", failed.getValue() );

    SimpleEntry<String, String> recovered = s3FileObjectBucketSpy.fixFilePath( "key", flakyBucket );
    assertEquals( flakyBucket, recovered.getValue() );
    assertEquals( "key", recovered.getKey() );

    s3FileObjectBucketSpy.fixFilePath( "key", flakyBucket );
    verify( s3ServiceMock, times( 2 ) ).doesBucketExistV2( flakyBucket );
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
