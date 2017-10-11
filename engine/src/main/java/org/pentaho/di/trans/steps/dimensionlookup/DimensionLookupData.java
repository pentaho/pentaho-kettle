/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.dimensionlookup;

import java.sql.PreparedStatement;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.hash.ByteArrayHashMap;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class DimensionLookupData extends BaseStepData implements StepDataInterface {
  public Date valueDateNow;

  public Database db;

  public Date min_date;
  public Date max_date;

  public int[] keynrs; // nrs in row of the keys
  public int[] fieldnrs; // nrs in row of the fields
  public int datefieldnr; // Nr of datefield field in row

  public ByteArrayHashMap cache;

  public long smallestCacheKey;

  public Long notFoundTk;

  public RowMetaInterface outputRowMeta;

  public RowMetaInterface lookupRowMeta;
  public RowMetaInterface returnRowMeta;

  public PreparedStatement prepStatementLookup;
  public PreparedStatement prepStatementInsert;
  public PreparedStatement prepStatementUpdate;
  public PreparedStatement prepStatementDimensionUpdate;
  public PreparedStatement prepStatementPunchThrough;

  public RowMetaInterface insertRowMeta;
  public RowMetaInterface updateRowMeta;
  public RowMetaInterface dimensionUpdateRowMeta;
  public RowMetaInterface punchThroughRowMeta;

  public RowMetaInterface cacheKeyRowMeta;
  public RowMetaInterface cacheValueRowMeta;

  public String schemaTable;

  public String realTableName;
  public String realSchemaName;

  public int startDateChoice;

  public int startDateFieldIndex;

  public int[] preloadKeyIndexes;

  public int preloadFromDateIndex;
  public int preloadToDateIndex;

  public DimensionCache preloadCache;

  public List<Integer> preloadIndexes;

  public List<Integer> lazyList;

  /**
   * The input row metadata, but converted to normal storage type
   */
  public RowMetaInterface inputRowMeta;

  public DimensionLookupData() {
    super();

    db = null;
    valueDateNow = null;
    smallestCacheKey = -1;
    realTableName = null;
    realSchemaName = null;
  }

}
