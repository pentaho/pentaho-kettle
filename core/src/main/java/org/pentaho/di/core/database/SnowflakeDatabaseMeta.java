/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.core.plugins.DatabaseMetaPlugin;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains Snowflake specific information through static final members
 *
 * @author Chris
 */
//TODO: meta from inquidia plugin - needs updates
@DatabaseMetaPlugin( type = "SNOWFLAKE", typeDescription = "Snowflake Database" )
public class SnowflakeDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {

  @Override
  public int[] getAccessTypeList() {
    return new int[]{ DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  @Override
  public int getDefaultDatabasePort() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
      return 443;
    }
    return -1;
  }

  @Override
  public String getXulOverlayFile() {
    return "snowflake";
  }

  @Override
  public Map<String, String> getDefaultOptions() {
    Map<String, String> defaultOptions = new HashMap<>();
    defaultOptions.put( getPluginId() + ".warehouse", "nowarehouse" );
    defaultOptions.put( getPluginId() + ".ssl", "on" );

    return defaultOptions;
  }

  @Override
  public boolean isFetchSizeSupported() {
    return false;
  }

  @Override
  public String getDriverClass() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC ) {
      return "sun.jdbc.odbc.JdbcOdbcDriver";
    } else {
      return "com.snowflake.client.jdbc.SnowflakeDriver";
    }
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC ) {
      return "jdbc:odbc:" + databaseName;
    } else {
      String realHostname = hostname;
      String account = hostname;
      if ( !realHostname.contains( "." ) ) {
        realHostname = hostname + ".snowflakecomputing.com";
      } else {
        account = hostname.substring( 0, hostname.indexOf( "." ) );
      }
      if ( Const.isEmpty( port ) ) {
        return "jdbc:snowflake://" + realHostname + "/?account=" + account + "&db=" + databaseName
          + "&user=" + getUsername() + "&password=" + getPassword() + "&warehouse=" + getWarehouse();
      } else {
        return "jdbc:snowflake://" + realHostname + ":" + port + "/?account=" + account + "&db=" + databaseName
          + "&user=" + getUsername() + "&password=" + getPassword() + "&warehouse=" + getWarehouse();
      }
    }
  }

  /**
   * Generates the SQL statement to add a column to the specified table
   *
   * @param tablename   The table to add
   * @param v           The column defined as a value
   * @param tk          the name of the technical key field
   * @param use_autoinc whether or not this field uses auto increment
   * @param pk          the name of the primary key field
   * @param semicolon   whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to add a column to the specified table
   */
  @Override
  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
                                       String pk, boolean semicolon ) {
    return "ALTER TABLE " + tablename + " ADD COLUMN " + getFieldDefinition( v, tk, pk, use_autoinc, true, false );
  }

  /**
   * Generates the SQL statement to drop a column from the specified table
   *
   * @param tablename   The table to add
   * @param v           The column defined as a value
   * @param tk          the name of the technical key field
   * @param use_autoinc whether or not this field uses auto increment
   * @param pk          the name of the primary key field
   * @param semicolon   whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to drop a column from the specified table
   */
  @Override
  public String getDropColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
                                        String pk, boolean semicolon ) {
    return "ALTER TABLE " + tablename + " DROP COLUMN " + v.getName() + Const.CR;
  }

  /**
   * Generates the SQL statement to modify a column in the specified table
   *
   * @param tablename   The table to add
   * @param v           The column defined as a value
   * @param tk          the name of the technical key field
   * @param use_autoinc whether or not this field uses auto increment
   * @param pk          the name of the primary key field
   * @param semicolon   whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to modify a column in the specified table
   */
  @Override
  public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk,
                                          boolean use_autoinc, String pk, boolean semicolon ) {
    return "ALTER TABLE " + tablename + " MODIFY COLUMN " + getFieldDefinition( v, tk, pk, use_autoinc, true, false );
  }

  @Override
  public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean use_autoinc,
                                    boolean add_fieldname, boolean add_cr ) {
    String retval = "";

    String fieldname = v.getName();
    int length = v.getLength();
    int precision = v.getPrecision();

    if ( add_fieldname ) {
      retval += fieldname + " ";
    }

    int type = v.getType();
    switch ( type ) {
      case ValueMetaInterface.TYPE_DATE:
        retval += "TIMESTAMP";
        break;
      case ValueMetaInterface.TYPE_BOOLEAN:
        if ( supportsBooleanDataType() ) {
          retval += "BOOLEAN";
        } else {
          retval += "CHAR(1)";
        }
        break;

      case ValueMetaInterface.TYPE_NUMBER:
      case ValueMetaInterface.TYPE_INTEGER:
      case ValueMetaInterface.TYPE_BIGNUMBER:
        if ( fieldname.equalsIgnoreCase( tk ) || // Technical key
          fieldname.equalsIgnoreCase( pk ) // Primary key
            ) {
          if ( use_autoinc ) {
            retval += "BIGINT AUTOINCREMENT NOT NULL PRIMARY KEY";
          } else {
            retval += "BIGINT NOT NULL PRIMARY KEY";
          }
        } else {
          // Integer values...
          if ( precision == 0 ) {
            if ( length > 9 ) {
              if ( length < 19 ) {
                // can hold signed values between -9223372036854775808 and 9223372036854775807
                // 18 significant digits
                retval += "BIGINT";
              } else {
                retval += "NUMBER(" + length + ", 0 )";
              }
            } else {
              retval += "INT";
            }
          } else {
            // Floating point values...
            if ( length > 15 ) {
              retval += "NUMBER(" + length + ", " + precision + ")";
            } else {
              retval += "FLOAT";
            }
          }
        }
        break;
      case ValueMetaInterface.TYPE_STRING:
        if ( length == 1 ) {
          retval += "CHAR(1)";
        } else {
          retval += "VARCHAR(" + length + ")";
        }
        break;
      case ValueMetaInterface.TYPE_BINARY:
        retval += "VARIANT";
        break;
      default:
        retval += " UNKNOWN";
        break;
    }

    if ( add_cr ) {
      retval += Const.CR;
    }

    return retval;
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[]{ "snowflake_jdbc.jar" };
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

  @SuppressWarnings( "WeakerAccess" )
  public String getSQLQueryColumnFields( String columnname, String tableName ) {
    return "SELECT " + columnname + " FROM " + tableName + " LIMIT 0";
  }

  /**
   * @see org.pentaho.di.core.database.DatabaseInterface#getNotFoundTK(boolean)
   */
  @Override
  public int getNotFoundTK( boolean use_autoinc ) {
    if ( supportsAutoInc() && use_autoinc ) {
      return 1;
    }
    return super.getNotFoundTK( use_autoinc );
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
    return new String[]{ "ALL", "ALTER", "AND", "ANY", "AS", "ASC", "BETWEEN", "BY", "CASE", "CAST", "CHECK", "CLUSTER",
      "COLUMN", "CONNECT", "CREATE", "CROSS", "CURRENT", "DELETE", "DESC", "DISTINCT", "DROP", "ELSE", "EXCLUSIVE",
      "EXISTS", "FALSE", "FOR", "FROM", "FULL", "GRANT", "GROUP", "HAVING", "IDENTIFIED", "IMMEDIATE", "IN",
      "INCREMENT", "INNER", "INSERT", "INTERSECT", "INTO", "IS", "JOIN", "LATERAL", "LEFT", "LIKE", "LOCK",
      "LONG", "MAXEXTENTS", "MINUS", "MODIFY", "NATURAL", "NOT", "NULL", "OF", "ON", "OPTION", "OR", "ORDER",
      "REGEXP", "RENAME", "REVOKE", "RIGHT", "RLIKE", "ROW", "ROWS", "SELECT", "SET", "SOME", "START", "TABLE",
      "THEN", "TO", "TRIGGER", "TRUE", "UNION", "UNIQUE", "UPDATE", "USING", "VALUES", "WHEN", "WHENEVER",
      "WHERE", "WITH" };
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.database.DatabaseInterface#getStartQuote()
   */
  @Override
  public String getStartQuote() {
    return "'";
  }

  /**
   * Simply add an underscore in the case of MySQL!
   *
   * @see org.pentaho.di.core.database.DatabaseInterface#getEndQuote()
   */
  @Override
  public String getEndQuote() {
    return "'";
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

}
