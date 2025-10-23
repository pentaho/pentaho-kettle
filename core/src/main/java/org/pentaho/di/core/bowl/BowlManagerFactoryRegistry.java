/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.core.bowl;

import org.pentaho.di.connections.ConnectionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * A Registry that holds Factories that generate Manager classes from a Bowl.
 * <p>
 * Used by BaseBowl to create single-instance-per-bowl manager instances from the factory method here.
 *
 */
public class BowlManagerFactoryRegistry {
  private static final BowlManagerFactoryRegistry instance = new BowlManagerFactoryRegistry();

  private final Map<Class<?>, ManagerFactory<?>> factories = new HashMap<>();

  private BowlManagerFactoryRegistry() {
    // initialize manager factories from core
    registerManagerFactory( ConnectionManager.class, new CachingManagerFactory<ConnectionManager>(
      ConnectionManager.class, ConnectionManager::getInstance ) );
  }

  public static BowlManagerFactoryRegistry getInstance() {
    return instance;
  }

  /**
   * Register a ManagerFactory for the given manager class.
   * </p>
   * Any manager that caches objects should implement CachingManager so that cached objects can be cleared when
   * refreshing the UI. See BaseBowl.clearManagers() and CachingManager.notifyChanged().
   * @param <T> the type of object being managed
   * @param managerClass the interface class of the manager
   * @param factoryMethod a method to create a new manager instance from a Bowl
   */
  public synchronized <T> void registerManagerFactory( Class<T> managerClass, ManagerFactory<T> factoryMethod ) {
    factories.put( managerClass, factoryMethod );
  }

  public synchronized <T> ManagerFactory<T> getManagerFactory( Class<T> managerClass ) {
    @SuppressWarnings( "unchecked" )
    ManagerFactory<T> factory = (ManagerFactory<T>) factories.get( managerClass );
    return factory;
  }

}
