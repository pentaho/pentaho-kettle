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


package org.pentaho.s3.vfs;

import org.apache.commons.vfs2.provider.FileNameParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * created by: rfellows date:       5/25/12
 */
public class S3FileNameParserIT {

  @Test
  public void testParseUri() throws Exception {
    FileNameParser parser = S3FileNameParser.getInstance();
    String origUri = "s3://fooBucket/rcf-emr-staging";
    S3FileName filename =
      (S3FileName) parser.parseUri( null, null, origUri );

    assertEquals( "fooBucket", filename.getBucketId() );

    assertEquals( origUri, filename.getURI() );
  }
}
