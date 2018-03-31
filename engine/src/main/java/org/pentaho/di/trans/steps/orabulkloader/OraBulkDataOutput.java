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

package org.pentaho.di.trans.steps.orabulkloader;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Does the opening of the output "stream". It's either a file or inter process communication which is transparant to
 * users of this class.
 *
 * @author Sven Boden
 * @since 20-feb-2007
 */
public class OraBulkDataOutput {
  private OraBulkLoaderMeta meta;
  private Writer output = null;
  private StringBuilder outbuf = null;
  private boolean first = true;
  private int[] fieldNumbers = null;
  private String enclosure = null;
  private SimpleDateFormat sdfDate = null;
  private SimpleDateFormat sdfDateTime = null;
  private String recTerm = null;

  public OraBulkDataOutput( OraBulkLoaderMeta meta, String recTerm ) {
    this.meta = meta;
    this.recTerm = recTerm;
  }

  public void open( VariableSpace space, Process sqlldrProcess ) throws KettleException {
    String loadMethod = meta.getLoadMethod();
    try {
      OutputStream os;

      if ( OraBulkLoaderMeta.METHOD_AUTO_CONCURRENT.equals( loadMethod ) ) {
        os = sqlldrProcess.getOutputStream();
      } else {
        // Else open the data file filled in.
        String dataFile = meta.getDataFile();
        dataFile = space.environmentSubstitute( dataFile );

        os = new FileOutputStream( dataFile, false );
      }

      String encoding = meta.getEncoding();
      if ( Utils.isEmpty( encoding ) ) {
        // Use the default encoding.
        output = new BufferedWriter( new OutputStreamWriter( os ) );
      } else {
        // Use the specified encoding
        output = new BufferedWriter( new OutputStreamWriter( os, encoding ) );
      }
    } catch ( IOException e ) {
      throw new KettleException( "IO exception occured: " + e.getMessage(), e );
    }
  }

  public void close() throws IOException {
    if ( output != null ) {
      output.close();
    }
  }

  Writer getOutput() {
    return output;
  }

  private String createEscapedString( String orig, String enclosure ) {
    StringBuilder buf = new StringBuilder( orig );

    Const.repl( buf, enclosure, enclosure + enclosure );
    return buf.toString();
  }

  public void writeLine( RowMetaInterface mi, Object[] row ) throws KettleException {
    if ( first ) {
      first = false;

      enclosure = meta.getEnclosure();

      // Setup up the fields we need to take for each of the rows
      // as this speeds up processing.
      fieldNumbers = new int[meta.getFieldStream().length];
      for ( int i = 0; i < fieldNumbers.length; i++ ) {
        fieldNumbers[i] = mi.indexOfValue( meta.getFieldStream()[i] );
        if ( fieldNumbers[i] < 0 ) {
          throw new KettleException( "Could not find field " + meta.getFieldStream()[i] + " in stream" );
        }
      }

      sdfDate = new SimpleDateFormat( "yyyy-MM-dd" );
      sdfDateTime = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );

      outbuf = new StringBuilder();
    }
    outbuf.setLength( 0 );

    // Write the data to the output
    ValueMetaInterface v;
    int number;
    for ( int i = 0; i < fieldNumbers.length; i++ ) {
      if ( i != 0 ) {
        outbuf.append( "," );
      }
      v = mi.getValueMeta( i );
      number = fieldNumbers[i];
      if ( row[number] == null ) {
        // TODO (SB): special check for null in case of Strings.
        outbuf.append( enclosure );
        outbuf.append( enclosure );
      } else {
        switch ( v.getType() ) {
          case ValueMetaInterface.TYPE_STRING:
            String s = mi.getString( row, number );
            if ( s.contains( enclosure ) ) {
              s = createEscapedString( s, enclosure );
            }
            outbuf.append( enclosure );
            outbuf.append( s );
            outbuf.append( enclosure );
            break;
          case ValueMetaInterface.TYPE_INTEGER:
            Long l = mi.getInteger( row, number );
            outbuf.append( enclosure );
            outbuf.append( l );
            outbuf.append( enclosure );
            break;
          case ValueMetaInterface.TYPE_NUMBER:
            Double d = mi.getNumber( row, number );
            outbuf.append( enclosure );
            outbuf.append( d );
            outbuf.append( enclosure );
            break;
          case ValueMetaInterface.TYPE_BIGNUMBER:
            BigDecimal bd = mi.getBigNumber( row, number );
            outbuf.append( enclosure );
            outbuf.append( bd );
            outbuf.append( enclosure );
            break;
          case ValueMetaInterface.TYPE_DATE:
            Date dt = mi.getDate( row, number );
            outbuf.append( enclosure );
            String mask = meta.getDateMask()[i];
            if ( OraBulkLoaderMeta.DATE_MASK_DATETIME.equals( mask ) ) {
              outbuf.append( sdfDateTime.format( dt ) );
            } else {
              // Default is date format
              outbuf.append( sdfDate.format( dt ) );
            }
            outbuf.append( enclosure );
            break;
          case ValueMetaInterface.TYPE_BOOLEAN:
            Boolean b = mi.getBoolean( row, number );
            outbuf.append( enclosure );
            if ( b ) {
              outbuf.append( "Y" );
            } else {
              outbuf.append( "N" );
            }
            outbuf.append( enclosure );
            break;
          case ValueMetaInterface.TYPE_BINARY:
            byte[] byt = mi.getBinary( row, number );
            outbuf.append( "<startlob>" );
            outbuf.append( byt );
            outbuf.append( "<endlob>" );
            break;
          case ValueMetaInterface.TYPE_TIMESTAMP:
            Timestamp timestamp = (Timestamp) mi.getDate( row, number );
            outbuf.append( enclosure );
            outbuf.append( timestamp.toString() );
            outbuf.append( enclosure );
            break;
          default:
            throw new KettleException( "Unsupported type" );
        }
      }
    }
    outbuf.append( recTerm );
    try {
      output.append( outbuf );
    } catch ( IOException e ) {
      throw new KettleException( "IO exception occured: " + e.getMessage(), e );
    }
  }
}
