/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

/**
 * Contains Netezza specific information through static final members
 *
 * @author Biswapesh
 * @since 16-oct-2006
 */

public class NetezzaDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {
  public static final int MAX_CHAR_LEN = 32767;

  /**
   * @return The extra option separator in database URL for this platform
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
    return "?";
  }

  @Override
  public int[] getAccessTypeList() {
    return new int[] {
      DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  @Override
  public int getDefaultDatabasePort() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
      return 5480;
    }
    return -1;
  }

  @Override
  public String getDriverClass() {
    return "org.netezza.Driver";
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) {
    return "jdbc:netezza://" + hostname + ":" + port + "/" + databaseName;
  }

  /**
   * Checks whether or not the command setFetchSize() is supported by the JDBC driver...
   *
   * @return true is setFetchSize() is supported!
   */
  @Override
  public boolean isFetchSizeSupported() {
    return true;
  }

  /**
   * @return true if the database supports bitmap indexes
   */
  @Override
  public boolean supportsBitmapIndex() {
    return false;
  }

  /**
   * @return true if the database supports synonyms
   */
  @Override
  public boolean supportsSynonyms() {
    return false;
  }

  @Override
  public boolean supportsSequences() {
    return true;
  }

  /**
   * @return true if auto incremment is supported
   */
  @Override
  public boolean supportsAutoInc() {
    return false;
  }

  @Override
  public String getLimitClause( int nrRows ) {
    return " limit " + nrRows;
  }

  @Override
  public String getSQLQueryFields( String tableName ) {
    return "SELECT * FROM " + tableName + getLimitClause( 1 );
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
    return "SELECT " + columnname + " FROM " + tableName + getLimitClause( 1 );
  }

  /**
   * Get the SQL to get the next value of a sequence. (Netezza version)
   *
   * @param sequenceName
   *          The sequence name
   * @return the SQL to get the next value of a sequence.
   */
  @Override
  public String getSQLNextSequenceValue( String sequenceName ) {
    return "select next value for " + sequenceName;
  }

  /**
   * Get the SQL to get the current value of a sequence. (Netezza version)
   *
   * @param sequenceName
   *          The sequence name
   * @return the SQL to get the current value of a sequence.
   */
  @Override
  public String getSQLCurrentSequenceValue( String sequenceName ) {
    return "select last_value from " + sequenceName;
  }

  /**
   * Check if a sequence exists.
   *
   * @param sequenceName
   *          The sequence to check
   * @return The SQL to get the name of the sequence back from the databases data dictionary
   */
  @Override
  public String getSQLSequenceExists( String sequenceName ) {
    return "SELECT seqname AS sequence_name from _v_sequence where seqname = '" + sequenceName.toLowerCase() + "'";
  }

  @Override
  public String getSQLListOfSequences() {
    return "SELECT seqname AS sequence_name from _v_sequence";
  }

