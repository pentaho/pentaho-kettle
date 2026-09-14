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

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.util.StorageUnitConverter;
import org.pentaho.s3common.DummyS3CommonObjects.DummyS3FileObject;
import org.pentaho.s3common.DummyS3CommonObjects.DummyS3FileSystem;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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
    DummyS3FileSystem fs = Mockito.spy( getDummyInstance() );
    doReturn( transferManager ).when( fs ).getS3TransferManager();
    fs.copy( src, dest );
    verify( transferManager, times( 1 ) ).copy( src, dest );
  }

  @Test
  public void testGetS3TransferManager_CreatesWithTransferManager() {
    DummyS3FileSystem fs = Mockito.spy( getDummyInstance() );
    S3TransferManager transferManager = mock( S3TransferManager.class );
    doReturn( transferManager ).when( fs ).getS3TransferManager();
    S3TransferManager result = fs.getS3TransferManager();
    assertNotNull( result );
  }

  @Test
  public void testBuildTransferManager_CreatesWithS3Client() {
    DummyS3FileSystem fs = Mockito.spy( getDummyInstance() );
    com.amazonaws.services.s3.AmazonS3 s3Client = mock( com.amazonaws.services.s3.AmazonS3.class );
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

    com.amazonaws.auth.AWSCredentialsProvider provider = S3CommonFileSystem.createCredentialsProvider( options );

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
