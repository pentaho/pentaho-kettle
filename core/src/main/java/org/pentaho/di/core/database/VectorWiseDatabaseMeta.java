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
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Contains Computer Associates Ingres specific information through static final members
 *
 * @author Matt
 * @since 11-mrt-2005
 */

public class VectorWiseDatabaseMeta extends IngresDatabaseMeta implements DatabaseInterface {

  @Override
  public String getURL( String hostname, String port, String databaseName ) {
    if ( Utils.isEmpty( port ) || "-1".equals( port ) ) {
      return "jdbc:ingres://" + hostname + ":VW7/" + databaseName;
    } else {
      return "jdbc:ingres://" + hostname + ":" + port + "/" + databaseName;
    }
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
    return "ALTER TABLE " + tablename + " ADD COLUMN " + getFieldDefinition( v, tk, pk, useAutoinc, true, false );
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
    return "ALTER TABLE "
      + tablename + " ALTER COLUMN " + getFieldDefinition( v, tk, pk, useAutoinc, true, false );
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
    return "ALTER TABLE " + tablename + " DROP COLUMN " + v.getName() + Const.CR;
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
          if ( useAutoinc ) {
            retval += "GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1";
          } else {
            retval += "BIGINT PRIMARY KEY NOT NULL";
          }
        } else {
          if ( precision == 0 ) {
            // integer numbers
            if ( length > 9 ) {
              retval += "BIGINT";
            } else {
              if ( length == -1 || length > 4 ) { // If the length is undefined or greater than 4, use a standard INTEGER
                retval += "INTEGER";
              } else {
                if ( length > 2 ) {
                  retval += "SMALLINT";
                } else {
                  retval += "INTEGER1";
                }
              }
            }
          } else {
            retval += "FLOAT8";
          }
        }
        break;
      case ValueMetaInterface.TYPE_STRING:
        // Maybe use some default DB String length in case length<=0
        if ( length > 0 ) {
          if ( length > 32000 ) {
            retval += "VARCHAR(32000)";
          } else {
            retval += "VARCHAR(" + length + ")";
          }
        } else {
          retval += "VARCHAR(9999)";
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
   * @param tableName
   *          The table to be truncated.
   * @return The SQL statement to truncate a table: remove all rows from it without a transaction
   */
  @Override
  public String getTruncateTableStatement( String tableName ) {
    return "CALL VECTORWISE( COMBINE '" + tableName + " - " + tableName + "' )";
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] { "iijdbc.jar" };
  }

  @Override
  public boolean supportsGetBlob() {
    return false;
  }

}
