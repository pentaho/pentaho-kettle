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


package org.pentaho.amazon.s3.provider;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListBucketsPaginatedRequest;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.pentaho.amazon.s3.S3Details;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.BaseVFSConnectionProvider;
import org.pentaho.di.connections.vfs.VFSRoot;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.s3.vfs.S3FileProvider;
import org.pentaho.s3common.S3CommonFileSystem;
import org.pentaho.s3common.S3CommonFileSystemConfigBuilder;
import org.pentaho.s3common.S3Options;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;

/**
 * Created by bmorrise on 2/5/19.
 */
@SuppressWarnings( "WeakerAccess" )
public class S3Provider extends BaseVFSConnectionProvider<S3Details> {

  public static final String ACCESS_KEY_SECRET_KEY = "0";
  public static final String CREDENTIALS_FILE = "1";
  public static final String NAME = "Amazon S3/Minio/HCP";
  private final Supplier<ConnectionManager> connectionManagerSupplier = ConnectionManager::getInstance;

  private final LogChannelInterface log = new LogChannel( this );

  @Override
  public Class<S3Details> getClassType() {
    return S3Details.class;
  }

  @Override
  public FileSystemOptions getOpts( S3Details s3Details ) {
    VariableSpace space = getSpace( s3Details );
    S3CommonFileSystemConfigBuilder s3CommonFileSystemConfigBuilder =
      new S3CommonFileSystemConfigBuilder( new FileSystemOptions() );
    s3CommonFileSystemConfigBuilder.setName( getVar( s3Details.getName(), space ) );
    s3CommonFileSystemConfigBuilder.setAccessKey( getVar( s3Details.getAccessKey(), space ) );
    s3CommonFileSystemConfigBuilder.setSecretKey( getVar( s3Details.getSecretKey(), space ) );
    s3CommonFileSystemConfigBuilder.setSessionToken( getVar( s3Details.getSessionToken(), space ) );
    s3CommonFileSystemConfigBuilder.setRegion( getVar( s3Details.getRegion(), space ) );
    s3CommonFileSystemConfigBuilder.setCredentialsFile( getVar( s3Details.getCredentialsFilePath(), space ) );
    s3CommonFileSystemConfigBuilder.setProfileName( getVar( s3Details.getProfileName(), space ) );
    s3CommonFileSystemConfigBuilder.setEndpoint( getVar( s3Details.getEndpoint(), space ) );
    s3CommonFileSystemConfigBuilder.setPathStyleAccess( getVar( s3Details.getPathStyleAccess(), space ) );
    s3CommonFileSystemConfigBuilder.setSignatureVersion( getVar( s3Details.getSignatureVersion(), space ) );
    s3CommonFileSystemConfigBuilder.setDefaultS3Config( getVar( s3Details.getDefaultS3Config(), space ) );
    s3CommonFileSystemConfigBuilder.setConnectionType( getVar( s3Details.getConnectionType(), space ) );
    s3CommonFileSystemConfigBuilder.setTrustStoreFilePath( getVar( s3Details.getTrustStoreFilePath(), space ) );
    s3CommonFileSystemConfigBuilder.setTrustStorePassword( getVar( s3Details.getTrustStorePassword(), space ) );
    s3CommonFileSystemConfigBuilder.setTrustAll( getVar( s3Details.getTrustAll(), space ) );
    s3CommonFileSystemConfigBuilder.setKeyStoreFilePath( getVar( s3Details.getKeyStoreFilePath(), space ) );
    s3CommonFileSystemConfigBuilder.setKeyStorePassword( getVar( s3Details.getKeyStorePassword(), space ) );
    return s3CommonFileSystemConfigBuilder.getFileSystemOptions();
  }

  @SuppressWarnings( "unchecked" )
  @Override public List<S3Details> getConnectionDetails() {
    return (List<S3Details>) connectionManagerSupplier.get().getConnectionDetailsByScheme( getKey() );
  }

