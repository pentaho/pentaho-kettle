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

package org.pentaho.amazon.s3;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.amazon.AmazonS3FileSystemBootstrap;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.bowl.BaseBowl;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.shared.MemorySharedObjectsIO;
import org.pentaho.di.shared.SharedObjectsIO;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.createfile.JobEntryCreateFile;
import org.pentaho.di.job.entries.special.JobEntrySpecial;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.getfilenames.GetFileNamesMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.di.www.Carte;
import org.pentaho.di.www.CarteSingleton;
import org.pentaho.di.www.SlaveServerConfig;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.xml.XmlMetaStore;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.utility.DockerImageName;

import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * PDI-20586 / PDI-20647 probes in a live Carte (PDI-21063 spike). Unlike the in-process lookups of
 * {@code PvfsConnectionIsolationMinioIT}, every request goes through Carte's {@code executeJob} servlet, runs a real
 * job (job entry writes a file) and transformation (Get File Names lists the bucket, Text File Output writes a file)
 * against MinIO, and keeps every Carte-lifetime cache warm between requests: the connection manager, the Commons VFS
 * file-system cache and file caches. Two MinIO users can each access only their own bucket, so stale credentials show
 * up as a real {@code 403}. 11.0 back-port (JUnit 4) of the master test.
 */
public class CarteConnectionCachingMinioIT {

  private static final String SUFFIX = UUID.randomUUID().toString().substring( 0, 8 );
  private static final String BUCKET_A = "carte-a-" + SUFFIX;
  private static final String BUCKET_B = "carte-b-" + SUFFIX;
  private static final String USER_A = "carte-user-a-" + SUFFIX;
  private static final String USER_B = "carte-user-b-" + SUFFIX;
  private static final String SECRET_A = "carte-secret-a-" + SUFFIX;
  private static final String SECRET_B = "carte-secret-b-" + SUFFIX;
  private static final String CARTE_USER = "cluster";
  private static final String CARTE_PASSWORD = "cluster";

  private static MinIOContainer minio;
  // S3Provider.ACCESS_KEY_SECRET_KEY is private on this branch.
  private static final String ACCESS_KEY_SECRET_KEY = "0";
  private static Path workDir;
  private static Carte carte;
  private static int cartePort;
  /** The connection manager of the Carte JVM (default bowl). */
  private static ConnectionManager carteManager;
  /** Another process (Spoon, a deployment) writing the same metastore folder. */
  private static ConnectionManager otherProcessManager;

  @BeforeClass
  public static void startCarte() throws Exception {
    minio = new MinIOContainer(
      DockerImageName.parse( "quay.io/minio/minio:latest" ).asCompatibleSubstituteFor( "minio/minio" ) );
    minio.start();
    mc( "alias", "set", "local", "http://localhost:9000", minio.getUserName(), minio.getPassword() );
    mc( "mb", "local/" + BUCKET_A, "local/" + BUCKET_B );
    createUserForBucket( USER_A, SECRET_A, BUCKET_A );
    createUserForBucket( USER_B, SECRET_B, BUCKET_B );

    KettleEnvironment.init( false );
    new AmazonS3FileSystemBootstrap().onEnvironmentInit();

    workDir = Files.createTempDirectory( "pdi21063-carte" );
    String metastoreFolder = workDir.resolve( "metastore" ).toString();
    IMetaStore carteMetastore = new XmlMetaStore( metastoreFolder );
    DefaultBowl.getInstance().setMetastoreSupplier( () -> carteMetastore );
    DefaultBowl.getInstance().clearCache();
    carteManager = DefaultBowl.getInstance().getManager( ConnectionManager.class );

    IMetaStore otherMetastore = new XmlMetaStore( metastoreFolder );
    SharedObjectsIO sharedObjectsIO = new MemorySharedObjectsIO();
    Bowl otherProcess = new BaseBowl() {
      @Override public IMetaStore getMetastore() {
        return otherMetastore;
      }

      @Override public SharedObjectsIO getSharedObjectsIO() {
        return sharedObjectsIO;
      }

      @Override public VariableSpace getADefaultVariableSpace() {
        return Variables.getADefaultVariableSpace();
      }

      @Override public String getLevelDisplayName() {
        return "other process";
      }
    };
    otherProcessManager = otherProcess.getManager( ConnectionManager.class );

    try ( ServerSocket socket = new ServerSocket( 0 ) ) {
      cartePort = socket.getLocalPort();
    }
    SlaveServer slaveServer =
      new SlaveServer( "pdi21063", "localhost", String.valueOf( cartePort ), CARTE_USER, CARTE_PASSWORD );
    SlaveServerConfig config = new SlaveServerConfig( slaveServer );
    config.setJoining( false );
    config.setAllowedExecuteFileSchemes( java.util.Arrays.asList( "file", "pvfs" ) );
    carte = new Carte( config, false );
  }

