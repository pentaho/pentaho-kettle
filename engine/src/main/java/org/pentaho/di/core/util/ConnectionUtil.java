/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.util;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.attributes.metastore.EmbeddedMetaStore;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.stores.delegate.DelegatingMetaStore;

/**
 * Utility for managing embedded named connections
 */
public class ConnectionUtil {

  private ConnectionUtil() {
  }

  /**
   * Initialize the connected meta store with embedded named connections
   *
   * @param meta The meta containing named connections
   */
  public static void init( AbstractMeta meta ) {
    ConnectionManager connectionManager = ConnectionManager.getInstance();

    EmbeddedMetaStore embeddedMetaStore = meta.getEmbeddedMetaStore();
    IMetaStore metaStore = null;
    try {
      if ( meta.getRepository() != null ) {
        if ( meta.getMetaStore() instanceof DelegatingMetaStore ) {
          metaStore = ( (DelegatingMetaStore) meta.getMetaStore() ).getActiveMetaStore();
        } else {
          metaStore = meta.getRepository().getMetaStore();
        }
      } else {
        metaStore = MetaStoreConst.openLocalPentahoMetaStore();
      }
    } catch ( MetaStoreException ignored ) {
      // Allow connectedMetaStore to be null
    }

    if ( metaStore != null ) {
      final IMetaStore connectedMetaStore = metaStore;
      connectionManager.setMetastoreSupplier( () -> connectedMetaStore );
      if ( embeddedMetaStore != null ) {
        connectionManager.copy( embeddedMetaStore, connectedMetaStore );
      }
    }
  }

}
