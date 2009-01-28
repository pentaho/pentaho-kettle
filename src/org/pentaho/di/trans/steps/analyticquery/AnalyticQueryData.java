 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

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
