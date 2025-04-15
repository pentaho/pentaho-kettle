package org.pentaho.di.core.runconfiguration.impl.pentaho;

import org.pentaho.di.core.runconfiguration.api.RunConfigurationProvider;
import org.pentaho.di.core.runconfiguration.api.CheckedMetaStoreSupplier;
import org.pentaho.di.core.runconfiguration.api.RunConfigurationProviderFactory;

public class DefaultRunConfigurationProviderFactory implements RunConfigurationProviderFactory {
  @Override public RunConfigurationProvider getProvider( CheckedMetaStoreSupplier metaStoreSupplier ) {
    return new DefaultRunConfigurationProvider( metaStoreSupplier );
  }
}
