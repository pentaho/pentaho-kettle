/*!
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* Copyright (c) 2002-2014 Pentaho Corporation..  All rights reserved.
*/
package org.pentaho.di.monitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.extension.ExtensionPointPluginType;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.util.Assert;
import org.pentaho.di.www.Carte;
import org.pentaho.di.www.CarteSingleton;
import org.pentaho.di.www.SlaveServerConfig;
import org.pentaho.di.www.WebServer;

public class CarteServerTriggeredTest extends BaseEventsTriggeredTest {

  private static final String TEST_HOSTNAME = "localhost";
  private static final String TEST_PORT = "8082";
  private static final String TEST_CARTE_CREDENTIALS_FILE = "test-resources/pwd/mockCartePassword.pwd";

  private SlaveServerConfig serverConfig;
  private CarteLauncher carteLauncher;
  private Thread carteThread;
  MockPlugin mockPluginCarteStartup;
  MockPlugin mockPluginCarteShutdown;
  DummyMonitor monitorStartup;
  DummyMonitor monitorShutdown;


  @Before
  public void setUp() throws Exception {

    super.setUp();

    SlaveServer slaveServer = new SlaveServer( TEST_HOSTNAME + ":" + TEST_PORT, TEST_HOSTNAME, TEST_PORT, null, null );
    serverConfig = new SlaveServerConfig( slaveServer );
    serverConfig.setPasswordFile( TEST_CARTE_CREDENTIALS_FILE );

    carteLauncher = new CarteLauncher( serverConfig );
    carteThread = new Thread( carteLauncher );

    monitorStartup = new DummyMonitor();
    monitorShutdown = new DummyMonitor();

    mockPluginCarteStartup = new MockPlugin( monitorStartup, new String[] { KettleExtensionPoint.CarteStartup.id },
      KettleExtensionPoint.CarteStartup.name() );

    mockPluginCarteShutdown = new MockPlugin( monitorShutdown, new String[] { KettleExtensionPoint.CarteShutdown.id },
      KettleExtensionPoint.CarteShutdown.name() );

    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, mockPluginCarteStartup );
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, mockPluginCarteShutdown );
  }

  @Test
  public void testCarteStartupAndStopMonitor() throws Exception {

    carteThread.start(); // start carte

    Thread.sleep( 5000 ); // wait 5 seconds for carte to be properly initialized ( usually quite fast )

    Assert.assertTrue( carteLauncher.isUpAndRunning() );
    Assert.assertTrue( monitorStartup.wasTriggered );
    Assert.assertTrue( monitorStartup.eventObject != null && monitorStartup.eventObject instanceof WebServer );

    Assert.assertTrue( ( (WebServer) monitorStartup.eventObject ).getHostname().equals( TEST_HOSTNAME ) );
    Assert.assertTrue( ( (WebServer) monitorStartup.eventObject ).getPort() == Integer.valueOf( TEST_PORT ) );

    stopCarteServer(); // stop carte

    Assert.assertTrue( monitorShutdown.wasTriggered );
    Assert.assertTrue( monitorShutdown.wasTriggered );
    Assert.assertTrue( monitorShutdown.eventObject != null && monitorShutdown.eventObject instanceof WebServer );

    Assert.assertTrue( ( (WebServer) monitorShutdown.eventObject ).getHostname().equals( TEST_HOSTNAME ) );
    Assert.assertTrue( ( (WebServer) monitorShutdown.eventObject ).getPort() == Integer.valueOf( TEST_PORT ) );
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();

    stopCarteServer(); // call carte server stop again at tearDown, just to be safe

    if( PluginRegistry.getInstance() != null ) {
      PluginRegistry.getInstance().removePlugin( ExtensionPointPluginType.class, mockPluginCarteStartup );
      PluginRegistry.getInstance().removePlugin( ExtensionPointPluginType.class, mockPluginCarteShutdown );
    }

    mockPluginCarteStartup = null;
    mockPluginCarteShutdown = null;

    monitorStartup.reset();
    monitorShutdown.reset();

    monitorStartup = null;
    monitorShutdown = null;

    carteThread = null;
    serverConfig = null;
    carteLauncher = null;
  }


  private void stopCarteServer() {
    try {

      if( carteLauncher.isUpAndRunning() ) {
        carteLauncher.getCarte().getWebServer().stopServer();
      }

    } catch ( Throwable t ) {
      /* do nothing */
    }
  }

  private class CarteLauncher implements Runnable {
    private SlaveServerConfig config;
    private Carte carte;

    public CarteLauncher( SlaveServerConfig config ) {
      this.config = config;
    }

    @Override
    public void run() {
      try {
        carte = new Carte( config );
      } catch ( Throwable t ) {
        carte = null;
      }
    }

    public Carte getCarte() {
      return carte;
    }

    public boolean isUpAndRunning() {
      return carte != null && carte.getWebServer() != null && carte.getWebServer().getServer() != null;
    }
  }
}
