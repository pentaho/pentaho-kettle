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


package org.pentaho.di.core.util;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.database.ConnectionManagementServiceMeta;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.trans.TransMeta;

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
    connectionManager.setMetastoreSupplier( MetaStoreConst.getDefaultMetastoreSupplier() );
  }

  /**
   * Find remote connection-management-service connections and initiate them.
   *
   * @param meta The meta containing named connections
   */
  public static void initConnectionManagementServiceConnections( TransMeta meta) {
    meta.getSteps().forEach(step -> {
      DatabaseMeta[] dbMetaList = step.getStepMetaInterface().getUsedDatabaseConnections();

      if ( dbMetaList.length != 0 ) {
        DatabaseMeta dbMeta = dbMetaList[0];
        DatabaseInterface dbInterface = dbMeta.getDatabaseInterface();

        if (dbInterface instanceof ConnectionManagementServiceMeta cmsMeta) {
          cmsMeta.fetchConnectionDetails();
        }
      }
    });
  }
}
