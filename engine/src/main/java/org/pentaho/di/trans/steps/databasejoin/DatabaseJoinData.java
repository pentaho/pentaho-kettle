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


package org.pentaho.di.trans.steps.databasejoin;

import java.sql.PreparedStatement;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseDatabaseStepData;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class DatabaseJoinData extends BaseDatabaseStepData implements StepDataInterface {
  public PreparedStatement pstmt;

  RowMetaInterface outputRowMeta;
  RowMetaInterface lookupRowMeta;

  public int[] keynrs; // parameter value index in an input row...
  public Object[] notfound; // Values in case nothing is found...
  public boolean isCanceled;

  public DatabaseJoinData() {
    super();
  }
}
