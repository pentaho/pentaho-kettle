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


package org.pentaho.di.core.logging;

import java.io.PrintStream;

import org.pentaho.di.core.Const;

public class LoggingPrintStream extends PrintStream {

  private static LogChannelInterface log = LogChannel.GENERAL;

  public LoggingPrintStream( PrintStream printStream ) {
    super( printStream );
  }

  @Override
  public void println() {
    log.logDebug( Const.CR );
  }

  @Override
  public void print( boolean b ) {
    log.logDebug( Boolean.toString( b ) );
  }

  @Override
  public void println( boolean b ) {
    log.logDebug( Boolean.toString( b ) + Const.CR );
  }

  @Override
  public void print( char c ) {
    log.logDebug( Character.toString( c ) );
  }

  @Override
  public void println( char c ) {
    log.logDebug( Character.toString( c ) + Const.CR );
  }

  @Override
  public void print( char[] s ) {
    log.logDebug( String.copyValueOf( s ) );
  }

  @Override
  public void println( char[] s ) {
    log.logDebug( String.copyValueOf( s ) + Const.CR );
  }

  @Override
  public void print( double d ) {
    log.logDebug( Double.toString( d ) );
  }

  @Override
  public void println( double d ) {
    log.logDebug( Double.toString( d ) + Const.CR );
  }

  @Override
  public void print( float f ) {
    log.logDebug( Float.toString( f ) );
  }

  @Override
  public void println( float f ) {
    log.logDebug( Float.toString( f ) + Const.CR );
  }

  @Override
  public void print( int i ) {
    log.logDebug( Integer.toString( i ) );
  }

  @Override
  public void println( int i ) {
    log.logDebug( Integer.toString( i ) + Const.CR );
  }

  @Override
  public void print( long l ) {
    log.logDebug( Long.toString( l ) );
  }

  @Override
  public void println( long l ) {
    log.logDebug( Long.toString( l ) + Const.CR );
  }

  @Override
  public void print( Object obj ) {
    log.logDebug( obj.toString() );
  }

  @Override
  public void println( Object obj ) {
    log.logDebug( obj.toString() + Const.CR );
  }

  @Override
  public void print( String s ) {
    log.logDebug( s );
  }

  @Override
  public void println( String s ) {
    log.logDebug( s + Const.CR );
  }

  @Override
  public PrintStream append( char c ) {
    log.logDebug( "" + c );
    return this;
  }

  @Override
  public PrintStream append( CharSequence csq ) {
    log.logDebug( csq.toString() );
    return this;
  }

  @Override
  public PrintStream append( CharSequence csq, int start, int end ) {
    log.logDebug( csq.subSequence( start, end ).toString() );
    return this;
  }
}
