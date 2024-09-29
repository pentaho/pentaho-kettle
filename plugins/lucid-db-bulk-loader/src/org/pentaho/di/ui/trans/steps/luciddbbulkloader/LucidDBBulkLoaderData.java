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


package org.pentaho.di.trans.steps.luciddbbulkloader;

import java.io.OutputStream;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Stores data for the LucidDB bulk load step.
 *
 * @author Matt
 * @since 14-nov-2008
 */
public class LucidDBBulkLoaderData extends BaseStepData implements StepDataInterface {
  public Database db;

  public int[] keynrs; // nr of keylookup -value in row...
  public ValueMetaInterface[] bulkFormatMeta;

  public StreamLogger errorLogger;

  public StreamLogger outputLogger;

  public byte[] quote;
  public byte[] separator;
  public byte[] newline;

  public ValueMetaInterface bulkTimestampMeta;
  public ValueMetaInterface bulkDateMeta;
  public ValueMetaInterface bulkNumberMeta;

  public int bufferSize;

  public byte[][] rowBuffer;

  public int bufferIndex;

  public String schemaTable;

  public String fifoFilename;

  public String bcpFilename;

  public OutputStream fifoStream;

  public LucidDBBulkLoader.SqlRunner sqlRunner;

  /**
   * Default constructor.
   */
  public LucidDBBulkLoaderData() {
    super();

    db = null;
  }
}
