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

package org.pentaho.di.trans.steps.setvaluefield;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class SetValueFieldData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface outputRowMeta;
  public RowMetaInterface convertRowMeta;

  public int[] indexOfField;
  public int[] indexOfReplaceByValue;

  public SetValueFieldData() {
    super();
  }

}
