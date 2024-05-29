/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.repository.pur;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.repository2.ClientRepositoryPaths;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.util.Assert.assertNull;

@RunWith( MockitoJUnitRunner.class )
public class PurRepositoryStressTest {
  private static MockedStatic mockedClientRepositoryPaths;
  private static MockedStatic mockedEncr;
  private static MockedStatic mockedAttributesMapUtil;
  private static MockedStatic mockedDBCache;

  private final int THREADS = 15;
  private final int SAMPLES = 250;
  private final int TIMEOUT = 30;
  private final int METHOD_CALLS_BY_SAMPLE = 27;

  private static PurRepository purRepository;// = new PurRepository();
  private IUnifiedRepository mockRepo;
  private RepositoryConnectResult result;
  private IRepositoryConnector connector;
  private PurRepositoryMeta mockMeta;
  private RepositoryFile mockRootFolder;
  private IUser user;

  private List<Method> testLockMethods = Arrays.asList( PurRepositoryStressTest.class.getMethods() )
    .stream()
    .filter( line -> line.getName().startsWith( "testLock" ) )
    .collect( Collectors.toList() );

  private PurRepositoryStressTest obj;

  public static void setupStaticMocks() {
    //Static mocks have to be setup for each thread in the test
    mockedClientRepositoryPaths = mockStatic( ClientRepositoryPaths.class );
    mockedClientRepositoryPaths.when( () -> ClientRepositoryPaths.getUserHomeFolderPath( any() ) ).thenReturn( "/" );
    mockedClientRepositoryPaths.when( () -> ClientRepositoryPaths.getEtcFolderPath() ).thenReturn( "/test" );

    //-- testLockLoadSlaveServer
    mockedEncr = mockStatic( Encr.class );
    mockedEncr.when( () -> Encr.decryptPasswordOptionallyEncrypted( anyString() ) ).thenReturn( "pass" );

    mockedAttributesMapUtil = mockStatic( AttributesMapUtil.class );
    mockedDBCache = mockStatic( DBCache.class );
  }

  @AfterClass
  public static void closeDown() {
    mockedClientRepositoryPaths.close();
    mockedEncr.close();
    mockedAttributesMapUtil.close();
    mockedDBCache.close();
  }

