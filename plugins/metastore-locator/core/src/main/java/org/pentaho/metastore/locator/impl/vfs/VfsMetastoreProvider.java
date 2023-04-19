/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2022-2023 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */

package org.pentaho.metastore.locator.impl.vfs;

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

@ServiceProvider( id = "VfsMetastoreProvider", description = "Provides access to a VFS metastore", provides = MetastoreProvider.class )
public class VfsMetastoreProvider implements MetastoreProvider, ServiceProviderInterface<MetastoreProvider> {
  private static final Logger logger = LoggerFactory.getLogger( VfsMetastoreProvider.class );
  private final MetastoreSupplier supplier;

  public VfsMetastoreProvider() {
    this( MetaStoreConst::openBootstrappingMetastore );
    logger.info( "MetaStoreConst::openPentahoVfsMetaStore called in VfsMetastoreProvider constructor" );
  }

  public VfsMetastoreProvider( MetastoreSupplier supplier ) {
    this.supplier = supplier;
    logger.info( "Setting supplier in VfsMetastoreProvider constructor" );
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
    return MetastoreLocator.VFS_PROVIDER_KEY;
  }
}
