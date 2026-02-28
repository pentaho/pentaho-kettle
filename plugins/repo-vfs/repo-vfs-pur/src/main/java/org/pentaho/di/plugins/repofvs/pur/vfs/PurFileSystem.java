package org.pentaho.di.plugins.repofvs.pur.vfs;

import org.pentaho.di.plugins.repofvs.pur.vfs.PurProvider.OutputConverter;
import org.pentaho.di.plugins.repofvs.pur.vfs.PurProvider.RepositoryAccess;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.util.Collection;
import java.util.Optional;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurFileSystem extends AbstractFileSystem {

  private static final Logger log = LoggerFactory.getLogger( PurFileSystem.class );

  private final IUnifiedRepository repo;
  private final IRepositoryContentConverterHandler contentHandler;
  private final Optional<OutputConverter> outputConverter;

  PurFileSystem( FileName rootName, FileSystemOptions fileSystemOptions, RepositoryAccess repoAccess ) throws FileSystemException {
    super( rootName, null, fileSystemOptions );
    this.repo = repoAccess.getPur();
    this.contentHandler = repoAccess.getContentHandler();
    this.outputConverter = repoAccess.getOutputConverter();
    if ( this.repo == null ) {
       throw new FileSystemException( "Unable to obtain a IUnifiedRepository instance" );
    }
    log.debug( "Unified repository VFS created ({})", new PurFileSystemConfig( fileSystemOptions ).isRemote() ?
      "remote client" : "local server" );
  }

  @Override
  protected void addCapabilities( Collection<Capability> caps ) {
    caps.addAll( PurProvider.capabilities );
  }

  @Override
  protected PurFileObject createFile( AbstractFileName name ) throws Exception {
    log.debug( "FS.createFile: {}", name );
    String path = name.getPath();
    RepositoryFile repoFile = repo.getFile( path );
    if ( repoFile == null ) {
      repoFile = new RepositoryFile.Builder( name.getBaseName() ).path( path ).build();
    }
    return new PurFileObject( name, this, repoFile );
  }

  protected IUnifiedRepository getRepository() {
    return repo;
  }

  protected IRepositoryContentConverterHandler getContentHandler() {
    return contentHandler;
  }

  protected Optional<OutputConverter> getOutputConverter() {
    return outputConverter;
  }

}
