package org.pentaho.di.repovfs.plugin.spoon;

import org.pentaho.di.repovfs.plugin.vfs.RepoVfsDetails;
import org.pentaho.di.repovfs.plugin.vfs.RepoVfsPdiProvider;

import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPlugin;
import org.pentaho.di.ui.spoon.SpoonPluginInterface;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;

import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates and removes the repository VFS connection as the Spoon user connects/disconnects
 * to the repository.
 */
@SpoonPlugin( id = "RepoVfsPlugin", image = "" )
public class RepoVfsSpoonLifecycleListener implements SpoonPluginInterface, SpoonLifecycleListener {

  public static final String CONNECTION_NAME = "repository";

  private static final Logger log = LoggerFactory.getLogger( RepoVfsSpoonLifecycleListener.class );

  public RepoVfsSpoonLifecycleListener() {
    log.debug( "RepoVfsSpoonLifecycleListener ctor" );
  }

  @Override
  public void onEvent( SpoonLifeCycleEvent evt ) {
    log.debug( "spoon event: {}", evt );
    switch ( evt ) {
      case REPOSITORY_CHANGED:
        Spoon spoon = Spoon.getInstance();
        Repository repo = spoon.getRepository();
        if ( repo != null ) {
          getConnectionManager( repo.getBowl() ).ifPresent( connMgr -> createConnection( connMgr, repo, CONNECTION_NAME ) );
        }
        break;
      case REPOSITORY_CONNECTED:
        // REPOSITORY_CHANGED gets called instead on connect..
        break;
      case REPOSITORY_DISCONNECTED:
        getConnectionManager( DefaultBowl.getInstance() ).ifPresent( connMgr -> deleteConnection( connMgr,
          CONNECTION_NAME ) );
        break;
      default:
        break;
    }
  }

  private Optional<ConnectionManager> getConnectionManager( Bowl bowl ) {
    try {
      ConnectionManager connMgr = bowl.getManager( ConnectionManager.class );
      return Optional.ofNullable( connMgr );
    } catch ( KettleException e ) {
      log.error( "Unable to get ConnectionManager", e );
      return Optional.empty();
    }
  }

  private static void findProvider( ConnectionManager mgr, Consumer<RepoVfsPdiProvider> task ) {
    mgr.getProvidersByType( RepoVfsPdiProvider.class ).stream().findAny().ifPresent( provider -> {
      if ( provider instanceof RepoVfsPdiProvider ) {
        task.accept( ( (RepoVfsPdiProvider) provider ) );
      }
    } );
  }

  private void deleteConnection( ConnectionManager connMgr, String connectionName ) {
    findProvider( connMgr, provider -> {
      provider.clearActiveDetails();
      connMgr.reset();
      log.info( "Removed repository VFS connection" );
    } );
  }

  private static void createConnection( ConnectionManager connMgr, Repository repo, String connectionName ) {
    IUser userInfo = repo.getUserInfo();

    RepoVfsDetails details = new RepoVfsDetails();
    repo.getUri().ifPresent( uri -> {
      log.debug( "URI: {}", uri.toString() );
      details.setUrl( uri.toString() );
      details.setName( connectionName );
      details.setUser( userInfo.getLogin() );
      details.setPass( userInfo.getPassword() );
      findProvider( connMgr, provider -> {
        provider.setActiveDetails( details );
        connMgr.reset();
        log.info( "Created repository VFS connection" );
      } );
    } );
  }

  @Override
  public void applyToContainer( String category, XulDomContainer container ) throws XulException {
    // no op
  }

  @Override
  public SpoonLifecycleListener getLifecycleListener() {
    return this;
  }

  @Override
  public SpoonPerspective getPerspective() {
    return null;
  }

}
