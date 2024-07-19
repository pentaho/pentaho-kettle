/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.connections.vfs.provider;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.VFSConnectionManagerHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResolvedConnectionFileObjectTest {
  ResolvedConnectionFileObject fileObject;
  AbstractFileObject<?> resolvedFileObject;

  @Before
  public void setup() {
    resolvedFileObject = mock( AbstractFileObject.class );

    ConnectionFileName rootFileName = new ConnectionFileName( null );
    FileSystemOptions fileSystemOptions = mock( FileSystemOptions.class );
    ConnectionManager manager = mock( ConnectionManager.class );
    VFSConnectionManagerHelper vfsManagerHelper = mock( VFSConnectionManagerHelper.class );

    ConnectionFileSystem connectionFileSystem =
      new ConnectionFileSystem( rootFileName, fileSystemOptions, manager, vfsManagerHelper );

    fileObject = new ResolvedConnectionFileObject(
      mock( ConnectionFileName.class ),
      connectionFileSystem,
      resolvedFileObject
    );
  }

  @Test
  public void testGetResolvedFileObject() {
    assertEquals( resolvedFileObject, fileObject.getResolvedFileObject() );
  }

  @Test
  public void testRequireResolvedFileObject() {
    assertEquals( resolvedFileObject, fileObject.requireResolvedFileObject() );
  }

  @Test
  public void testDelegatesGetType() throws FileSystemException {
    when( resolvedFileObject.getType() ).thenReturn( FileType.FILE );

    assertEquals( FileType.FILE, fileObject.getType() );

    when( resolvedFileObject.getType() ).thenReturn( FileType.FOLDER );

    assertEquals( FileType.FOLDER, fileObject.getType() );

    verify( resolvedFileObject, times( 2 ) ).getType();
  }

  @Test
  public void testDelegatesHoldObject() {
    Object strongRef = new Object();

    fileObject.holdObject( strongRef );

    verify( resolvedFileObject, times( 1 ) ).holdObject( strongRef );
  }

  @Test
  public void testDelegatesIsAttached() {
    when( resolvedFileObject.isAttached() ).thenReturn( true );

    assertTrue( fileObject.isAttached() );

    when( resolvedFileObject.isAttached() ).thenReturn( false );

    assertFalse( fileObject.isAttached() );

    verify( resolvedFileObject, times( 2 ) ).isAttached();
  }

  @Test
  public void testDelegatesIsReadable() throws FileSystemException {
    when( resolvedFileObject.isReadable() ).thenReturn( true );

    assertTrue( fileObject.isReadable() );

    when( resolvedFileObject.isReadable() ).thenReturn( false );

    assertFalse( fileObject.isReadable() );

    verify( resolvedFileObject, times( 2 ) ).isReadable();
  }

  @Test
  public void testDelegatesIsWritable() throws FileSystemException {
    when( resolvedFileObject.isWriteable() ).thenReturn( true );

    assertTrue( fileObject.isWriteable() );

    when( resolvedFileObject.isWriteable() ).thenReturn( false );

    assertFalse( fileObject.isWriteable() );

    verify( resolvedFileObject, times( 2 ) ).isWriteable();
  }

  @Test
  public void testDelegatesIsContentOpen() {
    when( resolvedFileObject.isContentOpen() ).thenReturn( true );

    assertTrue( fileObject.isContentOpen() );

    when( resolvedFileObject.isContentOpen() ).thenReturn( false );

    assertFalse( fileObject.isContentOpen() );

    verify( resolvedFileObject, times( 2 ) ).isContentOpen();
  }

  @Test
  public void testDelegatesIsExecutable() throws FileSystemException {
    when( resolvedFileObject.isExecutable() ).thenReturn( true );

    assertTrue( fileObject.isExecutable() );

    when( resolvedFileObject.isExecutable() ).thenReturn( false );

    assertFalse( fileObject.isExecutable() );

    verify( resolvedFileObject, times( 2 ) ).isExecutable();
  }

  @Test
  public void testDelegatesIsHidden() throws FileSystemException {
    when( resolvedFileObject.isHidden() ).thenReturn( true );

    assertTrue( fileObject.isHidden() );

    when( resolvedFileObject.isHidden() ).thenReturn( false );

    assertFalse( fileObject.isHidden() );

    verify( resolvedFileObject, times( 2 ) ).isHidden();
  }

  @Test
  public void testDelegatesCanRenameTo() {

    FileObject newFile = mock( FileObject.class );

    when( resolvedFileObject.canRenameTo( newFile ) ).thenReturn( true );

    assertTrue( fileObject.canRenameTo( newFile ) );

    when( resolvedFileObject.canRenameTo( newFile ) ).thenReturn( false );

    assertFalse( fileObject.canRenameTo( newFile ) );

    verify( resolvedFileObject, times( 2 ) ).canRenameTo( newFile );
  }

  @Test
  public void testGetAELSafeURIString() {

    when( resolvedFileObject.getPublicURIString() ).thenReturn( "s3://bucket" );

    assertEquals( "s3a://bucket", fileObject.getAELSafeURIString() );
  }
}
