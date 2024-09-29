/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.sftp.SftpFileObject;
import org.apache.commons.vfs2.util.PosixPermissions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

class SftpFileObjectWithWindowsSupport extends SftpFileObject {

  //icacls windows command permissions
  private static final String FULL_ACCESS = "(F)";
  private static final String MODIFY_ACCESS = "(M)";
  private static final String READ_AND_EXECUTE_ACCESS = "(RX)";
  private static final String READ_ACCESS = "(R)";
  private static final String WRITE_ACCESS = "(W)";
  private static final String WRITE_DATA_ADD_FILES_ACCESS = "WD";
  private static final String READ_DATA_ADD_FILES_ACCESS = "RD";

  private String path;

  SftpFileObjectWithWindowsSupport( AbstractFileName name, SftpFileSystemWindows fileSystem )
    throws FileSystemException {
    super( name, fileSystem );
    this.path = name.getPath();
  }

  @Override
  protected boolean doIsReadable() throws Exception {
    SftpFileSystemWindows fileSystem = (SftpFileSystemWindows) getAbstractFileSystem();
    if ( !fileSystem.isRemoteHostWindows() ) {
      return super.doIsReadable();
    } else {
      List<String> userGroups = fileSystem.getUserGroups();
      Map<String, String> filePermissions = fileSystem.getFilePermission( this.path );

      for ( String group : userGroups ) {
        String acl = filePermissions.get( group );
        if ( acl != null ) {
          return acl.contains( FULL_ACCESS ) || acl.contains( MODIFY_ACCESS )
            || acl.contains( READ_AND_EXECUTE_ACCESS ) || acl.contains( READ_ACCESS )
            || acl.contains( WRITE_ACCESS ) || acl.contains( WRITE_DATA_ADD_FILES_ACCESS )
            || acl.contains( READ_DATA_ADD_FILES_ACCESS );
        }
      }
      return false;
    }
  }

  @Override
  protected boolean doIsWriteable() throws Exception {
    SftpFileSystemWindows fileSystem = (SftpFileSystemWindows) getAbstractFileSystem();
    if ( !fileSystem.isRemoteHostWindows() ) {
      return super.doIsWriteable();
    } else {
      List<String> userGroups = fileSystem.getUserGroups();
      Map<String, String> filePermissions = fileSystem.getFilePermission( this.path );

      for ( String group : userGroups ) {
        String acl = filePermissions.get( group );
        if ( acl != null ) {
          return acl.contains( FULL_ACCESS ) || acl.contains( MODIFY_ACCESS )
            || acl.contains( WRITE_ACCESS ) || acl.contains( WRITE_DATA_ADD_FILES_ACCESS );
        }
      }
      return false;
    }
  }

  /**
   * <p>In case it's impossible to determine the actual permissions, returns an "always-true" permission.</p>
   *
   * @param checkIds
   * @return
   * @throws Exception
   */
  @Override
  protected PosixPermissions getPermissions( boolean checkIds ) throws Exception {
    if ( checkIds && ( getAbstractFileSystem() instanceof SftpFileSystemWindows )
      && ( (SftpFileSystemWindows) getAbstractFileSystem() ).isExecDisabled() ) {
      // Exec is disabled, so we won't be able to ascertain the current user's UID and GID.
      // Return "always-true" permissions as a workaround, knowing that the SFTP server won't
      // let us perform unauthorized actions anyway.
      return new PretendUserIsOwnerPosixPermissions();
    }

    return super.getPermissions( checkIds );
  }

  /**
   * <p>Pretends that the current user is always the owner and in the same group.</p>
   */
  public class PretendUserIsOwnerPosixPermissions extends PosixPermissions {
    public PretendUserIsOwnerPosixPermissions() {
      super( PosixPermissions.Type.UserReadable.getMask() + PosixPermissions.Type.UserWritable.getMask()
          + PosixPermissions.Type.UserExecutable.getMask() + PosixPermissions.Type.GroupReadable.getMask()
          + PosixPermissions.Type.GroupWritable.getMask() + PosixPermissions.Type.GroupExecutable.getMask()
          + PosixPermissions.Type.OtherReadable.getMask() + PosixPermissions.Type.OtherWritable.getMask()
          + PosixPermissions.Type.OtherExecutable.getMask(),
        true, true );
    }
  }

  @Override protected InputStream doGetInputStream( int bufferSize ) throws Exception {
    synchronized ( this.getFileSystem() ) {
      return new CountedDelegatingInputStream( super.doGetInputStream( bufferSize ) );
    }
  }

  @Override protected OutputStream doGetOutputStream( boolean bAppend ) throws Exception {
    synchronized ( this.getFileSystem() ) {
      return new CountedDelegatingOutputStream( super.doGetOutputStream( bAppend ) );
    }
  }

  class CountedDelegatingInputStream extends InputStream {

    InputStream delegate;

    public CountedDelegatingInputStream( InputStream in ) {
      ( (SftpFileSystemWindows) getAbstractFileSystem() ).sftpBufferOpened();
      delegate = in;
    }

    public CountedDelegatingInputStream() {
      super();
    }

    @Override public int read( byte[] b ) throws IOException {
      return delegate.read( b );
    }

    @Override public int read( byte[] b, int off, int len ) throws IOException {
      return delegate.read( b, off, len );
    }

    @Override public long skip( long n ) throws IOException {
      return delegate.skip( n );
    }

    @Override public int available() throws IOException {
      return delegate.available();
    }

    @Override public void close() throws IOException {
      delegate.close();
      ( (SftpFileSystemWindows) getAbstractFileSystem() ).sftpBufferOpened();
    }

    @Override public synchronized void mark( int readlimit ) {
      delegate.mark( readlimit );
    }

    @Override public synchronized void reset() throws IOException {
      delegate.reset();
    }

    @Override public boolean markSupported() {
      return delegate.markSupported();
    }

    @Override
    public int read() throws IOException {
      return delegate.read();
    }
  }

  class CountedDelegatingOutputStream extends OutputStream {

    private OutputStream delegate;

    public CountedDelegatingOutputStream() {
      super();
    }

    public CountedDelegatingOutputStream( OutputStream out ) {
      ( (SftpFileSystemWindows) getAbstractFileSystem() ).sftpBufferOpened();
      delegate = out;
    }

    @Override
    public void write( int b ) throws IOException {
      delegate.write( b );
    }

    @Override public void write( byte[] b ) throws IOException {
      delegate.write( b );
    }

    @Override public void write( byte[] b, int off, int len ) throws IOException {
      delegate.write( b, off, len );
    }

    @Override public void flush() throws IOException {
      delegate.flush();
    }

    @Override public void close() throws IOException {
      delegate.close();
      ( (SftpFileSystemWindows) getAbstractFileSystem() ).sftpBufferOpened();
    }
  }
}
