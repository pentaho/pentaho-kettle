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

package org.pentaho.di.engine.configuration.impl.pentaho;

import org.pentaho.di.engine.configuration.api.RunConfigurationProvider;
import org.pentaho.di.engine.configuration.api.CheckedMetaStoreSupplier;
import org.pentaho.di.engine.configuration.api.RunConfigurationProviderFactory;

public class DefaultRunConfigurationProviderFactory implements RunConfigurationProviderFactory {
  @Override public RunConfigurationProvider getProvider( CheckedMetaStoreSupplier metaStoreSupplier ) {
    return new DefaultRunConfigurationProvider( metaStoreSupplier );
  }
}
