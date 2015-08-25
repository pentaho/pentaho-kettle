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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.pentaho.di.core.util.ExecutorUtil;

public class ActiveCache<Key, Value> {
  private final Map<Key, ActiveCacheResult<Value>> valueMap;
  private final Map<Key, Future<ActiveCacheResult<Value>>> loadingMap;
  private final ActiveCacheLoader<Key, Value> loader;
  private final long timeout;
  private final ExecutorServiceGetter executorServiceGetter;

  public static interface ExecutorServiceGetter {
    ExecutorService getExecutor();
  }

  public ActiveCache( ActiveCacheLoader<Key, Value> loader, long timeout ) {
    this( loader, timeout, new ExecutorServiceGetter() {

      @Override
      public ExecutorService getExecutor() {
        return ExecutorUtil.getExecutor();
      }
    } );
  }

  public ActiveCache( ActiveCacheLoader<Key, Value> loader, long timeout, ExecutorServiceGetter executorServiceGetter ) {
    this( loader, new HashMap<Key, ActiveCacheResult<Value>>(), new HashMap<Key, Future<ActiveCacheResult<Value>>>(),
        timeout, executorServiceGetter );
  }

  public ActiveCache( ActiveCacheLoader<Key, Value> loader, Map<Key, ActiveCacheResult<Value>> valueMap,
      Map<Key, Future<ActiveCacheResult<Value>>> loadingMap, long timeout, ExecutorServiceGetter executorServiceGetter ) {
    this.valueMap = valueMap;
    this.loadingMap = loadingMap;
    this.loader = loader;
    this.timeout = timeout;
    this.executorServiceGetter = executorServiceGetter;
  }

  public Value get( Key key ) throws Exception {
    ActiveCacheResult<Value> result = null;
    Future<ActiveCacheResult<Value>> futureResult = null;
    synchronized ( this ) {
      result = valueMap.get( key );
      boolean shouldReload = false;
      long time = System.currentTimeMillis();
      if ( result == null || result.getTimeLoaded() + timeout < time ) {
        // Expired, we need to wait on reload
        result = null;
        shouldReload = true;
      } else if ( result.getTimeLoaded() + ( timeout / 2.0 ) < time ) {
        // Preemptively reload
        shouldReload = true;
      }
      if ( shouldReload ) {
        futureResult = loadingMap.get( key );
        if ( futureResult == null ) {
          futureResult =
              executorServiceGetter.getExecutor().submit(
                  new ActiveCacheCallable<Key, Value>( this, valueMap, loadingMap, key, loader ) );
          loadingMap.put( key, futureResult );
        }
      }
    }
    if ( result == null ) {
      result = futureResult.get();
    }
    Exception exception = result.getException();
    if ( exception != null ) {
      throw exception;
    }
    return result.getValue();
  }
}
