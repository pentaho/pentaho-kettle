package org.pentaho.di.cluster;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

/**
 * Encapsulates the Apache commons HTTP connection manager with a singleton.
 * We can use this to limit the number of open connections to slave servers.
 * 
 * @author matt
 *
 */
public class SlaveConnectionManager {

  private static SlaveConnectionManager slaveConnectionManager;
  
  private MultiThreadedHttpConnectionManager manager;
  
  private SlaveConnectionManager() {
    manager = new MultiThreadedHttpConnectionManager();
    manager.getParams().setDefaultMaxConnectionsPerHost(100);
    manager.getParams().setMaxTotalConnections(200);
  }
  
  public static SlaveConnectionManager getInstance() {
    if (slaveConnectionManager==null) {
      slaveConnectionManager=new SlaveConnectionManager();
    }
    return slaveConnectionManager;
  }
  
  public HttpClient createHttpClient() {
    return new HttpClient(manager);
  }
  
  public void shutdown() {
    manager.shutdown();
  }
}
