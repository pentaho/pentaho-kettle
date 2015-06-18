/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.database;

import org.pentaho.di.core.jdbc.ThinDriver;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Contains the wrapper for the Kettle Think JDBC driver database connection information through static final members
 *
 * @author Matt
 * @since 9-jul-2012
 */

public class KettleDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {
  @Override
  public int[] getAccessTypeList() {
    return new int[] {
      DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  @Override
  public String getDriverClass() {
    return ThinDriver.class.getName(); // always JDBC!
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) {
    return ThinDriver.BASE_URL + hostname + ":" + port + "/" + databaseName;
  }

  @Override
  public boolean supportsOptionsInURL() {
    return true;
  }

  @Override
  public String getExtraOptionIndicator() {
    return "?";
  }

  @Override
  public String getExtraOptionSeparator() {
    return "&";
  }

  /**
   * Checks whether or not the command setFetchSize() is supported by the JDBC driver...
   *
   * @return true is setFetchSize() is supported!
   */
  @Override
  public boolean isFetchSizeSupported() {
    return false;
  }

  @Override
  public boolean supportsNewLinesInSQL() {
    return false;
  }

  /**
   * @return true if the database supports bitmap indexes
   */
  @Override
  public boolean supportsBitmapIndex() {
    return false;
  }

  @Override
  public boolean supportsSchemas() {
    return false;
  }

  @Override
  public boolean supportsSynonyms() {
    return false;
  }

  @Override
  public boolean supportsCatalogs() {
    return false;
  }

  @Override
  public boolean supportsBooleanDataType() {
    return true;
  }

  @Override
  public boolean supportsViews() {
    return false;
  }

  /**
   * Most databases allow you to retrieve result metadata by preparing a SELECT statement.
   *
   * @return true if the database supports retrieval of query metadata from a prepared statement. False if the query
   *         needs to be executed first.
   */
  @Override
  public boolean supportsPreparedStatementMetadataRetrieval() {
    return true;
  }

  @Override
  public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean use_autoinc,
    boolean add_fieldname, boolean add_cr ) {
    return "// Unsupported";
  }

  @Override
  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
    String pk, boolean semicolon ) {
    return "// Unsupported";
  }

  @Override
  public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
    String pk, boolean semicolon ) {
    return "// Unsupported";
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] {};
  }

  @Override
  public boolean supportsSetMaxRows() {
    return true;
  }
}
