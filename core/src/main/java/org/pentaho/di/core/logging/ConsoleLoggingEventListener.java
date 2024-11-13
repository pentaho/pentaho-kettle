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


package org.pentaho.di.core.logging;

public class ConsoleLoggingEventListener implements KettleLoggingEventListener {

  private KettleLogLayout layout;

  public ConsoleLoggingEventListener() {
    this.layout = new KettleLogLayout( true );
  }

  @Override
  public void eventAdded( KettleLoggingEvent event ) {

    String logText = layout.format( event );

    if ( event.getLevel() == LogLevel.ERROR ) {
      KettleLogStore.OriginalSystemErr.println( logText );
    } else {
      KettleLogStore.OriginalSystemOut.println( logText );
    }
  }
}
