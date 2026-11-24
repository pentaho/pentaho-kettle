package org.pentaho.s3.vfs;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pentaho.amazon.s3.S3Details;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputMeta;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegates;
import org.pentaho.di.ui.spoon.delegates.SpoonJobDelegate;
import org.pentaho.di.ui.spoon.delegates.SpoonTransformationDelegate;
import org.pentaho.di.ui.spoon.trans.TransGraph;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.xml.XmlMetaStore;
import org.pentaho.s3.vfs.support.KettleS3Environment;
import org.pentaho.s3.vfs.support.TestBowl;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * PDI-20647: a saved text-reading transformation is rerun in the same Spoon context after its VFS connection
 * is deleted. The ticket's "TableInput reads a text file" is interpreted as Text File Input.
 * SWT widgets and save-dialog scheduling are mocked; TransGraph.start, Spoon's cache refresh, the local
 * transformation factory, connection manager, file-input step and MinIO are real.
 */
class SpoonConnectionDeletionMinioIT {
  private static final String BUCKET = "pdi20647-" + UUID.randomUUID().toString().substring( 0, 8 );
  private static final String STEP_NAME = "Read PVFS text";
  private static final List<String> EXPECTED_ROWS = List.of( "alpha", "beta" );

  @TempDir
  static Path workDir;

  private static MinIOContainer minio;
  private static KettleS3Environment environment;
  private static LogLevel originalLogLevel;

  @BeforeAll
  static void startMinio() throws Exception {
    originalLogLevel = DefaultLogLevel.getLogLevel();
    minio = new MinIOContainer(
      DockerImageName.parse( "quay.io/minio/minio:latest" ).asCompatibleSubstituteFor( "minio/minio" ) );
    minio.start();
    mc( "alias", "set", "local", "http://localhost:9000", minio.getUserName(), minio.getPassword() );
    mc( "mb", "local/" + BUCKET );
    minio.copyFileToContainer( Transferable.of( "alpha\nbeta\n".getBytes( StandardCharsets.UTF_8 ) ), "/tmp/input.txt" );
    mc( "cp", "/tmp/input.txt", "local/" + BUCKET + "/input.txt" );
    environment = new KettleS3Environment( minio.getS3URL() );
  }

  @AfterAll
  static void stopMinio() {
    try {
      if ( minio != null ) {
        minio.stop();
      }
    } finally {
      DefaultLogLevel.setLogLevel( originalLogLevel );
    }
  }

  @Test
  void pdi20647OptionalTextInputMustFailAfterDeletingItsConnection() throws Exception {
    assertRerunRejectsDeletedConnection( false );
  }

  @Test
  void pdi20647RequiredTextInputRejectsDeletedConnection() throws Exception {
    assertRerunRejectsDeletedConnection( true );
  }

  @Test
  void pdi20647MissingOptionalFileWithValidConnectionRemainsSkippable() throws Exception {
    SavedTransformation fixture = createFixture( false );
    Run missing = run( fixture.graph(), fixture.connection(), "absent.txt" );
    assertFalse( missing.failed(), missing.diagnostics() );
    assertTrue( missing.rows().isEmpty(), "a genuinely absent optional file must remain skippable" );
  }

  private static SavedTransformation createFixture( boolean required ) throws Exception {
    String connection = "pdi20647-" + UUID.randomUUID().toString().substring( 0, 8 );
    IMetaStore metastore = new XmlMetaStore( workDir.resolve( connection ).toString() );
    Bowl bowl = new TestBowl() {
      @Override public IMetaStore getMetastore() {
        return metastore;
      }
    };
    ConnectionManager manager = bowl.getManager( ConnectionManager.class );
    S3Details details = environment.details( connection );
    details.setAccessKey( minio.getUserName() );
    details.setSecretKey( minio.getPassword() );
    assertTrue( manager.save( details ), "precondition: MinIO connection saved" );

    Path filename = workDir.resolve( connection + ".ktr" );
    TransMeta savedMeta = createTransformation( bowl, filename, required );
    save( savedMeta );
    TransMeta openMeta = new TransMeta( bowl, filename.toString() );
    openMeta.clearChanged();
    TransGraph graph = createHeadlessGraph( bowl, metastore, openMeta );
    return new SavedTransformation( connection, manager, openMeta, graph );
  }

  private void assertRerunRejectsDeletedConnection( boolean required ) throws Exception {
    SavedTransformation fixture = createFixture( required );
    Run before = run( fixture.graph(), fixture.connection(), "input.txt" );
    assertFalse( before.failed(), before.diagnostics() );
    assertEquals( EXPECTED_ROWS, before.rows(), "the first execution must actually read the MinIO file" );

    // ConnectionDelegate.delete uses this same manager and marks the open transformation changed.
    fixture.manager().delete( fixture.connection() );
    fixture.openMeta().setChanged();
    assertNull( fixture.manager().getConnectionDetails( fixture.connection() ),
      "precondition: the connection really was deleted" );
    mc( "stat", "local/" + BUCKET + "/input.txt" );

    Run after = run( fixture.graph(), fixture.connection(), "input.txt" );
    assertTrue( after.failed(), "PDI-20647: Spoon rerun succeeded after connection deletion; required=" + required
      + ", rows=" + after.rows() + "\n" + after.diagnostics() );
  }

