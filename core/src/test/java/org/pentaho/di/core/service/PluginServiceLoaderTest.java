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
    Object otherProvider = new Object();
    TestService service = new TestService() { };
    TestService otherService = new TestService() { };

    PluginServiceLoader.registerService( provider, TestService.class, service, 0 );
    PluginServiceLoader.registerService( otherProvider, TestService.class, otherService, 0 );
    try {
      assertTrue( PluginServiceLoader.loadServices( TestService.class ).contains( service ) );
      assertTrue( PluginServiceLoader.loadServices( TestService.class ).contains( otherService ) );

      PluginServiceLoader.unregisterService( provider, TestService.class );

      assertFalse( PluginServiceLoader.loadServices( TestService.class ).contains( service ) );
      assertTrue( PluginServiceLoader.loadServices( TestService.class ).contains( otherService ) );
    } finally {
      PluginServiceLoader.unregisterService( provider, TestService.class );
      PluginServiceLoader.unregisterService( otherProvider, TestService.class );
    }
  }

  @Test
  public void unregisterServiceSupportsANullProvider() throws Exception {
    TestService service = new TestService() { };

    PluginServiceLoader.registerService( null, TestService.class, service, 0 );
    try {
      assertTrue( PluginServiceLoader.loadServices( TestService.class ).contains( service ) );

      PluginServiceLoader.unregisterService( null, TestService.class );

      assertFalse( PluginServiceLoader.loadServices( TestService.class ).contains( service ) );
    } finally {
      PluginServiceLoader.unregisterService( null, TestService.class );
    }
  }

  private interface TestService {
  }
}
