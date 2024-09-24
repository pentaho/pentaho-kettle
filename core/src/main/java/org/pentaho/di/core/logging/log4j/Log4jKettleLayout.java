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
package org.pentaho.di.core.logging.log4j;


import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogMessage;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.version.BuildVersion;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Log4jKettleLayout extends AbstractStringLayout implements Log4jLayout {
  private static final ThreadLocal<SimpleDateFormat> LOCAL_SIMPLE_DATE_PARSER = new ThreadLocal<SimpleDateFormat>() {
    @Override
    protected SimpleDateFormat initialValue() {
        return new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
    }
  };

  public static final String ERROR_STRING = "ERROR";

  private boolean timeAdded;

  public Log4jKettleLayout( Charset charset, boolean timeAdded ) {
    super( charset );
    this.timeAdded = timeAdded;
  }

  public String format( LogEvent event ) {
    // OK, perhaps the logging information has multiple lines of data.
    // We need to split this up into different lines and all format these
    // lines...
    //
    StringBuilder line = getStringBuilder();

    String dateTimeString = "";
    if ( timeAdded ) {
      dateTimeString = LOCAL_SIMPLE_DATE_PARSER.get().format( new Date( event.getTimeMillis() ) ) + " - ";
    }

    Object object = event.getMessage();
    if ( object instanceof LogMessage ) {
      LogMessage message = (LogMessage) object;

      String[] parts = message.getMessage().split( Const.CR );
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

        if ( message.isError() ) {
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

        line.append( parts[ i ] );
        if ( i < parts.length - 1 ) {
          line.append( Const.CR ); // put the CR's back in there!
        }
    }
    } else {
      line.append( dateTimeString );
      line.append( ( object != null ? object.toString() : "<null>" ) );
    }
    line.append( Const.CR );
    return line.toString();
  }

  public boolean ignoresThrowable() {
    return false;
  }

  public void activateOptions() {
      // not used
  }

  public boolean isTimeAdded() {
    return timeAdded;
  }

  public void setTimeAdded( boolean addTime ) {
    this.timeAdded = addTime;
  }

  @Override
  public byte[] getFooter() {
    return new byte[ 0 ];
  }

  @Override
  public byte[] getHeader() {
    return new byte[ 0 ];
  }

  @Override public String toSerializable( LogEvent event ) {
    return format( event );
  }

  @Override
  public String getContentType() {
    return null;
  }

  @Override
  public Map<String, String> getContentFormat() {
    return new HashMap<>();
  }

  @Override
  public void encode( LogEvent source, ByteBufferDestination destination ) {
      // not used
  }
}

