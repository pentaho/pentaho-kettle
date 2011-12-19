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
