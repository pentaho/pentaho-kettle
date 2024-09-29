/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core.util;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.metastore.MetaStoreConst;

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

}
