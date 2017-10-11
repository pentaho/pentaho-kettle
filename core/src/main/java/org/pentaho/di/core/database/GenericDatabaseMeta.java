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
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Contains Generic Database Connection information through static final members
 *
 * @author Matt
 * @since 11-mrt-2005
 */

public class GenericDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {
  public static final String ATRRIBUTE_CUSTOM_URL = "CUSTOM_URL";
  public static final String ATRRIBUTE_CUSTOM_DRIVER_CLASS = "CUSTOM_DRIVER_CLASS";

  @Override
  public int[] getAccessTypeList() {
    return new int[] {
      DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_JNDI };
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
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
      String driverClass = getAttributes().getProperty( ATRRIBUTE_CUSTOM_DRIVER_CLASS, "" );
      return driverClass;
    } else {
      return "sun.jdbc.odbc.JdbcOdbcDriver"; // always ODBC!
    }

  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
      String url = getAttributes().getProperty( ATRRIBUTE_CUSTOM_URL, "" );
      return url;
    } else {
      return "jdbc:odbc:" + databaseName;
    }
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
   * @param tableName
   *          The table to be truncated.
   * @return The SQL statement to truncate a table: remove all rows from it without a transaction
   */
  @Override
  public String getTruncateTableStatement( String tableName ) {
    return "DELETE FROM " + tableName;
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
    return "ALTER TABLE " + tablename + " MODIFY " + getFieldDefinition( v, tk, pk, use_autoinc, true, false );
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
          retval += "BIGSERIAL";
        } else {
          if ( length > 0 ) {
            if ( precision > 0 || length > 18 ) {
              retval += "NUMERIC(" + length + ", " + precision + ")";
            } else {
              if ( length > 9 ) {
                retval += "BIGINT";
              } else {
                if ( length < 5 ) {
                  retval += "SMALLINT";
                } else {
                  retval += "INTEGER";
                }
              }
            }

          } else {
            retval += "DOUBLE PRECISION";
          }
        }
        break;
      case ValueMetaInterface.TYPE_STRING:
        if ( length >= DatabaseMeta.CLOB_LENGTH ) {
          retval += "TEXT";
        } else {
          retval += "VARCHAR";
          if ( length > 0 ) {
            retval += "(" + length;
          } else {
            retval += "("; // Maybe use some default DB String length?
          }
          retval += ")";
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

  @Override
  public String[] getUsedLibraries() {
    return new String[] {};
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
