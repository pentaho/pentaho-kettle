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

package org.pentaho.di.trans.steps.ivwloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.google.common.annotations.VisibleForTesting;


/**
 * Performs a streaming bulk load to a VectorWise table.
 *
 * Based on Sven Boden's Oracle Bulk Loader step
 *
 * @author matt
 * @since 14-apr-2009
 */
public class IngresVectorwiseLoader extends BaseStep implements StepInterface {

  /** For i18n purposes, needed by Translator2!! */
  private static Class<?> PKG = IngresVectorwiseLoaderMeta.class;

  private IngresVectorwiseLoaderMeta meta;
  private IngresVectorwiseLoaderData data;

  public VWloadMonitor vwLoadMonitor;
  public Thread vwLoadMonitorThread;

  private LogWriter logWriter;
  private Thread logWriteThread;

  public IngresVectorwiseLoader( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
    TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean execute( IngresVectorwiseLoaderMeta meta ) throws KettleException {
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
        String mkFifoCmd = "mkfifo -m 666 [" + data.fifoFilename + "]";
        String[] args = new String[] { "mkfifo", "-m", "666", data.fifoFilename }; // handle spaces and permissions all
                                                                                   // at once.
        logDetailed( "Creating FIFO file using this command : " + mkFifoCmd );
        Process mkFifoProcess = rt.exec( args );
        StreamLogger errorLogger = new StreamLogger( log, mkFifoProcess.getErrorStream(), "mkFifoError" );
        StreamLogger outputLogger = new StreamLogger( log, mkFifoProcess.getInputStream(), "mkFifoOuptut" );
        new Thread( errorLogger ).start();
        new Thread( outputLogger ).start();
        int result = mkFifoProcess.waitFor();
        if ( result != 0 ) {
          throw new Exception( "Return code " + result + " received from statement : " + mkFifoCmd );
        }

        // String chmodCmd = "chmod 666 " + data.fifoFilename;
        // logDetailed("Setting FIFO file permissings using this command : " + chmodCmd);
        // Process chmodProcess = rt.exec(chmodCmd);
        // errorLogger = new StreamLogger(log, chmodProcess.getErrorStream(), "chmodError");
        // outputLogger = new StreamLogger(log, chmodProcess.getInputStream(), "chmodOuptut");
        // new Thread(errorLogger).start();
        // new Thread(outputLogger).start();
        // result = chmodProcess.waitFor();
        // if (result != 0) {
        // throw new Exception("Return code " + result + " received from statement : " + chmodCmd);
        // }
      }

      // 2) Execute the Ingres "sql" command...
      //

      String cmd = createCommandLine( meta );
      String logMessage = masqueradPassword( cmd );
      if ( meta.isUseDynamicVNode() ) {
        // masquerading the password for log
        logMessage = masqueradPassword( cmd );
      }
      logDetailed( "Executing command: " + logMessage );

      try {
        data.sqlProcess = rt.exec( cmd );
      } catch ( IOException ex ) {
        throw new KettleException( "Error while executing psql : " + logMessage, ex );
      }

      // any error message?
      //
      data.errorLogger = new StreamLogger( log, data.sqlProcess.getErrorStream(), "ERR_SQL", true );
      new Thread( data.errorLogger ).start();

      // any output?
      data.outputLogger = new StreamLogger( log, data.sqlProcess.getInputStream(), "OUT_SQL" );

      // Where do we send the data to? --> To STDIN of the sql process
      //
      data.sqlOutputStream = data.sqlProcess.getOutputStream();

      logWriter = new LogWriter( data.sqlProcess.getInputStream() );
      logWriteThread = new Thread( logWriter, "IngresVecorWiseStepLogWriter" );
      logWriteThread.start();

      vwLoadMonitor = new VWloadMonitor( data.sqlProcess, logWriter, logWriteThread );
      vwLoadMonitorThread = new Thread( vwLoadMonitor );
      vwLoadMonitorThread.start();

      logDetailed( "Connected to VectorWise with the 'sql' command." );

      // OK, from here on, we need to feed in the COPY command followed by the
      // data into the pgOutputStream
      //
      String loadCommand = createLoadCommand();
      logDetailed( "Executing command: " + loadCommand );
      data.sqlRunner = new SqlRunner( data, loadCommand );
      data.sqlRunner.start();

      logDetailed( "LOAD TABLE command started" );

      // Open a new fifo output stream, buffered.
      //
      openFifoFile();

      logDetailed( "Fifo stream opened" );

      // Wait until it all hooks up in the FIFO
      //
      waitForAConnection();

      logDetailed( "Ready to start bulk loading!" );
    } catch ( Exception ex ) {
      throw new KettleException( ex );
    }

