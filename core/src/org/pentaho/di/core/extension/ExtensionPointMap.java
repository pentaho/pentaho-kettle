/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class maintains a map of ExtensionPointInterface object to its name.
 */
public class ExtensionPointMap {

  private static LogChannelInterface log = new LogChannel( "ExtensionPointMap" );
  private static ExtensionPointMap INSTANCE = new ExtensionPointMap( PluginRegistry.getInstance() );

  private final PluginRegistry registry;
  private Table<String, String, Supplier<ExtensionPointInterface>> extensionPointPluginMap;

  private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

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

  private void unlockForWrite( boolean wasWriteLockedByCurrentThread ) {
    if ( !wasWriteLockedByCurrentThread ) {
      lock.writeLock().unlock();
    }
    registry.getLock().writeLock().unlock();
  }

  private boolean lockForWrite() {
    boolean wasWriteLockedByCurrentThread = false;
    while ( true ) {
      if ( lock.isWriteLockedByCurrentThread() ) {
        lock.writeLock().unlock();
        wasWriteLockedByCurrentThread = true;
      }
      if ( registry.getLock().writeLock().tryLock() ) {
        if ( lock.writeLock().tryLock() ) {
          return wasWriteLockedByCurrentThread;
        } else {
          registry.getLock().writeLock().unlock();
        }
      }
    }
  }

  public static ExtensionPointMap getInstance() {
    return INSTANCE;
  }

  /**
   * Retrieves the extension point map
   * 
   * @return
   */
  Map<String, Map<String, Supplier<ExtensionPointInterface>>> getMap() {
    lock.readLock().lock();
    try {
      return extensionPointPluginMap.rowMap();
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * Add the extension point plugin to the map
   * 
   * @param extensionPointPlugin
   */
  void addExtensionPoint( PluginInterface extensionPointPlugin ) {
    boolean wasWriteLockedByCurrentThread = lockForWrite();
    try {
      for ( String id : extensionPointPlugin.getIds() ) {
        extensionPointPluginMap.put( extensionPointPlugin.getName(), id, createLazyLoader( extensionPointPlugin ) );
      }
    } finally {
      unlockForWrite( wasWriteLockedByCurrentThread );
    }
  }

  /**
   * Remove the extension point plugin from the map
   * 
   * @param extensionPointPlugin
   */
  void removeExtensionPoint( PluginInterface extensionPointPlugin ) {
    boolean wasWriteLockedByCurrentThread = lockForWrite();
    try {
      for ( String id : extensionPointPlugin.getIds() ) {
        extensionPointPluginMap.remove( extensionPointPlugin.getName(), id );
      }
    } finally {
      unlockForWrite( wasWriteLockedByCurrentThread );
    }
  }

  /**
   * Reinitialize the extension point plugins map
   */
  public void reInitialize() {
    boolean wasWriteLockedByCurrentThread = lockForWrite();
    try {
      extensionPointPluginMap = HashBasedTable.create();
      final PluginRegistry registry = PluginRegistry.getInstance();
      List<PluginInterface> extensionPointPlugins = registry.getPlugins( ExtensionPointPluginType.class );
      for ( PluginInterface extensionPointPlugin : extensionPointPlugins ) {
        addExtensionPoint( extensionPointPlugin );
      }
    } finally {
      unlockForWrite( wasWriteLockedByCurrentThread );
    }
  }

  Supplier<ExtensionPointInterface> createLazyLoader( PluginInterface extensionPointPlugin ) {
    return Suppliers.memoize( new ExtensionPointLoader( extensionPointPlugin ) );
  }

  void callExtensionPoint( LogChannelInterface log, String id, Object object ) throws KettleException {
    boolean wasWriteLockedByCurrentThread = lockForWrite();
    try {
      Map<String, Supplier<ExtensionPointInterface>> value = extensionPointPluginMap.rowMap().get( id );
      if ( value != null ) {
        for ( Supplier<ExtensionPointInterface> extensionPoint : value.values() ) {
          extensionPoint.get().callExtensionPoint( log, object );
        }
      }
    } finally {
      unlockForWrite( wasWriteLockedByCurrentThread );
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
