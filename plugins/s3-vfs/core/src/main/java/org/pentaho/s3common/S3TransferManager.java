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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.Copy;

public class S3TransferManager {

  private static final Class<?> PKG = S3TransferManager.class;
  private static final Logger logger = LoggerFactory.getLogger( PKG );

  private final TransferManager transferManager;

  public S3TransferManager( TransferManager transferManager ) {
    this.transferManager = transferManager;
  }

  public TransferManager getTransferManager() {
    return transferManager;
  }

  /**
   * Perform S3->S3 multipart copy with a custom thread pool size.
   *
   * @param src           Source S3FileObject
   * @param dst           Destination S3FileObject
   * @param logger        Logger for logging
   * @throws FileSystemException if copy fails
   */
  public void copy( S3CommonFileObject src, S3CommonFileObject dst ) throws FileSystemException {
    if ( src == null || dst == null
      || src.bucketName == null || src.key == null
      || dst.bucketName == null || dst.key == null ) {
      throw new FileSystemException( "vfs.provider.s3/transfer.null-argument",
        src != null ? src.getQualifiedName() : "null",
        dst != null ? dst.getQualifiedName() : "null" );
    }
    try {
      testConnection( src, dst );
      Copy copy = getTransferManager().copy(
        src.bucketName, src.key,
        dst.bucketName, dst.key
      );
      copy.waitForCompletion();
      logger.info( "S3->S3 server-side copy succeeded: {} -> {}",
                   src.getQualifiedName(), dst.getQualifiedName() );
    } catch ( InterruptedException ie ) {
      Thread.currentThread().interrupt();
      throw new FileSystemException( "vfs.provider.s3/transfer.interrupted",
                                     src.getQualifiedName(), dst.getQualifiedName(), ie );
    } catch ( AmazonClientException e ) {
      throw new FileSystemException( "vfs.provider.s3/transfer.error",
                                     src.getQualifiedName(), dst.getQualifiedName(), e );
    }
  }

  /**
   * Checks if the S3 client has read access to the source object and write access to the destination bucket.
   * Throws FileSystemException if access is denied.
   */
  private void testConnection( S3CommonFileObject src, S3CommonFileObject dst ) throws FileSystemException {
    // Check read access to source object
    try {
      getTransferManager().getAmazonS3Client().getObjectMetadata( src.bucketName, src.key );
      logger.debug( "Read access to source object: {}", src.getQualifiedName() );
    } catch ( AmazonClientException e ) {
      throw new FileSystemException( "vfs.provider.s3/transfer.no-read-access", src.getQualifiedName(), e );
    }

    // Check write access to destination bucket (try to put a zero-byte object and delete it)
    String testKey = dst.key + ".acltest-" + System.currentTimeMillis();
    try {
      getTransferManager().getAmazonS3Client().putObject( dst.bucketName, testKey, "" );
      getTransferManager().getAmazonS3Client().deleteObject( dst.bucketName, testKey );
      logger.debug( "Write access to destination bucket: {}", dst.bucketName );
    } catch ( AmazonClientException e ) {
      throw new FileSystemException( "vfs.provider.s3/transfer.no-write-access", dst.bucketName, e );
    }
  }

  public void upload( FileObject src, S3CommonFileObject dst ) throws FileSystemException {
    if ( src == null || dst == null
      || dst.bucketName == null || dst.key == null ) {
      throw new FileSystemException( "vfs.provider.s3/transfer.null-argument",
        src != null ? src.getName().getURI() : "null",
        dst != null ? dst.getQualifiedName() : "null" );
    }
    try ( InputStream in = src.getContent().getInputStream() ) {
      String bucket = dst.bucketName;
      String key = dst.key;
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength( src.getContent().getSize() );
      TransferManager tm = getTransferManager();
      tm.upload( bucket, key, in, metadata ).waitForUploadResult();
    } catch ( InterruptedException e ) {
      Thread.currentThread().interrupt();
      throw new FileSystemException( "vfs.provider.s3/transfer.interrupted", e );
    } catch ( AmazonClientException e ) {
      throw new FileSystemException( "vfs.provider.s3/transfer.error", e );
    } catch ( IOException e ) {
      throw new FileSystemException( "vfs.provider.s3/transfer.close-error", e );
    }
  }
}
