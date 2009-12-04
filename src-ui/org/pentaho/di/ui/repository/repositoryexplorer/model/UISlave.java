/* Copyright (c) 2009 Pentaho Corporation.  All rights reserved. 
* This software was developed by Pentaho Corporation and is provided under the terms 
* of the GNU Lesser General Public License, Version 2.1. You may not use 
* this file except in compliance with the license. If you need a copy of the license, 
* please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
* Data Integration.  The Initial Developer is Pentaho Corporation.
*
* Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
* the license for the specific language governing your rights and limitations.*/
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
