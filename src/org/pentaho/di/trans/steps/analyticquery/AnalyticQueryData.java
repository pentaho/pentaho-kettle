/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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
public class AnalyticQueryData extends BaseStepData implements StepDataInterface
{
	// Grouped Field Indexes (faster than looking up by strings)
	public int  groupnrs[];
    
    public RowMetaInterface inputRowMeta;
    public RowMetaInterface outputRowMeta;
    
    // Two Integers for our processing
    // Window Size (the largest N we need to skip forward/back)
    public int window_size;
    // Queue Size (the size of our data queue (Window Size * 2 ) + 1)
    public int queue_size;
    /// Queue Cursor (the current processing location in the queue) reset with every group
    public int queue_cursor;
    
    // Queue for keeping the data.  We will push data onto the queue
    // and pop it off as we process rows.  The queue of data is required
    // to get the second previous row and the second ahead row, etc.
    public ConcurrentLinkedQueue<Object[]> data;
    
    public Object previous[];

	public AnalyticQueryData()
	{
		super();

	
	}

}
