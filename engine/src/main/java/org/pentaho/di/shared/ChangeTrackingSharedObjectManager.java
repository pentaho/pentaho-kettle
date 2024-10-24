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

package org.pentaho.di.shared;

import org.pentaho.di.core.exception.KettleException;

import java.util.List;

/**
 * This Manager wraps another manager and tracks changes.
 *
*/
public class ChangeTrackingSharedObjectManager<T extends SharedObjectInterface<T>> implements SharedObjectsManagementInterface<T> {

  private final SharedObjectsManagementInterface<T> parent;
  private volatile boolean changed = false;

  public ChangeTrackingSharedObjectManager( SharedObjectsManagementInterface<T> parent ) {
    this.parent = parent;
  }

  @Override
  public void add( T object ) throws KettleException {
    parent.add( object );
    changed = true;
  }

  @Override
  public T get( String name) throws KettleException {
    return parent.get( name );
  }

  @Override
  public List<T> getAll() throws KettleException {
    return parent.getAll();
  }

  @Override
  public void clear() throws KettleException {
    parent.clear();
    changed = true;
  }

  @Override
  public void remove( T object ) throws KettleException {
    parent.remove( object );
    changed = true;
  }

  @Override
  public void remove( String name) throws KettleException {
    parent.remove( name );
    changed = true;
  }

  public boolean hasChanged() {
    return changed;
  }


  public void clearChanged() {
    changed = false;
  }

}
