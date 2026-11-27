package org.pentaho.s3.vfs;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.pentaho.s3.vfs.support.KettleS3Environment;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class S3FolderIT {

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
  void createsAnExplicitFolder() throws Exception {
    resolve( "created" ).createFolder();

    FileObject folder = resolve( "created" );
    assertTrue( folder.exists() );
    assertEquals( FileType.FOLDER, folder.getType() );
  }

  @Test
  void deletesAFile() throws Exception {
    write( "delete/one.txt" );

    assertTrue( resolve( "delete/one.txt" ).delete() );
    assertFalse( resolve( "delete/one.txt" ).exists() );
  }

  @Test
  void deletesAFolderWithItsChildren() throws Exception {
    write( "delete-all/a.txt" );
    write( "delete-all/nested/b.txt" );

    // the folder, its two files and the nested folder
    assertEquals( 4, resolve( "delete-all" ).deleteAll() );
    assertFalse( resolve( "delete-all" ).exists() );
    assertFalse( resolve( "delete-all/nested/b.txt" ).exists() );
  }

  @Test
  void deletedFolderCanBeReplacedByFile() throws Exception {
    final String path = "folder-replaced-by-file";
    FileObject folder = resolve( path );
    folder.createFolder();
    assertEquals( FileType.FOLDER, folder.getType() );

    // something else deletes the folder and creates a file instead
    try ( S3Client client = S3Client.builder()
      .endpointOverride( URI.create( S3_MOCK.getServiceEndpoint() ) )
      .region( Region.US_EAST_1 )
      .credentialsProvider( StaticCredentialsProvider.create( AwsBasicCredentials.create( "test", "test" ) ) )
      .forcePathStyle( true )
      .build() ) {
      client.deleteObject( DeleteObjectRequest.builder().bucket( KettleS3Environment.BUCKET ).key( path + "/" ).build() );
      client.putObject( PutObjectRequest.builder().bucket( KettleS3Environment.BUCKET ).key( path ).build(),
        RequestBody.fromString( path, StandardCharsets.UTF_8 ) );
    }

    folder.refresh();
    assertEquals( FileType.FILE, folder.getType() );
    try ( InputStream in = folder.getContent().getInputStream() ) {
      assertEquals( path, new String( in.readAllBytes(), StandardCharsets.UTF_8 ) );
    }
  }

  @Test
  void createsFoldersAllTheWayDown() throws Exception {
    resolve( "created/one/two" ).createFolder();

    assertTrue( resolve( "created/one/two" ).exists() );
    assertTrue( resolve( "created/one" ).exists() );
  }

  @Test
  void creatingTheBucketRootIsANoOp() throws Exception {
    FileObject bucket = environment.vfs().getFileObject( bucketUri );

    bucket.createFolder();

    assertEquals( FileType.FOLDER, bucket.getType() );
  }

  private void write( String path ) throws Exception {
    try ( OutputStream out = resolve( path ).getContent().getOutputStream() ) {
      out.write( path.getBytes( StandardCharsets.UTF_8 ) );
    }
  }

  private FileObject resolve( String path ) throws Exception {
    return environment.vfs().getFileObject( bucketUri + "/" + path );
  }
}
