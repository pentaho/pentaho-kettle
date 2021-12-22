/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
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

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.util.Strings;

/**
 * This class was derived from Log4j HTMLLayout.
 * 
 * Appenders using this layout should have their encoding set to UTF-8 or UTF-16, otherwise events containing non ASCII
 * characters could result in corrupted log files.
 * 
 * @author tkafalas
 */
public class PurgeUtilityTextLayout extends AbstractStringLayout implements IPurgeUtilityLayout {

  protected static final int BUF_SIZE = 256;
  protected static final int MAX_CAPACITY = 1024;

  private static final Logger LOGGER = LogManager.getLogger( PurgeUtilityTextLayout.class );
  private Level loggerLogLevel = Level.DEBUG;

  // output buffer appended to when format() is invoked
  private StringBuffer sbuf = new StringBuffer( BUF_SIZE );

  static final String title = "Purge Utility Log";
  static final String footer = "End of Log";

  public PurgeUtilityTextLayout( Level loggerLogLevel ) {
    super(Charset.forName("UTF-8"));
    //super(((LoggerContext)LogManager.getContext(false)).getConfiguration(), title.getBytes(), footer.getBytes());
    this.loggerLogLevel = loggerLogLevel;
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

  public byte[] format( LogEvent event ) {

    if ( sbuf.capacity() > MAX_CAPACITY ) {
      sbuf = new StringBuffer( BUF_SIZE );
    } else {
      sbuf.setLength( 0 );
    }

    sbuf.append( Strings.LINE_SEPARATOR );

    if ( showTimeColumn() ) {
      DateFormat df = new SimpleDateFormat( "MM/dd/yyyy HH:mm:ss" );
      Date date = new Date();
      date.setTime( event.getTimeMillis() );
      String time = null;
      try {
        time = df.format( date );
      } catch ( Exception ex ) {
        LOGGER.error( "Error occured while converting date.", ex );
      }

      sbuf.append( time );
    }

    // File/Folder
    sbuf.append( "\t" );
    sbuf.append( ThreadContext.get( PurgeUtilityLog.FILE_KEY ) );

    // debug level
    if ( showLevelColumn() ) {
      sbuf.append( "\t" );
      sbuf.append( String.valueOf( event.getLevel() ) );
    }

    // Code class and line
    if ( showCodeLineColumn() ) {
      sbuf.append( "\t" );
      sbuf.append( ThreadContext.get( PurgeUtilityLogger.CODE_LINE ) );
    }

    // Message
    sbuf.append( "\t" );
    sbuf.append( event.getMessage().getFormattedMessage() );

    return sbuf.toString().getBytes();
  }

  /**
   * Returns appropriate headers.
   */
  public byte[] getHeader() {
    return title.getBytes();
  }

  /**
   * Returns the appropriate footers.
   */
  public byte[] getFooter() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append( footer );
    return sbuf.toString().getBytes();
  }

  /**
   * The layout does not handle the throwable contained in logging events. Hence, this method return <code>true</code>.
   */
  public boolean ignoresThrowable() {
    return true;
  }

  private boolean showCodeLineColumn() {
	  return Level.DEBUG.compareTo( loggerLogLevel ) >= 0 ? true : false;
  }

  private boolean showTimeColumn() {
    return Level.DEBUG.compareTo( loggerLogLevel ) >= 0 ? true : false;
  }

  private boolean showLevelColumn() {
    return true;
  }

  @Override
  public String toSerializable(LogEvent event) {
    return new String(format(event));
  }
}