  /**
   * Get the list of buckets for a Vfs Connection
   * @param s3Details
   * @return
   * @throws FileSystemException - throws exception whenever there is any exception thrown while doing the listBuckets() call
   */
  @Override public List<VFSRoot> getLocations( S3Details s3Details ) throws FileSystemException {
    List<VFSRoot> buckets = new ArrayList<>();
    try {
      AmazonS3 s3 = getS3Client( s3Details );
      if ( s3 != null ) {
        for ( Bucket bucket : s3.listBuckets() ) {
          buckets.add( new VFSRoot( bucket.getName(), bucket.getCreationDate() ) );
        }
      }
    } catch ( Exception e ) {
      log.logError( "Exception getting the list of buckets for connection '" + s3Details.getName() + "'", e );
      throw new FileSystemException( "Exception getting the list of buckets ", e );
    }
    return buckets;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getKey() {
    return S3FileProvider.SCHEME;
  }

  @Override public String getProtocol( S3Details s3Details ) {
    return S3FileProvider.SCHEME;
  }

  @Override public boolean test( S3Details s3Details ) throws KettleException {
    VariableSpace space = getSpace( s3Details );
    s3Details = prepare( s3Details );
    AmazonS3 amazonS3 = getS3Client( s3Details );
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    try {
      // make sure that sax parsing classes are loaded from this cl.
      // TCCL may have been set to a bundle classloader.
      Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
      Objects.requireNonNull( amazonS3 ).listBuckets( new ListBucketsPaginatedRequest().withMaxBuckets( 1 ) );

      List<? extends ConnectionDetails> connections =
        connectionManagerSupplier.get().getConnectionDetailsByScheme( S3FileProvider.SCHEME );

      if ( getBooleanValueOfVariable( space, s3Details.getDefaultS3ConfigVariable(),
        s3Details.getDefaultS3Config() ) ) {
        for ( ConnectionDetails details : connections ) {
          if ( !s3Details.getName().equalsIgnoreCase( details.getName() ) ) {
            S3Details removeDefault = (S3Details) details;
            removeDefault.setAccessKey( Encr.decryptPasswordOptionallyEncrypted(
              getVar( removeDefault.getAccessKey(), space ) ) );
            removeDefault.setSecretKey( Encr.decryptPasswordOptionallyEncrypted(
              getVar( removeDefault.getSecretKey(), space ) ) );
            removeDefault.setSessionToken(
              Encr.decryptPasswordOptionallyEncrypted( getVar( removeDefault.getSessionToken(), space ) ) );
            removeDefault.setCredentialsFile(
              Encr.decryptPasswordOptionallyEncrypted( getVar( removeDefault.getCredentialsFile(), space ) ) );
            removeDefault.setDefaultS3Config( "false" );
            removeDefault.setDefaultS3ConfigVariable( null );
            connectionManagerSupplier.get().save( removeDefault );
          }
        }
      }
      return true;
    } catch ( AmazonS3Exception e ) {
      // expected exception if credentials are invalid.  Log just the message.
      log.logError( e.getMessage() );
      return false;
    } catch ( Exception e ) {
      // unexpected exception, log the whole stack
      log.logError( e.getMessage(), e );
      return false;
    } finally {
      Thread.currentThread().setContextClassLoader( cl );
    }
  }

  @Override public S3Details prepare( S3Details s3Details ) throws KettleException {
    VariableSpace space = getSpace( s3Details );
    if ( s3Details.getAuthType().equals( CREDENTIALS_FILE ) ) {
      String credentialsFilePath = getVar( s3Details.getCredentialsFilePath(), space );
      if ( credentialsFilePath != null ) {
        try ( BufferedReader reader = Files.newBufferedReader( Paths.get( credentialsFilePath ) ) ) {
          StringBuilder builder = new StringBuilder();
          String currentLine;
          while ( ( currentLine = reader.readLine() ) != null ) {
            builder.append( currentLine ).append( "\n" );
          }
          s3Details.setCredentialsFile( builder.toString() );
        } catch ( IOException e ) {
          throw new KettleException( "Could not read file ", e );
        }
      }
    }
    return s3Details;
  }

  @Override
  public FileObject getDirectFile( Bowl bowl, S3Details s3Conn, String path ) throws KettleFileException {
    if ( !S3FileProvider.SCHEME.equals( s3Conn.getType() ) ) {
      return null;
    }
    FileSystemOptions fsopts = getOpts( s3Conn );
    S3CommonFileSystemConfigBuilder builder = new S3CommonFileSystemConfigBuilder( fsopts );
    // Disable "Use Defaults" so we don't call back into ConnectionManager for the default S3 connection
    builder.setUseDefaults( false );
    fsopts = builder.getFileSystemOptions();

    String uri = S3FileProvider.SCHEME + "://" + path;
    // use an empty Variables to prevent other "connection" values from causing StackOverflowErrors
    return KettleVFS.getInstance( bowl ).getFileObject( uri, new Variables(), fsopts );
  }

  public AmazonS3 getS3Client( S3Details s3Details ) throws KettleException {
    var opts = getOpts( s3Details );
    var cfgBuilder = new S3CommonFileSystemConfigBuilder( opts );

    var defaultS3 = S3CommonFileSystem.getDefaultS3Connection( connectionManagerSupplier, cfgBuilder );
    if ( defaultS3.isPresent() && defaultS3.get() instanceof S3Details defS3 ) {
      cfgBuilder = new S3CommonFileSystemConfigBuilder( getOpts( defS3 ) );
    }

    var options = S3Options.from( cfgBuilder );
    if ( StringUtils.isNotEmpty( options.base().endpoint() ) ) {
      return S3CommonFileSystem.createClientFromEndpoint( options );
    } else {
      return AmazonS3ClientBuilder.standard().withCredentials( S3CommonFileSystem.createCredentialsProvider( options ) )
        .enableForceGlobalBucketAccess().withRegion( options.base().region() ).build();
    }


  }

}
