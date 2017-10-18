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


import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.provider.sftp.SftpFileObject;

import java.util.HashMap;

public class SftpFileSystemWindowsProvider {
  private static HashMap<String, SftpFileSystemWindows> sftpFileSystemWindowsMap = new HashMap<>();

  public static SftpFileSystemWindows getSftpFileSystemWindows( SftpFileObject sftpFileObject ) throws FileSystemException {
    GenericFileName fileName;
    if ( sftpFileObject.isFolder() ) {
      fileName = (GenericFileName) sftpFileObject.getName();
    } else {
      fileName = (GenericFileName) sftpFileObject.getParent().getName();
    }
    SftpFileSystemWindows sftpFileSystemWindows = sftpFileSystemWindowsMap.get( fileName.toString() );
    if ( sftpFileSystemWindows == null ) {
      sftpFileSystemWindows = new SftpFileSystemWindows( fileName, null,
              sftpFileObject.getFileSystem().getFileSystemOptions() );
      sftpFileSystemWindowsMap.put( fileName.toString(), sftpFileSystemWindows );
    }
    return sftpFileSystemWindows;
  }

}
