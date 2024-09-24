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
 * Contains Vertica Analytic Database information through static final members
 *
 * @author DEinspanjer
 * @since 2009-03-16
 * @author Matt
 * @since May-2008
 */

public class VerticaDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {
  @Override
  public int[] getAccessTypeList() {
    return new int[] {
      DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  @Override
  public String getDriverClass() {
    return "com.vertica.Driver";

  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) {
    return "jdbc:vertica://" + hostname + ":" + port + "/" + databaseName;
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

  /**
   * @return true if the database supports bitmap indexes
   */
  @Override
  public boolean supportsBitmapIndex() {
    return false;
  }

  /**
   * Generates the SQL statement to add a column to the specified table For this generic type, i set it to the most
   * common possibility.
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
    return "--NOTE: Table cannot be altered unless all projections are dropped.\nALTER TABLE "
      + tablename + " ADD " + getFieldDefinition( v, tk, pk, useAutoinc, true, false );
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
    return "--NOTE: Table cannot be altered unless all projections are dropped.\nALTER TABLE "
      + tablename + " ALTER COLUMN "
      + v.getName() + " SET DATA TYPE " + getFieldDefinition( v, tk, pk, useAutoinc, false, false );
  }

  @Override
  public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean useAutoinc,
                                    boolean addFieldName, boolean addCr ) {
    String retval = "";

    String fieldname = v.getName();
    int length = v.getLength();
    // Unused in vertica
    // int precision = v.getPrecision();

    if ( addFieldName ) {
      retval += fieldname + " ";
    }

    int type = v.getType();
    switch ( type ) {
      case ValueMetaInterface.TYPE_DATE:
      case ValueMetaInterface.TYPE_TIMESTAMP:
        retval += "TIMESTAMP";
        break;
      case ValueMetaInterface.TYPE_BOOLEAN:
        retval += "BOOLEAN";
        break;
      case ValueMetaInterface.TYPE_NUMBER:
      case ValueMetaInterface.TYPE_BIGNUMBER:
        retval += "FLOAT";
        break;
      case ValueMetaInterface.TYPE_INTEGER:
        retval += "INTEGER";
        break;
      case ValueMetaInterface.TYPE_STRING:
        retval += ( length < 1 ) ? "VARCHAR" : "VARCHAR(" + length + ")";
        break;
      case ValueMetaInterface.TYPE_BINARY:
        retval += ( length < 1 ) ? "VARBINARY" : "VARBINARY(" + length + ")";
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

  @Override
  public String[] getUsedLibraries() {
    return new String[] { "vertica_2.5_jdk_5.jar" };
  }

  @Override
  public int getDefaultDatabasePort() {
    return 5433;
  }

  @Override
  public String getLimitClause( int nrRows ) {
    return " LIMIT " + nrRows;
  }

  @Override
  public int getMaxVARCHARLength() {
    return 4000;
  }

  @Override
  public String[] getReservedWords() {
    return new String[] {
      // From "SQL Reference Manual.pdf" found on support.vertica.com
      "ABORT", "ABSOLUTE", "ACCESS", "ACTION", "ADD", "AFTER", "AGGREGATE", "ALL", "ALSO", "ALTER", "ANALYSE",
      "ANALYZE", "AND", "ANY", "ARRAY", "AS", "ASC", "ASSERTION", "ASSIGNMENT", "AT", "AUTHORIZATION",
      "BACKWARD", "BEFORE", "BEGIN", "BETWEEN", "BIGINT", "BINARY", "BIT", "BLOCK_DICT", "BLOCKDICT_COMP",
      "BOOLEAN", "BOTH", "BY", "CACHE", "CALLED", "CASCADE", "CASE", "CAST", "CATALOG_PATH", "CHAIN", "CHAR",
      "CHARACTER", "CHARACTERISTICS", "CHECK", "CHECKPOINT", "CLASS", "CLOSE", "CLUSTER", "COALESCE", "COLLATE",
      "COLUMN", "COMMENT", "COMMIT", "COMMITTED", "COMMONDELTA_COMP", "CONSTRAINT", "CONSTRAINTS", "CONVERSION",
      "CONVERT", "COPY", "CORRELATION", "CREATE", "CREATEDB", "CREATEUSER", "CROSS", "CSV", "CURRENT_DATABASE",
      "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "CYCLE", "DATA",
      "DATABASE", "DATAPATH", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFAULTS",
      "DEFERRABLE", "DEFERRED", "DEFINER", "DELETE", "DELIMITER", "DELIMITERS", "DELTARANGE_COMP",
      "DELTARANGE_COMP_SP", "DELTAVAL", "DESC", "DETERMINES", "DIRECT", "DISTINCT", "DISTVALINDEX", "DO",
      "DOMAIN", "DOUBLE", "DROP", "EACH", "ELSE", "ENCODING", "ENCRYPTED", "END", "EPOCH", "ERROR", "ESCAPE",
      "EXCEPT", "EXCEPTIONS", "EXCLUDING", "EXCLUSIVE", "EXECUTE", "EXISTS", "EXPLAIN", "EXTERNAL", "EXTRACT",
      "FALSE", "FETCH", "FIRST", "FLOAT", "FOR", "FORCE", "FOREIGN", "FORWARD", "FREEZE", "FROM", "FULL",
      "FUNCTION", "GLOBAL", "GRANT", "GROUP", "HANDLER", "HAVING", "HOLD", "HOUR", "ILIKE", "IMMEDIATE",
      "IMMUTABLE", "IMPLICIT", "IN", "IN_P", "INCLUDING", "INCREMENT", "INDEX", "INHERITS", "INITIALLY",
      "INNER", "INOUT", "INPUT", "INSENSITIVE", "INSERT", "INSTEAD", "INT", "INTEGER", "INTERSECT", "INTERVAL",
      "INTO", "INVOKER", "IS", "ISNULL", "ISOLATION", "JOIN", "KEY", "LANCOMPILER", "LANGUAGE", "LARGE", "LAST",
      "LATEST", "LEADING", "LEFT", "LESS", "LEVEL", "LIKE", "LIMIT", "LISTEN", "LOAD", "LOCAL", "LOCALTIME",
      "LOCALTIMESTAMP", "LOCATION", "LOCK", "MATCH", "MAXVALUE", "MERGEOUT", "MINUTE", "MINVALUE", "MOBUF",
      "MODE", "MONTH", "MOVE", "MOVEOUT", "MULTIALGORITHM_COMP", "MULTIALGORITHM_COMP_SP", "NAMES", "NATIONAL",
      "NATURAL", "NCHAR", "NEW", "NEXT", "NO", "NOCREATEDB", "NOCREATEUSER", "NODE", "NODES", "NONE", "NOT",
      "NOTHING", "NOTIFY", "NOTNULL", "NOWAIT", "NULL", "NULLIF", "NUMERIC", "OBJECT", "OF", "OFF", "OFFSET",
      "OIDS", "OLD", "ON", "ONLY", "OPERATOR", "OPTION", "OR", "ORDER", "OUT", "OUTER", "OVERLAPS", "OVERLAY",
      "OWNER", "PARTIAL", "PARTITION", "PASSWORD", "PLACING", "POSITION", "PRECISION", "PREPARE", "PRESERVE",
      "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURAL", "PROCEDURE", "PROJECTION", "QUOTE", "READ", "REAL",
      "RECHECK", "RECORD", "RECOVER", "REFERENCES", "REFRESH", "REINDEX", "REJECTED", "RELATIVE", "RELEASE",
      "RENAME", "REPEATABLE", "REPLACE", "RESET", "RESTART", "RESTRICT", "RETURNS", "REVOKE", "RIGHT", "RLE",
      "ROLLBACK", "ROW", "ROWS", "RULE", "SAVEPOINT", "SCHEMA", "SCROLL", "SECOND", "SECURITY", "SEGMENTED",
      "SELECT", "SEQUENCE", "SERIALIZABLE", "SESSION", "SESSION_USER", "SET", "SETOF", "SHARE", "SHOW",
      "SIMILAR", "SIMPLE", "SMALLINT", "SOME", "SPLIT", "STABLE", "START", "STATEMENT", "STATISTICS", "STDIN",
      "STDOUT", "STORAGE", "STRICT", "SUBSTRING", "SYSID", "TABLE", "TABLESPACE", "TEMP", "TEMPLATE",
      "TEMPORARY", "TERMINATOR", "THAN", "THEN", "TIME", "TIMESTAMP", "TIMESTAMPTZ", "TIMETZ", "TO", "TOAST",
      "TRAILING", "TRANSACTION", "TREAT", "TRIGGER", "TRIM", "TRUE", "TRUE_P", "TRUNCATE", "TRUSTED", "TYPE",
      "UNCOMMITTED", "UNENCRYPTED", "UNION", "UNIQUE", "UNKNOWN", "UNLISTEN", "UNSEGMENTED", "UNTIL", "UPDATE",
      "USAGE", "USER", "USING", "VACUUM", "VALID", "VALIDATOR", "VALINDEX", "VALUES", "VARCHAR", "VARYING",
      "VERBOSE", "VIEW", "VOLATILE", "WHEN", "WHERE", "WITH", "WITHOUT", "WORK", "WRITE", "YEAR", "ZONE" };
  }

  @Override
  public String getSQLColumnExists( String columnname, String tablename ) {
    return super.getSQLColumnExists( columnname, tablename ) + getLimitClause( 1 );
  }

  @Override
  public String getSQLQueryFields( String tableName ) {
    return super.getSQLQueryFields( tableName ) + getLimitClause( 1 );
  }

  @Override
  public String getSQLTableExists( String tablename ) {
    return super.getSQLTableExists( tablename ) + getLimitClause( 1 );
  }

  @Override
  public String[] getViewTypes() {
    return new String[] {};
  }

  @Override
  public boolean supportsAutoInc() {
    return false;
  }

  @Override
  public boolean supportsBooleanDataType() {
    return true;
  }

  /**
   * @return true if the database requires you to cast a parameter to varchar before comparing to null. Only required
   *         for DB2 and Vertica
   *
   */
  @Override
  public boolean requiresCastToVariousForIsNull() {
    return true;
  }

  /**
   * @return This indicator separates the normal URL from the options
   */
  @Override
  public String getExtraOptionIndicator() {
    return "?";
  }

  @Override
  public String getExtraOptionSeparator() {
    return "&";
  }

  /**
   * @return true if the database supports sequences
   */
  @Override
  public boolean supportsSequences() {
    return true;
  }

  @Override
  public String getSQLSequenceExists( String sequenceName ) {
    return "SELECT sequence_name FROM sequences WHERE sequence_name = '" + sequenceName + "'";
  }

  @Override
  public String getSQLListOfSequences() {
    return "SELECT sequence_name FROM sequences";
  }

  /**
   * Get the SQL to get the next value of a sequence. (Vertica version)
   *
   * @param sequenceName
   *          The sequence name
   * @return the SQL to get the next value of a sequence.
   */
  @Override
  public String getSQLCurrentSequenceValue( String sequenceName ) {
    return "SELECT currval('" + sequenceName + "')";
  }

  /**
   * Get the SQL to get the next value of a sequence.
   *
   * @param sequenceName
   *          The sequence name
   * @return the SQL to get the next value of a sequence.
   */
  @Override
  public String getSQLNextSequenceValue( String sequenceName ) {
    return "SELECT nextval('" + sequenceName + "')";
  }

  /**
   * @return false as the database does not support timestamp to date conversion.
   */
  @Override
  public boolean supportsTimeStampToDateConversion() {
    return false;
  }

  /*
   * @return false as the database does not support BLOB data type
   */
  @Override
  public boolean supportsGetBlob() {
    return false;
  }

  /**
   * @return Handles the special case of Vertica where the display size returned is twice the precision. In that case,
   *         the length is the precision.
   */
  @Override
  public boolean isDisplaySizeTwiceThePrecision() {
    return true;
  }

}
