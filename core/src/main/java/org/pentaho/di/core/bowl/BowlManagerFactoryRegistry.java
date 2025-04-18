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
import org.pentaho.di.connections.ConnectionUpdateSubscriber;
import org.pentaho.di.core.exception.KettleException;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * A Registry that holds Factories that generate Manager classes from a Bowl.
 * <p>
 * Used by BaseBowl to create single-instance-per-bowl manager instances from the factory method here.
 *
 */
public class BowlManagerFactoryRegistry {
  private static final BowlManagerFactoryRegistry instance = new BowlManagerFactoryRegistry();

  private final Map<Class<?>, ManagerFactory<?>> factories = new HashMap<>();

  private static class ConnectionManagerFactory implements ManagerFactory<ConnectionManager> {
    // need to hang onto the subscriber as long as the Bowl exists
    private final WeakHashMap<Bowl, ConnectionUpdateSubscriber> subscribers = new WeakHashMap<>();

    public ConnectionManager apply( Bowl bowl ) throws KettleException {
      ConnectionManager connectionManager = ConnectionManager.getInstance( bowl );
      if ( !bowl.getParentBowls().isEmpty() ) {
        ConnectionUpdateSubscriber subscriber = connectionManager::reset;
        for ( Bowl parentBowl : bowl.getParentBowls() ) {
          parentBowl.getManager( ConnectionManager.class ).addSubscriber( subscriber );
        }
        subscribers.put( bowl, subscriber );
      }
      return connectionManager;
    }
  }

  private BowlManagerFactoryRegistry() {
    // initialize manager factories from core
    registerManagerFactory( ConnectionManager.class, new ConnectionManagerFactory() );
  }

  public static BowlManagerFactoryRegistry getInstance() {
    return instance;
  }

  public synchronized <T> void registerManagerFactory( Class<T> managerClass, ManagerFactory<T> factoryMethod ) {
    factories.put( managerClass, factoryMethod );
  }

  public synchronized <T> ManagerFactory<T> getManagerFactory( Class<T> managerClass ) {
    @SuppressWarnings( "unchecked" )
    ManagerFactory<T> factory = (ManagerFactory<T>) factories.get( managerClass );
    return factory;
  }

}
