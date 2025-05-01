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

