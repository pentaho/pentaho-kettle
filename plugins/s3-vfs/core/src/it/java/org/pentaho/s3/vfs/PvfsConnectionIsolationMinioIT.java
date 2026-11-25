/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.s3.vfs;

import org.apache.commons.vfs2.FileObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pentaho.amazon.s3.S3Details;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.VFSHelper;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.xml.XmlMetaStore;
import org.pentaho.s3.vfs.support.KettleS3Environment;
import org.pentaho.s3.vfs.support.TestBowl;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PDI-21063 spike: reproduces the PVFS connection-isolation symptoms against a real S3 server that enforces
 * per-user bucket policies. User A may only use bucket A and user B only bucket B, so any resolution that picks up
 * the other connection's credentials fails with a real 403 AccessDenied, as reported in PDI-21035 / PDI-20723.
 */
class PvfsConnectionIsolationMinioIT {
  private static final String CONNECTION_VARIABLE = "connection";
  private static final String ROUNDS_PROPERTY = "pdi21063.rounds";

  private static final String SUFFIX = UUID.randomUUID().toString().substring( 0, 8 );
  private static final String BUCKET_A = "bucket-a-" + SUFFIX;
  private static final String BUCKET_B = "bucket-b-" + SUFFIX;
  private static final String USER_A = "user-a-" + SUFFIX;
  private static final String USER_B = "user-b-" + SUFFIX;
  private static final String SECRET_A = "secret-a-" + SUFFIX;
  private static final String SECRET_B = "secret-b-" + SUFFIX;

  private static MinIOContainer minio;
  private static KettleS3Environment env;
  private static Bowl bowl;
  private static ConnectionManager manager;

  @BeforeAll
  static void startMinio() throws Exception {
    minio = new MinIOContainer(
      DockerImageName.parse( "quay.io/minio/minio:latest" ).asCompatibleSubstituteFor( "minio/minio" ) );
    minio.start();
    mc( "alias", "set", "local", "http://localhost:9000", minio.getUserName(), minio.getPassword() );
    mc( "mb", "local/" + BUCKET_A, "local/" + BUCKET_B );
    createUserForBucket( USER_A, SECRET_A, BUCKET_A );
    createUserForBucket( USER_B, SECRET_B, BUCKET_B );

    env = new KettleS3Environment( minio.getS3URL() );
    // MemoryMetaStore cannot re-save an existing element, which the edit scenario needs.
    IMetaStore metastore = new XmlMetaStore( Files.createTempDirectory( "pdi21063-metastore" ).toString() );
    bowl = new TestBowl() {
      @Override public IMetaStore getMetastore() {
        return metastore;
      }
    };
    manager = bowl.getManager( ConnectionManager.class );
  }

  @AfterAll
  static void stopMinio() {
    if ( minio != null ) {
      minio.stop();
    }
  }

  /**
   * PDI-21035 / PDI-20723: two steps of one transformation share a VariableSpace and write through different PVFS
   * connections at the same time. Each write must use its own connection's credentials.
   */
  @Test
  void pdi21035ParallelPvfsWritesUseTheirOwnConnection() throws Exception {
    String connA = saveConnection( "conn-a-", USER_A, SECRET_A );
    String connB = saveConnection( "conn-b-", USER_B, SECRET_B );
    VariableSpace transformationSpace = new Variables();
    int rounds = Integer.getInteger( ROUNDS_PROPERTY, 40 );

    // Sanity check: each connection works on its own bucket when used alone.
    write( "pvfs://" + connA + "/" + BUCKET_A + "/sanity.txt", new Variables() );
    write( "pvfs://" + connB + "/" + BUCKET_B + "/sanity.txt", new Variables() );

    List<String> failures = Collections.synchronizedList( new ArrayList<>() );
    ExecutorService executor = Executors.newFixedThreadPool( 2 );
    try {
      for ( int round = 0; round < rounds; round++ ) {
        CyclicBarrier start = new CyclicBarrier( 2 );
        String file = "/parallel-" + round + ".txt";
        Future<?> stepA = executor.submit( () -> writeAfter( start,
          "pvfs://" + connA + "/" + BUCKET_A + file, transformationSpace, failures ) );
        Future<?> stepB = executor.submit( () -> writeAfter( start,
          "pvfs://" + connB + "/" + BUCKET_B + file, transformationSpace, failures ) );
        stepA.get( 60, TimeUnit.SECONDS );
        stepB.get( 60, TimeUnit.SECONDS );
      }
    } finally {
      executor.shutdownNow();
    }
    assertTrue( failures.isEmpty(), failures.size() + " of " + ( rounds * 2 )
      + " parallel PVFS writes failed; first: " + ( failures.isEmpty() ? "" : failures.get( 0 ) ) );
  }

