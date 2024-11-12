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

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExtensionPointMapTest {
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();
  public static final String TEST_NAME = "testName";
  private PluginMockInterface pluginInterface;
  private ExtensionPointInterface extensionPoint;

  @Before
  public void setUp() {
    pluginInterface = mock( PluginMockInterface.class );
    when( pluginInterface.getName() ).thenReturn( TEST_NAME );
    when( pluginInterface.getMainType() ).thenReturn( (Class) ExtensionPointInterface.class );
    when( pluginInterface.getIds() ).thenReturn( new String[] {"testID"} );

    extensionPoint = mock( ExtensionPointInterface.class );
    when( pluginInterface.loadClass( ExtensionPointInterface.class ) ).thenReturn( extensionPoint );
  }

  @Test
  public void constructorTest() throws Exception {
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, pluginInterface );
    assertEquals( 1, ExtensionPointMap.getInstance().getNumberOfRows() );

    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, pluginInterface );
    assertEquals( 1, ExtensionPointMap.getInstance().getNumberOfRows() );

    PluginRegistry.getInstance().removePlugin( ExtensionPointPluginType.class, pluginInterface );
    assertEquals( 0, ExtensionPointMap.getInstance().getNumberOfRows() );

    // Verify lazy loading
    verify( pluginInterface, never() ).loadClass( any( Class.class ) );
  }

  @Test
  public void addExtensionPointTest() throws KettlePluginException {
    ExtensionPointMap.getInstance().addExtensionPoint( pluginInterface );
    assertEquals( ExtensionPointMap.getInstance().getTableValue( TEST_NAME, "testID" ), extensionPoint );

    // Verify cached instance
    assertEquals( ExtensionPointMap.getInstance().getTableValue( TEST_NAME, "testID" ), extensionPoint );
    verify( pluginInterface, times( 1 ) ).loadClass( any( Class.class ) );
  }
}
