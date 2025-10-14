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
import org.apache.commons.vfs2.FileSystemOptions;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;


/**
 * Unit tests for S3FileProvider
 */
public class S3NFileProviderTest {

  S3NFileProvider provider;

  @Before
  public void setUp() {
    provider = new S3NFileProvider();
  }

  @Test
  public void testDoCreateFileSystem() throws Exception {
    FileName fileName = mock( FileName.class );
    FileSystemOptions options = new FileSystemOptions();
    assertNotNull( provider.doCreateFileSystem( fileName, options ) );
  }

}
