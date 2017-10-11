/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.syslog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.TimeZone;

import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.productivity.java.syslog4j.SyslogConstants;
import org.productivity.java.syslog4j.SyslogIF;

/**
 * This defines settings for Syslog.
 *
 * @author Samatar
 * @since 05-01-2010
 *
 */
public class SyslogDefs {

  private static Class<?> PKG = JobEntrySyslog.class; // for i18n purposes, needed by Translator2!!

  public static final String DEFAULT_PROTOCOL_UDP = "udp";

  public static final int DEFAULT_PORT = 514;
  public static final String DEFAULT_DATE_FORMAT = "MMM dd HH:mm:ss";

  private static Hashtable<String, Integer> facHash;
  private static Hashtable<String, Integer> priHash;

  public static final String[] FACILITYS = new String[] {
    "KERNEL", "USER", "MAIL", "DAEMON", "AUTH", "SYSLOG", "LPR", "NEWS", "UUCP", "CRON", "LOCAL0", "LOCAL1",
    "LOCAL2", "LOCAL3", "LOCAL4", "LOCAL5", "LOCAL6", "LOCAL7", };
  public static final String[] PRIORITYS = new String[] {
    "EMERGENCY", "ALERT", "CRITICAL", "ERROR", "WARNING", "NOTICE", "INFO", "DEBUG", };

  static {
    facHash = new Hashtable<String, Integer>( 18 );
    facHash.put( "KERNEL", SyslogConstants.FACILITY_KERN );
    facHash.put( "USER", SyslogConstants.FACILITY_USER );
    facHash.put( "MAIL", SyslogConstants.FACILITY_MAIL );
    facHash.put( "DAEMON", SyslogConstants.FACILITY_DAEMON );
    facHash.put( "AUTH", SyslogConstants.FACILITY_AUTH );
    facHash.put( "SYSLOG", SyslogConstants.FACILITY_SYSLOG );
    facHash.put( "LPR", SyslogConstants.FACILITY_LPR );
    facHash.put( "NEWS", SyslogConstants.FACILITY_NEWS );
    facHash.put( "UUCP", SyslogConstants.FACILITY_UUCP );
    facHash.put( "CRON", SyslogConstants.FACILITY_CRON );
    facHash.put( "LOCAL0", SyslogConstants.FACILITY_LOCAL0 );
    facHash.put( "LOCAL1", SyslogConstants.FACILITY_LOCAL1 );
    facHash.put( "LOCAL2", SyslogConstants.FACILITY_LOCAL2 );
    facHash.put( "LOCAL3", SyslogConstants.FACILITY_LOCAL3 );
    facHash.put( "LOCAL4", SyslogConstants.FACILITY_LOCAL4 );
    facHash.put( "LOCAL5", SyslogConstants.FACILITY_LOCAL5 );
    facHash.put( "LOCAL6", SyslogConstants.FACILITY_LOCAL6 );
    facHash.put( "LOCAL7", SyslogConstants.FACILITY_LOCAL7 );

    priHash = new Hashtable<String, Integer>( 8 );
    priHash.put( "EMERGENCY", SyslogConstants.LEVEL_EMERGENCY );
    priHash.put( "ALERT", SyslogConstants.LEVEL_ALERT );
    priHash.put( "CRITICAL", SyslogConstants.LEVEL_CRITICAL );
    priHash.put( "ERROR", SyslogConstants.LEVEL_ERROR );
    priHash.put( "WARNING", SyslogConstants.LEVEL_WARN );
    priHash.put( "NOTICE", SyslogConstants.LEVEL_NOTICE );
    priHash.put( "INFO", SyslogConstants.LEVEL_INFO );
    priHash.put( "DEBUG", SyslogConstants.LEVEL_DEBUG );
  }

  public static int computeCode( int facility, int priority ) {
    return ( ( facility << 3 ) | priority );
  }

  public static int getPriority( String priority ) throws SyslogException {
    Integer result = SyslogDefs.priHash.get( priority );

    if ( result == null ) {
      throw new SyslogException( BaseMessages.getString( PKG, "JobEntrySyslog.UnknownPriotity", priority ) );
    }

    return result.intValue();
  }

  public static int getFacility( String facility ) throws SyslogException {
    Integer result = SyslogDefs.facHash.get( facility );

    if ( result == null ) {
      throw new SyslogException( BaseMessages.getString( PKG, "JobEntrySyslog.UnknownFacility", facility ) );
    }
    return result.intValue();
  }

  public static void sendMessage( SyslogIF syslog, int priority, String message, boolean addTimestamp,
    String pattern, boolean addHostName ) {

    String messageString = message;

    // Do we need to add hostname?
    if ( addHostName ) {
      messageString = Const.getHostname() + " " + messageString;
    }

    // Do we need to add timestamp
    if ( addTimestamp ) {
      SimpleDateFormat dateFormat = new SimpleDateFormat( pattern );
      dateFormat.setTimeZone( TimeZone.getDefault() );
      messageString = dateFormat.format( Calendar.getInstance().getTime() ) + " : " + messageString;
    }

    // send message
    switch ( priority ) {
      case SyslogConstants.LEVEL_EMERGENCY:
        syslog.emergency( messageString );
        break;
      case SyslogConstants.LEVEL_ALERT:
        syslog.alert( messageString );
        break;
      case SyslogConstants.LEVEL_CRITICAL:
        syslog.critical( messageString );
        break;
      case SyslogConstants.LEVEL_ERROR:
        syslog.error( messageString );
        break;
      case SyslogConstants.LEVEL_WARN:
        syslog.warn( messageString );
        break;
      case SyslogConstants.LEVEL_NOTICE:
        syslog.notice( messageString );
        break;
      case SyslogConstants.LEVEL_INFO:
        syslog.info( messageString );
        break;
      case SyslogConstants.LEVEL_DEBUG:
        syslog.debug( messageString );
        break;
      default:
        break;
    }
  }
}
