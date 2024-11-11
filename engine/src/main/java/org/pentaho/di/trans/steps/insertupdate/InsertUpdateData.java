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


package org.pentaho.di.trans.steps.insertupdate;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseDatabaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import java.sql.PreparedStatement;

/**
 * Stores data for the Insert/Update step.
 *
 * @author Matt
 * @since 24-jan-2005
 */
public class InsertUpdateData extends BaseDatabaseStepData implements StepDataInterface {

  public int[] keynrs; // nr of keylookup -value in row...
  public int[] keynrs2; // nr of keylookup2-value in row...
  public int[] valuenrs; // Stream valuename nrs to prevent searches.

  public RowMetaInterface outputRowMeta;

  public String schemaTable;

  public PreparedStatement prepStatementLookup;
  public PreparedStatement prepStatementUpdate;

  public RowMetaInterface updateParameterRowMeta;
  public RowMetaInterface lookupParameterRowMeta;
  public RowMetaInterface lookupReturnRowMeta;
  public RowMetaInterface insertRowMeta;

  /**
   * Default constructor.
   */
  public InsertUpdateData() {
    super();

    db = null;
  }
}
