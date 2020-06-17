/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.Date;
import java.util.Queue;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.metrics.MetricsSnapshot;
import org.pentaho.di.core.metrics.MetricsSnapshotInterface;
import org.pentaho.di.core.metrics.MetricsSnapshotType;

public class LogChannel implements LogChannelInterface {

  public static final LogChannelInterface GENERAL = new LogChannel( "General", false, false );
  public static final LogChannelInterface METADATA = new LogChannel( "Metadata", false, false );
  public static final LogChannelInterface UI = new LogChannel( "GUI", false, false );

  private static final String DEFAULT_LOG_SUBJECT = "Kettle";

  private static final MetricsRegistry metricsRegistry = MetricsRegistry.getInstance();

  private final String logChannelId;

  private LogLevel logLevel;

  private String containerObjectId;

  private boolean gatheringMetrics;

  private boolean forcingSeparateLogging;


  private String filter;

  private LogChannelFileWriterBuffer fileWriter;

  public LogChannel() {
    this( DEFAULT_LOG_SUBJECT );
  }

  public LogChannel( Object subject ) {
    logLevel = DefaultLogLevel.getLogLevel();
    logChannelId = LoggingRegistry.getInstance().registerLoggingSource( subject );
  }

  public LogChannel( Object subject, boolean gatheringMetrics ) {
    this( subject );
    this.gatheringMetrics = gatheringMetrics;
  }

  public LogChannel( Object subject, boolean gatheringMetrics, boolean isPurgeable ) {
    logLevel = DefaultLogLevel.getLogLevel();
    this.gatheringMetrics = gatheringMetrics;
    logChannelId = LoggingRegistry.getInstance().registerLoggingSource( subject, isPurgeable );
  }

  public LogChannel( Object subject, LoggingObjectInterface parentObject ) {
    if ( parentObject != null ) {
      this.logLevel = parentObject.getLogLevel();
      this.containerObjectId = parentObject.getContainerObjectId();
    } else {
      this.logLevel = DefaultLogLevel.getLogLevel();
      this.containerObjectId = null;
    }
    logChannelId = LoggingRegistry.getInstance().registerLoggingSource( subject );
  }

  public LogChannel( Object subject, LoggingObjectInterface parentObject, boolean gatheringMetrics ) {
    this( subject, parentObject );
    this.gatheringMetrics = gatheringMetrics;
  }

  @Override
  public String toString() {
    return logChannelId;
  }

  @Override
  public String getLogChannelId() {
    return logChannelId;
  }

  public void println( LogMessageInterface logMessage, LogLevel channelLogLevel ) {
    String subject = logMessage.getSubject();

    if ( !logMessage.getLevel().isVisible( channelLogLevel ) ) {
      return; // not for our eyes.
    }

    if ( subject == null ) {
      subject = DEFAULT_LOG_SUBJECT;
    }

    // Are the message filtered?
    // apply filter if defined, and not Error level
    boolean applyFilter = !logMessage.getLevel().isError() && !Utils.isEmpty( filter );
    if ( applyFilter && !subject.contains( filter ) && !logMessage.toString().contains( filter ) ) {
      return; // "filter" not found in row: don't show!
    }

    // Let's not keep everything...
    //
    if ( channelLogLevel.getLevel() >= logMessage.getLevel().getLevel() ) {
      KettleLoggingEvent loggingEvent = new KettleLoggingEvent( logMessage, System.currentTimeMillis(),
        logMessage.getLevel() );
      KettleLogStore.getAppender().addLogggingEvent( loggingEvent );

      if ( this.fileWriter == null ) {
        this.fileWriter = LoggingRegistry.getInstance().getLogChannelFileWriterBuffer( logChannelId );
      }

      // add to buffer
      if ( this.fileWriter != null ) {
        this.fileWriter.addEvent( loggingEvent );
      }
    }
  }

