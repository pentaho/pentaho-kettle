package org.pentaho.s3.vfs;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.pentaho.amazon.s3.provider.S3Provider;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.s3.vfs.support.KettleS3Environment;
import org.pentaho.s3.vfs.support.S3FaultProxy;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * URIs written for the old driver, where the real bucket is the first element of the key, under
 * {@link Const#KETTLE_COMPATIBILITY_ALLOW_S3_LEGACY_URI}.
 */
public class S3LegacyUriIT {

  @RegisterExtension
  static final S3MockExtension S3_MOCK = S3MockExtension.builder()
    .withSecureConnection( false )
    .withInitialBuckets( KettleS3Environment.BUCKET )
    .silent()
    .build();

  private static final String LEGACY_NOT_BUCKET = "s3";
  private static final String LEGACY_ROOT = "s3://" + LEGACY_NOT_BUCKET + "/" + KettleS3Environment.BUCKET + "/";
  private static final String DIRECT_ROOT = "s3://" + KettleS3Environment.BUCKET + "/";

  private final BucketProbes probes = new BucketProbes();
  private S3FaultProxy proxy;
  private KettleS3Environment environment;
  /** Unique per test, so every test gets file systems that read the flag afresh. */
  private FileSystemOptions options;

  @TempDir
  Path localDir;

  @BeforeEach
  void setUp() throws Exception {
    System.setProperty( Const.KETTLE_COMPATIBILITY_ALLOW_S3_LEGACY_URI, "Y" );
    proxy = new S3FaultProxy( S3_MOCK.getServiceEndpoint(), probes );
    environment = new KettleS3Environment( proxy.endpoint() );
    options = new S3Provider().getOpts( environment.details( "legacy-" + UUID.randomUUID() ) );
  }

  @AfterEach
  void tearDown() {
    System.clearProperty( Const.KETTLE_COMPATIBILITY_ALLOW_S3_LEGACY_URI );
    proxy.close();
  }

  @Test
  void writesReadsListsAndDeletesThroughLegacyUris() throws Exception {
    write( LEGACY_ROOT + "rw/file.txt", "legacy content" );

    assertEquals( "legacy content", read( DIRECT_ROOT + "rw/file.txt" ) );
    assertEquals( "legacy content", read( LEGACY_ROOT + "rw/file.txt" ) );
    assertEquals( List.of( "file.txt" ), childNames( LEGACY_ROOT + "rw" ) );

    try ( FileObject file = resolve( LEGACY_ROOT + "rw/file.txt" ) ) {
      assertEquals( "legacy content".getBytes( StandardCharsets.UTF_8 ).length, file.getContent().getSize() );
      assertTrue( file.getContent().getLastModifiedTime() > 0 );
      assertTrue( file.delete() );
    }
    assertFalse( resolve( DIRECT_ROOT + "rw/file.txt" ).exists() );
  }

  @Test
  void legacyBucketRootIsTheRealBucketRoot() throws Exception {
    write( DIRECT_ROOT + "root/file.txt", "x" );

    try ( FileObject root = resolve( "s3://" + LEGACY_NOT_BUCKET + "/" + KettleS3Environment.BUCKET ) ) {
      assertTrue( root.isFolder() );
      assertTrue( childNames( root ).contains( "root" ) );
    }
  }

  @Test
  void createsFoldersThroughLegacyUris() throws Exception {
    try ( FileObject folder = resolve( LEGACY_ROOT + "created-folder" ) ) {
      folder.createFolder();
    }

    assertTrue( resolve( DIRECT_ROOT + "created-folder" ).isFolder() );
  }

  @Test
  void uploadsEmptyStreamsAndLocalFilesToTheCorrectedLocation() throws Exception {
    try ( FileObject empty = resolve( LEGACY_ROOT + "upload/empty.txt" ) ) {
      empty.createFile();
    }
    Path local = Files.writeString( localDir.resolve( "local.txt" ), "from disk" );
    try ( FileObject source = environment.vfs().getFileObject( local.toUri().toString() );
          FileObject destination = resolve( LEGACY_ROOT + "upload/local.txt" ) ) {
      destination.copyFrom( source, Selectors.SELECT_SELF );
    }

    try ( FileObject empty = resolve( DIRECT_ROOT + "upload/empty.txt" ) ) {
      assertTrue( empty.isFile() );
      assertEquals( 0, empty.getContent().getSize() );
    }
    assertEquals( "from disk", read( DIRECT_ROOT + "upload/local.txt" ) );
  }

  @Test
  void copiesWithEitherOrBothEndpointsLegacy() throws Exception {
    write( DIRECT_ROOT + "copy/source.txt", "copied" );

    copy( LEGACY_ROOT + "copy/source.txt", DIRECT_ROOT + "copy/from-legacy.txt" );
    copy( DIRECT_ROOT + "copy/source.txt", LEGACY_ROOT + "copy/to-legacy.txt" );
    copy( LEGACY_ROOT + "copy/source.txt", LEGACY_ROOT + "copy/both-legacy.txt" );

    assertEquals( "copied", read( DIRECT_ROOT + "copy/from-legacy.txt" ) );
    assertEquals( "copied", read( DIRECT_ROOT + "copy/to-legacy.txt" ) );
    assertEquals( "copied", read( DIRECT_ROOT + "copy/both-legacy.txt" ) );
    assertEquals( 0, probes.failedCopies(), "server-side copy must not need the upload fallback" );
  }

  @Test
  void movesThroughLegacyUris() throws Exception {
    write( DIRECT_ROOT + "move/source.txt", "moved" );

    try ( FileObject source = resolve( LEGACY_ROOT + "move/source.txt" );
          FileObject target = resolve( LEGACY_ROOT + "move/target.txt" ) ) {
      source.moveTo( target );
    }

    assertFalse( resolve( DIRECT_ROOT + "move/source.txt" ).exists() );
    assertEquals( "moved", read( DIRECT_ROOT + "move/target.txt" ) );
  }

  @Test
  void bucketChecksAreCached() throws Exception {
    write( DIRECT_ROOT + "cached/a.txt", "a" );
    write( DIRECT_ROOT + "cached/b.txt", "b" );
    probes.reset();

    for ( int i = 0; i < 3; i++ ) {
      assertEquals( "a", read( LEGACY_ROOT + "cached/a.txt" ) );
      assertEquals( "b", read( DIRECT_ROOT + "cached/b.txt" ) );
    }

    assertEquals( 1, probes.headBuckets( LEGACY_NOT_BUCKET ) );
    assertEquals( 0, probes.headBuckets( KettleS3Environment.BUCKET ), "checked by the writes already" );
  }

  @Test
  void healthyBucketsAreNeverCorrected() throws Exception {
    write( DIRECT_ROOT + "healthy/file.txt", "healthy" );

    assertEquals( "healthy", read( DIRECT_ROOT + "healthy/file.txt" ) );
    assertEquals( List.of( "file.txt" ), childNames( DIRECT_ROOT + "healthy" ) );

    assertEquals( 0, probes.headBuckets( LEGACY_NOT_BUCKET ) );
  }

  @Test
  void missingCandidateKeepsTheOriginalLocation() throws Exception {
    try ( FileObject file = resolve( "s3://" + LEGACY_NOT_BUCKET + "/no-such-bucket/file.txt" ) ) {
      assertThrows( FileSystemException.class, file::exists );
    }

    assertEquals( 1, probes.headBuckets( "no-such-bucket" ) );
  }

  @Test
  void bucketCheckFailuresPropagate() throws Exception {
    probes.failHeadBucket( LEGACY_NOT_BUCKET, 500 );

    try ( FileObject file = resolve( LEGACY_ROOT + "failing/file.txt" ) ) {
      FileSystemException failure = assertThrows( FileSystemException.class, file::exists );
      assertEquals( 500, rootCause( failure ).statusCode() );
    }
    assertEquals( 0, probes.headBuckets( KettleS3Environment.BUCKET ) );
  }

  @Test
  void forbiddenBucketCountsAsExisting() throws Exception {
    probes.failHeadBucket( LEGACY_NOT_BUCKET, 403 );
    write( DIRECT_ROOT + "forbidden/file.txt", "x" );
    probes.reset();

    try ( FileObject file = resolve( LEGACY_ROOT + "forbidden/file.txt" ) ) {
      assertThrows( FileSystemException.class, file::exists );
    }
    assertEquals( 1, probes.headBuckets( LEGACY_NOT_BUCKET ) );
    assertEquals( 0, probes.headBuckets( KettleS3Environment.BUCKET ) );
  }

  @Test
  void disabledFlagLeavesLegacyUrisAlone() throws Exception {
    System.clearProperty( Const.KETTLE_COMPATIBILITY_ALLOW_S3_LEGACY_URI );
    write( DIRECT_ROOT + "disabled/file.txt", "x" );
    probes.reset();

    try ( FileObject file = resolve( LEGACY_ROOT + "disabled/file.txt" ) ) {
      assertThrows( FileSystemException.class, file::exists );
    }
    assertEquals( 0, probes.headBuckets() );
  }

  @Test
  void s3aIsNeverCorrected() throws Exception {
    byte[] content = "s3a".getBytes( StandardCharsets.UTF_8 );
    try ( FileObject file = resolve( "s3a://" + KettleS3Environment.BUCKET + "/s3a/file.txt" ) ) {
      try ( OutputStream out = file.getContent().getOutputStream() ) {
        out.write( content );
      }
      try ( InputStream in = file.getContent().getInputStream() ) {
        assertEquals( "s3a", new String( in.readAllBytes(), StandardCharsets.UTF_8 ) );
      }
    }
    probes.reset();
    try ( FileObject legacy = resolve( "s3a://" + LEGACY_NOT_BUCKET + "/" + KettleS3Environment.BUCKET + "/s3a/file.txt" ) ) {
      assertThrows( FileSystemException.class, legacy::exists );
    }

    assertEquals( 0, probes.headBuckets() );
  }

  private void copy( String source, String destination ) throws Exception {
    try ( FileObject src = resolve( source ); FileObject dst = resolve( destination ) ) {
      dst.copyFrom( src, Selectors.SELECT_SELF );
    }
  }

  private void write( String uri, String content ) throws Exception {
    try ( FileObject file = resolve( uri ); OutputStream out = file.getContent().getOutputStream() ) {
      out.write( content.getBytes( StandardCharsets.UTF_8 ) );
    }
  }

  private String read( String uri ) throws Exception {
    try ( FileObject file = resolve( uri ); InputStream in = file.getContent().getInputStream() ) {
      return new String( in.readAllBytes(), StandardCharsets.UTF_8 );
    }
  }

  private List<String> childNames( String uri ) throws Exception {
    try ( FileObject folder = resolve( uri ) ) {
      return childNames( folder );
    }
  }

  private static List<String> childNames( FileObject folder ) throws FileSystemException {
    return Arrays.stream( folder.getChildren() ).map( child -> child.getName().getBaseName() ).toList();
  }

  private FileObject resolve( String uri ) throws Exception {
    return environment.vfs().getFileObject( uri, new Variables(), options );
  }

  private static S3Exception rootCause( Throwable failure ) {
    Throwable cause = failure;
    while ( cause.getCause() != null ) {
      cause = cause.getCause();
    }
    return assertInstanceOf( S3Exception.class, cause );
  }

  /**
   * Counts HeadBucket requests per bucket, optionally failing some of them. Attaching a bucket root folder
   * (e.g. when a write creates parent folders) is also a HeadBucket, hence {@link #reset()} after setup.
   */
  private static final class BucketProbes implements S3FaultProxy.Fault {

    private final Map<String, AtomicInteger> headBuckets = new ConcurrentHashMap<>();
    private final Map<String, Integer> failures = new ConcurrentHashMap<>();
    private final AtomicInteger failedCopies = new AtomicInteger();

    @Override
    public Optional<S3FaultProxy.Failure> reject( HttpExchange exchange ) {
      String path = exchange.getRequestURI().getPath();
      String[] segments = path.replaceAll( "^/|/$", "" ).split( "/" );
      if ( "HEAD".equals( exchange.getRequestMethod() ) && segments.length == 1 ) {
        headBuckets.computeIfAbsent( segments[ 0 ], bucket -> new AtomicInteger() ).incrementAndGet();
        return Optional.ofNullable( failures.get( segments[ 0 ] ) )
          .map( status -> new S3FaultProxy.Failure( status, "Injected", "head-bucket" ) );
      }
      return Optional.empty();
    }

    @Override
    public void forwarded( HttpExchange exchange, int status ) {
      if ( exchange.getRequestHeaders().containsKey( "x-amz-copy-source" ) && status / 100 != 2 ) {
        failedCopies.incrementAndGet();
      }
    }

    private void reset() {
      headBuckets.clear();
    }

    private void failHeadBucket( String bucket, int status ) {
      failures.put( bucket, status );
    }

    private int headBuckets( String bucket ) {
      return Optional.ofNullable( headBuckets.get( bucket ) ).map( AtomicInteger::get ).orElse( 0 );
    }

    private int headBuckets() {
      return headBuckets.values().stream().mapToInt( AtomicInteger::get ).sum();
    }

    private int failedCopies() {
      return failedCopies.get();
    }
  }
}
