/* ****************************************************************************
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

package org.pentaho.di.repository.pur;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class PurRepositoryLoadLockTest {

  @Test
  public void repositoryLoadsReleaseReadLockBeforeDeserialization() throws Exception {
    PurRepository repository = spy( new PurRepository() );
    IUnifiedRepository unifiedRepository = mock( IUnifiedRepository.class );
    PurRepositoryMeta repositoryMeta = mock( PurRepositoryMeta.class );
    repository.init( repositoryMeta );
    repository.setTest( unifiedRepository );
    doReturn( mock( RepositoryDirectoryInterface.class ) ).when( repository ).findDirectory( anyString() );

    Field lockServiceField = PurRepository.class.getDeclaredField( "unifiedRepositoryLockService" );
    lockServiceField.setAccessible( true );
    lockServiceField.set( repository, new UnifiedRepositoryLockService( unifiedRepository ) );

    RepositoryFile file = mock( RepositoryFile.class );
    when( file.getId() ).thenReturn( "file-id" );
    when( file.getTitle() ).thenReturn( "file" );
    when( file.getPath() ).thenReturn( "/file" );
    when( unifiedRepository.getFileAtVersion( anyString(), eq( "v3" ) ) ).thenReturn( file );
    when( unifiedRepository.getFile( anyString() ) ).thenReturn( file );
    RepositoryDirectoryInterface directory = mock( RepositoryDirectoryInterface.class );
    doReturn( "/file.ktr" ).when( repository ).getPath( "file", directory, RepositoryObjectType.TRANSFORMATION );
    doReturn( "/file.kjb" ).when( repository ).getPath( "file", directory, RepositoryObjectType.JOB );

    VersionSummary versionSummary = mock( VersionSummary.class );
    when( versionSummary.getId() ).thenReturn( (Serializable) "revision-id" );
    when( versionSummary.getAuthor() ).thenReturn( "author" );
    when( versionSummary.getDate() ).thenReturn( new java.util.Date() );
    when( versionSummary.getMessage() ).thenReturn( "message" );
    when( unifiedRepository.getVersionSummary( any(), eq( "v3" ) ) ).thenReturn( versionSummary );

    NodeRepositoryFileData fileData = mock( NodeRepositoryFileData.class );
    when( fileData.getNode() ).thenReturn( mock( DataNode.class ) );
    doReturn( fileData ).when( unifiedRepository ).getDataAtVersionForRead( any(), eq( "v3" ),
      eq( NodeRepositoryFileData.class ) );

    assertWriteLockAvailableDuringDeserialization( repository, "transDelegate",
      () -> repository.loadTransformation( new StringObjectId( "file-id" ), "v3" ) );
    assertWriteLockAvailableDuringDeserialization( repository, "jobDelegate",
      () -> repository.loadJob( new StringObjectId( "file-id" ), "v3" ) );
    assertWriteLockAvailableDuringDeserialization( repository, "transDelegate",
      () -> repository.loadTransformation( "file", directory, null, false, "v3" ) );
    assertWriteLockAvailableDuringDeserialization( repository, "jobDelegate",
      () -> repository.loadJob( "file", directory, null, "v3" ) );
    assertWriteLockAvailableDuringAssembly( repository, "databaseMetaTransformer", mock( DatabaseMeta.class ),
      () -> repository.loadDatabaseMeta( new StringObjectId( "file-id" ), "v3" ) );
    assertWriteLockAvailableDuringAssembly( repository, "clusterTransformer", mock( ClusterSchema.class ),
      () -> repository.loadClusterSchema( new StringObjectId( "file-id" ), Collections.<SlaveServer>emptyList(), "v3" ) );
    assertWriteLockAvailableDuringAssembly( repository, "partitionSchemaTransformer", mock( PartitionSchema.class ),
      () -> repository.loadPartitionSchema( new StringObjectId( "file-id" ), "v3" ) );
    assertWriteLockAvailableDuringAssembly( repository, "slaveTransformer", mock( SlaveServer.class ),
      () -> repository.loadSlaveServer( new StringObjectId( "file-id" ), "v3" ) );
  }

  @Test
  public void databaseListReadReleasesReadLockBeforeDeserialization() throws Exception {
    PurRepository repository = spy( new PurRepository() );
    IUnifiedRepository unifiedRepository = mock( IUnifiedRepository.class );
    repository.init( mock( PurRepositoryMeta.class ) );
    repository.setTest( unifiedRepository );
    RepositoryFile file = mock( RepositoryFile.class );
    when( file.getTitle() ).thenReturn( "database" );
    doReturn( Collections.singletonList( file ) ).when( repository ).getAllFilesOfType( any(),
      eq( RepositoryObjectType.DATABASE ), eq( false ) );

    DataNode node = mock( DataNode.class, RETURNS_DEEP_STUBS );
    when( node.getProperty( "TYPE" ).getString() ).thenReturn( "Database" );
    NodeRepositoryFileData fileData = mock( NodeRepositoryFileData.class );
    when( fileData.getNode() ).thenReturn( node );
    when( unifiedRepository.getDataForReadInBatch( anyList(), eq( NodeRepositoryFileData.class ) ) )
      .thenReturn( Collections.singletonList( fileData ) );

    DatabaseDelegate databaseDelegate = spy( new DatabaseDelegate( repository ) );
    repository.setDatabaseMetaTransformerForTesting( databaseDelegate );
    doAnswer( invocation -> {
      assertRepositoryWriteLockAvailable();
      return mock( DatabaseMeta.class );
    } ).when( databaseDelegate ).dataNodeToElement( any( DataNode.class ) );

    assertEquals( 1, repository.readDatabases().size() );
  }

  @Test
  public void batchJobAndTransformationLoadsReleaseReadLockBeforeDeserialization() throws Exception {
    PurRepository repository = spy( new PurRepository() );
    IUnifiedRepository unifiedRepository = mock( IUnifiedRepository.class );
    repository.init( mock( PurRepositoryMeta.class ) );
    repository.setTest( unifiedRepository );
    doReturn( mock( RepositoryDirectoryInterface.class ) ).when( repository ).findDirectory( anyString() );

    RepositoryFile file = mock( RepositoryFile.class );
    when( file.getId() ).thenReturn( "file-id" );
    when( file.getTitle() ).thenReturn( "file" );
    when( file.getPath() ).thenReturn( "/dir/file.ktr" );
    when( file.getDescription() ).thenReturn( "description" );
    List<RepositoryFile> files = Collections.singletonList( file );
    NodeRepositoryFileData fileData = mock( NodeRepositoryFileData.class );
    when( fileData.getNode() ).thenReturn( mock( DataNode.class ) );
    when( unifiedRepository.getDataForReadInBatch( anyList(), eq( NodeRepositoryFileData.class ) ) )
      .thenReturn( Collections.singletonList( fileData ) );

    VersionSummary versionSummary = mock( VersionSummary.class );
    when( versionSummary.getId() ).thenReturn( (Serializable) "revision-id" );
    when( versionSummary.getAuthor() ).thenReturn( "author" );
    when( versionSummary.getDate() ).thenReturn( new java.util.Date() );
    when( versionSummary.getMessage() ).thenReturn( "message" );
    when( unifiedRepository.getVersionSummaryInBatch( anyList() ) ).thenReturn( Collections.singletonList( versionSummary ) );

    LogChannelInterface log = mock( LogChannelInterface.class );
    assertWriteLockAvailableDuringDeserialization( repository, "transDelegate", () -> {
      List<?> loaded = repository.loadTransformations( null, log, files, false );
      assertEquals( 1, loaded.size() );
      return loaded;
    } );
    assertWriteLockAvailableDuringDeserialization( repository, "jobDelegate", () -> {
      List<?> loaded = repository.loadJobs( null, log, files, false );
      assertEquals( 1, loaded.size() );
      return loaded;
    } );
  }

  @Test
  public void sharedObjectCacheLoadReleasesReadLockBeforeAssembly() throws Exception {
    PurRepository repository = spy( new PurRepository() );
    IUnifiedRepository unifiedRepository = mock( IUnifiedRepository.class );
    repository.init( mock( PurRepositoryMeta.class ) );
    repository.setTest( unifiedRepository );

    RepositoryFile file = mock( RepositoryFile.class );
    doReturn( Collections.singletonList( file ) ).when( repository ).getAllFilesOfType( any(),
      eq( RepositoryObjectType.DATABASE ), eq( false ) );
    NodeRepositoryFileData fileData = mock( NodeRepositoryFileData.class );
    when( unifiedRepository.getDataForReadInBatch( anyList(), eq( NodeRepositoryFileData.class ) ) )
      .thenReturn( Collections.singletonList( fileData ) );
    when( unifiedRepository.getVersionSummaryInBatch( anyList() ) )
      .thenReturn( Collections.singletonList( mock( VersionSummary.class ) ) );

    SharedObjectAssembler<?> assembler = mock( SharedObjectAssembler.class );
    java.lang.reflect.Field assemblerMapField = PurRepository.class.getDeclaredField( "sharedObjectAssemblerMap" );
    assemblerMapField.setAccessible( true );
    java.util.Map<RepositoryObjectType, SharedObjectAssembler<?>> assemblerMap =
      new java.util.EnumMap<>( RepositoryObjectType.class );
    assemblerMap.put( RepositoryObjectType.DATABASE, assembler );
    assemblerMapField.set( repository, assemblerMap );
    doAnswer( invocation -> {
      assertRepositoryWriteLockAvailable();
      return mock( DatabaseMeta.class );
    } ).when( assembler ).assemble( any(), any(), any() );

    java.util.Map<RepositoryObjectType, List<? extends SharedObjectInterface<?>>> loaded =
      new java.util.EnumMap<>( RepositoryObjectType.class );
    repository.readSharedObjects( loaded, RepositoryObjectType.DATABASE );

    assertEquals( 1, loaded.get( RepositoryObjectType.DATABASE ).size() );
  }

  @Test
  public void sharedObjectLoadFailureReleasesRepositoryLocks() throws Exception {
    PurRepository repository = spy( new PurRepository() );
    doThrow( new KettleException( "load failed" ) ).when( repository ).loadAndCacheSharedObjects( false );
    Method updateSharedObjectCache = PurRepository.class.getDeclaredMethod( "updateSharedObjectCache",
      RepositoryElementInterface.class, RepositoryObjectType.class, org.pentaho.di.repository.ObjectId.class );
    updateSharedObjectCache.setAccessible( true );

    FutureTask<Void> failedUpdate = new FutureTask<>( () -> {
      try {
        updateSharedObjectCache.invoke( repository, null, RepositoryObjectType.DATABASE,
          new StringObjectId( "database-id" ) );
        throw new AssertionError( "Expected shared-object load to fail" );
      } catch ( InvocationTargetException e ) {
        if ( !( e.getCause() instanceof KettleException ) ) {
          throw e;
        }
      }
      return null;
    } );
    Thread updateThread = new Thread( failedUpdate );
    updateThread.setDaemon( true );
    updateThread.start();
    failedUpdate.get( 2, TimeUnit.SECONDS );

    Field repositoryLockField = PurRepository.class.getDeclaredField( "readWriteLock" );
    repositoryLockField.setAccessible( true );
    ReadWriteLock repositoryLock = (ReadWriteLock) repositoryLockField.get( null );
    Field sharedObjectsLockField = PurRepository.class.getDeclaredField( "sharedObjectsLock" );
    sharedObjectsLockField.setAccessible( true );
    ReadWriteLock sharedObjectsLock = (ReadWriteLock) sharedObjectsLockField.get( null );

    FutureTask<Boolean> locksAvailable = new FutureTask<>( () -> {
      boolean repositoryWriteAvailable = repositoryLock.writeLock().tryLock();
      if ( repositoryWriteAvailable ) {
        repositoryLock.writeLock().unlock();
      }
      boolean sharedObjectsReadAvailable = sharedObjectsLock.readLock().tryLock();
      if ( sharedObjectsReadAvailable ) {
        sharedObjectsLock.readLock().unlock();
      }
      return repositoryWriteAvailable && sharedObjectsReadAvailable;
    } );
    Thread verificationThread = new Thread( locksAvailable );
    verificationThread.setDaemon( true );
    verificationThread.start();
    assertTrue( "A failed cache load must release both repository locks",
      locksAvailable.get( 2, TimeUnit.SECONDS ) );
  }

  private void assertWriteLockAvailableDuringDeserialization( PurRepository repository, String delegateName,
      RepositoryLoad load ) throws Exception {
    Field lockField = PurRepository.class.getDeclaredField( "readWriteLock" );
    lockField.setAccessible( true );
    ReadWriteLock lock = (ReadWriteLock) lockField.get( null );

    Field delegateField = PurRepository.class.getDeclaredField( delegateName );
    delegateField.setAccessible( true );
    ISharedObjectsTransformer transformer = mock( ISharedObjectsTransformer.class );
    delegateField.set( repository, transformer );
    doAnswer( invocation -> {
      FutureTask<Boolean> writeLockAcquired = new FutureTask<>( () -> {
        boolean acquired = lock.writeLock().tryLock( 1, TimeUnit.SECONDS );
        if ( acquired ) {
          lock.writeLock().unlock();
        }
        return acquired;
      } );
      Thread writer = new Thread( writeLockAcquired );
      writer.setDaemon( true );
      writer.start();
      assertTrue( "PUR write lock should be available during repository deserialization",
        writeLockAcquired.get( 2, TimeUnit.SECONDS ) );
      return null;
    } ).when( transformer ).dataNodeToElement( any( DataNode.class ), any( RepositoryElementInterface.class ) );

    assertNotNull( load.run() );
  }

  private void assertWriteLockAvailableDuringAssembly( PurRepository repository, String delegateName,
      Object assembledObject, RepositoryLoad load ) throws Exception {
    Field lockField = PurRepository.class.getDeclaredField( "readWriteLock" );
    lockField.setAccessible( true );
    ReadWriteLock lock = (ReadWriteLock) lockField.get( null );

    Field delegateField = PurRepository.class.getDeclaredField( delegateName );
    delegateField.setAccessible( true );
    Object delegate = mock( delegateField.getType() );
    delegateField.set( repository, delegate );
    SharedObjectAssembler<?> assembler = (SharedObjectAssembler<?>) delegate;
    doAnswer( invocation -> {
      FutureTask<Boolean> writeLockAcquired = new FutureTask<>( () -> {
        boolean acquired = lock.writeLock().tryLock( 1, TimeUnit.SECONDS );
        if ( acquired ) {
          lock.writeLock().unlock();
        }
        return acquired;
      } );
      Thread writer = new Thread( writeLockAcquired );
      writer.setDaemon( true );
      writer.start();
      assertTrue( "PUR write lock should be available during shared-object assembly",
        writeLockAcquired.get( 2, TimeUnit.SECONDS ) );
      return assembledObject;
    } ).when( assembler ).assemble( any(), any(), any() );

    assertNotNull( load.run() );
  }

  private void assertRepositoryWriteLockAvailable() throws Exception {
    Field lockField = PurRepository.class.getDeclaredField( "readWriteLock" );
    lockField.setAccessible( true );
    ReadWriteLock lock = (ReadWriteLock) lockField.get( null );
    FutureTask<Boolean> writeLockAcquired = new FutureTask<>( () -> {
      boolean acquired = lock.writeLock().tryLock( 1, TimeUnit.SECONDS );
      if ( acquired ) {
        lock.writeLock().unlock();
      }
      return acquired;
    } );
    Thread writer = new Thread( writeLockAcquired );
    writer.setDaemon( true );
    writer.start();
    assertTrue( "PUR write lock should be available during repository deserialization",
      writeLockAcquired.get( 2, TimeUnit.SECONDS ) );
  }

  private interface RepositoryLoad {
    Object run() throws KettleException;
  }
}