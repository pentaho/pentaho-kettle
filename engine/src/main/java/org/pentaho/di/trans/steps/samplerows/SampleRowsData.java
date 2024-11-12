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


package org.pentaho.di.trans.steps.samplerows;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.RangeSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 24-jan-2008
 */
public class SampleRowsData extends BaseStepData implements StepDataInterface {

  public RangeSet<Integer> rangeSet;
  public boolean addlineField;
  public RowMetaInterface previousRowMeta;
  public RowMetaInterface outputRowMeta;
  public Object[] outputRow;
  public int NrPrevFields;

  public SampleRowsData() {
    super();
    rangeSet = ImmutableRangeSet.of();
    addlineField = false;
    outputRow = null;
  }

}
