/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2018 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PluginTest {

  /**
   * test that a plugin's fragment is added to the plugin
   */
  @Test
  public void testFragmentMerge() {
    Map<Class<?>, String> classMap = new HashMap<Class<?>, String>() {{
        put( PluginTypeInterface.class, String.class.getName() );
      }};
    List<String> libraries = new ArrayList<String>() {{
        add( String.class.getName() );
      }};

    PluginInterface plugin =
      new Plugin(
        new String[] {"plugintest"}, BasePluginType.class, String.class, "", "plugin test", "",
        "", false, null, false, classMap, libraries, null,
        null, null, null, null );

    PluginInterface fragment =
      new Plugin(
        new String[] {"plugintest"}, BaseFragmentType.class, String.class, "", null,
        "fragment test",
        "fragment image",
        false, null, false,
        new HashMap<Class<?>, String>()  {{
            put( PluginTypeListener.class, Integer.class.getName() );
          }},
        new ArrayList<String>() {{
          add( Integer.class.getName() );
          }},
        null, null,
        "fragment doc url",
        "fragment cases url",
        "fragment forum url" );

    plugin.merge( fragment );

    assertTrue( classMap.containsKey( PluginTypeListener.class ) );
    assertEquals( libraries.size(), 2 );
    assertTrue( libraries.contains( Integer.class.getName() ) );
    assertEquals( "", plugin.getDescription() );
    assertEquals( fragment.getImageFile(), plugin.getImageFile() );
    assertEquals( fragment.getDocumentationUrl(), plugin.getDocumentationUrl() );
    assertEquals( fragment.getCasesUrl(), plugin.getCasesUrl() );
    assertEquals( fragment.getForumUrl(), plugin.getForumUrl() );
  }

  @Test
  public void testFragmentMergeWithNull() {
    PluginInterface plugin =
      new Plugin(
        new String[] {"plugintest"}, BasePluginType.class, String.class, "", "plugin test", "",
        "a", false, null, false, new HashMap<>(), Collections.emptyList(), null,
        null, null, null, null );

    plugin.merge( null );
    assertEquals( "a", plugin.getImageFile() );

    PluginInterface fragment = mock( PluginInterface.class );
    when( fragment.getImageFile() ).thenReturn( "b" );

    plugin.merge( fragment );
    assertEquals( "b", plugin.getImageFile() );

    when( fragment.getImageFile() ).thenReturn( null );

    plugin.merge( fragment );
    assertEquals( "b", plugin.getImageFile() );
  }
}
