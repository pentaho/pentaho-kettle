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

package com.pentaho.di.purge;

import java.io.OutputStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.MDC;

/**
 * 
 * @author TKafalas
 * 
 */
public class PurgeUtilityLogger implements IPurgeUtilityLogger {

  static private ThreadLocal<PurgeUtilityLogger> purgeUtilityLogger = new ThreadLocal<PurgeUtilityLogger>();
  static private ThreadLocal<PurgeUtilityLog> purgeUtilityLog = new ThreadLocal<PurgeUtilityLog>();
  private boolean isFormalLogger;
  static final String CODE_LINE = "codeLine";

  private PurgeUtilityLogger() {
    isFormalLogger = false;
    purgeUtilityLog.set( new PurgeUtilityLog() );
  }

  private PurgeUtilityLogger( OutputStream outputStream, String purgePath, Level logLevel ) {
    purgeUtilityLog.set( new PurgeUtilityLog( outputStream, purgePath, logLevel ) );
    isFormalLogger = true;
    getPurgeUtilityLog().setCurrentFilePath( getPurgeUtilityLog().getPurgePath() );
    info( "Start Purge Utility" );
  }

  public static void createNewInstance( OutputStream outputStream, String purgePath, Level logLevel ) {
    purgeUtilityLogger.set( new PurgeUtilityLogger( outputStream, purgePath, logLevel ) );
  }

  public static PurgeUtilityLogger getPurgeUtilityLogger() {
    if ( purgeUtilityLogger.get() == null ) {
      purgeUtilityLogger.set( new PurgeUtilityLogger() );
    }
    return purgeUtilityLogger.get();
  }

  public void endJob() {
    if ( isFormalLogger ) {
      getPurgeUtilityLog().setCurrentFilePath( getPurgeUtilityLog().getPurgePath() );
      info( "End Purge Utility" );
      getPurgeUtilityLog().endJob();
    }
  }

  public void setCurrentFilePath( String currentFilePath ) {
    if ( isFormalLogger ) {
      getPurgeUtilityLog().setCurrentFilePath( currentFilePath );
    }
  }

  public void info( String s ) {
    setCodeLine();
    getLogger().info( s );
  }

  public void error( String s ) {
    setCodeLine();
    getLogger().error( s );
  }

  public void debug( String s ) {
    setCodeLine();
    getLogger().debug( s );
  }

  public void warn( String s ) {
    setCodeLine();
    getLogger().debug( s );
  }

  @Override
  public void error( Exception e ) {
    setCodeLine();
    getLogger().error( throwableToString( e ) );

  }

  private PurgeUtilityLog getPurgeUtilityLog() {
    PurgeUtilityLog currentLog = purgeUtilityLog.get();
    if ( currentLog == null ) {
      throw new IllegalStateException( "No job started for current Thread" );
    }
    return currentLog;
  }

  private Logger getLogger() {
    return (Logger) getPurgeUtilityLog().getLogger();
  }

  public boolean hasLogger() {
    return ( purgeUtilityLog.get() == null ) ? false : true;
  }

  @Override
  public void debug( Object arg0 ) {
    setCodeLine();
    getLogger().debug( arg0 );
  }

  @Override
  public void debug( Object arg0, Throwable arg1 ) {
    setCodeLine();
    getLogger().debug( arg0, arg1 );
  }

  @Override
  public void error( Object arg0 ) {
    setCodeLine();
    getLogger().error( arg0 );
  }

  @Override
  public void error( Object arg0, Throwable arg1 ) {
    setCodeLine();
    getLogger().error( arg0, arg1 );

  }

  @Override
  public void fatal( Object arg0 ) {
    setCodeLine();
    getLogger().fatal( arg0 );

  }

  @Override
  public void fatal( Object arg0, Throwable arg1 ) {
    setCodeLine();
    getLogger().fatal( arg0, arg1 );

  }

  @Override
  public void info( Object arg0 ) {
    setCodeLine();
    getLogger().info( arg0 );

  }

  @Override
  public void info( Object arg0, Throwable arg1 ) {
    setCodeLine();
    getLogger().info( arg0, arg1 );

  }

  @Override
  public boolean isDebugEnabled() {
    setCodeLine();
    return getLogger().isDebugEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    setCodeLine();
    return Level.ERROR.isMoreSpecificThan( getLogger().getLevel() );
  }

  @Override
  public boolean isFatalEnabled() {
    setCodeLine();
    return Level.FATAL.isMoreSpecificThan( getLogger().getLevel() );
  }

  @Override
  public boolean isInfoEnabled() {
    setCodeLine();
    return getLogger().isInfoEnabled();
  }

  @Override
  public boolean isTraceEnabled() {
    setCodeLine();
    return getLogger().isTraceEnabled();
  }

  @Override
  public boolean isWarnEnabled() {
    setCodeLine();
    return Level.WARN.isMoreSpecificThan( getLogger().getLevel() );
  }

  @Override
  public void trace( Object arg0 ) {
    setCodeLine();
    getLogger().trace( arg0 );
  }

  @Override
  public void trace( Object arg0, Throwable arg1 ) {
    setCodeLine();
    getLogger().trace( arg0, arg1 );
  }

  @Override
  public void warn( Object arg0 ) {
    setCodeLine();
    getLogger().warn( arg0 );
  }

  @Override
  public void warn( Object arg0, Throwable arg1 ) {
    setCodeLine();
    getLogger().warn( arg0, arg1 );
  }

  private void setCodeLine() {
    for ( int stackLevel = 1; stackLevel < Thread.currentThread().getStackTrace().length; stackLevel++ ) {
      StackTraceElement ste = Thread.currentThread().getStackTrace()[stackLevel];
      if ( !ste.getClassName().equals( this.getClass().getName() ) ) {
        ThreadContext.put( CODE_LINE, ste.getClassName() + "." + ste.getMethodName() + ":" + ste.getLineNumber() );
        break;
      }
    }
  }

  /**
   * Get the first 20 lines of the stack trace
   */
  private String throwableToString( Throwable e ) {
    final StringBuilder result = new StringBuilder();
    final String LINEFEED = "\n";
    result.append( e.toString() );
    result.append( LINEFEED );
    int lineCount = 1;
    // add lines of stack trace
    for ( StackTraceElement element : e.getStackTrace() ) {
      result.append( element );
      result.append( LINEFEED );
      if ( lineCount++ >= 20 ) {
        break;
      }
    }
    return result.toString();
  }
}
