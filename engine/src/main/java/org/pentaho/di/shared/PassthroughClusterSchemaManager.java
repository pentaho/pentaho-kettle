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
import org.pentaho.di.cluster.ClusterSchemaManagementInterface;
import org.pentaho.di.cluster.ClusterSchemaManager;
import org.pentaho.di.core.exception.KettleException;
import org.w3c.dom.Node;

/**
 * This Manager that does not cache anything. Complete passthrough to the provided SharedObjectsIO instance.
 *
*/
public class PassthroughClusterSchemaManager extends PassthroughManager<ClusterSchema> implements ClusterSchemaManagementInterface {

  private SlaveServersSupplier slaveServerSupplier;

  public PassthroughClusterSchemaManager( SharedObjectsIO sharedObjectsIO, SlaveServersSupplier slaveServerSupplier ) {
    super( sharedObjectsIO, ClusterSchema.class, ClusterSchemaManager.CLUSTERSCHEMA_TYPE );
    this.slaveServerSupplier = slaveServerSupplier;
  }

  protected ClusterSchema createSharedObjectUsingNode( Node node ) throws KettleException {
    return new ClusterSchema( node, slaveServerSupplier.get() );
  }
}
