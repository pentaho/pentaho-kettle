/*!
 * Copyright 2024 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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

