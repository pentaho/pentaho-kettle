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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.pentaho.di.core.row.RowMetaInterface;

/**
 * Contains a buffer of rows. Instead of passing rows along the chain immediately we will batch them up to lower locking
 * overhead. The row set will start in accepting mode (accepting = true) It will remain there until the buffer is full.
 * Then it will switch to delivering mode (accepting = false) It will remain there until the buffer is empty.
 *
 * When the row set is done and no more rows will be entering the row set we will switch to delivering mode.
 *
 * @author Matt
 * @since 04-05-2011
 *
 */
public class BlockingBatchingRowSet extends BaseRowSet implements Comparable<RowSet>, RowSet {
  private BlockingQueue<Object[][]> putArray, getArray;

  private int putIndex, getIndex;
  private Object[][] inputBuffer, outputBuffer;

  private int size;

  private final int BATCHSIZE = 2;

  // private long inputTID = -1, outputTID = -1;

  /**
   * Create new non-blocking-queue with maxSize capacity.
   *
   * @param maxSize
   */
  public BlockingBatchingRowSet( int maxSize ) {
    super();

    // create a fixed sized queue for max performance
    //
    putArray = new ArrayBlockingQueue<Object[][]>( BATCHSIZE, true );
    getArray = new ArrayBlockingQueue<Object[][]>( BATCHSIZE, true );

    size = maxSize / BATCHSIZE; // each buffer's size
    Object[][] buffer;
    for ( int i = 0; i < BATCHSIZE; i++ ) {
      buffer = new Object[size][];
      putArray.offer( buffer );
    }
    outputBuffer = null;
    putIndex = getIndex = size;
  }

  @Override
  public boolean putRow( RowMetaInterface rowMeta, Object[] rowData ) {

    return putRowWait( rowMeta, rowData, Const.TIMEOUT_PUT_MILLIS, TimeUnit.MILLISECONDS );
  }

  /**
   * We need to wait until
   */
  @Override
  public boolean putRowWait( RowMetaInterface rowMeta, Object[] rowData, long time, TimeUnit tu ) {

    this.rowMeta = rowMeta;

    // If we're not accepting we block until we do
    //
    if ( inputBuffer == null ) {
      try {
        inputBuffer = putArray.poll( time, tu );
      } catch ( InterruptedException e ) {
        return false;
      }
      if ( inputBuffer == null ) {
        return false;
      }
      putIndex = 0;
    }
    inputBuffer[putIndex++] = rowData;
    if ( putIndex == size ) {
      try {
        getArray.offer( inputBuffer, time, tu );
        inputBuffer = null;
      } catch ( InterruptedException e ) {
        return false;
      }
    }

    return true;
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
    return getRowWait( Const.TIMEOUT_GET_MILLIS, TimeUnit.MILLISECONDS );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#getRowImmediate()
   */
  @Override
  public Object[] getRowImmediate() {

    return getRow();
  }

  @Override
  public Object[] getRowWait( long timeout, TimeUnit tu ) {
    if ( outputBuffer == null ) {
      try {
        outputBuffer = getArray.poll( timeout, tu );
      } catch ( InterruptedException e ) {
        return null;
      }
      if ( outputBuffer == null ) {
        return null;
      }
      getIndex = 0;
    }

    Object[] row = outputBuffer[getIndex];
    outputBuffer[getIndex++] = null; // prevent any hold-up to GC
    if ( getIndex == size ) {
      putArray.offer( outputBuffer );
      outputBuffer = null;
    }

    return row;
  }

  @Override
  public int size() {
    // does BlockingQueue.size() grab a lock? If so, frequent call to this method
    // may stress the locking system
    return size - getIndex + size * getArray.size();
  }

  @Override
  public void setDone() {
    super.setDone();
    if ( putIndex > 0 && putIndex < size && inputBuffer != null ) {
      inputBuffer[putIndex] = null; // signal the end of buffer
      for ( int i = putIndex + 1; i < size; i++ ) {
        inputBuffer[i] = null;
      }
      getArray.offer( inputBuffer );
    }
    putArray.clear();
  }

  @Override
  public void clear() {
    putArray.clear();
    getArray.clear();
    done.set( false );
  }
}
