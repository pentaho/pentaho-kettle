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



package org.pentaho.s3common;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.cache.SoftRefFilesCache;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.Test;
import org.pentaho.amazon.s3.S3Util;
import org.pentaho.di.core.util.StorageUnitConverter;
import org.pentaho.s3common.DummyS3CommonObjects.DummyS3FileObject;
import org.pentaho.s3common.DummyS3CommonObjects.DummyS3FileSystem;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.s3common.DummyS3CommonObjects.getDummyInstance;

public class S3CommonFileSystemTest {

  @Test
  public void getPartSize() {
    DummyS3FileSystem s3FileSystem = getDummyInstance();
    s3FileSystem.storageUnitConverter = new StorageUnitConverter();
    S3KettleProperty s3KettleProperty = mock( S3KettleProperty.class );
    when( s3KettleProperty.getPartSize() ).thenReturn( "6MB" );
    s3FileSystem.s3KettleProperty = s3KettleProperty;

    //TEST 1: Below max
    assertEquals( 6 * 1024 * 1024, s3FileSystem.getPartSize() );

    // TEst 2: above max
    when( s3KettleProperty.getPartSize() ).thenReturn( "600GB" );
    assertEquals( 600L * 1024 * 1024 * 1024, s3FileSystem.getPartSize() );
  }

  @Test
  public void testParsePartSize() {
    DummyS3FileSystem s3FileSystem = getDummyInstance();
    s3FileSystem.storageUnitConverter = new StorageUnitConverter();
    long _5MBLong = 5L * 1024L * 1024L;
    long _124MBLong = 124L * 1024L * 1024L;
    long _5GBLong = 5L * 1024L * 1024L * 1024L;
    long _12GBLong = 12L * 1024L * 1024L * 1024L;
    long minimumPartSize = _5MBLong;
    long maximumPartSize = _5GBLong;


    // TEST 1: below minimum
    assertEquals( minimumPartSize, s3FileSystem.parsePartSize( "1MB" ) );

    // TEST 2: at minimum
    assertEquals( minimumPartSize, s3FileSystem.parsePartSize( "5MB" ) );

    // TEST 3: between minimum and maximum
    assertEquals( _124MBLong, s3FileSystem.parsePartSize( "124MB" ) );

    // TEST 4: at maximum
    assertEquals( maximumPartSize, s3FileSystem.parsePartSize( "5GB" ) );

    // TEST 5: above maximum
    assertEquals( _12GBLong, s3FileSystem.parsePartSize( "12GB" ) );
  }

  @Test
  public void testCopy_DelegatesToTransferManager() throws Exception {
    DummyS3FileObject src = mock( DummyS3FileObject.class );
    DummyS3FileObject dest = mock( DummyS3FileObject.class );
    S3TransferManager transferManager = mock( S3TransferManager.class );
    DummyS3FileSystem fs = spy( getDummyInstance() );
    doReturn( transferManager ).when( fs ).getS3TransferManager();
    fs.copy( src, dest );
    verify( transferManager, times( 1 ) ).copy( src, dest );
  }

  @Test
  public void testGetS3TransferManager_CreatesWithTransferManager() {
    DummyS3FileSystem fs = spy( getDummyInstance() );
    S3TransferManager transferManager = mock( S3TransferManager.class );
    doReturn( transferManager ).when( fs ).getS3TransferManager();
    S3TransferManager result = fs.getS3TransferManager();
    assertNotNull( result );
  }

  @Test
  public void testBuildTransferManager_CreatesWithS3Client() {
    DummyS3FileSystem fs = spy( getDummyInstance() );
    AmazonS3 s3Client = mock( AmazonS3.class );
    doReturn( s3Client ).when( fs ).getS3Client();
    TransferManager tm = fs.buildTransferManager();
    assertNotNull( tm );
  }

  /**
   * When no explicit access/secret key or credentials-file profile is configured, {@link
   * S3CommonFileSystem#createCredentialsProvider(S3Options)} must return {@code null} rather than
   * a provider with empty/invalid credentials. Passing {@code null} to {@code
   * AwsClientBuilder.withCredentials(...)} makes the AWS SDK fall back to the {@code
   * DefaultAWSCredentialsProviderChain} at build time, i.e. auto-discovering credentials from IAM
   * roles, environment variables, the default {@code ~/.aws/credentials} profile, EC2/ECS/EKS
   * instance metadata, etc. This is what enables credential auto-discovery for S3 VFS.
   */
  @Test
  public void testCreateCredentialsProviderReturnsNullForAutoDiscoveryWhenNoExplicitCredentials() {
    S3CommonFileSystemConfigBuilder emptyConfig = new S3CommonFileSystemConfigBuilder( new FileSystemOptions() );
    S3Options options = S3Options.from( emptyConfig );
    assertNull( S3CommonFileSystem.createCredentialsProvider( options ) );
  }

  @Test
  public void testCreateCredentialsProviderUsesExplicitKeysWhenProvided() {
    S3CommonFileSystemConfigBuilder config = new S3CommonFileSystemConfigBuilder( new FileSystemOptions() );
    config.setAccessKey( "explicitAccessKey" );
    config.setSecretKey( "explicitSecretKey" );
    S3Options options = S3Options.from( config );

    AWSCredentialsProvider provider = S3CommonFileSystem.createCredentialsProvider( options );

    assertNotNull( provider );
    assertEquals( "explicitAccessKey", provider.getCredentials().getAWSAccessKeyId() );
    assertEquals( "explicitSecretKey", provider.getCredentials().getAWSSecretKey() );
  }

