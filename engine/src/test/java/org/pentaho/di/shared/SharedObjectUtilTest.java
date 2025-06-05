/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2025 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.shared;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.cluster.SlaveServerManagementInterface;
import org.pentaho.di.core.bowl.BaseBowl;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.partition.PartitionSchemaManagementInterface;
import org.pentaho.di.repository.IRepositoryImporter;
import org.pentaho.di.repository.RepositoryImporter;
import org.pentaho.di.shared.SharedObjectsIO.SharedObjectType;
import org.pentaho.di.shared.SharedObjectUtil.ComparedState;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.IMetaStore;

import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;


public class SharedObjectUtilTest {

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


}
