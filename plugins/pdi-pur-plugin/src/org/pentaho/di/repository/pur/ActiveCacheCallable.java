/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
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
