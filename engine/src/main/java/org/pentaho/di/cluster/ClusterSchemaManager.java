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


package org.pentaho.di.cluster;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.ManagerFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.shared.SharedObjectsIO;
import org.pentaho.di.shared.BaseSharedObjectsManager;

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
