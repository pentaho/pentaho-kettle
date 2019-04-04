/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.spoon;

import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.apache.commons.vfs2.FileObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import static org.mockito.Mockito.*;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.spoon.delegates.SpoonClustersDelegate;
import org.pentaho.di.ui.spoon.delegates.SpoonDBDelegate;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegates;
import org.pentaho.di.ui.spoon.delegates.SpoonJobDelegate;
import org.pentaho.di.ui.spoon.delegates.SpoonPartitionsDelegate;
import org.pentaho.di.ui.spoon.delegates.SpoonSlaveDelegate;
import org.pentaho.di.ui.spoon.delegates.SpoonTransformationDelegate;


/**
 * SharedObjectSyncUtil tests.
 * 
 */
public class SharedObjectSyncUtilTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final String BEFORE_SYNC_VALUE = "BeforeSync";

  private static final String AFTER_SYNC_VALUE = "AfterSync";

  private static final String SHARED_OBJECTS_FILE = "ram:/shared.xml";

  private SpoonDelegates spoonDelegates;

  private SharedObjectSyncUtil sharedUtil;

  private Spoon spoon;

  private Repository repository;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() {
    spoon = mock( Spoon.class );
    //when( spoon.getRepository() ).thenReturn( spoon.rep );
    spoonDelegates = mock( SpoonDelegates.class );
    spoonDelegates.jobs = new SpoonJobDelegate( spoon );
    spoonDelegates.trans = new SpoonTransformationDelegate( spoon );
    spoonDelegates.db = new SpoonDBDelegate( spoon );
    spoonDelegates.slaves = new SpoonSlaveDelegate( spoon );
    spoonDelegates.partitions = new SpoonPartitionsDelegate( spoon );
    spoonDelegates.clusters = new SpoonClustersDelegate( spoon );
    spoon.delegates = spoonDelegates;
    sharedUtil = new SharedObjectSyncUtil( spoon );
    repository = mock( Repository.class );
  }

  @After
  public void tearDown() throws Exception {
    FileObject sharedObjectsFile = KettleVFS.getFileObject( SHARED_OBJECTS_FILE );
    if ( sharedObjectsFile.exists() ) {
      sharedObjectsFile.delete();
    }
  }

  @Test
  public void synchronizeConnections() throws Exception {
    final String databaseName = "SharedDB";
    DatabaseMeta sharedDB0 = createDatabaseMeta( databaseName, true );
    saveSharedObjects( SHARED_OBJECTS_FILE, sharedDB0 );


    JobMeta job1 = createJobMeta();

    spoonDelegates.jobs.addJob( job1 );
    JobMeta job2 = createJobMeta();
    spoonDelegates.jobs.addJob( job2 );

    DatabaseMeta sharedDB2 = job2.getDatabase( 0 );
    assertEquals( databaseName, sharedDB2.getName() );
    DatabaseMeta sharedDB1 = job1.getDatabase( 0 );
    assertEquals( databaseName, sharedDB1.getName() );
    assertTrue( sharedDB1 != sharedDB2 );

    assertThat( sharedDB1.getHostname(), equalTo( BEFORE_SYNC_VALUE ) );
    sharedDB2.setHostname( AFTER_SYNC_VALUE );
    sharedUtil.synchronizeConnections( sharedDB2, sharedDB2.getName() );

    assertThat( sharedDB1.getHostname(), equalTo( AFTER_SYNC_VALUE ) );
  }

  @Test
  public void synchronizeConnections_sync_shared_only() throws Exception {
    final String databaseName = "DB";
    DatabaseMeta sharedDB0 = createDatabaseMeta( databaseName, true );

    saveSharedObjects( SHARED_OBJECTS_FILE, sharedDB0 );

    JobMeta job1 = createJobMeta();
    DatabaseMeta sharedDB1 = job1.getDatabase( 0 );

    spoonDelegates.jobs.addJob( job1 );

    DatabaseMeta unsharedDB2 = createDatabaseMeta( databaseName, false );
    JobMeta job2 = createJobMeta();
    spoonDelegates.jobs.addJob( job2 );
    job2.removeDatabase( 0 );
    job2.addDatabase( unsharedDB2 );

    JobMeta job3 = createJobMeta();
    DatabaseMeta sharedDB3 = job3.getDatabase( 0 );
    spoonDelegates.jobs.addJob( job3 );
    job3.addDatabase( sharedDB3 );

    sharedDB3.setHostname( AFTER_SYNC_VALUE );
    sharedUtil.synchronizeConnections( sharedDB3, sharedDB3.getName() );
    assertThat( sharedDB1.getHostname(), equalTo( AFTER_SYNC_VALUE ) );
    assertThat( unsharedDB2.getHostname(), equalTo( BEFORE_SYNC_VALUE ) );
  }

  @Test
  public void synchronizeConnections_should_not_sync_unshared() throws Exception {
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
    sharedUtil.synchronizeConnections( db2, db2.getName() );
    assertThat( sharedDB1.getHostname(), equalTo( BEFORE_SYNC_VALUE ) );
  }

  @Test
  public void synchronizeConnections_use_case_sensitive_name() throws Exception {
    JobMeta job1 = createJobMeta();
    DatabaseMeta sharedDB1 = createDatabaseMeta( "DB", true );
    job1.addDatabase( sharedDB1 );
    spoonDelegates.jobs.addJob( job1 );
    DatabaseMeta sharedDB2 = createDatabaseMeta( "Db", true );
    JobMeta job2 = createJobMeta();
    spoonDelegates.jobs.addJob( job2 );
    job2.addDatabase( sharedDB2 );

    sharedDB2.setHostname( AFTER_SYNC_VALUE );
    sharedUtil.synchronizeConnections( sharedDB2, sharedDB2.getName() );
    assertThat( sharedDB1.getHostname(), equalTo( BEFORE_SYNC_VALUE ) );
  }

  @Test
  public void synchronizeConnectionsRename() throws Exception {
    final String databaseName = BEFORE_SYNC_VALUE;
    DatabaseMeta sharedDB0 = createDatabaseMeta( databaseName, true );
    saveSharedObjects( SHARED_OBJECTS_FILE, sharedDB0 );


    JobMeta job1 = createJobMeta();
    spoonDelegates.jobs.addJob( job1 );

    JobMeta job2 = createJobMeta();
    spoonDelegates.jobs.addJob( job2 );

    DatabaseMeta sharedDB2 = job2.getDatabase( 0 );
    assertEquals( databaseName, sharedDB2.getName() );
    DatabaseMeta sharedDB1 = job1.getDatabase( 0 );
    assertEquals( databaseName, sharedDB1.getName() );
    assertTrue( sharedDB1 != sharedDB2 );

    assertThat( sharedDB1.getName(), equalTo( BEFORE_SYNC_VALUE ) );
    sharedDB2.setName( AFTER_SYNC_VALUE );
    sharedUtil.synchronizeConnections( sharedDB2, BEFORE_SYNC_VALUE );

    assertThat( sharedDB1.getName(), equalTo( AFTER_SYNC_VALUE ) );
  }

  @Test
  public void synchronizeConnectionsRenameBackAndForth() throws Exception {
    final String databaseName = "SharedDB";
    DatabaseMeta sharedDB0 = createDatabaseMeta( databaseName, true );
    saveSharedObjects( SHARED_OBJECTS_FILE, sharedDB0 );

    TransMeta t1 = createTransMeta();
    spoonDelegates.trans.addTransformation( t1 );

    TransMeta t2 = createTransMeta();
    spoonDelegates.trans.addTransformation( t2 );

    final String name2 = "NAME 2";
    DatabaseMeta sharedDB1 = t1.getDatabase( 0 );
    sharedDB1.setName( name2 );
    when( spoon.getActiveMeta() ).thenReturn( t1 );
    sharedUtil.synchronizeConnections( sharedDB1, databaseName );

    DatabaseMeta sharedDB2 = t2.getDatabase( 0 );
    assertTrue( sharedDB2.getName().equals( name2 ) );
    when( spoon.getActiveMeta() ).thenReturn( t2 );
    sharedDB2.setName( "name3" );
    sharedUtil.synchronizeConnections( sharedDB2, name2 );
    assertTrue( sharedDB1.getName().equals( sharedDB2.getName() ) );
  }

  @Test
  public void synchronizeSlaveServerRenameBackAndForth() throws Exception {
    final String serverName = "SharedServer";
    SlaveServer server0 = createSlaveServer( serverName, true );
    saveSharedObjects( SHARED_OBJECTS_FILE, server0 );

    JobMeta j1 = createJobMeta();
    spoonDelegates.jobs.addJob( j1 );

    JobMeta j2 = createJobMeta();
    spoonDelegates.jobs.addJob( j2 );

    final String name2 = "NAME 2";
    when( spoon.getActiveMeta() ).thenReturn( j1 );
    SlaveServer server1 = j1.getSlaveServers().get( 0 );
    server1.setName( name2 );
    sharedUtil.synchronizeSlaveServers( server1, serverName );

    SlaveServer server2 = j2.getSlaveServers().get( 0 );
    assertTrue( server2.getName().equals( name2 ) );
    when( spoon.getActiveMeta() ).thenReturn( j2 );
    server2.setName( "name3" );
    sharedUtil.synchronizeSlaveServers( server2, name2 );
    assertTrue( server1.getName().equals( server2.getName() ) );
  }

  @Test
  public void synchronizeSlaveServerRenameRepository() throws Exception {
    try {
      spoon.rep = repository;

      final String objectId = "object-id";
      final String serverName = "SharedServer";

      JobMeta job1 = createJobMeta();
      job1.setRepository( repository );
      job1.setSharedObjects( createSharedObjects( SHARED_OBJECTS_FILE ) );
      SlaveServer server1 = createSlaveServer( serverName, false );
      server1.setObjectId( new StringObjectId( objectId ) );
      job1.addOrReplaceSlaveServer( server1 );
      spoonDelegates.jobs.addJob( job1 );

      JobMeta job2 = createJobMeta();
      job2.setRepository( repository );
      job2.setSharedObjects( createSharedObjects( SHARED_OBJECTS_FILE ) );
      SlaveServer server2 = createSlaveServer( serverName, false );
      server2.setObjectId( new StringObjectId( objectId ) );
      spoonDelegates.jobs.addJob( job2 );

      server2.setName( AFTER_SYNC_VALUE );
      sharedUtil.synchronizeSlaveServers( server2 );
      job2.addOrReplaceSlaveServer( server2 );

      assertEquals( AFTER_SYNC_VALUE, job1.getSlaveServers().get( 0 ).getName() );
    } finally {
      spoon.rep = null;
    }
  }


  @Test
  public void synchronizeSlaveServerDeleteFromRepository() throws Exception {
    try {
      spoon.rep = repository;
      when( spoon.getRepository() ).thenReturn( repository );

      final String objectId = "object-id";
      final String serverName = "SharedServer";

      TransMeta trans = createTransMeta();
      trans.setRepository( repository );
      trans.setSharedObjects( createSharedObjects( SHARED_OBJECTS_FILE ) );
      SlaveServer server1 = createSlaveServer( serverName, false );
      server1.setObjectId( new StringObjectId( objectId ) );
      trans.addOrReplaceSlaveServer( server1 );
      spoon.delegates.trans.addTransformation( trans );

      JobMeta job = createJobMeta();
      job.setRepository( repository );
      job.setSharedObjects( createSharedObjects( SHARED_OBJECTS_FILE ) );
      SlaveServer server3 = createSlaveServer( serverName, false );
      server3.setObjectId( new StringObjectId( objectId ) );
      job.addOrReplaceSlaveServer( server3 );
      spoon.delegates.jobs.addJob( job );

      TransMeta trans2 = createTransMeta();
      trans2.setRepository( repository );
      trans2.setSharedObjects( createSharedObjects( SHARED_OBJECTS_FILE ) );
      SlaveServer server2 = createSlaveServer( serverName, false );
      server2.setObjectId( new StringObjectId( objectId ) );
      trans2.addOrReplaceSlaveServer( server2 );
      spoon.delegates.trans.addTransformation( trans2 );

      assertFalse( trans.getSlaveServers().isEmpty() );
      assertFalse( job.getSlaveServers().isEmpty() );
      spoon.delegates.slaves.delSlaveServer( trans2, server2 );
      verify( repository ).deleteSlave( server2.getObjectId() );

      assertTrue( trans.getSlaveServers().isEmpty() );
      assertTrue( job.getSlaveServers().isEmpty() );
    } finally {
      spoon.rep = null;
      when( spoon.getRepository() ).thenReturn( null );
    }
  }

  @Test
  public void synchronizePartitionSchemasDeleteFromRepository() throws Exception {
    try {
      spoon.rep = repository;
      when( spoon.getRepository() ).thenReturn( repository );

      final String objectId = "object-id";
      final String partitionName = "partsch";

      TransMeta trans1 = createTransMeta();
      trans1.setRepository( repository );
      trans1.setSharedObjects( createSharedObjects( SHARED_OBJECTS_FILE ) );
      PartitionSchema part1 = createPartitionSchema( partitionName, false );
      part1.setObjectId( new StringObjectId( objectId ) );
      trans1.addOrReplacePartitionSchema( part1 );
      spoon.delegates.trans.addTransformation( trans1 );

      TransMeta trans2 = createTransMeta();
      trans2.setRepository( repository );
      trans2.setSharedObjects( createSharedObjects( SHARED_OBJECTS_FILE ) );
      PartitionSchema part2 = createPartitionSchema( partitionName, false );
      part2.setObjectId( new StringObjectId( objectId ) );
      trans2.addOrReplacePartitionSchema( part2 );
      spoon.delegates.trans.addTransformation( trans2 );

      assertFalse( trans1.getPartitionSchemas().isEmpty() );
      spoon.delegates.partitions.delPartitionSchema( trans2, part2 );
      verify( repository ).deletePartitionSchema( part2.getObjectId() );
      assertTrue( trans1.getPartitionSchemas().isEmpty() );
    } finally {
      spoon.rep = null;
      when( spoon.getRepository() ).thenReturn( null );
    }
  }

  @Test
  public void synchronizeConnectionsOpenNew() throws Exception {
    final String databaseName = "SharedDB";
    DatabaseMeta sharedDB0 = createDatabaseMeta( databaseName, true );
    saveSharedObjects( SHARED_OBJECTS_FILE, sharedDB0 );


    JobMeta job1 = createJobMeta();
    spoonDelegates.jobs.addJob( job1 );
    DatabaseMeta sharedDB1 = job1.getDatabase( 0 );

    JobMeta job2 = createJobMeta();
    spoonDelegates.jobs.addJob( job2 );
    DatabaseMeta sharedDB2 = job2.getDatabase( 0 );


    assertThat( sharedDB1.getHostname(), equalTo( BEFORE_SYNC_VALUE ) );
    sharedDB2.setHostname( AFTER_SYNC_VALUE );
    sharedUtil.synchronizeConnections( sharedDB2, sharedDB2.getName() );

    assertThat( sharedDB1.getHostname(), equalTo( AFTER_SYNC_VALUE ) );

    JobMeta job3 = createJobMeta();
    spoonDelegates.jobs.addJob( job3 );
    DatabaseMeta sharedDB3 = job3.getDatabase( 0 );
    assertThat( sharedDB3.getHostname(), equalTo( AFTER_SYNC_VALUE ) );
  }

  @Test
  public void synchronizeSlaveServers() throws Exception {
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
  public void synchronizeSlaveServers_sync_shared_only() throws Exception {
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
  public void synchronizeSlaveServers_should_not_sync_unshared() throws Exception {
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
  public void synchronizeSlaveServers_use_case_sensitive_name() throws Exception {
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
  public void synchronizeSlaveServersRename() throws Exception {
    final String originalName = "slave";
    SlaveServer slaveServer = createSlaveServer( originalName, true );
    saveSharedObjects( SHARED_OBJECTS_FILE, slaveServer );


    JobMeta job1 = createJobMeta();
    spoonDelegates.jobs.addJob( job1 );

    JobMeta job2 = createJobMeta();
    spoonDelegates.jobs.addJob( job2 );

    SlaveServer server1 = job1.getSlaveServers().get( 0 );
    SlaveServer server2 = job2.getSlaveServers().get( 0 );

    assertTrue( server1 != server2 );
    final String newName = "spartacus";
    server1.setName( newName );
    sharedUtil.synchronizeSlaveServers( server1, originalName );

    assertEquals( 1, job1.getSlaveServerNames().length );
    server2 = job2.getSlaveServers().get( 0 );
    assertEquals( newName, server2.getName() );
  }

  @Test
  public void synchronizeClusterSchemas() throws Exception {
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
  public void synchronizeClusterSchemas_sync_shared_only() throws Exception {
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
  public void synchronizeClusterSchemas_should_not_sync_unshared() throws Exception {
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
  public void synchronizeClusterSchemas_use_case_sensitive_name() throws Exception {
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
  public void synchronizePartitionSchemas() throws Exception {
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
  public void synchronizePartitionSchemas_sync_shared_only() throws Exception {
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
  public void synchronizePartitionSchemas_should_not_sync_unshared() throws Exception {
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
  public void synchronizePartitionSchemas_use_case_sensitive_name() throws Exception {
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
  public void synchronizeSteps() throws Exception {
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
  public void synchronizeSteps_sync_shared_only() throws Exception {
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
  public void synchronizeSteps_should_not_sync_unshared() throws Exception {
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
  public void synchronizeSteps_use_case_sensitive_name() throws Exception {
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

  private JobMeta createJobMeta() throws Exception {
    JobMeta jobMeta = new JobMeta();
    jobMeta.setName( UUID.randomUUID().toString() );
    jobMeta.setFilename( UUID.randomUUID().toString() );
    jobMeta.setRepositoryDirectory( mock( RepositoryDirectory.class ) );
//    jobMeta.setSharedObjectsFile( SHARED_OBJECTS_FILE );
    initSharedObjects( jobMeta, SHARED_OBJECTS_FILE );
    when( spoon.getActiveMeta() ).thenReturn( jobMeta );
    return jobMeta;
  }

  private TransMeta createTransMeta() throws KettleException {
    TransMeta transMeta = new TransMeta();
    transMeta.setName( UUID.randomUUID().toString() );
    transMeta.setFilename( UUID.randomUUID().toString() );
    RepositoryDirectory repositoryDirectory = mock( RepositoryDirectory.class );
    doCallRealMethod().when( repositoryDirectory ).setName( anyString() );
    doCallRealMethod().when( repositoryDirectory ).getName();
    transMeta.setRepositoryDirectory( repositoryDirectory );
    initSharedObjects( transMeta, SHARED_OBJECTS_FILE );
    when( spoon.getActiveMeta() ).thenReturn( transMeta );
    return transMeta;
  }

  private static void initSharedObjects( AbstractMeta meta, String sharedObjectsFile ) throws KettleException {
    meta.setSharedObjectsFile( sharedObjectsFile );
    meta.setSharedObjects( meta.readSharedObjects() );
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

  private SharedObjects saveSharedObjects( String location, SharedObjectInterface...objects ) throws Exception {
    SharedObjects sharedObjects = createSharedObjects( location, objects );
    sharedObjects.saveToFile();
    return sharedObjects;
  }

  private static SharedObjects createSharedObjects( String location, SharedObjectInterface... objects )
    throws KettleXMLException {
    SharedObjects sharedObjects = new SharedObjects( location );
    for ( SharedObjectInterface sharedObject : objects ) {
      sharedObjects.storeObject( sharedObject );
    }
    return sharedObjects;
  }

}
