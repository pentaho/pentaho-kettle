package org.pentaho.s3.vfs;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import org.apache.commons.vfs2.FileObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.pentaho.amazon.s3.provider.S3Provider;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.s3.vfs.support.KettleS3Environment;
import org.pentaho.s3.vfs.support.S3FaultProxy;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class S3ReadWriteIT {

  @RegisterExtension
  static final S3MockExtension S3_MOCK = S3MockExtension.builder()
    .withSecureConnection( false )
    .withInitialBuckets( KettleS3Environment.BUCKET )
    .silent()
    .build();

  private static KettleS3Environment environment;
  private static String bucketUri;

  @BeforeAll
  static void startEnvironment() throws Exception {
    environment = new KettleS3Environment( S3_MOCK.getServiceEndpoint() );
    bucketUri = environment.createDefaultConnection( KettleS3Environment.BUCKET );
  }

  @Test
  void readsBackWhatWasWritten() throws Exception {
    FileObject file = resolve( "round-trip.txt" );
    byte[] content = "some content".getBytes( StandardCharsets.UTF_8 );

    try ( OutputStream out = file.getContent().getOutputStream() ) {
      out.write( content );
    }

    assertArrayEquals( content, read( resolve( "round-trip.txt" ) ) );
    assertEquals( content.length, resolve( "round-trip.txt" ).getContent().getSize() );
  }

  @Test
  void overwritingReplacesTheWholeContent() throws Exception {
    write( "overwrite.txt", "a long first version".getBytes( StandardCharsets.UTF_8 ) );
    write( "overwrite.txt", "short".getBytes( StandardCharsets.UTF_8 ) );

    assertEquals( "short", new String( read( resolve( "overwrite.txt" ) ), StandardCharsets.UTF_8 ) );
    assertEquals( 5, resolve( "overwrite.txt" ).getContent().getSize() );
  }

  @Test
  void writesMoreThanOnePart() throws Exception {
    byte[] megabyte = new byte[ 1024 * 1024 ];
    Arrays.fill( megabyte, (byte) 'x' );

    try ( OutputStream out = resolve( "multipart.bin" ).getContent().getOutputStream() ) {
      for ( int i = 0; i < 6; i++ ) {
        out.write( megabyte );
      }
    }

    assertEquals( 6L * megabyte.length, resolve( "multipart.bin" ).getContent().getSize() );
    assertEquals( 6L * megabyte.length, read( resolve( "multipart.bin" ) ).length );
  }

  @Test
  @ResourceLock( Resources.SYSTEM_PROPERTIES )
  void usesSystemPropertyCredentialsWithoutConnectionKeys() throws Exception {
    String savedAccess = System.getProperty( "aws.accessKeyId" );
    String savedSecret = System.getProperty( "aws.secretAccessKey" );
    String savedToken = System.getProperty( "aws.sessionToken" );
    String savedEndpoint = System.getProperty( "aws.endpointUrlS3" );
    String savedIgnoreEndpoints = System.getProperty( "aws.ignoreConfiguredEndpointUrls" );
    List<String> authorizations = new CopyOnWriteArrayList<>();

    try ( S3FaultProxy proxy = new S3FaultProxy( S3_MOCK.getServiceEndpoint(), exchange -> {
      authorizations.add( Optional.ofNullable( exchange.getRequestHeaders().getFirst( "Authorization" ) ).orElse( "" ) );
      return Optional.empty();
    } ) ) {
      var credentialsEnvironment = new KettleS3Environment( proxy.endpoint() );
      var details = credentialsEnvironment.details( "system-property-credentials" );
      details.setAccessKey( null );
      details.setSecretKey( null );
      var options = new S3Provider().getOpts( details );
      System.setProperty( "aws.accessKeyId", "property-access-key" );
      System.setProperty( "aws.secretAccessKey", "property-secret-key" );
      System.clearProperty( "aws.sessionToken" );
      System.setProperty( "aws.endpointUrlS3", proxy.endpoint() );
      System.setProperty( "aws.ignoreConfiguredEndpointUrls", "false" );

      String uri = "s3://" + KettleS3Environment.BUCKET + "/system-property-credentials.txt";
      byte[] content = "system property credentials".getBytes( StandardCharsets.UTF_8 );
      try ( FileObject file = credentialsEnvironment.vfs().getFileObject( uri, new Variables(), options ) ) {
        var fileSystem = (S3FileSystem) file.getFileSystem();
        var client = fileSystem.getS3Client();
        var credentials = client.serviceClientConfiguration().credentialsProvider().resolveIdentity().join();
        assertEquals( "property-access-key", credentials.accessKeyId() );
        assertEquals( "property-secret-key", credentials.secretAccessKey() );
        var endpoint = Optional.of( URI.create( proxy.endpoint() ) );
        assertEquals( endpoint, client.serviceClientConfiguration().endpointOverride(),
          "S3 client must use the local endpoint" );
        assertEquals( endpoint, fileSystem.getS3AsyncClient().serviceClientConfiguration().endpointOverride(),
          "Asynchronous S3 client must use the local endpoint" );
        try ( var out = file.getContent().getOutputStream() ) {
          out.write( content );
        }
        try ( var in = file.getContent().getInputStream() ) {
          assertArrayEquals( content, in.readAllBytes() );
        }
      }

      assertFalse( authorizations.isEmpty(), "Expected signed S3 requests" );
      assertTrue( authorizations.stream().allMatch( value -> value.contains( "Credential=property-access-key/" ) ) );
    } finally {
      restoreProperty( "aws.accessKeyId", savedAccess );
      restoreProperty( "aws.secretAccessKey", savedSecret );
      restoreProperty( "aws.sessionToken", savedToken );
      restoreProperty( "aws.endpointUrlS3", savedEndpoint );
      restoreProperty( "aws.ignoreConfiguredEndpointUrls", savedIgnoreEndpoints );
    }
  }

  @Test
  @ResourceLock( Resources.SYSTEM_PROPERTIES )
  void sendsSystemPropertySessionTokenWithoutConnectionKeys() throws Exception {
    String savedAccess = System.getProperty( "aws.accessKeyId" );
    String savedSecret = System.getProperty( "aws.secretAccessKey" );
    String savedToken = System.getProperty( "aws.sessionToken" );
    String savedEndpoint = System.getProperty( "aws.endpointUrlS3" );
    String savedIgnoreEndpoints = System.getProperty( "aws.ignoreConfiguredEndpointUrls" );
    List<String> tokens = new CopyOnWriteArrayList<>();

    try ( var proxy = new S3FaultProxy( S3_MOCK.getServiceEndpoint(), exchange -> {
      tokens.add( Optional.ofNullable( exchange.getRequestHeaders().getFirst( "X-Amz-Security-Token" ) ).orElse( "" ) );
      return Optional.empty();
    } ) ) {
      var credentialsEnvironment = new KettleS3Environment( proxy.endpoint() );
      var details = credentialsEnvironment.details( "system-property-session-token" );
      details.setAccessKey( null );
      details.setSecretKey( null );
      var options = new S3Provider().getOpts( details );
      System.setProperty( "aws.accessKeyId", "property-access-key" );
      System.setProperty( "aws.secretAccessKey", "property-secret-key" );
      System.setProperty( "aws.sessionToken", "property-session-token" );
      System.setProperty( "aws.endpointUrlS3", proxy.endpoint() );
      System.setProperty( "aws.ignoreConfiguredEndpointUrls", "false" );

      String uri = "s3://" + KettleS3Environment.BUCKET + "/system-property-session-token.txt";
      byte[] content = "session token".getBytes( StandardCharsets.UTF_8 );
      try ( FileObject file = credentialsEnvironment.vfs().getFileObject( uri, new Variables(), options ) ) {
        try ( var out = file.getContent().getOutputStream() ) {
          out.write( content );
        }
        try ( var in = file.getContent().getInputStream() ) {
          assertArrayEquals( content, in.readAllBytes() );
        }
      }

      assertFalse( tokens.isEmpty(), "Expected signed S3 requests" );
      assertTrue( tokens.stream().allMatch( "property-session-token"::equals ), "Every request must carry the token" );
    } finally {
      restoreProperty( "aws.accessKeyId", savedAccess );
      restoreProperty( "aws.secretAccessKey", savedSecret );
      restoreProperty( "aws.sessionToken", savedToken );
      restoreProperty( "aws.endpointUrlS3", savedEndpoint );
      restoreProperty( "aws.ignoreConfiguredEndpointUrls", savedIgnoreEndpoints );
    }
  }

  @Test
  @ResourceLock( Resources.SYSTEM_PROPERTIES )
  void listsBucketsWithSystemPropertyCredentials() throws Exception {
    String savedAccess = System.getProperty( "aws.accessKeyId" );
    String savedSecret = System.getProperty( "aws.secretAccessKey" );
    String savedToken = System.getProperty( "aws.sessionToken" );
    List<String> authorizations = new CopyOnWriteArrayList<>();
    List<String> tokens = new CopyOnWriteArrayList<>();

    try ( S3FaultProxy proxy = new S3FaultProxy( S3_MOCK.getServiceEndpoint(), exchange -> {
      authorizations.add( Optional.ofNullable( exchange.getRequestHeaders().getFirst( "Authorization" ) ).orElse( "" ) );
      tokens.add( Optional.ofNullable( exchange.getRequestHeaders().getFirst( "X-Amz-Security-Token" ) ).orElse( "" ) );
      return Optional.empty();
    } ) ) {
      var details = new KettleS3Environment( proxy.endpoint() ).details( "bucket-list-credentials" );
      details.setAccessKey( null );
      details.setSecretKey( null );
      System.setProperty( "aws.accessKeyId", "property-access-key" );
      System.setProperty( "aws.secretAccessKey", "property-secret-key" );
      System.setProperty( "aws.sessionToken", "property-session-token" );

      var locations = new S3Provider().getLocations( details );

      assertTrue( locations.stream().anyMatch( bucket -> bucket.getName().equals( KettleS3Environment.BUCKET ) ) );
      assertFalse( authorizations.isEmpty(), "Expected signed S3 requests" );
      assertTrue( authorizations.stream().allMatch( value -> value.contains( "Credential=property-access-key/" ) ) );
      assertTrue( tokens.stream().allMatch( "property-session-token"::equals ) );
    } finally {
      restoreProperty( "aws.accessKeyId", savedAccess );
      restoreProperty( "aws.secretAccessKey", savedSecret );
      restoreProperty( "aws.sessionToken", savedToken );
    }
  }

  @Test
  void listsBucketsWithConnectionCredentials() throws Exception {
    List<String> authorizations = new CopyOnWriteArrayList<>();
    try ( S3FaultProxy proxy = new S3FaultProxy( S3_MOCK.getServiceEndpoint(), exchange -> {
      authorizations.add( Optional.ofNullable( exchange.getRequestHeaders().getFirst( "Authorization" ) ).orElse( "" ) );
      return Optional.empty();
    } ) ) {
      var details = new KettleS3Environment( proxy.endpoint() ).details( "bucket-list-connection" );
      var locations = new S3Provider().getLocations( details );

      assertTrue( locations.stream().anyMatch( bucket -> bucket.getName().equals( KettleS3Environment.BUCKET ) ) );
      assertFalse( authorizations.isEmpty(), "Expected signed S3 requests" );
      assertTrue( authorizations.stream().allMatch( value -> value.contains( "Credential=test-access-key/" ) ) );
    }
  }

  private static void restoreProperty( String name, String value ) {
    if ( value == null ) {
      System.clearProperty( name );
    } else {
      System.setProperty( name, value );
    }
  }

  private void write( String path, byte[] content ) throws Exception {
    try ( OutputStream out = resolve( path ).getContent().getOutputStream() ) {
      out.write( content );
    }
  }

  private byte[] read( FileObject file ) throws Exception {
    try ( InputStream in = file.getContent().getInputStream() ) {
      return in.readAllBytes();
    }
  }

  private FileObject resolve( String path ) throws Exception {
    return environment.vfs().getFileObject( bucketUri + "/" + path );
  }
}
