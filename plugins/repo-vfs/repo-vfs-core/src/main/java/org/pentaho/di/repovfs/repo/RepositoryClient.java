package org.pentaho.di.repovfs.repo;


import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.client.Entity;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.util.MultiPartWriter;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.pentaho.di.repovfs.cfg.JCRSolutionConfig;

import org.pentaho.di.core.Const;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileTreeDto;
import org.pentaho.platform.util.RepositoryPathEncoder;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handles calls to server and (de)serialization
 */
public class RepositoryClient {
  private static final Logger log = LoggerFactory.getLogger( RepositoryClient.class );

  private Client client;
  private String url;

  private final JCRSolutionConfig cfg;

  public static class RepositoryClientException extends Exception {

    public Optional<ClientResponse> response = Optional.empty();

    public RepositoryClientException( String msg ) {
      super( msg );
    }

    public RepositoryClientException( ClientResponse response ) {
      super( "Failed with status: " + response.getStatus() );
      this.response = Optional.of( response );
    }

    public RepositoryClientException( String msg, Exception e ) {
      super( msg, e );
    }
  }

  public RepositoryClient( JCRSolutionConfig cfg,
                           String url,
                           BasicAuthentication auth ) {
    if ( url == null ) {
      throw new IllegalArgumentException( "URL cannot be null" );
    }
    this.url = url;
    this.cfg = cfg;

    CookieHandler.setDefault( new CookieManager( null, CookiePolicy.ACCEPT_ALL ) );

    this.client = createClient( cfg, auth );
  }

  private static Client createClient( JCRSolutionConfig cfg, BasicAuthentication auth ) {
    ClientConfig config = new ClientConfig();
    config.property( ClientProperties.FOLLOW_REDIRECTS, true );
    config.property( ClientProperties.READ_TIMEOUT, cfg.getTimeOut() );
    config.register( MultiPartWriter.class );
    config.register( JacksonFeature.class );
    Client client = ClientBuilder.newClient( config );
    auth.applyToClient( client );

    if ( log.isDebugEnabled() ) {
      addLoggingFilter( client );
    }

    return client;
  }

  /** Logs every request/response */
  private static void addLoggingFilter( Client client ) {
    client.register( new ClientRequestFilter() {
      @Override
      public void filter( ClientRequestContext requestContext ) throws IOException {
        log.debug( "Requesting: [{}] {}", requestContext.getMethod(), requestContext.getUri() );
      }
    });

    client.register( new ClientResponseFilter() {
      @Override
      public void filter( ClientRequestContext requestContext, ClientResponseContext responseContext ) throws IOException {
        int contentLength = responseContext.getLength();
        log.debug( "Response: {} Content-Length: {}", responseContext.getStatus(), contentLength );
      }
    } );
  }

  private RepositoryFileTreeDto fetchChildren( String encodedPath ) {
    String childrenUrl = cfg.getRepositoryPartialSvc( encodedPath );

    WebTarget target = client.target( url + childrenUrl );
    return target.path( "" ).request( MediaType.APPLICATION_XML_TYPE ).get( RepositoryFileTreeDto.class );
  }

  public Optional<RepositoryFileTreeDto> fetchChildTree( String[] parent ) {
    return Optional.ofNullable( fetchChildren( encodePath( parent ) ) );
  }

  /** Fetch children from repository */
  public RepositoryFileDto[] fetchChildren( String[] parentPath ) {
    String encodedPath = encodePath( parentPath );
    RepositoryFileTreeDto tree = fetchChildren( encodedPath );
    if ( tree == null || tree.getChildren() == null ) {
      return new RepositoryFileDto[0];
    }
    return tree.getChildren().stream().map( RepositoryFileTreeDto::getFile ).toArray( RepositoryFileDto[]::new );
  }

  /** Create folder with given path */
  public void createFolder( String filePath ) throws RepositoryClientException {
    try {
      String path = RepositoryPathEncoder.encodeRepositoryPath( filePath );
      String service = cfg.getCreateFolderSvc( path );

      WebTarget target = client.target( url + service );
      Response response = target.request( MediaType.TEXT_PLAIN ).put( Entity.text(" ") );
      if ( response.getStatus() != 200 ) {
        throw new RepositoryClientException( String.valueOf( response ) );
      }
    } catch ( WebApplicationException | ProcessingException e ) {
      throw new RepositoryClientException( "Client error creating folder", e );
    }
  }

  public RepositoryFileTreeDto getRoot() {
    return fetchChildTree( new String[0] ).orElse( null );
  }

  /** Download file contents */
  public InputStream getData( RepositoryFileDto fileDto ) {
    String urlPath = encodePath( fileDto.getPath() );
    // TODO: repo endpoint fails for unrecognized file types
    String endpoint = url + cfg.getDownloadSvc( urlPath );
    log.debug( "getData: " + endpoint );
    return client.target( endpoint ).request( MediaType.WILDCARD_TYPE ).get( InputStream.class );
  }

