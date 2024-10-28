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

package org.pentaho.di.partition;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.ManagerFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.shared.BaseSharedObjectsManager;
import org.pentaho.di.shared.SharedObjectsIO;
import org.w3c.dom.Node;

/**
 * This class uses the BaseSharedObjectManager to retrieve and save PartitionSchema objects. This is used by the UI.
 * <p>
 * This class provides the factory class to create instance of PartitionSchemaManager
 *
 */
public class PartitionSchemaManager extends BaseSharedObjectsManager<PartitionSchema> implements PartitionSchemaManagementInterface {

  public static final String PARTITIONSCHEMA_TYPE = SharedObjectsIO.SharedObjectType.PARTITIONSCHEMA.getName();

  public static PartitionSchemaManager getInstance( Bowl bowl) {
    return new PartitionSchemaManager( PARTITIONSCHEMA_TYPE, bowl.getSharedObjectsIO() );
  }

  protected PartitionSchemaManager (String type, SharedObjectsIO sharedObjectsIO ) {
    super( type, sharedObjectsIO );
  }

  @Override
  protected PartitionSchema createSharedObjectUsingNode( Node node ) throws KettleException {
    return new PartitionSchema( node );
  }

  /**
   * Factory for the PartitionSchemaManager. This factory class is registered with BowlFactory registry
   * during the initialization in KettleEnvironment
   */
  public static class PartitionSchemaManagerFactory implements ManagerFactory<PartitionSchemaManagementInterface> {
    public PartitionSchemaManagementInterface apply( Bowl bowl ) throws KettleException {
      return PartitionSchemaManager.getInstance( bowl );
    }
  }
}
