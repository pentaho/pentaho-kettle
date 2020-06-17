/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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
import java.util.function.Function;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.pentaho.di.core.logging.LoggingObjectType.DATABASE;
import static org.pentaho.di.core.logging.LoggingObjectType.TRANS;
import static org.pentaho.di.core.logging.LoggingObjectType.STEP;
import static org.pentaho.di.core.logging.LoggingObjectType.JOB;
import static org.pentaho.di.core.logging.LoggingObjectType.JOBENTRY;

public class Slf4jLoggingEventListener implements KettleLoggingEventListener {

  @VisibleForTesting Logger transLogger = LoggerFactory.getLogger( "org.pentaho.di.trans.Trans" );

  @VisibleForTesting Logger jobLogger = LoggerFactory.getLogger( "org.pentaho.di.job.Job" );

  @VisibleForTesting Logger diLogger = LoggerFactory.getLogger( "org.pentaho.di" );

  @VisibleForTesting Function<String, LoggingObjectInterface> logObjProvider =
    ( objId ) -> LoggingRegistry.getInstance().getLoggingObject( objId );

  private static final String SEPARATOR = "/";

  public Slf4jLoggingEventListener() {
  }


  @Override
  public void eventAdded( KettleLoggingEvent event ) {
    Object messageObject = event.getMessage();
    checkNotNull( messageObject, "Expected log message to be defined." );
    if ( messageObject instanceof LogMessage ) {
      LogMessage message = (LogMessage) messageObject;
      LoggingObjectInterface loggingObject = logObjProvider.apply( message.getLogChannelId() );

      if ( loggingObject == null ) {
        // this can happen if logObject has been discarded while log events are still in flight.
        logToLogger( diLogger, message.getLevel(),
          message.getSubject() + " " + message.getMessage() );
      } else if ( loggingObject.getObjectType() == TRANS || loggingObject.getObjectType() == STEP || loggingObject.getObjectType() == DATABASE  ) {
        logToLogger( transLogger, message.getLevel(), loggingObject, message );
      } else if ( loggingObject.getObjectType() == JOB || loggingObject.getObjectType() == JOBENTRY ) {
        logToLogger( jobLogger, message.getLevel(), loggingObject, message );
      }
    }
  }

  private void logToLogger( Logger logger, LogLevel logLevel, LoggingObjectInterface loggingObject,
                            LogMessage message ) {
    logToLogger( logger, logLevel,
      "[" + getDetailedSubject( loggingObject ) + "]  " + message.getMessage() );
  }

  private void logToLogger( Logger logger, LogLevel logLevel, String message ) {
    switch ( logLevel ) {
      case NOTHING:
        break;
      case ERROR:
        logger.error( message );
        break;
      case MINIMAL:
        logger.warn( message );
        break;
      case BASIC:
      case DETAILED:
        logger.info( message );
        break;
      case DEBUG:
        logger.debug( message );
        break;
      case ROWLEVEL:
        logger.trace( message );
        break;
      default:
        break;
    }
  }

  private String getDetailedSubject( LoggingObjectInterface loggingObject ) {
    LinkedList<String> subjects = new LinkedList<>();
    while ( loggingObject != null ) {
      if ( loggingObject.getObjectType() == TRANS || loggingObject.getObjectType() == JOB ) {
        RepositoryDirectoryInterface rd = loggingObject.getRepositoryDirectory();
        String name;
        if ( isNotBlank( loggingObject.getFilename() ) ) {
          name = loggingObject.getFilename();
        } else {
          name = loggingObject.getObjectType() == TRANS
            ? ( loggingObject.getObjectName() + RepositoryObjectType.TRANSFORMATION.getExtension() )
            : ( loggingObject.getObjectName() + RepositoryObjectType.JOB.getExtension() );
        }

        if ( rd != null ) {
          String path = rd.getPath();
          if ( path.equals( SEPARATOR ) ) {
            if ( name != null && name.length() > 0 ) {
              subjects.add( name );
            }
          } else {
            subjects.add( path + SEPARATOR + name );
          }
        } else if ( name != null && name.length() > 0 ) {
          subjects.add( name );
        }
      }
      loggingObject = loggingObject.getParent();
    }
    if ( subjects.size() > 0 ) {
      return subjects.size() > 1 ? formatDetailedSubject( subjects ) : subjects.get( 0 );
    } else {
      return "";
    }
  }

  private String formatDetailedSubject( LinkedList<String> subjects ) {
    StringBuilder string = new StringBuilder();
    for ( Iterator<String> it = subjects.descendingIterator(); it.hasNext(); ) {
      string.append( it.next() );
      if ( it.hasNext() ) {
        string.append( "  " );
      }
    }
    return string.toString();
  }
}
