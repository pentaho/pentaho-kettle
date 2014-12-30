/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.base;

import java.util.List;

import org.pentaho.di.core.namedconfig.model.NamedConfiguration;

public interface HasNamedConfigurationsInterface {

  /**
   * Get an ArrayList of defined Configuration objects.
   *
   * @return an ArrayList of defined Configuration objects.
   */
  public List<NamedConfiguration> getNamedConfigurations();

  /**
   * Get an string array of NamedConfiguration names.
   *
   * @return a string array of NamedConfiguration names.
   */
  public String[] getNamedConfigurationNames();
  
  /**
   * @param NamedConfigurations
   *          The NamedConfigurations to set.
   */
  public void setNamedConfigurations( List<NamedConfiguration> NamedConfigurations );

  /**
   * Add a configuration connection to the transformation.
   *
   * @param Configuration
   *          The configuration connection information.
   */
  public void addNamedConfiguration( NamedConfiguration Configuration );

  /**
   * Add a configuration connection to the transformation if that connection didn't exists yet. Otherwise, replace the
   * connection in the transformation
   *
   * @param Configuration
   *          The configuration connection information.
   */
  public void addOrReplaceNamedConfiguration( NamedConfiguration Configuration );

  /**
   * Add a configuration connection to the transformation on a certain location.
   *
   * @param p
   *          The location
   * @param ci
   *          The configuration connection information.
   */
  public void addNamedConfiguration( int p, NamedConfiguration ci );

  /**
   * Retrieves a configuration connection information a a certain location.
   *
   * @param i
   *          The configuration number.
   * @return The configuration connection information.
   */
  public NamedConfiguration getNamedConfiguration( int i );

  /**
   * Removes a configuration from the transformation on a certain location.
   *
   * @param i
   *          The location
   */
  public void removeNamedConfiguration( int i );

  /**
   * Count the nr of NamedConfigurations in the transformation.
   *
   * @return The nr of NamedConfigurations
   */
  public int nrNamedConfigurations();

  /**
   * Searches the list of NamedConfigurations for a configuration with a certain name
   *
   * @param name
   *          The name of the configuration connection
   * @return The configuration connection information or null if nothing was found.
   */
  public NamedConfiguration findNamedConfiguration( String name );

  /**
   * Find the location of configuration
   *
   * @param ci
   *          The configuration queried
   * @return The location of the configuration, -1 if nothing was found.
   */
  public int indexOfNamedConfiguration( NamedConfiguration ci );

  /**
   * Checks whether or not the configurations have changed.
   *
   * @return True if the configurations have been changed.
   */
  public boolean haveNamedConfigurationsChanged();
}
