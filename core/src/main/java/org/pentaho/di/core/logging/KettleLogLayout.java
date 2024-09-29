/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.version.BuildVersion;

public class KettleLogLayout {

  protected static Logger logger = LogManager.getLogger( KettleLogLayout.class );
  static LoggerContext context = (LoggerContext) LogManager.getContext( false );
  static Configuration configuration = context.getConfiguration();
  private static final ThreadLocal<SimpleDateFormat> LOCAL_SIMPLE_DATE_PARSER =
    new ThreadLocal<>();
  @VisibleForTesting
  protected static String log4J2Appender = "pdi-execution-appender";

  /**
   * This method will attempt to read and return the date Pattern within the {} from RollingFIle name
   * "pdi-execution-appender" off the log4j2.xml file.
   * If unsuccessful, then it will return the default date Pattern "yyyy/MM/dd HH:mm:ss".
   *
   * @return SimpleDateFormat
   */
  protected static SimpleDateFormat getLog4j2Appender() {
    Optional<Appender> log4j2xmlAppender =
      configuration.getAppenders().values().stream()
        .filter( a -> a.getName().equalsIgnoreCase( log4J2Appender ) ).findFirst();
    if ( log4j2xmlAppender.isPresent() ) {
      ArrayList<String> matchesArray = new ArrayList<>();
      String dateFormatFromLog4j2xml = log4j2xmlAppender.get().getLayout().getContentFormat().get( "format" );
      Pattern pattern = Pattern.compile( "(\\{(.*?)})" );
      Matcher matcher = pattern.matcher( dateFormatFromLog4j2xml );
      while ( matcher.find() ) {
        matchesArray.add( matcher.group( 2 ) );
      }
      if ( !matchesArray.isEmpty() ) {
        return processMatches( matchesArray );
      }
    }
    return new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
  }

  private static SimpleDateFormat processMatches( ArrayList<String> matchesArray ) {
    if ( matchesArray.get( 0 ) != null && !matchesArray.get( 0 ).isEmpty() ) {
      String strippedDateFormatFromLog4j2xml = matchesArray.get( 0 ).trim();
      if ( matchesArray.size() > 1 ) {
        try {
          SimpleDateFormat timezoneDatePattern = new SimpleDateFormat( strippedDateFormatFromLog4j2xml );
          timezoneDatePattern.setTimeZone( TimeZone.getTimeZone( matchesArray.get( 1 ).trim() ) );
          return timezoneDatePattern;
        } catch ( IllegalArgumentException e ) {
          logger.error( e.getMessage() );
        }
      }
      try {
        return new SimpleDateFormat( strippedDateFormatFromLog4j2xml );
      } catch ( IllegalArgumentException e ) {
        logger.error( e.getMessage() );

      }
    }
    return new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
  }

  public static final String ERROR_STRING = "ERROR";

  private boolean timeAdded;

  public KettleLogLayout() {
    this( true );
  }

  public KettleLogLayout( boolean addTime ) {
    this.timeAdded = addTime;
  }

  public String format( KettleLoggingEvent event ) {
    // OK, perhaps the logging information has multiple lines of data.
    // We need to split this up into different lines and all format these
    // lines...
    //
    StringBuilder line = new StringBuilder();

    String dateTimeString = "";
    if ( timeAdded ) {
      if ( LOCAL_SIMPLE_DATE_PARSER.get() == null ) {
        LOCAL_SIMPLE_DATE_PARSER.set( getLog4j2Appender() );
      }
      dateTimeString = LOCAL_SIMPLE_DATE_PARSER.get().format( new Date( event.timeStamp ) ) + " - ";
    }

    Object object = event.getMessage();
    if ( object instanceof LogMessage ) {
      LogMessage message = (LogMessage) object;

      String[] parts = message.getMessage() == null ? new String[] {} : message.getMessage().split( Const.CR );
      for ( int i = 0; i < parts.length; i++ ) {
        // Start every line of the output with a dateTimeString
        line.append( dateTimeString );

        // Include the subject too on every line...
        if ( message.getSubject() != null ) {
          line.append( message.getSubject() );
          if ( message.getCopy() != null ) {
            line.append( "." ).append( message.getCopy() );
          }
          line.append( " - " );
        }

        if ( i == 0 && message.isError() ) {
          BuildVersion buildVersion = BuildVersion.getInstance();
          line.append( ERROR_STRING );
          line.append( " (version " );
          line.append( buildVersion.getVersion() );
          if ( !Utils.isEmpty( buildVersion.getRevision() ) ) {
            line.append( ", build " );
            line.append( buildVersion.getRevision() );
          }
          if ( !Utils.isEmpty( buildVersion.getBuildDate() ) ) {
            line.append( " from " );
            line.append( buildVersion.getBuildDate() );
          }
          if ( !Utils.isEmpty( buildVersion.getBuildUser() ) ) {
            line.append( " by " );
            line.append( buildVersion.getBuildUser() );
          }
          line.append( ") : " );
        }

        line.append( parts[i] );
        if ( i < parts.length - 1 ) {
          line.append( Const.CR ); // put the CR's back in there!
        }
      }
    } else {
      line.append( dateTimeString );
      line.append( ( object != null ? object.toString() : "<null>" ) );
    }

    return line.toString();
  }

  public boolean ignoresThrowable() {
    return false;
  }

  public void activateOptions() {
  }

  public boolean isTimeAdded() {
    return timeAdded;
  }

  public void setTimeAdded( boolean addTime ) {
    this.timeAdded = addTime;
  }
}
