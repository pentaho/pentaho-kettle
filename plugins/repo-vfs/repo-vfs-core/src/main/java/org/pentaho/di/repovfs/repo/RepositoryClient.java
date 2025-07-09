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
import org.apache.commons.lang3.StringUtils;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Handles calls to server and serdes, holds a cached tree
 */
public class RepositoryClient {
  private static final Logger log = LoggerFactory.getLogger( RepositoryClient.class );

  // ":" --> "%3A"
  private static final String ENCODED_COLON = RepositoryPathEncoder.encodeURIComponent( ":" );

  private Client client;
  private String url;
  private RepositoryFileTreeDto root;
  private long refreshTime;

  private final boolean loadTreePartially;

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
    this.loadTreePartially = cfg.isPartialLoading();
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

  /** Clear the cached file tree */
  public void refreshRoot() {
    log.debug( "refreshing root.." );
    RepositoryFileTreeDto tree;
    if ( loadTreePartially ) {
      WebTarget target = client.target( url + cfg.getRepositoryPartialRootSvc() );
      tree = target.path( "" ).request( MediaType.APPLICATION_XML_TYPE ).get( RepositoryFileTreeDto.class );
      tree = proxyRoot( tree );
    } else {
      WebTarget target = client.target( url + cfg.getRepositorySvc() );
      tree = target.path( "" ).request( MediaType.APPLICATION_XML_TYPE ).get( RepositoryFileTreeDto.class );
    }
    setRoot( tree );
  }

  public void clearCache( String[] path ) {
    try {
      if ( loadTreePartially ) {
        lookupNode( path ).ifPresent( treeDto -> {
          if ( treeDto instanceof RepositoryFileTreeDtoProxy ) {
            RepositoryFileTreeDtoProxy proxy = (RepositoryFileTreeDtoProxy) treeDto;
            proxy.clearCache();
            if ( log.isDebugEnabled() ) {
              log.debug( "cache cleared for {} ", StringUtils.join( path, "/" ) );
            }
          } else {
            log.warn( "No proxy on partially loaded tree at path", StringUtils.join( path, "/" ) );
            this.root = null;
          }
        } );

      } else {
        this.root = null;
        log.debug( "cache cleared at root" );
      }
    } catch ( RepositoryClientException e ) {
      log.error( "Error refreshing folder: {}", StringUtils.join( path, "/" ), e );
    }
  }

  private RepositoryFileTreeDto fetchChildren( String path ) {
    String encodedPath = encodePathForRequest( path );
    String childrenUrl = cfg.getRepositoryPartialSvc( encodedPath );

    WebTarget target = client.target( url + childrenUrl );
    return target.path( "" ).request( MediaType.APPLICATION_XML_TYPE ).get( RepositoryFileTreeDto.class );

  }

  /** Load the children of the given path into the cached tree */
  public List<RepositoryFileTreeDto> loadChildren( String path ) {
    log.debug( "loadChildren({})", path );
    RepositoryFileTreeDto element = fetchChildren( path );
    List<RepositoryFileTreeDto> tree;
    if ( element == null || element.getChildren() == null ) {
      tree = Collections.emptyList();
    } else {
      List<RepositoryFileTreeDto> children = element.getChildren();
      tree = new ArrayList<RepositoryFileTreeDto>( children.size() );
      for ( RepositoryFileTreeDto child : children ) {
        RepositoryFileTreeDtoProxy dto = new RepositoryFileTreeDtoProxy( child, this );
        tree.add( dto );
      }
    }
    return tree;
  }