  @AfterClass
  public static void stopCarte() {
    if ( carte != null ) {
      carte.getWebServer().stopServer();
    }
    DefaultBowl.getInstance().setMetastoreSupplier( MetaStoreConst.getDefaultMetastoreSupplier() );
    DefaultBowl.getInstance().clearCache();
    if ( minio != null ) {
      minio.stop();
    }
  }

  /**
   * PDI-20586 (Datapipes on Carte): the connection holds {@code ${S3_ACCESS_KEY}} / {@code ${S3_SECRET_KEY}} and each
   * Carte request passes different values. Every request, at job and at transformation level, must use its own
   * values.
   */
  @Test
  public void pdi20586CarteUsesTheCredentialVariablesOfEachRequest() throws Exception {
    String conn = saveConnection( carteManager, "carte-var-", "${S3_ACCESS_KEY}", "${S3_SECRET_KEY}" );
    String job = writeJob( conn );

    Run first = execute( job, USER_A, SECRET_A, BUCKET_A );
    Run second = execute( job, USER_B, SECRET_B, BUCKET_B );
    Run crossed = execute( job, USER_B, SECRET_B, BUCKET_A );
    Run back = execute( job, USER_A, SECRET_A, BUCKET_A );

    assertAllRuns(
      () -> first.assertSucceeded( "request 1 (user A -> bucket A)" ),
      () -> second.assertSucceeded( "request 2 (user B -> bucket B) must not reuse user A's credentials" ),
      () -> crossed.assertFailed( "request 3 (user B -> bucket A) must be denied; success means user A's "
        + "credentials were reused" ),
      () -> back.assertSucceeded( "request 4 (user A -> bucket A) must not reuse user B's credentials" ) );
  }

  /**
   * PDI-20586: a connection edited by another process (Spoon or a deployment writing the shared metastore) must be
   * used by the next Carte request, without restarting Carte.
   */
  @Test
  public void pdi20586CarteUsesConnectionEditedByAnotherProcess() throws Exception {
    String conn = saveConnection( carteManager, "carte-edit-", USER_A, SECRET_A );
    String job = writeJob( conn );

    Run before = execute( job, null, null, BUCKET_A );
    saveConnection( otherProcessManager, conn, USER_B, SECRET_B );
    Run deniedAfterEdit = execute( job, null, null, BUCKET_A );
    Run allowedAfterEdit = execute( job, null, null, BUCKET_B );

    assertAllRuns(
      () -> before.assertSucceeded( "request before the edit (user A -> bucket A)" ),
      () -> deniedAfterEdit.assertFailed( "after the edit to user B, bucket A must be denied; success means "
        + "Carte still used the old (user A) connection" ),
      () -> allowedAfterEdit.assertSucceeded( "after the edit to user B, bucket B must be allowed" ) );
  }

  /**
   * PDI-20647: a connection deleted by another process must not stay usable in Carte.
   */
  @Test
  public void pdi20647CarteRejectsConnectionDeletedByAnotherProcess() throws Exception {
    String conn = saveConnection( carteManager, "carte-del-", USER_A, SECRET_A );
    String job = writeJob( conn );

    Run before = execute( job, null, null, BUCKET_A );
    otherProcessManager.delete( conn );
    Run afterDelete = execute( job, null, null, BUCKET_A );

    assertAllRuns(
      () -> before.assertSucceeded( "request before the delete" ),
      () -> afterDelete.assertFailed( "the connection was deleted, but Carte still executed with it" ) );
  }

  // region fixture

  private static String saveConnection( ConnectionManager manager, String nameOrPrefix, String accessKey,
                                        String secretKey ) throws Exception {
    String name = nameOrPrefix.endsWith( "-" ) ? nameOrPrefix + UUID.randomUUID().toString().substring( 0, 8 )
      : nameOrPrefix;
    S3Details details = new S3Details();
    details.setName( name );
    details.setAuthType( ACCESS_KEY_SECRET_KEY );
    details.setRegion( "us-east-1" );
    details.setEndpoint( minio.getS3URL() );
    details.setPathStyleAccess( "true" );
    details.setSignatureVersion( "AWSS3V4SignerType" );
    details.setAccessKey( accessKey );
    details.setSecretKey( secretKey );
    assertTrue( "precondition: connection " + name + " saved", manager.save( details ) );
    return name;
  }

