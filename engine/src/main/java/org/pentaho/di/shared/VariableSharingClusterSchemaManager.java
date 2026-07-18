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
