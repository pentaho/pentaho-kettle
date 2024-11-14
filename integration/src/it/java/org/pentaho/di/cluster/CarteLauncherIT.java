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


package org.pentaho.di.cluster;

import org.junit.Test;
import static org.junit.Assert.*;
import org.pentaho.di.core.KettleEnvironment;

public class CarteLauncherIT {

  @Test( timeout = 10 * 60 * 1000 )
  public void testLaunchStopSlaveServer() throws Exception {
    KettleEnvironment.init();

    CarteLauncher carteLauncher = new CarteLauncher( "localhost", 8282 );
    Thread thread = new Thread( carteLauncher );
    thread.start();

    // Wait until the carte object is available...
    //
    while ( carteLauncher.getCarte() == null && !carteLauncher.isFailure() ) {
      Thread.sleep( 100 );
    }

    assertFalse( thread.isAlive() );
    assertTrue( carteLauncher.getCarte().getWebServer().getServer().isRunning() );
    carteLauncher.getCarte().getWebServer().stopServer();
    assertFalse( carteLauncher.getCarte().getWebServer().getServer().isRunning() );
  }
}
