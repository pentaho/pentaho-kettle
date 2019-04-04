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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.Utils;

/**
 * Contains the base RowSet class to help implement RowSet variants.
 *
 * @author Matt
 * @since 22-01-2010
 *
 */
abstract class BaseRowSet implements Comparable<RowSet>, RowSet {
  protected RowMetaInterface rowMeta;

  protected AtomicBoolean done;
  protected volatile String originStepName;
  protected AtomicInteger originStepCopy;
  protected volatile String destinationStepName;
  protected AtomicInteger destinationStepCopy;

  protected volatile String remoteSlaveServerName;
  private ReadWriteLock lock;

  public BaseRowSet() {
    // not done putting data into this RowSet
    done = new AtomicBoolean( false );

    originStepCopy = new AtomicInteger( 0 );
    destinationStepCopy = new AtomicInteger( 0 );
    lock = new ReentrantReadWriteLock();
  }

  /**
   * Compares using the target steps and copy, not the source.
   * That way, re-partitioning is always done in the same way.
   */
  @Override
  public int compareTo( RowSet rowSet ) {
    lock.readLock().lock();
    String target;

    try {
      target = remoteSlaveServerName + "." + destinationStepName + "." + destinationStepCopy.intValue();
    } finally {
      lock.readLock().unlock();
    }

    String comp =
      rowSet.getRemoteSlaveServerName()
        + "." + rowSet.getDestinationStepName() + "." + rowSet.getDestinationStepCopy();

    return target.compareTo( comp );
  }

  public boolean equals( BaseRowSet rowSet ) {
    return compareTo( rowSet ) == 0;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#putRow(org.pentaho.di.core.row.RowMetaInterface, java.lang.Object[])
   */
  @Override
  public abstract boolean putRow( RowMetaInterface rowMeta, Object[] rowData );

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#putRowWait(org.pentaho.di.core.row.RowMetaInterface, java.lang.Object[],
   * long, java.util.concurrent.TimeUnit)
   */
  @Override
  public abstract boolean putRowWait( RowMetaInterface rowMeta, Object[] rowData, long time, TimeUnit tu );

  // default getRow with wait time = 100ms
  //
  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#getRow()
   */
  @Override
  public abstract Object[] getRow();

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#getRowImmediate()
   */
  @Override
  public abstract Object[] getRowImmediate();

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#getRowWait(long, java.util.concurrent.TimeUnit)
   */
  @Override
  public abstract Object[] getRowWait( long timeout, TimeUnit tu );

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#setDone()
   */
  @Override
  public void setDone() {
    done.set( true );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#isDone()
   */
  @Override
  public boolean isDone() {
    return done.get();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#getOriginStepName()
   */
  @Override
  public String getOriginStepName() {
    return originStepName;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#getOriginStepCopy()
   */
  @Override
  public int getOriginStepCopy() {
    return originStepCopy.get();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#getDestinationStepName()
   */
  @Override
  public String getDestinationStepName() {
    return destinationStepName;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#getDestinationStepCopy()
   */
  @Override
  public int getDestinationStepCopy() {
    return destinationStepCopy.get();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#getName()
   */
  @Override
  public String getName() {
    return toString();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#size()
   */
  @Override
  public abstract int size();

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#setThreadNameFromToCopy(java.lang.String, int, java.lang.String, int)
   */
  @Override
  public void setThreadNameFromToCopy( String from, int from_copy, String to, int to_copy ) {

    lock.writeLock().lock();
    try {
      originStepName = from;
      originStepCopy.set( from_copy );

      destinationStepName = to;
      destinationStepCopy.set( to_copy );
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public String toString() {
    StringBuilder str;

    lock.readLock().lock();
    try {
      str = new StringBuilder( originStepName )
        .append( "." )
        .append( originStepCopy )
        .append( " - " )
        .append( destinationStepName )
        .append( "." )
        .append( destinationStepCopy );

      if ( !Utils.isEmpty( remoteSlaveServerName ) ) {
        str.append( " (" )
          .append( remoteSlaveServerName )
          .append( ")" );
      }
    } finally {
      lock.readLock().unlock();
    }

    return str.toString();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#getRowMeta()
   */
  @Override
  public RowMetaInterface getRowMeta() {
    return rowMeta;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#setRowMeta(org.pentaho.di.core.row.RowMetaInterface)
   */
  @Override
  public void setRowMeta( RowMetaInterface rowMeta ) {
    this.rowMeta = rowMeta;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#getRemoteSlaveServerName()
   */
  @Override
  public String getRemoteSlaveServerName() {
    return remoteSlaveServerName;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.RowSetInterface#setRemoteSlaveServerName(java.lang.String)
   */
  @Override
  public void setRemoteSlaveServerName( String remoteSlaveServerName ) {
    this.remoteSlaveServerName = remoteSlaveServerName;
  }

  /**
   * By default we don't report blocking, only for monitored transformations.
   *
   * @return true if this row set is blocking on reading or writing.
   */
  @Override
  public boolean isBlocking() {
    return false;
  }

}
