/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.cluster;

import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.SSLContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class SlaveConnectionManagerTest {

  private SSLContext defaultContext;

  @Before
  public void setUp() throws Exception {
    SlaveConnectionManager.reset();
    defaultContext = SSLContext.getDefault();
  }

  @Test
  public void shouldOverrideDefaultSSLContextByDefault() throws Exception {
    System.clearProperty( "javax.net.ssl.keyStore" );
    SlaveConnectionManager instance = SlaveConnectionManager.getInstance();
    assertNotEquals( defaultContext, SSLContext.getDefault() );
  }

  @Test
  public void shouldNotOverrideDefaultSSLContextIfKeystoreIsSet() throws Exception {
    System.setProperty( "javax.net.ssl.keyStore", "NONE" );
    SlaveConnectionManager instance = SlaveConnectionManager.getInstance();
    assertEquals( defaultContext, SSLContext.getDefault() );
  }
}
