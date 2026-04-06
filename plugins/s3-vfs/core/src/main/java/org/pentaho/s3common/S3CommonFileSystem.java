/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.s3common;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.pentaho.amazon.s3.S3Details;
import org.pentaho.amazon.s3.S3Util;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.util.HttpClientManager;
import org.pentaho.di.core.util.StorageUnitConverter;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.s3common.S3Options.AuthKeys;
import org.pentaho.s3common.S3Options.CredentialsFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

public abstract class S3CommonFileSystem extends AbstractFileSystem {

  private static final Class<?> PKG = S3CommonFileSystem.class;
  private static final Logger logger = LoggerFactory.getLogger( PKG );

  // S3 part size constants
  /**
   * Minimum part size specified in documentation
   * see https://docs.aws.amazon.com/AmazonS3/latest/dev/qfacts.html
   */
  private static final String MIN_PART_SIZE = "5MB";
  /**
   * Maximum part size specified in documentation
   * see https://docs.aws.amazon.com/AmazonS3/latest/dev/qfacts.html
   */
  private static final String MAX_PART_SIZE = "5GB";
  private static final StorageUnitConverter STATIC_STORAGE_UNIT_CONVERTER = new StorageUnitConverter();
  private static final long MIN_PART_SIZE_BYTES = STATIC_STORAGE_UNIT_CONVERTER.displaySizeToByteCount( MIN_PART_SIZE );
  private static final long MAX_PART_SIZE_BYTES = STATIC_STORAGE_UNIT_CONVERTER.displaySizeToByteCount( MAX_PART_SIZE );

  // S3 client and connection state
  private AmazonS3 client;

  private String awsAccessKeyCache;
  private String awsSecretKeyCache;

  // config if using details
  private FileSystemOptions currentFileSystemOptions;

  // config if using default s3 connection
  private Map<String, String> currentConnectionProperties;

  private final Supplier<ConnectionManager> connectionManager = ConnectionManager::getInstance;

  // Helpers and utilities
  protected StorageUnitConverter storageUnitConverter;
  protected S3KettleProperty s3KettleProperty;
  protected S3TransferManager s3TransferManager;

  protected S3CommonFileSystem( final FileName rootName, final FileSystemOptions fileSystemOptions ) {
    this( rootName, fileSystemOptions, STATIC_STORAGE_UNIT_CONVERTER, new S3KettleProperty() );
  }

  protected S3CommonFileSystem( final FileName rootName, final FileSystemOptions fileSystemOptions,
                                final StorageUnitConverter storageUnitConverter, final S3KettleProperty s3KettleProperty ) {
    super( rootName, null, fileSystemOptions );
    this.storageUnitConverter = storageUnitConverter;
    this.s3KettleProperty = s3KettleProperty;
    this.currentConnectionProperties = new HashMap<>();
  }

  @Override
  protected void addCapabilities( Collection<Capability> caps ) {
    caps.addAll( S3CommonFileProvider.capabilities );
  }

  protected abstract FileObject createFile( AbstractFileName name ) throws Exception;

  public AmazonS3 getS3Client() {
    S3CommonFileSystemConfigBuilder s3CommonFileSystemConfigBuilder =
      new S3CommonFileSystemConfigBuilder( getFileSystemOptions() );

    var defaultS3Connection = getDefaultS3Connection( connectionManager, s3CommonFileSystemConfigBuilder );

    clearClientAndCacheIfChanged( s3CommonFileSystemConfigBuilder, defaultS3Connection );

    if ( client == null && getFileSystemOptions() != null ) {
      currentFileSystemOptions = getFileSystemOptions();
      client = createClientFromConfig( s3CommonFileSystemConfigBuilder, defaultS3Connection );
    }

    if ( client == null || haveEnvCredentialsChanged() ) {
      client = createClientFromEnv();
    }
    return client;
  }

