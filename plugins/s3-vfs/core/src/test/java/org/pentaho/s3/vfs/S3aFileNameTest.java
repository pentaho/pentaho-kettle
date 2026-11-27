package org.pentaho.s3.vfs;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** S3A compatibility tests, kept separate from the canonical S3 cases. */
class S3aFileNameTest {
  @Test
  void parsesAndPreservesTheAlias() throws Exception {
    String uri = "s3a://bucket/dir/file";
    var name = (S3FileName) S3FileNameParser.getInstance().parseUri( null, null, uri );
    assertEquals( "s3a", name.getScheme() );
    assertEquals( "bucket", name.getBucketId() );
    assertEquals( "/bucket/dir/file", name.getPath() );
    assertEquals( uri, name.getURI() );
    assertEquals( "s3a://bucket/other", name.createName( "/bucket/other", FileType.FILE ).getURI() );
  }

  @Test
  void legacyEmbeddedCredentialsHaveTheSameShapeAsS3() throws Exception {
    var parser = S3FileNameParser.getInstance();
    var canonical = (S3FileName) parser.parseUri( null, null, "s3://ACCESS:SECRET@s3/bucket/file" );
    var alias = (S3FileName) parser.parseUri( null, null, "s3a://ACCESS:SECRET@s3/bucket/file" );
    assertEquals( canonical.getBucketId(), alias.getBucketId() );
    assertEquals( canonical.getPath(), alias.getPath() );
    assertEquals( "s3a://ACCESS:SECRET@s3/bucket/file", alias.getURI() );
  }

  @Test
  void doesNotEnableS3n() {
    assertThrows( FileSystemException.class,
      () -> S3FileNameParser.getInstance().parseUri( null, null, "s3n://bucket/file" ) );
  }
}