  /** Download file contents with a buffered input stream of the given size */
  public InputStream getData( RepositoryFileDto fileDto, int bufferSize ) {
    return IOUtils.buffer( getData( fileDto ), bufferSize );
  }

  protected boolean isKettleFile( String fileName ) {
    switch ( FilenameUtils.getExtension( fileName ).toLowerCase() ) {
      case Const.STRING_TRANS_DEFAULT_EXT:
      case Const.STRING_JOB_DEFAULT_EXT:
        return true;
      default:
        return false;
    }
  }

  /** Delete file or folder */
  public void delete( RepositoryFileDto file ) throws RepositoryClientException {
    final WebTarget target = client.target( url + cfg.getDeleteFileOrFolderUrl() );
    Response response = target.request( MediaType.TEXT_PLAIN ).put( Entity.entity( file.getId(), MediaType.TEXT_PLAIN ) );

    if ( response == null || response.getStatus() != Response.Status.OK.getStatusCode() ) {
      throw new RepositoryClientException( "Failed with error-code " + response.getStatus() );
    }
  }

  /** Upload file with given data to the server */
  public void writeData( final String[] fileName, InputStream data ) throws RepositoryClientException {
    final StringBuilder pathBuilder = new StringBuilder();
    for ( int i = 0; i < fileName.length; i++ ) {
      if ( i != 0 ) {
        pathBuilder.append( "/" );
      }
      pathBuilder.append( fileName[ i ] );
    }
    String path = encodePath( pathBuilder.toString() );
    String service = cfg.getUploadSvc( path );

    final WebTarget target = client.target( url + service );
    Response response = target.request( MediaType.TEXT_PLAIN ).put( Entity.entity( data, MediaType.TEXT_PLAIN ) );

    throwOnError( response );
  }

  /**
   * Moves a file or folder to an existing destination
   */
  public void moveTo( RepositoryFileDto file, final String[] destinationPath ) throws RepositoryClientException {
    String destRepoPath = encodePath( destinationPath );
    String svc = cfg.getMoveToSvc( destRepoPath );

    WebTarget target = client.target( url + svc );
    final Response response = target.request( MediaType.TEXT_PLAIN ).put( Entity.entity( file.getId(), MediaType.TEXT_PLAIN ) );
    throwOnError( response );
  }

  /**
   * Renames a file or folder in place keeping the extension
   * @param origPath the path of the original file or folder
   * @param newNameNoExt the new name without extension
   */
  public void rename( String[] origPath, String newNameNoExt ) throws RepositoryClientException {
    String origRepoPath = encodePath( origPath );
    newNameNoExt = Encode.forUriComponent( newNameNoExt );
    String svc = cfg.getRenameSvc( origRepoPath, newNameNoExt );
    WebTarget target = client.target( url + svc );
    final Response response = target.request( MediaType.WILDCARD ).put( Entity.text(" ") );
    throwOnError( response );
  }

  public Optional<RepositoryFileDto> getFileInfo( String[] path ) throws RepositoryClientException {
    String encodedPath = encodePath( path );
    String svc = cfg.getFileInfoSvc( encodedPath );
    var target = client.target( url + svc );
    var response = target.request( MediaType.APPLICATION_XML_TYPE ).get();
    switch ( response.getStatus() ) {
      case HttpStatus.SC_OK:
        return Optional.of( response.readEntity( RepositoryFileDto.class ) );
      case HttpStatus.SC_NO_CONTENT:
        // 204 is used for not found
        return Optional.empty();
      default:
        throwOnError( response );
        // unreachable
        return Optional.empty();
    }
  }

  private static String encodePath( String[] path ) {
    return ":" + Stream.of( path ).map( Encode::forUriComponent ).collect( Collectors.joining( ":" ) );
  }

  private void throwOnError( final Response response ) throws RepositoryClientException {
    final int status = response.getStatus();

    if ( status != HttpStatus.SC_OK ) {
      if ( status == HttpStatus.SC_MOVED_TEMPORARILY
        || status == HttpStatus.SC_FORBIDDEN
        || status == HttpStatus.SC_UNAUTHORIZED ) {
        throw new RepositoryClientException( "Auth error" );
      } else {
        String errMsg;
        try {
          errMsg = response.readEntity( String.class );
        } catch ( Exception e ) {
          errMsg = "Unable to get error response entity";
        }
        throw new RepositoryClientException( errMsg + " status:" + status );
      }
    }
  }

  private static String encodePath( String path ) {
    String repoEncoded = RepositoryPathEncoder.encodeRepositoryPath( path );
    return Encode.forUriComponent( repoEncoded );
  }
}
