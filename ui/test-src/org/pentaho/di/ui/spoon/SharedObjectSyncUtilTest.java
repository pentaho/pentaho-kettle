package org.pentaho.di.ui.spoon;

import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegates;
import org.pentaho.di.ui.spoon.delegates.SpoonJobDelegate;
import org.pentaho.di.ui.spoon.delegates.SpoonTransformationDelegate;

/**
 * SharedObjectSyncUtil tests.
 * 
 */
public class SharedObjectSyncUtilTest {

  private static final String BEFORE_SYNC_VALUE = "BeforeSync";

  private static final String AFTER_SYNC_VALUE = "AfterSync";

  private SpoonDelegates spoonDelegates;

  private SharedObjectSyncUtil sharedUtil;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() {
    Spoon spoon = mock( Spoon.class );
    spoonDelegates = mock( SpoonDelegates.class );
    spoonDelegates.jobs = new SpoonJobDelegate( spoon );
    spoonDelegates.trans = new SpoonTransformationDelegate( spoon );
    sharedUtil = new SharedObjectSyncUtil( spoonDelegates );
  }

  @Test
  public void synchronizeConnections() {
    final String databaseName = "SharedDB";
    JobMeta job1 = createJobMeta();
    DatabaseMeta sharedDB1 = createDatabaseMeta( databaseName, true );
    job1.addDatabase( sharedDB1 );
    spoonDelegates.jobs.addJob( job1 );
    DatabaseMeta sharedDB2 = createDatabaseMeta( databaseName, true );
    JobMeta job2 = createJobMeta();
    spoonDelegates.jobs.addJob( job2 );
    job2.addDatabase( sharedDB2 );

    sharedDB2.setHostname( AFTER_SYNC_VALUE );
    sharedUtil.synchronizeConnections( sharedDB2 );
    assertThat( sharedDB1.getHostname(), equalTo( AFTER_SYNC_VALUE ) );
  }

  @Test
  public void synchronizeConnections_sync_shared_only() {
    final String databaseName = "DB";
    JobMeta job1 = createJobMeta();
    DatabaseMeta sharedDB1 = createDatabaseMeta( databaseName, true );
    job1.addDatabase( sharedDB1 );
    spoonDelegates.jobs.addJob( job1 );

    DatabaseMeta unsharedDB2 = createDatabaseMeta( databaseName, false );
    JobMeta job2 = createJobMeta();
    spoonDelegates.jobs.addJob( job2 );
    job2.addDatabase( unsharedDB2 );

    DatabaseMeta sharedDB3 = createDatabaseMeta( databaseName, true );
    JobMeta job3 = createJobMeta();
    spoonDelegates.jobs.addJob( job3 );
    job3.addDatabase( sharedDB3 );

    sharedDB3.setHostname( AFTER_SYNC_VALUE );
    sharedUtil.synchronizeConnections( sharedDB3 );
    assertThat( sharedDB1.getHostname(), equalTo( AFTER_SYNC_VALUE ) );
    assertThat( unsharedDB2.getHostname(), equalTo( BEFORE_SYNC_VALUE ) );
  }

  @Test
  public void synchronizeConnections_should_not_sync_unshared() {
    final String databaseName = "DB";
    JobMeta job1 = createJobMeta();
    DatabaseMeta sharedDB1 = createDatabaseMeta( databaseName, true );
    job1.addDatabase( sharedDB1 );
    spoonDelegates.jobs.addJob( job1 );
    DatabaseMeta db2 = createDatabaseMeta( databaseName, false );
    JobMeta job2 = createJobMeta();
    spoonDelegates.jobs.addJob( job2 );
    job2.addDatabase( db2 );

    db2.setHostname( AFTER_SYNC_VALUE );
    sharedUtil.synchronizeConnections( db2 );
    assertThat( sharedDB1.getHostname(), equalTo( BEFORE_SYNC_VALUE ) );
  }

