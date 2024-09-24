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

package org.pentaho.googledrive.vfs.util;

import com.google.api.client.util.IOUtils;
import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.AbstractDataStore;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.FileOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

public class CustomDataStoreFactory extends AbstractDataStoreFactory {

  private final File dataDirectory;

  public CustomDataStoreFactory( File dataDirectory ) throws IOException {
    dataDirectory = dataDirectory.getCanonicalFile();
    this.dataDirectory = dataDirectory;
    if ( IOUtils.isSymbolicLink( dataDirectory ) ) {
      throw new IOException( "unable to use a symbolic link: " + dataDirectory );
    } else if ( !dataDirectory.exists() && !dataDirectory.mkdirs() ) {
      throw new IOException( "unable to create directory: " + dataDirectory );
    }
  }

  protected <V extends Serializable> DataStore<V> createDataStore( String id ) throws IOException {
    return new CustomDataStore( this, this.dataDirectory, id );
  }

  static class CustomDataStore<V extends Serializable> extends AbstractDataStore<V> {
    private File dataFile = null;
    private File dataDirectory = null;
    private HashMap<String, byte[]> keyValueMap = Maps.newHashMap();
    private final Lock lock = new ReentrantLock();

    CustomDataStore( CustomDataStoreFactory dataStore, File dataDirectory, String id ) throws IOException {
      super( dataStore, id );
      this.dataDirectory = dataDirectory;
      this.dataFile = new File( this.dataDirectory, getId() );

      if ( IOUtils.isSymbolicLink( this.dataFile ) ) {
        throw new IOException( "unable to use a symbolic link: " + this.dataFile );
      }

      this.keyValueMap = Maps.newHashMap();

      if ( this.dataFile.exists() ) {
        this.keyValueMap = (HashMap) IOUtils.deserialize( new FileInputStream( this.dataFile ) );
      }
    }

    void save() throws IOException {
      this.dataFile.createNewFile();
      IOUtils.serialize( this.keyValueMap, new FileOutputStream( this.dataFile ) );
    }

    public final Set<String> keySet() throws IOException {
      this.lock.lock();

      Set var1;
      try {
        var1 = Collections.unmodifiableSet( this.keyValueMap.keySet() );
      } finally {
        this.lock.unlock();
      }

      return var1;
    }

    public FileDataStoreFactory getDataStoreFactory() {
      return (FileDataStoreFactory) super.getDataStoreFactory();
    }

    public final DataStore<V> clear() throws IOException {
      this.lock.lock();

      try {
        this.keyValueMap.clear();
        this.save();
      } finally {
        this.lock.unlock();
      }

      return this;
    }

    public final Collection<V> values() throws IOException {
      this.lock.lock();

      try {
        List<V> result = Lists.newArrayList();
        Iterator i$ = this.keyValueMap.values().iterator();

        while ( i$.hasNext() ) {
          byte[] bytes = (byte[]) i$.next();
          result.add( IOUtils.deserialize( bytes ) );
        }

        List var7 = Collections.unmodifiableList( result );
        return var7;
      } finally {
        this.lock.unlock();
      }
    }

    public final V get( String key ) throws IOException {
      if ( key == null ) {
        return null;
      } else {
        this.lock.lock();

        V var2;
        try {
          var2 = IOUtils.deserialize( (byte[]) this.keyValueMap.get( key ) );
        } finally {
          this.lock.unlock();
        }

        return var2;
      }
    }

    public final DataStore<V> set( String key, V value ) throws IOException {
      Preconditions.checkNotNull( key );
      Preconditions.checkNotNull( value );
      this.lock.lock();

      try {
        this.keyValueMap.put( key, IOUtils.serialize( value ) );
        this.save();
      } finally {
        this.lock.unlock();
      }

      return this;
    }

    public DataStore<V> delete( String key ) throws IOException {
      if ( key == null ) {
        return this;
      } else {
        this.lock.lock();

        try {
          this.keyValueMap.remove( key );
          this.save();
        } finally {
          this.lock.unlock();
        }

        return this;
      }
    }
  }
}
