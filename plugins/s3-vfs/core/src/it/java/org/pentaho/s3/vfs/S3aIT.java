package org.pentaho.s3.vfs;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.Selectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.pentaho.amazon.s3.provider.S3Provider;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.s3.vfs.support.KettleS3Environment;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** End-to-end S3A compatibility smoke tests; no additional S3A production path. */
public class S3aIT {
  @RegisterExtension
  static final S3MockExtension S3_MOCK = S3MockExtension.builder()
    .withSecureConnection( false ).withInitialBuckets( KettleS3Environment.BUCKET ).silent().build();

  private static KettleS3Environment environment;
  private static org.apache.commons.vfs2.FileSystemOptions options;

  @BeforeAll
  static void startEnvironment() throws Exception {
    environment = new KettleS3Environment( S3_MOCK.getServiceEndpoint() );
    options = new S3Provider().getOpts( environment.details( "s3a-smoke" ) );
  }

  @Test
  void writesAndReadsAcrossBothSchemes() throws Exception {
    byte[] content = "S3A round trip".getBytes( StandardCharsets.UTF_8 );
    for ( String scheme : new String[] { "s3", "s3a" } ) {
      String other = scheme.equals( "s3" ) ? "s3a" : "s3";
      try ( FileObject writer = resolve( scheme, "round-trip-" + scheme );
            FileObject reader = resolve( other, "round-trip-" + scheme ) ) {
        try ( var out = writer.getContent().getOutputStream() ) {
          out.write( content );
        }
        try ( var in = reader.getContent().getInputStream() ) {
          assertArrayEquals( content, in.readAllBytes() );
        }
        assertTrue( reader.delete() );
        writer.refresh();
        assertFalse( writer.exists() );
      }
    }
  }

  @Test
  void listsCopiesAndMovesAcrossSchemes() throws Exception {
    byte[] content = "copy and move".getBytes( StandardCharsets.UTF_8 );
    try ( FileObject source = resolve( "s3a", "listing/source.txt" );
          FileObject folder = resolve( "s3a", "listing/" );
          FileObject copy = resolve( "s3", "copy.txt" );
          FileObject moved = resolve( "s3a", "moved.txt" ) ) {
      try ( var out = source.getContent().getOutputStream() ) {
        out.write( content );
      }
      assertTrue( folder.isFolder() );
      FileObject[] children = folder.getChildren();
      try {
        assertTrue( Arrays.stream( children ).anyMatch( child -> child.getName().getBaseName().equals( "source.txt" ) ) );
        for ( FileObject child : children ) {
          assertEquals( "s3a", child.getName().getScheme() );
          assertTrue( child.isFile() );
        }
      } finally {
        for ( FileObject child : children ) {
          child.close();
        }
      }
      copy.copyFrom( source, Selectors.SELECT_SELF );
      copy.moveTo( moved );
      try ( var in = moved.getContent().getInputStream() ) {
        assertArrayEquals( content, in.readAllBytes() );
      }
      copy.refresh();
      assertFalse( copy.exists() );
      assertTrue( moved.delete() );
      assertTrue( source.delete() );
    }
  }

  private static FileObject resolve( String scheme, String key ) throws Exception {
    return environment.vfs().getFileObject(
      scheme + "://" + KettleS3Environment.BUCKET + "/" + key, new Variables(), options );
  }
}