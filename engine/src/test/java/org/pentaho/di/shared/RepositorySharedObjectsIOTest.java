/*!
 * Copyright 2024 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.di.shared;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.steps.loadsave.MemoryRepository;
import org.pentaho.di.trans.steps.loadsave.MemoryRepositoryExtended;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.Node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith( Parameterized.class )
public class RepositorySharedObjectsIOTest {

  private MemoryRepository rep;
  private RepositorySharedObjectsIO shared;

  public RepositorySharedObjectsIOTest( MemoryRepository rep ) {
    this.rep = rep;
  }

  /**
   * Test both Repository APIs
   */
  @Parameterized.Parameters
  public static List<Object[]> repositories() {
    ArrayList<Object[]> reps = new ArrayList<>();
    reps.add( new Object[] { new MemoryRepository() } );
    reps.add( new Object[] { new MemoryRepositoryExtended() } );
    return reps;
  }

  @Before
  public void setup() throws Exception {
    shared = new RepositorySharedObjectsIO( rep, () -> Collections.emptyList() );
  }

  @Test
  public void testSave() throws Exception {
    DatabaseMeta db = new DatabaseMeta();
    db.setName( "foo" );
    db.setDBName( "bar" );

    shared.saveSharedObject( SharedObjectsIO.SharedObjectType.CONNECTION.getName(), db.getName(), db.toNode() );

    Node node = shared.getSharedObject( SharedObjectsIO.SharedObjectType.CONNECTION.getName(), db.getName() );
    assertNotNull( node );
    DatabaseMeta readDb = new DatabaseMeta( node );
    assertNotNull( readDb.getObjectId() );

    Map<String, Node> dbs = shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() );
    assertNotNull( dbs );
    assertEquals( 1, dbs.size() );
    assertNotNull( dbs.get( "foo" ) );
    readDb = new DatabaseMeta( dbs.get( "foo" ) );
    assertNotNull( readDb.getObjectId() );
  }

  @Test
  public void testUpdate() throws Exception {
    DatabaseMeta db = new DatabaseMeta();
    db.setName( "foo" );
    db.setDBName( "bar" );

    shared.saveSharedObject( SharedObjectsIO.SharedObjectType.CONNECTION.getName(), db.getName(), db.toNode() );

    Map<String, Node> dbs = shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() );
    assertEquals( 1, dbs.size() );
    Node createdNode = shared.getSharedObject( SharedObjectsIO.SharedObjectType.CONNECTION.getName(), db.getName() );
    assertNotNull( createdNode );

    DatabaseMeta afterCreate = new DatabaseMeta( createdNode );

    db.setServername( "testing" );

    // should replace the existing item.
    shared.saveSharedObject( SharedObjectsIO.SharedObjectType.CONNECTION.getName(), db.getName(), db.toNode() );

    dbs = shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() );
    assertEquals( 1, dbs.size() );
    Node updatedNode = shared.getSharedObject( SharedObjectsIO.SharedObjectType.CONNECTION.getName(), db.getName() );
    assertNotNull( updatedNode );

    DatabaseMeta afterUpdate = new DatabaseMeta( updatedNode );

    assertEquals( db.getDescription(), afterUpdate.getDescription() );

    assertNull( afterCreate.getServername() );
    assertEquals( "testing", afterUpdate.getServername() );
  }

  @Test
  public void testDelete() throws Exception {
    DatabaseMeta db = new DatabaseMeta();
    db.setName( "foo" );
    db.setDBName( "bar" );

    shared.saveSharedObject( SharedObjectsIO.SharedObjectType.CONNECTION.getName(), db.getName(), db.toNode() );

    Node node = shared.getSharedObject( SharedObjectsIO.SharedObjectType.CONNECTION.getName(), db.getName() );
    assertNotNull( node );

    Map<String, Node> dbs = shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() );
    assertNotNull( dbs );
    assertEquals( 1, dbs.size() );
    assertNotNull( dbs.get( "foo" ) );

    shared.delete( SharedObjectsIO.SharedObjectType.CONNECTION.getName(), "foo" );

    node = shared.getSharedObject( SharedObjectsIO.SharedObjectType.CONNECTION.getName(), db.getName() );
    assertNull( node );

    dbs = shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() );
    assertNotNull( dbs );
    assertEquals( 0, dbs.size() );
  }

  @Test
  public void testOtherTypes() throws Exception {
    //test the non-db types.
    SlaveServer slaveServer = new SlaveServer();
    slaveServer.setName( "slave 1" );

    PartitionSchema partitionSchema = new PartitionSchema();
    partitionSchema.setName( "pschema 1" );

    ClusterSchema clusterSchema = new ClusterSchema();
    clusterSchema.setName( "Cluster Schema 1" );
    clusterSchema.setSlaveServers( Collections.singletonList( slaveServer ) );

    shared = new RepositorySharedObjectsIO( rep, () -> Collections.singletonList( slaveServer ) );
    shared.saveSharedObject( SharedObjectsIO.SharedObjectType.SLAVESERVER.getName(), slaveServer.getName(),
      slaveServer.toNode() );
    shared.saveSharedObject( SharedObjectsIO.SharedObjectType.PARTITIONSCHEMA.getName(), partitionSchema.getName(),
      partitionSchema.toNode() );
    shared.saveSharedObject( SharedObjectsIO.SharedObjectType.CLUSTERSCHEMA.getName(), clusterSchema.getName(),
      clusterSchema.toNode() );

    SlaveServer readSlaveServer = new SlaveServer( shared.getSharedObject(
      SharedObjectsIO.SharedObjectType.SLAVESERVER.getName(), slaveServer.getName() ) );

    PartitionSchema readPartitionSchema = new PartitionSchema( shared.getSharedObject(
      SharedObjectsIO.SharedObjectType.PARTITIONSCHEMA.getName(), partitionSchema.getName() ) );

    ClusterSchema readClusterSchema = new ClusterSchema( shared.getSharedObject( SharedObjectsIO.SharedObjectType.CLUSTERSCHEMA.getName(), clusterSchema.getName() ), Collections.singletonList( slaveServer ) );

    assertNotNull( readSlaveServer );
    assertNotNull( readSlaveServer.getObjectId() );
    assertNotNull( readPartitionSchema );
    assertNotNull( readPartitionSchema.getObjectId() );
    assertNotNull( readClusterSchema );
    assertEquals( 1, readClusterSchema.getSlaveServers().size() );
    assertNotNull( readClusterSchema.getObjectId() );

    Map<String, Node> readObjects = shared.getSharedObjects( SharedObjectsIO.SharedObjectType.SLAVESERVER.getName() );
    assertEquals( 1, readObjects.size() );
    readSlaveServer = new SlaveServer( readObjects.get( slaveServer.getName() ) );
    assertNotNull( readSlaveServer );
    assertNotNull( readSlaveServer.getObjectId() );

    readObjects = shared.getSharedObjects( SharedObjectsIO.SharedObjectType.PARTITIONSCHEMA.getName() );
    assertEquals( 1, readObjects.size() );
    readPartitionSchema = new PartitionSchema( readObjects.get( partitionSchema.getName() ) );
    assertNotNull( readPartitionSchema );
    assertNotNull( readPartitionSchema.getObjectId() );

    readObjects = shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CLUSTERSCHEMA.getName() );
    assertEquals( 1, readObjects.size() );
    readClusterSchema = new ClusterSchema( readObjects.get( clusterSchema.getName() ),
      Collections.singletonList( slaveServer ) );
    assertNotNull( readClusterSchema );
    assertNotNull( readClusterSchema.getObjectId() );

    shared.delete( SharedObjectsIO.SharedObjectType.SLAVESERVER.getName(), slaveServer.getName() );
    shared.delete( SharedObjectsIO.SharedObjectType.PARTITIONSCHEMA.getName(), partitionSchema.getName() );
    shared.delete( SharedObjectsIO.SharedObjectType.CLUSTERSCHEMA.getName(), clusterSchema.getName() );

    assertEquals( 0, shared.getSharedObjects( SharedObjectsIO.SharedObjectType.SLAVESERVER.getName() ).size() );
    assertEquals( 0, shared.getSharedObjects( SharedObjectsIO.SharedObjectType.PARTITIONSCHEMA.getName() ).size() );
    assertEquals( 0, shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CLUSTERSCHEMA.getName() ).size() );
  }

  @Test
  public void testClear() throws Exception {
    DatabaseMeta db = new DatabaseMeta();
    db.setName( "foo" );
    db.setDBName( "bar" );

    SlaveServer slaveServer = new SlaveServer();
    slaveServer.setName( "slave 1" );

    PartitionSchema partitionSchema = new PartitionSchema();
    partitionSchema.setName( "pschema 1" );

    ClusterSchema clusterSchema = new ClusterSchema();
    clusterSchema.setName( "Cluster Schema 1" );
    clusterSchema.setSlaveServers( Collections.singletonList( slaveServer ) );

    shared = new RepositorySharedObjectsIO( rep, () -> Collections.singletonList( slaveServer ) );
    shared.saveSharedObject( SharedObjectsIO.SharedObjectType.CONNECTION.getName(), db.getName(), db.toNode() );
    shared.saveSharedObject( SharedObjectsIO.SharedObjectType.SLAVESERVER.getName(), slaveServer.getName(),
      slaveServer.toNode() );
    shared.saveSharedObject( SharedObjectsIO.SharedObjectType.PARTITIONSCHEMA.getName(), partitionSchema.getName(),
      partitionSchema.toNode() );
    shared.saveSharedObject( SharedObjectsIO.SharedObjectType.CLUSTERSCHEMA.getName(), clusterSchema.getName(),
      clusterSchema.toNode() );

    shared.clear( SharedObjectsIO.SharedObjectType.CONNECTION.getName() );
    shared.clear( SharedObjectsIO.SharedObjectType.SLAVESERVER.getName() );
    shared.clear( SharedObjectsIO.SharedObjectType.PARTITIONSCHEMA.getName() );
    shared.clear( SharedObjectsIO.SharedObjectType.CLUSTERSCHEMA.getName() );

    assertEquals( 0, shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CONNECTION.getName() ).size() );
    assertEquals( 0, shared.getSharedObjects( SharedObjectsIO.SharedObjectType.SLAVESERVER.getName() ).size() );
    assertEquals( 0, shared.getSharedObjects( SharedObjectsIO.SharedObjectType.PARTITIONSCHEMA.getName() ).size() );
    assertEquals( 0, shared.getSharedObjects( SharedObjectsIO.SharedObjectType.CLUSTERSCHEMA.getName() ).size() );
  }

}
