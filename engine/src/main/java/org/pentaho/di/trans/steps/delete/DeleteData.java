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

package org.pentaho.di.trans.steps.delete;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseDatabaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import java.sql.PreparedStatement;

/**
 *
 * @author Tom
 * @since 28-March-2006
 *
 */
public class DeleteData extends BaseDatabaseStepData implements StepDataInterface {

  public int[] keynrs; // nr of keylookup -value in row...
  public int[] keynrs2; // nr of keylookup2-value in row...

  public RowMetaInterface outputRowMeta;

  public String schemaTable;

  public RowMetaInterface deleteParameterRowMeta;

  public PreparedStatement prepStatementDelete;

  public DeleteData() {
    super();

    db = null;
  }

}
