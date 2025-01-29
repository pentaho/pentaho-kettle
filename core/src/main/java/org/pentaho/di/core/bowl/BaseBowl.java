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

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;

public abstract class BaseBowl implements Bowl {

  private final Map<Class<?>, Object> managerInstances = new ConcurrentHashMap();
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

  // use with caution.
  public synchronized void clearManagers() {
    managerInstances.clear();
  }
}

