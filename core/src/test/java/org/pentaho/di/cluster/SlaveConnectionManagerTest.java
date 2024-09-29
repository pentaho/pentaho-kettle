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
