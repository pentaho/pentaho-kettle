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


package org.pentaho.di.core;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pentaho.di.core.row.RowMetaInterface;

/**
 * Contains a buffer of rows. Getting rows from the buffer or putting rows in the buffer is synchronized to allow
 * concurrent use of multiple Threads.
 *
 * This class also monitors the idle state of a RowSet
 *
 * @author Matt
 * @since 23-12-2010
 *
 */
public class BlockingListeningRowSet extends BaseRowSet implements Comparable<RowSet>, RowSet {
  private BlockingQueue<Object[]> queArray;

  private AtomicBoolean blocking;

  /**
   * Create new non-blocking-queue with maxSize capacity.
   *
   * @param maxSize
   */
  public BlockingListeningRowSet( int maxSize ) {
    super();

    // create an empty queue
    queArray = new ArrayBlockingQueue<Object[]>( maxSize, false );
    blocking = new AtomicBoolean( false );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#putRow(org.pentaho.di.core.row.RowMetaInterface, java.lang.Object[])
   */
  @Override
  public boolean putRow( RowMetaInterface rowMeta, Object[] rowData ) {
    return putRowWait( rowMeta, rowData, 100, TimeUnit.NANOSECONDS );
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
      blocking.set( true );
      boolean b = queArray.offer( rowData, time, tu );
      blocking.set( false );
      return b;
    } catch ( InterruptedException e ) {
      blocking.set( false );
      return false;
    } catch ( NullPointerException e ) {
      blocking.set( false );
      return false;
    }

  }

  // default getRow with wait time = 100ms
  //
  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#getRow()
   */
  @Override
  public Object[] getRow() {
    blocking.set( true );
    Object[] row = getRowWait( 100, TimeUnit.NANOSECONDS );
    blocking.set( false );
    return row;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#getRowImmediate()
   */
  @Override
  public Object[] getRowImmediate() {

    blocking.set( true );
    Object[] row = queArray.poll();
    blocking.set( false );
    return row;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#getRowWait(long, java.util.concurrent.TimeUnit)
   */
  @Override
  public Object[] getRowWait( long timeout, TimeUnit tu ) {

    try {
      blocking.set( true );
      Object[] row = queArray.poll( timeout, tu );
      blocking.set( false );
      return row;
    } catch ( InterruptedException e ) {
      blocking.set( false );
      return null;
    }
  }

  @Override
  public int size() {
    return queArray.size();
  }

  /**
   * @return true if this row set is blocking.
   */
  @Override
  public boolean isBlocking() {
    return blocking.get();
  }

  @Override
  public void clear() {
    queArray.clear();
    done.set( false );
  }

}
