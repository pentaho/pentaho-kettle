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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HadoopConfigurerFactory {
  
  private static List<String> CONFIGURER_NAMES = new ArrayList<String>();
  private static Map<String, String> CONFIGURER_LOOKUP = new HashMap<String,String>();
  
  static {
    // TODO these could be read from a property/config file somewhere
    CONFIGURER_NAMES.add("org.pentaho.hadoop.jobconf.GenericHadoopConfigurer");
    CONFIGURER_NAMES.add("org.pentaho.hadoop.jobconf.ClouderaHadoopConfigurer");
    CONFIGURER_NAMES.add("org.pentaho.hadoop.jobconf.MapRHadoopConfigurer");
    
    for (String cname : CONFIGURER_NAMES) {
      try {
        HadoopConfigurer config = (HadoopConfigurer)Class.forName(cname).newInstance();
        CONFIGURER_LOOKUP.put(config.distributionName(), cname);
      } catch (Exception ex) { }
    }
  }
  
  public static HadoopConfigurer locateConfigurer() {
    HadoopConfigurer detected = null;
    
    for (String conf : CONFIGURER_LOOKUP.keySet()) {
      try {
        HadoopConfigurer config = getConfigurer(conf);
        if (config.isAvailable()) {
          detected = config;
          break;
        }
      } catch (Exception ex) { }
    }    
    
    return detected;
  }
  
  
  /**
   * Get the named configuerer
   * 
   * @param distroName the name of the distribution
   * @return the corresponding configurer
   * @throws Exception if the named configurer is unknown
   */
  public static HadoopConfigurer getConfigurer(String distroName) 
    throws Exception {

    String implClass = CONFIGURER_LOOKUP.get(distroName);
    
    if (implClass == null) {
      throw new Exception("Unknown Hadoop distribution: " + distroName);
    }
    

    HadoopConfigurer config = (HadoopConfigurer)Class.forName(implClass).newInstance();
    return config;    
  }
  
  /**
   * Get a list of configurers that we could use. This should be used if
   * locateConfigurer() is unable to return a specific configurer for
   * the installed/available hadoop distribution.
   * 
   * @return a list of configurers.
   * @throws Exception if no configurers are available
   */
  public static List<HadoopConfigurer> getAvailableConfigurers() 
    throws Exception {
    List<HadoopConfigurer> available = new ArrayList<HadoopConfigurer>();
    for (String conf : CONFIGURER_LOOKUP.keySet()) {
      try {
        HadoopConfigurer config = getConfigurer(conf);
        
        // available configurers to choose from are those that can't auto
        // detect if their respective distributions are installed/available
        if (!config.isDetectable()) {
          available.add(config);
        }
      } catch (Exception ex) { }
    }
    
    if (available.size() == 0) {
      // make sure that generic is always available      
      available.add(getConfigurer("generic"));
    }
    
    return available;
  }
}
