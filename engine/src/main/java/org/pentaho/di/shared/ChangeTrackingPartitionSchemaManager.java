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

import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.partition.PartitionSchemaManagementInterface;

public class ChangeTrackingPartitionSchemaManager extends ChangeTrackingSharedObjectManager<PartitionSchema> implements PartitionSchemaManagementInterface {
  public ChangeTrackingPartitionSchemaManager( SharedObjectsManagementInterface<PartitionSchema> parent ) {
    super( parent );
  }
}
