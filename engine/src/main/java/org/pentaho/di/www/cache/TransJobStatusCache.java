/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.www.cache;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.FileUtils;
import org.pentaho.di.core.Const;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class TransJobStatusCache {

  private final ScheduledExecutorService removeService = Executors.newSingleThreadScheduledExecutor();

  private final Map<String, CachedItem> cachedMap = new ConcurrentHashMap<>();

  private static TransJobStatusCache instance = null;

  public static synchronized TransJobStatusCache getInstance() {
    if ( instance == null ) {
      instance = new TransJobStatusCache();
    }
    return instance;
  }

  public TransJobStatusCache() {
    removeService.scheduleAtFixedRate( () -> cachedMap.forEach( ( k, v ) -> {
      if ( LocalDate.now().isAfter( v.getExceedTime() ) ) {
        remove( k );
      }
    } ), 1, 1, TimeUnit.DAYS );
  }


  public void put( String id, String cacheString, String logId ) {
    String randomPref = UUID.randomUUID().toString();
    File file = null;
    try {
      file = File.createTempFile( randomPref, null );
      file.deleteOnExit();
      Files.write( file.toPath(), cacheString.getBytes( Const.XML_ENCODING ) );
      CachedItem item = new CachedItem( logId, file );
      if ( ( item = cachedMap.put( id, item ) ) != null ) {
        removeTask( item.getFile() );
      }
    } catch ( Exception e ) {
      cachedMap.remove( id );
      if ( file != null ) {
        file.delete();
      }
    }
  }


  public byte[] get( String id, String logId ) {
    CachedItem item = null;
    try {
      item = cachedMap.get( id );
      if ( item == null || logId == null ) {
        return null;
      }

      if ( logId.equals( item.getLogId() ) ) {
        synchronized ( item.getFile() ) {
          return Files.readAllBytes( item.getFile().toPath() );
        }
      }

    } catch ( Exception e ) {
      cachedMap.remove( id );
      if ( item != null ) {
        removeTask( item.getFile() );
      }
    }
    return null;
  }

  public void remove( String id ) {
    CachedItem item = cachedMap.remove( id );
    if ( item != null ) {
      removeTask( item.getFile() );
    }
  }

  private void removeTask( File file ) {
    removeService.execute( () -> removeFile( file ) );
  }

  private void removeFile( File file ) {
    synchronized ( file ) {
      if ( file.exists() ) {
        FileUtils.deleteQuietly( file );
      }
    }
  }

  @VisibleForTesting
  Map<String, CachedItem> getMap() {
    return cachedMap;
  }
}
