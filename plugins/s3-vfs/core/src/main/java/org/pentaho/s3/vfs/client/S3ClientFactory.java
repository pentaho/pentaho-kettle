package org.pentaho.s3.vfs.client;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.util.HttpClientManager;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.s3.vfs.client.S3Options.AuthKeys;
import org.pentaho.s3.vfs.client.S3Options.CredentialsFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkRequest;
import software.amazon.awssdk.core.interceptor.Context.AfterExecution;
import software.amazon.awssdk.core.interceptor.Context.BeforeExecution;
import software.amazon.awssdk.core.interceptor.Context.FailedExecution;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.core.interceptor.SdkExecutionAttribute;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.profiles.ProfileFile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import software.amazon.awssdk.services.s3.S3BaseClientBuilder;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

/**
 * Builds S3 clients from an {@link S3Options}.
 * <p>
 * Stateless on purpose: everything needed to build a client is passed in, nothing is cached here.
 * {@link S3ClientManager} owns the caching and decides when a client needs rebuilding.
 */
public class S3ClientFactory {

  private static final Logger logger = LoggerFactory.getLogger( S3ClientFactory.class );

  private final Behavior behavior;

  /** Resolves the trust and key stores, which are themselves VFS paths. */
  private final Bowl bowl;

  public S3ClientFactory( Behavior behavior, Bowl bowl ) {
    this.behavior = behavior;
    this.bowl = bowl;
  }

  /**
   * Builds a synchronous client, picking the endpoint-override path when one is set.
   */
  public S3Client create( S3Options options ) {
    if ( hasEndpoint( options ) ) {
      return createFromEndpoint( options );
    }
    return createForAws( options );
  }

  /**
   * Builds an asynchronous client, picking the endpoint-override path when one is set.
   */
  public S3AsyncClient createAsync( S3Options options ) {
    if ( hasEndpoint( options ) ) {
      return createAsyncFromEndpoint( options );
    }
    return createAsyncForAws( options );
  }

  private S3Client createFromEndpoint( S3Options options ) {
    S3ClientBuilder clientBuilder = configureForEndpoint( S3Client.builder(), options );

    if ( hasCustomTls( options ) ) {
      createSslHttpClientBuilder( options ).ifPresent( clientBuilder::httpClientBuilder );
    }
    return clientBuilder.build();
  }

  private S3AsyncClient createAsyncFromEndpoint( S3Options options ) {
    S3AsyncClientBuilder clientBuilder = configureForEndpoint( asyncBuilder(), options );

    if ( hasCustomTls( options ) ) {
      createSslAsyncHttpClientBuilder( options ).ifPresent( clientBuilder::httpClientBuilder );
    }
    return clientBuilder.build();
  }

  private S3Client createForAws( S3Options options ) {
    return configureForAws( S3Client.builder(), options ).build();
  }

  private S3AsyncClient createAsyncForAws( S3Options options ) {
    return configureForAws( asyncBuilder(), options ).build();
  }

  /**
   * Addresses an S3-compatible service directly, so the region is only there to satisfy signing.
   */
  private <B extends S3BaseClientBuilder<B, ?>> B configureForEndpoint( B clientBuilder, S3Options options ) {
    configureCommon( clientBuilder, options );
    return clientBuilder
      .endpointOverride( toEndpointUri( options.base().endpoint() ) )
      .region( region( options ) )
      .forcePathStyle( options.base().pathStyleAccess() );
  }

  /**
   * Addresses real AWS, where the region selects the endpoint and so can come from the environment.
   */
  private <B extends S3BaseClientBuilder<B, ?>> B configureForAws( B clientBuilder, S3Options options ) {
    configureCommon( clientBuilder, options );
    clientBuilder.crossRegionAccessEnabled( true );

    if ( isEnvRegionSet() ) {
      logger.info( "Region overridden by environment" );
    } else {
      clientBuilder.region( region( options ) );
    }
    return clientBuilder;
  }

  private <B extends S3BaseClientBuilder<B, ?>> void configureCommon( B clientBuilder, S3Options options ) {
    clientBuilder.overrideConfiguration( cfg -> cfg.addExecutionInterceptor( new LoggingInterceptor() ) );
    createCredentialsProvider( options ).ifPresent( clientBuilder::credentialsProvider );
  }

  private Region region( S3Options options ) {
    return options.base().region().map( Region::of ).orElseGet( behavior::defaultRegion );
  }

  /**
   * Multipart lets uploads of unknown length be streamed, which is the reason the asynchronous
   * client exists; the synchronous one has no equivalent.
   */
  private static S3AsyncClientBuilder asyncBuilder() {
    return S3AsyncClient.builder().multipartEnabled( true );
  }

