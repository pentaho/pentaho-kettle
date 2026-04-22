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

import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link JdbcDriverResolver}.
 *
 * <p>Covers the resolution chain (directory lookup, env/system-property precedence,
 * kettle.properties lookup), download success/failure, and concurrent-download locking.
 */
public class JdbcDriverResolverTest {

  @ClassRule
  public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  /** Fake JAR content — just a non-empty byte sequence. */
  private static final byte[] FAKE_JAR_BYTES = new byte[] { 0x50, 0x4B, 0x03, 0x04 };

  private static final String DRIVER_ID = "mysql-connector-j";
  private static final String JAR_NAME   = DRIVER_ID + ".jar";

  // System properties manipulated by tests — restored in @After
  private String savedJdbcDriversDir;
  private String savedServiceUrl;

  @Before
  public void saveSystemProperties() {
    savedJdbcDriversDir = System.getProperty( "JDBC_DRIVERS_DIRECTORY" );
    savedServiceUrl     = System.getProperty( "JDBC_DRIVER_SERVICE_URL" );
  }

  @After
  public void restoreSystemProperties() {
    restoreOrClear( "JDBC_DRIVERS_DIRECTORY", savedJdbcDriversDir );
    restoreOrClear( "JDBC_DRIVER_SERVICE_URL", savedServiceUrl );
    clearDownloadLocks();
  }

  private static void restoreOrClear( String key, String saved ) {
    if ( saved == null ) {
      System.clearProperty( key );
    } else {
      System.setProperty( key, saved );
    }
  }

  // ---------------------------------------------------------------------------
  // Step 1 — configured path exists as-is
  // ---------------------------------------------------------------------------

  @Test
  public void resolve_configuredPathExistsAsFile_returnsImmediately() throws Exception {
    File jar = tmp.newFile( JAR_NAME );
    Files.write( jar.toPath(), FAKE_JAR_BYTES );

    String result = JdbcDriverResolver.resolve( DRIVER_ID, jar.getAbsolutePath() );

    assertEquals( jar.getAbsolutePath(), result );
  }

  // ---------------------------------------------------------------------------
  // Step 2 — JDBC_DRIVERS_DIRECTORY system property (Const.getJdbcDriversDirectory)
  // ---------------------------------------------------------------------------

  @Test
  public void resolve_systemPropertyDirContainsJar_returnsFromThatDir() throws Exception {
    File driversDir = tmp.newFolder( "drivers" );
    File jar = new File( driversDir, JAR_NAME );
    Files.write( jar.toPath(), FAKE_JAR_BYTES );

    System.setProperty( "JDBC_DRIVERS_DIRECTORY", driversDir.getAbsolutePath() );
    // Configured path does NOT exist — only the base name is known
    String nonExistent = tmp.getRoot().getAbsolutePath() + "/nonexistent/" + JAR_NAME;

    String result = JdbcDriverResolver.resolve( DRIVER_ID, nonExistent );

    assertEquals( jar.getAbsoluteFile().getCanonicalPath(),
      new File( result ).getCanonicalPath() );
  }

  @Test
  public void resolve_systemPropertyDirDoesNotContainJar_continuesResolutionChain() throws Exception {
    File driversDir = tmp.newFolder( "drivers-empty" );
    // No JAR placed in the directory.
    System.setProperty( "JDBC_DRIVERS_DIRECTORY", driversDir.getAbsolutePath() );
    System.clearProperty( "JDBC_DRIVER_SERVICE_URL" );

    // Resolution must fall through to step 5 (download) and fail with "not configured"
    String nonExistent = tmp.getRoot().getAbsolutePath() + "/nonexistent/" + JAR_NAME;
    try {
      JdbcDriverResolver.resolve( DRIVER_ID, nonExistent );
      fail( "Expected KettleDatabaseException because no service URL is set" );
    } catch ( KettleDatabaseException ex ) {
      assertTrue( "Message should mention JDBC_DRIVER_SERVICE_URL",
        ex.getMessage().contains( "JDBC_DRIVER_SERVICE_URL" ) );
    }
  }

  // ---------------------------------------------------------------------------
  // Step 3 — kettle.properties JDBC_DRIVERS_DIRECTORY
  //          EnvUtil.readProperties("kettle.properties") checks new File("kettle.properties").exists()
  //          first, so a file named "kettle.properties" in the CWD is picked up immediately.
  // ---------------------------------------------------------------------------

