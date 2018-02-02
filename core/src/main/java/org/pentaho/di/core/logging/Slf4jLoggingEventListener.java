/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.Iterator;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.pentaho.di.core.logging.LoggingObjectType.TRANS;
import static org.pentaho.di.core.logging.LoggingObjectType.STEP;
import static org.pentaho.di.core.logging.LoggingObjectType.JOB;
import static org.pentaho.di.core.logging.LoggingObjectType.JOBENTRY;

public class Slf4jLoggingEventListener implements KettleLoggingEventListener {

  Logger transLogger = LoggerFactory.getLogger( "org.pentaho.di.trans.Trans" );

  Logger jobLogger = LoggerFactory.getLogger( "org.pentaho.di.job.Job" );

  public Slf4jLoggingEventListener() {
  }

  private LogLevel extractLogLevel( LoggingObjectInterface loggingObject ) {
    while ( loggingObject != null && loggingObject.getLogLevel() == null ) {
      loggingObject = loggingObject.getParent();
    }
    return loggingObject != null ? loggingObject.getLogLevel() != null ? loggingObject.getLogLevel() : LogLevel.BASIC : LogLevel.BASIC;
  }
  @Override
  public void eventAdded( KettleLoggingEvent event ) {
    Object messageObject = event.getMessage();
    if ( messageObject instanceof LogMessage ) {
      LogMessage message = (LogMessage) messageObject;
      LoggingObjectInterface loggingObject = LoggingRegistry.getInstance().getLoggingObject( message.getLogChannelId() );
      if ( loggingObject.getObjectType() == TRANS ||  loggingObject.getObjectType() == STEP ) {
        logToLogger( transLogger, extractLogLevel( loggingObject ), loggingObject, message );
      } else if ( loggingObject.getObjectType() == JOB || loggingObject.getObjectType() == JOBENTRY ) {
        logToLogger( jobLogger, extractLogLevel( loggingObject ), loggingObject, message );
      }
    }
  }
  private void logToLogger( Logger logger, LogLevel logLevel, LoggingObjectInterface loggingObject, LogMessage message ) {
    switch ( logLevel ) {
      case NOTHING:
        break;
      case ERROR:
        logger.error( getDetailedSubject( loggingObject ) + " " + message.getMessage() );
        break;
      case MINIMAL:
        logger.warn( getDetailedSubject( loggingObject ) + " " + message.getMessage() );
        break;
      case BASIC:
      case DETAILED:
        logger.info( getDetailedSubject( loggingObject ) + " " + message.getMessage() );
        break;
      case DEBUG:
        logger.debug( getDetailedSubject( loggingObject ) + " " + message.getMessage() );
        break;
      case ROWLEVEL:
        logger.trace( getDetailedSubject( loggingObject ) + " " + message.getMessage() );
        break;
      default:
        break;
    }
  }

  private String getDetailedSubject( LoggingObjectInterface loggingObject ) {
    LinkedList<String> subjects = new LinkedList<String>();
    while ( loggingObject != null ) {
      subjects.add( loggingObject.getObjectName() );
      if ( loggingObject.getObjectType() == TRANS || loggingObject.getObjectType() == JOB ) {
        if ( loggingObject.getFilename() != null ) {
          subjects.add( loggingObject.getFilename() );
        } else if ( loggingObject.getRepositoryDirectory() != null ) {
          subjects.add( loggingObject.getRepositoryDirectory().getPath() );
        }
      }
      loggingObject = loggingObject.getParent();
    }
    return subjects.size() > 1 ? formatDetailedSubject( subjects ) : subjects.get( 0 );
  }

  private String formatDetailedSubject( LinkedList<String> subjects ) {
    StringBuilder string = new StringBuilder();
    for ( Iterator<String> it = subjects.descendingIterator(); it.hasNext(); ) {
      string.append( "  " ).append( it.next() );
    }
    return string.toString();
  }
}
