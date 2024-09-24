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

package org.pentaho.di.trans.steps.tableinput;

import java.sql.ResultSet;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseDatabaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

/**
 * @author Matt
 * @since 20-jan-2005
 */
public class TableInputData extends BaseDatabaseStepData implements StepDataInterface {
  public Object[] nextrow;
  public Object[] thisrow;
  public ResultSet rs;
  public String lookupStep;
  public RowMetaInterface rowMeta;
  public RowSet rowSet;
  public boolean isCanceled;
  public StreamInterface infoStream;

  public TableInputData() {
    super();

    db = null;
    thisrow = null;
    nextrow = null;
    rs = null;
    lookupStep = null;
  }

}
