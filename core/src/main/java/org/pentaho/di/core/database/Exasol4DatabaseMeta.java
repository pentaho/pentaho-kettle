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

package org.pentaho.di.core.database;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Contains Exasol 4 specific information through static final members
 *
 * @author Slawomir Chodnicki
 * @since Jan 24, 2012
 */

public class Exasol4DatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {
  @Override
  public int[] getAccessTypeList() {
    return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  @Override
  public int getDefaultDatabasePort() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
      return 8563;
    }
    return -1;
  }

  /**
   * @return Whether or not the database can use auto increment type of fields (pk)
   */
  @Override
  public boolean supportsAutoInc() {
    // Exasol does support the identity column type, but does not support returning generated
    // keys on the jdbc driver
    return false;
  }

  /**
   * @see org.pentaho.di.core.database.DatabaseInterface#getLimitClause(int)
   */
  @Override
  public String getLimitClause( int nrRows ) {
    return " WHERE ROWNUM <= " + nrRows;
  }

  /**
   * Returns the minimal SQL to launch in order to determine the layout of the resultset for a given database table
   *
   * @param tableName
   *          The name of the table to determine the layout for
   * @return The SQL to launch.
   */
  @Override
  public String getSQLQueryFields( String tableName ) {
    return "SELECT /*+FIRST_ROWS*/ * FROM " + tableName + " WHERE 1=0";
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
    return "SELECT /*+FIRST_ROWS*/ " + columnname + " FROM " + tableName + " WHERE 1=0";
  }

  @Override
  public boolean needsToLockAllTables() {
    return false;
  }

  @Override
  public String getDriverClass() {
    return "com.exasol.jdbc.EXADriver";
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) throws KettleDatabaseException {
    return "jdbc:exa:" + hostname + ":" + port;
  }

  /**
   * Oracle doesn't support options in the URL, we need to put these in a Properties object at connection time...
   */
  @Override
  public boolean supportsOptionsInURL() {
    return true;
  }

  /**
   * @return true if the database supports sequences
   */
  @Override
  public boolean supportsSequences() {
    return false;
  }

  /**
   * @return true if we need to supply the schema-name to getTables in order to get a correct list of items.
   */
  @Override
  public boolean useSchemaNameForTableList() {
    return true;
  }

  /**
   * @return true if the database supports synonyms
   */
  @Override
  public boolean supportsSynonyms() {
    return false;
  }

  /**
   * Generates the SQL statement to add a column to the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param use_autoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to add a column to the specified table
   */
  @Override
  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
    String pk, boolean semicolon ) {
    return "ALTER TABLE "
      + tablename + " ADD ( " + getFieldDefinition( v, tk, pk, use_autoinc, true, false ) + " ) ";
  }

  /**
   * Generates the SQL statement to drop a column from the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param use_autoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
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
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param use_autoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to modify a column in the specified table
   */
  @Override
  public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
    String pk, boolean semicolon ) {

    return "ALTER TABLE "
      + tablename + " MODIFY COLUMN " + getFieldDefinition( v, tk, pk, use_autoinc, true, false );

  }

  @Override
  public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean use_autoinc,
    boolean add_fieldname, boolean add_cr ) {
    StringBuilder retval = new StringBuilder( 128 );

    String fieldname = v.getName();
    int length = v.getLength();
    int precision = v.getPrecision();

    if ( add_fieldname ) {
      retval.append( fieldname ).append( ' ' );
    }

    int type = v.getType();
    switch ( type ) {
      case ValueMetaInterface.TYPE_TIMESTAMP:
      case ValueMetaInterface.TYPE_DATE:
        retval.append( "TIMESTAMP" );
        break;
      case ValueMetaInterface.TYPE_BOOLEAN:
        retval.append( "BOOLEAN" );
        break;
      case ValueMetaInterface.TYPE_NUMBER:
      case ValueMetaInterface.TYPE_BIGNUMBER:
        retval.append( "DECIMAL" );
        if ( length > 0 ) {
          retval.append( '(' ).append( length );
          if ( precision > 0 ) {
            retval.append( ", " ).append( precision );
          }
          retval.append( ')' );
        }
        break;
      case ValueMetaInterface.TYPE_INTEGER:
        if ( fieldname.equalsIgnoreCase( tk ) || // Technical key
          fieldname.equalsIgnoreCase( pk ) // Primary key
        ) {
          // As soon as Exasol supports returning auto inc keys, this would be the correct type
          // if (use_autoinc) {
          // retval.append("BIGINT IDENTITY NOT NULL PRIMARY KEY");
          // } else {
          retval.append( "BIGINT NOT NULL PRIMARY KEY" );
          // }
        } else {
          retval.append( "INTEGER" );
        }

        break;
      case ValueMetaInterface.TYPE_STRING:

        if ( length > 0 && length <= 2000000 ) {
          retval.append( "VARCHAR(" ).append( length ).append( ')' );
        } else {
          retval.append( "VARCHAR(2000000)" ); // We don't know, so we just
          // use the maximum...
        }
        break;
      default:
        retval.append( "UNKNOWN" );
        break;
    }

    if ( add_cr ) {
      retval.append( Const.CR );
    }

    return retval.toString();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.ibridge.kettle.core.database.DatabaseInterface#getReservedWords()
   */
  @Override
  public String[] getReservedWords() {
    return new String[] {
      "ABSOLUTE", "ACTION", "ADD", "AFTER", "ALL", "ALLOCATE", "ALTER", "AND", "APPEND", "ARE", "ARRAY", "AS",
      "ASC", "ASENSITIVE", "ASSERTION", "AT", "ATTRIBUTE", "AUTHID", "AUTHORIZATION", "BEFORE", "BEGIN",
      "BETWEEN", "BIGINT", "BINARY", "BIT", "BLOB", "BLOCKED", "BOOL", "BOOLEAN", "BOTH", "BY", "BYTE", "CALL",
      "CALLED", "CARDINALITY", "CASCADE", "CASCADED", "CASE", "CASESPECIFIC", "CAST", "CATALOG", "CHAIN",
      "CHAR", "CHARACTER", "CHARACTERISTICS", "CHARACTER_SET_CATALOG", "CHARACTER_SET_NAME",
      "CHARACTER_SET_SCHEMA", "CHECK", "CHECKED", "CLOSE", "COALESCE", "COLLATE", "COLLATION",
      "COLLATION_CATALOG", "COLLATION_NAME", "COLLATION_SCHEMA", "COLUMN", "COMMIT", "CONDITION", "CONNECTION",
      "CONSTANT", "CONSTRAINT", "CONSTRAINTS", "CONSTRUCTOR", "CONTAINS", "CONTINUE", "CONTROL", "CONVERT",
      "CORRESPONDING", "CREATE", "CS", "CSV", "CUBE", "CURRENT", "CURRENT_DATE", "CURRENT_PATH", "CURRENT_ROLE",
      "CURRENT_SCHEMA", "CURRENT_SESSION", "CURRENT_STATEMENT", "CURRENT_TIME", "CURRENT_TIMESTAMP",
      "CURRENT_USER", "CURSOR", "CYCLE", "DATA", "DATALINK", "DATE", "DATETIME_INTERVAL_CODE",
      "DATETIME_INTERVAL_PRECISION", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE",
      "DEFERRED", "DEFINED", "DEFINER", "DELETE", "DEREF", "DERIVED", "DESC", "DESCRIBE", "DESCRIPTOR",
      "DETERMINISTIC", "DISABLE", "DISABLED", "DISCONNECT", "DISPATCH", "DISTINCT", "DLURLCOMPLETE",
      "DLURLPATH", "DLURLPATHONLY", "DLURLSCHEME", "DLURLSERVER", "DLVALUE", "DO", "DOMAIN", "DOUBLE", "DROP",
      "DYNAMIC", "DYNAMIC_FUNCTION", "DYNAMIC_FUNCTION_CODE", "EACH", "ELSE", "ELSEIF", "ELSIF", "ENABLE",
      "ENABLED", "END", "END-EXEC", "ENFORCE", "EQUALS", "ERRORS", "ESCAPE", "EXCEPT", "EXCEPTION", "EXEC",
      "EXECUTE", "EXISTS", "EXIT", "EXPORT", "EXTERNAL", "EXTRACT", "FALSE", "FBV", "FETCH", "FILE", "FINAL",
      "FIRST", "FLOAT", "FOLLOWING", "FOR", "FORALL", "FORCE", "FORMAT", "FOUND", "FREE", "FROM", "FS", "FULL",
      "FUNCTION", "GENERAL", "GENERATED", "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GRANTED", "GROUP",
      "GROUPING", "GROUP_CONCAT", "HAVING", "HOLD", "HOUR", "IDENTITY", "IF", "IFNULL", "IMMEDIATE",
      "IMPLEMENTATION", "IMPORT", "IN", "INDEX", "INDICATOR", "INNER", "INOUT", "INPUT", "INSENSITIVE",
      "INSERT", "INSTANCE", "INSTANTIABLE", "INT", "INTEGER", "INTEGRITY", "INTERSECT", "INTERVAL", "INTO",
      "INVOKER", "IS", "ITERATE", "JOIN", "KEY_MEMBER", "KEY_TYPE", "LARGE", "LAST", "LATERAL", "LEADING",
      "LEAVE", "LEFT", "LIKE", "LIMIT", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LOCATOR", "LOG", "LONGVARCHAR",
      "LOOP", "MAP", "MATCH", "MATCHED", "MERGE", "METHOD", "MINUS", "MINUTE", "MOD", "MODIFIES", "MODIFY",
      "MODULE", "MONTH", "NAMES", "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NEW", "NEXT", "NLS_DATE_FORMAT",
      "NLS_DATE_LANGUAGE", "NLS_NUMERIC_CHARACTERS", "NLS_TIMESTAMP_FORMAT", "NO", "NOLOGGING", "NONE", "NOT",
      "NULL", "NULLIF", "NUMBER", "NUMERIC", "OBJECT", "OF", "OFF", "OLD", "ON", "ONLY", "OPEN", "OPTION",
      "OPTIONS", "OR", "ORDER", "ORDERING", "ORDINALITY", "OTHERS", "OUT", "OUTER", "OUTPUT", "OVER",
      "OVERLAPS", "OVERLAY", "OVERRIDING", "PAD", "PARALLEL_ENABLE", "PARAMETER", "PARAMETER_SPECIFIC_CATALOG",
      "PARAMETER_SPECIFIC_NAME", "PARAMETER_SPECIFIC_SCHEMA", "PARTIAL", "PATH", "PERMISSION", "PLACING",
      "POSITION", "PRECEDING", "PREPARE", "PRESERVE", "PRIOR", "PRIVILEGES", "PROCEDURE", "RANDOM", "RANGE",
      "READ", "READS", "REAL", "RECOVERY", "RECURSIVE", "REF", "REFERENCES", "REFERENCING", "REGEXP_LIKE",
      "RELATIVE", "RELEASE", "RENAME", "REPEAT", "REPLACE", "RESTORE", "RESTRICT", "RESULT", "RETURN",
      "RETURNED_LENGTH", "RETURNED_OCTET_LENGTH", "RETURNS", "REVOKE", "RIGHT", "ROLLBACK", "ROLLUP", "ROUTINE",
      "ROW", "ROWS", "ROWTYPE", "SAVEPOINT", "SCHEMA", "SCOPE", "SCRIPT", "SCROLL", "SEARCH", "SECOND",
      "SECTION", "SECURITY", "SELECT", "SELECTIVE", "SELF", "SENSITIVE", "SEPARATOR", "SEQUENCE", "SESSION",
      "SESSION_USER", "SET", "SETS", "SHORTINT", "SIMILAR", "SMALLINT", "SOURCE", "SPACE", "SPECIFIC",
      "SPECIFICTYPE", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQL_BIGINT", "SQL_BIT", "SQL_CHAR",
      "SQL_DATE", "SQL_DECIMAL", "SQL_DOUBLE", "SQL_FLOAT", "SQL_INTEGER", "SQL_LONGVARCHAR", "SQL_NUMERIC",
      "SQL_REAL", "SQL_SMALLINT", "SQL_TIMESTAMP", "SQL_TINYINT", "SQL_TYPE_DATE", "SQL_TYPE_TIMESTAMP",
      "SQL_VARCHAR", "START", "STATE", "STATEMENT", "STATIC", "STRUCTURE", "STYLE", "SUBSTRING", "SUBTYPE",
      "SYSDATE", "SYSTEM", "SYSTEM_USER", "SYSTIMESTAMP", "TABLE", "TEMPORARY", "TEXT", "THEN", "TIME",
      "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TINYINT", "TO", "TRAILING", "TRANSACTION", "TRANSFORM",
      "TRANSFORMS", "TRANSLATION", "TREAT", "TRIGGER", "TRIM", "TRUE", "TRUNCATE", "UNDER", "UNION", "UNIQUE",
      "UNKNOWN", "UNLINK", "UNNEST", "UNTIL", "UPDATE", "USAGE", "USER", "USING", "VALUE", "VALUES", "VARCHAR",
      "VARCHAR2", "VARRAY", "VERIFY", "VIEW", "WHEN", "WHENEVER", "WHERE", "WHILE", "WINDOW", "WITH", "WITHIN",
      "WITHOUT", "WORK", "YEAR", "YES", "ZONE" };
  }

  /**
   * @return extra help text on the supported options on the selected database platform.
   */
  @Override
  public String getExtraOptionsHelpText() {
    return "http://www.exasol.com/knowledge-center.html";
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] { "exajdbc.jar" };
  }

  /**
   * Verifies on the specified database connection if an index exists on the fields with the specified name.
   *
   * @param database
   *          a connected database
   * @param schemaName
   * @param tableName
   * @param idxFields
   * @return true if the index exists, false if it doesn't.
   * @throws KettleDatabaseException
   */
  @Override
  public boolean checkIndexExists( Database database, String schemaName, String tableName, String[] idx_fields ) throws KettleDatabaseException {

    // no explicit index handling, indexes are not exposed in exasol. Assume
    // all indexes are there!
    return true;
  }

  @Override
  public boolean requiresCreateTablePrimaryKeyAppend() {
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
    return false;
  }

  /**
   * @return The maximum number of columns in a database, <=0 means: no known limit
   */
  @Override
  public int getMaxColumnsInIndex() {
    return -1;
  }

  /**
   * @param string
   * @return A string that is properly quoted for use in an Exasol SQL statement (insert, update, delete, etc)
   */
  @Override
  public String quoteSQLString( String string ) {
    string = string.replaceAll( "'", "''" );
    string = string.replaceAll( "\\n", "'||chr(13)||'" );
    string = string.replaceAll( "\\r", "'||chr(10)||'" );
    return "'" + string + "'";
  }

  /**
   * Returns a false as Exasol does not allow for the releasing of savepoints.
   */
  @Override
  public boolean releaseSavepoint() {
    return false;
  }

  @Override
  public boolean supportsErrorHandlingOnBatchUpdates() {
    return false;
  }
}
