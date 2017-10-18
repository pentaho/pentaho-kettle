/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 * **************************************************************************
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
 */

package org.pentaho.di.core.extension;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginRegistry;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExtensionPointMapTest {
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
