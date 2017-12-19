/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannelInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tatsiana_Kasiankova
 * 
 */
public class WebServerTest {

  /**
   * 
   */
  private static final String EMPTY_STRING = "";

  private static final boolean SHOULD_JOIN = false;

  private static final String HOST_NAME = "localhost";

  private static final int PORT = 8099;

  private static final String ACCEPTORS = "5";

  private static final int EXPECTED_ACCEPTORS = 5;

  private static final String ACCEPT_QUEUE_SIZE = "5000";

  private static final int EXPECTED_ACCEPT_QUEUE_SIZE = 5000;

  private static final String RES_MAX_IDLE_TIME = "200";

  private static final int EXPECTED_RES_MAX_IDLE_TIME = 200;

  private static final int EXPECTED_CONNECTORS_SIZE = 1;

  private WebServer webServer;
  private WebServer webServerNg;
  private TransformationMap trMapMock = mock( TransformationMap.class );
  private SlaveServerConfig sServerConfMock = mock( SlaveServerConfig.class );
  private SlaveServer sServer = mock( SlaveServer.class );
  private JobMap jbMapMock = mock( JobMap.class );
  private SocketRepository sRepoMock = mock( SocketRepository.class );
  private List<SlaveServerDetection> detections = new ArrayList<SlaveServerDetection>();
  private LogChannelInterface logMock = mock( LogChannelInterface.class );
  private static final SocketConnector defSocketConnector = new SocketConnector();

  @Before
  public void setup() throws Exception {
    System.setProperty( Const.KETTLE_CARTE_JETTY_ACCEPTORS, ACCEPTORS );
    System.setProperty( Const.KETTLE_CARTE_JETTY_ACCEPT_QUEUE_SIZE, ACCEPT_QUEUE_SIZE );
    System.setProperty( Const.KETTLE_CARTE_JETTY_RES_MAX_IDLE_TIME, RES_MAX_IDLE_TIME );

    when( sServerConfMock.getSlaveServer() ).thenReturn( sServer );
    when( trMapMock.getSlaveServerConfig() ).thenReturn( sServerConfMock );
    when( sServer.getPassword() ).thenReturn( "cluster" );
    when( sServer.getUsername() ).thenReturn( "cluster" );
    webServer =
        new WebServer( logMock, trMapMock, jbMapMock, sRepoMock, detections, HOST_NAME, PORT, SHOULD_JOIN, null );

  }

  @After
  public void tearDown() {
    webServer.setWebServerShutdownHandler( null ); // disable system.exit
    webServer.stopServer();

    System.getProperties().remove( Const.KETTLE_CARTE_JETTY_ACCEPTORS );
    System.getProperties().remove( Const.KETTLE_CARTE_JETTY_ACCEPT_QUEUE_SIZE );
    System.getProperties().remove( Const.KETTLE_CARTE_JETTY_RES_MAX_IDLE_TIME );

  }

  @Test
  public void testJettyOption_AcceptorsSetUp() throws Exception {
    assertEquals( getSocketConnectors( webServer ).size(), EXPECTED_CONNECTORS_SIZE );
    for ( SocketConnector sc : getSocketConnectors( webServer ) ) {
      assertEquals( EXPECTED_ACCEPTORS, sc.getAcceptors() );

    }

  }

  @Test
  public void testJettyOption_AcceptQueueSizeSetUp() throws Exception {
    assertEquals( getSocketConnectors( webServer ).size(), EXPECTED_CONNECTORS_SIZE );
    for ( SocketConnector sc : getSocketConnectors( webServer ) ) {
      assertEquals( EXPECTED_ACCEPT_QUEUE_SIZE, sc.getAcceptQueueSize() );
    }

  }

  @Test
  public void testJettyOption_LowResourceMaxIdleTimeSetUp() throws Exception {
    assertEquals( getSocketConnectors( webServer ).size(), EXPECTED_CONNECTORS_SIZE );
    for ( SocketConnector sc : getSocketConnectors( webServer ) ) {
      assertEquals( EXPECTED_RES_MAX_IDLE_TIME, sc.getLowResourceMaxIdleTime() );
    }

  }

  @Test
  public void testNoExceptionAndUsingDefaultServerValue_WhenJettyOptionSetAsInvalidValue() throws Exception {
    System.setProperty( Const.KETTLE_CARTE_JETTY_ACCEPTORS, "TEST" );
    try {
      webServerNg =
          new WebServer( logMock, trMapMock, jbMapMock, sRepoMock, detections, HOST_NAME, PORT + 1, SHOULD_JOIN, null );
    } catch ( NumberFormatException nmbfExc ) {
      fail( "Should not have thrown any NumberFormatException but it does: " + nmbfExc );
    }
    assertEquals( getSocketConnectors( webServerNg ).size(), EXPECTED_CONNECTORS_SIZE );
    for ( SocketConnector sc : getSocketConnectors( webServerNg ) ) {
      assertEquals( defSocketConnector.getAcceptors(), sc.getAcceptors() );
    }
    webServerNg.setWebServerShutdownHandler( null ); // disable system.exit
    webServerNg.stopServer();
  }

  @Test
  public void testNoExceptionAndUsingDefaultServerValue_WhenJettyOptionSetAsEmpty() throws Exception {
    System.setProperty( Const.KETTLE_CARTE_JETTY_ACCEPTORS, EMPTY_STRING );
    try {
      webServerNg =
          new WebServer( logMock, trMapMock, jbMapMock, sRepoMock, detections, HOST_NAME, PORT + 1, SHOULD_JOIN, null );
    } catch ( NumberFormatException nmbfExc ) {
      fail( "Should not have thrown any NumberFormatException but it does: " + nmbfExc );
    }
    assertEquals( getSocketConnectors( webServerNg ).size(), EXPECTED_CONNECTORS_SIZE );
    for ( SocketConnector sc : getSocketConnectors( webServerNg ) ) {
      assertEquals( defSocketConnector.getAcceptors(), sc.getAcceptors() );
    }
    webServerNg.setWebServerShutdownHandler( null ); // disable system.exit
    webServerNg.stopServer();
  }

  private List<SocketConnector> getSocketConnectors( WebServer wServer ) {
    List<SocketConnector> sConnectors = new ArrayList<SocketConnector>();
    Connector[] connectors = wServer.getServer().getConnectors();
    for ( Connector cn : connectors ) {
      if ( cn instanceof SocketConnector ) {
        sConnectors.add( (SocketConnector) cn );
      }
    }
    return sConnectors;
  }

}
