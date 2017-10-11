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

package org.pentaho.di.trans.steps.monetdbbulkloader;

import java.util.Date;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.MonetDBDatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.monetdbagilemart.MonetDBRowLimitException;
import org.pentaho.di.trans.steps.tableagilemart.AgileMartUtil;

import nl.cwi.monetdb.mcl.io.BufferedMCLReader;
import nl.cwi.monetdb.mcl.io.BufferedMCLWriter;
import nl.cwi.monetdb.mcl.net.MapiSocket;

/**
 * Performs a bulk load to a MonetDB table.
 * <p/>
 * Based on (copied from) Sven Boden's Oracle Bulk Loader step
 *
 * @author matt
 * @since 22-aug-2008
 */
public class MonetDBBulkLoader extends BaseStep implements StepInterface {
  private static Class<?> PKG = MonetDBBulkLoaderMeta.class; // for i18n purposes, needed by Translator2!!

  private MonetDBBulkLoaderMeta meta;
  private MonetDBBulkLoaderData data;
  private String message;
  private TransMeta localTransMeta;
  protected long rowsWritten = -1;
  private AgileMartUtil util = new AgileMartUtil();
  private RowMetaInterface physicalTableRowMeta;

  public String getMessage() {
    return message;
  }

  public MonetDBBulkLoader( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
      TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
    localTransMeta = transMeta;
  }

  protected void setMessage( String message ) {
    this.message = message;
  }

  protected MonetDBBulkLoaderMeta getMeta() {
    return meta;
  }

  protected String escapeOsPath( String path, boolean isWindows ) {

    StringBuilder sb = new StringBuilder();

    // should be done with a regex
    for ( int i = 0; i < path.length(); i++ ) {
      char c = path.charAt( i );
      if ( c == ' ' ) {
        sb.append( isWindows ? "^ " : "\\ " );
      } else {
        sb.append( c );
      }
    }
    return sb.toString();
  }

