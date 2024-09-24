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

package org.pentaho.di.trans.steps.update;

import java.sql.PreparedStatement;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseDatabaseStepData;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 28-feb-2005
 *
 */
public class UpdateData extends BaseDatabaseStepData implements StepDataInterface {

  public int[] keynrs; // nr of keylookup -value in row...
  public int[] keynrs2; // nr of keylookup2-value in row...
  public int[] valuenrs; // Stream valuename nrs to prevent searches.

  public String stringErrorKeyNotFound;

  public String stringFieldnames;

  public RowMetaInterface outputRowMeta;

  public String schemaTable;

  public PreparedStatement prepStatementLookup;
  public PreparedStatement prepStatementUpdate;

  public RowMetaInterface lookupParameterRowMeta;
  public RowMetaInterface lookupReturnRowMeta;
  public RowMetaInterface updateParameterRowMeta;

  public UpdateData() {
    super();

    db = null;
  }

}
