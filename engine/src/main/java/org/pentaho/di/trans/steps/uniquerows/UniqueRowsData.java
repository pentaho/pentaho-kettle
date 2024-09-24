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

package org.pentaho.di.trans.steps.uniquerows;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 22-jan-2005
 */
public class UniqueRowsData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface outputRowMeta;
  public RowMetaInterface compareRowMeta;
  public RowMetaInterface inputRowMeta;
  public long counter;
  public Object[] previous;
  public int[] fieldnrs;
  public String compareFields;
  public String realErrorDescription;
  public boolean sendDuplicateRows;

  public UniqueRowsData() {
    super();

    previous = null;
    counter = 0;
    realErrorDescription = null;
    compareFields = null;
    sendDuplicateRows = false;
  }

}