  public void println( LogMessageInterface message, Throwable e, LogLevel channelLogLevel ) {
    println( message, channelLogLevel );

    String stackTrace = Const.getStackTracker( e );
    LogMessage traceMessage = new LogMessage( stackTrace, message.getLogChannelId(), LogLevel.ERROR );
    println( traceMessage, channelLogLevel );
  }

  @Override
  public void logMinimal( String s ) {
    println( new LogMessage( s, logChannelId, LogLevel.MINIMAL ), logLevel );
  }

  @Override
  public void logBasic( String s ) {
    println( new LogMessage( s, logChannelId, LogLevel.BASIC ), logLevel );
  }

  @Override
  public void logError( String s ) {
    println( new LogMessage( s, logChannelId, LogLevel.ERROR ), logLevel );
  }

  @Override
  public void logError( String s, Throwable e ) {
    println( new LogMessage( s, logChannelId, LogLevel.ERROR ), e, logLevel );
  }

  @Override
  public void logBasic( String s, Object... arguments ) {
    println( new LogMessage( s, logChannelId, arguments, LogLevel.BASIC ), logLevel );
  }

  @Override
  public void logDetailed( String s, Object... arguments ) {
    println( new LogMessage( s, logChannelId, arguments, LogLevel.DETAILED ), logLevel );
  }

  @Override
  public void logError( String s, Object... arguments ) {
    println( new LogMessage( s, logChannelId, arguments, LogLevel.ERROR ), logLevel );
  }

  @Override
  public void logDetailed( String s ) {
    println( new LogMessage( s, logChannelId, LogLevel.DETAILED ), logLevel );
  }

  @Override
  public void logDebug( String s ) {
    println( new LogMessage( s, logChannelId, LogLevel.DEBUG ), logLevel );
  }

  @Override
  public void logDebug( String message, Object... arguments ) {
    println( new LogMessage( message, logChannelId, arguments, LogLevel.DEBUG ), logLevel );
  }

  @Override
  public void logRowlevel( String s ) {
    println( new LogMessage( s, logChannelId, LogLevel.ROWLEVEL ), logLevel );
  }

  @Override
  public void logMinimal( String message, Object... arguments ) {
    println( new LogMessage( message, logChannelId, arguments, LogLevel.MINIMAL ), logLevel );
  }

  @Override
  public void logRowlevel( String message, Object... arguments ) {
    println( new LogMessage( message, logChannelId, arguments, LogLevel.ROWLEVEL ), logLevel );
  }

  @Override
  public boolean isBasic() {
    return logLevel.isBasic();
  }

  @Override
  public boolean isDebug() {
    return logLevel.isDebug();
  }

  @Override
  public boolean isDetailed() {
    try {
      return logLevel.isDetailed();
    } catch ( NullPointerException ex ) {
      return false;
    }
  }

  @Override
  public boolean isRowLevel() {
    return logLevel.isRowlevel();
  }

  @Override
  public boolean isError() {
    return logLevel.isError();
  }

  @Override
  public LogLevel getLogLevel() {
    return logLevel;
  }

  @Override
  public void setLogLevel( LogLevel logLevel ) {
    this.logLevel = logLevel;
  }

  /**
   * @return the containerObjectId
   */
  @Override
  public String getContainerObjectId() {
    return containerObjectId;
  }

  /**
   * @param containerObjectId the containerObjectId to set
   */
  @Override
  public void setContainerObjectId( String containerObjectId ) {
    this.containerObjectId = containerObjectId;
  }

  /**
   * @return the gatheringMetrics
   */
  @Override
  public boolean isGatheringMetrics() {
    return gatheringMetrics;
  }

  /**
   * @param gatheringMetrics the gatheringMetrics to set
   */
  @Override
  public void setGatheringMetrics( boolean gatheringMetrics ) {
    this.gatheringMetrics = gatheringMetrics;
  }

  @Override
  public boolean isForcingSeparateLogging() {
    return forcingSeparateLogging;
  }