  /**
   * PDI-21092: a job parameter named "connection" selects the connection for raw s3:// URIs. Resolving an unrelated
   * PVFS URI with the same space must not overwrite it, so a later raw write keeps the caller's credentials.
   */
  @Test
  void pdi21092PvfsLookupDoesNotHijackCallerConnectionForRawS3() throws Exception {
    String connA = saveConnection( "conn-a-", USER_A, SECRET_A );
    String connB = saveConnection( "conn-b-", USER_B, SECRET_B );
    VariableSpace jobSpace = new Variables();
    jobSpace.setVariable( CONNECTION_VARIABLE, connB );
    String rawUri = "s3://" + BUCKET_B + "/raw-before.txt";
    assertNotNull( VFSHelper.getOpts( bowl, rawUri, connB, jobSpace ),
      "precondition: the connection variable selects options for raw s3:// (provider="
        + manager.getConnectionProvider( rawUri ) + ", details=" + manager.getConnectionDetails( rawUri, connB )
        + ")" );

    write( "s3://" + BUCKET_B + "/raw-before.txt", jobSpace );
    write( "pvfs://" + connA + "/" + BUCKET_A + "/pvfs.txt", jobSpace );

    String afterPvfs = jobSpace.getVariable( CONNECTION_VARIABLE );
    Exception rawFailure = null;
    try {
      write( "s3://" + BUCKET_B + "/raw-after.txt", jobSpace );
    } catch ( Exception e ) {
      rawFailure = e;
    }
    String rawFailureMessage = rootMessage( rawFailure );
    assertAll(
      () -> assertEquals( connB, afterPvfs, "the caller's connection variable was overwritten by the PVFS lookup" ),
      () -> assertEquals( "", rawFailureMessage, "raw s3:// write after a PVFS lookup failed" ) );
  }

  /**
   * PDI-20586 probe: a connection whose credentials come from variables must pick up a changed value on the next
   * lookup instead of reusing the cached S3 file system or client.
   */
  @Test
  void pdi20586ChangedCredentialVariableIsUsedOnNextLookup() throws Exception {
    S3Details details = env.details( "conn-var-" + UUID.randomUUID().toString().substring( 0, 8 ) );
    details.setAccessKey( "${S3_ACCESS_KEY}" );
    details.setSecretKey( "${S3_SECRET_KEY}" );
    assertTrue( manager.save( details ), "precondition: connection saved" );
    String uriBase = "pvfs://" + details.getName() + "/" + BUCKET_A;

    VariableSpace allowed = new Variables();
    allowed.setVariable( "S3_ACCESS_KEY", USER_A );
    allowed.setVariable( "S3_SECRET_KEY", SECRET_A );
    write( uriBase + "/var-allowed.txt", allowed );

    VariableSpace denied = new Variables();
    denied.setVariable( "S3_ACCESS_KEY", USER_B );
    denied.setVariable( "S3_SECRET_KEY", SECRET_B );
    assertThrows( Exception.class, () -> write( uriBase + "/var-denied.txt", denied ),
      "user B's credentials must be used and rejected for bucket A, but stale credentials were reused" );
  }

  /**
   * PDI-20586 probe: saving new credentials for an existing connection must take effect on the next lookup.
   */
  @Test
  void pdi20586EditedConnectionIsUsedOnNextLookup() throws Exception {
    String name = saveConnection( "conn-edit-", USER_A, SECRET_A );
    write( "pvfs://" + name + "/" + BUCKET_A + "/edit-before.txt", new Variables() );

    S3Details edited = env.details( name );
    edited.setAccessKey( USER_B );
    edited.setSecretKey( SECRET_B );
    assertTrue( manager.save( edited ), "precondition: edited connection saved" );

    assertThrows( Exception.class,
      () -> write( "pvfs://" + name + "/" + BUCKET_A + "/edit-after.txt", new Variables() ),
      "the edited (user B) credentials must be rejected for bucket A, but the old credentials were reused" );
  }

