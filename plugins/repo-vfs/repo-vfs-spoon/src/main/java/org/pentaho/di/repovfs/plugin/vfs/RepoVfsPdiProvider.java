package org.pentaho.di.repovfs.plugin.vfs;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryBowl;
import org.pentaho.di.repovfs.cfg.JCRSolutionConfig;
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

import java.net.URL;
import java.util.ArrayList;
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
 * eg.:
 * "pvfs://my-repo-conn/some/path" -> "jcr-solution:http://localhost:8080/pentaho!/some/path"
 * </pre>
 */
public class RepoVfsPdiProvider  extends BaseVFSConnectionProvider<RepoVfsDetails> {

  public static final String NAME = ConnectionManager.STRING_REPO_VFS_PROVIDER_NAME;

  private static final Logger log = LoggerFactory.getLogger( RepoVfsPdiProvider.class );

  @Override
  public List<VFSRoot> getLocations( RepoVfsDetails details ) {
    return Collections.emptyList();
  }

  @Override
  public String getProtocol( RepoVfsDetails vfsConnectionDetails ) {
    return JCRSolutionFileProvider.SCHEME;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getKey() {
    return JCRSolutionFileProvider.SCHEME;
  }

  @Override
  public Class<RepoVfsDetails> getClassType() {
    return RepoVfsDetails.class;
  }

  @Override
  public boolean test( RepoVfsDetails details ) throws KettleException {
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
    cfgBuilder.setUrl( opts, connectionDetails.getUrl() );
    cfgBuilder.setUser( opts, connectionDetails.getUser() );
    cfgBuilder.setPassword( opts, connectionDetails.getPass() );
    return opts;
  }
  @Override
  public VFSConnectionFileNameTransformer<RepoVfsDetails> getFileNameTransformer( ConnectionManager connectionManager ) {
    return new DefaultVFSConnectionFileNameTransformer<RepoVfsDetails>( connectionManager ) {

      @Override
      public FileName toProviderFileName( ConnectionFileName pvfsFileName, RepoVfsDetails details )
        throws KettleException {
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

      protected void appendProviderUriSchemePrefix( StringBuilder providerUriBuilder, RepoVfsDetails details )
        throws KettleException {
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
  public List<RepoVfsDetails> getConnectionDetails( ConnectionManager connectionManager ) {
    RepoVfsDetails details;
    List<RepoVfsDetails> detailsList = new ArrayList<>();
    Bowl bowl = connectionManager.getBowl();
    if ( bowl instanceof RepositoryBowl ) {
      details = createConnectionDetail( ( (RepositoryBowl) bowl).getRepository(), ConnectionManager.STRING_REPO_CONNECTION );
      detailsList.add( details );
    }
    return detailsList;

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

  private static RepoVfsDetails createConnectionDetail( Repository repo, String connectionName ) {
    IUser userInfo = repo.getUserInfo();

    RepoVfsDetails details = new RepoVfsDetails();
    repo.getUri().ifPresent( uri -> {
      log.debug( "URI: {}", uri.toString() );
      details.setUrl( uri.toString() );
      details.setName( connectionName );
      details.setUser( userInfo.getLogin() );
      details.setPass( userInfo.getPassword() );
    } );
    return details;
  }

}
