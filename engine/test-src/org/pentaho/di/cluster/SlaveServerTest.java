/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;

public class SlaveServerTest {
  SlaveServer slaveServer;

  @BeforeClass
  public static void beforeClass() throws KettleException {
    PluginRegistry.init();
  }

  @Before
  public void init() throws IOException {
    HttpClient httpClient = spy( new HttpClient() );
    doReturn( 404 ).when( httpClient ).executeMethod( any( HttpMethod.class ) );

    slaveServer = spy( new SlaveServer() );
  }

  @Test
  public void testModifyingName() {
    slaveServer.setName( "test" );
    List<SlaveServer> list = new ArrayList<SlaveServer>();
    list.add( slaveServer );

    SlaveServer slaveServer2 = spy( new SlaveServer() );
    slaveServer2.setName( "test" );

    slaveServer2.verifyAndModifySlaveServerName( list, null );

    assertTrue( !slaveServer.getName().equals( slaveServer2.getName() ) );
  }
}