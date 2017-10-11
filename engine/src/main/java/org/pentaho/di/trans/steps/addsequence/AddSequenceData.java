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

package org.pentaho.di.trans.steps.addsequence;

import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class AddSequenceData extends BaseStepData implements StepDataInterface {
  private Database db;
  private String lookup;
  public RowMetaInterface outputRowMeta;
  public Counter counter;

  // The runtime values, in which the environment variables are already resolved
  public long start;
  public long increment;
  public long maximum;

  public String realSchemaName;
  public String realSequenceName;

  /**
   *
   */
  public AddSequenceData() {
    super();

    db = null;
    realSchemaName = null;
    realSequenceName = null;
  }

  /**
   * @return Returns the db.
   */
  public Database getDb() {
    return db;
  }

  /**
   * @param db
   *          The db to set.
   */
  public void setDb( Database db ) {
    this.db = db;
  }

  /**
   * @return Returns the lookup string usually "@@"+the name of the sequence.
   */
  public String getLookup() {
    return lookup;
  }

  /**
   * @param lookup
   *          the lookup string usually "@@"+the name of the sequence.
   */
  public void setLookup( String lookup ) {
    this.lookup = lookup;
  }
}
