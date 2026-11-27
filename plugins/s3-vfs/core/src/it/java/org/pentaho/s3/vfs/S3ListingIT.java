package org.pentaho.s3.vfs;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.pentaho.s3.vfs.support.KettleS3Environment;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class S3ListingIT {

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
  void listsFilesBeforeSubFolders() throws Exception {
    write( "listing/a.txt" );
    write( "listing/b.txt" );
    write( "listing/nested/c.txt" );

    assertEquals( List.of( "a.txt", "b.txt", "nested" ), childNames( "listing" ) );
  }

  @Test
  void doesNotFlattenNestedContent() throws Exception {
    write( "flatten/deep/deeper/d.txt" );

    assertEquals( List.of( "deeper" ), childNames( "flatten/deep" ) );
  }

  @Test
  void browsesDownSeveralLevels() throws Exception {
    write( "browse/one/two/three/leaf.txt" );

    FileObject level = environment.vfs().getFileObject( bucketUri );
    for ( String step : List.of( "browse", "one", "two", "three" ) ) {
      level = level.getChild( step );
      assertEquals( FileType.FOLDER, level.getType(), step );
    }

    FileObject leaf = level.getChild( "leaf.txt" );
    assertEquals( FileType.FILE, leaf.getType() );
    assertEquals( bucketUri + "/browse/one/two/three/leaf.txt", leaf.getName().getURI() );
  }

  @Test
  void listsTopLevelEntriesOfTheBucket() throws Exception {
    write( "root-level.txt" );
    write( "root-folder/inside.txt" );

    // other tests share the bucket, so only membership can be asserted here
    assertTrue( childNames( "" ).containsAll( List.of( "root-level.txt", "root-folder" ) ) );
  }

  @Test
  void listsAnEmptyFolderAsHavingNoChildren() throws Exception {
    resolve( "empty-folder" ).createFolder();

    assertEquals( List.of(), childNames( "empty-folder" ) );
  }

  private void write( String path ) throws Exception {
    try ( OutputStream out = resolve( path ).getContent().getOutputStream() ) {
      out.write( path.getBytes( StandardCharsets.UTF_8 ) );
    }
  }

  private List<String> childNames( String path ) throws Exception {
    return Stream.of( resolve( path ).getChildren() )
      .map( child -> child.getName().getBaseName() )
      .toList();
  }

  private FileObject resolve( String path ) throws Exception {
    return environment.vfs().getFileObject( bucketUri + "/" + path );
  }
}
