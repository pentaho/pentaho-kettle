/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 - 2018 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.www;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.test.util.InternalState;

import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Created by ccaspanello on 5/31/2016.
 */

public class CarteTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  private MockedStatic<Client> clientMockedStatic;

  @Before
  public void setup() {
    clientMockedStatic = mockStatic( Client.class );
  }

  @After
  public void tearDown() {
    clientMockedStatic.close();
  }

  @Ignore( "This test isn't consistent/doesn't work." )
  @Test
  public void test() throws Exception {

    CountDownLatch latch = new CountDownLatch( 1 );

    System.setProperty( Const.KETTLE_SLAVE_DETECTION_TIMER, "100" );

    SlaveServer master = new SlaveServer();
    master.setHostname( "127.0.0.1" );
    master.setPort( "9000" );
    master.setUsername( "cluster" );
    master.setPassword( "cluster" );
    master.setMaster( true );

    SlaveServerConfig config = new SlaveServerConfig();
    config.setSlaveServer( master );

    Carte carte = new Carte( config );

    SlaveServerDetection slaveServerDetection = mock( SlaveServerDetection.class );
    carte.getWebServer().getDetections().add( slaveServerDetection );

    SlaveServer slaveServer = mock( SlaveServer.class, RETURNS_MOCKS );
    when( slaveServerDetection.getSlaveServer() ).thenReturn( slaveServer );
    when( slaveServer.getStatus() ).thenAnswer( new Answer<SlaveServerStatus>() {
      @Override public SlaveServerStatus answer( InvocationOnMock invocation ) throws Throwable {
        SlaveServerDetection anotherDetection = mock( SlaveServerDetection.class );
        carte.getWebServer().getDetections().add( anotherDetection );
        latch.countDown();
        return new SlaveServerStatus();
      }
    } );

    latch.await( 10, TimeUnit.SECONDS );
    assertEquals( carte.getWebServer().getDetections().size(), 2 );
    carte.getWebServer().stopServer();
  }

  @Test
  public void callStopCarteRestService() throws Exception {
    WebResource status = mock( WebResource.class );
    doReturn( "<serverstatus>" ).when( status ).get( String.class );

    WebResource stop = mock( WebResource.class );
    doReturn( "Shutting Down" ).when( stop ).get( String.class );

    Client client = mock( Client.class );
    doCallRealMethod().when( client ).addFilter( any( HTTPBasicAuthFilter.class ) );
    doCallRealMethod().when( client ).getHeadHandler();
    doReturn( status ).when( client ).resource( "http://localhost:8080/kettle/status/?xml=Y" );
    doReturn( stop ).when( client ).resource( "http://localhost:8080/kettle/stopCarte" );

    when( Client.create( any( ClientConfig.class ) ) ).thenReturn( client );

    Carte.callStopCarteRestService( "localhost", "8080", "admin", "Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde" );

    // the expected value is: "Basic <base64 encoded username:password>"
    assertEquals( "Basic " + new String( Base64.getEncoder().encode( "admin:password".getBytes( "utf-8" ) ) ),
      InternalState.getInternalState( client.getHeadHandler(), "authentication" ) );
  }
}
