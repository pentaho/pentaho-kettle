package org.pentaho.s3.vfs.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.Strings;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.pentaho.amazon.s3.S3Details;
import org.pentaho.amazon.s3.S3Util;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.s3.vfs.S3FileSystemConfigBuilder;
import org.pentaho.s3.vfs.client.S3Options.AuthKeys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.utils.SdkAutoCloseable;

/**
 * Owns the {@link S3Client} and {@link S3AsyncClient} instances associated with a particular
 * FileSystem instance, and the caches that decide when it is stale.
 * <p>
 * Client construction is delegated to {@link S3ClientFactory}.
 */
public class S3ClientManager {

  private static final Logger logger = LoggerFactory.getLogger( S3ClientManager.class );

  private final S3ClientFactory clientFactory;
  private final Behavior behavior;
  private final boolean hasFileSystemOptions;
  private final S3FileSystemConfigBuilder configBuilder;
  private final String rootUri;
  private final Supplier<ConnectionManager> connectionManager;
  private final Runnable onInvalidate;

  private S3Client client;

  /** Built on demand from whatever {@link #client} was last built from. */
  private S3AsyncClient asyncClient;
  private boolean clientCameFromEnv;

  /** Replaced clients, left open for anything still using them until {@link #closeClients()}. */
  private final List<SdkAutoCloseable> retiredClients = new ArrayList<>();

  // credential system properties last seen, used to check if changed for caching purposes
  private String awsAccessKeyCache;
  private String awsSecretKeyCache;
  private String awsSessionTokenCache;

  /** config if using default s3 connection */
  private Map<String, String> currentConnectionProperties = new HashMap<>();

  /**
   * 
   * @param clientFactory 
   * @param behavior
   * @param fileSystemOptions Options the associated 
   * @param rootUri VFS Root URI for the file system
   * @param connectionManager ConnectionManager supplier
   * @param onInvalidate Callback when a client is invalidated due to configuration changes
   */
  public S3ClientManager( S3ClientFactory clientFactory, Behavior behavior,
                          FileSystemOptions fileSystemOptions, String rootUri,
                          Supplier<ConnectionManager> connectionManager, Runnable onInvalidate ) {
    this.clientFactory = clientFactory;
    this.behavior = behavior;
    this.hasFileSystemOptions = fileSystemOptions != null;
    this.configBuilder = new S3FileSystemConfigBuilder( fileSystemOptions );
    this.rootUri = rootUri;
    this.connectionManager = connectionManager;
    this.onInvalidate = onInvalidate;
  }

  /**  */
  public synchronized S3Client getClient() throws FileSystemException {
    try {
      return resolveClient();
    } catch ( RuntimeException e ) {
      /*
         * The SDK builders report a bad region, endpoint or credential chain by throwing, so every
         * failure here is unchecked and would otherwise reach VFS as something it does not expect.
         */
      throw new FileSystemException( "Could not build the S3 client", e );
    }
  }

