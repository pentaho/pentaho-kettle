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


package org.pentaho.di.core.extension;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExtensionPointHandlerTest {
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();
  private static final String TEST_NAME = "testName";

  @Test
  public void callExtensionPointTest() throws Exception {
    PluginMockInterface pluginInterface = mock( PluginMockInterface.class );
    when( pluginInterface.getName() ).thenReturn( TEST_NAME );
    when( pluginInterface.getMainType() ).thenReturn( (Class) ExtensionPointInterface.class );
    when( pluginInterface.getIds() ).thenReturn( new String[] {"testID"} );

    ExtensionPointInterface extensionPoint = mock( ExtensionPointInterface.class );
    when( pluginInterface.loadClass( ExtensionPointInterface.class ) ).thenReturn( extensionPoint );

    PluginRegistry.addPluginType( ExtensionPointPluginType.getInstance() );
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, pluginInterface );

    final LogChannelInterface log = mock( LogChannelInterface.class );

    ExtensionPointHandler.callExtensionPoint( log, "noPoint", null );
    verify( extensionPoint, never() ).callExtensionPoint( any( LogChannelInterface.class ), any() );

    ExtensionPointHandler.callExtensionPoint( log, TEST_NAME, null );
    verify( extensionPoint, times( 1 ) ).callExtensionPoint( eq( log ), isNull() );
  }
}
