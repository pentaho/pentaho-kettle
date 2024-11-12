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


package org.pentaho.di.trans.steps.sqlfileoutput;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class SQLFileOutputData extends BaseStepData implements StepDataInterface {
  public int splitnr;

  public Database db;

  public String tableName;

  public OutputStreamWriter writer;

  public OutputStream fos;
  public RowMetaInterface outputRowMeta;

  /** Cache of the data formatter object */
  public SimpleDateFormat dateFormater;

  public boolean sendToErrorRow;

  public RowMetaInterface insertRowMeta;

  public SQLFileOutputData() {
    super();

    db = null;
    tableName = null;

  }

}
