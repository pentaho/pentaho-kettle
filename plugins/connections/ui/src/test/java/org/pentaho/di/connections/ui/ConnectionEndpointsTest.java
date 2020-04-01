/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.ui;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.ui.endpoints.ConnectionEndpoints;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;
import org.pentaho.osgi.metastore.locator.api.MetastoreLocator;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;

public class ConnectionEndpointsTest {

  private ConnectionManager connectionManager = ConnectionManager.getInstance();
  private MemoryMetaStore memoryMetaStore = new MemoryMetaStore();
  private static String DESCRIPTION = "Connection Description";
  private static String CONNECTION_NAME = "Connection Name";
  private static String PASSWORD = "testpassword";
  private static String PASSWORD2 = "testpassword2";

  @Before
  public void setup() throws Exception {
    KettleClientEnvironment.init();
    connectionManager.setMetastoreSupplier( () -> memoryMetaStore );
  }

  @Test
  public void createConnection() {
    addProvider();
    ConnectionEndpoints connectionEndpoints = new ConnectionEndpoints( getMetaStoreLocator() );
    try {
      connectionEndpoints.createConnection( getConnectionDetails(), CONNECTION_NAME );
    } catch ( Exception e ) {
      // Bypass exceptions thrown by lack of getSpoon().getShell().getDisplay() since we are not running the UI
    }

    Response response = connectionEndpoints.getConnectionExists( CONNECTION_NAME );
    assertEquals( "true", response.getEntity() );
  }

  private void addProvider() {
    TestConnectionProvider testConnectionProvider = new TestConnectionProvider( connectionManager );
    connectionManager.addConnectionProvider( TestConnectionProvider.SCHEME, testConnectionProvider );
  }

  private TestConnectionDetails getConnectionDetails() {
    TestConnectionDetails testConnectionDetails = new TestConnectionDetails();
    testConnectionDetails.setDescription( DESCRIPTION );
    testConnectionDetails.setName( CONNECTION_NAME );
    testConnectionDetails.setPassword( PASSWORD );
    testConnectionDetails.setPassword1( PASSWORD2 );
    return testConnectionDetails;
  }

  private MetastoreLocator getMetaStoreLocator() {
    return new MetastoreLocator() {
      @Override public IMetaStore getMetastore() {
        return memoryMetaStore;
      }

      @Override public IMetaStore getMetastore( String s ) {
        return null;
      }

      @Override public String setEmbeddedMetastore( IMetaStore iMetaStore ) {
        return null;
      }

      @Override public void disposeMetastoreProvider( String s ) {
      }

      @Override public IMetaStore getExplicitMetastore( String s ) {
        return null;
      }
    };
  }
}