  // --- Listing metadata cache -------------------------------------------------------------

  @Test
  public void testGetCachedFileMetadataReturnsNullOnCacheMiss() {
    DummyS3FileSystem fs = getDummyInstance();
    assertNull( fs.getCachedFileMetadata( "bucket", "never-listed-key" ) );
  }

  @Test
  public void testIsCachedFolderReturnsFalseOnCacheMiss() {
    DummyS3FileSystem fs = getDummyInstance();
    assertFalse( fs.isCachedFolder( "bucket", "never-listed-folder/" ) );
  }

  @Test
  public void testCacheListedFileMakesMetadataAvailableWithSizeAndLastModified() {
    DummyS3FileSystem fs = getDummyInstance();
    Date lastModified = new Date( 123456789L );
    fs.cacheListedFile( "bucket", objectSummary( "bucket", "key0", 555L, lastModified ) );

    ObjectMetadata metadata = fs.getCachedFileMetadata( "bucket", "key0" );

    assertNotNull( metadata );
    assertEquals( 555L, metadata.getContentLength() );
    assertEquals( lastModified, metadata.getLastModified() );
  }

  @Test
  public void testCacheListedFileWithoutLastModifiedYieldsMetadataWithNullLastModified() {
    DummyS3FileSystem fs = getDummyInstance();
    fs.cacheListedFile( "bucket", objectSummary( "bucket", "key0", 10L, null ) );

    ObjectMetadata metadata = fs.getCachedFileMetadata( "bucket", "key0" );

    assertNotNull( metadata );
    assertEquals( 10L, metadata.getContentLength() );
    assertNull( metadata.getLastModified() );
  }

  @Test
  public void testCacheListedFolderMakesIsCachedFolderTrue() {
    DummyS3FileSystem fs = getDummyInstance();
    fs.cacheListedFolder( "bucket", "sub/folder/" );
    assertTrue( fs.isCachedFolder( "bucket", "sub/folder/" ) );
  }

  @Test
  public void testCacheListedFileWithTrailingDelimiterIsCachedAsFolder() {
    DummyS3FileSystem fs = getDummyInstance();
    fs.cacheListedFile( "bucket", objectSummary( "bucket", "sub/marker/", 0L, null ) );

    assertTrue( fs.isCachedFolder( "bucket", "sub/marker/" ) );
    assertNull( fs.getCachedFileMetadata( "bucket", "sub/marker/" ) );
  }

  @Test
  public void testConcurrentBucketExistsMissesTriggerOnlyOneLiveCheck() throws Exception {
    DummyS3FileSystem fs = getDummyInstance();
    AtomicInteger invocations = new AtomicInteger();
    CountDownLatch start = new CountDownLatch( 1 );
    Predicate<String> slowCheck = bucket -> {
      invocations.incrementAndGet();
      try {
        Thread.sleep( 200 );
      } catch ( InterruptedException e ) {
        Thread.currentThread().interrupt();
      }

      return true;
    };

    ExecutorService pool = Executors.newFixedThreadPool( 8 );

    try {
      List<Future<Boolean>> results = new ArrayList<>();

      for ( int i = 0; i < 8; i++ ) {
        results.add( pool.submit( () -> {
          start.await();
          return fs.isBucketExists( "bucket", slowCheck );
        } ) );
      }

      start.countDown();

      for ( Future<Boolean> r : results ) {
        assertTrue( r.get( 10, TimeUnit.SECONDS ) );
      }
    } finally {
      pool.shutdownNow();
    }

    assertEquals( "concurrent misses must share a single live check", 1, invocations.get() );
  }

  @Test
  public void testFailedBucketExistsCheckIsNotCachedAndDoesNotBlockRetry() {
    DummyS3FileSystem fs = getDummyInstance();
    AtomicInteger invocations = new AtomicInteger();

    try {
      fs.isBucketExists( "bucket", b -> {
        invocations.incrementAndGet();
        throw new IllegalStateException( "boom" );
      } );

      fail( "expected the check failure to propagate" );
    } catch ( IllegalStateException expected ) {
      // expected
    }

    assertTrue( fs.isBucketExists( "bucket", b -> {
      invocations.incrementAndGet();
      return true;
    } ) );
    assertEquals( 2, invocations.get() );
  }

  private static void invokeClearMetadataCaches( DummyS3FileSystem fs ) throws Exception {
    Method clear = S3CommonFileSystem.class.getDeclaredMethod( "clearMetadataCaches" );
    clear.setAccessible( true );
    clear.invoke( fs );
  }

  @Test
  public void testListingFillStartedBeforeInvalidationIsDropped() throws Exception {
    DummyS3FileSystem fs = getDummyInstance();
    long generation = fs.getCacheGeneration();

    invokeClearMetadataCaches( fs );
    fs.cacheListedFile( "bucket", objectSummary( "bucket", "old.txt", 1L, null ), generation );
    fs.cacheListedFolder( "bucket", "oldDir/", generation );

    assertEquals( 0, listingCacheSize( fs ) );
    assertNull( fs.getCachedFileMetadata( "bucket", "old.txt" ) );
  }

  @Test
  public void testListingFillStartedAfterInvalidationIsKept() throws Exception {
    DummyS3FileSystem fs = getDummyInstance();
    invokeClearMetadataCaches( fs );

    fs.cacheListedFile( "bucket", objectSummary( "bucket", "new.txt", 1L, null ), fs.getCacheGeneration() );

    assertNotNull( fs.getCachedFileMetadata( "bucket", "new.txt" ) );
  }

