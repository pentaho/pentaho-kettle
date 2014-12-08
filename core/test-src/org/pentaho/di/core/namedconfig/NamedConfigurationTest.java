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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.namedconfig.exceptions.ConfigurationExistsException;
import org.pentaho.di.core.namedconfig.model.Configuration;
import org.pentaho.di.core.namedconfig.model.Group;

public class NamedConfigurationTest {

  @BeforeClass
  public static void setUpOnce() {
    File configFile =
        new File( IConfigurationManager.CONFIGURATION_FILE_DIR, IConfigurationManager.CONFIGURATION_FILE_NAME );
    if ( configFile.exists() ) {
      configFile.delete();
    }
  }

  @AfterClass
  public static void tearDownOnce() {
    File configDir = new File( IConfigurationManager.CONFIGURATION_FILE_DIR );
    File configFile = new File( configDir, IConfigurationManager.CONFIGURATION_FILE_NAME );
    configFile.delete();
    configDir.delete();
  }

  @Test
  public void testSaveConfiguration() throws Exception {
    IConfigurationManager manager = new ConfigurationManager();
    Configuration configuration = new Configuration( "Config1" );
    configuration.setProperty( "Group1", "Property1", "Value1" );
    manager.saveConfiguration( configuration );
    File configFile =
        new File( IConfigurationManager.CONFIGURATION_FILE_DIR, IConfigurationManager.CONFIGURATION_FILE_NAME );
    assertTrue( configFile.exists() );
  }

  @Test
  public void testFailCreatingExistingConfiguration() throws Exception {
    try {
      IConfigurationManager manager = new ConfigurationManager();
      Configuration configuration = new Configuration( "Config1" );
      configuration.setProperty( "Group2", "Property2", "Value2" );
      manager.saveConfiguration( configuration );
      fail();
    } catch ( ConfigurationExistsException e ) {
    }
  }

  @Test
  public void testRetrieveConfiguration() throws Exception {
    IConfigurationManager manager = new ConfigurationManager();
    Configuration configuration = manager.getConfiguration( "Config1" );
    assertTrue( configuration != null );
    assertTrue( configuration.containsGroup( "Group1" ) );
    Group group = configuration.getGroup( "Group1" );
    if ( group != null ) {
      assertTrue( group.containsProperty( "Property1" ) );
    } else {
      fail();
    }
  }

  @Test
  public void testDeleteConfiguration() throws Exception {
    IConfigurationManager manager = new ConfigurationManager();
    manager.deleteConfiguration( "Config1" );
    assertFalse( manager.configurationExists( "Config1" ) );
  }
}
