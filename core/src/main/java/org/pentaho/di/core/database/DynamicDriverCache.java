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

package org.pentaho.di.core.database;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;

import java.io.File;
import java.net.URL;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * JVM-wide cache for dynamically loaded JDBC {@link Driver} instances.
 *
 * <h3>Problem solved</h3>
 * {@link Database} creates a fresh {@link ChildFirstURLClassLoader} on every {@code connect()}
 * call. For JARs on a network mount (NFS, SMB) this means repeated stat() + open() RPCs and
 * full JAR reads — a 35 MB Snowflake fat JAR on NFS WAN can cost 2–8 seconds per
 * {@code connect()}.
 *
 * <h3>How the cache works</h3>
 * On the first {@code connect()} for a given (jarPath, driverClass) pair,
 * {@code Database.loadDynamicDriver} delegates to {@link #getOrLoadDriver}, which:
 * <ol>
 *   <li>Creates a dedicated {@link ChildFirstURLClassLoader}, reads the JAR once,
 *       loads the driver class, and instantiates the {@link Driver}.</li>
 *   <li>Stores the result as a {@link CacheEntry} keyed by
 *       {@code (absoluteJarPath, driverClassName)}.</li>
 *   <li>Returns the live {@link Driver} instance to the caller.</li>
 * </ol>
 * Every subsequent {@code connect()} call to the same database type returns the same
 * {@link Driver} instance from RAM — no classloader creation, no JAR file read,
 * no NFS round-trips.
 *
 * <h3>Isolation guarantee is NOT broken</h3>
 * Sharing a single {@link Driver} instance across connections is safe and is exactly
 * how JDBC connection pools work. {@link Driver#connect} is specified to be thread-safe.
 * If two connections use <em>different</em> JARs or different driver classes, they receive
 * separate cache entries and therefore separate classloaders.
 *
 * <h3>Eviction — size cap (FIFO)</h3>
 * When the cache exceeds {@link #MAX_CACHE_SIZE} entries, the oldest-inserted entry is
 * removed from the map. Its classloader is <strong>not</strong> explicitly closed here;
 * instead it is left for the GC to reclaim. This is intentional: in Java every loaded
 * class holds a strong reference back to its defining classloader, so as long as any
 * {@link Database} instance still holds a reference to the evicted {@link Driver}, the
 * classloader (and the classes it loaded) remains reachable and valid. No
 * {@link ClassNotFoundException} can occur mid-query due to a size eviction.
 *
 * <h3>Eviction — explicit (JAR replacement)</h3>
 * Call {@link #evictByJar(String)} when a JAR is replaced on disk (e.g. re-download).
 * The classloader <em>is</em> explicitly closed here to release the Windows file handle
 * so the new file can be written. Active connections already have a live {@link Driver}
 * reference and their already-loaded classes are unaffected.
 *
 * <h3>Thread safety</h3>
 * A {@link ReentrantReadWriteLock} is used so that the hot read path ({@link #getOrLoadDriver}
 * cache-hit) never blocks concurrent callers. The expensive JAR load (NFS/SMB read) is
 * performed <em>outside</em> any lock, followed by a write-lock double-check on insert.
 * The backing store is an <em>insertion-order</em> {@link LinkedHashMap} (not access-order)
 * so that {@code containsKey()} / {@code get()} are purely read-only and safe under the
 * shared read lock.
 */
// Singleton is intentional: this cache must be JVM-wide so all Database instances
// share the same OS page-cache warm-up state. One instance per JVM is the correct
// lifetime — a per-instance or per-request cache would defeat the purpose entirely.
@SuppressWarnings( "squid:S6548" )
public final class DynamicDriverCache {

  private static final LogChannelInterface log = LogChannel.GENERAL;

  /**
   * Maximum number of (jarPath, driverClass) entries kept in the cache.
   * Long-running servers could otherwise accumulate an entry per distinct driver JAR
   * seen over the server lifetime. 50 is far more than any real deployment needs.
   */
  static final int MAX_CACHE_SIZE = 50;

  /**
   * Singleton — one cache for the entire JVM lifetime.
   */
  private static final DynamicDriverCache INSTANCE = new DynamicDriverCache();

  /**
   * Cache key: absolute JAR path + driver class name.
   */
  static final class CacheKey {
    final String jarPath;
    final String driverClass;

    CacheKey( String jarPath, String driverClass ) {
      this.jarPath = jarPath;
      this.driverClass = driverClass;
    }

    @Override
    public boolean equals( Object o ) {
      if ( this == o ) {
        return true;
      }
      if ( !( o instanceof CacheKey ) ) {
        return false;
      }
      CacheKey other = (CacheKey) o;
      return jarPath.equals( other.jarPath ) && driverClass.equals( other.driverClass );
    }

    @Override
    public int hashCode() {
      return 31 * jarPath.hashCode() + driverClass.hashCode();
    }
  }

  /**
   * Holds one cached entry for a (jarPath, driverClass) pair.
   *
   * <p>{@code cachedLoader} is kept alive deliberately: every Java class holds a strong
   * reference back to its defining classloader, so the loader would survive GC anyway as
   * long as {@code driver} is reachable. Storing it explicitly lets {@link #evictByJar}
   * close the JAR file handle immediately on a forced eviction (required on Windows to
   * allow the JAR file to be replaced on disk).
   *
   * <p>{@code driver} is the live, thread-safe {@link Driver} instance returned to all
   * callers of {@link DynamicDriverCache#getOrLoadDriver}. {@link Driver#connect} is
   * specified to be thread-safe, so sharing across {@link Database} instances is correct.
   */
  static final class CacheEntry {
    final ChildFirstURLClassLoader cachedLoader;
    final Driver driver;

    CacheEntry( ChildFirstURLClassLoader cachedLoader, Driver driver ) {
      this.cachedLoader = cachedLoader;
      this.driver = driver;
    }
  }

  private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

  /**
   * Insertion-order (NOT access-order) map.
   * With accessOrder=false, containsKey()/get() are purely read-only — they do not
   * restructure the linked list — making them safe to call under the shared read lock.
   * Eviction removes the oldest-inserted (FIFO) entry when MAX_CACHE_SIZE is exceeded.
   */
  private final LinkedHashMap<CacheKey, CacheEntry> cache = new LinkedHashMap<>( 16, 0.75f, false );

  private DynamicDriverCache() {
  }

  /**
   * Returns the singleton instance.
   */
  public static DynamicDriverCache getInstance() {
    return INSTANCE;
  }

  /**
   * Returns a cached or newly loaded {@link Driver} for the given JAR and driver class.
   *
   * <p><b>Cache hit (warm path):</b> acquires the shared read lock, finds the entry, returns
   * the {@link Driver} immediately. No classloader creation, no JAR file read, no NFS I/O.
   *
   * <p><b>Cache miss (cold path):</b>
   * <ol>
   *   <li>Releases the read lock.</li>
   *   <li>Creates a {@link ChildFirstURLClassLoader}, loads the JAR (one NFS read), loads the
   *       driver class, instantiates the {@link Driver} — all <em>outside</em> any lock so
   *       concurrent read-lock callers are never blocked by slow storage.</li>
   *   <li>Acquires the write lock. If another thread already inserted the same key while
   *       we were loading, discards our copy and returns the winner's entry.</li>
   *   <li>Inserts the new entry. If {@link #MAX_CACHE_SIZE} is exceeded, the oldest-inserted
   *       (FIFO) entry is removed from the map. Its classloader is <em>not</em> explicitly
   *       closed — Java's class→classloader strong reference keeps it alive as long as any
   *       {@link Database} instance still holds the evicted {@link Driver}, preventing
   *       mid-query {@link ClassNotFoundException}. GC reclaims it when no references remain.</li>
   * </ol>
   *
   * @param jarAbsolutePath absolute path of the driver JAR on disk
   * @param driverClassName fully-qualified JDBC driver class name
   * @return a live, thread-safe {@link Driver} instance
   * @throws KettleDatabaseException if the JAR cannot be read or the class cannot be loaded
   */
  public Driver getOrLoadDriver( String jarAbsolutePath, String driverClassName )
    throws KettleDatabaseException {

    CacheKey key = new CacheKey( jarAbsolutePath, driverClassName );

    // 1. Fast read-lock check — concurrent callers never block each other here.
    rwLock.readLock().lock();
    try {
      CacheEntry hit = cache.get( key );
      if ( hit != null ) {
        log.logDebug( "DynamicDriverCache: cache hit for " + driverClassName + " @ " + jarAbsolutePath );
        return hit.driver;
      }
    } finally {
      rwLock.readLock().unlock();
    }

    // 2. Expensive work outside any lock — NFS/SMB reads happen here.
    log.logBasic( "DynamicDriverCache: loading " + driverClassName + " from " + jarAbsolutePath );
    URL jarUrl = getJarUrl( jarAbsolutePath );
    ChildFirstURLClassLoader loader = null;
    CacheEntry entry;
    try {
      loader = new ChildFirstURLClassLoader( new URL[] { jarUrl }, ClassLoader.getPlatformClassLoader() );
      Class<?> driverClass = loader.loadClass( driverClassName );
      Driver driver = (Driver) driverClass.getDeclaredConstructor().newInstance();
      entry = new CacheEntry( loader, driver );
    } catch ( Exception e ) {
      closeSilently( loader );
      throw new KettleDatabaseException(
        "DynamicDriverCache: failed to load driver '" + driverClassName
          + "' from '" + jarAbsolutePath + "': " + e.getMessage(), e );
    }

    // 3. Write lock — double-check, insert, collect oldest entry if over capacity.
    Driver driverToReturn;
    rwLock.writeLock().lock();
    try {
      CacheEntry existing = cache.get( key );
      if ( existing != null ) {
        // Another thread beat us to it — discard our copy, return the winner's Driver.
        closeSilently( loader );
        log.logDebug( "DynamicDriverCache: concurrent load race, discarding duplicate for " + driverClassName );
        return existing.driver;
      }
      cache.put( key, entry );
      log.logBasic( "DynamicDriverCache: cached driver " + driverClassName + " @ " + jarAbsolutePath );
      driverToReturn = entry.driver;

      // FIFO eviction — remove oldest-inserted entry when over capacity.
      // The classloader is NOT explicitly closed here: Java guarantees that every loaded
      // class holds a strong reference to its defining classloader, so the loader remains
      // reachable (and its classes usable) as long as any Database instance still holds
      // the evicted Driver. GC closes the JAR file handle once all references are gone.
      if ( cache.size() > MAX_CACHE_SIZE ) {
        Iterator<Map.Entry<CacheKey, CacheEntry>> iter = cache.entrySet().iterator();
        Map.Entry<CacheKey, CacheEntry> eldest = iter.next();
        iter.remove();
        log.logBasic( "DynamicDriverCache: evicting oldest entry (cap=" + MAX_CACHE_SIZE + ") "
          + eldest.getKey().driverClass + " @ " + eldest.getKey().jarPath );
      }
    } finally {
      rwLock.writeLock().unlock();
    }

    return driverToReturn;
  }


  /**
   * Removes all cached entries for the given JAR path and closes their classloaders.
   * Called when a JAR is replaced on disk (re-download).
   *
   * <p>Unlike size-based FIFO eviction, classloaders <em>are</em> closed explicitly here
   * because the JAR file is being replaced on disk and the old file handle must be released
   * (required on Windows to allow the new file to overwrite the old one). Any active
   * {@link Database} connections using the old {@link Driver} retain their existing
   * references and already-loaded classes; only future class-loads from this classloader
   * would fail, which is acceptable since the JAR itself is no longer valid.
   */
  public void evictByJar( String jarAbsolutePath ) {
    List<CacheEntry> toClose = new ArrayList<>();

    rwLock.writeLock().lock();
    try {
      cache.entrySet().removeIf( entry -> {
        if ( entry.getKey().jarPath.equals( jarAbsolutePath ) ) {
          toClose.add( entry.getValue() );
          log.logBasic( "DynamicDriverCache: evicted (jar replaced) "
            + entry.getKey().driverClass + " @ " + jarAbsolutePath );
          return true;
        }
        return false;
      } );
    } finally {
      rwLock.writeLock().unlock();
    }

    // Close classloaders outside the lock to release JAR file handles.
    toClose.forEach( e -> closeSilently( e.cachedLoader ) );
  }

  /**
   * Returns the number of entries currently in the cache. Useful for monitoring.
   */
  public int size() {
    rwLock.readLock().lock();
    try {
      return cache.size();
    } finally {
      rwLock.readLock().unlock();
    }
  }

  private static URL getJarUrl( String jarAbsolutePath ) throws KettleDatabaseException {
    File jarFile = new File( jarAbsolutePath );
    if ( !jarFile.exists() || !jarFile.isFile() ) {
      throw new KettleDatabaseException( "DynamicDriverCache: JAR not found: " + jarAbsolutePath );
    }
    if ( !jarFile.getName().toLowerCase().endsWith( ".jar" ) ) {
      throw new KettleDatabaseException( "DynamicDriverCache: not a JAR file: " + jarAbsolutePath );
    }

    URL jarUrl;
    try {
      jarUrl = jarFile.toPath().toUri().toURL();
    } catch ( Exception e ) {
      throw new KettleDatabaseException(
        "DynamicDriverCache: cannot convert path to URL: " + jarAbsolutePath, e );
    }
    return jarUrl;
  }


  private static void closeSilently( ChildFirstURLClassLoader loader ) {
    if ( loader == null ) {
      return;
    }
    try {
      loader.close();
    } catch ( Exception e ) {
      log.logDebug( "DynamicDriverCache: failed to close cached classloader: " + e.getMessage() );
    }
  }
}
