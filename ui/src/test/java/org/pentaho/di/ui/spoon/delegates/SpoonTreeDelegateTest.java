/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.delegates;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.After;
import org.mockito.MockedStatic;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TreeSelection;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.mockStatic;

public class SpoonTreeDelegateTest {

  private Spoon spoon = mock( Spoon.class );
  private static MockedStatic<ConstUI> constUI;
  private static MockedStatic<PluginRegistry> pluginRegistry;

  @Before
  public void setup() {
    constUI = mockStatic( ConstUI.class );
    pluginRegistry = mockStatic( PluginRegistry.class );
  }

  @After
  public void cleanUp() {
    constUI.close();
    pluginRegistry.close();
  }

  @Test
  public void getTreeObjects_getStepByName() {
    SpoonTreeDelegate std = spy( new SpoonTreeDelegate( spoon ) );

    Tree selection = mock( Tree.class );
    Tree core = mock( Tree.class );
    TreeItem item = mock( TreeItem.class );
    PluginInterface step = mock( PluginInterface.class );
    PluginRegistry registry = mock( PluginRegistry.class );

    TreeItem[] items = new TreeItem[] { item };

    when( ConstUI.getTreeStrings( item ) ).thenReturn( new String[] { "Output", "Delete" } );
    when( PluginRegistry.getInstance() ).thenReturn( registry );

    doReturn( items ).when( core ).getSelection();
    doReturn( null ).when( item ).getData( anyString() );
    doReturn( step ).when( registry ).findPluginWithName( StepPluginType.class, "Delete" );

    spoon.showJob = false;
    spoon.showTrans = true;

    TreeSelection[] ts = std.getTreeObjects( core, selection, core );

    assertEquals( 1, ts.length );
    assertEquals( step, ts[ 0 ].getSelection() );
  }

  @Test
  public void getTreeObjects_getStepById() {
    SpoonTreeDelegate std = spy( new SpoonTreeDelegate( spoon ) );

    Tree selection = mock( Tree.class );
    Tree core = mock( Tree.class );
    TreeItem item = mock( TreeItem.class );
    PluginInterface step = mock( PluginInterface.class );
    PluginRegistry registry = mock( PluginRegistry.class );

    TreeItem[] items = new TreeItem[] { item };

    when( ConstUI.getTreeStrings( item ) ).thenReturn( new String[] { "Output", "Avro Output" } );
    when( PluginRegistry.getInstance() ).thenReturn( registry );

    doReturn( items ).when( core ).getSelection();
    doReturn( "AvroOutputPlugin" ).when( item ).getData( anyString() );
    doReturn( step ).when( registry ).findPluginWithId( StepPluginType.class, "AvroOutputPlugin" );

    spoon.showJob = false;
    spoon.showTrans = true;

    TreeSelection[] ts = std.getTreeObjects( core, selection, core );

    assertEquals( 1, ts.length );
    assertEquals( step, ts[ 0 ].getSelection() );
  }
}
