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


package org.pentaho.di.trans.steps.transexecutor;

import java.util.List;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 18-mar-2013
 *
 */
public class TransExecutorData extends BaseStepData implements StepDataInterface {
  private Trans executorTrans;
  private TransMeta executorTransMeta;

  private RowMetaInterface inputRowMeta;

  private RowMetaInterface executorStepOutputRowMeta;
  private RowMetaInterface resultRowsOutputRowMeta;
  private RowMetaInterface executionResultsOutputRowMeta;
  private RowMetaInterface resultFilesOutputRowMeta;

  public List<RowMetaAndData> groupBuffer;
  public int groupSize;
  public int groupTime;
  public long groupTimeStart;
  public String groupField;
  public int groupFieldIndex;
  public ValueMetaInterface groupFieldMeta;

  public Object prevGroupFieldData;

  private RowSet executorStepOutputRowSet;
  private RowSet resultRowsRowSet;
  private RowSet resultFilesRowSet;
  private RowSet executionResultRowSet;

  public TransExecutorData() {
    super();
  }

  public Trans getExecutorTrans() {
    return executorTrans;
  }

  public void setExecutorTrans( Trans executorTrans ) {
    this.executorTrans = executorTrans;
  }

  public TransMeta getExecutorTransMeta() {
    return executorTransMeta;
  }

  public void setExecutorTransMeta( TransMeta executorTransMeta ) {
    this.executorTransMeta = executorTransMeta;
  }

  public RowMetaInterface getInputRowMeta() {
    return inputRowMeta;
  }

  public void setInputRowMeta( RowMetaInterface inputRowMeta ) {
    this.inputRowMeta = inputRowMeta;
  }

  public RowMetaInterface getExecutorStepOutputRowMeta() {
    return executorStepOutputRowMeta;
  }

  public void setExecutorStepOutputRowMeta( RowMetaInterface executorStepOutputRowMeta ) {
    this.executorStepOutputRowMeta = executorStepOutputRowMeta;
  }

  public RowMetaInterface getResultRowsOutputRowMeta() {
    return resultRowsOutputRowMeta;
  }

  public void setResultRowsOutputRowMeta( RowMetaInterface resultRowsOutputRowMeta ) {
    this.resultRowsOutputRowMeta = resultRowsOutputRowMeta;
  }

  public RowMetaInterface getExecutionResultsOutputRowMeta() {
    return executionResultsOutputRowMeta;
  }

  public void setExecutionResultsOutputRowMeta( RowMetaInterface executionResultsOutputRowMeta ) {
    this.executionResultsOutputRowMeta = executionResultsOutputRowMeta;
  }

  public RowMetaInterface getResultFilesOutputRowMeta() {
    return resultFilesOutputRowMeta;
  }

  public void setResultFilesOutputRowMeta( RowMetaInterface resultFilesOutputRowMeta ) {
    this.resultFilesOutputRowMeta = resultFilesOutputRowMeta;
  }

  public RowSet getExecutorStepOutputRowSet() {
    return executorStepOutputRowSet;
  }

  public void setExecutorStepOutputRowSet( RowSet executorStepOutputRowSet ) {
    this.executorStepOutputRowSet = executorStepOutputRowSet;
  }

  public RowSet getResultRowsRowSet() {
    return resultRowsRowSet;
  }

  public void setResultRowsRowSet( RowSet resultRowsRowSet ) {
    this.resultRowsRowSet = resultRowsRowSet;
  }

  public RowSet getResultFilesRowSet() {
    return resultFilesRowSet;
  }

  public void setResultFilesRowSet( RowSet resultFilesRowSet ) {
    this.resultFilesRowSet = resultFilesRowSet;
  }

  public RowSet getExecutionResultRowSet() {
    return executionResultRowSet;
  }

  public void setExecutionResultRowSet( RowSet executionResultRowSet ) {
    this.executionResultRowSet = executionResultRowSet;
  }
}
