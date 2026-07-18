/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/
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
