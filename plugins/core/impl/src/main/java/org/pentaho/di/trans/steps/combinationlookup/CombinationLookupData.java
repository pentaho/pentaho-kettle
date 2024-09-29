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


package org.pentaho.di.trans.steps.combinationlookup;

import java.sql.PreparedStatement;
import java.util.Map;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseDatabaseStepData;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class CombinationLookupData extends BaseDatabaseStepData implements StepDataInterface {
  public int[] keynrs; // nrs in row of the keys

  public Map<RowMetaAndData, Long> cache;

  public RowMetaInterface outputRowMeta;
  public RowMetaInterface lookupRowMeta;
  public RowMetaInterface insertRowMeta;
  public RowMetaInterface hashRowMeta;
  public String realTableName;
  public String realSchemaName;
  public boolean[] removeField;

  public String schemaTable;

  public PreparedStatement prepStatementLookup;
  public PreparedStatement prepStatementInsert;
  public long smallestCacheKey;

  /**
   * Default Constructor
   */
  public CombinationLookupData() {
    super();
    db = null;
    realTableName = null;
    realSchemaName = null;
  }
}
