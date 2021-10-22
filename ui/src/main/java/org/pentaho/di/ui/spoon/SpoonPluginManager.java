/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeListener;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SpoonPluginManager is a singleton class which loads all SpoonPlugins from the SPOON_HOME/plugins/spoon directory.
 * <p/>
 * Spoon Plugins are able to listen for SpoonLifeCycleEvents and can register categorized XUL Overlays to be retrieved
 * later.
 * <p/>
 * Spoon Plugins are deployed as directories under the SPOON_HOME/plugins/spoon directory. Each plugin must provide a
 * build.xml as the root of it's directory and have any required jars under a "lib" directory.
 * <p/>
 * The plugin.xml format is Spring-based e.g. <beans xmlns="http://www.springframework.org/schema/beans"
 * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation=
 * "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.1.xsd">
 * <p/>
 * <bean id="PLUGIN_ID" class="org.foo.SpoonPluginClassName"></bean> </beans>
 *
 * @author nbaker
 */
public class SpoonPluginManager implements PluginTypeListener {

  private static SpoonPluginManager instance = new SpoonPluginManager();
  private Map<Object, SpoonPluginInterface> plugins = new HashMap<>();
  private Map<String, List<SpoonPluginInterface>> pluginCategoryMap = new HashMap<>();

  @Override public void pluginAdded( final Object serviceObject ) {
    try {
      SpoonPluginInterface spoonPluginInterface =
          (SpoonPluginInterface) getPluginRegistry().loadClass( (PluginInterface) serviceObject );

      if ( plugins.get( serviceObject ) != null ) {
        return;
      }
      SpoonPluginCategories categories = spoonPluginInterface.getClass().getAnnotation( SpoonPluginCategories.class );
      if ( categories != null ) {
        for ( String cat : categories.value() ) {
          List<SpoonPluginInterface> categoryList = pluginCategoryMap.get( cat );
          if ( categoryList == null ) {
            categoryList = new ArrayList<>();
            pluginCategoryMap.put( cat, categoryList );
          }
          categoryList.add( spoonPluginInterface );
        }
      }

      if ( spoonPluginInterface.getPerspective() != null ) {
        getSpoonPerspectiveManager().addPerspective( spoonPluginInterface.getPerspective() );
      }

      plugins.put( serviceObject, spoonPluginInterface );

    } catch ( KettlePluginException e ) {
      e.printStackTrace();
    }
  }

  @Override public void pluginRemoved( Object serviceObject ) {
    SpoonPluginInterface spoonPluginInterface = plugins.get( serviceObject );
    if ( spoonPluginInterface == null ) {
      return;
    }

    SpoonPluginCategories categories = spoonPluginInterface.getClass().getAnnotation( SpoonPluginCategories.class );
    if ( categories != null ) {
      for ( String cat : categories.value() ) {
        List<SpoonPluginInterface> categoryList = pluginCategoryMap.get( cat );
        categoryList.remove( spoonPluginInterface );
      }
    }

    if ( spoonPluginInterface.getPerspective() != null ) {
      getSpoonPerspectiveManager().removePerspective( spoonPluginInterface.getPerspective() );
    }

    plugins.remove( serviceObject );
  }

  @Override public void pluginChanged( Object serviceObject ) {
    // Not implemented yet
  }

  /**
   * Return the single instance of this class
   *
   * @return SpoonPerspectiveManager
   */
  public static SpoonPluginManager getInstance() {
    if ( Const.isRunningOnWebspoonMode() ) {
      try {
        Class singletonUtil = Class.forName( "org.eclipse.rap.rwt.SingletonUtil" );
        Method getDeclaredMethod = singletonUtil.getDeclaredMethod( "getSessionInstance", Class.class );
        return (SpoonPluginManager) getDeclaredMethod.invoke( null, SpoonPluginManager.class );
      } catch ( ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e ) {
        e.printStackTrace();
        return null;
      }
    } else {
      return instance;
    }
  }

  public void applyPluginsForContainer( final String category, final XulDomContainer container ) throws XulException {
    List<SpoonPluginInterface> plugins = pluginCategoryMap.get( category );
    if ( plugins != null ) {
      for ( SpoonPluginInterface sp : plugins ) {
        sp.applyToContainer( category, container );
      }
    }
  }

  /**
   * Returns an unmodifiable list of all Spoon Plugins.
   *
   * @return list of plugins
   */
  public List<SpoonPluginInterface> getPlugins() {
    return Collections.unmodifiableList( Arrays.asList( plugins.values().toArray( new SpoonPluginInterface[] {} ) ) );
  }

  /**
   * Notifies all registered SpoonLifecycleListeners of the given SpoonLifeCycleEvent.
   *
   * @param evt event to notify listeners about
   */
  public void notifyLifecycleListeners( SpoonLifecycleListener.SpoonLifeCycleEvent evt ) {
    for ( SpoonPluginInterface p : plugins.values() ) {
      SpoonLifecycleListener listener = p.getLifecycleListener();
      if ( listener != null ) {
        listener.onEvent( evt );
      }
    }
  }

  PluginRegistry getPluginRegistry() {
    return PluginRegistry.getInstance();
  }

  SpoonPerspectiveManager getSpoonPerspectiveManager() {
    return SpoonPerspectiveManager.getInstance();
  }

  private SpoonPluginManager() {
    PluginRegistry pluginRegistry = getPluginRegistry();
    pluginRegistry.addPluginListener( SpoonPluginType.class, this );

    List<PluginInterface> plugins = pluginRegistry.getPlugins( SpoonPluginType.class );
    for ( PluginInterface plug : plugins ) {
      pluginAdded( plug );
    }
  }
}
