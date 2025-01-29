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

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.cluster.SlaveServerManagementInterface;
import org.pentaho.di.cluster.SlaveServerManager;
import org.pentaho.di.core.exception.KettleException;
import org.w3c.dom.Node;

/**
 * This Manager that does not cache anything. Complete passthrough to the provided SharedObjectsIO instance.
 *
*/
public class PassthroughSlaveServerManager extends PassthroughManager<SlaveServer> implements SlaveServerManagementInterface {

  public PassthroughSlaveServerManager( SharedObjectsIO sharedObjectsIO ) {
    super( sharedObjectsIO, SlaveServer.class, SlaveServerManager.SLAVESERVER_TYPE );
  }

  protected SlaveServer createSharedObjectUsingNode( Node node ) throws KettleException {
    return new SlaveServer( node );
  }
}
