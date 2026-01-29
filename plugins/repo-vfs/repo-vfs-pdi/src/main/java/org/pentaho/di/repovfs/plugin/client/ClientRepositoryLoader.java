package org.pentaho.di.repovfs.plugin.client;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
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
public class ClientRepositoryLoader {

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

  public IUnifiedRepository getPur( Repository repository ) throws KettleException {
    var pur = repository.getUnderlyingRepository();
    if ( pur == null ) {
      throw new KettleException( "Invalid repository type" );
    }
    return pur;
  }

  public Repository getRepository( FileSystemOptions opts ) throws KettleException {
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
    var repository = connectToRepository(
      cfg.getRepoName().orElseThrow( () -> new KettleException( "No repository name" ) ),
      cfg.getUser().orElseThrow( () -> new KettleException( "No user name" ) ),
      cfg.getPass().orElseThrow( () -> new KettleException( "No password" ) ) );
    if ( repository == null ) {
      throw new KettleException( "No repository" );
    }
    return repository;
  }

  private Repository connectToRepository( String repositoryName, String user, String password )
    throws KettleException {
    var repositories = repositoriesLoader.get();
    var repoMeta = repositories.findRepository( repositoryName );
    if ( repoMeta == null ) {
      throw new KettleException( "Repository not found: " + repositoryName );
    }
    var repo = PluginRegistry.getInstance().loadClass( RepositoryPluginType.class, repoMeta, Repository.class );
    repo.init( repoMeta );
    repo.connect( user, password );
    return repo;
  }


}
