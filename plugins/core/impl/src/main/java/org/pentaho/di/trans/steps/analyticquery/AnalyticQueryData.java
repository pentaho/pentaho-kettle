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


package org.pentaho.di.trans.steps.analyticquery;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author ngoodman
 * @since 27-jan-2009
 *
 */
public class AnalyticQueryData extends BaseStepData implements StepDataInterface {
  // Grouped Field Indexes (faster than looking up by strings)
  public int[] groupnrs;

  public RowMetaInterface inputRowMeta;
  public RowMetaInterface outputRowMeta;

  // Two Integers for our processing
  // Window Size (the largest N we need to skip forward/back)
  public int window_size;
  // Queue Size (the size of our data queue (Window Size * 2 ) + 1)
  public int queue_size;
  // / Queue Cursor (the current processing location in the queue) reset with every group
  public int queue_cursor;

  // Queue for keeping the data. We will push data onto the queue
  // and pop it off as we process rows. The queue of data is required
  // to get the second previous row and the second ahead row, etc.
  public ConcurrentLinkedQueue<Object[]> data;

  public Object[] previous;

  public AnalyticQueryData() {
    super();

  }

}