  public boolean execute( MonetDBBulkLoaderMeta meta, boolean wait ) throws KettleException {
    if ( log.isDetailed() ) {
      logDetailed( "Started execute" );
    }

    try {

      if ( log.isDetailed() ) {
        logDetailed( "Auto String Length flag: " + meta.isAutoStringWidths() );
      }

      DatabaseMeta dm = meta.getDatabaseMeta();

      String user = environmentSubstitute( Const.NVL( dm.getUsername(), "" ) );
      String password = Utils.resolvePassword( variables, Const.NVL( dm.getPassword(), "" ) );

      MapiSocket mserver = getMonetDBConnection();
      data.mserver = mserver;

      data.in = mserver.getReader();
      data.out = mserver.getWriter();

      String error = data.in.waitForPrompt();
      if ( error != null ) {
        throw new KettleException( "Error while connecting to MonetDB for bulk loading : " + error );
      }

      data.outputLogger = new StreamLogger( log, mserver.getInputStream(), "OUTPUT" );

      // If the truncate table checkbox is checked, we can do the truncate here.
      if ( meta.isTruncate() ) {
        truncate();
      }

      Database db = null;
      // get table metadata, will be used later for date type identification (DATE, TIMESTAMP, ...)
      try {

        db = new Database( meta.getParent(), dm );
        db.connect( user, password );
        physicalTableRowMeta = db.getTableFields( data.schemaTable );
      } catch ( Exception e ) {
        // try again, with the unquoted table...
        try {
          physicalTableRowMeta = db.getTableFields( meta.getTableName() );
        } catch ( Exception e1 ) {
          logBasic( "Could not get metadata for the physical table " + data.schemaTable + "." );
        }
      } finally {
        if ( db != null ) {
          db.disconnect();
        }
      }
      meta.setCompatibilityDbVersionMode();
    } catch ( Exception ex ) {
      throw new KettleException( ex );
    }

    return true;
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (MonetDBBulkLoaderMeta) smi;
    data = (MonetDBBulkLoaderData) sdi;

    try {
      Object[] r = getRow(); // Get row from input rowset & set row busy!
      if ( r == null ) { // no more input to be expected...

        setOutputDone();
        if ( !first ) {
          try {
            writeBufferToMonetDB();
            data.out.flush();
          } catch ( KettleException ke ) {
            throw ke;
          } finally {
            data.mserver.close();
          }
          util.updateMetadata( meta, rowsWritten );
        }
        return false;
      }

      if ( first ) {
        first = false;

        // Cache field indexes.
        //
        data.keynrs = new int[meta.getFieldStream().length];
        for ( int i = 0; i < data.keynrs.length; i++ ) {
          data.keynrs[i] = getInputRowMeta().indexOfValue( meta.getFieldStream()[i] );
        }

        // execute the psql statement...
        //
        execute( meta, true );
      }

      writeRowToMonetDB( getInputRowMeta(), r );
      putRow( getInputRowMeta(), r );
      incrementLinesOutput();

      return true;
    } catch ( MonetDBRowLimitException me ) {
      // we need to stop processing and clean up
      logDebug( me.getMessage() );
      stopAll();
      setOutputDone();
      return true;
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "MonetDBBulkLoader.Log.ErrorInStep" ), e );
      setErrors( 1 );
      stopAll();
      setOutputDone(); // signal end to receiver(s)
      return false;
    }
  }

  protected void writeRowToMonetDB( RowMetaInterface rowMeta, Object[] r ) throws KettleException {
    if ( data.bufferIndex == data.bufferSize || log.isDebug() ) {
      writeBufferToMonetDB();
    }
    addRowToBuffer( rowMeta, r );
  }

  protected void addRowToBuffer( RowMetaInterface rowMeta, Object[] r ) throws KettleException {

    StringBuilder line = new StringBuilder();

    try {
      for ( int i = 0; i < data.keynrs.length; i++ ) {
        if ( i > 0 ) {
          line.append( data.separator );
        }

        int index = data.keynrs[i];
        ValueMetaInterface valueMeta = rowMeta.getValueMeta( index );
        Object valueData = r[index];
        String nullRep = new String( data.nullrepresentation );
        if ( valueData != null ) {
          switch ( valueMeta.getType() ) {
            case ValueMetaInterface.TYPE_STRING:
              String str = valueMeta.getString( valueData );
              if ( str == null || str.equals( nullRep ) ) {
                // don't quote our null representation
                line.append( str );
                break;
              }
              line.append( data.quote );

              // escape any backslashes
              //
              str = str.replace( "\\", "\\\\" );
              str = str.replace( "\"", "\\\"" );
              if ( meta.isAutoStringWidths() ) {
                int len = valueMeta.getLength();
                if ( len < 1 ) {
                  len = MonetDBDatabaseMeta.DEFAULT_VARCHAR_LENGTH;
                }
                if ( str.length() > len ) {
                  // TODO log this event
                  str = str.substring( 0, len );
                }
                line.append( str );
              } else {
                line.append( str );
              }

              line.append( data.quote );
              break;
            case ValueMetaInterface.TYPE_INTEGER:
              if ( valueMeta.isStorageBinaryString() && meta.getFieldFormatOk()[i] ) {
                line.append( valueMeta.getString( valueData ) );
              } else {
                Long value = valueMeta.getInteger( valueData );
                if ( value == null ) {
                  line.append( data.nullrepresentation );
                } else {
                  line.append( Long.toString( value ) );
                }
              }
              break;
            //
            // TODO: Check MonetDB API for true column types and help set or suggest the correct formatter pattern to
            // the user.
            //
            case ValueMetaInterface.TYPE_TIMESTAMP:
            case ValueMetaInterface.TYPE_DATE:
              // Keep the data format as indicated.
              if ( valueMeta.isStorageBinaryString() && meta.getFieldFormatOk()[i] ) {
                line.append( valueMeta.getString( valueData ) );
              } else {

                ValueMetaInterface colMeta = null;
                if ( physicalTableRowMeta != null ) {
                  colMeta = physicalTableRowMeta.getValueMeta( index );
                }

                Date value = valueMeta.getDate( valueData );
                if ( value == null ) {
                  line.append( data.nullrepresentation );
                } else {

                  // MonetDB makes a distinction between the acceptable incoming string formats for
                  // the type DATE and TIMESTAMP.
                  //
                  // DATE - for date values (e.g., 2012-12-21)
                  // TIME - for time values (e.g., 15:51:36)
                  // TIMESTAMP - DATE and TIME put together (e.g., 2012-12-21 15:51:36)

                  if ( colMeta != null && colMeta.getOriginalColumnTypeName().equalsIgnoreCase( "date" ) ) {
                    line.append( data.monetDateMeta.getString( value ) );
                  } else if ( colMeta != null && colMeta.getOriginalColumnTypeName().equalsIgnoreCase( "time" ) ) {
                    line.append( data.monetTimeMeta.getString( value ) );
                  } else {
                    // colMeta.getOriginalColumnTypeName().equalsIgnoreCase("timestamp")
                    line.append( data.monetTimestampMeta.getString( value ) );
                  }
                }
              }
              break;
            case ValueMetaInterface.TYPE_BOOLEAN:
              Boolean value = valueMeta.getBoolean( valueData );
              if ( value == null ) {
                line.append( data.nullrepresentation );
              } else {
                if ( value.booleanValue() ) {
                  line.append( true );
                } else {
                  line.append( false );
                }
              }
              break;

            case ValueMetaInterface.TYPE_NUMBER:
              if ( valueMeta.isStorageBinaryString() && meta.getFieldFormatOk()[i] ) {
                line.append( valueMeta.getString( valueData ) );
              } else {
                Double dbl = valueMeta.getNumber( valueData );
                if ( dbl == null ) {
                  line.append( data.nullrepresentation );
                } else {
                  line.append( Double.toString( dbl ) );
                }
              }
              break;

            case ValueMetaInterface.TYPE_BIGNUMBER:
              if ( valueMeta.isStorageBinaryString() && meta.getFieldFormatOk()[i] ) {
                line.append( valueMeta.getString( valueData ) );
              } else {
                String string = valueMeta.getString( valueData );
                if ( string == null ) {
                  line.append( data.nullrepresentation );
                } else {
                  line.append( string );
                }
              }
              break;
            default:
              break;
          }
        } else {
          line.append( data.nullrepresentation );
        }
      }

      // finally write a newline
      //
      line.append( data.newline );

      // Now that we have the line, grab the content and store it in the buffer...
      //
      data.rowBuffer[data.bufferIndex] = line.toString(); // line.toByteArray();
      data.bufferIndex++;
    } catch ( Exception e ) {
      throw new KettleException( "Error serializing rows of data to the MonetDB API (MAPI).", e );
    }

  }

  public void truncate() throws KettleException {
    String cmd;
    String table = data.schemaTable;
    String truncateStatement = meta.getDatabaseMeta().getTruncateTableStatement( null, table );
    if ( truncateStatement == null ) {
      throw new KettleException( "Truncate table is not supported!" );
    }
    cmd = truncateStatement + ";";

    try {
      executeSql( cmd );
    } catch ( Exception e ) {
      throw new KettleException( "Error while truncating table " + table, e );
    }

    // try to update the metadata registry
    util.updateMetadata( meta, -1 );
    if ( log.isDetailed() ) {
      logDetailed( "Successfull: " + cmd );
    }

  }

  public void drop() throws KettleException {
    try {
      executeSql( "drop table " + data.schemaTable );
    } catch ( Exception e ) {
      throw new KettleException( "Error while dropping table " + data.schemaTable, e );
    }

  }

  public void autoAdjustSchema( MonetDBBulkLoaderMeta meta ) throws KettleException {

    if ( log.isDetailed() ) {
      logDetailed( "Attempting to auto adjust table structure" );
    }

    drop();

    if ( log.isDetailed() ) {
      logDetailed( "getTransMeta: " + getTransMeta() );
    }
    if ( log.isDetailed() ) {
      logDetailed( "getStepname: " + getStepname() );
    }
    SQLStatement statement = meta.getTableDdl( getTransMeta(), getStepname(), true, data, true );
    if ( log.isDetailed() ) {
      logDetailed( "Statement: " + statement );
    }
    if ( log.isDetailed() && statement != null ) {
      logDetailed( "Statement has SQL: " + statement.hasSQL() );
    }

    if ( statement != null && statement.hasSQL() ) {
      String cmd = statement.getSQL();
      try {
        executeSql( cmd );
      } catch ( Exception e ) {
        throw new KettleException( "Error while creating table " + data.schemaTable, e );
      }
    }

    if ( log.isDetailed() ) {
      logDetailed( "Successfull" );
    }
  }

  protected void writeBufferToMonetDB() throws KettleException {
    if ( data.bufferIndex == 0 ) {
      return;
    }

    try {
      StringBuilder cmdBuff = new StringBuilder();

      // first write the COPY INTO command...
      //

      String nullRep = environmentSubstitute( meta.getNULLrepresentation() );
      if ( nullRep == null ) {
        nullRep = new String( data.nullrepresentation );
      }

      cmdBuff
          .append( "COPY " ).append( data.bufferIndex ).append( " RECORDS INTO " ).append( data.schemaTable )
          .append( " FROM STDIN USING DELIMITERS '" ).append( new String( data.separator ) ).append(
          "','" + Const.CR + "','" ).append( new String( data.quote ) )
          .append( "' NULL AS '" + nullRep + "';" );
      String cmd = cmdBuff.toString();
      if ( log.isDetailed() ) {
        logDetailed( cmd );
      }

      data.out.write( 's' );
      data.out.write( cmdBuff.toString() );
      data.out.newLine();

      for ( int i = 0; i < data.bufferIndex; i++ ) {
        String buffer = data.rowBuffer[i];
        data.out.write( buffer );
        if ( log.isRowLevel() ) {
          logRowlevel( buffer );
        }
      }

      // wait for the prompt
      String error = data.in.waitForPrompt();
      if ( error != null ) {
        throw new KettleException( "Error loading data: " + error );
      }
      // write an empty line, forces the flush of the stream
      data.out.writeLine( "" );

      // again...
      error = data.in.waitForPrompt();
      if ( error != null ) {
        throw new KettleException( "Error loading data: " + error );
      }

      if ( !meta.isCompatibilityDbVersionMode() ) {
        // write an empty line, forces the flush of the stream
        data.out.writeLine( "" );

        // again...
        error = data.in.waitForPrompt();
        if ( error != null ) {
          throw new KettleException( "Error loading data: " + error );
        }
      }

      if ( log.isRowLevel() ) {
        logRowlevel( Const.CR );
      }

      // reset the buffer pointer...
      //
      data.bufferIndex = 0;
    } catch ( Exception e ) {
      throw new KettleException( "An error occurred writing data to the MonetDB API (MAPI) process", e );
    }
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (MonetDBBulkLoaderMeta) smi;
    data = (MonetDBBulkLoaderData) sdi;

    if ( super.init( smi, sdi ) ) {
      data.quote = environmentSubstitute( meta.getFieldEnclosure() );
      data.separator = environmentSubstitute( meta.getFieldSeparator() );

      String nulls = environmentSubstitute( meta.getNULLrepresentation() );
      if ( nulls == null ) {
        data.nullrepresentation = new String();
      } else {
        data.nullrepresentation = nulls;
      }
      data.newline = Const.CR;

      String encoding = environmentSubstitute( meta.getEncoding() );

      data.monetDateMeta = new ValueMetaDate( "dateMeta" );
      data.monetDateMeta.setConversionMask( "yyyy/MM/dd" );
      data.monetDateMeta.setStringEncoding( encoding );

      data.monetTimestampMeta = new ValueMetaDate( "timestampMeta" );
      data.monetTimestampMeta.setConversionMask( "yyyy/MM/dd HH:mm:ss" );
      data.monetTimestampMeta.setStringEncoding( encoding );

      data.monetTimeMeta = new ValueMetaDate( "timeMeta" );
      data.monetTimeMeta.setConversionMask( "HH:mm:ss" );
      data.monetTimeMeta.setStringEncoding( encoding );

      data.monetNumberMeta = new ValueMetaNumber( "numberMeta" );
      data.monetNumberMeta.setConversionMask( "#.#" );
      data.monetNumberMeta.setGroupingSymbol( "," );
      data.monetNumberMeta.setDecimalSymbol( "." );
      data.monetNumberMeta.setStringEncoding( encoding );

      data.bufferSize = Const.toInt( environmentSubstitute( meta.getBufferSize() ), 100000 );

      // Allocate the buffer
      //
      data.rowBuffer = new String[data.bufferSize]; // new byte[data.bufferSize][];
      data.bufferIndex = 0;

      // Make sure our database connection settings are consistent with our dialog settings by
      // altering the connection with an updated answer depending on the dialog setting.
      meta.getDatabaseMeta().setQuoteAllFields(  meta.isFullyQuoteSQL() );

      // Support parameterized database connection names
      String connectionName = meta.getDbConnectionName();
      if ( !Utils.isEmpty( connectionName ) && connectionName.startsWith( "${" ) && connectionName.endsWith( "}" ) ) {
        meta.setDatabaseMeta( localTransMeta.findDatabase( environmentSubstitute( connectionName ) ) );
      }

      // Schema-table combination...
      data.schemaTable =
          meta.getDatabaseMeta( this ).getQuotedSchemaTableCombination(
              environmentSubstitute( meta.getSchemaName() ), environmentSubstitute( meta.getTableName() ) );

      return true;
    }
    return false;
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (MonetDBBulkLoaderMeta) smi;
    data = (MonetDBBulkLoaderData) sdi;

    super.dispose( smi, sdi );
  }

  protected MonetDBBulkLoaderData getData() {
    return this.data;
  }

  protected MapiSocket getMonetDBConnection() throws Exception {
    if ( this.meta == null ) {
      throw new KettleException( "No metadata available to determine connection information from." );
    }
    DatabaseMeta dm = meta.getDatabaseMeta();
    String hostname = environmentSubstitute( Const.NVL( dm.getHostname(), "" ) );
    String portnum = environmentSubstitute( Const.NVL( dm.getDatabasePortNumberString(), "" ) );
    String user = environmentSubstitute( Const.NVL( dm.getUsername(), "" ) );
    String password = Utils.resolvePassword( variables, Const.NVL( dm.getPassword(), "" ) );
    String db = environmentSubstitute( Const.NVL( dm.getDatabaseName(), "" ) );

    MapiSocket mserver = getMonetDBConnection( hostname, Integer.valueOf( portnum ), user, password, db, log );
    return mserver;

  }

  protected static MapiSocket getMonetDBConnection( String host, int port,
      String user, String password, String db ) throws Exception {
    return getMonetDBConnection( host, port, user, password, db, null );
  }

  protected static MapiSocket getMonetDBConnection( String host, int port,
      String user, String password, String db, LogChannelInterface log ) throws Exception {
    MapiSocket mserver = new MapiSocket();
    mserver.setDatabase( db );
    mserver.setLanguage( "sql" );

    List<?> warnings = mserver.connect( host, port, user, password );
    if ( warnings != null ) {
      for ( Object warning : warnings ) {
        if ( log != null ) {
          log.logBasic( "MonetDB connection warning: " + warning );
        }
      }
    } else {
      if ( log != null ) {
        log.logDebug( "Successful MapiSocket connection to MonetDB established." );
      }
    }
    return mserver;
  }

  protected void executeSql( String query ) throws Exception {
    if ( this.meta == null ) {
      throw new KettleException( "No metadata available to determine connection information from." );
    }
    DatabaseMeta dm = meta.getDatabaseMeta();
    String hostname = environmentSubstitute( Const.NVL( dm.getHostname(), "" ) );
    String portnum = environmentSubstitute( Const.NVL( dm.getDatabasePortNumberString(), "" ) );
    String user = environmentSubstitute( Const.NVL( dm.getUsername(), "" ) );
    String password = environmentSubstitute( Const.NVL( dm.getPassword(), "" ) );
    String db = environmentSubstitute( Const.NVL( dm.getDatabaseName(), "" ) );

    executeSql( query, hostname, Integer.valueOf( portnum ), user, password, db );

  }

  /*
   * executeSQL Uses the MonetDB API to create a new server connection and the associated buffered Reader and Writer to
   * execute a single query.
   *
   * @param Query string
   *
   * @param Host URI
   *
   * @param Numerical port
   *
   * @param Username for establishing the connection
   *
   * @param Password for establishing the connection
   *
   * @param database to connect to
   */
  protected static void executeSql( String query, String host, int port, String user, String password, String db ) throws Exception {
    MapiSocket mserver = null;
    try {
      mserver = getMonetDBConnection( host, port, user, password, db );

      BufferedMCLReader in = mserver.getReader();
      BufferedMCLWriter out = mserver.getWriter();

      String error = in.waitForPrompt();
      if ( error != null ) {
        throw new Exception( "ERROR waiting for input reader: " + error );
      }

      // the leading 's' is essential, since it is a protocol
      // marker that should not be omitted, likewise the
      // trailing semicolon
      out.write( 's' );
      System.out.println( query );
      out.write( query );
      out.write( ';' );
      out.newLine();

      out.writeLine( "" );

      String line = null;
      while ( ( line = in.readLine() ) != null ) {
        int type = in.getLineType();

        // read till we get back to the prompt
        if ( type == BufferedMCLReader.PROMPT ) {
          break;
        }

        switch ( type ) {
          case BufferedMCLReader.ERROR:
            System.err.println( line );
            break;
          case BufferedMCLReader.RESULT:
            System.out.println( line );
            break;
          default:
            // unknown, header, ...
            break;
        }
      }

    } finally {
      if ( mserver != null ) {
        mserver.close();
      }
    }

  }

}
