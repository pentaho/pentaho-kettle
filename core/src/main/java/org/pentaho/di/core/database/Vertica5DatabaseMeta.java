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
import java.sql.SQLException;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Vertica Analytic Database version 5 and later (changed driver class name)
 *
 * @author DEinspanjer
 * @since 2009-03-16
 * @author Matt
 * @since May-2008
 * @author Jens
 * @since Aug-2012
 */

public class Vertica5DatabaseMeta extends VerticaDatabaseMeta {
  @Override
  public String getDriverClass() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
      return "com.vertica.jdbc.Driver";
    } else {
      return "sun.jdbc.odbc.JdbcOdbcDriver"; // always ODBC!
    }

  }

  /**
   * @return false as the database does not support timestamp to date conversion.
   */
  @Override
  public boolean supportsTimeStampToDateConversion() {
    return false;
  }

  /**
   * This method allows a database dialect to convert database specific data types to Kettle data types.
   *
   * @param rs
   *          The result set to use
   * @param val
   *          The description of the value to retrieve
   * @param index
   *          the index on which we need to retrieve the value, 0-based.
   * @return The correctly converted Kettle data type corresponding to the valueMeta description.
   * @throws KettleDatabaseException
   */
  @Override
  public Object getValueFromResultSet( ResultSet rs, ValueMetaInterface val, int index ) throws KettleDatabaseException {
    Object data;

    try {
      switch ( val.getType() ) {
        case ValueMetaInterface.TYPE_TIMESTAMP:
        case ValueMetaInterface.TYPE_DATE:
          if ( val.getOriginalColumnType() == java.sql.Types.TIMESTAMP ) {
            data = rs.getTimestamp( index + 1 );
            break; // Timestamp extends java.util.Date
          } else if ( val.getOriginalColumnType() == java.sql.Types.TIME ) {
            data = rs.getTime( index + 1 );
            break;
          } else {
            data = rs.getDate( index + 1 );
            break;
          }
        default:
          return super.getValueFromResultSet( rs, val, index );
      }
      if ( rs.wasNull() ) {
        data = null;
      }
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( "Unable to get value '"
        + val.toStringMeta() + "' from database resultset, index " + index, e );
    }

    return data;
  }
}
