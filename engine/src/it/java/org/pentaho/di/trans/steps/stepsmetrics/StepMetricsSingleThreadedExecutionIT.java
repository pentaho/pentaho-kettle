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



package org.pentaho.di.trans.steps.stepsmetrics;

import org.pentaho.test.util.SingleThreadedExecutionGuarder;

public class StepMetricsSingleThreadedExecutionIT extends SingleThreadedExecutionGuarder<StepsMetricsMeta> {

  @Override protected StepsMetricsMeta createMeta() {
    return new StepsMetricsMeta();
  }
}
