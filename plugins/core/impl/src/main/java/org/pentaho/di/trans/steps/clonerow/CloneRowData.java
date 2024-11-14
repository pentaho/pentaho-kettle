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


package org.pentaho.di.trans.steps.clonerow;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 27-06-2008
 */
public class CloneRowData extends BaseStepData implements StepDataInterface {

  public long nrclones;
  public RowMetaInterface outputRowMeta;
  public int indexOfNrCloneField;
  public boolean addInfosToRow;
  public int NrPrevFields;

  public CloneRowData() {
    super();
    nrclones = 0;
    indexOfNrCloneField = -1;
    addInfosToRow = false;
    NrPrevFields = 0;
  }

}
