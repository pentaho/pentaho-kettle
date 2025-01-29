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
