/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * 
 * @deprecated As of release 7.0, no longer needed. This class does not used in kettle5-log4j-plugin's project
 */
@Deprecated
public class CentralLogStore {
  /**
   * Create the central log store with optional limitation to the size
   *
   * @param maxSize the maximum size
   * @param maxLogTimeoutMinutes The maximum time that a log line times out in Minutes.
   */
  private CentralLogStore( int maxSize, int maxLogTimeoutMinutes ) {
    KettleLogStore.init( maxSize, maxLogTimeoutMinutes );
  }

  public void replaceLogCleaner( final int maxLogTimeoutMinutes ) {
    KettleLogStore.getInstance().replaceLogCleaner( maxLogTimeoutMinutes );
  }

  /**
   * Initialize the central log store with optional limitation to the size
   *
   * @param maxSize the maximum size
   * @param maxLogTimeoutHours The maximum time that a log line times out in hours.
   */
  public static void init( int maxSize, int maxLogTimeoutMinutes ) {
    KettleLogStore.init( maxSize, maxLogTimeoutMinutes );
  }

  public static void init() {
    KettleLogStore.init();
  }

  /**
   * @return the number (sequence, 1..N) of the last log line.
   * If no records are present in the buffer, 0 is returned.
   */
  public static int getLastBufferLineNr() {
    return KettleLogStore.getLastBufferLineNr();
  }

  /**
   *
   * Get all the log lines pertaining to the specified parent log channel id (including all children)
   *
   * @param parentLogChannelId the parent log channel ID to grab
   * @param includeGeneral include general log lines
   * @param from
   * @param to
   * @return the log lines found
   */
  public static List<LoggingEvent> getLogBufferFromTo( String parentLogChannelId, boolean includeGeneral, int from, int to ) {
    List<KettleLoggingEvent> events = KettleLogStore.getLogBufferFromTo( parentLogChannelId, includeGeneral, from, to );
    return convertKettleLoggingEventsToLog4jLoggingEvents( events );
  }

  private static List<LoggingEvent> convertKettleLoggingEventsToLog4jLoggingEvents( List<KettleLoggingEvent> events ) {
    LogWriter logWriter = LogWriter.getInstance();
    // Copy the events over for compatibility
    List<LoggingEvent> list = new ArrayList<LoggingEvent>();
    for ( KettleLoggingEvent event : events ) {
      LoggingEvent loggingEvent = new LoggingEvent(
        logWriter.getPentahoLogger().getClass().getName(),
        logWriter.getPentahoLogger(), // Category is deprecated
        event.getTimeStamp(),
        convertKettleLogLevelToLog4jLevel( event.getLevel() ),
        event.getMessage(), null );
      list.add( loggingEvent );
    }

    return list;

  }

  private static Level convertKettleLogLevelToLog4jLevel( LogLevel level ) {
    switch ( level ) {
      case BASIC:
        return Level.INFO;
      case DETAILED:
        return Level.INFO;
      case DEBUG:
        return Level.DEBUG;
      case ROWLEVEL:
        return Level.DEBUG;
      case MINIMAL:
        return Level.INFO;
      case ERROR:
        return Level.ERROR;
      case NOTHING:
        return Level.OFF;
      default:
        return Level.INFO;
    }
  }

  /**
   * Get all the log lines for the specified parent log channel id (including all children)
   *
   * @param channelId channel IDs to grab
   * @param includeGeneral include general log lines
   * @param from
   * @param to
   * @return
   */
  public static List<LoggingEvent> getLogBufferFromTo( List<String> channelId, boolean includeGeneral, int from, int to ) {
    return convertKettleLoggingEventsToLog4jLoggingEvents( KettleLogStore.getLogBufferFromTo( channelId, includeGeneral, from, to ) );
  }

  /*
   * This method will no longer be available in the Kettle 5 API
   *
   * @return The appender that represents the central logging store.  It is capable of giving back log rows in an incremental fashion, etc.
   *
  public static Log4jBufferAppender getAppender() {
    return getInstance().appender;
  }
  */

  /**
   * Discard all the lines for the specified log channel id AND all the children.
   *
   * @param parentLogChannelId the parent log channel id to be removed along with all its children.
   */
  public static void discardLines( String parentLogChannelId, boolean includeGeneralMessages ) {
    KettleLogStore.discardLines( parentLogChannelId, includeGeneralMessages );
  }
}
