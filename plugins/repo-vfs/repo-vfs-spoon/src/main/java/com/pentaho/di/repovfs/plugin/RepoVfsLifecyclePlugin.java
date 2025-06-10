package org.pentaho.di.repovfs.plugin;

import org.pentaho.di.repovfs.plugin.vfs.RepoVfsPdiProvider;
import org.pentaho.di.repovfs.vfs.JCRSolutionFileProvider;

import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.VFSLookupFilter;
import org.pentaho.di.core.annotations.KettleLifecyclePlugin;
import org.pentaho.di.core.lifecycle.KettleLifecycleListener;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.vfs.KettleVFS;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers {@link JCRSolutionFileProvider} with VFS FileSystemManager,
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

    log.debug( "fsm:" + fsm.getClass() );

    if ( fsm instanceof DefaultFileSystemManager ) {
      log.info( "Registering providers in File System Manager (VFS)..." );
      DefaultFileSystemManager dfsm = (DefaultFileSystemManager) fsm;
      Set<String> providers = Stream.of( fsm.getSchemes() ).collect( Collectors.toSet() );
      if ( !providers.contains( JCRSolutionFileProvider.SCHEME ) ) {
        try {
          dfsm.addProvider( JCRSolutionFileProvider.SCHEME, new JCRSolutionFileProvider() );
        } catch ( FileSystemException e ) {
          log.error( "Unable to create VFS provider for Repository", e );
        }
        log.debug( "Provider registered." );
      } else {
        log.debug( "Provider already present." );
      }
    } else {
      log.error( "Unexpected FileSystemManager {}, provider not registered in VFS", fsm.getClass() );
    }

    ConnectionManager mgr = connectionManager.get();
    if ( mgr != null ) {
      log.info( "registering providers in Connection Manager (PDI)" );
      RepoVfsPdiProvider provider = new RepoVfsPdiProvider();
      mgr.addConnectionProvider( JCRSolutionFileProvider.SCHEME, provider );
      VFSLookupFilter vfsLookupFilter = new VFSLookupFilter() {
        private final String startMatch = JCRSolutionFileProvider.SCHEME + ":";

        @Override
        public String filter( String input ) {
          if ( input.startsWith( startMatch ) ) {
            return JCRSolutionFileProvider.SCHEME;
          }
          // allow other filters to be used
          return null;
        }
      };
      mgr.addLookupFilter( vfsLookupFilter );
    }

  }

  @Override
  public void onEnvironmentShutdown() {
  }

}
