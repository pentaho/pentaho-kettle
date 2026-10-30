package org.pentaho.s3.vfs.upload;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.Selectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.pentaho.s3.vfs.support.KettleS3Environment;
import org.pentaho.s3.vfs.support.S3FaultProxy;
import org.pentaho.s3.vfs.support.UploadPartFault;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Transfers into S3 whose size crosses the threshold the client splits them at: uploads, where the
 * bytes come from outside S3, and server-side copies, where they never leave it.
 * <p>
 * Apart from the other tests because only these need sizes that large, and every other test would
 * pay for them.
 */
public class S3UploadIT {

  /** Past the 8MiB the client splits at, so the transfer is made of several parts. */
  private static final int MULTIPART_SIZE = 12 * 1024 * 1024;

  @RegisterExtension
  static final S3MockExtension S3_MOCK = S3MockExtension.builder()
    .withSecureConnection( false )
    .withInitialBuckets( KettleS3Environment.BUCKET )
    .silent()
    .build();
  // Adding .withProperty( "server.shutdown", "graceful" ) would silence the "Socket accept failed"
  // logs at the end of each class, but it makes every class wait for the connector to drain, which
  // can slow tests down

  private static KettleS3Environment environment;
  private static String bucketUri;

  @TempDir
  Path localDir;

  @BeforeAll
  static void startEnvironment() throws Exception {
    environment = new KettleS3Environment( S3_MOCK.getServiceEndpoint() );
    bucketUri = environment.createDefaultConnection( KettleS3Environment.BUCKET );
  }

  @Test
  void uploadsAFileSmallEnoughForASingleRequest() throws Exception {
    byte[] content = contentOf( 1024 );

    upload( localFile( "small.bin", content ), "upload/small.bin" );

    assertStored( "upload/small.bin", content );
  }

  @Test
  void uploadsAFileBigEnoughToBeSplitIntoParts() throws Exception {
    byte[] content = contentOf( MULTIPART_SIZE );

    upload( localFile( "big.bin", content ), "upload/big.bin" );

    assertStored( "upload/big.bin", content );
  }

  @Test
  @Timeout( 60 )
  void retriesATransientPartFailureWithoutLosingStreamContent() throws Exception {
    byte[] content = contentOf( MULTIPART_SIZE );
    String path = "upload/retried-stream.bin";

    UploadPartFault fault = new UploadPartFault( false );
    try ( S3FaultProxy proxy = new S3FaultProxy( S3_MOCK.getServiceEndpoint(), fault ) ) {
      KettleS3Environment proxied = new KettleS3Environment( proxy.endpoint() );
      String uri = proxied.createDefaultConnection( KettleS3Environment.BUCKET ) + "/" + path;
      try ( FileObject destination = proxied.vfs().getFileObject( uri );
            OutputStream out = destination.getContent().getOutputStream() ) {
        for ( int offset = 0; offset < content.length; offset += 64 * 1024 ) {
          out.write( content, offset, Math.min( 64 * 1024, content.length - offset ) );
        }
      }

      assertEquals( 1, fault.failures(), "must exercise a real UploadPart failure" );
      assertTrue( fault.successfulRetries() >= 1, "the same upload ID and part must succeed on retry" );
      // Read directly from S3Mock, independently of the fault-injection endpoint.
      assertStored( path, content );
    }
  }

  @Test
  @Timeout( 60 )
  void persistentPartFailureDoesNotPublishANewObject() throws Exception {
    String path = "upload/failed-new-stream.bin";
    assertFalse( resolve( path ).exists() );

    assertPersistentStreamFailure( path );

    FileObject stored = resolve( path );
    stored.refresh();
    assertFalse( stored.exists(), "failed multipart upload must not publish an object" );
  }

