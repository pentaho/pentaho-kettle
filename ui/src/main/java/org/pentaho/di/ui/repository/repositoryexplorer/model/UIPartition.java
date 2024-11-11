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
