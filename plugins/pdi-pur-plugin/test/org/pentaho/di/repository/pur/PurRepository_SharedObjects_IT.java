/*!
 * Copyright 2010 - 2016 Pentaho Corporation.  All rights reserved.
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

import org.junit.Test;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.shared.SharedObjectInterface;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Andrey Khayrutdinov
 */
public class PurRepository_SharedObjects_IT extends PurRepositoryTestBase {

  public PurRepository_SharedObjects_IT( Boolean lazyRepo ) {
    super( lazyRepo );
  }

  @Test
  public void loadClusters() throws Exception {
    testLoadSharedObjects( cluster() );
  }

  @Test
  public void loadDatabases() throws Exception {
    testLoadSharedObjects( database() );
  }

  @Test
  public void loadSlaves() throws Exception {
    testLoadSharedObjects( slaveServer() );
  }

  @Test
  public void loadPartitions() throws Exception {
    testLoadSharedObjects( partition() );
  }

  private void testLoadSharedObjects( RepositoryElementInterface sharedObject ) throws Exception {
    purRepository.save( sharedObject, null, null );

    Map<RepositoryObjectType, List<? extends SharedObjectInterface>> map = map();
    purRepository.readSharedObjects( map, sharedObject.getRepositoryElementType() );

    List<? extends SharedObjectInterface> objects = map.get( sharedObject.getRepositoryElementType() );
    assertNotNull( objects );
    assertEquals( 1, objects.size() );

    SharedObjectInterface object = objects.get( 0 );
    assertEquals( sharedObject, object );
  }

  @Test
  public void clusterIsRemovedFromCacheOnDelete() throws Exception {
    testElementIsRemovedFromCacheOnDelete( cluster(), new Remover() {
      @Override
      public void deleteFromRepository( RepositoryElementInterface element ) throws KettleException {
        purRepository.deleteClusterSchema( element.getObjectId() );
      }
    } );
  }

  @Test
  public void databaseIsRemovedFromCacheOnDelete() throws Exception {
    testElementIsRemovedFromCacheOnDelete( database(), new Remover() {
      @Override
      public void deleteFromRepository( RepositoryElementInterface element ) throws KettleException {
        purRepository.deleteDatabaseMeta( element.getName() );
      }
    } );
  }

  @Test
  public void slaveIsRemovedFromCacheOnDelete() throws Exception {
    testElementIsRemovedFromCacheOnDelete( slaveServer(), new Remover() {
      @Override
      public void deleteFromRepository( RepositoryElementInterface element ) throws KettleException {
        purRepository.deleteSlave( element.getObjectId() );
      }
    } );
  }

  @Test
  public void partitionIsRemovedFromCacheOnDelete() throws Exception {
    testElementIsRemovedFromCacheOnDelete( partition(), new Remover() {
      @Override
      public void deleteFromRepository( RepositoryElementInterface element ) throws KettleException {
        purRepository.deletePartitionSchema( element.getObjectId() );
      }
    } );
  }

  private void testElementIsRemovedFromCacheOnDelete( RepositoryElementInterface element, Remover remover )
    throws Exception {
    purRepository.save( element, null, null );
    assertNotNull( element.getObjectId() );

    List<? extends SharedObjectInterface> before =
        purRepository.loadAndCacheSharedObjects().get( element.getRepositoryElementType() );
    assertEquals( 1, before.size() );

    remover.deleteFromRepository( element );
    List<? extends SharedObjectInterface> after =
        purRepository.loadAndCacheSharedObjects().get( element.getRepositoryElementType() );
    assertTrue( after.isEmpty() );
  }

  @Test
  public void loadAllShared() throws Exception {
    ClusterSchema cluster = cluster();
    DatabaseMeta database = database();
    SlaveServer slaveServer = slaveServer();
    PartitionSchema partition = partition();

    purRepository.save( cluster, null, null );
    purRepository.save( database, null, null );
    purRepository.save( slaveServer, null, null );
    purRepository.save( partition, null, null );

    Map<RepositoryObjectType, List<? extends SharedObjectInterface>> map = map();
    purRepository.readSharedObjects( map, RepositoryObjectType.CLUSTER_SCHEMA, RepositoryObjectType.DATABASE,
        RepositoryObjectType.SLAVE_SERVER, RepositoryObjectType.PARTITION_SCHEMA );

    RepositoryElementInterface[] saved = new RepositoryElementInterface[] { cluster, database, slaveServer, partition };
    assertEquals( saved.length, map.size() );
    for ( RepositoryElementInterface sharedObject : saved ) {
      List<? extends SharedObjectInterface> list = map.get( sharedObject.getRepositoryElementType() );
      assertEquals( 1, list.size() );
      assertEquals( sharedObject, list.get( 0 ) );
    }
  }

  private static ClusterSchema cluster() {
    return new ClusterSchema( "testCluster", Collections.<SlaveServer>emptyList() );
  }

  private static DatabaseMeta database() {
    DatabaseMeta db = new DatabaseMeta();
    db.setName( "testDatabase" );
    return db;
  }

  private static SlaveServer slaveServer() {
    SlaveServer server = new SlaveServer();
    server.setName( "testSlaveServer" );
    return server;
  }

  private static PartitionSchema partition() {
    PartitionSchema schema = new PartitionSchema();
    schema.setName( "testPartitionSchema" );
    return schema;
  }

  private static Map<RepositoryObjectType, List<? extends SharedObjectInterface>> map() {
    return new EnumMap<RepositoryObjectType, List<? extends SharedObjectInterface>>( RepositoryObjectType.class );
  }

  private interface Remover {
    void deleteFromRepository( RepositoryElementInterface element ) throws KettleException;
  }
}
