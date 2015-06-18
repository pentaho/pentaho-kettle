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
