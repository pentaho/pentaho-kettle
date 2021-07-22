/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.pentaho.di.base;

import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MetaFileCacheImpl implements IMetaFileCache {
  private LogChannelInterface logger;

  protected Map<String, MetaFileCacheEntry<? extends AbstractMeta>> cacheMap = new ConcurrentHashMap<>();

  MetaFileCacheImpl( LogChannelInterface logger ) {
    this.logger = logger;
  }

  @Override public JobMeta getCachedJobMeta( String key ) {
    return cacheMap.get( key ) == null ? null : ( (JobMeta) cacheMap.get( key ).getMeta() );
  }

  @Override public TransMeta getCachedTransMeta( String key ) {
    return cacheMap.get( key ) == null ? null : ( (TransMeta) cacheMap.get( key ).getMeta() );
  }

  @Override public void cacheMeta( String key, JobMeta meta ) {
    cacheMap.put( key, new MetaFileCacheEntry<>( key, (JobMeta) meta.realClone( false ) ) );
  }

  @Override public void cacheMeta( String key, TransMeta meta ) {
    cacheMap.put( key, new MetaFileCacheEntry<>( key, (TransMeta) meta.realClone( false ) ) );
  }

  @Override public void logCacheSummary( LogChannelInterface log ) {
    if ( log == null ) {
      log = logger;
    }
    int lines = 0;
    for ( MetaFileCacheEntry<? extends AbstractMeta> entry : cacheMap.values() ) {
      if ( lines == 0 ) {
        log.logDetailed( "MetaFileCache Loading Summary" );
      }
      lines++;
      log.logDetailed( entry.getKey() + " was loaded " + entry.timesUsed + " times from the cache." );
    }
  }

  public class MetaFileCacheEntry<T extends AbstractMeta> {
    T meta;
    Timestamp created = new Timestamp( System.currentTimeMillis() );
    Timestamp lastAccessed;
    int timesUsed;
    String key;

    MetaFileCacheEntry( String key, T meta ) {
      this.key = key;
      if ( meta instanceof JobMeta ) {
        this.meta = (T) ( (JobMeta) meta ).realClone( false );  //always clone the meta
      } else if ( meta instanceof TransMeta ) {
        this.meta = (T) ( (TransMeta) meta ).realClone( false );  //always clone the meta
      }
    }

    public T getMeta() {
      timesUsed++;
      if ( logger.isDetailed() ) {
        String fileType = meta instanceof JobMeta ? "Job" : "Transformation";
        logger.logDetailed( "Loading " + fileType + " from cache with key " + key + " - count " + timesUsed );
      }
      lastAccessed = new Timestamp( System.currentTimeMillis() );
      return meta;
    }

    public Timestamp getCreated() {
      return created;
    }

    public Timestamp getLastAccessed() {
      return lastAccessed;
    }

    public int getTimesUsed() {
      return timesUsed;
    }

    public String getKey() {
      return key;
    }
  }
}
