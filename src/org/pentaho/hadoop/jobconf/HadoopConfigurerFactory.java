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

import org.pentaho.hadoop.HadoopConfigurerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HadoopConfigurerFactory {
  
  private static List<String> CONFIGURER_NAMES = new ArrayList<String>();
  private static Map<String, String> CONFIGURER_LOOKUP = new HashMap<String,String>();
  
  static {
    // TODO these could be read from a property/config file somewhere
    CONFIGURER_NAMES.add(GenericHadoopConfigurer.class.getName());
    CONFIGURER_NAMES.add(ClouderaHadoopConfigurer.class.getName());
    CONFIGURER_NAMES.add(MapRHadoopConfigurer.class.getName());
    
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
    throws HadoopConfigurerException {

    String implClass = CONFIGURER_LOOKUP.get(distroName);
    
    if (implClass == null) {
      throw new HadoopConfigurerException("Unknown Hadoop distribution: " + distroName);
    }

    try {
      return (HadoopConfigurer) Class.forName(implClass).newInstance();
    } catch (Exception ex) {
      throw new HadoopConfigurerException("Error loading Hadoop Configuration " + implClass, ex);
    }
  }
  
  /**
   * Get a list of configurers that we could use. This should be used if
   * locateConfigurer() is unable to return a specific configurer for
   * the installed/available hadoop distribution.
   * 
   * @return a list of configurers.
   */
  public static List<HadoopConfigurer> getAvailableConfigurers() {
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
      try {
        available.add(getConfigurer(GenericHadoopConfigurer.DISTRIBUTION_NAME));
      } catch (HadoopConfigurerException ex) {
        // Should not happen - must be caught in unit tests
        throw new IllegalStateException("Missing generic hadoop configuration");
      }
    }
    
    return available;
  }
}
