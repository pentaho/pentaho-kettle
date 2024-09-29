/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2023 by Hitachi Vantara : http://www.pentaho.com
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
