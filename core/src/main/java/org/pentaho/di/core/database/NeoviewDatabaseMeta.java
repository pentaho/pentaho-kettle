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

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Contains HP Neoview specific information through static final members
 *
 * @author Jens
 * @since 2008-04-18
 */

public class NeoviewDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {
  @Override
  public int[] getAccessTypeList() {
    return new int[] {
      DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  @Override
  public int getDefaultDatabasePort() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
      return 18650;
    }
    return -1;
  }

  /**
   * @return Whether or not the database can use auto increment type of fields (pk)
   */
  @Override
  public boolean supportsAutoInc() {
    return false; // Neoview can support this but can not read it back
  }

  /**
   * @see org.pentaho.di.core.database.DatabaseInterface#getLimitClause(int)
   */
  @Override
  public String getLimitClause( int nrRows ) {
    // it is SELECT [FIRST N] * FROM xyz but this is not supported by the Database class
    return "";
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
    return "SELECT [FIRST 1] * FROM " + tableName;
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
    return "SELECT [FIRST 1] " + columnname + " FROM " + tableName;
  }

  @Override
  public boolean needsToLockAllTables() {
    return false;
  }

  @Override
  public String getDriverClass() {
    return "com.hp.t4jdbc.HPT4Driver";
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) throws KettleDatabaseException {
    String appendix = "";
    if ( !Utils.isEmpty( databaseName ) ) {
      if ( databaseName.contains( "=" ) ) {
        // some properties here like serverDataSource, catalog, schema etc. already given
        appendix = ":" + databaseName;
      } else {
        // assume to set the schema
        appendix = ":schema=" + databaseName;
      }
    }
    return "jdbc:hpt4jdbc://" + hostname + ":" + port + "/" + appendix;
  }

