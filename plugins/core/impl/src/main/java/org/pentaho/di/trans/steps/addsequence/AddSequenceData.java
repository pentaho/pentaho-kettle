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
