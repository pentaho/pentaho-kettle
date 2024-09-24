/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.RowListener;

/**
 * Helper class for testcases. You can add an instance of this class to a step to read all of the Rows the step read or
 * wrote.
 *
 * @author Sven Boden
 */
public class RowStepCollector implements RowListener {
  private List<RowMetaAndData> rowsRead;
  private List<RowMetaAndData> rowsWritten;
  private List<RowMetaAndData> rowsError;

  public RowStepCollector() {
    rowsRead = new ArrayList<RowMetaAndData>();
    rowsWritten = new ArrayList<RowMetaAndData>();
    rowsError = new ArrayList<RowMetaAndData>();
  }

  @Override
  public void rowReadEvent( RowMetaInterface rowMeta, Object[] row ) {
    rowsRead.add( new RowMetaAndData( rowMeta, row ) );
  }

  @Override
  public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) {
    rowsWritten.add( new RowMetaAndData( rowMeta, row ) );
  }

  @Override
  public void errorRowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) {
    rowsError.add( new RowMetaAndData( rowMeta, row ) );
  }

  /**
   * Clear the rows read and rows written.
   */
  public void clear() {
    rowsRead.clear();
    rowsWritten.clear();
    rowsError.clear();
  }

  public List<RowMetaAndData> getRowsRead() {
    return rowsRead;
  }

  public List<RowMetaAndData> getRowsWritten() {
    return rowsWritten;
  }

  public List<RowMetaAndData> getRowsError() {
    return rowsError;
  }
}