  @Test
  public void synchronizeConnections_use_case_sensitive_name() {
    JobMeta job1 = createJobMeta();
    DatabaseMeta sharedDB1 = createDatabaseMeta( "DB", true );
    job1.addDatabase( sharedDB1 );
    spoonDelegates.jobs.addJob( job1 );
    DatabaseMeta sharedDB2 = createDatabaseMeta( "Db", true );
    JobMeta job2 = createJobMeta();
    spoonDelegates.jobs.addJob( job2 );
    job2.addDatabase( sharedDB2 );

    sharedDB2.setHostname( AFTER_SYNC_VALUE );
    sharedUtil.synchronizeConnections( sharedDB2 );
    assertThat( sharedDB1.getHostname(), equalTo( BEFORE_SYNC_VALUE ) );
  }

  @Test
  public void synchronizeSlaveServers() {
    final String slaveServerName = "SharedSlaveServer";
    JobMeta job1 = createJobMeta();
    SlaveServer slaveServer1 = createSlaveServer( slaveServerName, true );
    job1.setSlaveServers( Collections.singletonList( slaveServer1 ) );
    spoonDelegates.jobs.addJob( job1 );

    JobMeta job2 = createJobMeta();
    SlaveServer slaveServer2 = createSlaveServer( slaveServerName, true );
    job2.setSlaveServers( Collections.singletonList( slaveServer2 ) );
    spoonDelegates.jobs.addJob( job2 );

    slaveServer2.setHostname( AFTER_SYNC_VALUE );
    sharedUtil.synchronizeSlaveServers( slaveServer2 );
    assertThat( slaveServer1.getHostname(), equalTo( AFTER_SYNC_VALUE ) );
  }

  @Test
  public void synchronizeSlaveServers_sync_shared_only() {
    final String slaveServerName = "SlaveServer";
    JobMeta job1 = createJobMeta();
    SlaveServer slaveServer1 = createSlaveServer( slaveServerName, true );
    job1.setSlaveServers( Collections.singletonList( slaveServer1 ) );
    spoonDelegates.jobs.addJob( job1 );

    JobMeta job2 = createJobMeta();
    SlaveServer unsharedSlaveServer2 = createSlaveServer( slaveServerName, false );
    job2.setSlaveServers( Collections.singletonList( unsharedSlaveServer2 ) );
    spoonDelegates.jobs.addJob( job2 );

    JobMeta job3 = createJobMeta();
    SlaveServer slaveServer3 = createSlaveServer( slaveServerName, true );
    job3.setSlaveServers( Collections.singletonList( slaveServer3 ) );
    spoonDelegates.jobs.addJob( job3 );

    slaveServer3.setHostname( AFTER_SYNC_VALUE );
    sharedUtil.synchronizeSlaveServers( slaveServer3 );
    assertThat( slaveServer1.getHostname(), equalTo( AFTER_SYNC_VALUE ) );
    assertThat( unsharedSlaveServer2.getHostname(), equalTo( BEFORE_SYNC_VALUE ) );
  }

  @Test
  public void synchronizeSlaveServers_should_not_sync_unshared() {
    final String slaveServerName = "SlaveServer";
    JobMeta job1 = createJobMeta();
    SlaveServer slaveServer1 = createSlaveServer( slaveServerName, true );
    job1.setSlaveServers( Collections.singletonList( slaveServer1 ) );
    spoonDelegates.jobs.addJob( job1 );

    JobMeta job2 = createJobMeta();
    SlaveServer slaveServer2 = createSlaveServer( slaveServerName, false );
    job2.setSlaveServers( Collections.singletonList( slaveServer2 ) );
    spoonDelegates.jobs.addJob( job2 );

    slaveServer2.setHostname( AFTER_SYNC_VALUE );
    sharedUtil.synchronizeSlaveServers( slaveServer2 );
    assertThat( slaveServer1.getHostname(), equalTo( BEFORE_SYNC_VALUE ) );
  }

