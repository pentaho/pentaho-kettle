/*!
 * Copyright 2024 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