  public void setUpTest( boolean threadReused ) throws Exception {
    setupStaticMocks( );
    // -- Common
    this.purRepository = new PurRepository();
    this.mockRepo = mock( IUnifiedRepository.class );
    this.result = mock( RepositoryConnectResult.class );
    when( result.getUnifiedRepository() ).thenReturn( mockRepo );
    this.connector = mock( IRepositoryConnector.class );
    when( connector.connect( anyString(), anyString() ) ).thenReturn( result );
    this.user = mock( IUser.class );
    when( result.getUser() ).thenReturn( user );

    this.mockMeta = mock( PurRepositoryMeta.class );

    purRepository.init( mockMeta );
    purRepository.setPurRepositoryConnector( connector );

    this.mockRootFolder = mock( RepositoryFile.class );
    when( mockRootFolder.getId() ).thenReturn( "/" );
    when( mockRootFolder.getPath() ).thenReturn( "/" );
    when( mockRepo.getFile( "/" ) ).thenReturn( mockRootFolder );

    // -- testLocksSaveRepositoryDirectory
    RepositoryFile repFile = mock( RepositoryFile.class );
    doReturn( "id2" ).when( repFile ).getId();
    doReturn( repFile ).when( mockRepo )
      .createFolder( any( Serializable.class ), any( RepositoryFile.class ), any() );

    // -- testLocksIsUserHomeDirectory
    RepositoryFile folder1 = mock( RepositoryFile.class );
    doReturn( "/folder1/folder2/" ).when( folder1 ).getPath();
    doReturn( "folder2" ).when( folder1 ).getName();
    doReturn( folder1 ).when( mockRepo ).getFileById( any( Serializable.class ) );

    //-- testLocksDeleteRepositoryDirectory
    when( user.getLogin() ).thenReturn( "user1" );
    purRepository.connect( "TEST_USER", "TEST_PASSWORD" );

    //-- testLocksGetDirectoryNames
    List<RepositoryFile> children = new ArrayList<RepositoryFile>();
    doReturn( children ).when( mockRepo ).getChildren( any( Serializable.class ) );

    //-- testLocksGetObjectId
    String bdPath = "/test/pdi/databases";
    when( mockRepo.getFile( bdPath ) ).thenReturn( mockRootFolder );

    //-- testLockDeleteDatabaseMeta
    String bdMetaFile = "/test/pdi/databases/dbName.kdb";
    RepositoryFile folderBdMeta = mock( RepositoryFile.class );
    when( mockRepo.getFile( bdMetaFile ) ).thenReturn( folderBdMeta );

    String partitionSchemas = "/test/pdi/partitionSchemas";
    RepositoryFile folderPartitionSchemas = mock( RepositoryFile.class );
    when( mockRepo.getFile( partitionSchemas ) ).thenReturn( folderPartitionSchemas );

    String slaveServers = "/test/pdi/slaveServers";
    RepositoryFile folderSlaveServers = mock( RepositoryFile.class );
    when( mockRepo.getFile( slaveServers ) ).thenReturn( folderSlaveServers );

    String clusterSchemas = "/test/pdi/clusterSchemas";
    RepositoryFile folderClusterSchemas = mock( RepositoryFile.class );
    doReturn( "cschemas" ).when( folderBdMeta ).getId();
    doReturn( folderClusterSchemas ).when( mockRepo ).getFile( clusterSchemas );

    List<RepositoryFile> childrenRes = new ArrayList<RepositoryFile>();
    when( mockRepo.getChildren( any( Serializable.class ), anyString() ) ).thenReturn( childrenRes );

    //-- testLockLoadClusterSchema
    RepositoryFile folderLoad = mock( RepositoryFile.class );
    when( folderLoad.getTitle() ).thenReturn( "titleFolderLoad" );
    when( folderLoad.getId() ).thenReturn( "idFolderLoad" );
    when( mockRepo.getFileAtVersion( anyString(), eq( "v1" ) ) ).thenReturn( folderLoad );

    DataProperty dataNodeProp = mock( DataProperty.class );
    when( dataNodeProp.getBoolean() ).thenReturn( false );
    when( dataNodeProp.getLong() ).thenReturn( 0L );

    DataNode dataNodeRes = mock( DataNode.class );
    when( dataNodeRes.getProperty( anyString() ) ).thenReturn( dataNodeProp );

    DataNode stepsNode = mock( DataNode.class );
    when( stepsNode.getNodes() ).thenReturn( new ArrayList<DataNode>() );
    when( stepsNode.getProperty( anyString() ) ).thenReturn( dataNodeProp );

    DataNode dataNode = mock( DataNode.class );
    when( dataNode.getProperty( anyString() ) ).thenReturn( dataNodeProp );
    when( dataNode.getNode( anyString() ) ).thenReturn( dataNodeRes );
    when( dataNode.getNode( eq( "transPrivateDatabases" ) ) ).thenReturn( null );
    when( dataNode.getNode( eq( "steps" ) ) ).thenReturn( stepsNode );
    when( dataNode.getNode( eq( "notes" ) ) ).thenReturn( stepsNode );
    when( dataNode.getNode( eq( "hops" ) ) ).thenReturn( stepsNode );
    when( dataNode.getNode( eq( "jobPrivateDatabases" ) ) ).thenReturn( null );
    when( dataNode.getNode( eq( "entries" ) ) ).thenReturn( stepsNode );

    NodeRepositoryFileData modeRepoFileData = mock( NodeRepositoryFileData.class );
    when( modeRepoFileData.getNode() ).thenReturn( dataNode );
    doReturn( modeRepoFileData ).when( mockRepo ).getDataAtVersionForRead(
      any(), AdditionalMatchers.or( eq( "v1" ), eq( "v3" ) ), eq( NodeRepositoryFileData.class ) );

    VersionSummary vSummary = mock( VersionSummary.class );
    when( vSummary.getId() ).thenReturn( mock( Serializable.class ) );
    when( vSummary.getAuthor() ).thenReturn( "author" );
    when( vSummary.getDate() ).thenReturn( mock( Date.class ) );
    when( vSummary.getMessage() ).thenReturn( "message" );

    doReturn( vSummary ).when( mockRepo ).getVersionSummary( any(), any() );

    //-- testLockLoadTransformation
    String transfFile = "/folder1/folder2/transName.ktr";
    RepositoryFile transfFileMeta = mock( RepositoryFile.class );
    when( transfFileMeta.getId() ).thenReturn( "transName" );
    when( mockRepo.getFile( transfFile ) ).thenReturn( transfFileMeta );

    //-- testLockLoadJob
    String jobFile = "/folder1/folder2/jobName.kjb";
    RepositoryFile jobFileMeta = mock( RepositoryFile.class );
    doReturn( "jobName" ).when( jobFileMeta ).getId();
    doReturn( jobFileMeta ).when( mockRepo ).getFile( jobFile );

    //-- testLockSaveClusterSchema
    RepositoryFile clusterSchemaFile = mock( RepositoryFile.class );
    doReturn( "clusterSchemaFile" ).when( clusterSchemaFile ).getId();
    doReturn( clusterSchemaFile ).when( mockRepo )
      .updateFile( any( RepositoryFile.class ), any( IRepositoryFileData.class ), anyString() );

    //-- testLockGetObjectInformation
    doReturn( null ).when( mockRepo ).getFileById( "idnull" );

    //--- testLockLoadJob2 ---
    RepositoryFile folderLoad2 = mock( RepositoryFile.class );
    when( folderLoad2.getTitle() ).thenReturn( "titleFolderLoad" );
    when( folderLoad2.getId() ).thenReturn( "idFolderLoad" );
    when( folderLoad2.getPath() ).thenReturn( "/" );
    when( mockRepo.getFileAtVersion( any(), eq( "v3" ) ) ).thenReturn( folderLoad2 );

    purRepository.loadAndCacheSharedObjects( true );
  }

