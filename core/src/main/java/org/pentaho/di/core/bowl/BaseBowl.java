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
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

public abstract class BaseBowl implements Bowl {

  private volatile ConnectionManager connectionManager;

  @Override
  public ConnectionManager getConnectionManager() throws MetaStoreException {
    ConnectionManager result = connectionManager;
    if ( result != null ) {
      return result;
    }
    synchronized( this ) {
      if ( connectionManager == null ) {
        IMetaStore metastore = getMetastore();
        connectionManager = ConnectionManager.getInstance( () -> metastore, this );
      }
      return connectionManager;
    }
  }

}