  @Test
  public void synchronizeSlaveServers_use_case_sensitive_name() {
    JobMeta job1 = createJobMeta();
    SlaveServer slaveServer1 = createSlaveServer( "SlaveServer", true );
    job1.setSlaveServers( Collections.singletonList( slaveServer1 ) );
    spoonDelegates.jobs.addJob( job1 );

    JobMeta job2 = createJobMeta();
    SlaveServer slaveServer2 = createSlaveServer( "Slaveserver", true );
    job2.setSlaveServers( Collections.singletonList( slaveServer2 ) );
    spoonDelegates.jobs.addJob( job2 );

    slaveServer2.setHostname( AFTER_SYNC_VALUE );
    sharedUtil.synchronizeSlaveServers( slaveServer2 );
    assertThat( slaveServer1.getHostname(), equalTo( BEFORE_SYNC_VALUE ) );
  }

  @Test
  public void synchronizeClusterSchemas() {
    final String clusterSchemaName = "SharedClusterSchema";
    TransMeta transformarion1 = createTransMeta();
    ClusterSchema clusterSchema1 = createClusterSchema( clusterSchemaName, true );
    transformarion1.setClusterSchemas( Collections.singletonList( clusterSchema1 ) );
    spoonDelegates.trans.addTransformation( transformarion1 );

    TransMeta transformarion2 = createTransMeta();
    ClusterSchema clusterSchema2 = createClusterSchema( clusterSchemaName, true );
    transformarion2.setClusterSchemas( Collections.singletonList( clusterSchema2 ) );
    spoonDelegates.trans.addTransformation( transformarion2 );

    clusterSchema2.setDynamic( true );
    sharedUtil.synchronizeClusterSchemas( clusterSchema2 );
    assertThat( clusterSchema1.isDynamic(), equalTo( true ) );
  }

  @Test
  public void synchronizeClusterSchemas_sync_shared_only() {
    final String clusterSchemaName = "ClusterSchema";
    TransMeta transformarion1 = createTransMeta();
    ClusterSchema clusterSchema1 = createClusterSchema( clusterSchemaName, true );
    transformarion1.setClusterSchemas( Collections.singletonList( clusterSchema1 ) );
    spoonDelegates.trans.addTransformation( transformarion1 );

    TransMeta transformarion2 = createTransMeta();
    ClusterSchema unsharedClusterSchema2 = createClusterSchema( clusterSchemaName, false );
    transformarion2.setClusterSchemas( Collections.singletonList( unsharedClusterSchema2 ) );
    spoonDelegates.trans.addTransformation( transformarion2 );

    TransMeta transformarion3 = createTransMeta();
    ClusterSchema clusterSchema3 = createClusterSchema( clusterSchemaName, true );
    transformarion3.setClusterSchemas( Collections.singletonList( clusterSchema3 ) );
    spoonDelegates.trans.addTransformation( transformarion3 );

    clusterSchema3.setDynamic( true );
    sharedUtil.synchronizeClusterSchemas( clusterSchema3 );
    assertThat( clusterSchema1.isDynamic(), equalTo( true ) );
    assertThat( unsharedClusterSchema2.isDynamic(), equalTo( false ) );
  }

  @Test
  public void synchronizeClusterSchemas_should_not_sync_unshared() {
    final String clusterSchemaName = "ClusterSchema";
    TransMeta transformarion1 = createTransMeta();
    ClusterSchema clusterSchema1 = createClusterSchema( clusterSchemaName, true );
    transformarion1.setClusterSchemas( Collections.singletonList( clusterSchema1 ) );
    spoonDelegates.trans.addTransformation( transformarion1 );

    TransMeta transformarion2 = createTransMeta();
    ClusterSchema clusterSchema2 = createClusterSchema( clusterSchemaName, false );
    transformarion2.setClusterSchemas( Collections.singletonList( clusterSchema2 ) );
    spoonDelegates.trans.addTransformation( transformarion2 );

    clusterSchema2.setDynamic( true );
    sharedUtil.synchronizeClusterSchemas( clusterSchema2 );
    assertThat( clusterSchema1.isDynamic(), equalTo( false ) );
  }

