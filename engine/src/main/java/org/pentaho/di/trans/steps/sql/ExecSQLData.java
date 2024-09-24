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

package org.pentaho.di.trans.steps.sql;

import java.util.List;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 20-jan-2005
 */
public class ExecSQLData extends BaseStepData implements StepDataInterface {
  public Database db;
  public Result result;
  public int[] argumentIndexes;
  public List<Integer> markerPositions;
  public RowMetaInterface outputRowMeta;
  public String sql;
  public boolean isCanceled;
  public RowMetaInterface paramsMeta;

  public ExecSQLData() {
    super();

    db = null;
    result = null;
    argumentIndexes = null;
    markerPositions = null;
    paramsMeta = null;
  }
}
