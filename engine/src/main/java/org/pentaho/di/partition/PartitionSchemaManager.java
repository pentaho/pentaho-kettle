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
