package org.pentaho.di.repovfs.plugin.vfs;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.repository.HasRepositoryInterface;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repovfs.cfg.JCRSolutionConfig;
import org.pentaho.di.repovfs.plugin.server.RepositoryTypeGuesser;
import org.pentaho.di.repovfs.plugin.vfs.RepoVfsDetails.RepoType;
import org.pentaho.di.repovfs.repo.BasicAuthentication;
import org.pentaho.di.repovfs.repo.RepositoryClient;
import org.pentaho.di.repovfs.vfs.JCRSolutionFileProvider;
import org.pentaho.di.repovfs.vfs.JCRSolutionFileSystem;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.BaseVFSConnectionProvider;
import org.pentaho.di.connections.vfs.DefaultVFSConnectionFileNameTransformer;
import org.pentaho.di.connections.vfs.VFSConnectionFileNameTransformer;
import org.pentaho.di.connections.vfs.VFSRoot;
import org.pentaho.di.connections.vfs.provider.ConnectionFileName;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.plugins.repofvs.local.vfs.LocalPurProvider;

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
 * The PDI VFS provider that translates RepoVfsDetails connections into jcr-solution files.
 * It also has the custom VFSConnectionFileNameTransformer to handle pvfs↔︎jcr-solution URL conversion.
 * In the server, it will instead create pur-local connections that use the embedded instance.
 * eg.:
 * "pvfs://my-repo-conn/some/path" -> "jcr-solution:http://localhost:8080/pentaho!/some/path"
 * </pre>
 */
public class RepoVfsPdiProvider  extends BaseVFSConnectionProvider<RepoVfsDetails> {

  public static final String NAME = ConnectionManager.STRING_REPO_VFS_PROVIDER_NAME;
  public static final String KEY = "pentaho-repository";

  private static final Logger log = LoggerFactory.getLogger( RepoVfsPdiProvider.class );

  private final RepositoryTypeGuesser repoGuesser = new RepositoryTypeGuesser();

  @Override
  public List<VFSRoot> getLocations( RepoVfsDetails details ) {
    return Collections.emptyList();
  }

  @Override
  public String getProtocol( RepoVfsDetails vfsConnectionDetails ) {
    if ( vfsConnectionDetails.isRemote() ) {
      return JCRSolutionFileProvider.SCHEME;
    } else {
      return LocalPurProvider.SCHEME;
    }
  }

  @Override
  public String getName() {
    return NAME;
  }

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
    if ( details.isRemote() ) {
      return testRemoteConnection( details );
    } else {
      return repoGuesser.hasLocalRepository();
    }
  }

  private static boolean testRemoteConnection( RepoVfsDetails details ) {
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

    JCRSolutionFileSystem.ConfigBuilder cfgBuilder = JCRSolutionFileSystem.createConfigBuilder();
    cfgBuilder.setUseLocalRepo( opts, !connectionDetails.isRemote() );
    if ( connectionDetails.isRemote() ) {
      cfgBuilder.setUrl( opts, connectionDetails.getUrl() );
      cfgBuilder.setUser( opts, connectionDetails.getUser() );
      cfgBuilder.setPassword( opts, connectionDetails.getPass() );
    }
    return opts;
  }

  @Override
  public VFSConnectionFileNameTransformer<RepoVfsDetails> getFileNameTransformer( ConnectionManager connectionManager ) {
    return new DefaultVFSConnectionFileNameTransformer<RepoVfsDetails>( connectionManager ) {

      @Override
      public FileName toProviderFileName( ConnectionFileName pvfsFileName, RepoVfsDetails details )
        throws KettleException {
          if ( details.isRemote() ) {
            return getUriForRemote( pvfsFileName, details );
          } else {
            return super.toProviderFileName( pvfsFileName, details );
          }
      }

      protected void appendProviderUriSchemePrefix( StringBuilder providerUriBuilder, RepoVfsDetails details )
        throws KettleException {
          if ( details.isRemote() ) {
            appendProviderUriSchemePrefixForRemote( providerUriBuilder, details );
          } else {
            // default uses Details.getType directly instead of mapping to provider protocol
            // so it needs to be overridden too
            providerUriBuilder
              .append( LocalPurProvider.SCHEME )
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
      log.debug( "Valid repository present" );
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

  private static RepoVfsDetails createRemoteConnectionDetails( Repository pentahoRepo, String connectionName ) {
    URI uri = pentahoRepo.getUri().get();
    IUser userInfo = pentahoRepo.getUserInfo();
    RepoVfsDetails details = new RepoVfsDetails();
    details.setRepoType( RepoType.REMOTE );
    details.setUrl( uri.toString() );
    details.setName( connectionName );
    details.setUser( userInfo.getLogin() );
    details.setPass( userInfo.getPassword() );
    return details;
  }

}