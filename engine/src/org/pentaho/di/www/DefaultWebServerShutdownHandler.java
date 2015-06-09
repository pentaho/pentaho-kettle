package org.pentaho.di.www;

public class DefaultWebServerShutdownHandler implements IWebServerShutdownHandler {

  @Override
  public void shutdownWebServer() throws Exception {
    Thread.sleep( 30000 ); // Wait for karaf and kettle to shutdown, then do System.exit to take care of some
                           // straggler timer threads
    System.exit( 0 );
  }
}
