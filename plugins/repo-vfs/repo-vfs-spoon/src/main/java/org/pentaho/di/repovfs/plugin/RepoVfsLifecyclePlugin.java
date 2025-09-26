package org.pentaho.di.repovfs.plugin;

import org.pentaho.di.repovfs.plugin.vfs.RepoVfsPdiProvider;
import org.pentaho.di.repovfs.vfs.JCRSolutionFileProvider;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.VFSLookupFilter;
import org.pentaho.di.core.annotations.KettleLifecyclePlugin;
import org.pentaho.di.core.lifecycle.KettleLifecycleListener;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.plugins.repofvs.local.vfs.LocalPurProvider;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.FileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers {@link JCRSolutionFileProvider} and {@link LocalPurProvider} with VFS FileSystemManager,
 * and {@link RepoVfsPdiProvider} with PDI {@link ConnectionManager}.
 */
@KettleLifecyclePlugin( id = "RepoVfsPlugin", name = "Repo VFS plugin init" )
public class RepoVfsLifecyclePlugin implements KettleLifecycleListener {
  private static final Logger log = LoggerFactory.getLogger( RepoVfsLifecyclePlugin.class );

  private Supplier<ConnectionManager> connectionManager = ConnectionManager::getInstance;

  @Override
  public void onEnvironmentInit() throws LifecycleException {
    log.debug( "env init" );

    // Register as a file system type with VFS
    FileSystemManager fsm = KettleVFS.getInstance().getFileSystemManager();

    if ( fsm instanceof DefaultFileSystemManager ) {
      log.info( "Registering VFS providers in File System Manager" );
      VfsProviderRegistration registration = new VfsProviderRegistration( (DefaultFileSystemManager) fsm );
      registration.registerVfsProvider( JCRSolutionFileProvider.SCHEME, JCRSolutionFileProvider::new );
      registration.registerVfsProvider( LocalPurProvider.SCHEME, LocalPurProvider::new );
    } else {
      log.error( "Unexpected FileSystemManager {}, providers not registered in VFS", fsm.getClass() );
    }

    ConnectionManager mgr = connectionManager.get();
    if ( mgr != null ) {
      log.info( "Registering PVFS provider in Connection Manager" );
      RepoVfsPdiProvider provider = new RepoVfsPdiProvider();
      mgr.addConnectionProvider( RepoVfsPdiProvider.KEY, provider );
      registerVfsLookupFilter( RepoVfsPdiProvider.KEY, mgr, JCRSolutionFileProvider.SCHEME, LocalPurProvider.SCHEME );
    } else {
      log.error( "No Connection Manager" );
    }

  }

  /** Registers a filter that matches VFS paths or schemes to a PVFS provider key */
  private void registerVfsLookupFilter( String key, ConnectionManager mgr, String... schemes ) {

    VFSLookupFilter vfsLookupFilter = new VFSLookupFilter() {
      @Override
      public String filter( String input ) {
        for ( String scheme: schemes ) {
          if ( scheme.equals( input ) || input.startsWith( scheme + ":" ) ) {
            return key;
          }
        }
        // allow other filters to be used
        return null;
      }
    };
    mgr.addLookupFilter( vfsLookupFilter );
  }

  private static class VfsProviderRegistration {
    private final DefaultFileSystemManager dfsm;
    private final Set<String> providers;

    public VfsProviderRegistration( DefaultFileSystemManager dfsm ) {
      this.dfsm = dfsm;
      this.providers = Stream.of( dfsm.getSchemes() ).collect( Collectors.toSet() );
    }

    public boolean registerVfsProvider( String scheme, Supplier<FileProvider> provider ) {
      if ( !providers.contains( scheme ) ) {
        try {
          dfsm.addProvider( scheme, provider.get() );
          log.debug( "VFS provider [{}] registered.", scheme );
          return true;
        } catch ( FileSystemException e ) {
          log.error( "Unable to register VFS provider [{}] for Repository", scheme, e );
        }
      } else {
        log.debug( "Provider [{}] already present.", scheme );
      }
      return false;
    }
  }

  @Override
  public void onEnvironmentShutdown() {
  }


}
