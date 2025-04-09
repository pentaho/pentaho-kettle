package org.pentaho.di.engine.configuration.impl;

import org.pentaho.di.engine.configuration.api.RunConfigurationProvider;

public interface RunConfigurationProviderFactory {
  RunConfigurationProvider getProvider( CheckedMetaStoreSupplier metaStoreSupplier );
}
