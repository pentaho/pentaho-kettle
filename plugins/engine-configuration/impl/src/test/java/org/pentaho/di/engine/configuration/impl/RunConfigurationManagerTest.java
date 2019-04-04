/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationProvider;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfiguration;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfigurationExecutor;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfigurationProvider;
import org.pentaho.di.engine.configuration.impl.spark.SparkRunConfiguration;
import org.pentaho.di.engine.configuration.impl.spark.SparkRunConfigurationExecutor;
import org.pentaho.di.engine.configuration.impl.spark.SparkRunConfigurationProvider;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;
import org.pentaho.osgi.metastore.locator.api.MetastoreLocator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by bmorrise on 3/15/17.
 */
@RunWith( MockitoJUnitRunner.class )
public class RunConfigurationManagerTest {

  private RunConfigurationManager executionConfigurationManager;

  @Mock
  private DefaultRunConfigurationExecutor defaultRunConfigurationExecutor;

  @Before
  public void setup() throws Exception {

    MemoryMetaStore memoryMetaStore = new MemoryMetaStore();
    MetastoreLocator metastoreLocator = createMetastoreLocator( memoryMetaStore );

    DefaultRunConfigurationProvider defaultRunConfigurationProvider =
      new DefaultRunConfigurationProvider( metastoreLocator, defaultRunConfigurationExecutor );

    SparkRunConfigurationExecutor sparkRunConfigurationExecutor = new SparkRunConfigurationExecutor( null );
    SparkRunConfigurationProvider sparkRunConfigurationProvider =
      new SparkRunConfigurationProvider( metastoreLocator, sparkRunConfigurationExecutor );

    List<RunConfigurationProvider> runConfigurationProviders = new ArrayList<>();
    runConfigurationProviders.add( sparkRunConfigurationProvider );

    executionConfigurationManager = new RunConfigurationManager( runConfigurationProviders );
    executionConfigurationManager.setDefaultRunConfigurationProvider( defaultRunConfigurationProvider );

    DefaultRunConfiguration defaultRunConfiguration = new DefaultRunConfiguration();
    defaultRunConfiguration.setName( "Default Configuration" );
    defaultRunConfiguration.setDescription( "Default Configuration Description" );
    defaultRunConfiguration.setLocal( true );

    executionConfigurationManager.save( defaultRunConfiguration );

    SparkRunConfiguration sparkRunConfiguration = new SparkRunConfiguration();
    sparkRunConfiguration.setName( "Spark Configuration" );
    sparkRunConfiguration.setDescription( "Spark Configuration Description" );
    sparkRunConfiguration.setUrl( "127.0.0.1" );

    executionConfigurationManager.save( sparkRunConfiguration );
  }

  @After
  public void tearDown() {
    executionConfigurationManager.delete( "Default Configuration" );
    executionConfigurationManager.delete( "Spark Configuration" );
  }


  @Test
  public void testGetTypes() {
    String[] types = executionConfigurationManager.getTypes();
    assertTrue( Arrays.asList( types ).contains( DefaultRunConfiguration.TYPE ) );
    assertTrue( Arrays.asList( types ).contains( SparkRunConfiguration.TYPE ) );
  }

  @Test
  public void testLoad() {
    List<RunConfiguration> runConfigurations = executionConfigurationManager.load();

    assertEquals( runConfigurations.size(), 3 ); //Includes default
  }

  @Test
  public void testLoadByName() {

    DefaultRunConfiguration defaultRunConfiguration = (DefaultRunConfiguration) executionConfigurationManager
      .load( "Default Configuration" );

    assertNotNull( defaultRunConfiguration );
    assertEquals( defaultRunConfiguration.getName(), "Default Configuration" );
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
    assertTrue( names.contains( "Spark Configuration" ) );
  }

  @Test
  public void testGetRunConfigurationByType() {
    DefaultRunConfiguration defaultRunConfiguration =
      (DefaultRunConfiguration) executionConfigurationManager.getRunConfigurationByType( DefaultRunConfiguration.TYPE );

    SparkRunConfiguration sparkRunConfiguration =
      (SparkRunConfiguration) executionConfigurationManager.getRunConfigurationByType( SparkRunConfiguration.TYPE );

    assertNotNull( defaultRunConfiguration );
    assertNotNull( sparkRunConfiguration );
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
    MetastoreLocator metastoreLocator = createMetastoreLocator( memoryMetaStore );

    DefaultRunConfigurationProvider defaultRunConfigurationProvider =
      new DefaultRunConfigurationProvider( metastoreLocator, defaultRunConfigurationExecutor );

    SparkRunConfigurationExecutor sparkRunConfigurationExecutor = new SparkRunConfigurationExecutor( null );
    SparkRunConfigurationProvider sparkRunConfigurationProvider =
      new SparkRunConfigurationProvider( metastoreLocator, sparkRunConfigurationExecutor );

    List<RunConfigurationProvider> runConfigurationProviders = new ArrayList<>();
    runConfigurationProviders.add( sparkRunConfigurationProvider );

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

    SparkRunConfiguration sparkRunConfiguration = new SparkRunConfiguration();
    sparkRunConfiguration.setName( "d" );
    executionConfigurationManager.save( sparkRunConfiguration );

    DefaultRunConfiguration defaultRunConfiguration5 = new DefaultRunConfiguration();
    defaultRunConfiguration5.setName( "a" );
    executionConfigurationManager.save( defaultRunConfiguration5 );

    List<RunConfiguration> runConfigurations = executionConfigurationManager.load();

    assertEquals( runConfigurations.get( 0 ).getName(), DefaultRunConfigurationProvider.DEFAULT_CONFIG_NAME );
    assertEquals( runConfigurations.get( 1 ).getName(), "a" );
    assertEquals( runConfigurations.get( 2 ).getName(), "d" );
    assertEquals( runConfigurations.get( 3 ).getName(), "f" );
    assertEquals( runConfigurations.get( 4 ).getName(), "x" );
    assertEquals( runConfigurations.get( 5 ).getName(), "z" );

    List<String> names = executionConfigurationManager.getNames();

    assertEquals( names.get( 0 ), DefaultRunConfigurationProvider.DEFAULT_CONFIG_NAME );
    assertEquals( names.get( 1 ), "a" );
    assertEquals( names.get( 2 ), "d" );
    assertEquals( names.get( 3 ), "f" );
    assertEquals( names.get( 4 ), "x" );
    assertEquals( names.get( 5 ), "z" );
  }

  private static MetastoreLocator createMetastoreLocator( IMetaStore memoryMetaStore ) {
    return new MetastoreLocator() {

      @Override
      public IMetaStore getMetastore( String providerKey ) {
        return memoryMetaStore;
      }

      @Override
      public IMetaStore getMetastore() {
        return memoryMetaStore;
      }

      @Override public String setEmbeddedMetastore( IMetaStore metastore ) {
        return null;
      }

      @Override public void disposeMetastoreProvider( String providerKey ) {
      }

      @Override public IMetaStore getExplicitMetastore( String providerKey ) {
        return null;
      }
    };
  }
}
