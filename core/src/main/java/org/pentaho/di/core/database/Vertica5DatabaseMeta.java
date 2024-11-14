/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