  private static TransMeta createTransformation( Bowl bowl, Path filename, boolean required ) throws Exception {
    try ( InputStream template = SpoonConnectionDeletionMinioIT.class.getResourceAsStream(
      "/PDI-20647-spoon-read-delete-rerun.ktr" ) ) {
      assertNotNull( template, "PDI-20647 reproduction transformation is required" );
      Files.copy( template, filename );
    }
    TransMeta meta = new TransMeta( bowl, filename.toString() );
    TextFileInputMeta input = (TextFileInputMeta) meta.findStep( STEP_NAME ).getStepMetaInterface();
    input.inputFiles.fileRequired[0] = required ? "Y" : "N";
    return meta;
  }

  private static TransGraph createHeadlessGraph( Bowl bowl, IMetaStore metastore, TransMeta openMeta )
    throws Exception {
    Spoon spoon = mock( Spoon.class );
    setField( spoon, "globalManagementBowl", bowl );
    setField( spoon, "managementBowl", bowl );
    setField( spoon, "executionBowl", bowl );
    setField( spoon, "log", new LogChannel( "PDI-20647 Spoon" ) );
    spoon.delegates = mock( SpoonDelegates.class );
    spoon.delegates.trans = mock( SpoonTransformationDelegate.class );
    spoon.delegates.jobs = mock( SpoonJobDelegate.class );
    when( spoon.delegates.trans.getTransformationList() ).thenReturn( List.of( openMeta ) );
    when( spoon.delegates.jobs.getJobList() ).thenReturn( List.of() );
    when( spoon.getMetaStore() ).thenReturn( metastore );
    doCallRealMethod().when( spoon ).forceRefreshTree();
    doCallRealMethod().when( spoon ).forceRefreshTree( anyBoolean() );
    doCallRealMethod().when( spoon ).clearBowlCaches();

    Shell shell = mock( Shell.class );
    when( shell.getDisplay() ).thenReturn( mock( Display.class ) );
    TransGraph graph = mock( TransGraph.class );
    setField( graph, "spoon", spoon );
    setField( graph, "shell", shell );
    setField( graph, "transMeta", openMeta );
    setField( graph, "log", new LogChannel( "PDI-20647 TransGraph" ) );
    doCallRealMethod().when( graph ).start( any( TransExecutionConfiguration.class ) );
    doCallRealMethod().when( graph ).getTrans();
    doAnswer( invocation -> {
      if ( openMeta.hasChanged() ) {
        save( openMeta );
      }
      return null;
    } ).when( graph ).handleTransMetaChanges( any( TransMeta.class ) );
    return graph;
  }

  private static Run run( TransGraph graph, String connection, String inputFilename ) throws Exception {
    TransExecutionConfiguration config = new TransExecutionConfiguration();
    config.setLogLevel( LogLevel.ERROR );
    config.setClearingLog( false );
    config.getParams().put( "VFS_CONNECTION", connection );
    config.getParams().put( "S3_BUCKET", BUCKET );
    config.getParams().put( "INPUT_FILE", inputFilename );
    graph.start( config );
    Trans trans = graph.getTrans();
    assertNotNull( trans, "Spoon must create a real local transformation" );
    List<String> rows = new ArrayList<>();
    KettleException preparationFailure = null;
    try {
      trans.prepareExecution( null );
      trans.findStepInterface( STEP_NAME, 0 ).addRowListener( new RowAdapter() {
        @Override public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) {
          rows.add( (String) row[0] );
        }
      } );
      trans.startThreads();
      trans.waitUntilFinished();
    } catch ( KettleException failure ) {
      preparationFailure = failure;
    } finally {
      trans.stopAll();
    }
    String logs = KettleLogStore.getAppender().getBuffer( trans.getLogChannelId(), true ).toString();
    return new Run( trans.getErrors(), rows, preparationFailure, logs );
  }

  private static void save( TransMeta meta ) throws Exception {
    String filename = meta.getFilename();
    Path path = filename.startsWith( "file:" ) ? Path.of( URI.create( filename ) ) : Path.of( filename );
    Files.writeString( path, meta.getXML(), StandardCharsets.UTF_8 );
    meta.clearChanged();
  }

  private static void setField( Object target, String name, Object value ) throws Exception {
    Class<?> type = target.getClass();
    while ( type != null ) {
      try {
        Field field = type.getDeclaredField( name );
        field.setAccessible( true );
        field.set( target, value );
        return;
      } catch ( NoSuchFieldException missing ) {
        type = type.getSuperclass();
      }
    }
    throw new NoSuchFieldException( name );
  }

  private static void mc( String... arguments ) throws Exception {
    String[] command = new String[arguments.length + 1];
    command[0] = "mc";
    System.arraycopy( arguments, 0, command, 1, arguments.length );
    Container.ExecResult result = minio.execInContainer( command );
    assertEquals( 0, result.getExitCode(), result.getStderr() );
  }

  private record Run( long errors, List<String> rows, KettleException preparationFailure, String logs ) {
    boolean failed() {
      return errors > 0 || preparationFailure != null;
    }

    String diagnostics() {
      return "errors=" + errors + ", preparationFailure=" + preparationFailure + "\n" + logs;
    }
  }

  private record SavedTransformation( String connection, ConnectionManager manager, TransMeta openMeta,
                                      TransGraph graph ) {
  }
}