  public void testLocksSaveRepositoryDirectory() throws Exception {
    RepositoryDirectoryInterface childdi = mock( RepositoryDirectoryInterface.class );
    RepositoryDirectoryInterface parentdi = mock( RepositoryDirectoryInterface.class );
    ObjectId objId = mock( ObjectId.class );

    doReturn( "id1" ).when( objId ).getId();
    doReturn( objId ).when( parentdi ).getObjectId();
    doReturn( "/test1" ).when( parentdi ).getName();
    doReturn( parentdi ).when( childdi ).getParent();
    doReturn( "/test2" ).when( childdi ).getName();

    purRepository.saveRepositoryDirectory( childdi );
    verify( childdi, times( 1 ) ).setObjectId( any( StringObjectId.class ) );
  }

  public void testLocksIsUserHomeDirectory() {
    RepositoryFile repFile = mock( RepositoryFile.class );
    assertFalse( purRepository.isUserHomeDirectory( repFile ) );
  }

  public void testLocksDeleteRepositoryDirectory() throws Exception {
    ObjectId objId = mock( ObjectId.class );
    doReturn( "id1" ).when( objId ).getId();

    RepositoryDirectoryInterface repFile = mock( RepositoryDirectoryInterface.class );
    doReturn( objId ).when( repFile ).getObjectId();

    purRepository.deleteRepositoryDirectory( repFile, true );
    verify( repFile, times( 2 ) ).getObjectId();
  }

  public void testLocksRenameRepositoryDirectory() throws Exception {
    ObjectId objId = mock( ObjectId.class );
    doReturn( "id1" ).when( objId ).getId();

    purRepository.renameRepositoryDirectory( objId, null, "newName", true );
    verify( objId, times( 2 ) ).getId();
  }

  public void testLocksGetDirectoryNames() throws Exception {
    ObjectId objId = mock( ObjectId.class );
    doReturn( "id1" ).when( objId ).getId();

    assertNotNull( purRepository.getDirectoryNames( objId ) );
    verify( objId, times( 1 ) ).getId();
  }

  public void testLocksDeleteFileById() throws Exception {
    ObjectId objId = mock( ObjectId.class );
    doReturn( "id1" ).when( objId ).getId();

    purRepository.deleteFileById( objId );
    verify( objId, times( 1 ) ).getId();
  }

  public void testLocksExists() throws Exception {
    assertFalse( purRepository.exists( "name", null, null ) );
  }

  public void testLocksReadDatabases() throws Exception {
    // it doesn't test directly readDatabase locks, but it tests other locks that are
    // on other methods called by readDatabases method
    assertNotNull( purRepository.readDatabases() );
  }

  public void testLockDeleteDatabaseMeta() throws Exception {
    // can't assert anything here. if the method doesn't throw any exception,
    // it means that all locks were released
    purRepository.deleteDatabaseMeta( "dbName" );
  }

  public void testLockGetTransformationObjects() throws Exception {
    ObjectId objId = mock( ObjectId.class );
    doReturn( "id1" ).when( objId ).getId();

    assertNotNull( purRepository.getTransformationObjects( objId, false ) );
    verify( objId, times( 2 ) ).getId();
  }

  public void testLockLoadClusterSchema() throws Exception {
    ObjectId objId = mock( ObjectId.class );
    doReturn( "id1" ).when( objId ).getId();

    assertNotNull( purRepository.loadClusterSchema( objId, mock( List.class ), "v1" ) );
    verify( objId, times( 3 ) ).getId();
  }

