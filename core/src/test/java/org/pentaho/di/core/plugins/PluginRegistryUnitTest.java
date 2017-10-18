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

package org.pentaho.di.core.plugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.LoggingPluginType;
import org.pentaho.di.core.row.RowBuffer;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.util.UUID;

public class PluginRegistryUnitTest {

  @Test
  public void getGetPluginInformation() throws KettlePluginException {
    RowBuffer result = PluginRegistry.getInstance().getPluginInformation( BasePluginType.class );
    assertNotNull( result );
    assertEquals( 8, result.getRowMeta().size() );

    for ( ValueMetaInterface vmi : result.getRowMeta().getValueMetaList() ) {
      assertEquals( ValueMetaInterface.TYPE_STRING, vmi.getType() );
    }
  }

  /**
   * Test that additional plugin mappings can be added via the PluginRegistry.
   */
  @Test
  public void testSupplementalPluginMappings() throws Exception {
    PluginRegistry registry = PluginRegistry.getInstance();
    PluginInterface mockPlugin = mock( PluginInterface.class );
    when( mockPlugin.getIds() ).thenReturn( new String[] { "mockPlugin"} );
    when( mockPlugin.matches( "mockPlugin" ) ).thenReturn( true );
    when( mockPlugin.getName() ).thenReturn( "mockPlugin" );
    doReturn( LoggingPluginType.class ).when( mockPlugin ).getPluginType();
    registry.registerPlugin( LoggingPluginType.class, mockPlugin );


    registry.addClassFactory( LoggingPluginType.class, String.class, "mockPlugin", () -> { return "Foo"; } );
    String result = registry.loadClass( LoggingPluginType.class, "mockPlugin", String.class );
    assertEquals( "Foo", result );
    assertEquals( 2, registry.getPlugins( LoggingPluginType.class ).size() );


    // Now add another mapping and verify that it works and the existing supplementalPlugin was reused.
    UUID uuid = UUID.randomUUID();
    registry.addClassFactory( LoggingPluginType.class, UUID.class, "mockPlugin", () -> uuid );
    UUID out = registry.loadClass( LoggingPluginType.class, "mockPlugin", UUID.class );
    assertEquals( uuid, out );
    assertEquals( 2, registry.getPlugins( LoggingPluginType.class ).size() );


  }
}