  /**
   * PDI-20647 probe: a deleted connection must not stay usable through a cached file system.
   */
  @Test
  void pdi20647DeletedConnectionIsNotUsable() throws Exception {
    String name = saveConnection( "conn-del-", USER_A, SECRET_A );
    write( "pvfs://" + name + "/" + BUCKET_A + "/delete-before.txt", new Variables() );

    manager.delete( name );

    assertThrows( Exception.class,
      () -> write( "pvfs://" + name + "/" + BUCKET_A + "/delete-after.txt", new Variables() ),
      "a deleted connection was still usable" );
  }

  /**
   * PDI-21035 / PDI-20723, nested form: {@code zip:pvfs://} layers the zip provider over a PVFS file. Two steps sharing
   * a VariableSpace read zip entries through different connections at the same time; each read must use its own
   * connection, and the caller's space must not gain a {@code connection} variable (PDI-21092).
   */
  @Test
  void nestedZipOverPvfsParallelReadsUseTheirOwnConnection() throws Exception {
    String connA = saveConnection( "conn-zip-a-", USER_A, SECRET_A );
    String connB = saveConnection( "conn-zip-b-", USER_B, SECRET_B );
    int rounds = Integer.getInteger( ROUNDS_PROPERTY, 20 );
    // One archive per round, so every round creates new zip and PVFS file systems instead of hitting the cache.
    for ( int round = 0; round < rounds; round++ ) {
      writeBytes( "pvfs://" + connA + "/" + BUCKET_A + "/nested-" + round + ".zip", new Variables(),
        zipWith( "entry.txt", "A" + round ) );
      writeBytes( "pvfs://" + connB + "/" + BUCKET_B + "/nested-" + round + ".zip", new Variables(),
        zipWith( "entry.txt", "B" + round ) );
    }

    VariableSpace transformationSpace = new Variables();
    List<String> failures = Collections.synchronizedList( new ArrayList<>() );
    ExecutorService executor = Executors.newFixedThreadPool( 2 );
    try {
      for ( int round = 0; round < rounds; round++ ) {
        CyclicBarrier start = new CyclicBarrier( 2 );
        String entry = "/nested-" + round + ".zip!/entry.txt";
        String expectedA = "A" + round;
        String expectedB = "B" + round;
        Future<?> stepA = executor.submit( () -> readAfter( start,
          "zip:pvfs://" + connA + "/" + BUCKET_A + entry, expectedA, transformationSpace, failures ) );
        Future<?> stepB = executor.submit( () -> readAfter( start,
          "zip:pvfs://" + connB + "/" + BUCKET_B + entry, expectedB, transformationSpace, failures ) );
        stepA.get( 60, TimeUnit.SECONDS );
        stepB.get( 60, TimeUnit.SECONDS );
      }
    } finally {
      executor.shutdownNow();
    }
    assertAll(
      () -> assertTrue( failures.isEmpty(), failures.size() + " of " + ( rounds * 2 )
        + " parallel zip:pvfs reads failed; first: " + ( failures.isEmpty() ? "" : failures.get( 0 ) ) ),
      () -> assertNull( transformationSpace.getVariable( CONNECTION_VARIABLE ),
        "a nested PVFS lookup wrote the connection selection into the caller's space" ) );
  }

  /**
   * Nested {@code zip:pvfs://} with credentials taken from the caller's variables. The plain PVFS form resolves them
   * (see {@link #pdi20586ChangedCredentialVariableIsUsedOnNextLookup()}); this documents whether the layered form also
   * passes the caller's space down to the PVFS layer.
   */
  @Test
  void nestedZipOverPvfsResolvesCredentialVariablesFromCaller() throws Exception {
    S3Details details = env.details( "conn-zip-var-" + UUID.randomUUID().toString().substring( 0, 8 ) );
    details.setAccessKey( "${S3_ACCESS_KEY}" );
    details.setSecretKey( "${S3_SECRET_KEY}" );
    assertTrue( manager.save( details ), "precondition: connection saved" );
    VariableSpace allowed = new Variables();
    allowed.setVariable( "S3_ACCESS_KEY", USER_A );
    allowed.setVariable( "S3_SECRET_KEY", SECRET_A );
    String archive = "pvfs://" + details.getName() + "/" + BUCKET_A + "/nested-var.zip";
    writeBytes( archive, allowed, zipWith( "entry.txt", "variables" ) );

    assertEquals( "variables", read( "zip:" + archive + "!/entry.txt", allowed ),
      "zip:pvfs:// did not resolve the connection's credential variables from the caller's space" );
  }

