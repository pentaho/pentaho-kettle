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


package org.pentaho.di.trans.steps.monetdbbulkloader;

import nl.cwi.monetdb.mcl.io.BufferedMCLReader;
import nl.cwi.monetdb.mcl.io.BufferedMCLWriter;
import nl.cwi.monetdb.mcl.net.MapiSocket;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Stores data for the MonetDBBulkLoader step.
 *
 * @author James Dixon and Brandon Jackson, inspired by Sven Boden
 * @since 20-feb-2005 updated 01-Jan-2013
 *
 */
public class MonetDBBulkLoaderData extends BaseStepData implements StepDataInterface {
  public Database db;

  public int[] keynrs; // nr of keylookup -value in row...

  public StreamLogger errorLogger;

  public StreamLogger outputLogger;

  // MonetDB API
  public MapiSocket mserver;
  public BufferedMCLReader in;
  public BufferedMCLWriter out;

  public String quote; // fieldEnclosure in MonetDBBulkLoaderMeta
  public String separator; // fieldSeparator in MonetDBBulkLoaderMeta
  public String nullrepresentation; // NULLrepresentation in MonetDBBulkLoaderMeta
  public String newline; // receives value in the init(...) in MonetDBBulkLoader

  public ValueMetaInterface monetDateMeta;
  public ValueMetaInterface monetNumberMeta;

  public ValueMetaInterface monetTimestampMeta;
  public ValueMetaInterface monetTimeMeta;

  public int bufferSize;

  public String[] rowBuffer;

  public int bufferIndex;

  public String schemaTable;

  /**
   * Default constructor.
   */
  public MonetDBBulkLoaderData() {
    super();

    db = null;
  }
}
