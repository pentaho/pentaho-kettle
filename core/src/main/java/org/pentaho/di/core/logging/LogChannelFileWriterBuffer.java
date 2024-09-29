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


package org.pentaho.di.core.logging;

import org.pentaho.di.core.Const;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LogChannelFileWriterBuffer {

  private KettleLogLayout layout;

  private String logChannelId;

  private final List<KettleLoggingEvent> buffer = Collections.synchronizedList( new LinkedList<KettleLoggingEvent>() );

  public LogChannelFileWriterBuffer( String logChannelId ) {
    this.logChannelId = logChannelId;
    layout = new KettleLogLayout( true );
  }

  public void addEvent( KettleLoggingEvent event ) {
    synchronized ( buffer ) {
      buffer.add( event );
    }
  }

  public StringBuffer getBuffer() {
    StringBuffer stringBuffer = new StringBuffer( 1000 );

    synchronized ( buffer ) {
      for ( KettleLoggingEvent event : buffer ) {
        stringBuffer.append( layout.format( event ) ).append( Const.CR );
      }

      buffer.clear();
    }

    return stringBuffer;
  }

  public String getLogChannelId() {
    return logChannelId;
  }
}
