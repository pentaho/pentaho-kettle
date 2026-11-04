/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.amazon.s3.provider;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.pentaho.amazon.s3.S3Details;
import org.pentaho.amazon.s3.S3Util;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.BaseVFSConnectionProvider;
import org.pentaho.di.connections.vfs.VFSRoot;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.s3.vfs.S3FileProvider;
import org.pentaho.s3.vfs.S3FileSystemConfigBuilder;
import org.pentaho.s3.vfs.client.Behavior;
import org.pentaho.s3.vfs.client.S3ClientFactory;
import org.pentaho.s3.vfs.client.S3Options;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.vfs2.FileObject;

/**
 * PVFS provider for S3
 */
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
    S3FileSystemConfigBuilder configBuilder =
      new S3FileSystemConfigBuilder( new FileSystemOptions() );
    configBuilder.setName( getVar( s3Details.getName(), space ) );
    configBuilder.setAccessKey( getVar( s3Details.getAccessKey(), space ) );
    configBuilder.setSecretKey( getVar( s3Details.getSecretKey(), space ) );
    configBuilder.setSessionToken( getVar( s3Details.getSessionToken(), space ) );
    configBuilder.setRegion( getVar( s3Details.getRegion(), space ) );
    configBuilder.setCredentialsFile( getVar( s3Details.getCredentialsFilePath(), space ) );
    configBuilder.setProfileName( getVar( s3Details.getProfileName(), space ) );
    configBuilder.setEndpoint( getVar( s3Details.getEndpoint(), space ) );
    configBuilder.setPathStyleAccess( getVar( s3Details.getPathStyleAccess(), space ) );
    checkLegacySigVersion( getVar( s3Details.getSignatureVersion(), space ) );
    configBuilder.setDefaultS3Config( getVar( s3Details.getDefaultS3Config(), space ) );
    configBuilder.setConnectionType( getVar( s3Details.getConnectionType(), space ) );
    configBuilder.setTrustStoreFilePath( getVar( s3Details.getTrustStoreFilePath(), space ) );
    configBuilder.setTrustStorePassword( getVar( s3Details.getTrustStorePassword(), space ) );
    configBuilder.setTrustAll( getVar( s3Details.getTrustAll(), space ) );
    configBuilder.setKeyStoreFilePath( getVar( s3Details.getKeyStoreFilePath(), space ) );
    configBuilder.setKeyStorePassword( getVar( s3Details.getKeyStorePassword(), space ) );
    configBuilder.setUseDefaults( false ); // defaults only for direct s3 access
    return configBuilder.getFileSystemOptions();
  }

  private void checkLegacySigVersion( String sigVersion ) {
    if ( !StringUtils.isBlank( sigVersion ) && !Strings.CS.equals( sigVersion, S3Util.SIG_VERSION ) ) {
      log.logMinimal( BaseMessages.getString( S3Provider.class, "WARN.S3.SigVersion" ) );
    }
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
      S3Client s3 = getS3Client( s3Details );
      if ( s3 != null ) {
        for ( Bucket bucket : s3.listBuckets().buckets() ) {
          buckets.add( new VFSRoot( bucket.name(), Date.from( bucket.creationDate() ) ) );
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
    try {
      S3Client amazonS3 = getS3Client( s3Details );
      Objects.requireNonNull( amazonS3 ).listBuckets( ListBucketsRequest.builder().maxBuckets( 1 ).build() );

      if ( isDefaultConnection( s3Details, space ) ) {
        List<? extends ConnectionDetails> connections =
          connectionManagerSupplier.get().getConnectionDetailsByScheme( S3FileProvider.SCHEME );
        for ( ConnectionDetails details : connections ) {
          if ( !s3Details.getName().equalsIgnoreCase( details.getName() ) ) {
            S3Details removeDefault = (S3Details) details;
            if ( isDefaultConnection( removeDefault, space ) ) {
              unsetDefaultConnection( space, removeDefault );
            }
          }
        }
      }

      return true;
    } catch ( S3Exception e ) {
      // expected exception if credentials are invalid.  Log just the message.
      log.logError( e.getMessage() );
      return false;
    } catch ( Exception e ) {
      // unexpected exception, log the whole stack
      log.logError( e.getMessage(), e );
      return false;
    }
  }

  /** remove default connection status from connection */
  private void unsetDefaultConnection( VariableSpace space, S3Details removeDefault ) {
    // need to decrypt encrypted fields so they don't get double-encrypted on save
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

  private boolean isDefaultConnection( S3Details s3Details, VariableSpace space ) {
    return getBooleanValueOfVariable( space, s3Details.getDefaultS3ConfigVariable(),
      s3Details.getDefaultS3Config() );
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
    S3FileSystemConfigBuilder builder = new S3FileSystemConfigBuilder( fsopts );
    // Disable "Use Defaults" so we don't call back into ConnectionManager for the default S3 connection
    builder.setUseDefaults( false );
    fsopts = builder.getFileSystemOptions();

    String uri = S3FileProvider.SCHEME + "://" + path;
    // use an empty Variables to prevent other "connection" values from causing StackOverflowErrors
    return KettleVFS.getInstance( bowl ).getFileObject( uri, new Variables(), fsopts );
  }

  /** For callers with no bowl in hand, such as the connection provider methods. */
  public S3Client getS3Client( S3Details s3Details ) {
    return getS3Client( DefaultBowl.getInstance(), s3Details );
  }

  public S3Client getS3Client( Bowl bowl, S3Details s3Details ) {
    var opts = getOpts( s3Details );
    var cfgBuilder = new S3FileSystemConfigBuilder( opts );

    return new S3ClientFactory( new Behavior(), bowl ).create( S3Options.from( cfgBuilder ) );
  }

}