  /**
   * Job: START -> Create file {@code pvfs://conn/${BUCKET}/job-${RUN}.txt} -> transformation, which lists
   * {@code job-${RUN}.txt} with Get File Names and writes the result to {@code pvfs://conn/${BUCKET}/trans-${RUN}.txt}.
   */
  private static String writeJob( String conn ) throws Exception {
    String bucketUri = "pvfs://" + conn + "/${BUCKET}";

    GetFileNamesMeta list = new GetFileNamesMeta();
    list.setDefault();
    list.allocate( 1 );
    list.setFileName( new String[] { bucketUri } );
    list.setFileMask( new String[] { "job-${RUN}\\.txt" } );
    list.setExcludeFileMask( new String[] { "" } );
    list.setFileRequired( new String[] { "Y" } );
    list.setIncludeSubFolders( new String[] { "N" } );
    list.setFilterFileType( FileInputList.FileTypeFilter.ONLY_FILES );

    TextFileOutputMeta output = new TextFileOutputMeta();
    output.setDefault();
    output.setFileName( bucketUri + "/trans-${RUN}" );
    output.setExtension( "txt" );
    output.setHeaderEnabled( false );
    output.setCreateParentFolder( false );
    output.setOutputFields( new TextFileField[] {
      new TextFileField( "short_filename", ValueMetaInterface.TYPE_STRING, null, -1, -1, null, null, null, "" ) } );

    TransMeta transMeta = new TransMeta();
    transMeta.setName( "pdi21063-carte-list" );
    StepMeta listStep = new StepMeta( "Get File Names", list );
    StepMeta outputStep = new StepMeta( "Text file output", output );
    listStep.setLocation( 100, 100 );
    outputStep.setLocation( 300, 100 );
    listStep.setDraw( true );
    outputStep.setDraw( true );
    transMeta.addStep( listStep );
    transMeta.addStep( outputStep );
    transMeta.addTransHop( new TransHopMeta( listStep, outputStep ) );
    Path ktr = workDir.resolve( conn + ".ktr" );
    Files.write( ktr, transMeta.getXML().getBytes( StandardCharsets.UTF_8 ) );

    JobMeta jobMeta = new JobMeta();
    jobMeta.setName( "pdi21063-carte-" + conn );
    for ( String parameter : new String[] { "S3_ACCESS_KEY", "S3_SECRET_KEY", "BUCKET", "RUN" } ) {
      jobMeta.addParameterDefinition( parameter, "", "" );
    }
    JobEntryCopy start = new JobEntryCopy( withPluginId( new JobEntrySpecial( "START", true, false ) ) );
    JobEntryCreateFile createFile = withPluginId( new JobEntryCreateFile( "Create file" ) );
    createFile.setFilename( bucketUri + "/job-${RUN}.txt" );
    createFile.setFailIfFileExists( false );
    JobEntryCopy createFileCopy = new JobEntryCopy( createFile );
    JobEntryTrans trans = withPluginId( new JobEntryTrans( "List and write" ) );
    trans.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
    trans.setFileName( ktr.toString() );
    JobEntryCopy transCopy = new JobEntryCopy( trans );
    start.setLocation( 100, 100 );
    createFileCopy.setLocation( 300, 100 );
    transCopy.setLocation( 500, 100 );
    for ( JobEntryCopy copy : new JobEntryCopy[] { start, createFileCopy, transCopy } ) {
      copy.setDrawn();
    }
    jobMeta.addJobEntry( start );
    jobMeta.addJobEntry( createFileCopy );
    jobMeta.addJobEntry( transCopy );
    jobMeta.addJobHop( new JobHopMeta( start, createFileCopy ) );
    jobMeta.addJobHop( new JobHopMeta( createFileCopy, transCopy ) );
    Path kjb = workDir.resolve( conn + ".kjb" );
    Files.write( kjb, jobMeta.getXML().getBytes( StandardCharsets.UTF_8 ) );
    return kjb.toString();
  }

  private static <T extends JobEntryBase> T withPluginId( T entry ) {
    entry.setPluginId( PluginRegistry.getInstance().getPluginId( JobEntryPluginType.class, entry ) );
    return entry;
  }

