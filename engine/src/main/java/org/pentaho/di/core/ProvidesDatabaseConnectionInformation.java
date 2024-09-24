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

import org.pentaho.di.core.database.DatabaseMeta;

/**
 * An interface for transformation steps that connect to a database table. For example a table output step or a bulk
 * loader. This interface is used by the Agile BI plugin to determine which steps it can model or visualize.
 *
 * @author jamesdixon
 *
 */
public interface ProvidesDatabaseConnectionInformation {

  /**
   * Returns the database meta for this step
   *
   * @return
   */
  public DatabaseMeta getDatabaseMeta();

  /**
   * Returns the table name for this step
   *
   * @return
   */
  public String getTableName();

  /**
   * Returns the schema name for this step.
   *
   * @return
   */
  public String getSchemaName();

  /**
   * Provides a way for this object to return a custom message when database connection information is incomplete or
   * missing. If this returns {@code null} a default message will be displayed for missing information.
   *
   * @return A friendly message that describes that database connection information is missing and, potentially, why.
   */
  public String getMissingDatabaseConnectionInformationMessage();

}
