/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016-2021 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.base.Preconditions;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_BIGNUMBER;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_BINARY;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_BOOLEAN;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_DATE;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_INTEGER;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_NUMBER;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_STRING;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_TIMESTAMP;
import static org.pentaho.di.core.util.Utils.isEmpty;

@SuppressWarnings ( "unused" )
public class SnowflakeHVDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {

  private static final String ALTER_TABLE = "ALTER TABLE ";
  public static final String WAREHOUSE = "warehouse";

  @Override
  public int[] getAccessTypeList() {
    return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  @Override
  public int getDefaultDatabasePort() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
      return 443;
    }
    return -1;
  }

  @Override
  public Map<String, String> getDefaultOptions() {
    Map<String, String> defaultOptions = new HashMap<>();
    defaultOptions.put( getPluginId() + ".ssl", "on" );

    return defaultOptions;
  }

  @Override
  public boolean isFetchSizeSupported() {
    return false;
  }

  @Override
  public String getDriverClass() {
    return "com.snowflake.client.jdbc.SnowflakeDriver";
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) {
    Preconditions.checkArgument( !isEmpty( hostname ) );
    Preconditions.checkArgument( !isEmpty( databaseName ) );

    String realHostname = hostname;
    String account = hostname;
    if ( !realHostname.contains( "." ) ) {
      realHostname = hostname + ".snowflakecomputing.com";
    } else {
      account = hostname.substring( 0, hostname.indexOf( '.' ) );
    }
    // set the warehouse attribute as an "extra" option so it will be appended to the url.
    addExtraOption( getPluginId(), WAREHOUSE, getAttribute( WAREHOUSE, "" ) );
    return "jdbc:snowflake://"
      + realHostname
      + getParamIfSet( ":", port )
      + "/?account=" + account
      + "&db=" + databaseName;
  }

  private String getParamIfSet( String param, String val ) {
    if ( !isEmpty( val ) ) {
      return param + val;
    }
    return "";
  }

  /**
   * Generates the SQL statement to add a column to the specified table
   *
   * @param tablename  The table to add
   * @param v          The column defined as a value
   * @param tk         the name of the technical key field
   * @param useAutoinc whether or not this field uses auto increment
   * @param pk         the name of the primary key field
   * @param semicolon  whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to add a column to the specified table
   */
  @Override
  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
                                       String pk, boolean semicolon ) {
    return ALTER_TABLE + tablename + " ADD COLUMN " + getFieldDefinition( v, tk, pk, useAutoinc, true, false );
  }

  /**
   * Generates the SQL statement to drop a column from the specified table
   *
   * @param tablename  The table to add
   * @param v          The column defined as a value
   * @param tk         the name of the technical key field
   * @param useAutoinc whether or not this field uses auto increment
   * @param pk         the name of the primary key field
   * @param semicolon  whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to drop a column from the specified table
   */
  @Override
  public String getDropColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
                                        String pk, boolean semicolon ) {
    return ALTER_TABLE + tablename + " DROP COLUMN " + v.getName() + Const.CR;
  }

  @Override
  public String getSQLListOfSchemas( DatabaseMeta databaseMeta ) {
    String databaseName = getDatabaseName();
    if ( databaseMeta != null ) {
      databaseName = databaseMeta.environmentSubstitute( databaseName );
    }
    return "SELECT SCHEMA_NAME AS \"name\" FROM " + databaseName + ".INFORMATION_SCHEMA.SCHEMATA";
  }

  @Override
  public String getSQLListOfSchemas() {
    return getSQLListOfSchemas( null );
  }

  /**
   * Generates the SQL statement to modify a column in the specified table
   *
   * @param tableName  The table to add
   * @param v          The column defined as a value
   * @param tk         the name of the technical key field
   * @param useAutoinc whether or not this field uses auto increment
   * @param pk         the name of the primary key field
   * @param semicolon  whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to modify a column in the specified table
   */
  @Override
  public String getModifyColumnStatement( String tableName, ValueMetaInterface v, String tk,
                                          boolean useAutoinc, String pk, boolean semicolon ) {
    return ALTER_TABLE + tableName + " MODIFY COLUMN " + getFieldDefinition( v, tk, pk, useAutoinc, true, false );
  }

  @Override
  public String getFieldDefinition( ValueMetaInterface v, String surrogateKey, String primaryKey, boolean useAutoinc,
                                    boolean addFieldName, boolean addCr ) {
    String fieldDefinitionDdl = "";

    String newline = addCr ? Const.CR : "";

    String fieldname = v.getName();
    int length = v.getLength();
    int precision = v.getPrecision();
    int type = v.getType();

    boolean isKeyField = fieldname.equalsIgnoreCase( surrogateKey ) || fieldname.equalsIgnoreCase( primaryKey );

    if ( addFieldName ) {
      fieldDefinitionDdl += fieldname + " ";
    }
    if ( isKeyField ) {
      Preconditions.checkState( type == TYPE_NUMBER || type == TYPE_INTEGER || type == TYPE_BIGNUMBER );
      return ddlForPrimaryKey( useAutoinc ) + newline;
    }
    switch ( type ) {
      case TYPE_TIMESTAMP:
      case TYPE_DATE:
        // timestamp w/ local timezone
        fieldDefinitionDdl += "TIMESTAMP_LTZ";
        break;
      case TYPE_BOOLEAN:
        fieldDefinitionDdl += ddlForBooleanValue();
        break;
      case TYPE_NUMBER:
      case TYPE_INTEGER:
      case TYPE_BIGNUMBER:
        if ( precision == 0 ) {
          fieldDefinitionDdl += ddlForIntegerValue( length );
        } else {
          fieldDefinitionDdl += ddlForFloatValue( length, precision );
        }
        break;
      case TYPE_STRING:
        if ( length <= 0 ) {
          fieldDefinitionDdl += "VARCHAR";
        } else {
          fieldDefinitionDdl += "VARCHAR(" + length + ")";
        }
        break;
      case TYPE_BINARY:
        fieldDefinitionDdl += "VARIANT";
        break;
      default:
        fieldDefinitionDdl += " UNKNOWN";
        break;
    }
    return fieldDefinitionDdl + newline;
  }

  private String ddlForBooleanValue() {
    if ( supportsBooleanDataType() ) {
      return "BOOLEAN";
    } else {
      return "CHAR(1)";
    }
  }

  private String ddlForIntegerValue( int length ) {
    if ( length > 9 ) {
      if ( length < 19 ) {
        // can hold signed values between -9223372036854775808 and 9223372036854775807
        // 18 significant digits
        return "BIGINT";
      } else {
        return "NUMBER(" + length + ", 0 )";
      }
    } else {
      return "INT";
    }
  }

  private String ddlForFloatValue( int length, int precision ) {
    if ( length > 15 ) {
      return "NUMBER(" + length + ", " + precision + ")";
    } else {
      return "FLOAT";
    }
  }

  private String ddlForPrimaryKey( boolean useAutoinc ) {
    if ( useAutoinc ) {
      return "BIGINT AUTOINCREMENT NOT NULL PRIMARY KEY";
    } else {
      return "BIGINT NOT NULL PRIMARY KEY";
    }
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] { "snowflake_jdbc.jar" };
  }

  @Override
  public String getLimitClause( int nrRows ) {
    return " LIMIT " + nrRows;
  }

  /**
   * Returns the minimal SQL to launch in order to determine the layout of the resultset for a given database table
   *
   * @param tableName The name of the table to determine the layout for
   * @return The SQL to launch.
   */
  @Override
  public String getSQLQueryFields( String tableName ) {
    return "SELECT * FROM " + tableName + " LIMIT 0";
  }

  @Override
  public String getSQLTableExists( String tablename ) {
    return getSQLQueryFields( tablename );
  }

  @Override
  public String getSQLColumnExists( String columnname, String tablename ) {
    return getSQLQueryColumnFields( columnname, tablename );
  }

  public String getSQLQueryColumnFields( String columnname, String tableName ) {
    return "SELECT " + columnname + " FROM " + tableName + " LIMIT 0";
  }

  /**
   * @see org.pentaho.di.core.database.DatabaseInterface#getNotFoundTK(boolean)
   */
  @Override
  public int getNotFoundTK( boolean useAutoinc ) {
    if ( supportsAutoInc() && useAutoinc ) {
      return 1;
    }
    return super.getNotFoundTK( useAutoinc );
  }

  /**
   * @return The extra option separator in database URL for this platform (usually this is semicolon ; )
   */
  @Override
  public String getExtraOptionSeparator() {
    return "&";
  }

  /**
   * @return This indicator separates the normal URL from the options
   */
  @Override
  public String getExtraOptionIndicator() {
    return "&";
  }

  /**
   * @return true if the database supports transactions.
   */
  @Override
  public boolean supportsTransactions() {
    return false;
  }


  @Override
  public boolean supportsTimeStampToDateConversion() {
    return false; // The 3.6.9 driver _does_ support conversion, but errors when value is null.
  }

  /**
   * @return true if the database supports bitmap indexes
   */
  @Override
  public boolean supportsBitmapIndex() {
    return false;
  }

  /**
   * @return true if the database supports views
   */
  @Override
  public boolean supportsViews() {
    return true;
  }

  /**
   * @return true if the database supports synonyms
   */
  @Override
  public boolean supportsSynonyms() {
    return false;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.database.DatabaseInterface#getReservedWords()
   */
  @Override
  public String[] getReservedWords() {
    return new String[] { "ALL", "ALTER", "AND", "ANY", "AS", "ASC", "BETWEEN", "BY", "CASE", "CAST", "CHECK",
      "CLUSTER",
      "COLUMN", "CONNECT", "CREATE", "CROSS", "CURRENT", "DELETE", "DESC", "DISTINCT", "DROP", "ELSE", "EXCLUSIVE",
      "EXISTS", "FALSE", "FOR", "FROM", "FULL", "GRANT", "GROUP", "HAVING", "IDENTIFIED", "IMMEDIATE", "IN",
      "INCREMENT", "INNER", "INSERT", "INTERSECT", "INTO", "IS", "JOIN", "LATERAL", "LEFT", "LIKE", "LOCK",
      "LONG", "MAXEXTENTS", "MINUS", "MODIFY", "NATURAL", "NOT", "NULL", "OF", "ON", "OPTION", "OR", "ORDER",
      "REGEXP", "RENAME", "REVOKE", "RIGHT", "RLIKE", "ROW", "ROWS", "SELECT", "SET", "SOME", "START", "TABLE",
      "THEN", "TO", "TRIGGER", "TRUE", "UNION", "UNIQUE", "UPDATE", "USING", "VALUES", "WHEN", "WHENEVER",
      "WHERE", "WITH" };
  }


  /**
   * @return extra help text on the supported options on the selected database platform.
   */
  @Override
  public String getExtraOptionsHelpText() {
    return "https://docs.snowflake.net/manuals/user-guide/jdbc-configure.html";
  }

  /**
   * Get the SQL to insert a new empty unknown record in a dimension.
   *
   * @param schemaTable  the schema-table name to insert into
   * @param keyField     The key field
   * @param versionField the version field
   * @return the SQL to insert the unknown record into the SCD.
   */
  @Override
  public String getSQLInsertAutoIncUnknownDimensionRow( String schemaTable, String keyField,
                                                        String versionField ) {
    return "insert into " + schemaTable + "(" + keyField + ", " + versionField + ") values (1, 1)";
  }

  @Override
  public boolean supportsIndexes() {
    // https://www.snowflake.com/blog/automatic-query-optimization-no-tuning/
    return false;
  }

  /**
   * @param string The String to escape
   * @return A string that is properly quoted for use in a SQL statement (insert, update, delete, etc)
   */
  @Override
  public String quoteSQLString( String string ) {
    string = string.replaceAll( "'", "\\\\'" );
    string = string.replaceAll( "\\n", "\\\\n" );
    string = string.replaceAll( "\\r", "\\\\r" );
    return "'" + string + "'";
  }

  @Override
  public boolean releaseSavepoint() {
    return false;
  }

  @Override
  public boolean supportsErrorHandlingOnBatchUpdates() {
    return true;
  }

  @Override
  public boolean isRequiringTransactionsOnQueries() {
    return false;
  }

  /**
   * @return true if Kettle can create a repository on this type of database.
   */
  @Override
  public boolean supportsRepository() {
    return false;
  }

  @Override public void putOptionalOptions( Map<String, String> extraOptions ) {
    extraOptions.put( "SNOWFLAKEHV." + WAREHOUSE, getAttribute( WAREHOUSE, "" ) );
  }

  @Override public ResultSet getSchemas( DatabaseMetaData databaseMetaData, DatabaseMeta dbMeta )
    throws SQLException {
    return databaseMetaData.getSchemas( dbMeta.environmentSubstitute( dbMeta.getDatabaseName() ), null );
  }

  @Override public ResultSet getTables( DatabaseMetaData databaseMetaData, DatabaseMeta dbMeta, String schemaPattern,
                                        String tableNamePattern, String[] tableTypes ) throws SQLException {
    return databaseMetaData.getTables( dbMeta.environmentSubstitute( dbMeta.getDatabaseName() ), schemaPattern, tableNamePattern, tableTypes );
  }
}
