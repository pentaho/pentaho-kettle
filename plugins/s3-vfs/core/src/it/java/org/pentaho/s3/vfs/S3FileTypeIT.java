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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests S3 file type detection and metadata through VFS. */
public class S3FileTypeIT {

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
  void writtenObjectIsAnExistingFile() throws Exception {
    write( "types/file.txt" );

    FileObject file = resolve( "types/file.txt" );
    assertTrue( file.exists() );
    assertEquals( FileType.FILE, file.getType() );
    assertEquals( "types/file.txt".getBytes( StandardCharsets.UTF_8 ).length, file.getContent().getSize() );
    assertTrue( file.getContent().getLastModifiedTime() > 0 );
  }

  @Test
  void prefixOfAnObjectIsAFolderEvenIfNeverCreated() throws Exception {
    write( "virtual/inside.txt" );

    FileObject folder = resolve( "virtual" );
    assertTrue( folder.exists() );
    assertEquals( FileType.FOLDER, folder.getType() );
  }

  @Test
  void prefixWithOnlyNestedObjectsIsAFolder() throws Exception {
    write( "virtual-nested/inner/inside.txt" );

    FileObject folder = resolve( "virtual-nested" );
    assertTrue( folder.exists() );
    assertEquals( FileType.FOLDER, folder.getType() );
  }

  @Test
  void explicitFolderMarkerIsAFolder() throws Exception {
    FileObject folder = resolve( "explicit-type-folder" );
    folder.createFolder();
    folder.refresh();

    assertTrue( folder.exists() );
    assertEquals( FileType.FOLDER, folder.getType() );
  }

  @Test
  void unknownKeyDoesNotExist() throws Exception {
    FileObject missing = resolve( "types/no-such-key.txt" );

    assertFalse( missing.exists() );
    assertEquals( FileType.IMAGINARY, missing.getType() );
  }

  @Test
  void bucketRootIsAnExistingFolder() throws Exception {
    FileObject bucket = environment.vfs().getFileObject( bucketUri );

    assertTrue( bucket.exists() );
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
