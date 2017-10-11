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

package org.pentaho.di.core.logging;

import java.io.OutputStream;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;

public class FileLoggingEventListener implements KettleLoggingEventListener {

  private String filename;
  private FileObject file;

  public FileObject getFile() {
    return file;
  }

  private OutputStream outputStream;
  private KettleLogLayout layout;

  private KettleException exception;
  private String logChannelId;

  /**
   * Log all log lines to the specified file
   *
   * @param filename
   * @param append
   * @throws KettleException
   */
  public FileLoggingEventListener( String filename, boolean append ) throws KettleException {
    this( null, filename, append );
  }

  /**
   * Log only lines belonging to the specified log channel ID or one of it's children (grandchildren) to the specified
   * file.
   *
   * @param logChannelId
   * @param filename
   * @param append
   * @throws KettleException
   */
  public FileLoggingEventListener( String logChannelId, String filename, boolean append ) throws KettleException {
    this.logChannelId = logChannelId;
    this.filename = filename;
    this.layout = new KettleLogLayout( true );
    this.exception = null;

    file = KettleVFS.getFileObject( filename );
    outputStream = null;
    try {
      outputStream = KettleVFS.getOutputStream( file, append );
    } catch ( Exception e ) {
      throw new KettleException(
        "Unable to create a logging event listener to write to file '" + filename + "'", e );
    }
  }

  @Override
  public void eventAdded( KettleLoggingEvent event ) {

    try {
      Object messageObject = event.getMessage();
      if ( messageObject instanceof LogMessage ) {
        boolean logToFile = false;

        if ( logChannelId == null ) {
          logToFile = true;
        } else {
          LogMessage message = (LogMessage) messageObject;
          // This should be fast enough cause cached.
          List<String> logChannelChildren = LoggingRegistry.getInstance().getLogChannelChildren( logChannelId );
          // This could be non-optimal, consider keeping the list sorted in the logging registry
          logToFile = Const.indexOfString( message.getLogChannelId(), logChannelChildren ) >= 0;
        }

        if ( logToFile ) {
          String logText = layout.format( event );
          outputStream.write( logText.getBytes() );
          outputStream.write( Const.CR.getBytes() );
        }
      }
    } catch ( Exception e ) {
      exception = new KettleException( "Unable to write to logging event to file '" + filename + "'", e );
    }
  }

  public void close() throws KettleException {
    try {
      if ( outputStream != null ) {
        outputStream.close();
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unable to close output of file '" + filename + "'", e );
    }
  }

  public KettleException getException() {
    return exception;
  }

  public void setException( KettleException exception ) {
    this.exception = exception;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename( String filename ) {
    this.filename = filename;
  }

  public OutputStream getOutputStream() {
    return outputStream;
  }

  public void setOutputStream( OutputStream outputStream ) {
    this.outputStream = outputStream;
  }
}