    return true;
  }

  private String createLoadCommand() {
    String loadCommand = "";
    loadCommand +=
      "COPY TABLE " + meta.getDatabaseMeta().getQuotedSchemaTableCombination( null, meta.getTableName() ) + " ";

    // Build list of column names to set
    //
    loadCommand += "(" + Const.CR;
    for ( int cnt = 0; cnt < meta.getFieldDatabase().length; cnt++ ) {
      loadCommand += "  " + meta.getFieldDatabase()[cnt];
      if ( cnt < meta.getFieldDatabase().length - 1 ) {
        // loadCommand+="= c0";
        if ( meta.isUseSSV() ) {
          loadCommand += "= c0ssv ";
        } else {
          loadCommand += "= char(0) ";
          if ( "\t".equals( meta.getDelimiter() ) ) {
            loadCommand += "TAB";
          } else {
            loadCommand += "'" + meta.getDelimiter() + "'";
          }
        }
      } else {
        if ( meta.isUseSSV() ) {
          loadCommand += "= c0ssv";
        } else {
          loadCommand += "= char(0) NL";
        }
      }

      if ( cnt < meta.getFieldDatabase().length - 1 ) {
        loadCommand += ",";
      }
      loadCommand += Const.CR;
    }
    loadCommand += ") FROM '" + environmentSubstitute( meta.getFifoFileName() ) + "'";
    boolean withDone = false;

    if ( meta.isContinueOnError() ) {
      loadCommand += "WITH ON_ERROR=CONTINUE";
      withDone = true;
    }

    // If error file is available, add it to the log
    if ( meta.getErrorFileName() != null && meta.getErrorFileName().trim().length() != 0 ) {
      if ( withDone ) {
        loadCommand += ", ";
      } else {
        loadCommand += "WITH ";
      }
      loadCommand += "LOG='" + environmentSubstitute( meta.getErrorFileName() ) + "'";
    }

    loadCommand += " \\g" + Const.CR;

    // Also quite this session after the load
    //
    loadCommand += " \\q" + Const.CR;

    return loadCommand;
  }

  private void openFifoFile() throws Exception {

    // Ready to start writing rows to the FIFO file now...
    //
    logDetailed( "Opening fifo file " + data.fifoFilename + " for writing." );
    data.fifoOpener = new FifoOpener( data.fifoFilename );
    data.fifoOpener.start();
  }

  private void waitForAConnection() throws Exception {
    // Wait for either the sql statement to throw an error or the
    // fifo writer to throw an error
    //
    while ( !isStopped() ) {
      data.fifoOpener.join( 1000 );
      // check if SQL Proces is still running has exited throw Error

      if ( !checkSqlProcessRunning( data.sqlProcess ) ) {
        throw new Exception( "Ingres SQL process has stopped" );
      }

      if ( data.fifoOpener.getState() == Thread.State.TERMINATED ) {
        break;
      }

      try {
        data.sqlRunner.checkExcn();
      } catch ( Exception e ) {
        // We need to open a stream to the fifo to unblock the fifo writer
        // that was waiting for the sqlRunner that now isn't running
        data.fifoOpener.join();
        logError( "Make sure user has been granted the FILE privilege." );
        logError( "" );
        throw e;
      }

      data.fifoOpener.checkExcn();
    }

    logDetailed( "Opened fifo file " + data.fifoFilename + " for writing." );
  }

  /**
   * Create the command line for a sql process depending on the meta information supplied.
   *
   * @param meta
   *          The meta data to create the command line from
   *
   * @return The string to execute.
   *
   * @throws KettleException
   *           Upon any exception
   */
  public String createCommandLine( IngresVectorwiseLoaderMeta meta ) throws KettleException {
    StringBuilder sb = new StringBuilder( 300 );

    if ( !Utils.isEmpty( meta.getSqlPath() ) ) {
      try {
        FileObject fileObject = KettleVFS.getFileObject( environmentSubstitute( meta.getSqlPath() ), getTransMeta() );
        String sqlexec = Const.optionallyQuoteStringByOS( KettleVFS.getFilename( fileObject ) );
        sb.append( sqlexec );
        // sql @tc-dwh-test.timocom.net,tcp_ip,VW[ingres,pwd]::dwh
      } catch ( KettleFileException ex ) {
        throw new KettleException( "Error retrieving command string", ex );
      }
    } else {
      if ( meta.isUsingVwload() ) {
        if ( isDetailed() ) {
          logDetailed( "vwload defaults to system path" );
        }
        sb.append( "vwload" );
      } else {
        if ( isDetailed() ) {
          logDetailed( "sql defaults to system path" );
        }
        sb.append( "sql" );
      }
    }

    DatabaseMeta dm = meta.getDatabaseMeta();
    if ( dm != null ) {
      String databaseName = environmentSubstitute( Const.NVL( dm.getDatabaseName(), "" ) );
      String password =
        Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( Const.NVL( dm.getDatabaseInterface()
          .getPassword(), "" ) ) );
      String port = environmentSubstitute( Const.NVL( dm.getDatabasePortNumberString(), "" ) ).replace( "7", "" );
      String username = environmentSubstitute( Const.NVL( dm.getDatabaseInterface().getUsername(), "" ) );
      String hostname = environmentSubstitute( Const.NVL( dm.getDatabaseInterface().getHostname(), "" ) );
      String schemaTable = dm.getQuotedSchemaTableCombination( null, environmentSubstitute( meta.getTableName() ) );
      String encoding = environmentSubstitute( Const.NVL( meta.getEncoding(), "" ) );
      String fifoFile =
        Const.optionallyQuoteStringByOS( environmentSubstitute( Const.NVL( meta.getFifoFileName(), "" ) ) );
      String errorFile =
        Const.optionallyQuoteStringByOS( environmentSubstitute( Const.NVL( meta.getErrorFileName(), "" ) ) );
      int maxNrErrors = Const.toInt( environmentSubstitute( Const.NVL( meta.getMaxNrErrors(), "0" ) ), 0 );

      if ( meta.isUsingVwload() ) {
        sb.append( " -u " ).append( username );
        sb.append( " -P " ).append( password );
        sb.append( " -f " ).append( meta.getDelimiter() ).append( "" );
        sb.append( " -t " ).append( schemaTable );

        if ( !Utils.isEmpty( encoding ) ) {
          sb.append( " -C " ).append( encoding );
        }
        if ( !Utils.isEmpty( errorFile ) ) {
          sb.append( " -l " ).append( errorFile );
        }
        if ( maxNrErrors > 0 ) {
          // need multiplication for two because every wrong rows
          // provide 2 errors that is not evident
          sb.append( " -x " ).append( maxNrErrors * 2 );
        }
        sb.append( " " ).append( databaseName );
        sb.append( " " ).append( fifoFile );

      } else if ( meta.isUseDynamicVNode() ) {
        // logical portname in JDBC use a 7

        sb.append( " @" ).append( hostname ).append( "," ).append( port ).append( "[" ).append( username ).append( "," )
          .append( password ).append( "]::" ).append( databaseName );
      } else {
        // Database Name
        //
        sb.append( " " ).append( databaseName );
        if ( meta.isUseAuthentication() ) {
          sb.append( "-P" ).append( password );
        }
      }
    } else {
      throw new KettleException( "No connection specified" );
    }

    return sb.toString();
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (IngresVectorwiseLoaderMeta) smi;
    data = (IngresVectorwiseLoaderData) sdi;

    try {
      Object[] r = getRow(); // Get row from input rowset & set row busy!
      // no more input to be expected...
      if ( r == null ) {
        // only close output after the first row was processed
        // to prevent error (NPE) on empty rows set
        if ( !first ) {
          closeOutput();
        }

        if ( logWriter != null ) {
          logWriteThread.join();
          if ( logWriter.isErrorsOccured() ) {
            throw new SQLException( "The error was gotten from ingres sql process" );
          }
        }

        if ( vwLoadMonitorThread != null ) {
          vwLoadMonitorThread.join();
        }

        setOutputDone();
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
        data.bulkRowMeta = getInputRowMeta().clone();
        if ( meta.isUseStandardConversion() ) {
          for ( int i = 0; i < data.bulkRowMeta.size(); i++ ) {
            ValueMetaInterface valueMeta = data.bulkRowMeta.getValueMeta( i );
            if ( valueMeta.isStorageNormal() ) {
              if ( valueMeta.isDate() ) {
                valueMeta.setConversionMask( "yyyy-MM-dd HH:mm:ss" );
              } else if ( valueMeta.isNumeric() ) {
                valueMeta.setDecimalSymbol( "." );
                valueMeta.setGroupingSymbol( "" );
              }
            }
          }
        }

        // execute the client statement...
        //
        execute( meta );

        // Allocate a buffer
        //
        data.fileChannel = data.fifoOpener.getFileChannel();
        data.byteBuffer = ByteBuffer.allocate( data.bufferSize );
      }

      // check if SQL process is still running before processing row
      if ( !checkSqlProcessRunning( data.sqlProcess ) ) {
        throw new Exception( "Ingres SQL process has stopped" );
      }

      writeRowToBulk( data.bulkRowMeta, r );
      putRow( getInputRowMeta(), r );
      incrementLinesOutput();

      if ( checkFeedback( getLinesOutput() ) ) {
        logBasic( BaseMessages.getString( PKG, "IngresVectorwiseLoader.Log.LineNumber" ) + getLinesOutput() );
      }

      return true;

    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "IngresVectorwiseLoader.Log.ErrorInStep" ), e );
      setErrors( 1 );
      stopAll();
      setOutputDone(); // signal end to receiver(s)
      return false;
    }
  }

  private void closeOutput() throws Exception {

    // Flush the rest of the buffer to disk!
    //
    if ( data.byteBuffer.position() > 0 ) {
      data.byteBuffer.flip();
      data.fileChannel.write( data.byteBuffer );
    }

    // Close the fifo file...
    //
    data.fifoOpener.close();
    data.fileChannel = null;

    // wait for the INSERT statement to finish and check for any
    // error and/or warning...
    //
    data.sqlRunner.join();
    SqlRunner sqlRunner = data.sqlRunner;
    data.sqlRunner = null;
    sqlRunner.checkExcn();

    data.sqlOutputStream.close();
    data.sqlOutputStream = null;
  }

  private void writeRowToBulk( RowMetaInterface rowMeta, Object[] r ) throws KettleException {

    try {
      // So, we have this output stream to which we can write CSV data to.
      // Basically, what we need to do is write the binary data (from strings to
      // it as part of this proof of concept)
      //
      // The data format required is essentially "value|value|value|value"
      // new feature implemented
      // "use SSV which requires the format to be '"value";"value","value"'
      byte[] delimiter;
      if ( meta.isUseSSV() ) {
        delimiter = data.semicolon;
      } else {
        delimiter = data.separator;
      }

      for ( int i = 0; i < data.keynrs.length; i++ ) {
        if ( i > 0 ) {
          // Write a separator
          //
          write( delimiter );
        }

        int index = data.keynrs[i];
        ValueMetaInterface valueMeta = rowMeta.getValueMeta( index );
        Object valueData = r[index];

        if ( valueData != null ) {
          if ( valueMeta.isStorageBinaryString() ) {
            byte[] value = valueMeta.getBinaryString( valueData );
            write( value );
          } else {
            // We're using the bulk row metadata so dates and numerics should be in the correct format now...
            //
            String string = valueMeta.getString( valueData );
            if ( string != null ) {

              if ( meta.isEscapingSpecialCharacters() && valueMeta.isString() ) {
                string = replace( string, new String[] { "\n", "\r", }, new String[] { "\\n", "\\r", } );
              }
              // support of SSV feature
              //
              if ( meta.isUseSSV() ) {

                // replace " in string fields
                //
                if ( meta.isEscapingSpecialCharacters() && valueMeta.isString() ) {
                  string = replace( string, new String[] { "\"" }, new String[] { "\\\"" } );
                  log.logRowlevel( "\' \" \' symbol was added for the future processing" );
                }
                write( data.doubleQuote );
                write( data.getBytes( string ) );
                write( data.doubleQuote );
              } else {
                write( data.getBytes( string ) );
              }
            }
          }
        }
      }

      // finally write a newline
      //
      write( data.newline );
    } catch ( Exception e ) {
      // If something went wrong with the import,
      // rather return that error, in stead of "Pipe Broken"
      try {
        data.sqlRunner.checkExcn();
      } catch ( Exception loadEx ) {
        throw new KettleException( "Error serializing rows of data to the fifo file", loadEx );
      }

      throw new KettleException( "Error serializing rows of data to the fifo file", e );
    }

  }

  private void write( byte[] content ) throws IOException {

    if ( content == null || content.length == 0 ) {
      return;
    }

    // If exceptionally we have a block of data larger than the buffer simply dump it to disk!
    //
    if ( content.length > data.byteBuffer.capacity() ) {
      // It should be exceptional to have a single field containing over 50k data
      //
      ByteBuffer buf = ByteBuffer.wrap( content ); // slow method!
      data.byteBuffer.flip();
      data.fileChannel.write( buf );
    } else {
      // Normal situation, is there capacity in the buffer?
      //
      if ( data.byteBuffer.remaining() > content.length ) {
        // Yes, there is room: add content to buffer
        //
        data.byteBuffer.put( content );
      } else {
        // No: empty the buffer to disk
        //
        data.byteBuffer.flip();
        data.fileChannel.write( data.byteBuffer );
        data.byteBuffer.clear();
        data.byteBuffer.put( content );
      }
    }
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (IngresVectorwiseLoaderMeta) smi;
    data = (IngresVectorwiseLoaderData) sdi;

    if ( super.init( smi, sdi ) ) {
      if ( Utils.isEmpty( meta.getDelimiter() ) ) {
        data.separator = data.getBytes( "|" );
      } else {
        data.separator = data.getBytes( meta.getDelimiter() );
      }

      data.newline = data.getBytes( "\n" );
      data.semicolon = data.getBytes( ";" );
      data.doubleQuote = data.getBytes( "\"" );

      // Schema-table combination...
      data.schemaTable =
        meta.getDatabaseMeta().getQuotedSchemaTableCombination( null, environmentSubstitute( meta.getTableName() ) );

      data.encoding = environmentSubstitute( meta.getEncoding() );
      data.isEncoding = !Utils.isEmpty( environmentSubstitute( meta.getEncoding() ) );

      data.byteBuffer = null;

      String bufferSizeString = environmentSubstitute( meta.getBufferSize() );
      data.bufferSize = Utils.isEmpty( bufferSizeString ) ? 5000 : Const.toInt( bufferSizeString, 5000 );

      if ( meta.isTruncatingTable() && meta.getDatabaseMeta() != null ) {

        // Connect to Vectorwise over standard JDBC and truncate the table
        //
        Database db = new Database( this, meta.getDatabaseMeta() );
        try {
          db.connect();
          db.execStatement( "CALL VECTORWISE( COMBINE '" + data.schemaTable + " - " + data.schemaTable + "' )" );

          // Just to make sure VW gets the message
          //
          db.execStatement( "CALL VECTORWISE( COMBINE '" + data.schemaTable + " - " + data.schemaTable + "' )" );
          log.logDetailed( "Table " + data.schemaTable + " was truncated using a 'combine' statement." );
        } catch ( Exception e ) {
          log.logError( "Error truncating table", e );
          return false;
        } finally {
          db.disconnect();
        }

      }

      return true;
    }
    return false;
  }

  public boolean checkSqlProcessRunning( Process sqlProcess ) {
    try {
      int exitValue = sqlProcess.exitValue();
      logError( "SQL process exit code: " + exitValue );
      return false;
    } catch ( IllegalThreadStateException e ) {
      // ignore this exception since it is thrown when exitValue() is called on a
      // running process
      // Do nothing SQL Process still running
      return true;
    }
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (IngresVectorwiseLoaderMeta) smi;
    data = (IngresVectorwiseLoaderData) sdi;

    closeClientConnections( data );

    super.dispose( smi, sdi );
  }

  // Class to try and open a writer to a FIFO in a different thread.
  // Opening the FIFO is a blocking call, so we need to check for errors
  // after a small waiting period
  //
  public class FifoOpener extends Thread {
    private FileOutputStream fileOutputStream = null;
    private FileChannel fileChannel = null;
    private Exception ex;
    private String fifoName;

    public FifoOpener( String fifoName ) {
      this.fifoName = fifoName;
    }

    public void run() {
      try {

        fileOutputStream = new FileOutputStream( this.fifoName );
        fileChannel = fileOutputStream.getChannel();
      } catch ( Exception ex ) {
        this.ex = ex;
      }
    }

    public void checkExcn() throws Exception {
      // This is called from the main thread context to re-throw any saved
      // exception.
      //
      if ( ex != null ) {
        throw ex;
      }
    }

    public FileChannel getFileChannel() {
      return fileChannel;
    }

    public void close() throws IOException {
      if ( fileChannel != null && fileOutputStream != null ) {
        fileChannel.close();
        fileOutputStream.close();
      }
    }
  }

  static class SqlRunner extends Thread {
    private IngresVectorwiseLoaderData data;

    private String loadCommand;

    private Exception ex;

    SqlRunner( IngresVectorwiseLoaderData data, String loadCommand ) {
      this.data = data;
      this.loadCommand = loadCommand;
    }

    public void run() {
      try {
        data.sqlOutputStream.write( data.getBytes( loadCommand ) );
        data.sqlOutputStream.flush();
      } catch ( Exception ex ) {
        this.ex = ex;
      }
    }

    void checkExcn() throws Exception {
      // This is called from the main thread context to re-throw any saved
      // exception.
      //
      if ( ex != null ) {
        throw ex;
      }
    }
  }

  public class VWloadMonitor implements Runnable {
    private Process vwloadProcess;
    private LogWriter logWriter;
    private Thread outputLoggerThread;

    VWloadMonitor( Process loadProcess, LogWriter logWriter, Thread outputLoggerThread ) {
      this.vwloadProcess = loadProcess;
      this.logWriter = logWriter;
      this.outputLoggerThread = outputLoggerThread;
    }

    public void run() {
      try {
        int resultCode = vwloadProcess.waitFor();
        Long[] results = tryToParseVWloadResultMessage();
        if ( results != null ) {
          setLinesOutput( results[1] );
          setLinesRejected( results[2] );
        }
        boolean errorResult =
          ( resultCode != 0 ) || ( results != null && ( !meta.isContinueOnError() && !meta.isUsingVwload() ) );
        if ( errorResult ) {
          setLinesOutput( 0L );
          logError( "Bulk loader finish unsuccessfully" );
          setErrors( 1L );
        } else {
          setErrors( 0L );
        }
      } catch ( Exception ex ) {
        setErrors( 1L );
        logError( "Unexpected error encountered while monitoring bulk load process", ex );
      }
    }

    @SuppressWarnings( "resource" )
    private Long[] tryToParseVWloadResultMessage() throws InterruptedException, IOException {
      outputLoggerThread.join();
      Long[] result = new Long[3];
      if ( meta.isUsingVwload() ) {
        String lastLine = logWriter.getLastInputStreamLine();
        Scanner sc = null;
        try {
          sc = new Scanner( lastLine );
          sc = sc.useDelimiter( "\\D+" );
          int i = 0;
          while ( sc.hasNext() ) {
            result[i++] = sc.nextBigInteger().longValue();
          }
        } finally {
          if ( sc != null ) {
            sc.close();
          }
        }
      } else {
        if ( meta.getErrorFileName() == null ) {
          return null;
        }
        File errorFile = new File( meta.getErrorFileName() );
        if ( !errorFile.exists() ) {
          return null;
        } else {
          LineNumberReader lnr = new LineNumberReader( new FileReader( errorFile ) );
          lnr.skip( Long.MAX_VALUE );
          Integer errors = lnr.getLineNumber();
          result[1] = ( getLinesOutput() - errors );
          result[2] = Long.valueOf( errors );
        }
      }
      return result;
    }
  }

  public boolean closeClientConnections( IngresVectorwiseLoaderData data ) {
    // Close the output streams if still needed.
    //
    try {
      if ( data.fifoOpener != null ) {
        data.fifoOpener.close();
      }

      // Stop the SQL execution thread
      //
      if ( data.sqlRunner != null ) {
        data.sqlRunner.join();
        data.sqlRunner = null;
      }

      // remove the fifo file...
      //
      try {
        if ( data.fifoFilename != null ) {
          new File( data.fifoFilename ).deleteOnExit();
        }
      } catch ( Exception e ) {
        logError( "Unable to delete FIFO file : " + data.fifoFilename, e );
      }
    } catch ( Exception e ) {
      setErrors( 1L );
      logError( "Unexpected error encountered while closing the client connection", e );
      return false;
    }
    return true;
  }

  @VisibleForTesting
  String replace( String string, String[] searchStrings, String[] replaceStrings ) {
    StringBuilder builder = new StringBuilder( string );
    for ( int e = 0; e < Math.min( searchStrings.length, replaceStrings.length ); e++ ) {
      String chr = searchStrings[e];
      String rep = replaceStrings[e];
      int idx = builder.indexOf( chr, 0 );
      while ( idx != -1 ) {
        builder.replace( idx, idx + chr.length(), rep );
        idx = builder.indexOf( chr, idx + rep.length() );
      }
    }
    return builder.toString();
  }


  @VisibleForTesting
  String masqueradPassword( String input ) {
    String regex = "\\[.*,.*\\]";
    String substitution = "[username,password]";

    String result = substitute( input, regex, substitution );

    if ( !result.isEmpty() ) {
      return result;
    }
    regex = "-u\\s.*\\s-P\\s.*?\\s";
    substitution = "-u username, -P password ";

    result = substitute( input, regex, substitution );

    return result;
  }

  @VisibleForTesting
  String substitute( String input, String regex, String substitution ) {
    Pattern replace = Pattern.compile( regex );
    Matcher matcher = replace.matcher( input );
    if ( matcher.find() ) {
      return matcher.replaceAll( substitution );
    }
    return "";
  }

  class LogWriter implements Runnable {
    final InputStream is;
    boolean isErrorsOccured;
    String lastLine;

    public LogWriter( InputStream outStream ) {
      this.is = outStream;
    }

    @Override
    public void run() {
      printLog();
    }

    private void printLog() {
      try {
        InputStreamReader isr = new InputStreamReader( is );
        BufferedReader br = new BufferedReader( isr );
        String line = null;
        String ingresErrorRegex = ".*E_[A-Z]{1,2}[0-9]{3,4}.*";
        while ( ( line = br.readLine() ) != null ) {
          lastLine = line;
          if ( !line.matches( ingresErrorRegex ) ) {
            log.logBasic( LogLevelEnum.OUT.getPredicateMessage() + line );
          } else {
            log.logError( LogLevelEnum.ERROR.getPredicateMessage() + line );
            isErrorsOccured = true;
          }
        }
      } catch ( IOException ioe ) {
        log.logError( Const.getStackTracker( ioe ) );
      }
    }

    boolean isErrorsOccured() {
      return isErrorsOccured;
    }

    String getLastInputStreamLine() {
      return lastLine;
    }
  }

   /**
    * Log level of the current step
    */
  private enum LogLevelEnum {
    ERROR {
      public String getPredicateMessage() {
        return "ERR_SQL ";
      }
    },
    OUT {
      public String getPredicateMessage() {
        return "OUT_SQL ";
      }
    };
    abstract String getPredicateMessage();
  }
}
