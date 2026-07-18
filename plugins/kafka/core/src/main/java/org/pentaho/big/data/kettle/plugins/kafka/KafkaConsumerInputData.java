/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.big.data.kettle.plugins.kafka;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorData;


public class KafkaConsumerInputData extends TransExecutorData implements StepDataInterface {
  public RowMetaInterface outputRowMeta;

  /**
   *
   */
  public KafkaConsumerInputData() {
    super();
  }
}
