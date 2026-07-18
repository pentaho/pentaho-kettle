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



package org.pentaho.di.trans.steps.avro.input;

import java.util.Iterator;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.steps.file.BaseFileInputStepData;


public class AvroInputData extends BaseFileInputStepData {
  IPentahoAvroInputFormat input;
  IPentahoInputFormat.IPentahoRecordReader reader;
  Iterator<RowMetaAndData> rowIterator;
  RowMetaInterface outputRowMeta;
}
