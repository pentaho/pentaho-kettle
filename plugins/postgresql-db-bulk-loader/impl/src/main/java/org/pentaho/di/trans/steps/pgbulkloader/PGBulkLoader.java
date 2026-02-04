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


package org.pentaho.di.trans.steps.pgbulkloader;

//
// The "designer" notes of the PostgreSQL bulkloader:
// ----------------------------------------------
//
// Let's see how fast we can push data down the tube with the use of COPY FROM STDIN
//
//

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.postgresql.copy.PGCopyOutputStream;

import com.google.common.annotations.VisibleForTesting;

import org.postgresql.PGConnection;

/**
 * Performs a bulk load to a postgres table.
 *
 * Based on (copied from) Sven Boden's Oracle Bulk Loader step
 *
 * @author matt
 * @since 28-mar-2008
 */
public class PGBulkLoader extends BaseStep implements StepInterface {
  private static Class<?> PKG = PGBulkLoaderMeta.class; // for i18n purposes, needed by Translator2!!

  private Charset clientEncoding = Charset.defaultCharset();
  private PGBulkLoaderMeta meta;
  private PGBulkLoaderData data;
  private PGCopyOutputStream pgCopyOut;

  // Runtime-only effective field configuration.
  // IMPORTANT: Do not write these back into meta, otherwise Spoon dialog will show them after execution.
  private transient String[] runtimeStreamFields;
  private transient String[] runtimeTableFields;
  private transient String[] runtimeDateMask;

