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

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.pentaho.di.core.row.RowMetaInterface;

/**
 * A simplified rowset for steps for single threaded execution. This row set has no limited size.
 *
 * @author matt
 */
public class QueueRowSet extends BaseRowSet implements Comparable<RowSet>, RowSet {

  private LinkedList<Object[]> buffer;

  public QueueRowSet() {
    buffer = new LinkedList<Object[]>();
  }

  @Override
  public Object[] getRow() {
    Object[] retRow = buffer.pollFirst();
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
    buffer.add( rowData );
    return true;
  }

  @Override
  public boolean putRowWait( RowMetaInterface rowMeta, Object[] rowData, long time, TimeUnit tu ) {
    return putRow( rowMeta, rowData );
  }

  @Override
  public int size() {
    return buffer.size();
  }

  @Override
  public void clear() {
    buffer.clear();
    done.set( false );
  }

}
