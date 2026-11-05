package org.pentaho.s3.vfs;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.vfs2.FileSystemException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

/**
 * Some edge cases for the output stream, IT tests handle most functionality testing.
 */
class S3SdkOutputStreamTest {

  private S3FileSystem fileSystem;
  private S3AsyncClient client;

  @BeforeEach
  void setUp() {
    fileSystem = mock( S3FileSystem.class );
    client = mock( S3AsyncClient.class );
    doReturn( CompletableFuture.completedFuture( PutObjectResponse.builder().build() ) )
      .when( client ).putObject( any( PutObjectRequest.class ), any( AsyncRequestBody.class ) );
  }

  @Test
  void closingAfterAFailedStartUploadsNothing() throws Exception {
    givenTheFirstStartFails();
    try ( S3SdkOutputStream out = new S3SdkOutputStream( fileSystem, "bucket", "key" ) ) {
      assertThrows( IOException.class, () -> out.write( 'x' ) );
    }

    assertNothingUploaded();
  }

  @Test
  void writingAfterAFailedStartUploadsNothing() throws Exception {
    givenTheFirstStartFails();
    try ( S3SdkOutputStream out = new S3SdkOutputStream( fileSystem, "bucket", "key" ) ) {
      assertThrows( IOException.class, () -> out.write( 'x' ) );
      assertThrows( IOException.class, () -> out.write( 'y' ) );
    }

    assertNothingUploaded();
  }

  /** Opening and closing without writing still creates an empty file. */
  @Test
  void closingWithoutWritingUploadsAnEmptyObject() throws Exception {
    when( fileSystem.getS3AsyncClient() ).thenReturn( client );

    try ( S3SdkOutputStream out = new S3SdkOutputStream( fileSystem, "bucket", "key" ) ) {
      // nothing written
    }

    verify( client ).putObject( any( PutObjectRequest.class ), any( AsyncRequestBody.class ) );
  }

  /** A transient failure: had the stream tried again, the next attempt would have gone through. */
  private void givenTheFirstStartFails() throws FileSystemException {
    when( fileSystem.getS3AsyncClient() )
      .thenThrow( new FileSystemException( "Could not build the asynchronous S3 client" ) )
      .thenReturn( client );
  }

  private void assertNothingUploaded() {
    verify( client, never() ).putObject( any( PutObjectRequest.class ), any( AsyncRequestBody.class ) );
  }
}
