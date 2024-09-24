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