  @Override
  public void setForcingSeparateLogging( boolean forcingSeparateLogging ) {
    this.forcingSeparateLogging = forcingSeparateLogging;
  }

  @Override
  public void snap( MetricsInterface metric, long... value ) {
    snap( metric, null, value );
  }

  @Override
  public void snap( MetricsInterface metric, String subject, long... value ) {
    if ( !isGatheringMetrics() ) {
      return;
    }

    String key = MetricsSnapshot.getKey( metric, subject );
    Map<String, MetricsSnapshotInterface> metricsMap;
    MetricsSnapshotInterface snapshot;
    Queue<MetricsSnapshotInterface> metricsList;
    switch ( metric.getType() ) {
      case MAX:
        // Calculate and store the maximum value for this metric
        //
        if ( value.length != 1 ) {
          break; // ignore
        }

        metricsMap = metricsRegistry.getSnapshotMap( logChannelId );
        snapshot = metricsMap.get( key );
        if ( snapshot != null ) {
          if ( value[ 0 ] > snapshot.getValue() ) {
            snapshot.setValue( value[ 0 ] );
            snapshot.setDate( new Date() );
          }
        } else {
          snapshot = new MetricsSnapshot( MetricsSnapshotType.MAX, metric, subject, value[ 0 ], logChannelId );
          metricsMap.put( key, snapshot );
        }

        break;
      case MIN:
        // Calculate and store the minimum value for this metric
        //
        if ( value.length != 1 ) {
          break; // ignore
        }

        metricsMap = metricsRegistry.getSnapshotMap( logChannelId );
        snapshot = metricsMap.get( key );
        if ( snapshot != null ) {
          if ( value[ 0 ] < snapshot.getValue() ) {
            snapshot.setValue( value[ 0 ] );
            snapshot.setDate( new Date() );
          }
        } else {
          snapshot = new MetricsSnapshot( MetricsSnapshotType.MIN, metric, subject, value[ 0 ], logChannelId );
          metricsMap.put( key, snapshot );
        }

        break;
      case SUM:
        metricsMap = metricsRegistry.getSnapshotMap( logChannelId );
        snapshot = metricsMap.get( key );
        if ( snapshot != null ) {
          snapshot.setValue( snapshot.getValue() + value[ 0 ] );
        } else {
          snapshot = new MetricsSnapshot( MetricsSnapshotType.SUM, metric, subject, value[ 0 ], logChannelId );
          metricsMap.put( key, snapshot );
        }

        break;
      case COUNT:
        metricsMap = metricsRegistry.getSnapshotMap( logChannelId );
        snapshot = metricsMap.get( key );
        if ( snapshot != null ) {
          snapshot.setValue( snapshot.getValue() + 1L );
        } else {
          snapshot = new MetricsSnapshot( MetricsSnapshotType.COUNT, metric, subject, 1L, logChannelId );
          metricsMap.put( key, snapshot );
        }

        break;
      case START:
        metricsList = metricsRegistry.getSnapshotList( logChannelId );
        snapshot = new MetricsSnapshot( MetricsSnapshotType.START, metric, subject, 1L, logChannelId );
        metricsList.add( snapshot );

        break;
      case STOP:
        metricsList = metricsRegistry.getSnapshotList( logChannelId );
        snapshot = new MetricsSnapshot( MetricsSnapshotType.STOP, metric, subject, 1L, logChannelId );
        metricsList.add( snapshot );

        break;
      default:
        break;
    }

  }

  @Override
  public String getFilter() {
    return filter;
  }

  @Override
  public void setFilter( String filter ) {
    this.filter = filter;
  }

  /**
   * The global log channels are initialized on class load with
   * whatever the default log level is at the time.  This method
   * allows changing that value after load.
   */
  static void updateGlobalLogChannels( LogLevel level ) {
    GENERAL.setLogLevel( level );
    UI.setLogLevel( level );
    METADATA.setLogLevel( level );
  }
}
