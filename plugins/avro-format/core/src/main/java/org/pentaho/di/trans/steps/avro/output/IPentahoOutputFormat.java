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


package org.pentaho.di.trans.steps.avro.output;

import org.pentaho.di.core.RowMetaAndData;

import java.io.Closeable;

public interface IPentahoOutputFormat {
  IPentahoRecordWriter createRecordWriter() throws Exception;

  public interface IPentahoRecordWriter extends Closeable {
    void write( RowMetaAndData row ) throws Exception;
  }
}
