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


import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.provider.sftp.SftpFileProvider;

/**
 * This class serves two main purposes.
 * <ol>
 *     <li>
 *         To provide a correct way of running sftp commands on Windows server.
 *         Current implementation of commons-vfs calls <code>id -G</code> or <code>id -u</code>,
 *         which is incorrect for Windows.
 *     </li>
 *     <li>
 *         To provide a way to release sftp connections, when we're done.
 *         commons-vfs says we do not close fileSystemManager until application shutdown
 *         (see <a href="https://issues.apache.org/jira/browse/VFS-454">VFS-454</a>).
 *         Thus, we need to close the connections.<br/>
 *         There is {@link org.apache.commons.vfs2.provider.AbstractFileProvider#freeUnusedResources()},
 *         which we can use to achieve this. But, unfortunately, original SftpFileSystem only releases them
 *         when all file objects it's associated with are garbage collected. This is too restrictive and inconvenient.
 *         Here we make the sftp file system object releasable when there are open file objects,
 *         we only need i/o streams to be closed. <br/>
 *         This is pretty safe to do. Even a file object would want to open a stream once again (currently wouldn't),
 *         the file system object re-creates session and opens a new connection.
 *     </li>
 * </ol>
 * This provider replaces {@link SftpFileProvider} in FileSystemManager (see overridden providers.xml).
 * And then used to spawn SftpFileSystemWindows and SftpFileObjectWithWindowsSupport.
 */
public class SftpFileSystemWindowsProvider extends SftpFileProvider {

  @Override
  protected FileSystem doCreateFileSystem( FileName name, FileSystemOptions fileSystemOptions ) {
    return new SftpFileSystemWindows( (GenericFileName) name, null, fileSystemOptions );
  }

}
