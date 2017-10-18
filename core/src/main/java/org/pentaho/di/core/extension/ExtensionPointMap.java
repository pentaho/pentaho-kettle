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
package org.pentaho.di.core.extension;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeListener;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class maintains a map of ExtensionPointInterface object to its name.
 */
public class ExtensionPointMap {

  private static LogChannelInterface log = new LogChannel( "ExtensionPointMap" );
  private static ExtensionPointMap INSTANCE = new ExtensionPointMap( PluginRegistry.getInstance() );

  private final PluginRegistry registry;
  private Table<String, String, Supplier<ExtensionPointInterface>> extensionPointPluginMap;

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

  private ExtensionPointMap( PluginRegistry pluginRegistry ) {
    this.registry = pluginRegistry;
    extensionPointPluginMap = HashBasedTable.create();
    registry.addPluginListener( ExtensionPointPluginType.class, new PluginTypeListener() {

      @Override
      public void pluginAdded( Object serviceObject ) {
        addExtensionPoint( (PluginInterface) serviceObject );
      }

      @Override
      public void pluginRemoved( Object serviceObject ) {
        removeExtensionPoint( (PluginInterface) serviceObject );
      }

      @Override
      public void pluginChanged( Object serviceObject ) {
        removeExtensionPoint( (PluginInterface) serviceObject );
        addExtensionPoint( (PluginInterface) serviceObject );
      }

    } );

    List<PluginInterface> extensionPointPlugins = registry.getPlugins( ExtensionPointPluginType.class );
    for ( PluginInterface extensionPointPlugin : extensionPointPlugins ) {
      addExtensionPoint( extensionPointPlugin );
    }
  }

  public static ExtensionPointMap getInstance() {
    return INSTANCE;
  }

  /**
   * Add the extension point plugin to the map
   * 
   * @param extensionPointPlugin
   */
  public void addExtensionPoint( PluginInterface extensionPointPlugin ) {
    lock.writeLock().lock();
    try {
      for ( String id : extensionPointPlugin.getIds() ) {
        extensionPointPluginMap.put( extensionPointPlugin.getName(), id, createLazyLoader( extensionPointPlugin ) );
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Remove the extension point plugin from the map
   * 
   * @param extensionPointPlugin
   */
  public void removeExtensionPoint( PluginInterface extensionPointPlugin ) {
    lock.writeLock().lock();
    try {
      for ( String id : extensionPointPlugin.getIds() ) {
        extensionPointPluginMap.remove( extensionPointPlugin.getName(), id );
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Reinitialize the extension point plugins map
   */
  public void reInitialize() {
    lock.writeLock().lock();
    try {
      extensionPointPluginMap = HashBasedTable.create();
      final PluginRegistry registry = PluginRegistry.getInstance();
      List<PluginInterface> extensionPointPlugins = registry.getPlugins( ExtensionPointPluginType.class );
      for ( PluginInterface extensionPointPlugin : extensionPointPlugins ) {
        addExtensionPoint( extensionPointPlugin );
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  Supplier<ExtensionPointInterface> createLazyLoader( PluginInterface extensionPointPlugin ) {
    return Suppliers.memoize( new ExtensionPointLoader( extensionPointPlugin ) );
  }

  /**
   * Call the extension point(s) corresponding to the given id
   *
   * This iteration was isolated here to protect against ConcurrentModificationException using PluginRegistry's lock
   *
   * @param log     log channel to pass to extension point call
   * @param id      the id of the extension point interface
   * @param object  object to pass to extension point call
   */
  public void callExtensionPoint( LogChannelInterface log, String id, Object object ) throws KettleException {
    lock.readLock().lock();
    try {
      if ( extensionPointPluginMap.containsRow( id ) && !extensionPointPluginMap.rowMap().get( id ).values().isEmpty() ) {
        for ( Supplier<ExtensionPointInterface> extensionPoint : extensionPointPluginMap.row( id ).values() ) {
          extensionPoint.get().callExtensionPoint( log, object );
        }
      }
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * Returns the element in the position (rowId,columnId) of the table
   *
   * Useful for Unit Testing
   *
   * @param rowId     the key of the row to be accessed
   * @param columnId  the key of the column to be accessed
   */
  ExtensionPointInterface getTableValue( String rowId, String columnId ) {
    lock.readLock().lock();
    try {
      return extensionPointPluginMap.contains( rowId, columnId )
        ? extensionPointPluginMap.get( rowId, columnId ).get() : null;
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * Returns the number of rows of the table
   *
   * Useful for Unit Testing
   */
  int getNumberOfRows() {
    lock.readLock().lock();
    try {
      return extensionPointPluginMap.rowMap().size();
    } finally {
      lock.readLock().unlock();
    }
  }

  private class ExtensionPointLoader implements Supplier<ExtensionPointInterface> {
    private final PluginInterface extensionPointPlugin;

    private ExtensionPointLoader( PluginInterface extensionPointPlugin ) {
      this.extensionPointPlugin = extensionPointPlugin;
    }

    @Override public ExtensionPointInterface get() {
      try {
        return registry.loadClass( extensionPointPlugin, ExtensionPointInterface.class );
      } catch ( Exception e ) {
        getLog().logError( "Unable to load extension point for name = ["
          + ( extensionPointPlugin != null ? extensionPointPlugin.getName() : "null" ) + "]", e );
        return null;
      }
    }
  }

  public static LogChannelInterface getLog() {
    if ( log == null ) {
      log = new LogChannel( "ExtensionPointMap" );
    }
    return log;
  }
}
