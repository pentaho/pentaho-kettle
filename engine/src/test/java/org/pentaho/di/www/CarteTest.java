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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
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
    WebTarget status = mock( WebTarget.class );
    doReturn( "<serverstatus>" ).when( status.request() ).get();

    WebTarget stop = mock( WebTarget.class );
    doReturn( "Shutting Down" ).when( stop.request() ).get();

    Client client = mock( Client.class );
    HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic( "admin", "password" );
    doReturn( client ).when( client ).register( feature );
    doReturn( status ).when( client ).target( "http://localhost:8080/kettle/status/?xml=Y" );
    doReturn( stop ).when( client ).target( "http://localhost:8080/kettle/stopCarte" );

    when( ClientBuilder.newClient() ).thenReturn( client );

    Carte.callStopCarteRestService( "localhost", "8080", "admin", "Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde", false, null );

    // the expected value is: "Basic <base64 encoded username:password>"
    assertEquals( "Basic " + new String( Base64.getEncoder().encode( "admin:password".getBytes( "utf-8" ) ) ),
      InternalState.getInternalState( client.getConfiguration(), "authentication" ) );
  }
}
