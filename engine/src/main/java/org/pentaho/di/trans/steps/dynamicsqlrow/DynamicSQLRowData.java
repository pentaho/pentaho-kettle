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

package org.pentaho.di.trans.steps.dynamicsqlrow;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseDatabaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import java.util.ArrayList;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class DynamicSQLRowData extends BaseDatabaseStepData implements StepDataInterface {
  RowMetaInterface outputRowMeta;
  RowMetaInterface lookupRowMeta;

  public Object[] notfound; // Values in case nothing is found...

  public int indexOfSQLField;

  public boolean skipPreviousRow;

  public String previousSQL;

  public ArrayList<Object[]> previousrowbuffer;

  public boolean isCanceled;

  public DynamicSQLRowData() {
    super();

    db = null;
    notfound = null;
    indexOfSQLField = -1;
    skipPreviousRow = false;
    previousSQL = null;
    previousrowbuffer = new ArrayList<Object[]>();
  }
}