  private void assertPersistentStreamFailure( String path ) throws Exception {
    byte[] content = contentOf( MULTIPART_SIZE );
    UploadPartFault fault = new UploadPartFault( true );
    try ( S3FaultProxy proxy = new S3FaultProxy( S3_MOCK.getServiceEndpoint(), fault ) ) {
      KettleS3Environment proxied = new KettleS3Environment( proxy.endpoint() );
      String uri = proxied.createDefaultConnection( KettleS3Environment.BUCKET ) + "/" + path;
      try ( FileObject destination = proxied.vfs().getFileObject( uri );
            OutputStream out = destination.getContent().getOutputStream() ) {
        for ( int offset = 0; offset < content.length; offset += 64 * 1024 ) {
          out.write( content, offset, Math.min( 64 * 1024, content.length - offset ) );
        }
        IOException failure = assertThrows( IOException.class, out::close,
          "retry exhaustion must not be reported as a successful close" );
        Throwable cause = failure;
        while ( !( cause instanceof S3Exception ) && cause.getCause() != null ) {
          cause = cause.getCause();
        }
        assertTrue( cause instanceof S3Exception, "failure must retain the S3 service error: " + failure );
        assertEquals( 503, ( (S3Exception) cause ).statusCode() );
      }
      assertTrue( fault.mostFailuresForOnePart() >= 2, "must retry the same part before giving up" );
      assertEquals( 0, fault.successfulRetries(), "no rejected part may succeed" );
      assertTrue( fault.awaitAbort(), "SDK must finish aborting before the fault endpoint shuts down" );
    }
  }

  /**
   * The same size as the upload above, but between two keys, so the client copies it part by part
   * without the bytes ever passing through here. Reading the destination back is the only way to
   * tell the parts were put together in the right order.
   */
  @Test
  void copiesAFileBigEnoughToBeSplitIntoParts() throws Exception {
    byte[] content = contentOf( MULTIPART_SIZE );
    upload( localFile( "to-copy.bin", content ), "copy/source.bin" );

    copy( "copy/source.bin", "copy/destination.bin" );

    assertStored( "copy/destination.bin", content );
  }

  /**
   * A source of no length reaches the client the same way one of unknown length does, so this
   * covers both: the size is not declared and the content has to speak for itself.
   */
  @Test
  void uploadsAnEmptyFile() throws Exception {
    upload( localFile( "empty.bin", new byte[ 0 ] ), "upload/empty.bin" );

    assertStored( "upload/empty.bin", new byte[ 0 ] );
  }

  @Test
  void replacesAnObjectThatIsAlreadyThere() throws Exception {
    upload( localFile( "first.bin", bytes( "first" ) ), "upload/twice.bin" );

    upload( localFile( "second.bin", bytes( "second" ) ), "upload/twice.bin" );

    assertStored( "upload/twice.bin", bytes( "second" ) );
  }

  private void upload( FileObject src, String path ) throws Exception {
    resolve( path ).copyFrom( src, Selectors.SELECT_SELF );
  }

  /** Both ends are in S3, which is what sends this down the server-side copy path. */
  private void copy( String srcPath, String dstPath ) throws Exception {
    resolve( dstPath ).copyFrom( resolve( srcPath ), Selectors.SELECT_SELF );
  }

  private FileObject localFile( String name, byte[] content ) throws Exception {
    Path local = Files.write( localDir.resolve( name ), content );
    return environment.vfs().getFileObject( local.toUri().toString() );
  }

  private void assertStored( String path, byte[] expected ) throws Exception {
    FileObject stored = resolve( path );
    assertTrue( stored.exists(), "was not stored" );
    assertEquals( expected.length, stored.getContent().getSize() );
    verifyContent( stored, expected );
  }

  /**
   * Reads the object back and holds it against what was sent, a block at a time so a large transfer
   * is never in memory twice.
   */
  private void verifyContent( FileObject stored, byte[] expected ) throws Exception {
    try ( InputStream in = stored.getContent().getInputStream() ) {
      byte[] block = new byte[ 64 * 1024 ];
      int offset = 0;
      for ( int read = in.read( block ); read > 0; read = in.read( block ) ) {
        assertTrue( offset + read <= expected.length, "more content came back than was sent" );
        int mismatch = Arrays.mismatch( block, 0, read, expected, offset, offset + read );
        if ( mismatch >= 0 ) {
          fail( "content differs at offset " + ( offset + mismatch ) );
        }
        offset += read;
      }
      assertEquals( expected.length, offset, "less content came back than was sent" );
    }
  }

  private FileObject resolve( String path ) throws Exception {
    return environment.vfs().getFileObject( bucketUri + "/" + path );
  }

  /**
   * Content in which every word states the offset it belongs at, so a part that arrives out of
   * order, twice or not at all cannot pass unnoticed, and the index a comparison fails at says
   * where the bytes came from.
   */
  private static byte[] contentOf( int size ) {
    byte[] content = new byte[ size ];
    ByteBuffer offsets = ByteBuffer.wrap( content );
    while ( offsets.remaining() >= Long.BYTES ) {
      offsets.putLong( offsets.position() );
    }
    return content;
  }

  private static byte[] bytes( String content ) {
    return content.getBytes( StandardCharsets.UTF_8 );
  }
}
