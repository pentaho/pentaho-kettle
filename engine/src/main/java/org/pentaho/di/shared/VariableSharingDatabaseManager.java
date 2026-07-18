/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/
package org.pentaho.di.shared;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.variables.VariableSpace;

public class VariableSharingDatabaseManager extends VariableSharingSharedObjectManager<DatabaseMeta>
  implements DatabaseManagementInterface {

  public VariableSharingDatabaseManager( VariableSpace variables,
                                              SharedObjectsManagementInterface<DatabaseMeta> parent ) {
    super( variables, parent );
  }

}
