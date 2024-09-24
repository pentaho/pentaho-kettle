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
