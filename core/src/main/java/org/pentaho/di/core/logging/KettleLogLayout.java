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

package org.pentaho.di.core.logging;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.version.BuildVersion;

public class KettleLogLayout {
  private static final ThreadLocal<SimpleDateFormat> LOCAL_SIMPLE_DATE_PARSER =
    new ThreadLocal<SimpleDateFormat>() {
      @Override
      protected SimpleDateFormat initialValue() {
        return new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
      }
    };

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
