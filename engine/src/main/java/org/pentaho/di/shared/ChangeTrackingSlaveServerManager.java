package org.pentaho.di.shared;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.cluster.SlaveServerManagementInterface;

public class ChangeTrackingSlaveServerManager extends ChangeTrackingSharedObjectManager<SlaveServer> 
  implements SlaveServerManagementInterface {

  public ChangeTrackingSlaveServerManager( SharedObjectsManagementInterface<SlaveServer> parent ) {
    super( parent );
  }
}