  public void testLockLoadPartitionSchema() throws Exception {
    ObjectId objId = mock( ObjectId.class );
    doReturn( "id1" ).when( objId ).getId();

    assertNotNull( purRepository.loadPartitionSchema( objId, "v1" ) );
    verify( objId, times( 3 ) ).getId();
  }

  public void testLockLoadSlaveServer() throws Exception {
    ObjectId objId = mock( ObjectId.class );
    doReturn( "id1" ).when( objId ).getId();

    assertNotNull( purRepository.loadSlaveServer( objId, "v1" ) );
    verify( objId, times( 3 ) ).getId();
  }

  public void testLockRenameTransformation() throws Exception {
    ObjectId objId = mock( ObjectId.class );
    doReturn( "id1" ).when( objId ).getId();
    RepositoryDirectoryInterface rdi = mock( RepositoryDirectoryInterface.class );

    assertNotNull( purRepository.renameTransformation( objId, "vComment", rdi, "newTransName" ) );
    verify( objId, times( 2 ) ).getId();
  }

  public void testLockLoadTransformation() throws Exception {
    ObjectId parentObjId = mock( ObjectId.class );
    doReturn( "parentid1" ).when( parentObjId ).getId();

    RepositoryDirectoryInterface parentRepFile = mock( RepositoryDirectoryInterface.class );
    doReturn( parentObjId ).when( parentRepFile ).getObjectId();
    doReturn( "/folder1/folder2/" ).when( parentRepFile ).getPath();

    assertNotNull(
      purRepository.loadTransformation( "transName", parentRepFile, mock( ProgressMonitorListener.class ), false,
        "v1" ) );
  }

  public void testLockLoadJob() throws Exception {
    ObjectId parentObjId = mock( ObjectId.class );
    doReturn( "parentid1" ).when( parentObjId ).getId();

    RepositoryDirectoryInterface parentRepFile = mock( RepositoryDirectoryInterface.class );
    doReturn( parentObjId ).when( parentRepFile ).getObjectId();
    doReturn( "/folder1/folder2/" ).when( parentRepFile ).getPath();

    assertNotNull( purRepository.loadJob( "jobName", parentRepFile, mock( ProgressMonitorListener.class ), "v1" ) );
  }

  public void testLockSaveClusterSchema() throws Exception {
    ObjectId objId = mock( ObjectId.class );
    doReturn( "id1" ).when( objId ).getId();

    ClusterSchema rei = mock( ClusterSchema.class );
    doReturn( RepositoryObjectType.CLUSTER_SCHEMA ).when( rei ).getRepositoryElementType();
    doReturn( "clusterID" ).when( rei ).getName();
    doReturn( objId ).when( rei ).getObjectId();
    doReturn( rei ).when( rei ).clone();
    Calendar calendar = mock( Calendar.class );

    purRepository.save( rei, "vComment", calendar, mock( ProgressMonitorListener.class ), false );
  }

  public void testLockSavePartitionSchema() throws Exception {
    ObjectId objId = mock( ObjectId.class );
    doReturn( "id1" ).when( objId ).getId();

    PartitionSchema rei = mock( PartitionSchema.class );
    doReturn( RepositoryObjectType.PARTITION_SCHEMA ).when( rei ).getRepositoryElementType();
    doReturn( "partitionID" ).when( rei ).getName();
    doReturn( objId ).when( rei ).getObjectId();
    doReturn( rei ).when( rei ).clone();
    Calendar calendar = mock( Calendar.class );

    purRepository.save( rei, "vComment", calendar, mock( ProgressMonitorListener.class ), false );
  }

  public void testLockSaveSlaverSchema() throws Exception {
    ObjectId objId = mock( ObjectId.class );
    doReturn( "id1" ).when( objId ).getId();

    SlaveServer rei = mock( SlaveServer.class );
    doReturn( RepositoryObjectType.SLAVE_SERVER ).when( rei ).getRepositoryElementType();
    doReturn( "slaveID" ).when( rei ).getName();
    doReturn( objId ).when( rei ).getObjectId();
    doReturn( rei ).when( rei ).clone();
    Calendar calendar = mock( Calendar.class );

    purRepository.save( rei, "vComment", calendar, mock( ProgressMonitorListener.class ), false );
  }

  public void testLockGetDatabaseMetaParentFolderId() {
    assertNotNull( purRepository.getDatabaseMetaParentFolderId() );
  }

