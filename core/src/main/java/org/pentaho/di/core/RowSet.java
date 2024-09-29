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
  boolean putRow( RowMetaInterface rowMeta, Object[] rowData );

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
  boolean putRowWait( RowMetaInterface rowMeta, Object[] rowData, long time, TimeUnit tu );

  /**
   * Get a row from the input buffer, it blocks for a short period until a new row becomes available. Otherwise, it
   * returns null.
   *
   * @return a row of data or null if no row is available.
   */
  Object[] getRow();

  /**
   * Get the first row in the list immediately.
   *
   * @return a row of data or null if no row is available.
   */
  Object[] getRowImmediate();

  /**
   * get the first row in the list immediately if it is available or wait until timeout
   *
   * @return a row of data or null if no row is available.
   */
  Object[] getRowWait( long timeout, TimeUnit tu );

  /**
   * @return Set indication that there is no more input
   */
  void setDone();

  /**
   * @return Returns true if there is no more input and vice versa
   */
  boolean isDone();

  /**
   * @return Returns the originStepName.
   */
  String getOriginStepName();

  /**
   * @return Returns the originStepCopy.
   */
  int getOriginStepCopy();

  /**
   * @return Returns the destinationStepName.
   */
  String getDestinationStepName();

  /**
   * @return Returns the destinationStepCopy.
   */
  int getDestinationStepCopy();

  String getName();

  /**
   *
   * @return Return the size (or max capacity) of the RowSet
   */
  int size();

  /**
   * This method is used only in Trans.java when created RowSet at line 333. Don't need any synchronization on this
   * method
   *
   */
  void setThreadNameFromToCopy( String from, int fromCopy, String to, int toCopy );

  /**
   * @return the rowMeta
   */
  RowMetaInterface getRowMeta();

  /**
   * @param rowMeta
   *          the rowMeta to set
   */
  void setRowMeta( RowMetaInterface rowMeta );

  /**
   * @return the targetSlaveServer
   */
  String getRemoteSlaveServerName();

  /**
   * @param remoteSlaveServerName
   *          the remote slave server to set
   */
  void setRemoteSlaveServerName( String remoteSlaveServerName );

  /**
   * @return true if this row set is blocking.
   */
  boolean isBlocking();

  /**
   * Clear this rowset: remove all rows and remove the "done" flag.
   */
  void clear();
}
