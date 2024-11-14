/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.groupby;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class GroupByData extends BaseStepData implements StepDataInterface {
  public Object[] previous;

  /**
   * target value meta for aggregation fields
   */
  public RowMetaInterface aggMeta;
  public Object[] agg;
  public RowMetaInterface groupMeta;
  public RowMetaInterface groupAggMeta; // for speed: groupMeta+aggMeta
  public int[] groupnrs;
  /**
   * array, length is equal to aggMeta value
   * meta list size and metadata subject fields length. Values corresponds to input
   * values used to calculate target results.
   */
  public int[] subjectnrs;
  public long[] counts;

  public Set<Object>[] distinctObjs;

  public ArrayList<Object[]> bufferList;

  public File tempFile;

  public FileOutputStream fosToTempFile;

  public DataOutputStream dosToTempFile;

  public int rowsOnFile;

  public boolean firstRead;

  public FileInputStream fisToTmpFile;
  public DataInputStream disToTmpFile;

  public Object[] groupResult;

  public boolean hasOutput;

  public RowMetaInterface inputRowMeta;
  public RowMetaInterface outputRowMeta;

  public List<Integer> cumulativeSumSourceIndexes;
  public List<Integer> cumulativeSumTargetIndexes;

  public List<Integer> cumulativeAvgSourceIndexes;
  public List<Integer> cumulativeAvgTargetIndexes;

  public Object[] previousSums;

  public Object[] previousAvgSum;

  public long[] previousAvgCount;

  public ValueMetaInterface valueMetaInteger;
  public ValueMetaInterface valueMetaNumber;

  public double[] mean;

  public boolean newBatch;

  public GroupByData() {
    super();

    previous = null;
  }

}
