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

package org.pentaho.di.trans.steps.execsqlrow;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseDatabaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 20-jan-2005
 */
public class ExecSQLRowData extends BaseDatabaseStepData implements StepDataInterface {
  public Result result;
  public int indexOfSQLFieldname;
  public RowMetaInterface outputRowMeta;

  public ExecSQLRowData() {
    super();
    db = null;
    result = null;
    indexOfSQLFieldname = -1;
  }
}
