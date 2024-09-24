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
