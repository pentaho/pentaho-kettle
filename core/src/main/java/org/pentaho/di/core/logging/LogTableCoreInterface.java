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

package org.pentaho.di.core.logging;

import java.util.List;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.repository.RepositoryAttributeInterface;

public interface LogTableCoreInterface {

  /**
   * Saves the log table to a repository.
   *
   * @param attributeInterface
   *          The attribute interface used to store the attributes
   */
  public void saveToRepository( RepositoryAttributeInterface attributeInterface ) throws KettleException;

  /**
   * Loads details of the log table from a repository.
   *
   * @param attributeInterface
   *          The attribute interface used to load the attributes
   */
  public void loadFromRepository( RepositoryAttributeInterface attributeInterface ) throws KettleException;

  public String getConnectionName();

  public void setConnectionName( String connectionName );

  public DatabaseMeta getDatabaseMeta();

  public List<LogTableField> getFields();

  /**
   * @return The locally defined log schema name
   */
  public String getSchemaName();

  /**
   * @return The locally defined log table name
   */
  public String getTableName();

  /**
   * @return The actual schema name taking into account optionally defined KETTLE variables for global logging
   *         configuration
   */
  public String getActualSchemaName();

  /**
   * @return The actual table name taking into account optionally defined KETTLE variabled for global logging
   *         configuration
   */
  public String getActualTableName();

  /**
   * Assemble the log record from the logging subject.
   *
   * @param status
   *          The status to log
   * @param subject
   *          The subject object to log
   * @param parent
   *          The parent object to log
   * @return The log record to write
   * @throws in
   *           case there is a problem with the log record creation (incorrect settings, ...)
   */
  public RowMetaAndData getLogRecord( LogStatus status, Object subject, Object parent ) throws KettleException;

  public String getLogTableType();

  public String getConnectionNameVariable();

  public String getSchemaNameVariable();

  public String getTableNameVariable();

  public boolean isDefined();

  /**
   * @return The string that describes the timeout in days (variable supported) as a floating point number
   */
  public String getTimeoutInDays();

  /**
   * @return the field that represents the log date field or null if none was defined.
   */
  public LogTableField getLogDateField();

  /**
   * @return the field that represents the key to this logging table (batch id etc)
   */
  public LogTableField getKeyField();

  /**
   * @return the appropriately quoted (by the database metadata) schema/table combination
   */
  public String getQuotedSchemaTableCombination();

  /**
   * @return the field that represents the logging text (or null if none is found)
   */
  public LogTableField getLogField();

  /**
   * @return the field that represents the status (or null if none is found)
   */
  public LogTableField getStatusField();

  /**
   * @return the field that represents the number of errors (or null if none is found)
   */
  public LogTableField getErrorsField();

  /**
   * @return the field that represents the name of the object that is being used (or null if none is found)
   */
  public LogTableField getNameField();

  /**
   * @return A list of rows that contain the recommended indexed fields for this logging table.
   */
  public List<RowMetaInterface> getRecommendedIndexes();

  /**
   * Clone the log table
   *
   * @return The cloned log table
   */
  public Object clone();

  /**
   * Replace the metadata of the logtable with the one of the specified
   *
   * @param logTableInterface
   *          the new log table details
   */
  public void replaceMeta( LogTableCoreInterface logTableInterface );
}
