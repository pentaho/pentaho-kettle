package org.pentaho.di.core.runconfiguration.impl;

import org.pentaho.di.core.runconfiguration.api.RunConfigurationProvider;

public interface RunConfigurationProviderFactory {
  RunConfigurationProvider getProvider( CheckedMetaStoreSupplier metaStoreSupplier );
}
