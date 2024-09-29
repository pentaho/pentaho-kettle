/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2019 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;

public class GoogleBigQueryDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {

  private static Class<?> PKG = GoogleBigQueryDatabaseMeta.class; // for i18n purposes

  @Override public int[] getAccessTypeList() {
    return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  @Override public String[] getUsedLibraries() {
    return new String[] { "google-api-client-1.22.0.jar", "google-api-services-bigquery-v2-rev355-1.22.0.jar",
      "google-http-client-1.22.0.jar", "google-http-client-jackson2-1.22.0.jar",
      "google-oauth-client-1.22.0.jar", "GoogleBigQueryJDBC42.jar", "jackson-core-2.14.2.jar"};
  }

  @Override public String getDriverClass() {
    return "com.simba.googlebigquery.jdbc42.Driver";
  }

  @Override public String getURL( String hostname, String port, String databaseName ) {
    return "jdbc:bigquery://" + hostname + ":"
      + ( StringUtil.isEmpty( port ) ? "443" : port ) + ";"
      + ( StringUtil.isEmpty( databaseName ) ? "" : "ProjectId=" + databaseName ) + ";";
  }

  @Override public String getExtraOptionsHelpText() {
    return "https://cloud.google.com/bigquery/partners/simba-drivers/";
  }

  @Override public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean useAutoinc,
                                              boolean addFieldName, boolean addCr ) {
    String retval = "";

    String fieldname = v.getName();
    int precision = v.getPrecision();

    if ( addFieldName ) {
      retval += fieldname + " ";
    }

    int type = v.getType();
    switch ( type ) {
      case ValueMetaInterface.TYPE_TIMESTAMP:
        retval += "TIMESTAMP";
        break;

      case ValueMetaInterface.TYPE_DATE:
        retval += "DATE";
        break;

      case ValueMetaInterface.TYPE_BOOLEAN:
        retval += "BOOL";
        break;

      case ValueMetaInterface.TYPE_NUMBER:
      case ValueMetaInterface.TYPE_INTEGER:
      case ValueMetaInterface.TYPE_BIGNUMBER:
        if ( precision == 0 ) {
          retval += "INT64";
        } else {
          retval += "FLOAT64";
        }
        if ( fieldname.equalsIgnoreCase( tk )
          || fieldname.equalsIgnoreCase( pk ) ) {
          retval += " NOT NULL";
        }
        break;

      case ValueMetaInterface.TYPE_STRING:
        retval += "STRING";
        break;

      case ValueMetaInterface.TYPE_BINARY:
        retval += "BYTES";
        break;

      default:
        retval += " UNKNOWN";
        break;
    }

    if ( addCr ) {
      retval += Const.CR;
    }

    return retval;
  }

  @Override public String getAddColumnStatement(
    String tablename, ValueMetaInterface v, String tk,
    boolean useAutoinc, String pk, boolean semicolon ) {
    // BigQuery does not support DDL through JDBC.
    // https://cloud.google.com/bigquery/partners/simba-drivers/#do_the_drivers_provide_the_ability_to_manage_tables_create_table
    return null;
  }

  @Override public String getModifyColumnStatement(
    String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
    String pk, boolean semicolon ) {
    // BigQuery does not support DDL through JDBC.
    // https://cloud.google.com/bigquery/partners/simba-drivers/#do_the_drivers_provide_the_ability_to_manage_tables_create_table
    return null;
  }

  @Override public String getLimitClause( int nrRows ) {
    return " LIMIT " + nrRows;
  }

  @Override public String getSQLQueryFields( String tableName ) {
    return "SELECT * FROM " + tableName + " LIMIT 0";
  }

  @Override public String getSQLTableExists( String tablename ) {
    return getSQLQueryFields( tablename );
  }

  @Override public String getSQLColumnExists( String columnname, String tablename ) {
    return getSQLQueryColumnFields( columnname, tablename );
  }

  public String getSQLQueryColumnFields( String columnname, String tableName ) {
    return "SELECT " + columnname + " FROM " + tableName + " LIMIT 0";
  }

  @Override public boolean supportsAutoInc() {
    return false;
  }

  @Override public boolean supportsAutoGeneratedKeys() {
    return false;
  }

  @Override public boolean supportsTimeStampToDateConversion() {
    return false;
  }

  @Override public boolean supportsBooleanDataType() {
    return true;
  }

  @Override public boolean isRequiringTransactionsOnQueries() {
    return false;
  }

  @Override public String getExtraOptionSeparator() {
    return ";";
  }

  @Override public String getExtraOptionIndicator() {
    return "";
  }

  @Override public String getStartQuote() {
    return "`";
  }

  @Override public String getEndQuote() {
    return "`";
  }

  @Override public boolean supportsBitmapIndex() {
    return false;
  }

  @Override public boolean supportsViews() {
    return true;
  }

  @Override public boolean supportsSynonyms() {
    return false;
  }

  @Override public String[] getReservedWords() {
    return new String[] { "ALL", "AND", "ANY", "ARRAY", "AS", "ASC", "ASSERT_ROWS_MODIFIED", "AT", "BETWEEN",
      "COLLATE", "CONTAINS", "CREATE", "CROSS", "CUBE", "CURRENT", "DEFAULT", "DEFINE", "DESC", "DISTINCT",
      "ELSE", "END", "ENUM", "ESCAPE", "EXCEPT", "EXCLUDE", "EXISTS", "EXTRACT", "FALSE", "FETCH", "FOLLOWING",
      "FOR", "FROM", "FULL", "GROUP", "GROUPING", "GROUPS", "HASH", "HAVING", "IF", "IGNORE", "IN", "INNER",
      "INTERSECT", "INTERVAL", "INTO", "IS", "JOIN", "LATERAL", "LEFT", "LIKE", "LIMIT", "LOOKUP", "MERGE",
      "NATURAL", "NEW", "NO", "NOT", "NULL", "NULLS", "OF", "ON", "OR", "ORDER", "OUTER", "OVER", "PARTITION",
      "PRECEDING", "PROTO", "RANGE", "RECURSIVE", "RESPECT", "RIGHT", "ROLLUP", "ROWS", "SELECT", "SET", "SOME",
      "STRUCT", "TABLESAMPLE", "THEN", "TO", "TREAT", "TRUE", "UNBOUNDED", "UNION", "UNNEST", "USING", "WHEN",
      "WHERE", "WINDOW", "WITH", "WITHIN", "BY", "CASE", "CAST" };
  }

  @Override public boolean supportsStandardTableOutput() {
    return false;
  }

  @Override public String getUnsupportedTableOutputMessage() {
    return BaseMessages.getString( PKG, "GoogleBigQueryDatabaseMeta.UnsupportedTableOutputMessage" );
  }
}