  /**
   * Generates the SQL statement to add a column to the specified table Note: Netezza does not allow adding columns to
   * tables
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param useAutoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to add a column to the specified table
   */
  @Override
  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
    String pk, boolean semicolon ) {
    return null;
  }

  /**
   * Generates the SQL statement to drop a column from the specified table Note: Netezza does not allow
   * addition/deletion of columns to tables
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param useAutoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to drop a column from the specified table
   */
  @Override
  public String getDropColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
    String pk, boolean semicolon ) {
    return null;
  }

  /**
   * Generates the SQL statement to modify a column in the specified table. Note: Support for this in Netezza is
   * incomplete since Netezza allows very limited table/column modifications post-creation
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param useAutoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to modify a column in the specified table
   */
  @Override
  public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
    String pk, boolean semicolon ) {
    String retval = "";
    retval += "ALTER TABLE " + tablename + " MODIFY COLUMN " + v.getName() + Const.CR + ";" + Const.CR;
    return retval;
  }

  @Override
  public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean useAutoinc,
                                    boolean addFieldName, boolean addCr ) {
    String retval = "";

    String fieldname = v.getName();
    int length = v.getLength();
    int precision = v.getPrecision();

    if ( addFieldName ) {
      retval += fieldname + " ";
    }

    int type = v.getType();
    switch ( type ) {
      case ValueMetaInterface.TYPE_DATE:
        retval += "date";
        break;
      case ValueMetaInterface.TYPE_BOOLEAN:
        retval += "boolean";
        break;
      case ValueMetaInterface.TYPE_NUMBER:
      case ValueMetaInterface.TYPE_INTEGER:
      case ValueMetaInterface.TYPE_BIGNUMBER:
        if ( length > 0 ) {
          if ( precision == 0 ) {
            if ( length <= 2 ) {
              retval += "byteint";
            } else if ( length <= 4 ) {
              retval += "smallint";
            } else if ( length <= 9 ) {
              retval += "integer";
            } else {
              retval += "bigint";
            }
          } else {
            if ( length < 9 ) {
              retval += "real";
            } else if ( length < 18 ) {
              retval += "double";
            } else {
              retval += "numeric(" + length;
              if ( precision > 0 ) {
                retval += ", " + precision;
              }
              retval += ")";
            }
          }
        }
        break;
      case ValueMetaInterface.TYPE_STRING:
        if ( length > MAX_CHAR_LEN ) {
          retval += "varchar(" + MAX_CHAR_LEN + ")";
        } else {
          retval += "varchar(" + length + ")";
        }
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

  /**
   * @param the
   *          schema name to search in or null if you want to search the whole DB
   * @return The SQL on this database to get a list of stored procedures.
   */
  public String getSQLListOfProcedures( String schemaName ) {
    return null; // Netezza does not support database procedures
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.database.DatabaseInterface#getReservedWords()
   */
  @Override
  public String[] getReservedWords() {
    return new String[] {
      // As per the user manual
      "ABORT", "ADMIN", "AGGREGATE", "ALIGN", "ALL", "ALLOCATE", "ANALYSE", "ANALYZE", "AND", "ANY", "AS",
      "ASC", "BETWEEN", "BINARY", "BIT", "BOTH", "CASE", "CAST", "CHAR", "CHARACTER", "CHECK", "CLUSTER",
      "COALESCE", "COLLATE", "COLLATION", "COLUMN", "CONSTRAINT", "COPY", "CROSS", "CURRENT", "CURRENT_CATALOG",
      "CURRENT_DATE", "CURRENT_DB", "CURRENT_SCHEMA", "CURRENT_SID", "CURRENT_TIME", "CURRENT_TIMESTAMP",
      "CURRENT_USER", "CURRENT_USERID", "CURRENT_USEROID", "DEALLOCATE", "DEC", "DECIMAL", "DECODE", "DEFAULT",
      "DEFERRABLE", "DESC", "DISTINCT", "DISTRIBUTE", "DO", "ELSE", "END", "EXCEPT", "EXCLUDE", "EXISTS",
      "EXPLAIN", "EXPRESS", "EXTEND", "EXTRACT", "FALSE", "FIRST", "FLOAT", "FOLLOWING", "FOR", "FOREIGN",
      "FROM", "FULL", "FUNCTION", "GENSTATS", "GLOBAL", "GROUP", "HAVING", "ILIKE", "IN", "INDEX", "INITIALLY",
      "INNER", "INOUT", "INTERSECT", "INTERVAL", "INTO", "IS", "ISNULL", "JOIN", "LAST", "LEADING", "LEFT",
      "LIKE", "LIMIT", "LISTEN", "LOAD", "LOCAL", "LOCK", "MATERIALIZED", "MINUS", "MOVE", "NATURAL", "NCHAR",
      "NEW", "NOT", "NOTNULL", "NULL", "NULLIF", "NULLS", "NUMERIC", "NVL", "NVL2", "OFF", "OFFSET", "OLD",
      "ON", "ONLINE", "ONLY", "OR", "ORDER", "OTHERS", "OUT", "OUTER", "OVER", "OVERLAPS", "PARTITION",
      "POSITION", "PRECEDING", "PRECISION", "PRESERVE", "PRIMARY", "PUBLIC", "RANGE", "RECLAIM", "REFERENCES",
      "RESET", "REUSE", "RIGHT", "ROWS", "ROWSETLIMIT", "RULE", "SEARCH", "SELECT", "SEQUENCE", "SESSION_USER",
      "SETOF", "SHOW", "SOME", "SUBSTRING", "SYSTEM", "TABLE", "THEN", "TIES", "TIME", "TIMESTAMP", "TO",
      "TRAILING", "TRANSACTION", "TRIGGER", "TRIM", "TRUE", "UNBOUNDED", "UNION", "UNIQUE", "USER", "USING",
      "VACUUM", "VARCHAR", "VERBOSE", "VIEW", "WHEN", "WHERE", "WITH", "WRITE", "ABSOLUTE", "ACTION", "ADD",
      "ADMIN", "AFTER", "AGGREGATE", "ALIAS", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "ARRAY", "AS",
      "ASC", "ASSERTION", "AT", "AUTHORIZATION", "BEFORE", "BEGIN", "BINARY", "BIT", "BLOB", "BOOLEAN", "BOTH",
      "BREADTH", "BY", "CALL", "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CHAR", "CHARACTER", "CHECK",
      "CLASS", "CLOB", "CLOSE", "COLLATE", "COLLATION", "COLUMN", "COMMIT", "COMPLETION", "CONNECT",
      "CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONSTRUCTOR", "CONTINUE", "CORRESPONDING", "CREATE", "CROSS",
      "CUBE", "CURRENT", "CURRENT_DATE", "CURRENT_PATH", "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_",
      "TIMESTAMP", "CURRENT_USER", "CURSOR", "CYCLE", "DATA", "DATE", "DAY", "DEALLOCATE", "DEC", "DECIMAL",
      "DECLARE", "DEFAULT", "DEFERRABLE", "DEFERRED", "DELETE", "DEPTH", "DEREF", "DESC", "DESCRIBE",
      "DESCRIPTOR", "DESTROY", "DESTRUCTOR", "DETERMINISTIC", "DIAGNOSTICS", "DICTIONARY", "DISCONNECT",
      "DISTINCT", "DOMAIN", "DOUBLE", "DROP", "DYNAMIC", "EACH", "ELSE", "END_EXEC", "END", "EQUALS", "ESCAPE",
      "EVERY", "EXCEPT", "EXCEPTION", "EXEC", "EXECUTE", "EXTERNAL", "FALSE", "FETCH", "FIRST", "FLOAT", "FOR",
      "FOREIGN", "FOUND", "FREE", "FROM", "FULL", "FUNCTION", "GENERAL", "GET", "GLOBAL", "GO", "GOTO", "GRANT",
      "GROUP", "GROUPING", "HAVING", "HOST", "HOUR", "IDENTITY", "IGNORE", "IMMEDIATE", "IN", "INDICATOR",
      "INITIALIZE", "INITIALLY", "INNER", "INOUT", "INPUT", "INSERT", "INT", "INTEGER", "INTERSECT", "INTERVAL",
      "INTO", "IS", "ISOLATION", "ITERATE", "JOIN", "KEY", "LANGUAGE", "LARGE", "LAST", "LATERAL", "LEADING",
      "LEFT", "LESS", "LEVEL", "LIKE", "LIMIT", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LOCATOR", "MAP",
      "MATCH", "MINUTE", "MODIFIES", "MODIFY", "MODULE", "MONTH", "NAMES", "NATIONAL", "NATURAL", "NCHAR",
      "NCLOB", "NEW", "NEXT", "NO", "NONE", "NOT", "NULL", "NUMERIC", "OBJECT", "OF", "OFF", "OLD", "ON",
      "ONLY", "OPEN", "OPERATION", "OPTION", "OR", "ORDER", "ORDINALITY", "OUT", "OUTER", "OUTPUT", "PAD",
      "PARAMETER", "PARAMETERS", "PARTIAL", "PATH", "POSTFIX", "PRECISION", "PREFIX", "PREORDER", "PREPARE",
      "PRESERVE", "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURE", "PUBLIC", "READ", "READS", "REAL", "RECURSIVE",
      "REF", "REFERENCES", "REFERENCING", "RELATIVE", "RESTRICT", "RESULT", "RETURN", "RETURNS", "REVOKE",
      "RIGHT", "ROLE", "ROLLBACK", "ROLLUP", "ROUTINE", "ROW", "ROWS", "SAVEPOINT", "SCHEMA", "SCOPE", "SCROLL",
      "SEARCH", "SECOND", "SECTION", "SELECT", "SEQUENCE", "SESSION", "SESSION_USER", "SET", "SETS", "SIZE",
      "SMALLINT", "SOME", "SPACE", "SPECIFIC", "SPECIFICTYPE", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING",
      "START", "STATE", "STATEMENT", "STATIC", "STRUCTURE", "SYSTEM_USER", "TABLE", "TEMPORARY", "TERMINATE",
      "THAN", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSACTION",
      "TRANSLATION", "TREAT", "TRIGGER", "TRUE", "UNDER", "UNION", "UNIQUE", "UNKNOWN", "UNNEST", "UPDATE",
      "USAGE", "USER", "USING", "VALUE", "VALUES", "VARCHAR", "VARIABLE", "VARYING", "VIEW", "WHEN", "WHENEVER",
      "WHERE", "WITH", "WITHOUT", "WORK", "WRITE", "YEAR", "ZONE" };
  }

  /**
   * @param tableNames
   *          The names of the tables to lock
   * @return The SQL commands to lock database tables for write purposes.
   */
  @Override
  public String getSQLLockTables( String[] tableNames ) {
    return null; // Netezza does not support exclusive locking
  }

  /**
   * @param tableName
   *          The name of the table to unlock
   * @return The SQL command to unlock a database table.
   */
  @Override
  public String getSQLUnlockTables( String[] tableName ) {
    return null; // commit unlocks everything!
  }

  /**
   * @return true if the database defaults to naming tables and fields in uppercase. True for most databases except for
   *         stuborn stuff like Postgres ;-)
   */
  @Override
  public boolean isDefaultingToUppercase() {
    return false;
  }

  /**
   * @return true if the database resultsets support getTimeStamp() to retrieve date-time. (Date)
   */
  @Override
  public boolean supportsTimeStampToDateConversion() {
    return false;
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] { "nzjdbc.jar" };
  }

}
