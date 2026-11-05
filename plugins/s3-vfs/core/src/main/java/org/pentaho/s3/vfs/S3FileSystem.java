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

package org.pentaho.s3.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.util.StorageUnitConverter;
import org.pentaho.di.core.vfs.configuration.KettleGenericFileSystemConfigBuilder;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.s3.vfs.client.Behavior;
import org.pentaho.s3.vfs.client.S3ClientFactory;
import org.pentaho.s3.vfs.client.S3ClientManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.BlockingInputStreamAsyncRequestBody;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class S3FileSystem extends AbstractFileSystem {

  private static final Class<?> PKG = S3FileSystem.class;
  private static final Logger logger = LoggerFactory.getLogger( PKG );

  // S3 part size constants
  /**
   * Minimum part size specified in documentation
   * see https://docs.aws.amazon.com/AmazonS3/latest/dev/qfacts.html
   */
  private static final String MIN_PART_SIZE = "5MB";
  private static final String MAX_PART_SIZE = "5GB";
  private static final StorageUnitConverter STORAGE_UNIT_CONVERTER = new StorageUnitConverter();
  private static final long MIN_PART_SIZE_BYTES;
  private static final long MAX_PART_SIZE_BYTES;

  static {
    MIN_PART_SIZE_BYTES = STORAGE_UNIT_CONVERTER.displaySizeToByteCount( MIN_PART_SIZE );
    MAX_PART_SIZE_BYTES = STORAGE_UNIT_CONVERTER.displaySizeToByteCount( MAX_PART_SIZE );
  }

  private final Behavior behavior = new Behavior();

  private final S3ClientManager clientManager;

  private final Supplier<ConnectionManager> connectionManager = ConnectionManager::getInstance;

  // Helpers and utilities
  protected S3KettleProperty s3KettleProperty;

  private final boolean legacyS3UrisEnabled;
  private final Optional<S3LegacyUriSupport> legacyUris;

  protected S3FileSystem( final FileName rootName, final FileSystemOptions fileSystemOptions ) {
    super( rootName, null, fileSystemOptions );
    this.s3KettleProperty = new S3KettleProperty();
    this.legacyS3UrisEnabled = S3LegacyUriSupport.isEnabled( rootName );
    this.legacyUris = legacyS3UrisEnabled ? Optional.of( new S3LegacyUriSupport( this ) ) : Optional.empty();
    this.clientManager = new S3ClientManager( new S3ClientFactory( behavior, getBowl() ), behavior,
      getFileSystemOptions(), getRootURI(), connectionManager, this::clearFilesCache );
  }

  /** The bowl travels in the file system options; anything outside a project context lacks one. */
  private Bowl getBowl() {
    return Optional.ofNullable(
      KettleGenericFileSystemConfigBuilder.getInstance().getBowl( getFileSystemOptions() ) )
      .orElseGet( DefaultBowl::getInstance );
  }

  /**
   * Called by the client manager when the credentials it built a client from are no longer current,
   * so anything cached under them has to go.
   */
  private void clearFilesCache() {
    getFileSystemManager().getFilesCache().clear( this );
    if ( legacyS3UrisEnabled ) {
      getLegacyUriSupport().clearCache();
    }
  }

  boolean isLegacyS3UrisEnabled() {
    return legacyS3UrisEnabled;
  }

  /** Only to be called when {@link #isLegacyS3UrisEnabled()}. */
  S3LegacyUriSupport getLegacyUriSupport() {
    return legacyUris.orElseThrow(
      () -> new IllegalStateException( "Legacy S3 URI support is not enabled" ) );
  }

  @Override
  protected void addCapabilities( Collection<Capability> caps ) {
    caps.addAll( S3FileProvider.capabilities );
  }

  protected FileObject createFile( AbstractFileName name ) {
    return new S3FileObject( name, this );
  }

  public S3Client getS3Client() throws FileSystemException {
    return clientManager.getClient();
  }

  public S3AsyncClient getS3AsyncClient() throws FileSystemException {
    return clientManager.getAsyncClient();
  }

  /** Called when the file system is closed, and when VFS frees unused resources. */
  @Override
  protected void doCloseCommunicationLink() {
    clientManager.closeClients();
  }

  public long getPartSize() {
    return parsePartSize( s3KettleProperty.getPartSize() );
  }

  protected long parsePartSize( String partSizeString ) {
    long parsePartSize = convertToLong( partSizeString );
    if ( parsePartSize < MIN_PART_SIZE_BYTES ) {
      // an unset property is not a mistake worth reporting, only a value that made no sense
      if ( StringUtils.isNotBlank( partSizeString ) && logger.isWarnEnabled() ) {
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
    return STORAGE_UNIT_CONVERTER.displaySizeToByteCount( partSize );
  }

  /**
   * Uploads the content of the specified FileObject to the specified S3FileObject.
   * <p>
   * Goes through the async client, which splits anything past its multipart threshold into parts
   * uploaded in parallel. Smaller sources still cost a single request.
   *
   * @param src The source FileObject to upload from.
   * @param dst The destination S3FileObject to upload to.
   * @throws FileSystemException If an error occurs during the upload operation.
   */
  public void upload( FileObject src, S3FileObject dst ) throws FileSystemException {
    try {
      putObject( src, uploadRequest( dst ) );
    } catch ( IOException | CompletionException | SdkException e ) {
      throw new FileSystemException( "vfs.provider/copy-file.error", e, src, dst );
    }
  }

  /** Decided before the source is opened, so a legacy location never needs the content replayed. */
  private PutObjectRequest uploadRequest( S3FileObject dst ) throws FileSystemException {
    if ( legacyS3UrisEnabled ) {
      var destination = getLegacyUriSupport().forWrite( dst.bucketName, dst.key );
      return PutObjectRequest.builder().bucket( destination.bucket() ).key( destination.key() ).build();
    }
    return PutObjectRequest.builder().bucket( dst.bucketName ).key( dst.key ).build();
  }

  private void putObject( FileObject src, PutObjectRequest request ) throws IOException {
    BlockingInputStreamAsyncRequestBody body =
      AsyncRequestBody.forBlockingInputStream( contentLength( src ).orElse( null ) );

    try ( InputStream in = src.getContent().getInputStream() ) {
      CompletableFuture<PutObjectResponse> upload = getS3AsyncClient().putObject( request, body );
      body.writeInputStream( in );
      upload.join();
    }
  }

  /**
   * The length to declare for an upload, empty when the source cannot be trusted to report it.
   */
  private static Optional<Long> contentLength( FileObject src ) {
    try {
      long size = src.getContent().getSize();
      return size > 0 ? Optional.of( size ) : Optional.empty();
    } catch ( FileSystemException e ) {
      logger.warn( "Could not determine the size of {}, uploading without declaring a length",
        src.getName().getFriendlyURI(), e );
      return Optional.empty();
    }
  }

  /**
   * Copies the content of the specified S3FileObject to another S3FileObject, server-side.
   * <p>
   * Goes through the async client, which turns anything past its multipart threshold into a series
   * of part copies issued in parallel. That also lifts the 5GB ceiling a single copyObject has: the
   * bytes never leave S3, whatever the size.
   *
   * @param src  The source S3FileObject to copy from.
   * @param dest The destination S3FileObject to copy to.
   * @throws FileSystemException If an error occurs during the copy operation.
   */
  public void copy( S3FileObject src, S3FileObject dest ) throws FileSystemException {
    try {
      if ( legacyS3UrisEnabled && src.isLegacyS3UrisEnabled() && dest.isLegacyS3UrisEnabled() ) {
        getLegacyUriSupport().executeCopy( src.bucketName, src.key, dest.bucketName, dest.key, this::copyObject );
      } else {
        copyObject( src.bucketName, src.key, dest.bucketName, dest.key );
      }
    } catch ( CompletionException | SdkException e ) {
      throw new FileSystemException( "vfs.provider/copy-file.error", e, src.getQualifiedName(),
        dest.getQualifiedName() );
    }
  }

  private CopyObjectResponse copyObject( String sourceBucket, String sourceKey, String destinationBucket,
                                         String destinationKey ) throws FileSystemException {
    return getS3AsyncClient().copyObject( CopyObjectRequest.builder()
      .sourceBucket( sourceBucket ).sourceKey( sourceKey )
      .destinationBucket( destinationBucket ).destinationKey( destinationKey )
      .build() ).join();
  }

}
