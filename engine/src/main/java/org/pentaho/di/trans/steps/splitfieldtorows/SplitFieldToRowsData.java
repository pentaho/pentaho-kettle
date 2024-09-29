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


package org.pentaho.di.trans.steps.splitfieldtorows;

import java.util.regex.Pattern;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class SplitFieldToRowsData extends BaseStepData implements StepDataInterface {
  public int fieldnr;
  public RowMetaInterface outputRowMeta;
  public ValueMetaInterface splitMeta;
  public long rownr;
  public Pattern delimiterPattern;

  public SplitFieldToRowsData() {
    super();
    delimiterPattern = null;
  }

}
