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

package org.pentaho.di.trans.steps.tableoutput;

import java.sql.PreparedStatement;
import java.sql.Savepoint;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseDatabaseStepData;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Storage class for table output step.
 *
 * @author Matt
 * @since 24-jan-2005
 */
public class TableOutputData extends BaseDatabaseStepData implements StepDataInterface {
  public int warnings;
  public String tableName;
  public int[] valuenrs; // Stream valuename nrs to prevent searches.

  /**
   * Mapping between the SQL and the actual prepared statement. Normally this is only one, but in case we have more then
   * one, it's convenient to have this.
   */
  public Map<String, PreparedStatement> preparedStatements;

  public int indexOfPartitioningField;

  /** Cache of the data formatter object */
  public SimpleDateFormat dateFormater;

  /** Use batch mode or not? */
  public boolean batchMode;
  public int indexOfTableNameField;

  public List<Object[]> batchBuffer;
  public boolean sendToErrorRow;
  public RowMetaInterface outputRowMeta;
  public RowMetaInterface insertRowMeta;
  public boolean useSafePoints;
  public Savepoint savepoint;
  public boolean releaseSavepoint;

  public DatabaseMeta databaseMeta;

  public Map<String, Integer> commitCounterMap;

  public int commitSize;

  public TableOutputData() {
    super();

    db = null;
    warnings = 0;
    tableName = null;

    preparedStatements = new Hashtable<String, PreparedStatement>();

    indexOfPartitioningField = -1;
    indexOfTableNameField = -1;

    batchBuffer = new ArrayList<Object[]>();
    commitCounterMap = new HashMap<String, Integer>();

    releaseSavepoint = true;
  }
}
