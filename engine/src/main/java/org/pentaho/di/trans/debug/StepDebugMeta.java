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

package org.pentaho.di.trans.debug;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Condition;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.StepMeta;

/**
 * This class is used to define debugging meta data for a certain step. Basically it defines for which condition a
 * break-point becomes active.
 *
 * @author Matt
 *
 */
public class StepDebugMeta {

  public static final String XML_TAG = "step-debug-meta";

  private StepMeta stepMeta;

  private int rowCount;

  private boolean readingFirstRows;
  private boolean pausingOnBreakPoint;

  private Condition condition;

  private List<BreakPointListener> breakPointListers;

  private RowMetaInterface rowBufferMeta;
  private List<Object[]> rowBuffer;

  private int numberOfHits;

  public StepDebugMeta() {
    rowBuffer = new ArrayList<Object[]>();
    breakPointListers = new ArrayList<BreakPointListener>();
    numberOfHits = 0;
    readingFirstRows = true;
  }

  public StepDebugMeta( StepMeta stepMeta ) {
    this();
    this.stepMeta = stepMeta;
  }

  /**
   * @return the stepMeta
   */
  public StepMeta getStepMeta() {
    return stepMeta;
  }

  /**
   * @param stepMeta
   *          the stepMeta to set
   */
  public void setStepMeta( StepMeta stepMeta ) {
    this.stepMeta = stepMeta;
  }

  /**
   * @return the condition
   */
  public Condition getCondition() {
    return condition;
  }

  /**
   * @param condition
   *          the condition to set
   */
  public void setCondition( Condition condition ) {
    this.condition = condition;
  }

  /**
   * Add a break-point listener to the debug information. The listener will be called whenever a condition is hit
   *
   * @param breakPointListener
   *          the break point listener to add
   */
  public void addBreakPointListener( BreakPointListener breakPointListener ) {
    breakPointListers.add( breakPointListener );
  }

  /**
   * Remove a break-point listener from the debug information.
   *
   * @param breakPointListener
   *          the break point listener to remove
   */
  public void removeBreakPointListener( BreakPointListener breakPointListener ) {
    breakPointListers.remove( breakPointListener );
  }

  public void fireBreakPointListeners( TransDebugMeta transDebugMeta ) {
    for ( BreakPointListener listener : breakPointListers ) {
      listener.breakPointHit( transDebugMeta, this, rowBufferMeta, rowBuffer );
    }
    numberOfHits++;
  }

  /**
   * @return the rowCount
   */
  public int getRowCount() {
    return rowCount;
  }

  /**
   * @param rowCount
   *          the rowCount to set
   */
  public void setRowCount( int rowCount ) {
    this.rowCount = rowCount;
  }

  /**
   * @return the readingFirstRows
   */
  public boolean isReadingFirstRows() {
    return readingFirstRows;
  }

  /**
   * @param readingFirstRows
   *          the readingFirstRows to set
   */
  public void setReadingFirstRows( boolean readingFirstRows ) {
    this.readingFirstRows = readingFirstRows;
  }

  /**
   * @return the pausingOnBreakPoint
   */
  public boolean isPausingOnBreakPoint() {
    return pausingOnBreakPoint;
  }

  /**
   * @param pausingOnBreakPoint
   *          the pausingOnBreakPoint to set
   */
  public void setPausingOnBreakPoint( boolean pausingOnBreakPoint ) {
    this.pausingOnBreakPoint = pausingOnBreakPoint;
  }

  /**
   * @return the rowBufferMeta
   */
  public RowMetaInterface getRowBufferMeta() {
    return rowBufferMeta;
  }

  /**
   * @param rowBufferMeta
   *          the rowBufferMeta to set
   */
  public void setRowBufferMeta( RowMetaInterface rowBufferMeta ) {
    this.rowBufferMeta = rowBufferMeta;
  }

  /**
   * @return the rowBuffer
   */
  public List<Object[]> getRowBuffer() {
    return rowBuffer;
  }

  /**
   * @param rowBuffer
   *          the rowBuffer to set
   */
  public void setRowBuffer( List<Object[]> rowBuffer ) {
    this.rowBuffer = rowBuffer;
  }

  /**
   * @return the number of times the break-point listeners got called
   */
  public int getNumberOfHits() {
    return numberOfHits;
  }

  /**
   * @param numberOfHits
   *          the number of times the break-point listeners got called
   */
  public void setNumberOfHits( int numberOfHits ) {
    this.numberOfHits = numberOfHits;
  }
}