  @Test
  public void synchronizeClusterSchemas_use_case_sensitive_name() {
    TransMeta transformarion1 = createTransMeta();
    ClusterSchema clusterSchema1 = createClusterSchema( "ClusterSchema", true );
    transformarion1.setClusterSchemas( Collections.singletonList( clusterSchema1 ) );
    spoonDelegates.trans.addTransformation( transformarion1 );

    TransMeta transformarion2 = createTransMeta();
    ClusterSchema clusterSchema2 = createClusterSchema( "Clusterschema", true );
    transformarion2.setClusterSchemas( Collections.singletonList( clusterSchema2 ) );
    spoonDelegates.trans.addTransformation( transformarion2 );

    clusterSchema2.setDynamic( true );
    sharedUtil.synchronizeClusterSchemas( clusterSchema2 );
    assertThat( clusterSchema1.isDynamic(), equalTo( false ) );
  }

  @Test
  public void synchronizePartitionSchemas() {
    final String partitionSchemaName = "SharedPartitionSchema";
    TransMeta transformarion1 = createTransMeta();
    PartitionSchema partitionSchema1 = createPartitionSchema( partitionSchemaName, true );
    transformarion1.setPartitionSchemas( Collections.singletonList( partitionSchema1 ) );
    spoonDelegates.trans.addTransformation( transformarion1 );

    TransMeta transformarion2 = createTransMeta();
    PartitionSchema partitionSchema2 = createPartitionSchema( partitionSchemaName, true );
    transformarion2.setPartitionSchemas( Collections.singletonList( partitionSchema2 ) );
    spoonDelegates.trans.addTransformation( transformarion2 );

    partitionSchema2.setNumberOfPartitionsPerSlave( AFTER_SYNC_VALUE );
    sharedUtil.synchronizePartitionSchemas( partitionSchema2 );
    assertThat( partitionSchema1.getNumberOfPartitionsPerSlave(), equalTo( AFTER_SYNC_VALUE ) );
  }

  @Test
  public void synchronizePartitionSchemas_sync_shared_only() {
    final String partitionSchemaName = "PartitionSchema";
    TransMeta transformarion1 = createTransMeta();
    PartitionSchema partitionSchema1 = createPartitionSchema( partitionSchemaName, true );
    transformarion1.setPartitionSchemas( Collections.singletonList( partitionSchema1 ) );
    spoonDelegates.trans.addTransformation( transformarion1 );

    TransMeta transformarion2 = createTransMeta();
    PartitionSchema unsharedPartitionSchema2 = createPartitionSchema( partitionSchemaName, false );
    transformarion2.setPartitionSchemas( Collections.singletonList( unsharedPartitionSchema2 ) );
    spoonDelegates.trans.addTransformation( transformarion2 );

    TransMeta transformarion3 = createTransMeta();
    PartitionSchema partitionSchema3 = createPartitionSchema( partitionSchemaName, true );
    transformarion3.setPartitionSchemas( Collections.singletonList( partitionSchema3 ) );
    spoonDelegates.trans.addTransformation( transformarion3 );

    partitionSchema3.setNumberOfPartitionsPerSlave( AFTER_SYNC_VALUE );
    sharedUtil.synchronizePartitionSchemas( partitionSchema3 );
    assertThat( partitionSchema1.getNumberOfPartitionsPerSlave(), equalTo( AFTER_SYNC_VALUE ) );
    assertThat( unsharedPartitionSchema2.getNumberOfPartitionsPerSlave(), equalTo( BEFORE_SYNC_VALUE ) );
  }

