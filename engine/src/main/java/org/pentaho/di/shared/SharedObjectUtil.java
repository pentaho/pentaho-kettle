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
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.partition.PartitionSchemaManagementInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for dealing with Shared Objects
 *
 */
public class SharedObjectUtil {

  /**
   * Moves all shared objects from the source to the target
   */
  public static void moveAllSharedObjects( AbstractMeta sourceMeta, Bowl targetBowl ) throws KettleException {
    moveAll( sourceMeta.getSharedObjectManager( ClusterSchemaManagementInterface.class ),
             targetBowl.getManager(ClusterSchemaManagementInterface.class ) );
    moveAll( sourceMeta.getSharedObjectManager( DatabaseManagementInterface.class ),
             targetBowl.getManager( DatabaseManagementInterface.class ) );
    moveAll( sourceMeta.getSharedObjectManager( PartitionSchemaManagementInterface.class ),
             targetBowl.getManager( PartitionSchemaManagementInterface.class ) );
    moveAll( sourceMeta.getSharedObjectManager( SlaveServerManagementInterface.class ),
             targetBowl.getManager( SlaveServerManagementInterface.class ) );
  }

  public static <T extends SharedObjectInterface<T>> void moveAll( SharedObjectsManagementInterface<T> sourceManager,
    SharedObjectsManagementInterface<T> targetManager ) throws KettleException {
    if ( sourceManager != null && targetManager != null ) {
      copyAll( sourceManager, targetManager );
      sourceManager.clear();
    }
  }

  public static <T extends SharedObjectInterface<T>> void copyAll( SharedObjectsManagementInterface<T> sourceManager,
    SharedObjectsManagementInterface<T> targetManager ) throws KettleException {
    if ( sourceManager != null && targetManager != null ) {
      for ( T object : sourceManager.getAll() ) {
        targetManager.add( object );
      }
    }
  }

}

