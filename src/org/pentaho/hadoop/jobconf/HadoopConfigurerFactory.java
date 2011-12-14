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

public class HadoopConfigurerFactory {
  
  public static HadoopConfigurer getConfigurer(String distroName) 
    throws Exception {
    if (distroName.equalsIgnoreCase("generic")) {
      return new GenericHadoopConfigurer();
    }
    
    if (distroName.equalsIgnoreCase("cloudera")) {
      return new ClouderaHadoopConfigurer();
    }
    
    if (distroName.equalsIgnoreCase("mapr")) {
      return new MapRHadoopConfigurer();
    }
    
    throw new Exception("Unknown Hadoop distribution: " + distroName);
  }    
}
