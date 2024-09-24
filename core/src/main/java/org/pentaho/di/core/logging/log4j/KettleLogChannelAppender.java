/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.di.core.logging.log4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/*
 * This class acts as a bridge between log4j and the Kettle log channel system.
 * Use it if you need to get log messages from third-party code to a log channel.
 */
public class KettleLogChannelAppender implements Appender {
  /**
   * Maps Kettle LogLevels to Log4j Levels
   */
  public static final Map<LogLevel, Level> LOG_LEVEL_MAP;

  static {
    EnumMap<LogLevel, Level> map = new EnumMap<>( LogLevel.class );
    map.put( LogLevel.BASIC, Level.INFO );
    map.put( LogLevel.MINIMAL, Level.INFO );
    map.put( LogLevel.DEBUG, Level.DEBUG );
    map.put( LogLevel.ERROR, Level.ERROR );
    map.put( LogLevel.DETAILED, Level.INFO );
    map.put( LogLevel.ROWLEVEL, Level.DEBUG );
    map.put( LogLevel.NOTHING, Level.OFF );
    LOG_LEVEL_MAP = Collections.unmodifiableMap( map );
  }

  private final LogChannelInterface kettleLogChannelInterface;
  private final Layout<String> layout;
  private volatile State state;
  private ErrorHandler errorHandler;

  public KettleLogChannelAppender( LogChannelInterface log, Log4jKettleLayout layout ) {
    kettleLogChannelInterface = log;
    this.layout = layout;
    this.state = State.INITIALIZED;
    this.errorHandler = new ErrorHandler() {
      @Override public void error( String msg ) {
        kettleLogChannelInterface.logError( msg );
      }

      @Override public void error( String msg, Throwable t ) {
        kettleLogChannelInterface.logError( msg, t );
      }

      @Override public void error( String msg, LogEvent event, Throwable t ) {
        kettleLogChannelInterface.logError( msg, t );
      }
    };
  }

  public void append( LogEvent event ) {
    String s = layout.toSerializable( event );

    if ( Level.DEBUG.equals( event.getLevel() ) ) {
      kettleLogChannelInterface.logDebug( s );
    } else if ( Level.ERROR.equals( event.getLevel() )
      || Level.FATAL.equals( event.getLevel() ) ) {
      Throwable t = event.getThrown();
      if ( t == null ) {
        kettleLogChannelInterface.logError( s );
      } else {
        kettleLogChannelInterface.logError( s, t );
      }
    } else if ( Level.TRACE.equals( event.getLevel() ) ) {
      kettleLogChannelInterface.logRowlevel( s );
    } else if ( Level.OFF.equals( event.getLevel() ) ) {
      kettleLogChannelInterface.logMinimal( s );
    } else {
      // ALL, WARN, INFO, or others
      kettleLogChannelInterface.logBasic( s );
    }
  }

  @Override public String getName() {
    return kettleLogChannelInterface.getLogChannelId();
  }

  @Override public Layout<? extends Serializable> getLayout() {
    return layout;
  }

  @Override public boolean ignoreExceptions() {
    return true;
  }

  @Override public ErrorHandler getHandler() {
    return errorHandler;
  }

  @Override public void setHandler( ErrorHandler handler ) {
    this.errorHandler = handler;
  }

  @Override public State getState() {
    return state;
  }

  @Override public void initialize() {
    // no-op
  }

  @Override public void start() {
    this.state = State.STARTED;
  }

  @Override public void stop() {
    this.state = State.STOPPED;
  }

  @Override public boolean isStarted() {
    return State.STARTED.equals( state );
  }

  @Override public boolean isStopped() {
    return State.STOPPED.equals( state );
  }
}