  @Test
  public void synchronizePartitionSchemas_should_not_sync_unshared() {
    final String partitionSchemaName = "PartitionSchema";
    TransMeta transformarion1 = createTransMeta();
    PartitionSchema partitionSchema1 = createPartitionSchema( partitionSchemaName, true );
    transformarion1.setPartitionSchemas( Collections.singletonList( partitionSchema1 ) );
    spoonDelegates.trans.addTransformation( transformarion1 );

    TransMeta transformarion2 = createTransMeta();
    PartitionSchema partitionSchema2 = createPartitionSchema( partitionSchemaName, false );
    transformarion2.setPartitionSchemas( Collections.singletonList( partitionSchema2 ) );
    spoonDelegates.trans.addTransformation( transformarion2 );

    partitionSchema2.setNumberOfPartitionsPerSlave( AFTER_SYNC_VALUE );
    sharedUtil.synchronizePartitionSchemas( partitionSchema2 );
    assertThat( partitionSchema1.getNumberOfPartitionsPerSlave(), equalTo( BEFORE_SYNC_VALUE ) );
  }

  @Test
  public void synchronizePartitionSchemas_use_case_sensitive_name() {
    TransMeta transformarion1 = createTransMeta();
    PartitionSchema partitionSchema1 = createPartitionSchema( "PartitionSchema", true );
    transformarion1.setPartitionSchemas( Collections.singletonList( partitionSchema1 ) );
    spoonDelegates.trans.addTransformation( transformarion1 );

    TransMeta transformarion2 = createTransMeta();
    PartitionSchema partitionSchema2 = createPartitionSchema( "Partitionschema", true );
    transformarion2.setPartitionSchemas( Collections.singletonList( partitionSchema2 ) );
    spoonDelegates.trans.addTransformation( transformarion2 );

    partitionSchema2.setNumberOfPartitionsPerSlave( AFTER_SYNC_VALUE );
    sharedUtil.synchronizePartitionSchemas( partitionSchema2 );
    assertThat( partitionSchema1.getNumberOfPartitionsPerSlave(), equalTo( BEFORE_SYNC_VALUE ) );
  }

  @Test
  public void synchronizeSteps() {
    final String stepName = "SharedStep";
    TransMeta transformarion1 = createTransMeta();
    StepMeta step1 = createStepMeta( stepName, true );
    transformarion1.addStep( step1 );
    spoonDelegates.trans.addTransformation( transformarion1 );

    TransMeta transformarion2 = createTransMeta();
    StepMeta step2 = createStepMeta( stepName, true );
    transformarion2.addStep( step2 );
    spoonDelegates.trans.addTransformation( transformarion2 );

    step2.setDescription( AFTER_SYNC_VALUE );
    sharedUtil.synchronizeSteps( step2 );
    assertThat( step1.getDescription(), equalTo( AFTER_SYNC_VALUE ) );
  }

  @Test
  public void synchronizeSteps_sync_shared_only() {
    final String stepName = "Step";
    TransMeta transformarion1 = createTransMeta();
    StepMeta step1 = createStepMeta( stepName, true );
    transformarion1.addStep( step1 );
    spoonDelegates.trans.addTransformation( transformarion1 );

    TransMeta transformarion2 = createTransMeta();
    StepMeta unsharedStep2 = createStepMeta( stepName, false );
    transformarion2.addStep( unsharedStep2 );
    spoonDelegates.trans.addTransformation( transformarion2 );

    TransMeta transformarion3 = createTransMeta();
    StepMeta step3 = createStepMeta( stepName, true );
    transformarion3.addStep( step3 );
    spoonDelegates.trans.addTransformation( transformarion3 );

    step3.setDescription( AFTER_SYNC_VALUE );
    sharedUtil.synchronizeSteps( step3 );
    assertThat( step1.getDescription(), equalTo( AFTER_SYNC_VALUE ) );
    assertThat( unsharedStep2.getDescription(), equalTo( BEFORE_SYNC_VALUE ) );
  }