  private RepositoryFileTreeDtoProxy proxyRoot( RepositoryFileTreeDto original ) {
    // save it, as proxy will replace the list with empty
    List<RepositoryFileTreeDto> originalChildren = original.getChildren();
    RepositoryFileTreeDtoProxy proxy = new RepositoryFileTreeDtoProxy( original, this );
    if ( originalChildren == null ) {
      proxy.setChildren( Collections.<RepositoryFileTreeDto>emptyList() );
    } else {
      // pre-populating root's direct children
      List<RepositoryFileTreeDto> proxiedChildren = new ArrayList<RepositoryFileTreeDto>( originalChildren.size() );
      for ( RepositoryFileTreeDto child : originalChildren ) {
        proxiedChildren.add( new RepositoryFileTreeDtoProxy( child, this ) );
      }
      proxy.setChildren( proxiedChildren );
    }
    return proxy;
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

  /**
   * Make a colon-separated path with encoded fragments
   */
  public static String encodePathForRequest( String path ) {
    String pathEncoded = RepositoryPathEncoder.encodeRepositoryPath( path );
    String utf8 = RepositoryPathEncoder.encodeURIComponent( pathEncoded );
    // replace encoded colons with original
    return utf8.replaceAll( ENCODED_COLON, ":" );
  }

  public RepositoryFileTreeDto getRoot() {
    if ( root == null ) {
      refreshRoot();
    }

    return root;
  }

  public void setRoot( final RepositoryFileTreeDto root ) {
    if ( root == null ) {
      throw new NullPointerException();
    }

    this.refreshTime = System.currentTimeMillis();
    this.root = root;
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

  public long getRefreshTime() {
    return refreshTime;
  }

  public void setRefreshTime( final long refreshTime ) {
    this.refreshTime = refreshTime;
  }

  /** Delete file or folder */
  public void delete( RepositoryFileDto file ) throws RepositoryClientException {
    final WebTarget target = client.target( url + cfg.getDeleteFileOrFolderUrl() );
    Response response = target.request( MediaType.TEXT_PLAIN ).put( Entity.entity( file.getId(), MediaType.TEXT_PLAIN ) );

    if ( response == null || response.getStatus() != Response.Status.OK.getStatusCode() ) {
      throw new RepositoryClientException( "Failed with error-code " + response.getStatus() );
    }
  }

  /** Search for the given path, filling the cache tree along the way */
  public Optional<RepositoryFileTreeDto> lookupNode( final String[] path ) throws RepositoryClientException {
    if ( log.isDebugEnabled() ) {
      log.debug( "lookupNode: {}", StringUtils.join( path, "/" ) );
    }
    if ( root == null ) {
      refreshRoot();
    }
    if ( path.length == 0 ) {
      return Optional.of( root );
    }
    if ( "".equals( path[ 0 ] ) ) {
      if ( path.length == 1 ) {
        return Optional.of( root );
      }
    }
    RepositoryFileTreeDto element = root;
    for ( final String pathSegment : path ) {
      RepositoryFileTreeDto name = null;
      final List<RepositoryFileTreeDto> children = element.getChildren();
      if ( children == null ) {
        return Optional.empty();
      }

      for ( final RepositoryFileTreeDto child : children ) {
        final RepositoryFileDto file = getFileDto( child );
        if ( pathSegment.equals( file.getName() ) ) {
          name = child;
          break;
        }
      }
      if ( name == null ) {
        return Optional.empty();
      }
      element = name;
    }
    return Optional.ofNullable( element );
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

  /** Renames a file or folder in place */
  public void rename( String[] origPath, String newName ) throws RepositoryClientException {
    String origRepoPath = encodePath( origPath );
    String svc = cfg.getRenameSvc( origRepoPath, newName );
    WebTarget target = client.target( url + svc );
    final Response response = target.request( MediaType.WILDCARD ).put( Entity.text(" ") );
    throwOnError( response );
  }

  private static String encodePath( String[] path ) {
    String repoPath = ":" + StringUtils.join( path, ":" );
    return Encode.forUriComponent( repoPath );
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

  private RepositoryFileDto getFileDto( final RepositoryFileTreeDto child ) throws RepositoryClientException {
    final RepositoryFileDto file = child.getFile();
    if ( file == null ) {
      throw new RepositoryClientException(
        "BI-Server returned a RepositoryFileTreeDto without an attached RepositoryFileDto!" );
    }
    return file;
  }
}
