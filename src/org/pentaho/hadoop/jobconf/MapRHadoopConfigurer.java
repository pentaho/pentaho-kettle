/*******************************************************************************
 *
 * Pentaho Big Data
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
