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

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.w3c.dom.Node;

/**
 * Database Manager that does not cache anything. Complete passthrough to the provided SharedObjectsIO instance.
 *
 */
public class PassthroughDbConnectionManager extends PassthroughManager<DatabaseMeta> implements DatabaseManagementInterface {

  public PassthroughDbConnectionManager( SharedObjectsIO sharedObjectsIO ) {
    super( sharedObjectsIO, DatabaseConnectionManager.DB_TYPE );
  }

  protected DatabaseMeta createSharedObjectUsingNode( Node node ) throws KettleException {
    return new DatabaseMeta( node );
  }
}
