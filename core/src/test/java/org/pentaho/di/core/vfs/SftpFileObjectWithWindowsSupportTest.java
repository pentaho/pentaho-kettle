/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.provider.sftp.SftpFileObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SftpFileObjectWithWindowsSupportTest {

  @Test
  public void isReadableNormalCase() throws Exception {

    SftpFileObject baseFileObject = Mockito.mock( SftpFileObject.class );
    FileName fileName = Mockito.mock( FileName.class );
    Mockito.doReturn( "C/Test" ).when( fileName ).getPath();
    Mockito.doReturn( fileName ).when( baseFileObject ).getName();
    SftpFileSystemWindows sftpFileSystemWindows  = Mockito.mock( SftpFileSystemWindows.class );
    Mockito.doReturn( false ).when( sftpFileSystemWindows ).isRemoteHostWindows();
    SftpFileObjectWithWindowsSupport fileObject = new SftpFileObjectWithWindowsSupport( baseFileObject, sftpFileSystemWindows );
    fileObject.isReadable();
    Mockito.verify( baseFileObject, Mockito.times( 1 ) ).isReadable();
  }

  @Test
  public void isReadableWindowsCase() throws Exception {

    SftpFileObject baseFileObject = Mockito.mock( SftpFileObject.class );
    FileName fileName = Mockito.mock( FileName.class );
    String path = "C/Test";
    Mockito.doReturn( path ).when( fileName ).getPath();
    Mockito.doReturn( fileName ).when( baseFileObject ).getName();
    Mockito.doReturn( true ).when( baseFileObject ).exists();
    SftpFileSystemWindows sftpFileSystemWindows  = Mockito.mock( SftpFileSystemWindows.class );
    Mockito.doReturn( true ).when( sftpFileSystemWindows ).isRemoteHostWindows();

    List<String> groups = new ArrayList<>();
    String administrators = "Users";
    groups.add( administrators );
    Mockito.doReturn( groups ).when( sftpFileSystemWindows ).getUserGroups();

    HashMap<String, String> permissions = new HashMap<>();
    permissions.put( administrators, "(R)" );
    Mockito.doReturn( permissions ).when( sftpFileSystemWindows ).getFilePermission( path );

    SftpFileObjectWithWindowsSupport fileObject = new SftpFileObjectWithWindowsSupport( baseFileObject, sftpFileSystemWindows );
    boolean readable = fileObject.isReadable();
    Mockito.verify( baseFileObject, Mockito.never() ).isReadable();
    Assert.assertTrue( readable );
  }


  @Test
  public void isWriteableNormalCase() throws Exception {
    SftpFileObject baseFileObject = Mockito.mock( SftpFileObject.class );
    FileName fileName = Mockito.mock( FileName.class );
    Mockito.doReturn( "C/Test" ).when( fileName ).getPath();
    Mockito.doReturn( fileName ).when( baseFileObject ).getName();
    SftpFileSystemWindows sftpFileSystemWindows  = Mockito.mock( SftpFileSystemWindows.class );
    Mockito.doReturn( false ).when( sftpFileSystemWindows ).isRemoteHostWindows();
    SftpFileObjectWithWindowsSupport fileObject = new SftpFileObjectWithWindowsSupport( baseFileObject, sftpFileSystemWindows );
    fileObject.isWriteable();
    Mockito.verify( baseFileObject, Mockito.times( 1 ) ).isWriteable();
  }



  @Test
  public void isWriteableWindowsCase() throws Exception {

    SftpFileObject baseFileObject = Mockito.mock( SftpFileObject.class );
    FileName fileName = Mockito.mock( FileName.class );
    String path = "C/Test";
    Mockito.doReturn( path ).when( fileName ).getPath();
    Mockito.doReturn( fileName ).when( baseFileObject ).getName();
    Mockito.doReturn( true ).when( baseFileObject ).exists();
    SftpFileSystemWindows sftpFileSystemWindows  = Mockito.mock( SftpFileSystemWindows.class );
    Mockito.doReturn( true ).when( sftpFileSystemWindows ).isRemoteHostWindows();

    List<String> groups = new ArrayList<>();
    String administrators = "Users";
    groups.add( administrators );
    Mockito.doReturn( groups ).when( sftpFileSystemWindows ).getUserGroups();

    HashMap<String, String> permissions = new HashMap<>();
    permissions.put( administrators, "(W)" );
    Mockito.doReturn( permissions ).when( sftpFileSystemWindows ).getFilePermission( path );

    SftpFileObjectWithWindowsSupport fileObject = new SftpFileObjectWithWindowsSupport( baseFileObject, sftpFileSystemWindows );
    boolean isWriteable = fileObject.isWriteable();
    Mockito.verify( baseFileObject, Mockito.never() ).isWriteable();
    Assert.assertTrue( isWriteable );
  }

}
