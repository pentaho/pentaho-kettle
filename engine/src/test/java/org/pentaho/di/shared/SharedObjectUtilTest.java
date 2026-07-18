/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.di.shared;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.cluster.SlaveServerManagementInterface;
import org.pentaho.di.core.bowl.BaseBowl;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.partition.PartitionSchemaManagementInterface;
import org.pentaho.di.repository.IRepositoryImporter;
import org.pentaho.di.repository.RepositoryImporter;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.shared.SharedObjectsIO.SharedObjectType;
import org.pentaho.di.shared.SharedObjectUtil.ComparedState;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.IMetaStore;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class SharedObjectUtilTest {

  @BeforeClass
  public static void setup() throws Exception {
    KettleEnvironment.init();
  }

  @AfterClass
  public static void teardown() throws Exception {
    KettleEnvironment.reset();
  }

  @Test
  public void testCollectChanged() throws Exception {
    // EXISTING
    DatabaseMeta existingDb = new DatabaseMeta();
    existingDb.setName( "same" );

    SlaveServer existingSlaveServer = new SlaveServer();
    existingSlaveServer.setName( "existing" );

    PartitionSchema existingPartitionSchema = new PartitionSchema();
    existingPartitionSchema.setName( "changeme" );
    existingPartitionSchema.setPartitionIDs( List.of( "foo" ) );

    MemorySharedObjectsIO sharedIO = new MemorySharedObjectsIO();
    Bowl bowl = new BaseBowl() {
        @Override
        public SharedObjectsIO getSharedObjectsIO() {
          return sharedIO;
        }
        @Override
        public IMetaStore getMetastore() throws MetaStoreException {
          throw new UnsupportedOperationException( "should be unused" );
        }
        @Override
        public VariableSpace getADefaultVariableSpace() {
          throw new UnsupportedOperationException( "should be unused" );
        }
      };
    bowl.getManager( DatabaseManagementInterface.class ).add( existingDb );
    bowl.getManager( PartitionSchemaManagementInterface.class ).add( existingPartitionSchema );
    bowl.getManager( SlaveServerManagementInterface.class ).add( existingSlaveServer );

    // NEW
    DatabaseMeta sameDb = new DatabaseMeta();
    sameDb.setName( "same" );

    SlaveServer newSlaveServer = new SlaveServer();
    newSlaveServer.setName( "new" );

    PartitionSchema changedPartitionSchema = new PartitionSchema();
    changedPartitionSchema.setName( "changeme" );
    changedPartitionSchema.setPartitionIDs( List.of( "foo", "bar" ) );

    TransMeta sourceMeta = new TransMeta();
    sourceMeta.getSharedObjectManager( DatabaseManagementInterface.class ).add( sameDb );
    sourceMeta.getSharedObjectManager( PartitionSchemaManagementInterface.class ).add( changedPartitionSchema );
    sourceMeta.getSharedObjectManager( SlaveServerManagementInterface.class ).add( newSlaveServer );

    IRepositoryImporter importer = new RepositoryImporter( null );

    Map<SharedObjectType, Map<String, ComparedState>> changes =
      SharedObjectUtil.collectChangedSharedObjects( sourceMeta, bowl, importer );

    Map<String, ComparedState> csChanges = changes.get( SharedObjectType.CLUSTERSCHEMA );
    Assert.assertNotNull( csChanges );
    Assert.assertEquals( 0, csChanges.size() );

    Map<String, ComparedState> dbChanges = changes.get( SharedObjectType.CONNECTION );
    Assert.assertNotNull( dbChanges );
    Assert.assertEquals( 1, dbChanges.size() );
    Assert.assertEquals( ComparedState.UNMODIFIED, dbChanges.get( "same" ) );

    Map<String, ComparedState> psChanges = changes.get( SharedObjectType.PARTITIONSCHEMA );
    Assert.assertNotNull( psChanges );
    Assert.assertEquals( 1, psChanges.size() );
    Assert.assertEquals( ComparedState.MODIFIED, psChanges.get( "changeme" ) );

    Map<String, ComparedState> ssChanges = changes.get( SharedObjectType.SLAVESERVER );
    Assert.assertNotNull( ssChanges );
    Assert.assertEquals( 1, ssChanges.size() );
    Assert.assertEquals( ComparedState.NEW, ssChanges.get( "new" ) );
  }

  @Test
  public void testCopySharedObjectsClearsObjectIdsOnCopiedObjects() throws Exception {
    DatabaseMeta sourceDb = new DatabaseMeta();
    sourceDb.setName( "db" );
    sourceDb.setObjectId( new StringObjectId( "db-id" ) );

    SlaveServer sourceSlave = new SlaveServer();
    sourceSlave.setName( "slave" );
    sourceSlave.setObjectId( new StringObjectId( "slave-id" ) );

    PartitionSchema sourcePartition = new PartitionSchema();
    sourcePartition.setName( "partition" );
    sourcePartition.setObjectId( new StringObjectId( "partition-id" ) );

    MemorySharedObjectsIO sharedIO = new MemorySharedObjectsIO();
    Bowl bowl = new BaseBowl() {
      @Override
      public SharedObjectsIO getSharedObjectsIO() {
        return sharedIO;
      }

      @Override
      public IMetaStore getMetastore() throws MetaStoreException {
        throw new UnsupportedOperationException( "should be unused" );
      }

      @Override
      public VariableSpace getADefaultVariableSpace() {
        throw new UnsupportedOperationException( "should be unused" );
      }
    };

    bowl.getManager( DatabaseManagementInterface.class ).add( sourceDb );
    bowl.getManager( PartitionSchemaManagementInterface.class ).add( sourcePartition );
    bowl.getManager( SlaveServerManagementInterface.class ).add( sourceSlave );

    TransMeta targetMeta = new TransMeta();
    SharedObjectUtil.copySharedObjects( bowl, targetMeta, false );

    Assert.assertNull( targetMeta.getSharedObjectManager( DatabaseManagementInterface.class ).get( "db" ).getObjectId() );
    Assert.assertNull( targetMeta.getSharedObjectManager( PartitionSchemaManagementInterface.class ).get( "partition" )
      .getObjectId() );
    Assert.assertNull( targetMeta.getSharedObjectManager( SlaveServerManagementInterface.class ).get( "slave" )
      .getObjectId() );

    // The source object in the bowl keeps its repository identity.
    Assert.assertNotNull( bowl.getManager( DatabaseManagementInterface.class ).get( "db" ).getObjectId() );
  }

  @Test
  public void testCopyAllDefaultContractKeepsObjectIds() throws Exception {
    TransMeta sourceMeta = new TransMeta();
    DatabaseMeta sourceDb = new DatabaseMeta();
    sourceDb.setName( "db" );
    sourceDb.setObjectId( new StringObjectId( "db-id" ) );
    sourceMeta.getSharedObjectManager( DatabaseManagementInterface.class ).add( sourceDb );

    TransMeta targetMeta = new TransMeta();
    SharedObjectUtil.copyAll( sourceMeta.getSharedObjectManager( DatabaseManagementInterface.class ),
      targetMeta.getSharedObjectManager( DatabaseManagementInterface.class ), false );

    Assert.assertEquals( "db-id",
      targetMeta.getSharedObjectManager( DatabaseManagementInterface.class ).get( "db" ).getObjectId().toString() );
  }

  @Test
  public void testCopyUsedDbConnectionsDefaultAndClearObjectIdContracts() throws Exception {
    DatabaseMeta sourceDb = new DatabaseMeta();
    sourceDb.setName( "used-db" );
    sourceDb.setObjectId( new StringObjectId( "used-db-id" ) );

    MemorySharedObjectsIO sharedIO = new MemorySharedObjectsIO();
    Bowl bowl = new BaseBowl() {
      @Override
      public SharedObjectsIO getSharedObjectsIO() {
        return sharedIO;
      }

      @Override
      public IMetaStore getMetastore() throws MetaStoreException {
        throw new UnsupportedOperationException( "should be unused" );
      }

      @Override
      public VariableSpace getADefaultVariableSpace() {
        throw new UnsupportedOperationException( "should be unused" );
      }
    };
    bowl.getManager( DatabaseManagementInterface.class ).add( sourceDb );

    TransMeta legacyTarget = new TransMeta() {
      @Override
      public Set<String> getUsedDatabaseConnectionNames() {
        return Set.of( "used-db" );
      }
    };

    SharedObjectUtil.copyUsedDbConnections( bowl, legacyTarget );
    Assert.assertEquals( "used-db-id",
      legacyTarget.getSharedObjectManager( DatabaseManagementInterface.class ).get( "used-db" ).getObjectId()
        .toString() );

    TransMeta clearIdTarget = new TransMeta() {
      @Override
      public Set<String> getUsedDatabaseConnectionNames() {
        return Set.of( "used-db" );
      }
    };

    SharedObjectUtil.copyUsedDbConnections( bowl, clearIdTarget, true );
    Assert.assertNull( clearIdTarget.getSharedObjectManager( DatabaseManagementInterface.class ).get( "used-db" )
      .getObjectId() );
  }


}
