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


package org.pentaho.di.trans.steps.mergerows;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class MergeRowsData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface outputRowMeta;

  public Object[] one, two;
  public int[] keyNrs;
  public int[] valueNrs;

  public RowSet oneRowSet;
  public RowSet twoRowSet;

  public MergeRowsData() {
    super();
  }

}
