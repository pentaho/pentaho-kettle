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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.partition.PartitionSchemaManagementInterface;
import org.w3c.dom.Node;

public class PassthroughPartitionSchemaManager extends PassthroughManager<PartitionSchema> implements PartitionSchemaManagementInterface {

  public static final String PARTITIONSCHEMA_TYPE = SharedObjectsIO.SharedObjectType.PARTITIONSCHEMA.getName();

  public PassthroughPartitionSchemaManager( SharedObjectsIO sharedObjectsIO ) {
    super( sharedObjectsIO, PartitionSchema.class, PARTITIONSCHEMA_TYPE );
  }

  @Override
  protected PartitionSchema createSharedObjectUsingNode( Node node ) throws KettleException {
    return new PartitionSchema( node );
  }
}