  @Test
  public void testBucketCheckInFlightDuringInvalidationIsNotCached() throws Exception {
    DummyS3FileSystem fs = getDummyInstance();
    AtomicInteger invocations = new AtomicInteger();

    // The invalidation happens while the (old client's) check is running.
    assertTrue( fs.isBucketExists( "bucket", b -> {
      invocations.incrementAndGet();
      try {
        invokeClearMetadataCaches( fs );
      } catch ( Exception e ) {
        throw new IllegalStateException( e );
      }
      return true;
    } ) );

    assertEquals( 0, bucketExistsCache( fs ).size() );
    assertTrue( fs.isBucketExists( "bucket", b -> {
      invocations.incrementAndGet();
      return true;
    } ) );
    assertEquals( "result computed before the invalidation must not be reused", 2, invocations.get() );
  }

  @Test
  public void testListingFillStartedBeforeLocalMutationIsDropped() {
    DummyS3FileSystem fs = getDummyInstance();
    long generation = fs.getCacheGeneration();

    fs.invalidateCachedListing( "bucket", "deleted.txt" );
    fs.cacheListedFile( "bucket", objectSummary( "bucket", "deleted.txt", 1L, null ), generation );

    assertNull( "a listing that began before the delete must not re-cache the deleted object",
      fs.getCachedFileMetadata( "bucket", "deleted.txt" ) );
  }

  @Test
  public void testFirstClientCreationDoesNotInvalidateCaches() {
    DummyS3FileSystem fs = spyWithFilesCache( getDummyInstance() );
    // No filesystem options: the client is created from the environment (the path that used to clear caches).
    doReturn( null ).when( fs ).getFileSystemOptions();
    long before = fs.getCacheGeneration();

    fs.getS3Client();

    assertEquals( "creating the very first client must not bump the cache generation", before,
      fs.getCacheGeneration() );
  }

  @Test
  public void testBucketLockMemoryDoesNotGrowWithDistinctBucketNames() throws Exception {
    DummyS3FileSystem fs = getDummyInstance();
    Field locksField = S3CommonFileSystem.class.getDeclaredField( "bucketLocks" );
    locksField.setAccessible( true );
    int stripesBefore = ( (Object[]) locksField.get( fs ) ).length;

    for ( int i = 0; i < 1000; i++ ) {
      assertFalse( fs.isBucketExists( "missing-bucket-" + i, b -> false ) );
    }

    assertEquals( stripesBefore, ( (Object[]) locksField.get( fs ) ).length );
  }

  @Test
  public void testDifferentBucketsSharingAndNotSharingLockStripesStillCacheIndependently() {
    DummyS3FileSystem fs = getDummyInstance();
    AtomicInteger invocations = new AtomicInteger();
    for ( int round = 0; round < 2; round++ ) {
      for ( int i = 0; i < 100; i++ ) {
        assertTrue( fs.isBucketExists( "bucket-" + i, b -> {
          invocations.incrementAndGet();
          return true;
        } ) );
      }
    }
    assertEquals( "each bucket checked live once, then served from cache", 100, invocations.get() );
  }

  @Test
  public void testCacheHitIsRejectedWhenFileSystemOptionsChanged() throws Exception {
    DummyS3FileSystem fs = spyWithFilesCache( getDummyInstance() );
    populateCaches( fs );
    FileSystemOptions previous = new FileSystemOptions();
    new S3CommonFileSystemConfigBuilder( previous ).setRegion( "previous-region" );
    setField( fs, "currentFileSystemOptions", previous );

    assertNull( "listing metadata from the previous connection must not be served",
      fs.getCachedFileMetadata( "bucket", "key" ) );

    AtomicInteger invocations = new AtomicInteger();
    assertTrue( fs.isBucketExists( "bucket", b -> {
      invocations.incrementAndGet();
      return true;
    } ) );
    assertEquals( "bucket result from the previous connection must not be served", 1, invocations.get() );
  }

  @Test
  public void testCacheHitIsRejectedWhenDefaultConnectionPropertiesChanged() throws Exception {
    DummyS3FileSystem fs = spyWithFilesCache( getDummyInstance() );
    populateCaches( fs );
    setField( fs, "currentConnectionProperties", new HashMap<>( Map.of( "region", "old" ) ) );

    assertFalse( fs.isCachedFolder( "bucket", "key/" ) );
    assertNull( fs.getCachedFileMetadata( "bucket", "key" ) );
    assertEquals( 0, bucketExistsCache( fs ).size() );
  }

  @Test
  public void testCacheHitIsRejectedWhenEnvCredentialsChanged() {
    String accessKeyProp = S3Util.ACCESS_KEY_SYSTEM_PROPERTY;
    String oldValue = System.getProperty( accessKeyProp );
    try {
      System.clearProperty( accessKeyProp );
      DummyS3FileSystem fs = getDummyInstance();
      populateCaches( fs );
      System.setProperty( accessKeyProp, "another-key" );

      assertNull( fs.getCachedFileMetadata( "bucket", "key" ) );
      AtomicInteger invocations = new AtomicInteger();
      fs.isBucketExists( "bucket", b -> {
        invocations.incrementAndGet();
        return true;
      } );
      assertEquals( 1, invocations.get() );

      // The change is absorbed once; afterwards hits are served again.
      fs.isBucketExists( "bucket", b -> {
        invocations.incrementAndGet();
        return true;
      } );
      assertEquals( 1, invocations.get() );
    } finally {
      if ( oldValue == null ) {
        System.clearProperty( accessKeyProp );
      } else {
        System.setProperty( accessKeyProp, oldValue );
      }
    }
  }

