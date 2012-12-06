/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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
