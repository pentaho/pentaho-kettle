package org.pentaho.di.shared;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.ClusterSchemaManagementInterface;
import org.pentaho.di.core.variables.VariableSpace;

public class VariableSharingClusterSchemaManager extends VariableSharingSharedObjectManager<ClusterSchema>
  implements ClusterSchemaManagementInterface {

  public VariableSharingClusterSchemaManager( VariableSpace variables,
                                              SharedObjectsManagementInterface<ClusterSchema> parent ) {
    super( variables, parent );
  }

}