  @Test
  public void testCacheHitStillServedWhenConnectionUnchanged() {
    DummyS3FileSystem fs = spyWithFilesCache( getDummyInstance() );
    populateCaches( fs );

    assertNotNull( fs.getCachedFileMetadata( "bucket", "key" ) );
    assertTrue( fs.isBucketExists( "bucket", b -> {
      throw new IllegalStateException( "must be served from cache" );
    } ) );
  }

  @Test
  public void testExpiredEntryRemovalDoesNotDeleteConcurrentlyRefreshedEntry() throws Exception {
    DummyS3FileSystem fs = getDummyInstance();
    fs.cacheListedFile( "bucket", objectSummary( "bucket", "key", 1L, null ) );
    String cacheKey = "bucket" + '\u0000' + "key";
    Map<String, Object> cache = listingCache( fs );
    Object expiredEntry = cache.get( cacheKey );
    forceCacheEntryToExpire( fs, "bucket", "key" );

    // Wrap the cache so a fresh entry replaces the expired one right after the expired one is read.
    Map<String, Object> racing = new AbstractMap<>() {
      @Override
      public Set<Entry<String, Object>> entrySet() {
        return cache.entrySet();
      }

      @Override
      public Object get( Object k ) {
        Object value = cache.get( k );
        if ( value == expiredEntry ) {
          // concurrent refresh lands between the read and the removal
          fs.cacheListedFile( "bucket", objectSummary( "bucket", "key", 2L, null ) );
        }
        return value;
      }

      @Override
      public Object put( String k, Object v ) {
        return cache.put( k, v );
      }

      @Override
      public Object remove( Object k ) {
        return cache.remove( k );
      }

      @Override
      public boolean remove( Object k, Object v ) {
        return cache.remove( k, v );
      }
    };
    setField( fs, "listingMetadataCache", racing );

    assertNull( "the stale entry itself is a miss", fs.getCachedFileMetadata( "bucket", "key" ) );

    assertNotNull( "the freshly refreshed entry must survive the stale entry's removal",
      cache.get( cacheKey ) );
    assertEquals( 2L, fs.getCachedFileMetadata( "bucket", "key" ).getContentLength() );
  }

  @Test
  public void testBucketExistsCacheEvictsLeastRecentlyUsedWhenExceedingMaxSize() throws Exception {
    DummyS3FileSystem fs = getDummyInstance();
    Field maxField = S3CommonFileSystem.class.getDeclaredField( "BUCKET_CACHE_MAX_ENTRIES" );
    maxField.setAccessible( true );
    int max = maxField.getInt( null );

    for ( int i = 0; i <= max; i++ ) {
      fs.isBucketExists( "bucket-" + i, b -> true );
    }

    assertEquals( max, bucketExistsCache( fs ).size() );
    assertFalse( "oldest bucket must have been evicted", bucketExistsCache( fs ).containsKey( "bucket-0" ) );
    assertTrue( bucketExistsCache( fs ).containsKey( "bucket-" + max ) );
  }

  @Test
  public void testExpiredBucketEntriesAreSweptEvenWhenTheLiveCheckFails() throws Exception {
    DummyS3FileSystem fs = getDummyInstance();
    fs.isBucketExists( "stale-bucket", b -> true );
    forceBucketExistsCacheEntryToExpire( fs, "stale-bucket" );
    makeSweepDue( fs );

    try {
      fs.isBucketExists( "failing-bucket", b -> {
        throw new IllegalStateException( "boom" );
      } );
      fail( "expected the check failure to propagate" );
    } catch ( IllegalStateException expected ) {
      // expected
    }

    assertEquals( "the expired entry must be swept despite the failed check", 0, bucketExistsCache( fs ).size() );
  }

  private static Map<String, Object> mapRunningOnGet( Map<String, Object> delegate, Runnable afterRead ) {
    return new AbstractMap<String, Object>() {
      @Override
      public Set<Entry<String, Object>> entrySet() {
        return delegate.entrySet();
      }

      @Override
      public Object get( Object k ) {
        Object value = delegate.get( k );
        if ( value != null ) {
          afterRead.run();
        }
        return value;
      }

      @Override
      public Object put( String k, Object v ) {
        return delegate.put( k, v );
      }

      @Override
      public Object remove( Object k ) {
        return delegate.remove( k );
      }

      @Override
      public boolean remove( Object k, Object v ) {
        return delegate.remove( k, v );
      }
    };
  }

  @Test
  public void testInvalidationWaitsForInFlightListingHitDecision() throws Exception {
    DummyS3FileSystem fs = getDummyInstance();
    fs.cacheListedFile( "bucket", objectSummary( "bucket", "doomed.txt", 1L, null ) );
    Thread invalidator = new Thread( () -> fs.invalidateCachedListing( "bucket", "doomed.txt" ) );
    AtomicBoolean invalidatedDuringRead = new AtomicBoolean( true );
    setField( fs, "listingMetadataCache", mapRunningOnGet( listingCache( fs ), () -> {
      invalidator.start();
      joinQuietly( invalidator, 200 );
      invalidatedDuringRead.set( !invalidator.isAlive() );
    } ) );

    assertNotNull( fs.getCachedFileMetadata( "bucket", "doomed.txt" ) );
    assertFalse( "invalidation must not complete between the read and the validity decision",
      invalidatedDuringRead.get() );
    invalidator.join();
    assertNull( fs.getCachedFileMetadata( "bucket", "doomed.txt" ) );
  }

