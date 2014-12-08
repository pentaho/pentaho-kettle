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

package org.pentaho.di.core.namedconfig;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.namedconfig.exceptions.ConfigurationExistsException;
import org.pentaho.di.core.namedconfig.exceptions.ConfigurationNotFoundException;
import org.pentaho.di.core.namedconfig.model.Configuration;

public interface IConfigurationManager {

  public static String CONFIGURATION_FILE_NAME = "configurations.ser";
  public static String CONFIGURATION_FILE_DIR = Const.getKettleDirectory() + Const.FILE_SEPARATOR + "namedconfig";

  public List<Configuration> getConfigurations();

  public Configuration getConfiguration( String name );

  public boolean configurationExists( String name );

  public void saveConfiguration( Configuration configuration ) throws ConfigurationExistsException;

  public void deleteConfiguration( String name ) throws ConfigurationNotFoundException;

  public List<Configuration> getConfigurationTemplates();

  public Configuration getConfigurationTemplate( String name );

}
