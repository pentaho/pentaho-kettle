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


package org.pentaho.s3a.vfs;

import org.apache.commons.vfs2.FileType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class S3AFileNameTest {

  public static final String SCHEME = "s3a";
  public static final String SCHEME_DELIMITER = "://";

  private static final String BUCKET_ID = "FooBucket";

  @Test
  public void testGetURIWithoutBucket() {
    S3AFileName fileName = new S3AFileName( SCHEME, "", "", FileType.FOLDER );
    assertEquals( SCHEME + SCHEME_DELIMITER, fileName.getURI() );
  }

  @Test
  public void testGetURIWithBucket() {
    S3AFileName fileName = new S3AFileName( SCHEME, BUCKET_ID, "", FileType.FOLDER );
    assertEquals( SCHEME + SCHEME_DELIMITER, fileName.getURI() );
  }

  @Test
  public void testCreateNameWithoutBucket() {
    S3AFileName fileName = new S3AFileName( SCHEME, "", "", FileType.FOLDER );
    assertEquals( SCHEME + SCHEME_DELIMITER + "path/to/my/file",
      fileName.createName( "/path/to/my/file", FileType.FILE ).getURI() );
  }

  @Test
  public void testCreateNameWithBucket() {
    S3AFileName fileName = new S3AFileName( SCHEME, BUCKET_ID, "", FileType.FOLDER );
    assertEquals( SCHEME + SCHEME_DELIMITER + "path/to/my/file",
      fileName.createName( "/path/to/my/file", FileType.FILE ).getURI() );
  }

  @Test
  public void testAppendRootUriWithoutBucket() {
    S3AFileName fileName = new S3AFileName( SCHEME, "", "/FooFolder", FileType.FOLDER );
    assertEquals( SCHEME + SCHEME_DELIMITER + "FooFolder", fileName.getURI() );
  }

  @Test
  public void testAppendRootUriWithBucket() {
    S3AFileName fileName = new S3AFileName( SCHEME, BUCKET_ID, "/FooBucket/FooFolder", FileType.FOLDER );
    assertEquals( SCHEME + SCHEME_DELIMITER + "FooBucket/FooFolder", fileName.getURI() );
  }
}
