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

package org.pentaho.di.trans.steps.columnexists;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseDatabaseStepData;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class ColumnExistsData extends BaseDatabaseStepData implements StepDataInterface {
  public int indexOfTablename;
  public int indexOfColumnname;
  public String tablename;
  public String schemaname;
  public RowMetaInterface outputRowMeta;

  public ColumnExistsData() {
    super();
    indexOfTablename = -1;
    indexOfColumnname = -1;
    tablename = null;
    schemaname = null;
    db = null;
  }

}
