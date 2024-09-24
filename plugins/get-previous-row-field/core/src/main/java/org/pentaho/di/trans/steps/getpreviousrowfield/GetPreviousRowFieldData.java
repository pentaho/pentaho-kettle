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

package org.pentaho.di.trans.steps.getpreviousrowfield;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * return field value from previous row.
 *
 * @author Samatar Hassan
 * @since 07 September 2008
 */
public class GetPreviousRowFieldData extends BaseStepData implements StepDataInterface {

  public int[] inStreamNrs; // string infields
  public String[] outStreamNrs;
  public Object[] previousRow;
  public RowMetaInterface inputRowMeta;
  public RowMetaInterface outputRowMeta;
  public int NrPrevFields;

  /**
   * Default constructor.
   */
  public GetPreviousRowFieldData() {
    super();
    previousRow = null;
    NrPrevFields = 0;
  }
}
