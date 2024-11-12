/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package com.pentaho.di.purge;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * This class was derived from Log4j HTML
 * 
 * Appenders using this layout should have their encoding set to UTF-8 or UTF-16, otherwise events containing non ASCII
 * characters could result in corrupted log files.
 * 
 * @author tkafalas
 */
public class PurgeUtilityTextLayout implements StringLayout, IPurgeUtilityLayout {

  protected static final int BUF_SIZE = 256;
  protected static final int MAX_CAPACITY = 1024;
  public static final String LINE_SEP = System.getProperty("line.separator");
  private static final String REGEXP = Strings.LINE_SEPARATOR.equals("\n") ? "\n" : Strings.LINE_SEPARATOR + "|\n";

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

  @Override
  public Map<String, String> getContentFormat() {
    return null;
  }

  /**
   * No options to activate.
   */
  public void activateOptions() {
  }

  public String format( LogEvent event ) {

    Level logLevel = event.getLevel();
    if ( sbuf.capacity() > MAX_CAPACITY ) {
      sbuf = new StringBuffer( BUF_SIZE );
    } else {
      sbuf.setLength( 0 );
    }

    sbuf.append( LINE_SEP );

    if ( showTimeColumn() ) {
      DateFormat df = new SimpleDateFormat( "MM/dd/yyyy HH:mm:ss" );
      Date date = new Date();
      date.setTime( event.getTimeMillis() );
      String time = null;
      try {
        time = df.format( date );
      } catch ( Exception ex ) {
        StatusLogger.getLogger().error( "Error occured while converting date.", ex );
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
    sbuf.append( event.getMessage() );

    return sbuf.toString();
  }

  /**
   * Returns appropriate headers.
   */
  public byte[] getHeader() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append( title );
    return sbuf.toString().getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public byte[] toByteArray(LogEvent event) {
    return format (event).getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public String toSerializable( LogEvent event ) {
    return format (event);
  }

  /**
   * Returns the appropriate footers.
   */
  public byte[] getFooter() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append( "End of Log" );
    return sbuf.toString().getBytes(StandardCharsets.UTF_8);
  }

  /**
   * The layout does not handle the throwable contained in logging events. Hence, this method return <code>true</code>.
   */
  public boolean ignoresThrowable() {
    return true;
  }

  private boolean showCodeLineColumn() {
    return Level.DEBUG.isMoreSpecificThan( loggerLogLevel ) ? true : false;
  }

  private boolean showTimeColumn() {

    return Level.DEBUG.isMoreSpecificThan( loggerLogLevel ) ? true : false;
  }

  private boolean showLevelColumn() {
    return true;
  }

  @Override
  public Charset getCharset() {
    return StandardCharsets.UTF_8;
  }

  @Override
  public void encode(LogEvent source, ByteBufferDestination destination) {

  }
}
