package org.pentaho.s3.vfs;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.provider.FileNameParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;


/**
 * Unit tests for S3FileNameParser
 */
public class S3FileNameParserTest {

  FileNameParser parser;

  @BeforeEach
  public void setUp() throws Exception {
    parser = S3FileNameParser.getInstance();
  }

  @Test
  public void testParseUri() throws Exception {
    VfsComponentContext context = mock( VfsComponentContext.class );
    FileName fileName = mock( FileName.class );
    String uri = "s3://bucket/file";
    FileName noBaseFile = parser.parseUri( context, null, uri );
    assertNotNull( noBaseFile );
    assertEquals( "bucket", ( (S3FileName) noBaseFile ).getBucketId() );
    FileName withBaseFile = parser.parseUri( context, fileName, uri );
    assertNotNull( withBaseFile );
    assertEquals( "bucket", ( (S3FileName) withBaseFile ).getBucketId() );

    // assumption is that the whole URL is valid until it comes time to resolve to S3 objects
    uri = "s3://s3/bucket/file";
    withBaseFile = parser.parseUri( context, fileName, uri );
    assertEquals( "s3", ( (S3FileName)withBaseFile ).getBucketId() );

    //with credentials
    uri = "s3://ThiSiSA+PossibleAcce/ssK3y:PossiblES3cre+K3y@s3/bucket/file";
    withBaseFile = parser.parseUri( context, fileName, uri );
    assertEquals( "ThiSiSA+PossibleAcce/ssK3y:PossiblES3cre+K3y@s3", ( (S3FileName)withBaseFile ).getBucketId() );
  }

  @Test
  public void testParseUriRoundTrip() throws Exception {
    String origUri = "s3://fooBucket/rcf-emr-staging";
    S3FileName fileName = (S3FileName) parser.parseUri( null, null, origUri );

    assertEquals( "fooBucket", fileName.getBucketId() );
    assertEquals( origUri, fileName.getURI() );
  }
}
