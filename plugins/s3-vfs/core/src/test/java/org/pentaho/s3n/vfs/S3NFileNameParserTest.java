/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.s3n.vfs;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.provider.FileNameParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;


/**
 * Unit tests for S3FileNameParser
 */
public class S3NFileNameParserTest {

  FileNameParser parser;

  @Before
  public void setUp() throws Exception {
    parser = S3NFileNameParser.getInstance();
  }

  @Test
  public void testParseUri() throws Exception {
    VfsComponentContext context = mock( VfsComponentContext.class );
    FileName fileName = mock( FileName.class );
    String uri = "s3n://bucket/file";
    FileName noBaseFile = parser.parseUri( context, null, uri );
    assertNotNull( noBaseFile );
    assertEquals( "bucket", ( (S3NFileName) noBaseFile ).getBucketId() );
    FileName withBaseFile = parser.parseUri( context, fileName, uri );
    assertNotNull( withBaseFile );
    assertEquals( "bucket", ( (S3NFileName) withBaseFile ).getBucketId() );

    // assumption is that the whole URL is valid until it comes time to resolve to S3 objects
    uri = "s3n://s3n/bucket/file";
    withBaseFile = parser.parseUri( context, fileName, uri );
    assertEquals( "s3n", ( (S3NFileName)withBaseFile ).getBucketId() );

    //with credentials
    uri = "s3n://ThiSiSA+PossibleAcce/ssK3y:PossiblES3cre+K3y@s3n/bucket/file";
    withBaseFile = parser.parseUri( context, fileName, uri );
    assertEquals( "ThiSiSA+PossibleAcce/ssK3y:PossiblES3cre+K3y@s3n", ( (S3NFileName)withBaseFile ).getBucketId() );
  }
}
