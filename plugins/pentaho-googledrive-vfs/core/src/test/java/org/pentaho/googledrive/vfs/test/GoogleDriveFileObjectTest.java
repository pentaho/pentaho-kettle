/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.googledrive.vfs.test;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.pentaho.googledrive.vfs.GoogleDriveFileObject;

public class GoogleDriveFileObjectTest {

  private final String FOLDER = "googledrive://documents_folder";

  @Test public void testFileObject() throws Exception {
    FileSystemManager manager = mock( FileSystemManager.class );
    GoogleDriveFileObject fileObjectMock = mock( GoogleDriveFileObject.class );
    when( manager.resolveFile( FOLDER ) ).thenReturn( fileObjectMock );
    when( fileObjectMock.isFolder() ).thenReturn( true );
    when( fileObjectMock.exists() ).thenReturn( true );
    when( fileObjectMock.delete() ).thenReturn( true );
    FileObject fileObject = manager.resolveFile( FOLDER );
    fileObject.createFolder();
    assertTrue( fileObject.isFolder() );
    assertTrue( fileObject.exists() );
    assertTrue( fileObject.delete() );
    assertNull( fileObject.getChildren() );
  }
}
