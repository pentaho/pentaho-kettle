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

package org.pentaho.di.trans.steps.mysqlbulkloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
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

/**
 * Performs a streaming bulk load to a MySQL table.
 *
 * Based on Sven Boden's Oracle Bulk Loader step
 *
 * @author matt
 * @since 14-apr-2009
 */
public class MySQLBulkLoader extends BaseStep implements StepInterface {
  private static Class<?> PKG = MySQLBulkLoaderMeta.class; // for i18n purposes, needed by Translator2!!

  private MySQLBulkLoaderMeta meta;
  private MySQLBulkLoaderData data;
  private final long threadWaitTime = 300000;
  private final String threadWaitTimeText = "5min";

  public MySQLBulkLoader( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean execute( MySQLBulkLoaderMeta meta ) throws KettleException {
    Runtime rt = Runtime.getRuntime();

    try {
      // 1) Create the FIFO file using the "mkfifo" command...
      // Make sure to log all the possible output, also from STDERR
      //
      data.fifoFilename = environmentSubstitute( meta.getFifoFileName() );

      File fifoFile = new File( data.fifoFilename );
      if ( !fifoFile.exists() ) {
        // MKFIFO!
        //
        String mkFifoCmd = "mkfifo " + data.fifoFilename;
        //
        logBasic( BaseMessages.getString( PKG, "MySQLBulkLoader.Message.CREATINGFIFO",  data.dbDescription, mkFifoCmd ) );
        Process mkFifoProcess = rt.exec( mkFifoCmd );
        StreamLogger errorLogger = new StreamLogger( log, mkFifoProcess.getErrorStream(), "mkFifoError" );
        StreamLogger outputLogger = new StreamLogger( log, mkFifoProcess.getInputStream(), "mkFifoOuptut" );
        new Thread( errorLogger ).start();
        new Thread( outputLogger ).start();
        int result = mkFifoProcess.waitFor();
        if ( result != 0 ) {
          throw new Exception( BaseMessages.getString( PKG, "MySQLBulkLoader.Message.ERRORFIFORC", result, mkFifoCmd ) );
        }

        String chmodCmd = "chmod 666 " + data.fifoFilename;
        logBasic( BaseMessages.getString( PKG, "MySQLBulkLoader.Message.SETTINGPERMISSIONSFIFO",  data.dbDescription, chmodCmd ) );
        Process chmodProcess = rt.exec( chmodCmd );
        errorLogger = new StreamLogger( log, chmodProcess.getErrorStream(), "chmodError" );
        outputLogger = new StreamLogger( log, chmodProcess.getInputStream(), "chmodOuptut" );
        new Thread( errorLogger ).start();
        new Thread( outputLogger ).start();
        result = chmodProcess.waitFor();
        if ( result != 0 ) {
          throw new Exception( BaseMessages.getString( PKG, "MySQLBulkLoader.Message.ERRORFIFORC", result, chmodCmd ) );
        }
      }

      // 2) Make a connection to MySQL for sending SQL commands
      // (Also, we need a clear cache for getting up-to-date target metadata)
      DBCache.getInstance().clear( meta.getDatabaseMeta().getName() );
      if ( meta.getDatabaseMeta() == null ) {
        logError( BaseMessages.getString( PKG, "MySQLBulkLoader.Init.ConnectionMissing", getStepname() ) );
        return false;
      }
      data.db = new Database( this, meta.getDatabaseMeta() );
      data.db.shareVariablesWith( this );
      PluginInterface dbPlugin =
          PluginRegistry.getInstance().getPlugin( DatabasePluginType.class, meta.getDatabaseMeta().getDatabaseInterface() );
      data.dbDescription = ( dbPlugin != null ) ? dbPlugin.getDescription() : BaseMessages.getString( PKG, "MySQLBulkLoader.UnknownDB" );

      // Connect to the database
      if ( getTransMeta().isUsingUniqueConnections() ) {
        synchronized ( getTrans() ) {
          data.db.connect( getTrans().getTransactionId(), getPartitionID() );
        }
      } else {
        data.db.connect( getPartitionID() );
      }

      logBasic( BaseMessages.getString( PKG, "MySQLBulkLoader.Message.CONNECTED",  data.dbDescription ) );

      // 3) Now we are ready to run the load command...
      //
      executeLoadCommand();
    } catch ( Exception ex ) {
      throw new KettleException( ex );
    }

    return true;
  }

  private void executeLoadCommand() throws Exception {

    String loadCommand = "";
    loadCommand +=
        "LOAD DATA " + ( meta.isLocalFile() ? "LOCAL" : "" ) + " INFILE '"
            + environmentSubstitute( meta.getFifoFileName() ) + "' ";
    if ( meta.isReplacingData() ) {
      loadCommand += "REPLACE ";
    } else if ( meta.isIgnoringErrors() ) {
      loadCommand += "IGNORE ";
    }
    loadCommand += "INTO TABLE " + data.schemaTable + " ";
    if ( !Utils.isEmpty( meta.getEncoding() ) ) {
      loadCommand += "CHARACTER SET " + meta.getEncoding() + " ";
    }
    String delStr = meta.getDelimiter();
    if ( "\t".equals( delStr ) ) {
      delStr = "\\t";
    }

    loadCommand += "FIELDS TERMINATED BY '" + delStr + "' ";
    if ( !Utils.isEmpty( meta.getEnclosure() ) ) {
      loadCommand += "OPTIONALLY ENCLOSED BY '" + meta.getEnclosure() + "' ";
    }
    loadCommand +=
        "ESCAPED BY '" + meta.getEscapeChar() + ( "\\".equals( meta.getEscapeChar() ) ? meta.getEscapeChar() : "" )
            + "' ";

    // Build list of column names to set
    loadCommand += "(";
    for ( int cnt = 0; cnt < meta.getFieldTable().length; cnt++ ) {
      loadCommand += meta.getDatabaseMeta().quoteField( meta.getFieldTable()[cnt] );
      if ( cnt < meta.getFieldTable().length - 1 ) {
        loadCommand += ",";
      }
    }

    loadCommand += ");" + Const.CR;

    logBasic( BaseMessages.getString( PKG, "MySQLBulkLoader.Message.STARTING",  data.dbDescription, loadCommand ) );

    data.sqlRunner = new SqlRunner( data, loadCommand );
    data.sqlRunner.start();

    // Ready to start writing rows to the FIFO file now...
    //
    if ( !Const.isWindows() ) {
      logBasic( BaseMessages.getString( PKG, "MySQLBulkLoader.Message.OPENFIFO",  data.fifoFilename ) );
      OpenFifo openFifo = new OpenFifo( data.fifoFilename, 1000 );
      openFifo.start();

      // Wait for either the sql statement to throw an error or the
      // fifo writer to throw an error
      while ( true ) {
        openFifo.join( 200 );
        if ( openFifo.getState() == Thread.State.TERMINATED ) {
          break;
        }

        try {
          data.sqlRunner.checkExcn();
        } catch ( Exception e ) {
          // We need to open a stream to the fifo to unblock the fifo writer
          // that was waiting for the sqlRunner that now isn't running
          new BufferedInputStream( new FileInputStream( data.fifoFilename ) ).close();
          openFifo.join();
          logError( BaseMessages.getString( PKG, "MySQLBulkLoader.Message.ERRORFIFO" ) );
          logError( "" );
          throw e;
        }

        try {
          openFifo.checkExcn();
        } catch ( Exception e ) {
          throw e;
        }
      }
      data.fifoStream = openFifo.getFifoStream();
    }

  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (MySQLBulkLoaderMeta) smi;
    data = (MySQLBulkLoaderData) sdi;

    try {
      Object[] r = getRow(); // Get row from input rowset & set row busy!
      if ( r == null ) { // no more input to be expected...

        setOutputDone();

        closeOutput();

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

        data.bulkFormatMeta = new ValueMetaInterface[data.keynrs.length];
        for ( int i = 0; i < data.keynrs.length; i++ ) {
          ValueMetaInterface sourceMeta = getInputRowMeta().getValueMeta( data.keynrs[i] );
          if ( sourceMeta.isDate() ) {
            if ( meta.getFieldFormatType()[i] == MySQLBulkLoaderMeta.FIELD_FORMAT_TYPE_DATE ) {
              data.bulkFormatMeta[i] = data.bulkDateMeta.clone();
            } else if ( meta.getFieldFormatType()[i] == MySQLBulkLoaderMeta.FIELD_FORMAT_TYPE_TIMESTAMP ) {
              data.bulkFormatMeta[i] = data.bulkTimestampMeta.clone(); // default to timestamp
            }
          } else if ( sourceMeta.isNumeric()
              && meta.getFieldFormatType()[i] == MySQLBulkLoaderMeta.FIELD_FORMAT_TYPE_NUMBER ) {
            data.bulkFormatMeta[i] = data.bulkNumberMeta.clone();
          }

          if ( data.bulkFormatMeta[i] == null && !sourceMeta.isStorageBinaryString() ) {
            data.bulkFormatMeta[i] = sourceMeta.clone();
          }
        }

        // execute the client statement...
        //
        execute( meta );
      }

      // Every nr of rows we re-start the bulk load process to allow indexes etc to fit into the MySQL server memory
      // Performance could degrade if we don't do this.
      //
      if ( data.bulkSize > 0 && getLinesOutput() > 0 && ( getLinesOutput() % data.bulkSize ) == 0 ) {
        closeOutput();
        executeLoadCommand();
      }

      writeRowToBulk( getInputRowMeta(), r );
      putRow( getInputRowMeta(), r );
      incrementLinesOutput();

      return true;
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "MySQLBulkLoader.Log.ErrorInStep" ), e );
      setErrors( 1 );
      stopAll();
      setOutputDone(); // signal end to receiver(s)
      return false;
    }
  }

  private void closeOutput() throws Exception {

    if ( data.fifoStream != null ) {
      // Close the fifo file...
      //
      data.fifoStream.close();
      data.fifoStream = null;
    }

    if ( data.sqlRunner != null ) {

      // wait for the INSERT statement to finish and check for any error and/or warning...
      logDebug( "Waiting up to " + this.threadWaitTimeText + " for the MySQL load command thread to finish processing." ); // no requirement for NLS debug messages
      data.sqlRunner.join( this.threadWaitTime );
      SqlRunner sqlRunner = data.sqlRunner;
      data.sqlRunner = null;
      sqlRunner.checkExcn();
    }
  }

  private void writeRowToBulk( RowMetaInterface rowMeta, Object[] r ) throws KettleException {

    try {
      // So, we have this output stream to which we can write CSV data to.
      // Basically, what we need to do is write the binary data (from strings to it as part of this proof of concept)
      //
      // The data format required is essentially:
      //
      for ( int i = 0; i < data.keynrs.length; i++ ) {
        if ( i > 0 ) {
          // Write a separator
          //
          data.fifoStream.write( data.separator );
        }

        int index = data.keynrs[i];
        ValueMetaInterface valueMeta = rowMeta.getValueMeta( index );
        Object valueData = r[index];

        if ( valueData == null ) {
          data.fifoStream.write( "NULL".getBytes() );
        } else {
          switch ( valueMeta.getType() ) {
            case ValueMetaInterface.TYPE_STRING:
              data.fifoStream.write( data.quote );
              if ( valueMeta.isStorageBinaryString()
                  && meta.getFieldFormatType()[i] == MySQLBulkLoaderMeta.FIELD_FORMAT_TYPE_OK ) {
                // We had a string, just dump it back.
                data.fifoStream.write( (byte[]) valueData );
              } else {
                String string = valueMeta.getString( valueData );
                if ( string != null ) {
                  if ( meta.getFieldFormatType()[i] == MySQLBulkLoaderMeta.FIELD_FORMAT_TYPE_STRING_ESCAPE ) {
                    string = Const.replace( string, meta.getEscapeChar(), meta.getEscapeChar() + meta.getEscapeChar() );
                    string = Const.replace( string, meta.getEnclosure(), meta.getEscapeChar() + meta.getEnclosure() );
                  }
                  data.fifoStream.write( string.getBytes() );
                }
              }
              data.fifoStream.write( data.quote );
              break;
            case ValueMetaInterface.TYPE_INTEGER:
              if ( valueMeta.isStorageBinaryString() && data.bulkFormatMeta[i] == null ) {
                data.fifoStream.write( valueMeta.getBinaryString( valueData ) );
              } else {
                Long integer = valueMeta.getInteger( valueData );
                if ( integer != null ) {
                  data.fifoStream.write( data.bulkFormatMeta[i].getString( integer ).getBytes() );
                }
              }
              break;
            case ValueMetaInterface.TYPE_DATE:
              if ( valueMeta.isStorageBinaryString() && data.bulkFormatMeta[i] == null ) {
                data.fifoStream.write( valueMeta.getBinaryString( valueData ) );
              } else {
                Date date = valueMeta.getDate( valueData );
                if ( date != null ) {
                  data.fifoStream.write( data.bulkFormatMeta[i].getString( date ).getBytes() );
                }
              }
              break;
            case ValueMetaInterface.TYPE_BOOLEAN:
              if ( valueMeta.isStorageBinaryString() && data.bulkFormatMeta[i] == null ) {
                data.fifoStream.write( valueMeta.getBinaryString( valueData ) );
              } else {
                Boolean b = valueMeta.getBoolean( valueData );
                if ( b != null ) {
                  data.fifoStream.write( data.bulkFormatMeta[i].getString( b ).getBytes() );
                }
              }
              break;
            case ValueMetaInterface.TYPE_NUMBER:
              if ( valueMeta.isStorageBinaryString() && data.bulkFormatMeta[i] == null ) {
                data.fifoStream.write( (byte[]) valueData );
              } else {
                /**
                 * If this is the first line, reset default conversion mask for Number type (#.#;-#.#).
                 * This will make conversion mask to be calculated according to meta data (length, precision).
                 *
                 * http://jira.pentaho.com/browse/PDI-11421
                 */
                if ( getLinesWritten() == 0 ) {
                  data.bulkFormatMeta[i].setConversionMask( null );
                }

                Double d = valueMeta.getNumber( valueData );
                if ( d != null ) {
                  data.fifoStream.write( data.bulkFormatMeta[i].getString( d ).getBytes() );
                }
              }
              break;
            case ValueMetaInterface.TYPE_BIGNUMBER:
              if ( valueMeta.isStorageBinaryString() && data.bulkFormatMeta[i] == null ) {
                data.fifoStream.write( (byte[]) valueData );
              } else {
                BigDecimal bn = valueMeta.getBigNumber( valueData );
                if ( bn != null ) {
                  data.fifoStream.write( data.bulkFormatMeta[i].getString( bn ).getBytes() );
                }
              }
              break;
            default:
              break;
          }
        }
      }

      // finally write a newline
      //
      data.fifoStream.write( data.newline );

      if ( ( getLinesOutput() % 5000 ) == 0 ) {
        data.fifoStream.flush();
      }
    } catch ( IOException e ) {
      // If something went wrong with writing to the fifo, get the underlying error from MySQL
      try {
        logError( BaseMessages.getString( PKG, "MySQLBulkLoader.Message.IOERROR", this.threadWaitTimeText ) );
        try {
          data.sqlRunner.join( this.threadWaitTime );
        } catch ( InterruptedException ex ) {
          // Ignore errors
        }
        data.sqlRunner.checkExcn();
      } catch ( Exception loadEx ) {
        throw new KettleException( BaseMessages.getString( PKG, "MySQLBulkLoader.Message.ERRORSERIALIZING" ), loadEx );
      }

      // MySQL didn't finish, throw the generic "Pipe" exception.
      throw new KettleException( BaseMessages.getString( PKG, "MySQLBulkLoader.Message.ERRORSERIALIZING" ), e );

    } catch ( Exception e2 ) {
      // Null pointer exceptions etc.
      throw new KettleException( BaseMessages.getString( PKG, "MySQLBulkLoader.Message.ERRORSERIALIZING" ), e2 );
    }
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (MySQLBulkLoaderMeta) smi;
    data = (MySQLBulkLoaderData) sdi;

    if ( super.init( smi, sdi ) ) {
      if ( Utils.isEmpty( meta.getEnclosure() ) ) {
        data.quote = new byte[] {};
      } else {
        data.quote = environmentSubstitute( meta.getEnclosure() ).getBytes();
      }
      if ( Utils.isEmpty( meta.getDelimiter() ) ) {
        data.separator = "\t".getBytes();
      } else {
        data.separator = environmentSubstitute( meta.getDelimiter() ).getBytes();
      }
      data.newline = Const.CR.getBytes();

      String realEncoding = environmentSubstitute( meta.getEncoding() );
      data.bulkTimestampMeta = new ValueMetaDate( "timestampMeta" );
      data.bulkTimestampMeta.setConversionMask( "yyyy-MM-dd HH:mm:ss" );
      data.bulkTimestampMeta.setStringEncoding( realEncoding );

      data.bulkDateMeta = new ValueMetaDate( "dateMeta" );
      data.bulkDateMeta.setConversionMask( "yyyy-MM-dd" );
      data.bulkDateMeta.setStringEncoding( realEncoding );

      data.bulkNumberMeta = new ValueMetaNumber( "numberMeta" );
      data.bulkNumberMeta.setConversionMask( "#.#" );
      data.bulkNumberMeta.setGroupingSymbol( "," );
      data.bulkNumberMeta.setDecimalSymbol( "." );
      data.bulkNumberMeta.setStringEncoding( realEncoding );

      data.bulkSize = Const.toLong( environmentSubstitute( meta.getBulkSize() ), -1L );

      // Schema-table combination...
      data.schemaTable =
          meta.getDatabaseMeta().getQuotedSchemaTableCombination( environmentSubstitute( meta.getSchemaName() ),
              environmentSubstitute( meta.getTableName() ) );

      return true;
    }
    return false;
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (MySQLBulkLoaderMeta) smi;
    data = (MySQLBulkLoaderData) sdi;

    // Close the output streams if still needed.
    //
    try {
      if ( data.fifoStream != null ) {
        data.fifoStream.close();
      }

      // Stop the SQL execution thread
      //
      if ( data.sqlRunner != null ) {
        data.sqlRunner.join();
        data.sqlRunner = null;
      }
      // Release the database connection
      //
      if ( data.db != null ) {
        data.db.disconnect();
        data.db = null;
      }

      // remove the fifo file...
      //
      try {
        if ( data.fifoFilename != null ) {
          new File( data.fifoFilename ).delete();
        }
      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "MySQLBulkLoader.Message.UNABLETODELETE", data.fifoFilename ), e );
      }
    } catch ( Exception e ) {
      setErrors( 1L );
      logError( BaseMessages.getString( PKG, "MySQLBulkLoader.Message.UNEXPECTEDERRORCLOSING" ), e );
    }

    super.dispose( smi, sdi );
  }

  // Class to try and open a writer to a fifo in a different thread.
  // Opening the fifo is a blocking call, so we need to check for errors
  // after a small waiting period
  static class OpenFifo extends Thread {
    private BufferedOutputStream fifoStream = null;
    private Exception ex;
    private String fifoName;
    private int size;

    OpenFifo( String fifoName, int size ) {
      this.fifoName = fifoName;
      this.size = size;
    }

    @Override
    public void run() {
      try {
        fifoStream = new BufferedOutputStream( new FileOutputStream( OpenFifo.this.fifoName ), this.size );
      } catch ( Exception ex ) {
        this.ex = ex;
      }
    }

    void checkExcn() throws Exception {
      // This is called from the main thread context to rethrow any saved
      // excn.
      if ( ex != null ) {
        throw ex;
      }
    }

    BufferedOutputStream getFifoStream() {
      return fifoStream;
    }
  }

  static class SqlRunner extends Thread {
    private MySQLBulkLoaderData data;

    private String loadCommand;

    private Exception ex;

    SqlRunner( MySQLBulkLoaderData data, String loadCommand ) {
      this.data = data;
      this.loadCommand = loadCommand;
    }

    @Override
    public void run() {
      try {
        data.db.execStatement( loadCommand );
      } catch ( Exception ex ) {
        this.ex = ex;
      }
    }

    void checkExcn() throws Exception {
      // This is called from the main thread context to rethrow any saved
      // excn.
      if ( ex != null ) {
        throw ex;
      }
    }
  }
}