  @Test
  public void testClearWaitsForInFlightBucketHitDecision() throws Exception {
    DummyS3FileSystem fs = getDummyInstance();
    fs.isBucketExists( "bucket", b -> true );
    Thread clearer = new Thread( () -> {
      try {
        invokeClearMetadataCaches( fs );
      } catch ( Exception e ) {
        throw new IllegalStateException( e );
      }
    } );
    AtomicBoolean clearedDuringRead = new AtomicBoolean( true );
    setField( fs, "bucketExistsCache", mapRunningOnGet( bucketExistsCache( fs ), () -> {
      clearer.start();
      joinQuietly( clearer, 200 );
      clearedDuringRead.set( !clearer.isAlive() );
    } ) );

    assertTrue( fs.isBucketExists( "bucket", b -> false ) );
    assertFalse( "clearing must not complete between the read and the validity decision", clearedDuringRead.get() );
    clearer.join();
    assertEquals( 0, bucketExistsCache( fs ).size() );
  }

  private static void joinQuietly( Thread thread, long millis ) {
    try {
      thread.join( millis );
    } catch ( InterruptedException e ) {
      Thread.currentThread().interrupt();
    }
  }

  @Test
  public void testRebuiltEnvClientKeepsListingsCachedAfterCredentialRotation() {
    String accessKeyProp = S3Util.ACCESS_KEY_SYSTEM_PROPERTY;
    String oldValue = System.getProperty( accessKeyProp );
    try {
      System.clearProperty( accessKeyProp );
      DummyS3FileSystem fs = getDummyInstance();
      System.setProperty( accessKeyProp, "rotated-key" );
      fs.getS3Client();
      populateCaches( fs );

      assertNotNull( "metadata cached under the rebuilt client must not be cleared by a stale snapshot",
        fs.getCachedFileMetadata( "bucket", "key" ) );
    } finally {
      if ( oldValue == null ) {
        System.clearProperty( accessKeyProp );
      } else {
        System.setProperty( accessKeyProp, oldValue );
      }
    }
  }

  @Test
  public void testInvalidateCachedListingAlsoRemovesAncestorFolderEntries() {
    DummyS3FileSystem fs = getDummyInstance();
    fs.cacheListedFolder( "bucket", "dir/" );
    fs.cacheListedFolder( "bucket", "dir/sub/" );
    fs.cacheListedFile( "bucket", objectSummary( "bucket", "dir/sub/only-child.txt", 1L, null ) );
    fs.cacheListedFolder( "bucket", "other/" );
    fs.cacheListedFolder( "otherBucket", "dir/" );
    fs.cacheListedFile( "bucket", objectSummary( "bucket", "dir/sibling.txt", 1L, null ) );

    fs.invalidateCachedListing( "bucket", "dir/sub/only-child.txt" );

    assertNull( fs.getCachedFileMetadata( "bucket", "dir/sub/only-child.txt" ) );
    assertFalse( "virtual parent prefix must no longer be reported as present", fs.isCachedFolder( "bucket", "dir/sub/" ) );
    assertFalse( "virtual grandparent prefix must no longer be reported as present",
      fs.isCachedFolder( "bucket", "dir/" ) );
    assertTrue( "unrelated folder must stay cached", fs.isCachedFolder( "bucket", "other/" ) );
    assertTrue( "same prefix in another bucket must stay cached", fs.isCachedFolder( "otherBucket", "dir/" ) );
    assertNotNull( "sibling file must stay cached", fs.getCachedFileMetadata( "bucket", "dir/sibling.txt" ) );
  }

  @Test
  public void testInvalidateCachedListingForTopLevelKeyDoesNotTouchOtherEntries() {
    DummyS3FileSystem fs = getDummyInstance();
    fs.cacheListedFile( "bucket", objectSummary( "bucket", "top.txt", 1L, null ) );
    fs.cacheListedFolder( "bucket", "dir/" );

    fs.invalidateCachedListing( "bucket", "top.txt" );

    assertNull( fs.getCachedFileMetadata( "bucket", "top.txt" ) );
    assertTrue( fs.isCachedFolder( "bucket", "dir/" ) );
  }

  @Test
  public void testInvalidateCachedListingRemovesFileAndFolderEntries() {
    DummyS3FileSystem fs = getDummyInstance();
    fs.cacheListedFile( "bucket", objectSummary( "bucket", "a.txt", 1L, null ) );
    fs.cacheListedFolder( "bucket", "dir/" );
    fs.cacheListedFile( "bucket", objectSummary( "bucket", "keep.txt", 1L, null ) );

    fs.invalidateCachedListing( "bucket", "a.txt" );
    fs.invalidateCachedListing( "bucket", "dir/" );
    fs.invalidateCachedListing( "bucket", "dir" );

    assertNull( fs.getCachedFileMetadata( "bucket", "a.txt" ) );
    assertFalse( fs.isCachedFolder( "bucket", "dir/" ) );
    assertNotNull( fs.getCachedFileMetadata( "bucket", "keep.txt" ) );
  }

  @Test
  public void testGetCachedFileMetadataReturnsNullForFolderTypeCacheEntry() {
    DummyS3FileSystem fs = getDummyInstance();
    fs.cacheListedFolder( "bucket", "sub/folder/" );
    assertNull( fs.getCachedFileMetadata( "bucket", "sub/folder/" ) );
  }

  @Test
  public void testIsCachedFolderReturnsFalseForFileTypeCacheEntry() {
    DummyS3FileSystem fs = getDummyInstance();
    fs.cacheListedFile( "bucket", objectSummary( "bucket", "key0", 1L, null ) );
    assertFalse( fs.isCachedFolder( "bucket", "key0" ) );
  }

