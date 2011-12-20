/*
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.hadoop.jobconf;

import java.util.List;

import org.apache.hadoop.conf.Configuration;

public class MapRHadoopConfigurer extends AbstractHadoopConfigurer {
  
  protected String m_defaultCluster = "/";
  protected static final String s_protocol = "maprfs://";
  
  public String distributionName() {
    return "MapR";
  }
  
  public boolean isDetectable() {
    return true;
  }
  
  public boolean isAvailable() {
    boolean result = false;
    
    try {
      Class detected = Class.forName("com.mapr.fs.MapRFileSystem");
      if (detected != null) {
        result = true;
      }
    } catch (ClassNotFoundException ex) {
    } catch (NoClassDefFoundError ex) {
    }
    
    return result;
  }
  
  public void configure(String filesystemHost, String filesystemPort,
      String trackerHost, String trackerPort, Configuration conf,
      List<String> logMessages) throws Exception {
    
    if (filesystemHost == null || filesystemHost.length() == 0) {
      filesystemHost = m_defaultCluster;
      logMessages.add("Using MapR default cluster for filesystem");
    } else if (filesystemPort == null || filesystemPort.trim().length() == 0) {
      logMessages.add("Using MapR CLDB named cluster: " + filesystemHost 
          + " for filesystem");
      filesystemHost = "/mapr/" + filesystemHost;
    } else {
      logMessages.add("Using filesystem at " + filesystemHost + ":" + filesystemPort);
      filesystemHost = filesystemHost + ":" + filesystemPort;
    }
    
    m_filesystemURL = s_protocol + filesystemHost;
    conf.set("fs.default.name", m_filesystemURL);
    
    if (trackerHost == null || trackerHost.trim().length() == 0) {
      trackerHost = m_defaultCluster;
      logMessages.add("Using MapR default cluster for job tracker");
    } else if (trackerPort == null || trackerPort.trim().length() == 0) {
      logMessages.add("Using MapR CLDB named cluster: " + trackerHost +
      " for job tracker");
      trackerHost = "/mapr/" + trackerHost;
    } else {
      logMessages.add("Using job tracker at " + trackerHost + ":" + trackerPort);
      trackerHost = trackerHost + ":" + trackerPort;
    }
    
    m_jobtrackerURL = s_protocol + trackerHost;
    conf.set("mapred.job.tracker", m_jobtrackerURL);
    
    conf.set("fs.maprfs.impl", "com.mapr.fs.MapRFileSystem");
  }    
}
