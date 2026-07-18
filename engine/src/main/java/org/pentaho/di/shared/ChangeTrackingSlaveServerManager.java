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

public class ChangeTrackingSlaveServerManager extends ChangeTrackingSharedObjectManager<SlaveServer> 
  implements SlaveServerManagementInterface {

  public ChangeTrackingSlaveServerManager( SharedObjectsManagementInterface<SlaveServer> parent ) {
    super( parent );
  }
}
