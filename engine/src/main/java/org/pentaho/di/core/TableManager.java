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

package org.pentaho.di.core;

/**
 * An interface for transformation steps that manage loading data into a database table (or other storage system). For
 * example a table output step or a bulk loader. This interface is used by the Agile BI plugin to determine which steps
 * it can manipulate during loading of data.
 *
 * @author jamesdixon
 *
 */
public interface TableManager {

  /**
   * Sets the table name. If the name of the database table is determined programatically this method can be used to set
   * the table name.
   *
   * @param tableName
   */
  public void setTableName( String tableName );

  /**
   * Flushes the current in-memory buffer to the storage system. This is called during cancel operations when the
   * current set of data is still wanted.
   *
   * @return true if the operation succeeded
   */
  public boolean flush();

  /**
   * Drops the current table. This is used during management operations.
   *
   * @return true if the operation succeeded
   */
  public boolean dropTable();

  /**
   * Sets the row limit. This is used to limit the data loaded during a specific execution. This value might change
   * every time the transformation is executed.
   *
   * @param rowLimit
   */
  public void setRowLimit( long rowLimit );

  /**
   * Truncates the current table. This is used during management operations.
   *
   * @return true if the operation succeeded
   */
  public boolean truncateTable();

  /**
   * Adjust schema. This is used to change the schema when the input fields change.
   *
   * @return true if the operation succeeded
   */
  public boolean adjustSchema();

  /**
   * Returns a human-readable message about any errors that have occurred. If any of the operations (dropTable, flush,
   * truncateTable) fail, this method should return a nice, accurate message describing the problem that was
   * encountered.
   *
   * @return
   */
  public String getMessage();

}
