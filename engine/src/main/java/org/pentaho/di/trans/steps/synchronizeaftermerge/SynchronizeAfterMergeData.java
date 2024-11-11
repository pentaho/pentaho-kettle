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


package org.pentaho.di.trans.steps.synchronizeaftermerge;

import java.sql.PreparedStatement;
import java.sql.Savepoint;
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
 * Performs an insert/update/delete depending on the value of a field.
 *
 * @author Samatar
 * @since 13-10-2008
 */
public class SynchronizeAfterMergeData extends BaseDatabaseStepData implements StepDataInterface {

  public int[] keynrs; // nr of keylookup -value in row...
  public int[] keynrs2; // nr of keylookup2-value in row...
  public int[] valuenrs; // Stream valuename nrs to prevent searches.
  public int indexOfTableNameField;

  public int indexOfOperationOrderField;

  // List<String> updateColumns = new ArrayList<String>();
  /**
   * Mapping between the SQL and the actual prepared statement. Normally this is only one, but in case we have more then
   * one, it's convenient to have this.
   */
  public Map<String, PreparedStatement> preparedStatements;
  public String realTableName;
  public String realSchemaName;
  public String realSchemaTable;

  /** Use batch mode or not? */
  public boolean batchMode;

  PreparedStatement insertStatement;
  PreparedStatement lookupStatement;
  PreparedStatement updateStatement;
  PreparedStatement deleteStatement;

  public String insertValue;
  public String updateValue;
  public String deleteValue;

  public String stringErrorKeyNotFound;

  public String stringFieldnames;

  public boolean lookupFailure;

  public RowMetaInterface outputRowMeta;
  public RowMetaInterface inputRowMeta;

  public RowMetaInterface deleteParameterRowMeta;
  public RowMetaInterface updateParameterRowMeta;
  public RowMetaInterface lookupParameterRowMeta;
  public RowMetaInterface lookupReturnRowMeta;
  public RowMetaInterface insertRowMeta;

  public Map<String, Integer> commitCounterMap;
  public int commitSize;
  public DatabaseMeta databaseMeta;
  public boolean specialErrorHandling;
  public Savepoint savepoint;
  public boolean releaseSavepoint;
  public boolean supportsSavepoints;

  public List<Object[]> batchBuffer;

  /**
   * Default constructor.
   */
  public SynchronizeAfterMergeData() {
    super();
    insertStatement = null;
    lookupStatement = null;
    updateStatement = null;
    deleteStatement = null;

    indexOfTableNameField = -1;

    db = null;
    preparedStatements = new Hashtable<String, PreparedStatement>();
    realTableName = null;
    realSchemaName = null;
    batchMode = false;
    insertValue = null;
    updateValue = null;
    deleteValue = null;
    indexOfOperationOrderField = -1;
    lookupFailure = false;
    realSchemaTable = null;
    commitCounterMap = new HashMap<String, Integer>();
    batchBuffer = new ArrayList<Object[]>();
    releaseSavepoint = true;

  }
}
