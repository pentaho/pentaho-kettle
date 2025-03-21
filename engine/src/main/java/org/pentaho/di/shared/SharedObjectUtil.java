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

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.ClusterSchemaManagementInterface;
import org.pentaho.di.cluster.SlaveServerManagementInterface;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.partition.PartitionSchemaManagementInterface;
import org.pentaho.di.repository.IRepositoryImporter;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.shared.SharedObjectsIO.SharedObjectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utilities for dealing with Shared Objects
 *
 */
public class SharedObjectUtil {

  public enum ComparedState {
    NEW,
    MODIFIED,
    UNMODIFIED;
  }

  /**
   * Collects the changes that would be made by moveAllSharedObjects(). This method makes no changes.
   *
   *
   * @param sourceMeta File we are moving objects out of
   * @param targetBowl bowl we are moving objects into
   *
   * @return Map&lt;SharedObjectType,Map&lt;String,ComparedState&gt;&gt; results of comparison. Inner map key is the
   *         name of the object
   */
  public static Map<SharedObjectType, Map<String, ComparedState>> collectChangedSharedObjects( AbstractMeta sourceMeta,
      Bowl targetBowl, IRepositoryImporter importer ) throws KettleException{
    Map<SharedObjectType, Map<String, ComparedState>> retMap = new HashMap<>();
    retMap.put( SharedObjectType.CLUSTERSCHEMA,
      collectChangedSharedObjects( sourceMeta.getSharedObjectManager( ClusterSchemaManagementInterface.class ),
        targetBowl.getManager( ClusterSchemaManagementInterface.class ), importer ) );
    retMap.put( SharedObjectType.CONNECTION,
      collectChangedSharedObjects( sourceMeta.getSharedObjectManager( DatabaseManagementInterface.class ),
        targetBowl.getManager( DatabaseManagementInterface.class ), importer ) );
    retMap.put( SharedObjectType.PARTITIONSCHEMA,
      collectChangedSharedObjects( sourceMeta.getSharedObjectManager( PartitionSchemaManagementInterface.class ),
        targetBowl.getManager( PartitionSchemaManagementInterface.class ), importer ) );
    retMap.put( SharedObjectType.SLAVESERVER,
      collectChangedSharedObjects( sourceMeta.getSharedObjectManager( SlaveServerManagementInterface.class ),
        targetBowl.getManager( SlaveServerManagementInterface.class ), importer ) );
    return retMap;
  }

  public static <T extends SharedObjectInterface<T> & RepositoryElementInterface>
    Map<String, ComparedState> collectChangedSharedObjects( SharedObjectsManagementInterface<T> sourceManager,
      SharedObjectsManagementInterface<T> targetManager, IRepositoryImporter importer ) throws KettleException {
    if ( sourceManager == null ) {
      return Collections.emptyMap();
    }
    Map<String, ComparedState> retMap = new HashMap<>();
    List<T> all = sourceManager.getAll();
    for ( T object : all ) {
      T destObject = targetManager.get( object.getName() );
      if ( destObject == null ) {
        retMap.put( object.getName(), ComparedState.NEW );
      } else {
        if ( importer.equals( object, destObject ) ) {
          retMap.put( object.getName(), ComparedState.UNMODIFIED );
        } else {
          retMap.put( object.getName(), ComparedState.MODIFIED );
        }
      }
    }
    return retMap;
  }

  /**
   * Moves all shared objects from the source to the target, overwriting on conflict (case insensitive)
   */
  public static void moveAllSharedObjects( AbstractMeta sourceMeta, Bowl targetBowl ) throws KettleException {
    moveAll( sourceMeta.getSharedObjectManager( ClusterSchemaManagementInterface.class ),
             targetBowl.getManager( ClusterSchemaManagementInterface.class ) );
    moveAll( sourceMeta.getSharedObjectManager( DatabaseManagementInterface.class ),
             targetBowl.getManager( DatabaseManagementInterface.class ) );
    moveAll( sourceMeta.getSharedObjectManager( PartitionSchemaManagementInterface.class ),
             targetBowl.getManager( PartitionSchemaManagementInterface.class ) );
    moveAll( sourceMeta.getSharedObjectManager( SlaveServerManagementInterface.class ),
             targetBowl.getManager( SlaveServerManagementInterface.class ) );
  }

