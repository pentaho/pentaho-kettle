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
 * Contains Hypersonic specific information through static final members
 *
 * @author Matt
 * @since 11-mrt-2005
 */

public class H2DatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {
  @Override
  public int[] getAccessTypeList() {
    return new int[] {
      DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  /**
   * @see DatabaseInterface#getNotFoundTK(boolean)
   */
  @Override
  public int getNotFoundTK( boolean useAutoinc ) {
    return super.getNotFoundTK( useAutoinc );
  }

  @Override
  public String getDriverClass() {
    return "org.h2.Driver";
  }

  @Override
  public int getDefaultDatabasePort() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
      return 8082;
    }
    return -1;
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) {
    // If the database is an in-memory DB or if there is no valid port and hostname, go embedded
    //
    if ( ( databaseName != null && databaseName.startsWith( "mem:" ) )
      || ( ( Utils.isEmpty( port ) || "-1".equals( port ) ) && Utils.isEmpty( hostname ) ) ) {
      return "jdbc:h2:" + databaseName;
    } else {
      // Connect over TCP/IP
      //
      return "jdbc:h2:tcp://" + hostname + ":" + port + "/" + databaseName;
    }
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
   * @see DatabaseInterface#getSchemaTableCombination(java.lang.String, java.lang.String)
   */
  @Override
  @SuppressWarnings( "deprecation" )
  public String getSchemaTableCombination( String schemaName, String tablePart ) {
    return getBackwardsCompatibleSchemaTableCombination( schemaName, tablePart );
  }

  /**
   * @return true if the database supports bitmap indexes
   */
  @Override
  public boolean supportsBitmapIndex() {
    return false;
  }

  @Override
  public boolean supportsAutoInc() {
    return true;
  }

  @Override
  public boolean supportsGetBlob() {
    return true;
  }

  @Override
  public boolean supportsSetCharacterStream() {
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
    return "ALTER TABLE " + tablename + " ADD " + getFieldDefinition( v, tk, pk, useAutoinc, true, false );
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
    return "ALTER TABLE " + tablename + " ALTER " + getFieldDefinition( v, tk, pk, useAutoinc, true, false );
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
          retval += "IDENTITY";
        } else {
          if ( length > 0 ) {
            if ( precision > 0 || length > 18 ) {
              retval += "DECIMAL(" + length + ", " + precision + ")";
            } else {
              if ( length > 9 ) {
                retval += "BIGINT";
              } else {
                if ( length < 5 ) {
                  if ( length < 3 ) {
                    retval += "TINYINT";
                  } else {
                    retval += "SMALLINT";
                  }
                } else {
                  retval += "INTEGER";
                }
              }
            }

          } else {
            retval += "DOUBLE";
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
            // http://www.h2database.com/html/datatypes.html#varchar_type
            retval += "(" + Integer.MAX_VALUE;
          }
          retval += ")";
        }
        break;
      case ValueMetaInterface.TYPE_BINARY:
        retval += "BLOB";
        break;
      default:
        retval += "UNKNOWN";
        break;
    }

    if ( addCr ) {
      retval += Const.CR;
    }

    return retval;
  }

  @Override
  public String[] getReservedWords() {
    return new String[] {
      "CURRENT_TIMESTAMP", "CURRENT_TIME", "CURRENT_DATE", "CROSS", "DISTINCT", "EXCEPT", "EXISTS", "FROM",
      "FOR", "FALSE", "FULL", "GROUP", "HAVING", "INNER", "INTERSECT", "IS", "JOIN", "LIKE", "MINUS", "NATURAL",
      "NOT", "NULL", "ON", "ORDER", "PRIMARY", "ROWNUM", "SELECT", "SYSDATE", "SYSTIME", "SYSTIMESTAMP",
      "TODAY", "TRUE", "UNION", "WHERE", };
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] { "h2.jar" };
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

}
