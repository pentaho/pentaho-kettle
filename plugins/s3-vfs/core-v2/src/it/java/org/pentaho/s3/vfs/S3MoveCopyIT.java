package org.pentaho.s3.vfs;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.Selectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.pentaho.amazon.s3.provider.S3Provider;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.s3.vfs.support.KettleS3Environment;
import org.pentaho.s3.vfs.support.S3FaultProxy;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class S3MoveCopyIT {

  @RegisterExtension
  static final S3MockExtension S3_MOCK = S3MockExtension.builder()
    .withSecureConnection( false )
    .withInitialBuckets( KettleS3Environment.BUCKET )
    .silent()
    .build();

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
  void movesAFile() throws Exception {
    write( "move/source.txt", "moved content" );

    resolve( "move/source.txt" ).moveTo( resolve( "move/target.txt" ) );

    assertFalse( resolve( "move/source.txt" ).exists() );
    assertEquals( "moved content", read( resolve( "move/target.txt" ) ) );
  }

  @Test
  void movesAFolderWithItsChildren() throws Exception {
    write( "move-folder/from/a.txt", "a" );
    write( "move-folder/from/nested/b.txt", "b" );

    resolve( "move-folder/from" ).moveTo( resolve( "move-folder/to" ) );

    assertFalse( resolve( "move-folder/from" ).exists() );
    assertEquals( "a", read( resolve( "move-folder/to/a.txt" ) ) );
    assertEquals( "b", read( resolve( "move-folder/to/nested/b.txt" ) ) );
  }

  @Test
  void copiesWithinS3() throws Exception {
    write( "copy/source.txt", "copied content" );

    resolve( "copy/target.txt" ).copyFrom( resolve( "copy/source.txt" ), Selectors.SELECT_SELF );

    assertTrue( resolve( "copy/source.txt" ).exists() );
    assertEquals( "copied content", read( resolve( "copy/target.txt" ) ) );
  }

  @Test
  void copiesFromTheLocalFileSystem() throws Exception {
    Path local = Files.writeString( localDir.resolve( "local.txt" ), "from disk" );
    FileObject source = environment.vfs().getFileObject( local.toUri().toString() );

    resolve( "copy/uploaded.txt" ).copyFrom( source, Selectors.SELECT_SELF );

    assertEquals( "from disk", read( resolve( "copy/uploaded.txt" ) ) );
  }

  @Test
  @Timeout( 10 )
  void failedDirectS3CopyFallsBackToStreaming() throws Exception {
    assertCopyFallback( "direct-success", "s3", false );
  }

  @Test
  @Timeout( 10 )
  void failedPvfsCopyFallsBackToStreaming() throws Exception {
    assertCopyFallback( "pvfs-success", "pvfs", false );
  }

  @Test
  @Timeout( 10 )
  void failedDirectS3CopyAndUploadRetainBothErrors() throws Exception {
    assertCopyFallback( "direct-failure", "s3", true );
  }

  @Test
  @Timeout( 10 )
  void failedPvfsCopyAndUploadRetainBothErrors() throws Exception {
    assertCopyFallback( "pvfs-failure", "pvfs", true );
  }

  /**
   * Rejects server-side copy for a direct S3 or PVFS source, then verifies streaming recovery or,
   * when the fallback upload also fails, preservation of both errors without publishing partial content.
   */
  private void assertCopyFallback( String prefix, String sourceScheme, boolean failUpload ) throws Exception {
    String sourceKey = "copy-fallback/" + prefix + "/source.bin";
    String destinationKey = "copy-fallback/" + prefix + "/destination.bin";
    byte[] content = content();
    try ( FileObject original = resolve( sourceKey );
          OutputStream out = original.getContent().getOutputStream() ) {
      out.write( content );
    }

    CopyFault fault = new CopyFault( failUpload );
    try ( S3FaultProxy proxy = new S3FaultProxy( S3_MOCK.getServiceEndpoint(), fault ) ) {
      KettleS3Environment proxied = new KettleS3Environment( proxy.endpoint() );
      String proxiedBucket = proxied.createDefaultConnection( KettleS3Environment.BUCKET );
      String sourceUri = getSourceUri( sourceScheme, sourceKey );
      try ( FileObject source = proxied.vfs().getFileObject( sourceUri,
              new Variables(), new S3Provider().getOpts( proxied.details() ) );
            FileObject destination = proxied.vfs().getFileObject( proxiedBucket + "/" + destinationKey ) ) {
        assertEquals( sourceScheme, source.getName().getScheme() );
        if ( failUpload ) {
          assertCopyAndUploadErrors( source, destination );
          assertTrue( fault.uploadFailures() > 0, "fallback upload must reach the fault endpoint" );
        } else {
          destination.copyFrom( source, Selectors.SELECT_SELF );
        }
      }
      assertTrue( fault.copyFailures() > 0, "must attempt server-side copy even for PVFS sources" );
      assertTrue( fault.downloads() > 0, "fallback must stream the source through GET" );
    }

    // Verify independently of the fault endpoint, including that the source was not damaged.
    try ( FileObject source = resolve( sourceKey ); FileObject destination = resolve( destinationKey ) ) {
      assertContent( source, content );
      if ( failUpload ) {
        assertFalse( destination.exists(), "failed fallback must not publish a partial object" );
      } else {
        assertContent( destination, content );
      }
    }
  }

  private String getSourceUri( String sourceScheme, String sourceKey ) {
    return sourceScheme + "://"
      + ( "pvfs".equals( sourceScheme ) ? KettleS3Environment.DEFAULT_CONNECTION + "/" : "" )
      + KettleS3Environment.BUCKET + "/" + sourceKey;
  }

  private void assertCopyAndUploadErrors( FileObject source, FileObject destination ) {
    FileSystemException failure = assertThrows( FileSystemException.class,
      () -> destination.copyFrom( source, Selectors.SELECT_SELF ) );
    Throwable uploadFailure = assertInstanceOf( FileSystemException.class, failure.getCause() );
    assertServiceError( uploadFailure, "Injected upload failure" );
    assertEquals( 1, uploadFailure.getSuppressed().length, "original copy error must be suppressed" );
    assertServiceError( uploadFailure.getSuppressed()[ 0 ], "Injected copy failure" );
  }

  private void assertServiceError( Throwable failure, String message ) {
    Throwable cause = failure;
    while ( cause.getCause() != null ) {
      cause = cause.getCause();
    }
    S3Exception serviceError = assertInstanceOf( S3Exception.class, cause );
    assertEquals( 403, serviceError.statusCode() );
    assertEquals( message, serviceError.awsErrorDetails().errorMessage() );
  }

  private void assertContent( FileObject file, byte[] expected ) throws Exception {
    assertEquals( expected.length, file.getContent().getSize() );
    try ( InputStream in = file.getContent().getInputStream() ) {
      assertArrayEquals( expected, in.readAllBytes() );
    }
  }

  private static byte[] content() {
    ByteBuffer buffer = ByteBuffer.allocate( 256 * 1024 );
    while ( buffer.hasRemaining() ) {
      buffer.putInt( buffer.position() );
    }
    return buffer.array();
  }

  private void write( String path, String content ) throws Exception {
    try ( OutputStream out = resolve( path ).getContent().getOutputStream() ) {
      out.write( content.getBytes( StandardCharsets.UTF_8 ) );
    }
  }

  private String read( FileObject file ) throws Exception {
    try ( InputStream in = file.getContent().getInputStream() ) {
      return new String( in.readAllBytes(), StandardCharsets.UTF_8 );
    }
  }

  private FileObject resolve( String path ) throws Exception {
    return environment.vfs().getFileObject( bucketUri + "/" + path );
  }

  private static final class CopyFault implements S3FaultProxy.Fault {

    private final boolean failUpload;
    private final AtomicInteger copyFailures = new AtomicInteger();
    private final AtomicInteger uploadFailures = new AtomicInteger();
    private final AtomicInteger downloads = new AtomicInteger();

    private CopyFault( boolean failUpload ) {
      this.failUpload = failUpload;
    }

    @Override
    public Optional<S3FaultProxy.Failure> reject( HttpExchange exchange ) {
      if ( "PUT".equals( exchange.getRequestMethod() ) ) {
        if ( exchange.getRequestHeaders().containsKey( "x-amz-copy-source" ) ) {
          copyFailures.incrementAndGet();
          return Optional.of( new S3FaultProxy.Failure( 403, "AccessDenied", "copy" ) );
        }
        if ( failUpload ) {
          uploadFailures.incrementAndGet();
          return Optional.of( new S3FaultProxy.Failure( 403, "AccessDenied", "upload" ) );
        }
      }
      return Optional.empty();
    }

    @Override
    public void forwarded( HttpExchange exchange, int status ) {
      if ( "GET".equals( exchange.getRequestMethod() ) && exchange.getRequestURI().getRawQuery() == null
        && status / 100 == 2 ) {
        downloads.incrementAndGet();
      }
    }

    private int copyFailures() {
      return copyFailures.get();
    }

    private int uploadFailures() {
      return uploadFailures.get();
    }

    private int downloads() {
      return downloads.get();
    }
  }
}
