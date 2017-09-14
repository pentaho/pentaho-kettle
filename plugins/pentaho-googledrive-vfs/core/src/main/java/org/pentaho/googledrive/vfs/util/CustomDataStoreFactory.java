/*!
* Copyright 2010 - 2017 Pentaho Corporation.  All rights reserved.
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
*/

package org.pentaho.googledrive.vfs.util;

import com.google.api.client.util.IOUtils;
import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.AbstractDataStore;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CustomDataStoreFactory extends FileDataStoreFactory {

  public CustomDataStoreFactory( File dataDirectory ) throws IOException {
    super( dataDirectory );
  }

  protected <V extends Serializable> DataStore<V> createDataStore( String id ) throws IOException {
    return new CustomDataStore( this, getDataDirectory(), id );
  }

  static class CustomDataStore<V extends Serializable> extends AbstractDataStore<V> {
    private File dataFile = null;
    private File dataDirectory = null;
    private HashMap<String, byte[]> keyValueMap = Maps.newHashMap();
    private final Lock lock = new ReentrantLock();

    CustomDataStore( FileDataStoreFactory dataStore, File dataDirectory, String id ) throws IOException {
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
