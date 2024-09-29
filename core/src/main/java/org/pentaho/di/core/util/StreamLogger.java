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


package org.pentaho.di.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannelInterface;

public class StreamLogger implements Runnable {
  private InputStream is;

  private String type;

  private LogChannelInterface log;

  private Boolean errorStream;

  private String lastLine;

  public StreamLogger( LogChannelInterface log, InputStream is, String type ) {
    this( log, is, type, false );
  }

  public StreamLogger( LogChannelInterface log, InputStream is, String type, Boolean errorStream ) {
    this.log = log;
    this.is = is;
    this.type = type;
    this.errorStream = errorStream;
  }

  @Override
  public void run() {
    try {
      InputStreamReader isr = new InputStreamReader( is );
      BufferedReader br = new BufferedReader( isr );
      String line = null;
      while ( ( line = br.readLine() ) != null ) {
        lastLine = line;
        if ( errorStream ) {
          log.logError( type + " " + line );
        } else {
          log.logBasic( type + " " + line );
        }

      }
    } catch ( IOException ioe ) {
      log.logError( type + " " + Const.getStackTracker( ioe ) );
    }

  }

  public String getLastLine() {
    return lastLine;
  }

}
