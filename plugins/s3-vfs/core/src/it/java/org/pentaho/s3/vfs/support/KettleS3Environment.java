package org.pentaho.s3.vfs.support;

import org.pentaho.amazon.s3.S3Details;
import org.pentaho.amazon.s3.S3FileSystemBootstrap;
import org.pentaho.amazon.s3.provider.S3Provider;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.vfs.KettleVFS;

/**
 * A Kettle environment for S3 connections pointing at a local S3 server, on a bowl of its own so test
 * classes do not share connection state.
 */
public class KettleS3Environment {

  public static final String BUCKET = "balde";
  public static final String DEFAULT_CONNECTION = "s3conn";

  private static boolean kettleStarted = false;

  private final Bowl bowl = new TestBowl();
  private final String endpoint;

  public KettleS3Environment( String endpoint ) throws KettleException, LifecycleException {
    this.endpoint = endpoint;
    startKettle();
  }

  /** Saves the default connection and returns the {@code pvfs} URI of its bucket root. */
  public String createDefaultConnection( String bucketName ) throws KettleException {
    return createConnection( details( DEFAULT_CONNECTION ), bucketName );
  }

  /** Saves a connection and returns the {@code pvfs} URI of its bucket root. */
  public String createConnection( S3Details details, String bucketName ) throws KettleException {
    bowl.getManager( ConnectionManager.class ).save( details );
    return "pvfs://" + details.getName() + "/" + bucketName;
  }

  /** Details that reach the local server, for a test to adjust before saving. */
  public S3Details details( String name ) {
    S3Details details = details();
    details.setName( name );
    return details;
  }

  /** Unnamed details for direct S3 access to the local server. */
  public S3Details details() {
    S3Details details = new S3Details();
    details.setAuthType( S3Provider.ACCESS_KEY_SECRET_KEY );
    details.setAccessKey( "test-access-key" );
    details.setSecretKey( "test-secret-key" );
    details.setRegion( "us-east-1" );
    details.setEndpoint( endpoint );
    details.setPathStyleAccess( "true" );
    // the driver falls back to a system property that does not hold a signer name, so be explicit
    details.setSignatureVersion( "AWSS3V4SignerType" );
    return details;
  }

  public IKettleVFS vfs() {
    return KettleVFS.getInstance( bowl );
  }

  /**
   * The bootstrap is invoked directly because its lifecycle plugin is discovered by scanning the plugin
   * folder, which does not happen on a test classpath.
   */
  private static synchronized void startKettle() throws KettleException, LifecycleException {
    if ( kettleStarted ) {
      return;
    }
    KettleEnvironment.init( false );
    new S3FileSystemBootstrap().onEnvironmentInit();
    kettleStarted = true;
  }
}
