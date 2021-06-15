/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class SharedInstanceBuffer<K, I> implements Serializable {

  private final Lock readLock;
  private final Lock writeLock;
  private final Map<K, I> store;

  public SharedInstanceBuffer() {
    ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    readLock = readWriteLock.readLock();
    writeLock = readWriteLock.writeLock();
    store = new HashMap<>();
  }

  public I get( K key, InstanceCreator<K, I> instanceCreator ) {
    I result = getInstance( key );
    if( result == null ) {
      result = createInstance( key, instanceCreator );
    }
    return result;
  }

  public I remove( K key ) {
    writeLock.lock();
    try {
      return store.remove( key );
    } finally {
      writeLock.unlock();
    }
  }

  private I getInstance( K key ) {
    readLock.lock();
    try {
      return store.get( key );
    } finally {
      readLock.unlock();
    }
  }

  private I createInstance( K key, InstanceCreator<K, I> instanceCreator ) {
    writeLock.lock();
    try {
      // Re-check because another thread might have acquired write lock and created an instance
      // before we did. See doc on ReentrantReadWriteLock.
      I result = store.get( key );
      if( result == null ) {
        result = instanceCreator.createInstance( key );
        store.put( key, result );
      }
      return result;
    } finally {
      writeLock.unlock();
    }
  }

  public interface InstanceCreator<K, T> extends Serializable {
    T createInstance( K key );
  }

}
