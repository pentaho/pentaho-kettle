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

package org.pentaho.di.trans.steps.databaselookup;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class DatabaseLookupData extends BaseStepData implements StepDataInterface {
  public Cache cache;
  public Database db;

  public Object[] nullif; // Not found: default values...
  public int[] keynrs; // nr of keylookup -value in row...
  public int[] keynrs2; // nr of keylookup2-value in row...
  public int[] keytypes; // Types of the desired database values

  public RowMetaInterface outputRowMeta;
  public RowMetaInterface lookupMeta;
  public RowMetaInterface returnMeta;
  public boolean isCanceled;
  public boolean allEquals;
  public int[] conditions;
  public boolean hasDBCondition;

  public DatabaseLookupData() {
    super();

    db = null;
  }

  /**
   * Cache for {@code DatabaseLookup} step.
   */
  public interface Cache {
    /**
     * Returns the very first data row that matches all conditions or {@code null} if none has been found.
     * Note, cache should keep the order in which elements were put into it.
     *
     * @param lookupMeta  meta object for dealing with {@code lookupRow}
     * @param lookupRow   tuple containing values for comparison
     * @return first matching data row or {@code null}
     * @throws KettleException
     */
    Object[] getRowFromCache( RowMetaInterface lookupMeta, Object[] lookupRow ) throws KettleException;

    /**
     * Saved {@code add} as data row and {@code lookupRow} as a key for searching it.
     *
     * @param meta        step's meta
     * @param lookupMeta  {@code lookupRow}'s meta
     * @param lookupRow   tuple of keys
     * @param add         tuple of data
     */
    void storeRowInCache( DatabaseLookupMeta meta, RowMetaInterface lookupMeta, Object[] lookupRow, Object[] add );
  }
}
