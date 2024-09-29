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

import java.util.Collection;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.FileProvider;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.repo.IConnectedRepositoryInstance;
import org.pentaho.di.ui.spoon.Spoon;

/**
 * VFS for repository access.
 * 
 * @author Alexander Buloichik
 */
public class RepositoryVfsProvider implements FileProvider {
  public static final String SCHEME = "repo";

  private final IConnectedRepositoryInstance repositoryInstance;

  public RepositoryVfsProvider( IConnectedRepositoryInstance repositoryInstance ) {
    this.repositoryInstance = repositoryInstance;
    registerRepoVFS();
  }

  private void registerRepoVFS() {
    FileSystemManager fsm = KettleVFS.getInstance().getFileSystemManager();
    if ( fsm instanceof DefaultFileSystemManager ) {
      try {
        ( (DefaultFileSystemManager) fsm ).addProvider( SCHEME, this );
        final Spoon spoon = Spoon.getInstance();
        System.out.println(  );
      } catch ( Exception ex ) {
        throw new RuntimeException( "Error initialize repo:// VFS", ex );
      }
    }
  }

  protected Repository getRepo() {
    Repository repo = repositoryInstance.getConnectedRepositoryInstance();
    if ( repo == null ) {
      throw new RuntimeException( "Repository not connected" );
    }
    return repo;
  }

  @Override
  public FileObject findFile( FileObject baseFile, String uri, FileSystemOptions fileSystemOptions )
    throws FileSystemException {

    if ( !uri.startsWith( "repo://" ) ) {
      throw new FileSystemException( "WRONG_URL" );
    }
    String path = '/' + uri.substring( 7 );
    return new RepositoryVfsFileObject( this, path );
  }

  @Override
  public FileObject createFileSystem( String scheme, FileObject file, FileSystemOptions fileSystemOptions )
    throws FileSystemException {
    throw new NotImplementedException();
  }

  @Override
  public FileSystemConfigBuilder getConfigBuilder() {
    throw new NotImplementedException();
  }

  @Override
  public Collection<Capability> getCapabilities() {
    throw new NotImplementedException();
  }

  @Override
  public FileName parseUri( FileName root, String uri ) throws FileSystemException {
    throw new NotImplementedException();
  }
}
