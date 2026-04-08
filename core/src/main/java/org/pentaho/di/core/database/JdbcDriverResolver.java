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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.EnvUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Resolves the absolute path of a JDBC driver JAR when the originally configured path does not exist.
 *
 * <h3>Resolution chain</h3>
 * Given only the JAR file name (e.g. {@code mysql-connector-j-8.x.jar}), candidates are tried in order:
 * <ol>
 *   <li><b>Configured path as-is</b> — the value stored in the database connection attributes.</li>
 *   <li><b>{@code JDBC_DRIVERS_DIRECTORY}</b> — read via {@link Const#getJdbcDriversDirectory()}
 *       which checks OS env var first, then JVM system property. Covers Docker {@code ENV},
 *       Kubernetes {@code env:}, ECS task definitions, and {@code -D} JVM args uniformly.</li>
 *   <li><b>Kettle property {@code JDBC_DRIVERS_DIRECTORY}</b> from {@code kettle.properties}
 *       — for on-prem / BA Server installations.</li>
 *   <li><b>Pentaho solution path</b> — {@code ${solution-path}/jdbc-drivers/<jarName>}
 *       (resolved via {@code PentahoSystem} when available, relative path otherwise).</li>
 *   <li><b>Download</b> from the connection-management service — base URL read via
 *       {@link Const#getJdbcDriverServiceUrl()} ({@code JDBC_DRIVER_SERVICE_URL} env var or
 *       system property). JAR saved to the first writable directory from the chain above.</li>
 * </ol>
 *
 * <h3>Environment variable vs. system property</h3>
 * Every configurable key is resolved through {@link Const} using the established codebase pattern:
 * <pre>  NVL( System.getenv(key), System.getProperty(key) )</pre>
 * Environment variable always wins over JVM system property. This works identically on
 * bare-metal, Docker, Kubernetes, ECS, Azure Container Instances, and GCP Cloud Run.
 *
 * <h3>Thread safety</h3>
 * Concurrent downloads of the same JAR are serialised via a per-JAR {@link java.util.concurrent.locks.ReentrantLock}.
 * All other operations are stateless and safe for concurrent use.
 */
public final class JdbcDriverResolver {

  /**
   * Sub-directory name under the Pentaho solution path where JDBC drivers are stored.
   * The key name ({@code JDBC_DRIVERS_DIRECTORY}) is shared with {@link Const#getJdbcDriversDirectory()}.
   */
  public static final String JDBC_DRIVERS_SUBDIR = "jdbc-drivers";

  private static final LogChannelInterface log = LogChannel.GENERAL;

  /**
   * One lock per JAR file name — ensures that when two threads race to download the same
   * driver JAR only one performs the download while the other waits, then re-checks whether
   * the file already landed on disk before attempting its own download.
   */
  private static final ConcurrentHashMap<String, ReentrantLock> DOWNLOAD_LOCKS = new ConcurrentHashMap<>();

  private JdbcDriverResolver() {
    // utility class
  }

  /**
   * Returns an absolute path to the JDBC driver JAR.
   *
   * <p>If {@code configuredPath} points to an existing file it is returned immediately.
   * Otherwise the resolution chain described in the class Javadoc is followed.
   *
   * @param configuredPath the JAR path as stored in the database connection attributes
   *                       (may be a full path or just a file name)
   * @return absolute path of the resolved JAR file
   * @throws KettleDatabaseException if the JAR cannot be found or downloaded
   */
  public static String resolve( String driverId, String configuredPath ) throws KettleDatabaseException {
    if ( configuredPath == null || configuredPath.trim().isEmpty() ) {
      throw new KettleDatabaseException(
        "JdbcDriverResolver: configured JAR path for driver '" + driverId + "' is null or blank. "
          + "Set the dynamic driver JAR attribute (file name or absolute path) on the database connection." );
    }

    // 1 — configured path exists as-is
    if ( existsAsFile( configuredPath ) ) {
      return configuredPath;
    }

    String jarName = Paths.get( configuredPath ).getFileName().toString();
    log.logBasic( "JdbcDriverResolver: JAR not found at configured path '" + configuredPath
      + "' — searching by file name '" + jarName + "'" );

    // 2 — JDBC_DRIVERS_DIRECTORY: env var wins over system property (via Const)
    String resolved = tryDirectory( Const.getJdbcDriversDirectory(), jarName, "Const.getJdbcDriversDirectory()" );
    if ( resolved != null ) {
      return resolved;
    }

    // 3 — JDBC_DRIVERS_DIRECTORY from kettle.properties (on-prem / BA Server)
    resolved = tryDirectory( kettleProperty( "JDBC_DRIVERS_DIRECTORY" ), jarName,
      "kettle.properties:JDBC_DRIVERS_DIRECTORY" );
    if ( resolved != null ) {
      return resolved;
    }

    // 4 — Pentaho solution path  /jdbc-drivers/<jarName>
    resolved = tryDirectory( solutionJdbcDriversDir(), jarName, "solution-path/" + JDBC_DRIVERS_SUBDIR );
    if ( resolved != null ) {
      return resolved;
    }

    // 5 — download from connection-management service
    // If cache is enabled, check whether the JAR was already downloaded on a previous run
    // before making an outbound HTTP request to the connection-management service.
    boolean cacheEnabled = "Y".equalsIgnoreCase(
            EnvUtil.getSystemProperty( Const.KETTLE_DYNAMIC_DRIVER_CACHE_ENABLED ) );
    if ( cacheEnabled ) {
      Path cachedJar = resolveWritableSaveDir().resolve( driverId + ".jar" );
      if ( Files.isRegularFile( cachedJar ) ) {
        log.logBasic( "JdbcDriverResolver: cache hit — previously downloaded JAR → " + cachedJar );
        return cachedJar.toAbsolutePath().toString();
      }
    }
    return downloadFromService( driverId );
  }

  // ---------------------------------------------------------------------------
  // Internal helpers
  // ---------------------------------------------------------------------------

  /**
   * Checks if {@code dir/<jarName>} exists as a regular file and returns the path if so.
   * Returns {@code null} if {@code dir} is blank or the file does not exist.
   */
  private static String tryDirectory( String dir, String jarName, String label ) {
    if ( dir == null || dir.trim().isEmpty() ) {
      return null;
    }
    Path candidate = Paths.get( dir.trim(), jarName );
    if ( Files.isRegularFile( candidate ) ) {
      log.logBasic( "JdbcDriverResolver: found '" + jarName + "' via " + label + " → " + candidate );
      return candidate.toAbsolutePath().toString();
    }
    log.logDebug( "JdbcDriverResolver: not found at " + label + " → " + candidate );
    return null;
  }

  /**
   * Returns the value of {@code key} from {@code kettle.properties}, or {@code null}.
   */
  private static String kettleProperty( String key ) {
    try {
      java.util.Properties props = EnvUtil.readProperties( org.pentaho.di.core.Const.KETTLE_PROPERTIES );
      return props.getProperty( key );
    } catch ( Exception e ) {
      log.logDebug( "JdbcDriverResolver: could not read kettle.properties: " + e.getMessage() );
      return null;
    }
  }

  /**
   * Resolves the {@code jdbc-drivers} sub-directory under the Pentaho solution path.
   * Uses {@code PentahoSystem} when available (BA/EE server context); falls back to a
   * relative path resolved from the JVM working directory for plain PDI (Carte/Studio).
   */
  private static String solutionJdbcDriversDir() {
    // Try PentahoSystem (available when running inside the BA server)
    try {
      Class<?> sysClass = Class.forName( "org.pentaho.platform.engine.core.system.PentahoSystem" );
      Object appCtx = sysClass.getMethod( "getApplicationContext" ).invoke( null );
      if ( appCtx != null ) {
        String solutionPath = (String) appCtx.getClass()
          .getMethod( "getSolutionPath", String.class )
          .invoke( appCtx, JDBC_DRIVERS_SUBDIR );
        if ( solutionPath != null && !solutionPath.trim().isEmpty() ) {
          return solutionPath.trim();
        }
      }
    } catch ( Exception ignored ) {
      // Not running inside BA server — fall through to relative path
    }

    // Fallback: resolve relative to the current working directory (Carte / Studio)
    try {
      String cwd = new File( "" ).getCanonicalPath();
      // Walk up looking for "pentaho-server" or "data-integration" markers
      Path p = Paths.get( cwd );
      for ( int i = 0; i < 5; i++ ) {
        Path candidate = p.resolve( JDBC_DRIVERS_SUBDIR );
        if ( Files.isDirectory( candidate ) ) {
          return candidate.toString();
        }
        if ( p.getParent() == null ) {
          break;
        }
        p = p.getParent();
      }
      // Last resort: cwd/jdbc-drivers (may not exist yet — download will create it)
      return Paths.get( cwd, JDBC_DRIVERS_SUBDIR ).toString();
    } catch ( Exception e ) {
      log.logDebug( "JdbcDriverResolver: could not determine solution path: " + e.getMessage() );
      return null;
    }
  }

  /**
   * Downloads the driver JAR from the connection-management service and saves it to the
   * first writable JDBC drivers directory found (from the same resolution chain), or to
   * the system temp directory as last resort.
   *
   * <h3>Concurrent download safety</h3>
   * If two threads race to download the same JAR simultaneously:
   * <ul>
   *   <li>The first thread acquires a per-JAR {@link ReentrantLock} and performs the download.
   *       It writes to a temporary file ({@code <jarName>.tmp}) and atomically
   *       {@linkplain Files#move moves} it into the final path on success, so no thread
   *       ever sees a partial file.</li>
   *   <li>Every subsequent thread that arrives while the download is in progress waits on
   *       the same lock. Once unblocked it re-checks whether the file is already present —
   *       if yes it returns immediately without making a second HTTP request.</li>
   *   <li>If the first thread's download fails, the temp file is deleted and the next
   *       waiting thread retries the download rather than failing silently.</li>
   * </ul>
   *
   * <p>The download base URL is read from:
   * <ol>
   *   <li>Environment variable {@code JDBC_DRIVER_SERVICE_URL}</li>
   *   <li>System property {@code JDBC_DRIVER_SERVICE_URL}</li>
   * </ol>
   * The JAR is fetched from {@code <baseUrl>/api/v1/connection-drivers/<driverId>/download}.
   *
   * @param driverId       id of the JDBC driver to download from connection-management service
   * @return absolute path of the downloaded JAR
   * @throws KettleDatabaseException if the download URL is not configured or the download fails
   */
  private static String downloadFromService( String driverId )
    throws KettleDatabaseException {

    // Const.getJdbcDriverServiceUrl() applies NVL(getenv, getProperty) — works on all environments.
    String serviceBaseUrl = Const.getJdbcDriverServiceUrl();

        if ( serviceBaseUrl == null || serviceBaseUrl.trim().isEmpty() ) {
            throw new KettleDatabaseException(
                    "JdbcDriverResolver: driver JAR '" + driverId + ".jar" + "' not found at any known location "
                            + "Set environment variable or system property 'JDBC_DRIVER_SERVICE_URL'"
                            + " to enable automatic download from the connection-management service." );
        }

        // One lock per JAR name — threads for *different* JARs never block each other.
        ReentrantLock lock = DOWNLOAD_LOCKS.computeIfAbsent( driverId + ".jar", k -> new ReentrantLock() );
        lock.lock();
        try {
            Path saveDir = resolveWritableSaveDir();
            Path savePath = saveDir.resolve( driverId + ".jar" );

            // Re-check after acquiring the lock: a waiting thread may find the file was already
            // downloaded by the thread that held the lock before it.
            if ( Files.isRegularFile( savePath ) ) {
                log.logBasic( "JdbcDriverResolver: '" + driverId + ".jar" + "' already downloaded by concurrent thread → "
                        + savePath );
                return savePath.toAbsolutePath().toString();
            }

            String downloadUrl = serviceBaseUrl + "/api/v1/connection-drivers/" + driverId + "/download";
            // Write to a temp file first; move atomically on success so no thread ever
            // observes a partial JAR on disk.
            Path tempPath = saveDir.resolve( driverId + ".jar" + ".tmp" );

            log.logBasic( "JdbcDriverResolver: downloading '" + driverId + ".jar" + "' from " + downloadUrl
                    + " → " + savePath );

            try {
                Files.createDirectories( saveDir );
                HttpURLConnection conn = (HttpURLConnection) new URL( downloadUrl ).openConnection();
                conn.setConnectTimeout( 10_000 );
                conn.setReadTimeout( 60_000 );
                conn.setRequestMethod( "GET" );
                conn.setRequestProperty( "Accept", "*/*" );

                String bearerToken = CmsTokenProvider.getInstance().getToken();
                if ( bearerToken != null ) {
                  conn.setRequestProperty( "Authorization", "Bearer " + bearerToken );
                }

                int status = conn.getResponseCode();
                if ( status != HttpURLConnection.HTTP_OK ) {
                    throw new KettleDatabaseException(
                            "JdbcDriverResolver: download of '" + driverId + ".jar" + "' failed — HTTP " + status
                                    + " from " + downloadUrl );
                }

                try ( InputStream in = conn.getInputStream();
                      FileOutputStream out = new FileOutputStream( tempPath.toFile() ) ) {
                    byte[] buf = new byte[ 8192 ];
                    int read;
                    while ( ( read = in.read( buf ) ) != -1 ) {
                        out.write( buf, 0, read );
                    }
                } finally {
                    conn.disconnect();
                }

                // Move temp file to final location; fall back to non-atomic copy if needed.
                moveWithAtomicFallback( tempPath, savePath );

                log.logBasic( "JdbcDriverResolver: download complete → " + savePath );

                DynamicDriverCache.getInstance().evictByJar( savePath.toAbsolutePath().toString() );

                return savePath.toAbsolutePath().toString();

            } catch ( KettleDatabaseException e ) {
                deleteSilently( tempPath );
                throw e;
            } catch ( Exception e ) {
                deleteSilently( tempPath );
                throw new KettleDatabaseException(
                        "JdbcDriverResolver: failed to download '" + driverId + ".jar" + "' from '"
                                + downloadUrl + "': " + e.getMessage(), e );
            }
        } finally {
            lock.unlock();
            DOWNLOAD_LOCKS.remove( driverId + ".jar", lock );
        }
    }

  /**
   * Moves {@code source} to {@code target}, attempting an atomic move first and falling back to a
   * non-atomic replace if the filesystem does not support atomic moves.
   */
  private static void moveWithAtomicFallback( Path source, Path target ) throws java.io.IOException {
    try {
      Files.move( source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING );
    } catch ( java.nio.file.AtomicMoveNotSupportedException e ) {
      Files.move( source, target, StandardCopyOption.REPLACE_EXISTING );
    }
  }

  /**
   * Deletes a file without throwing — used to clean up temp files on failure.
   */
  private static void deleteSilently( Path path ) {
    try {
      Files.deleteIfExists( path );
    } catch ( Exception ignored ) {
      // best-effort cleanup
    }
  }

  /**
   * Returns the first writable directory from the resolution chain to use as the download target.
   * Falls back to the system temp directory if none is writable.
   */
  private static Path resolveWritableSaveDir() {
    String[] candidates = {
      Const.getJdbcDriversDirectory(),          // NVL(getenv, getProperty) for JDBC_DRIVERS_DIRECTORY
      kettleProperty( "JDBC_DRIVERS_DIRECTORY" ), // kettle.properties
      solutionJdbcDriversDir()                  // PentahoSystem / CWD walk
    };
    for ( String dir : candidates ) {
      if ( dir != null && !dir.trim().isEmpty() ) {
        Path p = Paths.get( dir.trim() );
        try {
          Files.createDirectories( p );
          if ( Files.isWritable( p ) ) {
            return p;
          }
        } catch ( Exception ignored ) {
          // try next
        }
      }
    }
    // Last resort — system temp directory
    return Paths.get( System.getProperty( "java.io.tmpdir" ), JDBC_DRIVERS_SUBDIR );
  }

  /**
   * Returns {@code true} if {@code path} points to an existing regular file.
   */
  private static boolean existsAsFile( String path ) {
    if ( path == null || path.trim().isEmpty() ) {
      return false;
    }
    return Files.isRegularFile( Paths.get( path ) );
  }
}
