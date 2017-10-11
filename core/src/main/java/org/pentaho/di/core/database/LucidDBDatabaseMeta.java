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
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Contains LucidDB specific information through static final members
 *
 * @author Matt
 * @since 24-oct-2008
 */

public class LucidDBDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {
  @Override
  public int[] getAccessTypeList() {
    return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  @Override
  public int getDefaultDatabasePort() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
      return 8034;
    }
    return -1;
  }

  @Override
  public String getDriverClass() {
    return "org.luciddb.jdbc.LucidDbClientDriver";

  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) {

    if ( !Utils.isEmpty( port ) && Const.toInt( port, -1 ) > 0 ) {
      return "jdbc:luciddb:http://" + hostname + ":" + port;
    } else {
      return "jdbc:luciddb:http://" + hostname;
    }
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
    return "SELECT " + columnname + " FROM " + tableName;
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
    // NOTE jvs 13-Dec-2008: This will be possible starting with LucidDB
    // v0.8.1, so might as well enable it now.
    return "ALTER TABLE " + tablename + " ADD " + getFieldDefinition( v, tk, pk, use_autoinc, true, false );
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
    // This is not possible in LucidDB...
    return null;
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
    // This is not possible in LucidDB...
    return null;
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
      case ValueMetaInterface.TYPE_TIMESTAMP:
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
            retval += "BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY";
          } else {
            retval += "BIGINT PRIMARY KEY";
          }
        } else {
          if ( precision == 0 ) {
            if ( length > 18 ) {
              retval += "DECIMAL(" + length + ",0)";
            } else {
              if ( length >= 9 ) {
                retval += "BIGINT";
              } else {
                retval += "INT";
              }
            }
          } else {
            if ( precision > 0 ) {
              if ( length > 0 ) {
                retval += "DECIMAL(" + length + "," + precision + ")";
              }
            } else {
              retval += "DOUBLE";
            }
          }
        }
        break;
      case ValueMetaInterface.TYPE_STRING:
        // Maybe use some default DB String length in case length<=0
        if ( length > 0 ) {
          retval += "VARCHAR(" + length + ")";
        } else {
          retval += "VARCHAR(100)";
        }
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

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.database.DatabaseInterface#getReservedWords()
   */
  @Override
  public String[] getReservedWords() {
    // Copied from : http://pub.eigenbase.org/wiki/FarragoSqlReservedWords
    //
    return new String[] {
      "ABS", "ABSOLUTE", "ACTION", "ADD", "ALL", "ALLOCATE", "ALLOW", "ALTER", "ANALYZE", "AND", "ANY", "ARE",
      "ARRAY", "AS", "ASC", "ASENSITIVE", "ASSERTION", "ASYMMETRIC", "AT", "ATOMIC", "AUTHORIZATION", "AVG",
      "BEGIN", "BETWEEN", "BIGINT", "BINARY", "BIT", "BIT_LENGTH", "BLOB", "BOOLEAN", "BOTH", "BY", "CALL",
      "CALLED", "CARDINALITY", "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CEIL", "CEILING", "CHAR",
      "CHARACTER", "CHARACTER_LENGTH", "CHAR_LENGTH", "CHECK", "CHECKPOINT", "CLOB", "CLOSE", "CLUSTERED",
      "COALESCE", "COLLATE", "COLLATION", "COLLECT", "COLUMN", "COMMIT", "CONDITION", "CONNECT", "CONNECTION",
      "CONSTRAINT", "CONSTRAINTS", "CONTINUE", "CONVERT", "CORR", "CORRESPONDING", "COUNT", "COVAR_POP",
      "COVAR_SAMP", "CREATE", "CROSS", "CUBE", "CUME_DIST", "CURRENT", "CURRENT_DATE",
      "CURRENT_DEFAULT_TRANSFORM_GROUP", "CURRENT_PATH", "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_TIMESTAMP",
      "CURRENT_TRANSFORM_GROUP_FOR_TYPE", "CURRENT_USER", "CURSOR", "CYCLE", "DATE", "DAY", "DEALLOCATE", "DEC",
      "DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE", "DEFERRED", "DELETE", "DENSE_RANK", "DEREF", "DESC",
      "DESCRIBE", "DESCRIPTOR", "DETERMINISTIC", "DIAGNOSTICS", "DISALLOW", "DISCONNECT", "DISTINCT", "DOMAIN",
      "DOUBLE", "DROP", "DYNAMIC", "EACH", "ELEMENT", "ELSE", "END", "END-EXEC", "ESCAPE", "EVERY", "EXCEPT",
      "EXCEPTION", "EXEC", "EXECUTE", "EXISTS", "EXP", "EXPLAIN", "EXTERNAL", "EXTRACT", "FALSE", "FETCH",
      "FILTER", "FIRST", "FIRST_VALUE", "FLOAT", "FLOOR", "FOR", "FOREIGN", "FOUND", "FREE", "FROM", "FULL",
      "FUNCTION", "FUSION", "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GROUP", "GROUPING", "HAVING", "HOLD",
      "HOUR", "IDENTITY", "IMMEDIATE", "IMPORT", "IN", "INADD", "INDICATOR", "INITIALLY", "INNER", "INOUT",
      "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERSECT", "INTERSECTION", "INTERVAL", "INTO", "IS",
      "ISOLATION", "JOIN", "KEY", "LANGUAGE", "LARGE", "LAST", "LAST_VALUE", "LATERAL", "LEADING", "LEFT",
      "LEVEL", "LIKE", "LIMIT", "LN", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LOWER", "MATCH", "MAX", "MEMBER",
      "MERGE", "METHOD", "MIN", "MINUTE", "MOD", "MODIFIES", "MODULE", "MONTH", "MULTISET", "NAMES", "NATIONAL",
      "NATURAL", "NCHAR", "NCLOB", "NEW", "NEXT", "NO", "NONE", "NORMALIZE", "NOT", "NULL", "NULLIF", "NUMERIC",
      "OCTET_LENGTH", "OF", "OLD", "ON", "ONLY", "OPEN", "OPTION", "OR", "ORDER", "OUT", "OUTADD", "OUTER",
      "OVER", "OVERLAPS", "OVERLAY", "PAD", "PARAMETER", "PARTIAL", "PARTITION", "PERCENTILE_CONT",
      "PERCENTILE_DISC", "PERCENT_RANK", "POSITION", "POWER", "PRECISION", "PREPARE", "PRESERVE", "PRIMARY",
      "PRIOR", "PRIVILEGES", "PROCEDURE", "PUBLIC", "RANGE", "RANK", "READ", "READS", "REAL", "RECURSIVE",
      "REF", "REFERENCES", "REFERENCING", "REGR_AVGX", "REGR_AVGY", "REGR_COUNT", "REGR_INTERCEPT", "REGR_R2",
      "REGR_SLOPE", "REGR_SXX", "REGR_SXY", "RELATIVE", "RELEASE", "RESTRICT", "RESULT", "RETURN", "RETURNS",
      "REVOKE", "RIGHT", "ROLLBACK", "ROLLUP", "ROW", "ROW_NUMBER", "ROWS", "SAVEPOINT", "SCHEMA", "SCOPE",
      "SCROLL", "SEARCH", "SECOND", "SECTION", "SELECT", "SENSITIVE", "SESSION", "SESSION_USER", "SET",
      "SIMILAR", "SIZE", "SMALLINT", "SOME", "SPACE", "SPECIFIC", "SPECIFICTYPE", "SQL", "SQLCODE", "SQLERROR",
      "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQRT", "START", "STATIC", "STDDEV_POP", "STDDEV_SAMP",
      "SUBMULTISET", "SUBSTRING", "SUM", "SYMMETRIC", "SYSTEM", "SYSTEM_USER", "TABLE", "TABLESAMPLE",
      "TEMPORARY", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TINYINT", "TO", "TRAILING",
      "TRANSACTION", "TRANSLATE", "TRANSLATION", "TREAT", "TRIGGER", "TRIM", "TRUE", "TRUNCATE", "UESCAPE",
      "UNION", "UNIQUE", "UNKNOWN", "UNNEST", "UPDATE", "UPPER", "USAGE", "USER", "USING", "VALUE", "VALUES",
      "VARBINARY", "VARCHAR", "VAR_POP", "VAR_SAMP", "VARYING", "VIEW", "WHEN", "WHENEVER", "WHERE",
      "WIDTH_BUCKET", "WINDOW", "WITH", "WITHIN", "WITHOUT", "WORK", "WRITE", "YEAR", "ZONE", };
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] { "LucidDbClient.jar" };
  }

  @Override
  public String getExtraOptionsHelpText() {
    return "http://pub.eigenbase.org/wiki/LucidDbDocs";
  }

  @Override
  public boolean useSchemaNameForTableList() {
    // This prevents the DB explorer from showing unqualified table names,
    // which is good, otherwise when it generates SQL to select from them,
    // there is no schema qualifier, resulting in an error.
    return true;
  }

  @Override
  public boolean requiresCastToVariousForIsNull() {
    return true;
  }

}
