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

package org.pentaho.di.core.bowl;

import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.IMetaStore;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Supplier;

/**
 * The default/global Bowl. A singleton for standard behavior when there is no custom Bowl.
 *
 */
public class DefaultBowl extends BaseBowl {
  private static final DefaultBowl INSTANCE = new DefaultBowl();

  // for testing
  private Supplier<IMetaStore> metastoreSupplier = MetaStoreConst.getDefaultMetastoreSupplier();
  private boolean customSupplier = false;

  private DefaultBowl() {
  }

  public static DefaultBowl getInstance() {
    return INSTANCE;
  }


  @Override
  public IMetaStore getExplicitMetastore() throws MetaStoreException {
    return metastoreSupplier.get();
  }

  @Override
  public IMetaStore getMetastore() throws MetaStoreException {
    return metastoreSupplier.get();
  }


  @Override
  public ConnectionManager getConnectionManager() throws MetaStoreException {
    // need to override getConnectionManager so this instance of DefaultBowl shares the same ConnectionManager
    // instance with ConnectionManager.getInstance()
    if ( customSupplier ) {
      return super.getConnectionManager();
    } else {
      return ConnectionManager.getInstance();
    }
  }

  @VisibleForTesting
  public void setMetastoreSupplier( Supplier<IMetaStore> metastoreSupplier ) {
    this.metastoreSupplier = metastoreSupplier;
    this.customSupplier = true;
  }


}
