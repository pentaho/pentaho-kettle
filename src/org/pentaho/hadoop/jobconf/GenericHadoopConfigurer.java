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

public class GenericHadoopConfigurer extends AbstractHadoopConfigurer {
  
  public static final String DISTRIBUTION_NAME = "generic";
  
  protected String m_defaultHDFSPort = "9000";
  protected String m_defaultTrackerPort = "9001";
  
  public String distributionName() {
    return DISTRIBUTION_NAME;
  }
  
  public void configure(String filesystemHost, String filesystemPort,
      String trackerHost, String trackerPort, Configuration conf,
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
