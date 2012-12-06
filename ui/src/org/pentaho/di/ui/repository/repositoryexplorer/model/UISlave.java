/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.repository.repositoryexplorer.model;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UISlave extends XulEventSourceAdapter {
  
  private SlaveServer slave;
  
  public UISlave(SlaveServer slave) {
    this.slave = slave;
  }
  
  public String getName() {
    if(slave != null) {
      return slave.getName();
    }
    return null;
  }
  
  public String getHost() {
    if(slave != null) {
      return slave.getHostname();
    }
    return null;
  }
  
  public String getPort() {
    if(slave != null) {
      return slave.getPort();
    }
    return null;
  }
  
  public SlaveServer getSlaveServer() {
    return slave;
  }
  
  public String isMaster() {
    if(slave != null) {
      if(slave.isMaster()) {
        return "Yes";
      } else {
        return "No";
      }
    }
    return "No";
  }

}
