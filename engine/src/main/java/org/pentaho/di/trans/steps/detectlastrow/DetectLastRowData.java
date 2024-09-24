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

package org.pentaho.di.trans.steps.detectlastrow;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 03June2008
 */
public class DetectLastRowData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface outputRowMeta;
  public int NrPrevFields;
  public RowMetaInterface previousRowMeta;

  private final Object[] trueArray = new Object[] { Boolean.TRUE };

  private final Object[] falseArray = new Object[] { Boolean.FALSE };

  /**
   * Return a array with a constant True.
   */
  public Object[] getTrueArray() {
    return trueArray;
  }

  /**
   * Return a array with a constant False.
   */
  public Object[] getFalseArray() {
    return falseArray;
  }
}
