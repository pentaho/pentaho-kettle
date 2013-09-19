/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.luciddbstreamingloader;

import java.io.ObjectOutputStream;
import java.net.Socket;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Stores data for the LucidDB Streaming Loader step.
 * 
 * @author Ray Zhang
 * @since Jan-05-2010
 */
public class LucidDBStreamingLoaderData extends BaseStepData implements
    StepDataInterface {
  public Database db;

  public int keynrs[]; // nr of keylookup -value in row...

  public String format[];

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
