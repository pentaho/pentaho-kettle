/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.repository.pur;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataNodeRef;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PurRepositoryClusterReferenceTest {

  private PurRepository repository;
  private SlaveServer slave;
  private DataNode attributes;
  private IUnifiedRepository unifiedRepository;
  private NodeRepositoryFileData clusterData;

  @Before
  @SuppressWarnings( "unchecked" )
  public void setUp() throws Exception {
    KettleLogStore.init();
    repository = spy( new PurRepository() );
    repository.init( mock( PurRepositoryMeta.class ) );
    unifiedRepository = mock( IUnifiedRepository.class );
    repository.setTest( unifiedRepository );
    doReturn( null ).when( repository ).createObjectRevision( any() );
    doReturn( Collections.emptyList() ).when( repository )
      .getAllFilesOfType( any(), any( RepositoryObjectType.class ), anyBoolean() );

    RepositoryFile slaveFile = mock( RepositoryFile.class );
    RepositoryFile clusterFile = mock( RepositoryFile.class );
    when( clusterFile.getId() ).thenReturn( "cluster-id" );
    when( clusterFile.getTitle() ).thenReturn( "cluster" );
    doReturn( Collections.singletonList( slaveFile ) ).when( repository )
      .getAllFilesOfType( any(), eq( RepositoryObjectType.SLAVE_SERVER ), eq( false ) );
    doReturn( Collections.singletonList( clusterFile ) ).when( repository )
      .getAllFilesOfType( any(), eq( RepositoryObjectType.CLUSTER_SCHEMA ), eq( false ) );

    DataNode node = new DataNode( "root" );
    node.setProperty( "BASE_PORT", "40000" );
    node.setProperty( "SOCKETS_BUFFER_SIZE", "2000" );
    node.setProperty( "SOCKETS_FLUSH_INTERVAL", "5000" );
    node.setProperty( "SOCKETS_COMPRESSED", true );
    node.setProperty( "DYNAMIC", false );
    attributes = node.addNode( "attributes" );
    attributes.setProperty( "NB_SLAVE_SERVERS", 1 );
    attributes.setProperty( "0", new DataNodeRef( "slave-id" ) );
    clusterData = new NodeRepositoryFileData( node );
    when( unifiedRepository.getDataForReadInBatch( anyList(), eq( NodeRepositoryFileData.class ) ) )
      .thenReturn( Arrays.asList( mock( NodeRepositoryFileData.class ), clusterData ) );
    when( unifiedRepository.getVersionSummaryInBatch( anyList() ) )
      .thenReturn( Arrays.asList( mock( VersionSummary.class ), mock( VersionSummary.class ) ) );

    slave = new SlaveServer();
    slave.setName( "slave" );
    slave.setObjectId( new StringObjectId( "slave-id" ) );
    SharedObjectAssembler<?> slaveAssembler = mock( SharedObjectAssembler.class );
    doReturn( slave ).when( slaveAssembler ).assemble( any(), any(), any() );
    Field field = PurRepository.class.getDeclaredField( "sharedObjectAssemblerMap" );
    field.setAccessible( true );
    Map<RepositoryObjectType, SharedObjectAssembler<?>> assemblers =
      (Map<RepositoryObjectType, SharedObjectAssembler<?>>) field.get( repository );
    assemblers.put( RepositoryObjectType.SLAVE_SERVER, slaveAssembler );
    assemblers.put( RepositoryObjectType.CLUSTER_SCHEMA, new ClusterDelegate( repository ) );
  }

  @Test
  public void coldCacheResolvesClusterSlaveWithoutRepositoryLookup() throws Exception {
    ClusterSchema cluster = loadCluster();

    assertEquals( 1, cluster.getSlaveServers().size() );
    assertEquals( "slave-id", cluster.getSlaveServers().get( 0 ).getObjectId().getId() );
    assertNotSame( slave, cluster.getSlaveServers().get( 0 ) );
    verify( repository, never() ).getSlaveServers();
  }

  @Test
  public void unpublishedBatchResolvesReferencesWithoutInitializingGlobalCache() throws Exception {
    Map<RepositoryObjectType, List<? extends SharedObjectInterface<?>>> loaded =
      new EnumMap<>( RepositoryObjectType.class );

    repository.readSharedObjects( loaded, RepositoryObjectType.DATABASE, RepositoryObjectType.PARTITION_SCHEMA,
      RepositoryObjectType.SLAVE_SERVER, RepositoryObjectType.CLUSTER_SCHEMA );

    ClusterSchema cluster = (ClusterSchema) loaded.get( RepositoryObjectType.CLUSTER_SCHEMA ).get( 0 );
    assertEquals( "slave-id", cluster.getSlaveServers().get( 0 ).getObjectId().getId() );
    Field cache = PurRepository.class.getDeclaredField( "sharedObjectsByType" );
    cache.setAccessible( true );
    assertNull( cache.get( repository ) );
    verify( repository, never() ).getSlaveServers();
  }

  @Test
  public void emptyLoadedSlaveListDoesNotFallBackToRepository() throws Exception {
    doReturn( Collections.emptyList() ).when( repository )
      .getAllFilesOfType( any(), eq( RepositoryObjectType.SLAVE_SERVER ), eq( false ) );
    useClusterOnlyBatch();

    ClusterSchema cluster = loadCluster();

    assertEquals( 1, cluster.getSlaveServers().size() );
    assertNull( cluster.getSlaveServers().get( 0 ) );
    verify( repository, never() ).getSlaveServers();
  }

  @Test
  public void clusterOnlyReadRetainsRepositoryLookup() throws Exception {
    useClusterOnlyBatch();
    doReturn( Collections.singletonList( slave ) ).when( repository ).getSlaveServers();

    ClusterSchema cluster = repository.getClusters( false ).get( 0 );

    assertEquals( "slave-id", cluster.getSlaveServers().get( 0 ).getObjectId().getId() );
    verify( repository ).getSlaveServers();
  }

  @Test
  public void missingSlaveDoesNotReenterCacheInitialization() throws Exception {
    attributes.setProperty( "0", new DataNodeRef( "missing-slave" ) );

    ClusterSchema cluster = loadCluster();

    assertEquals( 1, cluster.getSlaveServers().size() );
    assertNull( cluster.getSlaveServers().get( 0 ) );
    verify( repository, never() ).getSlaveServers();
  }

  @Test
  public void cachedReadsRemainDefensiveCopies() throws Exception {
    loadCluster();

    List<SlaveServer> first = repository.getSlaveServers();
    List<SlaveServer> second = repository.getSlaveServers();

    assertNotSame( slave, first.get( 0 ) );
    assertNotSame( first.get( 0 ), second.get( 0 ) );
    assertEquals( "slave-id", second.get( 0 ).getObjectId().getId() );
    verify( repository, times( 4 ) ).getAllFilesOfType( any(), any( RepositoryObjectType.class ), anyBoolean() );
  }

  private ClusterSchema loadCluster() throws Exception {
    Map<RepositoryObjectType, List<? extends SharedObjectInterface<?>>> loaded =
      repository.loadAndCacheSharedObjects( false );
    return (ClusterSchema) loaded.get( RepositoryObjectType.CLUSTER_SCHEMA ).get( 0 );
  }

  private void useClusterOnlyBatch() {
    when( unifiedRepository.getDataForReadInBatch( anyList(), eq( NodeRepositoryFileData.class ) ) )
      .thenReturn( Collections.singletonList( clusterData ) );
    when( unifiedRepository.getVersionSummaryInBatch( anyList() ) )
      .thenReturn( Collections.singletonList( mock( VersionSummary.class ) ) );
  }
}