package org.pentaho.di.plugins.repofvs.local.vfs;

import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.util.Collection;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalPurFileSystem extends AbstractFileSystem {

  private static final Logger log = LoggerFactory.getLogger( LocalPurFileSystem.class );

  private final IUnifiedRepository repo;
  private final IRepositoryContentConverterHandler contentHandler;

  protected LocalPurFileSystem( FileName rootName, FileSystemOptions fileSystemOptions, IUnifiedRepository repository, IRepositoryContentConverterHandler contentHandler ) {
    super( rootName, null, fileSystemOptions );
    this.repo = repository;
    this.contentHandler = contentHandler;
  }

  @Override
  protected void addCapabilities( Collection<Capability> caps ) {
    caps.addAll( LocalPurProvider.capabilities );
  }

  @Override
  protected LocalPurFileObject createFile( AbstractFileName name ) throws Exception {
    log.debug( "FS.createFile: {}", name );
    String path = name.getPath();
    RepositoryFile repoFile = repo.getFile( path );
    if ( repoFile == null ) {
      repoFile = new RepositoryFile.Builder( name.getBaseName() ).path( path ).build();
    }
    return new LocalPurFileObject( name, this, repoFile );
  }

  public IUnifiedRepository getRepository() {
    return repo;
  }

  public IRepositoryContentConverterHandler getContentHandler() {
    return contentHandler;
  }

}
