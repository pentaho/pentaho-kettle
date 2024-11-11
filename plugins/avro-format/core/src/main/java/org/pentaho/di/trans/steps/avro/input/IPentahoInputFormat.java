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

package org.pentaho.di.trans.steps.avro.input;

import org.pentaho.di.core.RowMetaAndData;

import java.io.Closeable;
import java.util.Collections;
import java.util.List;

public interface IPentahoInputFormat {

  /**
   * Get split parts.
   */
  default List<IPentahoInputSplit> getSplits() {
    return Collections.emptyList();
  }

  /**
   * Read one split part.
   */
  IPentahoRecordReader createRecordReader( IPentahoInputSplit split ) throws Exception;

  public interface IPentahoInputSplit {
  }

  public interface IPentahoRecordReader extends Iterable<RowMetaAndData>, Closeable {
  }
}
