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
