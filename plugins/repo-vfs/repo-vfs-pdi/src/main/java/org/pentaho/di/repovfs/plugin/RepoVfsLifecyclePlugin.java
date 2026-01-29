package org.pentaho.di.repovfs.plugin;

import org.pentaho.di.repovfs.plugin.client.ClientRepositoryLoader;
import org.pentaho.di.repovfs.plugin.config.Config;
import org.pentaho.di.repovfs.plugin.pvfs.RepoVfsPdiProvider;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.annotations.KettleLifecyclePlugin;
import org.pentaho.di.core.lifecycle.KettleLifecycleListener;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.plugins.KettleLifecyclePluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.plugins.repofvs.pur.converter.ContentConverterHandler;
import org.pentaho.di.plugins.repofvs.pur.vfs.PurProvider;
import org.pentaho.di.plugins.repofvs.pur.vfs.PurProvider.RepositoryAccess;
import org.pentaho.di.plugins.repovfs.ws.vfs.JCRSolutionFileProvider;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.FileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers {@link JCRSolutionFileProvider} and {@link PurProvider} with VFS FileSystemManager,
 * and {@link RepoVfsPdiProvider} with PDI {@link ConnectionManager}.
 */
@KettleLifecyclePlugin( id = RepoVfsLifecyclePlugin.ID, name = "Repo VFS plugin init" )
public class RepoVfsLifecyclePlugin implements KettleLifecycleListener {

  public static final String ID = "RepoVfsPlugin";

  private static final Logger log = LoggerFactory.getLogger( RepoVfsLifecyclePlugin.class );

  private static final String CONFIG_FILE = "config.properties";

  private Supplier<ConnectionManager> connectionManager = ConnectionManager::getInstance;

  @Override
  public void onEnvironmentInit() throws LifecycleException {
    log.debug( "env init" );

    var config = readConfig();

    // Register as a file system type with VFS
    FileSystemManager fsm = KettleVFS.getInstance().getFileSystemManager();

    if ( fsm instanceof DefaultFileSystemManager dfsm ) {
      log.info( "Registering VFS providers in File System Manager" );
      VfsProviderRegistration registration = new VfsProviderRegistration( dfsm );
      if ( config.useRemotePur() ) {
        registration.registerVfsProvider( PurProvider.SCHEME_REMOTE, RepoVfsLifecyclePlugin::createClientRemoteProvider );
      } else {
        registration.registerVfsProvider( JCRSolutionFileProvider.SCHEME, JCRSolutionFileProvider::new );
      }
      registration.registerVfsProvider( PurProvider.SCHEME_LOCAL, RepoVfsLifecyclePlugin::createServerLocalProvider );
    } else {
      log.error( "Unexpected FileSystemManager {}, providers not registered in VFS", fsm.getClass() );
    }

    ConnectionManager mgr = connectionManager.get();
    if ( mgr != null ) {
      log.info( "Registering PVFS provider in Connection Manager" );
      RepoVfsPdiProvider provider = new RepoVfsPdiProvider( config );
      mgr.addConnectionProvider( RepoVfsPdiProvider.KEY, provider );
      if ( config.useRemotePur() ) {
        registerVfsLookupFilter( RepoVfsPdiProvider.KEY, mgr,
          PurProvider.SCHEME_LOCAL,
          PurProvider.SCHEME_REMOTE );
      } else {
        registerVfsLookupFilter( RepoVfsPdiProvider.KEY, mgr,
          JCRSolutionFileProvider.SCHEME,
          PurProvider.SCHEME_LOCAL );
      }
    } else {
      log.error( "No Connection Manager" );
    }
  }

  /**
   * Creates a provider for use within a Pentaho Server, using the single repository and
   * content converter handler provided by the platform
   *
   * @return VFS provider
   */
  private static PurProvider createServerLocalProvider() {
    return new PurProvider( opts -> new RepositoryAccess() {

      @Override
      public IUnifiedRepository getPur() {
        return PentahoSystem.get( IUnifiedRepository.class );
      }

      @Override
      public IRepositoryContentConverterHandler getContentHandler() {
        return PentahoSystem.get( IRepositoryContentConverterHandler.class );
      }

    } );
  }

  /**
   * Creates a provider able to be used by client applications (ie anything outside Pentaho Server).
   * It relies on the `pur` plugin to get the appropriate content converters for kettle files.
   *
   * @return VFS provider
   * @see ContentConverterHandler
   */
  public static PurProvider createClientRemoteProvider() {
    var repoLoader = new ClientRepositoryLoader();
    return new PurProvider( opts -> {

      var repo = repoLoader.getRepository( opts );
      var pur = repoLoader.getPur( repo );

      return new RepositoryAccess() {

        @Override
        public IUnifiedRepository getPur() {
          return pur;
        }

        @Override
        public IRepositoryContentConverterHandler getContentHandler() {
          return new ContentConverterHandler( repo );
        }

      };

    } );

  }

  private Path getPluginFolder() throws URISyntaxException {
    var plugin = PluginRegistry.getInstance().getPlugin( KettleLifecyclePluginType.class, ID );
    return Path.of( plugin.getPluginDirectory().toURI() );
  }

  private Config readConfig() {
    try {
      var pluginPath = getPluginFolder();
      var configPath = pluginPath.resolve( CONFIG_FILE );
      if ( Files.exists( configPath ) ) {
        var props = new Configurations().properties( configPath.toFile() );
        return new Config( props );
      } else {
        log.error( "Configuration file {} not found", configPath );
      }
    } catch ( ConfigurationException e ) {
      log.error( "Uanble to read configuration file", e );
    } catch ( Exception e ) {
      log.error( "Unable to get plugin path", e );
    }
    return new Config();
  }

  /** Registers a filter that matches VFS paths or schemes to a PVFS provider key */
  private void registerVfsLookupFilter( String key, ConnectionManager mgr, String... schemes ) {
    mgr.addLookupFilter( input -> {
      for ( String scheme : schemes ) {
        if ( scheme.equals( input ) || input.startsWith( scheme + ":" ) ) {
          return key;
        }
      }
      // allow other filters to be used
      return null;
    } );
  }

  private static class VfsProviderRegistration {
    private final DefaultFileSystemManager dfsm;
    private final Set<String> providerSchemes;

    public VfsProviderRegistration( DefaultFileSystemManager dfsm ) {
      this.dfsm = dfsm;
      this.providerSchemes = Stream.of( dfsm.getSchemes() ).collect( Collectors.toSet() );
    }

    public boolean registerVfsProvider( String scheme, Supplier<FileProvider> provider ) {
      if ( !providerSchemes.contains( scheme ) ) {
        try {
          dfsm.addProvider( scheme, provider.get() );
          providerSchemes.add( scheme );
          log.debug( "VFS provider [{}] registered.", scheme );
          return true;
        } catch ( FileSystemException e ) {
          log.error( "Unable to register VFS provider [{}] for Repository", scheme, e );
        }
      } else {
        log.warn( "Provider [{}] already present.", scheme );
      }
      return false;
    }
  }

  @Override
  public void onEnvironmentShutdown() {
    // sonar wants a comment here
  }

}
