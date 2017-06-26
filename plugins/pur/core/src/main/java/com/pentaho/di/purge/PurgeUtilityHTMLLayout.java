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

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.Transform;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.MDC;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class was derived from Log4j HTMLLayout.
 * 
 * Appenders using this layout should have their encoding set to UTF-8 or UTF-16, otherwise events containing non ASCII
 * characters could result in corrupted log files.
 * 
 * @author tkafalas
 */
public class PurgeUtilityHTMLLayout extends Layout implements IPurgeUtilityLayout {

  protected static final int BUF_SIZE = 256;
  protected static final int MAX_CAPACITY = 1024;
  protected static final String fontCss = "font-family: arial,sans-serif; font-size: x-small";
  protected static final String thCss = "background: #336699; color: #FFFFFF; text-align: left";
  static String TRACE_PREFIX = "<br>&nbsp;&nbsp;&nbsp;&nbsp;";

  private Level loggerLogLevel = Level.DEBUG;

  // output buffer appended to when format() is invoked
  private StringBuffer sbuf = new StringBuffer( BUF_SIZE );

  String title = "Log4J Log Messages";

  public PurgeUtilityHTMLLayout( Level loggerLogLevel ) {
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
    return "text/html";
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

      sbuf.append( Layout.LINE_SEP + "<tr>" + Layout.LINE_SEP );

      sbuf.append( "<td>" );
      sbuf.append( Transform.escapeTags( time ) );
      sbuf.append( "</td>" + Layout.LINE_SEP );
    }

    sbuf.append( "<td title=\"Purge File/Folder\">" );
    sbuf.append( Transform.escapeTags( MDC.get( PurgeUtilityLog.FILE_KEY ) ) );
    sbuf.append( "</td>" + Layout.LINE_SEP );

    if ( showLevelColumn() ) {
      sbuf.append( "<td title=\"Level\">" );
      if ( logLevel.equals( Level.DEBUG ) ) {
        sbuf.append( "<font color=\"#339933\">" );
        sbuf.append( Transform.escapeTags( String.valueOf( event.getLevel() ) ) );
        sbuf.append( "</font>" );
      } else if ( logLevel.isGreaterOrEqual( Level.WARN ) ) {
        sbuf.append( "<font color=\"#993300\"><strong>" );
        sbuf.append( Transform.escapeTags( String.valueOf( event.getLevel() ) ) );
        sbuf.append( "</strong></font>" );
      } else {
        sbuf.append( Transform.escapeTags( String.valueOf( event.getLevel() ) ) );
      }
      sbuf.append( "</td>" + Layout.LINE_SEP );
    }

    if ( showCodeLineColumn() ) {
      LocationInfo locInfo = event.getLocationInformation();
      sbuf.append( "<td>" );
      sbuf.append( Transform.escapeTags( MDC.get( PurgeUtilityLogger.CODE_LINE ) ) );
      // sbuf.append( Transform.escapeTags( locInfo.getFileName() ) );
      // sbuf.append( ':' );
      // sbuf.append( locInfo.getLineNumber() );
      sbuf.append( "</td>" + Layout.LINE_SEP );
    }

    sbuf.append( "<td title=\"Message\">" );
    sbuf.append( Transform.escapeTags( event.getRenderedMessage() ) );
    sbuf.append( "</td>" + Layout.LINE_SEP );
    sbuf.append( "</tr>" + Layout.LINE_SEP );

    if ( event.getNDC() != null ) {
      sbuf.append( "<tr><td bgcolor=\"#EEEEEE\" style=\"font-size : "
          + "xx-small;\" colspan=\"6\" title=\"Nested Diagnostic Context\">" );
      sbuf.append( "NDC: " + Transform.escapeTags( event.getNDC() ) );
      sbuf.append( "</td></tr>" + Layout.LINE_SEP );
    }

    String[] s = event.getThrowableStrRep();
    if ( s != null ) {
      sbuf.append( "<tr><td bgcolor=\"#993300\" style=\"color:White; font-size : xx-small;\" colspan=\"6\">" );
      appendThrowableAsHTML( s, sbuf );
      sbuf.append( "</td></tr>" + Layout.LINE_SEP );
    }

    return sbuf.toString();
  }

  void appendThrowableAsHTML( String[] s, StringBuffer sbuf ) {
    if ( s != null ) {
      int len = s.length;
      if ( len == 0 ) {
        return;
      }
      sbuf.append( Transform.escapeTags( s[0] ) );
      sbuf.append( Layout.LINE_SEP );
      for ( int i = 1; i < len; i++ ) {
        sbuf.append( TRACE_PREFIX );
        sbuf.append( Transform.escapeTags( s[i] ) );
        sbuf.append( Layout.LINE_SEP );
      }
    }
  }

  /**
   * Returns appropriate HTML headers.
   */
  public String getHeader() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append( "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" "
        + "\"http://www.w3.org/TR/html4/loose.dtd\">" + Layout.LINE_SEP );
    sbuf.append( "<html>" + Layout.LINE_SEP );
    sbuf.append( "<head>" + Layout.LINE_SEP );
    sbuf.append( "<title>" + title + "</title>" + Layout.LINE_SEP );
    sbuf.append( "<style type=\"text/css\">" + Layout.LINE_SEP );
    sbuf.append( "<!--" + Layout.LINE_SEP );
    sbuf.append( "body, table {font-family: arial,sans-serif; font-size: x-small;}" + Layout.LINE_SEP );
    sbuf.append( "th {background: #336699; color: #FFFFFF; text-align: left;}" + Layout.LINE_SEP );
    sbuf.append( "-->" + Layout.LINE_SEP );
    sbuf.append( "</style>" + Layout.LINE_SEP );
    sbuf.append( "</head>" + Layout.LINE_SEP );
    sbuf.append( "<body bgcolor=\"#FFFFFF\" topmargin=\"6\" leftmargin=\"6\" style=\"" + fontCss + "\">"
        + Layout.LINE_SEP );
    sbuf.append( "<hr size=\"1\" noshade>" + Layout.LINE_SEP );
    sbuf.append( "Log session start time " + new Date() + "<br>" + Layout.LINE_SEP );
    sbuf.append( "<br>" + Layout.LINE_SEP );
    sbuf.append( "<table cellspacing=\"0\" cellpadding=\"4\" border=\"1\" bordercolor=\"#224466\" width=\"100%\">"
        + Layout.LINE_SEP );
    sbuf.append( "<tr style=\"" + thCss + "\">" + Layout.LINE_SEP );
    if ( showTimeColumn() ) {
      sbuf.append( "<th>Time</th>" + Layout.LINE_SEP );
    }
    sbuf.append( "<th>File/Folder</th>" + Layout.LINE_SEP );
    if ( showLevelColumn() ) {
      sbuf.append( "<th>Level</th>" + Layout.LINE_SEP );
    }
    if ( showCodeLineColumn() ) {
      sbuf.append( "<th>File:Line</th>" + Layout.LINE_SEP );
    }
    sbuf.append( "<th>Message</th>" + Layout.LINE_SEP );
    sbuf.append( "</tr>" + Layout.LINE_SEP );
    return sbuf.toString();
  }

  /**
   * Returns the appropriate HTML footers.
   */
  public String getFooter() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append( "</table>" + Layout.LINE_SEP );
    sbuf.append( "<br>" + Layout.LINE_SEP );
    sbuf.append( "</body></html>" );
    return sbuf.toString();
  }

  /**
   * The HTML layout handles the throwable contained in logging events. Hence, this method return <code>false</code>.
   */
  public boolean ignoresThrowable() {
    return false;
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
