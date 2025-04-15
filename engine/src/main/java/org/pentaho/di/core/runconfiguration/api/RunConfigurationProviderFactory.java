package org.pentaho.di.core.runconfiguration.api;

public interface RunConfigurationProviderFactory {
  RunConfigurationProvider getProvider( CheckedMetaStoreSupplier metaStoreSupplier );
}