  private static void readAfter( CyclicBarrier start, String uri, String expected, VariableSpace space,
                                 List<String> failures ) {
    try {
      start.await( 30, TimeUnit.SECONDS );
      String actual = read( uri, space );
      if ( !expected.equals( actual ) ) {
        failures.add( uri + " -> read '" + actual + "', expected '" + expected + "'" );
      }
    } catch ( Exception e ) {
      failures.add( uri + " -> " + rootMessage( e ) );
    }
  }

  private static String read( String uri, VariableSpace space ) throws Exception {
    FileObject file = KettleVFS.getInstance( bowl ).getFileObject( uri, space );
    try ( InputStream in = file.getContent().getInputStream() ) {
      return new String( in.readAllBytes(), StandardCharsets.UTF_8 );
    }
  }

  private static byte[] zipWith( String entry, String content ) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try ( ZipOutputStream zip = new ZipOutputStream( bytes ) ) {
      zip.putNextEntry( new ZipEntry( entry ) );
      zip.write( content.getBytes( StandardCharsets.UTF_8 ) );
      zip.closeEntry();
    }
    return bytes.toByteArray();
  }

  private static String saveConnection( String prefix, String accessKey, String secretKey ) throws Exception {
    S3Details details = env.details( prefix + UUID.randomUUID().toString().substring( 0, 8 ) );
    details.setAccessKey( accessKey );
    details.setSecretKey( secretKey );
    assertTrue( manager.save( details ), "precondition: connection " + details.getName() + " saved" );
    return details.getName();
  }

  private static void writeAfter( CyclicBarrier start, String uri, VariableSpace space, List<String> failures ) {
    try {
      start.await( 30, TimeUnit.SECONDS );
      write( uri, space );
    } catch ( Exception e ) {
      failures.add( uri + " -> " + rootMessage( e ) );
    }
  }

  private static void write( String uri, VariableSpace space ) throws Exception {
    writeBytes( uri, space, uri.getBytes( StandardCharsets.UTF_8 ) );
  }

  private static void writeBytes( String uri, VariableSpace space, byte[] content ) throws Exception {
    IKettleVFS vfs = KettleVFS.getInstance( bowl );
    FileObject file = vfs.getFileObject( uri, space );
    try ( OutputStream out = file.getContent().getOutputStream() ) {
      out.write( content );
    }
    file.refresh();
    assertTrue( file.exists(), "written: " + uri );
  }

  private static String rootMessage( Throwable e ) {
    if ( e == null ) {
      return "";
    }
    Throwable root = e;
    while ( root.getCause() != null && root.getCause() != root ) {
      root = root.getCause();
    }
    return root.getClass().getSimpleName() + ": " + root.getMessage();
  }

  private static void createUserForBucket( String user, String secret, String bucket ) throws Exception {
    String policy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Action\":[\"s3:*\"],"
      + "\"Resource\":[\"arn:aws:s3:::" + bucket + "\",\"arn:aws:s3:::" + bucket + "/*\"]}]}";
    String policyFile = "/tmp/" + user + ".json";
    exec( "sh", "-c", "printf '%s' '" + policy + "' > " + policyFile );
    mc( "admin", "user", "add", "local", user, secret );
    mc( "admin", "policy", "create", "local", user + "-policy", policyFile );
    mc( "admin", "policy", "attach", "local", user + "-policy", "--user", user );
  }

  private static void mc( String... args ) throws Exception {
    String[] command = new String[ args.length + 1 ];
    command[ 0 ] = "mc";
    System.arraycopy( args, 0, command, 1, args.length );
    exec( command );
  }

  private static void exec( String... command ) throws Exception {
    Container.ExecResult result = minio.execInContainer( command );
    if ( result.getExitCode() != 0 ) {
      throw new IllegalStateException( String.join( " ", command ) + " failed: " + result.getStdout()
        + result.getStderr() );
    }
  }
}
