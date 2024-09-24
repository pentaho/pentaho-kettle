/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.di.repository.pur;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class ActiveCacheCallable<Key, Value> implements Callable<ActiveCacheResult<Value>> {
  private final Key key;
  private final ActiveCacheLoader<Key, Value> loader;
  private final Object syncObject;
  private final Map<Key, ActiveCacheResult<Value>> valueMap;
  private final Map<Key, Future<ActiveCacheResult<Value>>> loadingMap;

  public ActiveCacheCallable( Object syncObject, Map<Key, ActiveCacheResult<Value>> valueMap,
      Map<Key, Future<ActiveCacheResult<Value>>> loadingMap, Key key, ActiveCacheLoader<Key, Value> loader ) {
    this.syncObject = syncObject;
    this.valueMap = valueMap;
    this.loadingMap = loadingMap;
    this.key = key;
    this.loader = loader;
  }

  @Override
  public ActiveCacheResult<Value> call() throws Exception {
    ActiveCacheResult<Value> result = null;
    try {
      result = new ActiveCacheResult<Value>( loader.load( key ), null );
    } catch ( Exception throwable ) {
      result = new ActiveCacheResult<Value>( null, throwable );
    } finally {
      synchronized ( syncObject ) {
        loadingMap.remove( key );
        // Only cache successful calls
        if ( result.getException() == null ) {
          valueMap.put( key, result );
        }
      }
    }
    return result;
  }
}