  @Test
  public void resolve_kettlePropertiesDirContainsJar_returnsFromThatDir() throws Exception {
    File driversDir = tmp.newFolder( "kettle-drivers" );
    File jar = new File( driversDir, JAR_NAME );
    Files.write( jar.toPath(), FAKE_JAR_BYTES );

    // Write kettle.properties to the CWD — EnvUtil checks this path first
    Path cwdKettleProps = Paths.get( "kettle.properties" );
    Files.write( cwdKettleProps,
      ( "JDBC_DRIVERS_DIRECTORY=" + driversDir.getAbsolutePath() ).getBytes() );

    try {
      // Ensure step 2 does NOT fire
      System.clearProperty( "JDBC_DRIVERS_DIRECTORY" );

      String nonExistent = tmp.getRoot().getAbsolutePath() + "/nonexistent/" + JAR_NAME;
      String result = JdbcDriverResolver.resolve( DRIVER_ID, nonExistent );

      assertEquals( jar.getAbsoluteFile().getCanonicalPath(),
        new File( result ).getCanonicalPath() );
    } finally {
      Files.deleteIfExists( cwdKettleProps );
    }
  }

  // ---------------------------------------------------------------------------
  // Step 5 — download from service
  // ---------------------------------------------------------------------------

  @Test
  public void resolve_noServiceUrl_throwsDescriptiveException() throws Exception {
    System.clearProperty( "JDBC_DRIVERS_DIRECTORY" );
    System.clearProperty( "JDBC_DRIVER_SERVICE_URL" );

    String nonExistent = tmp.getRoot().getAbsolutePath() + "/nonexistent/" + JAR_NAME;
    try {
      JdbcDriverResolver.resolve( DRIVER_ID, nonExistent );
      fail( "Expected KettleDatabaseException" );
    } catch ( KettleDatabaseException ex ) {
      assertTrue( ex.getMessage().contains( "JDBC_DRIVER_SERVICE_URL" ) );
    }
  }

  @Test
  public void resolve_downloadSucceeds_returnsDownloadedJarPath() throws Exception {
    HttpServer server = startFakeServer( 200, FAKE_JAR_BYTES );
    try {
      File saveDir = tmp.newFolder( "download-target" );
      System.setProperty( "JDBC_DRIVERS_DIRECTORY", saveDir.getAbsolutePath() );
      System.setProperty( "JDBC_DRIVER_SERVICE_URL",
        "http://localhost:" + server.getAddress().getPort() );

      String nonExistent = tmp.getRoot().getAbsolutePath() + "/nonexistent/" + JAR_NAME;
      String result = JdbcDriverResolver.resolve( DRIVER_ID, nonExistent );

      assertTrue( "Result path must end with the JAR name", result.endsWith( JAR_NAME ) );
      assertTrue( "Downloaded file must exist on disk", new File( result ).isFile() );
      assertTrue( "Downloaded file must not be empty", new File( result ).length() > 0 );
    } finally {
      server.stop( 0 );
    }
  }

  @Test
  public void resolve_downloadReturnsNon200_throwsException() throws Exception {
    HttpServer server = startFakeServer( 404, new byte[0] );
    try {
      File saveDir = tmp.newFolder( "download-fail-dir" );
      System.setProperty( "JDBC_DRIVERS_DIRECTORY", saveDir.getAbsolutePath() );
      System.setProperty( "JDBC_DRIVER_SERVICE_URL",
        "http://localhost:" + server.getAddress().getPort() );

      String nonExistent = tmp.getRoot().getAbsolutePath() + "/nonexistent/" + JAR_NAME;
      try {
        JdbcDriverResolver.resolve( DRIVER_ID, nonExistent );
        fail( "Expected KettleDatabaseException on HTTP 404" );
      } catch ( KettleDatabaseException ex ) {
        assertTrue( "Message should mention HTTP 404", ex.getMessage().contains( "404" ) );
      }
    } finally {
      server.stop( 0 );
    }
  }

  @Test
  public void resolve_downloadFails_tempFileIsCleanedUp() throws Exception {
    // Server that returns 500 so the download fails
    HttpServer server = startFakeServer( 500, new byte[0] );
    try {
      File saveDir = tmp.newFolder( "download-cleanup-dir" );
      System.setProperty( "JDBC_DRIVERS_DIRECTORY", saveDir.getAbsolutePath() );
      System.setProperty( "JDBC_DRIVER_SERVICE_URL",
        "http://localhost:" + server.getAddress().getPort() );

      String nonExistent = tmp.getRoot().getAbsolutePath() + "/nonexistent/" + JAR_NAME;
      try {
        JdbcDriverResolver.resolve( DRIVER_ID, nonExistent );
      } catch ( KettleDatabaseException ignored ) {
        // expected
      }

      // The .tmp file must not be left behind
      File tempFile = new File( saveDir, JAR_NAME + ".tmp" );
      assertTrue( "Temp file must be cleaned up after failed download", !tempFile.exists() );
    } finally {
      server.stop( 0 );
    }
  }

