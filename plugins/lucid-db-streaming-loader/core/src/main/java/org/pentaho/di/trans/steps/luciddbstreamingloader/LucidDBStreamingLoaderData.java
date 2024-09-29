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


package org.pentaho.di.trans.steps.luciddbstreamingloader;

import java.io.ObjectOutputStream;
import java.net.Socket;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.trans.step.BaseDatabaseStepData;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Stores data for the LucidDB Streaming Loader step.
 *
 * @author Ray Zhang
 * @since Jan-05-2010
 */
public class LucidDBStreamingLoaderData extends BaseDatabaseStepData implements StepDataInterface {

  public int[] keynrs; // nr of keylookup -value in row...

  public String[] format;

  public StreamLogger errorLogger;

  public StreamLogger outputLogger;

  public String schemaTable;

  public String sql_statement;

  public Socket client;

  public ObjectOutputStream objOut;

  public LucidDBStreamingLoader.SqlRunner sqlRunner;

  /**
   * Default constructor.
   */
  public LucidDBStreamingLoaderData() {
    super();

    db = null;
  }
}
