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
import org.apache.logging.log4j.core.util.Transform;
import org.apache.logging.log4j.util.Strings;

/**
 * This class was derived from Log4j HTMLLayout.
 * 
 * Appenders using this layout should have their encoding set to UTF-8 or UTF-16, otherwise events containing non ASCII
 * characters could result in corrupted log files.
 * 
 * @author tkafalas
 */
public class PurgeUtilityHTMLLayout extends AbstractStringLayout implements IPurgeUtilityLayout {

  protected static final int BUF_SIZE = 256;
  protected static final int MAX_CAPACITY = 1024;
  protected static final String fontCss = "font-family: arial,sans-serif; font-size: x-small";
  protected static final String thCss = "background: #336699; color: #FFFFFF; text-align: left";
  static String TRACE_PREFIX = "<br>&nbsp;&nbsp;&nbsp;&nbsp;";

  private static final Logger LOGGER = LogManager.getLogger( PurgeUtilityHTMLLayout.class );
  private Level loggerLogLevel = Level.DEBUG;

  // output buffer appended to when format() is invoked
  private StringBuffer sbuf = new StringBuffer( BUF_SIZE );

  static final String title = "Purge Utility Log";
  static final String footer = "End of Log";

  public PurgeUtilityHTMLLayout( Level loggerLogLevel ) {
    super(Charset.forName("UTF-8"));
    this.loggerLogLevel = loggerLogLevel;
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

  public byte[] format( LogEvent event ) {

    Level logLevel = event.getLevel();
    if ( sbuf.capacity() > MAX_CAPACITY ) {
      sbuf = new StringBuffer( BUF_SIZE );
    } else {
      sbuf.setLength( 0 );
    }

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

      sbuf.append( Strings.LINE_SEPARATOR + "<tr>" + Strings.LINE_SEPARATOR );

      sbuf.append( "<td>" );
      sbuf.append( Transform.escapeHtmlTags( time ) );
      sbuf.append( "</td>" + Strings.LINE_SEPARATOR );
    }

    sbuf.append( "<td title=\"Purge File/Folder\">" );
    sbuf.append( Transform.escapeHtmlTags( ThreadContext.get( PurgeUtilityLog.FILE_KEY ) ) );
    sbuf.append( "</td>" + Strings.LINE_SEPARATOR );

    if ( showLevelColumn() ) {
      sbuf.append( "<td title=\"Level\">" );
      if ( logLevel.equals( Level.DEBUG ) ) {
        sbuf.append( "<font color=\"#339933\">" );
        sbuf.append( Transform.escapeHtmlTags( String.valueOf( event.getLevel() ) ) );
        sbuf.append( "</font>" );
      } else if ( logLevel.compareTo( Level.WARN ) >= 0 ) {
        sbuf.append( "<font color=\"#993300\"><strong>" );
        sbuf.append( Transform.escapeHtmlTags( String.valueOf( event.getLevel() ) ) );
        sbuf.append( "</strong></font>" );
      } else {
        sbuf.append( Transform.escapeHtmlTags( String.valueOf( event.getLevel() ) ) );
      }
      sbuf.append( "</td>" + Strings.LINE_SEPARATOR );
    }

    if ( showCodeLineColumn() ) {
      sbuf.append( "<td>" );
      sbuf.append( Transform.escapeHtmlTags( ThreadContext.get( PurgeUtilityLogger.CODE_LINE ) ) );
      sbuf.append( "</td>" + Strings.LINE_SEPARATOR );
    }

    sbuf.append( "<td title=\"Message\">" );
    sbuf.append( Transform.escapeHtmlTags( event.getMessage().getFormattedMessage() ) );
    sbuf.append( "</td>" + Strings.LINE_SEPARATOR );
    sbuf.append( "</tr>" + Strings.LINE_SEPARATOR );

    if ( event.getContextStack() != null ) {
      sbuf.append( "<tr><td bgcolor=\"#EEEEEE\" style=\"font-size : "
          + "xx-small;\" colspan=\"6\" title=\"Nested Diagnostic Context\">" );
      appendThrowableAsHTML(event.getContextStack().asList().toArray(new String[event.getContextStack().asList().size()]), sbuf);
      sbuf.append( "</td></tr>" + Strings.LINE_SEPARATOR );
    }

    return sbuf.toString().getBytes();
  }

