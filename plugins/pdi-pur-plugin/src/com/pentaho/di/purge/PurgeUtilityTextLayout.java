/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.pentaho.di.purge;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.MDC;

/**
 * This class was derived from Log4j HTMLLayout.
 * 
 * Appenders using this layout should have their encoding set to UTF-8 or UTF-16, otherwise events containing non ASCII
 * characters could result in corrupted log files.
 * 
 * @author tkafalas
 */
public class PurgeUtilityTextLayout extends Layout implements IPurgeUtilityLayout {

  protected static final int BUF_SIZE = 256;
  protected static final int MAX_CAPACITY = 1024;

  private Level loggerLogLevel = Level.DEBUG;

  // output buffer appended to when format() is invoked
  private StringBuffer sbuf = new StringBuffer( BUF_SIZE );

  String title = "Log4J Log Messages";

  public PurgeUtilityTextLayout( Level loggerLogLevel ) {
    super();
    this.loggerLogLevel = loggerLogLevel;
  }

  /**
   * The <b>Title</b> option takes a String value. This option sets the document title of the generated HTML document.
   * 
   * <p>
   * Defaults to 'Log4J Log Messages'.
   */
  public void setTitle( String title ) {
    this.title = title;
  }

  /**
   * Returns the current value of the <b>Title</b> option.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns the content type output by this layout, i.e "text/html".
   */
  public String getContentType() {
    return "text/plain";
  }

  /**
   * No options to activate.
   */
  public void activateOptions() {
  }

  public String format( LoggingEvent event ) {

    Level logLevel = event.getLevel();
    if ( sbuf.capacity() > MAX_CAPACITY ) {
      sbuf = new StringBuffer( BUF_SIZE );
    } else {
      sbuf.setLength( 0 );
    }

    sbuf.append( Layout.LINE_SEP );

    if ( showTimeColumn() ) {
      DateFormat df = new SimpleDateFormat( "MM/dd/yyyy HH:mm:ss" );
      Date date = new Date();
      date.setTime( event.timeStamp );
      String time = null;
      try {
        time = df.format( date );
      } catch ( Exception ex ) {
        LogLog.error( "Error occured while converting date.", ex );
      }

      sbuf.append( time );
    }

    // File/Folder
    sbuf.append( "\t" );
    sbuf.append( MDC.get( PurgeUtilityLog.FILE_KEY ) );

    // debug level
    if ( showLevelColumn() ) {
      sbuf.append( "\t" );
      sbuf.append( String.valueOf( event.getLevel() ) );
    }

    // Code class and line
    if ( showCodeLineColumn() ) {
      sbuf.append( "\t" );
      sbuf.append( MDC.get( PurgeUtilityLogger.CODE_LINE ) );
    }

    // Message
    sbuf.append( "\t" );
    sbuf.append( event.getRenderedMessage() );

    return sbuf.toString();
  }

  /**
   * Returns appropriate headers.
   */
  public String getHeader() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append( title );
    return sbuf.toString();
  }

  /**
   * Returns the appropriate footers.
   */
  public String getFooter() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append( "End of Log" );
    return sbuf.toString();
  }

  /**
   * The layout does not handle the throwable contained in logging events. Hence, this method return <code>true</code>.
   */
  public boolean ignoresThrowable() {
    return true;
  }

  private boolean showCodeLineColumn() {
    return Level.DEBUG.isGreaterOrEqual( loggerLogLevel ) ? true : false;
  }

  private boolean showTimeColumn() {
    return Level.DEBUG.isGreaterOrEqual( loggerLogLevel ) ? true : false;
  }

  private boolean showLevelColumn() {
    return true;
  }
}
