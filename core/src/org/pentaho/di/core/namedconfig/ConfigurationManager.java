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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.namedconfig.exceptions.ConfigurationExistsException;
import org.pentaho.di.core.namedconfig.exceptions.ConfigurationNotFoundException;
import org.pentaho.di.core.namedconfig.model.Configuration;

public class ConfigurationManager implements IConfigurationManager {

  private List<Configuration> configurations;

  public ConfigurationManager() {
    loadConfigurations();
  }

  public List<Configuration> getConfigurations() {
    return configurations;
  }

  public Configuration getConfiguration( String name ) {
    for ( Configuration configuration : configurations ) {
      if ( configuration.getName().equals( name ) ) {
        return configuration;
      }
    }
    return null;
  }

  public boolean configurationExists( String name ) {
    Configuration configuration = getConfiguration( name );
    return configuration != null;
  }

  public void saveConfiguration( Configuration configuration ) throws ConfigurationExistsException {

    if ( !configurationExists( configuration.getName() ) ) {
      configurations.add( configuration );
      serializeConfigurations();
    } else {
      throw new ConfigurationExistsException();
    }
  }

  public void deleteConfiguration( String name ) throws ConfigurationNotFoundException {

    if ( configurationExists( name ) ) {
      Configuration configuration = getConfiguration( name );
      configurations.remove( configuration );
      serializeConfigurations();
    } else {
      throw new ConfigurationNotFoundException();
    }
  }

  public List<Configuration> getConfigurationTemplates() {

    return null;
  }

  public Configuration getConfigurationTemplate( String name ) {

    return null;
  }

  private void serializeConfigurations() {

    try {
      File configFile = new File( CONFIGURATION_FILE_DIR, CONFIGURATION_FILE_NAME );
      if ( !configFile.exists() ) {
        File configDir = new File( CONFIGURATION_FILE_DIR );
        configDir.mkdirs();
      }

      FileOutputStream fos = new FileOutputStream( configFile );
      ObjectOutputStream out = new ObjectOutputStream( fos );
      out.writeObject( configurations );
      out.close();
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  private void loadConfigurations() {

    try {
      File configFile = new File( CONFIGURATION_FILE_DIR, CONFIGURATION_FILE_NAME );
      if ( configFile.exists() ) {
        FileInputStream fis = new FileInputStream( configFile );
        ObjectInputStream in = new ObjectInputStream( fis );
        configurations = (List<Configuration>) in.readObject();
        in.close();
      } else {
        configurations = new ArrayList<Configuration>();
      }
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
    } catch ( ClassNotFoundException e ) {
      e.printStackTrace();
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }
}
