package org.pentaho.di.engine.configuration.impl;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.pentaho.di.core.runconfiguration.api.RunConfigurationProvider;
import org.pentaho.di.core.runconfiguration.api.CheckedMetaStoreSupplier;
import org.pentaho.di.core.runconfiguration.impl.RunConfigurationProviderFactoryManager;
import org.pentaho.di.core.runconfiguration.impl.pentaho.DefaultRunConfigurationProviderFactory;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

import java.util.List;

@FixMethodOrder( MethodSorters.NAME_ASCENDING )
public class RunConfigurationProviderFactoryManagerTest {
  @Test
  public void testDefault() {
    MemoryMetaStore memoryMetaStore = new MemoryMetaStore();
    CheckedMetaStoreSupplier metastoreSupplier = () -> memoryMetaStore;
    List<RunConfigurationProvider> providers =
      RunConfigurationProviderFactoryManager.getInstance().generateProviders( metastoreSupplier );

    Assert.assertEquals( 1, providers.size() );
    Assert.assertEquals( "Pentaho", providers.get( 0 ).getType() );
  }

  @Test
  public void testRegister() {
    MemoryMetaStore memoryMetaStore = new MemoryMetaStore();
    CheckedMetaStoreSupplier metastoreSupplier = () -> memoryMetaStore;

    RunConfigurationProviderFactoryManager.getInstance()
      .registerFactory( new DefaultRunConfigurationProviderFactory() );

    List<RunConfigurationProvider> providers =
      RunConfigurationProviderFactoryManager.getInstance().generateProviders( metastoreSupplier );

    Assert.assertEquals( 2, providers.size() );
    Assert.assertEquals( "Pentaho", providers.get( 0 ).getType() );
    Assert.assertEquals( "Pentaho", providers.get( 1 ).getType() );
  }
}
