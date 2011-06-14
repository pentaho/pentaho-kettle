package org.pentaho.di.cluster;

import org.pentaho.di.core.KettleEnvironment;

import junit.framework.TestCase;

public class CarteLauncherTest extends TestCase {
  public void testLaunchStopSlaveServer() throws Exception {
    KettleEnvironment.init();
    
    CarteLauncher carteLauncher = new CarteLauncher("localhost", 8282);
    Thread thread = new Thread(carteLauncher);
    thread.start();
    
    // Wait until the carte object is available...
    //
    while (carteLauncher.getCarte()==null && !carteLauncher.isFailure()) {
      Thread.sleep(100);
    }
    
    assertFalse(thread.isAlive());
    assertTrue(carteLauncher.getCarte().getWebServer().getServer().isRunning());
    carteLauncher.getCarte().getWebServer().stopServer();
    assertFalse(carteLauncher.getCarte().getWebServer().getServer().isRunning());
  }
}
