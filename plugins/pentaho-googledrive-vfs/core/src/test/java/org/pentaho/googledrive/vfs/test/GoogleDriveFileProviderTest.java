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


package org.pentaho.googledrive.vfs.test;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemOptions;
import org.junit.Test;
import org.pentaho.googledrive.vfs.GoogleDriveFileProvider;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.junit.Assert.assertTrue;

public class GoogleDriveFileProviderTest {

  private final String SCHEME = "googledrive";
  private static final String DISPLAY_NAME = "Google Drive";

  @Test public void testFileProvider() throws Exception {
    GoogleDriveFileProvider fileProvider = new GoogleDriveFileProvider();
    assertTrue( fileProvider.SCHEME.equals( SCHEME ) );
    assertTrue( fileProvider.DISPLAY_NAME.equals( DISPLAY_NAME ) );
    FileName fileName = mock( FileName.class );
    FileSystemOptions options = new FileSystemOptions();
    assertNotNull( fileProvider.doCreateFileSystem( fileName, options ) );
  }
}
