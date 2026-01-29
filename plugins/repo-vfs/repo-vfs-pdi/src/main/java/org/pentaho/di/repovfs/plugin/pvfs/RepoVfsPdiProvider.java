package org.pentaho.di.repovfs.plugin.pvfs;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.repository.HasRepositoryInterface;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repovfs.plugin.RepoVfsLifecyclePlugin;
import org.pentaho.di.repovfs.plugin.config.Config;
import org.pentaho.di.repovfs.plugin.pvfs.RepoVfsDetails.RepoType;
import org.pentaho.di.repovfs.plugin.server.RepositoryTypeGuesser;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.BaseVFSConnectionProvider;
import org.pentaho.di.connections.vfs.DefaultVFSConnectionFileNameTransformer;
import org.pentaho.di.connections.vfs.VFSConnectionFileNameTransformer;
import org.pentaho.di.connections.vfs.VFSRoot;
import org.pentaho.di.connections.vfs.provider.ConnectionFileName;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.plugins.repofvs.pur.vfs.PurFileSystemConfig;
import org.pentaho.di.plugins.repofvs.pur.vfs.PurProvider;
import org.pentaho.di.plugins.repovfs.ws.cfg.JCRSolutionConfig;
import org.pentaho.di.plugins.repovfs.ws.repo.BasicAuthentication;
import org.pentaho.di.plugins.repovfs.ws.repo.RepositoryClient;
import org.pentaho.di.plugins.repovfs.ws.vfs.JCRSolutionFileProvider;
import org.pentaho.di.plugins.repovfs.ws.vfs.JCRSolutionFileSystem;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <pre>
 * The PDI VFS provider that translates RepoVfsDetails connections into pur-l/pur-r files.
 * It also has a custom VFSConnectionFileNameTransformer to handle pvfs↔︎pur URL conversion.
 * In the server, it will instead create pur-local connections that use the embedded instance.
 * eg.:
 * "pvfs://my-repo-conn/some/path" -> "pur-l:/some/path"
 * </pre>
 */
public class RepoVfsPdiProvider  extends BaseVFSConnectionProvider<RepoVfsDetails> {

  public static final String NAME = ConnectionManager.STRING_REPO_VFS_PROVIDER_NAME;
  public static final String KEY = "pentaho-repository";

  public final boolean usePurr;

  private static final Logger log = LoggerFactory.getLogger( RepoVfsPdiProvider.class );

  private final RepositoryTypeGuesser repoGuesser = new RepositoryTypeGuesser();

  public RepoVfsPdiProvider( Config config ) {
    this.usePurr = config.useRemotePur();
  }

  @Override
  public List<VFSRoot> getLocations( RepoVfsDetails details ) {
    // only relevant for bucketed connections
    return Collections.emptyList();
  }

  @Override
  public String getProtocol( RepoVfsDetails vfsConnectionDetails ) {
    // nothing seems to call this method, but sounds like it should return the appropriate vfs scheme
    if ( vfsConnectionDetails.isRemote() ) {
      return usePurForRemote() ? PurProvider.SCHEME_REMOTE : JCRSolutionFileProvider.SCHEME;
    } else {
      return PurProvider.SCHEME_LOCAL;
    }
  }

  @Override
  public String getName() {
    return NAME;
  }

  /**
   * Identifier for this provider.
   * Due to a flaw in LookupFilter implementation, your life will be easier if this is the same as the VFS scheme. */
  @Override
  public String getKey() {
    return KEY;
  }

  @Override
  public Class<RepoVfsDetails> getClassType() {
    return RepoVfsDetails.class;
  }

  @Override
  public boolean test( RepoVfsDetails details ) throws KettleException {
    // not actually used unless default connections become testable
    if ( details.isRemote() ) {
      if ( usePurForRemote() ) {
        return testPurrConnection( details );
      }
      return testRemoteWSConnection( details );
    } else {
      return repoGuesser.hasLocalRepository();
    }
  }

  private boolean testPurrConnection( RepoVfsDetails details ) {
    var purProvider = RepoVfsLifecyclePlugin.createClientRemoteProvider();
    var fsOpts = getOpts( details );
    return purProvider.test( fsOpts );
  }

  private static boolean testRemoteWSConnection( RepoVfsDetails details ) {
    if ( details.tryParseUrl().isEmpty() ) {
      return false;
    }

    JCRSolutionConfig config = new JCRSolutionConfig();

    String user = details.getUser();
    String password = details.getPass();

    String url = details.getUrl();
    RepositoryClient client = new RepositoryClient( config, url, new BasicAuthentication( user, password ) );
    return client.getRoot() != null;
  }

  @Override
  public FileSystemOptions getOpts( RepoVfsDetails connectionDetails ) {
    FileSystemOptions opts = super.getOpts( connectionDetails );

    if ( connectionDetails.isRemote() && !usePurForRemote() ) {
      JCRSolutionFileSystem.ConfigBuilder cfgBuilder = JCRSolutionFileSystem.createConfigBuilder();
      cfgBuilder.setUseLocalRepo( opts, !connectionDetails.isRemote() );
      if ( connectionDetails.isRemote() ) {
        cfgBuilder.setUrl( opts, connectionDetails.getUrl() );
        cfgBuilder.setUser( opts, connectionDetails.getUser() );
        cfgBuilder.setPassword( opts, connectionDetails.getPass() );
      }
      return opts;
    }

    var cfg = new PurFileSystemConfig( opts );
    cfg.setUser( connectionDetails.getUser() );
    if ( connectionDetails.isRemote() ) {
      cfg.setPassword( connectionDetails.getPass() );
      cfg.setRepoName( connectionDetails.getRepoName() );
    }

    // no need
    return opts;
  }

