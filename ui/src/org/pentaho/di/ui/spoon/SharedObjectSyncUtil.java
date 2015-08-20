/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

import java.util.Collections;
import java.util.List;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegates;

/**
 * This utility provides methods for synchronization of kettle's shared objects.
 * 
 */
public class SharedObjectSyncUtil {

  private final ConnectionSynchronizationHandler connectionSynchronizationHandler =
      new ConnectionSynchronizationHandler();

  private final SlaveServerSynchronizationHandler slaveServerSynchronizationHandler =
      new SlaveServerSynchronizationHandler();

  private final ClusterSchemaSynchronizationHandler clusterSchemaSynchronizationHandler =
      new ClusterSchemaSynchronizationHandler();

  private final PartitionSchemaSynchronizationHandler partitionSchemaSynchronizationHandler =
      new PartitionSchemaSynchronizationHandler();

  private final StepMetaSynchronizationHandler stepMetaSynchronizationHandler = new StepMetaSynchronizationHandler();

  private final SpoonDelegates spoonDelegates;

  public SharedObjectSyncUtil( SpoonDelegates spoonDelegates ) {
    this.spoonDelegates = spoonDelegates;
  }

  /**
   * Synchronizes data from <code>database</code> to shared databases.
   * 
   * @param database
   *          data to share
   */
  public synchronized void synchronizeConnections( DatabaseMeta database ) {
    if ( !database.isShared() ) {
      return;
    }
    synchronizeJobs( database, connectionSynchronizationHandler );
    synchronizeTransformations( database, connectionSynchronizationHandler );
  }

  /**
   * Synchronizes data from <code>slaveServer</code> to shared slave servers.
   * 
   * @param slaveServer
   *          data to share
   */
  public synchronized void synchronizeSlaveServers( SlaveServer slaveServer ) {
    if ( !slaveServer.isShared() ) {
      return;
    }
    synchronizeJobs( slaveServer, slaveServerSynchronizationHandler );
    synchronizeTransformations( slaveServer, slaveServerSynchronizationHandler );
  }

  /**
   * Synchronizes data from <code>clusterSchema</code> to shared cluster schemas.
   * 
   * @param clusterSchema
   *          data to share
   */
  public synchronized void synchronizeClusterSchemas( ClusterSchema clusterSchema ) {
    if ( !clusterSchema.isShared() ) {
      return;
    }
    synchronizeTransformations( clusterSchema, clusterSchemaSynchronizationHandler );
  }

  /**
   * Synchronizes data from <code>clusterSchema</code> to shared partition schemas.
   * 
   * @param partitionSchema
   *          data to share
   */
  public synchronized void synchronizePartitionSchemas( PartitionSchema partitionSchema ) {
    if ( !partitionSchema.isShared() ) {
      return;
    }
    synchronizeTransformations( partitionSchema, partitionSchemaSynchronizationHandler );
  }

  /**
   * Synchronizes data from <code>clusterSchema</code> to shared steps.
   * 
   * @param step
   *          data to shares
   */
  public synchronized void synchronizeSteps( StepMeta step ) {
    if ( !step.isShared() ) {
      return;
    }
    synchronizeTransformations( step, stepMetaSynchronizationHandler );
  }

  private void synchronizeJobs( SharedObjectInterface sourceObject, SynchronizationHandler handler ) {
    for ( JobMeta currentJob : spoonDelegates.jobs.getLoadedJobs() ) {
      List<? extends SharedObjectInterface> objectsForSync = handler.getObjectsForSyncFromJob( currentJob );
      synchronize( sourceObject, objectsForSync, handler );
    }
  }

  private void synchronizeTransformations( SharedObjectInterface object, SynchronizationHandler handler ) {
    for ( TransMeta currentTransformation : spoonDelegates.trans.getLoadedTransformations() ) {
      List<? extends SharedObjectInterface> objectsForSync =
          handler.getObjectsForSyncFromTransformation( currentTransformation );
      synchronize( object, objectsForSync, handler );
    }
  }

  private static void synchronize( SharedObjectInterface object, List<? extends SharedObjectInterface> objectsForSync,
      SynchronizationHandler handler ) {
    for ( SharedObjectInterface objectForSync : objectsForSync ) {
      if ( objectForSync.isShared() && objectForSync.getName().equals( object.getName() ) && objectForSync != object ) {
        handler.doSynchronization( object, objectForSync );
        break;
      }
    }
  }

  public static interface SynchronizationHandler {

    List<? extends SharedObjectInterface> getObjectsForSyncFromJob( JobMeta job );

    List<? extends SharedObjectInterface> getObjectsForSyncFromTransformation( TransMeta transformation );

    void doSynchronization( SharedObjectInterface source, SharedObjectInterface target );

  }

  private static class ConnectionSynchronizationHandler implements SynchronizationHandler {

    @Override
    public List<DatabaseMeta> getObjectsForSyncFromJob( JobMeta job ) {
      return job.getDatabases();
    }

    @Override
    public List<DatabaseMeta> getObjectsForSyncFromTransformation( TransMeta transformation ) {
      return transformation.getDatabases();
    }

    @Override
    public void doSynchronization( SharedObjectInterface source, SharedObjectInterface target ) {
      ( (DatabaseMeta) target ).replaceMeta( (DatabaseMeta) source );
    }

  }

  private static class SlaveServerSynchronizationHandler implements SynchronizationHandler {

    @Override
    public List<SlaveServer> getObjectsForSyncFromJob( JobMeta job ) {
      return job.getSlaveServers();
    }

    @Override
    public List<SlaveServer> getObjectsForSyncFromTransformation( TransMeta transformation ) {
      return transformation.getSlaveServers();
    }

    @Override
    public void doSynchronization( SharedObjectInterface source, SharedObjectInterface target ) {
      ( (SlaveServer) target ).replaceMeta( (SlaveServer) source );
    }

  }

  private static class ClusterSchemaSynchronizationHandler implements SynchronizationHandler {

    @Override
    public List<ClusterSchema> getObjectsForSyncFromJob( JobMeta job ) {
      return Collections.emptyList();
    }

    @Override
    public List<ClusterSchema> getObjectsForSyncFromTransformation( TransMeta transformation ) {
      return transformation.getClusterSchemas();
    }

    @Override
    public void doSynchronization( SharedObjectInterface source, SharedObjectInterface target ) {
      ( (ClusterSchema) target ).replaceMeta( (ClusterSchema) source );
    }

  }

  private static class PartitionSchemaSynchronizationHandler implements SynchronizationHandler {

    @Override
    public List<PartitionSchema> getObjectsForSyncFromJob( JobMeta job ) {
      return Collections.emptyList();
    }

    @Override
    public List<PartitionSchema> getObjectsForSyncFromTransformation( TransMeta transformation ) {
      return transformation.getPartitionSchemas();
    }

    @Override
    public void doSynchronization( SharedObjectInterface source, SharedObjectInterface target ) {
      ( (PartitionSchema) target ).replaceMeta( (PartitionSchema) source );
    }

  }

  private static class StepMetaSynchronizationHandler implements SynchronizationHandler {

    @Override
    public List<StepMeta> getObjectsForSyncFromJob( JobMeta job ) {
      return Collections.emptyList();
    }

    @Override
    public List<StepMeta> getObjectsForSyncFromTransformation( TransMeta transformation ) {
      return transformation.getSteps();
    }

    @Override
    public void doSynchronization( SharedObjectInterface source, SharedObjectInterface target ) {
      ( (StepMeta) target ).replaceMeta( (StepMeta) source );
    }

  }

}
