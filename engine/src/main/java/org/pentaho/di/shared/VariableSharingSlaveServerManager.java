package org.pentaho.di.shared;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.cluster.SlaveServerManagementInterface;
import org.pentaho.di.core.variables.VariableSpace;

public class VariableSharingSlaveServerManager extends VariableSharingSharedObjectManager<SlaveServer>
  implements SlaveServerManagementInterface {

  public VariableSharingSlaveServerManager( VariableSpace variables,
                                            SharedObjectsManagementInterface<SlaveServer> parent ) {
    super( variables, parent );
  }

}
