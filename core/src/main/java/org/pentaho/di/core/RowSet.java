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

import org.pentaho.di.core.row.RowMetaInterface;

public interface RowSet {

  /**
   * Offer a row of data to this rowset providing for the description (metadata) of the row. If the buffer is full, wait
   * (block) for a small period of time.
   *
   * @param rowMeta
   *          The description of the row data
   * @param rowData
   *          the row of data
   * @return true if the row was successfully added to the rowset and false if this buffer was full.
   */
  public abstract boolean putRow( RowMetaInterface rowMeta, Object[] rowData );

  /**
   * Offer a row of data to this rowset providing for the description (metadata) of the row. If the buffer is full, wait
   * (block) for a period of time defined in this call.
   *
   * @param rowMeta
   *          The description of the row data
   * @param rowData
   *          the row of data
   * @param time
   *          The number of units of time
   * @param tu
   *          The unit of time to use
   * @return true if the row was successfully added to the rowset and false if this buffer was full.
   */
  public abstract boolean putRowWait( RowMetaInterface rowMeta, Object[] rowData, long time, TimeUnit tu );

  /**
   * Get a row from the input buffer, it blocks for a short period until a new row becomes available. Otherwise, it
   * returns null.
   *
   * @return a row of data or null if no row is available.
   */
  public abstract Object[] getRow();

  /**
   * Get the first row in the list immediately.
   *
   * @return a row of data or null if no row is available.
   */
  public abstract Object[] getRowImmediate();

  /**
   * get the first row in the list immediately if it is available or wait until timeout
   *
   * @return a row of data or null if no row is available.
   */
  public abstract Object[] getRowWait( long timeout, TimeUnit tu );

  /**
   * @return Set indication that there is no more input
   */
  public abstract void setDone();

  /**
   * @return Returns true if there is no more input and vice versa
   */
  public abstract boolean isDone();

  /**
   * @return Returns the originStepName.
   */
  public abstract String getOriginStepName();

  /**
   * @return Returns the originStepCopy.
   */
  public abstract int getOriginStepCopy();

  /**
   * @return Returns the destinationStepName.
   */
  public abstract String getDestinationStepName();

  /**
   * @return Returns the destinationStepCopy.
   */
  public abstract int getDestinationStepCopy();

  public abstract String getName();

  /**
   *
   * @return Return the size (or max capacity) of the RowSet
   */
  public abstract int size();

  /**
   * This method is used only in Trans.java when created RowSet at line 333. Don't need any synchronization on this
   * method
   *
   */
  public abstract void setThreadNameFromToCopy( String from, int from_copy, String to, int to_copy );

  /**
   * @return the rowMeta
   */
  public abstract RowMetaInterface getRowMeta();

  /**
   * @param rowMeta
   *          the rowMeta to set
   */
  public abstract void setRowMeta( RowMetaInterface rowMeta );

  /**
   * @return the targetSlaveServer
   */
  public abstract String getRemoteSlaveServerName();

  /**
   * @param remoteSlaveServerName
   *          the remote slave server to set
   */
  public abstract void setRemoteSlaveServerName( String remoteSlaveServerName );

  /**
   * @return true if this row set is blocking.
   */
  public abstract boolean isBlocking();

  /**
   * Clear this rowset: remove all rows and remove the "done" flag.
   */
  public abstract void clear();
}
