package org.pentaho.s3.vfs;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.pentaho.s3.vfs.support.KettleS3Environment;
import org.pentaho.s3.vfs.support.S3FaultProxy;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.lang.ref.Reference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.apache.commons.vfs2.FileType.FILE;
import static org.apache.commons.vfs2.FileType.FOLDER;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests S3 request counts while browsing files and folders through VFS.
 * Checking for regression to the number of calls made.
 * Tests will also fail if numbers go down, just change the test then.
 **/
public class S3BrowseRequestCountIT {

  @RegisterExtension
  static final S3MockExtension S3_MOCK = S3MockExtension.builder()
    .withSecureConnection( false )
    .withInitialBuckets( KettleS3Environment.BUCKET )
    .silent()
    .build();

  private static final Map<String, Map<String, FileType>> TREE = Map.of(
    "", Map.of( "root-one.txt", FILE, "root-two.txt", FILE, "browse_test", FOLDER ),
    "browse_test", Map.of( "one.txt", FILE, "two.txt", FILE, "marked", FOLDER, "virtual", FOLDER ),
    "browse_test/marked", Map.of( "one.txt", FILE, "two.txt", FILE, "nested-one", FOLDER, "nested-two", FOLDER ),
    "browse_test/virtual", Map.of( "one.txt", FILE, "two.txt", FILE, "nested-one", FOLDER, "nested-two", FOLDER ),
    "browse_test/marked/nested-one", Map.of( "leaf-one.txt", FILE, "leaf-two.txt", FILE ),
    "browse_test/marked/nested-two", Map.of( "leaf-one.txt", FILE, "leaf-two.txt", FILE ),
    "browse_test/virtual/nested-one", Map.of( "leaf-one.txt", FILE, "leaf-two.txt", FILE ),
    "browse_test/virtual/nested-two", Map.of( "leaf-one.txt", FILE, "leaf-two.txt", FILE )
  );

  @BeforeEach
  void populateBucket() {
    try ( S3Client client = S3Client.builder()
      .endpointOverride( URI.create( S3_MOCK.getServiceEndpoint() ) )
      .region( Region.US_EAST_1 )
      .credentialsProvider( StaticCredentialsProvider.create( AwsBasicCredentials.create( "test", "test" ) ) )
      .forcePathStyle( true )
      .build() ) {
      TREE.forEach( ( directory, children ) -> children.forEach( ( name, type ) -> {
        if ( type == FILE ) {
          String key = directory.isEmpty() ? name : directory + "/" + name;
          put( client, key );
        }
      } ) );
      // set up some of the folders as explicit objects (will get hits on HeadObject)
      put( client, "browse_test/marked/" );
      put( client, "browse_test/marked/nested-two/" );
      put( client, "browse_test/virtual/nested-two/" );
    }
  }

  @Test
  void browsingTreeKeepsS3RequestsWithinBaseline() throws Exception {
    var recorder = new RequestRecorder();
    try ( S3FaultProxy proxy = new S3FaultProxy( S3_MOCK.getServiceEndpoint(), recorder ) ) {
      KettleS3Environment environment = new KettleS3Environment( proxy.endpoint() );
      String bucketUri = environment.createDefaultConnection( KettleS3Environment.BUCKET );
      recorder.reset();

      FileObject bucket = environment.vfs().getFileObject( bucketUri );
      browse( bucket, "", new ArrayList<>() );
      List<Request> requests = recorder.snapshot();
      long bucketHeads = requests.stream().filter( Request::isBucketHead ).count();
      long fileHeads = requests.stream().filter( Request::isFileHead ).count();
      long folderHeads = requests.stream().filter( Request::isFolderHead ).count();
      long childListings = requests.stream().filter( Request::isChildListing ).count();
      long typeProbes = requests.stream().filter( Request::isTypeProbe ).count();
      long misses = requests.stream().filter( Request::isMiss ).count();
      assertAll( "S3 requests",
        () -> assertEquals( requests.size(), bucketHeads + fileHeads + folderHeads + childListings + typeProbes,
          "Classified requests" ),
        () -> assertEquals( 36, requests.size(), "Total requests" ), // previously: 43
        () -> assertEquals( 1, bucketHeads, "HeadBucket requests" ),
        () -> assertEquals( 16, fileHeads, "HeadObject requests for file keys" ), // previously: 23
        () -> assertEquals( 7, folderHeads, "HeadObject requests for folder keys" ),
        () -> assertEquals( 8, childListings, "ListObjectsV2 child-listing requests" ),
        () -> assertEquals( 4, typeProbes, "ListObjectsV2 type probes (max-keys=1)" ),
        () -> assertEquals( 4, misses, "Requests with non-200 responses" ) ); // previously: 11
    }
  }