  private S3Client resolveClient() {
    S3Options options = getOptions();

    // No need to watch for option changes: AbstractFileSystem.fileSystemOptions is final, and the
    // provider caches a distinct file system per options, so a change means a different instance.
    if ( client == null && hasFileSystemOptions ) {
      setClient( createFromConfig( options ), false );
    }

    if ( client == null || ( behavior.preferEnvCredentials() && haveEnvCredentialsChanged() ) ) {
      // nothing can be cached for a client that never existed, only a replaced one invalidates
      boolean replacing = client != null;
      awsAccessKeyCache = System.getProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY );
      awsSecretKeyCache = System.getProperty( S3Util.SECRET_KEY_SYSTEM_PROPERTY );
      awsSessionTokenCache = System.getProperty( S3Util.SESSION_TOKEN_SYSTEM_PROPERTY );
      setClient( clientFactory.createFromEnv(), true );
      if ( replacing ) {
        onInvalidate.run();
      }
    }
    return client;
  }

  /**
   * The asynchronous client, for transfers that benefit from parallelism.
   * <p>
   * Resolving the synchronous client first keeps the two on the same credentials: every decision
   * about staleness is made there, and this one is rebuilt whenever that one is.
   */
  public synchronized S3AsyncClient getAsyncClient() throws FileSystemException {
    // we are borrowing all the checks from the standard client, which is used for most operations
    // so ensure it is created and using the same options as this one
    getClient();

    try {
      if ( asyncClient == null ) {
        asyncClient = clientCameFromEnv
          ? clientFactory.createAsyncFromEnv()
          : clientFactory.createAsync( withUriKeys( getOptions() ) );
      }
      return asyncClient;
    } catch ( RuntimeException e ) {
      throw new FileSystemException( "Could not build the asynchronous S3 client", e );
    }
  }

  /**
   * Closes every client built so far, replaced ones included.
   * <p>
   * VFS may go on using a file system after closing its communication link, so the next request
   * simply builds new clients.
   */
  public synchronized void closeClients() {
    retireClients();
    retiredClients.forEach( S3ClientManager::closeQuietly );
    retiredClients.clear();
  }

  private void setClient( S3Client newClient, boolean fromEnv ) {
    // also retires the asynchronous client, which is no longer built from the same credentials.
    retireClients();
    client = newClient;
    clientCameFromEnv = fromEnv;
  }

  /** Transfers may still be running on the current clients, so they are only closed later. */
  private void retireClients() {
    if ( client != null ) {
      retiredClients.add( client );
      client = null;
    }
    if ( asyncClient != null ) {
      retiredClients.add( asyncClient );
      asyncClient = null;
    }
  }

  private static void closeQuietly( SdkAutoCloseable closeable ) {
    try {
      closeable.close();
    } catch ( RuntimeException e ) {
      logger.warn( "Could not close an S3 client", e );
    }
  }

  /** Get options from the default connection or config builder, as appropriate. */
  private S3Options getOptions() {
    return getDefaultS3Connection( connectionManager, configBuilder ).map(
      defaultConn -> {
        clearClientIfDefaultConnectionChanged( defaultConn );
        return S3Options.from( currentConnectionProperties );
      } ).orElseGet( () -> {
        if ( !currentConnectionProperties.isEmpty() ) {
          currentConnectionProperties = new HashMap<>();
          invalidate();
        }
        return S3Options.from( configBuilder );
      } );
  }

  private S3Client createFromConfig( S3Options options ) {
    logger.info( "Creating client from configuration" );
    return clientFactory.create( withUriKeys( options ) );
  }

  /** Support legacy credentials written straight into the URI, which predate the configuration fields. */
  private S3Options withUriKeys( S3Options options ) {
    S3Util.S3Keys keys = S3Util.getKeysFromURI( rootUri );
    if ( keys != null ) {
      logger.warn( "Passing keys in the endpoint is discouraged, please use the appropriate fields" );
      options.setAuthKeys( new AuthKeys( keys ) );
    }
    return options;
  }

  /** Invalidate the clients and let the caller clear anything keyed on the old credentials. */
  private void invalidate() {
    retireClients();
    onInvalidate.run();
  }

  /** Drop the client if the default PVFS connection's properties changed. */
  private void clearClientIfDefaultConnectionChanged( ConnectionDetails defaultS3Connection ) {
    // If the fileSystemOptions don't contain a name, the originating url is s3:// NOT pvfs://
    // Use a specified default PVFS connection if it's available.
    if ( configBuilder.getName() != null ) {
      return;
    }

    Map<String, String> newConnectionProperties = new HashMap<>();
    newConnectionProperties.putAll( defaultS3Connection.getProperties() );

    if ( !newConnectionProperties.equals( currentConnectionProperties ) ) {
      // Track the new connection, then force a new client and let the caller drop its cache.
      currentConnectionProperties = newConnectionProperties;
      invalidate();
    }
  }

  /**
   * Get the default S3 connection if it exists and is applicable to this configuration.
   * <br>
   * A default S3 connection will replace connection references that bypass pvfs (eg. by writing {@code s3://} directly)
   */
  private static Optional<? extends ConnectionDetails> getDefaultS3Connection(
    Supplier<ConnectionManager> connectionManagerSupplier, S3FileSystemConfigBuilder configBuilder ) {
    // Connections created via a details must always have a name
    if ( configBuilder.useDefaults() && configBuilder.getName() == null ) {
      try {
        return connectionManagerSupplier.get().getConnectionDetailsByScheme( "s3" ).stream()
          .filter( S3ClientManager::isDefaultS3Connection )
          .findFirst();
      } catch ( Exception ignored ) {
        // Ignore the exception, it's OK if we can't find a default S3 connection.
      }
    }
    return Optional.empty();
  }

  private static boolean isDefaultS3Connection( ConnectionDetails connectionDetails ) {
    return Strings.CI.equals( connectionDetails.getProperties().get( S3Details.PROP_DEFAULT_S3_CONFIG ), "true" );
  }

  private boolean haveEnvCredentialsChanged() {
    return S3Util.hasChanged( awsAccessKeyCache, System.getProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY ) )
      || S3Util.hasChanged( awsSecretKeyCache, System.getProperty( S3Util.SECRET_KEY_SYSTEM_PROPERTY ) )
      || S3Util.hasChanged( awsSessionTokenCache, System.getProperty( S3Util.SESSION_TOKEN_SYSTEM_PROPERTY ) );
  }

}
