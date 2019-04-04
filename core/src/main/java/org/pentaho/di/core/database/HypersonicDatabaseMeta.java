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
 * Contains Hypersonic specific information through static final members
 *
 * @author Matt
 * @since 11-mrt-2005
 */

public class HypersonicDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {
  @Override
  public int[] getAccessTypeList() {
    return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  @Override
  public int getDefaultDatabasePort() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
      return 9001;
    }
    return -1;
  }

  @Override
  public String getDriverClass() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC ) {
      return "sun.jdbc.odbc.JdbcOdbcDriver";
    } else {
      return "org.hsqldb.jdbcDriver";
    }
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC ) {
      return "jdbc:odbc:" + databaseName;
    } else {
      if ( ( Utils.isEmpty( port ) || "-1".equals( port ) ) && Utils.isEmpty( hostname ) ) {
        // When no port is specified, or port is 0 support local/memory
        // HSQLDB databases.
        return "jdbc:hsqldb:" + databaseName;
      } else {
        return "jdbc:hsqldb:hsql://" + hostname + ":" + port + "/" + databaseName;
      }
    }
  }

  /**
   * @return true if the database supports bitmap indexes
   */
  @Override
  public boolean supportsBitmapIndex() {
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
   *
   */
  @Override
  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
      String pk, boolean semicolon ) {
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
    return "ALTER TABLE " + tablename + " ALTER COLUMN " + getFieldDefinition( v, tk, pk, use_autoinc, true, false );
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
        if ( supportsBooleanDataType() ) {
          retval.append( "BOOLEAN" );
        } else {
          retval.append( "CHAR(1)" );
        }
        break;
      case ValueMetaInterface.TYPE_NUMBER:
      case ValueMetaInterface.TYPE_INTEGER:
      case ValueMetaInterface.TYPE_BIGNUMBER:
        if ( fieldname.equalsIgnoreCase( tk ) || // Technical key
            fieldname.equalsIgnoreCase( pk ) // Primary key
        ) {
          retval.append( "BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 0, INCREMENT BY 1) PRIMARY KEY" );
        } else {
          if ( length > 0 ) {
            if ( precision > 0 || length > 18 ) {
              retval.append( "NUMERIC(" ).append( length ).append( ", " ).append( precision ).append( ')' );
            } else {
              if ( length > 9 ) {
                retval.append( "BIGINT" );
              } else {
                if ( length < 5 ) {
                  retval.append( "SMALLINT" );
                } else {
                  retval.append( "INTEGER" );
                }
              }
            }

          } else {
            retval.append( "DOUBLE PRECISION" );
          }
        }
        break;
      case ValueMetaInterface.TYPE_STRING:
        if ( length >= DatabaseMeta.CLOB_LENGTH ) {
          retval.append( "LONGVARCHAR" );
        } else {
          retval.append( "VARCHAR" );
          if ( length > 0 ) {
            retval.append( '(' ).append( length );
          } else {
            retval.append( '(' ); // Maybe use some default DB String length?
          }
          retval.append( ')' );
        }
        break;
      default:
        retval.append( " UNKNOWN" );
        break;
    }

    if ( add_cr ) {
      retval.append( Const.CR );
    }

    return retval.toString();
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] { "hsqldb.jar" };
  }

  @Override
  public String getExtraOptionsHelpText() {
    return "http://hsqldb.sourceforge.net/doc/guide/ch04.html#N109DA";
  }

  @Override
  public String[] getReservedWords() {
    return new String[] { "ADD", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "ARRAY", "AS", "ASENSITIVE",
      "ASYMMETRIC", "AT", "ATOMIC", "AUTHORIZATION", "BEGIN", "BETWEEN", "BIGINT", "BINARY", "BLOB", "BOOLEAN", "BOTH",
      "BY", "CALL", "CALLED", "CASCADED", "CASE", "CAST", "CHAR", "CHARACTER", "CHECK", "CLOB", "CLOSE", "COLLATE",
      "COLUMN", "COMMIT", "CONDIITON", "CONNECT", "CONSTRAINT", "CONTINUE", "CORRESPONDING", "CREATE", "CROSS", "CUBE",
      "CURRENT", "CURRENT_DATE", "CURRENT_DEFAULT_TRANSFORM_GROUP", "CURRENT_PATH", "CURRENT_ROLE", "CURRENT_TIME",
      "CURRENT_TIMESTAMP", "CURRENT_TRANSFORM_GROUP_FOR_TYPE", "CURRENT_USER", "CURSOR", "CYCLE", "DATE", "DAY",
      "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DELETE", "DEREF", "DESCRIBE", "DETERMINISTIC",
      "DISCONNECT", "DISTINCT", "DO", "DOUBLE", "DROP", "DYNAMIC", "EACH", "ELEMENT", "ELSE", "ELSEIF", "END",
      "ESCAPE", "EXCEPT", "EXEC", "EXECUTE", "EXISTS", "EXIT", "EXTERNAL", "FALSE", "FETCH", "FILTER", "FLOAT", "FOR",
      "FOREIGN", "FREE", "FROM", "FULL", "FUNCTION", "GET", "GLOBAL", "GRANT", "GROUP", "GROUPING", "HANDLER",
      "HAVING", "HEADER", "HOLD", "HOUR", "IDENTITY", "IF", "IMMEDIATE", "IN", "INDICATOR", "INNER", "INOUT", "INPUT",
      "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERSECT", "INTERVAL", "INTO", "IS", "ITERATE", "JOIN", "LANGUAGE",
      "LARGE", "LATERAL", "LEADING", "LEAVE", "LEFT", "LIKE", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LOOP", "MATCH",
      "MEMBER", "METHOD", "MINUTE", "MODIFIES", "MODULE", "MONTH", "MULTISET", "NATIONAL", "NAUTRAL", "NCHAR", "NCLOB",
      "NEW", "NEXT", "NO", "NONE", "NOT", "NULL", "NUMERIC", "OF", "OLD", "ON", "ONLY", "OPEN", "OR", "ORDER", "OUT",
      "OUTER", "OUTPUT", "OVER", "OVERLAPS", "PARAMETER", "PARTITION", "PRECISION", "PREPARE", "PRIMARY", "PROCEDURE",
      "RANGE", "READS", "REAL", "RECURSIVE", "REF", "REFERENCES", "REFERENCING", "RELEASE", "REPEAT", "RESIGNAL",
      "RESULT", "RETURN", "RETURNS", "REVOKE", "RIGHT", "ROLLBACK", "ROLLUP", "ROW", "ROWS", "SAVEPOINT", "SCOPE",
      "SCROLL", "SECOND", "SEARCH", "SELECT", "SENSITIVE", "SESSION_USER", "SET", "SIGNAL", "SIMILAR", "SMALLINT",
      "SOME", "SPECIFIC", "SPECIFICTYPE", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "START", "STATIC",
      "SUBMULTISET", "SYMMETRIC", "SYSTEM", "SYSTEM_USER", "TABLE", "TABLESAMPLE", "THEN", "TIME", "TIMESTAMP",
      "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSLATION", "TREAT", "TRIGGER", "TRUE", "UNDO", "UNION",
      "UNIQUE", "UNKNOWN", "UNNEST", "UNTIL", "UPDATE", "USER", "USING", "VALUE", "VALUES", "VARCHAR", "VARYING",
      "WHEN", "WHENEVER", "WHERE", "WHILE", "WINDOW", "WITH", "WITHIN", "WITHOUT", "YEAR", "ALWAYS", "ACTION", "ADMIN",
      "AFTER", "ALIAS", "ASC", "AUTOCOMMIT", "AVG", "BACKUP", "BEFORE", "CACHED", "CASCADE", "CASEWHEN", "CHECKPOINT",
      "CLASS", "COALESCE", "COLLATION", "COMPACT", "COMPRESSED", "CONCAT", "CONVERT", "COUNT", "DATABASE", "DEFRAG",
      "DESC", "EVERY", "EXPLAIN", "EXTRACT", "GENERATED", "IFNULL", "IGNORECASE", "IMMEDIATELY", "INCREMENT", "INDEX",
      "KEY", "LIMIT", "LOGSIZE", "MAX", "MAXROWS", "MEMORY", "MERGE", "MIN", "MINUS", "NOW", "NOWAIT", "NULLIF", "NVL",
      "OFFSET", "PASSWORD", "SCHEMA", "PLAN", "PRESERVE", "POSITION", "PROPERTY", "PUBLIC", "QUEUE", "READONLY",
      "REFERENTIAL_INTEGRITY", "RENAME", "RESTART", "RESTRICT", "ROLE", "SCRIPT", "SCRIPTFORMAT", "SEQUENCE",
      "SHUTDOWN", "SOURCE", "STDDEV_POP", "STDDEV_SAMP", "SUBSTRING", "SUM", "SYSDATE", "TEMP", "TEMPORARY", "TEXT",
      "TODAY", "TOP", "TRIM", "VAR_POP", "VAR_SAMP", "VIEW", "WORK", "WRITE_DELAY", };
  }

  /**
   * @return true if the database supports sequences
   */
  @Override
  public boolean supportsSequences() {
    return true;
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
    return "SELECT * FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_NAME = '" + sequenceName + "'";
  }

  @Override
  public String getSQLListOfSequences() {
    return "SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES";
  }

  /**
   * Get the current value of a database sequence
   *
   * @param sequenceName
   *          The sequence to check
   * @return The current value of a database sequence
   */
  @Override
  public String getSQLCurrentSequenceValue( String sequenceName ) {
    // Note - the following only works for 2.x and higher HSQLDB. But we don't really use it anywhere
    return "SELECT " + sequenceName + ".currval FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_NAME = '"
        + sequenceName + "'";
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
    return "SELECT NEXT VALUE FOR " + sequenceName
        + " FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_NAME = '" + sequenceName + "'";
  }

}
