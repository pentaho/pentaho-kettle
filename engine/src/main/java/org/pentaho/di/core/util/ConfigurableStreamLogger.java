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

package org.pentaho.di.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;

/**
 * @author <a href="mailto:michael.gugerell@aschauer-edv.at">Michael Gugerell(asc145)</a>
 *
 *         Provides the ability to specify the desired logLevel on which the StreamLogger should write.
 */
public class ConfigurableStreamLogger implements Runnable {

  private InputStream is;
  private String type;
  private LogLevel logLevel;
  private LogChannelInterface log;

  /**
   * @param in
   *          the InputStream
   * @param logLevel
   *          the logLevel. Refer to org.pentaho.di.core.logging.LogWriter for constants
   * @param type
   *          the label for logger entries.
   */
  public ConfigurableStreamLogger( LogChannelInterface logChannel, final InputStream in, final LogLevel logLevel,
    final String type ) {
    this.log = logChannel;
    this.is = in;
    this.type = type;
    this.logLevel = logLevel;
  }

  /**
   * (non-Javadoc)
   *
   * @see java.lang.Runnable#run()
   */
  public void run() {
    try {
      InputStreamReader isr = new InputStreamReader( this.is );
      BufferedReader br = new BufferedReader( isr );
      String line = null;
      while ( ( line = br.readLine() ) != null ) {
        String logEntry = this.type + " " + line;
        switch ( this.logLevel ) {
          case MINIMAL:
            log.logMinimal( logEntry );
            break;
          case BASIC:
            log.logBasic( logEntry );
            break;
          case DETAILED:
            log.logDetailed( logEntry );
            break;
          case DEBUG:
            log.logDebug( logEntry );
            break;
          case ROWLEVEL:
            log.logRowlevel( logEntry );
            break;
          case ERROR:
            log.logError( logEntry );
            break;
          default: // NONE
            break;
        }
      }
    } catch ( IOException ioe ) {
      if ( log.isError() ) {
        log.logError( this.type + " " + Const.getStackTracker( ioe ) );
      }
    }
  }

}
