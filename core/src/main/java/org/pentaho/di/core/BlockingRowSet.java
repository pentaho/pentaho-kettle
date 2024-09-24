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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.pentaho.di.core.row.RowMetaInterface;

/**
 * Contains a buffer of rows. Getting rows from the buffer or putting rows in the buffer is synchronized to allow
 * concurrent use of multiple Threads.
 *
 * @author Matt
 * @since 04-04-2003
 *
 */
public class BlockingRowSet extends BaseRowSet implements Comparable<RowSet>, RowSet {
  private BlockingQueue<Object[]> queArray;

  private int timeoutPut;
  private int timeoutGet;

  /**
   * Create new non-blocking-queue with maxSize capacity.
   *
   * @param maxSize
   */
  public BlockingRowSet( int maxSize ) {
    super();

    // create an empty queue
    queArray = new ArrayBlockingQueue<Object[]>( maxSize, false );

    timeoutGet = Const.toInt( System.getProperty( Const.KETTLE_ROWSET_GET_TIMEOUT ), Const.TIMEOUT_GET_MILLIS );
    timeoutPut = Const.toInt( System.getProperty( Const.KETTLE_ROWSET_PUT_TIMEOUT ), Const.TIMEOUT_PUT_MILLIS );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#putRow(org.pentaho.di.core.row.RowMetaInterface, java.lang.Object[])
   */
  @Override
  public boolean putRow( RowMetaInterface rowMeta, Object[] rowData ) {
    return putRowWait( rowMeta, rowData, timeoutPut, TimeUnit.MILLISECONDS );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#putRowWait(org.pentaho.di.core.row.RowMetaInterface, java.lang.Object[],
   * long, java.util.concurrent.TimeUnit)
   */
  @Override
  public boolean putRowWait( RowMetaInterface rowMeta, Object[] rowData, long time, TimeUnit tu ) {
    this.rowMeta = rowMeta;
    try {

      return queArray.offer( rowData, time, tu );
    } catch ( InterruptedException e ) {
      return false;
    } catch ( NullPointerException e ) {
      return false;
    }

  }

  // default getRow with wait time = 100ms
  //
  /*
   * (non-Javadoc) System.getProperty("KETTLE_ROWSET_PUT_TIMEOUT")
   *
   * @see org.pentaho.di.core.RowSetInterface#getRow()
   */
  @Override
  public Object[] getRow() {
    return getRowWait( timeoutGet, TimeUnit.MILLISECONDS );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#getRowImmediate()
   */
  @Override
  public Object[] getRowImmediate() {

    return queArray.poll();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#getRowWait(long, java.util.concurrent.TimeUnit)
   */
  @Override
  public Object[] getRowWait( long timeout, TimeUnit tu ) {

    try {
      return queArray.poll( timeout, tu );
    } catch ( InterruptedException e ) {
      return null;
    }
  }

  @Override
  public int size() {
    return queArray.size();
  }

  @Override
  public void clear() {
    queArray.clear();
    done.set( false );
  }

}
