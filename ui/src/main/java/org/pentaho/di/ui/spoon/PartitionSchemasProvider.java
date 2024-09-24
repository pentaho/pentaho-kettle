/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.ui.spoon;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.TransMeta;

import java.util.List;

/**
 * An interface to conceal partitions' obtaining routine.
 *
 * @author Andrey Khayrutdinov
 */
public interface PartitionSchemasProvider {

  List<PartitionSchema> getPartitionSchemas( TransMeta transMeta ) throws KettleException;

  List<String> getPartitionSchemasNames( TransMeta transMeta ) throws KettleException;
}
