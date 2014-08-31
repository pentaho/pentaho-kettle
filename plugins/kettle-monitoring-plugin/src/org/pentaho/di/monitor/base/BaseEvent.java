/*!
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* Copyright (c) 2002-2014 Pentaho Corporation..  All rights reserved.
*/
package org.pentaho.di.monitor.base;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.KettleLoggingEvent;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.monitor.Constants;
import org.pentaho.di.monitor.MonitorEnvironment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * base event class that handles event-wide operations (such as logging)
 */
public abstract class BaseEvent implements IKettleMonitoringEvent {

  // Channel ID
  private String logChannelId;

  // event logging
  private String[] eventLogs;

  protected String[] filterEventLogging( String logChannelId ) throws KettleException {
    return filterEventLogging( logChannelId,
      MonitorEnvironment.getInstance().getLogLevelMessageTransportation(),
      MonitorEnvironment.getInstance().getMaxLogEntriesTransportation() );
  }

  protected String[] filterEventLogging( String logChannelId, LogLevel logLevel, int maxLogEntries )
    throws KettleException {

    // A maxLogEntries value < 1 is the equivalent of having log message transportation disabled
    // When 'NOTHING' is set, messages carried over will only have "DISABLED";
    if ( logLevel == null || logLevel == LogLevel.NOTHING || maxLogEntries < 1 ) {
      return new String[] { "DISABLED" };
    }

    // fetch latest ( maxLogEntries * 10 ) log lines from KettleLogStore
    List<KettleLoggingEvent> eventLogList =
      KettleLogStore.getLogBufferFromTo( logChannelId, true,
        Math.max( 0, KettleLogStore.getLastBufferLineNr() - ( maxLogEntries * 10 ) ),
        KettleLogStore.getLastBufferLineNr() );

    // nothing available
    if ( eventLogList == null || eventLogList.size() == 0 ) {
      return new String[] { };
    }

    // example: in cases maxLogEntries = 20, but so far there's only 7 logEntries
    int maxLogs = Math.min( eventLogList.size(), maxLogEntries );

    // we start including from latest (relevant) log event to oldest (relevant) log event
    Collections.reverse( eventLogList );

    ArrayList<String> eventLogs = new ArrayList<String>();

    for ( int i = 0; i < maxLogs; i++ ) {

      KettleLoggingEvent loggingEvent = eventLogList.get( i );

      if ( loggingEvent != null && isLogLevelAccepted( logLevel, loggingEvent.getLevel() ) ) {
        eventLogs.add( eventLogToString( loggingEvent ) );
      }
    }

    // When 'ERROR' is set and while no errors occur, messages carried over will only have "NO ERRORS".
    if ( eventLogs.size() == 0 && logLevel == LogLevel.ERROR ) {
      eventLogs.add( new String( "NO ERRORS" ) );

    } else if ( eventLogs.size() > 1 ) {
      // prior to filtering, we reversed the eventLogList
      Collections.reverse( eventLogs );
    }

    return eventLogs.toArray( new String[] { } );
  }

  /**
   * Log channel ID
   *
   * @return log channel ID
   */
  public String getLogChannelId() {
    return logChannelId;
  }

  /**
   * Log channel ID
   *
   * @param logChannelId log channel ID
   */
  public void setLogChannelId( String logChannelId ) {
    this.logChannelId = logChannelId;
  }

  /**
   * event logs
   *
   * @return event logs
   */
  public String[] getEventLogs() {
    return eventLogs;
  }

  /**
   * event logs
   *
   * @param eventLogs event logs
   */
  public void setEventLogs( String[] eventLogs ) {
    this.eventLogs = eventLogs;
  }

  /**
   * Define whether logs will be sent over alongside events.
   * <p/>
   * One of: NOTHING | ERROR | MINIMAL | BASIC | DETAILED | DEBUG
   */
  private boolean isLogLevelAccepted( LogLevel userDefinedlogLevel, LogLevel logLevel ) {

    if ( userDefinedlogLevel == null || logLevel == null || userDefinedlogLevel == LogLevel.NOTHING ) {
      return false;
    }

    switch( userDefinedlogLevel ) {

      case ERROR:
        return ( logLevel == LogLevel.ERROR );
      case MINIMAL:
        return ( logLevel == LogLevel.ERROR || logLevel == LogLevel.MINIMAL );
      case BASIC:
        return ( logLevel == LogLevel.ERROR || logLevel == LogLevel.MINIMAL || logLevel == LogLevel.BASIC );
      case DETAILED:
        return ( logLevel == LogLevel.ERROR || logLevel == LogLevel.MINIMAL || logLevel == LogLevel.BASIC
          || logLevel == LogLevel.DETAILED );
      case DEBUG:
        return ( logLevel == LogLevel.ERROR || logLevel == LogLevel.MINIMAL || logLevel == LogLevel.BASIC
          || logLevel == LogLevel.DETAILED || logLevel == LogLevel.DEBUG );
      default:
        return false;
    }
  }

  private String eventLogToString( KettleLoggingEvent loggingEvent ) {

    if ( loggingEvent != null ) {

      StringBuffer sb = new StringBuffer();
      sb.append( Constants.DATE_FORMAT.format( new Date( loggingEvent.getTimeStamp() ) ) );
      sb.append( " | " ).append( loggingEvent.getLevel().name().toUpperCase() );
      sb.append( " | " ).append( loggingEvent.getMessage() );
      return sb.toString();
    }
    return "N/A";
  }
}
