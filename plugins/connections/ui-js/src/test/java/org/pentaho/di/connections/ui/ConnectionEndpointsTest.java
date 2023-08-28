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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

public class ConnectionEndpointsTest {

  private ConnectionManager mockConnectionManager;
  private static String CONNECTION_NAME = "Connection Name";

  @Before
  public void setup() throws Exception {
    //KettleClientEnvironment.init(); // NOTE: with proper class structure, only need generic mocking library
  }

  @Test
  public void createConnection() {

    // SETUP
    mockConnectionManager = mock( ConnectionManager.class );
    ConnectionEndpoints connectionEndpoints = new ConnectionEndpoints( mockConnectionManager );
    when( mockConnectionManager.exists( eq( CONNECTION_NAME ) ) ).thenReturn( true );

    // EXECUTE 1: name match
    Response response1 = connectionEndpoints.getConnectionExists( CONNECTION_NAME );

    // VERIFY 1
    assertEquals( "true", response1.getEntity() );

    // EXECUTE 2: name does not exists
    Response response2 = connectionEndpoints.getConnectionExists( CONNECTION_NAME + "_XYZ"  );

    // VERIFY 2
    assertEquals( "false", response2.getEntity() );
  }

}
