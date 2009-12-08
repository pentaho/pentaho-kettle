/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.util.List;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UICluster extends XulEventSourceAdapter {
  
  private ClusterSchema cluster;
  
  public UICluster(ClusterSchema clusterSchema) {
    this.cluster = clusterSchema;
  }
  
  public String getName() {
    if(cluster != null) {
      return cluster.getName();
    }
    return null;
  }
  
  public ClusterSchema getClusterSchema() {
    return this.cluster;
  }
  
  public String getServerList() {
    if(cluster != null) {
      List<SlaveServer> slaves = cluster.getSlaveServers();
      if(slaves != null) {
        StringBuilder sb = new StringBuilder();
        for(SlaveServer slave : slaves) {
          // Append separator before slave
          if(sb.length() > 0) {
            sb.append(", "); //$NON-NLS-1$
          }
          sb.append(slave.getName());
        }
        
        if(sb.length() > 0) {
          return sb.toString();
        }
      }
    }
    return null;
  }

}