  /**
   * Neoview supports options in the URL.
   */
  @Override
  public boolean supportsOptionsInURL() {
    return true;
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
    return true;
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
    return "ALTER TABLE "
      + tablename + " ADD ( " + getFieldDefinition( v, tk, pk, useAutoinc, true, false ) + " ) ";
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
    return "ALTER TABLE " + tablename + " DROP ( " + v.getName() + " ) " + Const.CR;
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
    return "ALTER TABLE " + tablename + " MODIFY " + getFieldDefinition( v, tk, pk, useAutoinc, true, false );
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
      case ValueMetaInterface.TYPE_TIMESTAMP:
      case ValueMetaInterface.TYPE_DATE:
        retval += "TIMESTAMP";
        break;
      case ValueMetaInterface.TYPE_BOOLEAN:
        retval += "CHAR(1)";
        break;
      case ValueMetaInterface.TYPE_NUMBER:
      case ValueMetaInterface.TYPE_INTEGER:
      case ValueMetaInterface.TYPE_BIGNUMBER:
        if ( fieldname.equalsIgnoreCase( tk ) || // Technical key
          fieldname.equalsIgnoreCase( pk ) // Primary key
        ) {
          retval += "INTEGER NOT NULL PRIMARY KEY";
        } else {
          // Integer values...
          if ( precision == 0 ) {
            if ( length > 9 ) {
              if ( length <= 18 ) { // can hold max. 18
                retval += "NUMERIC(" + length + ")";
              } else {
                retval += "FLOAT";
              }
            } else {
              retval += "INTEGER";
            }
          } else {
            // Floating point values...
            // A double-precision floating-point number is accurate to approximately 15 decimal places.
            // +/- 2.2250738585072014e-308 through +/-1.7976931348623157e+308; stored in 8 byte
            // NUMERIC values are stored in less bytes, so we try to use them instead of a FLOAT:
            // 1 to 4 digits in 2 bytes, 5 to 9 digits in 4 bytes, 10 to 18 digits in 8 bytes
            if ( length <= 18 ) {
              retval += "NUMERIC(" + length;
              if ( precision > 0 ) {
                retval += ", " + precision;
              }
              retval += ")";
            } else {
              retval += "FLOAT";
            }
          }
        }
        break;
      case ValueMetaInterface.TYPE_STRING:
        // for LOB support see Neoview_JDBC_T4_Driver_Prog_Ref_2.2.pdf
        if ( length > 0 ) {
          if ( length <= 4028 ) {
            retval += "VARCHAR(" + length + ")";
          } else if ( length <= 4036 ) {
            retval += "CHAR(" + length + ")"; // squeezing 8 bytes ;-)
          } else {
            retval += "CLOB"; // before we go to CLOB
          }
        } else {
          retval += "CHAR(1)";
        }
        break;
      case ValueMetaInterface.TYPE_BINARY:
        retval += "BLOB";
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
   * @see com.ibridge.kettle.core.database.DatabaseInterface#getReservedWords()
   */
  @Override
  public String[] getReservedWords() {
    return new String[] {
      "ACTION", "FOR", "PROTOTYPE", "ADD", "FOREIGN", "PUBLIC", "ADMIN", "FOUND", "READ", "AFTER", "FRACTION",
      "READS", "AGGREGATE", "FREE", "REAL", "ALIAS", "FROM", "RECURSIVE", "ALL", "FULL", "REF", "ALLOCATE",
      "FUNCTION", "REFERENCES", "ALTER", "GENERAL", "REFERENCING", "AND", "GET", "RELATIVE", "ANY", "GLOBAL",
      "REPLACE", "ARE", "GO", "RESIGNAL", "ARRAY", "GOTO", "RESTRICT", "AS", "GRANT", "RESULT", "ASC", "GROUP",
      "RETURN", "ASSERTION", "GROUPING", "RETURNS", "ASYNC", "HAVING", "REVOKE", "AT", "HOST", "RIGHT",
      "AUTHORIZATION", "HOUR", "ROLE", "AVG", "IDENTITY", "ROLLBACK", "BEFORE", "IF", "ROLLUP", "BEGIN",
      "IGNORE", "ROUTINE", "BETWEEN", "IMMEDIATE", "ROW", "BINARY", "IN", "ROWS", "BIT", "INDICATOR",
      "SAVEPOINT", "BIT_LENGTH", "INITIALLY", "SCHEMA", "BLOB", "INNER", "SCOPE", "BOOLEAN", "INOUT", "SCROLL",
      "BOTH", "INPUT", "SEARCH", "BREADTH", "INSENSITIVE", "SECOND", "BY", "INSERT", "SECTION", "CALL", "INT",
      "SELECT", "CASE", "INTEGER", "SENSITIVE", "CASCADE", "INTERSECT", "SESSION", "CASCADED", "INTERVAL",
      "SESSION_USER", "CAST", "INTO", "SET", "CATALOG", "IS", "SETS", "CHAR", "ISOLATION", "SIGNAL",
      "CHAR_LENGTH", "ITERATE", "SIMILAR", "CHARACTER", "JOIN", "SIZE", "CHARACTER_LENGTH", "KEY", "SMALLINT",
      "CHECK", "LANGUAGE", "SOME", "CLASS", "LARGE", "CLOB", "LAST", "SPECIFIC", "CLOSE", "LATERAL",
      "SPECIFICTYPE", "COALESCE", "LEADING", "SQL", "COLLATE", "LEAVE", "SQL_CHAR", "COLLATION", "LEFT",
      "SQL_DATE", "COLUMN", "LESS", "SQL_DECIMAL", "COMMIT", "LEVEL", "SQL_DOUBLE", "COMPLETION", "LIKE",
      "SQL_FLOAT", "CONNECT", "LIMIT", "SQL_INT", "CONNECTION", "LOCAL", "SQL_INTEGER", "CONSTRAINT",
      "LOCALTIME", "SQL_REAL", "CONSTRAINTS", "LOCALTIMESTAMP", "SQL_SMALLINT", "CONSTRUCTOR", "LOCATOR",
      "SQL_TIME", "CONTINUE", "LOOP", "SQL_TIMESTAMP", "CONVERT", "LOWER", "SQL_VARCHAR", "CORRESPONDING",
      "MAP", "SQLCODE", "COUNT", "MATCH", "SQLERROR", "CREATE", "MAX", "SQLEXCEPTION", "CROSS", "MIN",
      "SQLSTATE", "CUBE", "MINUTE", "SQLWARNING", "CURRENT", "MODIFIES", "STRUCTURE", "CURRENT_DATE", "MODIFY",
      "SUBSTRING", "CURRENT_PATH", "MODULE", "SUM", "CURRENT_ROLE", "MONTH", "SYSTEM_USER", "CURRENT_TIME",
      "NAMES", "TABLE", "CURRENT_TIMESTAMP", "NATIONAL", "TEMPORARY", "CURRENT_USER", "NATURAL", "TERMINATE",
      "CURSOR", "NCHAR", "TEST", "CYCLE", "NCLOB", "THAN", "DATE", "NEW", "THEN", "DATETIME", "NEXT", "THERE",
      "DAY", "NO", "TIME", "DEALLOCATE", "NONE", "TIMESTAMP", "DEC", "NOT", "TIMEZONE_HOUR", "DECIMAL", "NULL",
      "TIMEZONE_MINUTE", "DECLARE", "NULLIF", "TO", "DEFAULT", "NUMERIC", "TRAILING", "DEFERRABLE", "OBJECT",
      "TRANSACTION", "DEFERRED", "OCTET_LENGTH", "TRANSLATE", "DELETE", "OF", "TRANSLATION", "DEPTH", "OFF",
      "TRANSPOSE", "DEREF", "OID", "TREAT", "DESC", "OLD", "TRIGGER", "DESCRIBE", "ON", "TRIM", "DESCRIPTOR",
      "ONLY", "TRUE", "DESTROY", "OPEN", "UNDER", "DESTRUCTOR", "OPERATORS", "UNION", "DETERMINISTIC", "OPTION",
      "UNIQUE", "DIAGNOSTICS", "OR", "UNKNOWN", "DISTINCT", "ORDER", "UNNEST", "DICTIONARY", "ORDINALITY",
      "UPDATE", "DISCONNECT", "OTHERS", "UPPER", "DOMAIN", "OUT", "UPSHIFT", "DOUBLE", "OUTER", "USAGE", "DROP",
      "OUTPUT", "USER", "DYNAMIC", "OVERLAPS", "USING", "EACH", "PAD", "VALUE", "ELSE", "PARAMETER", "VALUES",
      "ELSEIF", "PARAMETERS", "VARCHAR", "END", "PARTIAL", "VARIABLE", "END-EXEC", "PENDANT", "VARYING",
      "EQUALS", "POSITION", "VIEW", "ESCAPE", "POSTFIX", "VIRTUAL", "EXCEPT", "PRECISION", "VISIBLE",
      "EXCEPTION", "PREFIX", "WAIT", "EXEC", "PREORDER", "WHEN", "EXECUTE", "PREPARE", "WHENEVER", "EXISTS",
      "PRESERVE", "WHERE", "EXTERNAL", "PRIMARY", "WHILE", "EXTRACT", "PRIOR", "WITH", "FALSE", "PRIVATE",
      "WITHOUT", "FETCH", "PRIVILEGES", "WORK", "FIRST", "PROCEDURE", "WRITE", "FLOAT", "PROTECTED", "YEAR",
      "ZONE" };
  }

  @Override
  public String getSQLLockTables( String[] tableNames ) {
    StringBuilder sql = new StringBuilder( 128 );
    for ( int i = 0; i < tableNames.length; i++ ) {
      sql.append( "LOCK TABLE " ).append( tableNames[i] ).append( " IN EXCLUSIVE MODE;" ).append( Const.CR );
    }
    return sql.toString();
  }

  @Override
  public String getSQLUnlockTables( String[] tableNames ) {
    return null; // commit handles the unlocking!
  }

  /**
   * @return extra help text on the supported options on the selected database platform.
   */
  @Override
  public String getExtraOptionsHelpText() {
    return "https://www.hpe.com/psnow/doc/c01850029";
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] { "hpt4jdbc.jar" };
  }

