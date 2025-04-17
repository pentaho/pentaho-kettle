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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.engine.configuration.api.CheckedMetaStoreSupplier;
import org.pentaho.di.engine.configuration.api.RunConfigurationProvider;
import org.pentaho.di.engine.configuration.api.RunConfigurationProviderFactory;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfigurationProviderFactory;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RunConfigurationProviderFactoryManagerImplTest {

  @Test
  public void testDefaultProvidedMetastore() {
    MemoryMetaStore memoryMetaStore = new MemoryMetaStore();
    CheckedMetaStoreSupplier metastoreSupplier = () -> memoryMetaStore;
    List<RunConfigurationProvider> providers =
      new RunConfigurationProviderFactoryManagerImpl().generateProviders( metastoreSupplier );

    Assert.assertEquals( 1, providers.size() );
    Assert.assertEquals( "Pentaho", providers.get( 0 ).getType() );
  }

  @Test
  public void testPluginServiceLoaded() {
    MemoryMetaStore memoryMetaStore = new MemoryMetaStore();
    CheckedMetaStoreSupplier metastoreSupplier = () -> memoryMetaStore;

    Collection<RunConfigurationProviderFactory> factories = new ArrayList<>();
    factories.add( new DefaultRunConfigurationProviderFactory() );

    try ( MockedStatic<PluginServiceLoader> pluginServiceLoaderMockedStatic = Mockito.mockStatic(
      PluginServiceLoader.class ) ) {
      pluginServiceLoaderMockedStatic.when(
        () -> PluginServiceLoader.loadServices( RunConfigurationProviderFactory.class ) ).thenReturn( factories );

      List<RunConfigurationProvider> providers =
        new RunConfigurationProviderFactoryManagerImpl().generateProviders( metastoreSupplier );

      Assert.assertEquals( 2, providers.size() );
      Assert.assertEquals( "Pentaho", providers.get( 0 ).getType() );
      Assert.assertEquals( "Pentaho", providers.get( 1 ).getType() );
    } catch ( Exception e ) {
      System.out.println( e );
    }
  }
}
