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

package org.pentaho.di.cluster;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.ManagerFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.shared.SharedObjectsIO;
import org.pentaho.di.shared.BaseSharedObjectsManager;

import java.util.List;
import org.w3c.dom.Node;

/**
 * This class uses the BaseSharedObjectManager to retrieve and save ClusterSchema objects. This is used by the UI.
 * <p>
 * This class provides the factory class to create instance of ClusterSchemaManager
 *
 */
public class ClusterSchemaManager extends BaseSharedObjectsManager<ClusterSchema> implements ClusterSchemaManagementInterface {

  public static final String CLUSTERSCHEMA_TYPE = SharedObjectsIO.SharedObjectType.CLUSTERSCHEMA.getName();

  private SlaveServersSupplier slaveServerSupplier;

  /**
   * Create an instance of SharedObjectManager using the Bowl's SharedObjectIO
   * @param bowl
   * @return
   */
  public static ClusterSchemaManager getInstance( Bowl bowl ) {
    return new ClusterSchemaManager( bowl.getSharedObjectsIO(), () ->
      bowl.getManager( SlaveServerManagementInterface.class ).getAll() );
  }

  protected ClusterSchemaManager( SharedObjectsIO sharedObjectsIO, SlaveServersSupplier slaveServerSupplier ) {
    super( CLUSTERSCHEMA_TYPE, sharedObjectsIO );
    this.slaveServerSupplier = slaveServerSupplier;
  }

  @Override
  protected ClusterSchema createSharedObjectUsingNode( Node node ) throws KettleException {
    return new ClusterSchema( node, slaveServerSupplier.get() );
  }


  /**
   * Factory for the ClusterSchemaManager. This factory class is registered with BowlFactory registry
   * during the initialization in KettleEnvironment
   */
  public static class ClusterSchemaManagerFactory implements ManagerFactory<ClusterSchemaManagementInterface> {
    public ClusterSchemaManagementInterface apply( Bowl bowl ) throws KettleException {
      return ClusterSchemaManager.getInstance( bowl );
    }
  }
}
