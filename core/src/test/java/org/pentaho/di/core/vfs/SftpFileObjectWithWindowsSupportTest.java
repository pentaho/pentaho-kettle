/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.util.PosixPermissions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class SftpFileObjectWithWindowsSupportTest {

  private static final String PATH = "C/Test";
  private static final String USERS = "Users";
  private static final String PERMISSION_READ = "(R)";
  private static final String PERMISSION_WRITE = "(W)";

  @Test
  public void isReadableLinuxCase() throws Exception {
    FileObject fileObjectReadable = getLinuxFileObject( true, false );
    FileObject fileObjectNotReadable = getLinuxFileObject( false, false );

    assertTrue( fileObjectReadable.isReadable() );
    assertFalse( fileObjectNotReadable.isReadable() );
  }

  @Test
  public void isReadableWindowsCase() throws Exception {
    FileObject fileObjectReadable = getWindowsFileObject( true, false );
    FileObject fileObjectNotReadable = getWindowsFileObject( false, false );

    assertTrue( fileObjectReadable.isReadable() );
    assertFalse( fileObjectNotReadable.isReadable() );
  }

  @Test
  public void isWritableLinuxCase() throws Exception {
    FileObject fileObjectWritable = getLinuxFileObject( true, true );
    FileObject fileObjectNotWritable = getLinuxFileObject( true, false );

    assertTrue( fileObjectWritable.isWriteable() );
    assertFalse( fileObjectNotWritable.isWriteable() );
  }

  @Test
  public void isWritableWindowsCase() throws Exception {
    FileObject fileObjectWritable = getWindowsFileObject( true, true );
    FileObject fileObjectNotWritable = getWindowsFileObject( true, false );

    assertTrue( fileObjectWritable.isWriteable() );
    assertFalse( fileObjectNotWritable.isWriteable() );
  }

  private static FileObject getLinuxFileObject( boolean posixReadable, boolean posixWritable ) throws Exception {
    GenericFileName fileName = mock( GenericFileName.class );
    doReturn( PATH ).when( fileName ).getPath();
    SftpFileSystemWindows sftpFileSystem = spy( new SftpFileSystemWindows( fileName, null, null ) );
    doReturn( false ).when( sftpFileSystem ).isRemoteHostWindows();

    int permissions = 0;
    if ( posixReadable ) {
      permissions += 256;
    }
    if ( posixWritable ) {
      permissions += 128;
    }

    PosixPermissions posixPermissions = new PosixPermissions( permissions, true, true );
    return new SftpFileObjectWithWindowsSupport( fileName, sftpFileSystem ) {
      @Override
      public PosixPermissions getPermissions( boolean checkIds ) {
        return posixPermissions;
      }
      @Override
      public FileType getType() {
        return FileType.FILE;
      }
    };
  }

  private static FileObject getWindowsFileObject( boolean windowsReadable, boolean windowsWritable )
      throws Exception {
    GenericFileName fileName = mock( GenericFileName.class );
    doReturn( PATH ).when( fileName ).getPath();
    SftpFileSystemWindows sftpFileSystem = spy( new SftpFileSystemWindows( fileName, null, null ) );
    doReturn( true ).when( sftpFileSystem ).isRemoteHostWindows();

    List<String> groups = new ArrayList<>();
    groups.add( USERS );
    doReturn( groups ).when( sftpFileSystem ).getUserGroups();

    HashMap<String, String> permissions = new HashMap<>();
    doReturn( permissions ).when( sftpFileSystem ).getFilePermission( PATH );

    if ( windowsReadable ) {
      permissions.put( USERS, PERMISSION_READ );
    }
    if ( windowsWritable ) {
      permissions.put( USERS, PERMISSION_WRITE );
    }

    PosixPermissions posixPermissions = new PosixPermissions( 0, true, true );
    return new SftpFileObjectWithWindowsSupport( fileName, sftpFileSystem ) {
      @Override
      public PosixPermissions getPermissions( boolean checkIds ) {
        return posixPermissions;
      }
      @Override
      public FileType getType() {
        return FileType.FILE;
      }
    };
  }

}
