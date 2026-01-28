package org.pentaho.di.repovfs.plugin.client;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.plugins.repofvs.pur.RepositoryLoader;
import org.pentaho.di.plugins.repofvs.pur.converter.LazyLoader;
import org.pentaho.di.plugins.repofvs.pur.vfs.PurFileSystemConfig;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryBowl;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;

import org.apache.commons.vfs2.FileSystemOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads the repository when not on Pentaho Server.
 * Handles Spoon, Pan, Kitchen, and standalone Carte.
 * */
public class ClientRepositoryLoader implements RepositoryLoader {

  private static Logger log = LoggerFactory.getLogger( ClientRepositoryLoader.class );

  private final LazyLoader<RepositoriesMeta> repositoriesLoader = new LazyLoader<>( () -> {
    var repositories = new RepositoriesMeta();
    try {
      repositories.readData();
    } catch ( KettleException e ) {
      log.error( "Unable to load repositories.xml", e );
    }
    return repositories;
  } );

  @Override
  public IUnifiedRepository loadRepository( FileSystemOptions opts ) throws RepositoryLoadException {
    Repository repository = getRepository( opts );
    if ( repository == null ) {
      throw new RepositoryLoadException( "No repository" );
    }
    var pur = repository.getUnderlyingRepository();
    if ( pur == null ) {
      throw new RepositoryLoadException( "Invalid repository type" );
    }
    return pur;
  }

  private Repository getRepository( FileSystemOptions opts ) throws RepositoryLoadException {
    var cfg = new PurFileSystemConfig( opts );

    var bowl = cfg.getBowl();
    if ( bowl.isPresent() && bowl.get() instanceof RepositoryBowl repoBowl ) {
      log.debug( "Using repository from bowl" );
      return repoBowl.getRepository();
    }

    Spoon spoon = Spoon.getInstance();
    if ( spoon != null ) {
      log.debug( "Using spoon repository" );
      return spoon.getRepository();
    }
    // there is no spoon
    log.debug( "Connecting to repository" );
    return connectToRepository(
      cfg.getRepoName().orElseThrow( () -> new RepositoryLoadException( "No repository name" ) ),
      cfg.getUser().orElseThrow( () -> new RepositoryLoadException( "No user name" ) ),
      cfg.getPass().orElseThrow( () -> new RepositoryLoadException( "No password" ) ) );
  }

  private Repository connectToRepository( String repositoryName, String user, String password )
    throws RepositoryLoadException {
    var repositories = repositoriesLoader.get();
    var repoMeta = repositories.findRepository( repositoryName );
    if ( repoMeta == null ) {
      throw new RepositoryLoadException( "Repository not found: " + repositoryName );
    }
    try {
      Repository repo = PluginRegistry.getInstance().loadClass( RepositoryPluginType.class, repoMeta,
        Repository.class );
      repo.init( repoMeta );
      repo.connect( user, password );
      return repo;
    } catch ( KettlePluginException e ) {
      throw new RepositoryLoadException( "Unable to load repository type", e );
    } catch ( KettleException e ) {
      throw new RepositoryLoadException( "Unable to connect to repository", e );
    }
  }


}
