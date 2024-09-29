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


package org.pentaho.di.trans.steps.jobexecutor;

import java.util.List;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class JobExecutorData extends BaseStepData implements StepDataInterface {
  public Job executorJob;
  public JobMeta executorJobMeta;
  public RowMetaInterface inputRowMeta;
  public RowMetaInterface executionResultsOutputRowMeta;
  public RowMetaInterface resultRowsOutputRowMeta;
  public RowMetaInterface resultFilesOutputRowMeta;

  public List<RowMetaAndData> groupBuffer;
  public int groupSize;
  public int groupTime;
  public long groupTimeStart;
  public String groupField;
  public int groupFieldIndex;
  public ValueMetaInterface groupFieldMeta;
  public Object prevGroupFieldData;
  public RowSet resultRowsRowSet;
  public RowSet resultFilesRowSet;
  public RowSet executionResultRowSet;

  public JobExecutorData() {
    super();
  }

}
