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

package org.pentaho.di.trans.steps.sortedmerge;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;

public class RowSetRow {
  private RowSet rowSet;
  private RowMetaInterface rowMeta;
  private Object[] rowData;

  /**
   * @param rowSet
   * @param rowData
   */
  public RowSetRow( RowSet rowSet, RowMetaInterface rowMeta, Object[] rowData ) {
    super();
    this.rowSet = rowSet;
    this.rowMeta = rowMeta;
    this.rowData = rowData;
  }

  /**
   * @return the rowSet
   */
  public RowSet getRowSet() {
    return rowSet;
  }

  /**
   * @param rowSet
   *          the rowSet to set
   */
  public void setRowSet( RowSet rowSet ) {
    this.rowSet = rowSet;
  }

  /**
   * @return the rowData
   */
  public Object[] getRowData() {
    return rowData;
  }

  /**
   * @param rowData
   *          the rowData to set
   */
  public void setRowData( Object[] rowData ) {
    this.rowData = rowData;
  }

  /**
   * @return the rowMeta
   */
  public RowMetaInterface getRowMeta() {
    return rowMeta;
  }

  /**
   * @param rowMeta
   *          the rowMeta to set
   */
  public void setRowMeta( RowMetaInterface rowMeta ) {
    this.rowMeta = rowMeta;
  }
}
