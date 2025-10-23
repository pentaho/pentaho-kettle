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

import org.pentaho.di.core.exception.KettleException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;

public abstract class BaseBowl implements Bowl {

  private final Map<Class<?>, Object> managerInstances = new ConcurrentHashMap<>();
  private final Set<Bowl> parentBowls = ConcurrentHashMap.newKeySet();

  @Override
  public <T> T getManager( Class<T> managerClass ) throws KettleException {
    // can't use computeIfAbsent here because we want exceptions to propagate. :(
    T result = managerClass.cast( managerInstances.get( managerClass ) );
    if ( result != null ) {
      return result;
    }
    synchronized ( this ) {
      result = managerClass.cast( managerInstances.get( managerClass ) );
      if ( result == null ) {
        ManagerFactory<T> factory = BowlManagerFactoryRegistry.getInstance().getManagerFactory( managerClass );
        if ( factory == null ) {
          throw new KettleException( "No Factory found for " + managerClass );
        }
        result = factory.apply( this );
        if ( result == null ) {
          throw new KettleException( "Unable to create manager for " + managerClass );
        }
        managerInstances.put( managerClass, result );
      }
      return result;
    }
  }

  @Override
  public Set<Bowl> getParentBowls() {
    return parentBowls;
  }

  public void addParentBowl( Bowl parent ) {
    parentBowls.add( parent );
  }

  // use with caution. Clears all manager instances.
  // Other parts of the code rely on WeakReferences to managers to allow them to be GC'd when no
  // longer in use. This should generally only be used in tests
  public synchronized void clearManagers() {
    managerInstances.clear();
  }

  @Override
  public String getLevelDisplayName() {
    return "";
  }

  @Override
  public void clearCache() {
    // note that most metastores do not cache, caching is usually done in MetastoreFactory
    for ( Object managerObj : managerInstances.values() ) {
      if ( managerObj instanceof CachingManager manager ) {
        manager.clearCache();
      }
    }
    getSharedObjectsIO().clearCache();
  }
}