  // ---------------------------------------------------------------------------
  // Concurrent download — only one HTTP request for the same JAR
  // ---------------------------------------------------------------------------

  @Test
  public void resolve_concurrentDownloadsOfSameJar_onlyOneHttpRequest() throws Exception {
    AtomicInteger requestCount = new AtomicInteger( 0 );
    CountDownLatch firstRequestStarted = new CountDownLatch( 1 );
    CountDownLatch releaseFirst = new CountDownLatch( 1 );

    // Server that stalls the first request so the second thread is forced to wait
    HttpServer server = HttpServer.create( new InetSocketAddress( 0 ), 0 );
    server.createContext( "/", exchange -> {
      requestCount.incrementAndGet();
      firstRequestStarted.countDown();
      try {
        releaseFirst.await(); // hold first request until second thread is queued
      } catch ( InterruptedException ignored ) {
        Thread.currentThread().interrupt();
      }
      byte[] body = FAKE_JAR_BYTES;
      exchange.sendResponseHeaders( 200, body.length );
      try ( OutputStream os = exchange.getResponseBody() ) {
        os.write( body );
      }
      exchange.close();
    } );
    server.start();

    try {
      File saveDir = tmp.newFolder( "concurrent-dir" );
      System.setProperty( "JDBC_DRIVERS_DIRECTORY", saveDir.getAbsolutePath() );
      System.setProperty( "JDBC_DRIVER_SERVICE_URL",
        "http://localhost:" + server.getAddress().getPort() );

      String nonExistent = tmp.getRoot().getAbsolutePath() + "/nonexistent/" + JAR_NAME;

      ExecutorService pool = Executors.newFixedThreadPool( 2 );
      Future<String> f1 = pool.submit( () -> JdbcDriverResolver.resolve( DRIVER_ID, nonExistent ) );

      firstRequestStarted.await(); // wait until thread 1 is inside the HTTP call

      Future<String> f2 = pool.submit( () -> JdbcDriverResolver.resolve( DRIVER_ID, nonExistent ) );

      releaseFirst.countDown(); // let the download finish

      String path1 = f1.get();
      String path2 = f2.get();
      pool.shutdown();

      assertEquals( "Both threads must return the same path", path1, path2 );
      // Only one HTTP request should have been made
      assertEquals( "Exactly one download should have occurred", 1, requestCount.get() );
    } finally {
      server.stop( 0 );
    }
  }

  // ---------------------------------------------------------------------------
  // resolveWritableSaveDir — falls back to system temp when no candidate is writable
  // ---------------------------------------------------------------------------

  @Test
  public void resolve_noWritableDir_savesToSystemTempSubdir() throws Exception {
    // Clear all directory candidates so resolveWritableSaveDir falls back to tmpdir
    System.clearProperty( "JDBC_DRIVERS_DIRECTORY" );

    HttpServer server = startFakeServer( 200, FAKE_JAR_BYTES );
    try {
      System.setProperty( "JDBC_DRIVER_SERVICE_URL",
        "http://localhost:" + server.getAddress().getPort() );

      String nonExistent = tmp.getRoot().getAbsolutePath() + "/nonexistent/" + JAR_NAME;
      String result = JdbcDriverResolver.resolve( DRIVER_ID, nonExistent );

      assertTrue( "Result must point to an existing file", new File( result ).isFile() );
      // Clean up so we don't leave files in the real tmpdir
      new File( result ).delete();
    } finally {
      server.stop( 0 );
    }
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /**
   * Starts a minimal HTTP server on a random port that responds with {@code statusCode}
   * and {@code body} to every GET request.
   */
  private static HttpServer startFakeServer( int statusCode, byte[] body ) throws IOException {
    HttpServer server = HttpServer.create( new InetSocketAddress( 0 ), 0 );
    server.createContext( "/", exchange -> {
      exchange.sendResponseHeaders( statusCode, body.length );
      try ( OutputStream os = exchange.getResponseBody() ) {
        os.write( body );
      }
      exchange.close();
    } );
    server.start();
    return server;
  }

  /**
   * Clears the static {@code DOWNLOAD_LOCKS} map via reflection so tests do not interfere
   * with each other (the map is normally cleaned up per-download, but failed downloads in
   * tests can leave stale entries).
   */
  @SuppressWarnings( "unchecked" )
  private static void clearDownloadLocks() {
    try {
      Field f = JdbcDriverResolver.class.getDeclaredField( "DOWNLOAD_LOCKS" );
      f.setAccessible( true );
      ( (ConcurrentHashMap<String, ReentrantLock>) f.get( null ) ).clear();
    } catch ( Exception e ) {
      // best-effort
    }
  }
}
