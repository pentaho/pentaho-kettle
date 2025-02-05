/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
import org.pentaho.di.engine.configuration.impl.spark.SparkRunConfiguration;
import org.pentaho.di.engine.configuration.impl.spark.SparkRunConfigurationProvider;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.locator.api.MetastoreLocator;
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
    MetastoreLocator metastoreLocator = createMetastoreLocator( memoryMetaStore );
    DefaultRunConfigurationProvider defaultRunConfigurationProvider =
      new DefaultRunConfigurationProvider( metastoreSupplier );

    SparkRunConfigurationProvider sparkRunConfigurationProvider =
    new SparkRunConfigurationProvider( metastoreLocator );

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

    assertEquals( 3, runConfigurations.size() ); //Includes default
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
    CheckedMetaStoreSupplier metastoreSupplier = () -> memoryMetaStore;
    DefaultRunConfigurationProvider defaultRunConfigurationProvider =
      new DefaultRunConfigurationProvider( metastoreSupplier );

    SparkRunConfigurationProvider sparkRunConfigurationProvider =
            null;
    sparkRunConfigurationProvider = new SparkRunConfigurationProvider( metastoreLocator );

    List<RunConfigurationProvider> runConfigurationProviders = new ArrayList<>();
    runConfigurationProviders.add( sparkRunConfigurationProvider );

    executionConfigurationManager = new RunConfigurationManager( runConfigurationProviders );
    executionConfigurationManager.setDefaultRunConfigurationProvider( defaultRunConfigurationProvider );

    DefaultRunConfiguration defaultRunConfiguration1 = new DefaultRunConfiguration();
    defaultRunConfiguration1.setName( "z" );
    executionConfigurationManager.save( defaultRunConfiguration1 );

    SparkRunConfiguration sparkRunConfiguration = new SparkRunConfiguration();
    sparkRunConfiguration.setName( "d" );
    executionConfigurationManager.save( sparkRunConfiguration );

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
    assertEquals( "d", runConfigurations.get( 2 ).getName() );
    assertEquals( "f", runConfigurations.get( 3 ).getName() );
    assertEquals( "x", runConfigurations.get( 4 ).getName() );
    assertEquals( "z", runConfigurations.get( 5 ).getName() );

    List<String> names = executionConfigurationManager.getNames();

    assertEquals( DefaultRunConfigurationProvider.DEFAULT_CONFIG_NAME, names.get( 0 ) );
    assertEquals( "a", names.get( 1 ) );
    assertEquals( "d", names.get( 2 ) );
    assertEquals( "f", names.get( 3 ) );
    assertEquals( "x", names.get( 4 ) );
    assertEquals( "z", names.get( 5 ) );
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

      @Override public IMetaStore getExplicitMetastore(String providerKey ) {
        return null;
      }
    };
  }

}
