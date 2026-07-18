/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.ui.repository.repositoryexplorer.model;

import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UIPartition extends XulEventSourceAdapter {

  private PartitionSchema partitionSchema;

  public UIPartition( PartitionSchema partitionSchema ) {
    this.partitionSchema = partitionSchema;
  }

  public PartitionSchema getPartitionSchema() {
    return this.partitionSchema;
  }

  public String getName() {
    if ( partitionSchema != null ) {
      return partitionSchema.getName();
    }
    return null;
  }

}
