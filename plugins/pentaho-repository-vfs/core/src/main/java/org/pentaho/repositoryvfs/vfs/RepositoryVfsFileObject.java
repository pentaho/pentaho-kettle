/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.repositoryvfs.vfs;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.operations.FileOperations;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryObjectInterface;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

public class RepositoryVfsFileObject implements FileObject {
  protected final RepositoryVfsProvider provider;
  protected final String path;
  protected FileType type = FileType.FILE_OR_FOLDER;
  private RepositoryFile repositoryFile;

  public RepositoryVfsFileObject( RepositoryVfsProvider provider, String path ) {
    this.provider = provider;
    this.path = path.replaceAll( "/{2,}", "/" );
    repositoryFile = getRepositoryFile();
  }

  protected synchronized RepositoryFile getRepositoryFile() {
    if ( repositoryFile == null ) {
      try {
        repositoryFile = provider.getRepo().getUnderlyingRepository().getFile( path );
        if ( repositoryFile == null ) {
          return null;
        }
        type = repositoryFile.isFolder() ? FileType.FOLDER : FileType.FILE;
      } catch ( Exception ex ) {
        throw new RuntimeException( ex );
      }
    }
    return repositoryFile;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( obj instanceof FileObject ) {
      return compareTo( (FileObject) obj ) == 0;
    } else {
      return false;
    }
  }

  @Override
  public int compareTo( FileObject o ) {
    return getPublicURIString().compareTo( o.getPublicURIString() );
  }

  @Override
  public Iterator<FileObject> iterator() {
    throw new NotImplementedException();
  }

  @Override
  public boolean canRenameTo( FileObject newfile ) {
    throw new NotImplementedException();
  }

  @Override
  public void close() throws FileSystemException {
  }

  @Override
  public void copyFrom( FileObject srcFile, FileSelector selector ) throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public void createFile() throws FileSystemException {
    type = FileType.FILE;
  }

  @Override
  public void createFolder() throws FileSystemException {
    try {
      provider.getRepo().createRepositoryDirectory( provider.getRepo().loadRepositoryDirectoryTree(), path );
      type = FileType.FOLDER;
    } catch ( KettleException ex ) {
      throw new FileSystemException( ex );
    }
  }

  @Override
  public boolean delete() throws FileSystemException {
    if ( getRepositoryFile() != null ) {
      provider.getRepo().getUnderlyingRepository().deleteFile( repositoryFile.getId(), null );
    }
    return true;
  }

  @Override
  public int delete( FileSelector selector ) throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public int deleteAll() throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public boolean exists() throws FileSystemException {
    return getRepositoryFile() != null;
  }

  @Override
  public FileObject[] findFiles( FileSelector selector ) throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public void findFiles( FileSelector selector, boolean depthwise, List<FileObject> selected )
    throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public FileObject getChild( String name ) throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public FileObject[] getChildren() throws FileSystemException {
    try {
      RepositoryDirectoryInterface dir = provider.getRepo().findDirectory( path );
      if ( dir == null ) {
        return null;
      }
      List<RepositoryObjectInterface> ch = new ArrayList<>();
      ch.addAll( dir.getChildren() );
      ch.addAll( dir.getRepositoryObjects() );

      FileObject[] result = new RepositoryVfsFileObject[ch.size()];
      for ( int i = 0; i < ch.size(); i++ ) {
        result[i] = new RepositoryVfsFileObject( provider, path + '/' + ch.get( i ).getName() );
      }
      return result;
    } catch ( Exception ex ) {
      throw new FileSystemException( ex );
    }
  }

  @Override
  public FileContent getContent() throws FileSystemException {
    return new RepositoryVfsFileContent( this );
  }

  @Override
  public FileOperations getFileOperations() throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public FileSystem getFileSystem() {
    throw new NotImplementedException();
  }

  @Override
  public FileName getName() {
    return new RepositoryVfsFileName( path.toString(), type );
  }

  @Override
  public RepositoryVfsFileObject getParent() throws FileSystemException {
    if ( "/".equals( path ) ) {
      return null;
    }
    int p = path.lastIndexOf( '/' );
    if ( p == 0 ) {
      p = 1; // for root
    }
    String parentPath = path.substring( 0, p );
    return new RepositoryVfsFileObject( provider, parentPath );
  }

  @Override
  public String getPublicURIString() {
    return "repo:/" + path;
  }

  @Override
  public FileType getType() throws FileSystemException {
    return type;
  }

  @Override
  public URL getURL() throws FileSystemException {
    try {
      return new URL( getPublicURIString() );
    } catch ( Exception ex ) {
      throw new FileSystemException( ex );
    }
  }

  @Override
  public boolean isAttached() {
    throw new NotImplementedException();
  }

  @Override
  public boolean isContentOpen() {
    throw new NotImplementedException();
  }

  @Override
  public boolean isExecutable() throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public boolean isFile() throws FileSystemException {
    return getType() == FileType.FILE;
  }

  @Override
  public boolean isFolder() throws FileSystemException {
    return getType() == FileType.FOLDER;
  }

  @Override
  public boolean isHidden() throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public boolean isReadable() throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public boolean isWriteable() throws FileSystemException {
    RepositoryFile file = getRepositoryFile();
    return file == null;
  }

  @Override
  public void moveTo( FileObject destFile ) throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public void refresh() throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public FileObject resolveFile( String path ) throws FileSystemException {
    return new RepositoryVfsFileObject( provider, this.path + '/' + path );
  }

  @Override
  public FileObject resolveFile( String name, NameScope scope ) throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public boolean setExecutable( boolean executable, boolean ownerOnly ) throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public boolean setReadable( boolean readable, boolean ownerOnly ) throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public boolean setWritable( boolean writable, boolean ownerOnly ) throws FileSystemException {
    throw new NotImplementedException();
  }
}
