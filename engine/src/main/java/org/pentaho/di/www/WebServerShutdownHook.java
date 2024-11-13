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


package org.pentaho.di.www;

public class WebServerShutdownHook extends Thread {

  WebServer webServer;
  boolean shuttingDown; //Set when shutting down so we only stop the server once.

  WebServerShutdownHook( WebServer webServer ) {
    this.webServer = webServer;
  }

  @Override
  public void run() {
    if ( !shuttingDown ) {
      try {
        webServer.stopServer();
      } catch ( Exception e ) {
        e.printStackTrace();
      }
    }
  }

  public boolean isShuttingDown() {
    return shuttingDown;
  }

  public void setShuttingDown( boolean shuttingDown ) {
    this.shuttingDown = shuttingDown;
  }

}