  public PGBulkLoader( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  private static final class EffectiveFields {
    final String[] streamFields;
    final String[] tableFields;
    final String[] dateMask;

    EffectiveFields( String[] streamFields, String[] tableFields, String[] dateMask ) {
      this.streamFields = streamFields;
      this.tableFields = tableFields;
      this.dateMask = dateMask;
    }
  }

  private EffectiveFields getEffectiveFields( RowMetaInterface inputRowMeta ) throws KettleException {
    // If we already computed runtime fields for this execution, reuse them.
    if ( runtimeStreamFields != null && runtimeStreamFields.length > 0 ) {
      return new EffectiveFields( runtimeStreamFields, runtimeTableFields, runtimeDateMask );
    }

    String[] stream = meta.getFieldStream();
    String[] table = meta.getFieldTable();
    String[] dateMask = meta.getDateMask();

    // If Fields tab is empty, use incoming row metadata (runtime only).
    if ( stream == null || stream.length == 0 ) {
      // Backward compatible behavior: if we don't have input metadata (unit tests / edge cases),
      // do not fail early. Return empty arrays.
      if ( inputRowMeta == null || inputRowMeta.size() <= 0 ) {
        runtimeStreamFields = new String[0];
        runtimeTableFields = new String[0];
        runtimeDateMask = new String[0];
        return new EffectiveFields( runtimeStreamFields, runtimeTableFields, runtimeDateMask );
      }

      int nrFields = inputRowMeta.size();
      stream = new String[nrFields];
      table = new String[nrFields];
      dateMask = new String[nrFields]; // default: PASS_THROUGH

      for ( int i = 0; i < nrFields; i++ ) {
        String fieldName = inputRowMeta.getValueMeta( i ).getName();
        stream[i] = fieldName;
        table[i] = fieldName;
      }
    } else {
      // Normalize arrays locally without mutating meta.
      if ( table == null || table.length != stream.length ) {
        String[] newTable = new String[stream.length];
        if ( table != null ) {
          System.arraycopy( table, 0, newTable, 0, Math.min( table.length, newTable.length ) );
        }
        for ( int i = 0; i < newTable.length; i++ ) {
          if ( Utils.isEmpty( newTable[i] ) ) {
            newTable[i] = stream[i];
          }
        }
        table = newTable;
      }

      if ( dateMask == null || dateMask.length != stream.length ) {
        String[] newDateMask = new String[stream.length];
        if ( dateMask != null ) {
          System.arraycopy( dateMask, 0, newDateMask, 0, Math.min( dateMask.length, newDateMask.length ) );
        }
        dateMask = newDateMask;
      }
    }

    runtimeStreamFields = stream;
    runtimeTableFields = table;
    runtimeDateMask = dateMask;

    return new EffectiveFields( runtimeStreamFields, runtimeTableFields, runtimeDateMask );
  }

  private void ensureDateFormatChoicesInitialized( EffectiveFields fields ) {
    int nrFields = fields.streamFields == null ? 0 : fields.streamFields.length;
    if ( data.dateFormatChoices != null && data.dateFormatChoices.length == nrFields ) {
      return;
    }

    data.dateFormatChoices = new int[nrFields];
    for ( int i = 0; i < nrFields; i++ ) {
      String mask = fields.dateMask != null && fields.dateMask.length > i ? fields.dateMask[i] : null;

      if ( Utils.isEmpty( mask ) ) {
        data.dateFormatChoices[i] = PGBulkLoaderMeta.NR_DATE_MASK_PASS_THROUGH;
      } else if ( mask.equalsIgnoreCase( PGBulkLoaderMeta.DATE_MASK_DATE ) ) {
        data.dateFormatChoices[i] = PGBulkLoaderMeta.NR_DATE_MASK_DATE;
      } else if ( mask.equalsIgnoreCase( PGBulkLoaderMeta.DATE_MASK_DATETIME ) ) {
        data.dateFormatChoices[i] = PGBulkLoaderMeta.NR_DATE_MASK_DATETIME;
      } else {
        data.dateFormatChoices[i] = PGBulkLoaderMeta.NR_DATE_MASK_PASS_THROUGH;
      }
    }
  }

  /**
   * Get the contents of the control file as specified in the meta object
   *
   * @return a string containing the control file contents
   */
  public String getCopyCommand( ) throws KettleException {
    EffectiveFields fields = getEffectiveFields( getInputRowMeta() );

    DatabaseMeta dm = meta.getDatabaseMeta();

    StringBuilder contents = new StringBuilder( 500 );

    String tableName =
      dm.getQuotedSchemaTableCombination(
        environmentSubstitute( meta.getSchemaName() ), environmentSubstitute( meta.getTableName() ) );

    contents.append( "COPY " );
    contents.append( tableName );

    // Names of columns (optional). If none are defined, omit the column list (valid PostgreSQL syntax).
    String[] streamFields = fields.streamFields;
    String[] tableFields = fields.tableFields;

    if ( streamFields != null && streamFields.length > 0 ) {
      contents.append( " ( " );
      for ( int i = 0; i < streamFields.length; i++ ) {
        if ( i != 0 ) {
          contents.append( ", " );
        }
        contents.append( dm.quoteField( tableFields[i] ) );
      }
      contents.append( " ) " );
    } else {
      contents.append( " " );
    }

    contents.append( " FROM STDIN" );

    contents.append( " WITH CSV DELIMITER AS '" ).append( environmentSubstitute( meta.getDelimiter() ) )
      .append( "' QUOTE AS '" ).append( environmentSubstitute( meta.getEnclosure() ) ).append( "'" );
    contents.append( ";" ).append( Const.CR );

    return contents.toString();
  }

  void checkClientEncoding() throws Exception {
    Connection connection = data.db.getConnection();

    Statement statement = connection.createStatement();

    try {
      try ( ResultSet rs = statement.executeQuery( "show client_encoding" ) ) {
        if ( !rs.next() || rs.getMetaData().getColumnCount() != 1 ) {
          logBasic( "Cannot detect client_encoding, using system default encoding" );
          return;
        }

        String clientEncodingStr = rs.getString( 1 );
        logBasic( "Detect client_encoding: " + clientEncodingStr );
        clientEncoding = Charset.forName( clientEncodingStr );
      }
    } catch ( SQLException | IllegalArgumentException ex ) {
      logError( "Cannot detect PostgreSQL client_encoding, using system default encoding", ex );
    } finally {
      statement.close();
    }
  }

  private void do_copy( PGBulkLoaderMeta meta, boolean wait ) throws KettleException {
    data.db = getDatabase( this, meta );
    String copyCmd = getCopyCommand();
    try {
      connect();

      checkClientEncoding();

      processTruncate();

      logBasic( "Launching command: " + copyCmd );
      pgCopyOut = new PGCopyOutputStream( (PGConnection) data.db.getConnection(), copyCmd );

    } catch ( Exception ex ) {
      throw new KettleException( "Error while preparing the COPY " + copyCmd, ex );
    }
  }

  @VisibleForTesting
  Database getDatabase( LoggingObjectInterface parentObject, PGBulkLoaderMeta pgBulkLoaderMeta ) {
    DatabaseMeta dbMeta = pgBulkLoaderMeta.getDatabaseMeta();
    // If dbNameOverride is present, clone the origin db meta and override the DB name
    String dbNameOverride = environmentSubstitute( pgBulkLoaderMeta.getDbNameOverride() );
    if ( !Utils.isEmpty( dbNameOverride ) ) {
      dbMeta = (DatabaseMeta) pgBulkLoaderMeta.getDatabaseMeta().clone();
      dbMeta.setDBName( dbNameOverride.trim() );
      logDebug( "DB name overridden to the value: " + dbNameOverride );
    }
    return new Database( parentObject, dbMeta );
  }

  void connect() throws KettleException {
    if ( getTransMeta().isUsingUniqueConnections() ) {
      synchronized ( getTrans() ) {
        data.db.connect( getTrans().getTransactionId(), getPartitionID() );
      }
    } else {
      data.db.connect( getPartitionID() );
    }
  }

  void processTruncate() throws Exception {
    Connection connection = data.db.getConnection();

    String loadAction = environmentSubstitute( meta.getLoadAction() );

    if ( loadAction.equalsIgnoreCase( "truncate" ) ) {
      DatabaseMeta dm = meta.getDatabaseMeta();
      String tableName =
        dm.getQuotedSchemaTableCombination( environmentSubstitute( meta.getSchemaName() ),
          environmentSubstitute( meta.getTableName() ) );
      logBasic( "Launching command: " + "TRUNCATE " + tableName );

      Statement statement = connection.createStatement();

      try {
        statement.executeUpdate( "TRUNCATE " + tableName );
      } catch ( Exception ex ) {
        throw new KettleException( "Error while truncating " + tableName, ex );
      } finally {
        statement.close();
      }
    }
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (PGBulkLoaderMeta) smi;
    data = (PGBulkLoaderData) sdi;

    try {
      Object[] r = getRow(); // Get row from input rowset & set row busy!

      if ( r == null ) { // no more input to be expected...

        setOutputDone();

        // Close the output stream...
        // will be null if no records (empty stream)
        if ( data != null && pgCopyOut != null ) {
          pgCopyOut.flush();
          pgCopyOut.endCopy();

        }

        return false;
      }

      if ( first ) {
        first = false;

        // At this point, init() has already had a chance to auto-populate
        // meta.getFieldStream()/meta.getFieldTable() from input metadata (if available).
        // We just cache indexes and start COPY.

        EffectiveFields fields = getEffectiveFields( getInputRowMeta() );

        // Cache field indexes.
        //
        data.keynrs = new int[fields.streamFields.length];
        for ( int i = 0; i < data.keynrs.length; i++ ) {
          data.keynrs[i] = getInputRowMeta().indexOfValue( fields.streamFields[i] );
          if ( data.keynrs[i] < 0 ) {
            throw new KettleException( "Field '" + fields.streamFields[i]
                    + "' specified for bulk load not found in input stream" );
          }
        }

        // Initialize date format choices to match the runtime-selected fields.
        ensureDateFormatChoicesInitialized( fields );

        // execute the copy statement... pgCopyOut is setup there
        //
        do_copy( meta, true );

        // Write rows of data hereafter...
        //
      }

      writeRowToPostgres( getInputRowMeta(), r );

      putRow( getInputRowMeta(), r );
      incrementLinesOutput();

      return true;
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "GPBulkLoader.Log.ErrorInStep" ), e );
      setErrors( 1 );
      stopAll();
      setOutputDone(); // signal end to receiver(s)
      return false;
    }
  }

  @VisibleForTesting
  void writeRowToPostgres( RowMetaInterface rowMeta, Object[] r ) throws KettleException {

    try {
      // Backward compatible behavior:
      // - null keynrs is an error (not initialized)
      // - empty keynrs means "no fields": just write a newline (old implementation behavior)
      if ( data.keynrs == null ) {
        throw new KettleException( "No fields defined to load to database" );
      }
      if ( data.keynrs.length == 0 ) {
        pgCopyOut.write( data.newline );
        return;
      }

      // Safety net: if metadata-derived fields are used, ensure choices exist and align.
      if ( data.dateFormatChoices == null || data.dateFormatChoices.length != data.keynrs.length ) {
        ensureDateFormatChoicesInitialized( getEffectiveFields( rowMeta ) );
      }

      for ( int i = 0; i < data.keynrs.length; i++ ) {
        if ( i > 0 ) {
          pgCopyOut.write( data.separator );
        }

        int index = data.keynrs[i];

        // Defensive checks to avoid ArrayIndexOutOfBoundsException when row metadata / row data mismatch.
        if ( rowMeta == null ) {
          throw new KettleException( "Input row metadata is null while writing to PostgreSQL COPY stream" );
        }
        if ( index < 0 || index >= rowMeta.size() ) {
          throw new KettleException(
            "Field index " + index + " is out of bounds for input row metadata (size=" + rowMeta.size() + ")" );
        }
        if ( r == null ) {
          throw new KettleException( "Input row data is null while writing to PostgreSQL COPY stream" );
        }
        if ( index >= r.length ) {
          throw new KettleException(
            "Field index " + index + " is out of bounds for input row data (length=" + r.length + "). "
              + "This indicates a mismatch between input row metadata and the actual row object array." );
        }

        ValueMetaInterface valueMeta = rowMeta.getValueMeta( index );
        Object valueData = r[index];

        if ( valueData != null ) {
          switch ( valueMeta.getType() ) {
            case ValueMetaInterface.TYPE_STRING:
              pgCopyOut.write( data.quote );

              String quoteStr = new String( data.quote );
              String escapedString = valueMeta.getString( valueData ).replace( quoteStr, quoteStr + quoteStr );
              pgCopyOut.write( escapedString.getBytes( clientEncoding ) );

              pgCopyOut.write( data.quote );
              break;
            case ValueMetaInterface.TYPE_INTEGER:
            case ValueMetaInterface.TYPE_BOOLEAN:
              if ( valueMeta.isStorageBinaryString() ) {
                pgCopyOut.write( (byte[]) valueData );
              } else {
                pgCopyOut.write( Long.toString( valueMeta.getInteger( valueData ) ).getBytes( clientEncoding ) );
              }
              break;
            case ValueMetaInterface.TYPE_DATE:
              switch ( data.dateFormatChoices[i] ) {
                case PGBulkLoaderMeta.NR_DATE_MASK_PASS_THROUGH:
                  if ( valueMeta.isStorageBinaryString() ) {
                    pgCopyOut.write( (byte[]) valueData );
                  } else {
                    String dateString = valueMeta.getString( valueData );
                    if ( dateString != null ) {
                      pgCopyOut.write( dateString.getBytes( clientEncoding ) );
                    }
                  }
                  break;
                case PGBulkLoaderMeta.NR_DATE_MASK_DATE:
                  String dateString = data.dateMeta.getString( valueMeta.getDate( valueData ) );
                  if ( dateString != null ) {
                    pgCopyOut.write( dateString.getBytes( clientEncoding ) );
                  }
                  break;
                case PGBulkLoaderMeta.NR_DATE_MASK_DATETIME:
                  String dateTimeString = data.dateTimeMeta.getString( valueMeta.getDate( valueData ) );
                  if ( dateTimeString != null ) {
                    pgCopyOut.write( dateTimeString.getBytes( clientEncoding ) );
                  }
                  break;
                default:
                  throw new KettleException(
                    "PGBulkLoader doesn't know how to handle date (neither passthrough, nor date or datetime for field "
                      + valueMeta.getName() );
              }
              break;
            case ValueMetaInterface.TYPE_TIMESTAMP:
              switch ( data.dateFormatChoices[i] ) {
                case PGBulkLoaderMeta.NR_DATE_MASK_PASS_THROUGH:
                  if ( valueMeta.isStorageBinaryString() ) {
                    pgCopyOut.write( (byte[]) valueData );
                  } else {
                    String tsString = valueMeta.getString( valueData );
                    if ( tsString != null ) {
                      pgCopyOut.write( tsString.getBytes( clientEncoding ) );
                    }
                  }
                  break;
                case PGBulkLoaderMeta.NR_DATE_MASK_DATE:
                  String tsDateString = data.dateMeta.getString( valueMeta.getDate( valueData ) );
                  if ( tsDateString != null ) {
                    pgCopyOut.write( tsDateString.getBytes( clientEncoding ) );
                  }
                  break;
                case PGBulkLoaderMeta.NR_DATE_MASK_DATETIME:
                  String tsDateTimeString = data.dateTimeMeta.getString( valueMeta.getDate( valueData ) );
                  if ( tsDateTimeString != null ) {
                    pgCopyOut.write( tsDateTimeString.getBytes( clientEncoding ) );
                  }
                  break;
                default:
                  throw new KettleException(
                    "PGBulkLoader doesn't know how to handle timestamp (neither passthrough, nor date or datetime for field "
                      + valueMeta.getName() );
              }
              break;
            case ValueMetaInterface.TYPE_NUMBER:
              if ( valueMeta.isStorageBinaryString() ) {
                pgCopyOut.write( (byte[]) valueData );
              } else {
                pgCopyOut.write( Double.toString( valueMeta.getNumber( valueData ) ).getBytes( clientEncoding ) );
              }
              break;
            case ValueMetaInterface.TYPE_BIGNUMBER:
              if ( valueMeta.isStorageBinaryString() ) {
                pgCopyOut.write( (byte[]) valueData );
              } else {
                BigDecimal big = valueMeta.getBigNumber( valueData );
                if ( big != null ) {
                  pgCopyOut.write( big.toString().getBytes( clientEncoding ) );
                }
              }
              break;
            default:
              throw new KettleException( "PGBulkLoader doesn't handle the type " + valueMeta.getTypeDesc() );
          }
        }
      }

      pgCopyOut.write( data.newline );
    } catch ( Exception e ) {
      throw new KettleException( "Error serializing rows of data to the COPY command", e );
    }

  }

  protected void verifyDatabaseConnection() throws KettleException {
    // Confirming Database Connection is defined.
    if ( meta.getDatabaseMeta() == null ) {
      throw new KettleException( BaseMessages.getString( PKG, "PGBulkLoaderMeta.GetSQL.NoConnectionDefined" ) );
    }
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (PGBulkLoaderMeta) smi;
    data = (PGBulkLoaderData) sdi;

    String enclosure = environmentSubstitute( meta.getEnclosure() );
    String separator = environmentSubstitute( meta.getDelimiter() );

    if ( super.init( smi, sdi ) ) {

      // Confirming Database Connection is defined.
      try {
        verifyDatabaseConnection();
      } catch ( KettleException ex ) {
        logError( ex.getMessage() );
        return false;
      }

      if ( enclosure != null ) {
        data.quote = enclosure.getBytes();
      } else {
        data.quote = new byte[] {};
      }
      if ( separator != null ) {
        data.separator = separator.getBytes();
      } else {
        data.separator = new byte[] {};
      }
      data.newline = Const.CR.getBytes();

      // Do not initialize dateFormatChoices here when Fields tab is empty.
      // We build runtime-only fields from incoming metadata on the first row.
      data.dateFormatChoices = null;
      return true;
    }
    return false;
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (PGBulkLoaderMeta) smi;
    data = (PGBulkLoaderData) sdi;

    try {
      if ( pgCopyOut != null ) {
        pgCopyOut.close();
      }
    } catch ( IOException e ) {
      logError( "Error while closing the Postgres Output Stream", e.getMessage() );
    }

    if ( data.db != null ) {
      data.db.close();
    }
    super.dispose( smi, sdi );
  }

}
