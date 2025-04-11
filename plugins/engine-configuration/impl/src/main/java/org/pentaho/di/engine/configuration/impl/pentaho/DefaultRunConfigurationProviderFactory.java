package org.pentaho.di.engine.configuration.impl.pentaho;

import org.pentaho.di.engine.configuration.api.RunConfigurationProvider;
import org.pentaho.di.engine.configuration.impl.CheckedMetaStoreSupplier;
import org.pentaho.di.engine.configuration.impl.RunConfigurationProviderFactory;

public class DefaultRunConfigurationProviderFactory implements RunConfigurationProviderFactory {
  @Override public RunConfigurationProvider getProvider( CheckedMetaStoreSupplier metaStoreSupplier ) {
    return new DefaultRunConfigurationProvider( metaStoreSupplier );
  }
}
