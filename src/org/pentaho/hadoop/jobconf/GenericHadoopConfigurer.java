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

import org.apache.hadoop.mapred.JobConf;

public class GenericHadoopConfigurer extends AbstractHadoopConfigurer {
  
  protected String m_defaultHDFSPort = "9000";
  protected String m_defaultTrackerPort = "9001";
  
  public String distributionName() {
    return "Generic";
  }
  
  public void configure(String filesystemHost, String filesystemPort,
      String trackerHost, String trackerPort, JobConf conf,
      List<String> logMessages) throws Exception {
    
    if (filesystemHost == null || filesystemHost.trim().length() == 0) {
      throw new Exception("No hdfs host specified!");
    }
    
    if (filesystemPort == null || filesystemPort.trim().length() == 0) {
      logMessages.add("No hdfs port specified - using default: " + m_defaultHDFSPort);
      filesystemPort = m_defaultHDFSPort;
    }
    
    m_filesystemURL = "hdfs://" + filesystemHost + ":" + filesystemPort;
    
    if (trackerHost == null || trackerHost.trim().length() == 0) {
      throw new Exception("No job tracker host specified!");
    }
    
    if (trackerPort == null || trackerPort.trim().length() == 0) {
      trackerPort = m_defaultTrackerPort;
      logMessages.add("No job tracker port specified - using default: " + m_defaultTrackerPort);
    }
    
    m_jobtrackerURL = trackerHost + ":" + trackerPort;
    
    conf.set("fs.default.name", m_filesystemURL);
    conf.set("mapred.job.tracker", m_jobtrackerURL);
  }
}
