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
