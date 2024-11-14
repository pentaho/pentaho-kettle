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


package org.pentaho.di.trans.steps.textfileoutputlegacy;


import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutput;

import java.io.IOException;

/**
 * This is deprecated version with capability run as command.
 * @deprecated use {@link org.pentaho.di.trans.steps.textfileoutput.TextFileOutput} instead.
 */
@Deprecated
public class TextFileOutputLegacy extends TextFileOutput {

  public TextFileOutputLegacy( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                               Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  protected boolean writeRowTo( Object[] row ) throws KettleException {
    if ( ( (TextFileOutputLegacyMeta) meta).isFileAsCommand() ) {
      return writeRowToCommand( row );
    } else {
      return super.writeRowTo( row );
    }
  }

  private boolean writeRowToCommand( Object[] row ) throws KettleException {
    if ( row != null ) {
      if ( data.writer == null ) {
        initCommandStreamWriter( environmentSubstitute( meta.getFileName() ) );
      }
      first = false;
      writeRow( data.outputRowMeta, row );
      putRow( data.outputRowMeta, row ); // in case we want it to go further...

      if ( checkFeedback( getLinesOutput() ) ) {
        logBasic( "linenr " + getLinesOutput() );
      }
      return true;
    } else {
      if ( ( data.writer == null ) && !Utils.isEmpty( meta.getEndedLine() ) ) {
        initCommandStreamWriter( environmentSubstitute( meta.getFileName() ) );
        initBinaryDataFields();
      }
      writeEndedLine();
      closeCommand();
      setOutputDone();
      return false;
    }
  }


  private boolean closeCommand() {
    boolean retval;

    try {
      if ( data.writer != null ) {
        data.writer.flush();

        // If writing a ZIP or GZIP file not from a command, do not close the writer or else
        // the closing of the ZipOutputStream below will throw an "already closed" exception.
        // Rather than checking for compression types, it is easier to check for cmdProc != null
        // because if that check fails, we know we will get into the ZIP/GZIP processing below.
        if ( log.isDebug() ) {
          logDebug( "Closing output stream" );
        }
        data.writer.close();
        if ( log.isDebug() ) {
          logDebug( "Closed output stream" );
        }
      }
      data.writer = null;
      if ( log.isDebug() ) {
        logDebug( "Ending running external command" );
      }

      if ( data.cmdProc != null ) {
        int procStatus = data.cmdProc.waitFor();
        // close the streams
        // otherwise you get "Too many open files, java.io.IOException" after a lot of iterations
        try {
          data.cmdProc.getErrorStream().close();
          data.cmdProc.getOutputStream().flush();
          data.cmdProc.getOutputStream().close();
          data.cmdProc.getInputStream().close();
        } catch ( IOException e ) {
          if ( log.isDetailed() ) {
            logDetailed( "Warning: Error closing streams: " + e.getMessage() );
          }
        }
        data.cmdProc = null;
        if ( log.isBasic() && procStatus != 0 ) {
          logBasic( "Command exit status: " + procStatus );
        }
      }

      retval = true;
    } catch ( Exception e ) {
      logError( "Exception trying to close file: " + e.toString() );
      setErrors( 1 );
      //Clean resources
      data.writer = null;
      retval = false;
    }

    return retval;
  }

  @Override
  protected void initOutput() throws KettleException {
    if ( ( (TextFileOutputLegacyMeta) meta ).isFileAsCommand() ) {
      initCommandStreamWriter( environmentSubstitute( meta.getFileName() ) );
    } else {
      super.initOutput();
    }
  }

  @Override
  protected void close() throws IOException {
    if ( ( (TextFileOutputLegacyMeta) meta ).isFileAsCommand() ) {
      closeCommand();
    } else {
      super.close();
    }
  }

  private void initCommandStreamWriter( String cmdstr ) throws KettleException {
    data.writer = null;
    try {
      if ( log.isDebug() ) {
        logDebug( "Spawning external process" );
      }
      if ( data.cmdProc != null ) {
        logError( "Previous command not correctly terminated" );
        setErrors( 1 );
      }
      if ( Const.getOS().equals( "Windows 95" ) ) {
        cmdstr = "command.com /C " + cmdstr;
      } else {
        if ( Const.getOS().startsWith( "Windows" ) ) {
          cmdstr = "cmd.exe /C " + cmdstr;
        }
      }
      if ( isDetailed() ) {
        logDetailed( "Starting: " + cmdstr );
      }
      Runtime runtime = Runtime.getRuntime();
      data.cmdProc = runtime.exec( cmdstr, EnvUtil.getEnvironmentVariablesForRuntimeExec() );
      data.writer = data.cmdProc.getOutputStream();

      StreamLogger stdoutLogger = new StreamLogger( log, data.cmdProc.getInputStream(), "(stdout)" );
      StreamLogger stderrLogger = new StreamLogger( log, data.cmdProc.getErrorStream(), "(stderr)" );
      new Thread( stdoutLogger ).start();
      new Thread( stderrLogger ).start();
    } catch ( Exception e ) {
      throw new KettleException( "Error opening new file : " + e.toString() );
    }
  }
}

