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

package org.pentaho.amazon.s3;

import org.apache.commons.vfs2.FileObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.amazon.AmazonS3FileSystemBootstrap;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.VFSHelper;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.bowl.BaseBowl;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.shared.MemorySharedObjectsIO;
import org.pentaho.di.shared.SharedObjectsIO;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.xml.XmlMetaStore;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * PDI-21063 spike, 11.0 back-port of the master {@code PvfsConnectionIsolationMinioIT} (JUnit 4, self-contained).
 * MinIO enforces per-user bucket policies: user A may only use bucket A and user B only bucket B, so a resolution
 * that picks up the other connection's credentials fails with a real 403 AccessDenied.
 */
public class PvfsConnectionIsolationMinioIT {
  private static final String CONNECTION_VARIABLE = "connection";
  private static final String ROUNDS_PROPERTY = "pdi21063.rounds";
  // S3Provider.ACCESS_KEY_SECRET_KEY is private on this branch.
  private static final String ACCESS_KEY_SECRET_KEY = "0";

  private static final String SUFFIX = UUID.randomUUID().toString().substring( 0, 8 );
  private static final String BUCKET_A = "bucket-a-" + SUFFIX;
  private static final String BUCKET_B = "bucket-b-" + SUFFIX;
  private static final String USER_A = "user-a-" + SUFFIX;
  private static final String USER_B = "user-b-" + SUFFIX;
  private static final String SECRET_A = "secret-a-" + SUFFIX;
  private static final String SECRET_B = "secret-b-" + SUFFIX;

  private static MinIOContainer minio;
  private static Bowl bowl;
  private static ConnectionManager manager;

  @BeforeClass
  public static void startMinio() throws Exception {
    minio = new MinIOContainer(
      DockerImageName.parse( "quay.io/minio/minio:latest" ).asCompatibleSubstituteFor( "minio/minio" ) );
    minio.start();
    mc( "alias", "set", "local", "http://localhost:9000", minio.getUserName(), minio.getPassword() );
    mc( "mb", "local/" + BUCKET_A, "local/" + BUCKET_B );
    createUserForBucket( USER_A, SECRET_A, BUCKET_A );
    createUserForBucket( USER_B, SECRET_B, BUCKET_B );

    KettleEnvironment.init( false );
    new AmazonS3FileSystemBootstrap().onEnvironmentInit();
    // MemoryMetaStore cannot re-save an existing element, which the edit scenario needs.
    IMetaStore metastore = new XmlMetaStore( Files.createTempDirectory( "pdi21063-metastore" ).toString() );
    bowl = createBowl( metastore );
    manager = connectionManager( bowl );
  }

  private static Bowl createBowl( IMetaStore metastore ) {
    SharedObjectsIO sharedObjectsIO = new MemorySharedObjectsIO();
    return new BaseBowl() {
      @Override public IMetaStore getMetastore() {
        return metastore;
      }

      @Override public SharedObjectsIO getSharedObjectsIO() {
        return sharedObjectsIO;
      }

      @Override public VariableSpace getADefaultVariableSpace() {
        return Variables.getADefaultVariableSpace();
      }
    };
  }

  private static ConnectionManager connectionManager( Bowl bowl ) throws Exception {
    return bowl.getManager( ConnectionManager.class );
  }

  @AfterClass
  public static void stopMinio() {
    if ( minio != null ) {
      minio.stop();
    }
  }

  /** PDI-21035 / PDI-20723: parallel PVFS writes sharing one VariableSpace must each use their own connection. */
  @Test
  public void pdi21035ParallelPvfsWritesUseTheirOwnConnection() throws Exception {
    String connA = saveConnection( "conn-a-", USER_A, SECRET_A );
    String connB = saveConnection( "conn-b-", USER_B, SECRET_B );
    VariableSpace transformationSpace = new Variables();
    int rounds = Integer.getInteger( ROUNDS_PROPERTY, 40 );

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
    assertTrue( failures.size() + " of " + ( rounds * 2 ) + " parallel PVFS writes failed; first: "
      + ( failures.isEmpty() ? "" : failures.get( 0 ) ), failures.isEmpty() );
  }

  /** PDI-21092: resolving a PVFS URI must not overwrite the caller's "connection" variable used by raw s3://. */
  @Test
  public void pdi21092PvfsLookupDoesNotHijackCallerConnectionForRawS3() throws Exception {
    String connA = saveConnection( "conn-a-", USER_A, SECRET_A );
    String connB = saveConnection( "conn-b-", USER_B, SECRET_B );
    VariableSpace jobSpace = new Variables();
    jobSpace.setVariable( CONNECTION_VARIABLE, connB );
    String rawUri = "s3://" + BUCKET_B + "/raw-before.txt";
    assertNotNull( "precondition: the connection variable selects options for raw s3://",
      VFSHelper.getOpts( bowl, rawUri, connB, jobSpace ) );

    write( rawUri, jobSpace );
    write( "pvfs://" + connA + "/" + BUCKET_A + "/pvfs.txt", jobSpace );

    String afterPvfs = jobSpace.getVariable( CONNECTION_VARIABLE );
    String rawFailure = "";
    try {
      write( "s3://" + BUCKET_B + "/raw-after.txt", jobSpace );
    } catch ( Exception e ) {
      rawFailure = rootMessage( e );
    }
    assertEquals( "connection variable after PVFS lookup | raw s3:// write failure",
      connB + " | ", afterPvfs + " | " + rawFailure );
  }

