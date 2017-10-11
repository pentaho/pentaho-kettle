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

import java.sql.ResultSet;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Contains MySQL specific information through static final members
 *
 * @author Matt
 * @since 11-mrt-2005
 */

public class MSAccessDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {
  @Override
  public int[] getAccessTypeList() {
    return new int[] { DatabaseMeta.TYPE_ACCESS_ODBC };
  }

  @Override
  public boolean supportsSetCharacterStream() {
    return false;
  }

  /**
   * @see DatabaseInterface#getNotFoundTK(boolean)
   */
  @Override
  public int getNotFoundTK( boolean use_autoinc ) {
    if ( supportsAutoInc() && use_autoinc ) {
      return 1;
    }
    return super.getNotFoundTK( use_autoinc );
  }

  @Override
  public String getDriverClass() {
    return "sun.jdbc.odbc.JdbcOdbcDriver"; // always ODBC!
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) {
    return "jdbc:odbc:" + databaseName;
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
   * @see org.pentaho.di.core.database.DatabaseInterface#getSchemaTableCombination(java.lang.String, java.lang.String)
   */
  @Override
  @SuppressWarnings( "deprecation" )
  public String getSchemaTableCombination( String schema_name, String table_part ) {
    return getBackwardsCompatibleSchemaTableCombination( schema_name, table_part );
  }

  /**
   * Get the maximum length of a text field for this database connection. This includes optional CLOB, Memo and Text
   * fields. (the maximum!)
   *
   * @return The maximum text field length for this database type. (mostly CLOB_LENGTH)
   */
  @Override
  public int getMaxTextFieldLength() {
    return 65536;
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
   * @return true if the database JDBC driver supports the setLong command
   */
  @Override
  public boolean supportsSetLong() {
    return false;
  }

  /**
   * @return true if the database supports views
   */
  @Override
  public boolean supportsViews() {
    return false;
  }

  /**
   * @return true if the database supports synonyms
   */
  @Override
  public boolean supportsSynonyms() {
    return false;
  }

  /**
   * @param tableName
   *          The table to be truncated.
   * @return The SQL statement to truncate a table: remove all rows from it without a transaction
   */
  @Override
  public String getTruncateTableStatement( String tableName ) {
    return "DELETE FROM " + tableName;
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
    return "ALTER TABLE " + tablename + " ADD COLUMN " + getFieldDefinition( v, tk, pk, use_autoinc, true, false );
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
      + tablename + " ALTER COLUMN " + getFieldDefinition( v, tk, pk, use_autoinc, true, false );
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
        retval += "DATETIME";
        break;
      // Move back to Y/N for bug - [# 1538] Repository on MS ACCESS: error creating repository
      case ValueMetaInterface.TYPE_BOOLEAN:
        if ( supportsBooleanDataType() ) {
          retval += "BIT";
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
            retval += "COUNTER PRIMARY KEY";
          } else {
            retval += "LONG PRIMARY KEY";
          }
        } else {
          if ( precision == 0 ) {
            if ( length > 9 ) {
              retval += "DOUBLE";
            } else {
              if ( length > 5 ) {
                retval += "LONG";
              } else {
                retval += "INTEGER";
              }
            }
          } else {
            retval += "DOUBLE";
          }
        }
        break;
      case ValueMetaInterface.TYPE_STRING:
        if ( length > 0 ) {
          if ( length < 256 ) {
            retval += "TEXT(" + length + ")";
          } else {
            retval += "MEMO";
          }
        } else {
          retval += "TEXT";
        }
        break;
      case ValueMetaInterface.TYPE_BINARY:
        retval += " LONGBINARY";
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
    return new String[] {
      /*
       * http://support.microsoft.com/kb/q109312 Note that if you set a reference to a type library, an object library,
       * or an ActiveX control, that library's reserved words are also reserved words in your database. For example, if
       * you add an ActiveX control to a form, a reference is set and the names of the objects, methods, and properties
       * of that control become reserved words in your database. For existing objects with names that contain reserved
       * words, you can avoid errors by surrounding the object name with brackets [ ], see
       * getStartQuote(),getEndQuote().
       */
      "ADD", "ALL", "ALPHANUMERIC", "ALTER", "AND", "ANY", "APPLICATION", "AS", "ASC", "ASSISTANT",
      "AUTOINCREMENT", "AVG", "BETWEEN", "BINARY", "BIT", "BOOLEAN", "BY", "BYTE", "CHAR", "CHARACTER",
      "COLUMN", "COMPACTDATABASE", "CONSTRAINT", "CONTAINER", "COUNT", "COUNTER", "CREATE", "CREATEDATABASE",
      "CREATEFIELD", "CREATEGROUP", "CREATEINDEX", "CREATEOBJECT", "CREATEPROPERTY", "CREATERELATION",
      "CREATETABLEDEF", "CREATEUSER", "CREATEWORKSPACE", "CURRENCY", "CURRENTUSER", "DATABASE", "DATE",
      "DATETIME", "DELETE", "DESC", "DESCRIPTION", "DISALLOW", "DISTINCT", "DISTINCTROW", "DOCUMENT", "DOUBLE",
      "DROP", "ECHO", "ELSE", "END", "EQV", "ERROR", "EXISTS", "EXIT", "FALSE", "FIELD", "FIELDS", "FILLCACHE",
      "FLOAT", "FLOAT4", "FLOAT8", "FOREIGN", "FORM", "FORMS", "FROM", "FULL", "FUNCTION", "GENERAL",
      "GETOBJECT", "GETOPTION", "GOTOPAGE", "GROUP", "GUID", "HAVING", "IDLE", "IEEEDOUBLE", "IEEESINGLE", "IF",
      "IGNORE", "IMP", "IN", "INDEX", "INDEX", "INDEXES", "INNER", "INSERT", "INSERTTEXT", "INT", "INTEGER",
      "INTEGER1", "INTEGER2", "INTEGER4", "INTO", "IS", "JOIN", "KEY", "LASTMODIFIED", "LEFT", "LEVEL", "LIKE",
      "LOGICAL", "LOGICAL1", "LONG", "LONGBINARY", "LONGTEXT", "MACRO", "MATCH", "MAX", "MIN", "MOD", "MEMO",
      "MODULE", "MONEY", "MOVE", "NAME", "NEWPASSWORD", "NO", "NOT", "NULL", "NUMBER", "NUMERIC", "OBJECT",
      "OLEOBJECT", "OFF", "ON", "OPENRECORDSET", "OPTION", "OR", "ORDER", "OUTER", "OWNERACCESS", "PARAMETER",
      "PARAMETERS", "PARTIAL", "PERCENT", "PIVOT", "PRIMARY", "PROCEDURE", "PROPERTY", "QUERIES", "QUERY",
      "QUIT", "REAL", "RECALC", "RECORDSET", "REFERENCES", "REFRESH", "REFRESHLINK", "REGISTERDATABASE",
      "RELATION", "REPAINT", "REPAIRDATABASE", "REPORT", "REPORTS", "REQUERY", "RIGHT", "SCREEN", "SECTION",
      "SELECT", "SET", "SETFOCUS", "SETOPTION", "SHORT", "SINGLE", "SMALLINT", "SOME", "SQL", "STDEV", "STDEVP",
      "STRING", "SUM", "TABLE", "TABLEDEF", "TABLEDEFS", "TABLEID", "TEXT", "TIME", "TIMESTAMP", "TOP",
      "TRANSFORM", "TRUE", "TYPE", "UNION", "UNIQUE", "UPDATE", "USER", "VALUE", "VALUES", "VAR", "VARP",
      "VARBINARY", "VARCHAR", "WHERE", "WITH", "WORKSPACE", "XOR", "YEAR", "YES", "YESNO" };
  }

  /**
   * @return The start quote sequence, mostly just double quote, but sometimes [, ...
   */
  @Override
  public String getStartQuote() {
    return "[";
  }

  /**
   * @return The end quote sequence, mostly just double quote, but sometimes ], ...
   */
  @Override
  public String getEndQuote() {
    return "]";
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] {};
  }

