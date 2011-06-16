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
 

package org.pentaho.di.trans.steps.multimerge;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Comparator;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Biswapesh
 * @since 24-nov-2005
 *
 */

public class MultiMergeJoinData extends BaseStepData implements StepDataInterface
{
        static public class QueueEntry {
            public Object[] row;
            public int index;
        }
	
        static public class QueueComparator implements Comparator<QueueEntry> {
            MultiMergeJoinData data;
            QueueComparator(MultiMergeJoinData data) {
                this.data = data;
            }
            
            @Override
            public int compare(QueueEntry a, QueueEntry b) {
                try {
                    int cmp = data.metas[a.index].compare(a.row, data.metas[b.index], b.row, data.keyNrs[a.index], data.keyNrs[b.index]);
                    return cmp > 0 ? 1 : cmp < 0 ? -1 : 0;
                } catch (KettleException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        }

	public Object[][] rows;
	public RowMetaInterface[] metas;
	public RowMetaInterface outputRowMeta; //just for speed: oneMeta+twoMeta
	public Object[][] dummy;
    public List<List<Object[]>> results;
    public PriorityQueue<QueueEntry> queue;
    public boolean optional;
    public int[][] keyNrs;
    public int[] drainIndices;
	
    public RowSet[] rowSets;
    public QueueEntry[] queueEntries;
    public int[] rowLengths;
    
	/**
	 * Default initializer
	 */
	public MultiMergeJoinData()
	{
		super();
		rows = null;
		metas = null;
		dummy = null;
		optional = false;
		keyNrs = null;
	}

}
