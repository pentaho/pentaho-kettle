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

package org.pentaho.di.trans.steps.socketwriter;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Write data to a TCP/IP socket read by SocketReader. The data being sent over the socket is one serialized Row object
 * including metadata and then a series of serialized rows, data only.
 *
 * This part of the SocketWriter/SocketRead pair contains the ServerSocket.
 *
 * @author Matt
 * @since 1-dec-2006
 */
public class SocketWriter extends BaseStep implements StepInterface {
  private static Class<?> PKG = SocketWriterMeta.class; // for i18n purposes, needed by Translator2!!

  private SocketWriterMeta meta;
  private SocketWriterData data;

  public SocketWriter( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (SocketWriterMeta) smi;
    data = (SocketWriterData) sdi;

    try {
      if ( first ) {
        int bufferSize = Const.toInt( environmentSubstitute( meta.getBufferSize() ), 1000 );

        data.clientSocket = data.serverSocket.accept();

        if ( meta.isCompressed() ) {
          data.outputStream =
            new DataOutputStream( new BufferedOutputStream( new GZIPOutputStream( data.clientSocket
              .getOutputStream() ), bufferSize ) );
        } else {
          data.outputStream =
            new DataOutputStream( new BufferedOutputStream( data.clientSocket.getOutputStream(), bufferSize ) );
        }

        data.flushInterval = Const.toInt( environmentSubstitute( meta.getFlushInterval() ), 4000 );
      }
    } catch ( Exception e ) {
      logError( "Error accepting from socket : " + e.toString() );
      logError( "Stack trace: " + Const.CR + Const.getStackTracker( e ) );

      setErrors( 1 );
      stopAll();
      setOutputDone();
      if ( data.clientSocket != null ) {
        try {
          data.clientSocket.shutdownInput();
          data.clientSocket.shutdownOutput();
          data.clientSocket.close();
          logError( "Closed connection to SocketWriter" );
        } catch ( IOException e1 ) {
          logError( "Failed to close connection to SocketWriter" );
        }
      }

      return false;
    }

    Object[] r = getRow(); // get row, set busy!
    // Input rowMeta is automatically set, available when needed

    if ( r == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }

    try {
      if ( first ) {
        getInputRowMeta().writeMeta( data.outputStream );
        first = false;
      }
      getInputRowMeta().writeData( data.outputStream, r );
      incrementLinesOutput();

      // flush every X rows
      if ( getLinesOutput() > 0 && data.flushInterval > 0 && ( getLinesOutput() % data.flushInterval ) == 0 ) {
        data.outputStream.flush();
      }

    } catch ( Exception e ) {
      logError( "Error writing to socket : " + e.toString() );
      logError( "Failing row : " + getInputRowMeta().getString( r ) );
      logError( "Stack trace: " + Const.CR + Const.getStackTracker( e ) );

      setErrors( 1 );
      stopAll();
      setOutputDone();
      return false;
    }

    if ( checkFeedback( getLinesRead() ) ) {
      logBasic( BaseMessages.getString( PKG, "SocketWriter.Log.LineNumber" ) + getLinesRead() );
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SocketWriterMeta) smi;
    data = (SocketWriterData) sdi;

    if ( super.init( smi, sdi ) ) {
      try {
        data.serverSocketPort = Integer.parseInt( environmentSubstitute( meta.getPort() ) );
        data.serverSocket =
          getTrans().getSocketRepository().openServerSocket(
            data.serverSocketPort, getTransMeta().getName() + " - " + this.toString() );

        return true;
      } catch ( Exception e ) {
        logError( "Error creating server socket: " + e.toString() );
        logError( Const.getStackTracker( e ) );
      }
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    // Ignore errors, we don't care
    // If we are here, it means all work is done
    // It's a lot of work to keep it all in sync for now we don't need to do that.
    //
    if ( data.outputStream != null ) {
      try {
        data.outputStream.close();
      } catch ( Exception e ) {
        // Ignore errors
      }
    }

    if ( data.clientSocket != null && !data.clientSocket.isClosed() ) {
      try {
        data.clientSocket.shutdownInput();
        data.clientSocket.shutdownOutput();
        data.clientSocket.close();
        if ( log.isDetailed() ) {
          logDetailed( "Closed connection to SocketWriter" );
        }
      } catch ( IOException e1 ) {
        logError( "Failed to close connection to SocketWriter" );
      }
    }

    if ( data.serverSocket != null && !data.serverSocket.isClosed() ) {
      try {
        data.serverSocket.close();
      } catch ( IOException e ) {
        // Ignore errors
      }
    }

    try {
      getTrans().getSocketRepository().releaseSocket( data.serverSocketPort );
    } catch ( IOException ignore ) {
      // Ignore errors
    }
    super.dispose( smi, sdi );
  }

}
