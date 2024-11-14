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
