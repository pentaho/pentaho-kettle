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


package org.pentaho.di.core.lifecycle;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.plugins.LifecyclePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.core.plugins.PluginTypeListener;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LifecycleSupport {
  @VisibleForTesting protected static PluginRegistry registry = PluginRegistry.getInstance();
  private Set<LifecycleListener> lifeListeners;
  private boolean started;
  private LifeEventHandler handler;

  public LifecycleSupport() {
    lifeListeners =
      Collections.synchronizedSet( loadPlugins( LifecyclePluginType.class, LifecycleListener.class ) );

    final PluginRegistry registry = PluginRegistry.getInstance();
    registry.addPluginListener( LifecyclePluginType.class, new PluginTypeListener() {
      public void pluginAdded( Object serviceObject ) {
        LifecycleListener listener = null;
        try {
          listener = (LifecycleListener) PluginRegistry.getInstance().loadClass( (PluginInterface) serviceObject );
        } catch ( KettlePluginException e ) {
          e.printStackTrace();
          return;
        }

        lifeListeners.add( listener );
        if ( started ) {
          try {
            listener.onStart( handler );
          } catch ( LifecycleException e ) {
            e.printStackTrace();
          }
        }

      }

      public void pluginRemoved( Object serviceObject ) {
        lifeListeners.remove( serviceObject );
      }

      public void pluginChanged( Object serviceObject ) {
      }
    } );
  }

  /**
   * Instantiate the main plugin class types for the plugin type provided from the set of registered plugins via
   * {@link PluginRegistry}.
   *
   * @param pluginType
   *          Type of plugin whose main class types should be instanticated
   * @return Set of plugin main class instances (a.k.a. plugins)
   */
  static <T> Set<T> loadPlugins( Class<? extends PluginTypeInterface> pluginType, Class<T> mainPluginClass ) {
    Set<T> pluginInstances = new HashSet<T>();
    List<PluginInterface> plugins = registry.getPlugins( pluginType );
    for ( PluginInterface plugin : plugins ) {
      try {
        pluginInstances.add( registry.loadClass( plugin, mainPluginClass ) );
      } catch ( Throwable e ) {
        LogChannel.GENERAL.logError( "Unexpected error loading class for plugin " + plugin.getName(), e );
      }
    }
    return pluginInstances;
  }

  public void onStart( LifeEventHandler handler ) throws LifecycleException {
    // Caching the last handler and the fact that start has been called. This would cause problems if onStart
    // is called by more than one handler.
    this.handler = handler;
    started = true;
    for ( LifecycleListener listener : lifeListeners ) {
      listener.onStart( handler );

    }
  }

  public void onExit( LifeEventHandler handler ) throws LifecycleException {
    for ( LifecycleListener listener : lifeListeners ) {
      listener.onExit( handler );
    }
  }
}
