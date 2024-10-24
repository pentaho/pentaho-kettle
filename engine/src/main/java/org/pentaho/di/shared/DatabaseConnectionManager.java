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

package org.pentaho.di.shared;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.ManagerFactory;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.w3c.dom.Node;

/**
 * This class uses the SharedObjectsIO to retrieve and save shared objects. This is used by the UI.
 * <p>
 * This class caches the state of the underlying SharedObjectsIO, and does not re-read from the source. Only changes
 * written through this interface will be reflected.
 */
public class DatabaseConnectionManager extends BaseSharedObjectsManager<DatabaseMeta> implements DatabaseManagementInterface {
  public static final String DB_TYPE = SharedObjectsIO.SharedObjectType.CONNECTION.getName();

  /**
   * Create an instance of SharedObjectManager using the Bowl's SharedObjectIO
   * @param bowl
   * @return
   */
  public static DatabaseConnectionManager getInstance( Bowl bowl ) {
    return new DatabaseConnectionManager( bowl.getSharedObjectsIO() );
  }

  protected DatabaseConnectionManager( SharedObjectsIO sharedObjectsIO ) {
    super( DB_TYPE, sharedObjectsIO );
  }

  @Override
  protected DatabaseMeta createSharedObjectUsingNode( Node node ) throws KettleException {
    return new DatabaseMeta( node );
  }

  /**
   * Factory for the DatabaseConnectionManager. This factory class is registered with BowlFactory registry
   * during the initialization in KettleEnvironment
   */
  public static class DatabaseConnectionManagerFactory implements ManagerFactory<DatabaseManagementInterface> {
    public DatabaseManagementInterface apply( Bowl bowl ) throws KettleException {
      return DatabaseConnectionManager.getInstance( bowl );
    }
  }
}

