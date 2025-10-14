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

import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartRequest;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.StorageUnitConverter;
import org.pentaho.di.i18n.BaseMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Custom OutputStream that enables chunked uploads into S3
 */
public class S3CommonPipedOutputStream extends PipedOutputStream {

  private static final Class<?> PKG = S3CommonPipedOutputStream.class;
  private static final Logger logger = LoggerFactory.getLogger( S3CommonPipedOutputStream.class );
  private static final LogChannelInterface consoleLog = new LogChannel( BaseMessages.getString( PKG, "TITLE.S3File" ) );

  /**
   * set to aws multipart minimum 5MB.
   */
  private static final int DEFAULT_PART_SIZE = 5 * 1024 * 1024;
  private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool( 1 );
  private boolean initialized = false;
  private boolean blockedUntilDone = true;
  private PipedInputStream pipedInputStream;
  private S3AsyncTransferRunner s3AsyncTransferRunner;
  private S3CommonFileSystem fileSystem;
  private Future<Boolean> result = null;
  private String bucketId;
  private String key;
  /**
   * AWS Multipart part size.
   */
  private int partSize;

  public S3CommonPipedOutputStream( S3CommonFileSystem fileSystem, String bucketId, String key ) throws IOException {
    this( fileSystem, bucketId, key, DEFAULT_PART_SIZE );
  }

  public S3CommonPipedOutputStream( S3CommonFileSystem fileSystem, String bucketId, String key, int partSize ) throws IOException {
    this.pipedInputStream = new PipedInputStream();

    try {
      this.pipedInputStream.connect( this );
    } catch ( IOException e ) {
      // FATAL, unexpected
      throw new IOException( "could not connect to pipedInputStream", e );
    }

    this.s3AsyncTransferRunner = new S3AsyncTransferRunner();
    this.bucketId = bucketId;
    this.key = key;
    this.fileSystem = fileSystem;
    this.partSize = partSize;
  }

  private void initializeWrite() {
    if ( !initialized ) {
      initialized = true;
      result = this.executor.submit( s3AsyncTransferRunner );
    }
  }

  public boolean isBlockedUntilDone() {
    return blockedUntilDone;
  }

  public void setBlockedUntilDone( boolean blockedUntilDone ) {
    this.blockedUntilDone = blockedUntilDone;
  }

  @Override
  public void write( int b ) throws IOException {
    initializeWrite();
    super.write( b );
  }

  @Override
  public void write( byte[] b, int off, int len ) throws IOException {
    initializeWrite();
    super.write( b, off, len );
  }

  @Override
  public void close() throws IOException {
    super.close();

    if ( initialized && isBlockedUntilDone() ) {
      while ( !result.isDone() ) {
        try {
          Thread.sleep( 100 );
        } catch ( InterruptedException e ) {
          logger.error( BaseMessages.getString( PKG, "ERROR.S3MultiPart.ExceptionCaught" ), e );
          Thread.currentThread().interrupt();
        }
      }
    }
    this.executor.shutdown();
  }

  class S3AsyncTransferRunner implements Callable<Boolean> {

    public Boolean call() throws Exception {
      boolean returnVal = true;
      List<PartETag> partETags = new ArrayList<>();

      // Step 1: Initialize
      InitiateMultipartUploadRequest initRequest;
      initRequest = new InitiateMultipartUploadRequest( bucketId, key );

      InitiateMultipartUploadResult initResponse = null;

      // NOTE: byte[] max size is ~2GB < 5GB = aws api max part size
      try ( ByteArrayOutputStream baos = new ByteArrayOutputStream( partSize );
            BufferedInputStream bis = new BufferedInputStream( pipedInputStream, partSize ) ) {
        initResponse = fileSystem.getS3Client().initiateMultipartUpload( initRequest );
        // Step 2: Upload parts.
        byte[] tmpBuffer = new byte[ partSize ];
        int read = 0;
        long offset = 0;
        long totalRead = 0;
        int partNum = 1;

        S3CommonWindowedSubstream s3is;
        logger.info( BaseMessages.getString( PKG, "INFO.S3MultiPart.Start" ) );
        while ( ( read = bis.read( tmpBuffer ) ) >= 0 ) {

          // if something was actually read
          if ( read > 0 ) {
            baos.write( tmpBuffer, 0, read );
            totalRead += read;
          }

          if ( totalRead > partSize ) {
            s3is = new S3CommonWindowedSubstream( baos.toByteArray() );

            UploadPartRequest uploadRequest = new UploadPartRequest()
              .withBucketName( bucketId ).withKey( key )
              .withUploadId( initResponse.getUploadId() ).withPartNumber( partNum++ )
              .withFileOffset( offset )
              .withPartSize( totalRead )
              .withInputStream( s3is );

            // Upload part and add response to our list.
            logger.info( BaseMessages.getString( PKG, "INFO.S3MultiPart.Upload", partNum - 1, offset, Long.toString( totalRead ) ) );
            partETags.add( fileSystem.getS3Client().uploadPart( uploadRequest ).getPartETag() );

            offset += totalRead;
            totalRead = 0; // reset part size counter
            baos.reset(); // reset output stream to 0
          }
        }

        // Step 2.1 upload last part
        s3is = new S3CommonWindowedSubstream( baos.toByteArray() );

        UploadPartRequest uploadRequest = new UploadPartRequest()
          .withBucketName( bucketId ).withKey( key )
          .withUploadId( initResponse.getUploadId() ).withPartNumber( partNum++ )
          .withFileOffset( offset )
          .withPartSize( totalRead )
          .withInputStream( s3is )
          .withLastPart( true );

        logger.info( BaseMessages.getString( PKG, "INFO.S3MultiPart.Upload", partNum - 1, offset, totalRead ) );
        partETags.add( fileSystem.getS3Client().uploadPart( uploadRequest ).getPartETag() );

        // Step 3: Complete.
        logger.info( BaseMessages.getString( PKG, "INFO.S3MultiPart.Complete" ) );
        CompleteMultipartUploadRequest compRequest =
          new CompleteMultipartUploadRequest( bucketId, key, initResponse.getUploadId(), partETags );

        fileSystem.getS3Client().completeMultipartUpload( compRequest );
      } catch ( OutOfMemoryError oome ) {
        consoleLog.logError( BaseMessages.getString( PKG,
          "ERROR.S3MultiPart.UploadOutOfMemory", new StorageUnitConverter().byteCountToDisplaySize( partSize ) ),
          oome );
        returnVal = false;
      } catch ( Exception e ) {
        logger.error( BaseMessages.getString( PKG, "ERROR.S3MultiPart.ExceptionCaught" ), e );
        if ( initResponse == null ) {
          close();
        } else {
          fileSystem.getS3Client()
            .abortMultipartUpload( new AbortMultipartUploadRequest( bucketId, key, initResponse.getUploadId() ) );
          logger.error( BaseMessages.getString( PKG, "ERROR.S3MultiPart.Aborted" ) );
        }
        returnVal = false;
      }

      return returnVal;
    }
  }
}
