/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.plugins;

import org.junit.Test;

import java.net.URL;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Andrey Khayrutdinov
 */
public class PluginRegistryClassloadingTest {

  private static final String PLUGIN_DIRECTORY =
    "http://pentaho.com/" + PluginRegistryClassloadingTest.class.getSimpleName();

  @Test
  public void parentFirstClassLoaderIsCreatedForPatternedPlugin() throws Exception {
    Plugin plugin = createPluginFor( mock( PluginTypeInterface.class ).getClass(), "TestPlugin", "TestCategory",
      PluginMainClass.class );

    PluginRegistry registry = PluginRegistry.getInstance();
    registry.addParentClassLoaderPatterns( plugin, new String[] { PluginMainClass.class.getName() } );
    try {
      KettleURLClassLoader cl = registry.createClassLoader( plugin );
      assertThat( cl, is( instanceOf( KettleSelectiveParentFirstClassLoader.class ) ) );
    } finally {
      registry.removeParentClassLoaderPatterns( plugin );
    }
  }

  @Test
  public void simpleClassLoaderIsCreatedForUnpatternedPlugin() throws Exception {
    Plugin plugin = createPluginFor( mock( PluginTypeInterface.class ).getClass(), "TestPlugin", "TestCategory",
      PluginMainClass.class );

    PluginRegistry registry = PluginRegistry.getInstance();
    KettleURLClassLoader cl = registry.createClassLoader( plugin );
    assertThat( cl, is( not( instanceOf( KettleSelectiveParentFirstClassLoader.class ) ) ) );
  }

  @Test
  public void parentFirstClassLoaderIsCreatedForAllPluginsHavingTheSameDirAsPatterned() throws Exception {
    Plugin plugin1 = createPluginFor( mock( PluginTypeInterface.class ).getClass(), "TestPlugin", "TestCategory",
      PluginMainClass.class );
    Plugin plugin2 = createPluginFor( mock( BasePluginType.class ).getClass(), "TestPlugin2", "TestCategory2",
      AnotherPluginMainClass.class );
    assertFalse( plugin1.getPluginType().equals( plugin2.getPluginType() ) );

    PluginRegistry registry = PluginRegistry.getInstance();
    registry.addParentClassLoaderPatterns( plugin1, new String[] { PluginMainClass.class.getName() } );
    try {
      KettleURLClassLoader cl = registry.createClassLoader( plugin2 );
      assertThat( cl, is( instanceOf( KettleSelectiveParentFirstClassLoader.class ) ) );
    } finally {
      registry.removeParentClassLoaderPatterns( plugin1 );
    }
  }

  private static Plugin createPluginFor( Class<? extends PluginTypeInterface> pluginType, String id, String category,
                                         Class<?> mainClass ) throws Exception {
    Map<Class<?>, String> classMap = Collections.<Class<?>, String>singletonMap( mainClass, mainClass.getName() );
    return
      new Plugin( new String[] { id }, pluginType, mainClass,
        category, id, "", null, false, false,
        classMap, Collections.<String>emptyList(), "", new URL( PLUGIN_DIRECTORY ) );
  }

  public static class PluginMainClass {
  }

  public static class AnotherPluginMainClass {
  }
}
