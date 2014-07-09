/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.logging.log4j;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.KettleLoggingEvent;
import org.pentaho.di.core.logging.LoggingPlugin;
import org.pentaho.di.core.logging.LoggingPluginInterface;

@LoggingPlugin(
  id = "Log4jLogging" )
public class Log4jLogging implements LoggingPluginInterface {

  public static final String STRING_PENTAHO_DI_LOGGER_NAME = "org.pentaho.di";

  public static final String STRING_PENTAHO_DI_CONSOLE_APPENDER = "ConsoleAppender:" + STRING_PENTAHO_DI_LOGGER_NAME;

  private Logger pentahoLogger;

  public Log4jLogging() {
    pentahoLogger = Logger.getLogger( STRING_PENTAHO_DI_LOGGER_NAME );
    pentahoLogger.setAdditivity( false );
  }

  @Override
  public void eventAdded( KettleLoggingEvent event ) {
    switch ( event.getLevel() ) {
      case ERROR:
        pentahoLogger.log( Level.ERROR, event.getMessage() );
        break;
      case DEBUG:
      case ROWLEVEL:
        pentahoLogger.log( Level.DEBUG, event.getMessage() );
        break;
      default:
        pentahoLogger.log( Level.INFO, event.getMessage() );
        break;
    }
  }

  @Override
  public void init() {
    KettleLogStore.getAppender().addLoggingEventListener( this );
  }

  public void dispose() {
    KettleLogStore.getAppender().removeLoggingEventListener( this );
  }
}
