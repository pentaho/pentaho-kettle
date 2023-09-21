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
package org.pentaho.di.ui.spoon;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class SpoonPluginManagerTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Spy
  private SpoonPluginManager spoonPluginManager;

  @Mock
  private PluginRegistry pluginRegistry;

  @Mock
  private PluginInterface plugin1, plugin2;

  @Mock
  private SpoonPerspective spoonPerspective;

  @Mock
  private SpoonPerspectiveManager spoonPerspectiveManager;

  @Mock
  private XulDomContainer xulDomContainer;

  private SpoonPluginInterface spoonPluginInterface1 = new DummyPluginInterface();
  private SpoonPluginInterface spoonPluginInterface2 = new DummyPluginInterface();
  private DummyLifecycleListener dummyLifecycleListener = new DummyLifecycleListener();

  private Map<SpoonPluginInterface, Integer> applies = new HashMap<>();
  private AtomicInteger notifications = new AtomicInteger();

  @Before
  public void setUp() throws KettlePluginException {
    when( spoonPluginManager.getPluginRegistry() ).thenReturn( pluginRegistry );
    when( spoonPluginManager.getSpoonPerspectiveManager() ).thenReturn( spoonPerspectiveManager );
    when( pluginRegistry.loadClass( any( PluginInterface.class ) ) )
        .thenReturn( spoonPluginInterface1, spoonPluginInterface2 );
  }

  @Test
  public void testPluginAdded() throws Exception {
    spoonPluginManager.pluginAdded( plugin1 );

    verify( spoonPerspectiveManager ).addPerspective( spoonPerspective );
    assertEquals( 1, spoonPluginManager.getPlugins().size() );
    assertSame( spoonPluginInterface1, spoonPluginManager.getPlugins().get( 0 ) );
  }

  @Test
  public void testPluginRemoved() throws Exception {
    spoonPluginManager.pluginAdded( plugin1 );
    spoonPluginManager.pluginRemoved( plugin1 );

    verify( spoonPerspectiveManager ).removePerspective( spoonPerspective );
  }

  @Test
  public void testApplyPluginsForContainer() throws Exception {
    spoonPluginManager.pluginAdded( plugin1 );
    spoonPluginManager.pluginAdded( plugin2 );
    spoonPluginManager.applyPluginsForContainer( "trans-graph", xulDomContainer );

    assertEquals( 2, applies.size() );
    assertEquals( 1, (int) applies.get( spoonPluginInterface1 ) );
    assertEquals( 1, (int) applies.get( spoonPluginInterface2 ) );
  }

  @Test
  public void testGetPlugins() throws Exception {
    spoonPluginManager.pluginAdded( plugin1 );
    spoonPluginManager.pluginAdded( plugin2 );

    List<SpoonPluginInterface> pluginInterfaces = spoonPluginManager.getPlugins();

    assertEquals( 2, pluginInterfaces.size() );
    assertTrue( pluginInterfaces
        .containsAll( Arrays.asList( spoonPluginInterface1, spoonPluginInterface2 ) ) );
  }

  @Test
  public void testNotifyLifecycleListeners() throws Exception {
    spoonPluginManager.pluginAdded( plugin1 );
    spoonPluginManager.pluginAdded( plugin2 );

    spoonPluginManager.notifyLifecycleListeners( SpoonLifecycleListener.SpoonLifeCycleEvent.STARTUP );

    assertEquals( 2, notifications.get() );
  }

  @SpoonPluginCategories( { "trans-graph" } )
  private class DummyPluginInterface implements SpoonPluginInterface {
    @Override public void applyToContainer( String category, XulDomContainer container ) throws XulException {
      if ( applies.get( this ) == null ) {
        applies.put( this, 1 );
      } else {
        applies.put( this, applies.get( this ) + 1 );
      }
    }

    @Override public SpoonLifecycleListener getLifecycleListener() {
      return dummyLifecycleListener;
    }

    @Override public SpoonPerspective getPerspective() {
      return spoonPerspective;
    }
  }

  private class DummyLifecycleListener implements SpoonLifecycleListener {
    @Override public void onEvent( SpoonLifeCycleEvent evt ) {
      if ( evt == SpoonLifeCycleEvent.STARTUP ) {
        notifications.incrementAndGet();
      }
    }
  }
}