  public void testLockUndeleteObject() throws Exception {
    ObjectId objId = mock( ObjectId.class );
    doReturn( "id1" ).when( objId ).getId();
    RepositoryElementMetaInterface rem = mock( RepositoryElementMetaInterface.class );
    doReturn( objId ).when( rem ).getObjectId();
    purRepository.undeleteObject( rem );

    verify( rem, times( 1 ) ).getObjectId();
    verify( objId, times( 1 ) ).getId();
  }

  public void testLockGetObjectInformation() throws Exception {
    ObjectId objId = mock( ObjectId.class );
    doReturn( "idnull" ).when( objId ).getId();

    assertNull( purRepository.getObjectInformation( objId, null ) );
  }

  public void testLockLoadJob2() throws Exception {
    ObjectId objId = mock( ObjectId.class );
    doReturn( "id1" ).when( objId ).getId();

    assertNotNull( purRepository.loadJob( objId, "v3" ) );
    verify( objId, times( 2 ) ).getId();
  }

  public void testLockLoadTransformation2() throws Exception {
    ObjectId objId = mock( ObjectId.class );
    doReturn( "id1" ).when( objId ).getId();

    assertNotNull( purRepository.loadTransformation( objId, "v3" ) );
    verify( objId, times( 2 ) ).getId();
  }

  public void testLockLoadGetJobsUsingDatabase() throws Exception {
    ObjectId objId = mock( ObjectId.class );
    doReturn( "id1" ).when( objId ).getId();

    assertNotNull( purRepository.getJobsUsingDatabase( objId ) );
    verify( objId, times( 1 ) ).getId();
  }

  public void testLockLoadGetTransformationsUsingDatabase() throws Exception {
    ObjectId objId = mock( ObjectId.class );
    doReturn( "id1" ).when( objId ).getId();

    assertNotNull( purRepository.getTransformationsUsingDatabase( objId ) );
    verify( objId, times( 1 ) ).getId();
  }

  public void testLockGetChildren() {
    purRepository.getChildren( "/folder1/folder2/", "" );
  }

  private class Flow implements Runnable {
    boolean success = false;
    Throwable t = null;
    int nr;

    public Flow( int nr ) {
      this.nr = nr;
    }

    @Override
    public void run() {
      try {
        System.out.println(
          "[PurRepositoryStressTest_" + Thread.currentThread().getName() + "]-> Starting Sample - " + this.nr );
        if ( this.nr < THREADS ) {
          obj.setupStaticMocks( );
        }
        Random randomGenerator = new Random();
        for ( int i = 0; i < METHOD_CALLS_BY_SAMPLE; i++ ) {
          int index = randomGenerator.nextInt( testLockMethods.size() );
          Method item = testLockMethods.get( index );
          //Add the following line for debug purposes
          //System.out.println( "****** " + this.nr + " ********" + item.getName() );
          item.invoke( obj );
        }

        System.out.println(
          "[PurRepositoryStressTest_" + Thread.currentThread().getName() + "]-> Finishing Sample - " + this.nr );
      } catch ( Throwable e ) {
        this.t = e;
        return;
      }
      success = true;
    }
  }

  @Test
  public void runLoadTest() throws Exception {
    KettleClientEnvironment.init();
    obj = new PurRepositoryStressTest();
    obj.setUpTest( false );

    final ThreadFactory factory =
      new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger( 0 );

        public Thread newThread( Runnable r ) {
          final Thread t =
            Executors.defaultThreadFactory().newThread( r );
          t.setDaemon( true );
          t.setName( "THREAD" + '_' + counter.incrementAndGet() );
          return t;
        }
      };

    final ScheduledExecutorService exec = Executors.newScheduledThreadPool( THREADS, factory );

    Map<Future, Flow> futures = new ConcurrentHashMap<Future, Flow>();
    for ( int i = 0; i < SAMPLES; i++ ) {
      Flow mr = new Flow( i );
      futures.put( exec.submit( mr ), mr );
    }

    try {
      exec.shutdown();
      exec.awaitTermination( TIMEOUT, TimeUnit.SECONDS );
      AtomicBoolean success = new AtomicBoolean( true );
      futures.entrySet().forEach( entry -> {
        try {
          entry.getKey().get();
        } catch ( InterruptedException e ) {
          success.set( false );
          fail( "Interrupted future" );
          e.printStackTrace();
        } catch ( ExecutionException e ) {
          success.set( false );
          fail( "Execution exception" );
          e.printStackTrace();
        }
        if ( !entry.getValue().success ) {
          success.set( false );
          entry.getValue().t.printStackTrace();
        }
      } );
      Assert.assertTrue( success.get() );
    } catch ( InterruptedException e ) {
      fail( "Did not complete." );
    }
  }
}


