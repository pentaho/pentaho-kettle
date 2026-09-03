/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2031-09-03
 ******************************************************************************/

package org.pentaho.di.core.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PluginServiceLoaderTest {

  @Test
  public void unregisterServiceRemovesOnlyTheProviderRegistration() throws Exception {
    Object provider = new Object();
    TestService service = new TestService() { };

    PluginServiceLoader.registerService( provider, TestService.class, service, 0 );
    try {
      assertTrue( PluginServiceLoader.loadServices( TestService.class ).contains( service ) );

      PluginServiceLoader.unregisterService( provider, TestService.class );

      assertFalse( PluginServiceLoader.loadServices( TestService.class ).contains( service ) );
    } finally {
      PluginServiceLoader.unregisterService( provider, TestService.class );
    }
  }

  private interface TestService {
  }
}