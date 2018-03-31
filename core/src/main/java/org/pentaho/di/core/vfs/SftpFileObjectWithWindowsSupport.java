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

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.sftp.SftpFileObject;

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
}