  @Test
  public void testCachedMetadataIsIsolatedPerBucket() {
    DummyS3FileSystem fs = getDummyInstance();
    fs.cacheListedFile( "bucketA", objectSummary( "bucketA", "shared-key", 111L, null ) );

    assertNotNull( fs.getCachedFileMetadata( "bucketA", "shared-key" ) );
    assertNull( fs.getCachedFileMetadata( "bucketB", "shared-key" ) );
  }

  @Test
  public void testExpiredCacheEntryIsTreatedAsMissAndEvicted() throws Exception {
    DummyS3FileSystem fs = getDummyInstance();
    fs.cacheListedFile( "bucket", objectSummary( "bucket", "expiring-key", 42L, null ) );
    assertNotNull( "sanity check: entry must be cached before it's forced to expire",
      fs.getCachedFileMetadata( "bucket", "expiring-key" ) );

    forceCacheEntryToExpire( fs, "bucket", "expiring-key" );

    assertNull( fs.getCachedFileMetadata( "bucket", "expiring-key" ) );
    assertEquals( "expired entry must have been evicted from the cache", 0, listingCacheSize( fs ) );
  }

  @Test
  public void testListingCacheEvictsOldestEntryWhenExceedingMaxSize() {
    DummyS3FileSystem fs = getDummyInstance();
    int maxEntries = 50_000;
    for ( int i = 0; i <= maxEntries; i++ ) {
      fs.cacheListedFile( "bucket", objectSummary( "bucket", "key" + i, i, null ) );
    }

    assertNull( "the oldest entry must have been evicted once the cache exceeded its max size",
      fs.getCachedFileMetadata( "bucket", "key0" ) );
    assertNotNull( "the most recently added entry must still be cached",
      fs.getCachedFileMetadata( "bucket", "key" + maxEntries ) );
  }

  // --- Bucket-exists cache -----------------------------------------------------------------

  @Test
  public void testIsBucketExistsInvokesCheckOnlyOnceForRepeatedCalls() {
    DummyS3FileSystem fs = getDummyInstance();
    AtomicInteger invocationCount = new AtomicInteger();
    Predicate<String> check = bucket -> {
      invocationCount.incrementAndGet();
      return true;
    };

    for ( int i = 0; i < 5; i++ ) {
      assertTrue( fs.isBucketExists( "bucket", check ) );
    }

    assertEquals( "bucket-exists check must only be invoked once while the cache entry is valid",
      1, invocationCount.get() );
  }

  @Test
  public void testIsBucketExistsCachesResultPerBucketIndependently() {
    DummyS3FileSystem fs = getDummyInstance();
    Predicate<String> existsCheck = "bucketA"::equals;

    assertTrue( fs.isBucketExists( "bucketA", existsCheck ) );
    assertFalse( fs.isBucketExists( "bucketB", existsCheck ) );
    // Repeating shouldn't change the cached outcome even though the underlying predicate would now
    // disagree for "bucketA" - proving the cached value, not the live predicate, is being returned.
    assertTrue( fs.isBucketExists( "bucketA", bucket -> false ) );
    assertFalse( fs.isBucketExists( "bucketB", bucket -> true ) );
  }

  @Test
  public void testIsBucketExistsRechecksAfterCacheEntryExpires() throws Exception {
    DummyS3FileSystem fs = getDummyInstance();
    AtomicInteger invocationCount = new AtomicInteger();
    Predicate<String> check = bucket -> {
      invocationCount.incrementAndGet();
      return true;
    };

    assertTrue( fs.isBucketExists( "bucket", check ) );
    forceBucketExistsCacheEntryToExpire( fs, "bucket" );
    assertTrue( fs.isBucketExists( "bucket", check ) );

    assertEquals( "an expired cache entry must trigger a fresh check", 2, invocationCount.get() );
  }

  @Test
  public void testIsBucketExistsCacheDisabledWhenCacheDisabled() {
    // s3.vfs.cache.enabled=false (S3KettleProperty#isCacheEnabled) must disable the bucket-exists
    // cache entirely, so every call re-checks live - required for deployments that can't tolerate any
    // staleness (e.g. buckets being created/deleted concurrently by other automation).
    DummyS3FileSystem fs = getDummyInstance( false );
    AtomicInteger invocationCount = new AtomicInteger();
    Predicate<String> check = bucket -> {
      invocationCount.incrementAndGet();
      return true;
    };

    for ( int i = 0; i < 5; i++ ) {
      assertTrue( fs.isBucketExists( "bucket", check ) );
    }

    assertEquals( "with the cache disabled, every call must re-check live", 5, invocationCount.get() );
  }

  @Test
  public void testListingCacheDisabledWhenCacheDisabled() {
    // Same enabled/disabled flag must also disable the listing-metadata cache used by
    // getCachedFileMetadata()/isCachedFolder(), not just the bucket-exists cache.
    DummyS3FileSystem fs = getDummyInstance( false );
    fs.cacheListedFile( "bucket", objectSummary( "bucket", "key0", 42L, null ) );

    assertNull( "with the cache disabled, a listed file must not be retrievable from the cache",
      fs.getCachedFileMetadata( "bucket", "key0" ) );
  }

  @Test
  public void testCacheEnabledByDefault() {
    // Sanity check: getDummyInstance() (no explicit enabled/disabled stub, real S3KettleProperty) must
    // behave with caching ON by default, matching S3VFS_CACHE_ENABLED_DEFAULT.
    DummyS3FileSystem fs = getDummyInstance();
    fs.cacheListedFile( "bucket", objectSummary( "bucket", "key0", 42L, null ) );

    assertNotNull( "caching must be enabled by default", fs.getCachedFileMetadata( "bucket", "key0" ) );
  }

