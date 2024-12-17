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


package org.pentaho.di.connections.common.basic;

import org.pentaho.di.connections.vfs.BaseVFSConnectionDetails;
import org.pentaho.di.core.variables.VariableSpace;

public abstract class TestBaseVFSConnectionDetails extends BaseVFSConnectionDetails {

  VariableSpace space;

  @Override
  public VariableSpace getSpace() {
    return space;
  }

  @Override
  public void setSpace( VariableSpace space ) {
    this.space = space;
  }

}
