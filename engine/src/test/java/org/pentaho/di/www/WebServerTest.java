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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jetty.server.LowResourceMonitor;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.eclipse.jetty.server.Connector;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tatsiana_Kasiankova
 * 
 */
public class WebServerTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

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
  private List<SlaveServerDetection> detections = new ArrayList<>();
  private LogChannelInterface logMock = mock( LogChannelInterface.class );
  private ServerConnector defServerConnector;

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
    defServerConnector = new ServerConnector(webServer.getServer(), -1, -1);
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
    assertEquals( EXPECTED_CONNECTORS_SIZE, getServerConnectors( webServer ).size() );
    for ( ServerConnector sc : getServerConnectors( webServer ) ) {
      assertEquals( EXPECTED_ACCEPTORS, sc.getAcceptors() );

    }

  }

  @Test
  public void testJettyOption_AcceptQueueSizeSetUp() throws Exception {
    assertEquals( EXPECTED_CONNECTORS_SIZE, getServerConnectors( webServer ).size() );
    for ( ServerConnector sc : getServerConnectors( webServer ) ) {
      assertEquals( EXPECTED_ACCEPT_QUEUE_SIZE, sc.getAcceptQueueSize() );
    }

  }

  @Test
  public void testJettyOption_LowResourceMaxIdleTimeSetUp() throws Exception {
    LowResourceMonitor lowResourceMonitor = webServer.getServer().getBean( LowResourceMonitor.class );
    assertNotNull( lowResourceMonitor );
    assertEquals( EXPECTED_RES_MAX_IDLE_TIME, lowResourceMonitor.getLowResourcesIdleTimeout() );
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
    assertEquals( EXPECTED_CONNECTORS_SIZE, getServerConnectors( webServerNg ).size() );
    for ( ServerConnector sc : getServerConnectors( webServerNg ) ) {
      assertEquals( defServerConnector.getAcceptors(), sc.getAcceptors() );
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
    assertEquals( EXPECTED_CONNECTORS_SIZE, getServerConnectors( webServerNg ).size() );
    for ( ServerConnector sc : getServerConnectors( webServerNg ) ) {
      assertEquals( defServerConnector.getAcceptors(), sc.getAcceptors() );
    }
    webServerNg.setWebServerShutdownHandler( null ); // disable system.exit
    webServerNg.stopServer();
  }

  private List<ServerConnector> getServerConnectors( WebServer wServer ) {
    List<ServerConnector> sConnectors = new ArrayList<>();
    Connector[] connectors = wServer.getServer().getConnectors();
    for ( Connector cn : connectors ) {
      if ( cn instanceof ServerConnector ) {
        sConnectors.add( (ServerConnector) cn );
      }
    }
    return sConnectors;
  }
}
