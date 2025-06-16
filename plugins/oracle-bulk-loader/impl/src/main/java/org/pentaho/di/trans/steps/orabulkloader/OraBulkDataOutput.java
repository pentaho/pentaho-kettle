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


package org.pentaho.di.trans.steps.orabulkloader;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;

import com.google.common.annotations.VisibleForTesting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.vfs2.FileObject;

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
  private String recTerm;

  public OraBulkDataOutput( OraBulkLoaderMeta meta, String recTerm ) {
    this.meta = meta;
    this.recTerm = recTerm;
  }

  public void open( Bowl bowl, VariableSpace space, Process sqlldrProcess ) throws KettleException {
    String loadMethod = meta.getLoadMethod();
    try {
      OutputStream os;

      if ( OraBulkLoaderMeta.METHOD_AUTO_CONCURRENT.equals( loadMethod ) ) {
        os = sqlldrProcess.getOutputStream();
      } else {
        // Else open the data file filled in.
        String dataFilePath = getFilename( getFileObject( bowl, space.environmentSubstitute( meta.getDataFile() ), space ) );
        File dataFile = new File( dataFilePath );
        // Make sure the parent directory exists
        dataFile.getParentFile().mkdirs();
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

  @SuppressWarnings( "ArrayToString" )
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
        outbuf.append( ',' );
      }
      number = fieldNumbers[i];
      v = mi.getValueMeta( number );
      if ( row[number] == null ) {
        // TODO (SB): special check for null in case of Strings.
        outbuf.append( enclosure );
        outbuf.append( enclosure );
      } else {
        switch ( v.getType() ) {
          case ValueMetaInterface.TYPE_STRING:
            String s = mi.getString( row, number );
            outbuf.append( enclosure );
            if ( null != s ) {
              if ( s.contains( enclosure ) ) {
                s = createEscapedString( s, enclosure );
              }
              outbuf.append( s );
            }
            outbuf.append( enclosure );
            break;
          case ValueMetaInterface.TYPE_INTEGER:
            Long l = mi.getInteger( row, number );
            outbuf.append( enclosure );
            if ( null != l ) {
              outbuf.append( l );
            }
            outbuf.append( enclosure );
            break;
          case ValueMetaInterface.TYPE_NUMBER:
            Double d = mi.getNumber( row, number );
            outbuf.append( enclosure );
            if ( null != d ) {
              outbuf.append( d );
            }
            outbuf.append( enclosure );
            break;
          case ValueMetaInterface.TYPE_BIGNUMBER:
            BigDecimal bd = mi.getBigNumber( row, number );
            outbuf.append( enclosure );
            if ( null != bd ) {
              outbuf.append( bd );
            }
            outbuf.append( enclosure );
            break;
          case ValueMetaInterface.TYPE_DATE:
            Date dt = mi.getDate( row, number );
            outbuf.append( enclosure );
            if ( null != dt ) {
              String mask = meta.getDateMask()[i];
              if ( OraBulkLoaderMeta.DATE_MASK_DATETIME.equals( mask ) ) {
                outbuf.append( sdfDateTime.format( dt ) );
              } else {
                // Default is date format
                outbuf.append( sdfDate.format( dt ) );
              }
            }
            outbuf.append( enclosure );
            break;
          case ValueMetaInterface.TYPE_BOOLEAN:
            Boolean b = mi.getBoolean( row, number );
            outbuf.append( enclosure );
            if ( null != b ) {
              if ( b ) {
                outbuf.append( 'Y' );
              } else {
                outbuf.append( 'N' );
              }
            }
            outbuf.append( enclosure );
            break;
          case ValueMetaInterface.TYPE_BINARY:
            byte[] byt = mi.getBinary( row, number );
            outbuf.append( "<startlob>" );
            // TODO REVIEW - implicit .toString
            outbuf.append( byt );
            outbuf.append( "<endlob>" );
            break;
          case ValueMetaInterface.TYPE_TIMESTAMP:
            Timestamp timestamp = (Timestamp) mi.getDate( row, number );
            outbuf.append( enclosure );
            if ( null != timestamp ) {
              outbuf.append( timestamp.toString() );
            }
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

  @VisibleForTesting
  String getFilename( FileObject fileObject ) {
    return KettleVFS.getFilename( fileObject );
  }

  @VisibleForTesting
  FileObject getFileObject( Bowl bowl, String vfsFilename, VariableSpace space ) throws KettleFileException {
    return KettleVFS.getInstance( bowl ).getFileObject( vfsFilename, space );
  }
}