  // --- Expired-entry sweep and cache clearing ------------------------------------------------

  @Test
  public void testExpiredEntriesNeverReadAgainAreSweptOnNextWriteWhenDue() throws Exception {
    DummyS3FileSystem fs = getDummyInstance();
    fs.cacheListedFile( "bucket", objectSummary( "bucket", "stale-key", 1L, null ) );
    fs.isBucketExists( "stale-bucket", b -> true );
    forceCacheEntryToExpire( fs, "bucket", "stale-key" );
    forceBucketExistsCacheEntryToExpire( fs, "stale-bucket" );
    makeSweepDue( fs );

    fs.cacheListedFile( "bucket", objectSummary( "bucket", "fresh-key", 2L, null ) );

    assertEquals( "only the fresh listing entry must remain", 1, listingCacheSize( fs ) );
    assertNotNull( fs.getCachedFileMetadata( "bucket", "fresh-key" ) );
    assertEquals( "expired bucket-exists entry must be swept", 0, bucketExistsCache( fs ).size() );
  }

  @Test
  public void testSweepIsSkippedWhenNotDue() throws Exception {
    DummyS3FileSystem fs = getDummyInstance();
    fs.cacheListedFile( "bucket", objectSummary( "bucket", "stale-key", 1L, null ) );
    forceCacheEntryToExpire( fs, "bucket", "stale-key" );

    fs.cacheListedFile( "bucket", objectSummary( "bucket", "fresh-key", 2L, null ) );

    assertEquals( "sweep must run at most once per TTL period", 2, listingCacheSize( fs ) );
  }

  @Test
  public void testSweepKeepsUnexpiredEntries() throws Exception {
    DummyS3FileSystem fs = getDummyInstance();
    fs.cacheListedFolder( "bucket", "folder/" );
    fs.isBucketExists( "bucket", b -> true );
    makeSweepDue( fs );

    fs.cacheListedFile( "bucket", objectSummary( "bucket", "key", 2L, null ) );

    assertTrue( fs.isCachedFolder( "bucket", "folder/" ) );
    assertEquals( 2, listingCacheSize( fs ) );
    assertEquals( 1, bucketExistsCache( fs ).size() );
  }

  @Test
  public void testClearMetadataCachesEmptiesBothCaches() throws Exception {
    DummyS3FileSystem fs = getDummyInstance();
    fs.cacheListedFile( "bucket", objectSummary( "bucket", "key", 1L, null ) );
    fs.isBucketExists( "bucket", b -> true );

    Method clear = S3CommonFileSystem.class.getDeclaredMethod( "clearMetadataCaches" );
    clear.setAccessible( true );
    clear.invoke( fs );

    assertEquals( 0, listingCacheSize( fs ) );
    assertEquals( 0, bucketExistsCache( fs ).size() );
    assertNull( fs.getCachedFileMetadata( "bucket", "key" ) );
  }

  @Test
  public void testGetS3ClientClearsMetadataCachesWhenFileSystemOptionsChange() throws Exception {
    DummyS3FileSystem fs = spyWithFilesCache( getDummyInstance() );
    populateCaches( fs );
    setField( fs, "client", mock( AmazonS3.class ) );
    FileSystemOptions previous = new FileSystemOptions();
    new S3CommonFileSystemConfigBuilder( previous ).setRegion( "previous-region" );
    setField( fs, "currentFileSystemOptions", previous );

    fs.getS3Client();

    assertCachesEmpty( fs );
  }

  @Test
  public void testGetS3ClientClearsMetadataCachesWhenEnvCredentialsChange() throws Exception {
    String accessKeyProp = S3Util.ACCESS_KEY_SYSTEM_PROPERTY;
    String oldValue = System.getProperty( accessKeyProp );

    try {
      DummyS3FileSystem fs = spyWithFilesCache( getDummyInstance() );
      setField( fs, "client", mock( AmazonS3.class ) );
      setField( fs, "awsAccessKeyCache", "old-key" );
      System.setProperty( accessKeyProp, "new-key" );
      populateCaches( fs );

      fs.getS3Client();

      assertCachesEmpty( fs );
    } finally {
      if ( oldValue == null ) {
        System.clearProperty( accessKeyProp );
      } else {
        System.setProperty( accessKeyProp, oldValue );
      }
    }
  }

  @Test
  public void testGetS3ClientKeepsMetadataCachesWhenNothingChanged() throws Exception {
    DummyS3FileSystem fs = spyWithFilesCache( getDummyInstance() );
    setField( fs, "client", mock( AmazonS3.class ) );
    populateCaches( fs );

    fs.getS3Client();

    assertEquals( 1, listingCacheSize( fs ) );
    assertEquals( 1, bucketExistsCache( fs ).size() );
  }

  private static DummyS3FileSystem spyWithFilesCache( DummyS3FileSystem fs ) {
    DummyS3FileSystem spy = spy( fs );
    DefaultFileSystemManager manager = mock( DefaultFileSystemManager.class );
    SoftRefFilesCache cache = mock( SoftRefFilesCache.class );
    when( manager.getFilesCache() ).thenReturn( cache );
    doReturn( manager ).when( spy ).getFileSystemManager();
    return spy;
  }

  private static void populateCaches( DummyS3FileSystem fs ) {
    fs.cacheListedFile( "bucket", objectSummary( "bucket", "key", 1L, null ) );
    fs.isBucketExists( "bucket", b -> true );
  }