  /** One {@code /kettle/executeJob} request, waiting for the job to finish. */
  private static Run execute( String job, String accessKey, String secretKey, String bucket ) throws Exception {
    String run = UUID.randomUUID().toString().substring( 0, 8 );
    Map<String, String> query = new LinkedHashMap<>();
    query.put( "job", job );
    query.put( "level", "Basic" );
    query.put( "BUCKET", bucket );
    query.put( "RUN", run );
    if ( accessKey != null ) {
      query.put( "S3_ACCESS_KEY", accessKey );
      query.put( "S3_SECRET_KEY", secretKey );
    }
    StringBuilder url = new StringBuilder( "http://localhost:" + cartePort + "/kettle/executeJob/?" );
    for ( Map.Entry<String, String> parameter : query.entrySet() ) {
      url.append( parameter.getKey() ).append( '=' ).append( encode( parameter.getValue() ) ).append( '&' );
    }

    HttpURLConnection http = (HttpURLConnection) URI.create( url.toString() ).toURL().openConnection();
    http.setRequestProperty( "Authorization", "Basic " + Base64.getEncoder()
      .encodeToString( ( CARTE_USER + ":" + CARTE_PASSWORD ).getBytes( StandardCharsets.UTF_8 ) ) );
    int status = http.getResponseCode();
    String body = new String( ( status < 400 ? http.getInputStream() : http.getErrorStream() ).readAllBytes(),
      StandardCharsets.UTF_8 );
    assertEquals( body, 200, status );
    Matcher id = Pattern.compile( "<id>(.*?)</id>" ).matcher( body );
    assertTrue( "Carte did not return a job id: " + body, id.find() );

    Job carteJob = CarteSingleton.getInstance().getJobMap().findJob( id.group( 1 ) );
    assertNotNull( "job " + id.group( 1 ) + " not registered in Carte", carteJob );
    carteJob.waitUntilFinished();
    Result result = carteJob.getResult();
    String log = KettleLogStore.getAppender().getBuffer( carteJob.getLogChannelId(), true ).toString();
    return new Run( bucket, run, result.getNrErrors() == 0 && result.getResult(),
      exists( bucket, "job-" + run + ".txt" ), exists( bucket, "trans-" + run + ".txt" ), log );
  }

  private static String encode( String value ) throws Exception {
    return URLEncoder.encode( value, "UTF-8" );
  }

  /** Runs every check and reports all failures together, like JUnit 5 {@code assertAll}. */
  private static void assertAllRuns( Runnable... checks ) {
    List<String> failures = new ArrayList<>();
    for ( Runnable check : checks ) {
      try {
        check.run();
      } catch ( AssertionError e ) {
        failures.add( e.getMessage() );
      }
    }
    if ( !failures.isEmpty() ) {
      fail( failures.size() + " of " + checks.length + " checks failed:\n" + String.join( "\n", failures ) );
    }
  }

  private static boolean exists( String bucket, String object ) throws Exception {
    return minio.execInContainer( "mc", "stat", "local/" + bucket + "/" + object ).getExitCode() == 0;
  }

  private static final class Run {
    private final String bucket;
    private final String run;
    private final boolean succeeded;
    private final boolean jobFileWritten;
    private final boolean transFileWritten;
    private final String log;

    Run( String bucket, String run, boolean succeeded, boolean jobFileWritten, boolean transFileWritten,
         String log ) {
      this.bucket = bucket;
      this.run = run;
      this.succeeded = succeeded;
      this.jobFileWritten = jobFileWritten;
      this.transFileWritten = transFileWritten;
      this.log = log;
    }

    void assertSucceeded( String scenario ) {
      assertTrue( scenario + ": " + this, succeeded && jobFileWritten && transFileWritten );
    }

    void assertFailed( String scenario ) {
      assertTrue( scenario + ": " + this, !succeeded && !transFileWritten );
    }

    @Override
    public String toString() {
      return "bucket=" + bucket + ", run=" + run + ", jobSucceeded=" + succeeded + ", jobEntryWrote="
        + jobFileWritten + ", transformationWrote=" + transFileWritten + "\n--- Carte job log (errors) ---\n"
        + errorLines( log );
    }

    private static String errorLines( String log ) {
      StringBuilder lines = new StringBuilder();
      String[] all = log.split( "\n" );
      for ( int i = 0; i < all.length; i++ ) {
        if ( all[ i ].contains( "ERROR" ) || all[ i ].contains( "403" ) ) {
          lines.append( all[ i ] ).append( '\n' );
          if ( i + 1 < all.length ) {
            lines.append( all[ i + 1 ] ).append( '\n' );
          }
        }
      }
      return lines.length() == 0 ? "(none)" : lines.toString();
    }
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
      throw new IllegalStateException( String.join( " ", command ) + " failed: " + result.getStderr() );
    }
  }
  // endregion
}