  /** PDI-20586 probe: a changed credential variable must be used on the next lookup. */
  @Test
  public void pdi20586ChangedCredentialVariableIsUsedOnNextLookup() throws Exception {
    S3Details details = details( "conn-var-" + UUID.randomUUID().toString().substring( 0, 8 ) );
    details.setAccessKey( "${S3_ACCESS_KEY}" );
    details.setSecretKey( "${S3_SECRET_KEY}" );
    assertTrue( "precondition: connection saved", manager.save( details ) );
    String uriBase = "pvfs://" + details.getName() + "/" + BUCKET_A;

    VariableSpace allowed = new Variables();
    allowed.setVariable( "S3_ACCESS_KEY", USER_A );
    allowed.setVariable( "S3_SECRET_KEY", SECRET_A );
    write( uriBase + "/var-allowed.txt", allowed );

    VariableSpace denied = new Variables();
    denied.setVariable( "S3_ACCESS_KEY", USER_B );
    denied.setVariable( "S3_SECRET_KEY", SECRET_B );
    expectWriteFailure( uriBase + "/var-denied.txt", denied,
      "user B's credentials must be used and rejected for bucket A, but stale credentials were reused" );
  }

  /** PDI-20586 probe: saved new credentials must take effect on the next lookup. */
  @Test
  public void pdi20586EditedConnectionIsUsedOnNextLookup() throws Exception {
    String name = saveConnection( "conn-edit-", USER_A, SECRET_A );
    write( "pvfs://" + name + "/" + BUCKET_A + "/edit-before.txt", new Variables() );

    S3Details edited = details( name );
    edited.setAccessKey( USER_B );
    edited.setSecretKey( SECRET_B );
    assertTrue( "precondition: edited connection saved", manager.save( edited ) );

    expectWriteFailure( "pvfs://" + name + "/" + BUCKET_A + "/edit-after.txt", new Variables(),
      "the edited (user B) credentials must be rejected for bucket A, but the old credentials were reused" );
  }

  /** PDI-20647 probe: a deleted connection must not stay usable through a cached file system. */
  @Test
  public void pdi20647DeletedConnectionIsNotUsable() throws Exception {
    String name = saveConnection( "conn-del-", USER_A, SECRET_A );
    write( "pvfs://" + name + "/" + BUCKET_A + "/delete-before.txt", new Variables() );

    manager.delete( name );

    expectWriteFailure( "pvfs://" + name + "/" + BUCKET_A + "/delete-after.txt", new Variables(),
      "a deleted connection was still usable" );
  }

  private static S3Details details( String name ) {
    S3Details details = new S3Details();
    details.setName( name );
    details.setAuthType( ACCESS_KEY_SECRET_KEY );
    details.setRegion( "us-east-1" );
    details.setEndpoint( minio.getS3URL() );
    details.setPathStyleAccess( "true" );
    details.setSignatureVersion( "AWSS3V4SignerType" );
    return details;
  }

  private static String saveConnection( String prefix, String accessKey, String secretKey ) {
    S3Details details = details( prefix + UUID.randomUUID().toString().substring( 0, 8 ) );
    details.setAccessKey( accessKey );
    details.setSecretKey( secretKey );
    assertTrue( "precondition: connection " + details.getName() + " saved", manager.save( details ) );
    return details.getName();
  }

  private static void expectWriteFailure( String uri, VariableSpace space, String message ) {
    try {
      write( uri, space );
    } catch ( Exception | AssertionError expected ) {
      return;
    }
    fail( message );
  }

  private static void writeAfter( CyclicBarrier start, String uri, VariableSpace space, List<String> failures ) {
    try {
      start.await( 30, TimeUnit.SECONDS );
      write( uri, space );
    } catch ( Exception | AssertionError e ) {
      failures.add( uri + " -> " + rootMessage( e ) );
    }
  }

  private static void write( String uri, VariableSpace space ) throws Exception {
    FileObject file = KettleVFS.getInstance( bowl ).getFileObject( uri, space );
    try ( OutputStream out = file.getContent().getOutputStream() ) {
      out.write( uri.getBytes( StandardCharsets.UTF_8 ) );
    }
    file.refresh();
    assertTrue( "written: " + uri, file.exists() );
  }

  private static String rootMessage( Throwable e ) {
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
