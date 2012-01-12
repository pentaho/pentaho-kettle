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

public interface HadoopConfigurer {
  
  String distributionName();
  
  String getFilesystemURL();
  String getJobtrackerURL();
  
  /**
   * If it is possible to detect which distribution is installed, 
   * then implementers should return true if they detect that their 
   * distribution is in use and false otherwise
   * 
   * @return true if the specific hadoop distribution is in use
   */
  boolean isAvailable();
  
  /**
   * If it is possible to detect that this particular
   * distribution is installed then implementers should
   * return true
   * 
   * @return true if it is possible to detect whether this
   * distribution is installed/available.
   */
  boolean isDetectable();
  
  /**
   * Setup the config object based on the supplied information with
   * respect to the specific distribution
   * 
   * @param filesystemHost
   * @param filesystemPort
   * @param trackerHost
   * @param trackerPort
   * @param conf
   * @param logMessages
   * @throws Exception
   */
  void configure(String filesystemHost, String filesystemPort, 
      String trackerHost, String trackerPort, Configuration conf, 
      List<String> logMessages) throws Exception;
}
