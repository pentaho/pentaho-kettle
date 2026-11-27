package org.pentaho.s3.vfs;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.value.ValueMetaBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Accepts URIs written for the old driver, where sometimes the real bucket is the first element of the key,
 * e.g. {@code s3://s3/real-bucket/folder/file.txt}.
 * <p>
 * Only built for {@code s3} file systems when {@link Const#KETTLE_COMPATIBILITY_ALLOW_S3_LEGACY_URI}
 * is {@code Y}; callers must check {@link S3FileSystem#isLegacyS3UrisEnabled()} before using it.
 * <p>
 * Locations are resolved before each operation: when the original bucket does not exist and the
 * first key element names one that does, the operation runs against that one instead. Bucket
 * existence is cached briefly, so healthy buckets cost one check per TTL.
 */
final class S3LegacyUriSupport {

  private static final Logger logger = LoggerFactory.getLogger( S3LegacyUriSupport.class );

  private static final String DELIMITER = "/";

  // Bounded (LRU, evicting the least recently used bucket) so a workload touching many bucket names can't grow it
  // without limit within a TTL window.
  private static final int BUCKET_CACHE_MAX_ENTRIES = 1_000;

  // Striped locks guarding the per-bucket check-and-publish. A fixed number of monitors keeps memory independent of
  // how many distinct bucket names are seen; buckets that share a stripe only serialize their checks.
  private static final int BUCKET_LOCK_STRIPES = 32;

  private final S3FileSystem fileSystem;

  private final boolean cacheEnabled;
  private final long cacheTtlNanos;
  private final AtomicLong lastEvictionNanos = new AtomicLong( System.nanoTime() );

  private final Map<String, CachedBucketExists> bucketExistsCache =
    Collections.synchronizedMap( new LinkedHashMap<>( 16, 0.75f, true ) {
      @Override
      protected boolean removeEldestEntry( Map.Entry<String, CachedBucketExists> eldest ) {
        return size() > BUCKET_CACHE_MAX_ENTRIES;
      }
    } );

  // Bumped (under cacheLock) every time the cache is invalidated because the client changed. Cache fills remember
  // the generation they started under and are dropped if it changed, so a check that began against an old client
  // can't publish its result after the invalidation.
  private final Object cacheLock = new Object();
  private volatile long cacheGeneration;

  private final Object[] bucketLocks = createBucketLocks();

  record Location( String bucket, String key ) {
  }

  @FunctionalInterface
  interface Operation<T, E extends Exception> {
    T apply( String bucket, String key ) throws E;
  }

  @FunctionalInterface
  interface CopyOperation<T, E extends Exception> {
    T apply( String sourceBucket, String sourceKey, String destinationBucket, String destinationKey ) throws E;
  }

  static boolean isEnabled( FileName rootName ) {
    return "s3".equals( rootName.getScheme() )
      &&  ValueMetaBase.convertStringToBoolean( System.getProperty( Const.KETTLE_COMPATIBILITY_ALLOW_S3_LEGACY_URI, "N" ) );
  }

  S3LegacyUriSupport( S3FileSystem fileSystem ) {
    this.fileSystem = fileSystem;
    this.cacheEnabled = fileSystem.s3KettleProperty.isCacheEnabled();
    this.cacheTtlNanos = TimeUnit.SECONDS.toNanos( fileSystem.s3KettleProperty.getCacheTtlSeconds() );
  }

  <T, E extends Exception> T execute( String bucket, String key, Operation<T, E> operation )
    throws E, FileSystemException {
    Location location = resolve( new Location( bucket, key ) );
    return operation.apply( location.bucket(), location.key() );
  }

  <T, E extends Exception> T executeCopy( String sourceBucket, String sourceKey, String destinationBucket,
                                          String destinationKey, CopyOperation<T, E> operation )
    throws E, FileSystemException {
    Location source = resolve( new Location( sourceBucket, sourceKey ) );
    Location destination = resolve( new Location( destinationBucket, destinationKey ) );
    return operation.apply( source.bucket(), source.key(), destination.bucket(), destination.key() );
  }

  /** The location an upload must go to, decided before any content is consumed. */
  Location forWrite( String bucket, String key ) throws FileSystemException {
    return resolve( new Location( bucket, key ) );
  }

  /** Drop bucket existence cached under a previous client. */
  void clearCache() {
    synchronized ( cacheLock ) {
      cacheGeneration++;
      bucketExistsCache.clear();
    }
  }

  private Location resolve( Location original ) throws FileSystemException {
    if ( original.bucket().isEmpty() || bucketExists( original.bucket() ) ) {
      return original;
    }
    Optional<Location> candidate = candidate( original.key() );
    if ( candidate.isPresent() && bucketExists( candidate.get().bucket() ) ) {
      logger.error( "Bucket '{}' not found, using legacy S3 URI interpretation: bucket '{}', key '{}'",
        original.bucket(), candidate.get().bucket(), candidate.get().key() );
      return candidate.get();
    }
    return original;
  }

  /** The first key element as the bucket, the rest as the key. */
  private static Optional<Location> candidate( String key ) {
    int slash = key.indexOf( DELIMITER );
    String candidateBucket = slash < 0 ? key : key.substring( 0, slash );
    String candidateKey = slash < 0 ? "" : key.substring( slash + 1 );
    if ( candidateBucket.isEmpty() ) {
      return Optional.empty();
    }
    return Optional.of( new Location( candidateBucket, candidateKey ) );
  }

  private boolean bucketExists( String bucket ) throws FileSystemException {
    if ( !cacheEnabled ) {
      return checkBucketExists( bucket );
    }

    Optional<CachedBucketExists> cached = getValidBucketEntry( bucket );
    if ( cached.isPresent() ) {
      return cached.get().exists;
    }

    // Refresh atomically per bucket: concurrent callers wait for a single live check instead of each issuing one,
    // and an older result can never overwrite a newer one. If the check throws, nothing is cached.
    synchronized ( bucketLock( bucket ) ) {
      cached = getValidBucketEntry( bucket );
      if ( cached.isPresent() ) {
        return cached.get().exists;
      }

      long generation = cacheGeneration;
      try {
        boolean exists = checkBucketExists( bucket );
        synchronized ( cacheLock ) {
          if ( generation == cacheGeneration ) {
            bucketExistsCache.put( bucket, new CachedBucketExists( exists, cacheTtlNanos ) );
          }
        }
        return exists;
      } finally {
        // Also sweep after failed checks, so expired entries don't linger while checks keep failing.
        evictExpiredIfDue();
      }
    }
  }

  /**
   * Live check. HEAD responses carry no error code, so the status decides: like the old driver, a bucket
   * that answers from another region or denies access exists. Any other failure propagates.
   */
  private boolean checkBucketExists( String bucket ) throws FileSystemException {
    try {
      fileSystem.getS3Client().headBucket( HeadBucketRequest.builder().bucket( bucket ).build() );
      return true;
    } catch ( NoSuchBucketException e ) {
      return false;
    } catch ( S3Exception e ) {
      return switch ( e.statusCode() ) {
        case 301, 403 -> true;
        // 400 is what an invalid bucket name gets
        case 400, 404 -> false;
        default -> throw e;
      };
    }
  }

  /**
   * Reads and validates under {@code cacheLock}, the same lock invalidation holds, so an entry is never returned
   * once it has been invalidated.
   */
  private Optional<CachedBucketExists> getValidBucketEntry( String bucket ) {
    synchronized ( cacheLock ) {
      return Optional.ofNullable( bucketExistsCache.get( bucket ) ).filter( cached -> !cached.isExpired() );
    }
  }

  /**
   * Expired entries are otherwise only dropped when their own bucket is read again, so sweep on write, at most once
   * per TTL period, to release entries that are never looked up again.
   */
  private void evictExpiredIfDue() {
    long now = System.nanoTime();
    long last = lastEvictionNanos.get();

    if ( now - last < cacheTtlNanos || !lastEvictionNanos.compareAndSet( last, now ) ) {
      return;
    }

    synchronized ( bucketExistsCache ) {
      bucketExistsCache.values().removeIf( CachedBucketExists::isExpired );
    }
  }

  private static Object[] createBucketLocks() {
    Object[] locks = new Object[ BUCKET_LOCK_STRIPES ];
    for ( int i = 0; i < locks.length; i++ ) {
      locks[ i ] = new Object();
    }
    return locks;
  }

  private Object bucketLock( String bucket ) {
    return bucketLocks[ Math.floorMod( bucket.hashCode(), BUCKET_LOCK_STRIPES ) ];
  }

  private static final class CachedBucketExists {
    private final boolean exists;
    private final long expiresAtNanos;

    private CachedBucketExists( boolean exists, long ttlNanos ) {
      this.exists = exists;
      this.expiresAtNanos = System.nanoTime() + ttlNanos;
    }

    private boolean isExpired() {
      return System.nanoTime() > expiresAtNanos;
    }
  }
}
