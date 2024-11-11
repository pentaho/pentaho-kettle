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


package org.pentaho.di.core.vfs;

import com.jcraft.jsch.Session;
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
    Session session = mock( Session.class );
    SftpFileSystemWindows sftpFileSystem = spy( new SftpFileSystemWindows( fileName, session, null ) );
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
    Session session = mock( Session.class );
    doReturn( PATH ).when( fileName ).getPath();
    SftpFileSystemWindows sftpFileSystem = spy( new SftpFileSystemWindows( fileName, session, null ) );
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
