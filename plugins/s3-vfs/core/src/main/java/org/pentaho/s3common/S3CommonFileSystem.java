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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.pentaho.amazon.s3.S3Util;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.util.StorageUnitConverter;
import org.pentaho.di.i18n.BaseMessages;
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

public abstract class S3CommonFileSystem extends AbstractFileSystem {

  private static final Class<?> PKG = S3CommonFileSystem.class;
  private static final Logger logger = LoggerFactory.getLogger( PKG );
  private static final String DEFAULT_S3_CONFIG_PROPERTY = "defaultS3Config";

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
  private FileSystemOptions currentFileSystemOptions;
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

    Optional<? extends ConnectionDetails> defaultS3Connection = Optional.empty();
    try {
      if ( s3CommonFileSystemConfigBuilder.useDefaults() ) {
        defaultS3Connection =
        connectionManager.get().getConnectionDetailsByScheme( "s3" ).stream().filter(
          connectionDetails -> connectionDetails.getProperties().get( DEFAULT_S3_CONFIG_PROPERTY ) != null
            && connectionDetails.getProperties().get( DEFAULT_S3_CONFIG_PROPERTY ).equalsIgnoreCase( "true" ) )
        .findFirst();
      }
    } catch ( Exception ignored ) {
      // Ignore the exception, it's OK if we can't find a default S3 connection.
    }

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

    if ( client == null && getFileSystemOptions() != null ) {
      currentFileSystemOptions = getFileSystemOptions();
      String accessKey = null;
      String secretKey = null;
      String sessionToken = null;
      String region = null;
      String credentialsFilePath = null;
      String profileName = null;
      String endpoint = null;
      String signatureVersion = null;
      String pathStyleAccess = null;

      if ( s3CommonFileSystemConfigBuilder.getName() == null && defaultS3Connection.isPresent() ) {
        accessKey = Encr.decryptPassword( currentConnectionProperties.get( "accessKey" ) );
        secretKey = Encr.decryptPassword( currentConnectionProperties.get( "secretKey" ) );
        sessionToken = Encr.decryptPassword( currentConnectionProperties.get( "sessionToken" ) );
        region = currentConnectionProperties.get( "region" );
        credentialsFilePath = currentConnectionProperties.get( "credentialsFilePath" );
        profileName = currentConnectionProperties.get( "profileName" );
        endpoint = currentConnectionProperties.get( "endpoint" );
        signatureVersion = currentConnectionProperties.get( "signatureVersion" );
        pathStyleAccess = currentConnectionProperties.get( S3CommonFileSystemConfigBuilder.PATHSTYLE_ACCESS );
      } else {
        accessKey = s3CommonFileSystemConfigBuilder.getAccessKey();
        secretKey = s3CommonFileSystemConfigBuilder.getSecretKey();
        sessionToken = s3CommonFileSystemConfigBuilder.getSessionToken();
        region = s3CommonFileSystemConfigBuilder.getRegion();
        credentialsFilePath = s3CommonFileSystemConfigBuilder.getCredentialsFile();
        profileName = s3CommonFileSystemConfigBuilder.getProfileName();
        endpoint = s3CommonFileSystemConfigBuilder.getEndpoint();
        signatureVersion = s3CommonFileSystemConfigBuilder.getSignatureVersion();
        pathStyleAccess = s3CommonFileSystemConfigBuilder.getPathStyleAccess();
      }
      boolean access = ( pathStyleAccess == null ) || Boolean.parseBoolean( pathStyleAccess );

      AWSCredentialsProvider awsCredentialsProvider = null;
      Regions regions = Regions.DEFAULT_REGION;

      S3Util.S3Keys keys = S3Util.getKeysFromURI( getRootURI() );
      if ( keys != null ) {
        accessKey = keys.getAccessKey();
        secretKey = keys.getSecretKey();
      }

      if ( !S3Util.isEmpty( accessKey ) && !S3Util.isEmpty( secretKey ) ) {
        AWSCredentials awsCredentials;
        if ( S3Util.isEmpty( sessionToken ) ) {
          awsCredentials = new BasicAWSCredentials( accessKey, secretKey );
        } else {
          awsCredentials = new BasicSessionCredentials( accessKey, secretKey, sessionToken );
        }
        awsCredentialsProvider = new AWSStaticCredentialsProvider( awsCredentials );
        regions = S3Util.isEmpty( region ) ? Regions.DEFAULT_REGION : Regions.fromName( region );
      } else if ( !S3Util.isEmpty( credentialsFilePath ) ) {
        ProfilesConfigFile profilesConfigFile = new ProfilesConfigFile( credentialsFilePath );
        awsCredentialsProvider = new ProfileCredentialsProvider( profilesConfigFile, profileName );
      }

      if ( !S3Util.isEmpty( endpoint ) ) {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setSignerOverride(
          S3Util.isEmpty( signatureVersion ) ? S3Util.SIGNATURE_VERSION_SYSTEM_PROPERTY : signatureVersion );
        client = AmazonS3ClientBuilder.standard()
          .withEndpointConfiguration( new AwsClientBuilder.EndpointConfiguration( endpoint, regions.getName() ) )
          .withPathStyleAccessEnabled( access )
          .withClientConfiguration( clientConfiguration )
          .withCredentials( awsCredentialsProvider )
          .build();
      } else {
        AmazonS3ClientBuilder clientBuilder = AmazonS3ClientBuilder.standard()
          .enableForceGlobalBucketAccess()
          .withCredentials( awsCredentialsProvider );
        if ( !isRegionSet() ) {
          clientBuilder.withRegion( regions );
        }
        client = clientBuilder.build();
      }
    }

    if ( client == null || hasClientChangedCredentials() ) {
      try {
        if ( isRegionSet() ) {
          client = AmazonS3ClientBuilder.standard()
            .enableForceGlobalBucketAccess()
            .build();
        } else {
          client = AmazonS3ClientBuilder.standard()
            .enableForceGlobalBucketAccess()
            .withRegion( Regions.DEFAULT_REGION )
            .build();
        }
        awsAccessKeyCache = System.getProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY );
        awsSecretKeyCache = System.getProperty( S3Util.SECRET_KEY_SYSTEM_PROPERTY );
      } catch ( Exception ex ) {
        logger.error( "Could not get an S3Client", ex );
      }
    }
    return client;
  }

  private boolean hasClientChangedCredentials() {
    return client != null
      && ( S3Util.hasChanged( awsAccessKeyCache, System.getProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY ) )
      || S3Util.hasChanged( awsSecretKeyCache, System.getProperty( S3Util.SECRET_KEY_SYSTEM_PROPERTY ) ) );
  }

  protected boolean isRegionSet() {
    //region is set if explicitly set in env variable or configuration file is explicitly set
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

}