  @Test
  void browsingTreeReattachKeepsS3RequestsWithinBaseline() throws Exception {
    var recorder = new RequestRecorder();
    try ( S3FaultProxy proxy = new S3FaultProxy( S3_MOCK.getServiceEndpoint(), recorder ) ) {
      KettleS3Environment environment = new KettleS3Environment( proxy.endpoint() );
      String bucketUri = environment.createDefaultConnection( KettleS3Environment.BUCKET );
      FileObject bucket = environment.vfs().getFileObject( bucketUri );
      List<FileObject> retained = new ArrayList<>();

      browse( bucket, "", retained );

      // Now test reatacching
      recorder.reset();
      bucket = environment.vfs().getFileObject( bucketUri );
      browse( bucket, "", retained );
      // since kettle vfs uses a weak ref cache, this ensures the file objects aren't garbage collected until here
      Reference.reachabilityFence( retained );

      List<Request> requests = recorder.snapshot();
      long bucketHeads = requests.stream().filter( Request::isBucketHead ).count();
      long fileHeads = requests.stream().filter( Request::isFileHead ).count();
      long folderHeads = requests.stream().filter( Request::isFolderHead ).count();
      long childListings = requests.stream().filter( Request::isChildListing ).count();
      long typeProbes = requests.stream().filter( Request::isTypeProbe ).count();
      long misses = requests.stream().filter( Request::isMiss ).count();
      assertAll( "S3 requests",
        () -> assertEquals( requests.size(), bucketHeads + fileHeads + folderHeads + childListings + typeProbes,
          "Classified requests" ),
        () -> assertEquals( 32, requests.size(), "Total requests" ), // previously: 43
        () -> assertEquals( 1, bucketHeads, "HeadBucket requests" ),
        () -> assertEquals( 16, fileHeads, "HeadObject requests for file keys" ), // previously: 23
        () -> assertEquals( 3, folderHeads, "HeadObject requests for folder keys" ), // previously: 7
        () -> assertEquals( 8, childListings, "ListObjectsV2 child-listing requests" ),
        () -> assertEquals( 4, typeProbes, "ListObjectsV2 type probes (max-keys=1)" ),
        () -> assertEquals( 0, misses, "Requests with non-200 responses" ) ); // previously: 11
    }
  }

  private static void put( S3Client client, String key ) {
    client.putObject( PutObjectRequest.builder().bucket( KettleS3Environment.BUCKET ).key( key ).build(),
      RequestBody.empty() );
  }

  private static void browse( FileObject folder, String path, List<FileObject> retained ) throws Exception {
    FileObject[] children = folder.getChildren();
    retained.addAll( Arrays.asList( children ) );
    Map<String, FileObject> byName = Arrays.stream( children )
      .collect( Collectors.toMap( child -> child.getName().getBaseName(), child -> child ) );
    Map<String, FileType> expected = TREE.get( path );
    assertEquals( expected.keySet(), byName.keySet(), path );
    for ( String name : expected.keySet().stream().sorted().toList() ) {
      FileObject child = byName.get( name );
      String childPath = path.isEmpty() ? name : path + "/" + name;
      assertEquals( expected.get( name ), child.getType(), childPath );
      if ( expected.get( name ) == FOLDER ) {
        browse( child, childPath, retained );
      }
    }
  }

  private record Request( String method, URI uri, CompletableFuture<Integer> status ) {
    private boolean isMiss() {
      return status.join() != 200;
    }

    private boolean isBucketHead() {
      return method.equals( "HEAD" ) && uri.getPath().replaceAll( "/+$", "" )
        .equals( "/" + KettleS3Environment.BUCKET );
    }

    private boolean isObjectHead() {
      return method.equals( "HEAD" ) && uri.getPath().startsWith( "/" + KettleS3Environment.BUCKET + "/" )
        && !isBucketHead();
    }

    private boolean isFileHead() {
      return isObjectHead() && !uri.getPath().endsWith( "/" );
    }

    private boolean isFolderHead() {
      return isObjectHead() && uri.getPath().endsWith( "/" );
    }

    private boolean isListing() {
      return method.equals( "GET" ) && uri.getRawQuery() != null && uri.getRawQuery().contains( "list-type=2" );
    }

    private boolean isTypeProbe() {
      return isListing() && uri.getRawQuery().contains( "max-keys=1" );
    }

    private boolean isChildListing() {
      return isListing() && !isTypeProbe();
    }
  }

  private static final class RequestRecorder implements S3FaultProxy.Fault {
    private final List<Request> requests = new CopyOnWriteArrayList<>();
    private final Map<HttpExchange, CompletableFuture<Integer>> pending = new ConcurrentHashMap<>();

    @Override
    public Optional<S3FaultProxy.Failure> reject( HttpExchange exchange ) {
      CompletableFuture<Integer> status = new CompletableFuture<>();
      pending.put( exchange, status );
      requests.add( new Request( exchange.getRequestMethod(), exchange.getRequestURI(), status ) );
      return Optional.empty();
    }

    @Override
    public void forwarded( HttpExchange exchange, int status ) {
      pending.remove( exchange ).complete( status );
    }

    private void reset() {
      snapshot();
      requests.clear();
    }

    private List<Request> snapshot() {
      List<Request> snapshot = List.copyOf( requests );
      snapshot.forEach( request -> request.status().orTimeout( 10, TimeUnit.SECONDS ).join() );
      return snapshot;
    }
  }
}