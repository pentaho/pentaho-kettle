package org.pentaho.di.repovfs.plugin.vfs;

import org.pentaho.di.repovfs.cfg.JCRSolutionConfig;
import org.pentaho.di.repovfs.repo.BasicAuthentication;
import org.pentaho.di.repovfs.repo.RepositoryClient;
import org.pentaho.di.repovfs.vfs.JCRSolutionFileProvider;
import org.pentaho.di.repovfs.vfs.JCRSolutionFileSystem;

import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.BaseVFSConnectionProvider;
import org.pentaho.di.connections.vfs.DefaultVFSConnectionFileNameTransformer;
import org.pentaho.di.connections.vfs.VFSConnectionFileNameTransformer;
import org.pentaho.di.connections.vfs.VFSRoot;
import org.pentaho.di.connections.vfs.provider.ConnectionFileName;
import org.pentaho.di.core.exception.KettleException;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemOptions;


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

  /** The connection instance for this provider */
  private Optional<RepoVfsDetails> activeDetails = Optional.empty();

  /** Set an active connection instance for this provider */
  public void setActiveDetails( RepoVfsDetails details ) {
    activeDetails = Optional.of( details );
  }

  /** Clear the active connection instance for this provider, if any */
  public void clearActiveDetails() {
    activeDetails = Optional.empty();
  }

  /** Get the active connection instance for this provider, if any */
  public Optional<RepoVfsDetails> getActiveDetails() {
    return activeDetails;
  }

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
    return activeDetails.map( Collections::singletonList ).orElse( Collections.emptyList() );
  }

  @Override
  public List<String> getNames() {
    return activeDetails.map( ConnectionDetails::getName ).map( Collections::singletonList ).orElse( Collections
      .emptyList() );
  }

  @Override
  public List<RepoVfsDetails> getConnectionDetails( ConnectionManager connectionManager ) {
    return getConnectionDetails();
  }

  @Override
  public List<String> getNames( ConnectionManager connectionManager ) {
    return getNames();
  }

  @Override
  public boolean isStorageManaged() {
    return false;
  }
}
