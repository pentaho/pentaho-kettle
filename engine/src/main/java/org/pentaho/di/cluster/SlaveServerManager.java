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
 * This class uses the BaseSharedObjectManager to retrieve and save SlaveServer objects. This is used by the UI.
 * <p>
 * This class provides the factory class to create instance of SlaveServerManager
 *
 */
public class SlaveServerManager extends BaseSharedObjectsManager<SlaveServer> implements SlaveServerManagementInterface {

  public static final String SLAVESERVER_TYPE = SharedObjectsIO.SharedObjectType.SLAVESERVER.getName();

  /**
   * Create an instance of SharedObjectManager using the Bowl's SharedObjectIO
   * @param bowl
   * @return
   */
  public static SlaveServerManager getInstance( Bowl bowl ) {
    return new SlaveServerManager( SLAVESERVER_TYPE, bowl.getSharedObjectsIO() );
  }

  protected SlaveServerManager( String type, SharedObjectsIO sharedObjectsIO ) {
    super( type, sharedObjectsIO );
  }

  @Override
  protected SlaveServer createSharedObjectUsingNode( Node node ) {
    return new SlaveServer( node );
  }


  /**
   * Factory for the SlaveServerManager. This factory class is registered with BowlFactory registry
   * during the initialization in KettleEnvironment
   */
  public static class SlaveServerManagerFactory implements ManagerFactory<SlaveServerManagementInterface> {
    public SlaveServerManagementInterface apply( Bowl bowl ) throws KettleException {
      return SlaveServerManager.getInstance( bowl );
    }
  }
}
