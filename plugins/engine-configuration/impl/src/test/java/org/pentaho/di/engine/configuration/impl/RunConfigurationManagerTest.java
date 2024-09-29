/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.di.engine.configuration.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationProvider;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfiguration;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfigurationExecutor;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfigurationProvider;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by bmorrise on 3/15/17.
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class RunConfigurationManagerTest {

  private RunConfigurationManager executionConfigurationManager;

  @Before
  public void setup() throws Exception {

    MemoryMetaStore memoryMetaStore = new MemoryMetaStore();
    CheckedMetaStoreSupplier metastoreSupplier = () -> memoryMetaStore;

    DefaultRunConfigurationProvider defaultRunConfigurationProvider =
      new DefaultRunConfigurationProvider( metastoreSupplier );

    List<RunConfigurationProvider> runConfigurationProviders = new ArrayList<>();
    executionConfigurationManager = new RunConfigurationManager( runConfigurationProviders );
    executionConfigurationManager.setDefaultRunConfigurationProvider( defaultRunConfigurationProvider );

    DefaultRunConfiguration defaultRunConfiguration = new DefaultRunConfiguration();
    defaultRunConfiguration.setName( "Default Configuration" );
    defaultRunConfiguration.setDescription( "Default Configuration Description" );
    defaultRunConfiguration.setLocal( true );

    executionConfigurationManager.save( defaultRunConfiguration );
  }

  @After
  public void tearDown() {
    executionConfigurationManager.delete( "Default Configuration" );
  }


  @Test
  public void testGetTypes() {
    String[] types = executionConfigurationManager.getTypes();
    assertTrue( Arrays.asList( types ).contains( DefaultRunConfiguration.TYPE ) );
  }

  @Test
  public void testLoad() {
    List<RunConfiguration> runConfigurations = executionConfigurationManager.load();

    assertEquals( 2, runConfigurations.size() ); //Includes default
  }

  @Test
  public void testLoadByName() {

    DefaultRunConfiguration defaultRunConfiguration = (DefaultRunConfiguration) executionConfigurationManager
      .load( "Default Configuration" );

    assertNotNull( defaultRunConfiguration );
    assertEquals( "Default Configuration", defaultRunConfiguration.getName() );
  }

  @Test
  public void testSaveAndDelete() {
    DefaultRunConfiguration defaultRunConfiguration = new DefaultRunConfiguration();
    defaultRunConfiguration.setName( "New Run Configuration" );

    executionConfigurationManager.save( defaultRunConfiguration );

    DefaultRunConfiguration loadedRunConfiguration =
      (DefaultRunConfiguration) executionConfigurationManager.load( "New Run Configuration" );

    assertEquals( loadedRunConfiguration.getName(), defaultRunConfiguration.getName() );

    executionConfigurationManager.delete( "New Run Configuration" );

    loadedRunConfiguration = (DefaultRunConfiguration) executionConfigurationManager.load( "New Run Configuration" );

    assertNull( loadedRunConfiguration );
  }

  @Test
  public void testGetNames() {
    List<String> names = executionConfigurationManager.getNames();

    assertTrue( names.contains( DefaultRunConfigurationProvider.DEFAULT_CONFIG_NAME ) );
    assertTrue( names.contains( "Default Configuration" ) );
  }

  @Test
  public void testGetRunConfigurationByType() {
    DefaultRunConfiguration defaultRunConfiguration =
      (DefaultRunConfiguration) executionConfigurationManager.getRunConfigurationByType( DefaultRunConfiguration.TYPE );
    assertNotNull( defaultRunConfiguration );
  }

  @Test
  public void testGetExecutor() {
    DefaultRunConfigurationExecutor defaultRunConfigurationExecutor =
      (DefaultRunConfigurationExecutor) executionConfigurationManager.getExecutor( DefaultRunConfiguration.TYPE );
    assertNotNull( defaultRunConfigurationExecutor );
  }

  @Test
  public void testOrdering() {
    MemoryMetaStore memoryMetaStore = new MemoryMetaStore();
    CheckedMetaStoreSupplier metastoreSupplier = () -> memoryMetaStore;
    DefaultRunConfigurationProvider defaultRunConfigurationProvider =
      new DefaultRunConfigurationProvider( metastoreSupplier );

    List<RunConfigurationProvider> runConfigurationProviders = new ArrayList<>();

    executionConfigurationManager = new RunConfigurationManager( runConfigurationProviders );
    executionConfigurationManager.setDefaultRunConfigurationProvider( defaultRunConfigurationProvider );

    DefaultRunConfiguration defaultRunConfiguration1 = new DefaultRunConfiguration();
    defaultRunConfiguration1.setName( "z" );
    executionConfigurationManager.save( defaultRunConfiguration1 );

    DefaultRunConfiguration defaultRunConfiguration2 = new DefaultRunConfiguration();
    defaultRunConfiguration2.setName( "f" );
    executionConfigurationManager.save( defaultRunConfiguration2 );

    DefaultRunConfiguration defaultRunConfiguration3 = new DefaultRunConfiguration();
    defaultRunConfiguration3.setName( "x" );
    executionConfigurationManager.save( defaultRunConfiguration3 );

    DefaultRunConfiguration defaultRunConfiguration5 = new DefaultRunConfiguration();
    defaultRunConfiguration5.setName( "a" );
    executionConfigurationManager.save( defaultRunConfiguration5 );

    List<RunConfiguration> runConfigurations = executionConfigurationManager.load();

    assertEquals( DefaultRunConfigurationProvider.DEFAULT_CONFIG_NAME, runConfigurations.get( 0 ).getName() );
    assertEquals( "a", runConfigurations.get( 1 ).getName() );
    assertEquals( "f", runConfigurations.get( 2 ).getName() );
    assertEquals( "x", runConfigurations.get( 3 ).getName() );
    assertEquals( "z", runConfigurations.get( 4 ).getName() );

    List<String> names = executionConfigurationManager.getNames();

    assertEquals( DefaultRunConfigurationProvider.DEFAULT_CONFIG_NAME, names.get( 0 ) );
    assertEquals( "a", names.get( 1 ) );
    assertEquals( "f", names.get( 2 ) );
    assertEquals( "x", names.get( 3 ) );
    assertEquals( "z", names.get( 4 ) );
  }

}
