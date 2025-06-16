/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
    super( sharedObjectsIO, ClusterSchemaManager.CLUSTERSCHEMA_TYPE );
    this.slaveServerSupplier = slaveServerSupplier;
  }

  protected ClusterSchema createSharedObjectUsingNode( Node node ) throws KettleException {
    return new ClusterSchema( node, slaveServerSupplier.get() );
  }
}