  @Test
  public void synchronizeSteps_should_not_sync_unshared() {
    final String stepName = "Step";
    TransMeta transformarion1 = createTransMeta();
    StepMeta step1 = createStepMeta( stepName, true );
    transformarion1.addStep( step1 );
    spoonDelegates.trans.addTransformation( transformarion1 );

    TransMeta transformarion2 = createTransMeta();
    StepMeta step2 = createStepMeta( stepName, false );
    transformarion2.addStep( step2 );
    spoonDelegates.trans.addTransformation( transformarion2 );

    step2.setDescription( AFTER_SYNC_VALUE );
    sharedUtil.synchronizeSteps( step2 );
    assertThat( step1.getDescription(), equalTo( BEFORE_SYNC_VALUE ) );
  }

  @Test
  public void synchronizeSteps_use_case_sensitive_name() {
    TransMeta transformarion1 = createTransMeta();
    StepMeta step1 = createStepMeta( "STEP", true );
    transformarion1.addStep( step1 );
    spoonDelegates.trans.addTransformation( transformarion1 );

    TransMeta transformarion2 = createTransMeta();
    StepMeta step2 = createStepMeta( "Step", true );
    transformarion2.addStep( step2 );
    spoonDelegates.trans.addTransformation( transformarion2 );

    step2.setDescription( AFTER_SYNC_VALUE );
    sharedUtil.synchronizeSteps( step2 );
    assertThat( step1.getDescription(), equalTo( BEFORE_SYNC_VALUE ) );
  }

  private static JobMeta createJobMeta() {
    JobMeta jobMeta = new JobMeta();
    jobMeta.setName( UUID.randomUUID().toString() );
    jobMeta.setFilename( UUID.randomUUID().toString() );
    jobMeta.setRepositoryDirectory( mock( RepositoryDirectory.class ) );
    return jobMeta;
  }

  private static TransMeta createTransMeta() {
    TransMeta transMeta = new TransMeta();
    transMeta.setName( UUID.randomUUID().toString() );
    transMeta.setFilename( UUID.randomUUID().toString() );
    RepositoryDirectory repositoryDirectory = mock( RepositoryDirectory.class );
    doCallRealMethod().when( repositoryDirectory ).setName( anyString() );
    doCallRealMethod().when( repositoryDirectory ).getName();
    transMeta.setRepositoryDirectory( repositoryDirectory );
    return transMeta;
  }

  private static StepMeta createStepMeta( String name, boolean shared ) {
    StepMeta stepMeta = new StepMeta();
    stepMeta.setName( name );
    stepMeta.setDescription( BEFORE_SYNC_VALUE );
    stepMeta.setShared( shared );
    return stepMeta;
  }

  private static PartitionSchema createPartitionSchema( String name, boolean shared ) {
    PartitionSchema partitionSchema = new PartitionSchema();
    partitionSchema.setName( name );
    partitionSchema.setNumberOfPartitionsPerSlave( BEFORE_SYNC_VALUE );
    partitionSchema.setShared( shared );
    return partitionSchema;
  }

  private static SlaveServer createSlaveServer( String name, boolean shared ) {
    SlaveServer slaveServer = new SlaveServer();
    slaveServer.setHostname( BEFORE_SYNC_VALUE );
    slaveServer.setName( name );
    slaveServer.setShared( shared );
    return slaveServer;
  }

  private static ClusterSchema createClusterSchema( String name, boolean shared ) {
    ClusterSchema clusterSchema = new ClusterSchema();
    clusterSchema.setName( name );
    clusterSchema.setDescription( BEFORE_SYNC_VALUE );
    clusterSchema.setDynamic( false );
    clusterSchema.setShared( shared );
    return clusterSchema;
  }

  private static DatabaseMeta createDatabaseMeta( String name, boolean shared ) {
    DatabaseMeta database = new DatabaseMeta();
    database.setName( name );
    database.setShared( shared );
    database.setHostname( BEFORE_SYNC_VALUE );
    return database;
  }

}
