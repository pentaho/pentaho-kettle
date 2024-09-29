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


package org.pentaho.di.core;

import java.util.concurrent.TimeUnit;

import org.pentaho.di.core.row.RowMetaInterface;

/**
 * A simplified rowset for steps that always only need to only have a single row on input...
 *
 * @author matt
 */
public class SingleRowRowSet extends BaseRowSet implements Comparable<RowSet>, RowSet {

  private Object[] row;

  @Override
  public Object[] getRow() {
    Object[] retRow = row;
    row = null;
    return retRow;
  }

  @Override
  public Object[] getRowImmediate() {
    return getRow();
  }

  @Override
  public Object[] getRowWait( long timeout, TimeUnit tu ) {
    return getRow();
  }

  @Override
  public boolean putRow( RowMetaInterface rowMeta, Object[] rowData ) {
    this.rowMeta = rowMeta;
    this.row = rowData;
    return true;
  }

  @Override
  public boolean putRowWait( RowMetaInterface rowMeta, Object[] rowData, long time, TimeUnit tu ) {
    return putRow( rowMeta, rowData );
  }

  @Override
  public int size() {
    return row == null ? 0 : 1;
  }

  @Override
  public void clear() {
    row = null;
    done.set( false );
  }

}
