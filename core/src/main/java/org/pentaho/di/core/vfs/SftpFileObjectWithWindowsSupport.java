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

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.operations.FileOperations;
import org.apache.commons.vfs2.provider.sftp.SftpFileObject;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SftpFileObjectWithWindowsSupport implements FileObject {

  //icacls windows command permissions
  private static final String FULL_ACCESS = "(F)";
  private static final String MODIFY_ACCESS = "(M)";
  private static final String READ_AND_EXECUTE_ACCESS = "(RX)";
  private static final String READ_ACCESS = "(R)";
  private static final String WRITE_ACCESS = "(W)";
  private static final String WRITE_DATA_ADD_FILES_ACCESS = "WD";
  private static final String READ_DATA_ADD_FILES_ACCESS = "RD";

  private SftpFileObject sftpFileObject;
  private SftpFileSystemWindows sftpFileSystemWindows;
  private String path;


  public SftpFileObjectWithWindowsSupport( SftpFileObject sftpFileObject,
                                           SftpFileSystemWindows sftpFileSystemWindows ) throws FileSystemException {
    this.sftpFileObject = sftpFileObject;
    this.path = sftpFileObject.getName().getPath();
    this.sftpFileSystemWindows = sftpFileSystemWindows;
  }


  @Override
  public boolean isReadable() throws FileSystemException {
    try {
      if ( !this.sftpFileSystemWindows.isRemoteHostWindows() ) {
        return sftpFileObject.isReadable();
      } else {
        return this.exists() && this.doIsReadable();
      }

    } catch ( Exception var ) {
      throw new FileSystemException( "vfs.provider/check-is-readable.error", sftpFileObject.getName(), var );
    }
  }

  private boolean doIsReadable() throws Exception {
    boolean readable;
    List<String> userGroups = this.sftpFileSystemWindows.getUserGroups();
    Map<String, String> filePermissions = this.sftpFileSystemWindows.getFilePermission( this.path );

    for ( String group : userGroups ) {
      String acl = filePermissions.get( group );
      if ( acl != null ) {
        readable = acl.contains( FULL_ACCESS ) || acl.contains( MODIFY_ACCESS )
                || acl.contains( READ_AND_EXECUTE_ACCESS ) || acl.contains( READ_ACCESS )
                || acl.contains( WRITE_ACCESS ) || acl.contains( WRITE_DATA_ADD_FILES_ACCESS )
                || acl.contains( READ_DATA_ADD_FILES_ACCESS );
        if ( readable ) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean isWriteable() throws FileSystemException {
    try {
      if ( !this.sftpFileSystemWindows.isRemoteHostWindows() ) {
        return sftpFileObject.isWriteable();
      } else {
        return this.exists() && this.doIsWriteable();
      }

    } catch ( Exception var ) {
      throw new FileSystemException( "vfs.provider/check-is-writeable.error", sftpFileObject.getName(), var );
    }
  }

  protected boolean doIsWriteable() throws Exception {
    boolean writeable;
    List<String> userGroups = this.sftpFileSystemWindows.getUserGroups();
    Map<String, String> filePermissions = this.sftpFileSystemWindows.getFilePermission( this.path );

    for ( String group : userGroups ) {
      String acl = filePermissions.get( group );
      if ( acl != null ) {
        writeable = acl.contains( FULL_ACCESS ) || acl.contains( MODIFY_ACCESS )
                || acl.contains( WRITE_ACCESS ) || acl.contains( WRITE_DATA_ADD_FILES_ACCESS );
        if ( writeable ) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean canRenameTo( FileObject fileObject ) {
    return sftpFileObject.canRenameTo( fileObject );
  }

  @Override
  public void close() throws FileSystemException {
    sftpFileObject.close();
  }

  @Override
  public void copyFrom( FileObject fileObject, FileSelector fileSelector ) throws FileSystemException {
    sftpFileObject.copyFrom( fileObject, fileSelector );
  }

  @Override
  public void createFile() throws FileSystemException {
    sftpFileObject.createFile();
  }

  @Override
  public void createFolder() throws FileSystemException {
    sftpFileObject.createFolder();
  }

  @Override
  public boolean delete() throws FileSystemException {
    return sftpFileObject.delete();
  }

  @Override
  public int delete( FileSelector fileSelector ) throws FileSystemException {
    return sftpFileObject.delete( fileSelector );
  }

  @Override
  public int deleteAll() throws FileSystemException {
    return sftpFileObject.deleteAll();
  }

  @Override
  public boolean exists() throws FileSystemException {
    return sftpFileObject.exists();
  }

  @Override
  public FileObject[] findFiles( FileSelector fileSelector ) throws FileSystemException {
    return sftpFileObject.findFiles( fileSelector );
  }

  @Override
  public void findFiles( FileSelector fileSelector, boolean b, List<FileObject> list ) throws FileSystemException {
    sftpFileObject.findFiles( fileSelector, b, list );
  }

  @Override
  public FileObject getChild( String s ) throws FileSystemException {
    return sftpFileObject.getChild( s );
  }

  @Override
  public FileObject[] getChildren() throws FileSystemException {
    return sftpFileObject.getChildren();
  }

  @Override
  public FileContent getContent() throws FileSystemException {
    return sftpFileObject.getContent();
  }

  @Override
  public FileOperations getFileOperations() throws FileSystemException {
    return sftpFileObject.getFileOperations();
  }

  @Override
  public FileSystem getFileSystem() {
    return this.sftpFileSystemWindows;
  }

  @Override
  public FileName getName() {
    return sftpFileObject.getName();
  }

  @Override
  public FileObject getParent() throws FileSystemException {
    return sftpFileObject.getParent();
  }

  @Override
  public String getPublicURIString() {
    return sftpFileObject.getPublicURIString();
  }

  @Override
  public FileType getType() throws FileSystemException {
    return sftpFileObject.getType();
  }

  @Override
  public URL getURL() throws FileSystemException {
    return sftpFileObject.getURL();
  }

  @Override
  public boolean isAttached() {
    return sftpFileObject.isAttached();
  }

  @Override
  public boolean isContentOpen() {
    return sftpFileObject.isContentOpen();
  }

  @Override
  public boolean isExecutable() throws FileSystemException {
    return sftpFileObject.isExecutable();
  }

  @Override
  public boolean isFile() throws FileSystemException {
    return sftpFileObject.isFile();
  }

  @Override
  public boolean isFolder() throws FileSystemException {
    return sftpFileObject.isFolder();
  }

  @Override
  public boolean isHidden() throws FileSystemException {
    return sftpFileObject.isHidden();
  }


  @Override
  public void moveTo( FileObject fileObject ) throws FileSystemException {
    sftpFileObject.moveTo( fileObject );
  }

  @Override
  public void refresh() throws FileSystemException {
    sftpFileObject.refresh();
  }

  @Override
  public FileObject resolveFile( String s ) throws FileSystemException {
    return sftpFileObject.resolveFile( s );
  }

  @Override
  public FileObject resolveFile( String s, NameScope nameScope ) throws FileSystemException {
    return sftpFileObject.resolveFile( s, nameScope );
  }

  @Override
  public boolean setExecutable( boolean b, boolean b1 ) throws FileSystemException {
    return sftpFileObject.setExecutable( b, b1 );
  }

  @Override
  public boolean setReadable( boolean b, boolean b1 ) throws FileSystemException {
    return sftpFileObject.setReadable( b, b1 );
  }

  @Override
  public boolean setWritable( boolean b, boolean b1 ) throws FileSystemException {
    return sftpFileObject.setWritable( b, b1 );
  }

  @Override
  public int compareTo( FileObject o ) {
    return sftpFileObject.compareTo( o );
  }

  @Override
  public Iterator<FileObject> iterator() {
    return sftpFileObject.iterator();
  }

}
