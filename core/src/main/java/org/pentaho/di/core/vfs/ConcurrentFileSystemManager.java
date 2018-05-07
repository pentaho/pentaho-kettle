/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.vfs;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.FileProvider;
import org.pentaho.di.core.osgi.api.VfsEmbeddedFileSystemCloser;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class is aimed to be a thread-safe version of
 * {@link org.apache.commons.vfs2.impl.StandardFileSystemManager StandardFileSystemManager}.
 * It locks methods that accessing or mutating the providers Map in
 * {@link org.apache.commons.vfs2.impl.DefaultFileSystemManager DefaultFileSystemManager}.
 */
public class ConcurrentFileSystemManager extends StandardFileSystemManager {

  private static final String CONFIG_RESOURCE = "providers.xml";

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

  public ConcurrentFileSystemManager() {
    setConfiguration( this.getClass().getResource( CONFIG_RESOURCE ) );
  }

  @Override
  public void addProvider( String[] urlSchemes, FileProvider provider ) throws FileSystemException {
    lock.writeLock().lock();
    try {
      super.addProvider( urlSchemes, provider );
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public String[] getSchemes() {
    lock.readLock().lock();
    try {
      return super.getSchemes();
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public boolean hasProvider( String scheme ) {
    lock.readLock().lock();
    try {
      return super.hasProvider( scheme );
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public FileObject resolveFile( FileObject baseFile, String uri, FileSystemOptions fileSystemOptions ) throws FileSystemException {
    lock.readLock().lock();
    try {
      return super.resolveFile( baseFile, uri, fileSystemOptions );
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public FileName resolveName( FileName base, String name, NameScope scope ) throws FileSystemException {
    lock.readLock().lock();
    try {
      return super.resolveName( base, name, scope );
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public FileName resolveURI( String uri ) throws FileSystemException {
    lock.readLock().lock();
    try {
      return super.resolveURI( uri );
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public FileObject createFileSystem( String scheme, FileObject file ) throws FileSystemException {
    lock.readLock().lock();
    try {
      return super.createFileSystem( scheme, file );
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public void _closeFileSystem( FileSystem filesystem ) {
    lock.readLock().lock();
    try {
      super._closeFileSystem( filesystem );
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public Collection<Capability> getProviderCapabilities( String scheme ) throws FileSystemException {
    lock.readLock().lock();
    try {
      return super.getProviderCapabilities( scheme );
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public FileSystemConfigBuilder getFileSystemConfigBuilder( String scheme ) throws FileSystemException {
    lock.readLock().lock();
    try {
      return super.getFileSystemConfigBuilder( scheme );
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public void close() {
    lock.writeLock().lock();
    try {
      super.close();
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public void freeUnusedResources() {
    lock.readLock().lock();
    try {
      super.freeUnusedResources();
    } finally {
      lock.readLock().unlock();
    }
  }

  public void closeEmbeddedFileSystem( String embeddedMetastoreKey ) {
    lock.readLock().lock();
    Map<String, FileProvider> providers;
    try {
      // Close the file system
      java.lang.reflect.Field field = null;
      try {
        field = this.getClass().getSuperclass().getSuperclass().getDeclaredField( "providers" );
        field.setAccessible( true );
      } catch ( NoSuchFieldException e ) {
        e.printStackTrace();
      }
      providers = (Map<String, FileProvider>) field.get( this );
      FileProvider provider = providers.get( "hc" );
      if ( provider != null ) {
        ( (VfsEmbeddedFileSystemCloser) provider ).closeFileSystem( embeddedMetastoreKey );
      }
    } catch ( IllegalAccessException e ) {
      e.printStackTrace();
    } finally {
      lock.readLock().unlock();
    }
  }
}
