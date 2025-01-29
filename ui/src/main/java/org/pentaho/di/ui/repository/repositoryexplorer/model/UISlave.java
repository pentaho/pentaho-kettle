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


package org.pentaho.di.ui.repository.repositoryexplorer.model;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UISlave extends XulEventSourceAdapter {

  private SlaveServer slave;

  public UISlave( SlaveServer slave ) {
    this.slave = slave;
  }

  public String getName() {
    if ( slave != null ) {
      return slave.getName();
    }
    return null;
  }

  public String getHost() {
    if ( slave != null ) {
      return slave.getHostname();
    }
    return null;
  }

  public String getPort() {
    if ( slave != null ) {
      return slave.getPort();
    }
    return null;
  }

  public SlaveServer getSlaveServer() {
    return slave;
  }

  public String isMaster() {
    if ( slave != null ) {
      if ( slave.isMaster() ) {
        return "Yes";
      } else {
        return "No";
      }
    }
    return "No";
  }

}
