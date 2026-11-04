/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.s3.vfs;

import org.apache.commons.vfs2.FileType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * created by: rfellows date:       05/17/2012
 */
class S3FileNameTest {

  static final String SCHEME = "s3";
  static final String SCHEME_DELIMITER = "://";

  private static final String BUCKET_ID = "FooBucket";

  @Test
  void testGetURIWithoutBucket() {
    S3FileName fileName = new S3FileName( SCHEME, "", "", FileType.FOLDER );
    assertEquals( SCHEME + SCHEME_DELIMITER, fileName.getURI() );
  }

  @Test
  void testGetURIWithBucket() {
    S3FileName fileName = new S3FileName( SCHEME, BUCKET_ID, "", FileType.FOLDER );
    assertEquals( SCHEME + SCHEME_DELIMITER, fileName.getURI() );
  }

  @Test
  void testCreateNameWithoutBucket() {
    S3FileName fileName = new S3FileName( SCHEME, "", "", FileType.FOLDER );
    assertEquals( SCHEME + SCHEME_DELIMITER + "path/to/my/file",
      fileName.createName( "/path/to/my/file", FileType.FILE ).getURI() );
  }

  @Test
  void testCreateNameWithBucket() {
    S3FileName fileName = new S3FileName( SCHEME, BUCKET_ID, "", FileType.FOLDER );
    assertEquals( SCHEME + SCHEME_DELIMITER + "path/to/my/file",
      fileName.createName( "/path/to/my/file", FileType.FILE ).getURI() );
  }

  @Test
  void testAppendRootUriWithoutBucket() {
    S3FileName fileName = new S3FileName( SCHEME, "", "/FooFolder", FileType.FOLDER );
    assertEquals( SCHEME + SCHEME_DELIMITER + "FooFolder", fileName.getURI() );
  }

  @Test
  void testAppendRootUriWithBucket() {
    S3FileName fileName = new S3FileName( SCHEME, BUCKET_ID, "/FooBucket/FooFolder", FileType.FOLDER );
    assertEquals( SCHEME + SCHEME_DELIMITER + "FooBucket/FooFolder", fileName.getURI() );
  }
}
