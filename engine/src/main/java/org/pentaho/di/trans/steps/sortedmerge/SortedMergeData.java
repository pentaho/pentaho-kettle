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


package org.pentaho.di.trans.steps.sortedmerge;

import java.util.Comparator;
import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class SortedMergeData extends BaseStepData implements StepDataInterface {
  public int[] fieldIndices;
  // public RowComparator rowComparator;
  public RowMetaInterface rowMeta;
  public List<RowSetRow> sortedBuffer;
  public Comparator<RowSetRow> comparator;

  public SortedMergeData() {
    super();
  }
}