  @Override
  public VFSConnectionFileNameTransformer<RepoVfsDetails> getFileNameTransformer( ConnectionManager connectionManager ) {
    return new DefaultVFSConnectionFileNameTransformer<RepoVfsDetails>( connectionManager ) {

      @Override
      public FileName toProviderFileName( ConnectionFileName pvfsFileName, RepoVfsDetails details )
        throws KettleException {
          if ( details.isRemote() && !usePurForRemote() ) {
            return getUriForRemote( pvfsFileName, details );
          } else {
            return super.toProviderFileName( pvfsFileName, details );
          }
      }

      protected void appendProviderUriSchemePrefix( StringBuilder providerUriBuilder, RepoVfsDetails details )
        throws KettleException {
        if ( details.isRemote() ) {
          if ( usePurForRemote() ) {
            providerUriBuilder
              .append( PurProvider.SCHEME_REMOTE )
              .append( "://" );
          } else {
            appendProviderUriSchemePrefixForRemote( providerUriBuilder, details );
          }
        } else {
          // default uses Details.getType directly instead of mapping to provider protocol
          // so it needs to be overridden too
          providerUriBuilder
            .append( PurProvider.SCHEME_LOCAL )
            .append( "://" );
        }
      }

      private FileName getUriForRemote( ConnectionFileName pvfsFileName, RepoVfsDetails details ) throws KettleException {
        StringBuilder providerUriBuilder = new StringBuilder();
        // eg. jcr-solution:http://localhost:8080/pentaho!
        providerUriBuilder.append( JCRSolutionFileProvider.SCHEME );
        providerUriBuilder.append( ":" );
        providerUriBuilder.append( details.getUrl() );
        providerUriBuilder.append( "!" );
        // path component
        appendProviderUriRestPath( providerUriBuilder, pvfsFileName.getPath(), details );
        String uri = providerUriBuilder.toString();
        return parseUri( uri );
      }

      private void appendProviderUriSchemePrefixForRemote( StringBuilder providerUriBuilder, RepoVfsDetails details ) {
        String urlProtocol = details.tryParseUrl().map( URL::getProtocol ).orElse( "http" );
        providerUriBuilder
          .append( JCRSolutionFileProvider.SCHEME )
          .append( ":" )
          .append( urlProtocol )
          .append( "://" );
      }
    };
  }

  private boolean usePurForRemote() {
    return usePurr;
  }

  @Override
  public List<RepoVfsDetails> getConnectionDetails() {
    throw new UnsupportedOperationException( "Deprecated method " );
  }

  @Override
  public List<String> getNames() {
    throw new UnsupportedOperationException( "Deprecated method " );
  }

  @Override
  public List<RepoVfsDetails> getConnectionDetails( ConnectionManager connectionManager) {
    Bowl bowl = connectionManager.getBowl();
    if ( bowl instanceof HasRepositoryInterface ) {
      Repository diRepository = ( (HasRepositoryInterface) bowl ).getRepository();
      if ( diRepository != null ) {
        return getConnectionDetails( diRepository, ConnectionManager.STRING_REPO_CONNECTION );
      }
    }
    return Collections.emptyList();
  }

  private List<RepoVfsDetails> getConnectionDetails( Repository diRepo, String connectionName ) {
    if ( isPentahoRepository( diRepo ) ) {
      log.debug( "Valid repository present: {}", diRepo.getName() );
      RepoVfsDetails details = createRemoteConnectionDetails( diRepo, connectionName );
      if ( repoGuesser.canUseLocalRepository( diRepo ) ) {
        log.debug( "Using local repository instance" );
        details = createLocalConnectionDetails( connectionName );
      }
      return Collections.singletonList( details );
    }
    return Collections.emptyList();
  }

  @Override
  public List<String> getNames( ConnectionManager connectionManager ) {
    return getConnectionDetails( connectionManager ).stream()
            .map( RepoVfsDetails::getName )
            .collect( Collectors.toList() );
  }

  @Override
  public boolean isStorageManaged() {
    // let ConnectionManager know we roll our own getConnectionDetails
    return false;
  }

  private static RepoVfsDetails createLocalConnectionDetails( String connectionName ) {
    RepoVfsDetails details = new RepoVfsDetails();
    details.setRepoType( RepoType.LOCAL );
    details.setName( connectionName );
    return details;
  }

  private boolean isPentahoRepository( Repository repo ) {
    return repo.getUri().isPresent() && repo.getUserInfo() != null;
  }

  private RepoVfsDetails createRemoteConnectionDetails( Repository pentahoRepo, String connectionName ) {
    URI uri = pentahoRepo.getUri().get();
    IUser userInfo = pentahoRepo.getUserInfo();
    RepoVfsDetails details = new RepoVfsDetails();
    details.setRepoType( RepoType.REMOTE );
    if ( !usePurForRemote() ) {
      details.setUrl( uri.toString() );
    } else {
      details.setRepoName( pentahoRepo.getName() );
    }
    details.setName( connectionName );
    details.setUser( userInfo.getLogin() );
    details.setPass( userInfo.getPassword() );
    return details;
  }

}