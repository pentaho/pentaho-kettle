package org.pentaho.s3.vfs;

import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.BufferedSplittableAsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.utils.CancellableOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.commons.vfs2.FileSystemException;

/**
 * Thin adapter over the AWS SDK's streaming async request body.
 */
public class S3SdkOutputStream extends OutputStream {

  private final S3FileSystem fileSystem;
  private final String bucketId;
  private final String key;

  private boolean closed = false;
  private boolean started = false;
  /** The upload could not start, so nothing may start another: not further writes, not closing. */
  private boolean failedToStart = false;
  private CompletableFuture<PutObjectResponse> uploadFuture;
  private CancellableOutputStream sdkOutputStream;

  public S3SdkOutputStream( S3FileSystem fileSystem, String bucketId, String key ) {
    this.fileSystem = fileSystem;
    this.bucketId = bucketId;
    this.key = key;
  }

  @Override
  public void write( int b ) throws IOException {
    ensureStarted();
    sdkOutputStream.write( b );
  }

  @Override
  public void write( byte[] b, int off, int len ) throws IOException {
    if ( b == null ) {
      throw new NullPointerException( "byte[] cannot be null" );
    }
    if ( off < 0 || len < 0 || off + len > b.length ) {
      throw new IndexOutOfBoundsException( "Invalid write range" );
    }

    ensureStarted();
    sdkOutputStream.write( b, off, len );
  }

  @Override
  public void flush() throws IOException {
    if ( closed ) {
      throw new IOException( "Output stream is closed" );
    }
    if ( sdkOutputStream != null ) {
      sdkOutputStream.flush();
    }
  }

  @Override
  public void close() throws IOException {
    if ( closed ) {
      return;
    }
    closed = true;
    if ( failedToStart ) {
      // already reported by the write that hit it; an empty object would replace the target
      return;
    }

    try {
      if ( !started ) {
        putObject( AsyncRequestBody.empty() );
      } else {
        sdkOutputStream.close();
      }
      awaitCompletion();
    } catch ( IOException e ) {
      cancelStream();
      throw e;
    } catch ( RuntimeException e ) {
      cancelStream();
      throw new IOException( "Failed to close S3 output stream", e );
    }
  }

  private void ensureStarted() throws IOException {
    if ( closed ) {
      throw new IOException( "Output stream is closed" );
    }
    if ( failedToStart ) {
      throw new IOException( "S3 upload failed to start" );
    }
    if ( !started ) {
      startUpload();
    }
  }

  private void startUpload() throws IOException {
    if ( started ) {
      return;
    }
    var requestBody = AsyncRequestBody.forBlockingOutputStream( null );
    try {
      putObject( BufferedSplittableAsyncRequestBody.create( requestBody ) );
      sdkOutputStream = requestBody.outputStream();
      started = true;
    } catch ( Exception e ) {
      failedToStart = true;
      // the request may already be out, waiting for a body that will never come
      if ( uploadFuture != null ) {
        uploadFuture.cancel( true );
      }
      throw new IOException( "Failed to start S3 upload", e );
    }
  }

  private void putObject( AsyncRequestBody requestBody ) throws FileSystemException {
    S3AsyncClient s3AsyncClient = fileSystem.getS3AsyncClient();
    uploadFuture = s3AsyncClient.putObject( PutObjectRequest.builder()
      .bucket( bucketId )
      .key( key )
      .build(), requestBody );
  }

  private void awaitCompletion() throws IOException {
    if ( uploadFuture == null ) {
      return;
    }

    try {
      uploadFuture.get();
    } catch ( InterruptedException e ) {
      Thread.currentThread().interrupt();
      cancelStream();
      throw new IOException( "Interrupted while uploading to S3", e );
    } catch ( ExecutionException e ) {
      Throwable cause = e.getCause();
      if ( cause instanceof IOException ioe ) {
        throw ioe;
      }
      throw new IOException( "S3 upload failed", cause );
    } catch ( CancellationException e ) {
      throw new IOException( "S3 upload was cancelled", e );
    }
  }

  private void cancelStream() {
    if ( sdkOutputStream != null ) {
      sdkOutputStream.cancel();
    }
  }
}
