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


package org.pentaho.di.www.cache;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.FileUtils;

import org.apache.commons.lang3.NotImplementedException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.pentaho.di.core.Const;
import org.hibernate.Cache;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class CarteStatusCache implements Cache {

  public static final String CARTE_STATUS_CACHE = "CARTE_CACHE";
  private static SessionFactory SESSION_FACTORY;
  private Session session;

  /**
   * Switching the thread launched to be daemon otherwise it blocks the pentaho server shutdown
   */
  private final ScheduledExecutorService removeService = Executors.newSingleThreadScheduledExecutor(
    new ThreadFactory() {
      public Thread newThread( Runnable r ) {
        Thread t = Executors.defaultThreadFactory().newThread( r );
        t.setDaemon( true );
        t.setName( CarteStatusCache.class.getSimpleName() );
        return t;
      }
    } );

  private final Map<String, CachedItem> cachedMap = new ConcurrentHashMap<>();

  private static final CarteStatusCache instance = new CarteStatusCache();

  private int period = 0;

  private TimeUnit timeUnit = null;

  public static CarteStatusCache getInstance() {
    return instance;
  }

  private CarteStatusCache() {
    period = Integer.parseInt( Const.getEnvironmentVariable( "CARTE_CLEAR_PERIOD", "1" ) );
    timeUnit = TimeUnit.valueOf( Const.getEnvironmentVariable( "CARTE_CLEAR_TIMEUNIT", "DAYS" ) );

    removeService.scheduleAtFixedRate( this::clear, 1, 1, TimeUnit.DAYS );
  }

  public void clear() {
    cachedMap.forEach( ( k, v ) -> {
      if ( LocalDate.now().isAfter( v.getExceedTime() ) ) {
        remove( k );
      }
    } );
  }

  public void put( String logId, String cacheString, int from ) {
    String randomPref = UUID.randomUUID().toString();
    File file = null;
    try {
      file = File.createTempFile( randomPref, null );
      file.deleteOnExit();
      Files.write( file.toPath(), cacheString.getBytes( Const.XML_ENCODING ) );
      CachedItem item = new CachedItem( file, from );
      if ( ( item = cachedMap.put( logId, item ) ) != null ) {
        removeTask( item.getFile() );
      }
    } catch ( Exception e ) {
      cachedMap.remove( logId );
      if ( file != null ) {
        file.delete();
      }
    }
  }

  public void put( Object key, Object value ) throws Exception {
    cachedMap.put( (String) key, (CachedItem) value );
  }

  public byte[] get( String logId, int from ) {
    CachedItem item = null;
    try {
      item = cachedMap.get( logId );
      if ( item == null || item.getFrom() != from ) {
        return null;
      }

      synchronized( item.getFile() ) {
        return Files.readAllBytes( item.getFile().toPath() );
      }


    } catch ( Exception e ) {
      cachedMap.remove( logId );
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
    synchronized( file ) {
      if ( file.exists() ) {
        FileUtils.deleteQuietly( file );
      }
    }
  }

  @VisibleForTesting
  Map<String, CachedItem> getMap() {
    return cachedMap;
  }

  // We have to stub out the methods in the new hibernate cache to fool it into thinking it's
  // a normal cache object so it can creating the region. But thereafter hibernate never seems
  // to maintain it as all our code taps this object directly.
  @Override public SessionFactory getSessionFactory() {
    throwNotImplemented();
    return null;
  }

  @Override public boolean containsEntity( Class<?> aClass, Object o ) {
    return false;
  }

  @Override public boolean containsEntity( String s, Object o ) {
    return false;
  }

  @Override public void evictEntityData( Class<?> aClass, Object o ) {

  }

  @Override public void evictEntityData( String s, Object o ) {

  }

  @Override public void evictEntityData( Class entityClass ) {
    throwNotImplemented();
  }

  @Override public void evictEntityData( String entityName ) {
    throwNotImplemented();
  }

  @Override public void evictEntityData() {
    throwNotImplemented();
  }

  @Override public void evictNaturalIdData( Class entityClass ) {
    throwNotImplemented();
  }

  @Override public void evictNaturalIdData( String entityName ) {
    throwNotImplemented();
  }

  @Override public void evictNaturalIdData() {
    throwNotImplemented();
  }

  @Override public boolean containsCollection( String s, Object o ) {
    return false;
  }

  @Override public void evictCollectionData( String s, Object o ) {

  }

  @Override public void evictCollectionData( String role ) {
    throwNotImplemented();
  }

  @Override public void evictCollectionData() {
    throwNotImplemented();
  }

  @Override public boolean containsQuery( String regionName ) {
    throwNotImplemented();
    return false;
  }

  @Override public void evictDefaultQueryRegion() {
    throwNotImplemented();
  }

  @Override public void evictQueryRegion( String regionName ) {
    throwNotImplemented();
  }

  @Override public void evictQueryRegions() {
    throwNotImplemented();
  }

  @Override public void evictRegion( String regionName ) {
    throwNotImplemented();
  }


  private void throwNotImplemented(){
    throw new NotImplementedException( "Method not Implemented with upgrade to hibernate 5.4.24");
  }

  @Override
  public boolean contains( Class aClass, Object o ) {
    return false;
  }

  @Override
  public void evict( Class aClass, Object o ) {

  }

  @Override
  public void evict( Class aClass ) {

  }

  @Override
  public <T> T unwrap( Class<T> aClass ) {
    return null;
  }
}
