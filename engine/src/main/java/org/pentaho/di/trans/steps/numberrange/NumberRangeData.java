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


package org.pentaho.di.trans.steps.numberrange;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * + * Data for the NumberRangePlugin + * + * @author ronny.roeller@fredhopper.com + * +
 */
public class NumberRangeData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface outputRowMeta;
  public int inputColumnNr;

  public NumberRangeData() {
    super();
    inputColumnNr = -1;
  }
}
