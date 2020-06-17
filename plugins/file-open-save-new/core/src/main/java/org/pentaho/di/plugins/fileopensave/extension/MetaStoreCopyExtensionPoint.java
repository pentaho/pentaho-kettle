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

package org.pentaho.di.plugins.fileopensave.extension;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.osgi.metastore.locator.api.MetastoreLocator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Abstract extension point for import/export of meta store
 */
public abstract class MetaStoreCopyExtensionPoint implements ExtensionPointInterface {

  public static final String VFS_CONNECTIONS = "VFS Connections";

  private final MetastoreLocator metastoreLocator;
  private final Supplier<ConnectionManager> connectionManagerSupplier = ConnectionManager::getInstance;

  public MetaStoreCopyExtensionPoint( MetastoreLocator metastoreLocator ) {
    this.metastoreLocator = metastoreLocator;
  }

  /**
   * Asynchronously import the named connections from the embedded meta store
   *
   * @param meta The AbstractMeta with the embedded meta store
   * @param callback Callback to run after import
   */
  protected void asyncImportAll( AbstractMeta meta, Runnable callback ) {
    ExecutorService executorService = Executors.newCachedThreadPool();
    executorService.submit( () -> {
      importAll( meta );
      callback.run();
    } );
  }

  /**
   * Import named connections from the embedded meta store
   *
   * @param meta The AbstractMeta with the embedded meta store
   */
  protected void importAll( AbstractMeta meta ) {
    if ( meta.getEmbeddedMetaStore() != null && metastoreLocator != null ) {
      getConnectionManager().copy( meta.getEmbeddedMetaStore(), metastoreLocator.getMetastore() );
    }
  }

  /**
   * Export named connections from the connected meta store
   *
   * @param meta The AbstractMeta with the embedded meta store
   */
  protected void exportAll( AbstractMeta meta ) {
    if ( meta.getEmbeddedMetaStore() != null && metastoreLocator != null ) {
      getConnectionManager().clear( meta.getEmbeddedMetaStore() );
      getConnectionManager().copy( metastoreLocator.getMetastore(), meta.getEmbeddedMetaStore() );
    }
  }

  /**
   * Access the ConnectionManager singleton
   *
   * @return ConnectionManager singleton
   */
  private ConnectionManager getConnectionManager() {
    return connectionManagerSupplier.get();
  }
}
