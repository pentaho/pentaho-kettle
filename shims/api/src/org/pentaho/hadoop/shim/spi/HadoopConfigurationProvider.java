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

package org.pentaho.hadoop.shim.spi;

import org.pentaho.hadoop.shim.ConfigurationException;
import org.pentaho.hadoop.shim.HadoopConfiguration;

import java.util.List;

/**
 * Provides a mechanism to load Hadoop configurations.
 * 
 * @author Jordan Ganoff (jganoff@pentaho.com)
 */
public interface HadoopConfigurationProvider {
  
  /**
   * Query this provider to determine if it can provide a specific configuration.
   *
   * @param id Identifier of configuration to check for
   * @return {@code true} if the configuration can be obtained with this provider.
   */
  public boolean hasConfiguration(String id);

  /**
   * Retrieve all known configurations.
   *
   * @return List of all configurations available through this provider.
   */
  public List<? extends HadoopConfiguration> getConfigurations();

  /**
   * Retrieve a configuration by identifier.
   *
   * @param id Identifier of the configuration to retrieve
   * @return The Hadoop connection whose id matches the provided one.
   * @throws ConfigurationException Error retrieving the desired configuration
   */
  public HadoopConfiguration getConfiguration(String id) throws ConfigurationException;

  /**
   * Retrieve the current "active" Hadoop configuration.
   * 
   * @return The currently active Hadoop configuration 
   * @throws ConfigurationException Error retrieving the active configuration
   */
  public HadoopConfiguration getActiveConfiguration() throws ConfigurationException;
}
