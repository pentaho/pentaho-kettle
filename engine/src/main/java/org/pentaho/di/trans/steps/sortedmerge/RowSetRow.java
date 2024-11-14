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
