/*!
 * Copyright 2024 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