  private static boolean hasEndpoint( S3Options options ) {
    return StringUtils.isNotEmpty( options.base().endpoint() );
  }

  private static boolean hasCustomTls( S3Options options ) {
    return options.trustStore().isPresent() || options.keyStore().isPresent();
  }

  /**
   * Builds a client from the environment, leaving credentials to the SDK's default provider chain,
   * which reads the access key, secret key and session token system properties first.
   */
  public S3Client createFromEnv() {
    return configureFromEnv( S3Client.builder() ).build();
  }

  /** The asynchronous counterpart of {@link #createFromEnv()}. */
  public S3AsyncClient createAsyncFromEnv() {
    return configureFromEnv( asyncBuilder() ).build();
  }

  private <B extends S3BaseClientBuilder<B, ?>> B configureFromEnv( B clientBuilder ) {
    logger.info( "Creating client from environment" );
    clientBuilder.crossRegionAccessEnabled( true );
    if ( !isEnvRegionSet() ) {
      clientBuilder.region( behavior.defaultRegion() );
    }
    return clientBuilder;
  }

  /**
   * Whether a region is explicitly set in the environment, from either:
   * <ul>
   * <li>an environment variable</li>
   * <li>a system variable</li>
   * <li>a configuration file, in the default location or specified by an environment variable</li>
   * <li>the EC2 instance this is running on</li>
   * </ul><br>
   * This can be used to override the user-defined region, so beware of increasing its scope.
   */
  public boolean isEnvRegionSet() {
    try {
      return DefaultAwsRegionProviderChain.builder().build().getRegion() != null;
    } catch ( Exception e ) {
      // thrown if not found or blank, ignore
      return false;
    }
  }

  /** v1 defaulted a scheme-less endpoint to https; endpointOverride rejects one outright. */
  private static URI toEndpointUri( String endpoint ) {
    URI uri = URI.create( endpoint );
    return uri.getScheme() == null ? URI.create( "https://" + endpoint ) : uri;
  }

  public static Optional<AwsCredentialsProvider> createCredentialsProvider( S3Options options ) {
    return options.authKeys().map( S3ClientFactory::createCredentialsProvider )
      .or( () -> options.credFileAuth().map( S3ClientFactory::createCredentialsProvider ) );
  }

  private static AwsCredentialsProvider createCredentialsProvider( AuthKeys authKeys ) {
    AwsCredentials awsCredentials;
    if ( authKeys.sessionToken() == null ) {
      awsCredentials = AwsBasicCredentials.create( authKeys.accessKey(), authKeys.secretKey() );
    } else {
      awsCredentials = AwsSessionCredentials.create( authKeys.accessKey(), authKeys.secretKey(),
        authKeys.sessionToken() );
    }
    return StaticCredentialsProvider.create( awsCredentials );
  }

  private static AwsCredentialsProvider createCredentialsProvider( CredentialsFile credFile ) {
    ProfileFile profileFile = ProfileFile.builder()
      .content( Paths.get( credFile.credentialsFile() ) )
      .type( ProfileFile.Type.CREDENTIALS )
      .build();
    return ProfileCredentialsProvider.builder()
      .profileFile( profileFile )
      .profileName( credFile.profileName() )
      .build();
  }

  /**
   * Configures an SSL enabled client. Handing the SDK a builder rather than a client lets it own the
   * client, and so close it along with the S3 client.
   */
  public Optional<ApacheHttpClient.Builder> createSslHttpClientBuilder( S3Options opts ) {
    boolean trustAll = opts.trustStore().map( S3Options.TrustStore::trustAll ).orElse( false );
    String trustStoreFile = opts.trustStore().map( S3Options.TrustStore::filePath ).orElse( null );
    String trustStorePass = opts.trustStore().map( S3Options.TrustStore::pass ).orElse( null );
    String keyStoreFile = opts.keyStore().map( S3Options.KeyStore::filePath ).orElse( null );
    String keyStorePass = opts.keyStore().map( S3Options.KeyStore::password ).orElse( null );

    try {
      if ( trustAll ) { // ignore other fields
        var sslContext = HttpClientManager.getSslContext( true, null, null );
        return Optional.of( ApacheHttpClient.builder()
          .socketFactory( new SSLConnectionSocketFactory( sslContext ) ) );
      }

      var vfs = KettleVFS.getInstance( bowl );
      try ( var trustStoreIn = getInputStream( vfs, trustStoreFile );
            var keyStoreIn = getInputStream( vfs, keyStoreFile ) ) {

        SSLContext sslContext = HttpClientManager.getSslContext(
          trustAll, trustStoreIn, trustStorePass,
          keyStoreIn, keyStorePass, keyStorePass );
        return Optional.of( ApacheHttpClient.builder()
          .socketFactory( new SSLConnectionSocketFactory( sslContext ) ) );
      }
    } catch ( Exception e ) {
      logger.error( "Failed to create SSL configuration from trust store", e );
      return Optional.empty();
    }
  }