  @Override
  public boolean supportsGetBlob() {
    return false;
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

    String tablename = database.getDatabaseMeta().getQuotedSchemaTableCombination( schemaName, tableName );

    boolean[] exists = new boolean[idx_fields.length];
    for ( int i = 0; i < exists.length; i++ ) {
      exists[i] = false;
    }

    try {
      // Get a list of all the indexes for this table
      //
      ResultSet indexList = null;
      try {
        indexList = database.getDatabaseMetaData().getIndexInfo( null, null, tablename, false, true );
        while ( indexList.next() ) {
          // String tablen = indexList.getString("TABLE_NAME");
          // String indexn = indexList.getString("INDEX_NAME");
          String column = indexList.getString( "COLUMN_NAME" );
          // int pos = indexList.getShort("ORDINAL_POSITION");
          // int type = indexList.getShort("TYPE");

          int idx = Const.indexOfString( column, idx_fields );
          if ( idx >= 0 ) {
            exists[idx] = true;
          }
        }
      } finally {
        if ( indexList != null ) {
          indexList.close();
        }
      }

      // See if all the fields are indexed...
      boolean all = true;
      for ( int i = 0; i < exists.length && all; i++ ) {
        if ( !exists[i] ) {
          all = false;
        }
      }

      return all;
    } catch ( Exception e ) {
      throw new KettleDatabaseException( "Unable to determine if indexes exists on table [" + tablename + "]", e );
    }
  }

  /**
   * Get the SQL to insert a new empty unknown record in a dimension.
   *
   * @param schemaTable
   *          the schema-table name to insert into
   * @param keyField
   *          The key field
   * @param versionField
   *          the version field
   * @return the SQL to insert the unknown record into the SCD.
   */
  @Override
  public String getSQLInsertAutoIncUnknownDimensionRow( String schemaTable, String keyField, String versionField ) {
    return "insert into " + schemaTable + "(" + versionField + ") values (1)";
  }

}
