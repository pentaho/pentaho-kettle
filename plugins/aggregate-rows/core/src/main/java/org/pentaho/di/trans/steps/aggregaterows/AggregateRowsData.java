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


package org.pentaho.di.trans.steps.aggregaterows;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class AggregateRowsData extends BaseStepData implements StepDataInterface {
  public int[] fieldnrs;
  public int nrfields;
  public Object[] values;
  public long[] counts;
  public RowMetaInterface outputRowMeta;

  public AggregateRowsData() {
    super();
  }

}
