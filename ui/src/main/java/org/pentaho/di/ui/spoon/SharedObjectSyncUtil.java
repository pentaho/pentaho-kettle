/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.RepositoryElementInterface;
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
  private final Spoon spoon;

  public SharedObjectSyncUtil( Spoon spoon ) {
    this.spoonDelegates = spoon.delegates;
    this.spoon = spoon;
    spoonDelegates.slaves.setSharedObjectSyncUtil( this );
    spoonDelegates.clusters.setSharedObjectSyncUtil( this );
    spoonDelegates.partitions.setSharedObjectSyncUtil( this );
  }

  public synchronized void synchronizeConnections( DatabaseMeta database ) {
    synchronizeConnections( database, database.getName() );
  }
  /**
   * Synchronizes data from <code>database</code> to shared databases.
   * 
   * @param database
   *          data to share
   */
  public synchronized void synchronizeConnections( DatabaseMeta database, String originalName ) {
    if ( !database.isShared() ) {
      return;
    }
    synchronizeJobs( database, connectionSynchronizationHandler, originalName );
    synchronizeTransformations( database, connectionSynchronizationHandler, originalName );
    saveSharedObjects();
  }

  private void saveSharedObjects() {
    try {
      // flush to file for newly opened
      spoon.getActiveMeta().saveSharedObjects();
    } catch ( KettleException e ) {
      spoon.getLog().logError( e.getLocalizedMessage(), e );
    }
  }

  /**
   * Synchronizes data from <code>slaveServer</code> to shared slave servers.
   * 
   * @param slaveServer
   *          data to share
   */
  public synchronized void synchronizeSlaveServers( SlaveServer slaveServer ) {
    synchronizeSlaveServers( slaveServer, slaveServer.getName() );
  }

  public synchronized void synchronizeSlaveServers( SlaveServer slaveServer, String originalName ) {
    if ( slaveServer.isShared() ) {
      synchronizeJobs( slaveServer, slaveServerSynchronizationHandler, originalName );
      synchronizeTransformations( slaveServer, slaveServerSynchronizationHandler, originalName );
      saveSharedObjects();
    }
    if ( slaveServer.getObjectId() != null ) {
      updateRepositoryObjects( slaveServer, slaveServerSynchronizationHandler );
    }
  }

  public synchronized void deleteSlaveServer( SlaveServer removed ) {
    synchronizeAll( true, meta -> meta.getSlaveServers().remove( removed ) );
  }

  public synchronized void deleteClusterSchema( ClusterSchema removed ) {
    synchronizeTransformations( true, transMeta -> transMeta.getClusterSchemas().remove( removed ) );
  }

  public synchronized void deletePartitionSchema( PartitionSchema removed ) {
    synchronizeTransformations( true, transMeta -> transMeta.getPartitionSchemas().remove( removed ) );
  }

  private <T extends SharedObjectInterface & RepositoryElementInterface>
    void updateRepositoryObjects( T updatedObject, SynchronizationHandler<T> handler ) {
    synchronizeJobs( true, job -> synchronizeByObjectId( updatedObject, handler.getObjectsForSyncFromJob( job ), handler ) );
    synchronizeTransformations( true, trans ->
      synchronizeByObjectId( updatedObject, handler.getObjectsForSyncFromTransformation( trans ), handler ) );
  }

  private synchronized void synchronizeJobs( boolean includeActive, Consumer<JobMeta> synchronizeAction ) {
    JobMeta current = spoon.getActiveJob();
    for ( JobMeta job : spoonDelegates.jobs.getLoadedJobs() ) {
      if ( includeActive || job != current ) {
        synchronizeAction.accept( job );
      }
    }
  }

  private synchronized void synchronizeTransformations( boolean includeActive, Consumer<TransMeta> synchronizeAction  ) {
    TransMeta current = spoon.getActiveTransformation();
    for ( TransMeta trans : spoonDelegates.trans.getLoadedTransformations() ) {
      if ( includeActive || trans != current ) {
        synchronizeAction.accept( trans );
      }
    }
  }

  private synchronized void synchronizeAll( boolean includeActive, Consumer<AbstractMeta> synchronizeAction  ) {
    EngineMetaInterface current = spoon.getActiveMeta();
    for ( TransMeta trans : spoonDelegates.trans.getLoadedTransformations() ) {
      if ( includeActive || trans != current ) {
        synchronizeAction.accept( trans );
      }
    }
    for ( JobMeta job : spoonDelegates.jobs.getLoadedJobs() ) {
      if ( includeActive || job != current ) {
        synchronizeAction.accept( job );
      }
    }
  }

  /**
   * Synchronizes data from <code>clusterSchema</code> to shared cluster schemas.
   * 
   * @param clusterSchema
   *          data to share
   */
  public synchronized void synchronizeClusterSchemas( ClusterSchema clusterSchema ) {
    synchronizeClusterSchemas( clusterSchema, clusterSchema.getName() );
  }

  public synchronized void synchronizeClusterSchemas( ClusterSchema clusterSchema, String originalName ) {
    if ( clusterSchema.isShared() ) {
      synchronizeTransformations( clusterSchema, clusterSchemaSynchronizationHandler, originalName );
    }
    if ( clusterSchema.getObjectId() != null ) {
      updateRepositoryObjects( clusterSchema, clusterSchemaSynchronizationHandler );
    }
  }

  /**
   * Synchronizes data from <code>clusterSchema</code> to shared partition schemas.
   * 
   * @param partitionSchema
   *          data to share
   */
  public synchronized void synchronizePartitionSchemas( PartitionSchema partitionSchema ) {
    synchronizePartitionSchemas( partitionSchema, partitionSchema.getName() );
  }

  public synchronized void synchronizePartitionSchemas( PartitionSchema partitionSchema, String originalName ) {
    if ( partitionSchema.isShared() ) {
      synchronizeTransformations( partitionSchema, partitionSchemaSynchronizationHandler, originalName );
    }
    if ( partitionSchema.getObjectId() != null ) {
      updateRepositoryObjects( partitionSchema, partitionSchemaSynchronizationHandler );
    }
  }

  /**
   * Synchronizes data from <code>clusterSchema</code> to shared steps.
   * 
   * @param step
   *          data to shares
   */
  public synchronized void synchronizeSteps( StepMeta step ) {
    synchronizeSteps( step, step.getName() );
  }

  public synchronized void synchronizeSteps( StepMeta step, String originalName ) {
    if ( !step.isShared() ) {
      return;
    }
    synchronizeTransformations( step, stepMetaSynchronizationHandler, step.getName() );
  }

  private void logError( KettleException e ) {
    if ( spoon.getLog() != null ) {
      spoon.getLog().logError( e.getLocalizedMessage(), e );
    }
  }

  private <T extends SharedObjectInterface> void synchronizeJobs( T sourceObject, SynchronizationHandler<T> handler,
      String originalName ) {
    for ( JobMeta currentJob : spoonDelegates.jobs.getLoadedJobs() ) {
      List<T> objectsForSync = handler.getObjectsForSyncFromJob( currentJob );
      synchronizeShared( sourceObject, originalName, objectsForSync, handler );
    }
  }

  private <T extends SharedObjectInterface> void synchronizeTransformations( T object,
      SynchronizationHandler<T> handler, String originalName ) {
    for ( TransMeta currentTransformation : spoonDelegates.trans.getLoadedTransformations() ) {
      List<T> objectsForSync =
          handler.getObjectsForSyncFromTransformation( currentTransformation );
      synchronizeShared( object, originalName, objectsForSync, handler );
    }
  }

  private static <T extends SharedObjectInterface> void synchronizeShared(
      T object, String name, List<T> objectsForSync, SynchronizationHandler<T> handler ) {
    synchronize( object, toSync -> toSync.isShared() && toSync.getName().equals( name ), objectsForSync, handler );
  }

  private static <T extends SharedObjectInterface & RepositoryElementInterface>
    void synchronizeByObjectId( T object, List<T> objectsForSync, SynchronizationHandler<T> handler ) {
    synchronize( object, toSync -> object.getObjectId().equals( toSync.getObjectId() ), objectsForSync, handler );
  }

  private static <T extends SharedObjectInterface> void synchronize( T object, Predicate<T> pred, List<T> objectsForSync,
      SynchronizationHandler<T> handler ) {
    for ( T objectToSync : objectsForSync ) {
      if ( pred.test( objectToSync ) && object != objectToSync ) {
        handler.doSynchronization( object, objectToSync );
      }
    }
  }

  protected static interface SynchronizationHandler<T extends SharedObjectInterface> {

    List<T> getObjectsForSyncFromJob( JobMeta job );

    List<T> getObjectsForSyncFromTransformation( TransMeta transformation );

    void doSynchronization( T source, T target );

  }

  private static class ConnectionSynchronizationHandler implements SynchronizationHandler<DatabaseMeta> {

    @Override
    public List<DatabaseMeta> getObjectsForSyncFromJob( JobMeta job ) {
      return job.getDatabases();
    }

    @Override
    public List<DatabaseMeta> getObjectsForSyncFromTransformation( TransMeta transformation ) {
      return transformation.getDatabases();
    }

    @Override
    public void doSynchronization( DatabaseMeta source, DatabaseMeta target ) {
      target.replaceMeta( source );
    }

  }

  private static class SlaveServerSynchronizationHandler implements SynchronizationHandler<SlaveServer> {

    @Override
    public List<SlaveServer> getObjectsForSyncFromJob( JobMeta job ) {
      return job.getSlaveServers();
    }

    @Override
    public List<SlaveServer> getObjectsForSyncFromTransformation( TransMeta transformation ) {
      return transformation.getSlaveServers();
    }

    @Override
    public void doSynchronization( SlaveServer source, SlaveServer target ) {
      target.replaceMeta( source );
    }

  }

  private static class ClusterSchemaSynchronizationHandler implements SynchronizationHandler<ClusterSchema> {

    @Override
    public List<ClusterSchema> getObjectsForSyncFromJob( JobMeta job ) {
      return Collections.emptyList();
    }

    @Override
    public List<ClusterSchema> getObjectsForSyncFromTransformation( TransMeta transformation ) {
      return transformation.getClusterSchemas();
    }

    @Override
    public void doSynchronization( ClusterSchema source, ClusterSchema target ) {
      target.replaceMeta( source );
    }

  }

  private static class PartitionSchemaSynchronizationHandler implements SynchronizationHandler<PartitionSchema> {

    @Override
    public List<PartitionSchema> getObjectsForSyncFromJob( JobMeta job ) {
      return Collections.emptyList();
    }

    @Override
    public List<PartitionSchema> getObjectsForSyncFromTransformation( TransMeta transformation ) {
      return transformation.getPartitionSchemas();
    }

    @Override
    public void doSynchronization( PartitionSchema source, PartitionSchema target ) {
      target.replaceMeta( source );
    }

  }

  private static class StepMetaSynchronizationHandler implements SynchronizationHandler<StepMeta> {

    @Override
    public List<StepMeta> getObjectsForSyncFromJob( JobMeta job ) {
      return Collections.emptyList();
    }

    @Override
    public List<StepMeta> getObjectsForSyncFromTransformation( TransMeta transformation ) {
      return transformation.getSteps();
    }

    @Override
    public void doSynchronization( StepMeta source, StepMeta target ) {
      target.replaceMeta( source );
    }

  }

}
