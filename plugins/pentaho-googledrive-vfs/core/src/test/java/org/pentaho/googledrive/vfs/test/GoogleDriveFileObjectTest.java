/*!
* Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