  /**
   * Copies shared objects from the source bowl to the target AbstractMeta if they aren't already there. If onlyUsedDbs
   * is true, the only database connections that will be copied will be those used by the target meta.
   *
   *
   * @param sourceBowl
   * @param targetMeta
   */
  public static void copySharedObjects( Bowl sourceBowl, AbstractMeta targetMeta, boolean onlyUsedDbs )
      throws KettleException {
    copyAll( sourceBowl.getManager( ClusterSchemaManagementInterface.class ),
             targetMeta.getSharedObjectManager( ClusterSchemaManagementInterface.class ), false );
    copyAll( sourceBowl.getManager( PartitionSchemaManagementInterface.class ),
             targetMeta.getSharedObjectManager( PartitionSchemaManagementInterface.class ), false );
    copyAll( sourceBowl.getManager( SlaveServerManagementInterface.class ),
             targetMeta.getSharedObjectManager( SlaveServerManagementInterface.class ), false );

    if ( onlyUsedDbs ) {
      copyUsedDbConnections( sourceBowl, targetMeta );
    } else {
      copyAll( sourceBowl.getManager( DatabaseManagementInterface.class ),
               targetMeta.getSharedObjectManager( DatabaseManagementInterface.class ), false );
    }
  }

  /**
   * Moves all shared objects from the source manager to the target manager, overwriting existing objects (case
   * insensitive)
   *
   *
   * @param <T>
   * @param sourceManager
   * @param targetManager
   */
  public static <T extends SharedObjectInterface<T> & RepositoryElementInterface>
    void moveAll( SharedObjectsManagementInterface<T> sourceManager, SharedObjectsManagementInterface<T> targetManager )
      throws KettleException {
    if ( sourceManager != null && targetManager != null ) {
      copyAll( sourceManager, targetManager, true );
      sourceManager.clear();
    }
  }

  /**
   * Copies all the shared objects from the source manager to the target manager. If overwrite is true, copy will
   * happen whether or not the target manager already has the object (case insensitive). If overwrite is false, copy
   * will not happen if the target manager already has the object
   *
   *
   * @param sourceManager
   * @param targetManager
   * @param overwrite
   */
  public static <T extends SharedObjectInterface<T> & RepositoryElementInterface>
    void copyAll( SharedObjectsManagementInterface<T> sourceManager, SharedObjectsManagementInterface<T> targetManager,
      boolean overwrite ) throws KettleException {
    if ( sourceManager != null && targetManager != null ) {
      for ( T object : sourceManager.getAll() ) {
        if ( overwrite || targetManager.get( object.getName() ) == null ) {
          targetManager.add( object );
        }
      }
    }
  }

  public static void copyUsedDbConnections( Bowl sourceBowl, AbstractMeta targetMeta ) throws KettleException {
    DatabaseManagementInterface sourceManager = sourceBowl.getManager( DatabaseManagementInterface.class );
    DatabaseManagementInterface targetManager = targetMeta.getSharedObjectManager( DatabaseManagementInterface.class );

    Set<String> usedNames = targetMeta.getUsedDatabaseConnectionNames();
    for ( String name : usedNames ) {
      DatabaseMeta db = sourceManager.get( name );
      if ( db != null ) {
        if ( targetManager.get( name ) == null ){
          targetManager.add( db );
          targetMeta.databaseUpdated( name );
        }
      }
    }
  }

  /**
   * updates all the database connections from the bowl in the meta. Updates ObjectIds before import to the repository.
   * Call after moveAllSharedObjects.
   *
   *
   * @param bowl
   * @param meta
   */
  public static void patchDatabaseConnections( Bowl bowl, AbstractMeta meta ) throws KettleException {
    DatabaseManagementInterface dbMgr = bowl.getManager( DatabaseManagementInterface.class );
    for ( DatabaseMeta storedDB : dbMgr.getAll() ) {
      meta.databaseUpdated( storedDB.getName() );
    }
  }

  /**
   * Remove ObjectIds from all the Local objects. Use before serializing to some format that should not contain object
   * ids from the repository.
   *
   *
   * @param meta
   */
  public static void stripObjectIds( AbstractMeta meta ) throws KettleException {
    stripObjectIds( meta.getSharedObjectManager( ClusterSchemaManagementInterface.class ) );
    stripObjectIds( meta.getSharedObjectManager( DatabaseManagementInterface.class ) );
    stripObjectIds( meta.getSharedObjectManager( PartitionSchemaManagementInterface.class ) );
    stripObjectIds( meta.getSharedObjectManager( SlaveServerManagementInterface.class ) );
  }

  public static <T extends SharedObjectInterface<T> & RepositoryElementInterface>
      void stripObjectIds( SharedObjectsManagementInterface<T> manager ) throws KettleException {
    if ( manager == null ) {
      return;
    }
    List<T> all = manager.getAll();
    for ( T object : all ) {
      object.setObjectId( null );
      manager.add( object );
    }
  }

}