  private static void assertCachesEmpty( DummyS3FileSystem fs ) throws Exception {
    assertEquals( 0, listingCacheSize( fs ) );
    assertEquals( 0, bucketExistsCache( fs ).size() );
  }

  private static void setField( Object target, String name, Object value ) throws Exception {
    Field f = S3CommonFileSystem.class.getDeclaredField( name );
    f.setAccessible( true );
    f.set( target, value );
  }

  // --- EC2 metadata switch -------------------------------------------------------------------

  private static void withEc2MetadataProperty( String initial, Runnable body ) {
    String key = SDKGlobalConfiguration.AWS_EC2_METADATA_DISABLED_SYSTEM_PROPERTY;
    String old = System.getProperty( key );
    try {
      if ( initial == null ) {
        System.clearProperty( key );
      } else {
        System.setProperty( key, initial );
      }
      body.run();
    } finally {
      if ( old == null ) {
        System.clearProperty( key );
      } else {
        System.setProperty( key, old );
      }
    }
  }

  private static void createFileSystemWithDisableEc2Metadata( boolean disable ) {
    S3KettleProperty props = mock( S3KettleProperty.class );
    when( props.isCacheEnabled() ).thenReturn( true );
    when( props.getCacheTtlSeconds() ).thenReturn( 60 );
    when( props.isDisableEc2Metadata() ).thenReturn( disable );
    new DummyS3FileSystem( new DummyS3CommonObjects.DummyS3FileName( "s3", "bucket", "/bucket/key",
      FileType.FOLDER ), new FileSystemOptions(), new StorageUnitConverter(), props );
  }

  @Test
  public void testDisableEc2MetadataSetsSdkPropertyWhenEnabled() {
    withEc2MetadataProperty( null, () -> {
      createFileSystemWithDisableEc2Metadata( true );
      assertEquals( "true",
        System.getProperty( SDKGlobalConfiguration.AWS_EC2_METADATA_DISABLED_SYSTEM_PROPERTY ) );
    } );
  }

  @Test
  public void testDisableEc2MetadataLeavesSdkPropertyUnsetByDefault() {
    withEc2MetadataProperty( null, () -> {
      createFileSystemWithDisableEc2Metadata( false );
      assertNull( System.getProperty( SDKGlobalConfiguration.AWS_EC2_METADATA_DISABLED_SYSTEM_PROPERTY ) );
    } );
  }

  @Test
  public void testDisableEc2MetadataDoesNotOverrideExplicitJvmSetting() {
    withEc2MetadataProperty( "false", () -> {
      createFileSystemWithDisableEc2Metadata( true );
      assertEquals( "false",
        System.getProperty( SDKGlobalConfiguration.AWS_EC2_METADATA_DISABLED_SYSTEM_PROPERTY ) );
    } );
  }

  private static void makeSweepDue( DummyS3FileSystem fs ) throws Exception {
    Field f = S3CommonFileSystem.class.getDeclaredField( "lastEvictionNanos" );
    f.setAccessible( true );
    ( (AtomicLong) f.get( fs ) ).set( System.nanoTime() - TimeUnit.HOURS.toNanos( 1 ) );
  }

  @SuppressWarnings( "unchecked" )
  private static Map<String, Object> bucketExistsCache( DummyS3FileSystem fs ) throws Exception {
    Field f = S3CommonFileSystem.class.getDeclaredField( "bucketExistsCache" );
    f.setAccessible( true );
    return (Map<String, Object>) f.get( fs );
  }

  private static void forceBucketExistsCacheEntryToExpire( DummyS3FileSystem fs, String bucket ) throws Exception {
    Field cacheField = S3CommonFileSystem.class.getDeclaredField( "bucketExistsCache" );
    cacheField.setAccessible( true );
    @SuppressWarnings( "unchecked" )
    Map<String, Object> cache = (Map<String, Object>) cacheField.get( fs );
    Object entry = cache.get( bucket );
    assertNotNull( "expected a bucket-exists cache entry to already exist for " + bucket, entry );

    Field expiresAtNanosField = entry.getClass().getDeclaredField( "expiresAtNanos" );
    expiresAtNanosField.setAccessible( true );
    expiresAtNanosField.setLong( entry, System.nanoTime() - 1 );
  }

  private static S3ObjectSummary objectSummary( String bucket, String key, long size, Date lastModified ) {
    S3ObjectSummary summary = new S3ObjectSummary();
    summary.setBucketName( bucket );
    summary.setKey( key );
    summary.setSize( size );
    summary.setLastModified( lastModified );
    return summary;
  }

  @SuppressWarnings( "unchecked" )
  private static Map<String, Object> listingCache( DummyS3FileSystem fs ) throws Exception {
    Field cacheField = S3CommonFileSystem.class.getDeclaredField( "listingMetadataCache" );
    cacheField.setAccessible( true );
    return (Map<String, Object>) cacheField.get( fs );
  }

  private static int listingCacheSize( DummyS3FileSystem fs ) throws Exception {
    return listingCache( fs ).size();
  }

  private static void forceCacheEntryToExpire( DummyS3FileSystem fs, String bucket, String key ) throws Exception {
    Map<String, Object> cache = listingCache( fs );
    String cacheKey = bucket + '\u0000' + key;
    Object entry = cache.get( cacheKey );
    assertNotNull( "expected a cache entry to already exist for " + cacheKey, entry );

    Field expiresAtNanosField = entry.getClass().getDeclaredField( "expiresAtNanos" );
    expiresAtNanosField.setAccessible( true );
    expiresAtNanosField.setLong( entry, System.nanoTime() - 1 );
  }
}
