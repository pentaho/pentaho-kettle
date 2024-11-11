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

package org.pentaho.metastore.locator.impl.local;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.service.ServiceProvider;
import org.pentaho.di.core.service.ServiceProviderInterface;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.locator.api.MetastoreLocator;
import org.pentaho.metastore.locator.api.MetastoreProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bryan on 3/28/16.
 */
@ServiceProvider( id = "LocalFileMetastoreProvider", description = "Provides access to a local file metastore", provides = MetastoreProvider.class )
public class LocalFileMetastoreProvider implements MetastoreProvider, ServiceProviderInterface<MetastoreProvider> {
  private static final Logger logger = LoggerFactory.getLogger( LocalFileMetastoreProvider.class );
  private final MetastoreSupplier supplier;


  public LocalFileMetastoreProvider() {
    this( MetaStoreConst::openLocalPentahoMetaStore );
  }

  public LocalFileMetastoreProvider( MetastoreSupplier supplier ) {
    this.supplier = supplier;
  }

  @Override public IMetaStore getMetastore() {
    try {
      return supplier.getMetastore();
    } catch ( MetaStoreException e ) {
      logger.error( "Unable to open local metastore", e );
      return null;
    }
  }

  @VisibleForTesting interface MetastoreSupplier {
    IMetaStore getMetastore() throws MetaStoreException;
  }

  @Override
  public String getProviderType() {
    return MetastoreLocator.LOCAL_PROVIDER_KEY;
  }
}
