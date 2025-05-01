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

/**
 * This is the management interface used by the UI to perform CRUD operation. The implementors of this interface will
 * be scoped based on the bowl and can be retrieved using bowl's getManager()
 *
 */
public class ChangeTrackingDatabaseManager extends ChangeTrackingSharedObjectManager<DatabaseMeta> implements DatabaseManagementInterface {

  public ChangeTrackingDatabaseManager( SharedObjectsManagementInterface<DatabaseMeta> parent ) {
    super( parent );
  }
}