  private AmazonS3 createClientFromEnv() {
    try {
      awsAccessKeyCache = System.getProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY );
      awsSecretKeyCache = System.getProperty( S3Util.SECRET_KEY_SYSTEM_PROPERTY );
      if ( isEnvRegionSet() ) {
        return AmazonS3ClientBuilder.standard()
          .enableForceGlobalBucketAccess()
          .build();
      } else {
        return AmazonS3ClientBuilder.standard()
          .enableForceGlobalBucketAccess()
          .withRegion( Regions.DEFAULT_REGION )
          .build();
      }
    } catch ( Exception ex ) {
      logger.error( "Could not get an S3Client", ex );
      return null;
    }
  }

  private AmazonS3 createClientFromConfig( S3CommonFileSystemConfigBuilder s3CommonFileSystemConfigBuilder,
                                          Optional<? extends ConnectionDetails> defaultS3Connection ) {

    S3Options options;
    if ( s3CommonFileSystemConfigBuilder.getName() == null && defaultS3Connection.isPresent() ) {
      // using a default S3 connection
      options = S3Options.from( currentConnectionProperties );
    } else {
      options = S3Options.from( s3CommonFileSystemConfigBuilder );
    }

    S3Util.S3Keys keys = S3Util.getKeysFromURI( getRootURI() );
    if ( keys != null ) {
      logger.warn( "Passing keys in the endpoint is discouraged, please use the appropriate fields" );
      options.setAuthKeys( new AuthKeys( keys ) );
    }

    return createS3Client( options );
  }

  private AmazonS3 createS3Client( S3Options options ) {
    if ( StringUtils.isNotEmpty( options.base().endpoint() ) ) {
      return createClientFromEndpoint( options );
    } else {
      AmazonS3ClientBuilder clientBuilder = AmazonS3ClientBuilder.standard()
        .enableForceGlobalBucketAccess()
        .withCredentials( createCredentialsProvider( options ) );
      if ( !isEnvRegionSet() ) {
        clientBuilder.withRegion( options.base().region() );
      }
      return clientBuilder.build();
    }
  }


  public static AWSCredentialsProvider createCredentialsProvider( S3Options options ) {
    return options.authKeys().map( S3CommonFileSystem::createCredentialsProvider )
        .or( () -> options.credFileAuth().map( S3CommonFileSystem::createCredentialsProvider ) )
        .orElse( null );
  }

  public static AmazonS3 createClientFromEndpoint( S3Options options ) {
    var baseOpts = options.base();
    ClientConfiguration clientConfiguration = new ClientConfiguration();
    clientConfiguration.setSignerOverride( S3Util.isEmpty( baseOpts.signatureVersion() ) ?
      S3Util.SIGNATURE_VERSION_SYSTEM_PROPERTY : baseOpts.signatureVersion() );

    if ( options.trustStore().isPresent() || options.keyStore().isPresent() ) {
      setSslContext( clientConfiguration, options );
    }
    return AmazonS3ClientBuilder.standard()
      .withEndpointConfiguration(
        new AwsClientBuilder.EndpointConfiguration( baseOpts.endpoint(), baseOpts.region() ) )
      .withPathStyleAccessEnabled( baseOpts.pathStyleAccess() )
      .withClientConfiguration( clientConfiguration )
      .withCredentials( createCredentialsProvider( options ) ).build();
  }

  /** Invalidate client and clear VFS cache if properties changed */
  private void clearClientAndCacheIfChanged( S3CommonFileSystemConfigBuilder s3CommonFileSystemConfigBuilder,
                                             Optional<? extends ConnectionDetails> defaultS3Connection ) {
    // If the fileSystemOptions don't contain a name, the originating url is s3:// NOT pvfs://
    // Use a specified default PVFS connection if it's available.
    if ( s3CommonFileSystemConfigBuilder.getName() == null ) {
      // Copy the connection properties
      Map<String, String> newConnectionProperties = new HashMap<>();
      defaultS3Connection
              .ifPresent( connectionDetails -> newConnectionProperties.putAll( connectionDetails.getProperties() ) );
      // Have the default connection properties changed?
      if ( !newConnectionProperties.equals( currentConnectionProperties ) ) {
        // Force a new connection if the default PVFS was changed
        client = null;
        // Track the new connection
        currentConnectionProperties = newConnectionProperties;
        // Clear the file system cache as the credentials have changed and the cache is now invalid.
        this.getFileSystemManager().getFilesCache().clear( this );
      }
    }
    if ( currentFileSystemOptions != null && !currentFileSystemOptions.equals( getFileSystemOptions() ) ) {
      client = null;
      this.getFileSystemManager().getFilesCache().clear( this );
    }
  }

  public static Optional<? extends ConnectionDetails> getDefaultS3Connection( Supplier<ConnectionManager> connectionManagerSupplier,
                                                                              S3CommonFileSystemConfigBuilder s3CommonFileSystemConfigBuilder ) {
    if ( s3CommonFileSystemConfigBuilder.useDefaults() ) {
      try {
        return connectionManagerSupplier.get().getConnectionDetailsByScheme( "s3" ).stream()
          .filter( S3CommonFileSystem::isDefaultS3Connection )
          .findFirst();
      } catch ( Exception ignored ) {
        // Ignore the exception, it's OK if we can't find a default S3 connection.
      }
    }
    return Optional.empty();
  }

  private static boolean isDefaultS3Connection( ConnectionDetails connectionDetails ) {
    return StringUtils.equalsIgnoreCase( connectionDetails.getProperties().get( S3Details.PROP_DEFAULT_S3_CONFIG ), "true" );
  }

  private static AWSCredentialsProvider createCredentialsProvider( AuthKeys authKeys ) {
    AWSCredentials awsCredentials;
    if ( authKeys.sessionToken() == null ) {
      awsCredentials = new BasicAWSCredentials( authKeys.accessKey(), authKeys.secretKey() );
    } else {
      awsCredentials = new BasicSessionCredentials( authKeys.accessKey(), authKeys.secretKey(), authKeys.sessionToken() );
    }
    return new AWSStaticCredentialsProvider( awsCredentials );
  }

  private static AWSCredentialsProvider createCredentialsProvider( CredentialsFile credFile ) {
      ProfilesConfigFile profilesConfigFile = new ProfilesConfigFile( credFile.credentialsFile() );
      return new ProfileCredentialsProvider( profilesConfigFile, credFile.profileName() );
  }

  private boolean haveEnvCredentialsChanged() {
    return S3Util.hasChanged( awsAccessKeyCache, System.getProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY ) )
      || S3Util.hasChanged( awsSecretKeyCache, System.getProperty( S3Util.SECRET_KEY_SYSTEM_PROPERTY ) );
  }

  /** region is set if explicitly set in env or configuration file is explicitly set */
  protected boolean isEnvRegionSet() {
    if ( System.getenv( S3Util.AWS_REGION ) != null || System.getenv( S3Util.AWS_CONFIG_FILE ) != null ) {
      return true;
    }
    //check if configuration file exists in default location
    File awsConfigFolder = new File(
      System.getProperty( "user.home" ) + File.separator + S3Util.AWS_FOLDER + File.separator + S3Util.CONFIG_FILE );
    if ( awsConfigFolder.exists() ) {
      return true;
    }
    //When running on an Amazon EC2 instance getCurrentRegion will get its region. Null if not running in an EC2 instance
    return Regions.getCurrentRegion() != null;
  }

  public long getPartSize() {
    return parsePartSize( s3KettleProperty.getPartSize() );
  }

  protected long parsePartSize( String partSizeString ) {
    long parsePartSize = convertToLong( partSizeString );
    if ( parsePartSize < MIN_PART_SIZE_BYTES ) {
      if ( logger.isWarnEnabled() ) {
        logger.warn( BaseMessages.getString( PKG, "WARN.S3MultiPart.DefaultPartSize", partSizeString, MIN_PART_SIZE ) );
      }
      parsePartSize = MIN_PART_SIZE_BYTES;
    }

    // still allow > 5GB, api might be updated in the future
    if ( logger.isWarnEnabled() && parsePartSize > MAX_PART_SIZE_BYTES ) {
      logger.warn( BaseMessages.getString( PKG, "WARN.S3MultiPart.MaximumPartSize", partSizeString, MAX_PART_SIZE ) );
    }
    return parsePartSize;
  }

  protected long convertToLong( String partSize ) {
    return storageUnitConverter.displaySizeToByteCount( partSize );
  }

  protected TransferManager buildTransferManager() {
    TransferManagerBuilder transferManagerBuilder = TransferManagerBuilder.standard()
      .withS3Client( getS3Client() );
    if ( getPartSize() > 0 ) {
      transferManagerBuilder.withMinimumUploadPartSize( getPartSize() );
      transferManagerBuilder.withMultipartCopyPartSize( getPartSize() ); // Only used above 5GB by default, can be changed with multipartCopyThreshold
    }
    return transferManagerBuilder.build();
  }

  protected S3TransferManager getS3TransferManager() {
    if ( s3TransferManager == null ) {
      s3TransferManager = new S3TransferManager( buildTransferManager() );
    }
    return s3TransferManager;
  }

  /**
   * Uploads the content of the specified FileObject to the specified S3FileObject using multipart upload.
   *
   * @param src The source FileObject to upload from.
   * @param dst The destination S3FileObject to upload to.
   * @throws FileSystemException If an error occurs during the upload operation.
   */
  public void upload( FileObject src, S3CommonFileObject dst ) throws FileSystemException {
    getS3TransferManager().upload( src, dst );
  }

  /**
   * Copies the content of the specified S3FileObject to another S3FileObject using server-side multipart copy.
   * If the source or destination is not an S3FileObject, it will throw a FileSystemException.
   *
   * @param src  The source S3FileObject to copy from.
   * @param dest The destination S3FileObject to copy to.
   * @throws FileSystemException If an error occurs during the copy operation.
   */
  public void copy( S3CommonFileObject src, S3CommonFileObject dest ) throws FileSystemException {
    getS3TransferManager().copy( src, dest );
  }

  public static void setSslContext( ClientConfiguration clientConfig, S3Options opts ) {
    boolean trustAll = opts.trustStore().map( S3Options.TrustStore::trustAll ).orElse( false );
    String trustStoreFile = opts.trustStore().map( S3Options.TrustStore::filePath ).orElse( null );
    String trustStorePass = opts.trustStore().map( S3Options.TrustStore::pass ).orElse( null );
    String keyStoreFile = opts.keyStore().map( S3Options.KeyStore::filePath ).orElse( null );
    String keyStorePass = opts.keyStore().map( S3Options.KeyStore::password ).orElse( null );

    var bowl = DefaultBowl.getInstance();
    var vfs = KettleVFS.getInstance( bowl );
    try ( var trustStoreIn = getInputStream( vfs, trustStoreFile );
          var keyStoreIn = getInputStream( vfs, keyStoreFile ) ) {

      SSLContext sslContext = HttpClientManager.getSslContext(
        trustAll, trustStoreIn, trustStorePass,
        keyStoreIn, keyStorePass, keyStorePass );
      clientConfig.getApacheHttpClientConfig().setSslSocketFactory(
        new SSLConnectionSocketFactory( sslContext ) );

    } catch ( Exception e ) {
      logger.error( "Failed to create SSL configuration from trust store", e );
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

}
