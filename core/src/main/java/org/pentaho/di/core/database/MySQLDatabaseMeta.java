/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.collect.Sets;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;

import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Contains MySQL specific information through static final members
 *
 * @author Matt
 * @since 11-mrt-2005
 */

public class MySQLDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {
  private static final Class<?> PKG = MySQLDatabaseMeta.class;

  private static final int VARCHAR_LIMIT = 65_535;

  private static String driverClass = "";

  private static final Set<String>
    shortMessageExceptions =
    Sets.newHashSet( "com.mysql.jdbc.PacketTooBigException", "com.mysql.jdbc.MysqlDataTruncation",
      "com.mysql.cj.jdbc.exceptions.PacketTooBigException",
      "com.mysql.cj.jdbc.exceptions.MysqlDataTruncation" );

  public MySQLDatabaseMeta() {
    determineDriverClass();
  }

  @Override public int[] getAccessTypeList() {
    return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  @Override public int getDefaultDatabasePort() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
      return 3306;
    }
    return -1;
  }

  @Override public String getLimitClause( int nrRows ) {
    return " LIMIT " + nrRows;
  }

  /**
   * Returns the minimal SQL to launch in order to determine the layout of the resultset for a given database table
   *
   * @param tableName
   *   The name of the table to determine the layout for
   * @return The SQL to launch.
   */
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

  /**
   * @see org.pentaho.di.core.database.DatabaseInterface#getNotFoundTK(boolean)
   */
  @Override public int getNotFoundTK( boolean useAutoinc ) {
    if ( supportsAutoInc() && useAutoinc ) {
      return 1;
    }
    return super.getNotFoundTK( useAutoinc );
  }

  /*
  * Fix for BACKLOG-33475 Upgrade bulkload support to include MySQL 8.0
  * Change necessary since the jdbc driver for MySQL 5.7 and 8.0 package name changed from
  * org.gjt.mm.mysql.Driver to com.mysql.cj.jdbc.Driver
  * */
  @Override public String getDriverClass() {
    return determineDriverClass();
  }

  private static String determineDriverClass() {
    if ( driverClass.isEmpty() ) {
      try {
        driverClass = "com.mysql.cj.jdbc.Driver";
        Class.forName( driverClass );
      } catch ( ClassNotFoundException e ) {
        driverClass = "com.mysql.jdbc.Driver";
      }
    }
    return driverClass;
  }

  @Override public String getURL( String hostname, String port, String databaseName ) {
    if ( Utils.isEmpty( port ) ) {
      return "jdbc:mysql://" + hostname + "/" + databaseName;
    } else {
      return "jdbc:mysql://" + hostname + ":" + port + "/" + databaseName;
    }
  }

  /**
   * @return The extra option separator in database URL for this platform (usually this is semicolon ; )
   */
  @Override public String getExtraOptionSeparator() {
    return "&";
  }

  /**
   * @return This indicator separates the normal URL from the options
   */
  @Override public String getExtraOptionIndicator() {
    return "?";
  }

  /**
   * @return true if the database supports transactions.
   */
  @Override public boolean supportsTransactions() {
    return false;
  }

  /**
   * @return true if the database supports bitmap indexes
   */
  @Override public boolean supportsBitmapIndex() {
    return false;
  }

  /**
   * @return true if the database supports views
   */
  @Override public boolean supportsViews() {
    return true;
  }

  /**
   * @return true if the database supports synonyms
   */
  @Override public boolean supportsSynonyms() {
    return false;
  }

  /**
   * Generates the SQL statement to add a column to the specified table
   *
   * @param tablename
   *   The table to add
   * @param v
   *   The column defined as a value
   * @param tk
   *   the name of the technical key field
   * @param useAutoinc
   *   whether or not this field uses auto increment
   * @param pk
   *   the name of the primary key field
   * @param semicolon
   *   whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to add a column to the specified table
   */
  @Override public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
    String pk, boolean semicolon ) {
    return "ALTER TABLE " + tablename + " ADD " + getFieldDefinition( v, tk, pk, useAutoinc, true, false );
  }

  /**
   * Generates the SQL statement to modify a column in the specified table
   *
   * @param tablename
   *   The table to add
   * @param v
   *   The column defined as a value
   * @param tk
   *   the name of the technical key field
   * @param useAutoinc
   *   whether or not this field uses auto increment
   * @param pk
   *   the name of the primary key field
   * @param semicolon
   *   whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to modify a column in the specified table
   */
  @Override public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk,
                                                    boolean useAutoinc, String pk, boolean semicolon ) {
    return "ALTER TABLE " + tablename + " MODIFY " + getFieldDefinition( v, tk, pk, useAutoinc, true, false );
  }

  @Override public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean useAutoinc,
                                              boolean addFieldName, boolean addCr ) {
    String retval = "";

    String fieldname = v.getName();
    if ( v.getLength() == DatabaseMeta.CLOB_LENGTH ) {
      v.setLength( getMaxTextFieldLength() );
    }
    int length = v.getLength();
    int precision = v.getPrecision();

    if ( addFieldName ) {
      retval += fieldname + " ";
    }

    int type = v.getType();
    switch ( type ) {
      case ValueMetaInterface.TYPE_TIMESTAMP:
      case ValueMetaInterface.TYPE_DATE:
        retval += "DATETIME";
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
          if ( useAutoinc ) {
            retval += "BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY";
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
                retval += "DECIMAL(" + length + ")";
              }
            } else {
              retval += "INT";
            }
          } else {
            // Floating point values...
            if ( length > 15 ) {
              retval += "DECIMAL(" + length;
              if ( precision > 0 ) {
                retval += ", " + precision;
              }
              retval += ")";
            } else {
              // A double-precision floating-point number is accurate to approximately 15 decimal places.
              // http://mysql.mirrors-r-us.net/doc/refman/5.1/en/numeric-type-overview.html
              retval += "DOUBLE";
            }
          }
        }
        break;
      case ValueMetaInterface.TYPE_STRING:
        if ( length > 0 ) {
          if ( length == 1 ) {
            retval += "CHAR(1)";
          } else if ( length < 256 ) {
            retval += "VARCHAR(" + length + ")";
          } else if ( length < 65536 ) {
            retval += "TEXT";
          } else if ( length < 16777216 ) {
            retval += "MEDIUMTEXT";
          } else {
            retval += "LONGTEXT";
          }
        } else {
          retval += "TINYTEXT";
        }
        break;
      case ValueMetaInterface.TYPE_BINARY:
        retval += "LONGBLOB";
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

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.database.DatabaseInterface#getReservedWords()
   */
  @Override public String[] getReservedWords() {
    return new String[] { "ADD", "ALL", "ALTER", "ANALYZE", "AND", "AS", "ASC", "ASENSITIVE", "BEFORE", "BETWEEN",
      "BIGINT", "BINARY", "BLOB", "BOTH", "BY", "CALL", "CASCADE", "CASE", "CHANGE", "CHAR", "CHARACTER", "CHECK",
      "COLLATE", "COLUMN", "CONDITION", "CONNECTION", "CONSTRAINT", "CONTINUE", "CONVERT", "CREATE", "CROSS",
      "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "DATABASE", "DATABASES",
      "DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE", "DAY_SECOND", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DELAYED",
      "DELETE", "DESC", "DESCRIBE", "DETERMINISTIC", "DISTINCT", "DISTINCTROW", "DIV", "DOUBLE", "DROP", "DUAL", "EACH",
      "ELSE", "ELSEIF", "ENCLOSED", "ESCAPED", "EXISTS", "EXIT", "EXPLAIN", "FALSE", "FETCH", "FLOAT", "FOR", "FORCE",
      "FOREIGN", "FROM", "FULLTEXT", "GOTO", "GRANT", "GROUP", "HAVING", "HIGH_PRIORITY", "HOUR_MICROSECOND",
      "HOUR_MINUTE", "HOUR_SECOND", "IF", "IGNORE", "IN", "INDEX", "INFILE", "INNER", "INOUT", "INSENSITIVE", "INSERT",
      "INT", "INTEGER", "INTERVAL", "INTO", "IS", "ITERATE", "JOIN", "KEY", "KEYS", "KILL", "LEADING", "LEAVE", "LEFT",
      "LIKE", "LIMIT", "LINES", "LOAD", "LOCALTIME", "LOCALTIMESTAMP", "LOCATE", "LOCK", "LONG", "LONGBLOB", "LONGTEXT",
      "LOOP", "LOW_PRIORITY", "MATCH", "MEDIUMBLOB", "MEDIUMINT", "MEDIUMTEXT", "MIDDLEINT", "MINUTE_MICROSECOND",
      "MINUTE_SECOND", "MOD", "MODIFIES", "NATURAL", "NOT", "NO_WRITE_TO_BINLOG", "NULL", "NUMERIC", "ON", "OPTIMIZE",
      "OPTION", "OPTIONALLY", "OR", "ORDER", "OUT", "OUTER", "OUTFILE", "POSITION", "PRECISION", "PRIMARY", "PROCEDURE",
      "PURGE", "READ", "READS", "REAL", "REFERENCES", "REGEXP", "RENAME", "REPEAT", "REPLACE", "REQUIRE", "RESTRICT",
      "RETURN", "REVOKE", "RIGHT", "RLIKE", "SCHEMA", "SCHEMAS", "SECOND_MICROSECOND", "SELECT", "SENSITIVE",
      "SEPARATOR", "SET", "SHOW", "SMALLINT", "SONAME", "SPATIAL", "SPECIFIC", "SQL", "SQLEXCEPTION", "SQLSTATE",
      "SQLWARNING", "SQL_BIG_RESULT", "SQL_CALC_FOUND_ROWS", "SQL_SMALL_RESULT", "SSL", "STARTING", "STRAIGHT_JOIN",
      "TABLE", "TERMINATED", "THEN", "TINYBLOB", "TINYINT", "TINYTEXT", "TO", "TRAILING", "TRIGGER", "TRUE", "UNDO",
      "UNION", "UNIQUE", "UNLOCK", "UNSIGNED", "UPDATE", "USAGE", "USE", "USING", "UTC_DATE", "UTC_TIME",
      "UTC_TIMESTAMP", "VALUES", "VARBINARY", "VARCHAR", "VARCHARACTER", "VARYING", "WHEN", "WHERE", "WHILE", "WITH",
      "WRITE", "XOR", "YEAR_MONTH", "ZEROFILL" };
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.database.DatabaseInterface#getStartQuote()
   */
  @Override public String getStartQuote() {
    return "`";
  }

  /**
   * Simply add an underscore in the case of MySQL!
   *
   * @see org.pentaho.di.core.database.DatabaseInterface#getEndQuote()
   */
  @Override public String getEndQuote() {
    return "`";
  }

  /**
   * @param tableNames
   *   The names of the tables to lock
   * @return The SQL command to lock database tables for write purposes.
   */
  @Override public String getSQLLockTables( String[] tableNames ) {
    String sql = "LOCK TABLES ";
    for ( int i = 0; i < tableNames.length; i++ ) {
      if ( i > 0 ) {
        sql += ", ";
      }
      sql += tableNames[i] + " WRITE";
    }
    sql += ";" + Const.CR;

    return sql;
  }

  /**
   * @param tableName
   *   The name of the table to unlock
   * @return The SQL command to unlock a database table.
   */
  @Override public String getSQLUnlockTables( String[] tableName ) {
    return "UNLOCK TABLES"; // This unlocks all tables
  }

  @Override public boolean needsToLockAllTables() {
    return true;
  }

  /**
   * @return extra help text on the supported options on the selected database platform.
   */
  @Override public String getExtraOptionsHelpText() {
    return "https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-configuration-properties.html";
  }

  @Override public String[] getUsedLibraries() {
    return new String[] { "mysql-connector-java-3.1.14-bin.jar" };
  }

  /**
   * @param tableName
   * @return true if the specified table is a system table
   */
  @Override public boolean isSystemTable( String tableName ) {
    if ( tableName.startsWith( "sys" ) ) {
      return true;
    }
    if ( tableName.equals( "dtproperties" ) ) {
      return true;
    }
    return false;
  }

  /**
   * Get the SQL to insert a new empty unknown record in a dimension.
   *
   * @param schemaTable
   *   the schema-table name to insert into
   * @param keyField
   *   The key field
   * @param versionField
   *   the version field
   * @return the SQL to insert the unknown record into the SCD.
   */
  @Override public String getSQLInsertAutoIncUnknownDimensionRow( String schemaTable, String keyField,
    String versionField ) {
    return "insert into " + schemaTable + "(" + keyField + ", " + versionField + ") values (1, 1)";
  }

  /**
   * @param string
   * @return A string that is properly quoted for use in a SQL statement (insert, update, delete, etc)
   */
  @Override public String quoteSQLString( String string ) {
    string = string.replaceAll( "'", "\\\\'" );
    string = string.replaceAll( "\\n", "\\\\n" );
    string = string.replaceAll( "\\r", "\\\\r" );
    return "'" + string + "'";
  }

  /**
   * @return true if the database is a MySQL variant, like MySQL 5.1, InfiniDB, InfoBright, and so on.
   */
  @Override public boolean isMySQLVariant() {
    return true;
  }

  /**
   * Returns a false as Oracle does not allow for the releasing of savepoints.
   */
  @Override public boolean releaseSavepoint() {
    return false;
  }

  @Override public boolean supportsErrorHandlingOnBatchUpdates() {
    return true;
  }

  @Override public boolean isRequiringTransactionsOnQueries() {
    return false;
  }

  /**
   * @return true if Kettle can create a repository on this type of database.
   */
  @Override public boolean supportsRepository() {
    return true;
  }

  @Override public boolean fullExceptionLog( Exception e ) {
    Throwable cause = ( e == null ? null : e.getCause() );
    return !( cause != null && shortMessageExceptions.contains( cause.getClass().getName() ) );
  }

  @Override
  public void addDefaultOptions() {
    //These options don't appear to get into the UI since it creates the DatabaseMeta with a hard coded Oracle type.
    // Since it is called from the constuctor, I'm leaving it in, in case some other placed creates it but this
    //appears to be implemented code that serves no purpose anymore.  See getDefaultOptions below.
    addExtraOption( getPluginId(), "defaultFetchSize", "500" );
    addExtraOption( getPluginId(), "useCursorFetch", "true" );
  }

  @Override
  public Map<String, String> getDefaultOptions() {
    Map<String, String> defaultOptions = new HashMap<>();
    defaultOptions.put( getPluginId() + ".defaultFetchSize", "500" );
    defaultOptions.put( getPluginId() + ".useCursorFetch", "true" );

    return defaultOptions;
  }

  @Override
  public int getMaxVARCHARLength() {
    return VARCHAR_LIMIT;
  }

  @Override
  public int getMaxTextFieldLength() {
    return Integer.MAX_VALUE;
  }

  /**
   * Returns the column name for a MySQL field checking if the driver major version is "greater than" or "lower or equal" to 3.
   *
   * @param dbMetaData
   * @param rsMetaData
   * @param index
   * @return The column label if version is greater than 3 or the column name if version is lower or equal to 3.
   * @throws KettleDatabaseException
   */
  public String getLegacyColumnName( DatabaseMetaData dbMetaData, ResultSetMetaData rsMetaData, int index ) throws KettleDatabaseException {
    if ( dbMetaData == null ) {
      throw new KettleDatabaseException( BaseMessages.getString( PKG, "MySQLDatabaseMeta.Exception.LegacyColumnNameNoDBMetaDataException" ) );
    }

    if ( rsMetaData == null ) {
      throw new KettleDatabaseException( BaseMessages.getString( PKG, "MySQLDatabaseMeta.Exception.LegacyColumnNameNoRSMetaDataException" ) );
    }

    try {
      return dbMetaData.getDriverMajorVersion() > 3 ? rsMetaData.getColumnLabel( index ) : rsMetaData.getColumnName( index );
    } catch ( Exception e ) {
      throw new KettleDatabaseException( String.format( "%s: %s", BaseMessages.getString( PKG, "MySQLDatabaseMeta.Exception.LegacyColumnNameException" ), e.getMessage() ), e );
    }
  }
}