  @Override
  public boolean supportsBitmapIndex() {
    return false;
  }

  @Override
  public int getMaxVARCHARLength() {
    return 4028;
  }

  @Override
  public String getTruncateTableStatement( String tableName ) {
    return "DELETE FROM " + tableName;
  }

  /**
   * This method allows a database dialect to convert database specific data types to Kettle data types.
   *
   * @param resultSet
   *          The result set to use
   * @param valueMeta
   *          The description of the value to retrieve
   * @param index
   *          the index on which we need to retrieve the value, 0-based.
   * @return The correctly converted Kettle data type corresponding to the valueMeta description.
   * @throws KettleDatabaseException
   */
  @Override
  public Object getValueFromResultSet( ResultSet rs, ValueMetaInterface val, int i ) throws KettleDatabaseException {
    Object data = null;

    try {
      switch ( val.getType() ) {
        case ValueMetaInterface.TYPE_BOOLEAN:
          data = Boolean.valueOf( rs.getBoolean( i + 1 ) );
          break;
        case ValueMetaInterface.TYPE_NUMBER:
          data = new Double( rs.getDouble( i + 1 ) );
          break;
        case ValueMetaInterface.TYPE_BIGNUMBER:
          data = rs.getBigDecimal( i + 1 );
          break;
        case ValueMetaInterface.TYPE_INTEGER:
          data = Long.valueOf( rs.getLong( i + 1 ) );
          break;
        case ValueMetaInterface.TYPE_STRING:
          if ( val.isStorageBinaryString() ) {
            data = rs.getBytes( i + 1 );
          } else {
            data = rs.getString( i + 1 );
          }
          break;
        case ValueMetaInterface.TYPE_BINARY:
          if ( supportsGetBlob() ) {
            Blob blob = rs.getBlob( i + 1 );
            if ( blob != null ) {
              data = blob.getBytes( 1L, (int) blob.length() );
            } else {
              data = null;
            }
          } else {
            data = rs.getBytes( i + 1 );
          }
          break;
        case ValueMetaInterface.TYPE_TIMESTAMP:
        case ValueMetaInterface.TYPE_DATE:
          if ( val.getOriginalColumnType() == java.sql.Types.TIME ) {
            // Neoview can not handle getDate / getTimestamp for a Time column
            data = rs.getTime( i + 1 );
            break; // Time is a subclass of java.util.Date, the default date
                   // will be 1970-01-01
          } else if ( val.getPrecision() != 1 && supportsTimeStampToDateConversion() ) {
            data = rs.getTimestamp( i + 1 );
            break; // Timestamp extends java.util.Date
          } else {
            data = rs.getDate( i + 1 );
            break;
          }
        default:
          break;
      }
      if ( rs.wasNull() ) {
        data = null;
      }
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( "Unable to get value '"
        + val.toStringMeta() + "' from database resultset, index " + i, e );
    }

    return data;
  }

}
