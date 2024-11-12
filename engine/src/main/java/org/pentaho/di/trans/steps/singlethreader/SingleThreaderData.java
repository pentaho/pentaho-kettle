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


package org.pentaho.di.trans.steps.singlethreader;

import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.SingleThreadedTransExecutor;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class SingleThreaderData extends BaseStepData implements StepDataInterface {
  public Trans mappingTrans;

  public SingleThreadedTransExecutor executor;

  public int batchSize;

  public TransMeta mappingTransMeta;

  public RowMetaInterface outputRowMeta;
  public RowProducer rowProducer;

  public int batchCount;
  public int batchTime;
  public long startTime;
  public StepMeta injectStepMeta;
  public StepMeta retrieveStepMeta;
  public List<Object[]> errorBuffer;
  public int lastLogLine;

  public SingleThreaderData() {
    super();
    mappingTrans = null;

  }

  public Trans getMappingTrans() {
    return mappingTrans;
  }

  public void setMappingTrans( Trans mappingTrans ) {
    this.mappingTrans = mappingTrans;
  }
}
