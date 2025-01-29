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
import org.apache.logging.log4j.core.util.Transform;
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
public class PurgeUtilityHTMLLayout implements StringLayout, IPurgeUtilityLayout {

  protected static final int BUF_SIZE = 256;
  protected static final int MAX_CAPACITY = 1024;
  protected static final String fontCss = "font-family: arial,sans-serif; font-size: x-small";
  protected static final String thCss = "background: #336699; color: #FFFFFF; text-align: left";
  static String TRACE_PREFIX = "<br>&nbsp;&nbsp;&nbsp;&nbsp;";
  public static final String LINE_SEP = System.getProperty("line.separator");
  private static final String REGEXP = Strings.LINE_SEPARATOR.equals("\n") ? "\n" : Strings.LINE_SEPARATOR + "|\n";

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

      sbuf.append( LINE_SEP + "<tr>" + LINE_SEP );

      sbuf.append( "<td>" );
      sbuf.append( Transform.escapeHtmlTags( time ) );
      sbuf.append( "</td>" + LINE_SEP );
    }

    sbuf.append( "<td title=\"Purge File/Folder\">" );
    sbuf.append( Transform.escapeHtmlTags( ThreadContext.get( PurgeUtilityLog.FILE_KEY ) ) );
    sbuf.append( "</td>" + LINE_SEP );

    if ( showLevelColumn() ) {
      sbuf.append( "<td title=\"Level\">" );
      if ( logLevel.equals( Level.DEBUG ) ) {
        sbuf.append( "<font color=\"#339933\">" );
        sbuf.append( Transform.escapeHtmlTags( String.valueOf( event.getLevel() ) ) );
        sbuf.append( "</font>" );
      } else if ( logLevel.isMoreSpecificThan( Level.WARN ) ) {
        sbuf.append( "<font color=\"#993300\"><strong>" );
        sbuf.append( Transform.escapeHtmlTags( String.valueOf( event.getLevel() ) ) );
        sbuf.append( "</strong></font>" );
      } else {
        sbuf.append( Transform.escapeHtmlTags( String.valueOf( event.getLevel() ) ) );
      }
      sbuf.append( "</td>" + LINE_SEP );
    }

    if ( showCodeLineColumn() ) {
      StackTraceElement element = event.getSource();
     sbuf.append( "<td>" );
      sbuf.append( Transform.escapeHtmlTags( ThreadContext.get( PurgeUtilityLogger.CODE_LINE ) ) );
      // sbuf.append( Transform.escapeTags( locInfo.getFileName() ) );
      // sbuf.append( ':' );
      // sbuf.append( element.getLineNumber() );
      sbuf.append( "</td>" + LINE_SEP );
    }

    sbuf.append( "<td title=\"Message\">" );
    sbuf.append( Transform.escapeHtmlTags(event.getMessage().getFormattedMessage()).replaceAll(REGEXP, "<br />") );
    sbuf.append( "</td>" + LINE_SEP );
    sbuf.append( "</tr>" + LINE_SEP );

    if (event.getContextStack() != null && !event.getContextStack().isEmpty()) {
      sbuf.append("<tr><td bgcolor=\"#EEEEEE\" style=\"font-size : ").append("xx-small");
      sbuf.append(";\" colspan=\"6\" ");
      sbuf.append("title=\"Nested Diagnostic Context\">");
      sbuf.append("NDC: ").append(Transform.escapeHtmlTags(event.getContextStack().toString()));
      sbuf.append( "</td></tr>" + LINE_SEP );
    }

    String[] s = event.getContextStack().asList().toArray( new String[0] );
    if ( s != null ) {
      sbuf.append( "<tr><td bgcolor=\"#993300\" style=\"color:White; font-size : xx-small;\" colspan=\"6\">" );
      appendThrowableAsHTML( s, sbuf );
      sbuf.append( "</td></tr>" + LINE_SEP );
    }

    return sbuf.toString();
  }

  void appendThrowableAsHTML( String[] s, StringBuffer sbuf ) {
    if ( s != null ) {
      int len = s.length;
      if ( len == 0 ) {
        return;
      }
      sbuf.append( Transform.escapeHtmlTags( s[0] ) );
      sbuf.append( LINE_SEP );
      for ( int i = 1; i < len; i++ ) {
        sbuf.append( TRACE_PREFIX );
        sbuf.append( Transform.escapeHtmlTags( s[i] ) );
        sbuf.append( LINE_SEP );
      }
    }
  }

  /**
   * Returns appropriate HTML headers.
   */
  @Override
  public byte[] getHeader() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append( "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" "
        + "\"http://www.w3.org/TR/html4/loose.dtd\">" + LINE_SEP );
    sbuf.append( "<html>" + LINE_SEP );
    sbuf.append( "<head>" + LINE_SEP );
    sbuf.append( "<title>" + title + "</title>" + LINE_SEP );
    sbuf.append( "<style type=\"text/css\">" + LINE_SEP );
    sbuf.append( "<!--" + LINE_SEP );
    sbuf.append( "body, table {font-family: arial,sans-serif; font-size: x-small;}" + LINE_SEP );
    sbuf.append( "th {background: #336699; color: #FFFFFF; text-align: left;}" + LINE_SEP );
    sbuf.append( "-->" + LINE_SEP );
    sbuf.append( "</style>" + LINE_SEP );
    sbuf.append( "</head>" + LINE_SEP );
    sbuf.append( "<body bgcolor=\"#FFFFFF\" topmargin=\"6\" leftmargin=\"6\" style=\"" + fontCss + "\">"
        + LINE_SEP );
    sbuf.append( "<hr size=\"1\" noshade>" + LINE_SEP );
    sbuf.append( "Log session start time " + new Date() + "<br>" + LINE_SEP );
    sbuf.append( "<br>" + LINE_SEP );
    sbuf.append( "<table cellspacing=\"0\" cellpadding=\"4\" border=\"1\" bordercolor=\"#224466\" width=\"100%\">"
        + LINE_SEP );
    sbuf.append( "<tr style=\"" + thCss + "\">" + LINE_SEP );
    if ( showTimeColumn() ) {
      sbuf.append( "<th>Time</th>" + LINE_SEP );
    }
    sbuf.append( "<th>File/Folder</th>" + LINE_SEP );
    if ( showLevelColumn() ) {
      sbuf.append( "<th>Level</th>" + LINE_SEP );
    }
    if ( showCodeLineColumn() ) {
      sbuf.append( "<th>File:Line</th>" + LINE_SEP );
    }
    sbuf.append( "<th>Message</th>" + LINE_SEP );
    sbuf.append( "</tr>" + LINE_SEP );
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
   * Returns the appropriate HTML footers.
   */
  @Override
  public byte[] getFooter() {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append( "</table>" + LINE_SEP );
    sbuf.append( "<br>" + LINE_SEP );
    sbuf.append( "</body></html>" );
    return sbuf.toString().getBytes(StandardCharsets.UTF_8);
  }

  /**
   * The HTML layout handles the throwable contained in logging events. Hence, this method return <code>false</code>.
   */
  public boolean ignoresThrowable() {
    return false;
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
