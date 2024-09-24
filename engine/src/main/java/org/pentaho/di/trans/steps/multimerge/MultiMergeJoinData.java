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

package org.pentaho.di.trans.steps.multimerge;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

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

public class MultiMergeJoinData extends BaseStepData implements StepDataInterface {
  public static class QueueEntry {
    public Object[] row;
    public int index;
  }

  public static class QueueComparator implements Comparator<QueueEntry> {
    MultiMergeJoinData data;

    QueueComparator( MultiMergeJoinData data ) {
      this.data = data;
    }

    @Override
    public int compare( QueueEntry a, QueueEntry b ) {
      try {
        int cmp =
          data.metas[a.index].compare(
            a.row, data.metas[b.index], b.row, data.keyNrs[a.index], data.keyNrs[b.index] );
        return cmp > 0 ? 1 : cmp < 0 ? -1 : 0;
      } catch ( KettleException e ) {
        throw new RuntimeException( e.getMessage() );
      }
    }
  }

  public Object[][] rows;
  public RowMetaInterface[] metas;
  public RowMetaInterface outputRowMeta; // just for speed: oneMeta+twoMeta
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
  public MultiMergeJoinData() {
    super();
    rows = null;
    metas = null;
    dummy = null;
    optional = false;
    keyNrs = null;
  }

}
