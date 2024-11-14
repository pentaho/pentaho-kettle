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


package org.pentaho.di.trans.steps.pgbulkloader;

import java.io.OutputStream;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.postgresql.PGConnection;

/**
 * Stores data for the GPBulkLoader step.
 *
 * @author Sven Boden
 * @since 20-feb-2005
 */
public class PGBulkLoaderData extends BaseStepData implements StepDataInterface {
  public Database db;

  public int[] keynrs; // nr of keylookup -value in row...

  public StreamLogger errorLogger;

  public Process psqlProcess;

  public StreamLogger outputLogger;

  public OutputStream pgOutputStream;

  public byte[] quote;
  public byte[] separator;
  public byte[] newline;

  public PGConnection pgdb;

  public int[] dateFormatChoices;

  public ValueMetaInterface dateMeta;
  public ValueMetaInterface dateTimeMeta;

  /**
   * Default constructor.
   */
  public PGBulkLoaderData() {
    super();

    db = null;

    // Let's use ISO 8601 format. This in unambiguous with PostgreSQL
    dateMeta = new ValueMetaDate( "date" );
    dateMeta.setConversionMask( "yyyy-MM-dd" );

    dateTimeMeta = new ValueMetaDate( "date" );
    // Let's keep milliseconds. Didn't find a way to keep microseconds (max resolution with PG)
    dateTimeMeta.setConversionMask( "yyyy-MM-dd HH:mm:ss.SSS" );
  }
}
