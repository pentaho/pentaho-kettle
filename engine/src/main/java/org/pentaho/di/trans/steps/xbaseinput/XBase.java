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


package org.pentaho.di.trans.steps.xbaseinput;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;

import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;

/**
 * Handles file reading from XBase (DBF) type of files.
 *
 * @author Matt
 * @since 12-08-2004
 *
 */

public class XBase {
  private final LogChannelInterface log;

  private String dbfFile;
  private DBFReader reader;
  private InputStream inputstream;
  private boolean error;

  private static final byte FIELD_TYPE_I = 73;

  public XBase ( LogChannelInterface log, String dbfFile ) {
    this.log = log;
    this.dbfFile = dbfFile;
  }

  public XBase ( LogChannelInterface log, InputStream inputStream ) {
    this.log = log;
    this.inputstream = inputStream;
  }

  public XBase( LogChannelInterface log, String dbfFile, InputStream inputStream ) throws DBFException {
    this.log = log;
    this.dbfFile = dbfFile;
    this.inputstream = inputStream;
    this.reader = new DBFReader( inputStream );
  }

  public void open() throws KettleException {
    try {
      if ( inputstream == null ) {
        inputstream = new FileInputStream( dbfFile );
      }
      reader = new DBFReader( inputstream );
    } catch ( DBFException e ) {
      throw new KettleException( "Error opening DBF metadata", e );
    } catch ( IOException e ) {
      throw new KettleException( "Error reading DBF file", e );
    }
  }

  @SuppressWarnings( "fallthrough" )
  public RowMetaInterface getFields() throws KettleException {
    String debug = "get fields from XBase file";
    RowMetaInterface row = new RowMeta();

    try {
      // Fetch all field information
      //
      debug = "allocate data types";
      int fieldCount = reader.getFieldCount();

      for ( int i = 0; i < fieldCount; i++ ) {
        if ( log.isDebug() ) {
          debug = "get field #" + i;
        }

        DBFField field = reader.getField( i );
        ValueMetaInterface value = null;

        byte datatype = field.getDataType();
        switch ( datatype ) {
          case DBFField.FIELD_TYPE_M: // Memo
            debug = "memo field";
            if ( log.isDebug() ) {
              log.logDebug( "Field #" + i + " is a memo-field! (" + field.getName() + ")" );
            }
          case DBFField.FIELD_TYPE_C: // Character
            // case DBFField.FIELD_TYPE_P: // Picture
            debug = "character field";
            value = new ValueMetaString( field.getName() );
            value.setLength( field.getFieldLength() );
            break;
          case FIELD_TYPE_I: // Integer
          case DBFField.FIELD_TYPE_N: // Numeric
          case DBFField.FIELD_TYPE_F: // Float
            debug = "Number field";
            value = new ValueMetaNumber( field.getName() );
            value.setLength( field.getFieldLength(), field.getDecimalCount() );
            break;
          case DBFField.FIELD_TYPE_L: // Logical
            debug = "Logical field";
            value = new ValueMetaBoolean( field.getName() );
            value.setLength( -1, -1 );
            break;
          case DBFField.FIELD_TYPE_D: // Date
            debug = "Date field";
            value = new ValueMetaDate( field.getName() );
            value.setLength( -1, -1 );
            break;
          default:
            if ( log.isDebug() ) {
              log.logDebug( "Unknown Datatype" + datatype );
            }
        }

        if ( value != null ) {
          row.addValueMeta( value );
        }
      }
    } catch ( Exception e ) {
      throw new KettleException( "Error reading DBF metadata (in part " + debug + ")", e );
    }

    return row;
  }

  public Object[] getRow( RowMetaInterface fields ) throws KettleException {
    return getRow( RowDataUtil.allocateRowData( fields.size() ) );
  }

  public Object[] getRow( Object[] r ) throws KettleException {
    try {
      // Read the next record
      //
      Object[] rowobj = reader.nextRecord();

      // Are we at the end yet?
      //
      if ( rowobj == null ) {
        return null;
      }

      // Set the values in the row...
      //
      int fieldCount = reader.getFieldCount();
      for ( int i = 0; i < fieldCount; i++ ) {
        switch ( reader.getField( i ).getDataType() ) {
          case DBFField.FIELD_TYPE_M: // Memo
            if ( rowobj[i] != null ) {
              r[i] = rowobj[i];
            }
            break;
          case DBFField.FIELD_TYPE_C: // Character
            r[i] = Const.rtrim( (String) rowobj[i] );
            break;
          case FIELD_TYPE_I: // Numeric
            try {
              if ( rowobj[i] != null ) {
                r[i] = ( (Integer) rowobj[i] ).doubleValue();
              }
            } catch ( NumberFormatException e ) {
              throw new KettleException( "Error parsing field #"
                + ( i + 1 ) + " : " + reader.getField( i ).getName(), e );
            }
            break;
          case DBFField.FIELD_TYPE_N: // Numeric
            // Convert to Double!!
            try {
              if ( rowobj[i] != null ) {
                r[i] = rowobj[i];
              }
            } catch ( NumberFormatException e ) {
              throw new KettleException( "Error parsing field #"
                + ( i + 1 ) + " : " + reader.getField( i ).getName(), e );
            }
            break;
          case DBFField.FIELD_TYPE_F: // Float
            // Convert to double!!
            try {
              if ( rowobj[i] != null ) {
                r[i] = new Double( (Float) rowobj[i] );
              }
            } catch ( NumberFormatException e ) {
              throw new KettleException( "Error parsing field #"
                + ( i + 1 ) + " : " + reader.getField( i ).getName(), e );
            }
            break;
          case DBFField.FIELD_TYPE_L: // Logical
            r[i] = rowobj[i];
            break;
          case DBFField.FIELD_TYPE_D: // Date
            r[i] = rowobj[i];
            break;
          /*
           * case DBFField.FIELD_TYPE_P: // Picture v.setValue( (String)rowobj[i] ); // Set to String at first... break;
           */
          default:
            break;
        }
      }
    } catch ( DBFException e ) {
      log.logError( "Unable to read row : " + e.toString() );
      error = true;
      throw new KettleException( "Unable to read row from XBase file", e );
    } catch ( Exception e ) {
      log.logError( "Unexpected error while reading row: " + e.toString() );
      error = true;
      throw new KettleException( "Unable to read row from XBase file", e );
    }

    return r;
  }

  public boolean close() {
    boolean retval = false;
    try {
      if ( inputstream != null ) {
        inputstream.close();
      }

      retval = true;
    } catch ( IOException e ) {
      log.logError( "Couldn't close file [" + dbfFile + "] : " + e.toString() );
      error = true;
    }

    return retval;
  }

  public boolean hasError() {
    return error;
  }

  @Override
  public String toString() {
    if ( dbfFile != null ) {
      return dbfFile;
    } else {
      return getClass().getName();
    }
  }

  /**
   * @return the dbfFile
   */
  public String getDbfFile() {
    return dbfFile;
  }

  /**
   * @param dbfFile
   *          the dbfFile to set
   */
  public void setDbfFile( String dbfFile ) {
    this.dbfFile = dbfFile;
  }

  /**
   * @return the reader
   */
  public DBFReader getReader() {
    return reader;
  }
}