  /**
   * Configures an SSL enabled async client, handed to the SDK as a builder for the same reason as
   * {@link #createSslHttpClientBuilder(S3Options)}.
   * The asynchronous HTTP layer is configured with managers rather than with the {@link SSLContext}
   * the synchronous one takes, so the two cannot share a client or configuration.
   */
  public Optional<NettyNioAsyncHttpClient.Builder> createSslAsyncHttpClientBuilder( S3Options opts ) {
    boolean trustAll = opts.trustStore().map( S3Options.TrustStore::trustAll ).orElse( false );
    String trustStoreFile = opts.trustStore().map( S3Options.TrustStore::filePath ).orElse( null );
    String trustStorePass = opts.trustStore().map( S3Options.TrustStore::pass ).orElse( null );
    String keyStoreFile = opts.keyStore().map( S3Options.KeyStore::filePath ).orElse( null );
    String keyStorePass = opts.keyStore().map( S3Options.KeyStore::password ).orElse( null );

    try {
      var httpClientBuilder = NettyNioAsyncHttpClient.builder();

      if ( trustAll ) {
        httpClientBuilder.tlsTrustManagersProvider( HttpClientManager::getTrustAllManagers );
        return Optional.of( httpClientBuilder );
      }

      var vfs = KettleVFS.getInstance( bowl );
      try ( var trustStoreIn = getInputStream( vfs, trustStoreFile );
            var keyStoreIn = getInputStream( vfs, keyStoreFile ) ) {
        if ( trustStoreIn != null ) {
          var trustManagers = HttpClientManager.getTrustManagers( trustStoreIn, trustStorePass );
          httpClientBuilder.tlsTrustManagersProvider( () -> trustManagers );
        }
        if ( keyStoreIn != null ) {
          var keyManagers = HttpClientManager.getKeyManagers( keyStoreIn, keyStorePass, keyStorePass );
          httpClientBuilder.tlsKeyManagersProvider( () -> keyManagers );
        }
        return Optional.of( httpClientBuilder );
      }
    } catch ( Exception e ) {
      logger.error( "Failed to create SSL configuration from trust store", e );
      return Optional.empty();
    }
  }

  private static InputStream getInputStream( IKettleVFS vfs, String path ) throws KettleFileException,
    FileSystemException {
    if ( path == null ) {
      return null;
    }
    var fileObj = vfs.getFileObject( path );
    try {
      var content = fileObj.getContent();
      var inputStream = content.getInputStream();
      return new FilterInputStream( inputStream ) {
        @Override
        public void close() throws IOException {
          IOUtils.closeQuietly( inputStream, content, fileObj );
        }
      };
    } catch ( FileSystemException e ) {
      IOUtils.closeQuietly( fileObj );
      throw e;
    }
  }

  private static class LoggingInterceptor implements ExecutionInterceptor {

    private static final Logger log = LoggerFactory.getLogger( LoggingInterceptor.class );

    @Override
    public void beforeExecution( BeforeExecution context, ExecutionAttributes executionAttributes ) {
      if ( log.isDebugEnabled() ) {
        log.debug( "Calling {}: {}", getOpName( executionAttributes ), getPath( context.request() ) );
      }
    }

    @Override
    public void afterExecution( AfterExecution context, ExecutionAttributes executionAttributes ) {
      if ( log.isTraceEnabled() ) {
        log.trace( "Called {}", getOpName( executionAttributes ) );
      }
    }

    @Override
    public void onExecutionFailure( FailedExecution context, ExecutionAttributes executionAttributes ) {
      // this will also throw an exception somewhere
      if ( log.isDebugEnabled() ) {
        log.debug( "Execution failed for {} ({})",
          getOpName( executionAttributes ),
          getStatus( context.httpResponse() ) );
      }
    }

    private static String getOpName( ExecutionAttributes executionAttributes ) {
      return executionAttributes.getAttribute( SdkExecutionAttribute.OPERATION_NAME );
    }

    private static String getPath( SdkRequest request ) {
      String buck = request.getValueForField( "Bucket", String.class ).orElse( "<no bucket>" );
      String key = request.getValueForField( "Key", String.class )
        .or( () -> request.getValueForField( "Prefix", String.class ) )
        .orElse( "<no key>" );
      return buck + "/" + key;
    }

    private static String getStatus( Optional<SdkHttpResponse> resp ) {
      return resp.map( r -> r.statusCode() + " " + r.statusText().orElse( "?" ) ).orElse( "<no http resp>" );
    }

  }

}