  void appendThrowableAsHTML( String[] s, StringBuffer sbuf ) {
    if ( s != null ) {
      int len = s.length;
      if ( len == 0 ) {
        return;
      }
      sbuf.append( Transform.escapeHtmlTags( s[0] ) );
      sbuf.append( Strings.LINE_SEPARATOR );
      for ( int i = 1; i < len; i++ ) {
        sbuf.append( TRACE_PREFIX );
        sbuf.append( Transform.escapeHtmlTags( s[i] ) );
        sbuf.append( Strings.LINE_SEPARATOR );
      }
    }
  }

  /**
   * Returns appropriate HTML headers.
   */
  public byte[] getHeader() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append( "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" "
        + "\"http://www.w3.org/TR/html4/loose.dtd\">" + Strings.LINE_SEPARATOR );
    sbuf.append( "<html>" + Strings.LINE_SEPARATOR );
    sbuf.append( "<head>" + Strings.LINE_SEPARATOR );
    sbuf.append( "<title>" + title + "</title>" + Strings.LINE_SEPARATOR );
    sbuf.append( "<style type=\"text/css\">" + Strings.LINE_SEPARATOR );
    sbuf.append( "<!--" + Strings.LINE_SEPARATOR );
    sbuf.append( "body, table {font-family: arial,sans-serif; font-size: x-small;}" + Strings.LINE_SEPARATOR );
    sbuf.append( "th {background: #336699; color: #FFFFFF; text-align: left;}" + Strings.LINE_SEPARATOR );
    sbuf.append( "-->" + Strings.LINE_SEPARATOR );
    sbuf.append( "</style>" + Strings.LINE_SEPARATOR );
    sbuf.append( "</head>" + Strings.LINE_SEPARATOR );
    sbuf.append( "<body bgcolor=\"#FFFFFF\" topmargin=\"6\" leftmargin=\"6\" style=\"" + fontCss + "\">"
        + Strings.LINE_SEPARATOR );
    sbuf.append( "<hr size=\"1\" noshade>" + Strings.LINE_SEPARATOR );
    sbuf.append( "Log session start time " + new Date() + "<br>" + Strings.LINE_SEPARATOR );
    sbuf.append( "<br>" + Strings.LINE_SEPARATOR );
    sbuf.append( "<table cellspacing=\"0\" cellpadding=\"4\" border=\"1\" bordercolor=\"#224466\" width=\"100%\">"
        + Strings.LINE_SEPARATOR );
    sbuf.append( "<tr style=\"" + thCss + "\">" + Strings.LINE_SEPARATOR );
    if ( showTimeColumn() ) {
      sbuf.append( "<th>Time</th>" + Strings.LINE_SEPARATOR );
    }
    sbuf.append( "<th>File/Folder</th>" + Strings.LINE_SEPARATOR );
    if ( showLevelColumn() ) {
      sbuf.append( "<th>Level</th>" + Strings.LINE_SEPARATOR );
    }
    if ( showCodeLineColumn() ) {
      sbuf.append( "<th>File:Line</th>" + Strings.LINE_SEPARATOR );
    }
    sbuf.append( "<th>Message</th>" + Strings.LINE_SEPARATOR );
    sbuf.append( "</tr>" + Strings.LINE_SEPARATOR );
    return sbuf.toString().getBytes();
  }

  /**
   * Returns the appropriate HTML footers.
   */
  public byte[] getFooter() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append( "</table>" + Strings.LINE_SEPARATOR );
    sbuf.append( "<br>" + Strings.LINE_SEPARATOR );
    sbuf.append( "</body></html>" );
    return sbuf.toString().getBytes();
  }

  /**
   * The HTML layout handles the throwable contained in logging events. Hence, this method return <code>false</code>.
   */
  public boolean ignoresThrowable() {
    return false;
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
